package com.networkedassets.gherkin.runner.gherkin

import gherkin.ast.Background

class GherkinBackground(val background: List<Background>) {

    fun isEmpty(): Boolean {
        return background.isEmpty()
    }
}