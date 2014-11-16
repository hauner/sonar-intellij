package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;


public class GradleAssociator implements SonarQubeAssociator {

  public GradleAssociator () {
  }

  @Override
  public String getProjectName() {
    return null;
  }

  @Override
  public void associate(@NotNull ISonarRemoteProject sonarProject) {
  }
}
