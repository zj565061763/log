package com.sd.demo.log

import com.sd.lib.log.FLogger

abstract class BaseLogger : FLogger() {

}

class AppLogger : BaseLogger() {
    override fun onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50)
    }
}