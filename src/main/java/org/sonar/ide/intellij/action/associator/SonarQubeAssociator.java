package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public interface SonarQubeAssociator {
  @Nullable String getSonarProjectName();
  void associate(@NotNull ISonarRemoteProject sonarProject);
  void associate(@NotNull SonarProject sonarProject);
}
