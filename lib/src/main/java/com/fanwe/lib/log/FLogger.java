package com.fanwe.lib.log;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FLogger
{
    private static final Map<String, FLogger> MAP_LOGGER = new HashMap<>();
    private final Logger mLogger;
    private Handler mDefaultHandler;

    protected FLogger(Logger logger)
    {
        if (logger == null)
            throw new NullPointerException("logger is null");
        mLogger = logger;
    }

    /**
     * 返回默认的对象(名字为FLogger)
     *
     * @return
     */
    public static FLogger get()
    {
        return get(FLogger.class.getSimpleName());
    }

    /**
     * 返回某个名字对应的Logger对象
     *
     * @param name
     * @return
     */
    public final static FLogger get(String name)
    {
        if (TextUtils.isEmpty(name))
            return null;

        synchronized (MAP_LOGGER)
        {
            FLogger logger = MAP_LOGGER.get(name);
            if (logger == null)
            {
                logger = new FLogger(Logger.getLogger(name));
                MAP_LOGGER.put(name, logger);
            }
            return logger;
        }
    }

    public final String getName()
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
    }

    /**
     * 设置日志等级
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
