package org.sonar.ide.intellij.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.EventListener;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.inspection.LastInspectionResult;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshRuleListener;
import org.sonar.ide.intellij.model.SonarTreeModel;
import org.sonar.ide.intellij.model.ToolWindowModel;
import org.sonar.ide.intellij.model.ViolatationChangedListener;
import org.sonar.ide.intellij.model.ViolationTableModel;
import org.sonar.ide.intellij.worker.RefreshRuleWorker;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SonarToolWindow implements LoadingSonarFilesListener, RefreshRuleListener {

    private final Icon INSPECT_ICON = IconLoader.getIcon("/icons/inspect.png");

    private ActionToolbar toolbar;
    private final ToggleAction localAnalysisButton = new ShowLocalAnalysisAction();

    private JXBusyLabel loadingLabel;
    private FileNamesToolTipBuilder fileNamesToolTipBuilder = new FileNamesToolTipBuilder();
    private final ViolationCellRenderer cellRenderer = new ViolationCellRenderer();
    private volatile Map<String, String> rulesToolTipMap = null;
    private final Project project;
    private final ToolWindow toolWindow;
    private boolean refreshingInProgress = false;

    public static SonarToolWindow createSonarToolWindow(Project project, ToolWindow toolWindow) {
        return new SonarToolWindow(project, toolWindow);
    }

    private SonarToolWindow(final Project project, final ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        this.project = project;

        final ViolationTableModel violationTableModel = new ViolationTableModel();

        final JTabbedPane tabbedPane = new JTabbedPane();
        final JXTable violationsTable = new JXTable(violationTableModel);
        final SonarTreeModel treeModel = new SonarTreeModel(true);
        final JXTree violationsTree = new JXTree(treeModel);
        violationsTree.setShowsRootHandles(true);
        final AnAction displayDescriptionAction = new DisplayDescriptionAction(violationsTree);

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
        downloadRules();
        violationsTree.getSelectionModel().addTreeSelectionListener(mySelectionListener);
        violationsTree.setEditable(false);
        violationsTree.setCellRenderer(cellRenderer);
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
        localViolationsTree.setCellRenderer(cellRenderer);
        localViolationsTree.setRootVisible(false);
        final JScrollPane scrollPane = new JScrollPane(localViolationsTree);
        JScrollPane scrollPaneTree = new JScrollPane(violationsTree);
        final SonarConsolePanel console = new SonarConsolePanel(project);
        SimpleToolWindowPanel simpleToolWindowPanel = new SimpleToolWindowPanel(false, true);

        toolbar = createToolbar();
        simpleToolWindowPanel.setToolbar(toolbar.getComponent());
        simpleToolWindowPanel.setContent(scrollPane);
        tabbedPane.addTab("Current file", simpleToolWindowPanel);
        tabbedPane.addTab("Last inspection", scrollPaneTree);
        tabbedPane.addTab("Console", console.getMainPanel());

        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_STARTED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                tabbedPane.setSelectedIndex(2);
            }
        });

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
        group.add(displayDescriptionAction);
        panel.add(this.loadingLabel, makeGridBagConstraintForToolbarActions(0));


        final SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);

        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                toolbar.updateActionsImmediately();

                refreshCurrentInspectionPanel();
            }

            private void refreshCurrentInspectionPanel() {
                if (violationTableModel.getCurrentVirtualFile() != null) {
                    sonarProjectComponent.getToolWindowModel().refreshViolationsTable(violationTableModel.getCurrentVirtualFile());
                }
            }
        });

        EventBus.subscribe(EventKind.REMOTE_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                toolbar.updateActionsImmediately();
                sonarProjectComponent.getToolWindowModel().refreshViolationsTable(violationTableModel.getCurrentVirtualFile());
            }
        });


        PopupHandler.installPopupHandler(violationsTree, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());
        final DefaultActionGroup tableTreeChoiceGroup = new DefaultActionGroup();
        final DefaultActionGroup localTreeActionGroup = new DefaultActionGroup();
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

            @Override
            public void update(AnActionEvent e) {
                super.update(e);
            }
        };
        tableTreeChoice.getTemplatePresentation().setText("Tree view");
        tableTreeChoiceGroup.add(tableTreeChoice);
        localTreeActionGroup.add(tableTreeChoice);
        AnAction localDisplayDescriptionAction = new DisplayDescriptionAction(localViolationsTree);
        localTreeActionGroup.add(localDisplayDescriptionAction);
        PopupHandler.installPopupHandler(localViolationsTree, localTreeActionGroup, ActionPlaces.UNKNOWN, ActionManager.getInstance());
        PopupHandler.installPopupHandler(violationsTable, tableTreeChoiceGroup, ActionPlaces.UNKNOWN, ActionManager.getInstance());


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

        SonarProjectComponent projectComponent = project.getComponent(SonarProjectComponent.class);
        ToolWindowModel toolWindowModel = new ToolWindowModel(project, violationTableModel, localTreeModel, projectComponent.getSonarAnalysis());
        projectComponent.setToolWindowModel(toolWindowModel);
        LastInspectionResult.getInstance().addListener(new ViolatationChangedListener() {
            @Override
            public void violationChanged(Map<VirtualFile, List<Violation>> violations) {
                treeModel.setViolations(violations);
            }
        });
        violationsTree.addMouseListener(new TreeMousePressedListener(violationsTree, displayDescriptionAction));
        localViolationsTree.addMouseListener(new TreeMousePressedListener(localViolationsTree, localDisplayDescriptionAction));


        projectComponent.getSonarAnalysis().addLoadingFileListener(this);
    }

    private GridBagConstraints makeGridBagConstraintForToolbarActions(int x) {
        GridBagConstraints gridBagConstraintsLoading = new GridBagConstraints();
        gridBagConstraintsLoading.gridx = x;
        gridBagConstraintsLoading.gridy = 0;
        gridBagConstraintsLoading.gridwidth = 1;
        gridBagConstraintsLoading.gridheight = 1;
        gridBagConstraintsLoading.fill = GridBagConstraints.BOTH;
        gridBagConstraintsLoading.anchor = GridBagConstraints.CENTER;
        gridBagConstraintsLoading.weightx = 0;
        gridBagConstraintsLoading.weighty = 0;
        return gridBagConstraintsLoading;
    }

    private void downloadRules() {
        if (refreshingInProgress || (rulesToolTipMap != null && rulesToolTipMap.size() != 0))
            return;
        RefreshRuleWorker refreshRuleWorker = new RefreshRuleWorker(project);
        refreshRuleWorker.addListener(this);
        refreshingInProgress = true;
        refreshRuleWorker.execute();

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

    @Override
    public void doneRefreshRules(List<Rule> rules) {
        Map<String, String> map = new HashMap<String, String>();
        for (Rule rule : rules)
            map.put(rule.getKey(), rule.getDescription());
        rulesToolTipMap = map;
        refreshingInProgress = false;
    }

    private class DisplayDescriptionAction extends AnAction {
        private final JXTree tree;

        public DisplayDescriptionAction(JXTree tree) {
            super("Display description");
            this.tree = tree;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            TreePath selectionPath = tree.getSelectionModel().getSelectionPath();
            if (selectionPath != null) {
                Object selection = selectionPath.getLastPathComponent();
                if (selection instanceof SonarTreeModel.RuleLabel) {
                    if (rulesToolTipMap != null && rulesToolTipMap.size() != 0) {
                        final SonarTreeModel.RuleLabel ruleLabel = (SonarTreeModel.RuleLabel) selection;
                        String toolTipText = rulesToolTipMap.get(ruleLabel.getRuleKey());
                        if (toolTipText != null) {
                            Balloon popup = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(toolTipText, MessageType.INFO, null)
                                    .createBalloon();
                            JComponent component = toolWindow.getComponent();
                            popup.show(new RelativePoint(component, new Point()), Balloon.Position.above);
                            return;
                        }
                    }
                    Balloon popup = JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder("<p>Can't show description</p><p>Check your sonar project configuration</p>", MessageType.WARNING, null)
                            .createBalloon();
                    JComponent component = toolWindow.getComponent();
                    popup.show(new RelativePoint(component, new Point()), Balloon.Position.above);
                    downloadRules();
                }
            }
        }


    }


    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(localAnalysisButton);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
    }

    class ShowLocalAnalysisAction extends ToggleAction {

        private boolean state = false;

        ShowLocalAnalysisAction() {
            super("Show local analysis", "Toggle to switch to local analysis", INSPECT_ICON);
        }

        @Override
        public boolean isSelected(AnActionEvent anActionEvent) {
            return state;
        }

        @Override
        public void update(AnActionEvent e) {

            final SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
            boolean localAnalysisAvailable = sonarProjectComponent.isLocalAnalysisAvailable();
            e.getPresentation().setEnabled(localAnalysisAvailable);
            state = sonarProjectComponent.getSonarAnalysis().isLocalAnalysis();
            super.update(e);
        }

        @Override
        public void setSelected(AnActionEvent anActionEvent, boolean b) {
            final SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
            if (b) {
                sonarProjectComponent.switchToExistingLocalAnalysis();
            } else {
                sonarProjectComponent.switchToRemoteAnalysis();
            }
        }
    }
}
