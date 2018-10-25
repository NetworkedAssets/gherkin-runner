package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.networkedassets.gherkin.runner.specification.ExampleBindings
import java.io.Serializable

data class GherkinFeature(
        val name: String,
        val tags: List<String>,
        var backgrounds: GherkinBackground,
        var envBindings: ExampleBindings
        ) : Serializable {
    @JsonIgnore
    val scenarios = mutableListOf<GherkinScenario>()


    fun addScenario(scenario: GherkinScenario) {
        scenarios.add(scenario)
    }
}