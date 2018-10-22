package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class GherkinScenario(val name: String?,
                           val description: String?,
                           val tags: List<String>,
                           @JsonIgnore val feature: GherkinFeature,
                           val outline: GherkinScenario? = null,
                           val bindings: Map<String, String>? = null) : Serializable {
    @JsonIgnore
    val steps = mutableListOf<GherkinStep>()
    val examples = mutableListOf<GherkinExamples>()

    val featureName
        get() = this.feature.name

    val fullTree = "$featureName/$name"

    val isOutline
        get() = !this.examples.isEmpty()

    fun addStep(step: GherkinStep) {
        steps.add(step)
    }

    fun addExamples(gherkinExamples: GherkinExamples) {
        examples.add(gherkinExamples)
    }
}