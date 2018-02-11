package com.networkedassets.gherkin.runner.gherkin

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable

data class GherkinScenario(val name: String,
                           val description: String?,
                           @JsonIgnore val feature: GherkinFeature) : Serializable {
    val steps = mutableListOf<GherkinStep>()

    val featureName
        get() = this.feature.name

    val fullTree = "$featureName/$name"

    fun addStep(step: GherkinStep) {
        steps.add(step)
    }
}