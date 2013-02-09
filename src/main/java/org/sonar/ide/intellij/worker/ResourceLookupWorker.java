package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Query;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class ResourceLookupWorker extends RefreshSonarFileWorker<Resource> {

    public interface ResourceLoadCallback {
        public void resourceLoaded(@Nullable Resource resource);
    }

    private ResourceLoadCallback callback;

    public ResourceLookupWorker(Project project, VirtualFile virtualFile, ResourceLoadCallback callback) {
        super(project, virtualFile);
            this.callback = callback;
    }

    @Override
    protected String getResourceKey() {
        return SonarResourceKeyUtils.createFileOrFolderResourceKey(this.getProject(), virtualFile);
    }

    @Override
    protected Query<Resource> getQuery(String resourceKey) {
        return ResourceQuery.create(resourceKey);
    }

    @Override
    protected void done() {

        try {
            List<Resource> resources = get();
            if (resources != null && !resources.isEmpty()) {
                callback.resourceLoaded(resources.get(0));
            }   else {
                callback.resourceLoaded(null);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
