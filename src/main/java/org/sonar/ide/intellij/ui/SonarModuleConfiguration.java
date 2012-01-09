package org.sonar.ide.intellij.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.model.ProjectComboBoxModel;
import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        new RefreshProjectList().execute();
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
      new RefreshProjectList().execute();
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
    sonarModuleComponent.getState().projectKey = ((SonarProject) (cmbProject.getSelectedItem())).getResource().getKey();
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

  class RefreshProjectList extends SwingWorker<List<SonarProject>, Void> {

    @Override
    protected List<SonarProject> doInBackground() throws Exception {
      ResourceQuery query = new ResourceQuery();
      query.setQualifiers("TRK,BRC");
      query.setDepth(1);

      List<Resource> resources = getSonar().findAll(query);
      
      List<SonarProject> projects = new ArrayList<SonarProject>();

      for (Resource resource : resources) {
        SonarProject project = new SonarProject(resource);
        projects.add(project);
      }

      Collections.sort(projects, new Comparator<SonarProject>() {
        @Override
        public int compare(SonarProject o1, SonarProject o2) {
          return o1.toString().compareTo(o2.toString());
        }
      });

      return projects;
    }

    @Override
    protected void done() {
      try {
        List<SonarProject> projects = get();
        projectComboBoxModel.refreshProjectList(projects);

        String projectKey = sonarModuleComponent.getState().projectKey;
        if (!StringUtils.isBlank(projectKey)) {
          SonarProject selectedProject = projectComboBoxModel.findProjectByKey(projectKey);
          projectComboBoxModel.setSelectedItem(selectedProject);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }
}
