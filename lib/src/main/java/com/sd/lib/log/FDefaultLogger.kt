package com.sd.lib.log

class FDefaultLogger : FLogger() {
    override fun onCreate() {
    }
}

inline fun fLogInfo(block: () -> Any) {
    FDefaultLogger::class.info(block)
}

inline fun fLogWaring(block: () -> Any) {
    FDefaultLogger::class.warning(block)
}

inline fun fLogSevere(thrown: Throwable? = null, block: () -> Any) {
    FDefaultLogger::class.severe(thrown, block)
}