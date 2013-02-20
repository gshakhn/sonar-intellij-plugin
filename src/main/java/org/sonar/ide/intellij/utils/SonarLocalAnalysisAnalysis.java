package org.sonar.ide.intellij.utils;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sonar.ide.intellij.listener.LoadingSonarFilesListener;
import org.sonar.ide.intellij.listener.RefreshListener;
import org.sonar.wsclient.JdkUtils;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.WSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SonarLocalAnalysisAnalysis implements SonarAnalysis {

    private JSONObject rawViolations;

    private Project project;

    private Map<VirtualFile, List<Violation>> violationSet = new ConcurrentHashMap<VirtualFile, List<Violation>>();


    public SonarLocalAnalysisAnalysis(Project project, JSONObject rawViolations) {
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
        return null;
    }

    @Override
    public void removeViolation(VirtualFile virtualFile, Violation violation) {

        // TODO(AGAL): implement this to remve from result .. could be easy if we could delete from rawViolation
        throw new RuntimeException("Cannot remove violation in local analysis");
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
        refreshListener.doneRefresh(newFile, null);
    }

    @Override
    public void clear() {
        rawViolations.clear();
        violationSet.clear();
    }
}
