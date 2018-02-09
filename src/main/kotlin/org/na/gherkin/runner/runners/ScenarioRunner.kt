package org.na.gherkin.runner.runners

import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.na.gherkin.runner.data.RunType
import org.na.gherkin.runner.exception.MultipleImplementationsException
import org.na.gherkin.runner.exception.NotFoundImplementationException
import org.na.gherkin.runner.gherkin.GherkinScenario
import org.na.gherkin.runner.report.data.CallbackType
import org.na.gherkin.runner.report.data.FeatureReport
import org.na.gherkin.runner.specification.FeatureSpecification
import org.na.gherkin.runner.util.Reflection
import java.lang.reflect.Method

class ScenarioRunner(private val scenario: GherkinScenario,
                     private val scenarioDescription: Description,
                     private val featureReport: FeatureReport,
                     private val notifier: RunNotifier) {

    private val scenarioReport = featureReport.addForScenario(scenario)

    val log = KotlinLogging.logger { }

    fun run(featureSpecification: FeatureSpecification) {
        log.info("$$ Scenario ${scenario.name}")
        scenarioReport.start()
        try {
            val scenarioMethod = Reflection.getMethodForScenario(featureSpecification, scenario)
            runImplemented(featureSpecification, scenarioMethod)
        } catch (e: NotFoundImplementationException) {
            runNotImplemented()
        } catch (e: MultipleImplementationsException) {
            runMultipleImplementations()
        }
        log.info("Scenario running finished for ${scenario.fullTree} \r\n")
        scenarioReport.end()
    }

    fun run(runType: RunType) {
        log.info("$$ Scenario ${scenario.fullTree}")
        scenarioReport.start()
        when(runType) {
            RunType.NOT_IMPLEMENTED -> runNotImplemented()
            RunType.MULTIPLE_IMPLEMENTATIONS -> runMultipleImplementations()
        }
        log.info("Scenario running finished for ${scenario.fullTree} \r\n")
        scenarioReport.end()
    }

    private fun runImplemented(featureSpecification: FeatureSpecification, scenarioMethod: Method) {
        beforeScenarioRunner().run(featureSpecification)
        featureSpecification.clearStepDefs()
        scenarioMethod.invoke(featureSpecification)
        val stepDefs = featureSpecification.stepDefs
        stepRunners().forEach { it.run(stepDefs) }
        afterScenarioRunner().run(featureSpecification)
    }

    private fun runNotImplemented() {
        scenarioReport.notImplemented()
        stepRunners().forEach { it.run(RunType.NOT_IMPLEMENTED) }
    }

    private fun runMultipleImplementations() {
        scenarioReport.multipleImplementations()
        stepRunners().forEach { it.run(RunType.MULTIPLE_IMPLEMENTATIONS) }
    }

    private fun stepsJoinedWithDescriptions() = scenario.steps.zip(scenarioDescription.children.subList(1, scenarioDescription.children.size - 1))

    private fun stepRunners() = stepsJoinedWithDescriptions().map { (step, description) ->
        StepRunner(step, description, scenarioReport, notifier)
    }
    private fun beforeScenarioRunner() = CallbackRunner(scenarioDescription.children.first(), CallbackType.BEFORE_SCENARIO, scenarioReport, notifier)
    private fun afterScenarioRunner() = CallbackRunner(scenarioDescription.children.last(), CallbackType.AFTER_SCENARIO, scenarioReport, notifier)
}