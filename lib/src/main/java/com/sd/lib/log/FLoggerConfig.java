package com.sd.lib.log;

public class FLoggerConfig
{
    private static FLoggerConfig sConfig;

    public final int mLogDay;

    private FLoggerConfig(Builder builder)
    {
        mLogDay = builder.mLogDay <= 0 ? 7 : builder.mLogDay;
    }

    public static FLoggerConfig get()
    {
        if (sConfig == null)
        {
            synchronized (FLoggerConfig.class)
            {
                init(new Builder().build());
            }
        }
        return sConfig;
    }

    /**
     * 初始化
     *
     * @param config
     */
    public static synchronized void init(FLoggerConfig config)
    {
        if (config == null)
            throw new IllegalArgumentException("config is null");

        if (sConfig != null)
            throw new RuntimeException(FLoggerConfig.class.getSimpleName() + " has been init");

        sConfig = config;
    }

    public static class Builder
    {
        private int mLogDay;

        public Builder setLogDay(int logDay)
        {
            mLogDay = logDay;
            return this;
        }

        public FLoggerConfig build()
        {
            return new FLoggerConfig(this);
        }
    }
}
