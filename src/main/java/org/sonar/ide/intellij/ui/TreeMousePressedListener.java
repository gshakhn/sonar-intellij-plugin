package org.sonar.ide.intellij.ui;

import com.intellij.openapi.actionSystem.AnAction;
import org.jdesktop.swingx.JXTree;
import org.sonar.ide.intellij.model.SonarTreeModel;

import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeMousePressedListener extends MouseAdapter {
  private final JXTree tree;
  private final AnAction displayDescriptionAction;

  public TreeMousePressedListener(JXTree tree, AnAction displayDescriptionAction) {
    this.tree = tree;
    this.displayDescriptionAction = displayDescriptionAction;
  }
  @Override
  public void mousePressed(MouseEvent e) {
    TreePath p = tree.getPathForLocation(e.getX(), e.getY());
    if (p != null && !p.equals(tree.getSelectionModel().getSelectionPath()))
      tree.getSelectionModel().setSelectionPath(p);
    displayDescriptionAction.getTemplatePresentation().setVisible(p != null && p.getLastPathComponent() instanceof SonarTreeModel.RuleLabel);
  }
}
