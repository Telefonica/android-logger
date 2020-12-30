package com.telefonica.androidlogger.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.telefonica.androidlogger.domain.LogPriority
import com.telefonica.androidlogger.domain.log
import com.telefonica.androidlogger.ui.getLaunchIntent
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.button_add_logs).setOnClickListener {
            for (i in 1..NUMBER_OF_LOGS_TO_ADD) {
                log(
                    getRandomLogPriority(),
                    getRandomLogTag(),
                    getRandomLogMessage(),
                    getRandomThrowable()
                )
            }
        }
        findViewById<Button>(R.id.button_show_logger).setOnClickListener {
            startActivity(getLaunchIntent(this, emptyList()))
        }
    }

    @LogPriority
    private fun getRandomLogPriority() = Random.nextInt(Log.ASSERT) + Log.VERBOSE

    private fun getRandomLogMessage() =
        if (Random.nextBoolean()) "This is a short log message" else """
                    This is a long log message. A long log message can be expanded by clicking on
                    this long entry. In this way, you would be able to see whole log message. Also 
                    if you do a long press on the entry, log message content will be copied to the 
                    clipboard
                """.trimIndent()

    private fun getRandomLogTag() =
        when (Random.nextInt(6)) {
            0 -> LoggerConfig.LOGTAG_MY_FRAGMENT
            1 -> LoggerConfig.LOGTAG_MY_ACTIVITY
            2 -> LoggerConfig.LOGTAG_MY_SERVICE
            3 -> LoggerConfig.LOGTAG_YOUR_STORAGE
            4 -> LoggerConfig.LOGTAG_YOUR_API_CLIENT
            else -> LoggerConfig.LOGTAG_THEIR_VIEW_MODEL
        }

    private fun getRandomThrowable(): Throwable? =
        if (Random.nextBoolean()) RuntimeException("Ooops!").fillInStackTrace() else null

    private companion object {
        const val NUMBER_OF_LOGS_TO_ADD = 20
    }
}