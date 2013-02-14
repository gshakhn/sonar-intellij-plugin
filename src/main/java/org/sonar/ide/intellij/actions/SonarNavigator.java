package org.sonar.ide.intellij.actions;

import com.intellij.ide.BrowserUtil;
import org.sonar.ide.intellij.component.SonarModuleComponent;


public class SonarNavigator {

  protected static final String RESOURCE_PATH = "dashboard/index/";

  public void navigateToDashboard(SonarModuleComponent sonarModuleComponent, String resourceId) {

    if (sonarModuleComponent.isConfigured()) {
      String url = generateUrl(sonarModuleComponent.getState().host, resourceId);
      BrowserUtil.launchBrowser(url);
    }
  }

  protected static String generateUrl(String host, String resourceId) {
    if (!host.endsWith("/")) {
      host += "/";
    }
    return host + RESOURCE_PATH + resourceId;
  }
}