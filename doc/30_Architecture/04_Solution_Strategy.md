# Solution strategy

In order make specification runnable and provide output of tests GherkinRunner will: 
* look for Gherkin files in classpath
* iterate over found features, scenarios, steps and for each will: 
	* scan classpath in specified package using reflection to find implementation for it
	* run the implementation if found
	* put result in memory structure
* generate report in HTML, JSON
* additionally it will come with extensions system, which will let you extends functionality of runner e.g. add custom 
report exporter