package com.networkedassets.gherkin.runner.metadata

class MetadataListeners {
    var featureMetadataListener: ((Any) -> Unit)? = null
    var scenarioMetadataListener: ((Any) -> Unit)? = null
    var stepMetadataListener: ((Any) -> Unit)? = null
}