package com.networkedassets.gherkin.runner.annotation

annotation class ElasticSearchReporting(val address: String,
                                        val port: Int,
                                        val clusterName: String,
                                        val index: String,
                                        val splitReport: Boolean = true)