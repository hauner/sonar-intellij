package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public interface SonarQubeAssociator {
  String getProjectName();
  void associate(@NotNull ISonarRemoteProject sonarProject);
}
