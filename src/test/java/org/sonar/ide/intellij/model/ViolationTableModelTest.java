package org.sonar.ide.intellij.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.Violation;

import java.util.ArrayList;
import java.util.List;

public class ViolationTableModelTest {

    ViolationTableModel model;

    @Before
    public void setUp() {
        model = new ViolationTableModel();
    }

    @Test
    public void testColumnNames() {
        Assert.assertEquals("Severity", model.getColumnName(0));
        Assert.assertEquals("Rule Name", model.getColumnName(1));
        Assert.assertEquals("Line Number", model.getColumnName(2));
        Assert.assertEquals("Line", model.getColumnName(3));
        Assert.assertEquals("Message", model.getColumnName(4));
        Assert.assertEquals("?", model.getColumnName(-1));
        Assert.assertEquals("?", model.getColumnName(5));
    }

    @Test
    public void testColumnClass() {
        Assert.assertEquals(String.class, model.getColumnClass(0));
        Assert.assertEquals(String.class, model.getColumnClass(1));
        Assert.assertEquals(Integer.class, model.getColumnClass(2));
        Assert.assertEquals(String.class, model.getColumnClass(3));
        Assert.assertEquals(String.class, model.getColumnClass(4));
    }

    @Test
    public void testColumnCount() {
        Assert.assertEquals(5, model.getColumnCount());
    }

    @Test
    public void testRowCount() {
        VirtualFile file = new LightVirtualFile("file.txt");
        List<Violation> violations = new ArrayList<Violation>();
        model.setViolations(file, violations);

        Assert.assertEquals(0, model.getRowCount());

        violations.add(new Violation());
        Assert.assertEquals(1, model.getRowCount());

        violations.add(new Violation());
        Assert.assertEquals(2, model.getRowCount());
    }

    @Test
    public void testCurrentVirtualFile() {
        VirtualFile file1 = new LightVirtualFile("file1.txt");
        VirtualFile file2 = new LightVirtualFile("file1.txt");

        Assert.assertNull(model.getCurrentVirtualFile());

        model.setViolations(file1, new ArrayList<Violation>());
        Assert.assertEquals(file1, model.getCurrentVirtualFile());

        model.setSource(file2, new Source());
        Assert.assertEquals(file2, model.getCurrentVirtualFile());
    }

    @Test
    public void settingViolationsOrSourceWithADifferentVirtualFileResetsOther() {
        VirtualFile file1 = new LightVirtualFile("file1.txt");
        VirtualFile file2 = new LightVirtualFile("file1.txt");

        ArrayList<Violation> violations = new ArrayList<Violation>();
        violations.add(new Violation());
        model.setViolations(file1, violations);
        Assert.assertSame(violations, model.violations);
        Assert.assertNull(model.source);

        Source source = new Source();
        model.setSource(file2, source);
        Assert.assertNotSame(violations, model.violations);
        Assert.assertSame(source, model.source);
    }

    @Test
    public void testValueAt() {
        VirtualFile file = new LightVirtualFile("file.txt");

        // Add violations
        Violation violation1 = new Violation();
        violation1.setSeverity("Major");
        violation1.setRuleName("A rule");
        violation1.setLine(null);
        violation1.setMessage("This violation has no line associated with it");

        Violation violation2 = new Violation();
        violation2.setSeverity("Minor");
        violation2.setRuleName("Another rule");
        violation2.setLine(10);
        violation2.setMessage("This violation has a line associated with it");

        List<Violation> violations = new ArrayList<Violation>();
        violations.add(violation1);
        violations.add(violation2);

        model.setViolations(file, violations);

        Assert.assertEquals(model.getValueAt(0, 0), "Major");
        Assert.assertEquals(model.getValueAt(0, 1), "A rule");
        Assert.assertEquals(model.getValueAt(0, 2), null);
        Assert.assertEquals(model.getValueAt(0, 3), "UNKNOWN");
        Assert.assertEquals(model.getValueAt(0, 4), "This violation has no line associated with it");

        Assert.assertEquals(model.getValueAt(1, 0), "Minor");
        Assert.assertEquals(model.getValueAt(1, 1), "Another rule");
        Assert.assertEquals(model.getValueAt(1, 2), 10);
        Assert.assertEquals(model.getValueAt(1, 3), "UNKNOWN");
        Assert.assertEquals(model.getValueAt(1, 4), "This violation has a line associated with it");


        // Add source code
        Source source = new Source();
        source.addLine(10, "x = 123");

        model.setSource(file, source);

        Assert.assertEquals(model.getValueAt(0, 0), "Major");
        Assert.assertEquals(model.getValueAt(0, 1), "A rule");
        Assert.assertEquals(model.getValueAt(0, 2), null);
        Assert.assertEquals(model.getValueAt(0, 3), "UNKNOWN");
        Assert.assertEquals(model.getValueAt(0, 4), "This violation has no line associated with it");

        Assert.assertEquals(model.getValueAt(1, 0), "Minor");
        Assert.assertEquals(model.getValueAt(1, 1), "Another rule");
        Assert.assertEquals(model.getValueAt(1, 2), 10);
        Assert.assertEquals(model.getValueAt(1, 3), "x = 123");
        Assert.assertEquals(model.getValueAt(1, 4), "This violation has a line associated with it");
    }
}
