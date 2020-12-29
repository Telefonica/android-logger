package com.telefonica.androidlogger.domain

import android.content.Context

@Suppress("UNUSED_PARAMETER")
fun initAppLogger(context: Context, logCategories: List<LogCategory>) {

}

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun log(@LogPriority priority: Int, tag: String, message: String? = null, throwable: Throwable? = null) {

}
