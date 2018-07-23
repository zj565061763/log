package com.fanwe.lib.log;

public final class FLogger extends BaseLogger
{
    private static FLogger sInstance;

    public static FLogger get()
    {
        if (sInstance == null)
        {
            synchronized (FLogger.class)
            {
                if (sInstance == null)
                    sInstance = new FLogger();
            }
        }
        return sInstance;
    }
}
