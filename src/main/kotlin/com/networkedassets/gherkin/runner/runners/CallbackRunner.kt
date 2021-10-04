package com.networkedassets.gherkin.runner.runners

import com.networkedassets.gherkin.runner.report.data.CallbackReport
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import mu.KotlinLogging
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.Method

class CallbackRunner(
    callbackDescription: Description,
    notifier: RunNotifier
) {

    val log = KotlinLogging.logger { }

    private val eachNotifier = EachTestNotifier(notifier, callbackDescription)

    fun runImplemented(featureSpecification: FeatureSpecification, callbackMethods: List<Method>, callbackReport: CallbackReport) {
        eachNotifier.fireTestStarted()
        try {
            callbackMethods.forEach { it.invoke(featureSpecification) }
            callbackReport.passed()
        } catch (e: AssumptionViolatedException) {
            callbackReport.failed(e)
            eachNotifier.addFailedAssumption(e)
            log.error("Step failed on assertion", e)
        } catch (e: Throwable) {
            callbackReport.failed(e)
            eachNotifier.addFailure(e)
            log.error("Step failed because of unexpected exception", e)
        }
        eachNotifier.fireTestFinished()
    }

    fun runNotImplemented(callbackReport: CallbackReport) {
        eachNotifier.fireTestStarted()
        callbackReport.notImplemented()
        eachNotifier.fireTestFinished()
    }

    fun runMultipleImplementations(callbackReport: CallbackReport) {
        eachNotifier.fireTestIgnored()
        callbackReport.multipleImplementations()
    }
}