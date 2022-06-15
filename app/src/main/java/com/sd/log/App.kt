package com.sd.log

import android.app.Application
import com.sd.lib.log.FLogger
import com.sd.lib.log.ext.FLogBuilder
import com.sd.lib.log.info
import com.sd.lib.log.severe
import com.sd.lib.log.warning

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化
        FLogger.init(this)

        // kotlin扩展写法
        AppLogger::class.java.info { "App info" }
        AppLogger::class.java.warning { "App warning" }
        AppLogger::class.java.severe { "App severe" }
        AppLogger::class.java.severe(RuntimeException("exception")) { "App severe" }

        // 用FLogBuilder构建日志字符串
        AppLogger::class.java.info { FLogBuilder().add("App FLogBuilder") }
    }
}