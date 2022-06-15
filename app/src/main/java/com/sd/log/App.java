package com.sd.log;

import android.app.Application;

import com.sd.lib.log.FLogger;

public class App extends Application {
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        // 初始化
        FLogger.init(this);
    }

    public static App getInstance() {
        return sInstance;
    }
}
