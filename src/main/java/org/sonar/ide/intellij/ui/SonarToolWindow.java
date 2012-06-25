package org.sonar.ide.intellij.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.model.*;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Map;

public final class SonarToolWindow implements LoadingSonarFilesListener {

  private JXBusyLabel loadingLabel;
  private FileNamesToolTipBuilder fileNamesToolTipBuilder = new FileNamesToolTipBuilder();

  public static SonarToolWindow createSonarToolWindow(Project project, ToolWindow toolWindow) {
    return new SonarToolWindow(project, toolWindow);
  }

  private SonarToolWindow(final Project project, ToolWindow toolWindow) {

    final ViolationTableModel violationTableModel = new ViolationTableModel();

    final JTabbedPane tabbedPane = new JTabbedPane();
    final JXTable violationsTable = new JXTable(violationTableModel);
    final SonarTreeModel treeModel = new SonarTreeModel(true);
    final JXTree violationsTree = new JXTree(treeModel);
    violationsTree.setShowsRootHandles(true);
    TreeSelectionListener mySelectionListener = new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        if (e.getNewLeadSelectionPath() == null)
          return;
        Object selection = e.getNewLeadSelectionPath().getLastPathComponent();
        if (selection instanceof SonarTreeModel.ViolationLabel) {
          final SonarTreeModel.ViolationLabel violationLabel = (SonarTreeModel.ViolationLabel) selection;
          if (violationLabel.getLine() != null) {
            DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
              @Override
              public void run(DataContext dataContext) {
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, violationLabel.getVirtualFile(), violationLabel.getLine() - 1, 0);
                descriptor.navigate(false);
              }
            });
          }
        }
      }
    };
    violationsTree.getSelectionModel().addTreeSelectionListener(mySelectionListener);
    violationsTree.setEditable(false);
    violationsTree.setCellRenderer(new ViolationCellRenderer());
    violationsTree.setRootVisible(false);
    if (LastInspectionResult.getInstance().getViolations() != null)
      treeModel.setViolations(LastInspectionResult.getInstance().getViolations());
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

    SonarTreeModel localTreeModel = new SonarTreeModel(false);
    final JXTree localViolationsTree = new JXTree(localTreeModel);
    localViolationsTree.setShowsRootHandles(true);
    localViolationsTree.getSelectionModel().addTreeSelectionListener(mySelectionListener);
    localViolationsTree.setCellRenderer(new ViolationCellRenderer());
    localViolationsTree.setRootVisible(false);
    final JScrollPane scrollPane = new JScrollPane(localViolationsTree);
    JScrollPane scrollPaneTree = new JScrollPane(violationsTree);
    tabbedPane.addTab("Current file", scrollPane);
    tabbedPane.addTab("Last inspection", scrollPaneTree);
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
    panel.add(tabbedPane, gridBagConstraintsScrollPane);

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
    final DefaultActionGroup group = new DefaultActionGroup();
    ToggleAction groupFilesAction = new ToggleAction() {
      private boolean state = true;

      @Override
      public boolean isSelected(AnActionEvent e) {
        return state;
      }

      @Override
      public void setSelected(AnActionEvent e, boolean state) {
        this.state = state;
        treeModel.setGroupFiles(state);
      }
    };
    groupFilesAction.getTemplatePresentation().setText("Group files");
    group.add(groupFilesAction);
    panel.add(this.loadingLabel, gridBagConstraintsLoading);
    PopupHandler.installPopupHandler(violationsTree, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());

    final DefaultActionGroup tableTreeChoiceGroup = new DefaultActionGroup();
    ToggleAction tableTreeChoice = new ToggleAction() {
      private boolean state = true;

      @Override
      public boolean isSelected(AnActionEvent e) {
        return state;
      }

      @Override
      public void setSelected(AnActionEvent e, boolean state) {
        this.state = state;
        scrollPane.setViewportView(state ? localViolationsTree : violationsTable);
      }
    };
    tableTreeChoice.getTemplatePresentation().setText("Tree view");
    tableTreeChoiceGroup.add(tableTreeChoice);
    PopupHandler.installPopupHandler(localViolationsTree, tableTreeChoiceGroup, ActionPlaces.UNKNOWN, ActionManager.getInstance());
    PopupHandler.installPopupHandler(violationsTable, tableTreeChoiceGroup, ActionPlaces.UNKNOWN, ActionManager.getInstance());


    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "", false);
    toolWindow.getContentManager().addContent(content);

    ToolWindowModel toolWindowModel = new ToolWindowModel(project, violationTableModel, localTreeModel);
    toolWindowModel.addListener(this);
    SonarProjectComponent projectComponent = project.getComponent(SonarProjectComponent.class);
    projectComponent.setToolWindowModel(toolWindowModel);
    LastInspectionResult.getInstance().addListener(new ViolatationChangedListener() {
      @Override
      public void violationChanged(Map<VirtualFile, List<Violation>> violations) {
        treeModel.setViolations(violations);
      }
    });


  }

  @Override
  public void loadingFiles(final List<VirtualFile> filesLoading) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        loadingLabel.setBusy(!filesLoading.isEmpty());
        loadingLabel.setToolTipText(fileNamesToolTipBuilder.generateToolTip(filesLoading));
      }
    });
  }

}
