# IntelliJ IDEA plugin connecting to Teamserver REST API. #

### Prerequisites ###
* IntelliJ IDEA version 9.0 or later (either Community Edition or Ultimate) must be installed on your computer.
* Plugin DevKit plugin must be enabled in IDE. Go to IntelliJ settings => Plugins => search for Plugin DevKit and make sure it is enabled.
* IntelliJ Platform SDK must be configured for your IDEA project. Right click on project folder in IntelliJ => Open Module Settings => Project => Project SDK.
If necessary add a new "IntelliJ Platform Plugin SDK". It should point to the installation folder of IntelliJ.
* Project language level in project settings should be set to 8. Right click on project folder in IntelliJ => Open Module Settings => Project => Project language level.
More information on setting up development environment: http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html
* Gradle must be installed and added to "Path" system environment variable: https://gradle.org/install/

### Build from the command line ###
* Clone the project.
* Run <code>gradle buildPlugin</code> in project root directory.
* Plugin zip archive should be generated to project root folder/build/distributions.
* To install the generated zip archive of the plugin go to File => Settings => Plugins => Install Plugin from disk.

### Build from IDE ###
* Clone the project.
* Open the project with IntelliJ IDEA.
* In the "Event Log" notification, which should appear in the bottom right corner, click "Import Gradle project".
* Fill in the import dialog.
* Open Gradle tool window and run "buildPlugin" task, which is in the "intellij" task folder.
* Plugin zip archive should be generated to project root folder/build/distributions.
* To install the generated zip archive of the plugin go to File => Settings => Plugins => Install Plugin from disk.

### Run from the command line ###
* Run ``` gradle runIdea ``` from the command line within the project root folder.
* A separate instance of IntelliJ IDEA should be launched with the plugin installed.

### Run from the IDE ### 
* Build the project
* Open Gradle tool window and run "runIdea" task, which is inside the "intellij" task folder.
* A separate instance of IntelliJ IDEA should be launched with the plugin installed.

### Publishing the plugin ###
* http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html

### Releasing a new version of the plugin ###
Releases are done by the gradle-release plugin (https://github.com/researchgate/gradle-release). 

* Run ``` gradle release ``` command. 

By default, releases are only accepted from the master branch.
To accept any branch, add the following configuration to the build.gradle file: 

`````` 
 release {
    git {
        requireBranch = ''
    }
 }
 ```
 
 In case you need to release the plugin with uncommitted changes (for testing), add the following configuration:
 
 ``` 
  release {
    failOnCommitNeeded = false
  }
  ```


To release the plugin non-interactively: ``` gradle release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=1.0.0 -Prelease.newVersion=1.1.0-SNAPSHOT ```
The command needs the current release version and the new version parameters.