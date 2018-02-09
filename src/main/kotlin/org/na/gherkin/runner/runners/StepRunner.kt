package org.na.gherkin.runner.runners

import groovy.lang.Closure
import mu.KotlinLogging
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.na.gherkin.runner.data.RunType
import org.na.gherkin.runner.exception.MultipleImplementationsException
import org.na.gherkin.runner.exception.NotFoundImplementationException
import org.na.gherkin.runner.gherkin.GherkinStep
import org.na.gherkin.runner.gherkin.StepKeyword
import org.na.gherkin.runner.report.data.ReportState
import org.na.gherkin.runner.report.data.ScenarioReport
import org.na.gherkin.runner.util.Reflection

class StepRunner(private val step: GherkinStep,
                 private val stepDescription: Description,
                 private val scenarioReport: ScenarioReport,
                 private val notifier: RunNotifier) {

    private val stepReport = scenarioReport.addForStep(step)
    private val eachNotifier = EachTestNotifier(notifier, stepDescription)

    val log = KotlinLogging.logger { }

    fun run(stepDefs: Map<Pair<StepKeyword, String>, Closure<Any>>) {
        log.info("^^ ${step.fullContent}")
        stepReport.start()
        try {
            val stepDef = Reflection.getClosureForStep(stepDefs, step)
            if(anyStepBeforeHasProblem()) {
                runSkipped()
            } else {
                runImplemented(stepDef)
            }
        } catch (e: NotFoundImplementationException) {
            runNotImplemented()
        } catch (e: MultipleImplementationsException) {
            runMultipleImplementations()
        }
        log.info("Step running finished for ${step.fullContent}\r\n")
        stepReport.end()
    }

    fun run(runType: RunType) {
        log.info("^^ ${step.fullContent}")
        stepReport.start()
        when(runType) {
            RunType.NOT_IMPLEMENTED -> runNotImplemented()
            RunType.MULTIPLE_IMPLEMENTATIONS -> runMultipleImplementations()
        }
        log.info("Step running finished for ${step.fullContent}\r\n")
        stepReport.end()
    }

    private fun runImplemented(stepDef: Closure<Any>) {
        eachNotifier.fireTestStarted()
        try {
            if(step.data != null) {
                stepDef.call(step.data)
            } else {
                stepDef.call()
            }

            stepReport.passed()
        } catch (e: AssumptionViolatedException) {
            stepReport.failed(e)
            eachNotifier.addFailedAssumption(e)
            log.error("Step failed on assertion", e)
        } catch (e: Throwable) {
            stepReport.failed(e)
            eachNotifier.addFailure(e)
            log.error("Step failed because of unexpected exception", e)
        }
        eachNotifier.fireTestFinished()
    }

    private fun runSkipped() {
        eachNotifier.fireTestIgnored()
        stepReport.skipped()
    }

    private fun runNotImplemented() {
        eachNotifier.fireTestIgnored()
        stepReport.notImplemented()
    }

    private fun runMultipleImplementations() {
        eachNotifier.fireTestIgnored()
        stepReport.multipleImplementations()
    }

    private fun anyStepBeforeHasProblem(): Boolean {
        val problemStates = listOf(ReportState.FAILED, ReportState.NOT_IMPLEMENTED, ReportState.MULTIPLE_IMPLEMENTATIONS)
        return scenarioReport.stepReports.any {
            problemStates.contains(it.state)
        }
    }
}