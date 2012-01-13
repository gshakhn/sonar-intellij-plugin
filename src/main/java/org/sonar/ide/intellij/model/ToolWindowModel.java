package org.sonar.ide.intellij.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.RefreshSourceListener;
import org.sonar.ide.intellij.listener.RefreshViolationsListener;
import org.sonar.ide.intellij.worker.RefreshSourceWorker;
import org.sonar.ide.intellij.worker.RefreshViolationsWorker;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolWindowModel implements RefreshViolationsListener, RefreshSourceListener {
  private Project project;
  private ViolationTableModel violationTableModel;

  private Map<VirtualFile, List<Violation>> violationsCache = new HashMap<VirtualFile, List<Violation>>();
  private Map<VirtualFile, Source> sourceCache = new HashMap<VirtualFile, Source>();

  public ToolWindowModel(Project project, ViolationTableModel violationTableModel) {
    this.project = project;
    this.violationTableModel = violationTableModel;
  }

  public ViolationTableModel getViolationTableModel() {
    return this.violationTableModel;
  }

  public void refreshViolationsTable(VirtualFile newFile) {
    refreshViolations(newFile);
    refreshSource(newFile);
  }

  private void refreshViolations(VirtualFile newFile) {
    if (this.violationsCache.containsKey(newFile)) {
      doneRefreshViolations(newFile, this.violationsCache.get(newFile));
    } else {
      RefreshViolationsWorker refreshViolationsWorker = new RefreshViolationsWorker(this.project, newFile);
      refreshViolationsWorker.addListener(this);
      refreshViolationsWorker.execute();
    }
  }

  private void refreshSource(VirtualFile newFile) {
    if (this.sourceCache.containsKey(newFile)) {
      doneRefreshSource(newFile, this.sourceCache.get(newFile));
    } else {
      RefreshSourceWorker refreshSourceWorker = new RefreshSourceWorker(this.project, newFile);
      refreshSourceWorker.addListener(this);
      refreshSourceWorker.execute();
    }
  }

  @Override
  public void doneRefreshViolations(final VirtualFile virtualFile, final List<Violation> violations) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        violationsCache.put(virtualFile, violations);

        if (isFileCurrentlySelected(virtualFile)) {
          violationTableModel.setViolations(virtualFile, violations);
        }
      }
    });
  }

  @Override
  public void doneRefreshSource(final VirtualFile virtualFile, final Source source) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        sourceCache.put(virtualFile, source);

        if (isFileCurrentlySelected(virtualFile)) {
          violationTableModel.setSource(virtualFile, source);
        }
      }
    });
  }

  private boolean isFileCurrentlySelected(VirtualFile virtualFile) {
    VirtualFile[] selectedFiles = FileEditorManager.getInstance(this.project).getSelectedFiles();
    for (VirtualFile selectedFile : selectedFiles) {
      if (selectedFile.equals(virtualFile)) {
        return true;
      }
    }

    return false;
  }
}
