package org.na.gherkin.runner.annotation

import kotlin.reflect.KClass

annotation class Extensions(vararg val value: KClass<*>)