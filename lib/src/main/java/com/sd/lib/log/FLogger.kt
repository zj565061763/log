package com.sd.lib.log

import android.content.Context
import java.io.File
import java.text.ParseException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

abstract class FLogger protected constructor() {
    private val _loggerName = javaClass.name
    private val _logger = Logger.getLogger(_loggerName).apply {
        level = sGlobalLevel
    }

    private var _logFileLimit = 0
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
    fun setLevel(level: Level?) {
        val safeLevel = level ?: Level.ALL
        _logger.level = safeLevel
        _logFileHandler?.level = safeLevel
    }

    /**
     * 开启日志文件
     * @param limitMB 文件大小限制(MB)
     */
    fun openLogFile(limitMB: Int) {
        openLogFileInternal(savedContext, limitMB)
    }

    /**
     * 关闭日志文件
     */
    fun closeLogFile() {
        closeLogFileInternal()
    }

    /**
     * 开启日志文件
     * @param limitMB 文件大小限制(MB)
     */
    @Synchronized
    private fun openLogFileInternal(context: Context, limitMB: Int) {
        require(limitMB > 0) { "limitMB must greater than 0" }
        if (_logFileHandler == null || _logFileLimit != limitMB) {
            closeLogFileInternal()
            try {
                _logFileHandler = SimpleFileHandler(context, _loggerName, limitMB).apply {
                    formatter = SimpleLogFormatter()
                    level = this@FLogger.level
                }.also {
                    _logger.addHandler(it)
                }
                _logFileLimit = limitMB
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    //---------- log start ----------

    fun info(msg: String?) {
        log(Level.INFO, msg)
    }

    fun warning(msg: String?) {
        log(Level.WARNING, msg)
    }

    fun severe(msg: String?) {
        log(Level.SEVERE, msg)
    }

    fun severe(msg: String?, throwable: Throwable?) {
        log(Level.SEVERE, msg, throwable)
    }

    fun log(level: Level, msg: String?) {
        _logger.log(level, msg ?: "")
    }

    fun log(level: Level, msg: String?, thrown: Throwable?) {
        _logger.log(level, msg ?: "", thrown)
    }

    //---------- log end ----------

    companion object {
        private val sLoggerHolder: MutableMap<Class<*>, FLogger> = ConcurrentHashMap()
        private var sGlobalLevel = Level.ALL
        private var sContext: Context? = null

        private val savedContext: Context
            get() {
                return checkNotNull(sContext) { "You should call FLogger.init(Context) before this" }
            }

        /**
         * 初始化
         */
        @JvmStatic
        fun init(context: Context?) {
            if (context == null) return
            synchronized(this@Companion) {
                if (sContext == null) {
                    sContext = context.applicationContext
                }
            }
        }

        /**
         * 获得指定的日志类对象，内部会保存日志对象
         */
        @JvmStatic
        fun <T : FLogger> get(clazz: Class<T>): FLogger {
            require(clazz != FLogger::class.java) { "clazz must not be " + FLogger::class.java }
            val logger = synchronized(this@Companion) {
                val cache = sLoggerHolder[clazz]
                if (cache != null) return cache

                clazz.newInstance().also {
                    sLoggerHolder[clazz] = it
                }
            }.also {
                // onCreate()不需要同步，在synchronized外触发
                it.onCreate()
            }
            return logger
        }

        /**
         * 清空所有日志对象
         */
        @JvmStatic
        fun clearLogger() {
            synchronized(this@Companion) {
                for (item in sLoggerHolder.values) {
                    item.closeLogFile()
                }
                sLoggerHolder.clear()
            }
        }

        /**
         * 设置全局日志输出等级
         */
        @JvmStatic
        fun setGlobalLevel(level: Level?) {
            val safeLevel = level ?: Level.ALL
            synchronized(this@Companion) {
                if (sGlobalLevel != safeLevel) {
                    sGlobalLevel = safeLevel
                    clearLogger()
                }
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
        @Synchronized
        fun deleteLogFile(saveDays: Int) {
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
                    val fileTime = format.parse(item.name).time
                    if (fileTime < limitTime) {
                        listExpired.add(item)
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                    deleteFileOrDir(item)
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

        private fun deleteFileOrDir(file: File?): Boolean {
            if (file == null || !file.exists()) return true
            return file.deleteRecursively()
        }
    }
}