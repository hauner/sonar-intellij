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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.model.ISonarIssue;

import javax.annotation.CheckForNull;

public class InspectionUtils {

  private static final Logger LOG = Logger.getInstance(InspectionUtils.class);

  private static final char DELIMITER = ':';
  private static final char PACKAGE_DELIMITER = '.';
  private static final String DEFAULT_PACKAGE_NAME = "[default]";

  private InspectionUtils() {
    // Utility class
  }

  @Nullable
  public static String getComponentKey(String moduleKey, Module module, PsiFile file, String serverVersion) {
    final VirtualFile virtualFile = file.getVirtualFile();
    if (null == virtualFile) {
      return null;
    }
    final String filePath = virtualFile.getPath();
    // IntelliJ is only compatible with 4.1+
    if (!serverVersion.startsWith("4.1")) {
      return getComponentKeyForSonarQube42(moduleKey, module, virtualFile);
    }
    return getComponentKeyForSonarQube41(moduleKey, file, virtualFile, filePath);
  }

  private static String getComponentKeyForSonarQube41(String moduleKey, PsiFile file, VirtualFile virtualFile, String filePath) {
    if (file instanceof PsiJavaFile) {
      return getJavaComponentKey(moduleKey, (PsiJavaFile) file);
    }
    final StringBuilder result = new StringBuilder();
    result.append(moduleKey).append(":");

    VirtualFile sourceRootForFile = ProjectFileIndex.SERVICE.getInstance(file.getProject()).getSourceRootForFile(virtualFile);
    // getSourceRootForFile doesn't work in phpstorm for some reasons
    if (null == sourceRootForFile) {
      sourceRootForFile = ProjectFileIndex.SERVICE.getInstance(file.getProject()).getContentRootForFile(virtualFile);
    }

    if (sourceRootForFile != null) {
      final String sourceRootForFilePath = sourceRootForFile.getPath() + "/";

      String baseFileName = filePath.replace(sourceRootForFilePath, "");

      if (baseFileName.equals(file.getName())) {
        result.append("[root]/");
      }

      result.append(baseFileName);
    }
    return result.toString();
  }

  @CheckForNull
  private static String getComponentKeyForSonarQube42(String moduleKey, Module module, VirtualFile file) {
    final StringBuilder result = new StringBuilder();
    result.append(moduleKey).append(":");
    String relativePath = computeRelativePath(module, file);
    if (relativePath != null) {
      result.append(relativePath);
      return result.toString();
    }
    return null;
  }

  @CheckForNull
  public static String computeRelativePath(Module module, VirtualFile file) {
    String rootPath = getModuleRootPath(module);
    if (rootPath == null) {
      return null;
    }
    String filePath = file.getPath();
    if (filePath.startsWith(rootPath)) {
      return filePath.substring(rootPath.length());
    }
    return null;
  }

  @CheckForNull
  public static String getModuleRootPath(Module module) {
    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

    VirtualFile[] contentRoots = rootManager.getContentRoots();
    if (contentRoots.length != 1) {
      LOG.error("Module " + module + " contains " + contentRoots.length + " content roots and this is not supported");
      return null;
    }
    return contentRoots[0].getPath() + "/";
  }

  @NotNull
  public static TextRange getLineRange(@NotNull PsiFile psiFile, ISonarIssue issue) {
    Project project = psiFile.getProject();
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    Document document = documentManager.getDocument(psiFile.getContainingFile());
    if (document == null) {
      return TextRange.EMPTY_RANGE;
    }
    int line = issue.line() != null ? issue.line() - 1 : 0;
    return getTextRangeForLine(document, line);
  }

  public static TextRange getLineRange(@NotNull PsiElement psiElement) {
    Project project = psiElement.getProject();
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    Document document = documentManager.getDocument(psiElement.getContainingFile().getContainingFile());
    if (document == null) {
      return TextRange.EMPTY_RANGE;
    }
    int line = document.getLineNumber(psiElement.getTextOffset());
    int lineEndOffset = document.getLineEndOffset(line);
    return new TextRange(psiElement.getTextOffset(), lineEndOffset);
  }

  private static TextRange getTextRangeForLine(Document document, int line) {
    try {
      int lineStartOffset = document.getLineStartOffset(line);
      int lineEndOffset = document.getLineEndOffset(line);
      return new TextRange(lineStartOffset, lineEndOffset);
    } catch (IndexOutOfBoundsException e) {
      // Local file should be different than remote
      return TextRange.EMPTY_RANGE;
    }
  }

  @Nullable
  public static PsiElement getStartElementAtLine(@NotNull final PsiFile file, ISonarIssue issue) {
    if (issue.line() == null) {
      return file;
    }
    int ijLine = issue.line() - 1;
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    PsiElement element = null;
    try {
      if (document != null) {
        final int offset = document.getLineStartOffset(ijLine);
        element = file.getViewProvider().findElementAt(offset);
        if (element != null && document.getLineNumber(element.getTextOffset()) != ijLine) {
          element = element.getNextSibling();
        }
      }
    } catch (@NotNull final IndexOutOfBoundsException ignore) {
      // Ignore this exception
    }

    while (element != null && element.getTextLength() == 0) {
      element = element.getNextSibling();
    }

    return element;
  }

  public static String getProblemMessage(@NotNull ISonarIssue issue) {
    return issue.isNew() ? "NEW: " + issue.message() : issue.message();
  }

  private static String getJavaComponentKey(final String moduleKey, final PsiJavaFile file) {
    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      @Override
      public String compute() {
        String result = null;
        String packageName = file.getPackageName();
        if (StringUtils.isWhitespace(packageName)) {
          packageName = DEFAULT_PACKAGE_NAME;
        }
        String fileName = StringUtils.substringBeforeLast(file.getName(), ".");
        if (moduleKey != null) {
          result = new StringBuilder()
              .append(moduleKey).append(DELIMITER).append(packageName)
              .append(PACKAGE_DELIMITER).append(fileName)
              .toString();
        }
        return result;
      }
    });
  }
}
