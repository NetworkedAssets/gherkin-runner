package com.networkedassets.gherkin.runner.runners

import com.networkedassets.gherkin.runner.data.RunType
import com.networkedassets.gherkin.runner.exception.MultipleImplementationsException
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.report.data.Report
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import com.networkedassets.gherkin.runner.util.Reflection
import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier

class FeatureRunner(private val implementationsPackage: String,
                    private val feature: GherkinFeature,
                    private val featureDescription: Description,
                    private val report: Report,
                    private val notifier: RunNotifier) {

    private val featureReport = report.addForFeature(feature)

    val log = KotlinLogging.logger { }

    fun run() {
        log.info("## Feature ${feature.name}")
        featureReport.start()
        try {
            val featureSpecification = Reflection.getFeatureSpecification(implementationsPackage, feature)

            runImplemented(featureSpecification)
        } catch (e: NotFoundImplementationException) {
            runNotImplemented()
        } catch (e: MultipleImplementationsException) {
            runMultipleImplementations()
        }
        log.info("Feature running finished for ${feature.name}\r\n")
        featureReport.end()
    }

    private fun runImplemented(featureSpecification: FeatureSpecification) {


        beforeFeatureRunner().run(featureSpecification)
        scenarioRunners().forEach { it.run(featureSpecification) }
        afterFeatureRunner().run(featureSpecification)
    }

    private fun runNotImplemented() {
        featureReport.notImplemented()
        scenarioRunners().forEach { it.run(RunType.NOT_IMPLEMENTED) }
    }

    private fun runMultipleImplementations() {
        featureReport.multipleImplementations()
        scenarioRunners().forEach { it.run(RunType.MULTIPLE_IMPLEMENTATIONS) }
    }

    private fun scenariosJoinedWithDescriptions() = feature.scenarios.zip(
            featureDescription.children.subList(1, featureDescription.children.size - 1))

    private fun scenarioRunners() = scenariosJoinedWithDescriptions().map { (scenario, description) ->
        ScenarioRunner(scenario, description, featureReport, notifier)
    }

    private fun beforeFeatureRunner() = CallbackRunner(featureDescription.children.first(), CallbackType.BEFORE_FEATURE, featureReport, notifier)
    private fun afterFeatureRunner() = CallbackRunner(featureDescription.children.last(), CallbackType.AFTER_FEATURE, featureReport, notifier)
}