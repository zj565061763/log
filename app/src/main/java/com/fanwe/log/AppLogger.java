package com.fanwe.log;

import android.util.Log;

import com.fanwe.lib.log.FLogger;

public class AppLogger extends FLogger
{
    public static final String TAG = AppLogger.class.getSimpleName();

    @Override
    protected void onCreate()
    {
        Log.i(TAG, "onCreate:" + this);
    }
}
