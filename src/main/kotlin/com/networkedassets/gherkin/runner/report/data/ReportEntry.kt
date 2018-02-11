package com.networkedassets.gherkin.runner.report.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.networkedassets.gherkin.runner.SharedData
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

open class ReportEntry(@JsonIgnore val parent: ReportEntry?) {
    var startTime: LocalDateTime? = null
    var endTime: LocalDateTime? = null
    open var state: ReportState? = null
    var beforeReport: CallbackReport? = null
    var afterReport: CallbackReport? = null
    val log = mutableListOf<LogEntry>()

    val executionTime: Long
        get() = ChronoUnit.MILLIS.between(startTime, endTime)

    fun start() {
        startTime = LocalDateTime.now()
        SharedData.currentStepReport = this
    }

    fun end() {
        endTime = LocalDateTime.now()
        SharedData.currentStepReport = parent
    }

    fun notImplemented() {
        state = ReportState.NOT_IMPLEMENTED
    }

    fun multipleImplementations() {
        state = ReportState.MULTIPLE_IMPLEMENTATIONS
    }

    fun addForBeforeCallback(): CallbackReport {
        val callbackReport = CallbackReport(this)
        beforeReport = callbackReport
        return callbackReport
    }

    fun addForAfterCallback(): CallbackReport {
        val callbackReport = CallbackReport(this)
        afterReport = callbackReport
        return callbackReport
    }

    fun addLogMessage(message: String) {
        var currentReport: ReportEntry? = this
        var currentIndent = 0
        while (currentReport != null) {
            currentReport.log.add(LogEntry(message, currentIndent))
            currentReport = currentReport.parent
            currentIndent++
        }
    }
}