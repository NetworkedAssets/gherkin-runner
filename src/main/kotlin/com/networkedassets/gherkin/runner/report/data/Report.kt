package com.networkedassets.gherkin.runner.report.data

import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.metadata.TestEnvironment

class Report(val suiteName: String, val environment: TestEnvironment? = null) : ReportEntry(null) {
    val featureReports: MutableList<FeatureReport> = mutableListOf()

    override var state: ReportState? = null
        get() =
            if (field == null) {
                if(anyChildFeatureFailed())
                    ReportState.FAILED
                else
                    ReportState.PASSED
            } else
                field

    private fun anyChildFeatureFailed() = featureReports.any { featureReport ->
        featureReport.state == ReportState.FAILED
    }

    fun getNumberOfFeatures() = featureReports.size
    fun getNumberOfSkippedFeatures() = getNumberOfFeaturesWithState(ReportState.SKIPPED)
    fun getNumberOfFailedFeatures() = getNumberOfFeaturesWithState(ReportState.FAILED)
    fun getNumberOfPassedFeatures() = getNumberOfFeaturesWithState(ReportState.PASSED)
    fun getNumberOfNotImplementedFeatures() = getNumberOfFeaturesWithState(ReportState.NOT_IMPLEMENTED)
    fun getNumberOfMultipleImplementationsFeatures() = getNumberOfFeaturesWithState(ReportState.MULTIPLE_IMPLEMENTATIONS)


    fun getNumberOfScenarios() = featureReports.map { it.getNumberOfScenarios() }.sum()
    fun getNumberOfSkippedScenarios() = featureReports.map { it.getNumberOfSkippedScenarios() }.sum()
    fun getNumberOfFailedScenarios() = featureReports.map { it.getNumberOfFailedScenarios() }.sum()
    fun getNumberOfPassedScenarios() = featureReports.map { it.getNumberOfPassedScenarios() }.sum()
    fun getNumberOfNotImplementedScenarios() = featureReports.map { it.getNumberOfNotImplementedScenarios() }.sum()
    fun getNumberOfMultipleImplementationsScenarios() = featureReports.map { it.getNumberOfMultipleImplementationsScenarios() }.sum()


    fun getNumberOfSteps() = featureReports.map { it.getNumberOfSteps() }.sum()
    fun getNumberOfSkippedSteps() = featureReports.map { it.getNumberOfSkippedSteps() }.sum()
    fun getNumberOfFailedSteps() = featureReports.map { it.getNumberOfFailedSteps() }.sum()
    fun getNumberOfPassedSteps() = featureReports.map { it.getNumberOfPassedSteps() }.sum()
    fun getNumberOfNotImplementedSteps() = featureReports.map { it.getNumberOfNotImplementedSteps() }.sum()
    fun getNumberOfMultipleImplementationsSteps() = featureReports.map { it.getNumberOfMultipleImplementationsSteps() }.sum()

    private fun getNumberOfFeaturesWithState(state: ReportState) =
            featureReports.filter { featureReport -> featureReport.state == state }.size

    fun addForFeature(feature: GherkinFeature): FeatureReport {
        val featureReport = FeatureReport(feature, this)
        featureReports.add(featureReport)
        return featureReport
    }
}