package org.na.gherkin.runner.metadata

import org.na.gherkin.runner.metadata.TestEndpointType.ACTUAL

class TestActualEndpoint(name: String, val url: String, val status: TestEndpointStatus) : TestEndpoint(name, ACTUAL)