package com.fanwe.log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.log.FFileHandler;
import com.fanwe.lib.log.FLogger;
import com.fanwe.lib.looper.FLooper;
import com.fanwe.lib.looper.impl.FSimpleLooper;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;

public class MainActivity extends AppCompatActivity
{
    private FLooper mLooper = new FSimpleLooper();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = FFileHandler.getLogFilePath("testlog", this);
        try
        {
            Handler handler = new FFileHandler(path, 1 * FFileHandler.MB, 1, true);
            FLogger.getDefault().addHandler(handler);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        FLogger.getDefault().log(Level.INFO, "onCreate");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        FLogger.getDefault().log(Level.INFO, "onResume");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        FLogger.getDefault().log(Level.INFO, "onPause");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FLogger.getDefault().log(Level.INFO, "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.getDefault().log(Level.INFO, "onDestroy");
        mLooper.stop();
    }
}
