package org.sonar.ide.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.sonar.wsclient.services.Violation;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class SonarTreeModel implements TreeModel {
  private List<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();

  private Map<VirtualFile, List<Violation>> violations;

  private Map<RuleLabel, List<FileLabel>> fileLabelsMap;

  private Map<FileLabel, List<ViolationLabel>> violationLabelsMap;

  List<RuleLabel> ruleLabels;

  private Map<RuleLabel, List<FileLabel>> virtualFiles;

  private String root = new String("ROOT");

  private boolean groupFiles = true;

  private boolean multipleFiles = true;

  public SonarTreeModel(boolean multipleFiles) {
    this.multipleFiles = multipleFiles;
    this.groupFiles = multipleFiles;
  }

  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent == root) {
      return ruleLabels.get(index);
    } else if (parent instanceof RuleLabel && groupFiles) {
      return fileLabelsMap.get(parent).get(index);
    } else if (parent instanceof FileLabel || (parent instanceof RuleLabel && !groupFiles)) {
      ViolationLabel violationLabel;
      if (groupFiles)
        violationLabel = violationLabelsMap.get(parent).get(index);
      else {
        List<ViolationLabel> allViolationLabels = new LinkedList<ViolationLabel>();
        for (FileLabel fileLabel : fileLabelsMap.get(parent)) {
          allViolationLabels.addAll(violationLabelsMap.get(fileLabel));
        }
        violationLabel = allViolationLabels.get(index);
      }
      violationLabel.setShowFileName(!groupFiles && multipleFiles);
      return violationLabel;
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent == root) {
      if (ruleLabels == null)
        return 0;
      return ruleLabels.size();
    } else if (parent instanceof RuleLabel && groupFiles) {
      return fileLabelsMap.get(parent).size();
    } else if (parent instanceof FileLabel || (parent instanceof RuleLabel && !groupFiles)) {
      if (groupFiles)
        return violationLabelsMap.get(parent).size();
      else {
        int size = 0;
        for (FileLabel fileLabel : fileLabelsMap.get(parent)) {
          size += violationLabelsMap.get(fileLabel).size();
        }
        return size;
      }
    }
    return 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    return node instanceof ViolationLabel;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent == root) {
      if (ruleLabels == null)
        return 0;
      return ruleLabels.indexOf(child);
    } else if (parent instanceof RuleLabel && groupFiles) {
      return fileLabelsMap.get(parent).indexOf(child);
    } else if (parent instanceof FileLabel || (parent instanceof RuleLabel && !groupFiles)) {
      if (groupFiles)
        return violationLabelsMap.get(parent).indexOf(child);
      else {
        List<ViolationLabel> allViolationLabels = new LinkedList<ViolationLabel>();
        for (FileLabel fileLabel : fileLabelsMap.get(parent)) {
          allViolationLabels.addAll(violationLabelsMap.get(fileLabel));
        }
        return allViolationLabels.indexOf(child);
      }
    }
    return 0;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    treeModelListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    treeModelListeners.remove(l);
  }

  public void setViolations(Map<VirtualFile, List<Violation>> violations) {
    ruleLabels = new LinkedList<RuleLabel>();
    fileLabelsMap = new HashMap<RuleLabel, List<FileLabel>>();
    violationLabelsMap = new HashMap<FileLabel, List<ViolationLabel>>();
    for (Map.Entry<VirtualFile, List<Violation>> entry : violations.entrySet()) {
      final VirtualFile file = entry.getKey();
      if (entry.getValue() == null)
        continue;
      for (Violation violation : entry.getValue()) {
        final String ruleName = violation.getRuleName();
        RuleLabel ruleLabel = (RuleLabel) CollectionUtils.find(ruleLabels, new Predicate() {
          @Override
          public boolean evaluate(Object o) {
            return o instanceof RuleLabel && ((RuleLabel) o).getRuleName().equals(ruleName);
          }
        });
        if (ruleLabel == null) {
          ruleLabel = new RuleLabel(ruleName, violation.getSeverity());
          ruleLabels.add(ruleLabel);
          fileLabelsMap.put(ruleLabel, new LinkedList<FileLabel>());
        }
        FileLabel fileLabel = (FileLabel) CollectionUtils.find(fileLabelsMap.get(ruleLabel), new Predicate() {
          @Override
          public boolean evaluate(Object o) {
            return o instanceof FileLabel && ((FileLabel) o).getVirtualFile().equals(file);
          }
        });
        if (fileLabel == null) {
          fileLabel = new FileLabel(file);
          fileLabelsMap.get(ruleLabel).add(fileLabel);
          violationLabelsMap.put(fileLabel, new LinkedList<ViolationLabel>());
        }
        violationLabelsMap.get(fileLabel).add(new ViolationLabel(violation, file, false));
      }
    }
    Collections.sort(ruleLabels, new Comparator<RuleLabel>() {
      List<String> types = Arrays.asList(new String[]{"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO"});

      @Override
      public int compare(RuleLabel o1, RuleLabel o2) {
        if (o1.toString().equals(o2.toString()))
          return 0;
        if (o1.getSeverity().equals(o2.getSeverity()))
          return o1.toString().compareTo(o2.toString());
        for (String type : types) {
          if (type.equals(o1.getSeverity()))
            return -1;
          if (type.equals(o2.getSeverity()))
            return 1;
        }
        return 0;
      }
    });

    for (TreeModelListener treeModelListener : treeModelListeners)
      treeModelListener.treeStructureChanged(new TreeModelEvent(getRoot(), new Object[]{getRoot()}));
  }

  public void setGroupFiles(boolean groupFiles) {
    this.groupFiles = groupFiles;
    for (TreeModelListener treeModelListener : treeModelListeners)
      treeModelListener.treeStructureChanged(new TreeModelEvent(getRoot(), new Object[]{getRoot()}));
  }

  public class ViolationLabel {
    private Violation violation;
    private VirtualFile virtualFile;
    private boolean showFileName;

    public ViolationLabel(Violation violation, VirtualFile virtualFile, boolean showFileName) {
      this.violation = violation;
      this.virtualFile = virtualFile;
      this.showFileName = showFileName;
    }

    public VirtualFile getVirtualFile() {
      return virtualFile;
    }

    public Integer getLine() {
      return violation.getLine();
    }

    public String toString() {
      return new StringBuilder()
          .append(showFileName ? virtualFile.getName() : "")
          .append(showFileName ? ":" : "")
          .append(violation.getLine())
          .append(": ")
          .append(violation.getMessage())
          .toString();
    }

    public void setShowFileName(boolean showFileName) {
      this.showFileName = showFileName;
    }
  }


  public class RuleLabel {
    private String ruleName;
    private String severity;

    public RuleLabel(String ruleName, String severity) {
      this.ruleName = ruleName;
      this.severity = severity;
    }

    public String toString() {
      return ruleName;
    }

    public String getRuleName() {
      return ruleName;
    }

    public String getSeverity() {
      return severity;
    }
  }

  public class FileLabel {
    private VirtualFile file;

    public FileLabel(VirtualFile file) {
      this.file = file;
    }

    public String toString() {
      return file.getName();
    }

    public VirtualFile getVirtualFile() {
      return file;
    }
  }
}
