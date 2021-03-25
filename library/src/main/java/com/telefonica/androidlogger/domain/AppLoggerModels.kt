package com.telefonica.androidlogger.domain

import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import java.util.*

data class LogCategory(
        val name: String,
        @ColorInt val color: Int,
        val logTags: List<String>
)

@Retention(AnnotationRetention.SOURCE)
@IntDef(
        Log.VERBOSE,
        Log.DEBUG,
        Log.INFO,
        Log.WARN,
        Log.ERROR,
        Log.ASSERT
)
annotation class LogPriority

data class LogEntry(
        val id: Int,
        @LogPriority val priority: Int,
        val date: Date,
        val tag: String,
        val categories: List<LogCategory>?,
        val message: String
)
