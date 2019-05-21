package com.bb.googleplaybb.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.bb.googleplaybb.domain.AppLiked;
import com.bb.googleplaybb.domain.User;

import java.util.ArrayList;

/**
 * Created by Boby on 2019/5/16.
 */

public class LoginUtils {
    private LoginDBHelper mDBHelper = new LoginDBHelper(UIUtils.getContext());
    private static LoginUtils instance;

    private LoginUtils() {

    }

    public static LoginUtils getInstance() {
        if (instance == null) {
            synchronized (LoginUtils.class) {
                if (instance == null) {
                    instance = new LoginUtils();
                }
            }
        }
        return instance;
    }

    public boolean insertUser(String id, String pwd, String userName,String photoPath) {
        if (id != null && pwd != null && !findUserById(id)) {
            //先查找id是否已注册
            ContentValues values = new ContentValues();
            values.put(LoginDBHelper.USER_ID, id);
            values.put(LoginDBHelper.USER_PWD, pwd);
            values.put(LoginDBHelper.USER_Name, userName);
            values.put(LoginDBHelper.USER_PHOTO_PATH, photoPath);
            SQLiteDatabase writableDatabase = mDBHelper.getWritableDatabase();
            long insert = writableDatabase.insert(LoginDBHelper.TABLE_USER, null, values);
            return insert > 0;
        }
        return false;
    }

    public User findUser(String id, String pwd) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_USER, null, LoginDBHelper.USER_ID + " = ? and " + LoginDBHelper.USER_PWD + " = ?", new String[]{id, pwd}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToNext()) {
            String userName = cursor.getString(cursor.getColumnIndex(LoginDBHelper.USER_Name));
            String photoPath = cursor.getString(cursor.getColumnIndex(LoginDBHelper.USER_PHOTO_PATH));
            user = new User(id, pwd, userName,photoPath);
            cursor.close();
        }
        return user;
    }

    private boolean findUserById(String id) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_USER, null, LoginDBHelper.USER_ID + " = ?", new String[]{id}, null, null, null);
        if (cursor != null) {
            boolean result = cursor.moveToNext();
            cursor.close();
            return result;
        }
        return false;
    }

    public boolean isUserNameExist(String user_name) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_USER, null, LoginDBHelper.USER_Name + " = ?", new String[]{user_name}, null, null, null);
        if (cursor != null) {
            boolean result = cursor.moveToNext();
            cursor.close();
            return result;
        }
        return false;
    }

    public boolean isPhoneExist(String id) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_USER, null, LoginDBHelper.USER_ID + " = ?", new String[]{id}, null, null, null);
        if (cursor != null) {
            boolean result = cursor.moveToNext();
            cursor.close();
            return result;
        }
        return false;
    }

    /*--------------------------------------------------------------------------------------------*/

    public long insertLiked(AppLiked appLiked) {
        if (!TextUtils.isEmpty(appLiked.getUser_id())) {
            ContentValues values = new ContentValues();
            values.put(LoginDBHelper.USER_ID, appLiked.getUser_id());
            values.put(LoginDBHelper.APP_ID, appLiked.getApp_id());
            values.put(LoginDBHelper.APP_NAME, appLiked.getApp_name());
            values.put(LoginDBHelper.APP_DES, appLiked.getApp_des());
            values.put(LoginDBHelper.APP_ICON, appLiked.getApp_icon());
            values.put(LoginDBHelper.APP_PACKAGE_NAME, appLiked.getApp_package_name());

            SQLiteDatabase writableDatabase = mDBHelper.getWritableDatabase();
            long insert = writableDatabase.insert(LoginDBHelper.TABLE_LIKE, null, values);
            return insert;
        }
        return 0;
    }

    public int deleteLiked(String user_id, String app_id) {
        SQLiteDatabase writableDatabase = mDBHelper.getWritableDatabase();
        return writableDatabase.delete(LoginDBHelper.TABLE_LIKE, LoginDBHelper.USER_ID + " = ? and " + LoginDBHelper.APP_ID + " = ?", new String[]{user_id, app_id});
    }

    public boolean findLiked(String user_id, String app_id) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_LIKE, null, LoginDBHelper.USER_ID + " = ? and " + LoginDBHelper.APP_ID + " = ?", new String[]{user_id, app_id}, null, null, null);
        if (cursor != null) {
            boolean result = cursor.moveToNext();
            cursor.close();
            return result;
        }
        return false;
    }

    public ArrayList<AppLiked> getAllLikedApp(String user_id) {
        SQLiteDatabase readableDatabase = mDBHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(LoginDBHelper.TABLE_LIKE, null, LoginDBHelper.USER_ID + " = ?", new String[]{user_id}, null, null, null);
        ArrayList<AppLiked> appLikeds = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            AppLiked appLiked = new AppLiked(cursor);
            appLikeds.add(appLiked);
        }
        cursor.close();
        return appLikeds;
    }


    public class LoginDBHelper extends SQLiteOpenHelper {
        private static final String NAME = "User.db";
        private static final int VERSION = 1;
        public static final String TABLE_USER = "user_info";
        public static final String TABLE_LIKE = "like_info";

        //User表
        public static final String USER_ID = "user_id";
        public static final String USER_PWD = "user_pwd";
        public static final String USER_Name = "user_name";
        public static final String USER_PHOTO_PATH = "user_photo_path";

        //收藏表
        public static final String APP_ID = "app_id";
        public static final String APP_NAME = "app_name";
        public static final String APP_DES = "app_des";
        public static final String APP_PACKAGE_NAME = "app_package_name";
        public static final String APP_ICON = "app_icon";

        private final String CREATE_USER = "create table user_info(user_id text primary key,user_pwd text,user_name text,user_photo_path text)";
        private final String CREATE_LIKE = "create table like_info(user_id text,app_id text,app_name text,app_des text,app_package_name text,app_icon text)";

        public LoginDBHelper(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_USER);
            db.execSQL(CREATE_LIKE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists user_info");
            db.execSQL("drop table if exists like_info");
        }
    }
}
