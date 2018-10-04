package com.networkedassets.gherkin.runner.specification

import com.networkedassets.gherkin.runner.GherkinRunner
import groovy.lang.Closure
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.*
import gherkin.ast.Background
import org.junit.runner.RunWith


@RunWith(GherkinRunner::class)
open class FeatureSpecification {
    val stepDefs = mutableMapOf<Pair<StepKeyword, String>, Closure<Any>>()
    lateinit var background: Background
    lateinit var bindings: ExampleBindings

    private var lastType: StepKeyword = GIVEN

    fun background(stepText: String, closure: Closure<Any>) {

    }

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