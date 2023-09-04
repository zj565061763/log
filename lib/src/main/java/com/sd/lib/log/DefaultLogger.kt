package com.sd.lib.log

import java.util.logging.Level

internal class FDefaultLogger : FLogger() {
    override fun onCreate() {
    }
}

inline fun fLog(
    level: Level = Level.INFO,
    thrown: Throwable? = null,
    block: () -> Any,
) {
    fLog<FDefaultLogger>(
        level = level,
        thrown = thrown,
        block = block,
    )
}