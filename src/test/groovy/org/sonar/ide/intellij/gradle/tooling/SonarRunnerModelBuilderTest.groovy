package org.sonar.ide.intellij.gradle.tooling

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.sonar.runner.plugins.SonarRunnerPlugin;
import spock.lang.Specification;


class SonarRunnerModelBuilderTest extends Specification {
  SonarRunnerModelBuilder builder = new SonarRunnerModelBuilder()

  def setup () {
  }

  def "can build model" () {
    expect:
    builder.canBuild(SonarRunnerModel.name)
  }

  def "does build model when sonar runner plugin is applied" () {
    given:
    Project project = ProjectBuilder.builder().withName('root').build()
    project.plugins.apply (SonarRunnerPlugin)

    project.sonarRunner {
      sonarProperties {
        property "sonar.projectKey", "key"
        property "sonar.projectName", "name"
      }
    }

    when:
    SonarRunnerModel result = builder.buildAll (SonarRunnerModel.name, project) as SonarRunnerModel

    then:
    result.projectKey == 'key'
    result.projectName == 'name'
  }

  def "returns null when sonar runner plugin is not applied" () {
    given:
    Project project = ProjectBuilder.builder().withName('root').build()

    when:
    def result = builder.buildAll (SonarRunnerModel.name, project)

    then:
    !result
  }

}
