package org.sonar.ide.intellij.runner;

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;


public class SonarRunnerJavaParametersTest {

    public static final String A_PROJECT_KEY = "aKey";
    public static final String PROJECT_VERSION = "1";
    public static final String PROJECT_NAME = "sampleProject";
    public static final String RAW_HOST = "localhost";
    public static final String SOURCE_PATH = "src";
    public static final String USER = "test";
    public static final String PASSWORD = "password";

    private File sonarRunnerJar = new File(SonarRunner.class.getResource("jars/sonar-runner-2.0.jar").getFile());

    private SonarRunnerJavaParameters parameters = new SonarRunnerJavaParameters();

    private Map<String, String> expectedParameterList = buildExpectedParameterList();

    private Map<String, String> buildExpectedParameterList() {
        HashMap<String, String> stringStringHashMap = new HashMap<String, String>();

        stringStringHashMap.put("sonar.projectKey", A_PROJECT_KEY);
        stringStringHashMap.put("sonar.projectName", PROJECT_NAME);
        stringStringHashMap.put("sonar.projectVersion", PROJECT_VERSION);
        stringStringHashMap.put("sonar.sources", SOURCE_PATH);
        stringStringHashMap.put("sonar.dryRun", "true");
        stringStringHashMap.put("sonar.dryRun.export.path", "dryRun.json");
        stringStringHashMap.put("sonar.verbose", "true");
        stringStringHashMap.put("sonar.host.url", "http://" + RAW_HOST);
        stringStringHashMap.put("sonar.login", USER);
        stringStringHashMap.put("sonar.password", PASSWORD);
        return stringStringHashMap;
    }

    @Test
    public void testBuildRunnerParameters() throws Exception {

        JavaParameters javaParameters = parameters.withProjectKey(A_PROJECT_KEY).
                withProjectVersion(PROJECT_VERSION).
                withProjectName(PROJECT_NAME).
                withHost(RAW_HOST).
                runOnSource(SOURCE_PATH).
                withUserName(USER).
                withPassword(PASSWORD).buildJavaParameters(sonarRunnerJar);


        assertOnJavaParameters(javaParameters, expectedParameterList);

    }

    private void assertOnJavaParameters(JavaParameters javaParameters, Map<String, String> theExpectedParameterList) {
        assertNotNull(javaParameters);
        assertEquals(sonarRunnerJar.getAbsolutePath(), javaParameters.getClassPath().getPathsString());
        ParametersList programParametersList = javaParameters.getProgramParametersList();
        Set<Map.Entry<String, String>> entries = theExpectedParameterList.entrySet();
        for (Map.Entry<String, String> entry : entries) {

            String propertyValue = programParametersList.getPropertyValue(entry.getKey());
            assertEquals("Expecting same values for parameter key [" + entry.getKey() + "]", entry.getValue(), propertyValue);
        }
    }

    @Test
    public void testBuildRunnerParametersWithoutUserCredentials() throws Exception {
        JavaParameters javaParameters = parameters.withProjectKey(A_PROJECT_KEY).
                withProjectVersion(PROJECT_VERSION).
                withProjectName(PROJECT_NAME).
                withHost(RAW_HOST).
                runOnSource(SOURCE_PATH).buildJavaParameters(sonarRunnerJar);

        expectedParameterList.remove("sonar.password");
        expectedParameterList.remove("sonar.login");
        assertOnJavaParameters(javaParameters, expectedParameterList);
    }

    @Test
    public void testBuildWithIncompleteInput() throws Exception {
        buildAndExpectToFail(parameters);
        buildAndExpectToFail(parameters.withProjectName("A"));
        buildAndExpectToFail(parameters.withProjectVersion("1"));
        buildAndExpectToFail(parameters.runOnSource("src"));
        buildAndExpectToFail(parameters.withHost("localhost"));
        JavaParameters javaParameters = parameters.withProjectKey("abc:abc").buildJavaParameters(sonarRunnerJar);
        assertNotNull(javaParameters);

    }

    private void buildAndExpectToFail(SonarRunnerJavaParameters parameters) {
        try {
            parameters.buildJavaParameters(sonarRunnerJar);
            fail("Expecting error because of incomplete parameter list");
        } catch (IllegalArgumentException e) {
            // let assume to pass but would be more perfect to check the error message
        }
    }
}
