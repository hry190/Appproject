# Gradle Wrapper

本项目使用 Gradle 8.10.2。需要 `gradle-wrapper.jar` 才能运行 `./gradlew`。

## 生成方式

任选一种：

### 方式 A：用 Android Studio 打开项目
Android Studio 首次同步时会自动下载并创建 `gradle/wrapper/gradle-wrapper.jar`。

### 方式 B：手动下载
从 Gradle 官方仓库下载对应版本：
https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
解压后将 `lib/plugins/gradle-wrapper-*.jar` 改名为 `gradle-wrapper.jar` 放到此处。

### 方式 C：本地有 gradle
```bash
gradle wrapper --gradle-version 8.10.2
```