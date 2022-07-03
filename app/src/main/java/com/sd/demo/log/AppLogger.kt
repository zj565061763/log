package com.sd.demo.log

import android.util.Log
import com.sd.lib.log.FLogger

class AppLogger : FLogger() {
    override fun onCreate() {
        Log.i("AppLogger", "logger onCreate")
        // 开启日志文件，限制最大50MB
        openLogFile(50)
    }

    fun finalize() {
        Log.i("AppLogger", "logger finalize")
    }
}