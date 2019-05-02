package com.bb.googleplaybb.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bb.googleplaybb.manager.AppDownloadManager.DownloadTask.ThreadInfo;

import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;


public class DBUtils {
    private DataBaseOpenHelper dao = new DataBaseOpenHelper(UIUtils.getContext());
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
        contentValues.put(DataBaseOpenHelper.SIZE, threadInfo.size);
        contentValues.put(DataBaseOpenHelper.THREADID, threadInfo.threadId);
        contentValues.put(DataBaseOpenHelper.START, threadInfo.startIndex);
        contentValues.put(DataBaseOpenHelper.END, threadInfo.endIndex);
        contentValues.put(DataBaseOpenHelper.FINISH, threadInfo.mFinished);
        long insert;
        synchronized (DBUtils.class) {
            insert = writableDatabase.insert(DataBaseOpenHelper.TABLENAME, null, contentValues);
        }
        Log.e("zyc", "addThreadInfo: " + threadInfo.threadId + "---" + insert);
//        writableDatabase.close();
    }

    public void deleteThreadInfo(String threadId) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        long insert;
        synchronized (DBUtils.class) {
            insert = writableDatabase.delete(DataBaseOpenHelper.TABLENAME, DataBaseOpenHelper.THREADID + " = ?", new String[]{threadId});
        }
        Log.e("zyc", "deleteThreadInfo: " + threadId + "---" + insert);
//        writableDatabase.close();
    }

    public void deleteThreadInfoById(String id) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        int delete;
        synchronized (DBUtils.class) {
            delete = writableDatabase.delete(DataBaseOpenHelper.TABLENAME, "id = ?", new String[]{id});
        }
        Log.e("zyc", "deleteThreadInfoById: " + id + "---" + delete);
//        writableDatabase.close();
    }

    public void updateThreadInfo(String threadId, long finished) {
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBaseOpenHelper.FINISH,finished);
        int update;
        synchronized (DBUtils.class) {
            update = writableDatabase.update(DataBaseOpenHelper.TABLENAME, values, DataBaseOpenHelper.THREADID + " = ?", new String[]{threadId});
        }
        Log.e("zyc", "updateThreadInfo: " + threadId + "---" + update);
//        writableDatabase.close();
    }

    public ArrayList<ThreadInfo> getThreadInfo(String id) {
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        ArrayList<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        synchronized (DBUtils.class) {
            cursor = readableDatabase.query(DataBaseOpenHelper.TABLENAME, null, "id = ?", new String[]{id}, null, null, null);
        }
        while (null != cursor && cursor.moveToNext()) {
            String id1 = cursor.getString(cursor.getColumnIndex("id"));
            String threadId = cursor.getString(cursor.getColumnIndex(DataBaseOpenHelper.THREADID));
            long size = cursor.getLong(cursor.getColumnIndex(DataBaseOpenHelper.SIZE));
            long start = cursor.getLong(cursor.getColumnIndex(DataBaseOpenHelper.START));
            long end = cursor.getLong(cursor.getColumnIndex(DataBaseOpenHelper.END));
            long finish = cursor.getLong(cursor.getColumnIndex(DataBaseOpenHelper.FINISH));

            ThreadInfo thread = new ThreadInfo(id1, size, start, end, finish);
            Log.e("zyc", "getThreadInfo: " + thread);
            list.add(thread);

        }

//        readableDatabase.close();
        cursor.close();
        return list;
    }

    public boolean findThreadInfo(String id, String thread_id) {
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        List<AppDownloadManager.DownloadTask.ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        synchronized (DBUtils.class) {
            cursor = readableDatabase.query(DataBaseOpenHelper.TABLENAME, null, "id = ? and " + DataBaseOpenHelper.THREADID + " = ?", new String[]{id, thread_id}, null, null, null);
        }
        if (cursor != null) {
            boolean b = cursor.moveToNext();
//            readableDatabase.close();
            cursor.close();
            return b;
        }
        return false;
    }

    class DataBaseOpenHelper extends SQLiteOpenHelper {
        public static final String TABLENAME = "thread_info";
        public static final String DBNAME = "download.db";
        public static final String THREADID = "threadId";
        public static final String START = "start";
        public static final String END = "end";
        public static final String FINISH = "finish";
        public static final String SIZE = "size";
        //数据库版本
        private static final int VERSION = 1;
        //创建数据库表sql语句
        private final String SQL_CREATE = "create table " + TABLENAME + "(" + THREADID + " text primary key," + SIZE + " text,id text," + START + " integer," + END + " integer," + FINISH + " integer)";
        //删除数据库表语句
        private final String SQL_DROP = "drop table if exists thread_info";


        private DataBaseOpenHelper(Context context) {
            super(context, DBNAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP);
        }
    }
}
