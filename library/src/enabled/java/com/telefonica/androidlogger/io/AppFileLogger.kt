package com.telefonica.androidlogger.io

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.flattener.Flattener2
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.BackupStrategy2.NO_LIMIT
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import com.telefonica.androidlogger.domain.LogPriority
import com.telefonica.androidlogger.io.executor.Executor
import com.telefonica.androidlogger.io.executor.TaskCallback
import okio.BufferedSink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*
import kotlin.math.min

internal open class AppFileLogger(
    private val appContext: Context,
    private val executor: Executor
) {
    private var logPrinter: FilePrinter? = null

    open fun init() {
        logPrinter = FilePrinter.Builder(appContext.cacheDir.path)
            .fileNameGenerator(ChangelessFileNameGenerator(PERSISTED_LOGS_FILE_NAME))
            .backupStrategy(FileSizeBackupStrategy2(MAX_PERSISTED_LOGS_FILE_SIZE_BYTES, NO_LIMIT))
            .flattener(ClassicFlattener())
            .build()
            .apply {
                XLog.init(this)
            }
    }

    open fun log(@LogPriority priority: Int, tag: String, message: String) {
        /* File logger does not support ASSERT priority logs */
        logPrinter?.println(min(priority, Log.ERROR), tag, message)
    }

    open fun getReport(callback: TaskCallback<Uri>) =
        executor.submit(
            task = {
                FileProvider.getUriForFile(
                    appContext,
                    appContext.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX,
                    buildLogReportFile()
                )
            },
            callback = callback
        )

    fun storeVisibleLogs(visibleLogs: String, callback: TaskCallback<Uri>) {
        executor.submit(
            task = {
                val outputFile = createTempFile()
                outputFile.writeText(visibleLogs)
                FileProvider.getUriForFile(
                    appContext,
                    appContext.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX,
                    outputFile
                )
            },
            callback = callback
        )
    }

    private fun buildLogReportFile(): File {
        val outputFile = createTempFile()
        var source: Source? = null
        var sink: BufferedSink? = null
        try {
            source = buildLogReportStream().source()
            sink = outputFile.sink().buffer()
            sink.writeAll(source)
        } finally {
            source?.close()
            sink?.close()
        }

        return outputFile
    }

    private fun createTempFile(): File {
        val outputDir = File(appContext.cacheDir, REPORTS_DIRECTORY).apply { mkdir() }
        return File.createTempFile(TEMPORAL_FILE_PREFIX, TEMPORAL_FILE_EXTENSION, outputDir)
    }

    private fun buildLogReportStream(): InputStream =
        getLogFiles()
            .map { FileInputStream(it) }
            .let {
                SequenceInputStream(Collections.enumeration(it))
            }

    private fun getLogFiles(): List<File> =
        listOfNotNull(
            getLogFile(PERSISTED_LOGS_BACKUP_FILE_NAME),
            getLogFile(PERSISTED_LOGS_FILE_NAME)
        )

    private fun getLogFile(fileName: String): File? =
        File(appContext.cacheDir, fileName)
            .takeIf(File::exists)

    private companion object {
        const val PERSISTED_LOGS_FILE_NAME = "persisted_app_logs.txt"
        const val PERSISTED_LOGS_BACKUP_FILE_NAME = "$PERSISTED_LOGS_FILE_NAME.bak"
        const val MAX_PERSISTED_LOGS_FILE_SIZE_BYTES = (2 * 1024 * 1024).toLong()
        const val REPORTS_DIRECTORY = "app_logs_report"
        const val TEMPORAL_FILE_PREFIX = "report"
        const val TEMPORAL_FILE_EXTENSION = ".txt"
        const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".providers.AppLoggerFileProvider"
    }
}