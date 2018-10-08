package com.networkedassets.gherkin.runner

import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.report.ElasticsearchReportExporter
import com.networkedassets.gherkin.runner.report.HPQCReportExporter
import com.networkedassets.gherkin.runner.report.HTMLReportExporter
import com.networkedassets.gherkin.runner.report.JSONReportExporter
import com.networkedassets.gherkin.runner.report.JUnitReportExporter
import com.networkedassets.gherkin.runner.report.ReportExporter
import com.networkedassets.gherkin.runner.report.data.Report
import com.networkedassets.gherkin.runner.runners.FeatureRunner
import com.networkedassets.gherkin.runner.util.GherkinLoader
import com.networkedassets.gherkin.runner.util.Reflection
import mu.KotlinLogging
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.notification.RunNotifier
import kotlin.reflect.KClass


class GherkinRunner(private val clazz: Class<*>) : Runner(), Filterable {
    private val featureFilter = Reflection.getFeatureAnnotationValue(clazz)
    private var scenarioFilter: String? = null
    private lateinit var features: List<GherkinFeature>
    private lateinit var description: Description

    val log = KotlinLogging.logger { }

    override fun run(notifier: RunNotifier) {
        log.info { "Starting GherkinRunner test suite" }
        initializeFeatures()
        initializeDescription()
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
                HPQCReportExporter::class,
                JUnitReportExporter::class)
        extensions.addAll(Reflection.getExtensions(clazz) ?: setOf())
        val reports = Reflection.getReports(clazz) ?: setOf("HTML")

        ReportExporter.exportReport(report, extensions, reports)

        val elasticsearchReporting = Reflection.getElasticsearchReporting(clazz)
        if (elasticsearchReporting != null) {
            log.info("Elasticsearch reporting turned on, trying to put report into Elasticsearch")
            ElasticsearchReportExporter.reportToElasticsearch(elasticsearchReporting, report)
        }
        log.info("Report has been exported")
    }

    private fun featuresJoinedWithDescriptions() = features.zip(description.children)

    override fun filter(filter: Filter) {
        val filterDescription = filter.describe()
        if(filterDescription.startsWith("Method ")) {
            scenarioFilter = filterDescription.removePrefix("Method ").split("(")[0]
        }
    }

    override fun getDescription(): Description {
        initializeFeatures()
        initializeDescription()
        return description
    }

    private fun initializeDescription() {
        if(!::description.isInitialized) {
            description = Description.createSuiteDescription("GherkinRunner")
            features.forEach { feature ->
                val featureName = feature.name.replaceDotsWithIntelliJFriendlyDots()
                val featureSuiteDescription = Description.createSuiteDescription(featureName)
                val beforeFeature = Description.createTestDescription(featureName, "> Before feature")
                featureSuiteDescription.addChild(beforeFeature)
                feature.scenarios.forEach { scenario ->
                    val scenarioName = scenario.name.replaceDotsWithIntelliJFriendlyDots()
                    val scenarioSuiteDescription = Description.createSuiteDescription(scenarioName)
                    val beforeScenario = Description.createTestDescription(scenarioName, "> Before scenario")
                    scenarioSuiteDescription.addChild(beforeScenario)
                    scenario.steps.forEach { step ->
                        val stepContent = step.fullContent.replaceDotsWithIntelliJFriendlyDots()
                        val stepDescription = Description.createTestDescription(scenarioName, stepContent)
                        scenarioSuiteDescription.addChild(stepDescription)
                    }
                    val afterScenario = Description.createTestDescription(scenarioName, "> After scenario")
                    scenarioSuiteDescription.addChild(afterScenario)
                    featureSuiteDescription.addChild(scenarioSuiteDescription)
                }
                val afterFeature = Description.createTestDescription(featureName, "> After feature")
                featureSuiteDescription.addChild(afterFeature)
                description.addChild(featureSuiteDescription)
            }
        }
    }

    private fun initializeFeatures() {
        if(!::features.isInitialized) features = GherkinLoader.loadFeatures(featureFilter = featureFilter, scenarioFilter = scenarioFilter)
    }

    private fun String.replaceDotsWithIntelliJFriendlyDots() = this.replace('.', '․')
}