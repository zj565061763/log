package com.sd.demo.log

import android.app.Application
import com.sd.lib.log.FLogLevel
import com.sd.lib.log.FLogger
import com.sd.lib.log.flog

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FLogger.open(
            directory = filesDir.resolve("app_log"),
            level = FLogLevel.All,
            enableConsoleLog = true,
        )
    }
}