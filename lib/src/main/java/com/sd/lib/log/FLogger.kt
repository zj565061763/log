package com.sd.lib.log

import android.util.Log
import java.io.File
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference


abstract class FLogger protected constructor() {
    private val _loggerClass = this@FLogger.javaClass
    internal val loggerTag: String = _loggerClass.name

    /** 日志发布对象 */
    private var _publisher: FLogPublisher? = null

    /** 当前对象是否已经被移除 */
    @Volatile
    private var _isRemoved: Boolean = false
        set(value) {
            require(value) { "Can not set false to this flag" }
            field = value
        }

    /** 日志等级 */
    var level: FLogLevel = sGlobalLevel
        set(value) {
            if (_isRemoved) return
            field = value
        }

    /**
     * 日志对象被创建回调
     */
    protected abstract fun onCreate()

    /**
     * 指定的[level]是否可以输出
     */
    private fun isLoggable(level: FLogLevel): Boolean {
        if (_isRemoved) return false
        return level >= this.level
    }

    /**
     * 打开日志文件
     * @param limitMB 文件大小限制(MB)，小于等于0表示无限制
     */
    protected fun openLogFile(limitMB: Int) {
        if (_isRemoved) return
        synchronized(Companion) {
            val publisher = _publisher
            if (publisher != null) return
            defaultLogPublisher(
                directory = logDirectory,
                limitMB = limitMB,
            ).also {
                _publisher = it
                sLogPublisherHolder[_loggerClass] = it
                logMsg { "$loggerTag publisher +++++ size:${sLogPublisherHolder.size}" }
            }
        }
    }

    /**
     * 关闭日志文件
     */
    private fun closeLogFile() {
        synchronized(Companion) {
            val publisher = _publisher ?: return
            try {
                publisher.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                check(sLogPublisherHolder.remove(_loggerClass) === publisher)
                logMsg { "$loggerTag publisher ----- size:${sLogPublisherHolder.size}" }
                _publisher = null
            }
        }
    }

    /**
     * 销毁
     */
    private fun destroy() {
        _isRemoved = true
        closeLogFile()
    }

    /**
     * 对象即将被销毁，子类不能调用此方法
     */
    protected fun finalize() {
        logMsg { "$loggerTag finalize start" }
        destroy()
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
            this@FLogger.log(FLogLevel.Waring, msg)
        }

        override fun error(msg: String?) {
            this@FLogger.log(FLogLevel.Error, msg)
        }
    }

    private fun log(level: FLogLevel, msg: String?) {
        if (!isLoggable(level)) return
        if (msg.isNullOrEmpty()) return

        val record = sLogRecordGenerator.generate(
            logger = this@FLogger,
            level = level,
            msg = msg,
        )

        _publisher?.publish(record)
        sConsolePublisher?.publish(record)
    }

    companion object {
        /**
         * 如果[FLogger]被添加到[sLoggerRefQueue]的时候，[FLogger.finalize]未触发，则[FLogger._publisher]可能还未关闭。
         * 同时外部调用了[get]方法创建了新的[FLogger]对象并打开了[FLogger.openLogFile]，会导致有两个[FLogPublisher]指向同一个日志文件。
         */
        private val sLogPublisherHolder: MutableMap<Class<out FLogger>, FLogPublisher> = hashMapOf()

        private val sLoggerHolder: MutableMap<Class<out FLogger>, LoggerRef<FLogger>> = hashMapOf()
        private val sLoggerRefQueue: ReferenceQueue<FLogger> = ReferenceQueue()

        /** 全局日志等级 */
        private var sGlobalLevel: FLogLevel = FLogLevel.All

        /** 日志文件目录 */
        private var sLogDirectory: File? = null
        /** 控制台日志打印 */
        @Volatile
        private var sConsolePublisher: FLogPublisher? = null

        private val logDirectory: File
            get() = checkNotNull(sLogDirectory) { "You should invoke FLogger.open() before this." }

        private val sLogRecordGenerator = LogRecordGenerator()

        /** 调试模式，tag：FLogger */
        var debug = false

        @JvmStatic
        fun open(
            directory: File,
            enableConsoleLog: Boolean = false,
        ) {
            synchronized(this@Companion) {
                val dir = sLogDirectory
                if (dir != null) return
                sLogDirectory = directory
                sConsolePublisher = if (enableConsoleLog) defaultConsolePublisher() else null
            }
        }

        /**
         * 获取日志Api
         */
        @JvmStatic
        fun get(clazz: Class<out FLogger>): FLoggerApi {
            require(clazz != FLogger::class.java) { "clazz must not be " + FLogger::class.java }
            val newLogger = synchronized(this@Companion) {
                checkInit()
                releaseReference()

                val cache = sLoggerHolder[clazz]?.get()
                if (cache != null) return cache.loggerApi

                sLogPublisherHolder.remove(clazz)?.also {
                    it.close()
                    logMsg { "publisher closed before finalize ${clazz.name} size:${sLogPublisherHolder.size}" }
                }

                clazz.newInstance().also { logger ->
                    sLoggerHolder[clazz] = LoggerRef(clazz, logger, sLoggerRefQueue)
                    logMsg { "${clazz.name} +++++ size:${sLoggerHolder.size}" }
                }
            }
            newLogger.onCreate()
            return newLogger.loggerApi
        }

        /**
         * 设置全局日志输出等级
         */
        @JvmStatic
        fun setGlobalLevel(level: FLogLevel) {
            synchronized(this@Companion) {
                if (sGlobalLevel != level) {
                    sGlobalLevel = level
                    clearLogger()
                }
            }
        }

        /**
         * 删除日志文件
         */
        @JvmStatic
        fun deleteLogFile() {
            logDir {
                if (it.exists()) {
                    it.deleteRecursively()
                }
            }
        }

        /**
         * 日志文件目录
         */
        @JvmStatic
        fun <T> logDir(block: (dir: File) -> T): T {
            return synchronized(this@Companion) {
                clearLogger()
                block(logDirectory)
            }
        }

        /**
         * 清空所有日志对象
         */
        private fun clearLogger() {
            synchronized(this@Companion) {
                while (sLoggerHolder.isNotEmpty()) {
                    sLoggerHolder.toMap().forEach {
                        it.value.get()?.destroy()
                        sLoggerHolder.remove(it.key)
                    }
                }
            }
        }

        /**
         * 移除引用
         */
        private fun releaseReference() {
            while (true) {
                val reference = sLoggerRefQueue.poll() ?: break
                if (reference is LoggerRef) {
                    sLoggerHolder.remove(reference.clazz)
                    logMsg { "${reference.clazz.name} ----- size:${sLoggerHolder.size}" }
                } else {
                    error("Unknown reference $reference")
                }
            }
        }

        private fun checkInit() {
            checkNotNull(sLogDirectory) { "You should invoke FLogger.open() before this." }
        }
    }
}

private class LoggerRef<T>(
    val clazz: Class<*>,
    referent: T,
    queue: ReferenceQueue<in T>,
) : SoftReference<T>(referent, queue)

internal inline fun logMsg(block: () -> String) {
    if (FLogger.debug) {
        Log.i("FLogger", block())
    }
}