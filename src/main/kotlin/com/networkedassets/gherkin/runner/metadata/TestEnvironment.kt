package com.networkedassets.gherkin.runner.metadata

class TestEnvironment(val name: String) {
    val endpoints = mutableListOf<TestEndpoint>()

    fun addMockEndpoint(name: String) {
        endpoints.add(TestMockEndpoint(name))
    }

    fun addActualEndpoint(name: String, url: String, status: TestEndpointStatus) {
        endpoints.add(TestActualEndpoint(name, url, status))
    }
}