Introduction
-----------

This is an [IntelliJ IDEA] plugin for [Sonar]. There was a plugin called [SONAR IDE], but it's development for [IntelliJ IDEA] has been discontinued with IntelliJ 9. Therefore I decided to write [my own plugin] as a learning experience while using the existing plugin as inspiration.

[IntelliJ IDEA]: http://www.jetbrains.com/idea/
[Sonar]: http://www.sonarsource.org/
[SONAR IDE]: http://docs.codehaus.org/display/SONAR/IntelliJ+IDEA+Plugin
[my own plugin]: https://github.com/gshakhn/sonar-intellij-plugin

Download
--------

You can install the plugin through the official JetBrains repo. You can also manually download it [here].

[here]: http://plugins.jetbrains.com/plugin?pr=idea&pluginId=7168

Building the plugin
------------------

I am building the plugin locally using IntelliJ 12.0.4 (Build 123.169). To build it locally on your machine, modify idea.version and idea.build in pom.xml to match your local install. Unfortunately, I'm unable to find any versions of OpenAPI in Maven past 7.0.3. You'll have to install the various Intellij jars located in the lib folder of your IntelliJ install into your local Maven repository via:

    mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.intellij -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar

Alternatively, you can run the install-intellij-libs.sh script that [simonbrandhof] created.

    ./install-intellij-libs.sh <path to IntelliJ 12.0.4>

You'll also need to install a custom version of [ideauidesigner-maven-plugin]. See that readme for installation instructions.

After you install all the jars this plugin needs into your local repo, just run

    mvn package

The resulting zip file will be located in the target folder.

[simonbrandhof]: https://github.com/simonbrandhof
[ideauidesigner-maven-plugin]: https://github.com/gshakhn/ideauidesigner-maven-plugin

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

Version 1.0.5
- Fix NPE if looking at file that isn't analyzed by sonar. (see [#17](https://github.com/gshakhn/sonar-intellij-plugin/issues/17))

Version 1.0.4
- Add a quick fix that removes a violation.
  If you've fixed a violation locally, but haven't run an analysis yet, you can mark the violation fixed locally.
- Add menu item to clear cache. If cached violation data is stale, it can be refreshed now.

Version 1.0.3
- Add 'Open in browser' functionality (see [#11](https://github.com/gshakhn/sonar-intellij-plugin/issues/11)). Thanks [ggili]!
- Cache violations for inspection purposes. Should hit the sonar server much less now.

Version 1.0.2
- Update build dependencies

Version 1.0.1
- fixed IndexOutOfBoundsException in RefreshSourceWorker.done() (see [#9](https://github.com/gshakhn/sonar-intellij-plugin/issues/9))

Version 1.0
- added support for [IntelliJ IDEA] 12 (Leda 12.0.2)  (see [#7](https://github.com/gshakhn/sonar-intellij-plugin/issues/7))
- added handling of proxy configuration

Version 0.1
- first release for [IntelliJ IDEA] 11.0.1

[ggili]: https://github.com/ggili


License
------------------

This plugin is licensed under [LGPL Version 3].

Feel free to fork and submit pull requests. I'll try to get them into the mainline ASAP.


This plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

In other words, this plugin won't break your computer as far as I know, but use at your own risk.

[LGPL Version 3]: http://www.gnu.org/licenses/lgpl-3.0.txt
