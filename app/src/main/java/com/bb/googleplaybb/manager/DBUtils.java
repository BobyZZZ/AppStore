package com.bb.googleplaybb.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.ThreadInfo;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class DBUtils {
    private DBHelper dao = new DBHelper(UIUtils.getContext());
    private static DBUtils dbUtils;

    private DBUtils() {
    }

    public static DBUtils getInstance() {
        if (dbUtils == null) {
            synchronized (DBUtils.class) {
                if (dbUtils == null) {
                    dbUtils = new DBUtils();
                }
            }
        }
        return dbUtils;
    }

    public void addThreadInfo(AppDownloadManager.DownloadTask.ThreadInfo threadInfo) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", threadInfo.id);
        contentValues.put(DBHelper.SIZE, threadInfo.size);
        contentValues.put(DBHelper.THREADID, threadInfo.threadId);
        contentValues.put(DBHelper.START, threadInfo.startIndex);
        contentValues.put(DBHelper.END, threadInfo.endIndex);
        contentValues.put(DBHelper.FINISH, threadInfo.mFinished);
        long insert;
        insert = writableDatabase.insert(DBHelper.THREAD_INFO, null, contentValues);
//        Log.i("zyc", "addThreadInfo: " + threadInfo.threadId + "---" + insert);
    }

    public void deleteThreadInfo(String threadId) {
        long insert;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        insert = writableDatabase.delete(DBHelper.THREAD_INFO, DBHelper.THREADID + " = ?", new String[]{threadId});
//        Log.i("zyc", "deleteThreadInfo: " + threadId + "---" + insert);
    }

    public void deleteThreadInfoById(String id) {
        int delete;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        delete = writableDatabase.delete(DBHelper.THREAD_INFO, "id = ?", new String[]{id});
//        Log.i("zyc", "deleteThreadInfoById: " + id + "---" + delete);
        Log.e("zyc", "deleteThreadInfoById: " + delete);
    }

    public void delete(String id) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        writableDatabase.delete(DBHelper.THREAD_INFO, "id = ?", new String[]{id});
        writableDatabase.delete(DBHelper.DOWNLOAD_INFO, "id = ?", new String[]{id});
    }

    public void updateThreadInfo(String threadId, long finished) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.FINISH, finished);
        int update;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        update = writableDatabase.update(DBHelper.THREAD_INFO, values, DBHelper.THREADID + " = ?", new String[]{threadId});
//        Log.i("zyc", "updateThreadInfo: " + threadId + "---" + "finished:" + finished + "---" + update);
    }

    public void update(DownloadInfo downloadInfo, String threadId, long finished) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.FINISH, finished);

        ContentValues values1 = new ContentValues();
        values1.put(DBHelper.DOWNLOADSIZE, downloadInfo.mDownloadedSize);
        values1.put(DBHelper.CURRENTSTATE, downloadInfo.mCurrentState);

        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        writableDatabase.update(DBHelper.THREAD_INFO, values, DBHelper.THREADID + " = ?", new String[]{threadId});
        writableDatabase.update(DBHelper.DOWNLOAD_INFO, values1, DBHelper.ID + " = ?", new String[]{downloadInfo.id});
    }

    public ArrayList<ThreadInfo> getThreadInfo(String id) {
        ArrayList<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        cursor = readableDatabase.query(DBHelper.THREAD_INFO, null, "id = ?", new String[]{id}, null, null, null);
        while (null != cursor && cursor.moveToNext()) {
            String id1 = cursor.getString(cursor.getColumnIndex("id"));
            long size = cursor.getLong(cursor.getColumnIndex(DBHelper.SIZE));
            long start = cursor.getLong(cursor.getColumnIndex(DBHelper.START));
            long end = cursor.getLong(cursor.getColumnIndex(DBHelper.END));
            long finish = cursor.getLong(cursor.getColumnIndex(DBHelper.FINISH));

            ThreadInfo thread = new ThreadInfo(id1, size, start, end, finish);
//            Log.i("zyc", "getThreadInfo: " + thread);
            list.add(thread);

        }
        cursor.close();
        return list;
    }

    public boolean findThreadInfo(String id, String thread_id) {
        List<AppDownloadManager.DownloadTask.ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        cursor = readableDatabase.query(DBHelper.THREAD_INFO, null, DBHelper.THREADID + " = ?", new String[]{thread_id}, null, null, null);
        if (cursor != null) {
            boolean b = cursor.moveToNext();
            cursor.close();
            return b;
        }
        return false;
    }

    public ArrayList<String> getAllDownloadId() {
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        Cursor cursor = readableDatabase.query(DBHelper.THREAD_INFO, new String[]{DBHelper.ID}, null, null, DBHelper.ID, null, null);
        ArrayList<String> objects = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            objects.add(cursor.getString(0));
            Log.e("zyc", "getAllDownloadId: " + cursor.getString(0));
        }
        cursor.close();
        return objects;
    }

    /*-------------------------------------------------Download_info表------------------------------------------------*/


    public void addDownloadInfo(DownloadInfo downloadInfo) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.ID, downloadInfo.id);
        contentValues.put(DBHelper.ICON, downloadInfo.icon);
        contentValues.put(DBHelper.PACKAGENAME, downloadInfo.packageName);
        contentValues.put(DBHelper.NAME, downloadInfo.name);
        contentValues.put(DBHelper.DOWNLOADURL, downloadInfo.downloadUrl);
        contentValues.put(DBHelper.SIZE, downloadInfo.size);
        contentValues.put(DBHelper.CURRENTSTATE, downloadInfo.mCurrentState);
        contentValues.put(DBHelper.DOWNLOADSIZE, downloadInfo.mDownloadedSize);
        contentValues.put(DBHelper.PATH, downloadInfo.path);
        long insert;
        insert = writableDatabase.insert(DBHelper.DOWNLOAD_INFO, null, contentValues);
        Log.i("zyc", "addDownloadInfo: " + downloadInfo.id + "---" + insert);
    }

    public void deleteDownloadInfo(String id) {
        long insert;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        insert = writableDatabase.delete(DBHelper.DOWNLOAD_INFO, DBHelper.ID + " = ?", new String[]{id});
        Log.i("zyc", "deleteDownloadInfo: " + id + "---" + insert);
    }


    public void updateDownloadInfo(DownloadInfo downloadInfo) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.DOWNLOADSIZE, downloadInfo.mDownloadedSize);
        values.put(DBHelper.CURRENTSTATE, downloadInfo.mCurrentState);
        int update;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        update = writableDatabase.update(DBHelper.DOWNLOAD_INFO, values, DBHelper.ID + " = ?", new String[]{downloadInfo.id});
        Log.i("zyc", "updateDownloadInfo: " + downloadInfo.id + "---" + "finished:" + downloadInfo.mDownloadedSize + "---" + update);
    }

    public DownloadInfo getDownloadInfo(String id) {
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        Cursor cursor = readableDatabase.query(DBHelper.DOWNLOAD_INFO, null, "id = ?", new String[]{id}, null, null, null);
        DownloadInfo info = new DownloadInfo();
        if (cursor != null && cursor.moveToNext()) {
            info.id = id;
            info.icon = cursor.getString(cursor.getColumnIndex(DBHelper.ICON));
            info.packageName = cursor.getString(cursor.getColumnIndex(DBHelper.PACKAGENAME));
            info.name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            info.downloadUrl = cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOADURL));
            info.size = cursor.getLong(cursor.getColumnIndex(DBHelper.SIZE));
            info.mCurrentState = cursor.getInt(cursor.getColumnIndex(DBHelper.CURRENTSTATE));
            info.mDownloadedSize = cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOADSIZE));
            info.path = cursor.getString(cursor.getColumnIndex(DBHelper.PATH));
        }
        Log.e("zyc", "getDownloadInfo: " + info);
        cursor.close();
        return info;
    }

    public ArrayList<DownloadInfo> getAllDownloadInfo() {
        ArrayList<DownloadInfo> infos = new ArrayList<>();
        ArrayList<String> ids = getAllDownloadId();
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();

        for (String id : ids) {
            cursor = readableDatabase.query(DBHelper.DOWNLOAD_INFO, null, "id = ?", new String[]{id}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                DownloadInfo info = new DownloadInfo();
                info.id = id;
                info.icon = cursor.getString(cursor.getColumnIndex(DBHelper.ICON));
                info.packageName = cursor.getString(cursor.getColumnIndex(DBHelper.PACKAGENAME));
                info.name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
                info.downloadUrl = cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOADURL));
                info.size = cursor.getLong(cursor.getColumnIndex(DBHelper.SIZE));
                info.mCurrentState = cursor.getInt(cursor.getColumnIndex(DBHelper.CURRENTSTATE));
                info.mDownloadedSize = cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOADSIZE));
                info.path = cursor.getString(cursor.getColumnIndex(DBHelper.PATH));
                infos.add(info);
                Log.e("zyc", "getDownloadInfo: " + info);
            }
            cursor.close();
        }
        return infos;
    }

    class DBHelper extends SQLiteOpenHelper {
        public static final String THREAD_INFO = "thread_info";
        public static final String DOWNLOAD_INFO = "download_info";
        public static final String DBNAME = "download.db";
        public static final String THREADID = "threadId";
        public static final String START = "start";
        public static final String END = "end";
        public static final String FINISH = "finish";
        public static final String SIZE = "size";
        public static final String ID = "id";
        public static final String ICON = "icon";
        public static final String PACKAGENAME = "packageName";
        public static final String NAME = "name";
        public static final String DOWNLOADURL = "downloadUrl";
        public static final String CURRENTSTATE = "mCurrentState";
        public static final String DOWNLOADSIZE = "mDownloadSize";
        public static final String PATH = "path";
        //数据库版本
        private static final int VERSION = 1;
        //创建thread_info表
        private final String CREATE_THREAD = "create table " + THREAD_INFO + "(" + THREADID + " text primary key," + SIZE + " text,id text," + START + " integer," + END + " integer," + FINISH + " integer)";
        private final String CREATE_DOWNLOAD = "create table download_info (id text primary key,icon text,packageName text,name text,downloadUrl text,size integer,mCurrentState integer,mDownloadSize integer,path text)";

        //删除数据库表语句
        private final String SQL_DROP = "drop table if exists thread_info";


        private DBHelper(Context context) {
            super(context, DBNAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_THREAD);
            db.execSQL(CREATE_DOWNLOAD);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP);
            db.execSQL("drop table if exists download_info");
        }
    }
}
