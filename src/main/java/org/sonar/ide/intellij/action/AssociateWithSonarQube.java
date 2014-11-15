/*
 * SonarQube IntelliJ
 * Copyright (C) 2013-2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.ide.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.sonar.ide.intellij.associate.AssociateDialog;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.console.SonarQubeConsole;
import org.sonar.ide.intellij.util.SonarQubeBundle;
import org.sonar.ide.intellij.wsclient.WSClientFactory;


public class AssociateWithSonarQube extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    Project p = e.getProject();
    if (p != null) {
      MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(p);
      ProjectSettings settings = p.getComponent(ProjectSettings.class);
      AssociateDialog dialog = new AssociateDialog(p, settings.isAssociated());
      SonarQubeAssociator associator = new SonarQubeAssociator(p, settings, mavenProjectsManager,
          ModuleManager.getInstance(p), SonarQubeConsole.getSonarQubeConsole(p), WSClientFactory.getInstance());

      SonarQubeAction action = new SonarQubeAction(p, settings, mavenProjectsManager, dialog, associator);
      action.associate(getProjectName(p));
    }
  }

  @Override
  public void update(AnActionEvent e) {
    Project p = e.getProject();
    if (p != null) {
      ProjectSettings settings = p.getComponent(ProjectSettings.class);
      if (!settings.isAssociated()) {
        e.getPresentation().setText(SonarQubeBundle.message("sonarqube.associate.action.label"));
      } else {
        e.getPresentation().setText(SonarQubeBundle.message("sonarqube.associate.action.label_update"));
      }
    }
  }

  private String getProjectName(Project project) {
    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);

    if (mavenProjectsManager.isMavenizedProject() && mavenProjectsManager.hasProjects()) {
      return mavenProjectsManager.getRootProjects().get(0).getDisplayName();
    }

    return null;
  }
}
