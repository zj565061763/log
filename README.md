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

    init {
        // 调试模式，tag：FLogger
        FLogger.isDebug = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 传统写法
        FLogger.get(AppLogger::class.java).info("onCreate")

        // Kotlin写法
        fLog<AppLogger> { "onCreate" }
    }
}
```