package org.sonar.ide.intellij.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.sonar.ide.intellij.associate.AssociateDialog;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;

public class SonarQubeAction {
  private Project project;
  private ProjectSettings settings;
  private MavenProjectsManager mavenProjectsManager;
  private AssociateDialog dialog;
  private SonarQubeAssociator associator;

  public SonarQubeAction(Project project, ProjectSettings settings, MavenProjectsManager mavenProjectsManager,
                         AssociateDialog dialog, SonarQubeAssociator associator) {
    this.project = project;
    this.settings = settings;
    this.mavenProjectsManager = mavenProjectsManager;
    this.dialog = dialog;
    this.associator = associator;
  }

  public void associate() {
    associate(null);
  }

  public void associate(@Nullable String projectName) {
    if (settings.isAssociated()) {
      dialog.setSelectedSonarQubeProject(settings.getServerId(), settings.getProjectKey());
    } else {
      // try to guess project association
      if (projectName != null) {
        dialog.setFilter(projectName);
      }
    }
    dialog.show();
    processResult(project, mavenProjectsManager, settings, dialog);
  }


  private void processResult(Project p, MavenProjectsManager mavenProjectsManager, ProjectSettings settings, AssociateDialog dialog) {
    if (dialog.getExitCode() == AssociateDialog.UNASSOCIATE_EXIT_CODE) {
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
