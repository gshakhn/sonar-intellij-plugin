package org.sonar.ide.intellij.model;

import org.sonar.wsclient.services.Resource;

public class SonarProject {
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
