package org.sonar.ide.intellij.ui;

import com.intellij.openapi.util.IconLoader;
import org.sonar.ide.intellij.model.SonarTreeModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ViolationCellRenderer extends DefaultTreeCellRenderer {
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean sel,
                                                boolean expanded,
                                                boolean leaf, int row,
                                                boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    String path = null;
    if (value instanceof SonarTreeModel.RuleLabel) {
      SonarTreeModel.RuleLabel ruleLabel = (SonarTreeModel.RuleLabel) value;
      path = "/icons/" + ruleLabel.getSeverity() + ".png";
    }
    if (path != null) {
      this.setIcon(IconLoader.getIcon(path));
    }
    return this;
  }
}
