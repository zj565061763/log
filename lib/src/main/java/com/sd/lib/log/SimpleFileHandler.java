package com.sd.lib.log;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

class SimpleFileHandler extends FileHandler
{
    private static final int MB = 1024 * 1024;
    private static final String DEFAULT_DIR_NAME = "flog";
    private static final String FILE_SUFFIX = ".log";

    private final Context mContext;
    private final String mFilename;

    /**
     * @param context
     * @param filename log文件名
     * @param limitMB  log文件大小限制(单位MB)
     * @throws IOException
     * @throws SecurityException
     */
    public SimpleFileHandler(Context context, String filename, int limitMB) throws IOException, SecurityException
    {
        super(getLogFilePath(filename + FILE_SUFFIX, context), limitMB * MB, 1, true);

        if (TextUtils.isEmpty(filename))
            throw new NullPointerException("filename is null or empty");

        if (limitMB <= 0)
            throw new IllegalArgumentException("limitMB must greater than 0");

        final int max = Integer.MAX_VALUE / SimpleFileHandler.MB;
        if (limitMB > max)
            throw new IllegalArgumentException("limitMB must less than " + max);

        mFilename = filename + FILE_SUFFIX;
        mContext = context.getApplicationContext();
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
