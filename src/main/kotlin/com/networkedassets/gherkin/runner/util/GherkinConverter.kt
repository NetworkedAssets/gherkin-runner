package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.gherkin.*
import com.networkedassets.gherkin.runner.specification.ExampleBindings
import gherkin.ast.*

object GherkinConverter {
    fun convertFeature(feature: Feature): GherkinFeature {
        val envBindings = ExampleBindings(convert2DArrToEnvBindings(convertBackground(feature).getDataTableForStep(0)))
        val gherkinFeature = GherkinFeature(feature.name,
                feature.tags.map { it.name },
                convertBackground(feature),
                envBindings)
        feature.children
                .flatMap {
                    val scenario = convertScenario(gherkinFeature, it)
                    if (scenario.isOutline) converOutlineToManyScenarios(scenario)
                    else listOf(scenario)
                }.forEach({
                    gherkinFeature.addScenario(it)
                })
        return gherkinFeature
    }

    private fun converOutlineToManyScenarios(scenario: GherkinScenario) =
            scenario.examples.flatMap { example ->
                example.bindings.map { binding ->
                    val gherkinScenario = GherkinScenario(scenario.name?.fillPlaceholdersWithValues(binding),
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

    private fun convert2DArrToEnvBindings(dataTableForStep: Array<Array<String>>?): Map<String, String> {
        val converted = mutableMapOf<String, String>()
        dataTableForStep
                ?.dropWhile { row -> row[0].contains("#") }
                ?.map { row -> converted.put(row[0], row[1]) }
        return converted
    }

    private fun convertBackground(feature: Feature): GherkinBackground {
        val filteredBackgr = feature.children.filterIsInstance<Background>()
        return GherkinBackground(filteredBackgr)
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

    private fun convertStep(scenario: GherkinScenario, stepKeyword: StepKeyword, realKeyword: StepKeyword, step: Step): GherkinStep {
        val argument = step.argument
        val data = argument as? DataTable
        return GherkinStep(stepKeyword, realKeyword, step.text, scenario, data?.to2DArray())
    }

    private fun convertStepKeyword(keyword: String): StepKeyword {
        return StepKeyword.valueOf(keyword.toUpperCase().trim())
    }

    private fun DataTable.to2DArray() = this.rows.map { it.cells.map { it.value }.toTypedArray() }.toTypedArray()


    /**
     * Allows 3 use cases for Example's table headers
     * 1: |#modem |
     *    | modem1|
     * 2: |#modem <Modem>|
     *    | modem1       |
     * 3: |modem <Modem> |
     *    | modem1       |
     * in order to bind variable from Steps use just <modem>
     * it would work with all 3 use-cases
     */
    private fun String.fillPlaceholdersWithValues(bindings: Map<String, String>) =
            bindings.toList().fold(this) { acc, binding ->
                val first = binding.first
                //implements optional hash sign use-case
                val matchEntire = Regex("[#]?(.[^ ]*)$|[#]?(.*)([ ][<].*)$").matchEntire(first)
                if (matchEntire != null) {
                    val (firstGroup, secondGroup) = matchEntire.destructured
                    val expToReplace: String
                    if (!firstGroup.isEmpty()) {
                        expToReplace = firstGroup
                    } else {
                        expToReplace = secondGroup
                    }
                    acc.replace("<${expToReplace}>", binding.second)
                } else {
                    acc.replace("<${first}>", binding.second)
                }
            }

}
