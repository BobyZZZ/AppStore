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
        insert = writableDatabase.insert(DBHelper.TABLENAME, null, contentValues);
        Log.e("zyc", "addThreadInfo: " + threadInfo.threadId + "---" + insert);
    }

    public void deleteThreadInfo(String threadId) {
        long insert;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        insert = writableDatabase.delete(DBHelper.TABLENAME, DBHelper.THREADID + " = ?", new String[]{threadId});
        Log.e("zyc", "deleteThreadInfo: " + threadId + "---" + insert);
    }

    public void deleteThreadInfoById(String id) {
        int delete;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        delete = writableDatabase.delete(DBHelper.TABLENAME, "id = ?", new String[]{id});
        Log.e("zyc", "deleteThreadInfoById: " + id + "---" + delete);
    }

    public void updateThreadInfo(String threadId, long finished) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.FINISH, finished);
        int update;
        SQLiteDatabase writableDatabase = dao.getWritableDatabase();
        update = writableDatabase.update(DBHelper.TABLENAME, values, DBHelper.THREADID + " = ?", new String[]{threadId});
        Log.e("zyc", "updateThreadInfo: " + threadId + "---" + "finished:" + finished + "---" + update);
    }

    public ArrayList<ThreadInfo> getThreadInfo(String id) {
        ArrayList<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        cursor = readableDatabase.query(DBHelper.TABLENAME, null, "id = ?", new String[]{id}, null, null, null);
        while (null != cursor && cursor.moveToNext()) {
            String id1 = cursor.getString(cursor.getColumnIndex("id"));
            long size = cursor.getLong(cursor.getColumnIndex(DBHelper.SIZE));
            long start = cursor.getLong(cursor.getColumnIndex(DBHelper.START));
            long end = cursor.getLong(cursor.getColumnIndex(DBHelper.END));
            long finish = cursor.getLong(cursor.getColumnIndex(DBHelper.FINISH));

            ThreadInfo thread = new ThreadInfo(id1, size, start, end, finish);
            Log.e("zyc", "getThreadInfo: " + thread);
            list.add(thread);

        }
        cursor.close();
        return list;
    }

    public boolean findThreadInfo(String id, String thread_id) {
        List<AppDownloadManager.DownloadTask.ThreadInfo> list = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = dao.getReadableDatabase();
        cursor = readableDatabase.query(DBHelper.TABLENAME, null, DBHelper.THREADID + " = ?", new String[]{thread_id}, null, null, null);
        if (cursor != null) {
            boolean b = cursor.moveToNext();
            cursor.close();
            return b;
        }
        return false;
    }

    class DBHelper extends SQLiteOpenHelper {
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


        private DBHelper(Context context) {
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
