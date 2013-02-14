package org.sonar.ide.intellij.listener;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.SonarProjectComponent;

public class SonarFileEditorManagerListener implements FileEditorManagerListener {

  private SonarProjectComponent sonarProjectComponent;
  public SonarFileEditorManagerListener(SonarProjectComponent sonarProjectComponent) {
    this.sonarProjectComponent = sonarProjectComponent;
  }

  @Override
  public void fileOpened(FileEditorManager source, VirtualFile file) {
  }

  @Override
  public void fileClosed(FileEditorManager source, VirtualFile file) {
  }

  @Override
  public void selectionChanged(FileEditorManagerEvent event) {
    if (sonarProjectComponent != null && sonarProjectComponent.getToolWindowModel() != null && event.getNewFile() != null) {
      sonarProjectComponent.getToolWindowModel().refreshViolationsTable(event.getNewFile());
    }
  }
}
