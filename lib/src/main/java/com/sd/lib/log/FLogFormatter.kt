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

            val brackets = record.level != FLogLevel.Info || !record.isMainThread
            if (brackets) {
                append("[")
            }

            if (record.level != FLogLevel.Info) {
                append(record.level.displayName())
            }

            if (!record.isMainThread) {
                append(",")
                append(record.threadId.toString())
            }

            if (brackets) {
                append("]")
            }

            append(" ")
            append(record.msg)
            append("\n")
        }
    }
}

private fun FLogLevel.displayName(): String {
    return when (this) {
        FLogLevel.All -> "A"
        FLogLevel.Debug -> "D"
        FLogLevel.Info -> ""
        FLogLevel.Waring -> "W"
        FLogLevel.Error -> "E"
        FLogLevel.Off -> "O"
    }
}

private fun Int.oneLeadingZero(): String {
    return this.toString().let {
        if (it.length == 1) "0$it" else it
    }
}