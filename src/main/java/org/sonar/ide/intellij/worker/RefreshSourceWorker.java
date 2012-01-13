package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.RefreshSourceListener;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.SourceQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshSourceWorker extends RefreshSonarFileWorker<Source> {

  private List<RefreshSourceListener> listeners = new ArrayList<RefreshSourceListener>();

  public RefreshSourceWorker(Project project, VirtualFile virtualFile) {
    super(project, virtualFile);
  }
  
  public void addListener(RefreshSourceListener listener) {
    listeners.add(listener);
  }

  @Override
  protected Source doInBackground() throws Exception {
    String resourceKey = getResourceKey();
    Sonar sonar = getSonar();

    SourceQuery sourceQuery = SourceQuery.create(resourceKey);
    return sonar.find(sourceQuery);
  }

  @Override
  protected void done() {
    try {
      Source source = get();
      for (RefreshSourceListener listener : this.listeners) {
        listener.doneRefreshSource(this.virtualFile, source);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (ExecutionException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
