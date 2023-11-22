package com.sd.lib.log

import android.util.Log
import com.sd.lib.log.FLoggerManager.get
import java.io.File
import java.util.Collections

abstract class FLogger protected constructor() {
    internal val loggerTag: String = this@FLogger.javaClass.name

    /** 当前对象是否已经被销毁 */
    @Volatile
    private var _isDestroyed: Boolean = false
        set(value) {
            require(value)
            field = value
        }

    /** 日志发布对象 */
    @Volatile
    private var _publisher: FLogPublisher? = null
        set(value) {
            field = value
            if (value != null) {
                addPublisher(this@FLogger, value)
            } else {
                removePublisher(this@FLogger)
            }
        }

    /** 日志等级 */
    @Volatile
    var level: FLogLevel = FLoggerManager.getGlobalLevel()

    /**
     * 日志对象被创建回调
     */
    abstract fun onCreate()

    /**
     * 打开日志文件
     * @param limitMB 文件大小限制(MB)，小于等于0表示无限制
     */
    protected fun openLogFile(limitMB: Int) {
        synchronized(FLoggerManager) {
            if (_isDestroyed) return
            val publisher = _publisher ?: kotlin.run {
                val file = FLoggerManager.getLogDirectory().resolve("${loggerTag}.log")
                createPublisher(file).also { _publisher = it }
            }
            publisher.limitMB(limitMB)
        }
    }

    /**
     * 创建[FLogPublisher]
     */
    protected open fun createPublisher(file: File): FLogPublisher {
        return defaultPublisher(file)
    }

    internal fun destroy() {
        synchronized(FLoggerManager) {
            _isDestroyed = true
            try {
                _publisher?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _publisher = null
            }
        }
    }

    /**
     * 对象即将被销毁，子类不能调用此方法
     */
    protected fun finalize() {
        logMsg { "$loggerTag finalize start" }
        destroy()
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

    /**
     * 指定的[level]是否可以输出
     */
    private fun isLoggable(level: FLogLevel): Boolean {
        if (_isDestroyed) return false
        return level >= this.level
    }

    private fun log(level: FLogLevel, msg: String?) {
        if (msg.isNullOrEmpty()) return
        if (!isLoggable(level)) return

        val record = FLoggerManager.newLogRecord(
            logger = this@FLogger,
            level = level,
            msg = msg,
        )

        _publisher?.publish(record)
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

        /**
         * 如果日志对象被自动回收时，[FLogger.finalize]还未触发，则[FLogger._publisher]可能还未关闭，
         * 如果此时外部调用[get]方法创建新的日志对象并打开了[FLogger.openLogFile]，会导致有两个[FLogPublisher]指向同一个日志文件。
         */
        private val _publisherHolder: MutableMap<Class<out FLogger>, FLogPublisher> = Collections.synchronizedMap(hashMapOf())

        fun addPublisher(logger: FLogger, publisher: FLogPublisher) {
            _publisherHolder.put(logger.javaClass, publisher)?.let { old ->
                // TODO get的时候检查
                try {
                    old.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    logMsg { "close old publisher ${logger.javaClass} size:${_publisherHolder.size}" }
                }
            }
        }

        fun removePublisher(logger: FLogger) {
            _publisherHolder.remove(logger.javaClass)
        }
    }
}

internal inline fun logMsg(block: () -> String) {
    Log.i("FLogger", block())
}