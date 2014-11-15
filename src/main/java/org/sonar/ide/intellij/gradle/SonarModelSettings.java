package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.components.*;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "GradleSonarRunnerSettings", storages = {@Storage(id = "default", file = StoragePathMacros.MODULE_FILE)})
public class SonarModelSettings implements PersistentStateComponent<SonarModelSettings>, ModuleComponent  {
  private String moduleName;
  private String sonarProjectKey;
  private String sonarProjectName;


  public SonarModelSettings() {
  }

  public SonarModelSettings(Module module) {
    moduleName = module.getName();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "SonarModelSettings";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public void moduleAdded() {
  }

  @Nullable
  @Override
  public SonarModelSettings getState() {
    return this;
  }

  @Override
  public void loadState(SonarModelSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getSonarProjectKey() {
    return sonarProjectKey;
  }

  public void setSonarProjectKey(String sonarProjectKey) {
    this.sonarProjectKey = sonarProjectKey;
  }

  public String getSonarProjectName() {
    return sonarProjectName;
  }

  public void setSonarProjectName(String sonarProjectName) {
    this.sonarProjectName = sonarProjectName;
  }
}
