package com.fanwe.lib.log;

public class SimpleLogger extends FLogger
{
    @Override
    protected void onCreate()
    {
    }

    public static FLogger get()
    {
        return get(SimpleLogger.class);
    }
}
