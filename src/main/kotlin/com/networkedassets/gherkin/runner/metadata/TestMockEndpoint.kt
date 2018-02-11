package com.networkedassets.gherkin.runner.metadata

import com.networkedassets.gherkin.runner.metadata.TestEndpointType.MOCK

class TestMockEndpoint(name: String): TestEndpoint(name, MOCK)