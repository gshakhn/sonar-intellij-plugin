package org.sonar.ide.intellij.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class VersionTest {

    @Test
    public void testCreateVersion() throws Exception {
        Version version = new Version(1, 2, 0);
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getIncrementalVersion());
    }

    @Test
    public void testWrongVersionNumbers() throws Exception {

        parseAndExpectError("1.", "Invalid version number expecting xx.xx.xx, but got [1.]");
        parseAndExpectError(".2.1", "Version number elements must be a positive numeric value, but was []");
        parseAndExpectError("1..1", "Version number elements must be a positive numeric value, but was []");
        parseAndExpectError("1.a.1", "Version number elements must be a positive numeric value, but was [a]");
        parseAndExpectError("a.2.1", "Version number elements must be a positive numeric value, but was [a]");
        parseAndExpectError("1.2.c", "Version number elements must be a positive numeric value, but was [c]");
        parseAndExpectError("1.2.-3", "Version number elements must be a positive numeric value, but was [-3]");
        parseAndExpectError("1.-2.-3", "Version number elements must be a positive numeric value, but was [-3]");
    }

    private void parseAndExpectError(String version, String expectedMessage) {
        try {
            Version.parse(version);
            fail();
        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testParseCorrectVersion() throws Exception {
        assertEquals(new Version(1, 2, 0), Version.parse("1.2"));
        assertEquals(new Version(1, 2, 0), Version.parse("1.2.0"));
        assertEquals(new Version(2, 1, 1), Version.parse("2.1.1"));
        assertEquals(new Version(2, 1), Version.parse("2.1.0"));
        assertEquals(new Version(2, 1), Version.parse("2.1.0"));
    }

    @Test
    public void testTestCompareVersionsAreBelow() throws Exception {
        assertTrue(new Version(1, 2, 0).compareTo(new Version(1, 2, 1)) < 0);
        assertTrue(new Version(1, 2, 0).compareTo(new Version(2, 1, 1)) < 0);
        assertTrue(new Version(1, 2, 0).compareTo(new Version(2, 0, 0)) < 0);
        assertTrue(new Version(1, 2, 0).compareTo(new Version(1, 3, 0)) < 0);
    }

    @Test
    public void testTestCompareVersionsAreAbove() throws Exception {
        assertTrue(new Version(1, 2, 1).compareTo(new Version(1, 2, 0)) > 0);
        assertTrue(new Version(2, 1, 1).compareTo(new Version(1, 2, 0)) > 0);
        assertTrue(new Version(2, 0, 0).compareTo(new Version(1, 2, 0)) > 0);
        assertTrue(new Version(1, 3, 0).compareTo(new Version(1, 2, 0)) > 0);
    }

    @Test
    public void testEquality() throws Exception {
        assertTrue(new Version(1, 2, 1).compareTo(new Version(1, 2, 1)) == 0);
        assertTrue(new Version(2, 1, 0).compareTo(new Version(2, 1)) == 0);
        assertTrue(new Version(2, 2, 0).compareTo(new Version(2, 2)) == 0);


    }
}

