package org.sonar.ide.intellij.action.associator.facades;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IdeaProjectTest {
  Module module;
  Project project;
  ProjectSettings settings;
  ModuleManager moduleManager;
  SonarModelSettings sonarSettings;

  @Before
  public void setup() {
    module = mock(Module.class);
    project = mock(Project.class);
    settings = new ProjectSettings();
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

    IdeaProject ideaProject = new IdeaProject(project, settings, moduleManager);

    assertThat(ideaProject.getSonarProjectName(), is (sonarProjectName));
  }

  @Test
  public void shouldGetSonarProjectNameWithoutModule() {
    String rootModuleName = "Root Module Name";
    when(project.getName()).thenReturn(rootModuleName);
    when(moduleManager.findModuleByName(rootModuleName)).thenReturn(null);

    IdeaProject ideaProject = new IdeaProject(project, settings, moduleManager);

    assertThat(ideaProject.getSonarProjectName(), is (nullValue()));
  }

  @Test
  public void shouldSetSonarServerId() {
    String serverId = "server id";
    IdeaProject ideaProject = new IdeaProject(null, settings, null);

    ideaProject.setSonarServerId(serverId);

    assertThat(settings.getServerId(), is (serverId));
  }

  @Test
  public void shouldSetSonarProjectKey() {
    String sonarProjectKey = "project key";
    IdeaProject ideaProject = new IdeaProject(null, settings, null);

    ideaProject.setSonarProjectKey(sonarProjectKey);

    assertThat(settings.getProjectKey(), is(sonarProjectKey));
  }

  @Test
  public void shouldGetName() {
    String projectName = "Project Name";
    when(project.getName()).thenReturn(projectName);

    IdeaProject ideaProject = new IdeaProject(project, null, null);

    assertThat(ideaProject.getName(), is(projectName));
  }

  @Test
  public void shouldAddSonarModuleAssociation() {
    String ideaModuleName = "Idea Module Name";
    String sonarModuleKey = "Sonar Module Key";

    IdeaProject ideaProject = new IdeaProject(null, settings, null);
    ideaProject.addSonarModuleAssociation(ideaModuleName, sonarModuleKey);

    assertThat(settings.getModuleKeys().get(ideaModuleName), is(sonarModuleKey));
  }

  @Test
  public void shouldClearSonarModuleAssociations() {
    IdeaProject ideaProject = new IdeaProject(null, settings, null);
    ideaProject.addSonarModuleAssociation("key A", "value");
    ideaProject.addSonarModuleAssociation("key B", "value");

    ideaProject.clearSonarModuleAssociations();

    assertThat(settings.getModuleKeys().isEmpty(), is(true));
  }

  @Test
  public void shouldGetModules() {
    Module moduleA = mock(Module.class);
    Module moduleB = mock(Module.class);
    when(moduleManager.getModules()).thenReturn(new Module[]{moduleA, moduleB});

    IdeaProject ideaProject = new IdeaProject(null, null, moduleManager);
    Module[] modules = ideaProject.getModules();

    assertThat(modules.length, is(2));
  }
}
