package org.sonar.ide.intellij.gradle;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intellij.openapi.externalSystem.util.ExternalSystemConstants.EXTERNAL_SYSTEM_ID_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.jetbrains.plugins.gradle.util.GradleConstants.SYSTEM_ID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GradleProjectManagerTest {
  Project project;
  ModuleManager moduleManager;

  @Before
  public void setUp() throws Exception {
    project = mock(Project.class);
    moduleManager = mock(ModuleManager.class);
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void doesCheckIfModuleIsAGradleModule () {
    Module module = mock(Module.class);
    when(module.getOptionValue(EXTERNAL_SYSTEM_ID_KEY)).thenReturn(SYSTEM_ID.toString());

    GradleProjectManager gradle = new GradleProjectManager(project, moduleManager);

    assertThat(gradle.isGradleModule(module), is(true));
  }

  @Test
  public void doesCheckIfProjectIsAGradleProject () {
    Module module1 = mock(Module.class);
    when(module1.getName()).thenReturn("Module 1");
    Module module2 = mock(Module.class);
    when(module1.getName()).thenReturn("Project Module");
    when(module2.getOptionValue(EXTERNAL_SYSTEM_ID_KEY)).thenReturn(SYSTEM_ID.toString());

    when(project.getName()).thenReturn("Project Module");
    when (moduleManager.findModuleByName("Project Module")).thenReturn(module2);

    GradleProjectManager gradle = new GradleProjectManager(project, moduleManager);

    assertThat(gradle.isGradleProject(), is(true));
  }
}
