package org.sonar.ide.intellij.runner;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;


public class SonarRunner {

    public static final String SONAR_RUNNER_MAIN_CLASS = "org.sonar.runner.Main";

    private ProcessListener sonarRunnerMonitor;

    class SonarRunnerException extends RuntimeException {

        SonarRunnerException(String s) {
            super(s);
        }

        SonarRunnerException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }

    public SonarRunner(ProcessListener sonarRunnerMonitor) {
        this.sonarRunnerMonitor = sonarRunnerMonitor;
    }

    public File runAnalysis(Project project, SonarRunnerJavaParameters runParameters) {

        File sonarAnalyser = null;
        try {
            sonarAnalyser = unpackSonarRunner();
            executeSonarRunner(project, runParameters, sonarAnalyser);

            return new File(project.getBasePath(), runParameters.getResultFile());
        } catch (IOException e) {
            throw new SonarRunnerException("Error extracting sonar runner", e);
        } finally {
            FileUtils.deleteQuietly(sonarAnalyser);
        }
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
            osProcessHandler.waitFor();

        } catch (ExecutionException e) {
            throw new SonarRunnerException("Error running sonar runner", e);
        } catch (RuntimeException e) {
            throw new SonarRunnerException("Error running sonar runner", e);
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
