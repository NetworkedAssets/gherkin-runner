package com.networkedassets.gherkin.runner.util

import com.networkedassets.gherkin.runner.annotation.AfterFeature
import com.networkedassets.gherkin.runner.annotation.AfterScenario
import com.networkedassets.gherkin.runner.annotation.BeforeFeature
import com.networkedassets.gherkin.runner.annotation.BeforeScenario
import com.networkedassets.gherkin.runner.report.data.CallbackType

object CallbackMapper {

    fun CallbackType.toAnnotation() = when (this) {
        CallbackType.BEFORE_FEATURE -> BeforeFeature::class
        CallbackType.AFTER_FEATURE -> AfterFeature::class
        CallbackType.BEFORE_SCENARIO -> BeforeScenario::class
        CallbackType.AFTER_SCENARIO -> AfterScenario::class
    }

}