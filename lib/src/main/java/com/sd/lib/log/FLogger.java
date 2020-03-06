package com.sd.lib.log;

import android.content.Context;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FLogger
{
    private static final Map<Class<?>, FLogger> MAP_LOGGER = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> MAP_TAG = new ConcurrentHashMap<>();

    private static Level sGlobalLevel = Level.ALL;

    private final Logger mLogger;

    private Context mContext;
    private SimpleFileHandler mFileHandler;
    private int mLogFileLimit;
    private Level mLogFileLevel;

    protected FLogger()
    {
        final Class<?> clazz = getClass();
        if (MAP_TAG.remove(clazz) == null)
            throw new RuntimeException("you can not call this constructor");

        mLogger = Logger.getLogger(clazz.getName());
        mLogger.setLevel(sGlobalLevel);
    }

    /**
     * 日志对象被创建回调
     */
    protected abstract void onCreate();

    /**
     * 获得指定的日志类对象
     * <p>
     * 内部会保存日志对象，在对象未被移除之前返回的是同一个对象
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

        FLogger logger = MAP_LOGGER.get(clazz);
        if (logger != null)
            return logger;

        try
        {
            MAP_TAG.put(clazz, clazz);
            logger = clazz.newInstance();

            MAP_LOGGER.put(clazz, logger);
            logger.onCreate();
            return logger;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空所有日志对象
     */
    public synchronized static void clearLogger()
    {
        for (FLogger item : MAP_LOGGER.values())
        {
            item.closeLogFile();
        }

        MAP_LOGGER.clear();
    }

    /**
     * 设置全局日志输出等级，小于设置等级的将不会被输出
     * <br>
     * 此方法需要在日志对象未被实例化之前调用
     *
     * @param level
     */
    public synchronized static final void setGlobalLevel(Level level)
    {
        if (level == null)
            level = Level.ALL;

        if (sGlobalLevel != level)
        {
            sGlobalLevel = level;
            clearLogger();
        }
    }

    /**
     * 返回日志等级
     *
     * @return
     */
    public Level getLevel()
    {
        return mLogger.getLevel();
    }

    /**
     * 设置日志等级{@link Logger#setLevel(Level)}
     *
     * @param level
     */
    public synchronized final void setLevel(Level level)
    {
        if (level == null)
            level = Level.ALL;

        mLogger.setLevel(level);

        if (mFileHandler != null)
        {
            if (mLogFileLevel == null)
                mFileHandler.setLevel(level);
        }
    }

    /**
     * 设置写入文件的日志等级
     *
     * @param level null-表示跟随默认的日志等级
     */
    public synchronized final void setFileLevel(Level level)
    {
        if (mLogFileLevel != level)
        {
            mLogFileLevel = level;

            if (mFileHandler != null)
                mFileHandler.setLevel(level != null ? level : mLogger.getLevel());
        }
    }

    /**
     * 打开日志文件功能
     *
     * @param context
     * @param limitMB 文件大小限制(MB)
     */
    public synchronized final void openLogFile(Context context, int limitMB)
    {
        if (limitMB <= 0)
            throw new IllegalArgumentException("limitMB must greater than 0");

        if (mFileHandler == null || mLogFileLimit != limitMB)
        {
            mContext = context.getApplicationContext();
            closeLogFileInternal();

            try
            {
                mFileHandler = new SimpleFileHandler(mContext, mLogger.getName(), limitMB);
                mFileHandler.setFormatter(new SimpleLogFormatter());
                mFileHandler.setLevel(mLogFileLevel != null ? mLogFileLevel : mLogger.getLevel());

                mLogger.addHandler(mFileHandler);
                mLogFileLimit = limitMB;

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭日志文件功能
     */
    public synchronized final void closeLogFile()
    {
        closeLogFileInternal();
    }

    private void closeLogFileInternal()
    {
        if (mFileHandler != null)
        {
            mFileHandler.close();
            mLogger.removeHandler(mFileHandler);
            mFileHandler = null;
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        closeLogFileInternal();
    }

    //---------- log start ----------

    public final void info(String msg)
    {
        log(Level.INFO, msg);
    }

    public final void warning(String msg)
    {
        log(Level.WARNING, msg);
    }

    public final void severe(String msg)
    {
        log(Level.SEVERE, msg);
    }

    public final void severe(String msg, Throwable throwable)
    {
        log(Level.SEVERE, msg, throwable);
    }

    public final void log(Level level, String msg)
    {
        mLogger.log(level, msg);
    }

    public final void log(Level level, String msg, Throwable thrown)
    {
        mLogger.log(level, msg, thrown);
    }

    //---------- log end ----------

    /**
     * 删除所有日志文件
     *
     * @param context
     */
    public static synchronized void deleteLogFile(Context context)
    {
        final File dir = SimpleFileHandler.getLogFileDir(context);
        if (dir == null)
            return;

        if (!dir.exists())
            return;

        clearLogger();
        deleteFileOrDir(dir);
    }

    private static boolean deleteFileOrDir(File file)
    {
        if (file == null || !file.exists())
            return true;

        if (file.isFile())
            return file.delete();

        final File[] files = file.listFiles();
        if (files != null)
        {
            for (File item : files)
            {
                deleteFileOrDir(item);
            }
        }
        return file.delete();
    }

    public static synchronized int deleteExpiredLogDir(Context context)
    {
        final File dir = SimpleFileHandler.getLogFileDir(context);
        if (dir == null)
            return 0;

        final File[] files = dir.listFiles();
        if (files == null || files.length <= 0)
            return 0;

        final int logDay = FLoggerConfig.get().mLogDay - 1;
        final Calendar calendar = Calendar.getInstance();
        if (logDay > 0)
            calendar.add(Calendar.DAY_OF_YEAR, -logDay);

        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        final Date dateLimit = calendar.getTime();

        int count = 0;
        for (File item : files)
        {
            if (item.isFile())
            {
                item.delete();
            } else if (item.isDirectory())
            {
                final String filename = item.getName();
                try
                {
                    final Date dateFile = format.parse(filename);
                    final boolean before = dateFile.before(dateLimit);

                    if (before)
                    {
                        deleteFileOrDir(item);
                        count++;
                    }
                } catch (ParseException e)
                {
                    e.printStackTrace();
                    deleteFileOrDir(item);
                    continue;
                }
            }
        }
        return count;
    }
}
