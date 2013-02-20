package org.sonar.ide.intellij.utils;

import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONObject;
import org.sonar.wsclient.services.Violation;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 2/19/13
 * Time: 12:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class WSViolationUnMarshaller {

    public static Violation unMarshallViolation(JSONObject jsonViolation) {

        Violation violation = new Violation();
        Long line = (Long) jsonViolation.get("line");
        violation.setLine(line != null ? line.intValue() : null);
        violation.setMessage(ObjectUtils.toString(jsonViolation.get("message")));
        violation.setSeverity(ObjectUtils.toString(jsonViolation.get("severity")));
        violation.setRuleKey(ObjectUtils.toString(jsonViolation.get("rule_key")));
        violation.setRuleName(ObjectUtils.toString(jsonViolation.get("rule_name")));

        return violation;
    }
}
