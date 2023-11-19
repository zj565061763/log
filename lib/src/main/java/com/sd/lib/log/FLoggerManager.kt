package com.sd.lib.log

import java.io.File
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

internal object FLoggerManager {
    /**
     * 如果[FLogger]被添加到[_loggerRefQueue]的时候，[FLogger.finalize]未触发，则[FLogger._publisher]可能还未关闭。
     * 同时外部调用了[get]方法创建了新的[FLogger]对象并打开了[FLogger.openLogFile]，会导致有两个[FLogPublisher]指向同一个日志文件。
     */
    private val _publisherHolder: MutableMap<Class<out FLogger>, FLogPublisher> = hashMapOf()

    private val _loggerHolder: MutableMap<Class<out FLogger>, LoggerRef<FLogger>> = hashMapOf()
    private val _loggerRefQueue: ReferenceQueue<FLogger> = ReferenceQueue()

    /** 全局日志等级 */
    private var _level: FLogLevel = FLogLevel.All

    /** 日志文件目录 */
    private var _logDirectory: File? = null

    /** 日志记录生成器 */
    private val _logRecordGenerator = LogRecordGenerator()
    /** 控制台日志打印 */
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
        require(clazz != FLogger::class.java) { "clazz must not be " + FLogger::class.java }
        val newLogger = synchronized(this@FLoggerManager) {
            // check init
            logDirectory()
            releaseReference()

            val cache = _loggerHolder[clazz]?.get()
            if (cache != null) return cache.loggerApi

            _publisherHolder.remove(clazz)?.also {
                it.close()
                logMsg { "publisher closed before finalize ${clazz.name} size:${_publisherHolder.size}" }
            }

            clazz.newInstance().also { logger ->
                _loggerHolder[clazz] = LoggerRef(clazz, logger, _loggerRefQueue)
                logMsg { "${clazz.name} +++++ size:${_loggerHolder.size}" }
            }
        }
        newLogger.onCreate()
        return newLogger.loggerApi
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
    @JvmStatic
    fun <T> logDir(block: (dir: File) -> T): T {
        return synchronized(this@FLoggerManager) {
            clearLoggerLocked()
            block(logDirectory())
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
    }

    /**
     * 移除引用
     */
    private fun releaseReference() {
        while (true) {
            val reference = _loggerRefQueue.poll() ?: break
            if (reference is LoggerRef) {
                _loggerHolder.remove(reference.clazz)
                logMsg { "${reference.clazz.name} ----- size:${_loggerHolder.size}" }
            } else {
                error("Unknown reference $reference")
            }
        }
    }

    //---------- Api for logger ----------

    fun getGlobalLevel(): FLogLevel = _level

    fun logDirectory(): File = checkNotNull(_logDirectory) { "You should invoke FLogger.open() before this." }

    fun newLogRecord(logger: FLogger, level: FLogLevel, msg: String): FLogRecord {
        return _logRecordGenerator.generate(
            logger = logger,
            level = level,
            msg = msg,
        )
    }

    fun publishToConsole(record: FLogRecord) {
        _consolePublisher?.publish(record)
    }

    fun addPublisher(logger: FLogger, publisher: FLogPublisher) {
        synchronized(this@FLoggerManager) {
            _publisherHolder[logger.javaClass] = publisher
            logMsg { "${logger.loggerTag} publisher +++++ size:${_publisherHolder.size}" }
        }
    }

    fun removePublisher(logger: FLogger, publisher: FLogPublisher) {
        synchronized(this@FLoggerManager) {
            check(_publisherHolder.remove(logger.javaClass) === publisher)
            logMsg { "${logger.loggerTag} publisher ----- size:${_publisherHolder.size}" }
        }
    }
}

private class LoggerRef<T>(
    val clazz: Class<*>,
    referent: T,
    queue: ReferenceQueue<in T>,
) : SoftReference<T>(referent, queue)