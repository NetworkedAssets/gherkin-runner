package com.networkedassets.gherkin.runner.data

enum class CriteriaType(val runPriority: Int) {
    ALL_SCENARIOS(1), NAME_CONTAINS(2), NAME_NOT_CONTAINS(3)
}