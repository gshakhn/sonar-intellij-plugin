package org.sonar.ide.intellij.model;

public class ToolWindowModel {
  private ViolationTableModel violationTableModel;

  public ToolWindowModel(ViolationTableModel violationTableModel) {
    this.violationTableModel = violationTableModel;
  }

  public ViolationTableModel getViolationTableModel() {
    return violationTableModel;
  }
}
