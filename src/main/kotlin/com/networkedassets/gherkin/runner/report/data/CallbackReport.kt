package com.networkedassets.gherkin.runner.report.data

import com.fasterxml.jackson.annotation.JsonIgnore
import mu.KLogger
import org.apache.commons.lang3.exception.ExceptionUtils

class CallbackReport(parent: ReportEntry) : ReportEntry(parent) {
    @JsonIgnore
    var thrownException: Throwable? = null

    fun getError(): String? = thrownException?.message
    fun getErrorStackTrace(): String? =
        if (thrownException != null)
            ExceptionUtils.getStackTrace(thrownException)
        else null

    fun decorate(log: KLogger, callbackName: String, function: () -> Unit) {
        log.info("++ $callbackName")
        start()
        function()
        log.info("Callback running finished for $callbackName\r\n")
        end()
    }

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