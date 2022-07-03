package com.sd.lib.log

import android.content.Context
import com.sd.lib.context.FContext
import java.io.File
import java.text.ParseException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

abstract class FLogger protected constructor() {
    private val _loggerName = javaClass.name
    private val _logger = Logger.getLogger(_loggerName).apply {
        level = sGlobalLevel
    }

    @Volatile
    private var _isAlive = true
        set(value) {
            require(!value) { "Can not set active to true" }
            field = value
        }

    private var _logFileHandler: SimpleFileHandler? = null

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
    @Synchronized
    fun setLevel(level: Level) {
        if (!_isAlive) return
        _logger.level = level
        _logFileHandler?.level = level
    }

    /**
     * 指定的[level]是否可以输出
     */
    fun isLoggable(level: Level): Boolean {
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
    @Synchronized
    private fun openLogFileInternal(context: Context, limitMB: Int) {
        require(limitMB > 0) { "limitMB must greater than 0" }
        if (!_isAlive) return

        val fileHandler = _logFileHandler
        if (fileHandler != null && fileHandler.limitMB == limitMB) {
            return
        }

        closeLogFileInternal()
        try {
            _logFileHandler = SimpleFileHandler(context, _loggerName, limitMB).also {
                it.level = this@FLogger.level
                _logger.addHandler(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭日志文件
     */
    @Synchronized
    private fun closeLogFileInternal() {
        _logFileHandler?.let {
            it.close()
            _logger.removeHandler(it)
            _logFileHandler = null
        }
    }

    /**
     * 销毁
     */
    private fun destroy() {
        _isAlive = false
        closeLogFileInternal()
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
        if (_isAlive) {
            _logger.log(level, msg, thrown)
        }
    }

    //---------- log end ----------

    companion object {
        private val sLoggerHolder: MutableMap<Class<*>, FLogger> = HashMap()
        private var sGlobalLevel = Level.ALL

        private val savedContext
            get() = checkNotNull(FContext.get()) { "Context is null" }

        /**
         * 获得指定的日志类对象，内部会保存日志对象
         */
        @JvmStatic
        fun <T : FLogger> get(clazz: Class<T>): FLogger {
            require(clazz != FLogger::class.java) { "clazz must not be " + FLogger::class.java }
            return synchronized(this@Companion) {
                val cache = sLoggerHolder[clazz]
                if (cache != null) return cache

                clazz.newInstance().also {
                    sLoggerHolder[clazz] = it
                }
            }.also {
                // onCreate()不需要同步，在synchronized外触发
                it.onCreate()
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
                    item.destroy()
                }
                sLoggerHolder.clear()
            }
        }

        /**
         * 删除所有日志文件
         */
        @JvmStatic
        fun deleteLogFile() {
            deleteLogFile(0)
        }

        /**
         * 删除日志文件
         *
         * @param saveDays 要保留的日志天数，如果 saveDays <= 0 ，则删除所有日志
         */
        @JvmStatic
        fun deleteLogFile(saveDays: Int) {
            synchronized(this@Companion) {
                val dir = SimpleFileHandler.getLogFileDir(savedContext)
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

                val listExpired = mutableListOf<File>()
                for (item in files) {
                    if (item.isFile) {
                        deleteFileOrDir(item)
                        continue
                    }

                    try {
                        val fileTime = format.parse(item.name)?.time ?: 0
                        if (fileTime < limitTime) {
                            listExpired.add(item)
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        listExpired.add(item)
                    }
                }

                if (listExpired.isEmpty()) {
                    return
                }

                // 删除之前要先清空日志对象
                clearLogger()
                for (item in listExpired) {
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

inline fun <T : FLogger> KClass<T>.info(block: () -> Any) {
    log(
        level = Level.INFO,
        block = block,
    )
}

inline fun <T : FLogger> KClass<T>.warning(block: () -> Any) {
    log(
        level = Level.WARNING,
        block = block,
    )
}

inline fun <T : FLogger> KClass<T>.severe(thrown: Throwable? = null, block: () -> Any) {
    log(
        level = Level.SEVERE,
        thrown = thrown,
        block = block,
    )
}

inline fun <T : FLogger> KClass<T>.log(
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