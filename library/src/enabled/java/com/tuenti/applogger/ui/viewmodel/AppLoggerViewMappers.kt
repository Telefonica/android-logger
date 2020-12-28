package com.tuenti.applogger.ui.viewmodel

import android.util.Log
import com.tuenti.applogger.domain.LogCategory
import com.tuenti.applogger.domain.LogEntry
import java.text.SimpleDateFormat

internal fun LogEntry.toViewModel(
    dateFormat: SimpleDateFormat,
    expanded: Boolean
) = LogEntryViewModel(
    id = id,
    priority = priority.toLogPriority(),
    header = "${dateFormat.format(date)} - $tag",
    category = category?.toViewModel(),
    message = message,
    expanded = expanded
)

internal fun LogCategory.toViewModel() = LogCategoryViewModel(
    name = name,
    color = color
)

internal fun Int.toLogPriority() =
    when {
        this < Log.VERBOSE -> LogPriorityViewModel.VERBOSE
        this == Log.VERBOSE -> LogPriorityViewModel.VERBOSE
        this == Log.DEBUG -> LogPriorityViewModel.DEBUG
        this == Log.INFO -> LogPriorityViewModel.INFO
        this == Log.WARN -> LogPriorityViewModel.WARN
        this == Log.ERROR -> LogPriorityViewModel.ERROR
        else -> LogPriorityViewModel.ASSERT
    }