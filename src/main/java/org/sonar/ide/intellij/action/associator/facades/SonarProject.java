package org.sonar.ide.intellij.action.associator.facades;

import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public class SonarProject {
  private final ISonarRemoteProject project;

  public SonarProject (ISonarRemoteProject project) {
    this.project = project;
  }

  public ISonarRemoteProject getProject() {
    return project;
  }

  public String getKey() {
    return null;
  }

  public String getServerId() {
    return null;
  }
}
