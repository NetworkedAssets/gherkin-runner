
package com.networkedassets.gherkin.runner


import com.networkedassets.gherkin.runner.annotation.Feature
import com.networkedassets.gherkin.runner.specification.FeatureSpecification

@Feature("Provisioning KAI service")
class ProvisioningInternetAccessSpec extends FeatureSpecification {



    def "KAI service successful provisioning <modem>"() {

        given("<modem> is OFF") {
            println "fsdfsd"
//            println properties
        }

    }
}