package com.sd.log;

import android.app.Application;

import com.sd.lib.log.FLoggerConfig;

public class App extends Application
{
    private static App sInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;

        FLoggerConfig.init(
                new FLoggerConfig.Builder()
                        // 设置默认保存最近5天的日志
                        .setLogDay(5)
                        .build());
    }

    public static App getInstance()
    {
        return sInstance;
    }
}
