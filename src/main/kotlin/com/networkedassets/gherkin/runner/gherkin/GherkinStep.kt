package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class GherkinStep(val keyword: StepKeyword,
                       val realKeyword: StepKeyword,
                       val content: String,
                       @JsonIgnore val scenario: GherkinScenario,
                       @JsonIgnore val data: Array<Array<String>>? = null,
                       val outlinedContent: String? = null) : Serializable {
    val featureName
        get() = this.scenario.feature.name

    val scenarioName
        get() = this.scenario.name

    val fullContent
        get() = "${keyword.toString().toLowerCase().capitalize()} $content"

    val fullTree = "$featureName/$scenarioName/$fullContent"
}