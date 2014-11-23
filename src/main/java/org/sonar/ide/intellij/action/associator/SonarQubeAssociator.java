package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public interface SonarQubeAssociator {
  @Nullable String getProjectName();
  void associate(@NotNull ISonarRemoteProject sonarProject);
}
