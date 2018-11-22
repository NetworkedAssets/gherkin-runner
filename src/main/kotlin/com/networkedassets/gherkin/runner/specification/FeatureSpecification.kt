package com.networkedassets.gherkin.runner.specification

import com.networkedassets.gherkin.runner.GherkinRunner
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.AND
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.GIVEN
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.THEN
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.WHEN
import groovy.lang.Closure
import org.junit.runner.RunWith


@RunWith(GherkinRunner::class)
open class FeatureSpecification {
    val stepDefs = mutableMapOf<Pair<StepKeyword, String>, Closure<Any>>()
    lateinit var bindings: ExampleBindings
    lateinit var feature: GherkinFeature
    private var lastType: StepKeyword = GIVEN

    fun given(stepText: String, closure: Closure<Any>) {
        stepDefs.put(Pair(GIVEN, stepText), closure)
        lastType = GIVEN
    }

    fun `when`(stepText: String, closure: Closure<Any>) {
        stepDefs.put(Pair(WHEN, stepText), closure)
        lastType = WHEN
    }

    fun then(stepText: String, closure: Closure<Any>) {
        stepDefs.put(Pair(THEN, stepText), closure)
        lastType = THEN
    }

    fun and(stepText: String, closure: Closure<Any>) {
        when (lastType) {
            GIVEN -> given(stepText, closure)
            WHEN -> `when`(stepText, closure)
            THEN -> then(stepText, closure)
            AND -> throw IllegalStateException("Should never happen. AND token is illegal to be lastType")
        }
    }

    fun clearStepDefs() {
        stepDefs.clear()
    }
}