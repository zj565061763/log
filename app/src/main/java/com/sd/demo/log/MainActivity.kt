package com.sd.demo.log

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.lib.log.FLogger
import com.sd.lib.log.fLog

class MainActivity : AppCompatActivity() {

    init {
        // 调试模式，tag：FLogger
        FLogger.isDebug = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 传统写法
        FLogger.get(AppLogger::class.java).info("onCreate")

        // Kotlin写法
        fLog<AppLogger> { "onCreate" }
    }

    override fun onStart() {
        super.onStart()
        fLog<AppLogger> { "onStart" }
    }

    override fun onStop() {
        super.onStop()
        fLog<AppLogger> { "onStop" }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 删除日志文件，saveDays等于1，表示保留1天的日志，即保留今天的日志，删除今天之前的所有日志
        FLogger.deleteLogFile(1)
    }
}