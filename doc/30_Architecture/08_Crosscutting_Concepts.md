# Crosscutting concepts

## Domain Model

Refer to the code for the domain model implemented.

## Configurability

Gherkin Runner is configurable using annotations over the class with main JUnit4 tests class annotated with `@RunWith(GherkinRunner.class)`.

For details regarding configuration read chapter **User Manual**

## Logging

Gherkin Runner uses the SLF4J API for logging purposes. 
For Kotlin extensions to SLF4J, kotlin-logging library is used. 

## Testing

Code of the project is not yet covered by any tests, but it is planned to implement some unit tests implemented using 
JUnit 4 and AssertJ.

## Exception Handling

All errors and warnings are logged.

## Build Management

Gherkin Runner uses Gradle for building the artifacts.