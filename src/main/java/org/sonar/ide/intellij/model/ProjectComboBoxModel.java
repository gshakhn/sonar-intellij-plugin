package org.sonar.ide.intellij.model;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectComboBoxModel extends DefaultComboBoxModel {
    private List<SonarProject> projects = new ArrayList<SonarProject>();

    public void refreshProjectList(List<SonarProject> projects, String existingProjectKey) {
        this.projects = projects;

        removeAllElements();
        for (SonarProject project : projects) {
            addElement(project);
        }

        if (!StringUtils.isBlank(existingProjectKey)) {
            SonarProject selectedProject = findProjectByKey(existingProjectKey);
            setSelectedItem(selectedProject);
        }
    }

    private SonarProject findProjectByKey(String projectKey) {
        for (SonarProject project : projects) {
            if (project.getResource().getKey().equals(projectKey)) {
                return project;
            }
        }

        return null;
    }

}
