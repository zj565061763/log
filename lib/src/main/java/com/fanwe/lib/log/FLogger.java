package com.fanwe.lib.log;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FLogger
{
    private static final String DEFAULT_NAME = "FLogger";
    public static final Map<String, Logger> MAP_LOGGER = new HashMap<>();

    protected FLogger()
    {
    }

    public final static Logger getDefault()
    {
        return get(DEFAULT_NAME);
    }

    public final synchronized static Logger get(String name)
    {
        if (TextUtils.isEmpty(name))
        {
            return null;
        }
        Logger logger = MAP_LOGGER.get(name);
        if (logger == null)
        {
            logger = Logger.getLogger(name);
            MAP_LOGGER.put(name, logger);
        }
        return logger;
    }

    /**
     * 移除Logger对象
     *
     * @param logger
     */
    public final synchronized static void remove(Logger logger)
    {
        final String name = logger.getName();
        if (TextUtils.isEmpty(name))
        {
            return;
        }
        MAP_LOGGER.remove(name);
    }
}
