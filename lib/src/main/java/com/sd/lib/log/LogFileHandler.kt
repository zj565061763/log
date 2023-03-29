package com.sd.lib.log

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

internal class LogFileHandler(
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
            val dir = getLogFileDirToday(context)
            return dir.absolutePath + File.separator + fileName + FILE_SUFFIX
        }

        private fun getLogFileDirToday(context: Context): File {
            val today = newDateFormat().format(Date())
            return File(getLogFileDir(context), today).also {
                checkDir(it)
            }
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

    override fun close() {
        try {
            super.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun finalize() {
        close()
    }
}

private class SimpleLogFormatter : Formatter() {
    private val _dateFormat = SimpleDateFormat("MMdd HH:mm:ss.SSS")

    override fun format(record: LogRecord): String {
        val date = _dateFormat.format(Date(record.millis))
        val message = formatMessage(record)

        val thrown = record.thrown
        val error = if (thrown == null) {
            ""
        } else {
            val stringWriter = StringWriter()
            PrintWriter(stringWriter).use {
                it.println()
                thrown.printStackTrace(it)
            }
            stringWriter.toString()
        }

        return buildString {
            // 日期
            append(date)
            // 日志等级
            append(getLevelString(record.level))
            // 日志信息
            append(message)
            // 异常信息
            append(error)
            // 换行
            append("\n")
        }
    }

    companion object {
        private fun getLevelString(level: Level): String {
            return when (level) {
                Level.INFO -> " "
                Level.WARNING -> "(w)"
                Level.SEVERE -> "(s)"
                else -> "($level)"
            }
        }
    }
}