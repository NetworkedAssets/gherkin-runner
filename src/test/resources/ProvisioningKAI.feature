# Each feature should have very short description about it's basic goal in the 'Overview' section.
Feature: Provisioning KAI service
  Overview:
  Provisioning of KAI (Kable Internet) service booked by VF customer which allows to access the Internet via a Cable Modem.
  Cable Modem is then connected to the customer CPE (e.g. Computer).

  Background:
    Given Infrastructure
    # Infrastructure describes context of use of each SBP system component involved in the provisioning process.
    # Assumption is that Gherkin-Runner at this stage will only verify if each of them is up and running so the test can be executed on it.
    # Each component has to be of type predefined as Java class and have name declared.
    # In scenario it can be referenced using '$' sign and it will be treated in scenario as stateful object.
    # 'component', 'type' and 'description' are restricted keywords in table headers.
      | #component        | #type                  | #description                                                                     |
      | rdu               | RDU                    | Together with DPE it's being used to generate CPE device config.                 |
      | eventManager      | EventManager           | Handles events from CMTS about Cable Modem ON.                                   |
      | sbpDatabase       | Database               | Stores data about account and provisioning status.                               |
      | deviceManager     | DeviceManager          | CCB uses it to add contract data into SBP system.                                |
      | provAccountPoller | ProvisionAccountPoller | Pools for new provisioning requests queued from SI_QUEUE SBP-database table.     |
      | ccbPaula          | CCBPaula               | CRM system which stores data about customers.                                    |
      | sbpCoreapp        | SBPCoreapp             | Implements core pipeline for provisioning process.                               |
      | meps              | MEPS                   | Implements event-manager component.                                              |
      | clientCPE         | LinuxVM                | Machine connected directly to the provisioned modem - simulates user end-device. |

  Scenario Outline: KAI service successful provisioning <modem>
    Given <modem> is OFF
    And <modem> is removed by $deviceManager
    And <account> is prepared in $sbpDatabase
      | $sbpDatabase.accountType             | KUN                 |
      | $sbpDatabase.accountNr               | <account.accountNr> |
      | $sbpDatabase.accountHsiServiceCode   | S16                 |
      | $sbpDatabase.accountKaiBarringStatus | UNBARRED            |
    And self-install attributes are set for <modem> in $sbpDatabase
      | $sbpDatabase.Q_SI_QUEUE_state     | 0                   |
      | $sbpDatabase.Q_SI_QUEUE_accountNr | <account.accountNr> |
      | $sbpDatabase.Q_SI_QUEUE_operator  | AUTOTEST            |
    When <modem> is turned ON
    And Wait until ON
    And $clientCPE network interface is restarted
    Then status of <modem> in $sbpDatabase indicates that it is provisioned
      | $sbpDatabase.Q_ACCOUNT_QUEUE_state  | ready |
      | $sbpDatabase.Q_SI_QUEUE_state       | 1     |
      | $sbpDatabase.Q_PROV_ACK_QUEUE_state | ready |
    And $sbpDatabase.Q_ACCOUNT_DETAILS has no errors for provisioned <modem>
    And <clientCPE> reaches google.com

    # Example is an object which has type of predefined Java class.
    # In scenario it can be referenced by '<>' braces.
    # Each example (each table row) will be executed by Gherkin-Runner as separate test.
    # 'type' and 'description' are restricted keywords in table columns.
    Examples: Correct user accounts, contracts and mac addresses of existing modems
      | #modem | #account <Account> | #description          |
      | modem1 | account1           | Use Case for Homebox3 |
      | modem2 | account2           | Use Case for Homebox2 |
      | modem3 | account2           | Use Case for Homebox2 |
      | modem4 | account3           | Use Case for Homebox4 |
      | modem5 | account3           | Use Case for Homebox4 |
      | modem6 | account3           | Use Case for Homebox4 |