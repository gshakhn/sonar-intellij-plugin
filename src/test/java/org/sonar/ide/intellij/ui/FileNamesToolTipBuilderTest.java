package org.sonar.ide.intellij.ui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FileNamesToolTipBuilderTest {

    private final FileNamesToolTipBuilder builder = new FileNamesToolTipBuilder();
    private List<VirtualFile> virtualFiles = new ArrayList<VirtualFile>();

    @Test
    public void testGenerateToolTipFromEmptyList() {
        Assert.assertEquals("", builder.generateToolTip(virtualFiles));
    }

    @Test
    public void testGenerateToolTipFromListWithOneNullElement() {
        Assert.assertEquals("", builder.generateToolTip(virtualFiles));
    }

    @Test
    public void testGenerateToolTipFromListWithOneRealElement() {
        virtualFiles.add(new LightVirtualFile("first.txt"));
        Assert.assertEquals("Loading data for\nfirst.txt", builder.generateToolTip(virtualFiles));
    }

    @Test
    public void testGenerateToolTipFromListWithMoreRealElements() {
        virtualFiles.add(new LightVirtualFile("first.txt"));
        virtualFiles.add(null); // no idea how THIS can happen
        virtualFiles.add(new LightVirtualFile("second.txt"));
        virtualFiles.add(new LightVirtualFile("third.txt"));
        Assert.assertEquals("Loading data for\nfirst.txt\nsecond.txt\nthird.txt", builder.generateToolTip(virtualFiles));
    }

}
