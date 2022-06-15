package com.sd.log

import android.app.Application
import com.sd.lib.log.FLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化
        FLogger.init(this)
    }
}