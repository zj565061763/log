package com.fanwe.lib.log;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class FLogger
{
    private static final Map<String, Logger> MAP_LOGGER = new HashMap<>();

    protected FLogger()
    {
    }

    /**
     * 返回默认的Logger对象(名字为FLogger)
     *
     * @return
     */
    public static Logger get()
    {
        return get(FLogger.class.getName());
    }

    /**
     * 返回某个名字对应的Logger对象
     *
     * @param name
     * @return
     */
    public final synchronized static Logger get(String name)
    {
        if (TextUtils.isEmpty(name))
            return null;

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
        if (logger == null)
            return;

        MAP_LOGGER.remove(logger.getName());
    }

    //---------- utils start ----------

    public final static void removeHandler(Class<?> clazz, Logger logger)
    {
        if (clazz == null || logger == null)
            return;

        final Handler[] handlers = logger.getHandlers();
        if (handlers == null || handlers.length <= 0)
            return;

        for (Handler item : handlers)
        {
            logger.removeHandler(item);
        }
    }

    //---------- utils end ----------
}
