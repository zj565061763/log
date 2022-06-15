package com.sd.lib.log;

import android.content.Context;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
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
    private static Context sContext = null;

    private final Logger mLogger;

    private int mLogFileLimit;
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
     * 初始化
     */
    public synchronized static void init(Context context) {
        if (context == null) return;
        if (sContext == null) {
            sContext = context.getApplicationContext();
        }
    }

    private static Context getContext() {
        if (sContext == null) {
            throw new IllegalStateException("You should call FLogger.init(Context) before this");
        }
        return sContext;
    }

    /**
     * 获得指定的日志类对象，内部会保存日志对象
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
        if (mFileHandler != null) {
            mFileHandler.setLevel(level);
        }
    }

    /**
     * 开启日志文件
     *
     * @param limitMB 文件大小限制(MB)
     */
    public final void openLogFile(int limitMB) {
        openLogFileInternal(getContext(), limitMB);
    }

    /**
     * 关闭日志文件
     */
    public final void closeLogFile() {
        closeLogFileInternal();
    }

    /**
     * 开启日志文件
     *
     * @param limitMB 文件大小限制(MB)
     */
    private synchronized void openLogFileInternal(Context context, int limitMB) {
        if (limitMB <= 0) {
            throw new IllegalArgumentException("limitMB must greater than 0");
        }

        if (mFileHandler == null || mLogFileLimit != limitMB) {
            closeLogFileInternal();

            try {
                mFileHandler = new SimpleFileHandler(context, mLogger.getName(), limitMB);
                mFileHandler.setFormatter(new SimpleLogFormatter());
                mFileHandler.setLevel(mLogger.getLevel());

                mLogger.addHandler(mFileHandler);
                mLogFileLimit = limitMB;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭日志文件
     */
    private synchronized void closeLogFileInternal() {
        if (mFileHandler != null) {
            mFileHandler.close();
            mLogger.removeHandler(mFileHandler);
            mFileHandler = null;
        }
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
     */
    public static void deleteLogFile() {
        deleteLogFile(0);
    }

    /**
     * 删除日志文件
     *
     * @param saveDays 要保留的日志天数，如果 saveDays <= 0 ，则删除所有日志
     */
    public static synchronized void deleteLogFile(int saveDays) {
        final File dir = SimpleFileHandler.getLogFileDir(getContext());
        if (!dir.exists()) {
            return;
        }

        if (saveDays <= 0) {
            // 删除全部日志
            clearLogger();
            deleteFileOrDir(dir);
            return;
        }

        final File[] files = dir.listFiles();
        if (files == null || files.length <= 0) {
            return;
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -(saveDays - 1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final long limitTime = calendar.getTime().getTime();
        final DateFormat format = SimpleFileHandler.newDateFormat();

        final List<File> listExpired = new ArrayList<>();
        for (File item : files) {
            if (item.isFile()) {
                deleteFileOrDir(item);
                continue;
            }

            try {
                final long fileTime = format.parse(item.getName()).getTime();
                if (fileTime < limitTime) {
                    listExpired.add(item);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                deleteFileOrDir(item);
            }
        }

        if (listExpired.isEmpty()) {
            return;
        }

        // 删除之前要先清空日志对象
        clearLogger();
        for (File item : listExpired) {
            deleteFileOrDir(item);
        }
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
