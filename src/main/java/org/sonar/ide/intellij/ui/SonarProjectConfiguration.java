package org.sonar.ide.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
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

public class SonarProjectConfiguration extends BaseConfigurable implements RefreshProjectListListener {
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
  private List<SonarProject> projectList = null;

  private SonarProjectComponent sonarProjectComponent;
  private final Project project;
  private Module[] modules;

  public SonarProjectConfiguration(Project project) {
    this.project = project;

    this.modules = ModuleManager.getInstance(this.project).getModules();
    sonarProjectComponent = project.getComponent(SonarProjectComponent.class);

    testButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

      }
    });
    testButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startRefreshProjects();
      }
    });
    applyToAllModulesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateModules(false);
      }
    });
    applyToNotConfiguredButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateModules(true);
      }
    });
    DocumentListener documentListener = new DocumentListener() {
      public void onChange() {
        applyToAllModulesButton.setEnabled(false);
        applyToNotConfiguredButton.setEnabled(false);
        setModified(false);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        onChange();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        onChange();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        onChange();
      }
    };
    txtHost.getDocument().addDocumentListener(documentListener);
    txtUser.getDocument().addDocumentListener(documentListener);
    txtPassword.getDocument().addDocumentListener(documentListener);

    if (sonarProjectComponent != null) {
      txtHost.setText(sonarProjectComponent.getState().host);
      txtUser.setText(sonarProjectComponent.getState().user);
      txtPassword.setText(sonarProjectComponent.getState().password);
    }
  }

  private void startRefreshProjects() {
    lblRefreshingProjects.setBusy(true);
    applyToAllModulesButton.setEnabled(false);
    applyToNotConfiguredButton.setEnabled(false);
    txtHost.setEnabled(false);
    txtUser.setEnabled(false);
    txtPassword.setEnabled(false);

    RefreshProjectListWorker refreshProjectListWorker = new RefreshProjectListWorker(this.getSonar());
    refreshProjectListWorker.addListener(this);
    refreshProjectListWorker.execute();
  }

  @Override
  public void doneRefreshProjects(List<SonarProject> newProjectList) {
    if (newProjectList == null) {
      applyToAllModulesButton.setEnabled(false);
      applyToNotConfiguredButton.setEnabled(false);
      lblRefreshingProjects.setBusy(false);
      txtHost.setEnabled(true);
      txtUser.setEnabled(true);
      txtPassword.setEnabled(true);
    } else {
      projectList = newProjectList;
      applyToAllModulesButton.setEnabled(true);
      applyToNotConfiguredButton.setEnabled(true);
      lblRefreshingProjects.setBusy(false);
      txtHost.setEnabled(true);
      txtUser.setEnabled(true);
      txtPassword.setEnabled(true);
      setModified(true);
    }
  }

  public void updateModules(final boolean emptyOnly) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        if (projectList == null)
          return;
        MultiMap<String, String> sonarProjectMap = new MultiMap<String, String>();
        for (SonarProject project : projectList) {
          String fullKey = project.getResource().getKey();
          sonarProjectMap.putValue(fullKey.substring(fullKey.indexOf(":") + 1), fullKey);
        }
        for (Module module : modules) {
          SonarModuleComponent.SonarModuleState sonarModuleState = module.getComponent(SonarModuleComponent.class).getState();
          if (!emptyOnly || !sonarModuleState.configured) {
            sonarModuleState.host = txtHost.getText();
            sonarModuleState.user = txtUser.getText();
            sonarModuleState.password = new String(txtPassword.getPassword());
            sonarModuleState.configured = true;
            String key = module.getName();
            if (sonarProjectMap.get(key).size() == 1) {
              sonarModuleState.projectKey = sonarProjectMap.get(key).iterator().next();
            }
          }
        }
      }
    });
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Sonar Configuration";
  }

  @Override
  public Icon getIcon() {
    return null;
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
    sonarProjectComponent.getState().host = txtHost.getText();
    sonarProjectComponent.getState().user = txtUser.getText();
    sonarProjectComponent.getState().password = new String(txtPassword.getPassword());
    sonarProjectComponent.getState().configured = true;
  }


  @Override
  public void reset() {
  }

  @Override
  public void disposeUIResources() {
  }

  private Sonar getSonar() {
    String host = txtHost.getText();
    String user = txtUser.getText();
    String password = new String(txtPassword.getPassword());
    return new Sonar(new HttpClient4Connector(new Host(host, user, password)));
  }
}
