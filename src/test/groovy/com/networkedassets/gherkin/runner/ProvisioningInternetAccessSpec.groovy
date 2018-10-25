
package com.networkedassets.gherkin.runner


import com.networkedassets.gherkin.runner.annotation.Feature
import com.networkedassets.gherkin.runner.specification.FeatureSpecification

@Feature("Provisioning KAI service")
class ProvisioningInternetAccessSpec extends FeatureSpecification {




    def "KAI service successful provisioning <modem>"() {

        given("<modem> is OFF") {
            println bindings.bindings
        }
        and ("<modem> is removed by \$deviceManager"){
//            getDeviceManager(envBindings.get("Device Manager")).remove(bindings.get("modem"))

        }
        and ("account data is prepared in \$sbpDatabase") {

        }

    }
}