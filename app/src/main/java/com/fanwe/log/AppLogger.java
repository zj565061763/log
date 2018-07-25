package com.fanwe.log;

import android.util.Log;

import com.fanwe.lib.log.FLogger;

public class AppLogger extends FLogger
{
    public static final String TAG = AppLogger.class.getSimpleName();

    @Override
    protected void onCreate()
    {
        openLogFile(50, App.getInstance());
        Log.i(TAG, "onCreate:" + this);
    }
}
