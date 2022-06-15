package com.sd.lib.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class SimpleLogFormatter extends Formatter {
    private final Date mDate = new Date();
    private final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        mDate.setTime(record.getMillis());
        final String date = mDateFormat.format(mDate);
        final String message = formatMessage(record);

        String error = "";
        final Throwable throwable = record.getThrown();
        if (throwable != null) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);

            printWriter.println();
            throwable.printStackTrace(printWriter);
            printWriter.close();

            error = stringWriter.toString();
        }

        final StringBuilder builder = new StringBuilder();
        // 日期
        builder.append(date);
        // 日志等级
        builder.append(" (").append(record.getLevel()).append(") ");
        // 日志信息
        builder.append(message);
        // 异常信息
        builder.append(error);
        // 换行
        builder.append(getNextLine());
        return builder.toString();
    }

    private static String getNextLine() {
        return System.getProperty("line.separator");
    }
}
