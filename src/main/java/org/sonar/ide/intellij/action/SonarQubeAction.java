package org.sonar.ide.intellij.action;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.sonar.ide.intellij.associate.AssociateDialog;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.console.SonarQubeConsole;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;

import java.util.List;


public class SonarQubeAction {
  private Project project;
  private ProjectSettings settings;
  private MavenProjectsManager mavenProjectsManager;
  private AssociateDialog dialog;

  public SonarQubeAction(Project project, ProjectSettings settings, MavenProjectsManager mavenProjectsManager, AssociateDialog dialog) {
    this.project = project;
    this.settings = settings;
    this.mavenProjectsManager = mavenProjectsManager;
    this.dialog = dialog;
  }

  public void associate() {
    if (settings.isAssociated()) {
      dialog.setSelectedSonarQubeProject(settings.getServerId(), settings.getProjectKey());
    } else {
      // Try to guess project association
      if (mavenProjectsManager.isMavenizedProject() && mavenProjectsManager.hasProjects()) {
        dialog.setFilter(mavenProjectsManager.getRootProjects().get(0).getDisplayName());
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
        SonarQubeAssociator associator = new SonarQubeAssociator(this, p, settings, mavenProjectsManager, sonarProject);
        associator.associate();
      }
    }
  }

  void associateModules(ProjectSettings settings, MavenProjectsManager mavenProjectsManager, ISonarRemoteProject sonarProject,
                        Module[] ijModules, SonarQubeConsole console, String branchSuffix, List<ISonarRemoteModule> sonarModules) {
    for (Module ijModule : ijModules) {
      MavenProject mavenModule = mavenProjectsManager.findProject(ijModule);
      if (mavenModule == null) {
        console.error("Module " + ijModule.getName() + " is not a Maven module");
      } else {
        String expectedKey = sonarKey(mavenModule) + branchSuffix;
        if (expectedKey.equals(sonarProject.getKey())) {
          settings.getModuleKeys().put(ijModule.getName(), expectedKey);
        } else {
          associateModule(settings, console, sonarModules, ijModule, expectedKey);
        }
      }

    }
  }

  private void associateModule(ProjectSettings settings, SonarQubeConsole console, List<ISonarRemoteModule> sonarModules, Module ijModule, String expectedKey) {
    boolean found = false;
    for (ISonarRemoteModule sonarModule : sonarModules) {
      if (expectedKey.equals(sonarModule.getKey())) {
        settings.getModuleKeys().put(ijModule.getName(), expectedKey);
        found = true;
        break;
      }
    }
    if (!found) {
      console.error("Unable to find matching SonarQube module for IntelliJ module " + ijModule.getName());
    }
  }

  String guessBranchSuffix(MavenProject rootProject, String key) {
    String rootKey = sonarKey(rootProject);
    if (key.startsWith(rootKey)) {
      return key.substring(rootKey.length());
    }
    return "";
  }

  private String sonarKey(MavenProject project) {
    return project.getMavenId().getGroupId() + ":" + project.getMavenId().getArtifactId();
  }
}
