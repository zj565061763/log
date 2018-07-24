package com.fanwe.log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.log.SimpleLogger;
import com.fanwe.lib.looper.FLooper;
import com.fanwe.lib.looper.impl.FSimpleLooper;

public class MainActivity extends AppCompatActivity
{
    private final FLooper mLooper = new FSimpleLooper();
    private long mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleLogger.get().setLogFile(50, this);

        mLooper.setInterval(1000);
        mLooper.start(new Runnable()
        {
            @Override
            public void run()
            {
                mCount++;
                SimpleLogger.get().info("loop count:" + mCount);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SimpleLogger.get().info("onDestroy");
        mLooper.stop();
    }
}
