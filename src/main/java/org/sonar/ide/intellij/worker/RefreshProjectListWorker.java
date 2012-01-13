package org.sonar.ide.intellij.worker;

import org.sonar.ide.intellij.listener.RefreshProjectListListener;
import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshProjectListWorker extends SwingWorker<List<SonarProject>, Void> {

  private Sonar sonar;
  private List<RefreshProjectListListener> listeners = new ArrayList<RefreshProjectListListener>();

  public RefreshProjectListWorker(Sonar sonar) {
    this.sonar = sonar;
  }

  public void addListener(RefreshProjectListListener listener) {
    listeners.add(listener);
  }

  @Override
  protected List<SonarProject> doInBackground() throws Exception {
    ResourceQuery query = new ResourceQuery();
    query.setQualifiers("TRK,BRC");
    query.setDepth(1);

    List<Resource> resources = this.sonar.findAll(query);

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
      for (RefreshProjectListListener listener : this.listeners) {
        listener.doneRefreshProjects(projects);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
