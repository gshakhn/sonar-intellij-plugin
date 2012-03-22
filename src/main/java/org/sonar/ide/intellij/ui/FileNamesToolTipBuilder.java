package org.sonar.ide.intellij.ui;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class FileNamesToolTipBuilder {
  public String generateToolTip(List<VirtualFile> filesLoading) {
    if (filesLoading.isEmpty()) {
      return StringUtils.EMPTY;
    }
    StringBuilder newTooltip = new StringBuilder();
    for (VirtualFile file : filesLoading) {
      if (file==null) {
        continue;
      }
      if (newTooltip.length() > 0) {
        newTooltip.append("\n");
      }
      newTooltip.append(file.getName());
    }
    newTooltip.insert(0, "Loading data for\n");
    return newTooltip.toString();
  }
}
