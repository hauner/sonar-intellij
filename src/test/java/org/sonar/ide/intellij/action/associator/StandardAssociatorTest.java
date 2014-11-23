package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.console.SonarQubeConsole;
import org.sonar.ide.intellij.model.SonarQubeServer;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;
import org.sonar.ide.intellij.wsclient.ISonarWSClientFacade;
import org.sonar.ide.intellij.wsclient.WSClientFactory;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class StandardAssociatorTest {
  Project project;
  ProjectSettings settings;
  MavenProjectsManager mavenManager;
  ModuleManager moduleManager;
  ISonarRemoteProject sonarProject;
  SonarQubeConsole console;
  SonarQubeServer sonarQubeServer;
  WSClientFactory clientFactory;
  ISonarWSClientFacade sonarClient;

  @Before
  public void setup() {
    project = mock(Project.class);
    settings = new ProjectSettings();
    mavenManager = mock(MavenProjectsManager.class);
    moduleManager = mock(ModuleManager.class);
    sonarProject = mock(ISonarRemoteProject.class);
    console = mock(SonarQubeConsole.class);
    sonarQubeServer = mock(SonarQubeServer.class);
    clientFactory = mock(WSClientFactory.class);
    sonarClient = mock(ISonarWSClientFacade.class);
  }

  @Test
  public void clearsOldModuleAssociations() {
    when(moduleManager.getModules()).thenReturn(new Module[]{});
    settings.getModuleKeys().put("localModuleName", "remoteProjectKey");
    when(sonarQubeServer.getId()).thenReturn("sonarServerId");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, null, moduleManager, console, null);
    associator.associate(new SonarProject(sonarProject));

    assertThat(settings.getModuleKeys().isEmpty(), is(true));
  }

  @Test
  public void associatesSingleModuleProject() {
    Module module = mock(Module.class);
    when(module.getName()).thenReturn("moduleName");
    when(moduleManager.getModules()).thenReturn(new Module[]{module});
    when(sonarProject.getKey()).thenReturn("sonarProjectKey");
    when(sonarQubeServer.getId()).thenReturn("sonarServerId");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, null, moduleManager, console, null);
    associator.associate(new SonarProject(sonarProject));

    assertThat(settings.getModuleKeys().size(), is(1));
    assertThat(settings.getModuleKeys().containsKey("moduleName"), is(true));
    assertThat(settings.getModuleKeys().get("moduleName"), equalTo("sonarProjectKey"));
    assertThat(settings.getServerId(), equalTo("sonarServerId"));
    assertThat(settings.getProjectKey(), equalTo("sonarProjectKey"));
  }

  @Test
  public void rejectsMultiModuleProjectIfNotMaven() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});
    when(mavenManager.isMavenizedProject()).thenReturn(false);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, null);
    associator.associate(new SonarProject(sonarProject));

    verify(console).error(startsWith("Only multi-module Maven projects are supported"));
    assertThat(settings.isAssociated(), is(false));
    assertThat(settings.getServerId(), is(nullValue()));
    assertThat(settings.getProjectKey(), is(nullValue()));
  }

  @Test
  public void rejectsMultiModuleMavenProjectWithMultipleRoots() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});
    MavenProject root1 = mock(MavenProject.class);
    MavenProject root2 = mock(MavenProject.class);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1, root2));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, null);
    associator.associate(new SonarProject(sonarProject));

    verify(console).error(startsWith("Maven projects with more than 1 root project"));
    assertThat(settings.isAssociated(), is(false));
    assertThat(settings.getServerId(), is(nullValue()));
    assertThat(settings.getProjectKey(), is(nullValue()));
  }

  @Test
  public void requestSonarModulesOfMultiModuleMavenProject() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});

    MavenId id1 = mock(MavenId.class);
    when(id1.getGroupId()).thenReturn("groupId");
    when(id1.getArtifactId()).thenReturn("artifactId");
    MavenProject root1 = mock(MavenProject.class);
    when(root1.getMavenId()).thenReturn(id1);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    MavenProject project1 = mock(MavenProject.class);
    when(mavenManager.getProjects()).thenReturn(Arrays.asList(project1));

    when(sonarProject.getKey()).thenReturn("groupId:artifactId:sonarProjectKey");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    when(clientFactory.getSonarClient(sonarQubeServer)).thenReturn(sonarClient);
    ISonarRemoteModule remoteModule = mock(ISonarRemoteModule.class);
    when(sonarClient.getRemoteModules(sonarProject)).thenReturn(Arrays.asList(remoteModule));

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, clientFactory);
    associator.associate(new SonarProject(sonarProject));

    verify(sonarClient).getRemoteModules(sonarProject);
    verify(console).info(startsWith("Project has 1 modules while remote"));
  }

  @Test
  public void associateMultiModuleProjectWarnsIfModuleIsNotAMavenModule() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(module2.getName()).thenReturn("*Module 2*");
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});

    MavenId id1 = mock(MavenId.class);
    when(id1.getGroupId()).thenReturn("groupId");
    when(id1.getArtifactId()).thenReturn("artifactId");
    MavenProject root1 = mock(MavenProject.class);
    when(root1.getMavenId()).thenReturn(id1);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    MavenProject project1 = mock(MavenProject.class);
    MavenProject project2 = mock(MavenProject.class);
    when(project1.getMavenId()).thenReturn(id1);
    when(project2.getMavenId()).thenReturn(id1);
    when(mavenManager.getProjects()).thenReturn(Arrays.asList(project1, project2));

    when(sonarProject.getKey()).thenReturn("groupId:artifactId:sonarProjectKey");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    when(clientFactory.getSonarClient(sonarQubeServer)).thenReturn(sonarClient);
    ISonarRemoteModule remoteModule = mock(ISonarRemoteModule.class);
    when(sonarClient.getRemoteModules(sonarProject)).thenReturn(Arrays.asList(remoteModule));

    when(mavenManager.findProject(module1)).thenReturn(project1);
    when(mavenManager.findProject(module2)).thenReturn(null);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, clientFactory);
    associator.associate(new SonarProject(sonarProject));

    verify(console).error(startsWith("Module *Module 2* is not a Maven module"));
  }

  @Test
  public void associatesModuleToSonarProject() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(module1.getName()).thenReturn("*Module 1*");
    when(module2.getName()).thenReturn("*Module 2*");
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});

    MavenId id1 = mock(MavenId.class);
    when(id1.getGroupId()).thenReturn("groupId");
    when(id1.getArtifactId()).thenReturn("artifactId");
    MavenProject root1 = mock(MavenProject.class);
    when(root1.getMavenId()).thenReturn(id1);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    MavenProject project1 = mock(MavenProject.class);
    MavenProject project2 = mock(MavenProject.class);
    when(project1.getMavenId()).thenReturn(id1);
    when(project2.getMavenId()).thenReturn(id1);
    when(mavenManager.getProjects()).thenReturn(Arrays.asList(project1, project2));

    when(sonarProject.getKey()).thenReturn("groupId:artifactId:sonarProjectKey");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    when(clientFactory.getSonarClient(sonarQubeServer)).thenReturn(sonarClient);
    ISonarRemoteModule remoteModule = mock(ISonarRemoteModule.class);
    when(sonarClient.getRemoteModules(sonarProject)).thenReturn(Arrays.asList(remoteModule));

    when(mavenManager.findProject(module1)).thenReturn(project1);
    when(mavenManager.findProject(module2)).thenReturn(null);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, clientFactory);
    associator.associate(new SonarProject(sonarProject));

    assertThat(settings.getModuleKeys().size(), is(1));
    assertThat(settings.getModuleKeys().containsKey("*Module 1*"), is(true));
    assertThat(settings.getModuleKeys().get("*Module 1*"), equalTo("groupId:artifactId:sonarProjectKey"));
  }

  @Test
  public void associatesModuleToSonarModule() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(module1.getName()).thenReturn("*Module 1*");
    when(module2.getName()).thenReturn("*Module 2*");
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});

    MavenId id1 = mock(MavenId.class);
    when(id1.getGroupId()).thenReturn("groupId");
    when(id1.getArtifactId()).thenReturn("artifactId");
    MavenProject root1 = mock(MavenProject.class);
    when(root1.getMavenId()).thenReturn(id1);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    MavenProject project1 = mock(MavenProject.class);
    MavenProject project2 = mock(MavenProject.class);
    when(project1.getMavenId()).thenReturn(id1);
    when(project2.getMavenId()).thenReturn(id1);
    when(mavenManager.getProjects()).thenReturn(Arrays.asList(project1, project2));

    when(sonarProject.getKey()).thenReturn("sonarProjectKey");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    when(clientFactory.getSonarClient(sonarQubeServer)).thenReturn(sonarClient);
    ISonarRemoteModule remoteModule = mock(ISonarRemoteModule.class);
    when(remoteModule.getKey()).thenReturn("groupId:artifactId");
    when(sonarClient.getRemoteModules(sonarProject)).thenReturn(Arrays.asList(remoteModule));

    when(mavenManager.findProject(module1)).thenReturn(project1);
    when(mavenManager.findProject(module2)).thenReturn(null);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, clientFactory);
    associator.associate(new SonarProject(sonarProject));

    assertThat(settings.getModuleKeys().size(), is(1));
    assertThat(settings.getModuleKeys().containsKey("*Module 1*"), is(true));
    assertThat(settings.getModuleKeys().get("*Module 1*"), equalTo("groupId:artifactId"));
  }

  @Test
  public void warnsWhenNoSonarModuleMatches() {
    Module module1 = mock(Module.class);
    Module module2 = mock(Module.class);
    when(module1.getName()).thenReturn("*Module 1*");
    when(module2.getName()).thenReturn("*Module 2*");
    when(moduleManager.getModules()).thenReturn(new Module[]{module1, module2});

    MavenId id1 = mock(MavenId.class);
    when(id1.getGroupId()).thenReturn("groupId");
    when(id1.getArtifactId()).thenReturn("artifactId");
    MavenProject root1 = mock(MavenProject.class);
    when(root1.getMavenId()).thenReturn(id1);
    when(mavenManager.getRootProjects()).thenReturn(Arrays.asList(root1));
    when(mavenManager.isMavenizedProject()).thenReturn(true);

    MavenProject project1 = mock(MavenProject.class);
    MavenProject project2 = mock(MavenProject.class);
    when(project1.getMavenId()).thenReturn(id1);
    when(project2.getMavenId()).thenReturn(id1);
    when(mavenManager.getProjects()).thenReturn(Arrays.asList(project1, project2));

    when(sonarProject.getKey()).thenReturn("sonarProjectKey");
    when(sonarProject.getServer()).thenReturn(sonarQubeServer);
    when(clientFactory.getSonarClient(sonarQubeServer)).thenReturn(sonarClient);
    ISonarRemoteModule remoteModule = mock(ISonarRemoteModule.class);
    when(remoteModule.getKey()).thenReturn("does not match");
    when(sonarClient.getRemoteModules(sonarProject)).thenReturn(Arrays.asList(remoteModule));

    when(mavenManager.findProject(module1)).thenReturn(project1);
    when(mavenManager.findProject(module2)).thenReturn(null);

    SonarQubeAssociator associator = new StandardAssociator(null, settings, mavenManager, moduleManager, console, clientFactory);
    associator.associate(new SonarProject(sonarProject));

    verify(console).error(startsWith("Unable to find matching SonarQube module"));
  }
}
