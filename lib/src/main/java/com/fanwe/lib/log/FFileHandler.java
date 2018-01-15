package com.fanwe.lib.log;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

public class FFileHandler extends FileHandler
{
    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

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

    private void init()
    {
        setFormatter(new FLogFormatter());
    }

    public static String getLogFilePath(String fileName, Context context)
    {
        final String logDirName = "flog";
        File dir = context.getExternalFilesDir(logDirName);
        if (dir == null)
        {
            dir = new File(context.getFilesDir(), logDirName);
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
