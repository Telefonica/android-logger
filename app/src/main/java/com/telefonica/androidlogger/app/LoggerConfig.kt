package com.telefonica.androidlogger.app

import android.graphics.Color
import com.telefonica.androidlogger.domain.LogCategory

object LoggerConfig {

    /* Some testing log tags. Just for demo */
    const val LOGTAG_MY_FRAGMENT = "MyFragment"
    const val LOGTAG_MY_ACTIVITY = "MyActivity"
    const val LOGTAG_MY_SERVICE = "MyService"
    const val LOGTAG_YOUR_STORAGE = "YourStorage"
    const val LOGTAG_YOUR_API_CLIENT = "YourApiClient"
    const val LOGTAG_THEIR_VIEW_MODEL = "TheirViewModel"

    val categories: List<LogCategory> = listOf(
        LogCategory(
            name = "My Category",
            color = Color.parseColor("#28A745"),
            logTags = listOf(
                LOGTAG_MY_ACTIVITY,
                LOGTAG_MY_FRAGMENT,
                LOGTAG_MY_SERVICE
            )
        ),
        LogCategory(
            name = "Your Category",
            color = Color.parseColor("#17A2B8"),
            logTags = listOf(
                LOGTAG_YOUR_STORAGE,
                LOGTAG_YOUR_API_CLIENT
            )
        ),
    )
}