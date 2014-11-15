package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.service.project.ProjectStructureHelper;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataService;
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants;
import com.intellij.openapi.externalSystem.util.Order;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


@Order(ExternalSystemConstants.BUILTIN_SERVICE_ORDER + 1)
public class SonarModelDataService implements ProjectDataService<SonarResolverData, Module> {
  @NotNull
  private final ProjectStructureHelper myProjectStructureHelper;

  public SonarModelDataService(@NotNull ProjectStructureHelper helper) {
    myProjectStructureHelper = helper;
  }

  @NotNull
  @Override
  public Key<SonarResolverData> getTargetDataKey() {
    return SonarResolverData.KEY;
  }

  @Override
  public void importData(@NotNull Collection<DataNode<SonarResolverData>> toImport, @NotNull Project project, boolean synchronous) {
    if (toImport.isEmpty()) {
      return;
    }

    // one loop only.
    for (DataNode<SonarResolverData> dataNode : toImport) {
      SonarModelSettings settings = getSonarModelSettings(dataNode, project);

      SonarResolverData data = dataNode.getData();
      settings.setSonarProjectKey(data.getModelData().getProjectKey());
      settings.setSonarProjectName(data.getModelData().getProjectName());
    }
  }

  @Override
  public void removeData(@NotNull final Collection<? extends Module> toRemove, @NotNull Project project, boolean synchronous) {
    // nop
  }

  private SonarModelSettings getSonarModelSettings (DataNode<SonarResolverData> dataNode, Project project) {
    return getModule(dataNode, project).getComponent(SonarModelSettings.class);
  }

  private Module getModule (DataNode<SonarResolverData> dataNode, Project project) {
    return myProjectStructureHelper.findIdeModule((ModuleData) dataNode.getParent().getData(), project);
  }
}
