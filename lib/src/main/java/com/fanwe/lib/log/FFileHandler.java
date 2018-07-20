package com.fanwe.lib.log;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

public class FFileHandler extends FileHandler
{
    public static final int MB = 1024 * 1024;
    public static final String DEFAULT_DIR_NAME = "flog";

    public FFileHandler(Context context) throws IOException, SecurityException
    {
        this(context.getPackageName(),
                100 * MB,
                context);
    }

    public FFileHandler(int limit, Context context) throws IOException, SecurityException
    {
        this(context.getPackageName(),
                limit,
                context);
    }

    /**
     * @param fileName log文件名
     * @param limit    log文件大小
     * @param context
     * @throws IOException
     * @throws SecurityException
     */
    public FFileHandler(String fileName, int limit, Context context) throws IOException, SecurityException
    {
        super(getLogFilePath(fileName, context),
                limit,
                1,
                true);

        init();
    }

    private void init()
    {
        setFormatter(new FLogFormatter());
    }

    public static String getLogFilePath(String fileName, Context context)
    {
        if (TextUtils.isEmpty(fileName))
            return null;

        File dir = context.getExternalFilesDir(DEFAULT_DIR_NAME);
        if (dir == null)
            dir = new File(context.getFilesDir(), DEFAULT_DIR_NAME);

        if (dir.exists() || dir.mkdirs())
        {
            return dir.getAbsolutePath() + File.separator + fileName;
        } else
        {
            return null;
        }
    }
}
