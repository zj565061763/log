package com.sd.log;

import com.sd.lib.log.FLogger;

public class AppLogger extends FLogger {
    @Override
    protected void onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50);
    }
}
