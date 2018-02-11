package com.networkedassets.gherkin.runner.gherkin

import java.io.Serializable

data class GherkinFeature(val name: String) : Serializable {
    val scenarios = mutableListOf<GherkinScenario>()

    fun addScenario(scenario: GherkinScenario) {
        scenarios.add(scenario)
    }
}