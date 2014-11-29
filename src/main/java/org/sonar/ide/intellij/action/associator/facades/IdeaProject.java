package org.sonar.ide.intellij.action.associator.facades;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;


public class IdeaProject {
  private Project project;
  private ProjectSettings settings;
  private ModuleManager moduleManager;

  public IdeaProject(Project project, ProjectSettings settings, ModuleManager moduleManager) {
    this.project = project;
    this.settings = settings;
    this.moduleManager = moduleManager;
  }

  public String getName() {
    return project.getName();
  }

  public String getSonarProjectName() {
    Module module = moduleManager.findModuleByName(project.getName());
    if (module == null) {
      return null;
    }

    return module.getComponent(SonarModelSettings.class).getSonarProjectName();
  }

  public void setSonarServerId(String serverId) {
    settings.setServerId(serverId);
  }

  public void setSonarProjectKey(String sonarProjectKey) {
    settings.setProjectKey(sonarProjectKey);
  }

  public Module[] getModules() {
    return moduleManager.getModules();
  }

  public void addSonarModuleAssociation(String ideaModuleName, String sonarModuleKey) {
    settings.getModuleKeys().put(ideaModuleName, sonarModuleKey);
  }

  public void clearSonarModuleAssociations() {
    settings.getModuleKeys().clear();
  }
}
