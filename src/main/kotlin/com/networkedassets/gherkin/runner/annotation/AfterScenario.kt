package com.networkedassets.gherkin.runner.annotation

import com.networkedassets.gherkin.runner.data.CriteriaType

annotation class AfterScenario(val criteria: Array<Criteria> = [Criteria(type = CriteriaType.ALL_SCENARIOS)])