package com.sd.lib.log;

import android.content.Context;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FLogger {
    private static final Map<Class<?>, FLogger> MAP_LOGGER = new ConcurrentHashMap<>();
    private static Level sGlobalLevel = Level.ALL;

    private final Logger mLogger;

    private int mLogFileLimit;
    private Level mLogFileLevel;
    private SimpleFileHandler mFileHandler;

    protected FLogger() {
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
     * 内部会保存日志对象，在对象未被移除之前返回的是同一个对象
     */
    public synchronized static <T extends FLogger> FLogger get(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null when get logger");
        }
        if (clazz == FLogger.class) {
            throw new IllegalArgumentException("clazz must not be " + FLogger.class);
        }

        final FLogger cache = MAP_LOGGER.get(clazz);
        if (cache != null) {
            return cache;
        }

        try {
            final FLogger logger = clazz.newInstance();
            MAP_LOGGER.put(clazz, logger);
            logger.onCreate();
            return logger;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空所有日志对象
     */
    public synchronized static void clearLogger() {
        for (FLogger item : MAP_LOGGER.values()) {
            item.closeLogFile();
        }
        MAP_LOGGER.clear();
    }

    /**
     * 设置全局日志输出等级
     */
    public synchronized static void setGlobalLevel(Level level) {
        if (level == null) {
            level = Level.ALL;
        }

        if (sGlobalLevel != level) {
            sGlobalLevel = level;
            clearLogger();
        }
    }

    /**
     * 返回当前的日志等级
     */
    public Level getLevel() {
        return mLogger.getLevel();
    }

    /**
     * 设置日志等级
     */
    public synchronized final void setLevel(Level level) {
        if (level == null) {
            level = Level.ALL;
        }

        mLogger.setLevel(level);

        if (mLogFileLevel == null) {
            if (mFileHandler != null) {
                mFileHandler.setLevel(level);
            }
        }
    }

    /**
     * 设置写入文件的日志等级
     *
     * @param level null-表示跟随默认的日志等级
     */
    public synchronized final void setFileLevel(Level level) {
        if (mLogFileLevel != level) {
            mLogFileLevel = level;

            if (mFileHandler != null) {
                mFileHandler.setLevel(level != null ? level : mLogger.getLevel());
            }
        }
    }

    /**
     * 打开日志文件功能
     *
     * @param limitMB 文件大小限制(MB)
     */
    public synchronized final void openLogFile(Context context, int limitMB) {
        if (limitMB <= 0) {
            throw new IllegalArgumentException("limitMB must greater than 0");
        }

        if (mFileHandler == null || mLogFileLimit != limitMB) {
            final Context appContext = context.getApplicationContext();
            closeLogFileInternal();

            try {
                mFileHandler = new SimpleFileHandler(appContext, mLogger.getName(), limitMB);
                mFileHandler.setFormatter(new SimpleLogFormatter());
                mFileHandler.setLevel(mLogFileLevel != null ? mLogFileLevel : mLogger.getLevel());

                mLogger.addHandler(mFileHandler);
                mLogFileLimit = limitMB;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭日志文件功能
     */
    public final void closeLogFile() {
        closeLogFileInternal();
    }

    private synchronized void closeLogFileInternal() {
        if (mFileHandler != null) {
            mFileHandler.close();
            mLogger.removeHandler(mFileHandler);
            mFileHandler = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeLogFileInternal();
    }

    //---------- log start ----------

    public final void info(String msg) {
        log(Level.INFO, msg);
    }

    public final void warning(String msg) {
        log(Level.WARNING, msg);
    }

    public final void severe(String msg) {
        log(Level.SEVERE, msg);
    }

    public final void severe(String msg, Throwable throwable) {
        log(Level.SEVERE, msg, throwable);
    }

    public final void log(Level level, String msg) {
        mLogger.log(level, msg);
    }

    public final void log(Level level, String msg, Throwable thrown) {
        mLogger.log(level, msg, thrown);
    }

    //---------- log end ----------

    /**
     * 删除所有日志文件
     *
     * @param context
     */
    public static synchronized void deleteLogFile(Context context) {
        final File dir = SimpleFileHandler.getLogFileDir(context);
        if (dir == null) {
            return;
        }

        if (!dir.exists()) {
            return;
        }

        clearLogger();
        deleteFileOrDir(dir);
    }

    /**
     * 删除过期的日志
     *
     * @param context
     * @param saveDays 要保留的日志天数
     * @return 被删除的日志天数
     */
    public static synchronized int deleteExpiredLogDir(Context context, int saveDays) {
        if (saveDays <= 0) {
            return 0;
        }

        final File dir = SimpleFileHandler.getLogFileDir(context);
        if (dir == null) {
            return 0;
        }

        final File[] files = dir.listFiles();
        if (files == null || files.length <= 0) {
            return 0;
        }

        final int logDay = saveDays - 1;
        final Calendar calendar = Calendar.getInstance();
        if (logDay > 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -logDay);
        }

        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        final String dateLimitString = format.format(calendar.getTime());

        long dateLimit = 0;
        try {
            dateLimit = format.parse(dateLimitString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

        final List<File> listExpired = new ArrayList<>(5);
        for (File item : files) {
            if (item.isFile()) {
                item.delete();
            } else if (item.isDirectory()) {
                final String filename = item.getName();
                try {
                    final long dateFile = format.parse(filename).getTime();
                    final long dateDelta = dateFile - dateLimit;
                    if (dateDelta < 0) {
                        listExpired.add(item);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    deleteFileOrDir(item);
                    continue;
                }
            }
        }

        if (listExpired.isEmpty()) {
            return 0;
        }

        // 删除之前要先清空日志对象
        clearLogger();
        for (File item : listExpired) {
            deleteFileOrDir(item);
        }

        return listExpired.size();
    }

    private static boolean deleteFileOrDir(File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }

        final File[] files = file.listFiles();
        if (files != null) {
            for (File item : files) {
                deleteFileOrDir(item);
            }
        }
        return file.delete();
    }
}
