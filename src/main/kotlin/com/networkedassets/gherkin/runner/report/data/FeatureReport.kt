package com.networkedassets.gherkin.runner.report.data

import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario

class FeatureReport(val feature: GherkinFeature, parent: ReportEntry) : ReportEntry(parent) {
    val scenarioReports: MutableList<ScenarioReport> = mutableListOf()

    override var state: ReportState? = null
        get() =
            if (field == null) {
                if(anyChildScenarioFailed())
                    ReportState.FAILED
                else
                    ReportState.PASSED
            } else
                field

    private fun anyChildScenarioFailed() = scenarioReports.any { scenarioReport ->
        scenarioReport.state == ReportState.FAILED
    }

    fun getNumberOfScenarios() = scenarioReports.size
    fun getNumberOfSkippedScenarios() = getNumberOfScenariosWithState(ReportState.SKIPPED)
    fun getNumberOfFailedScenarios() = getNumberOfScenariosWithState(ReportState.FAILED)
    fun getNumberOfPassedScenarios() = getNumberOfScenariosWithState(ReportState.PASSED)
    fun getNumberOfNotImplementedScenarios() = getNumberOfScenariosWithState(ReportState.NOT_IMPLEMENTED)
    fun getNumberOfMultipleImplementationsScenarios() = getNumberOfScenariosWithState(ReportState.MULTIPLE_IMPLEMENTATIONS)


    fun getNumberOfSteps() = scenarioReports.map { it.getNumberOfSteps() }.sum()
    fun getNumberOfSkippedSteps() = scenarioReports.map { it.getNumberOfSkippedSteps() }.sum()
    fun getNumberOfFailedSteps() = scenarioReports.map { it.getNumberOfFailedSteps() }.sum()
    fun getNumberOfPassedSteps() = scenarioReports.map { it.getNumberOfPassedSteps() }.sum()
    fun getNumberOfNotImplementedSteps() = scenarioReports.map { it.getNumberOfNotImplementedSteps() }.sum()
    fun getNumberOfMultipleImplementationsSteps() = scenarioReports.map { it.getNumberOfMultipleImplementationsSteps() }.sum()

    private fun getNumberOfScenariosWithState(state: ReportState) =
            scenarioReports.filter { scenarioReport -> scenarioReport.state == state }.size

    fun addForScenario(scenario: GherkinScenario): ScenarioReport {
        val scenarioReport = ScenarioReport(scenario, this)
        scenarioReports.add(scenarioReport)
        return scenarioReport
    }
}