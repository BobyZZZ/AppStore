package com.bb.googleplaybb.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bb.googleplaybb.BuildConfig;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Response;

import static com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.THREADCOUNT;

/**
 * Created by Boby on 2018/7/18.
 */

public class AppDownloadManager {
    private static AppDownloadManager downloadManager = new AppDownloadManager();

    public static final int STATE_UNDO = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_DOWNLOADING = 4;
    public static final int STATE_ERROR = 5;
    public static final int STATE_SUCCESS = 6;

    //2.观察者集合
    private ArrayList<DownloadObserver> mObserverList = new ArrayList<>();//观察者集合
    private ConcurrentHashMap<String, DownloadInfo> mDownloadInfoMap = new ConcurrentHashMap<>();//下载信息集合
    private ConcurrentHashMap<String, DownloadTask> mDownloadTaskMap = new ConcurrentHashMap<>();//下载任务集合
    //    private HashMap<String, DownloadTask> mDownloadTaskMap = new HashMap<>();//下载任务集合
//    private HashMap<String, DownloadInfo> mDownloadInfoMap = new HashMap<>();//下载信息集合

    private AppDownloadManager() {
    }

    public static AppDownloadManager getDownloadManager() {
        return downloadManager;
    }

    //通知所有观察者更新状态
    public synchronized void notifyDownloadStateChange(DownloadInfo downloadInfo) {
        for (int i = 0; i < mObserverList.size(); i++) {
            mObserverList.get(i).notifyDownloadStateChange(downloadInfo);
        }
    }

    //通知所有观察者更新进度
    public synchronized void notifyDownloadProgressChange(DownloadInfo downloadInfo) {
        for (int i = 0; i < mObserverList.size(); i++) {
            mObserverList.get(i).notifyDownloadProgressChange(downloadInfo);
        }
    }

    //下载方法
    public synchronized void download(AppInfo appInfo) {
        //首先从任务列表中获取下载信息
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        if (downloadInfo == null) {
            //下载信息为空则新创建一个下载信息
            downloadInfo = DownloadInfo.copy(appInfo);//状态默认为undo、进度为0
            //判断下载信息对应本地的文件是否存在,存在则修改downloadInfo的下载进度
            downloadInfo.mDownloadedSize = getDownloadedSize(downloadInfo);
        }
        downloadInfo.mCurrentState = STATE_WAITING;
        notifyDownloadStateChange(downloadInfo);
        //添加到下载信息集合
        mDownloadInfoMap.put(downloadInfo.id, downloadInfo);

        DownloadTask task = new DownloadTask(downloadInfo);
        mDownloadTaskMap.put(downloadInfo.id, task);
        task.start();
    }

    public static long getDownloadedSize(DownloadInfo downloadInfo) {
        File file = new File(downloadInfo.getFilePath());
        ArrayList<DownloadTask.ThreadInfo> threadInfosInDB = DBUtils.getInstance().getThreadInfo(downloadInfo.id);
        long downloadedSize = 0;
        if (file.exists()) {
            if (file.length() == downloadInfo.size) {
                downloadedSize = file.length();
            } else if (threadInfosInDB.size() == THREADCOUNT) {
                for (DownloadTask.ThreadInfo in : threadInfosInDB) {
                    downloadedSize += in.mFinished;
                }
            } else {
                ArrayList<DownloadTask.ThreadInfo> threadList = downloadInfo.getThreadList();
                for (DownloadTask.ThreadInfo info : threadList) {
                    int count = 0;
                    for (DownloadTask.ThreadInfo in : threadInfosInDB) {
                        if (info.threadId.equals(in.threadId)) {
                            downloadedSize += in.mFinished;
                            break;
                        }
                        count++;
                    }
                    if (count == threadInfosInDB.size()) {
                        downloadedSize += info.size;
                    }
                }
            }
            Log.e("zyc", "fileDownloadedSize: " + downloadedSize);
        }
        return downloadedSize;
    }

    public static class DownloadTask {
        public static final int THREADCOUNT = 3;
        private DownloadInfo mDownloadInfo;

        public DownloadTask(DownloadInfo downloadInfo) {
            mDownloadInfo = downloadInfo;
        }

        public void start() {
            DBUtils dbUtils = DBUtils.getInstance();
            if (mDownloadInfo.isThreadsEmpty()) {
                //首次打开应用时为空，查询数据库
                ArrayList<ThreadInfo> list = dbUtils.getThreadInfo(mDownloadInfo.id);
                if (!list.isEmpty()) {
                    mDownloadInfo.addThreads(list);
                }
            }

            if (mDownloadInfo.isThreadsEmpty()) {
                //之前没有下载过应用,创建线程信息
                mDownloadInfo.initThread();
                //插入数据库
                for (ThreadInfo threadInfo : mDownloadInfo.mThreads) {
                    dbUtils.addThreadInfo(threadInfo);
                }
            }

            //在线程池中执行
            for (ThreadInfo threadInfo : mDownloadInfo.mThreads) {
                ThreadManager.getThreadPool().execute(threadInfo);
            }
        }

        public void stop() {
            if (mDownloadInfo.mThreads != null && !mDownloadInfo.mThreads.isEmpty()) {
                for (int i = 0; i < mDownloadInfo.mThreads.size(); i++) {
                    ThreadManager.getThreadPool().cancel(mDownloadInfo.mThreads.get(i));
                }
            }
        }

        public static class ThreadInfo implements Runnable {
            public String id;//应用id
            public String threadId;//应用id
            public long mFinished;
            public long startIndex;
            public long endIndex;
            public long size;
            public DownloadInfo mDownloadInfo;
            public final String TAG = "zycThreadInfo";

            public ThreadInfo(String id, long size, long startIndex, long endIndex, long finished) {
                this.id = id;
                this.size = size;
                this.threadId = id + startIndex;
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.mFinished = finished;
            }

            @Override
            public void run() {
                //开始下载
                long totalTime = System.currentTimeMillis();
                DBUtils dbUtils = DBUtils.getInstance();
                if (mDownloadInfo.mCurrentState != STATE_DOWNLOADING) {
                    mDownloadInfo.mCurrentState = STATE_DOWNLOADING;
                    downloadManager.notifyDownloadStateChange(mDownloadInfo);
                }

                //断点
                long start = startIndex + mFinished;
                Log.e(TAG, "size:" + size + "---range: " + startIndex + "---" + endIndex + "---start:" + start);
                Response response = NetHelper.download(NetHelper.URL + mDownloadInfo.downloadUrl, start, endIndex);
                try {
                    if (response != null && response.isSuccessful()) {
                        File file = new File(mDownloadInfo.path);
                        InputStream in = response.body().byteStream();
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        randomAccessFile.seek(start);
                        int len = -1;
                        byte[] buffer = new byte[2048];
                        String TAG = "time";
                        //每300ms更新一次数据库断点记录
                        long timeRecord = System.currentTimeMillis();
                        while ((len = in.read(buffer)) != -1 && mDownloadInfo.mCurrentState == STATE_DOWNLOADING) {
                            long oneTime = System.currentTimeMillis();
                            randomAccessFile.write(buffer, 0, len);
                            mDownloadInfo.mDownloadedSize += len;
                            mFinished += len;

                            long updateThreadInfoTime = System.currentTimeMillis();
                            if (updateThreadInfoTime - timeRecord >= 300) {
                                dbUtils.updateThreadInfo(threadId, mFinished);
                                timeRecord = updateThreadInfoTime;
                                Log.e(TAG, "updateThreadInfoTime:"  + (System.currentTimeMillis() - updateThreadInfoTime));
                            }

                            Log.e(TAG, "oneTime:"  + (System.currentTimeMillis() - oneTime));
                            downloadManager.notifyDownloadProgressChange(mDownloadInfo);
                        }
                        if (mFinished == size) {
                            //单个下载成功，从数据库中删除记录
                            dbUtils.deleteThreadInfo(threadId);
                            mDownloadInfo.mFinishedCount++;
                            mDownloadInfo.removeThread(this);
                        }

                        Log.e(TAG, "id:" + mDownloadInfo.id + "---mFinishedCount: " + mDownloadInfo.mFinishedCount + "---" + file.length());
                        if (mDownloadInfo.mFinishedCount == THREADCOUNT && file.length() == mDownloadInfo.size) {
                            //下载成功
                            Log.e(TAG, "totalTime: " +(System.currentTimeMillis() - totalTime));
                            mDownloadInfo.mCurrentState = STATE_SUCCESS;
                            downloadManager.notifyDownloadStateChange(mDownloadInfo);
                            dbUtils.deleteThreadInfoById(id);
                        } else if (mDownloadInfo.mCurrentState == STATE_PAUSE) {
                            //更新数据库中记录
                            downloadManager.notifyDownloadStateChange(mDownloadInfo);
                        } else if (mDownloadInfo.mCurrentState != STATE_DOWNLOADING) {
                            //下载失败,删除记录，删除文件
                            if (file.exists()) {
                                file.delete();
                            }
                            dbUtils.deleteThreadInfoById(id);
                            downloadManager.mDownloadTaskMap.remove(mDownloadInfo.id);

                            mDownloadInfo.mCurrentState = STATE_ERROR;
                            mDownloadInfo.mDownloadedSize = 0;
                            downloadManager.notifyDownloadStateChange(mDownloadInfo);
                        }
                    } else {
                        //下载失败,
                        DBUtils.getInstance().deleteThreadInfoById(id);
                        downloadManager.mDownloadTaskMap.remove(mDownloadInfo.id);

                        mDownloadInfo.mCurrentState = STATE_ERROR;
                        mDownloadInfo.mDownloadedSize = 0;
                        downloadManager.notifyDownloadStateChange(mDownloadInfo);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.body().close();
                        response.close();
                    }
                }

                //下载完成后从任务集合中删除
                if (mDownloadInfo.mFinishedCount == 3) {
                    downloadManager.mDownloadTaskMap.remove(mDownloadInfo.id);
                }
            }

            @Override
            public String toString() {
                return "ThreadInfo{" +
                        "id='" + id + '\'' +
                        ",threadId='" + threadId + '\'' +
                        ", mFinished=" + mFinished +
                        ", startIndex=" + startIndex +
                        ", endIndex=" + endIndex +
                        ", size=" + size +
                        '}';
            }

            public void setDownloadInfo(DownloadInfo downloadInfo) {
                mDownloadInfo = downloadInfo;
            }
        }
    }

    //安装
    public synchronized void install(AppInfo appInfo) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        if (downloadInfo == null) {
            downloadInfo = DownloadInfo.copy(appInfo);
        }
        if (downloadInfo != null) {
            // 跳到系统的安装页面进行安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri;
            //判断是否是安卓7.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(UIUtils.getContext(),
                        BuildConfig.APPLICATION_ID + ".fileProvider",
                        new File(downloadInfo.getFilePath()));
            } else {
                uri = Uri.parse("file://" + downloadInfo.getFilePath());
//                uri = Uri.fromFile(new File(downloadInfo.getFilePath()));
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            UIUtils.getContext().startActivity(intent);
        }
    }

    //暂停
    public synchronized void pause(AppInfo appInfo) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        if (downloadInfo != null) {
            if (downloadInfo.mCurrentState == STATE_DOWNLOADING || downloadInfo.mCurrentState == STATE_WAITING) {
                DownloadTask task = mDownloadTaskMap.get(downloadInfo.id);
                if (task != null) {
                    task.stop();
                }

                downloadInfo.mCurrentState = STATE_PAUSE;
                notifyDownloadStateChange(downloadInfo);
            }
        }
    }

//    //下载方法
//    public synchronized void download(AppInfo appInfo) {
//        //首先从任务列表中获取下载信息
//        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
//        if (downloadInfo == null) {
//            //下载信息为空则新创建一个下载信息
//            downloadInfo = DownloadInfo.copy(appInfo);//状态默认为undo、进度为0
//            //判断下载信息对应本地的文件是否存在,存在则修改downloadInfo的下载进度
//            File file = new File(downloadInfo.getFilePath());
//            if (file.exists()) {
//                downloadInfo.mDownloadedSize = file.length();
//            }
//        }
//        downloadInfo.mCurrentState = STATE_WAITING;
//        notifyDownloadStateChange(downloadInfo);
//        //添加到下载信息集合
//        mDownloadInfoMap.put(downloadInfo.id, downloadInfo);
//
//        DownloadTask task = new DownloadTask(downloadInfo);
//        mDownloadTaskMap.put(downloadInfo.id, task);
//        ThreadManager.getThreadPool().execute(task);
//    }
//
//    //下载任务，在线程池中运行
//    public class DownloadTask implements Runnable {
//        private DownloadInfo downloadInfo;
//
//        public DownloadTask(DownloadInfo downloadInfo) {
//            this.downloadInfo = downloadInfo;
//        }
//
//        @Override
//        public void run() {
//            //开始下载
//            System.out.println("开始下载拉");
//            downloadInfo.mCurrentState = STATE_DOWNLOADING;
//            notifyDownloadStateChange(downloadInfo);
//
//            System.out.println("downloadInfo.path:" + downloadInfo.path);
//            File file = new File(downloadInfo.path);
//
//            OkHttpClient client = new OkHttpClient();
//            Request request = null;
//            if (!file.exists() || file.length() != downloadInfo.mDownloadedSize || downloadInfo.mDownloadedSize == 0) {
//                //从头开始下载
//                System.out.println("从头开始下载.....");
//                file.delete();//文件的大小跟downloadInfo当前的进度不一致或者下载状态为失败，则删除文件，第一种情况文件不存在删除文件也不会报错
//                downloadInfo.mDownloadedSize = 0;
//                request = new Request.Builder().url(NetHelper.URL + downloadInfo.downloadUrl).build();
//            } else {
//                //断点续传
//                System.out.println("断点续传");
//                request = new Request.Builder().url(NetHelper.URL + downloadInfo.downloadUrl)
//                        .addHeader("RANGE", "bytes=" + file.length() + "-").build();
//            }
//            Response response = null;
//            try {
//                response = client.newCall(request).execute();
//                if (response != null && response.isSuccessful()) {
//                    InputStream in = response.body().byteStream();
////                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
////                    randomAccessFile.seek(file.length());
//                    FileOutputStream out = new FileOutputStream(file, true);
//                    int len = -1;
//                    byte[] buffer = new byte[2048];
//                    while ((len = in.read(buffer)) != -1 && downloadInfo.mCurrentState == STATE_DOWNLOADING) {
//                        out.write(buffer, 0, len);
//                        downloadInfo.mDownloadedSize += len;
//                        notifyDownloadProgressChange(downloadInfo);
//                    }
//                    out.flush();
//                    //走到此处情况可能为：下载完成、下载被暂停了、下载失败
//                    if (file.length() == downloadInfo.size) {
//                        //下载完成
//                        downloadInfo.mCurrentState = STATE_SUCCESS;
//                        notifyDownloadStateChange(downloadInfo);
//                    } else if (downloadInfo.mCurrentState == STATE_PAUSE) {
//                        notifyDownloadStateChange(downloadInfo);
//                    } else {
//                        //下载失败
//                        downloadInfo.mCurrentState = STATE_ERROR;
//                        downloadInfo.mDownloadedSize = 0;
//                        notifyDownloadStateChange(downloadInfo);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (response != null) {
//                    response.body().close();
//                    response.close();
//                }
//            }
//            //下载完成后从任务集合中删除
//            mDownloadTaskMap.remove(downloadInfo.id);
//        }
//    }

    //3.注册观察者
    public synchronized void registeredObserver(DownloadObserver observer) {
        if (observer != null && !mObserverList.contains(observer)) {
            mObserverList.add(observer);
        }
    }

    //4.删除观察者
    public synchronized void unregisteredObserver(DownloadObserver observer) {
        mObserverList.remove(observer);
    }

    //1.观察者接口
    public interface DownloadObserver {
        void notifyDownloadStateChange(DownloadInfo downloadInfo);

        void notifyDownloadProgressChange(DownloadInfo downloadInfo);
    }

    public DownloadInfo getDownloadInfo(AppInfo appInfo) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        return downloadInfo;
    }
}
