#  App Logger for Android

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://github.com/Telefonica/android-logger)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue)](https://github.com/Telefonica/android-logger)
[![Support](https://img.shields.io/badge/Support-%3E%3D%20Android%205.0-brightgreen)](https://github.com/Telefonica/android-logger)
[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.4-blue.svg)](https://kotlinlang.org/docs/reference/whatsnew14.html)

App Logger that allows app logs visualization, classification, filtering, sharing and recording from the application where it is included.

## Installation

Inside the dependency block in the `build.gradle` of your application, add this line to add the library:

```gradle
dependencies {
    ...
    implementation 'com.telefonica.androidlogger:logger:$version'
    ...
}
```

In case you want to disable any logging on release versions (and avoid inclusion of any transitive libraries), we provide a no-op version that will do nothing, without the need of handling this from your app code:

```gradle
dependencies {
    ...
    debugImplementation 'com.telefonica.androidlogger:logger:$version'
    releaseImplementation 'com.telefonica.androidlogger:logger-noop:$version'
    ...
}
```

## Configuration

TODO

## Demo app

There is a demo app of the logger viewer window in this repository. To compile the app manually run the [App](app) module in Android Studio.

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md)
