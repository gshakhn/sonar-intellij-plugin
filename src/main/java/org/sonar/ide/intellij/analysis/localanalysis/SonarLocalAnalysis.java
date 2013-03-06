package org.sonar.ide.intellij.analysis.localanalysis;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sonar.ide.intellij.analysis.SonarAnalysis;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.ide.intellij.utils.IntellijIdeaUtils;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SonarLocalAnalysis implements SonarAnalysis {

    private JSONObject rawViolations;

    private Project project;

    private final Map<VirtualFile, List<Violation>> violationSet = new ConcurrentHashMap<VirtualFile, List<Violation>>();

    private final Map<VirtualFile, Source> sourceSet = new ConcurrentHashMap<VirtualFile, Source>();

    public SonarLocalAnalysis(Project project, JSONObject rawViolations) {
        this.project = project;
        this.rawViolations = rawViolations;
    }

    @Override
    public List<Violation> getViolations(VirtualFile virtualFile) {
        final List<Violation> violations;
        if (this.violationSet.containsKey(virtualFile)) {
            violations = this.violationSet.get(virtualFile);
        } else {
            violations = unMarshallViolation(virtualFile);
            this.violationSet.put(virtualFile, violations);
        }

        return violations;
    }

    @Override
    public Source getSource(VirtualFile virtualFile) {

        if (sourceSet.containsKey(virtualFile)) {
            return sourceSet.get(virtualFile);
        } else {
            synchronized (sourceSet) {
                if (sourceSet.containsKey(virtualFile)) {
                    return sourceSet.get(virtualFile);
                }
                return fillSourceCache(virtualFile);
            }
        }
    }

    private Source fillSourceCache(VirtualFile virtualFile) {
        PsiFileSystemItem psiFileItem = IntellijIdeaUtils.findPsiFileItem(project, virtualFile);
        Source source = new Source();
        String[] lines = psiFileItem.getText().split("\n");
        int index = 0;
        for (String line : lines) {
            source.addLine(index, line);
            index++;
        }

        sourceSet.put(virtualFile, source);

        return source;
    }

    @Override
    public void removeViolation(VirtualFile virtualFile, Violation violation) {
        throw new RuntimeException("Remove violation is not available in local analysis");
    }

    @Override
    public void addLoadingFileListener(LoadingSonarFilesListener listener) {
        // not so important yet to notify ui. The raw data un-marshall should be really fast
    }

    @Override
    public void loadViolations(VirtualFile newFile, RefreshListener<Violation> refreshListener) {

        final List<Violation> violations = getViolations(newFile);

        refreshListener.doneRefresh(newFile, violations);
    }

    private List<Violation> unMarshallViolation(VirtualFile newFile) {
        String partialResourceKey = SonarResourceKeyUtils.createPartialResourceKey(project, newFile);
        JSONArray violationForCurrentFile = (JSONArray) rawViolations.get(partialResourceKey);

        final List<Violation> violations = new ArrayList<Violation>();
        if (violationForCurrentFile != null) {
            for (Object violationObj : violationForCurrentFile) {
                Violation violation;
                JSONObject jsonViolation = (JSONObject) violationObj;
                violation = WSViolationUnMarshaller.unMarshallViolation(jsonViolation);

                violations.add(violation);
            }
        }

        return violations;
    }

    @Override
    public void loadSource(VirtualFile newFile, RefreshListener<Source> refreshListener) {
        refreshListener.doneRefresh(newFile, Collections.singletonList(getSource(newFile)));
    }

    @Override
    public void clear() {
        violationSet.clear();
        sourceSet.clear();
    }

    @Override
    public boolean isLocalAnalysis() {
        return true;
    }

    Map<VirtualFile, List<Violation>> getViolationSet() {
        return violationSet;
    }

    Map<VirtualFile, Source> getSourceSet() {
        return sourceSet;
    }
}
