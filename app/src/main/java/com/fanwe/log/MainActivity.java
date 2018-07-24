package com.fanwe.log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.log.FLogger;

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
    protected void onResume()
    {
        super.onResume();
        FLogger.get(AppLogger.class).info("onResume");
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
    }
}
