package org.sonar.ide.intellij.listener;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Violation;

import java.util.List;

public interface RefreshViolationsListener {
  void doneRefreshViolations(VirtualFile virtualFile, List<Violation> violations);
}
