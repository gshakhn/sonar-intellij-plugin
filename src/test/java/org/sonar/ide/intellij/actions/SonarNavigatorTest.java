package org.sonar.ide.intellij.actions;

import junit.framework.Assert;
import org.junit.Test;
import org.sonar.ide.intellij.actions.SonarNavigator;

public class SonarNavigatorTest {

  @Test
  public void testUrlGeneratorWithoutSlash() {
    String url = SonarNavigator.generateUrl("localhost", "123");

    Assert.assertEquals("localhost/" + SonarNavigator.RESOURCE_PATH + "123", url);
  }

  @Test
  public void testUrlGeneratorWithSlash() {
    String url = SonarNavigator.generateUrl("localhost/", "123");

    Assert.assertEquals("localhost/" + SonarNavigator.RESOURCE_PATH + "123", url);
  }

}
