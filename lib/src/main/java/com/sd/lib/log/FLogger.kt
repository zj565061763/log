package com.sd.lib.log

import android.content.Context
import android.util.Log
import com.sd.lib.context.FContext
import java.io.File
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.text.ParseException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

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
    private var _fileHandler: SimpleFileHandler? = null

    /**
     * 日志对象被创建回调
     */
    protected abstract fun onCreate()

    /**
     * 日志等级
     */
    val level: Level
        get() = _logger.level ?: Level.ALL

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
                _fileHandler = SimpleFileHandler(context, _loggerName, limitMB).also { handler ->
                    handler.level = this@FLogger.level
                    _logger.addHandler(handler)
                    sLoggerHandlerHolder[this@FLogger.javaClass] = handler
                    logMsg { "${this@FLogger.javaClass.name} handler +++++  size:${sLoggerHandlerHolder.size}" }
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

            handler.close()
            _logger.removeHandler(handler)

            val clazz = this@FLogger.javaClass
            if (handler === sLoggerHandlerHolder[clazz]) {
                sLoggerHandlerHolder.remove(clazz)
                logMsg { "${clazz.name} handler ----- size:${sLoggerHandlerHolder.size}" }
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
        try {
            logMsg { "${this@FLogger.javaClass.name} finalize" }
            destroy()
            releaseReference()
        } catch (e: Exception) {
            e.printStackTrace()
            logMsg { "${this@FLogger.javaClass.name} finalize error $e" }
        }
    }

    //---------- log start ----------

    fun info(msg: String?) {
        log(Level.INFO, msg)
    }

    fun warning(msg: String?) {
        log(Level.WARNING, msg)
    }

    @JvmOverloads
    fun severe(msg: String?, thrown: Throwable? = null) {
        log(Level.SEVERE, msg, thrown)
    }

    @JvmOverloads
    fun log(level: Level, msg: String?, thrown: Throwable? = null) {
        if (msg.isNullOrEmpty()) return
        if (_isRemoved) return
        _logger.log(level, msg, thrown)
    }

    //---------- log end ----------

    companion object {
        /**
         * 如果[FLogger]被添加到[sRefQueue]的时候，[FLogger.finalize]未触发，则[FLogger._fileHandler]可能还未关闭。
         * 同时外部调用了[get]方法创建了新的[FLogger]对象并打开了[FLogger.openLogFile]，会导致有两个[SimpleFileHandler]指向同一个日志文件。
         * 所以需要[sLoggerHandlerHolder]来保存[SimpleFileHandler]避免这种情况。
         */
        private val sLoggerHandlerHolder: MutableMap<Class<out FLogger>, SimpleFileHandler> = HashMap()

        private val sRefQueue = ReferenceQueue<FLogger>()
        private val sLoggerHolder: MutableMap<Class<out FLogger>, LoggerRef<FLogger>> = HashMap()

        /** 默认等级 */
        private var sGlobalLevel = Level.ALL

        private val savedContext
            get() = checkNotNull(FContext.get()) { "Context is null" }

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
                val reference = sRefQueue.poll()
                if (reference is LoggerRef) {
                    sLoggerHolder.remove(reference.clazz)
                    logMsg { "${reference.clazz.name} ----- size:${sLoggerHolder.size}" }
                } else {
                    break
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
        fun getLogFileDir(): File {
            synchronized(this@Companion) {
                return SimpleFileHandler.getLogFileDir(savedContext)
            }
        }

        /**
         * 删除日志文件
         *
         * @param saveDays 要保留的日志天数，如果 saveDays <= 0 ，则删除所有日志
         */
        @JvmStatic
        fun deleteLogFile(saveDays: Int) {
            synchronized(this@Companion) {
                val dir = getLogFileDir()
                if (!dir.exists()) return

                if (saveDays <= 0) {
                    // 删除全部日志
                    clearLogger()
                    deleteFileOrDir(dir)
                    return
                }

                val files = dir.listFiles()
                if (files.isNullOrEmpty()) {
                    return
                }

                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(saveDays - 1))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val limitTime = calendar.time.time
                val format = SimpleFileHandler.newDateFormat()

                val listDelete = mutableListOf<File>()
                for (item in files) {
                    if (item.isFile) {
                        listDelete.add(item)
                        continue
                    }

                    try {
                        val fileTime = format.parse(item.name)?.time ?: 0
                        if (fileTime < limitTime) {
                            listDelete.add(item)
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        listDelete.add(item)
                    }
                }

                if (listDelete.isEmpty()) {
                    return
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
    q: ReferenceQueue<in T>,
) : SoftReference<T>(referent, q)

internal inline fun logMsg(block: () -> String) {
    if (FLogger.isDebug) {
        Log.i("FLogger", block())
    }
}

inline fun KClass<out FLogger>.info(block: () -> Any) {
    log(
        level = Level.INFO,
        block = block,
    )
}

inline fun KClass<out FLogger>.warning(block: () -> Any) {
    log(
        level = Level.WARNING,
        block = block,
    )
}

inline fun KClass<out FLogger>.severe(thrown: Throwable? = null, block: () -> Any) {
    log(
        level = Level.SEVERE,
        thrown = thrown,
        block = block,
    )
}

inline fun KClass<out FLogger>.log(
    level: Level,
    thrown: Throwable? = null,
    block: () -> Any,
) {
    val logger = FLogger.get(this.java)
    if (logger.isLoggable(level)) {
        val msg = block().toString()
        logger.log(level = level, msg = msg, thrown = thrown)
    }
}