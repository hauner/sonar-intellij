package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.action.facade.IdeaProject;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public class GradleAssociator implements SonarQubeAssociator {
  private Project project;
  private ProjectSettings settings;
  private ModuleManager moduleManager;

  public GradleAssociator(Project project, ProjectSettings settings, ModuleManager moduleManager) {
    this.project = project;
    this.settings = settings;
    this.moduleManager = moduleManager;
  }

  @Override
  public String getProjectName() {
    Module projectModule = moduleManager.findModuleByName(project.getName());
    if (projectModule == null) {
      return null;
    }

    SonarModelSettings settings = projectModule.getComponent(SonarModelSettings.class);
    return settings.getSonarProjectName();
  }

  @Override
  public void associate(@NotNull ISonarRemoteProject sonarProject) {
    settings.setServerId(sonarProject.getServer().getId());
    settings.setProjectKey(sonarProject.getKey());

    settings.getModuleKeys().clear();

    Module[] modules = moduleManager.getModules();
    for (Module module : modules) {
      settings.getModuleKeys().put(module.getName(), sonarProject.getKey());
    }
  }
}
