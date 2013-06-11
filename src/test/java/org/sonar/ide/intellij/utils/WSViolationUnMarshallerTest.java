package org.sonar.ide.intellij.utils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.ide.intellij.analysis.localanalysis.WSViolationUnMarshaller;
import org.sonar.wsclient.services.Violation;

public class WSViolationUnMarshallerTest {

    private static final String SAMPLE_JSON_STRING = "{\"line\":39,\"message\":\"Name 'pomodoroIcon' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.\",\"severity\":\"MINOR\",\"rule_key\":\"com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck\",\"rule_repository\":\"checkstyle\",\"rule_name\":\"Constant Name\"}";

    @Test
    public void testUnmarshallViolation() throws Exception {

        Violation violation = WSViolationUnMarshaller.unMarshallViolation((JSONObject) JSONValue.parse(SAMPLE_JSON_STRING));
        assertViolationData(violation, 39, "Name 'pomodoroIcon' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.", "MINOR", "checkstyle:com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck", "Constant Name");
    }

    private void assertViolationData(Violation violation, Integer lineNumber, String message, String severity, String resourceKey, String constantName) {
        Assert.assertEquals(lineNumber, violation.getLine());
        Assert.assertEquals(message, violation.getMessage());
        Assert.assertEquals(severity, violation.getSeverity());
        Assert.assertEquals(resourceKey, violation.getRuleKey());
        Assert.assertEquals(constantName, violation.getRuleName());
    }

    @Test
    public void testUnmarshallEmptyViolation() throws Exception {
        Violation violation = WSViolationUnMarshaller.unMarshallViolation((JSONObject) JSONValue.parse("{}"));
        Assert.assertNotNull(violation);
        assertViolationData(violation, null, "", "", "null:null", "");
    }
}
