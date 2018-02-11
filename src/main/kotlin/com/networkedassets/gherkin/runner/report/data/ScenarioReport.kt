package com.networkedassets.gherkin.runner.report.data

import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.gherkin.GherkinStep

class ScenarioReport(val scenario: GherkinScenario, parent: ReportEntry) : ReportEntry(parent) {
    val stepReports: MutableList<StepReport> = mutableListOf()

    override var state: ReportState? = null
        get() =
            if (field == null) {
                if(anyChildStepFailed())
                    ReportState.FAILED
                else
                    ReportState.PASSED
            } else
                field

    private fun anyChildStepFailed() = stepReports.any { stepReport ->
        stepReport.state == ReportState.FAILED
    }

    fun getNumberOfSteps() = stepReports.size
    fun getNumberOfSkippedSteps() = getNumberOfStepsWithState(ReportState.SKIPPED)
    fun getNumberOfFailedSteps() = getNumberOfStepsWithState(ReportState.FAILED)
    fun getNumberOfPassedSteps() = getNumberOfStepsWithState(ReportState.PASSED)
    fun getNumberOfNotImplementedSteps() = getNumberOfStepsWithState(ReportState.NOT_IMPLEMENTED)
    fun getNumberOfMultipleImplementationsSteps() = getNumberOfStepsWithState(ReportState.MULTIPLE_IMPLEMENTATIONS)

    private fun getNumberOfStepsWithState(state: ReportState) =
            stepReports.filter { stepReport -> stepReport.state == state }.size

    fun addForStep(step: GherkinStep): StepReport {
        val stepReport = StepReport(step, this)
        stepReports.add(stepReport)
        return stepReport
    }
}