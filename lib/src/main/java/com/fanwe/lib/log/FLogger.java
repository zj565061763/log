package com.fanwe.lib.log;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        return get(FLogger.class.getSimpleName());
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

    //---------- utils start ----------

    private final static List<Handler> getHandlers(Logger logger)
    {
        if (logger == null)
        {
            return null;
        }
        Handler[] handlers = logger.getHandlers();
        if (handlers == null || handlers.length <= 0)
        {
            return null;
        }
        return Arrays.asList(handlers);
    }

    public final static void removeHandler(Class<?> clazz, Logger logger)
    {
        if (clazz == null)
        {
            return;
        }
        List<Handler> listHandler = getHandlers(logger);
        if (listHandler == null)
        {
            return;
        }
        Iterator<Handler> it = listHandler.iterator();
        while (it.hasNext())
        {
            Handler item = it.next();
            if (item.getClass() == clazz)
            {
                logger.removeHandler(item);
            }
        }
    }

    //---------- utils end ----------
}
