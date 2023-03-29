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
    val limitMB: Int,
) : FileHandler(
    getLogFilePath(context, filename),
    limitMB * (1024 * 1024),
    1,
    true,
) {

    init {
        formatter = SimpleLogFormatter()
    }

    companion object {
        private const val DirName = "flog"
        private const val FilePostfix = ".log"

        private fun getLogFilePath(context: Context, fileName: String): String {
            val parent = getLogFileDir(context)
            val child = dirDateFormat().format(Date())
            val dir = File(parent, child).also { checkDir(it) }
            return dir.absolutePath + File.separator + fileName + FilePostfix
        }

        fun getLogFileDir(context: Context): File {
            val parent = context.getExternalFilesDir(null) ?: context.filesDir
            return File(parent, DirName).also { checkDir(it) }
        }

        fun dirDateFormat(): DateFormat {
            return SimpleDateFormat("yyyyMMdd")
        }

        private fun checkDir(dir: File): Boolean {
            return (dir.exists() && dir.isDirectory) || dir.mkdirs()
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