package org.sonar.ide.intellij.runner;


import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SonarRunner {

    public static final String SONAR_RUNNER_MAIN_CLASS = "org.sonar.runner.Main";

    private SonarRunnerMonitor sonarRunnerMonitor;

    public static class SonarRunnerException extends RuntimeException {

        SonarRunnerException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public SonarRunner(SonarRunnerMonitor sonarRunnerMonitor) {
        this.sonarRunnerMonitor = sonarRunnerMonitor;
    }

    public File runAnalysis(Project project, SonarRunnerJavaParameters runParameters) {

        File sonarAnalyser = null;
        try {
            sonarAnalyser = unpackSonarRunner();
            cleanupOldResults(project);
            executeSonarRunner(project, runParameters, sonarAnalyser);

            return new File(project.getBasePath(), runParameters.getResultFile());
        } catch (IOException e) {
            throw new SonarRunnerException("Error unpacking sonar runner", e);
        } finally {
            try {
                if (sonarAnalyser != null) {
                    FileUtils.forceDeleteOnExit(sonarAnalyser);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanupOldResults(Project project) throws IOException {
        FileUtils.deleteQuietly(new File(project.getBasePath(), ".sonar"));
    }

    private void executeSonarRunner(Project project, SonarRunnerJavaParameters runParameters, File sonarRunnerPath) {

        try {

            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            JavaParameters javaParameters = runParameters.buildJavaParameters(sonarRunnerPath);

            javaParameters.setMainClass(SONAR_RUNNER_MAIN_CLASS);
            javaParameters.setJdk(projectSdk);
            javaParameters.setWorkingDirectory(project.getBasePath());


            OSProcessHandler osProcessHandler = javaParameters.createOSProcessHandler();
            osProcessHandler.addProcessListener(sonarRunnerMonitor);
            osProcessHandler.startNotify();
            while (!osProcessHandler.isProcessTerminated()) {

                if (sonarRunnerMonitor.shouldCancelRequest()) {
                    osProcessHandler.getProcess().destroy();
                } else {
                    Thread.sleep(300);
                }
            }
            osProcessHandler.waitFor();

        } catch (Exception e) {
            throw new SonarRunnerException("Error running sonar runner check log files", e);
        }
    }

    private File unpackSonarRunner() throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            File tmpSonarRunnerJarPath = File.createTempFile("sonar-runner", ".jar");
            os = new FileOutputStream(tmpSonarRunnerJarPath);
            is = SonarRunner.class.getResourceAsStream("jars/sonar-runner-2.0.jar");
            IOUtils.copy(is, os);
            return tmpSonarRunnerJarPath;
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }
}
