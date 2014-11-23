package org.sonar.ide.intellij.action.associator.facades;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.gradle.SonarModelSettings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IdeaProjectTest {
  Module module;
  Project project;
  ModuleManager moduleManager;
  SonarModelSettings sonarSettings;

  @Before
  public void setup() {
    module = mock(Module.class);
    project = mock(Project.class);
    moduleManager = mock(ModuleManager.class);
    sonarSettings = mock(SonarModelSettings.class);
  }

  @Test
  public void shouldGetSonarProjectName() {
    String rootModuleName = "Root Module Name";
    String sonarProjectName = "Sonar Project Name";
    when(project.getName()).thenReturn(rootModuleName);
    when(moduleManager.findModuleByName(rootModuleName)).thenReturn(module);
    when(module.getComponent(SonarModelSettings.class)).thenReturn(sonarSettings);
    when(sonarSettings.getSonarProjectName()).thenReturn(sonarProjectName);

    IdeaProject ideaProject = new IdeaProject(project, moduleManager);

    assertThat(ideaProject.getSonarProjectName(), is (sonarProjectName));
  }

  @Test
  public void shouldGetSonarProjectNameWithoutModule() {
    String rootModuleName = "Root Module Name";
    when(project.getName()).thenReturn(rootModuleName);
    when(moduleManager.findModuleByName(rootModuleName)).thenReturn(null);

    IdeaProject ideaProject = new IdeaProject(project, moduleManager);

    assertThat(ideaProject.getSonarProjectName(), is (nullValue()));
  }
}
