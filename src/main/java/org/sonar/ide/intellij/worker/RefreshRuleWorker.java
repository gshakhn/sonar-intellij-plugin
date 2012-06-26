package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.RefreshRuleListener;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.services.RuleQuery;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RefreshRuleWorker extends SwingWorker<List<Rule>, Void> {
  private Project project;

  private List<RefreshRuleListener> listeners = new LinkedList<RefreshRuleListener>();

  public RefreshRuleWorker(Project project) {
    this.project = project;
  }

  @Override
  protected List<Rule> doInBackground() throws Exception {
    SonarProjectComponent component = project.getComponent(SonarProjectComponent.class);
    Sonar sonar = component.getSonar();
    return sonar.findAll(new RuleQuery("java"));
  }

  @Override
  protected void done() {
    try {
      List<Rule> rules = get();
      for (RefreshRuleListener listener : listeners)
        listener.doneRefreshRules(rules);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

  public void addListener(RefreshRuleListener listener) {
    listeners.add(listener);
  }

}
