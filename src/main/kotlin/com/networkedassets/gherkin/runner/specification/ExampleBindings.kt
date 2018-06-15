package com.networkedassets.gherkin.runner.specification

class ExampleBindings(val bindings: Map<String, String>?) {
    fun getString(name: String) = (getBidingsSafe()[name] ?: throw Exception("Binding for '$name' not found")).trim()

    fun getInt(name: String) = getString(name).toIntOrNull() ?: throw Exception("Binding for '$name' is not integer value")

    fun getDouble(name: String) = getString(name).toDoubleOrNull() ?: throw Exception("Binding for '$name' is not floating point value")

    fun getBoolean(name: String): Boolean {
        val valueLowercase = getString(name).toLowerCase()
        return when {
            listOf("y", "yes", "true").contains(valueLowercase) -> true
            listOf("n", "no", "false").contains(valueLowercase) -> false
            valueLowercase.endsWith("n't") || valueLowercase.endsWith(" not") -> false
            else -> true
        }
    }

    private fun getBidingsSafe() = bindings ?: throw Exception("This scenario does not have bindings")
}