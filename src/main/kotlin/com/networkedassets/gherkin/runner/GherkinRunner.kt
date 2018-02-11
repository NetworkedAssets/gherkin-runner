package com.networkedassets.gherkin.runner

import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import com.networkedassets.gherkin.runner.report.HPQCExporter
import com.networkedassets.gherkin.runner.report.HTMLReportExporter
import com.networkedassets.gherkin.runner.report.JSONReportExporter
import com.networkedassets.gherkin.runner.report.ReportExporter
import com.networkedassets.gherkin.runner.report.data.Report
import com.networkedassets.gherkin.runner.runners.FeatureRunner
import com.networkedassets.gherkin.runner.util.GherkinLoader
import com.networkedassets.gherkin.runner.util.Reflection
import kotlin.reflect.KClass

class GherkinRunner(private val clazz: Class<*>) : Runner() {
    private val features = GherkinLoader.loadFeatures()
    private val description = createDescription()

    val log = KotlinLogging.logger { }

    override fun run(notifier: RunNotifier) {
        log.info { "Starting GherkinRunner test suite" }
        val gherkinRunnerMetadata = Reflection.getGherkinRunnerMetadata(clazz)
        val report = Report(gherkinRunnerMetadata.suiteName, gherkinRunnerMetadata.environment)
        report.start()
        val implementationsPackage = Reflection.getImplementationsPackage(clazz) ?: clazz.`package`.name
        featuresJoinedWithDescriptions().forEach { (feature, featureDescription) ->
            val featureRunner = FeatureRunner(implementationsPackage, feature, featureDescription, report, notifier)
            featureRunner.run()
        }
        report.end()
        log.info("Exporting report")
        val extensions = mutableSetOf<KClass<*>>(
                JSONReportExporter::class,
                HTMLReportExporter::class,
                HPQCExporter::class)
        extensions.addAll(Reflection.getExtensions(clazz) ?: setOf())
        val reports = Reflection.getReports(clazz) ?: setOf("HTML")

        ReportExporter.exportReport(report, extensions, reports)
        log.info("Report has been exported")
    }

    private fun featuresJoinedWithDescriptions() = features.zip(description.children)

    override fun getDescription() = description

    private fun createDescription(): Description {
        val description = Description.createSuiteDescription("GherkinRunner")
        features.forEach { feature ->
            val featureSuiteDescription = Description.createSuiteDescription(feature.name)
            val beforeFeature = Description.createTestDescription(feature.name, "> Before feature")
            featureSuiteDescription.addChild(beforeFeature)
            feature.scenarios.forEach { scenario ->
                val scenarioSuiteDescription = Description.createSuiteDescription(scenario.name)
                val beforeScenario = Description.createTestDescription(scenario.name, "> Before scenario")
                scenarioSuiteDescription.addChild(beforeScenario)
                scenario.steps.forEach { step ->
                    val stepDescription = Description.createTestDescription(scenario.name, step.fullContent)
                    scenarioSuiteDescription.addChild(stepDescription)
                }
                val afterScenario = Description.createTestDescription(scenario.name, "> After scenario")
                scenarioSuiteDescription.addChild(afterScenario)
                featureSuiteDescription.addChild(scenarioSuiteDescription)
            }
            val afterFeature = Description.createTestDescription(feature.name, "> After feature")
            featureSuiteDescription.addChild(afterFeature)
            description.addChild(featureSuiteDescription)
        }
        return description
    }
}