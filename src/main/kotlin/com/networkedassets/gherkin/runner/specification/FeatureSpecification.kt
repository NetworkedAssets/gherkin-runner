package com.networkedassets.gherkin.runner.specification

import com.networkedassets.gherkin.runner.GherkinRunner
import com.networkedassets.gherkin.runner.exception.InvalidMetadataManipulationException
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.AND
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.GIVEN
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.THEN
import com.networkedassets.gherkin.runner.gherkin.StepKeyword.WHEN
import com.networkedassets.gherkin.runner.metadata.MetadataListeners
import groovy.lang.Closure
import org.junit.runner.RunWith


@RunWith(GherkinRunner::class)
abstract class FeatureSpecification {
    val stepDefs = mutableMapOf<Pair<StepKeyword, String>, Closure<Any>>()
    lateinit var bindings: ExampleBindings
    lateinit var feature: GherkinFeature
    private var lastType: StepKeyword = GIVEN
    val metadataListeners = MetadataListeners()

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

    fun setFeatureMetadata(metadata: Any) {
        setMetadata(metadataListeners.featureMetadataListener, metadata, "setFeatureMetadata invoked out of feature implementation scope")
    }

    fun setScenarioMetadata(metadata: Any) {
        setMetadata(metadataListeners.scenarioMetadataListener, metadata, "setScenarioMetadata invoked out of scenario implementation scope")
    }

    fun setStepMetadata(metadata: Any) {
        setMetadata(metadataListeners.stepMetadataListener, metadata, "setStepMetadata invoked out of step implementation scope")
    }

    private fun setMetadata(listener: ((Any) -> Unit)?, metadata: Any, errorMessage: String) {
        if (listener != null) {
            listener(metadata)
        } else {
            throw InvalidMetadataManipulationException(errorMessage)
        }
    }
}