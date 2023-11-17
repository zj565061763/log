package com.sd.lib.log

import android.os.Looper
import java.util.Calendar

interface FLogRecord {
    val tag: String
    val msg: String
    val level: FLogLevel
    val millis: Long
    val isMainThread: Boolean
    val threadId: Long

    val timeYear: Int
    val timeMonth: Int
    val timeDayOfMonth: Int
    val timeHourOfDay: Int
    val timeMinute: Int
    val timeSecond: Int
    val timeMillisecond: Int
}

private data class DefaultLogRecord(
    override val tag: String,
    override val msg: String,
    override val level: FLogLevel,
    override val millis: Long,
    override val isMainThread: Boolean,
    override val threadId: Long,
) : FLogRecord {

    private val _calendar by lazy {
        Calendar.getInstance().apply {
            this.timeInMillis = millis
        }
    }

    override val timeYear: Int
        get() = _calendar.get(Calendar.YEAR)

    override val timeMonth: Int
        get() = _calendar.get(Calendar.MONTH) + 1

    override val timeDayOfMonth: Int
        get() = _calendar.get(Calendar.DAY_OF_MONTH)

    override val timeHourOfDay: Int
        get() = _calendar.get(Calendar.HOUR_OF_DAY)

    override val timeMinute: Int
        get() = _calendar.get(Calendar.MINUTE)

    override val timeSecond: Int
        get() = _calendar.get(Calendar.SECOND)

    override val timeMillisecond: Int
        get() = _calendar.get(Calendar.MILLISECOND)
}

internal class LogRecordGenerator {
    fun generate(logger: FLogger, level: FLogLevel, msg: String): FLogRecord {
        return DefaultLogRecord(
            tag = logger.loggerTag,
            msg = msg,
            level = level,
            millis = System.currentTimeMillis(),
            isMainThread = Looper.getMainLooper() === Looper.myLooper(),
            threadId = Thread.currentThread().id,
        )
    }
}