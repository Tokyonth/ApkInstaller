package com.tokyonth.installer.utils.ktx

import java.io.File
import java.text.DecimalFormat

fun File.deleteFolderFile(deletePath: Boolean) {
    try {
        if (this.isDirectory) {
            val files = this.listFiles()!!
            for (value in files) {
                value.deleteFolderFile(true)
            }
        }
        if (deletePath) {
            if (!this.isDirectory) {
                this.delete()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun File.fileOrFolderSize(): Long {
    try {
        if (!this.exists()) return 0
        return if (!this.isDirectory) {
            this.length()
        } else {
            var total: Long = 0
            val files = this.listFiles()
            if (files == null || files.isEmpty()) {
                return 0
            }
            for (f in files) {
                total += f.fileOrFolderSize()
            }
            total
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

fun Long.toMemorySize(): String {
    val g = 1024 * 1024 * 1024
    val m = 1024 * 1024
    val k = 1024
    val decimalFormat = DecimalFormat("0.00")
    return when {
        (this / g >= 1) -> {
            decimalFormat.format(this / g) + "GB"
        }
        (this / m >= 1) -> {
            decimalFormat.format(this / m) + "MB"
        }
        (this / k >= 1) -> {
            decimalFormat.format(this / k) + "KB"
        }
        else -> this.toString() + "B"
    }
}
