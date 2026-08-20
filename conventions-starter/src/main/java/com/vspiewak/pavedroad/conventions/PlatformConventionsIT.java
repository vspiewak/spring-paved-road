package com.vspiewak.pavedroad.conventions;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The platform runtime conventions : the context loads, the application is named (traces need a
 * {@code service.name}) and named after its Maven artifactId, and the actuator health endpoint (the
 * deploy probe) answers.
 *
 * <p>Services extend this with their own context wiring — {@code @SpringBootTest},
 * {@code @AutoConfigureRestTestClient}, plus whatever the context needs (testcontainers imports,
 * mocked partner beans) :
 *
 * <pre>{@code
 * @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
 * @AutoConfigureRestTestClient
 * @Import(Containers.class)
 * class ConventionsIT extends PlatformConventionsIT {}
 * }</pre>
 */
public abstract class PlatformConventionsIT {

  @Autowired protected RestTestClient client;
  @Autowired protected ApplicationContext context;
  @Autowired protected Environment environment;

  @Test
  protected void shouldLoadApplicationContext() {
    assertThat(context).isNotNull();
  }

  @Test
  protected void shouldDefineSpringApplicationName() {
    assertThat(environment.getProperty("spring.application.name"))
        .as("spring.application.name must be set so traces have a meaningful service.name")
        .isNotBlank();
  }

  @Test
  protected void shouldHaveSpringApplicationNameEqualToMavenArtifactId() throws Exception {
    assertThat(environment.getProperty("spring.application.name"))
        .as("spring.application.name must match the Maven artifactId")
        .isEqualTo(mavenArtifactId());
  }

  @Test
  protected void shouldHaveActuatorHealth() {
    var result = client.get().uri("/actuator/health").exchange().returnResult(String.class);
    assertThat(result.getStatus().value()).isEqualTo(200);
    assertThat((String) JsonPath.read(result.getResponseBody(), "$.status")).isEqualTo("UP");
  }

  /**
   * The module's artifactId read straight from pom.xml (the surefire and IDE working directory is
   * the module root) — deliberately NOT {@code BuildProperties}, whose backing
   * build-info.properties only exists after the Maven build-info goal ran, making IDE runs fail.
   */
  private static String mavenArtifactId() throws Exception {
    var pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("pom.xml"));
    NodeList children = pom.getDocumentElement().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if ("artifactId".equals(child.getNodeName())) {
        return child.getTextContent().trim();
      }
    }
    throw new IllegalStateException("no <artifactId> found in pom.xml");
  }
}
