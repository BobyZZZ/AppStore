package com.bb.googleplaybb.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.bb.googleplaybb.BuildConfig;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
                    ThreadManager.getThreadPool().cancel(task);
                }

                downloadInfo.mCurrentState = STATE_PAUSE;
                notifyDownloadStateChange(downloadInfo);
            }
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
            File file = new File(downloadInfo.getFilePath());
            if (file.exists()) {
                downloadInfo.mCurrentPosition = file.length();
            }
        }
        downloadInfo.mCurrentState = STATE_WAITING;
        notifyDownloadStateChange(downloadInfo);
        //添加到下载信息集合
        mDownloadInfoMap.put(downloadInfo.id, downloadInfo);

        DownloadTask task = new DownloadTask(downloadInfo);
        mDownloadTaskMap.put(downloadInfo.id, task);
        ThreadManager.getThreadPool().execute(task);
    }

    //下载任务，在线程池中运行
    class DownloadTask implements Runnable {
        private DownloadInfo downloadInfo;

        public DownloadTask(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void run() {
            //开始下载
            System.out.println("开始下载拉");
            downloadInfo.mCurrentState = STATE_DOWNLOADING;
            notifyDownloadStateChange(downloadInfo);

            System.out.println("downloadInfo.path:" + downloadInfo.path);
            File file = new File(downloadInfo.path);

            OkHttpClient client = new OkHttpClient();
            Request request = null;
            if (!file.exists() || file.length() != downloadInfo.mCurrentPosition || downloadInfo.mCurrentPosition == 0) {
                //从头开始下载
                System.out.println("从头开始下载.....");
                file.delete();//文件的大小跟downloadInfo当前的进度不一致或者下载状态为失败，则删除文件，第一种情况文件不存在删除文件也不会报错
                downloadInfo.mCurrentPosition = 0;
                request = new Request.Builder().url(NetHelper.URL + downloadInfo.downloadUrl).build();
            } else {
                //断点续传
                System.out.println("断点续传");
                request = new Request.Builder().url(NetHelper.URL + downloadInfo.downloadUrl)
                        .addHeader("RANGE", "bytes=" + file.length() + "-").build();
            }
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response != null && response.isSuccessful()) {
                    InputStream in = response.body().byteStream();
//                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//                    randomAccessFile.seek(file.length());
                    FileOutputStream out = new FileOutputStream(file, true);
                    int len = -1;
                    byte[] buffer = new byte[2048];
                    while ((len = in.read(buffer)) != -1 && downloadInfo.mCurrentState == STATE_DOWNLOADING) {
                        out.write(buffer, 0, len);
                        downloadInfo.mCurrentPosition += len;
                        notifyDownloadProgressChange(downloadInfo);
//                        Thread.sleep(5);
                    }
                    out.flush();
                    //走到此处情况可能为：下载完成、下载被暂停了、下载失败
                    if (file.length() == downloadInfo.size) {
                        //下载完成
                        downloadInfo.mCurrentState = STATE_SUCCESS;
                        notifyDownloadStateChange(downloadInfo);
                    } else if (downloadInfo.mCurrentState == STATE_PAUSE) {
                        notifyDownloadStateChange(downloadInfo);
                    } else {
                        //下载失败
                        downloadInfo.mCurrentState = STATE_ERROR;
                        downloadInfo.mCurrentPosition = 0;
                        notifyDownloadStateChange(downloadInfo);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();//3700647701924
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
            //下载完成后从任务集合中删除
            mDownloadTaskMap.remove(downloadInfo.id);
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
}
