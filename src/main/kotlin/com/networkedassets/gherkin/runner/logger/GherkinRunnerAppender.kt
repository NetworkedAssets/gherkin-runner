package com.networkedassets.gherkin.runner.logger

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.StringLayout
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.layout.PatternLayout
import com.networkedassets.gherkin.runner.SharedData
import com.networkedassets.gherkin.runner.report.data.*
import java.io.File

@Plugin(name = "GherkinRunnerAppender", category = "Core", elementType = "appender", printObject = true)
class GherkinRunnerAppender(name: String,
                            filter: Filter?,
                            layout: Layout<String>) : AbstractAppender(name, filter, layout) {

    private val reportsDir = System.getProperty("reportsPath") ?: "${System.getProperty("user.dir")}/build/gherkinrunner"
    private val debugLogFilePath = "$reportsDir/log/debug.log"
    private val debugLogFile = File(debugLogFilePath)
    private val infoLogFilePath = "$reportsDir/log/info.log"
    private val infoLogFile = File(infoLogFilePath)

    init {
        debugLogFile.delete()
        debugLogFile.parentFile.mkdirs()
        infoLogFile.delete()
        infoLogFile.parentFile.mkdirs()
    }

    override fun append(event: LogEvent) {
        val currentStepReport = SharedData.currentStepReport

        if (event.level.isMoreSpecificThan(Level.INFO)) {
            currentStepReport?.addLogMessage(formatMessage(event, currentStepReport, false).trim())
            appendMessageToLogFile(formatMessage(event, currentStepReport), infoLogFile)
            print(formatMessage(event, currentStepReport))
        }
        appendMessageToLogFile(formatMessage(event, currentStepReport), debugLogFile)
    }

    private fun appendMessageToLogFile(message: String, logFile: File) {
        logFile.appendText(message)
    }

    private fun formatMessage(event: LogEvent, reportEntry: ReportEntry?, withIndent: Boolean = true): String {
        val message = getStringLayout().toSerializable(event)
        return if (withIndent) indentMessage(message, getIndentForReport(reportEntry)) else message
    }

    private fun indentMessage(message: String, indentSize: Int): String {
        val indent = "\t".repeat(indentSize)
        return "$indent$message"
    }

    private fun getIndentForReport(reportEntry: ReportEntry?): Int {
        return when (reportEntry) {
            is FeatureReport -> 1
            is ScenarioReport -> 2
            is StepReport -> 3
            is CallbackReport ->
                if (reportEntry.parent is FeatureReport) 2
                else 3
            else -> 0
        }
    }

    private fun getStringLayout(): StringLayout {
        return layout as StringLayout
    }

    companion object {
        @PluginFactory
        @JvmStatic
        fun createAppender(@PluginAttribute("name") name: String = "GherkinRunnerAppender",
                           @PluginElement("Layout") layout: Layout<String> = PatternLayout.createDefaultLayout(),
                           @PluginElement("Filter") filter: Filter?): GherkinRunnerAppender {
            return GherkinRunnerAppender(name, filter, layout)
        }
    }
}