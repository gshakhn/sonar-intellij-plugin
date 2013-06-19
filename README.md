Introduction
-----------

This is the unofficial [IntelliJ IDEA] plugin for [Sonar]. There was a plugin called SONAR IDE, but it's development for [IntelliJ IDEA] has been discontinued with IntelliJ 9. Today there is an [Eclipse plugin called SonarQube](http://docs.codehaus.org/display/SONAR/Using+SonarQube+in+Eclipse) only.
So I decided to scratch an itch and write my own. Then a few people contributed to make it better. Isn't open source awesome?

[IntelliJ IDEA]: http://www.jetbrains.com/idea/
[Sonar]: http://www.sonarsource.org/


Download
--------

You can install the plugin through the official JetBrains repo. You can also manually download it [here].
There is also an [11.x compatible version] on the [11.x] branch.

[here]: http://plugins.jetbrains.com/plugin?pr=idea&pluginId=7168
[11.x compatible version]: https://github.com/gshakhn/sonar-intellij-plugin/blob/11.x/sonar-intellij-plugin-idea11.zip
[11.x]: https://github.com/gshakhn/sonar-intellij-plugin/tree/11.x

Building the plugin
------------------

To build the plugin on your machine you need to have at least a downloaded copy of IntelliJ 12.0.4 (Build 123.169).
The plugin depends on multiple jars of IntelliJ IDEA but as these are not available via Maven Central, you'll have to
install the various Intellij jars located in the lib folder of your IntelliJ install into your local Maven repository.

For your convienience we created a bash script which will do exactly this for you
```
    $ cd sonar-intellij-plugin/
    $ ./install-intellij-libs.sh 12.0.4 <path to IntelliJ 12.0.4>
```

To run the maven build you'll also need to install an updated version of [ideauidesigner-maven-plugin]. See that readme for installation instructions.
[ideauidesigner-maven-plugin]: https://github.com/gshakhn/ideauidesigner-maven-plugin/tree/12.x

After you install all the jars this plugin needs into your local repo, just run

    mvn package

The resulting zip file will be located in the target folder.


Using the plugin
------------------

**PREREQUISITES:**
The plugin expects you to have access to a running instance of [Sonar] where you already performed an analysis for the sources you have locally. If you haven't done so, yet, please consider reading the Sonar documentation on how to integrate Sonar analysis e.g. [with your Maven build](http://docs.codehaus.org/display/SONAR/Installing+and+Configuring+Maven) first.

1. Go to  File > Project Settings > Modules > Your Module > Sonar Configuration.
2. Enter your host and username/password if required.
3. Click the Load Projects button.
4. Select the Sonar project associated with the module and hit OK.
5. Open the Sonar tab at the bottom.
6. Navigate to a Java file that has violations.
7. Assuming everything is configured correctly, you should see the violations show up in the table.


Usability Notes
------------------
- The violation list and source code for a Java file are cached. If the violation is fixed and another Sonar analysis is complete, you'll have to clear the cache through the context menu. (Right click on any open file editor and go to the Sonar menu)
- There is very little error handling for a bad configuration.


Changelog
---------

Version 1.0.7
- Fix "NPE when starting Intellij 12" ([#32](https://github.com/gshakhn/sonar-intellij-plugin/issues/32))

Version 1.0.6
- Fix Open in Browser if host doesn't have http:// ([#23](https://github.com/gshakhn/sonar-intellij-plugin/issues/23) and [#26](https://github.com/gshakhn/sonar-intellij-plugin/pull/26))
- Add URL to plugin.xml so website shows up in JetBrains repository. ([#21](https://github.com/gshakhn/sonar-intellij-plugin/issues/21))
- Change text in Project Configuration UI so it's more intuitive. ([#24](https://github.com/gshakhn/sonar-intellij-plugin/issues/24))
- Fix "Running the build with IDEA 12.1 jars fails tests" ([#27](https://github.com/gshakhn/sonar-intellij-plugin/issues/27))
- Fix "NullPointerException if project isn't in Sonar" ([#30](https://github.com/gshakhn/sonar-intellij-plugin/issues/30))
- Fix "Code Formatting" ([#28](https://github.com/gshakhn/sonar-intellij-plugin/issues/28))
- Fix "ToolWindow icons should be 13x13 - warning on a console" ([#29](https://github.com/gshakhn/sonar-intellij-plugin/issues/29))
- Potentially fix deadlock issue in SonarCache.

Version 1.0.5
- Fix NPE if looking at file that isn't analyzed by sonar. ([#17](https://github.com/gshakhn/sonar-intellij-plugin/issues/17))

Version 1.0.4
- Add a quick fix that removes a violation.
  If you've fixed a violation locally, but haven't run an analysis yet, you can mark the violation fixed locally.
- Add menu item to clear cache. If cached violation data is stale, it can be refreshed now.

Version 1.0.3
- Add 'Open in browser' functionality ([#11](https://github.com/gshakhn/sonar-intellij-plugin/issues/11)). Thanks [ggili]!
- Cache violations for inspection purposes. Should hit the sonar server much less now.

Version 1.0.2
- Update build dependencies

Version 1.0.1
- Fix IndexOutOfBoundsException in RefreshSourceWorker.done() ([#9](https://github.com/gshakhn/sonar-intellij-plugin/issues/9))

Version 1.0
- Add support for [IntelliJ IDEA] 12 (Leda 12.0.2)  ([#7](https://github.com/gshakhn/sonar-intellij-plugin/issues/7))
- Add handling of proxy configuration

Version 0.1
- First release for [IntelliJ IDEA] 11.0.1

[ggili]: https://github.com/ggili


License
------------------

This plugin is licensed under [LGPL Version 3].

Feel free to fork and submit pull requests. I'll try to get them into the mainline ASAP.


This plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

In other words, this plugin won't break your computer as far as I know, but use at your own risk.

[LGPL Version 3]: http://www.gnu.org/licenses/lgpl-3.0.txt
