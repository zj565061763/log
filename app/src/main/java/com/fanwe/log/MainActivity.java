package com.fanwe.log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.log.FLogger;
import com.fanwe.lib.looper.FLooper;
import com.fanwe.lib.looper.impl.FSimpleLooper;

public class MainActivity extends AppCompatActivity
{
    private FLooper mLooper = new FSimpleLooper();
    private long mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FLogger.get().setLogFileEnable(this);

        mLooper.setInterval(1000);
        mLooper.start(new Runnable()
        {
            @Override
            public void run()
            {
                mCount++;
                FLogger.get().info("loop count:" + mCount);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.get().info("onDestroy");
        mLooper.stop();
    }
}
