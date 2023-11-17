package com.sd.demo.log

import android.app.Application
import com.sd.lib.log.FLogLevel
import com.sd.lib.log.FLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FLogger.debug = true
        FLogger.open(
            directory = filesDir.resolve("app_log"),
            level = FLogLevel.All,
            enableConsoleLog = true,
        )
    }
}