package com.sd.lib.log

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

internal class SimpleLogFormatter : Formatter() {
    private val _date = Date()
    private val _dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    override fun format(record: LogRecord): String {
        _date.time = record.millis
        val date = _dateFormat.format(_date)
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

        val builder = StringBuilder().apply {
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
        return builder.toString()
    }

    companion object {
        private val nextLine: String
            get() = System.getProperty("line.separator") ?: "\r\n"
    }
}