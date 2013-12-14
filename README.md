Introduction
-----------

**PLEASE NOTE: THIS PLUGIN HAS BEEN MOVED TO https://github.com/sonar-intellij-plugin/sonar-intellij-plugin. ANY FEATURE REQUESTS OR BUG REPORTS SHOULD GO THERE.**
Many thanks for your support!


Changelog
---------

Moved to https://github.com/sonar-intellij-plugin/sonar-intellij-plugin

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
