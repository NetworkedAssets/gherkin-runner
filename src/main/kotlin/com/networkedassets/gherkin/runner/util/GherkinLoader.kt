package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.gherkin.GherkinExamples
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.gherkin.GherkinStep
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.AND
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.GIVEN
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.valueOf
import com.networkedassets.gherkin.runner.util.GherkinLoader.to2DArray
import gherkin.AstBuilder
import gherkin.Parser
import gherkin.ast.DataTable
import gherkin.ast.Feature
import gherkin.ast.GherkinDocument
import gherkin.ast.ScenarioDefinition
import gherkin.ast.ScenarioOutline
import gherkin.ast.Step
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.util.regex.Pattern


object GherkinLoader {
    @JvmStatic
    @JvmOverloads
    fun loadFeatures(packagePrefix: String = "", featureFilter: String? = null, scenarioFilter: String? = null): List<GherkinFeature> {
        val reflections = Reflections(packagePrefix, ResourcesScanner())
        val fileNames = reflections.getResources(Pattern.compile(".*\\.feature"))
        return fileNames.map({ readFeatureFromFile(it) }).filter(featureFilter, scenarioFilter)
    }

    private fun List<GherkinFeature>.filter(featureFilter: String? = null, scenarioFilter: String? = null): List<GherkinFeature> {
        return this.filter { featureFilter == null || it.name == featureFilter }.map {
            val gherkinFeature = GherkinFeature(it.name)
            gherkinFeature.scenarios.addAll(it.scenarios.filter { scenario -> scenarioFilter == null ||
                    scenario.name == scenarioFilter || scenario.outline?.name == scenarioFilter })
            gherkinFeature
        }
    }

    private fun readFeatureFromFile(path: String): GherkinFeature {
        val parser = Parser<GherkinDocument>(AstBuilder())
        val featureFileContent = this.javaClass.classLoader.getResource(path).readText()
        val gherkinDocument = parser.parse(featureFileContent)
        val feature = gherkinDocument.feature
        return convertFeature(feature)
    }

    private fun convertFeature(feature: Feature): GherkinFeature {
        val gherkinFeature = GherkinFeature(feature.name)
        feature.children
                .flatMap {
                    val scenario = convertScenario(gherkinFeature, it)
                    if (scenario.isOutline) converOutlineToManyScenarios(scenario)
                    else listOf(scenario)
                }.forEach({ gherkinFeature.addScenario(it) })
        return gherkinFeature
    }

    private fun convertScenario(feature: GherkinFeature, scenario: ScenarioDefinition): GherkinScenario {
        val gherkinScenario = GherkinScenario(scenario.name, scenario.description?.trim(), feature)
        scenario.steps.forEach {
            val stepKeyword = convertStepKeyword(it.keyword)
            val realKeyword =
                    if (stepKeyword == AND)
                        if (scenario.steps.isEmpty()) GIVEN else gherkinScenario.steps.last().realKeyword
                    else stepKeyword
            val gherkinStep = convertStep(gherkinScenario, stepKeyword, realKeyword, it)
            gherkinScenario.addStep(gherkinStep)
        }
        if (scenario is ScenarioOutline) {
            scenario.examples.forEach { examples ->
                val gherkinExamples = GherkinExamples(examples.name, examples.description, gherkinScenario)
                examples.tableBody.forEach { tableRow ->
                    val examplesPairs = tableRow.cells
                            .mapIndexed { index, tableCell -> Pair(examples.tableHeader.cells[index].value, tableCell.value) }
                            .toTypedArray()
                    gherkinExamples.addBinding(mapOf(*examplesPairs))
                }
                gherkinScenario.addExamples(gherkinExamples)
            }
        }
        return gherkinScenario
    }

    private fun converOutlineToManyScenarios(scenario: GherkinScenario) =
            scenario.examples.flatMap { example ->
                example.bindings.map { binding ->
                    val gherkinScenario = GherkinScenario(scenario.name.fillPlaceholdersWithValues(binding),
                            scenario.description?.fillPlaceholdersWithValues(binding), scenario.feature, scenario, binding)
                    scenario.steps.forEach { step ->
                        val gherkinStep = GherkinStep(step.keyword, step.realKeyword, step.content.fillPlaceholdersWithValues(binding), scenario,
                                step.data, step.content)
                        gherkinScenario.addStep(gherkinStep)
                    }
                    gherkinScenario
                }
            }


    private fun convertStep(scenario: GherkinScenario, stepKeyword: StepKeyword, realKeyword: StepKeyword, step: Step): GherkinStep {
        val argument = step.argument
        val data = argument as? DataTable
        return GherkinStep(stepKeyword, realKeyword, step.text, scenario, data?.to2DArray())
    }

    private fun convertStepKeyword(keyword: String): StepKeyword {
        return valueOf(keyword.toUpperCase().trim())
    }

    private fun DataTable.to2DArray() = this.rows.map { it.cells.map { it.value }.toTypedArray() }.toTypedArray()


    private fun String.fillPlaceholdersWithValues(bindings: Map<String, String>) =
            bindings.toList().fold(this) { acc, binding -> acc.replace("<${binding.first}>", binding.second) }
}