package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.action.associator.facades.IdeaProject;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class GradleAssociatorTest {
  Project project;
  ProjectSettings settings;
  ModuleManager moduleManager;
  SonarProject sonarProject;
  SonarModelSettings sonarModelSettings;

  IdeaProject ideaProject;

  @Before
  public void setUp() throws Exception {
    project = mock(Project.class);
    settings = new ProjectSettings();
    moduleManager = mock(ModuleManager.class);
    sonarProject = mock(SonarProject.class);
    sonarModelSettings = mock(SonarModelSettings.class);

    ideaProject = mock(IdeaProject.class);
  }

  @Test
  public void getsTheNameOfTheProjectInSonarIfItExists() {
    String sonarProjectName = "Sonar Project Name";
    when(ideaProject.getSonarProjectName()).thenReturn(sonarProjectName);

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);

    assertThat(associator.getSonarProjectName(), is(sonarProjectName));
  }

  @Test
  public void getsTheNameOfTheProjectInSonarIfItDoesNotExist() {
    when(ideaProject.getSonarProjectName()).thenReturn(null);

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);

    assertThat(associator.getSonarProjectName(), is(nullValue()));
  }

  @Test
  public void associatingAProjectDoesClearAnyPreviousAssociations() {
    String ideaProjectName = "Idea Project Name";
    String sonarProjectKey = "Sonar Project Key";
    String sonarServerId = "Sonar Server Id";
    when(ideaProject.getName()).thenReturn(ideaProjectName);
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServerId()).thenReturn(sonarServerId);
    when(sonarProject.getModules()).thenReturn(new ISonarRemoteModule[] {});

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);
    associator.associate(sonarProject);

    verify(ideaProject).clearSonarModuleAssociations();
  }

  @Test
  public void associatesSingleModuleProject () {
    String ideaProjectName = "Idea Project Name";
    String sonarProjectKey = "Sonar Project Key";
    String sonarServerId = "Sonar Server Id";
    when(ideaProject.getName()).thenReturn(ideaProjectName);
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServerId()).thenReturn(sonarServerId);
    when(sonarProject.getModules()).thenReturn(new ISonarRemoteModule[] {});

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);
    associator.associate(sonarProject);

    verify(ideaProject).setSonarServerId(sonarServerId);
    verify(ideaProject).setSonarProjectKey(sonarProjectKey);
    verify(ideaProject).addSonarModuleAssociation(ideaProjectName, sonarProjectKey);
  }

  @Test
  public void associateMultiModuleProject () {
    String ideaProjectName = "Idea Project Name";
    String sonarProjectKey = "Sonar Project Key";
    String sonarServerId = "Sonar Server Id";
    when(ideaProject.getName()).thenReturn(ideaProjectName);
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServerId()).thenReturn(sonarServerId);

    String ideaModuleNameA = "Idea Module Name A";
    String ideaModuleNameB = "Idea Module Name B";
    Module prjModule = mock(Module.class);
    Module subModuleA = mock(Module.class);
    Module subModuleB = mock(Module.class);
    when(subModuleA.getName()).thenReturn(ideaModuleNameA);
    when(subModuleB.getName()).thenReturn(ideaModuleNameB);
    when(prjModule.getOptionValue(anyString())).thenReturn(ideaProjectName);
    when(subModuleA.getOptionValue(anyString())).thenReturn(":" + ideaModuleNameA);
    when(subModuleB.getOptionValue(anyString())).thenReturn(":" + ideaModuleNameB);
    when(ideaProject.getModules()).thenReturn(new Module[] {subModuleA, subModuleB, prjModule});

    ISonarRemoteModule sonarModuleA = mock(ISonarRemoteModule.class);
    ISonarRemoteModule sonarModuleB = mock(ISonarRemoteModule.class);
    when(sonarModuleA.getName()).thenReturn("Sonar Module Name A");
    when(sonarModuleB.getName()).thenReturn("Sonar Module Name B");
    when(sonarModuleA.getKey()).thenReturn(sonarProjectKey + ":" + ideaModuleNameA);
    when(sonarModuleB.getKey()).thenReturn(sonarProjectKey + ":" + ideaModuleNameB);
    when(sonarProject.getModules()).thenReturn(new ISonarRemoteModule[] {sonarModuleB, sonarModuleA});

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);
    associator.associate(sonarProject);

    verify(ideaProject).addSonarModuleAssociation(ideaProjectName, sonarProjectKey);
    verify(ideaProject).addSonarModuleAssociation(ideaModuleNameA, sonarProjectKey + ":" + ideaModuleNameA);
    verify(ideaProject).addSonarModuleAssociation(ideaModuleNameB, sonarProjectKey + ":" + ideaModuleNameB);
  }
}
