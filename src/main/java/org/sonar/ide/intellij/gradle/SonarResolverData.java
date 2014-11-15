package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.project.AbstractExternalEntityData;
import org.jetbrains.annotations.NotNull;


public class SonarResolverData extends AbstractExternalEntityData {
  private static final long serialVersionUID = 1L;

  @NotNull
  public static final Key<SonarResolverData> KEY = Key.create(SonarResolverData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

  private SonarModelData modelData;

  public SonarResolverData(@NotNull ProjectSystemId owner, @NotNull SonarModelData modelData) {
    super(owner);
    this.modelData = modelData;
  }

  public SonarModelData getModelData() {
    return modelData;
  }
}
