package com.fanwe.lib.log;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FLogger
{
    private static final Map<Class<?>, FLogger> MAP_LOGGER = new HashMap<>();
    private final Logger mLogger;
    private Handler mDefaultHandler;

    private FLogger(Logger logger)
    {
        if (logger == null)
            throw new NullPointerException("logger is null");
        mLogger = logger;
    }

    protected FLogger()
    {
        this(null);
    }

    /**
     * 返回默认的对象
     *
     * @return
     */
    public static final FLogger get()
    {
        return get(FLogger.class);
    }

    /**
     * 返回指定的对象
     *
     * @param clazz
     * @return
     */
    public static final <T extends FLogger> FLogger get(Class<T> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        synchronized (MAP_LOGGER)
        {
            FLogger logger = MAP_LOGGER.get(clazz);
            if (logger == null)
            {
                final String name = clazz.getName();
                logger = new FLogger(Logger.getLogger(name));
                MAP_LOGGER.put(clazz, logger);
            }
            return logger;
        }
    }

    /**
     * 设置全局是否打开日志缓存到文件的功能，仅当次调用的时候有效，对象可以调用{@link #setLogFileEnable(Context)}覆盖此方法的设置
     *
     * @param context
     */
    public static final void setGlobalLogFileEnable(Context context)
    {
        synchronized (MAP_LOGGER)
        {
            for (Map.Entry<Class<?>, FLogger> item : MAP_LOGGER.entrySet())
            {
                item.getValue().setLogFileEnable(context);
            }
        }
    }

    /**
     * 设置全局日志输出等级，仅当次调用的时候有效，对象可以调用{@link #setLevel(Level)}覆盖此方法的设置
     *
     * @param level
     */
    public static final void setGlobalLevel(Level level)
    {
        synchronized (MAP_LOGGER)
        {
            for (Map.Entry<Class<?>, FLogger> item : MAP_LOGGER.entrySet())
            {
                item.getValue().setLevel(level);
            }
        }
    }

    private String getName()
    {
        return mLogger.getName();
    }

    private Handler getDefaultHandler(Context context)
    {
        if (mDefaultHandler == null)
        {
            try
            {
                mDefaultHandler = new FFileHandler(getName() + ".log", 100 * FFileHandler.MB, context);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return mDefaultHandler;
    }

    /**
     * 设置是否打开日志缓存到文件的功能
     *
     * @param context null-关闭，不为null-打开
     */
    public final void setLogFileEnable(Context context)
    {
        removeHandlers(mLogger);
        if (context != null)
            mLogger.addHandler(getDefaultHandler(context));
        else
            mDefaultHandler = null;
    }

    /**
     * 设置日志输出等级，小于设置等级的将不会被输出
     *
     * @param level
     */
    public final void setLevel(Level level)
    {
        mLogger.setLevel(level);
    }

    /**
     * 移除当前对象
     */
    public final void remove()
    {
        synchronized (MAP_LOGGER)
        {
            MAP_LOGGER.remove(this);
        }
    }

    //---------- log start ----------

    public final void log(Level level, String msg)
    {
        mLogger.log(level, msg);
    }

    public final void info(String msg)
    {
        mLogger.info(msg);
    }

    public final void warning(String msg)
    {
        mLogger.warning(msg);
    }

    public final void severe(String msg)
    {
        mLogger.severe(msg);
    }

    //---------- log end ----------

    //---------- utils start ----------

    private final static void removeHandlers(Logger logger)
    {
        final Handler[] handlers = logger.getHandlers();
        for (Handler item : handlers)
        {
            logger.removeHandler(item);
        }
    }

    //---------- utils end ----------
}
