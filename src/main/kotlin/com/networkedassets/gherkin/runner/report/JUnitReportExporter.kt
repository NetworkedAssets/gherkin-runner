package com.networkedassets.gherkin.runner.report

import com.networkedassets.gherkin.runner.junit.Failure
import com.networkedassets.gherkin.runner.junit.Properties
import com.networkedassets.gherkin.runner.junit.Testcase
import com.networkedassets.gherkin.runner.junit.Testsuite
import com.networkedassets.gherkin.runner.report.data.FeatureReport
import com.networkedassets.gherkin.runner.report.data.Report
import com.networkedassets.gherkin.runner.report.data.ReportState
import com.networkedassets.gherkin.runner.report.data.ScenarioReport
import java.io.StringWriter
import java.net.InetAddress
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class JUnitReportExporter : ReportExporter {
    override val name = "JUnit"

    override fun export(report: Report) {
        val result = report.featureReports.map(::convertFeatureReportToTestsuite)
        result.forEach { testsuite ->
            val junitReportFilePath = "${reportsDir()}/junit/JUNIT-REPORT-${testsuite.name}.xml"
            writeToFile(junitReportFilePath, testsuiteToXMLString(testsuite))
        }
    }

    private fun convertFeatureReportToTestsuite(featureReport: FeatureReport) =
            Testsuite().apply {
                name = featureReport.feature.name
                tests = featureReport.getNumberOfScenarios().toString()
                skipped = (featureReport.getNumberOfSkippedScenarios() + featureReport.getNumberOfMultipleImplementationsScenarios()
                        + featureReport.getNumberOfNotImplementedScenarios()).toString()
                failures = featureReport.getNumberOfFailedScenarios().toString()
                errors = "0"
                timestamp = featureReport.startTime?.format(DateTimeFormatter.ISO_DATE_TIME)
                hostname = InetAddress.getLocalHost().hostName
                time = (featureReport.executionTime / 1000.0).toString()
                properties = Properties()
                systemOut = featureReport.log.joinToString("\n") { it.message }
                systemErr = ""
                val repeatedScenarios = featureReport.scenarioReports.groupBy { it.scenario.name }.filter { it.value.size > 1 }
                val notRepeatedScenarios = featureReport.scenarioReports.groupBy { it.scenario.name }.filter { it.value.size == 1 }
                val testcases = repeatedScenarios.flatMap {
                    it.value.mapIndexed { i, scenarioReport ->
                        convertScenarioReportToTestcase(featureReport.feature.name, "${scenarioReport.scenario.name} #${i + 1}", scenarioReport)
                    }
                }.union(
                        notRepeatedScenarios.flatMap {
                            it.value.map {
                                convertScenarioReportToTestcase(featureReport.feature.name, it.scenario.name, it)
                            }
                        }
                )
                testcase.addAll(testcases)
            }

    private fun convertScenarioReportToTestcase(featureName: String, scenarioName: String?, scenarioReport: ScenarioReport) =
            Testcase().apply {
                name = scenarioName
                classname = featureName
                time = (scenarioReport.executionTime / 1000.0).toString()
                val failures = scenarioReport.stepReports.filter { scenarioReport.state == ReportState.FAILED }.map {
                    Failure().apply {
                        message = it.getError()
                        type = it.thrownException?.let { it::class.qualifiedName }
                        content = it.getErrorStackTrace()
                    }
                }
                failure.addAll(failures)
                val skippedStates = listOf(ReportState.NOT_IMPLEMENTED, ReportState.SKIPPED, ReportState.MULTIPLE_IMPLEMENTATIONS,
                        ReportState.MULTIPLE_IMPLEMENTATIONS)
                if (scenarioReport.state in skippedStates) {
                    skipped = ""
                }
            }

    private fun testsuiteToXMLString(testsuite: Testsuite): String {
        val jaxbContext = JAXBContext.newInstance(Testsuite::class.java)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)

        val stringWriter = StringWriter()
        stringWriter.use {
            marshaller.marshal(testsuite, stringWriter)
        }
        return stringWriter.toString()
    }
}