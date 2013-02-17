package org.sonar.ide.intellij.listener;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Source;

import java.util.List;

public interface RefreshListener<T> {
  void doneRefresh(VirtualFile virtualFile, List<T> t);
}
