package org.sonar.ide.intellij.worker;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import org.sonar.ide.intellij.listener.RefreshProjectListListener;
import org.sonar.ide.intellij.model.SonarProject;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshProjectListWorker extends SwingWorker<List<SonarProject>, Void> {

    private static final Logger LOG = Logger.getInstance("#org.sonar.ide.intellij.worker.RefreshProjectListWorker");
    private Sonar sonar;
    private List<RefreshProjectListListener> listeners = new ArrayList<RefreshProjectListListener>();

    public RefreshProjectListWorker(Sonar sonar) {
        this.sonar = sonar;
    }

    public void addListener(RefreshProjectListListener listener) {
        listeners.add(listener);
    }

    @Override
    protected List<SonarProject> doInBackground() throws Exception {
        final ResourceQuery query = new ResourceQuery();
        query.setQualifiers("TRK,BRC");
        query.setDepth(1);
        query.setTimeoutMilliseconds(15000);

        List<Resource> resources;
        try {
            resources = this.sonar.findAll(query);
        } catch (final Exception e) {
            LOG.warn("Error occurred querrying Sonar " + query.getUrl(), e);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    Throwable rootCause = e;
                    while (rootCause.getCause() != null) {
                        rootCause = e.getCause();
                    }
                    Messages.showErrorDialog("Connection to Sonar could not be established! " + rootCause.getLocalizedMessage(), "Sonar Connection Failed");
                }
            });
            return null;
        }

        final List<SonarProject> projects = new ArrayList<SonarProject>();

        for (Resource resource : resources) {
            final SonarProject project = new SonarProject(resource);
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
            for (RefreshProjectListListener listener : this.listeners) {
                listener.doneRefreshProjects(projects);
            }
        } catch (InterruptedException e) {
            LOG.error("Refreshing of project was interrupted!", e);
        } catch (ExecutionException e) {
            LOG.error("Refreshing of project could not be successfully executed!", e);
        }
    }
}
