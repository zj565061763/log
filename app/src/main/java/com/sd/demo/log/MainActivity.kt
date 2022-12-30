package com.sd.demo.log

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.lib.log.FLogger
import com.sd.lib.log.ext.FLogBuilder
import com.sd.lib.log.fLog

class MainActivity : AppCompatActivity() {

    init {
        FLogger.isDebug = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 传统写法
        FLogger.get(AppLogger::class.java).info("onCreate")

        // 删除日志文件，saveDays等于1，表示保留1天的日志，即保留今天的日志，删除今天之前的所有日志
        FLogger.deleteLogFile(1)

        // 日志文件目录
        fLog<AppLogger> { "logFileDir ${FLogger.getLogFileDir()}" }
    }

    override fun onStart() {
        super.onStart()
        // Kotlin写法
        fLog<AppLogger> { "onStart" }
    }

    override fun onResume() {
        super.onResume()
        val textView = findViewById<View>(R.id.tv_content)

        // FlogBuilder写法
        fLog<AppLogger> {
            FLogBuilder().add("onResume").nextLine()
                .pair("textView", textView).nextLine()
                .pairHash("textView hash", textView).nextLine()
                .pairStr("textView string", textView).nextLine()
                .instance(textView).nextLine()
                .instanceStr(textView)
        }
    }

    override fun onStop() {
        super.onStop()
        val nullValue: String? = null
        fLog<AppLogger> {
            FLogBuilder()
                .clazz(MainActivity::class.java)
                .clazzFull(MainActivity::class.java)
                .add("onStop")
                .add(this)
                .addHash(this)
                .pair("nullValue", nullValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fLog<AppLogger> { "onDestroy" }
    }
}