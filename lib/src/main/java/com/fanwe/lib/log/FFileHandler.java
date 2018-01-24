package com.fanwe.lib.log;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

public class FFileHandler extends FileHandler
{
    public static final int KB = 1024;
    public static final int MB = 1024 * KB;
    public static final String DEFAULT_DIR_NAME = "flog";

    public FFileHandler() throws IOException, SecurityException
    {
        init();
    }

    public FFileHandler(String pattern) throws IOException, SecurityException
    {
        super(pattern);
        init();
    }

    public FFileHandler(String pattern, boolean append) throws IOException, SecurityException
    {
        super(pattern, append);
        init();
    }

    public FFileHandler(String pattern, int limit, int count) throws IOException, SecurityException
    {
        super(pattern, limit, count);
        init();
    }

    public FFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException
    {
        super(pattern, limit, count, append);
        init();
    }

    // add

    /**
     * @param fileName log文件名
     * @param limit    log文件大小
     * @param context
     * @throws IOException
     */
    public FFileHandler(String fileName, int limit, Context context) throws IOException
    {
        this(getLogFilePath(fileName, context),
                limit,
                1,
                true);
    }

    private void init()
    {
        setFormatter(new FLogFormatter());
    }

    public static String getLogFilePath(String fileName, Context context)
    {
        if (TextUtils.isEmpty(fileName))
        {
            return null;
        }

        File dir = context.getExternalFilesDir(DEFAULT_DIR_NAME);
        if (dir == null)
        {
            dir = new File(context.getFilesDir(), DEFAULT_DIR_NAME);
        }

        if (dir.exists() || dir.mkdirs())
        {
            return dir.getAbsolutePath() + File.separator + fileName;
        } else
        {
            return null;
        }
    }
}
