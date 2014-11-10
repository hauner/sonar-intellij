package org.sonar.ide.intellij.gradle.tooling;

import org.gradle.tooling.model.Model;

import java.io.Serializable;


public interface SonarRunnerModel extends Model, Serializable {
  /**
   * @return sonar.projectKey
   */
  String getProjectKey();

  /**
   * @return sonar.projectName
   */
  String getProjectName();
}
