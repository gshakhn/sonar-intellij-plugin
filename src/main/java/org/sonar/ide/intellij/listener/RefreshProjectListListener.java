package org.sonar.ide.intellij.listener;

import org.sonar.ide.intellij.model.SonarProject;

import java.util.List;

public interface RefreshProjectListListener {
  void doneRefreshProjects(List<SonarProject> newProjectList);
}
