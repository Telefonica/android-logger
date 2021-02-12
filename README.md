#  App Logger for Android

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://github.com/Telefonica/android-logger)
[![Version](https://maven-badges.herokuapp.com/maven-central/com.telefonica/androidlogger/badge.png)](https://search.maven.org/artifact/com.telefonica/androidlogger)
[![Support](https://img.shields.io/badge/Support-%3E%3D%20Android%205.0-brightgreen)](https://github.com/Telefonica/android-logger)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.4-blue.svg)](https://kotlinlang.org/docs/reference/whatsnew14.html)

App Logger that allows app logs visualization, classification, filtering, sharing and recording from the application where it is included.

## Installation

Inside the dependency block in the `build.gradle` of your application, add this line to add the library:

```gradle
dependencies {
    ...
    implementation 'com.telefonica.androidlogger:$version'
    ...
}
```

In case you want to disable any logging on release versions (and avoid inclusion of any transitive libraries), we provide a no-op version that will do nothing, without the need of handling this from your app code:

```gradle
dependencies {
    ...
    debugImplementation 'com.telefonica.androidlogger:$version'
    releaseImplementation 'com.telefonica.androidlogger-no-op:$version'
    ...
}
```

## Configuration

To initialize the logger, call `initAppLogger` in your Application's onCreate:

```kotlin
open class MyApplication : Application() {

    val categories: List<LogCategory> = listOf(
        LogCategory(
            name = "UI",
            color = Color.parseColor("#28A745"),
            logTags = listOf(
                "MyActivity",
                "MyFragment",
            )
        ),
        LogCategory(
            name = "Your Category",
            color = Color.parseColor("#17A2B8"),
            logTags = listOf(
                "MyStorage",
                "MyApiClient",
            )
        ),
    )
    
    override fun onCreate() {
        super.onCreate()
        initAppLogger(applicationContext, LoggerGroup.values().flatMap(transformation))
    }
}
```

And use `log` to write a log entry:
```kotlin
log(DEBUG, "MyActivity", "onCreate happened")
```

Finally, to open the logger activity, just get the intent calling `getLaunchIntent`
```kotlin
startActivity(getLaunchIntent(context))
```

## Demo app

There is a demo app of the logger viewer window in this repository. To compile the app manually run the [App](app) module in Android Studio.

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md)
