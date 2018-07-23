package com.fanwe.lib.log;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

class SimpleFileHandler extends FileHandler
{
    public SimpleFileHandler(Context context) throws IOException, SecurityException
    {
        this(100 * MB, context);
    }

    public SimpleFileHandler(int limit, Context context) throws IOException, SecurityException
    {
        this(context.getPackageName(), limit, context);
    }

    /**
     * @param filename log文件名
     * @param limit    log文件大小
     * @param context
     * @throws IOException
     * @throws SecurityException
     */
    public SimpleFileHandler(String filename, int limit, Context context) throws IOException, SecurityException
    {
        super(getLogFilePath(filename, context), limit, 1, true);

        if (TextUtils.isEmpty(filename))
            throw new NullPointerException("filename is null or empty");
        mFilename = filename;
        mContext = context.getApplicationContext();

        init();
    }

    public static final int MB = 1024 * 1024;
    public static final String DEFAULT_DIR_NAME = "flog";

    private final Context mContext;
    private final String mFilename;

    private void init()
    {
        setFormatter(new SimpleLogFormatter());
    }

    /**
     * 删除日志文件
     */
    public final void deleteLogFile()
    {
        final File logDir = getLogFileDir(mContext);
        if (logDir == null)
            return;

        final File[] files = logDir.listFiles();
        if (files == null || files.length <= 0)
            return;

        for (File item : files)
        {
            final String name = item.getName();
            if (name.startsWith(mFilename))
                item.delete();
        }
    }

    private static String getLogFilePath(String fileName, Context context)
    {
        if (TextUtils.isEmpty(fileName))
            return null;

        final File dir = getLogFileDir(context);
        if (dir == null)
            return null;

        return dir.getAbsolutePath() + File.separator + fileName;
    }

    private static File getLogFileDir(Context context)
    {
        File dir = context.getExternalFilesDir(DEFAULT_DIR_NAME);
        if (dir == null)
            dir = new File(context.getFilesDir(), DEFAULT_DIR_NAME);

        if (dir.exists() || dir.mkdirs())
        {
            return dir;
        } else
        {
            return null;
        }
    }
}
