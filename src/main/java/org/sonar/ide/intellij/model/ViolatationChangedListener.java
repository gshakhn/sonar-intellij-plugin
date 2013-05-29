package org.sonar.ide.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Violation;

import java.util.List;
import java.util.Map;

public interface ViolatationChangedListener {
    void violationChanged(Map<VirtualFile, List<Violation>> violations);
}
