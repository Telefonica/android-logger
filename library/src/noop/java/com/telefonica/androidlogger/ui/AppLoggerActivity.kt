package com.telefonica.androidlogger.ui

import android.content.Context
import android.content.Intent
import com.telefonica.androidlogger.domain.LogCategory

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun getLaunchIntent(context: Context, categories: List<LogCategory> = emptyList()): Intent? =
    null