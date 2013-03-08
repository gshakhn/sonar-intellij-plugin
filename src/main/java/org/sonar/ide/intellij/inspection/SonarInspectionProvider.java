package org.sonar.ide.intellij.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

public class SonarInspectionProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {
        return new Class[]{SonarViolationInspection.class};
    }
}
