package com.networkedassets.gherkin.runner.annotation

import com.networkedassets.gherkin.runner.data.CriteriaType

annotation class Criteria(val type: CriteriaType, val value: String = "")