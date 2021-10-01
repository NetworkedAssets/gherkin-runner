package com.networkedassets.gherkin.runner.runners

import com.networkedassets.gherkin.runner.annotation.AfterScenario
import com.networkedassets.gherkin.runner.annotation.BeforeScenario
import com.networkedassets.gherkin.runner.data.CriteriaType
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.report.data.ReportEntry
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import com.networkedassets.gherkin.runner.util.CallbackMapper.toAnnotation
import com.networkedassets.gherkin.runner.util.Reflection
import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.Method


class ScenarioCallbackRunner(
    private val callbackType: CallbackType,
    private val scenario: GherkinScenario,
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
            Reflection.getCallbackMethods(featureSpecification, callbackType)
                .filter { it.fulfillsCriteria() }
                .sortedWith(
                    when (callbackType) {
                        CallbackType.AFTER_SCENARIO -> compareByDescending { it.computeMinPriority() }
                        else -> compareBy { it.computeMinPriority() }
                    }
                )
                .let { callbackRunner.runImplemented(featureSpecification, it, featureCallbackReport) }
        } catch (e: NotFoundImplementationException) {
            callbackRunner.runNotImplemented(featureCallbackReport)
        }
    }

    private fun Method.fulfillsCriteria() = computeCriteria().all {
        when (it.type) {
            CriteriaType.ALL_SCENARIOS -> true
            CriteriaType.NAME_CONTAINS -> it.value in scenario.name
            CriteriaType.NAME_NOT_CONTAINS -> it.value !in scenario.name
        }
    }

    private fun Method.computeMinPriority() = computeCriteria().map { it.type }.min()

    private fun Method.computeCriteria() = getAnnotation(callbackType.toAnnotation().java)
        .let {
            when (it) {
                is BeforeScenario -> it.criteria
                is AfterScenario -> it.criteria
                else -> emptyArray()
            }
        }

}