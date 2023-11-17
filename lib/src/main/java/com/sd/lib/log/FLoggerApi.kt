package com.sd.lib.log

inline fun <reified T : FLogger> flogD(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Debug)) {
            debug(block().toString())
        }
    }
}

inline fun <reified T : FLogger> flogI(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Info)) {
            info(block().toString())
        }
    }
}

inline fun <reified T : FLogger> flogW(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Warning)) {
            warning(block().toString())
        }
    }
}

inline fun <reified T : FLogger> flogE(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Error)) {
            error(block().toString())
        }
    }
}

interface FLoggerApi {
    fun isLoggable(level: FLogLevel): Boolean
    fun debug(msg: String?)
    fun info(msg: String?)
    fun warning(msg: String?)
    fun error(msg: String?)
}