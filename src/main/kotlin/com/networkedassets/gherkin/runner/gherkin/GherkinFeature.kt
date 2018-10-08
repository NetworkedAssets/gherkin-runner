package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import gherkin.ast.Background
import java.io.Serializable

data class GherkinFeature(
        val name: String,
        val tags: List<String>) : Serializable {
    @JsonIgnore
    val scenarios = mutableListOf<GherkinScenario>()

    @JsonIgnore
    val backgrounds = mutableListOf<Background>()

    fun addScenario(scenario: GherkinScenario) {
        scenarios.add(scenario)
    }

    fun addBackgrounds(background: List<Background>) {
        backgrounds.addAll(background)
    }
}