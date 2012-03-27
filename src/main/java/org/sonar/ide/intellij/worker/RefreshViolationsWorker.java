package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.RefreshViolationsListener;
import org.sonar.wsclient.services.Query;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshViolationsWorker extends RefreshSonarFileWorker<Violation> {

  private List<RefreshViolationsListener> listeners = new ArrayList<RefreshViolationsListener>();

  public RefreshViolationsWorker(Project project, VirtualFile virtualFile) {
    super(project, virtualFile);
  }

  public void addListener(RefreshViolationsListener listener) {
    listeners.add(listener);
  }

  @Override
  protected Query<Violation> getQuery(String resourceKey) {
    ViolationQuery violationQuery = ViolationQuery.createForResource(resourceKey);
    violationQuery.setDepth(-1);
    return violationQuery;
  }

  @Override
  protected void done() {
    try {
      List<Violation> violations = get();
      if (violations == null) {
        violations = new ArrayList<Violation>();
      }
      for (RefreshViolationsListener listener : this.listeners) {
        listener.doneRefreshViolations(this.virtualFile, violations);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
