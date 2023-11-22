package com.sd.lib.log

import java.io.File
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.Collections

internal object FLoggerManager {
    /**
     * 如果日志对象被自动回收时，[FLogger.finalize]还未触发，则[FLogger._publisher]可能还未关闭，
     * 如果此时外部调用[get]方法创建新的日志对象并打开了[FLogger.openLogFile]，会导致有两个[FLogPublisher]指向同一个日志文件。
     */
    private val _publisherHolder: MutableMap<Class<out FLogger>, FLogPublisher> = Collections.synchronizedMap(hashMapOf())

    private val _loggerHolder: MutableMap<Class<out FLogger>, LoggerRef<FLogger>> = hashMapOf()
    private val _loggerRefQueue: ReferenceQueue<FLogger> = ReferenceQueue()

    /** 日志记录生成器 */
    private val _logRecordGenerator = LogRecordGenerator()

    /** 日志文件目录 */
    @Volatile
    private var _logDirectory: File? = null

    /** 全局日志等级 */
    @Volatile
    private var _level: FLogLevel = FLogLevel.All

    /** 控制台日志打印 */
    @Volatile
    private var _consolePublisher: FLogPublisher? = null

    /**
     * 打开日志
     */
    fun open(
        /** 日志文件目录 */
        directory: File,

        /** 日志等级 */
        level: FLogLevel = FLogLevel.All,

        /** 是否打印控制台日志 */
        enableConsoleLog: Boolean = false,
    ) {
        synchronized(this@FLoggerManager) {
            _logDirectory?.let { return }
            _logDirectory = directory
            _level = level
            _consolePublisher = if (enableConsoleLog) defaultConsolePublisher() else null
        }
    }

    /**
     * 获取日志Api
     */
    fun get(clazz: Class<out FLogger>): FLoggerApi {
        return synchronized(this@FLoggerManager) {
            // check init
            getLogDirectory()

            val cache = _loggerHolder[clazz]?.get()
            if (cache?.isRemoved == false) return cache.loggerApi

            clazz.getDeclaredConstructor().newInstance()!!.also { logger ->
                _loggerHolder[clazz] = LoggerRef(clazz, logger, _loggerRefQueue)
                logMsg { "${clazz.name} +++++ size:${_loggerHolder.size}" }
            }
        }.also { newLogger ->
            newLogger.onCreate()
        }.loggerApi
    }

    /**
     * 设置全局日志等级
     */
    fun setGlobalLevel(level: FLogLevel) {
        synchronized(this@FLoggerManager) {
            if (_level != level) {
                _level = level
                clearLoggerLocked()
            }
        }
    }

    /**
     * 删除日志文件
     */
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
    fun <T> logDir(block: (dir: File) -> T): T {
        return synchronized(this@FLoggerManager) {
            clearLoggerLocked()
            block(getLogDirectory())
        }
    }

    /**
     * 清空所有日志对象
     */
    private fun clearLoggerLocked() {
        while (_loggerHolder.isNotEmpty()) {
            _loggerHolder.toMap().forEach {
                it.value.get()?.isRemoved = true
                _loggerHolder.remove(it.key)
            }
        }
        logMsg { "clear logger" }
    }

    //---------- Api for logger ----------

    fun getGlobalLevel(): FLogLevel = _level

    fun getLogDirectory(): File = checkNotNull(_logDirectory) { "You should invoke FLogger.open() before this." }

    fun newLogRecord(logger: FLogger, level: FLogLevel, msg: String): FLogRecord {
        return _logRecordGenerator.generate(
            tag = logger.loggerTag,
            level = level,
            msg = msg,
        )
    }

    fun publishToConsole(record: FLogRecord) {
        _consolePublisher?.publish(record)
    }

    fun addPublisher(logger: FLogger, publisher: FLogPublisher) {
        _publisherHolder.put(logger.javaClass, publisher)?.let { old ->
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

    fun releaseLogger() {
        synchronized(this@FLoggerManager) {
            while (true) {
                val ref = _loggerRefQueue.poll() ?: break
                if (ref is LoggerRef) {
                    _loggerHolder.remove(ref.clazz)
                    logMsg { "${ref.clazz.name} ----- size:${_loggerHolder.size}" }
                } else {
                    error("Unknown reference $ref")
                }
            }
        }
    }
}

private class LoggerRef<T>(
    val clazz: Class<*>,
    instance: T,
    queue: ReferenceQueue<in T>,
) : WeakReference<T>(instance, queue)