package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.action.associator.facades.IdeaProject;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
  public void reAssociatingAProjectClearsPreviousModuleAssociations() {
    String ideaProjectName = "Idea Project Name";
    String sonarProjectKey = "Sonar Project Key";
    String sonarServerId = "Sonar Server Id";
    when(ideaProject.getName()).thenReturn(ideaProjectName);
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServerId()).thenReturn(sonarServerId);

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

    SonarQubeAssociator associator = new GradleAssociator(ideaProject);
    associator.associate(sonarProject);

    verify(ideaProject).setSonarServerId(sonarServerId);
    verify(ideaProject).setSonarProjectKey(sonarProjectKey);
    verify(ideaProject).addSonarModuleAssociation(ideaProjectName, sonarProjectKey);
  }
}
