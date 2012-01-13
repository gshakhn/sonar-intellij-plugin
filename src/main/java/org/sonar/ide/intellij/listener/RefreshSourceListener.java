package org.sonar.ide.intellij.listener;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Source;

public interface RefreshSourceListener {
  void doneRefreshSource(VirtualFile virtualFile, Source source);
}
