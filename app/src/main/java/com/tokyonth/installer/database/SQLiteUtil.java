package com.tokyonth.installer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteUtil {

    public static void addData(Context context, String pkg) {
        SQLiteHelper dbHelper = new SQLiteHelper(context,"freezeList",1);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pkg", pkg);
        sqLiteDatabase.insert("appList", null, values);
        sqLiteDatabase.close();
    }

    public static void delData(Context context, String pkg) {
        SQLiteHelper dbHelper = new SQLiteHelper(context,"freezeList",1);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.delete("appList", "pkg=?", new String[]{pkg});
        sqLiteDatabase.close();
    }

    public static List<String> getAllData(Context context) {
        List<String> list = new ArrayList<>();
        SQLiteHelper dbHelper = new SQLiteHelper(context,"freezeList",1);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("appList", new String[] { "_id",
                "pkg" }, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("pkg")));
        }
        cursor.close();
        sqLiteDatabase.close();
        return list;
    }

}
