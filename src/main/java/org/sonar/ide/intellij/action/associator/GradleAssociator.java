package org.sonar.ide.intellij.action.associator;

import com.intellij.openapi.externalSystem.util.ExternalSystemConstants;
import com.intellij.openapi.module.Module;
import org.sonar.ide.intellij.action.associator.facades.IdeaProject;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;


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
  public void associate(SonarProject sonarProject) {
    clearCurrentAssociation();
    setProjectAssociation(sonarProject);
    setModuleAssociations(sonarProject);
  }

  private void clearCurrentAssociation() {
    ideaProject.clearSonarModuleAssociations();
  }

  private void setProjectAssociation(SonarProject sonarProject) {
    ideaProject.setSonarServerId(sonarProject.getServerId());
    ideaProject.setSonarProjectKey(sonarProject.getKey());
    ideaProject.addSonarModuleAssociation(ideaProject.getName(), sonarProject.getKey());
  }

  private void setModuleAssociations(SonarProject sonarProject) {
    ISonarRemoteModule[] sonarModules = sonarProject.getModules();
    for(ISonarRemoteModule sonarModule: sonarModules) {
      Module matchingIdeaModule = findIdeaModule(sonarProject.getKey(), sonarModule.getKey());
      if (matchingIdeaModule != null) {
        ideaProject.addSonarModuleAssociation(matchingIdeaModule.getName(), sonarModule.getKey());
      }
    }
  }

  private Module findIdeaModule(String sonarProjectKey, String sonarModuleKey) {
    for (Module ideaModule : ideaProject.getModules()) {
      String gradleProjectKey = ideaModule.getOptionValue(ExternalSystemConstants.LINKED_PROJECT_ID_KEY);
      String expectedSonarKey = sonarProjectKey + gradleProjectKey;

      if (sonarModuleKey.equals(expectedSonarKey)) {
        return ideaModule;
      }
    }
    return null;
  }

}
