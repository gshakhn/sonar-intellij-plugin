package org.sonar.ide.intellij.component;


import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.IdeaTestCase;
import org.sonar.ide.intellij.runner.SonarRunner;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;
import org.sonar.ide.intellij.runner.SonarRunnerJavaParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SonarRunnerTest extends IdeaTestCase {


    private SonarRunnerJavaParameters sonarRunnerJavaParameters;

    public void setUp() throws Exception {
        super.setUp();
        sonarRunnerJavaParameters = new SonarRunnerJavaParameters();
        sonarRunnerJavaParameters.withProjectKey("pomodoro:pomodoro").
                runOnSource("src").withProjectVersion("1.0.2").withProjectName("pomodoro").withHost("localhost");
    }

    public void testSonarRunnerExecutionParameters() throws Exception {

        Sdk testProjectJdk = getTestProjectJdk();
        NewProjectUtil.applyJdkToProject(myProject, testProjectJdk);
        ProjectJdkTable.getInstance().addJdk(getTestProjectJdk());


        SonarRunnerConsoleMonitor sonarRunnerMonitor = new SonarRunnerConsoleMonitor();

        new SonarRunner(sonarRunnerMonitor).runAnalysis(myProject, sonarRunnerJavaParameters);

        sonarRunnerMonitor.dumpLogs(System.out);
        String next = sonarRunnerMonitor.getNotifications().iterator().next();
        List<String> parameters = new ArrayList<String>(Arrays.asList(next.trim().split(" ")));

        assertEquals(PathManager.getHomePath() + "/java/mockJDK-1.7/bin/java", parameters.get(0));
        assertEquals("-Dfile.encoding=UTF-8", parameters.get(1));
        assertEquals("-classpath", parameters.get(2));
        assertTrue(parameters.get(3).matches(".*\\/sonar-runner.*\\.jar"));

        assertEquals("org.sonar.runner.Main", parameters.get(4));
        assertTrue(parameters.contains("-Dsonar.projectName=pomodoro"));
        assertTrue(parameters.contains("-Dsonar.projectVersion=1.0.2"));
        assertTrue(parameters.contains("-Dsonar.projectKey=pomodoro:pomodoro"));
        assertTrue(parameters.contains("-Dsonar.sources=src"));
    }

    public void testRunSonarRunnerWithNoJDKConfigured() throws Exception {
        try {
            SonarRunnerConsoleMonitor sonarRunnerMonitor = new SonarRunnerConsoleMonitor();

            new SonarRunner(sonarRunnerMonitor).runAnalysis(myProject, sonarRunnerJavaParameters);
            fail();
        } catch (SonarRunner.SonarRunnerException e) {
            assertEquals("Error running sonar runner check log files", e.getMessage());
        }

    }
}
