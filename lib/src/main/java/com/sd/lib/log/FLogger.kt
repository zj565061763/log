package com.sd.lib.log

import android.util.Log
import java.io.File

abstract class FLogger protected constructor() {
    internal val loggerTag: String = this@FLogger.javaClass.name

    /** 日志发布对象 */
    private var _publisher: FLogPublisher? = null
    private val _publisherLazy: FLogPublisher by lazy {
        val file = FLoggerManager.getLogDirectory().resolve("${loggerTag}.log")
        createPublisher(file).also {
            _publisher = it
            FLoggerManager.addPublisher(this@FLogger, it)
        }
    }

    /** 是否打开日志文件 */
    @Volatile
    private var _openLogFile: Boolean = false

    /** 当前对象是否已经被移除 */
    @Volatile
    internal var isRemoved: Boolean = false
        set(value) {
            require(value) { "Can not set false to this flag" }
            field = value
        }

    /** 日志等级 */
    @Volatile
    var level: FLogLevel = FLoggerManager.getGlobalLevel()
        set(value) {
            if (isRemoved) return
            field = value
        }

    /**
     * 日志对象被创建回调
     */
    abstract fun onCreate()

    /**
     * 指定的[level]是否可以输出
     */
    private fun isLoggable(level: FLogLevel): Boolean {
        if (isRemoved) return false
        return level >= this.level
    }

    /**
     * 打开日志文件
     * @param limitMB 文件大小限制(MB)，小于等于0表示无限制
     */
    protected fun openLogFile(limitMB: Int) {
        if (isRemoved) return
        _publisherLazy.limitMB(limitMB)
        _openLogFile = true
    }

    /**
     * 创建[FLogPublisher]
     */
    protected open fun createPublisher(file: File): FLogPublisher {
        return defaultLogPublisher(file)
    }

    /**
     * 对象即将被销毁，子类不能调用此方法
     */
    protected fun finalize() {
        logMsg { "$loggerTag finalize start" }
        isRemoved = true

        try {
            _publisher?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            FLoggerManager.removePublisher(this@FLogger)
        }

        FLoggerManager.releaseLogger()
        logMsg { "$loggerTag finalize end" }
    }

    //---------- Api ----------

    internal val loggerApi = object : FLoggerApi {
        override fun isLoggable(level: FLogLevel): Boolean {
            return this@FLogger.isLoggable(level)
        }

        override fun debug(msg: String?) {
            this@FLogger.log(FLogLevel.Debug, msg)
        }

        override fun info(msg: String?) {
            this@FLogger.log(FLogLevel.Info, msg)
        }

        override fun warning(msg: String?) {
            this@FLogger.log(FLogLevel.Warning, msg)
        }

        override fun error(msg: String?) {
            this@FLogger.log(FLogLevel.Error, msg)
        }
    }

    private fun log(level: FLogLevel, msg: String?) {
        if (!isLoggable(level)) return
        if (msg.isNullOrEmpty()) return

        val record = FLoggerManager.newLogRecord(
            logger = this@FLogger,
            level = level,
            msg = msg,
        )

        if (_openLogFile) _publisherLazy.publish(record)
        FLoggerManager.publishToConsole(record)
    }

    companion object {
        /**
         * 打开日志
         */
        @JvmStatic
        fun open(
            /** 日志文件目录 */
            directory: File,

            /** 日志等级 */
            level: FLogLevel,

            /** 是否打印控制台日志 */
            enableConsoleLog: Boolean,
        ) {
            FLoggerManager.open(
                directory = directory,
                level = level,
                enableConsoleLog = enableConsoleLog,
            )
        }

        /**
         * 获取日志Api
         */
        @JvmStatic
        fun get(clazz: Class<out FLogger>): FLoggerApi {
            return FLoggerManager.get(clazz)
        }

        /**
         * 打印[FLogLevel.Debug]控制台日志，不会写入到文件中，tag：com.sd.lib.log.DebugLogger
         */
        @JvmStatic
        fun debug(msg: String?) {
            get(DebugLogger::class.java).debug(msg)
        }

        /**
         * 设置全局日志等级
         */
        @JvmStatic
        fun setGlobalLevel(level: FLogLevel) {
            FLoggerManager.setGlobalLevel(level)
        }

        /**
         * 删除日志文件
         */
        @JvmStatic
        fun deleteLogFile() {
            FLoggerManager.deleteLogFile()
        }

        /**
         * 日志文件目录
         */
        @JvmStatic
        fun <T> logDir(block: (dir: File) -> T): T {
            return FLoggerManager.logDir(block)
        }
    }
}

internal inline fun logMsg(block: () -> String) {
    Log.i("FLogger", block())
}