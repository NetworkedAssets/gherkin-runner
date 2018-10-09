package com.networkedassets.gherkin.runner.gherkin

import gherkin.ast.Background
import gherkin.ast.DataTable
import gherkin.ast.Step

class GherkinBackground(private val background: List<Background>) {
    fun getSteps(): List<Step> {
        return background.map { back -> back.steps }.flatten()
    }

    fun getStep(stepNum: Int): Step? {
        return background.map { back -> back.steps }.flatten().getOrNull(stepNum)
    }

    fun getDataTableForStep(stepNum: Int): Array<Array<String>>? {
        if (!containsData(stepNum)) {
            return null
        }
       return extractDataTable(getStep(stepNum))
    }

    fun containsData(stepNum: Int): Boolean {
        if (getStep(stepNum)?.argument == null) {
            return false
        }
        return true
    }

    fun getDataTableByStepName(stepName: String):  Array<Array<String>>? {
        val step = background.map { back -> back.steps }.flatten().find { step -> step.text.equals(stepName) }
        return extractDataTable(step)
    }

    private fun extractDataTable(step: Step?): Array<Array<String>>? {
        val argument = step?.argument
        val dataTable = argument as? DataTable
        return dataTable?.rows?.map { it.cells.map { it.value }.toTypedArray() }?.toTypedArray()
    }
}