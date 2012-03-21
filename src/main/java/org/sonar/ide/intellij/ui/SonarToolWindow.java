package org.sonar.ide.intellij.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.model.ToolWindowModel;
import org.sonar.ide.intellij.model.ViolationTableModel;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public final class SonarToolWindow implements LoadingSonarFilesListener {

  private JXBusyLabel loadingLabel;
  private FileNamesToolTipBuilder fileNamesToolTipBuilder = new FileNamesToolTipBuilder();

  public static SonarToolWindow createSonarToolWindow(Project project, ToolWindow toolWindow) {
    return new SonarToolWindow(project, toolWindow);
  }

  private SonarToolWindow(final Project project, ToolWindow toolWindow) {
    final ViolationTableModel violationTableModel = new ViolationTableModel();

    final JXTable violationsTable = new JXTable(violationTableModel);
    violationsTable.addHighlighter(new ToolTipHighlighter(HighlightPredicate.IS_TEXT_TRUNCATED));
    violationsTable.setFillsViewportHeight(true);

    violationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    violationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    violationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
        if (!listSelectionModel.isSelectionEmpty()) {
          Integer selectionIndex = listSelectionModel.getMinSelectionIndex();
          final Violation violation = violationTableModel.getViolation(violationsTable.convertRowIndexToModel(selectionIndex));

          if (violation.getLine() != null) {
            DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
              @Override
              public void run(DataContext dataContext) {
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, violationTableModel.getCurrentVirtualFile(), violation.getLine() - 1, 0);
                descriptor.navigate(false);
              }
            });
          }
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(violationsTable);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gridBagConstraintsScrollPane = new GridBagConstraints();
    gridBagConstraintsScrollPane.gridx = 0;
    gridBagConstraintsScrollPane.gridy = 1;
    gridBagConstraintsScrollPane.gridwidth = 10;
    gridBagConstraintsScrollPane.gridheight = 10;
    gridBagConstraintsScrollPane.fill = GridBagConstraints.BOTH;
    gridBagConstraintsScrollPane.anchor = GridBagConstraints.CENTER;
    gridBagConstraintsScrollPane.weightx = 1;
    gridBagConstraintsScrollPane.weighty = 10;
    panel.add(scrollPane, gridBagConstraintsScrollPane);

    GridBagConstraints gridBagConstraintsLoading = new GridBagConstraints();
    gridBagConstraintsLoading.gridx = 0;
    gridBagConstraintsLoading.gridy = 0;
    gridBagConstraintsLoading.gridwidth = 1;
    gridBagConstraintsLoading.gridheight = 1;
    gridBagConstraintsLoading.fill = GridBagConstraints.BOTH;
    gridBagConstraintsLoading.anchor = GridBagConstraints.CENTER;
    gridBagConstraintsLoading.weightx = 0;
    gridBagConstraintsLoading.weighty = 0;
    this.loadingLabel = new JXBusyLabel();
    panel.add(this.loadingLabel, gridBagConstraintsLoading);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "", false);
    toolWindow.getContentManager().addContent(content);

    ToolWindowModel toolWindowModel = new ToolWindowModel(project, violationTableModel);
    toolWindowModel.addListener(this);
    SonarProjectComponent projectComponent = project.getComponent(SonarProjectComponent.class);
    projectComponent.setToolWindowModel(toolWindowModel);
  }

  @Override
  public void loadingFiles(final List<VirtualFile> filesLoading) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        loadingLabel.setBusy(!filesLoading.isEmpty());
        loadingLabel.setToolTipText(fileNamesToolTipBuilder.generateToolTip(filesLoading));
      }
    });
  }

}
