package com.telefonica.androidlogger.ui.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tuenti.applogger.R

internal class LogListItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    val container: View = view.findViewById(R.id.container)
    val header: TextView = view.findViewById(R.id.header)
    val message: TextView = view.findViewById(R.id.message)
    val indicator: View = view.findViewById(R.id.indicator)
}
