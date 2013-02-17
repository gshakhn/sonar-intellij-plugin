package org.sonar.ide.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshSourceListener;
import org.sonar.ide.intellij.listener.RefreshViolationsListener;
import org.sonar.ide.intellij.worker.RefreshSourceWorker;
import org.sonar.ide.intellij.worker.RefreshViolationsWorker;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class SonarCache implements RefreshViolationsListener, RefreshSourceListener {
  private Project project;

  private final Map<VirtualFile, List<Violation>> violationsCache = new HashMap<VirtualFile, List<Violation>>();
  private final Map<VirtualFile, Source> sourceCache = new HashMap<VirtualFile, Source>();

  private final Set<VirtualFile> currentlyLoadingViolations = Collections.synchronizedSet(new HashSet<VirtualFile>());
  private final Set<VirtualFile> currentlyLoadingSources = Collections.synchronizedSet(new HashSet<VirtualFile>());

  private final Set<LoadingSonarFilesListener> loadingFilesListeners = new HashSet<LoadingSonarFilesListener>();
  private final Map<VirtualFile, List<RefreshViolationsListener>> refreshViolationListeners = Collections.synchronizedMap(new HashMap<VirtualFile, List<RefreshViolationsListener>>());
  private final Map<VirtualFile, List<RefreshSourceListener>> refreshSourceListeners = Collections.synchronizedMap(new HashMap<VirtualFile, List<RefreshSourceListener>>());

  public SonarCache(Project project) {
    this.project = project;
  }

  public List<Violation> getViolations(VirtualFile virtualFile){
    if (violationsCache.containsKey(virtualFile)) {
      return violationsCache.get(virtualFile);
    } else {
      RefreshViolationsWorker refreshViolationsWorker = new RefreshViolationsWorker(this.project, virtualFile);
      refreshViolationsWorker.addListener(this);
      refreshViolationsWorker.execute();
      try {
        return refreshViolationsWorker.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  public void loadViolations(VirtualFile virtualFile, RefreshViolationsListener listener) {
    if (this.violationsCache.containsKey(virtualFile)) {
      listener.doneRefreshViolations(virtualFile, this.violationsCache.get(virtualFile));
    } else {
      synchronized (refreshViolationListeners) {
        if (!refreshViolationListeners.containsKey(virtualFile)) {
          refreshViolationListeners.put(virtualFile, new ArrayList<RefreshViolationsListener>());
        }

        refreshViolationListeners.get(virtualFile).add(listener);
      }
      synchronized (this.currentlyLoadingViolations) {
        if (!this.currentlyLoadingViolations.contains(virtualFile)) {
          this.currentlyLoadingViolations.add(virtualFile);
          refreshLoadingSonarFiles();

          RefreshViolationsWorker refreshViolationsWorker = new RefreshViolationsWorker(this.project, virtualFile);
          refreshViolationsWorker.addListener(this);
          refreshViolationsWorker.execute();
        }
      }
    }
  }

  public void loadSource(VirtualFile virtualFile, RefreshSourceListener listener) {
    if (this.sourceCache.containsKey(virtualFile)) {
      listener.doneRefreshSource(virtualFile, this.sourceCache.get(virtualFile));
    } else {
      synchronized (refreshSourceListeners) {
        if (!refreshSourceListeners.containsKey(virtualFile)) {
          refreshSourceListeners.put(virtualFile, new ArrayList<RefreshSourceListener>());
        }

        refreshSourceListeners.get(virtualFile).add(listener);
      }
      synchronized (this.currentlyLoadingSources) {
        if (!this.currentlyLoadingSources.contains(virtualFile)) {
          this.currentlyLoadingSources.add(virtualFile);
          refreshLoadingSonarFiles();

          RefreshSourceWorker refreshSourceWorker = new RefreshSourceWorker(this.project, virtualFile);
          refreshSourceWorker.addListener(this);
          refreshSourceWorker.execute();
        }
      }
    }
  }

  private void refreshLoadingSonarFiles() {
    Set<VirtualFile> currentlyLoadingFiles = new HashSet<VirtualFile>();
    currentlyLoadingFiles.addAll(this.currentlyLoadingViolations);
    currentlyLoadingFiles.addAll(this.currentlyLoadingSources);

    for (LoadingSonarFilesListener listener : this.loadingFilesListeners) {
      listener.loadingFiles(new ArrayList<VirtualFile>(currentlyLoadingFiles));
    }
  }

  @Override
  public void doneRefreshViolations(VirtualFile virtualFile, List<Violation> violations) {
    this.violationsCache.put(virtualFile, violations);

    synchronized (this.currentlyLoadingViolations) {
      this.currentlyLoadingViolations.remove(virtualFile);
    }

    synchronized (this.refreshViolationListeners) {
      if (this.refreshViolationListeners.containsKey(virtualFile)) {
        for (RefreshViolationsListener listener : this.refreshViolationListeners.get(virtualFile)) {
          listener.doneRefreshViolations(virtualFile, violations);
        }
        this.refreshViolationListeners.get(virtualFile).clear();
      }
    }

    refreshLoadingSonarFiles();
  }

  @Override
  public void doneRefreshSource(VirtualFile virtualFile, Source source) {
    this.sourceCache.put(virtualFile, source);

    synchronized (this.currentlyLoadingSources) {
      this.currentlyLoadingSources.remove(virtualFile);
    }

    synchronized (this.refreshSourceListeners) {
      if (this.refreshSourceListeners.containsKey(virtualFile)) {
        for (RefreshSourceListener listener : this.refreshSourceListeners.get(virtualFile)) {
          listener.doneRefreshSource(virtualFile, source);
        }
        this.refreshSourceListeners.get(virtualFile).clear();
      }
    }

    refreshLoadingSonarFiles();
  }

  public void addLoadingFileListener(LoadingSonarFilesListener listener) {
    this.loadingFilesListeners.add(listener);
  }
}
