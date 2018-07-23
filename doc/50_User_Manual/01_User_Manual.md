# Gherkin Runner User Manual

## 1. Dependency

To start using Gherkin Runner, add Groovy to your project and following GR dependency using jitpack.io:

Gradle: 

`testCompile "com.github.NetworkedAssets:gherkin-runner:[commit-hash-or-branch-name]"`

Maven: 

```xml
<dependency>
    <groupId>com.github.NetworkedAssets</groupId>
    <artifactId>gherkin-runner</artifactId>
    <version>[commit-hash-or-branch-name]</version>
    <scope>test</scope>
</dependency>
```

**Gherkin Runner is hosted on jitpack, so it is required to add it to your project's repositories**

## 2. Main test class

Gherkin Runner is based on JUnit4, so to start using it you need to create test class annotated with `@RunWith(GherkinRunner.class)`
When such class is started by any tests runner it looks for Gherkin files with extension .feature in classpath and runs
implementation of these features if found. 

The simplest main tests class looks as following: 

```groovy
@RunWith(GherkinRunner.class)
class TestsRunner {}
```

## 3. Implementing Gherkin specification

Implementation of features is written in Groovy.

### Elements implementation 

#### Feature 
Implementation of feature is class annotated with `@Feature("<feature name>")` and extending _FeatureSpecification_ class. 
In feature implementation class scenarios of the feature can be implemented. 

Example: `@Feature("Manipulating list elements")`

#### Scenario 
Scenario implementation is method in feature class with signature: `def "<scenario name>"()`. 
Inside scenario implementation steps of the scenario should be implemented.

Example: `def "Add element to list"()`

#### Step 
To implement scenario's step use one of methods: `given("<step name>")`, `when("<step name>")`, `then("<step name>")` or 
`and("<step name>")` defined in FeatureSpecification. All of these methods takes two arguments. First is string with 
text content of step and the second is Closure with code to be run in this step. 

Example: 
```
when("The element added to the list") {
    list.add("a")
}
```

### Full specification example implementation

BDD scenario:
```gherkin
Feature: Manipulating list elements

  Scenario: Add element to list
    Given The element is not in the list
    When The element added to the list
    Then The list conains the element
```

Implementation:
```groovy
@Feature("Manipulating list elements")
class ManipulatingListElementsSpec extends FeatureSpecification {
    def "Add element to list"() {
        def list = []

        given("The element is not in the list") {
            list.clear()
        }

        when("The element added to the list") {
            list.add("a")
        }

        then("The list conains the element") {
            assertThat(list).contains("a")
        }
    }
}
```

## 4. Callbacks

You can do something before/after feature/scenario using following annotation over method, that should be run in time 
defined by annotation name:

* `@BeforeFeature`
* `@AfterFeature`
* `@BeforeScenario`
* `@AfterScenario`

### Example

```groovy
@Feature("Manipulating list elements")
class ManipulatingListElementsSpec extends FeatureSpecification {
    @BeforeScenario
    def before() {
        // this method will be rune before every scenario
    }
    
    // ... scenario definitions
}
```

## 5. Configuration

Configuration is done using annotations, methods in main tests class.

You can configure:
* metadata for test suite, which is shown in reports using `metadata()` method, which returns object of `RunnerMetadata` 
* package, where runner should search implementation to improve performance using `@ImplementationsPackage` annotation (default: the same, as main tests class package)
* reports, which runner should generate using `@Reports` annotation built-in are `HPQC`, `HTML`, `JSON` (default: \['HTML'])
* extensions, which extends runner functions using `@Extensions` annotation. At the moment the only one possible extension is ReportExporter.

Example configuration:

```groovy
@RunWith(GherkinRunner.class)
@ImplementationsPackage("com.networkedassets.tests.specs")
@Extensions(MyHTMLReportExporter.class)
@Reports("MyHTML")
class TestsRunner {
    def suiteName = 'Some Automated Tests'

    def metadata() {
        return new RunnerMetadata(suiteName)
    }
}
```

## 6. Receive data table from Gherkin file

It is possible to receive data table defined in Gherkin file:

```gherkin
When The element added to the list
    |element|
    |abc    |
```

The data table is passed by GR as string 2D array to closure implementing step. You can receive it in following way:
```
when("The element added to the list") { data ->
    list.add(data[1][0])
}
```

## 6. Scenario outline

Gherkin Runner is able to run scenario outlines expressed in features file in following way:

```gherkin
Feature: New businesses

  Scenario Outline: Businesses should provide required data

    Given a restaurant <business> on <location>
    When <business> signs up to Mapper
    Then it <should?> be added to the platform
    And its name <should?> appear on the map at <location>

    Examples: Business name and location should be required
      | business         | location | should?   |
      | UNNAMED BUSINESS | NOWHERE  | shouldn't |

    Examples: Allow only businesses with correct names
      | business         | location                  | should?   |
      | Back to Black    | 8114 2nd Street, Stockton | should    |
      | UNNAMED BUSINESS | 8114 2nd Street, Stockton | shouldn't |
```

To implement it you have to use scenario and names of steps from outline, like this:
```
@Feature("New businesses")
class NewBusinessSpec extends FeatureSpecification {
    def "Businesses should provide required data"() {
        given("a restaurant <business> on <location>") {
            // getting some value from binding defined in feature file
            bindings.getString("business")
        }

        when("<business> signs up to Mapper") {
        }

        then("it <should?> be added to the platform") {
        }

        and("its name <should?> appear on the map at <location>") {
        }
    }
}
```

Gherkin runner will run the implementation as many times, as many is records in examples tables. Placeholders in scenario name and steps will be 
replaced with bindings values from table for current run. To get bounded values in test implementation `bindings` object should be used. It offers 
four methods for getting values represented as specified type: getString(), getBoolean(), getInt(), getDouble(). When there is no bindings with key
passed to one of these methods exception will be thrown. The exception will be also thrown, when it will be not possible to convert some value to
specified type. It is worth of notice, that getBoolean() will return false for values: "false", "n", "no" and ones, which end with "not" or "n't".
The rest of values will be converted to true.

## 6. Running only scenarios matching given tags condition

You can point Gherkin Runner, which scenarios it should run using tags. You can add tag to any feature, scenario or examples in following way:

```gherkin
@NewBusinessFeature
Feature: New businesses

  @SomeTag2
  Scenario Outline: Businesses should provide required data
    Given a restaurant <business> on <location>
    When <business> signs up to Mapper
    Then it <should?> be added to the platform
    And its name <should?> appear on the map at <location>
    
    @SomeOtherTag
    Examples: Business name and location should be required
      | business         | location | should?   |
      | UNNAMED BUSINESS | NOWHERE  | shouldn't |

    @AnotherTag
    Examples: Allow only businesses with correct names
      | business         | location                  | should?   |
      | Back to Black    | 8114 2nd Street, Stockton | should    |
      | UNNAMED BUSINESS | 8114 2nd Street, Stockton | shouldn't |
```

Tags are inherited in following direction feature > scenario > examples. It means, that if feature has @tag1, then all scenarios will also have @tag1.
You can select, which scenarios should be run using `gherkinTags` JVM property. Value assigned to this property should be correct logical expression,
in which tags and logical operators like OR, AND, NOT can be used. Example correct tags conditions:

*`@SomeOtherTag` - runs only scenarios with @SomeOtherTag
*`@SomeOtherTag OR @AnotherTag` - runs only scenarios with @SomeOtherTag or with @AnotherTag
*`@NewBusinessFeature AND NOT @AnotherTag` - runs only scenarios with @NewBusinessFeature and without @AnotherTag
*`@NewBusinessFeature AND NOT (@AnotherTag OR @SomeOtherTag)` - runs only scenarios with @NewBusinessFeature and without @AnotherTag and @SomeOtherTag
