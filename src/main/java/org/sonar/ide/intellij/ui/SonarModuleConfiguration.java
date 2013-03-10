package org.sonar.ide.intellij.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jetbrains.annotations.Nls;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.listener.RefreshProjectListListener;
import org.sonar.ide.intellij.model.ProjectComboBoxModel;
import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.ide.intellij.utils.SonarUtils;
import org.sonar.ide.intellij.worker.RefreshProjectListWorker;
import org.sonar.wsclient.Sonar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SonarModuleConfiguration extends BaseConfigurable implements RefreshProjectListListener {
    private JLabel lblHost;
    private JTextField txtHost;
    private JButton btnRefreshProjects;
    private JTextField txtUser;
    private JLabel lblUser;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblUseProxy;
    private JCheckBox useProxyBox;
    private JLabel lblProject;
    private JComboBox cmbProject;
    private JPanel pnlMain;
    private JXBusyLabel lblRefreshingProjects;

    private ProjectComboBoxModel projectComboBoxModel;

    private SonarModuleComponent sonarModuleComponent;

    public SonarModuleConfiguration(Module module) {
        sonarModuleComponent = module.getComponent(SonarModuleComponent.class);
        projectComboBoxModel = new ProjectComboBoxModel();
        cmbProject.setModel(projectComboBoxModel);

        btnRefreshProjects.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startRefreshProjects();
            }
        });
        cmbProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModified(true);
            }
        });


        txtHost.setText(sonarModuleComponent.getState().host);
        txtUser.setText(sonarModuleComponent.getState().user);
        txtPassword.setText(sonarModuleComponent.getState().password);
        useProxyBox.setSelected(sonarModuleComponent.getState().useProxy);


        if (!StringUtils.isEmpty(txtHost.getText())) {
            startRefreshProjects();
        }
    }

    private void startRefreshProjects() {
        lblRefreshingProjects.setBusy(true);
        btnRefreshProjects.setEnabled(false);
        txtHost.setEnabled(false);
        txtUser.setEnabled(false);
        txtPassword.setEnabled(false);
        useProxyBox.setEnabled(false);
        cmbProject.setEnabled(false);

        Sonar sonarConn = SonarUtils.getSonar(txtHost.getText(), txtUser.getText(), new String(
                txtPassword.getPassword()),
                useProxyBox.isSelected());
        RefreshProjectListWorker refreshProjectListWorker = new RefreshProjectListWorker(sonarConn);
        refreshProjectListWorker.addListener(this);
        refreshProjectListWorker.execute();
    }

    @Override
    public void doneRefreshProjects(final List<SonarProject> newProjectList) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (newProjectList != null) {
                    projectComboBoxModel.refreshProjectList(newProjectList, sonarModuleComponent.getState().projectKey);
                }
                lblRefreshingProjects.setBusy(false);
                btnRefreshProjects.setEnabled(true);
                txtHost.setEnabled(true);
                txtUser.setEnabled(true);
                txtPassword.setEnabled(true);
                useProxyBox.setEnabled(true);
                cmbProject.setEnabled(true);
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Sonar Configuration";
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        return pnlMain;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (cmbProject.getSelectedItem() != null) {
            sonarModuleComponent.getState().host = txtHost.getText();
            sonarModuleComponent.getState().user = txtUser.getText();
            sonarModuleComponent.getState().password = new String(txtPassword.getPassword());
            sonarModuleComponent.getState().projectKey =
                    ((SonarProject) (cmbProject.getSelectedItem())).getResource().getKey();
            sonarModuleComponent.getState().useProxy = useProxyBox.isSelected();
            sonarModuleComponent.getState().configured = true;
        }
    }

    // needed for IDEA 11
    public Icon getIcon() {
        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }
}
