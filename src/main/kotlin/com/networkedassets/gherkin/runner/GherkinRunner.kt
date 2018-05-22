package com.networkedassets.gherkin.runner

import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.report.ElasticsearchReportExporter
import com.networkedassets.gherkin.runner.report.HPQCExporter
import com.networkedassets.gherkin.runner.report.HTMLReportExporter
import com.networkedassets.gherkin.runner.report.JSONReportExporter
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
                HPQCExporter::class)
        extensions.addAll(Reflection.getExtensions(clazz) ?: setOf())
        val reports = Reflection.getReports(clazz) ?: setOf("HTML")

        ReportExporter.exportReport(report, extensions, reports)

        val elasticsearchReporting = Reflection.getElasticsearchReporting(clazz)
        if (elasticsearchReporting != null) {
            log.info("Elasticsaerch reporting turned on, trying to put report into Elastisearch")
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

    private fun initializeDescription() {
        if(!::description.isInitialized) description = Description.createSuiteDescription("GherkinRunner")
    }

    private fun initializeFeatures() {
        if(!::features.isInitialized) features = GherkinLoader.loadFeatures(featureFilter = featureFilter, scenarioFilter = scenarioFilter)
    }
}