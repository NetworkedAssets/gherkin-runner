
package com.networkedassets.gherkin.runner


import com.networkedassets.gherkin.runner.annotation.Feature
import com.networkedassets.gherkin.runner.specification.FeatureSpecification

@Feature("Provisioning KAI service")
class ProvisioningInternetAccessSpec extends FeatureSpecification {



    def "KAI service successful provisioning"() {

        given("<modem> is OFF") {
            data -> println data
//            println properties
        }
        and ("<modem> is removed by \$deviceManager"){

        }
        and ("account data is prepared in \$sbpDatabase") {

        }

    }
}