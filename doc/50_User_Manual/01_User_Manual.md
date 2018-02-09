# Gherkin Runner User Manual

## 1. Dependency

To start using Gherkin Runner add Groovy to your project and following GR dependency:

Gradle: 

`testCompile "org.na:gherkin-runner:[gr-version]"`

Maven: 

```xml
<dependency>
    <groupId>org.na</groupId>
    <artifactId>gherkin-runner</artifactId>
    <version>[gr-version]</version>
    <scope>test</scope>
</dependency>
```

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
@ImplementationsPackage("org.na.tests.specs")
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
