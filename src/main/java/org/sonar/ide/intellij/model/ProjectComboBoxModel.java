package org.sonar.ide.intellij.model;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gshakhn
 * Date: 1/2/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectComboBoxModel extends DefaultComboBoxModel {
    private List<SonarProject> projects;

    public ProjectComboBoxModel() {
    }

    public void refreshProjectList(Sonar sonar) {
        projects = new ArrayList<SonarProject>();

        ResourceQuery query = new ResourceQuery();
        query.setQualifiers("TRK,BRC");
        query.setDepth(1);

        List<Resource> resources = sonar.findAll(query);
        removeAllElements();

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

        for (SonarProject project : projects) {
            addElement(project);
        }
    }
    
    public SonarProject findProjectByKey(String projectKey) {
        for (SonarProject project : projects) {
            if (project.getResource().getKey().equals(projectKey)) {
                return project;
            }
        }

        return null;
    }

    public static class SonarProject {
        private Resource resource;

        public SonarProject(Resource resource) {
            this.resource = resource;
        }

        @Override
        public String toString() {
            return resource.getName() + " - " + resource.getVersion();
        }

        public Resource getResource() {
            return resource;
        }
    }
}
