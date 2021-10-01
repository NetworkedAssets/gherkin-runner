package com.networkedassets.gherkin.runner.runners

import com.networkedassets.gherkin.runner.data.RunType
import com.networkedassets.gherkin.runner.exception.MultipleImplementationsException
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.report.data.FeatureReport
import com.networkedassets.gherkin.runner.specification.ExampleBindings
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import com.networkedassets.gherkin.runner.util.Reflection
import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
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
            featureSpecification.bindings = ExampleBindings(scenario.bindings)
            featureSpecification.metadataListeners.scenarioMetadataListener = { metadata -> scenarioReport.metadata = metadata }
            runImplemented(featureSpecification, scenarioMethod)
            featureSpecification.metadataListeners.scenarioMetadataListener = null
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
        stepRunners().forEach { it.run(featureSpecification) }
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
    private fun beforeScenarioRunner() =
        ScenarioCallbackRunner(CallbackType.BEFORE_SCENARIO, scenario, scenarioDescription.children.first(), scenarioReport, notifier)

    private fun afterScenarioRunner() =
        ScenarioCallbackRunner(CallbackType.AFTER_SCENARIO, scenario, scenarioDescription.children.last(), scenarioReport, notifier)
}