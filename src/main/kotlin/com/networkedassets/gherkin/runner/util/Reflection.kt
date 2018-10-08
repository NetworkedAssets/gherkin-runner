package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.annotation.*
import com.networkedassets.gherkin.runner.exception.MultipleImplementationsException
import com.networkedassets.gherkin.runner.exception.NotFoundImplementationException
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import com.networkedassets.gherkin.runner.gherkin.GherkinStep
import com.networkedassets.gherkin.runner.gherkin.StepKeyword
import com.networkedassets.gherkin.runner.metadata.RunnerMetadata
import com.networkedassets.gherkin.runner.report.data.CallbackType
import com.networkedassets.gherkin.runner.specification.FeatureSpecification
import groovy.lang.Closure
import org.reflections.Reflections
import java.lang.reflect.Method
import kotlin.reflect.KClass

object Reflection {
    fun getFeatureSpecification(implementationsPackage: String, feature: GherkinFeature): FeatureSpecification {
        val featureName = feature.name
        val reflections = Reflections(implementationsPackage)
        val featureClasses = reflections.getTypesAnnotatedWith(Feature::class.java).filter {
            val annotation = it.annotations.filter { it is Feature }[0] as Feature
            annotation.value == featureName
        }

        if (featureClasses.size > 1) {
            throw MultipleImplementationsException("Multiple feature implementations for $featureName")
        } else if (featureClasses.isEmpty()) {
            throw NotFoundImplementationException("Feature implementation not found for $featureName")
        }
        val featureSpecification = featureClasses.first().newInstance() as FeatureSpecification
        featureSpecification.backgrounds = feature.backgrounds
        return featureSpecification
    }

    fun getMethodForScenario(featureSpecification: FeatureSpecification, scenario: GherkinScenario): Method {
        val scenarioName = scenario.outline?.name ?: scenario.name
        val scenarioMethods = featureSpecification.javaClass.methods.filter({ it.name == scenarioName })

        if (scenarioMethods.size > 1) {
            throw MultipleImplementationsException("Multiple scenario implementations for ${scenario.fullTree}")
        } else if (scenarioMethods.isEmpty()) {
            throw NotFoundImplementationException("Scenario implementation not found for ${scenario.fullTree}")
        }
        return scenarioMethods.first()
    }

    fun getClosureForStep(stepDefs: Map<Pair<StepKeyword, String>, Closure<Any>>, step: GherkinStep): Closure<Any> {
        val stepContent = step.outlinedContent ?: step.content
        return stepDefs[Pair(step.realKeyword, stepContent)]
                ?: throw NotFoundImplementationException("Step implementation not found for ${step.fullTree}")
    }

    fun getCallbackMethod(featureSpecification: FeatureSpecification, callbackType: CallbackType): Method {
        return when (callbackType) {
            CallbackType.BEFORE_FEATURE -> getCallbackMethod(featureSpecification, BeforeFeature::class)
            CallbackType.AFTER_FEATURE -> getCallbackMethod(featureSpecification, AfterFeature::class)
            CallbackType.BEFORE_SCENARIO -> getCallbackMethod(featureSpecification, BeforeScenario::class)
            CallbackType.AFTER_SCENARIO -> getCallbackMethod(featureSpecification, AfterScenario::class)
        }
    }

    private fun getCallbackMethod(featureSpecification: FeatureSpecification, annotationClass: KClass<out Annotation>): Method {
        val callbackMethods = featureSpecification.javaClass.methods
                .filter({ it.getAnnotation(annotationClass.java) != null })
        if (callbackMethods.size > 1) {
            throw MultipleImplementationsException("Multiple ${annotationClass.simpleName} callback implementations for ${featureSpecification::class.simpleName}")
        } else if (callbackMethods.isEmpty()) {
            throw NotFoundImplementationException("${annotationClass.simpleName} Callback implementation not found for ${featureSpecification::class.simpleName}")
        }
        return callbackMethods.first()
    }

    fun getGherkinRunnerMetadata(runnerClass: Class<*>): RunnerMetadata {
        try {
            val metadataMethod = runnerClass.getMethod("metadata")
            if (metadataMethod.returnType.isAssignableFrom(RunnerMetadata::class.java)) {
                metadataMethod.invoke(runnerClass.newInstance()) as RunnerMetadata
            }
        } catch (ignore: NoSuchMethodException) {
        }
        return RunnerMetadata()
    }

    fun getImplementationsPackage(runnerClass: Class<*>): String? {
        val implementationsPackage = runnerClass.getAnnotation(ImplementationsPackage::class.java)
        return implementationsPackage?.value
    }

    fun getExtensions(runnerClass: Class<*>): Set<KClass<*>>? {
        val extensions = runnerClass.getAnnotation(Extensions::class.java)
        return extensions?.value?.toSet()
    }

    fun getReports(runnerClass: Class<*>): Set<String>? {
        val reports = runnerClass.getAnnotation(Reports::class.java)
        return reports?.value?.toSet()
    }

    fun getFeatureAnnotationValue(runnerClass: Class<*>): String? {
        val reports = runnerClass.getAnnotation(Feature::class.java)
        return reports?.value
    }

    fun getElasticsearchReporting(runnerClass: Class<*>): ElasticSearchReporting? {
        return runnerClass.getAnnotation(ElasticSearchReporting::class.java)
    }
}