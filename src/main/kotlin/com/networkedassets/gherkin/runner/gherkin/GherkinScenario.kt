package com.networkedassets.gherkin.runner.gherkin

class GherkinScenario(
        name: String,
        description: String?,
        val tags: List<String>,
        feature: GherkinFeature,
        val outline: GherkinScenario? = null,
        val bindings: Map<String, String>? = null
) : GherkinScenarioBase(name, description, feature) {
    override val name: String = super.name!!

    val examples = mutableListOf<GherkinExamples>()

    val isOutline
        get() = !this.examples.isEmpty()

    fun addExamples(gherkinExamples: GherkinExamples) {
        examples.add(gherkinExamples)
    }
}