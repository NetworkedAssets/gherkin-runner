
package com.networkedassets.gherkin.runner

import com.networkedassets.gherkin.runner.annotation.BeforeFeature
import com.networkedassets.gherkin.runner.annotation.Feature
import com.networkedassets.gherkin.runner.specification.FeatureSpecification

@Feature("Provisioning KAI service")
class ProvisioningInternetAccessSpec extends FeatureSpecification {


    @BeforeFeature
    def before() {

    }

    def "KAI service successful provisioning <modem>"() {

        given("<modem> is OFF") {
            println envBindings.bindings
        }
        and ("<modem> is removed by \$deviceManager"){

        }
        and ("account data is prepared in \$sbpDatabase") {

        }

    }
}