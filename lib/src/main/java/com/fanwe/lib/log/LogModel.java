package com.fanwe.lib.log;

import android.text.TextUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogModel implements Serializable
{
    private static final long serialVersionUID = 0L;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

    /**
     * 日志时间戳（毫秒）
     */
    private long time;
    /**
     * 时间格式化
     */
    private String timeFormat;
    /**
     * 日志信息
     */
    private String message;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 日志等级
     */
    private String level;
    /**
     * 异常
     */
    private Throwable throwable;

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
        setTimeFormat(DATE_FORMAT.format(new Date(time)));
    }

    public String getTimeFormat()
    {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat)
    {
        this.timeFormat = timeFormat;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public void setThrowable(Throwable throwable)
    {
        this.throwable = throwable;
        if (throwable != null)
        {
            StringBuilder sb = new StringBuilder(throwable.toString());
            sb.append(getNextLine());

            StackTraceElement[] trace = throwable.getStackTrace();
            for (StackTraceElement item : trace)
            {
                sb.append("\tat " + item);
                sb.append(getNextLine());
            }

            setErrorMessage(sb.toString());
        }
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    private static String getNextLine()
    {
        return System.getProperty("line.separator");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getNextLine());
        sb.append(getTimeFormat()).append(":")
                .append("(").append(getLevel()).append(") ")
                .append(getMessage());
        if (!TextUtils.isEmpty(getErrorMessage()))
        {
            sb.append(getNextLine()).append(getErrorMessage());
        }

        return sb.toString();
    }
}
