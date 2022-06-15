package com.sd.log

import android.app.Application
import com.sd.lib.log.FLogger
import com.sd.lib.log.ext.FLogBuilder
import com.sd.lib.log.info

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化
        FLogger.init(this)

        // kotlin扩展写法
        AppLogger::class.java.info { "App onCreate" }
        AppLogger::class.java.info { FLogBuilder().add("App onCreate FLogBuilder") }
    }
}