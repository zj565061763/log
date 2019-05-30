package com.sd.lib.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class SimpleLogFormatter extends Formatter
{
    private final Date mDate = new Date();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final StringBuilder mStringBuilder = new StringBuilder();

    @Override
    public String format(LogRecord record)
    {
        if (mStringBuilder.length() > 0)
            mStringBuilder.delete(0, mStringBuilder.length());

        mDate.setTime(record.getMillis());
        final String date = mDateFormat.format(mDate);

        final String message = formatMessage(record);

        String throwable = "";
        if (record.getThrown() != null)
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        // 日期
        mStringBuilder.append(date);
        // 日志等级
        mStringBuilder.append(" (").append(record.getLevel()).append(") ");
        // 日志信息
        mStringBuilder.append(message);
        // 异常信息
        mStringBuilder.append(throwable);
        // 换行
        mStringBuilder.append(getNextLine());

        return mStringBuilder.toString();
    }

    private static String getNextLine()
    {
        return System.getProperty("line.separator");
    }
}
