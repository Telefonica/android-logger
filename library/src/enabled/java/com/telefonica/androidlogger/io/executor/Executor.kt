package com.telefonica.androidlogger.io.executor

import android.util.Log
import java.util.concurrent.ExecutorService

internal class Executor(
    private val executor: ExecutorService
) {
    @Suppress("TooGenericExceptionCaught")
    fun <T> submit(task: () -> T, callback: TaskCallback<T>) {
        executor.execute {
            var result: T? = null
            try {
                result = task.invoke()
            } catch (e: Exception) {
                Log.e(LOGTAG, "Task Execution Failed", e)
            }
            callback.func?.invoke(result)
        }
    }

    private companion object {
        const val LOGTAG = "AppLoggerExecutor"
    }
}
