package com.sd.log;

import android.util.Log;

import com.sd.lib.log.FLogger;

public class AppLogger extends FLogger
{
    public static final String TAG = AppLogger.class.getSimpleName();

    @Override
    protected void onCreate()
    {
        openLogFile(App.getInstance(), 50);
        Log.i(TAG, "Logger onCreate:" + this);
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        Log.i(TAG, "Logger finalize:" + this);
    }
}
