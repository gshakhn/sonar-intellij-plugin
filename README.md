Introduction
-----------

This is an [IntelliJ IDEA] plugin for [Sonar]. There was a plugin called [SONAR IDE], but it's development for [IntelliJ IDEA] has been discontinued with IntelliJ 9. Therefore I decided to write [my own plugin] as a learning experience while using the existing plugin as inspiration.

[IntelliJ IDEA]: http://www.jetbrains.com/idea/
[Sonar]: http://www.sonarsource.org/
[SONAR IDE]: http://docs.codehaus.org/display/SONAR/IntelliJ+IDEA+Plugin
[my own plugin]: https://github.com/gshakhn/sonar-intellij-plugin

Download
--------
At the moment there is a precompiled ZIP for your convenience. You can simply download the ZIP and use the "Install from disk..." feature in IDEA.
The ZIP is available here: [https://github.com/gshakhn/sonar-intellij-plugin/tree/master/download](https://github.com/gshakhn/sonar-intellij-plugin/raw/master/download/sonar-intellij-plugin-1.0.zip)


Building the plugin
------------------

I am building the plugin locally using IntelliJ 12.0.2 (Build 123.100). To build it locally on your machine, modify idea.version and idea.build in pom.xml to match your local install. Unfortunately, I'm unable to find any versions of OpenAPI in Maven past 7.0.3. You'll have to install the various Intellij jars located in the lib folder of your IntelliJ install into your local Maven repository via:

    mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.intellij -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar

Alternatively, you can run the install-intellij-libs.sh script that [simonbrandhof] created.

    ./install-intellij-libs.sh <path to IntelliJ 12.0.2>


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


Changelog
---------

Version 1.0.1
- fixed IndexOutOfBoundsException in RefreshSourceWorker.done()

Version 1.0
- added support for [IntelliJ IDEA] 12 (Leda 12.0.2)
- added handling of proxy configuration

Version 0.1
- first release for [IntelliJ IDEA] 11.0.1


License
------------------

This plugin is licensed under [LGPL Version 3].

Feel free to fork and submit pull requests. I'll try to get them into the mainline ASAP.


This plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

In other words, this plugin won't break your computer as far as I know, but use at your own risk.

[LGPL Version 3]: http://www.gnu.org/licenses/lgpl-3.0.txt
