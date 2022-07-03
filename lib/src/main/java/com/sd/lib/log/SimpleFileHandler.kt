package com.sd.lib.log

import android.content.Context
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.FileHandler

internal class SimpleFileHandler(
    context: Context,
    filename: String,
    limitMB: Int,
) : FileHandler(
    getLogFilePath(context, filename),
    limitMB * MB,
    1,
    true,
) {
    val limitMB = limitMB

    init {
        formatter = SimpleLogFormatter()
    }

    companion object {
        private const val MB = 1024 * 1024
        private const val DIR_NAME = "flog"
        private const val FILE_SUFFIX = ".log"

        private fun getLogFilePath(context: Context, fileName: String): String {
            val today = newDateFormat().format(Date())
            val dir = File(getLogFileDir(context), today).also {
                checkDir(it)
            }
            return dir.absolutePath + File.separator + fileName + FILE_SUFFIX
        }

        fun getLogFileDir(context: Context): File {
            val dir = context.getExternalFilesDir(DIR_NAME)
            if (dir != null && checkDir(dir)) return dir
            return File(context.filesDir, DIR_NAME).also {
                checkDir(it)
            }
        }

        fun newDateFormat(): DateFormat {
            return SimpleDateFormat("yyyyMMdd")
        }

        private fun checkDir(dir: File): Boolean {
            return dir.exists() || dir.mkdirs()
        }
    }

    protected fun finalize() {
        try {
            close()
        } catch (e: Exception) {
            // 忽略
        }
    }
}