package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class GherkinExamples(val name: String,
                           val description: String?,
                           val tags: List<String>,
                           @JsonIgnore val scenario: GherkinScenario) : Serializable {
    val bindings = mutableListOf<Map<String, String>>()

    val fullTree = "${scenario.fullTree}/$name"

    fun addBinding(binding: Map<String, String>) {
        bindings.add(binding)
    }
}