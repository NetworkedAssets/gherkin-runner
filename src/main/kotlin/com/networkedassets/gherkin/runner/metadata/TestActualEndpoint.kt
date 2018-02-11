package com.networkedassets.gherkin.runner.metadata

import com.networkedassets.gherkin.runner.metadata.TestEndpointType.ACTUAL

class TestActualEndpoint(name: String, val url: String, val status: TestEndpointStatus) : TestEndpoint(name, ACTUAL)