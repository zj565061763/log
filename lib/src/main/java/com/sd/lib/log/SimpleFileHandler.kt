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
    getLogFilePath(context, filename + FILE_SUFFIX),
    limitMB * MB,
    1,
    true,
) {
    companion object {
        private const val MB = 1024 * 1024
        private const val DIR_NAME = "flog"
        private const val FILE_SUFFIX = ".log"

        private fun getLogFilePath(context: Context, fileName: String): String {
            val today = newDateFormat().format(Date())
            val dir = File(getLogFileDir(context), today)
            checkDir(dir)
            return dir.absolutePath + File.separator + fileName
        }

        @JvmStatic
        fun getLogFileDir(context: Context): File {
            var dir = context.getExternalFilesDir(DIR_NAME)
            if (checkDir(dir)) return dir!!
            dir = File(context.filesDir, DIR_NAME)
            checkDir(dir)
            return dir
        }

        @JvmStatic
        fun newDateFormat(): DateFormat {
            return SimpleDateFormat("yyyyMMdd")
        }

        private fun checkDir(dir: File?): Boolean {
            return if (dir == null) false else dir.exists() || dir.mkdirs()
        }
    }
}