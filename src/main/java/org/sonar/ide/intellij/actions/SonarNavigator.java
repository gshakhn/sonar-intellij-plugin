package org.sonar.ide.intellij.actions;

import com.intellij.ide.BrowserUtil;
import org.sonar.ide.intellij.component.SonarModuleComponent;


public class SonarNavigator {

    public void navigateToDashboard(SonarModuleComponent sonarModuleComponent, String resourceId) {

        if (sonarModuleComponent.isConfigured()) {

            String url = sonarModuleComponent.getState().host + "/dashboard/index/" + resourceId;
            BrowserUtil.launchBrowser(url);

        }
    }
}