package org.na.gherkin.runner.report

import org.na.gherkin.runner.report.data.Report
import java.nio.file.Paths

class HTMLReportExporter : ReportExporter {
    override val name = "HTML"

    override fun export(report: Report) {
        val htmlReportDir = "${reportsDir()}/html"
        val htmlReportFilePath = "$htmlReportDir/data.js"
        ReportInstaller.installResources(Paths.get(htmlReportDir), this.javaClass, "reporttemplate")
        val json = ReportSerializer.reportToJson(report)
        val jsonDataContent = "var reportData = $json"
        writeToFile(htmlReportFilePath, jsonDataContent)
    }
}