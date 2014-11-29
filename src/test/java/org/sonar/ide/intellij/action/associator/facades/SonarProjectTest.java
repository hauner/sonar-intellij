package org.sonar.ide.intellij.action.associator.facades;

import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.model.SonarQubeServer;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;
import org.sonar.ide.intellij.wsclient.ISonarWSClientFacade;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SonarProjectTest {
  ISonarRemoteProject remoteProject;
  SonarQubeServer sonarQubeServer;
  ISonarWSClientFacade clientFacade;

  @Before
  public void setup() {
    remoteProject = mock(ISonarRemoteProject.class);
    sonarQubeServer = mock(SonarQubeServer.class);
    clientFacade = mock(ISonarWSClientFacade.class);
  }

  @Test
  public void shouldGetProject() {
    SonarProject project = new SonarProject(remoteProject, null);

    assertThat(project.getProject(), is(remoteProject));
  }

  @Test
  public void shouldGetKey() {
    String key = "Key";
    when(remoteProject.getKey()).thenReturn(key);

    SonarProject project = new SonarProject(remoteProject, null);

    assertThat(project.getKey(), is(key));
  }

  @Test
  public void shouldGetServerId() {
    String id = "Id";
    when(sonarQubeServer.getId()).thenReturn(id);
    when(remoteProject.getServer()).thenReturn(sonarQubeServer);

    SonarProject project = new SonarProject(remoteProject, null);

    assertThat(project.getServerId(), is(id));
  }

  @Test
  public void shouldGetModules() {
    ISonarRemoteModule moduleA = mock(ISonarRemoteModule.class);
    ISonarRemoteModule moduleB = mock(ISonarRemoteModule.class);

    List<ISonarRemoteModule> remoteModules = Arrays.asList(moduleA, moduleB);
    when(clientFacade.getRemoteModules(remoteProject)).thenReturn(remoteModules);

    SonarProject project = new SonarProject(remoteProject, clientFacade);
    ISonarRemoteModule[] modules = project.getModules();

    assertThat(modules.length, is(2));
  }
}
