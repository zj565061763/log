# Gradle

[![](https://jitpack.io/v/zj565061763/log.svg)](https://jitpack.io/#zj565061763/log)

# Sample

```kotlin
class AppLogger : FLogger() {
    override fun onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50)
    }
}
```

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 传统写法
        FLogger.get(AppLogger::class.java).info("info")

        // Kotlin写法
        flogD<AppLogger> { "debug" }
        flogI<AppLogger> { "info" }
        flogW<AppLogger> { "warning" }
        flogE<AppLogger> { "error" }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 删除日志文件
        FLogger.deleteLogFile()
    }
}
```