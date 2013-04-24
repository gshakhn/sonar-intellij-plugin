package org.sonar.ide.intellij.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.MultiMap;
import org.jdesktop.swingx.JXBusyLabel;
import org.jetbrains.annotations.Nls;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.RefreshProjectListListener;
import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.ide.intellij.utils.SonarUtils;
import org.sonar.ide.intellij.worker.RefreshProjectListWorker;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SonarProjectConfiguration extends BaseConfigurable implements RefreshProjectListListener
{
    private JLabel lblHost;
    private JTextField txtHost;
    private JTextField txtUser;
    private JLabel lblUser;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JPanel pnlMain;
    private JXBusyLabel lblRefreshingProjects;
    private JButton applyToAllModulesButton;
    private JButton applyToNotConfiguredButton;
    private JButton testButton;
    private JCheckBox useProxyBox;
    private List<SonarProject> projectList = null;

    private SonarProjectComponent sonarProjectComponent;
    private final Project project;
    private Module[] modules;

    public SonarProjectConfiguration(Project project)
    {
        this.project = project;

        this.modules = ModuleManager.getInstance(this.project).getModules();
        sonarProjectComponent = project.getComponent(SonarProjectComponent.class);

        testButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                startRefreshProjects();
            }
        });
        applyToAllModulesButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateModules(false);
            }
        });
        applyToNotConfiguredButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateModules(true);
            }
        });
        DocumentListener documentListener = new DocumentListener()
        {
            public void onChange()
            {
                applyToAllModulesButton.setEnabled(false);
                applyToNotConfiguredButton.setEnabled(false);
                setModified(false);
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                onChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                onChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                onChange();
            }
        };
        txtHost.getDocument().addDocumentListener(documentListener);
        txtUser.getDocument().addDocumentListener(documentListener);
        txtPassword.getDocument().addDocumentListener(documentListener);

        if (sonarProjectComponent != null)
        {
            txtHost.setText(sonarProjectComponent.getState().host);
            txtUser.setText(sonarProjectComponent.getState().user);
            txtPassword.setText(sonarProjectComponent.getState().password);
            useProxyBox.setSelected(sonarProjectComponent.getState().useProxy);
        }
    }

    private void startRefreshProjects()
    {
        lblRefreshingProjects.setBusy(true);
        applyToAllModulesButton.setEnabled(false);
        applyToNotConfiguredButton.setEnabled(false);
        testButton.setEnabled(false);
        txtHost.setEnabled(false);
        txtUser.setEnabled(false);
        txtPassword.setEnabled(false);
        useProxyBox.setEnabled(false);

        final Sonar sonarConn = SonarUtils.getSonar(txtHost.getText(), txtUser.getText(), new String(txtPassword.getPassword()),
                                          useProxyBox.isSelected());
        final RefreshProjectListWorker refreshProjectListWorker = new RefreshProjectListWorker(sonarConn);
        refreshProjectListWorker.addListener(this);
        refreshProjectListWorker.execute();
    }

    @Override
    public void doneRefreshProjects(final List<SonarProject> newProjectList)
    {
        boolean modified = newProjectList != null;
        applyToAllModulesButton.setEnabled(modified);
        applyToNotConfiguredButton.setEnabled(modified);
        if (modified)
        {
            projectList = newProjectList;
            setModified(true);
        }
        txtHost.setEnabled(true);
        txtUser.setEnabled(true);
        txtPassword.setEnabled(true);
        useProxyBox.setEnabled(true);
        testButton.setEnabled(true);
        lblRefreshingProjects.setBusy(false);
    }

    public void updateModules(final boolean emptyOnly)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (projectList == null)
                {
                    return;
                }
                MultiMap<String, String> sonarProjectMap = new MultiMap<String, String>();
                for (SonarProject project : projectList)
                {
                    String fullKey = project.getResource().getKey();
                    sonarProjectMap.putValue(fullKey.substring(fullKey.indexOf(":") + 1), fullKey);
                }
                for (Module module : modules)
                {
                    SonarModuleComponent.SonarModuleState sonarModuleState = module.getComponent(
                            SonarModuleComponent.class).getState();
                    if (!emptyOnly || !sonarModuleState.configured)
                    {
                        sonarModuleState.host = txtHost.getText();
                        sonarModuleState.user = txtUser.getText();
                        sonarModuleState.password = new String(txtPassword.getPassword());
                        sonarModuleState.useProxy = useProxyBox.isSelected();
                        sonarModuleState.configured = true;
                        String key = module.getName();
                        if (sonarProjectMap.get(key).size() == 1)
                        {
                            sonarModuleState.projectKey = sonarProjectMap.get(key).iterator().next();
                        }
                    }
                }
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName()
    {
        return "Sonar Configuration";
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

    @Override
    public JComponent createComponent()
    {
        return pnlMain;
    }

    @Override
    public void apply() throws ConfigurationException
    {
        if (sonarProjectComponent != null && sonarProjectComponent.getState() != null)
        {
            sonarProjectComponent.getState().host = txtHost.getText();
            sonarProjectComponent.getState().user = txtUser.getText();
            sonarProjectComponent.getState().password = new String(txtPassword.getPassword());
            sonarProjectComponent.getState().useProxy = useProxyBox.isSelected();
            sonarProjectComponent.getState().configured = true;
        }
    }


    @Override
    public void reset()
    {
    }

    @Override
    public void disposeUIResources()
    {
    }
}
