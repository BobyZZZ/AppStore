package com.bb.googleplaybb.domain;

import android.os.Environment;
import android.util.Log;

import com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.ThreadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.manager.DBUtils;

import java.io.File;
import java.util.ArrayList;

import static com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.THREADCOUNT;

/**
 * Created by Boby on 2018/7/18.
 */

public class DownloadInfo {
    public String id;//应用id
    public String name;//应用名称
    public String downloadUrl;//安装包下载url
    public long size;//安装包大小
    public int mCurrentState;//下载状态
    public long mDownloadedSize;//已下载大小
    public String path;//安装包本地路径
    public int mFinishedCount;//已完成的线程数量

    private String GOOGLEPLAY = "GooglePlayBB";//存放在sd卡的文件夹
    private String DOWNLOAD = "start";
    public ArrayList<ThreadInfo> mThreads;
    private long sum;
    private final String TAG = "zycDownloadInfo";

    public boolean isThreadsEmpty() {
        return mThreads == null || mThreads.isEmpty();
    }

    public void removeThread(ThreadInfo threadInfo) {
        mThreads.remove(threadInfo);
    }
    /**
     * 把数据库查找出来的数据放到集合中
     * @param threadInfos
     */
    public void addThreads(ArrayList<ThreadInfo> threadInfos) {
        if (threadInfos != null && !threadInfos.isEmpty()) {
            mThreads = new ArrayList<>();
            for (ThreadInfo info : threadInfos) {
                info.setDownloadInfo(this);
                mThreads.add(info);
            }
        }
    }

    public ArrayList<ThreadInfo> getThreadList() {
        ArrayList<ThreadInfo> list = new ArrayList<>();
        long boundSize = size / THREADCOUNT;//每一块的大小
        for (int i = 0; i < THREADCOUNT; i++) {
            AppDownloadManager.DownloadTask.ThreadInfo threadInfo;
            if (i == THREADCOUNT - 1) {
                //最后一个
                long lastSize = boundSize + size % THREADCOUNT;
                sum += lastSize;
                threadInfo = new ThreadInfo(id, lastSize, i * boundSize, size - 1, 0);
            } else {
                threadInfo = new ThreadInfo(id, boundSize, i * boundSize, (i + 1) * boundSize - 1, 0);
                sum += boundSize;
            }
            threadInfo.setDownloadInfo(DownloadInfo.this);
            list.add(threadInfo);
        }
        return list;
    }

    public void initThread() {
        mThreads = getThreadList();
        Log.e(TAG, "downloadInfo.size : " + size);
        Log.e(TAG, "sum: " + sum);
    }

    public static DownloadInfo copy(AppInfo info) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.id = info.id;
        downloadInfo.name = info.name;
        downloadInfo.downloadUrl = info.downloadUrl;
        downloadInfo.size = info.size;

        downloadInfo.mCurrentState = AppDownloadManager.STATE_UNDO;
        downloadInfo.mDownloadedSize = 0;
        downloadInfo.path = downloadInfo.getFilePath();


        return downloadInfo;
    }

    public float getProgress() {
        if (size != 0) {
            return mDownloadedSize / (float) size;
        }
        return 0;
    }

    public String getFilePath() {
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(GOOGLEPLAY);
        sb.append(File.separator);
        sb.append(DOWNLOAD);

        if (createDir(sb.toString())) {
            return sb.append(File.separator + name + ".apk").toString();
        }
        return null;
    }

    //是否存在文件夹
    public boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return dir.mkdirs();
        }
        return true;
    }


}
