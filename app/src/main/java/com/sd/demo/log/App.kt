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

        /**
         * 打印[FLogLevel.Debug]等级的日志，不会写入到文件中，tag：[com.sd.lib.log.DebugLogger]
         */
        flog { "App onCreate" }
    }
}