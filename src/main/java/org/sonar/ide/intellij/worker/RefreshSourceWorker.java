package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.RefreshSourceListener;
import org.sonar.wsclient.services.Query;
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
  protected Query<Source> getQuery(String resourceKey) {
    return SourceQuery.create(resourceKey);
  }

  @Override
  protected void done() {
    try {
      List<Source> sources = get();
      Source source = (sources == null || sources.isEmpty()) ? null : sources.get(0);
      for (RefreshSourceListener listener : this.listeners) {
        listener.doneRefreshSource(this.virtualFile, source);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
