package com.sd.demo.log

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.log.databinding.ActivityMainBinding
import com.sd.lib.log.FLogger
import com.sd.lib.log.flog
import com.sd.lib.log.flogI
import kotlin.time.measureTime

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btn.setOnClickListener {
            // 删除日志文件
            FLogger.deleteLogFile()
        }
    }

    override fun onStart() {
        super.onStart()
        flogI<AppLogger> { "onStart" }
    }

    override fun onStop() {
        super.onStop()
        flogI<AppLogger> { "onStop" }
    }
}

private fun testLogPerformance(times: Int = 1_0000) {
    val log = "1".repeat(512)
    measureTime {
        repeat(times) {
            flogI<AppLogger> { log }
        }
    }.let {
        flog { "time:${it.inWholeMilliseconds}" }
    }
}