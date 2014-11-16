package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import static com.intellij.openapi.externalSystem.util.ExternalSystemConstants.*;
import static org.jetbrains.plugins.gradle.util.GradleConstants.SYSTEM_ID;


public class GradleProjectManager {
  private Project project;
  private ModuleManager moduleManager;

  public GradleProjectManager(Project project, ModuleManager moduleManager) {
    this.project = project;
    this.moduleManager = moduleManager;
  }

  public boolean isGradleModule(Module module) {
    return SYSTEM_ID.toString().equals(module.getOptionValue(EXTERNAL_SYSTEM_ID_KEY));
  }

  public boolean isGradleProject() {
    Module projectModule = moduleManager.findModuleByName(project.getName());
    return projectModule != null && isGradleModule(projectModule);
  }
}
