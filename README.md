# Gradle

[![](https://jitpack.io/v/zj565061763/log.svg)](https://jitpack.io/#zj565061763/log)

# Demo

```kotlin
class AppLogger : FLogger() {
    override fun onCreate() {
        // 开启日志文件，限制最大50MB
        openLogFile(50)
    }
}
```

```
// kotlin
fLog<AppLogger> { "hello world!" }

// java
FLogger.get(AppLogger.class).info("onCreate");
```