package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.gherkin.GherkinBackground
import com.networkedassets.gherkin.runner.gherkin.GherkinExamples
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.gherkin.GherkinScenarioBase
import com.networkedassets.gherkin.runner.gherkin.GherkinStep
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import gherkin.ast.Background
import gherkin.ast.DataTable
import gherkin.ast.Examples
import gherkin.ast.Feature
import gherkin.ast.Scenario
import gherkin.ast.ScenarioDefinition
import gherkin.ast.ScenarioOutline
import gherkin.ast.Step

object GherkinConverter {
    fun convertFeature(feature: Feature): GherkinFeature {
        val gherkinFeature = GherkinFeature(feature.name, feature.tags.map { it.name })
        feature.children
                .filter { it -> it is Scenario || it is ScenarioOutline }
                .flatMap {
                    val scenario = convertScenario(gherkinFeature, it)
                    if (scenario.isOutline) converOutlineToManyScenarios(scenario)
                    else listOf(scenario)
                }.forEach {
                    gherkinFeature.addScenario(it)
                }
        feature.children
                .filterIsInstance<Background>()
                .map {
                    convertBackground(gherkinFeature, it)
                }.forEach {
                    gherkinFeature.addBackground(it)
                }

        return gherkinFeature
    }

    private fun converOutlineToManyScenarios(scenario: GherkinScenario) =
            scenario.examples.flatMap { example ->
                example.bindings.map { binding ->
                    val gherkinScenario = GherkinScenario(scenario.name.fillPlaceholdersWithValues(binding),
                            scenario.description?.fillPlaceholdersWithValues(binding), example.tags, scenario.feature, scenario, binding)
                    scenario.steps.forEach { step ->
                        val gherkinStep = GherkinStep(step.keyword, step.realKeyword, step.content.fillPlaceholdersWithValues(binding), scenario,
                                step.data, step.content)
                        gherkinScenario.addStep(gherkinStep)
                    }
                    gherkinScenario
                }
            }

    private fun convertScenario(feature: GherkinFeature, scenario: ScenarioDefinition): GherkinScenario {
        val scenarioTags = when (scenario) {
            is Scenario -> scenario.tags
            is ScenarioOutline -> scenario.tags
            else -> emptyList()
        }.map { it.name }
        val scenarioTagsMergedWithFeatureTags = listOf(scenarioTags, feature.tags).flatten().distinct()
        val gherkinScenario = GherkinScenario(scenario.name, scenario.description?.trim(), scenarioTagsMergedWithFeatureTags, feature)
        scenario.steps.forEach {
            val stepKeyword = convertStepKeyword(it.keyword)
            val realKeyword =
                    if (stepKeyword == StepKeyword.AND)
                        if (scenario.steps.isEmpty()) StepKeyword.GIVEN else gherkinScenario.steps.last().realKeyword
                    else stepKeyword
            val gherkinStep = convertStep(gherkinScenario, stepKeyword, realKeyword, it)
            gherkinScenario.addStep(gherkinStep)
        }
        if (scenario is ScenarioOutline) {
            scenario.examples.forEach { examples ->
                gherkinScenario.addExamples(convertExamples(examples, gherkinScenario))
            }
        }
        return gherkinScenario
    }

    private fun convertBackground(feature: GherkinFeature, background: Background): GherkinBackground {
        val gherkinBackground = GherkinBackground(background.name, background.description?.trim(), feature)
        background.steps.forEach {
            val stepKeyword = convertStepKeyword(it.keyword)
            val realKeyword =
                    if (stepKeyword == StepKeyword.AND)
                        if (background.steps.isEmpty()) StepKeyword.GIVEN else gherkinBackground.steps.last().realKeyword
                    else stepKeyword
            val gherkinStep = convertStep(gherkinBackground, stepKeyword, realKeyword, it)
            gherkinBackground.addStep(gherkinStep)
        }
        return gherkinBackground
    }

    private fun convertExamples(examples: Examples,
                                gherkinScenario: GherkinScenario): GherkinExamples {
        val examplesTags = examples.tags.map { it.name }
        val gherkinExamples = GherkinExamples(examples.name, examples.description,
                listOf(examplesTags, gherkinScenario.tags).flatten().distinct(), gherkinScenario)
        examples.tableBody.forEach { tableRow ->
            val examplesPairs = tableRow.cells
                    .mapIndexed { index, tableCell -> Pair(examples.tableHeader.cells[index].value, tableCell.value) }
                    .toTypedArray()
            gherkinExamples.addBinding(mapOf(*examplesPairs))
        }
        return gherkinExamples
    }

    private fun convertStep(scenario: GherkinScenarioBase, stepKeyword: StepKeyword, realKeyword: StepKeyword, step: Step): GherkinStep {
        val argument = step.argument
        val data = argument as? DataTable
        return GherkinStep(stepKeyword, realKeyword, step.text, scenario, data?.to2DArray())
    }

    private fun convertStepKeyword(keyword: String): StepKeyword {
        return StepKeyword.valueOf(keyword.toUpperCase().trim())
    }

    private fun DataTable.to2DArray() = this.rows.map { it.cells.map { it.value }.toTypedArray() }.toTypedArray()

    //TODO: to be discussed if it is still needed?
    private fun String.fillPlaceholdersWithValues(bindings: Map<String, String>) =
            bindings.toList().fold(this) { acc, binding ->
                val first = binding.first
                val matchEntire = Regex("[#]?(.[^ ]*)$|[#]?(.*)([ ][<].*)$").matchEntire(first)
                if (matchEntire != null) {
                    val (firstGroup, secondGroup) = matchEntire.destructured
                    val expToReplace = if (!firstGroup.isEmpty()) firstGroup else secondGroup
                    acc.replace("<$expToReplace>", binding.second)
                } else {
                    acc.replace("<$first>", binding.second)
                }
            }

}
