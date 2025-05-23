package com.telefonica.androidlogger.domain

import android.content.Context

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun initAppLogger(
    context: Context,
    logCategories: List<LogCategory> = emptyList(),
    logToDisk: Boolean = true
) {
    /* Do nothing */
}

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun log(@LogPriority priority: Int, tag: String, message: String? = null, throwable: Throwable? = null) {

}
