package org.sonar.ide.intellij.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class AbstractSonarInspection extends LocalInspectionTool {

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Sonar";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    protected TextRange getTextRange(@NotNull Document document, int line) {
        try {
            int lineStartOffset = document.getLineStartOffset(line);
            int lineEndOffset = document.getLineEndOffset(line);
            return new TextRange(lineStartOffset, lineEndOffset);
        } catch (IndexOutOfBoundsException e) {
            int lastLine = document.getLineCount() - 1;
            int lineStartOffset = document.getLineStartOffset(lastLine);
            int lineEndOffset = document.getLineEndOffset(lastLine);
            return new TextRange(lineStartOffset, lineEndOffset);
        }
    }
}
