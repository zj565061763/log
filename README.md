# About
简单的日志封装

# Gradle
[![](https://jitpack.io/v/zj565061763/log.svg)](https://jitpack.io/#zj565061763/log)

# 使用
```java
/**
 * 创建一个日志类，日志的tag为当前类的全类名
 */
public class AppLogger extends FLogger
{
    /**
     * 当前日志对象被创建后会回调此方法
     */
    @Override
    protected void onCreate()
    {
        /**
         * 打开日志文件功能，设置最大50MB，日志文件位于sdcard/Android/data/包名/files/flog/当天日期/当前对象的全类名.log
         */
        openLogFile(App.getInstance(), 50);
    }
}
```

```java
public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * FLogger.get(AppLogger.class)获得指定的日志类对象，
         * 内部会保存日志对象，在对象未被移除之前返回的是同一个对象
         */
        FLogger.get(AppLogger.class).info("onCreate");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FLogger.get(AppLogger.class).info("onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        FLogger.get(AppLogger.class).info("onResume");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FLogger.get(AppLogger.class).info("onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.get(AppLogger.class).info("onDestroy");

        // 删除所有日志文件
//        FLogger.deleteLogFile(this);

        // 删除2天之前的日志目录
        FLogger.deleteExpiredLogDir(this, 2);
    }
}
```
