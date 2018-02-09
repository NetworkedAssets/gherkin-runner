# Architecture constraints

## Technical Constraints

### Written in Kotlin for usage in Groovy

Runner is implemented using Kotlin, but it will consume Groovy classes as features implementation, so it is important 
to do not use in the runner interface level Kotlin-specific syntax, which looks ugly, when used in Java or Groovy.

## Organizational Constraints

### Architecture documentation

The documentation should be structured like the arc42 template.

