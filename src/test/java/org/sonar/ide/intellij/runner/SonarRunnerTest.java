package org.sonar.ide.intellij.runner;


import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.IdeaTestCase;

public class SonarRunnerTest extends IdeaTestCase {


    public void testRunSonarClient() throws Exception {

        Sdk testProjectJdk = getTestProjectJdk();
        NewProjectUtil.applyJdkToProject(myProject, testProjectJdk);
        ProjectJdkTable.getInstance().addJdk(getTestProjectJdk());


        SonarRunnerMonitor sonarRunnerMonitor = new SonarRunnerMonitor();
        SonarRunnerJavaParameters sonarRunnerJavaParameters = new SonarRunnerJavaParameters();
        sonarRunnerJavaParameters.withProjectKey("pomodoro:pomodoro").runOnSource("src").withProjectVersion("1.0.2").withProjectName("pomodoro");
        new SonarRunner(sonarRunnerMonitor).runAnalysis(myProject,  sonarRunnerJavaParameters);

        sonarRunnerMonitor.dumpLogs(System.out);
        String next = sonarRunnerMonitor.getNotificatios().iterator().next();
        String [] parameters = next.split(" ");
        assertEquals(PathManager.getHomePath() + "/java/mockJDK-1.7/bin/java", parameters[0]);
        assertEquals("-Dfile.encoding=UTF-8", parameters[1]);
        assertEquals("-classpath", parameters[2]);
        assertTrue(parameters[3].matches(".*\\/sonar-runner.*\\.jar"));

        assertEquals("org.sonar.runner.Main", parameters[4]);
        assertEquals("-Dsonar.projectName=pomodoro", parameters[5]);
        assertEquals("-Dsonar.projectVersion=1.0.2", parameters[6]);
        assertEquals("-Dsonar.projectKey=pomodoro:pomodoro", parameters[7]);
        assertEquals("-Dsonar.sources=src", parameters[8].trim());
    }
}
