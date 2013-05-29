package org.sonar.ide.intellij.listener;


import org.sonar.wsclient.services.Rule;

import java.util.List;

public interface RefreshRuleListener {
    void doneRefreshRules(List<Rule> rules);
}
