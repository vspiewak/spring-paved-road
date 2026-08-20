package com.vspiewak.pavedroad.cucumber.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Generic, service-agnostic HTTP step definitions : requests against the running application via
 * {@link RestTestClient}, plus status and JSON-path assertions. Depends only on a {@code
 * RestTestClient} bean (provided by {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} +
 * {@code @AutoConfigureRestTestClient}), so it is reusable as-is by any service. Service-specific
 * behaviour stays in the service's own step classes.
 *
 * <p>State is scenario-scoped : cucumber-spring creates a fresh instance per scenario.
 *
 * <p>Consumers add {@code com.vspiewak.pavedroad.cucumber.http} to their Cucumber glue.
 */
@RequiredArgsConstructor
public class HttpSteps {

  private final RestTestClient client;

  private EntityExchangeResult<String> response;

  @When("I send a GET request to {string}")
  public void iSendAGetRequestTo(String url) {
    response = client.get().uri(url).exchange().returnResult(String.class);
  }

  @Then("the response status is {int}")
  public void theResponseStatusIs(int expectedStatus) {
    assertThat(response.getStatus().value()).isEqualTo(expectedStatus);
  }

  @Then("the response json path {string} is {string}")
  public void theResponseJsonPathIsString(String jsonPath, String expected) {
    Object value = JsonPath.read(response.getResponseBody(), jsonPath);
    assertThat(String.valueOf(value)).isEqualTo(expected);
  }

  @Then("the response json path {string} has {int} element(s)")
  public void theResponseJsonPathHasElements(String jsonPath, int expectedCount) {
    List<Object> value = JsonPath.read(response.getResponseBody(), jsonPath);
    assertThat(value).hasSize(expectedCount);
  }
}
