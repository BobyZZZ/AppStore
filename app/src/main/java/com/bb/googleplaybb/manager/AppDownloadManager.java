package com.bb.googleplaybb.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bb.googleplaybb.BuildConfig;
import com.bb.googleplaybb.broadcastReceiver.NotificationBroadcastReceiver;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.NotificationBroadcastHelper;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Response;

import static com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.THREADCOUNT;


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
    private LinkedHashMap<String, DownloadTask> mWaitingTask = new LinkedHashMap<>();

    private AppDownloadManager() {
        DBUtils dbUtils = DBUtils.getInstance();
        ArrayList<DownloadInfo> list = dbUtils.getAllDownloadInfo();
        for (DownloadInfo info : list) {
            info.mCurrentState = STATE_PAUSE;
            mDownloadInfoMap.put(info.id, info);
        }
    }

    public static AppDownloadManager getDownloadManager() {
        return downloadManager;
    }

    /*
    * 获取所有未下载完的任务
    * */
    public ArrayList<DownloadInfo> getAllUnfinishedDownloadTask() {
        ArrayList<DownloadInfo> list = new ArrayList<>();
        if (mDownloadInfoMap.size() > 0) {
            Iterator<Map.Entry<String, DownloadInfo>> it = mDownloadInfoMap.entrySet().iterator();
            while (it.hasNext()) {
                DownloadInfo downloadInfo = it.next().getValue();
                if (downloadInfo.mCurrentState != STATE_SUCCESS) {
                    list.add(downloadInfo);
                }
            }
        }
        return list;
    }

    //通知所有观察者更新状态
    public synchronized void notifyDownloadStateChange(DownloadInfo downloadInfo) {
        if (downloadInfo.mCurrentState == STATE_SUCCESS || downloadInfo.mCurrentState == STATE_PAUSE) {
            runNextTask();
        }
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
        DBUtils dbUtils = DBUtils.getInstance();
        //首先从任务列表中获取下载信息
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        if (downloadInfo == null) {
            //下载信息为空则新创建一个下载信息
            downloadInfo = DownloadInfo.copy(appInfo);//状态默认为undo、进度为0
            //判断下载信息对应本地的文件是否存在,存在则修改downloadInfo的下载进度
            dbUtils.addDownloadInfo(downloadInfo);
        }
        downloadInfo.mCurrentState = STATE_WAITING;
        notifyDownloadStateChange(downloadInfo);
        dbUtils.updateDownloadInfo(downloadInfo);
        //添加到下载信息集合
        mDownloadInfoMap.put(downloadInfo.id, downloadInfo);

        DownloadTask task = new DownloadTask(downloadInfo);

        //最多同时下载两个应用
        if (mDownloadTaskMap.size() >= 2) {
            mWaitingTask.put(downloadInfo.id, task);
        } else {
            mDownloadTaskMap.put(downloadInfo.id, task);
            task.start();
        }
    }

    public synchronized void download(DownloadInfo downloadInfo) {
        //首先从任务列表中获取下载信息
        DownloadInfo info = mDownloadInfoMap.get(downloadInfo.id);
        if (info != null) {
            info.mCurrentState = STATE_WAITING;
            notifyDownloadStateChange(info);
            //添加到下载信息集合
            mDownloadInfoMap.put(info.id, info);

            DownloadTask task = new DownloadTask(info);

            //最多同时下载两个应用
            if (mDownloadTaskMap.size() >= 2) {
                mWaitingTask.put(info.id, task);
            } else {
                mDownloadTaskMap.put(info.id, task);
                task.start();
            }
        }
    }

    public void runNextTask() {
        if (mWaitingTask.size() > 0) {
            Iterator<Map.Entry<String, DownloadTask>> iterator = mWaitingTask.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, DownloadTask> entry = iterator.next();
                String key = entry.getKey();
                DownloadTask task = entry.getValue();
                Log.i("runNextTask: ", "mWaitingTask.size: " + mWaitingTask.size());

                mDownloadTaskMap.put(key, task);
                task.start();

                mWaitingTask.remove(key);
                Log.i("runNextTask: ", "mWaitingTask.size: " + mWaitingTask.size());
            }

        }
    }

    /**
     * 获取应用已下载的大小
     *
     * @param downloadInfo
     * @return
     */
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
        }
        return downloadedSize;
    }

    public static class DownloadTask {
        public static final int THREADCOUNT = 3;
        public DownloadInfo mDownloadInfo;

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
                } else {
                    //之前没有下载过应用,创建线程信息
                    mDownloadInfo.initThread();
                    //插入数据库
                    for (ThreadInfo threadInfo : mDownloadInfo.mThreads) {
                        dbUtils.addThreadInfo(threadInfo);
                    }
                }
            }

            //在线程池中执行
            for (ThreadInfo threadInfo : mDownloadInfo.mThreads) {
                ThreadManager.getThreadPool().execute(threadInfo);
            }

            //开始下载，发送广播，显示notification
            NotificationBroadcastHelper.send(NotifycationHelper.ACTION_NOTIFY, mDownloadInfo.id);
//            NotifycationHelper.getInstance().notify(mDownloadInfo);
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
                        //每300ms更新一次数据库记录
                        long timeRecord = System.currentTimeMillis();
                        while ((len = in.read(buffer)) != -1 && mDownloadInfo.mCurrentState == STATE_DOWNLOADING) {
                            long oneTime = System.currentTimeMillis();
                            randomAccessFile.write(buffer, 0, len);
                            mDownloadInfo.mDownloadedSize += len;
                            mFinished += len;

                            long updateThreadInfoTime = System.currentTimeMillis();
                            if (updateThreadInfoTime - timeRecord >= 300) {
                                dbUtils.update(mDownloadInfo, threadId, mFinished);
                                timeRecord = updateThreadInfoTime;
                                Log.e(TAG, "updateDBTime:" + (System.currentTimeMillis() - updateThreadInfoTime));

                            }

                            Log.e(TAG, "oneTimeIO:" + (System.currentTimeMillis() - oneTime));
                            downloadManager.notifyDownloadProgressChange(mDownloadInfo);
                            NotificationBroadcastHelper.send(NotifycationHelper.ACTION_NOTIFY, id);
//                            NotifycationHelper.getInstance().notify(mDownloadInfo);
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
                            Log.e(TAG, "totalTime: " + (System.currentTimeMillis() - totalTime));
                            mDownloadInfo.mCurrentState = STATE_SUCCESS;
                            downloadManager.notifyDownloadStateChange(mDownloadInfo);
                            dbUtils.delete(id);
                        } else if (mDownloadInfo.mCurrentState == STATE_PAUSE) {
                            //更新数据库中记录
                            dbUtils.update(mDownloadInfo, threadId, mFinished);
                            if (++mDownloadInfo.mPauseCount == THREADCOUNT - mDownloadInfo.mFinishedCount) {
                                downloadManager.notifyDownloadStateChange(mDownloadInfo);
                                mDownloadInfo.mPauseCount = 0;
                            }
                        } else if (mDownloadInfo.mCurrentState != STATE_DOWNLOADING) {
                            //下载失败,删除记录，删除文件
                            if (file.exists()) {
                                file.delete();
                            }
                            dbUtils.delete(id);
                            downloadManager.mDownloadTaskMap.remove(mDownloadInfo.id);

                            mDownloadInfo.mCurrentState = STATE_ERROR;
                            mDownloadInfo.mDownloadedSize = 0;
                            downloadManager.notifyDownloadStateChange(mDownloadInfo);
                        }
                    } else {
                        //下载失败,
                        dbUtils.delete(id);
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

    public synchronized void delete(DownloadInfo info) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(info.id);
        if (downloadInfo != null) {
            DownloadTask task = mDownloadTaskMap.get(downloadInfo.id);
            if (task != null) {
                task.stop();
                mDownloadTaskMap.remove(downloadInfo.id);
            } else {
                task = mWaitingTask.get(downloadInfo.id);
                if (task != null) {
                    task.stop();
                    mWaitingTask.remove(downloadInfo.id);
                }
            }

            mDownloadInfoMap.remove(info.id);
            //删除本地文件
            File file = new File(downloadInfo.getFilePath());
            //下载失败,删除记录，删除文件
            if (file.exists()) {
                file.delete();
            }
            DBUtils.getInstance().delete(downloadInfo.id);
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
                    mDownloadTaskMap.remove(downloadInfo.id);
                } else {
                    task = mWaitingTask.get(downloadInfo.id);
                    if (task != null) {
                        task.stop();
                        mWaitingTask.remove(downloadInfo.id);
                    }
                }

                downloadInfo.mCurrentState = STATE_PAUSE;
                notifyDownloadStateChange(downloadInfo);
            }
        }
    }

    public synchronized void pause(DownloadInfo info) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(info.id);
        if (downloadInfo != null) {
            if (downloadInfo.mCurrentState == STATE_DOWNLOADING || downloadInfo.mCurrentState == STATE_WAITING) {
                DownloadTask task = mDownloadTaskMap.get(downloadInfo.id);
                if (task != null) {
                    task.stop();
                    mDownloadTaskMap.remove(downloadInfo.id);
                } else {
                    task = mWaitingTask.get(downloadInfo.id);
                    if (task != null) {
                        task.stop();
                        mWaitingTask.remove(downloadInfo.id);
                    }
                }

                downloadInfo.mCurrentState = STATE_PAUSE;
                notifyDownloadStateChange(downloadInfo);
            }
        }
    }

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

    public DownloadInfo getDownloadInfo(String id) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(id);
        return downloadInfo;
    }
}
