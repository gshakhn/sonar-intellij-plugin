package org.sonar.ide.intellij.analysis.localanalysis;


public class RunResult {

    private SonarLocalAnalysis localAnalysisAnalysis;

    private int exitCode;

    private Throwable error;

    private String message;

    public RunResult(SonarLocalAnalysis localAnalysisAnalysis, int exitCode) {
        this.localAnalysisAnalysis = localAnalysisAnalysis;
        this.exitCode = exitCode;
    }

    public RunResult(String message, Throwable cause) {
        this.message = message;
        this.error = cause;
        this.exitCode = 1;
    }

    public RunResult(String message, int errorCode) {
        this.exitCode = errorCode;
        this.message = message;
    }


    public SonarLocalAnalysis getLocalAnalysisAnalysis() {
        return localAnalysisAnalysis;
    }


    public String getMessage() {
        return (error == null) ? message : message + " " + error.getMessage();
    }

    public boolean isSuccess() {
        return (exitCode == 0);
    }
}
