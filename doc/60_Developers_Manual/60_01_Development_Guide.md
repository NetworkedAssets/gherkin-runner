# Development guide

Gherkin Runner is written in [Kotlin](https://kotlinlang.org/) language.

## Setting Project up

* Install JDK 8
* Checkout project in your favourite IDE
* Run `./gradlew build` for Linux or `gradlew.bat build` for
  Windows to verify whether project builds

#### Compile time

Compile time dependencies are managed by Gradle.

## Development rules

### General

* All branches other than master should be named `feature/{ticket
  id}{descriptive name}` or `feature/{descriptive name}` if there is no
  ticket
* If a pull request cannot be merged to master due to merge conflicts,
  these conflicts should be resolved by pull request issuer by merging
  master into the branch being pulled.
