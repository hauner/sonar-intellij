package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants;
import com.intellij.openapi.externalSystem.util.Order;
import com.intellij.openapi.util.KeyValue;
import com.intellij.util.containers.ContainerUtil;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension;
import org.jetbrains.plugins.gradle.util.GradleConstants;
import org.sonar.ide.intellij.gradle.tooling.SonarRunnerModel;
import org.sonar.ide.intellij.gradle.tooling.SonarRunnerModelBuilder;

import java.util.*;

@Order(ExternalSystemConstants.UNORDERED)
public class SonarResolverExtension extends AbstractProjectResolverExtension {

  @Override
  public void populateModuleExtraModels(@NotNull IdeaModule gradleModule, @NotNull DataNode<ModuleData> ideModule) {
    SonarRunnerModel sonarRunnerModel = resolverCtx.getExtraProject(gradleModule, SonarRunnerModel.class);
    if (sonarRunnerModel != null) {
      SonarModelData userData = new SonarModelData();
      userData.setProjectKey(sonarRunnerModel.getProjectKey());
      userData.setProjectName(sonarRunnerModel.getProjectName());
      ideModule.createChild(SonarResolverData.KEY, new SonarResolverData(GradleConstants.SYSTEM_ID, userData));
    }
    nextResolver.populateModuleExtraModels(gradleModule, ideModule);
  }

  @NotNull
  @Override
  public List<KeyValue<String, String>> getExtraJvmArgs() {
    return new ArrayList<KeyValue<String, String>>();
  }

  @NotNull
  @Override
  public Set<Class> getExtraProjectModelClasses() {
    return Collections.<Class>singleton(SonarRunnerModel.class);
  }

  @NotNull
  @Override
  public List<String> getExtraCommandLineArgs() {
    return new ArrayList<String>();
  }

  @NotNull
  @Override
  public Set<Class> getToolingExtensionsClasses() {
    return ContainerUtil.<Class>set (
        SonarRunnerModelBuilder.class
    );
  }
}
