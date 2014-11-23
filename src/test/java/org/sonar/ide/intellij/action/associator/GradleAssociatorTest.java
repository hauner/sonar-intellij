package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.gradle.SonarModelSettings;
import org.sonar.ide.intellij.model.SonarQubeServer;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GradleAssociatorTest {
  Project project;
  ProjectSettings settings;
  ModuleManager moduleManager;
  ISonarRemoteProject sonarProject;
  SonarQubeServer sonarQubeServer;
  SonarModelSettings sonarModelSettings;

  @Before
  public void setUp() throws Exception {
    project = mock(Project.class);
    settings = new ProjectSettings();
    moduleManager = mock(ModuleManager.class);
    sonarProject = mock(ISonarRemoteProject.class);
    sonarQubeServer = mock(SonarQubeServer.class);
    sonarModelSettings = mock(SonarModelSettings.class);
  }

  @Test
  public void knowsTheNameOfTheProjectModuleOld() {
    String prjName = "Project Module";
    when(project.getName()).thenReturn(prjName);

    Module prjModule = mock(Module.class);
    when(prjModule.getComponent(SonarModelSettings.class)).thenReturn(sonarModelSettings);
    when(moduleManager.findModuleByName(prjName)).thenReturn(prjModule);

    String prjNameSonar = "Project Sonar";
    when(sonarModelSettings.getSonarProjectName()).thenReturn(prjNameSonar);

    SonarQubeAssociator associator = new GradleAssociator(project, settings, moduleManager);

    assertThat(associator.getProjectName(), is(prjNameSonar));
  }

  @Test
  public void knowsTheNameOfTheProjectModule() {
    String prjName = "Project Module";
    when(project.getName()).thenReturn(prjName);

    Module prjModule = mock(Module.class);
    when(prjModule.getComponent(SonarModelSettings.class)).thenReturn(sonarModelSettings);
    when(moduleManager.findModuleByName(prjName)).thenReturn(prjModule);

    String prjNameSonar = "Project Sonar";
    when(sonarModelSettings.getSonarProjectName()).thenReturn(prjNameSonar);

    SonarQubeAssociator associator = new GradleAssociator(project, settings, moduleManager);

    assertThat(associator.getProjectName(), is(prjNameSonar));
  }


  @Test
  public void reAssociatingAProjectClearsPreviousModuleAssociations() {
    settings.getModuleKeys().put("localModuleName", "remoteProjectKey");
    when(moduleManager.getModules()).thenReturn(new Module[]{});

    String sonarProjectKey = "Sonar Project Key";
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    String serverId = "Sonar Server Id";
    when(sonarQubeServer.getId()).thenReturn(serverId);

    SonarQubeAssociator associator = new GradleAssociator(project, settings, moduleManager);
    associator.associate(sonarProject);

    assertThat(settings.getModuleKeys().isEmpty(), is(true));
  }

  @Test
  public void associatesSingleModuleProject () {
    String moduleName = "Root Module";
    Module module = mock(Module.class);
    when(module.getName()).thenReturn(moduleName);
    when(moduleManager.getModules()).thenReturn(new Module[]{module});

    String sonarProjectKey = "Sonar Project Key";
    when(sonarProject.getKey()).thenReturn(sonarProjectKey);
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    String serverId = "Sonar Server Id";
    when(sonarQubeServer.getId()).thenReturn(serverId);

    SonarQubeAssociator associator = new GradleAssociator(project, settings, moduleManager);
    associator.associate(sonarProject);

    assertThat(settings.getModuleKeys().size(), is(1));
    assertThat(settings.getModuleKeys().containsKey(moduleName), is(true));
    assertThat(settings.getModuleKeys().get(moduleName), equalTo(sonarProjectKey));
    assertThat(settings.getProjectKey(), is(sonarProjectKey));
    assertThat(settings.getServerId(), is(serverId));
  }
}
