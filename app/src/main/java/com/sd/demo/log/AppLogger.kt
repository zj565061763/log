package com.sd.demo.log

import com.sd.lib.log.FLogger
import com.sd.lib.log.fLogInfo

class AppLogger : FLogger() {
    override fun onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50)
        fLogInfo { "$this onCreate" }
    }
}