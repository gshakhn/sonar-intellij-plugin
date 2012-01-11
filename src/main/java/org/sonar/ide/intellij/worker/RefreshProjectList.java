package org.sonar.ide.intellij.worker;

import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.ide.intellij.ui.SonarModuleConfiguration;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshProjectList extends SwingWorker<List<SonarProject>, Void> {

  SonarModuleConfiguration sonarModuleConfiguration;

  public RefreshProjectList(SonarModuleConfiguration sonarModuleConfiguration) {
    this.sonarModuleConfiguration = sonarModuleConfiguration;
  }

  @Override
  protected List<SonarProject> doInBackground() throws Exception {
    ResourceQuery query = new ResourceQuery();
    query.setQualifiers("TRK,BRC");
    query.setDepth(1);

    List<Resource> resources = this.sonarModuleConfiguration.getSonar().findAll(query);

    List<SonarProject> projects = new ArrayList<SonarProject>();

    for (Resource resource : resources) {
      SonarProject project = new SonarProject(resource);
      projects.add(project);
    }

    Collections.sort(projects, new Comparator<SonarProject>() {
      @Override
      public int compare(SonarProject o1, SonarProject o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });

    return projects;
  }

  @Override
  protected void done() {
    try {
      List<SonarProject> projects = get();
      this.sonarModuleConfiguration.finishRefreshProjects(projects);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
