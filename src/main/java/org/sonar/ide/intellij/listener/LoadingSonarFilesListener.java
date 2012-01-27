package org.sonar.ide.intellij.listener;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public interface LoadingSonarFilesListener {
  void loadingFiles(List<VirtualFile> filesLoading);
}
