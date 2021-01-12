package com.telefonica.androidlogger.app

import android.app.Application
import com.telefonica.androidlogger.domain.initAppLogger

class AndroidLoggerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppLogger(this, LoggerConfig.categories)
    }
}
