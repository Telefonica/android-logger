package com.telefonica.androidlogger.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.telefonica.androidlogger.domain.LogPriority
import com.telefonica.androidlogger.domain.log
import com.telefonica.androidlogger.ui.getLaunchIntent
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.button_add_logs).setOnClickListener {
            repeat(NUMBER_OF_LOGS_TO_ADD) {
                log(
                    getRandomLogPriority(),
                    getRandomLogTag(),
                    getRandomLogMessage(),
                    getRandomThrowable()
                )
            }
        }
        findViewById<Button>(R.id.button_show_logger).setOnClickListener {
            startActivity(
                    getLaunchIntent(this,
                    listOf(
                            LoggerConfig.categories[1],
                            LoggerConfig.categories[0],
                            LoggerConfig.categories[2])
                    )
            )
        }
    }

    @LogPriority
    private fun getRandomLogPriority(): Int = Random.nextInt(Log.VERBOSE..Log.ASSERT)

    private fun getRandomLogMessage(): String =
        if (Random.nextBoolean()) "This is a short log message" else """
                    This is a long log message. A long log message can be expanded by clicking on
                    this long entry. In this way, you would be able to see whole log message. Also 
                    if you do a long press on the entry, log message content will be copied to the 
                    clipboard
                """.trimIndent()

    private fun getRandomLogTag(): String =
        when (Random.nextInt(1..6)) {
            1 -> LoggerConfig.LOGTAG_MY_FRAGMENT
            2 -> LoggerConfig.LOGTAG_MY_ACTIVITY
            3 -> LoggerConfig.LOGTAG_MY_SERVICE
            4 -> LoggerConfig.LOGTAG_YOUR_STORAGE
            5 -> LoggerConfig.LOGTAG_YOUR_API_CLIENT
            else -> LoggerConfig.LOGTAG_THEIR_VIEW_MODEL
        }

    private fun getRandomThrowable(): Throwable? =
        if (Random.nextBoolean()) RuntimeException("Ooops!").fillInStackTrace() else null

    private companion object {
        const val NUMBER_OF_LOGS_TO_ADD = 20
    }
}
