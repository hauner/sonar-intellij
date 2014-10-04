package org.sonar.ide.intellij.action;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.console.SonarQubeConsole;
import org.sonar.ide.intellij.wsclient.ISonarRemoteModule;
import org.sonar.ide.intellij.wsclient.ISonarRemoteProject;
import org.sonar.ide.intellij.wsclient.WSClientFactory;

import java.util.List;

public class SonarQubeAssociator {
  private final MavenProjectsManager mavenProjectsManager;
  private final ISonarRemoteProject sonarProject;
  private ProjectSettings settings;
  private SonarQubeAction action;
  private Project p;

  public SonarQubeAssociator (SonarQubeAction action, Project p, ProjectSettings settings,
      MavenProjectsManager mavenProjectsManager, @NotNull ISonarRemoteProject sonarProject) {
    this.action = action;
    this.p = p;
    this.settings = settings;
    this.mavenProjectsManager = mavenProjectsManager;
    this.sonarProject = sonarProject;
  }

  public void associate() {
    ModuleManager moduleManager = ModuleManager.getInstance(p);
    Module[] ijModules = moduleManager.getModules();
    settings.getModuleKeys().clear();
    SonarQubeConsole console = SonarQubeConsole.getSonarQubeConsole(p);
    if (ijModules.length == 1) {
      settings.getModuleKeys().put(ijModules[0].getName(), sonarProject.getKey());
    } else if (ijModules.length > 1) {
      if (!mavenProjectsManager.isMavenizedProject()) {
        console.error("Only multi-module Maven projects are supported for now");
        return;
      }
      List<MavenProject> rootProjects = mavenProjectsManager.getRootProjects();
      List<MavenProject> mavenModules = mavenProjectsManager.getProjects();
      if (rootProjects.size() > 1) {
        console.error("Maven projects with more than 1 root project are not supported");
        return;
      }
      MavenProject rootProject = rootProjects.get(0);
      String branchSuffix = action.guessBranchSuffix(rootProject, sonarProject.getKey());
      List<ISonarRemoteModule> sonarModules = WSClientFactory.getInstance().getSonarClient(sonarProject.getServer()).getRemoteModules(sonarProject);
      if (sonarModules.size() + 1 != mavenModules.size()) {
        console.info("Project has " + mavenModules.size() + " modules while remote SonarQube project has " + (sonarModules.size() + 1) + " modules");
      }
      action.associateModules(settings, mavenProjectsManager, sonarProject, ijModules, console, branchSuffix, sonarModules);
    }

    settings.setServerId(sonarProject.getServer().getId());
    settings.setProjectKey(sonarProject.getKey());
  }
}
