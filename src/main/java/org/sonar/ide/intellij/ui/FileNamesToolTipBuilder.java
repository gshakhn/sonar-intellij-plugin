package org.sonar.ide.intellij.ui;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class FileNamesToolTipBuilder {

    private static final String PREFIX = "Loading data for\n";

    public String generateToolTip(List<VirtualFile> filesLoading) {
        if (filesLoading.isEmpty()) {
            return StringUtils.EMPTY;
        }
        StringBuilder newTooltip = new StringBuilder();
        newTooltip.append(PREFIX);
        for (VirtualFile file : filesLoading) {
            if (file == null) {
                continue;
            }
            if (newTooltip.length() > PREFIX.length()) {
                newTooltip.append("\n");
            }
            newTooltip.append(file.getName());
        }
        return newTooltip.toString();
    }
}
