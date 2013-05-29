package org.sonar.ide.intellij.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;

/**
 * Handy utility class for intellij api enrichment
 */
public class IntellijIdeaUtils {

    public static PsiFileSystemItem findPsiFileItem(final Project project, final VirtualFile virtualFile) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        return ApplicationManager.getApplication().runReadAction(new Computable<PsiFileSystemItem>() {
            @Override
            public PsiFileSystemItem compute() {
                final PsiFileSystemItem item;
                if (virtualFile.isDirectory()) {
                    item = psiManager.findDirectory(virtualFile);
                } else {
                    item = psiManager.findFile(virtualFile);
                }

                return item;
            }
        });
    }
}
