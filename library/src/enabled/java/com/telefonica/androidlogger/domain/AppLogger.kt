package com.telefonica.androidlogger.domain

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telefonica.androidlogger.io.AppConsoleLogger
import com.telefonica.androidlogger.io.AppFileLogger
import com.telefonica.androidlogger.io.executor.Executor
import com.telefonica.androidlogger.io.executor.TaskCallback
import java.util.concurrent.Executors

internal var appLoggerBLInstance: AppLoggerBL? = null

@JvmOverloads
fun initAppLogger(
    context: Context,
    logCategories: List<LogCategory> = emptyList(),
    logToDisk: Boolean = true
) {
    appLoggerBLInstance = AppLoggerBL(
        fileLogger = if (logToDisk) {
            AppFileLogger(context,Executor(Executors.newSingleThreadExecutor()))
        } else {
            null
        },
        consoleLogger = AppConsoleLogger()
    ).apply {
        init(context, logCategories)
    }
}

@JvmOverloads
fun log(@LogPriority priority: Int, tag: String, message: String? = null, throwable: Throwable? = null) {
    appLoggerBLInstance?.log(priority, tag, message ?: "", throwable)
}

internal fun getLogs(): LiveData<List<LogEntry>> =
    appLoggerBLInstance?.getLogs() ?: MutableLiveData<List<LogEntry>>().apply { value = emptyList() }

internal fun getCategories(): List<LogCategory> =
    appLoggerBLInstance?.categories ?: emptyList()

internal fun getPersistedLogs(callback: TaskCallback<Uri>) {
    appLoggerBLInstance?.getPersistedLogs(callback)
}

internal fun storeVisibleLogs(visibleLogs: String, callback: TaskCallback<Uri>) {
    appLoggerBLInstance?.storeVisibleLogs(visibleLogs, callback)
}