package com.tokyonth.installer.database

import android.content.ContentValues
import android.content.Context

import java.util.ArrayList

object SQLiteUtil {

    fun addData(context: Context, pkg: String) {
        val dbHelper = SQLiteHelper(context, "freezeList", 1)
        val sqLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("pkg", pkg)
        sqLiteDatabase.insert("appList", null, values)
        sqLiteDatabase.close()
    }

    fun delData(context: Context, pkg: String) {
        val dbHelper = SQLiteHelper(context, "freezeList", 1)
        val sqLiteDatabase = dbHelper.writableDatabase
        sqLiteDatabase.delete("appList", "pkg=?", arrayOf(pkg))
        sqLiteDatabase.close()
    }

    fun getAllData(context: Context): ArrayList<String> {
        val list = ArrayList<String>()
        val dbHelper = SQLiteHelper(context, "freezeList", 1)
        val sqLiteDatabase = dbHelper.readableDatabase
        val cursor = sqLiteDatabase.query("appList", arrayOf("_id", "pkg"),
                null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("pkg")))
        }
        cursor.close()
        sqLiteDatabase.close()
        return list
    }

}
