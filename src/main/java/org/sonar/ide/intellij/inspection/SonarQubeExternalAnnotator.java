/*
 * SonarQube IntelliJ
 * Copyright (C) 2013-2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.ide.intellij.inspection;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.config.ProjectSettings;
import org.sonar.ide.intellij.config.SonarQubeSettings;
import org.sonar.ide.intellij.console.SonarQubeConsole;
import org.sonar.ide.intellij.model.ISonarIssue;
import org.sonar.ide.intellij.model.SonarQubeServer;
import org.sonar.ide.intellij.wsclient.ISonarWSClientFacade;
import org.sonar.ide.intellij.wsclient.SonarWSClientException;
import org.sonar.ide.intellij.wsclient.WSClientFactory;

import java.util.ArrayList;
import java.util.List;

public class SonarQubeExternalAnnotator extends ExternalAnnotator<SonarQubeExternalAnnotator.State, SonarQubeExternalAnnotator.State> {

  private static final Logger LOG = Logger.getInstance(SonarQubeExternalAnnotator.class);

  public static class State {
    private PsiFile file;
    private VirtualFile vfile;
    private Project project;
    private SonarQubeServer server;
    private List<ISonarIssue> remoteIssues;
    private String componentKey;
  }

  @Nullable
  @Override
  public State collectInformation(@NotNull PsiFile file) {
    VirtualFile vfile = file.getVirtualFile();
    if (vfile == null) {
      return null;
    }
    State result = new State();
    result.file = file;
    result.vfile = vfile;
    result.project = file.getProject();
    ProjectSettings projectSettings = result.project.getComponent(ProjectSettings.class);
    if (!projectSettings.isAssociated()) {
      return null;
    }
    String serverId = projectSettings.getServerId();
    SonarQubeSettings settings = SonarQubeSettings.getInstance();
    result.server = settings.getServer(serverId);
    SonarQubeConsole console = SonarQubeConsole.getSonarQubeConsole(result.project);
    if (result.server == null) {
      console.error("Project is associated to a server that is not configured: " + serverId);
      return null;
    }
    return result;
  }

  @Nullable
  @Override
  public State doAnnotate(State collectedInfo) {
    final Project p = collectedInfo.project;
    SonarQubeConsole console = SonarQubeConsole.getSonarQubeConsole(p);
    ProjectSettings projectSettings = p.getComponent(ProjectSettings.class);
    VirtualFile virtualFile = collectedInfo.vfile;
    if (virtualFile == null) {
      return null;
    }
    Module module = ProjectRootManager.getInstance(collectedInfo.file.getProject()).getFileIndex().getModuleForFile(virtualFile);
    if (module == null) {
      return null;
    }
    String sonarKeyOfModule = projectSettings.getModuleKeys().get(module.getName());
    if (sonarKeyOfModule == null) {
      console.error("Module " + module.getName() + " is not associated to SonarQube");
      return null;
    }
    collectedInfo.componentKey = InspectionUtils.getComponentKey(sonarKeyOfModule, module, collectedInfo.file, collectedInfo.server.getVersion());

    SonarQubeIssueCache cache = p.getComponent(SonarQubeIssueCache.class);

    if (!cache.getModifiedFile().contains(collectedInfo.file)) {
      populateRemoteIssues(collectedInfo, console, collectedInfo.server, collectedInfo.componentKey);
    }
    return collectedInfo;
  }

  @Override
  public void apply(@NotNull PsiFile file, State annotationResult, @NotNull AnnotationHolder holder) {
    if (annotationResult == null) {
      return;
    }
    SonarQubeIssueCache cache = annotationResult.project.getComponent(SonarQubeIssueCache.class);
    if (cache.getModifiedFile().contains(annotationResult.file)) {
      for (final IssueOnPsiElement issueOnPsiElement : cache.getLocalIssuesByElement().get(file)) {
        createIssueAnnotation(holder, file, issueOnPsiElement);
      }
    } else {
      for (final ISonarIssue issue : annotationResult.remoteIssues) {
        createIssueAnnotation(holder, file, new IssueOnPsiElement(file, issue));
      }
    }
  }

  private void populateRemoteIssues(State state, SonarQubeConsole console, SonarQubeServer server, String componentKey) {
    try {
      ISonarWSClientFacade sonarClient = WSClientFactory.getInstance().getSonarClient(server);
      state.remoteIssues = sonarClient.getUnresolvedRemoteIssues(componentKey);
    } catch (SonarWSClientException e) {
      LOG.warn("Unable to retrieve remote issues", e);
      console.error("Unable to retrieve remote issues: " + e.getMessage());
      state.remoteIssues = new ArrayList<ISonarIssue>();
    }
  }

  public static void createIssueAnnotation(AnnotationHolder holder, PsiFile psiFile, IssueOnPsiElement issueOnPsiElement) {
    Annotation annotation;
    String message = InspectionUtils.getProblemMessage(issueOnPsiElement.getIssue());
    if (issueOnPsiElement.getIssue().line() == null) {
      annotation = createAnnotation(holder, message, psiFile);
      annotation.setFileLevelAnnotation(true);
    } else {
      PsiElement startElement = issueOnPsiElement.getPsiElement();
      if (startElement == null) {
        // There is no AST element on this line. Maybe a tabulation issue on a blank line?
        annotation = createAnnotation(holder, message, InspectionUtils.getLineRange(psiFile, issueOnPsiElement.getIssue()));
      } else if (startElement.isValid()) {
        TextRange lineRange = InspectionUtils.getLineRange(startElement);
        annotation = createAnnotation(holder, message, lineRange);
      } else {
        return;
      }
    }
    annotation.setTooltip(message);
  }

  private static Annotation createAnnotation(AnnotationHolder holder, String message, PsiElement location) {
    return holder.createWarningAnnotation(location, message);
  }

  private static Annotation createAnnotation(AnnotationHolder holder, String message, TextRange textRange) {
    return holder.createWarningAnnotation(textRange, message);
  }
}
