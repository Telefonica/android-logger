package com.telefonica.androidlogger.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.telefonica.androidlogger.domain.initAppLogger
import com.telefonica.androidlogger.domain.log
import com.telefonica.androidlogger.ui.getLaunchIntent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAppLogger(this, emptyList())
        setupViews()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.button_add_logs).setOnClickListener {
            log(Log.VERBOSE, "Tag 1", "My log 1")
            log(Log.DEBUG, "Tag 2", "My log 2")
            log(Log.INFO, "Tag 3", "My log 3")
            log(Log.WARN, "Tag 4", "My log 4")
            log(Log.ERROR, "Tag 5", "My log 5")
            log(Log.ASSERT, "Tag 6", "My log 6")
        }
        findViewById<Button>(R.id.button_show_logger).setOnClickListener {
            startActivity(getLaunchIntent(this, emptyList()))
        }
    }
}