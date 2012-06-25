package org.sonar.ide.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Violation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LastInspectionResult {
  private static LastInspectionResult ourInstance = new LastInspectionResult();

  private static List<ViolatationChangedListener> listeners = new LinkedList<ViolatationChangedListener>();

  private Map<VirtualFile, List<Violation>> violations = null;

  public static LastInspectionResult getInstance() {
    return ourInstance;
  }

  private LastInspectionResult() {
  }

  public void setViolations(Map<VirtualFile, List<Violation>> violations) {
    this.violations = violations;
    for (ViolatationChangedListener listener : listeners) {
      listener.violationChanged(violations);
    }
  }

  public Map<VirtualFile, List<Violation>> getViolations() {
    return violations;
  }

  public void addListener(ViolatationChangedListener listener) {
    listeners.add(listener);
  }

  public void removeListener(ViolatationChangedListener listener) {
    listeners.remove(listener);
  }

}
