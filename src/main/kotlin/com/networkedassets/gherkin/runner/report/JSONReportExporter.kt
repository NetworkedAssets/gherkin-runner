package com.networkedassets.gherkin.runner.report

import com.networkedassets.gherkin.runner.report.data.Report

class JSONReportExporter : ReportExporter {
    override val name = "JSON"

    override fun export(report: Report) {
        val jsonReportFilePath = "${reportsDir()}/report.json"
        val json = JSONSerializer.toJson(report)
        writeToFile(jsonReportFilePath, json)
    }
}