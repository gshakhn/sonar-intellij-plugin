package org.sonar.ide.intellij.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.sonar.ide.intellij.component.SonarModuleComponent;


public class SonarResourceKeyUtils {

    /**
     * Callback interface for creating sonar resource key based on the input file {file|folder}
     */
    interface SonarResourceKeyMaker {
        String makeResourceKey(PsiFileSystemItem psiFile, SonarModuleComponent sonarModuleComponent);
    }

    private static String createResourceKey(final Project project, final VirtualFile virtualFile, final SonarResourceKeyMaker keyMaker) {
        final SonarModuleComponent sonarModuleComponent = getSonarModuleComponent(project, virtualFile);
        if (sonarModuleComponent == null) { // There is no module for this file
            return null;
        }

        if (!sonarModuleComponent.isConfigured()) {
            return null;
        }

        final PsiManager psiManager = PsiManager.getInstance(project);
        final PsiFileSystemItem psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFileSystemItem>() {
            @Override
            public PsiFileSystemItem compute() {
                PsiFileSystemItem item;
                if (virtualFile.isDirectory()) {
                    item = psiManager.findDirectory(virtualFile);
                } else {
                    item = psiManager.findFile(virtualFile);
                }

                return item;
            }
        });

        return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            @Override
            public String compute() {
                return keyMaker.makeResourceKey(psiFile, sonarModuleComponent);
            }
        });
    }

    public static String createFileResourceKey(final Project project, final VirtualFile virtualFile) {
        return createResourceKey(project, virtualFile, new SonarResourceKeyMaker() {
            @Override
            public String makeResourceKey(PsiFileSystemItem psiFile, SonarModuleComponent sonarModuleComponent) {
                if (!(psiFile instanceof PsiJavaFile)) {
                    return null;
                }

                return SonarResourceKeyUtils.makeFileKey((PsiJavaFile) psiFile, sonarModuleComponent);
            }
        });
    }

    private static String makeFileKey(PsiJavaFile psiFile, SonarModuleComponent sonarModuleComponent) {
        String packageName = psiFile.getPackageName();
        String className = psiFile.getClasses()[0].getName();

        return sonarModuleComponent.getState().projectKey + ":" + packageName + "." + className;
    }

    public static String createFileOrFolderResourceKey(final Project project, final VirtualFile virtualFile) {

        return createResourceKey(project, virtualFile, new SonarResourceKeyMaker() {
            @Override
            public String makeResourceKey(PsiFileSystemItem psiFile, SonarModuleComponent sonarModuleComponent) {

                if (psiFile == null) {
                    return null;
                }

                if (psiFile.isDirectory()) {
                    return SonarResourceKeyUtils.makeDirectoryResourceKey(psiFile, sonarModuleComponent);
                } else {
                    if (!(psiFile instanceof PsiJavaFile)) {
                        return null;
                    }
                    return SonarResourceKeyUtils.makeFileKey((PsiJavaFile) psiFile, sonarModuleComponent);
                }
            }
        });
    }

    private static String makeDirectoryResourceKey(PsiFileSystemItem psiFile, SonarModuleComponent sonarModuleComponent) {
        PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage((PsiDirectory) (psiFile));
        if (aPackage == null) {
            return null;
        }
        return sonarModuleComponent.getState().projectKey + ":" + aPackage.getQualifiedName();
    }

    public static SonarModuleComponent getSonarModuleComponent(Project project, VirtualFile virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        if (module == null) { // This file doesn't belong to a module.
            return null;
        } else {
            return module.getComponent(SonarModuleComponent.class);
        }
    }
}
