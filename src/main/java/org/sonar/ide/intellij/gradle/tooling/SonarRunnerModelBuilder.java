package org.sonar.ide.intellij.gradle.tooling;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.tooling.ErrorMessageBuilder;
import org.jetbrains.plugins.gradle.tooling.ModelBuilderService;

import java.util.Map;


public class SonarRunnerModelBuilder implements ModelBuilderService {
  public static final String SONAR_RUNNER_TASK_NAME = "sonarRunner";
  public static final String SONAR_PROPERTIES_NAME = "sonarProperties";

  public SonarRunnerModelBuilder() {
  }

  @Override
  public boolean canBuild(String modelName) {
    return modelName.equals(SonarRunnerModel.class.getName());
  }

  @Override
  public Object buildAll(String modelName, Project project) {
    Map<String, Object> props = properties(project);

    if (props == null) {
      return null;
    }

    return new SonarRunnerModelDefault(
        (String) props.get("sonar.projectKey"),
        (String) props.get("sonar.projectName")
    );
  }

  private static Map<String, Object> properties(Project project) {
    TaskContainer container = project.getTasks();

    Task task = container.findByName(SONAR_RUNNER_TASK_NAME);
    if (task == null) {
      return null;
    }

    Object properties = task.property(SONAR_PROPERTIES_NAME);
    if (properties == null) {
      return null;
    }

    //noinspection unchecked
    return (Map<String, Object>) properties;
  }

  @NotNull
  @Override
  public ErrorMessageBuilder getErrorMessageBuilder(@NotNull Project project, @NotNull Exception e) {
    return ErrorMessageBuilder.create(project, e, "SonarQube - SonarRunnerModelBuilder");
  }
}
