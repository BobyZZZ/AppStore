package com.bb.googleplaybb.manager;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.http.HttpHelper;
import com.bb.googleplaybb.utils.IOUtils;
import com.bb.googleplaybb.utils.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Boby on 2018/7/18.
 */

public class DownloadManager {
    private static DownloadManager downloadManager = new DownloadManager();

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

    private DownloadManager() {
    }

    public static DownloadManager getDownloadManager() {
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
        if (downloadInfo != null) {
            // 跳到系统的安装页面进行安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + downloadInfo.path),
                    "application/vnd.android.package-archive");
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
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        if (downloadInfo == null) {
            downloadInfo = DownloadInfo.copy(appInfo);
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

            System.out.println("downloadInfo.path:"+ downloadInfo.path);
            File file = new File(downloadInfo.path);

            HttpHelper.HttpResult httpResult = null;
            if (!file.exists() || file.length() != downloadInfo.mCurrentPosition || downloadInfo.mCurrentPosition == 0) {
                //从头开始下载
                System.out.println("从头开始下载.....");
                file.delete();//文件的大小跟downloadInfo当前的进度不一致或者下载状态为失败，则删除文件，第一种情况文件不存在删除文件也不会报错
                downloadInfo.mCurrentPosition = 0;

                httpResult = HttpHelper.download(HttpHelper.URL + "download?name=" + downloadInfo.downloadUrl);
            } else {
                //断点续传
                System.out.println("断点续传");
                httpResult = HttpHelper.download(HttpHelper.URL + "download?name=" + downloadInfo.downloadUrl + "&range=" + downloadInfo.mCurrentPosition);
            }

            if (httpResult != null && httpResult.getInputStream() != null) {
                InputStream in = httpResult.getInputStream();
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file, true);
                    int len = 0;
                    byte[] buffer = new byte[1024 * 4];
                    while ((len = in.read(buffer)) != -1 && downloadInfo.mCurrentState == STATE_DOWNLOADING) {//此处控制下载暂停
                        out.write(buffer, 0, len);
                        out.flush();//保证数据写进去
                        downloadInfo.mCurrentPosition += len;
                        System.out.println("进去了，进去了这么多：" + downloadInfo.mCurrentPosition + "; id:"+downloadInfo.id);
                        notifyDownloadProgressChange(downloadInfo);
                    }
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(in);
                    IOUtils.close(out);
                }
            } else {
                //网络异常
                System.out.println("网络异常");
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
        public void notifyDownloadStateChange(DownloadInfo downloadInfo);

        public void notifyDownloadProgressChange(DownloadInfo downloadInfo);
    }

    public DownloadInfo getDownloadInfo(AppInfo appInfo) {
        DownloadInfo downloadInfo = mDownloadInfoMap.get(appInfo.id);
        return downloadInfo;
    }
}
