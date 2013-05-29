package org.sonar.ide.intellij.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 2/18/13
 * Time: 10:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class SonarAnalysisAdaptor implements SonarAnalysis {

    protected SonarAnalysis sonarAnalysis;

    public SonarAnalysisAdaptor(SonarAnalysis sonarAnalysis) {
        this.sonarAnalysis = sonarAnalysis;
    }

    @Override
    public List<Violation> getViolations(VirtualFile virtualFile) {
        return sonarAnalysis.getViolations(virtualFile);
    }

    @Override
    public Source getSource(VirtualFile virtualFile) {
        return sonarAnalysis.getSource(virtualFile);
    }

    @Override
    public void removeViolation(VirtualFile virtualFile, Violation violation) {
        sonarAnalysis.removeViolation(virtualFile, violation);
    }

    @Override
    public void addLoadingFileListener(LoadingSonarFilesListener listener) {
        sonarAnalysis.addLoadingFileListener(listener);
    }

    @Override
    public void loadViolations(VirtualFile newFile, RefreshListener<Violation> refreshListener) {
        sonarAnalysis.loadViolations(newFile, refreshListener);
    }

    @Override
    public void loadSource(VirtualFile newFile, RefreshListener<Source> refreshListener) {
        sonarAnalysis.loadSource(newFile, refreshListener);
    }

    @Override
    public void clear() {
        sonarAnalysis.clear();
    }

    @Override
    public boolean isLocalAnalysis() {
        return sonarAnalysis.isLocalAnalysis();
    }
}
