package com.sd.lib.log

import android.content.Context
import android.util.Log
import com.sd.lib.ctx.fContext
import java.io.File
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.text.ParseException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

abstract class FLogger protected constructor() {
    private val _loggerClass = this@FLogger.javaClass
    private val _loggerName = _loggerClass.name

    private val _logger = Logger.getLogger(_loggerName).apply {
        this.level = sGlobalLevel
    }

    /** 当前对象是否已经被移除 */
    @Volatile
    private var _isRemoved = false
        set(value) {
            require(value) { "Can not set false to this flag" }
            field = value
        }

    /** 日志文件处理 */
    private var _fileHandler: LogFileHandler? = null

    /**
     * 日志对象被创建回调
     */
    protected abstract fun onCreate()

    /**
     * 日志等级
     */
    val level: Level
        get() = _logger.level ?: sGlobalLevel

    /**
     * 设置日志等级
     */
    fun setLevel(level: Level) {
        synchronized(Companion) {
            if (_isRemoved) return
            _logger.level = level
            _fileHandler?.level = level
        }
    }

    /**
     * 指定的[level]是否可以输出
     */
    fun isLoggable(level: Level): Boolean {
        if (_isRemoved) return false
        return _logger.isLoggable(level)
    }

    /**
     * 开启日志文件
     * @param limitMB 文件大小限制(MB)
     */
    protected fun openLogFile(limitMB: Int) {
        openLogFileInternal(savedContext, limitMB)
    }

    /**
     * 开启日志文件
     * @param limitMB 文件大小限制(MB)
     */
    private fun openLogFileInternal(context: Context, limitMB: Int) {
        require(limitMB > 0) { "Require limitMB > 0" }
        synchronized(Companion) {
            if (_isRemoved) return
            if (_fileHandler?.limitMB == limitMB) return

            closeLogFileInternal()
            try {
                _fileHandler = LogFileHandler(context, _loggerName, limitMB).also { handler ->
                    handler.level = this@FLogger.level
                    _logger.addHandler(handler)
                    sLoggerHandlerHolder[_loggerClass] = handler
                    logMsg { "${_loggerClass.name} handler +++++  size:${sLoggerHandlerHolder.size}" }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 关闭日志文件
     */
    private fun closeLogFileInternal() {
        synchronized(Companion) {
            val handler = _fileHandler ?: return
            _fileHandler = null

            try {
                handler.close()
                _logger.removeHandler(handler)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (handler === sLoggerHandlerHolder[_loggerClass]) {
                sLoggerHandlerHolder.remove(_loggerClass)
                logMsg { "${_loggerClass.name} handler ----- size:${sLoggerHandlerHolder.size}" }
            }
        }
    }

    /**
     * 销毁
     */
    private fun destroy() {
        _isRemoved = true
        closeLogFileInternal()
    }

    /**
     * 对象即将被销毁，子类不能调用此方法
     */
    protected fun finalize() {
        logMsg { "${_loggerClass.name} finalize start" }
        destroy()
        logMsg { "${_loggerClass.name} finalize end" }
    }

    //---------- log start ----------

    @JvmOverloads
    fun info(msg: String?, thrown: Throwable? = null) {
        log(
            level = Level.INFO,
            msg = msg,
            thrown = thrown,
        )
    }

    @JvmOverloads
    fun warning(msg: String?, thrown: Throwable? = null) {
        log(
            level = Level.WARNING,
            msg = msg,
            thrown = thrown,
        )
    }

    @JvmOverloads
    fun severe(msg: String?, thrown: Throwable? = null) {
        log(
            level = Level.SEVERE,
            msg = msg,
            thrown = thrown,
        )
    }

    @JvmOverloads
    fun log(level: Level, msg: String?, thrown: Throwable? = null) {
        if (_isRemoved) return
        if (msg.isNullOrEmpty() && thrown == null) return
        _logger.log(level, msg, thrown)
    }

    //---------- log end ----------

    companion object {
        /**
         * 如果[FLogger]被添加到[sRefQueue]的时候，[FLogger.finalize]未触发，则[FLogger._fileHandler]可能还未关闭。
         * 同时外部调用了[get]方法创建了新的[FLogger]对象并打开了[FLogger.openLogFile]，会导致有两个[LogFileHandler]指向同一个日志文件。
         * 所以需要[sLoggerHandlerHolder]来保存[LogFileHandler]避免这种情况。
         */
        private val sLoggerHandlerHolder: MutableMap<Class<out FLogger>, LogFileHandler> = hashMapOf()

        /** 保存Logger对象 */
        private val sLoggerHolder: MutableMap<Class<out FLogger>, LoggerRef<FLogger>> = hashMapOf()
        private val sRefQueue = ReferenceQueue<FLogger>()

        /** 默认等级 */
        private var sGlobalLevel = Level.ALL

        private val savedContext get() = fContext

        /** 调试模式，tag：FLogger */
        @JvmStatic
        var isDebug = false

        /**
         * 获得指定的日志类对象，内部会保存日志对象
         */
        @JvmStatic
        fun get(clazz: Class<out FLogger>): FLogger {
            require(clazz != FLogger::class.java) { "clazz must not be " + FLogger::class.java }
            return synchronized(this@Companion) {
                releaseReference()

                val cache = sLoggerHolder[clazz]?.get()
                if (cache != null) return cache

                val handler = sLoggerHandlerHolder[clazz]
                if (handler != null) {
                    /**
                     * [FLogger.finalize]还未触发，手动关闭handler
                     */
                    handler.close()
                    sLoggerHandlerHolder.remove(clazz)
                    logMsg { "handler closed before finalize ${clazz.name} size:${sLoggerHandlerHolder.size}" }
                }

                clazz.newInstance().also { logger ->
                    sLoggerHolder[clazz] = LoggerRef(clazz, logger, sRefQueue)
                    logMsg { "${clazz.name} +++++ size:${sLoggerHolder.size}" }
                }
            }.also {
                // onCreate()不需要同步，在synchronized外触发
                it.onCreate()
            }
        }

        /**
         * 移除引用
         */
        private fun releaseReference() {
            while (true) {
                val reference = sRefQueue.poll() ?: break
                if (reference is LoggerRef) {
                    sLoggerHolder.remove(reference.clazz)
                    logMsg { "${reference.clazz.name} ----- size:${sLoggerHolder.size}" }
                } else {
                    error("Unknown reference $reference")
                }
            }
        }

        /**
         * 设置全局日志输出等级
         */
        @JvmStatic
        fun setGlobalLevel(level: Level) {
            synchronized(this@Companion) {
                if (sGlobalLevel != level) {
                    sGlobalLevel = level
                    clearLogger()
                }
            }
        }

        /**
         * 清空所有日志对象
         */
        @JvmStatic
        fun clearLogger() {
            synchronized(this@Companion) {
                for (item in sLoggerHolder.values) {
                    item.get()?.destroy()
                }
                sLoggerHolder.clear()
            }
        }

        /**
         * 日志文件目录
         */
        @JvmStatic
        fun logFileDir(block: (dir: File) -> Unit) {
            synchronized(this@Companion) {
                val dir = LogFileHandler.getLogFileDir(savedContext)
                block(dir)
            }
        }

        /**
         * 删除日志文件
         *
         * @param saveDays 要保留的日志天数，如果 saveDays <= 0 ，则删除所有日志
         */
        @JvmStatic
        fun deleteLogFile(saveDays: Int) {
            logFileDir { dir ->
                if (!dir.exists()) return@logFileDir

                if (saveDays <= 0) {
                    // 删除全部日志
                    clearLogger()
                    deleteFileOrDir(dir)
                    return@logFileDir
                }

                val files = dir.listFiles()
                if (files.isNullOrEmpty()) {
                    return@logFileDir
                }

                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(saveDays - 1))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val limitTime = calendar.time.time
                val format = LogFileHandler.dirDateFormat()

                val listDelete = mutableListOf<File>()
                for (item in files) {
                    if (item.isFile) {
                        listDelete.add(item)
                        continue
                    }

                    val fileTime: Long = try {
                        format.parse(item.name)?.time ?: 0
                    } catch (e: ParseException) {
                        0
                    }

                    if (fileTime < limitTime) {
                        listDelete.add(item)
                    }
                }

                if (listDelete.isEmpty()) {
                    return@logFileDir
                }

                // 删除之前要先清空日志对象
                clearLogger()
                for (item in listDelete) {
                    deleteFileOrDir(item)
                }
            }
        }

        private fun deleteFileOrDir(file: File?): Boolean {
            if (file == null || !file.exists()) return true
            return file.deleteRecursively()
        }
    }
}

private class LoggerRef<T>(
    val clazz: Class<*>,
    referent: T,
    queue: ReferenceQueue<in T>,
) : SoftReference<T>(referent, queue)


inline fun <reified T : FLogger> fLogWarning(
    thrown: Throwable? = null,
    block: () -> Any,
) {
    fLog<T>(
        level = Level.WARNING,
        thrown = thrown,
        block = block,
    )
}

inline fun <reified T : FLogger> fLogSevere(
    thrown: Throwable? = null,
    block: () -> Any,
) {
    fLog<T>(
        level = Level.SEVERE,
        thrown = thrown,
        block = block,
    )
}

inline fun <reified T : FLogger> fLog(
    level: Level = Level.INFO,
    thrown: Throwable? = null,
    block: () -> Any,
) {
    val logger = FLogger.get(T::class.java)
    if (logger.isLoggable(level)) {
        logger.log(
            level = level,
            msg = block().toString(),
            thrown = thrown,
        )
    }
}

internal inline fun logMsg(block: () -> String) {
    if (FLogger.isDebug) {
        Log.i("FLogger", block())
    }
}