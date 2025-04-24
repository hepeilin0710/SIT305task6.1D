package com.example.a61d;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 2; // ✅ 升级版本
    public static final String TABLE_USERS = "users";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT, " +
                "phone TEXT, " +
                "interests TEXT" + // ✅ 新增字段
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN interests TEXT");
        }
    }

    // ✅ 插入用户
    public boolean insertUser(String username, String email, String password, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        values.put("phone", phone);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // ✅ 校验登录
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND password = ?", new String[]{username, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    // ✅ 更新兴趣列表
    public boolean updateUserInterests(String username, ArrayList<String> interests) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("interests", TextUtils.join(",", interests));
        int rows = db.update(TABLE_USERS, values, "username = ?", new String[]{username});
        return rows > 0;
    }

    // ✅ 获取兴趣字符串（逗号分隔）
    public String getUserInterests(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT interests FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            String result = cursor.getString(0);
            cursor.close();
            return result;
        }
        cursor.close();
        return null;
    }

    // ✅ 可选：获取兴趣列表形式（ArrayList）
    public ArrayList<String> getUserInterestsList(String username) {
        String raw = getUserInterests(username);
        if (raw != null && !raw.isEmpty()) {
            return new ArrayList<>(Arrays.asList(raw.split(",")));
        }
        return new ArrayList<>();
    }
}

