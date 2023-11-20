package com.sd.lib.log

/**
 * 打印[FLogLevel.Debug]控制台日志，不会写入到文件中，tag：com.sd.lib.log.DebugLogger
 */
inline fun fDebug(block: () -> Any) {
    flogD<DebugLogger>(block)
}

/**
 * 打印[FLogLevel.Debug]日志
 */
inline fun <reified T : FLogger> flogD(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Debug)) {
            debug(block().toString())
        }
    }
}

/**
 * 打印[FLogLevel.Info]日志
 */
inline fun <reified T : FLogger> flogI(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Info)) {
            info(block().toString())
        }
    }
}

/**
 * 打印[FLogLevel.Warning]日志
 */
inline fun <reified T : FLogger> flogW(block: () -> Any) {
    with(FLogger.get(T::class.java)) {
        if (isLoggable(FLogLevel.Warning)) {
            warning(block().toString())
        }
    }
}

/**
 * 打印[FLogLevel.Error]日志
 */
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

internal class DebugLogger : FLogger() {
    override fun onCreate() {}
}