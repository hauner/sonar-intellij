package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.util.Key;


public class SonarModelData {
  public final static Key<SonarModelData> KEY = new Key<SonarModelData>("Sonar.Gradle");

  private String projectKey;
  private String projectName;

  /**
   * @return sonar.projectKey
   */
  public String getProjectKey() {
    return projectKey;
  }

  /**
   * @return sonar.projectName
   */
  public String getProjectName() {
    return projectName;
  }

  void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  void setProjectName(String projectName) {
    this.projectName = projectName;
  }
}
