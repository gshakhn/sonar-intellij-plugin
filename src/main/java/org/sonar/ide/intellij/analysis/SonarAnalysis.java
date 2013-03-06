package org.sonar.ide.intellij.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.List;

/**
 * Interface representing the intellij sonar data repository
 */
public interface SonarAnalysis {

    List<Violation> getViolations(VirtualFile virtualFile);

    Source getSource(VirtualFile virtualFile);

    void removeViolation(VirtualFile virtualFile, Violation violation);

    void addLoadingFileListener(LoadingSonarFilesListener listener);

    void loadViolations(VirtualFile newFile, RefreshListener<Violation> refreshListener);

    void loadSource(VirtualFile newFile, RefreshListener<Source> refreshListener);

    void clear();

    boolean isLocalAnalysis();
}
