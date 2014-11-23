package org.sonar.ide.intellij.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.sonar.ide.intellij.action.associator.SonarQubeAssociator;
import org.sonar.ide.intellij.associate.AssociateDialog;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;

public class SonarQubeAction {
  private Project project;
  private ProjectSettings settings;
  private AssociateDialog dialog;
  private SonarQubeAssociator associator;

  public SonarQubeAction(Project project, ProjectSettings settings, AssociateDialog dialog, SonarQubeAssociator associator) {
    this.project = project;
    this.settings = settings;
    this.dialog = dialog;
    this.associator = associator;
  }

  public void associate() {
    if (settings.isAssociated()) {
      dialog.setSelectedSonarQubeProject(settings.getServerId(), settings.getProjectKey());
    } else {
      // try to guess project association
      String projectName = associator.getSonarProjectName();
      if (projectName != null) {
        dialog.setFilter(projectName);
      }
    }
    dialog.show();
    processResult(project, settings, dialog);
  }


  private void processResult(Project p, ProjectSettings settings, AssociateDialog dialog) {
    if (dialog.isExitCodeUnassociate()) {
      settings.unassociate();
    } else if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      settings.setServerId(null);
      settings.setProjectKey(null);
      ISonarRemoteProject sonarProject = dialog.getSelectedSonarQubeProject();
      if (sonarProject == null) {
        settings.unassociate();
      } else {
        associator.associate(sonarProject);
      }
    }
  }
}
