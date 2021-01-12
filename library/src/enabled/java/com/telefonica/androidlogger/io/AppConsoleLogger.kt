package com.telefonica.androidlogger.io

import android.util.Log
import com.telefonica.androidlogger.domain.LogPriority
import kotlin.math.min

internal open class AppConsoleLogger {

    open fun log(@LogPriority priority: Int, tag: String, message: String) {
        val logFunction: (String) -> Unit = when (priority) {
            Log.ASSERT -> { msg -> Log.wtf(tag, msg) }
            Log.ERROR -> { msg -> Log.e(tag, msg) }
            Log.WARN -> { msg -> Log.w(tag, msg) }
            Log.INFO -> { msg -> Log.i(tag, msg) }
            Log.DEBUG -> { msg -> Log.d(tag, msg) }
            else -> { msg -> Log.v(tag, msg) }
        }
        logWithSplits(message, logFunction)
    }

    private fun logWithSplits(message: String, logFunction: (String) -> Unit) {
        var i = 0
        while (i < message.length) {
            logFunction.invoke(message.substring(i, min(message.length, i + MAX_LOGCAT_LINE_LENGTH)))
            i += MAX_LOGCAT_LINE_LENGTH
        }
    }

    private companion object {
        const val MAX_LOGCAT_LINE_LENGTH = 4000
    }
}