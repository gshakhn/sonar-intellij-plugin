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
import org.sonar.ide.intellij.model.ViolationTableModel;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SonarToolWindowFactory implements ToolWindowFactory {

    private JPanel myToolWindowContent;
    private JTable violationsTable;
    private JButton refreshViolations;
    private ViolationTableModel violationTableModel;

    public SonarToolWindowFactory() {
        refreshViolations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SonarToolWindowFactory.this.refreshViolationList();
            }
        });
        
        violationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
                if (!listSelectionModel.isSelectionEmpty()) {
                    Integer selectionIndex = listSelectionModel.getMinSelectionIndex();
                    final Violation selectedViolation = violationTableModel.getViolation(selectionIndex);
                    
                    DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
                        @Override
                        public void run(DataContext dataContext) {
                            Project project = DataKeys.PROJECT.getData(dataContext);
                            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, violationTableModel.getVirtualFile(), selectedViolation.getLine(), 0);
                            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);

        violationTableModel = new ViolationTableModel();
        violationsTable.setModel(violationTableModel);
        violationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void refreshViolationList() {
        violationTableModel.refreshViolations();
    }
}
