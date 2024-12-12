package com.telefonica.androidlogger.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telefonica.androidlogger.io.AppConsoleLogger
import com.telefonica.androidlogger.io.AppFileLogger
import com.telefonica.androidlogger.io.executor.TaskCallback
import java.util.*
import kotlin.collections.ArrayList

internal open class AppLoggerBL(
        private val fileLogger: AppFileLogger?,
        private val consoleLogger: AppConsoleLogger
) {
    private val logs: LinkedList<LogEntry> = LinkedList()
    private val logsData: MutableLiveData<List<LogEntry>> = MutableLiveData()

    var categories: List<LogCategory> = emptyList()
        private set
    private var tagsMap: Map<String, List<LogCategory>> = emptyMap()
    private var idCounter: Int = 0

    open fun init(appContext: Context, logCategories: List<LogCategory>) {
        categories = logCategories
        tagsMap = categories
                .flatMap { category ->
                    category.logTags.map { tag ->
                        tag to category
                    }
                }.groupBy({ it.first }, { it.second })
        logsData.value = emptyList()
        fileLogger?.init()
    }

    open fun log(@LogPriority priority: Int, tag: String, message: String, throwable: Throwable?) {
        val messageToLog: String = getLogMessage(message, throwable)
        addLog(
                LogEntry(
                        id = ++idCounter,
                        priority = priority,
                        tag = tag,
                        date = Date(),
                        categories = tagsMap[tag],
                        message = messageToLog
                )
        )
        fileLogger?.log(priority, tag, messageToLog)
        consoleLogger.log(priority, tag, messageToLog)
    }

    open fun getLogs(): LiveData<List<LogEntry>> =
            logsData

    open fun getPersistedLogs(callback: TaskCallback<Uri>) {
        fileLogger?.getReport(callback)
    }

    fun storeVisibleLogs(visibleLogs: String, callback: TaskCallback<Uri>) {
        fileLogger?.storeVisibleLogs(visibleLogs, callback)
    }

    private fun addLog(logEntry: LogEntry) {
        synchronized(logs) {
            logs.addLast(logEntry)
            if (logs.size > MAX_LOGS_IN_MEMORY) {
                logs.removeFirst()
            }
            logsData.postValue(ArrayList(logs))
        }
    }

    private fun getLogMessage(msg: String, throwable: Throwable?): String {
        return "${Thread.currentThread().id} - $msg ${throwable?.let { "\n${Log.getStackTraceString(it)}" } ?: ""}"
    }

    private companion object {
        const val MAX_LOGS_IN_MEMORY = 5000
    }
}