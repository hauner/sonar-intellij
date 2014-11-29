package org.sonar.ide.intellij.action.associator.facades;

import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;
import org.sonar.ide.intellij.wsclient.ISonarWSClientFacade;

import java.util.List;


public class SonarProject {
  private final ISonarRemoteProject project;
  private ISonarWSClientFacade client;

  public SonarProject (ISonarRemoteProject project, ISonarWSClientFacade client) {
    this.project = project;
    this.client = client;
  }

  public ISonarRemoteProject getProject() {
    return project;
  }

  public String getKey() {
    return project.getKey();
  }

  public String getServerId() {
    return project.getServer().getId();
  }

  public ISonarRemoteModule[] getModules() {
    List<ISonarRemoteModule> remoteModules = client.getRemoteModules(project);
    return remoteModules.toArray(new ISonarRemoteModule[remoteModules.size()]);
  }
}
