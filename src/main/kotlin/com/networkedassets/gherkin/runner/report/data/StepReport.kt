package com.networkedassets.gherkin.runner.report.data

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.commons.lang3.exception.ExceptionUtils
import com.networkedassets.gherkin.runner.gherkin.GherkinStep

class StepReport(val step: GherkinStep, parent: ReportEntry) : ReportEntry(parent) {
    @JsonIgnore
    var thrownException: Throwable? = null

    fun getError(): String? = thrownException?.message
    fun getErrorStackTrace(): String? =
            if (thrownException != null)
                ExceptionUtils.getStackTrace(thrownException)
            else null


    fun passed() {
        state = ReportState.PASSED
    }

    fun skipped() {
        state = ReportState.SKIPPED
    }

    fun failed(throwable: Throwable) {
        thrownException = throwable
        state = ReportState.FAILED
    }
}

