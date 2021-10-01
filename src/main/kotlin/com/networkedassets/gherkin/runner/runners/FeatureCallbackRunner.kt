package com.networkedassets.gherkin.runner.runners

import com.networkedassets.gherkin.runner.exception.MultipleImplementationsException
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.report.data.ReportEntry
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import com.networkedassets.gherkin.runner.util.Reflection
import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier

class FeatureCallbackRunner(
    private val callbackType: CallbackType,
    callbackDescription: Description,
    report: ReportEntry,
    notifier: RunNotifier
) {

    val log = KotlinLogging.logger { }

    private val callbackRunner = CallbackRunner(callbackDescription, notifier)
    private val featureCallbackReport = when (callbackType) {
        CallbackType.BEFORE_FEATURE, CallbackType.BEFORE_SCENARIO -> report.addForBeforeCallback()
        CallbackType.AFTER_FEATURE, CallbackType.AFTER_SCENARIO -> report.addForAfterCallback()
    }

    fun run(featureSpecification: FeatureSpecification) = featureCallbackReport.decorate(log, callbackType.humanize()) {
        try {
            val callbackMethod = Reflection.getCallbackMethod(featureSpecification, callbackType)
            callbackRunner.runImplemented(featureSpecification, listOf(callbackMethod), featureCallbackReport)
        } catch (e: NotFoundImplementationException) {
            callbackRunner.runNotImplemented(featureCallbackReport)
        } catch (e: MultipleImplementationsException) {
            callbackRunner.runMultipleImplementations(featureCallbackReport)
        }
    }
}