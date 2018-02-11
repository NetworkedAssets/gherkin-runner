package com.networkedassets.gherkin.runner.report

import com.networkedassets.gherkin.runner.report.data.Report
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

interface ReportExporter {
    fun reportsDir() = System.getProperty("reportsPath") ?: "${System.getProperty("user.dir")}/build/gherkinrunner"

    val name: String
    fun export(report: Report)

    fun writeToFile(path: String, content: String) {
        val jsonReportFile = File(path)
        jsonReportFile.parentFile.mkdirs()
        jsonReportFile.writeText(content)
    }

    companion object {
        fun exportReport(report: Report, registeredExtensions: Set<KClass<*>>, reports: Set<String>) {
            registeredExtensions.filter { it.isSubclassOf(ReportExporter::class) }.forEach {
                val reportExporter = it.createInstance() as ReportExporter
                if (reports.contains(reportExporter.name)) reportExporter.export(report)
            }
        }
    }
}