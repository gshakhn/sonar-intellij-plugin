package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Model;
import org.sonar.wsclient.services.Query;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class RefreshSonarFileWorker<T extends Model> extends SwingWorker<List<T>, Void> {
    private Project project;
    protected VirtualFile virtualFile;

    private List<RefreshListener<T>> listeners = new ArrayList<RefreshListener<T>>();

    protected RefreshSonarFileWorker(Project project, VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
    }

    public void addListener(RefreshListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    protected List<T> doInBackground() throws Exception {
        String resourceKey = getResourceKey();
        if (resourceKey == null) {
            return new ArrayList<T>();
        }
        Sonar sonar = getSonar();

        Query<T> query = getQuery(resourceKey);
        return sonar.findAll(query);
    }

    protected void notifyListeners(List<T> results) {
        for (RefreshListener<T> listener : this.listeners) {
            listener.doneRefresh(this.virtualFile, results);
        }
    }

    protected abstract Query<T> getQuery(String resourceKey);

    private Sonar getSonar() {
        return getSonarModuleComponent().getSonar();
    }

    protected String getResourceKey() {
        return SonarResourceKeyUtils.createFileResourceKey(this.project, virtualFile);
    }

    private SonarModuleComponent getSonarModuleComponent() {
        return SonarResourceKeyUtils.getSonarModuleComponent(project, virtualFile);
    }

    protected Project getProject() {
        return project;
    }
}
