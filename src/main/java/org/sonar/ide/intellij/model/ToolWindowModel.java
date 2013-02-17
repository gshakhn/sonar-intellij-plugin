package org.sonar.ide.intellij.model;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.ide.intellij.utils.SonarCache;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import javax.swing.SwingUtilities;
import java.util.*;

public class ToolWindowModel {
  private Project project;
  private ViolationTableModel violationTableModel;
  private SonarTreeModel violationTreeModel;
  private SonarCache sonarCache;

  public ToolWindowModel(Project project, ViolationTableModel violationTableModel, SonarTreeModel violationTreeModel, SonarCache sonarCache) {
    this.project = project;
    this.violationTableModel = violationTableModel;
    this.violationTreeModel = violationTreeModel;
    this.sonarCache = sonarCache;
  }

  public ViolationTableModel getViolationTableModel() {
    return this.violationTableModel;
  }

  public void refreshViolationsTable(VirtualFile newFile) {
    sonarCache.loadViolations(newFile, new RefreshListener<Violation>() {
      @Override
      public void doneRefresh(final VirtualFile virtualFile, final List<Violation> violations) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (isFileCurrentlySelected(virtualFile)) {
              violationTableModel.setViolations(virtualFile, violations);
              Map<VirtualFile, List<Violation>> map = new HashMap<VirtualFile, List<Violation>>();
              map.put(virtualFile, violations);
              violationTreeModel.setViolations(map);
            }
          }
        });
      }
    });

    sonarCache.loadSource(newFile, new RefreshListener<Source>() {
      @Override
      public void doneRefresh(final VirtualFile virtualFile, final List<Source> source) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (isFileCurrentlySelected(virtualFile) && source != null && !source.isEmpty()) {
              violationTableModel.setSource(virtualFile, source.get(0));
            }
          }
        });
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
