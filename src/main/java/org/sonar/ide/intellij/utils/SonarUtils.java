package org.sonar.ide.intellij.utils;

public class SonarUtils {
  public static String fixHostName(String hostName) {
    String host = hostName;
    if (host.indexOf("://") == -1)
      host = "http://" + host;
    if (host.charAt(host.length()-1) == '/')
      host = host.substring(0, host.length()-1);
    return host;
  }
}
