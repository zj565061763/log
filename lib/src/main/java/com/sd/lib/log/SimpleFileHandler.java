package com.sd.lib.log;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

class SimpleFileHandler extends FileHandler {
    private static final int MB = 1024 * 1024;
    private static final String DIR_NAME = "flog";
    private static final String FILE_SUFFIX = ".log";

    /**
     * @param filename log文件名
     * @param limitMB  log文件大小限制(单位MB)
     */
    public SimpleFileHandler(Context context, String filename, int limitMB) throws IOException, SecurityException {
        super(getLogFilePath(context, filename + FILE_SUFFIX), limitMB * MB, 1, true);
    }

    private static String getLogFilePath(Context context, String fileName) {
        final File dir = getLogDayFileDir(context);
        if (dir == null) {
            return null;
        }

        return dir.getAbsolutePath() + File.separator + fileName;
    }

    private static File getLogDayFileDir(Context context) {
        File dir = getLogFileDir(context);
        if (dir == null) {
            return null;
        }

        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        final String formatDate = format.format(new Date());

        dir = new File(dir, formatDate);
        if (checkDir(dir)) {
            return dir;
        }

        return null;
    }

    static File getLogFileDir(Context context) {
        File dir = context.getExternalFilesDir(DIR_NAME);
        if (checkDir(dir)) {
            return dir;
        }

        dir = new File(context.getFilesDir(), DIR_NAME);
        if (checkDir(dir)) {
            return dir;
        }

        return null;
    }

    private static boolean checkDir(File dir) {
        if (dir == null) return false;
        return dir.exists() || dir.mkdirs();
    }
}
