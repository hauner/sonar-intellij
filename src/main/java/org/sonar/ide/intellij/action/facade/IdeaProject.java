package org.sonar.ide.intellij.action.facade;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.gradle.SonarModelSettings;


public class IdeaProject {
  private Project project;
  private ModuleManager moduleManager;

  public IdeaProject(Project project, ModuleManager moduleManager) {
    this.project = project;
    this.moduleManager = moduleManager;
  }

  public @Nullable String getSonarProjectName() {
    Module module = moduleManager.findModuleByName(project.getName());
    if (module == null) {
      return null;
    }

    return module.getComponent(SonarModelSettings.class).getSonarProjectName();
  }
}
