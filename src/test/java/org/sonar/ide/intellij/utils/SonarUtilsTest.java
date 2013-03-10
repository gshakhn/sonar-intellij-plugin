package org.sonar.ide.intellij.utils;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SonarUtilsTest {

    @Test
    public void testProtocolIsAdded() throws Exception {
        assertEquals("http://localhost:8080", SonarUtils.fixHostName("localhost:8080"));
    }

    @Test
    public void testExtraEndSlashIsRemoved() throws Exception {
        assertEquals("http://localhost:8080", SonarUtils.fixHostName("http://localhost:8080/"));
    }

    @Test
    public void testProtocolIsPreserved() throws Exception {
        assertEquals("abc://localhost:8080", SonarUtils.fixHostName("abc://localhost:8080/"));
    }
}
