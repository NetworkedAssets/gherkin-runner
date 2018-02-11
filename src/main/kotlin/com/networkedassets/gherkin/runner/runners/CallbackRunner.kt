package com.networkedassets.gherkin.runner.runners

import mu.KotlinLogging
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import com.networkedassets.gherkin.runner.exception.MultipleImplementationsException
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.report.data.ReportEntry
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import com.networkedassets.gherkin.runner.util.Reflection
import java.lang.reflect.Method

class CallbackRunner(private val callbackDescription: Description,
                     private val callbackType: CallbackType,
                     private val report: ReportEntry,
                     private val notifier: RunNotifier) {

    val log = KotlinLogging.logger { }

    private val eachNotifier = EachTestNotifier(notifier, callbackDescription)

    private val featureCallbackReport = when (callbackType) {
        CallbackType.BEFORE_FEATURE, CallbackType.BEFORE_SCENARIO -> report.addForBeforeCallback()
        CallbackType.AFTER_FEATURE, CallbackType.AFTER_SCENARIO -> report.addForAfterCallback()
    }

    fun run(featureSpecification: FeatureSpecification) {
        log.info("++ ${callbackType.humanize()}")
        featureCallbackReport.start()
        try {
            val callbackMethod = Reflection.getCallbackMethod(featureSpecification, callbackType)
            runImplemented(featureSpecification, callbackMethod)
        } catch (e: NotFoundImplementationException) {
            runNotImplemented()
        } catch (e: MultipleImplementationsException) {
            runMultipleImplementations()
        }
        log.info("Callback running finished for ${callbackType.humanize()}\r\n")
        featureCallbackReport.end()
    }

    private fun runImplemented(featureSpecification: FeatureSpecification, callbackMethod: Method) {
        eachNotifier.fireTestStarted()
        try {
            callbackMethod.invoke(featureSpecification)
            featureCallbackReport.passed()
        } catch (e: AssumptionViolatedException) {
            featureCallbackReport.failed(e)
            eachNotifier.addFailedAssumption(e)
            log.error("Step failed on assertion", e)
        } catch (e: Throwable) {
            featureCallbackReport.failed(e)
            eachNotifier.addFailure(e)
            log.error("Step failed because of unexpected exception", e)
        }
        eachNotifier.fireTestFinished()
    }

    private fun runSkipped() {
        featureCallbackReport.skipped()
    }

    private fun runNotImplemented() {
        eachNotifier.fireTestStarted()
        featureCallbackReport.notImplemented()
        eachNotifier.fireTestFinished()
    }

    private fun runMultipleImplementations() {
        eachNotifier.fireTestIgnored()
        featureCallbackReport.multipleImplementations()
    }
}