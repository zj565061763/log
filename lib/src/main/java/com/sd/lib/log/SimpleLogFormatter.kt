package com.sd.lib.log

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

internal class SimpleLogFormatter : Formatter() {
    private val _dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    override fun format(record: LogRecord): String {
        val date = _dateFormat.format(Date(record.millis))
        val message = formatMessage(record)

        val thrown = record.thrown
        val error = if (thrown == null) "" else {
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
            append(" (").append(record.level).append(") ")
            // 日志信息
            append(message)
            // 异常信息
            append(error)
            // 换行
            append(nextLine)
        }
    }

    companion object {
        private val nextLine
            get() = System.getProperty("line.separator") ?: "\r\n"
    }
}