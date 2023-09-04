package com.sd.demo.log

import android.app.Application
import com.sd.lib.log.fLog

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        fLog { "App onCreate" }
    }
}