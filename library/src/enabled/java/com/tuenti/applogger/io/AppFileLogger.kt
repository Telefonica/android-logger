package com.tuenti.applogger.io

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import com.tuenti.applogger.domain.LogPriority
import com.tuenti.applogger.io.executor.Executor
import com.tuenti.applogger.io.executor.TaskCallback
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*

internal open class AppFileLogger(
    private val appContext: Context,
    private val executor: Executor
) {
    private var logPrinter: FilePrinter? = null

    open fun init() {
        logPrinter = FilePrinter.Builder(appContext.cacheDir.path)
            .fileNameGenerator(ChangelessFileNameGenerator(PERSISTED_LOGS_FILE_NAME))
            .backupStrategy(FileSizeBackupStrategy(MAX_PERSISTED_LOGS_FILE_SIZE_BYTES))
            .logFlattener(ClassicFlattener())
            .build()
            .apply {
                XLog.init(this)
            }
    }

    open fun log(@LogPriority priority: Int, tag: String, message: String) {
        logPrinter?.println(priority, tag, message)
    }

    open fun getReport(callback: TaskCallback<Uri>) =
        executor.submit(
            task = { FileProvider.getUriForFile(appContext, appContext.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX, buildLogReportFile()) },
            callback = callback
        )

    private fun buildLogReportFile(): File {
        val outputDir = File(appContext.cacheDir, REPORTS_DIRECTORY).apply { mkdir() }
        val outputFile = File.createTempFile(TEMPORAL_FILE_PREFIX, TEMPORAL_FILE_EXTENSION, outputDir)

        var source: Source? = null
        var sink: BufferedSink? = null
        try {
            source = Okio.source(buildLogReportStream())
            sink = Okio.buffer(Okio.sink(outputFile))
            sink.writeAll(source)
        } finally {
            source?.close()
            sink?.close()
        }

        return outputFile
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