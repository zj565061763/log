package com.sd.lib.log

internal fun defaultLogFormatter(): FLogFormatter = DefaultLogFormatter()

interface FLogFormatter {
    fun format(record: FLogRecord): String
}

private class DefaultLogFormatter : FLogFormatter {
    override fun format(record: FLogRecord): String {
        val date = record.run {
            "${
                timeYear
            }${
                timeMonth
            }${
                timeDayOfMonth
            } ${
                timeHourOfDay.oneLeadingZero()
            }:${
                timeMinute.oneLeadingZero()
            }:${
                timeSecond.oneLeadingZero()
            }.${
                timeMillisecond
            }"
        }
        return buildString {
            append(date)

            append("[")
            append(record.tag)

            if (record.level != FLogLevel.Info) {
                append(",")
                append(record.level.tag)
            }

            if (!record.isMainThread) {
                append(",")
                append(record.threadId.toString())
            }

            append("] ")
            append(record.msg)
            append("\n")
        }
    }
}

private fun Int.oneLeadingZero(): String {
    return this.toString().let {
        if (it.length == 1) "0$it" else it
    }
}