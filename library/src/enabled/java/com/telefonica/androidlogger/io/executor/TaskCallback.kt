package com.telefonica.androidlogger.io.executor

class TaskCallback<T>(
    func: (T?) -> Unit
) {
    @Volatile
    internal var func: ((T?) -> Unit)? = func

    fun cancel() {
        func = null
    }
}