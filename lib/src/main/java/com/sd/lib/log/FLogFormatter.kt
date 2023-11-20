package com.sd.lib.log

internal fun defaultLogFormatter(): FLogFormatter = DefaultLogFormatter()

internal interface FLogFormatter {
    fun format(record: FLogRecord): String
}

private class DefaultLogFormatter : FLogFormatter {
    override fun format(record: FLogRecord): String {
        val date = record.run {
            "${
                timeYear
            }${
                timeMonth.leadingZero(2)
            }${
                timeDayOfMonth.leadingZero(2)
            } ${
                timeHourOfDay.leadingZero(2)
            }:${
                timeMinute.leadingZero(2)
            }:${
                timeSecond.leadingZero(2)
            }.${
                timeMillisecond.leadingZero(3)
            }"
        }
        return buildString {
            append(date)
            if (record.millisConcurrent > 0) {
                append(".${record.millisConcurrent}")
            }

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
        FLogLevel.Warning -> "W"
        FLogLevel.Error -> "E"
        FLogLevel.Off -> "O"
    }
}

private fun Int.leadingZero(size: Int): String {
    return this.toString().let {
        val repeat = size - it.length
        if (repeat > 0) "0".repeat(repeat) + it
        else it
    }
}