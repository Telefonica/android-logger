package com.telefonica.androidlogger.ui.viewmodel

import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telefonica.androidlogger.domain.LogCategory
import com.telefonica.androidlogger.domain.LogEntry
import com.telefonica.androidlogger.domain.getCategories
import com.telefonica.androidlogger.domain.getLogs
import com.telefonica.androidlogger.domain.getPersistedLogs
import com.telefonica.androidlogger.io.executor.TaskCallback
import com.telefonica.androidlogger.ui.livedata.filter
import com.telefonica.androidlogger.ui.livedata.map
import com.telefonica.androidlogger.ui.livedata.throttle
import java.text.SimpleDateFormat
import java.util.*

internal class AppLoggerViewModel : ViewModel() {

    private var searchTerm: String? = null

    var availableCategories: List<LogCategoryViewModel> = emptyList()
        private set
    private var selectedCategories: MutableList<LogCategoryViewModel> = mutableListOf()
    private var showCategoriesLogsOnly: Boolean = true

    private var minLogLevel: LogPriorityViewModel = LogPriorityViewModel.VERBOSE

    private var expandedLogsIds: MutableList<Int> = mutableListOf()

    private val filterTrigger = MutableLiveData<Unit>()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val logs: LiveData<List<LogEntryViewModel>> =
        getLogs()
            .throttle()
            .filter(filterTrigger) {
                it.filterByLogLevel()
                    .filterBySearchTerm()
                    .filterBySelectedCategories()
            }.map { logs ->
                logs.map { entry ->
                    entry.toViewModel(dateFormat, expandedLogsIds.contains(entry.id))
                }
            }

    fun init(
        selectedCategoriesNames: List<String> = emptyList()
    ) {
        val allCategories: List<LogCategory> = getCategories()
        availableCategories = allCategories
            .filter { selectedCategoriesNames.contains(it.name) }
            .ifEmpty {
                showCategoriesLogsOnly = false
                allCategories
            }
            .map { it.toViewModel() }
    }

    fun onCategorySelected(category: LogCategoryViewModel) {
        selectedCategories.add(category)
        filter()
    }

    fun onMinLogLevelSelected(logPriority: LogPriorityViewModel) {
        minLogLevel = logPriority
        filter()
    }

    fun onCategoryUnselected(category: LogCategoryViewModel) {
        selectedCategories.remove(category)
        filter()
    }

    fun onSearchTermIntroduced(searchTerm: String) {
        this.searchTerm = searchTerm.takeIf { it.isNotBlank() }
        filter()
    }

    fun onLogClicked(log: LogEntryViewModel) {
        if (expandedLogsIds.contains(log.id)) {
            expandedLogsIds.remove(log.id)
        } else {
            expandedLogsIds.add(log.id)
        }
        filter()
    }

    fun getFilteredLogs(): LiveData<List<LogEntryViewModel>> =
        logs

    fun getAllLogs(callback: TaskCallback<Uri>) {
        getPersistedLogs(callback)
    }

    private fun List<LogEntry>.filterByLogLevel() =
        filter {
            it.priority.toLogPriority() >= minLogLevel
        }

    private fun List<LogEntry>.filterBySearchTerm() =
        searchTerm?.let { term ->
            this.filter { entry ->
                entry.tag.lowerCaseContains(term) || entry.message.lowerCaseContains(term)
            }
        } ?: this

    private fun String.lowerCaseContains(anotherString: String) =
        toLowerCase(Locale.ROOT).contains(anotherString.toLowerCase(Locale.ROOT))

    private fun List<LogEntry>.filterBySelectedCategories(): List<LogEntry> {
        val isEmptyCategoriesSelection = selectedCategories.isEmpty()
        val isEmptyFiltersSelectionWithAllCategories =
                isEmptyCategoriesSelection && !showCategoriesLogsOnly
        return filter { logEntry ->
            logEntry.categories?.let { logEntryCategories ->
                val availableCategoriesMatching = logEntryCategories
                        .filter { availableCategories.contains(it.toViewModel()) }
                val selectedCategoriesMatching = logEntryCategories
                        .filter { selectedCategories.contains(it.toViewModel()) }
                (isEmptyCategoriesSelection && availableCategoriesMatching.isNotEmpty())
                        || selectedCategoriesMatching.isNotEmpty()
            } ?: isEmptyFiltersSelectionWithAllCategories
        }.map { getLogEntryWithFilteredCategories(it) }
    }

    private fun getLogEntryWithFilteredCategories(logEntry: LogEntry): LogEntry {
        logEntry.categories?.let { logEntryCategories ->
            val filteredCategories = logEntryCategories.filter {
                availableCategories.contains(it.toViewModel())
                        || selectedCategories.contains(it.toViewModel())
            }
            return logEntry.copy(categories = filteredCategories)
        }
        return logEntry
    }

    private fun filter() {
        /* Trigger filter */
        filterTrigger.value = Unit
    }
}

internal data class LogEntryViewModel(
        val id: Int,
        val priority: LogPriorityViewModel,
        val header: String,
        val categories: List<LogCategoryViewModel>?,
        val message: String,
        val expanded: Boolean
)

internal data class LogCategoryViewModel(
    val name: String,
    @ColorInt val color: Int
)

internal enum class LogPriorityViewModel(
    val displayName: String,
    @ColorInt val color: Int
) {
    VERBOSE("Verbose", Color.parseColor("#6C757D")),
    DEBUG("Debug", Color.parseColor("#000000")),
    INFO("Info", Color.parseColor("#007BFF")),
    WARN("Warn", Color.parseColor("#FFC107")),
    ERROR("Error", Color.parseColor("#DC3545")),
    ASSERT("Assert", Color.parseColor("#E83E8C"))
}