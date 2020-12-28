package com.tuenti.applogger.domain

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tuenti.applogger.io.executor.TaskCallback

@Suppress("UNUSED_PARAMETER")
fun initAppLogger(context: Context, logCategories: List<LogCategory>) {

}

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun log(@LogPriority priority: Int, tag: String, message: String? = null, throwable: Throwable? = null) {

}

fun getLogs(): LiveData<List<LogEntry>> =
    MutableLiveData<List<LogEntry>>().apply { value = emptyList() }

fun getCategories(): List<LogCategory> =
    emptyList()

@Suppress("UNUSED_PARAMETER")
fun getPersistedLogs(callback: TaskCallback<Uri>) {

}