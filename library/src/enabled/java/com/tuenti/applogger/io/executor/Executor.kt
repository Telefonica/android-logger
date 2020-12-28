package com.tuenti.applogger.io.executor

import android.util.Log
import java.util.concurrent.ExecutorService

internal class Executor(
    private val executor: ExecutorService
) {
    fun <T> submit(task: () -> T, callback: TaskCallback<T>) {
        executor.execute {
            var result: T? = null
            try {
                result = task.invoke()
            } catch (throwable: Throwable) {
                Log.e(LOGTAG, "Task Execution Failed", throwable)
            }
            callback.func?.invoke(result)
        }
    }

    private companion object {
        const val LOGTAG = "AppLoggerExecutor"
    }
}
