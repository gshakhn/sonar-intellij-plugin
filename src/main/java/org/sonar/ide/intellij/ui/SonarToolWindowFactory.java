package org.sonar.ide.intellij.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.model.ToolWindowModel;
import org.sonar.ide.intellij.model.ViolationTableModel;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class SonarToolWindowFactory implements ToolWindowFactory {
  public SonarToolWindowFactory() {
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    final ViolationTableModel violationTableModel = new ViolationTableModel();

    JTable violationsTable = new JTable(violationTableModel);
    violationsTable.setFillsViewportHeight(true);

    violationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    violationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    violationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
        if (!listSelectionModel.isSelectionEmpty()) {
          Integer selectionIndex = listSelectionModel.getMinSelectionIndex();
          final Violation selectedViolation = violationTableModel.getViolation(selectionIndex);

          if (selectedViolation.getLine() != null) {
            DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
              @Override
              public void run(DataContext dataContext) {
                Project project = DataKeys.PROJECT.getData(dataContext);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, violationTableModel.getCurrentVirtualFile(), selectedViolation.getLine(), 0);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
              }
            });
          }
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(violationsTable);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 1;
    gridBagConstraints.gridheight = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
    panel.add(scrollPane, gridBagConstraints);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "", false);
    toolWindow.getContentManager().addContent(content);

    ToolWindowModel toolWindowModel = new ToolWindowModel(project, violationTableModel);
    SonarProjectComponent projectComponent = project.getComponent(SonarProjectComponent.class);
    projectComponent.setToolWindowModel(toolWindowModel);
  }
}
