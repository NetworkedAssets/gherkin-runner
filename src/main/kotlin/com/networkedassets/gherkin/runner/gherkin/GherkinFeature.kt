package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.networkedassets.gherkin.runner.specification.ExampleBindings
import java.io.Serializable

data class GherkinFeature(
        val name: String,
        val tags: List<String>
        ) : Serializable {
    @JsonIgnore
    val scenarios = mutableListOf<GherkinScenario>()

    @JsonIgnore
    val backgrounds = mutableListOf<GherkinBackground>()

    fun addScenario(scenario: GherkinScenario) {
        scenarios.add(scenario)
    }

    fun addBackground(background: GherkinBackground) {
        backgrounds.add(background)
    }
}