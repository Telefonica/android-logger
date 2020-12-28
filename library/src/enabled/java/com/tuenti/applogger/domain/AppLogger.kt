package com.tuenti.applogger.domain

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tuenti.applogger.io.AppConsoleLogger
import com.tuenti.applogger.io.AppFileLogger
import com.tuenti.applogger.io.executor.Executor
import com.tuenti.applogger.io.executor.TaskCallback
import java.util.concurrent.Executors

internal var appLoggerBLInstance: AppLoggerBL? = null

fun initAppLogger(context: Context, logCategories: List<LogCategory>) {
    appLoggerBLInstance = AppLoggerBL(
        fileLogger = AppFileLogger(
            appContext = context,
            executor = Executor(Executors.newSingleThreadExecutor())),
        consoleLogger = AppConsoleLogger()
    ).apply {
        init(context, logCategories)
    }
}

@JvmOverloads
fun log(@LogPriority priority: Int, tag: String, message: String? = null, throwable: Throwable? = null) {
    appLoggerBLInstance?.log(priority, tag, message ?: "", throwable)
}

fun getLogs(): LiveData<List<LogEntry>> =
    appLoggerBLInstance?.getLogs() ?: MutableLiveData<List<LogEntry>>().apply { value = emptyList() }

fun getCategories(): List<LogCategory> =
    appLoggerBLInstance?.categories ?: emptyList()

fun getPersistedLogs(callback: TaskCallback<Uri>) {
    appLoggerBLInstance?.getPersistedLogs(callback)
}