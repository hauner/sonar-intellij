package org.sonar.ide.intellij.gradle.tooling;

import java.io.Serializable;


public class SonarRunnerModelDefault implements SonarRunnerModel, Serializable {
  private String projectKey;
  private String projectName;

  public SonarRunnerModelDefault (String projectKey, String projectName) {
    this.projectKey = projectKey;
    this.projectName = projectName;
  }

  @Override
  public String getProjectKey() {
    return projectKey;
  }

  @Override
  public String getProjectName() {
    return projectName;
  }
}
