package com.sd.log;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.lib.log.FLogger;
import com.sd.lib.log.ext.FLogBuilder;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FLogger.get(AppLogger.class).info("onCreate");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FLogger.get(AppLogger.class).info("onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final View textView = findViewById(R.id.tv_content);
        FLogger.get(AppLogger.class).info("onResume" + new FLogBuilder()
                .pair("textView", textView)
                .instance(textView)
                .instanceString(textView));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FLogger.get(AppLogger.class).info("onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.get(AppLogger.class).info("onDestroy");

        // 删除所有日志文件
//        FLogger.deleteLogFile(this);

        // 删除2天之前的日志目录
        FLogger.deleteExpiredLogDir(this, 2);
    }
}
