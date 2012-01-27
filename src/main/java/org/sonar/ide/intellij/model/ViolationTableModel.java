package org.sonar.ide.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ViolationTableModel extends AbstractTableModel {

  private List<Violation> violations;
  private VirtualFile currentVirtualFile;
  private Source source;

  public ViolationTableModel() {
    reset();
  }

  public void reset() {
    this.violations = new ArrayList<Violation>();
    this.source = null;
    this.currentVirtualFile = null;
  }

  public void setViolations(VirtualFile virtualFile, List<Violation> newViolations) {
    if (!virtualFile.equals(this.currentVirtualFile)) {
      reset();
    }

    this.currentVirtualFile = virtualFile;
    this.violations = newViolations;

    fireTableDataChanged();
  }

  public void setSource(VirtualFile virtualFile, Source newSource) {
    if (!virtualFile.equals(this.currentVirtualFile)) {
      reset();
    }

    this.currentVirtualFile = virtualFile;
    this.source = newSource;

    fireTableDataChanged();
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Severity";
      case 1:
        return "Rule Name";
      case 2:
        return "Line Number";
      case 3:
        return "Line";
      case 4:
        return "Message";
      default:
        return "?";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnIndex == 2 ? Integer.class : String.class;
  }

  @Override
  public int getRowCount() {
    return violations.size();
  }

  @Override
  public int getColumnCount() {
    return 5;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Violation violation = violations.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return violation.getSeverity();
      case 1:
        return violation.getRuleName();
      case 2:
        return violation.getLine();
      case 3:
        if (this.source == null) {
          return "UNKNOWN";
        } else {
          Integer lineNumber = violation.getLine();
          return this.source.getLine(lineNumber);
        }
      case 4:
        return violation.getMessage();
      default:
        return "";
    }
  }

  public Violation getViolation(int index) {
    return violations.get(index);
  }

  public VirtualFile getCurrentVirtualFile() {
    return this.currentVirtualFile;
  }
}
