package com.fanwe.lib.log;

import android.content.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FLogger<T extends FLogger>
{
    private static final Map<Class<?>, FLogger> MAP_LOGGER = new ConcurrentHashMap<>();
    private static Level sGlobalLevel;

    private final Logger mLogger;
    private FFileHandler mFileHandler;

    private FLogger(Class<T> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        final String name = clazz.getName();
        mLogger = Logger.getLogger(name);

        setLevel(sGlobalLevel);
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
    public static FLogger get()
    {
        return get(FLogger.class);
    }

    /**
     * 返回指定的对象
     *
     * @param clazz
     * @return
     */
    protected static final <T extends FLogger> FLogger get(Class<T> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        FLogger logger = MAP_LOGGER.get(clazz);
        if (logger == null)
        {
            logger = new FLogger(clazz);
            MAP_LOGGER.put(clazz, logger);
        }
        return logger;
    }

    /**
     * 设置全局日志输出等级，小于设置等级的将不会被输出
     *
     * @param level
     */
    public static final void setGlobalLevel(Level level)
    {
        for (Map.Entry<Class<?>, FLogger> item : MAP_LOGGER.entrySet())
        {
            item.getValue().setLevel(level);
        }
        sGlobalLevel = level;
    }

    /**
     * 删除所有日志文件
     */
    public static final void deleteAllLogFile()
    {
        for (Map.Entry<Class<?>, FLogger> item : MAP_LOGGER.entrySet())
        {
            item.getValue().deleteLogFile();
        }
    }

    private void setLevel(Level level)
    {
        mLogger.setLevel(level);
    }

    /**
     * 设置是否打开日志缓存到文件的功能
     *
     * @param context null-关闭，不为null-打开
     */
    public final void setLogFileEnable(Context context)
    {
        if (context == null)
        {
            removeHandlers(mLogger);
            mFileHandler = null;
        } else
        {
            if (mFileHandler == null)
            {
                try
                {
                    mFileHandler = new FFileHandler(mLogger.getName() + ".log", 100 * FFileHandler.MB, context);
                    mLogger.addHandler(mFileHandler);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除日志文件
     */
    public final void deleteLogFile()
    {
        if (mFileHandler != null)
            mFileHandler.deleteLogFile();
    }

    /**
     * 移除当前对象
     */
    public final void remove()
    {
        MAP_LOGGER.remove(this);
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
