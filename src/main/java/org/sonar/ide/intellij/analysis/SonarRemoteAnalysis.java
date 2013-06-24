package org.sonar.ide.intellij.analysis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.ide.intellij.worker.RefreshSonarFileWorker;
import org.sonar.ide.intellij.worker.RefreshSourceWorker;
import org.sonar.ide.intellij.worker.RefreshViolationsWorker;
import org.sonar.wsclient.services.Model;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Represents the source and violations data which was done remotely
 */
public class SonarRemoteAnalysis implements SonarAnalysis {

    private Cache<Violation> violationCache;
    private Cache<Source> sourceCache;

    private final Set<LoadingSonarFilesListener> loadingFilesListeners = new HashSet<LoadingSonarFilesListener>();

    public SonarRemoteAnalysis(final Project project) {
        this.violationCache = new ViolationsCache(project);
        this.sourceCache = new SourceCache(project);
    }

    public List<Violation> getViolations(VirtualFile virtualFile) {
        return this.violationCache.get(virtualFile);
    }

    public void loadViolations(VirtualFile virtualFile, RefreshListener<Violation> listener) {
        this.violationCache.load(virtualFile, listener);
    }

    public void removeViolation(VirtualFile virtualFile, Violation violation) {
        violationCache.removeFromCache(virtualFile, violation);
    }

    public Source getSource(VirtualFile virtualFile) {
        List<Source> sources = sourceCache.get(virtualFile);
        if (sources == null || sources.isEmpty()) {
            return null;
        } else {
            return sources.get(0);
        }
    }

    public void loadSource(VirtualFile virtualFile, RefreshListener<Source> listener) {
        this.sourceCache.load(virtualFile, listener);
    }

    private void refreshLoadingSonarFiles() {
        Set<VirtualFile> currentlyLoadingFiles = new HashSet<VirtualFile>();
        currentlyLoadingFiles.addAll(this.violationCache.getCurrentlyLoadingFiles());
        currentlyLoadingFiles.addAll(this.sourceCache.getCurrentlyLoadingFiles());

        for (LoadingSonarFilesListener listener : this.loadingFilesListeners) {
            listener.loadingFiles(new ArrayList<VirtualFile>(currentlyLoadingFiles));
        }
    }

    public void addLoadingFileListener(LoadingSonarFilesListener listener) {
        this.loadingFilesListeners.add(listener);
    }

    public void clear() {
        this.violationCache.clear();
        this.sourceCache.clear();
    }

    @Override
    public boolean isLocalAnalysis() {
        return false;
    }

    private abstract class Cache<T extends Model> {

        private final Map<VirtualFile, List<T>> cache = new ConcurrentHashMap<VirtualFile, List<T>>();
        private final Map<VirtualFile, RefreshSonarFileWorker<T>> currentlyLoading = new ConcurrentHashMap<VirtualFile, RefreshSonarFileWorker<T>>();
        private final Map<VirtualFile, List<RefreshListener<T>>> loadListeners = new ConcurrentHashMap<VirtualFile, List<RefreshListener<T>>>();

        protected Project project;

        public Cache(Project project) {
            this.project = project;
        }

        public Set<VirtualFile> getCurrentlyLoadingFiles() {
            return currentlyLoading.keySet();
        }

        public void removeFromCache(VirtualFile virtualFile, T t) {
            this.cache.get(virtualFile).remove(t);
        }

        public List<T> get(VirtualFile virtualFile) {
            if (cache.containsKey(virtualFile)) {
                return cache.get(virtualFile);
            } else {
                RefreshSonarFileWorker<T> worker = createAndExecuteRefreshWorker(virtualFile);
                try {
                    return worker.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        public void load(VirtualFile virtualFile, RefreshListener<T> listener) {
            if (this.cache.containsKey(virtualFile)) {
                listener.doneRefresh(virtualFile, this.cache.get(virtualFile));
            } else {
                if (!loadListeners.containsKey(virtualFile)) {
                    loadListeners.put(virtualFile, new ArrayList<RefreshListener<T>>());
                }

                loadListeners.get(virtualFile).add(listener);
                createAndExecuteRefreshWorker(virtualFile);
            }
        }

        protected abstract RefreshSonarFileWorker<T> createWorker(VirtualFile virtualFile);

        private RefreshSonarFileWorker<T> createAndExecuteRefreshWorker(VirtualFile virtualFile) {
            if (!this.currentlyLoading.containsKey(virtualFile)) {
                RefreshSonarFileWorker<T> worker = createWorker(virtualFile);

                this.currentlyLoading.put(virtualFile, worker);
                refreshLoadingSonarFiles();

                worker.addListener(new RefreshListener<T>() {
                    @Override
                    public void doneRefresh(VirtualFile virtualFile, List<T> ts) {
                        finishRefresh(virtualFile, ts);
                    }
                });
                worker.execute();

                return worker;
            } else {
                return currentlyLoading.get(virtualFile);
            }
        }

        private void finishRefresh(VirtualFile virtualFile, List<T> t) {
            this.cache.put(virtualFile, t);

            this.currentlyLoading.remove(virtualFile);

            if (this.loadListeners.containsKey(virtualFile)) {
                for (RefreshListener<T> listener : this.loadListeners.get(virtualFile)) {
                    listener.doneRefresh(virtualFile, t);
                }
                this.loadListeners.get(virtualFile).clear();
            }

            refreshLoadingSonarFiles();
        }

        public void clear() {
            this.cache.clear();
        }
    }

    private class ViolationsCache extends Cache<Violation> {
        private ViolationsCache(Project project) {
            super(project);
        }

        @Override
        protected RefreshSonarFileWorker<Violation> createWorker(VirtualFile virtualFile) {
            return new RefreshViolationsWorker(this.project, virtualFile);
        }
    }

    private class SourceCache extends Cache<Source> {
        private SourceCache(Project project) {
            super(project);
        }

        @Override
        protected RefreshSonarFileWorker<Source> createWorker(VirtualFile virtualFile) {
            return new RefreshSourceWorker(this.project, virtualFile);
        }
    }
}
