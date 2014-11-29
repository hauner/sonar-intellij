package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;


public interface SonarQubeAssociator {
  @Nullable String getSonarProjectName();
  void associate(SonarProject sonarProject);
}
