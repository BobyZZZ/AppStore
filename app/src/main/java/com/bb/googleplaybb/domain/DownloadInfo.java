package com.bb.googleplaybb.domain;

import android.app.DownloadManager;
import android.os.Environment;


import com.bb.googleplaybb.manager.AppDownloadManager;

import java.io.File;

/**
 * Created by Boby on 2018/7/18.
 */

public class DownloadInfo {
    public String id;
    public String name;
    public String downloadUrl;
    public long size;
    public int mCurrentState;
    public long mCurrentPosition;
    public String path;

    private String GOOGLEPLAY = "GooglePlayBB";//存放在sd卡的文件夹
    private String DOWNLOAD = "download";

    public static DownloadInfo copy(AppInfo info) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.id = info.id;
        downloadInfo.name = info.name;
        downloadInfo.downloadUrl = info.downloadUrl;
        downloadInfo.size = info.size;

        downloadInfo.mCurrentState = AppDownloadManager.STATE_UNDO;
        downloadInfo.mCurrentPosition = 0;
        downloadInfo.path = downloadInfo.getFilePath();


        return downloadInfo;
    }

    public float getProgress() {
        if (size != 0) {
            return mCurrentPosition / (float) size;
        }
        return 0;
//        return mCurrentPosition / size;
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
