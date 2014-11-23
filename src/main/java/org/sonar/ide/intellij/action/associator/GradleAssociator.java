package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.action.associator.facades.IdeaProject;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public class GradleAssociator implements SonarQubeAssociator {
  private ProjectSettings settings;
  private ModuleManager moduleManager;
  private IdeaProject ideaProject;

  public GradleAssociator(Project project, ProjectSettings settings, ModuleManager moduleManager) {
    this.settings = settings;
    this.moduleManager = moduleManager;
  }

  public GradleAssociator(IdeaProject ideaProject) {
    this.ideaProject = ideaProject;
  }

  @Override
  public String getSonarProjectName() {
    return ideaProject.getSonarProjectName();
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

  @Override
  public void associate(@NotNull SonarProject sonarProject) {
    associate(sonarProject.getProject());
  }
}
