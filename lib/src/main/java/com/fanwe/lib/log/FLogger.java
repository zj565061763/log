package com.fanwe.lib.log;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FLogger
{
    static final Map<Class<?>, FLogger> MAP_LOGGER = new ConcurrentHashMap<>();
    static final Map<Class<?>, Class<?>> MAP_TAG = new HashMap<>();
    static Level sGlobalLevel;

    final Logger mLogger;
    SimpleFileHandler mFileHandler;
    int mLogFileLimit;

    protected FLogger()
    {
        synchronized (MAP_TAG)
        {
            if (MAP_TAG.remove(getClass()) == null)
                throw new RuntimeException("you can not call this constructor");
        }

        mLogger = Logger.getLogger(getClass().getName());
        mLogger.setLevel(sGlobalLevel);
    }

    /**
     * 日志对象被创建回调
     */
    protected abstract void onCreate();

    public static final <T extends FLogger> FLogger get(Class<T> clazz)
    {
        if (clazz == null)
            return null;

        FLogger logger = MAP_LOGGER.get(clazz);
        if (logger == null)
        {
            try
            {
                synchronized (MAP_TAG)
                {
                    MAP_TAG.put(clazz, clazz);
                    logger = clazz.newInstance();
                    if (MAP_TAG.containsKey(clazz))
                        throw new RuntimeException("you must remove tag from tag map after logger instance created");
                }

                logger.onCreate();
                MAP_LOGGER.put(clazz, logger);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return logger;
    }


    /**
     * 设置全局日志输出等级，小于设置等级的将不会被输出
     * <br>
     * 此方法需要在日志对象未被实例化之前调用
     *
     * @param level
     */
    public static final void setGlobalLevel(Level level)
    {
        if (!MAP_LOGGER.isEmpty())
            throw new RuntimeException("you can not call this method after logger instance created");

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

    /**
     * 设置日志缓存文件大小
     *
     * @param limitMB 小于等于0-关闭文件缓存；大于0开启文件缓存(单位MB)
     * @param context
     */
    public synchronized final void setLogFile(int limitMB, Context context)
    {
        if (limitMB <= 0)
        {
            removeHandlers(mLogger);
            mFileHandler = null;
        } else
        {
            if (mFileHandler == null || limitMB != mLogFileLimit)
            {
                if (limitMB > (Integer.MAX_VALUE / SimpleFileHandler.MB))
                    throw new IllegalArgumentException("too much limitMB");
                try
                {
                    mLogFileLimit = limitMB;
                    mFileHandler = new SimpleFileHandler(mLogger.getName() + ".log", limitMB * SimpleFileHandler.MB, context);

                    removeHandlers(mLogger);
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
    public synchronized final void deleteLogFile()
    {
        if (mFileHandler != null)
            mFileHandler.deleteLogFile();
    }

    /**
     * 移除当前对象
     */
    public final void remove()
    {
        MAP_LOGGER.remove(getClass());
    }

    //---------- log start ----------

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
