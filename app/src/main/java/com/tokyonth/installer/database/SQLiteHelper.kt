package com.tokyonth.installer.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SQLiteHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {

    @JvmOverloads
    constructor(context: Context, name: String, version: Int = Version) : this(context, name, null, version) {
        Log.e("DataBase", "update")
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sql = "CREATE TABLE appList (_id INTEGER PRIMARY KEY AUTOINCREMENT, pkg TEXT)"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {

        private const val Version = 1

    }

}
