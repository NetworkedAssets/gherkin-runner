package org.na.gherkin.runner.report.data

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.commons.lang3.exception.ExceptionUtils

class CallbackReport(parent: ReportEntry) : ReportEntry(parent) {
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