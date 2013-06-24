package org.sonar.ide.intellij.analysis.localanalysis;

import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONObject;
import org.sonar.wsclient.services.Violation;


public class WSViolationUnMarshaller {

    public static Violation unMarshallViolation(JSONObject jsonViolation) {

        Violation violation = new Violation();
        Long line = (Long) jsonViolation.get("line");
        violation.setLine(line != null ? line.intValue() : null);
        violation.setMessage(ObjectUtils.toString(jsonViolation.get("message")));
        violation.setSeverity(ObjectUtils.toString(jsonViolation.get("severity")));
        violation.setRuleKey(ObjectUtils.toString(jsonViolation.get("rule_repository") + ":" + jsonViolation.get("rule_key")));
        violation.setRuleName(ObjectUtils.toString(jsonViolation.get("rule_name")));

        return violation;
    }
}
