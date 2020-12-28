package com.tuenti.applogger.ui.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast

import com.tuenti.applogger.R

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.tuenti.applogger.ui.holder.LogListItemHolder
import com.tuenti.applogger.ui.viewmodel.AppLoggerViewModel
import com.tuenti.applogger.ui.viewmodel.LogEntryViewModel
import kotlin.math.min

internal class LogListAdapter(
    private val viewModel: AppLoggerViewModel
) : RecyclerView.Adapter<LogListItemHolder>() {

    private var logEntryList: List<LogEntryViewModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogListItemHolder =
        LogListItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.log_row_item, parent, false))

    override fun onBindViewHolder(holder: LogListItemHolder, position: Int) {
        with(logEntryList[position]) {
            holder.header.text = header
            holder.header.setTextColor(priority.color)
            holder.message.text = if (expanded) message else message.substring(0, min(message.length, MAX_TEXT_LENGTH_NOT_EXPANDED))
            holder.message.maxLines = if (expanded) Int.MAX_VALUE else DEFAULT_MESSAGE_LINES
            holder.message.setTextColor(priority.color)
            holder.indicator.setBackgroundColor(category?.color ?: Color.TRANSPARENT)
            holder.container.setOnClickListener {
                viewModel.onLogClicked(this)
            }
            holder.container.setOnLongClickListener {
                copyMessageToClipboard(it.context, message)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return logEntryList.size
    }

    fun onDataModified(newLogEntryList: List<LogEntryViewModel>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return logEntryList[oldItemPosition] == newLogEntryList[newItemPosition]
            }

            override fun getOldListSize(): Int {
                return logEntryList.size
            }

            override fun getNewListSize(): Int {
                return newLogEntryList.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return logEntryList[oldItemPosition] == newLogEntryList[newItemPosition]
            }
        }).dispatchUpdatesTo(this)
        logEntryList = newLogEntryList
    }

    fun getVisibleInfoAsString(): String =
        logEntryList.joinToString("\n") { "${it.header}: ${it.message}" }

    private fun copyMessageToClipboard(context: Context, message: String) {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.copy_clipboard_label), message)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.copy_clipboard_success), Toast.LENGTH_SHORT)
            .show()
    }

    private companion object {
        const val DEFAULT_MESSAGE_LINES = 4
        const val MAX_TEXT_LENGTH_NOT_EXPANDED = 300
    }
}