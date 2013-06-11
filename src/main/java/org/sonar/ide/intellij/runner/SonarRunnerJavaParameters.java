package org.sonar.ide.intellij.runner;


import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import org.apache.commons.lang.StringUtils;
import org.sonar.ide.intellij.utils.SonarUtils;

import java.io.File;
import java.util.Properties;

public class SonarRunnerJavaParameters {

    public static final String DRY_RUN_JSON = "dryRun.json";

    private Properties runProperties;
    private String projectName;
    private String projectVersion;
    private String source;
    private String projectKey;
    private String user;
    private String password;
    private String host;


    private Properties buildRunProperties() {

        runProperties = new Properties();
        nullSafePut("sonar.projectKey", this.projectKey);
        nullSafePut("sonar.projectName", this.projectName);
        nullSafePut("sonar.projectVersion", this.projectVersion);
        nullSafePut("sonar.sources", this.source);
        nullSafePut("sonar.host.url", host);
        runProperties.put("sonar.dryRun", "true");
        runProperties.put("sonar.dryRun.export.path", DRY_RUN_JSON);
        runProperties.put("sonar.verbose", "true");

        if (StringUtils.isNotBlank(user)) {
            runProperties.put("sonar.login", user);
            runProperties.put("sonar.password", password);
        }


        return runProperties;
    }

    private void nullSafePut(String key, String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Parameter [" + key + "] is not set");
        }
        runProperties.put(key, value);
    }

    public SonarRunnerJavaParameters withProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public SonarRunnerJavaParameters withProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
        return this;
    }

    public SonarRunnerJavaParameters runOnSource(String source) {
        this.source = source;
        return this;
    }

    public SonarRunnerJavaParameters withProjectKey(String projectKey) {
        this.projectKey = projectKey;

        return this;
    }

    public JavaParameters buildJavaParameters(File sonarRunnerJar) {

        if (!sonarRunnerJar.exists()) {
            throw new IllegalArgumentException("Sonar runner is not available at [" + sonarRunnerJar.getAbsolutePath() + "]");
        }

        JavaParameters javaParameters = new JavaParameters();
        javaParameters.getClassPath().add(sonarRunnerJar);

        appendMandatoryParameters(javaParameters, buildRunProperties());

        return javaParameters;
    }

    private void appendMandatoryParameters(JavaParameters javaParameters, Properties runProperties) {
        ParametersList programParametersList = javaParameters.getProgramParametersList();
        for (String key : runProperties.stringPropertyNames()) {
            programParametersList.add("-D" + key + "=" + runProperties.getProperty(key));
        }
    }

    public String getResultFile() {
        return ".sonar/" + DRY_RUN_JSON;
    }


    public SonarRunnerJavaParameters withUserName(String user) {
        this.user = user;

        return this;
    }

    public SonarRunnerJavaParameters withPassword(String password) {
        this.password = password;
        return this;
    }

    public SonarRunnerJavaParameters withHost(String host) {
        this.host = SonarUtils.fixHostName(host);

        return this;
    }
}
