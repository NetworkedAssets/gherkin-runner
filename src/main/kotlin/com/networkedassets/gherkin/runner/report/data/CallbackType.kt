package com.networkedassets.gherkin.runner.report.data

enum class CallbackType {
    BEFORE_FEATURE,
    AFTER_FEATURE,
    BEFORE_SCENARIO,
    AFTER_SCENARIO;

    fun humanize() = this.name.toLowerCase().capitalize().replace("_", " ")
}