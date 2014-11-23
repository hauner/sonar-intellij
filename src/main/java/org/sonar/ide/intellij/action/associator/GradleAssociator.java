package org.sonar.ide.intellij.action.associator;

import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.action.associator.facades.IdeaProject;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;


public class GradleAssociator implements SonarQubeAssociator {
  private IdeaProject ideaProject;

  public GradleAssociator(IdeaProject ideaProject) {
    this.ideaProject = ideaProject;
  }

  @Override
  public String getSonarProjectName() {
    return ideaProject.getSonarProjectName();
  }

  @Override
  public void associate(@NotNull SonarProject sonarProject) {
    ideaProject.setSonarServerId(sonarProject.getServerId());
    ideaProject.setSonarProjectKey(sonarProject.getKey());

    ideaProject.clearSonarModuleAssociations();
    ideaProject.addSonarModuleAssociation(ideaProject.getName(), sonarProject.getKey());
  }

}
