package com.sd.log

import com.sd.lib.log.FLogger

class AppLogger : FLogger() {
    override fun onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50)
    }
}