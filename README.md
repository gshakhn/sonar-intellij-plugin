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

Alternatively, you can run the install-intellij-libs.sh script that [simonbrandhof] created.

    ./install-intellij-libs.sh <path to IntelliJ 11.0.1>


After you install all the jars this plugin needs into your local repo, just run

    mvn package

The resulting zip file will be located in the target folder.

[simonbrandhof]: https://github.com/simonbrandhof


Using the plugin
------------------

1. Go to  File > Project Settings > Modules > Your Module > Sonar Configuration.
2. Enter your host and username/password if required.
3. Click the Load Projects button.
4. Select the Sonar project associated with the module and hit OK.
5. Open the Sonar tab at the bottom.
6. Navigate to a Java file that has violations.
7. Assuming everything is configured correctly, you should see the violations show up in the table.


Usability Notes
------------------
- The violation list and source code for a Java file are cached. If the violation is fixed and another Sonar analysis is complete, IntelliJ will have to be restarted in order for the violation to disappear in IntelliJ.
- There is very little error handling for a bad configuration.

License
------------------

This plugin is licensed under [LGPL Version 3].

Feel free to fork and submit pull requests. I'll try to get them into the mainline ASAP.


This plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

In other words, this plugin won't break your computer as far as I know, but use at your own risk.

[LGPL Version 3]: http://www.gnu.org/licenses/lgpl-3.0.txt