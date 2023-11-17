package com.sd.demo.log

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.log.databinding.ActivityMainBinding
import com.sd.lib.log.FLogger
import com.sd.lib.log.flogI

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