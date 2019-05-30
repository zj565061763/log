package com.sd.lib.log;

import android.content.Context;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FLogger
{
    private static final Map<Class<?>, WeakReference<FLogger>> MAP_LOGGER = new ConcurrentHashMap<>();
    private static final ReferenceQueue<FLogger> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static final Map<Class<?>, Class<?>> MAP_TAG = new HashMap<>();

    private static Level sGlobalLevel = Level.ALL;

    private final Logger mLogger;

    private SimpleFileHandler mFileHandler;
    private int mLogFileLimit;
    private Level mLogFileLevel;
    private Context mContext;

    protected FLogger()
    {
        if (MAP_TAG.remove(getClass()) == null)
            throw new RuntimeException("you can not call this constructor");

        mLogger = Logger.getLogger(getClass().getName());
        mLogger.setLevel(sGlobalLevel);
    }

    /**
     * 日志对象被创建回调
     */
    protected abstract void onCreate();

    /**
     * 获得指定的日志类对象
     * <p>
     * 内部采用弱引用指向对象，在对象未被回收之前返回的是同一个对象，如果对象已经被回收，则会创建新的对象返回
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public synchronized static final <T extends FLogger> FLogger get(Class<T> clazz)
    {
        if (clazz == null)
            throw new IllegalArgumentException("clazz is null when get logger");
        if (clazz == FLogger.class)
            throw new IllegalArgumentException("clazz must not be " + FLogger.class);

        releaseIfNeed();

        FLogger logger = null;
        final WeakReference<FLogger> reference = MAP_LOGGER.get(clazz);
        if (reference != null)
        {
            logger = reference.get();
            if (logger != null)
                return logger;
        }

        try
        {
            MAP_TAG.put(clazz, clazz);
            logger = clazz.newInstance();
            if (MAP_TAG.containsKey(clazz))
                throw new RuntimeException("you must remove tag from tag map after logger instance created");

            logger.onCreate();
            MAP_LOGGER.put(clazz, new WeakReference<>(logger, REFERENCE_QUEUE));
            return logger;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void releaseIfNeed()
    {
        while (true)
        {
            final Reference<? extends FLogger> reference = REFERENCE_QUEUE.poll();
            if (reference == null)
                break;

            for (Map.Entry<Class<?>, WeakReference<FLogger>> item : MAP_LOGGER.entrySet())
            {
                if (item.getValue() == reference)
                {
                    MAP_LOGGER.remove(item.getKey());
                    break;
                }
            }
        }
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
     * 删除并关闭文件功能
     *
     * @param open 成功删除关闭后是否重新打开
     */
    public static final void deleteAllLogFile(boolean open)
    {
        for (Map.Entry<Class<?>, WeakReference<FLogger>> item : MAP_LOGGER.entrySet())
        {
            final FLogger logger = item.getValue().get();
            if (logger != null)
                logger.deleteLogFile(open);
        }
    }

    /**
     * {@link #openLogFile(int, Level, Context)}
     *
     * @param limitMB
     * @param context
     */
    public final void openLogFile(int limitMB, Context context)
    {
        openLogFile(limitMB, Level.ALL, context);
    }

    /**
     * 打开日志文件功能
     *
     * @param limitMB 文件大小限制(单位MB)
     * @param level   记录到文件的最小日志等级，小于指定等级的日志不会记录到文件
     * @param context
     */
    public synchronized final void openLogFile(int limitMB, Level level, Context context)
    {
        if (level == null)
            level = Level.ALL;

        if (mFileHandler == null || mLogFileLimit != limitMB || mLogFileLevel != level)
        {
            mLogFileLimit = limitMB;
            mLogFileLevel = level;
            mContext = context.getApplicationContext();
            closeLogFile();

            try
            {
                mFileHandler = new SimpleFileHandler(mLogger.getName(), limitMB, mContext);
                mFileHandler.setLevel(level);

                mLogger.addHandler(mFileHandler);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 关闭日志文件功能
     */
    public synchronized final void closeLogFile()
    {
        closeLogFileInternal(false);
    }

    /**
     * 删除并关闭文件功能
     *
     * @param open 成功删除关闭后是否重新打开
     */
    public synchronized final void deleteLogFile(boolean open)
    {
        if (closeLogFileInternal(true))
        {
            if (open)
                openLogFile(mLogFileLimit, mLogFileLevel, mContext);
        }
    }

    private boolean closeLogFileInternal(boolean delete)
    {
        if (mFileHandler != null)
        {
            mFileHandler.close();
            mLogger.removeHandler(mFileHandler);

            if (delete)
                mFileHandler.deleteLogFile();

            mFileHandler = null;
            return true;
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        closeLogFile();
    }

    //---------- log start ----------

    public final void info(String msg)
    {
        mLogger.log(Level.INFO, msg);
    }

    public final void warning(String msg)
    {
        mLogger.log(Level.WARNING, msg);
    }

    public final void severe(String msg)
    {
        mLogger.log(Level.SEVERE, msg);
    }

    public final void severe(String msg, Throwable throwable)
    {
        mLogger.log(Level.SEVERE, msg, throwable);
    }

    //---------- log end ----------
}
