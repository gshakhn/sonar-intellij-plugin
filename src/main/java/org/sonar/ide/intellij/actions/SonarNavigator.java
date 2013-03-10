package org.sonar.ide.intellij.actions;

import com.intellij.ide.BrowserUtil;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.utils.SonarUtils;


public class SonarNavigator {

    protected static final String RESOURCE_PATH = "/dashboard/index/";

    public void navigateToDashboard(SonarModuleComponent sonarModuleComponent, String resourceId) {

        if (sonarModuleComponent.isConfigured()) {
            String url = generateUrl(sonarModuleComponent.getState().host, resourceId);
            BrowserUtil.launchBrowser(url);
        }
    }

    protected static String generateUrl(String host, String resourceId) {
        return SonarUtils.fixHostName(host) + RESOURCE_PATH + resourceId;
    }
}