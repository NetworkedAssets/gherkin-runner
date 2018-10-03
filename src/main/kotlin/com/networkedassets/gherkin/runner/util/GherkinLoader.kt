package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.exception.InvalidTagsExpressionException
import com.networkedassets.gherkin.runner.gherkin.GherkinFeature
import com.networkedassets.gherkin.runner.gherkin.GherkinScenario
import gherkin.AstBuilder
import gherkin.Parser
import gherkin.ast.GherkinDocument
import mu.KotlinLogging
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.util.regex.Pattern
import javax.script.ScriptEngineManager


object GherkinLoader {
    val log = KotlinLogging.logger { }

    @JvmStatic
    @JvmOverloads
    fun loadFeatures(packagePrefix: String = "", featureFilter: String? = null, scenarioFilter: String? = null): List<GherkinFeature> {
        log.info { "Loading feature specifications from package: '$packagePrefix' with feature filter: '$featureFilter' and scenario filter: '$scenarioFilter'" }
        val reflections = Reflections(packagePrefix, ResourcesScanner())
        val fileNames = reflections.getResources(Pattern.compile(".*\\.feature"))
        return fileNames.map({ readFeatureFromFile(it) }).filter(featureFilter, scenarioFilter)
    }

    fun loadGenericGherkinData(path: String) : gherkin.ast.Feature {
        log.info { "Reading feature file: '$path'" }
        val parser = Parser<GherkinDocument>(AstBuilder())
        val featureFileContent = this.javaClass.classLoader.getResource(path).readText()
        val gherkinDocument = parser.parse(featureFileContent)
        return gherkinDocument.feature
    }


    private fun readFeatureFromFile(path: String): GherkinFeature {
        log.info { "Reading feature file: '$path'" }
        val parser = Parser<GherkinDocument>(AstBuilder())
        val featureFileContent = this.javaClass.classLoader.getResource(path).readText()
        val gherkinDocument = parser.parse(featureFileContent)
        val feature = gherkinDocument.feature
        return GherkinConverter.convertFeature(feature)
    }

    private fun List<GherkinFeature>.filter(featureFilter: String? = null, scenarioFilter: String? = null): List<GherkinFeature> {
        val tagsExpression = System.getProperty("gherkinTags")
        if(!tagsExpression.isNullOrBlank()) log.info { "Filtering features and scenarios using expression: '$tagsExpression'" }
        return this.filter { filterFeature(it, featureFilter) }.map {
            val gherkinFeature = it.copy()
            gherkinFeature.scenarios.addAll(it.scenarios.filter { filterScenario(it, scenarioFilter, tagsExpression) })
            gherkinFeature
        }.filter { it.scenarios.size > 0 }
    }

    private fun filterFeature(feature: GherkinFeature, featureFilter: String?) =
            (featureFilter == null || feature.name == featureFilter)

    private fun filterScenario(scenario: GherkinScenario,
                               scenarioFilter: String?,
                               tagsExpression: String?) =
            (scenarioFilter == null || scenario.name == scenarioFilter || scenario.outline?.name == scenarioFilter)
                    && matchToTagsExpression(tagsExpression, scenario.tags)

    private fun matchToTagsExpression(expression: String?, tags: List<String>): Boolean {
        return if (!expression.isNullOrEmpty()) {
            val replacedInput = expression!!.replace(" AND ", " && ").replace(" OR ", " || ").replace(" NOT ", " !").replace(" NOT(", " !(")
            val withTagsInput = replacedInput.replace("@[^\\s)]+".toRegex()) {
                tags.contains(it.value).toString()
            }
            try {
                ScriptEngineManager().getEngineByName("javascript").eval(withTagsInput) as Boolean
            } catch (e: Exception) {
                throw InvalidTagsExpressionException("Tags expression '$expression' is invalid and can not be parsed!", e)
            }
        } else true
    }
}