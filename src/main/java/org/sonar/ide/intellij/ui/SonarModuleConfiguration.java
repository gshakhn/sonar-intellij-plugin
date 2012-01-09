package org.sonar.ide.intellij.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.model.ProjectComboBoxModel;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SonarModuleConfiguration extends BaseConfigurable {
  private JLabel lblHost;
  private JTextField txtHost;
  private JButton btnRefreshProjects;
  private JTextField txtUser;
  private JLabel lblUser;
  private JLabel lblPassword;
  private JPasswordField txtPassword;
  private JLabel lblProject;
  private JComboBox cmbProject;
  private JPanel pnlMain;

  private ProjectComboBoxModel projectComboBoxModel;

  private SonarModuleComponent sonarModuleComponent;

  public SonarModuleConfiguration(Module module) {
    sonarModuleComponent = module.getComponent(SonarModuleComponent.class);
    projectComboBoxModel = new ProjectComboBoxModel();
    cmbProject.setModel(projectComboBoxModel);

    btnRefreshProjects.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectComboBoxModel.refreshProjectList(getSonar());
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

    if (!StringUtils.isEmpty(txtHost.getText())) {
      projectComboBoxModel.refreshProjectList(getSonar());

      ProjectComboBoxModel.SonarProject selectedProject = projectComboBoxModel.findProjectByKey(sonarModuleComponent.getState().projectKey);
      projectComboBoxModel.setSelectedItem(selectedProject);
    }
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
    sonarModuleComponent.getState().host = txtHost.getText();
    sonarModuleComponent.getState().user = txtUser.getText();
    sonarModuleComponent.getState().password = txtPassword.getPassword().toString();
    sonarModuleComponent.getState().projectKey = ((ProjectComboBoxModel.SonarProject) (cmbProject.getSelectedItem())).getResource().getKey();
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
    String password = txtPassword.getPassword().toString();
    return new Sonar(new HttpClient4Connector(new Host(host, user, password)));
  }
}
