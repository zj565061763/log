package com.sd.demo.log

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.lib.log.FLogger
import com.sd.lib.log.ext.FLogBuilder
import com.sd.lib.log.info

class MainActivity : AppCompatActivity() {

    init {
        FLogger.debug = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 传统写法
        FLogger.get(AppLogger::class.java).info("onCreate")

        // 删除所有日志文件
//        FLogger.deleteLogFile()

        // 删除日志文件，saveDays等于1，表示保留1天的日志，即保留今天的日志，删除今天之前的所有日志
        FLogger.deleteLogFile(1)

        // 日志文件目录
        AppLogger::class.info { "logFileDir ${FLogger.getLogFileDir()}" }
    }

    override fun onStart() {
        super.onStart()
        // Kotlin扩展写法
        AppLogger::class.info { "onStart" }
    }

    override fun onResume() {
        super.onResume()
        val textView = findViewById<View>(R.id.tv_content)

        // FlogBuilder写法
        AppLogger::class.info {
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
        AppLogger::class.info {
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
        AppLogger::class.info { "onDestroy" }
    }
}