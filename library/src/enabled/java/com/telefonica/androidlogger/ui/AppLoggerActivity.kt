package com.telefonica.androidlogger.ui

import android.animation.LayoutTransition
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import com.telefonica.androidlogger.R
import com.telefonica.androidlogger.ui.adapter.LogListAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telefonica.androidlogger.domain.LogCategory
import com.telefonica.androidlogger.io.executor.TaskCallback
import com.telefonica.androidlogger.ui.viewmodel.AppLoggerViewModel
import com.telefonica.androidlogger.ui.viewmodel.LogCategoryViewModel
import com.telefonica.androidlogger.ui.viewmodel.LogPriorityViewModel
import java.io.File
import java.security.InvalidParameterException

class AppLoggerActivity : AppCompatActivity() {

    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var recyclerView: RecyclerView
    private lateinit var filtersView: ViewGroup
    private lateinit var adapter: LogListAdapter
    private lateinit var viewModel: AppLoggerViewModel

    private var automaticScrollEnabled = true

    private var searchView: SearchView? = null

    private var shareAllLogsCallback: TaskCallback<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_logger)

        viewModel = ViewModelProviders.of(this)
            .get(AppLoggerViewModel::class.java)
            .apply {
                init(intent.getStringArrayListExtra(EXTRA_CATEGORIES_NAMES) ?: emptyList())
            }

        initToolbar()
        initLogsList()
        initFiltersView()

        viewModel.getFilteredLogs().observe(this@AppLoggerActivity, Observer {
            adapter.onDataModified(it)
            if (automaticScrollEnabled) {
                recyclerView.post { scrollToBottom() }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app_logger, menu)

        searchView = (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.onSearchTermIntroduced(query)
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.onSearchTermIntroduced(query)
                    return false
                }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_search -> true
            R.id.action_filter -> {
                toggleCategoryFilter()
                true
            }
            R.id.action_scroll -> {
                automaticScrollEnabled = true
                scrollToBottom()
                true
            }
            R.id.action_share_visible -> {
                shareVisibleLogs()
                true
            }
            R.id.action_share_all -> {
                shareAllLogs()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        searchView?.let {
            if (!it.isIconified) {
                it.isIconified = true
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        shareAllLogsCallback?.cancel()
        super.onDestroy()
    }

    private fun initToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        with(supportActionBar!!) {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.toolbar_title)
        }
    }

    private fun initLogsList() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    automaticScrollEnabled = false
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        adapter = LogListAdapter(viewModel)
        recyclerView.adapter = adapter
    }

    private fun initFiltersView() {
        filtersView = findViewById(R.id.filters)
        initCategoriesFilter()
        initLogLevelsFilter()
    }

    private fun initCategoriesFilter() {
        val categoriesTextView = findViewById<TextView>(R.id.categories_text)
        val categoriesChipGroup = findViewById<ChipGroup>(R.id.categories_chipgroup)
        val availableCategories: List<LogCategoryViewModel> = viewModel.availableCategories

        if (availableCategories.size <= 1) {
            categoriesTextView.visibility = View.GONE
            categoriesChipGroup.visibility = View.GONE
        } else {
            findViewById<ChipGroup>(R.id.categories_chipgroup).apply {
                configureAnimations()
                availableCategories.forEach {
                    inflateChip(
                        displayText = it.name,
                        color = it.color,
                        checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                viewModel.onCategorySelected(it)
                            } else {
                                viewModel.onCategoryUnselected(it)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun initLogLevelsFilter() {
        findViewById<ChipGroup>(R.id.log_levels_chipgroup).apply {
            configureAnimations()
            LogPriorityViewModel.values().forEachIndexed { index, priorityViewModel ->
                inflateChip(
                    displayText = priorityViewModel.displayName,
                    color = priorityViewModel.color,
                    checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            viewModel.onMinLogLevelSelected(priorityViewModel)
                        }
                    }
                ).let {
                    if (index == 0) {
                        check(it.id)
                    }
                }
            }
        }
    }

    private fun ChipGroup.configureAnimations() {
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
    }

    private fun ChipGroup.inflateChip(
        displayText: String,
        @ColorInt color: Int,
        checkedChangeListener: CompoundButton.OnCheckedChangeListener
    ): Chip =
        LayoutInflater
            .from(context)
            .inflate(R.layout.log_category_chip, this, false)
            .let { it as Chip }
            .let {
                @ColorInt val chipUncheckedBackgroundColor: Int =
                    this@AppLoggerActivity.getThemeColor(R.attr.colorPrimary)
                @ColorInt val chipTextColor: Int =
                    this@AppLoggerActivity.getThemeColor(R.attr.colorOnPrimary)
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val backgroundColors = intArrayOf(
                    color,
                    chipUncheckedBackgroundColor
                )
                it.chipBackgroundColor = ColorStateList(states, backgroundColors)
                it.chipStrokeColor = ColorStateList.valueOf(color)
                it.checkedIcon?.setTint(chipTextColor)
                it.setTextColor(ColorStateList.valueOf(chipTextColor))
                it.text = displayText
                it.setOnCheckedChangeListener(checkedChangeListener)
                addView(it)
                it
            }

    private fun Context.getThemeColor(@AttrRes themeColor: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(themeColor, typedValue, true)
        if (typedValue.data == TypedValue.DATA_NULL_UNDEFINED) {
            throw InvalidParameterException("Theme color is not specified!")
        }
        return typedValue.data
    }

    private fun toggleCategoryFilter() {
        if (filtersView.visibility == View.VISIBLE) {
            filtersView.visibility = View.GONE
        } else {
            filtersView.visibility = View.VISIBLE
        }
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun shareVisibleLogs() {
        val visibleInfoAsString: String = adapter.getVisibleInfoAsString()
        shareAllLogsCallback = TaskCallback<Uri> { uri ->
            mainThreadHandler.post {
                if (uri != null) {
                    launchShareAllLogsIntent(uri)
                } else {
                    Toast.makeText(this, R.string.share_visible_logs_failure, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.apply {
            viewModel.onLogsThatShouldBeStoredIntoAFile(visibleInfoAsString, this)
        }
    }

    private fun shareAllLogs() {
        shareAllLogsCallback = TaskCallback<Uri> { uri ->
            mainThreadHandler.post {
                if (uri != null) {
                    launchShareAllLogsIntent(uri)
                } else {
                    Toast.makeText(this, R.string.share_all_logs_failure, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.apply {
            viewModel.getAllLogs(this)
        }
    }

    private fun launchShareAllLogsIntent(uri: Uri) {
        val shareIntent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
        }, null)
        startActivity(shareIntent)
    }
}

private const val EXTRA_CATEGORIES_NAMES = "extra_categories_names"

@JvmOverloads
fun getLaunchIntent(context: Context, categories: List<LogCategory> = emptyList()): Intent? =
    Intent(context, AppLoggerActivity::class.java)
        .putExtra(EXTRA_CATEGORIES_NAMES, ArrayList(categories.map { it.name }))