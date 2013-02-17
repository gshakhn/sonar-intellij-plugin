package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Query;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.SourceQuery;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshSourceWorker extends RefreshSonarFileWorker<Source> {

  public RefreshSourceWorker(Project project, VirtualFile virtualFile) {
    super(project, virtualFile);
  }

  @Override
  protected Query<Source> getQuery(String resourceKey) {
    return SourceQuery.create(resourceKey);
  }

  @Override
  protected void done() {
    try {
      List<Source> sources = get();
      notifyListeners(sources);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
