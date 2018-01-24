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
    private long mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            Handler handler = new FFileHandler("testlog", 1 * FFileHandler.MB, this);
            FLogger.get().addHandler(handler);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        mLooper.start(1, new Runnable()
        {
            @Override
            public void run()
            {
                mCount++;
                FLogger.get().log(Level.INFO, "loop count:" + mCount);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.get().log(Level.INFO, "onDestroy");
        mLooper.stop();
    }
}
