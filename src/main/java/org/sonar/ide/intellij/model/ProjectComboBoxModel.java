package org.sonar.ide.intellij.model;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProjectComboBoxModel extends DefaultComboBoxModel {
  private List<SonarProject> projects = new ArrayList<SonarProject>();

  public void refreshProjectList(List<SonarProject> projects) {
    this.projects = projects;

    removeAllElements();
    for (SonarProject project : projects) {
      addElement(project);
    }
  }

  public SonarProject findProjectByKey(String projectKey) {
    for (SonarProject project : projects) {
      if (project.getResource().getKey().equals(projectKey)) {
        return project;
      }
    }

    return null;
  }

}
