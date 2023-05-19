package com.sd.demo.log

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.demo.log.ui.theme.AppTheme
import com.sd.lib.log.FLogger
import com.sd.lib.log.fLog

class MainActivity : ComponentActivity() {

    init {
        // 调试模式，tag：FLogger
        FLogger.isDebug = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content()
            }
        }

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
        FLogger.deleteLogFile()
    }
}

@Composable
private fun Content() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = {

            }
        ) {
            Text(text = "button")
        }
    }
}