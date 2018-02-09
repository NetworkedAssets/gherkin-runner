package org.na.gherkin.runner.metadata

import org.na.gherkin.runner.metadata.TestEndpointType.MOCK

class TestMockEndpoint(name: String): TestEndpoint(name, MOCK)