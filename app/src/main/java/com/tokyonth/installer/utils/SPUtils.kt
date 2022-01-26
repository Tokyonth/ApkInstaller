package com.tokyonth.installer.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants

object SPUtils {

    private const val USER = Constants.SP_FILE_NAME

    /**
     * 添加
     */
    fun <T> Any.putSP(key: String, value: T, commit: Boolean = true) {
        putSP(getSharedPreferences(this), key, value, commit = commit)
    }

    /**
     * 获取
     */
    fun <T> Any.getSP(key: String, defValue: T): T {
        return getSP(getSharedPreferences(this), key, defValue)
    }

    /**
     * 删除
     */
    fun Any.delSP(key: String) {
        getSharedPreferences(this).edit(commit = true) {
            remove(key)
        }
    }

    private fun getSharedPreferences(any: Any): SharedPreferences {
        return when (any) {
            is Activity -> {
                any.getSharedPreferences(USER, Context.MODE_PRIVATE)
            }
            is Fragment -> {
                any.requireActivity().getSharedPreferences(USER, Context.MODE_PRIVATE)
            }
            is Context -> {
                any.applicationContext.getSharedPreferences(USER, Context.MODE_PRIVATE)
            }
            else -> {
                App.context.getSharedPreferences(USER, Context.MODE_PRIVATE)
            }
        }
    }

    private fun <T> putSP(
        mShareConfig: SharedPreferences,
        key: String,
        value: T,
        commit: Boolean = true
    ) {
        mShareConfig.edit(commit = commit) {
            when (value) {
                is String -> putString(key, value)
                is Long -> putLong(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getSP(mShareConfig: SharedPreferences, key: String, defValue: T): T {
        val value = when (defValue) {
            is String -> mShareConfig.getString(key, defValue)
            is Long ->
                java.lang.Long.valueOf(mShareConfig.getLong(key, defValue))
            is Boolean ->
                java.lang.Boolean.valueOf(
                    mShareConfig.getBoolean(
                        key,
                        defValue
                    )
                )
            is Int -> Integer.valueOf(mShareConfig.getInt(key, defValue))
            is Float ->
                java.lang.Float.valueOf(mShareConfig.getFloat(key, defValue))
            else -> {
                mShareConfig.getString(key, defValue.toString())
            }
        } as T
        return value
    }

}
