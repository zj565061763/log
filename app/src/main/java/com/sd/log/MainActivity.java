package com.sd.log;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.lib.log.FLogger;
import com.sd.lib.log.ext.FLogBuilder;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FLogger.get(AppLogger.class).info("onCreate");

        // 删除所有日志文件
//        FLogger.deleteLogFile();

        // 删除日志文件，saveDays等于1，表示保留1天的日志，即保留今天的日志，删除今天之前的所有日志
        FLogger.deleteLogFile(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FLogger.get(AppLogger.class).info("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        final View textView = findViewById(R.id.tv_content);

        FLogger.get(AppLogger.class).info("onResume" + new FLogBuilder().nextLine()
                .pair("textView", textView).nextLine()
                .pairHash("textView hash", textView).nextLine()
                .pairStr("textView string", textView).nextLine()
                .instance(textView).nextLine()
                .instanceStr(textView));
    }

    @Override
    protected void onStop() {
        super.onStop();
        FLogger.get(AppLogger.class).info(new FLogBuilder()
                .clazz(MainActivity.class)
                .clazzFull(MainActivity.class)
                .add("onStop")
                .toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FLogger.get(AppLogger.class).info("onDestroy");
    }
}
