package org.sonar.ide.intellij.action;

import com.intellij.openapi.project.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonar.ide.intellij.action.associator.SonarQubeAssociator;
import org.sonar.ide.intellij.action.associator.facades.SonarProject;
import org.sonar.ide.intellij.associate.AssociateDialog;
import org.sonar.ide.intellij.config.ProjectSettings;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


// characterization tests
@SuppressWarnings("ConstantConditions")
public class SonarQubeActionTest {
  Project project;
  ProjectSettings settings;
  AssociateDialog dialog;
  SonarQubeAssociator associator;

  @Before
  public void setup() {
    project = mock(Project.class);
    settings = new ProjectSettings();
    dialog = mock(AssociateDialog.class);
    associator = mock(SonarQubeAssociator.class);
  }


  @Test
  public void initializesSelectedProjectInDialogWhenAssociated() {
    String serverId = "serverId";
    String projectKey = "projectKey";

    settings.setServerId(serverId);
    settings.setProjectKey(projectKey);

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, null);
    action.associate();

    verify(dialog).setSelectedSonarQubeProject(serverId, projectKey);
  }

  @Test
  public void initializeProjectFilterInDialogWhenNotAssociatedToProject() {
    String displayName = "displayName";
    when(associator.getSonarProjectName()).thenReturn(displayName);

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, associator);
    action.associate();

    verify(dialog).setFilter(displayName);
  }

  @Test
  public void doNotInitializeProjectFilterInDialogWithoutProjectName() {
    String displayName = null;
    when(associator.getSonarProjectName()).thenReturn(displayName);

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, associator);
    action.associate();

    verify(dialog, never ()).setFilter(anyString());
  }

  @Test
  public void doNotInitializeProjectFilterInDialogIfAlreadyAssociated() {
    settings.setServerId("id");
    settings.setProjectKey("key");

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, null);
    action.associate();

    verify(dialog, never ()).setFilter(anyString());
  }

  @Test
  public void doesShowDialogWhenAssociated() {
    settings.setServerId("serverId");
    settings.setProjectKey("projectKey");

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, null);
    action.associate();

    verify(dialog).show();
  }

  @Test
  public void doesShowDialogWhenNotAssociated() {
    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, associator);
    action.associate();

    verify(dialog).show();
  }

  @Test
  public void unassociatesProjectIfDialogClosedWithUnassociate() {
    settings.setServerId("serverId");
    settings.setProjectKey("projectKey");

    when(dialog.isExitCodeUnassociate()).thenReturn(true);

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, associator);
    action.associate();

    verify(dialog).show();
    assertThat(settings.isAssociated(), is(false));
  }

  @Test
  public void unassociatesProjectIfUnselectedInDialog() {
    settings.setServerId("serverId");
    settings.setProjectKey("projectKey");

    when(dialog.isExitCodeOk()).thenReturn(true);
    when(dialog.getSelectedSonarQubeProject()).thenReturn(null);

    SonarQubeAction action = new SonarQubeAction(null, settings, dialog, associator);
    action.associate();

    verify(dialog).show();
    assertThat(settings.isAssociated(), is(false));
  }

  @Test
  public void associateProjectIfSelectedInDialog() {
    SonarProject sonarProject = mock(SonarProject.class);

    when(dialog.isExitCodeOk()).thenReturn(true);
    when(dialog.getSelectedSonarProject()).thenReturn(sonarProject);

    SonarQubeAction action = new SonarQubeAction(project, settings, dialog, associator);
    action.associate();

    verify(dialog).show();
    verify(associator).associate(sonarProject);
  }
}
