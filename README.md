Introduction
-----------

This is an [IntelliJ] plugin for [Sonar]. There was an [existing plugin], but it doesn't work in the latest versions of IntelliJ. I decided to write my own plugin as a learning experience while using the existing plugin as inspiration.

[IntelliJ]: http://www.jetbrains.com/idea/
[Sonar]: http://www.sonarsource.org/
[existing plugin]: http://docs.codehaus.org/display/SONAR/IntelliJ+IDEA+Plugin


Building the plugin
------------------

I am building the plugin locally using IntelliJ 11.0.1 (Build 111.67). To build it locally on your machine, modify idea.version and idea.build in pom.xml to match your local install. Unfortunately, I'm unable to find any versions of OpenAPI in Maven past 7.0.3. You'll have to install the various Intellij jars located in the lib folder of your IntelliJ install into your local Maven repository via:

    mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.intellij -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar

After you install all the jars this plugin needs into your local repo, just run

    mvn package

The resulting zip file will be located in the target folder.