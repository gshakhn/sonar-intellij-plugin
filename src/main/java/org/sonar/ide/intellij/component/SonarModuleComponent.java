package org.sonar.ide.intellij.component;

import org.sonar.wsclient.Sonar;

public interface SonarModuleComponent {
  SonarModuleState getState();

  class SonarModuleState {
    public String host;
    public String user;
    public String password;
    public String projectKey;
  }

  Sonar getSonar();
}
