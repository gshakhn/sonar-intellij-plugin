package org.sonar.ide.intellij;

import org.sonar.wsclient.Sonar;

public interface SonarModuleComponent {
  State getState();

  class State {
    public String host;
    public String user;
    public String password;
    public String projectKey;
  }

  Sonar getSonar();
}
