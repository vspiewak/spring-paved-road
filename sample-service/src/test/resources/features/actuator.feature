Feature: Actuator

  Scenario: The service is healthy
    When I send a GET request to "/actuator/health"
    Then the response status is 200
    And the response json path "$.status" is "UP"

  Scenario: Unknown endpoints are a 404
    When I send a GET request to "/bad-endpoint"
    Then the response status is 404
    And the response json path "$.status" is "404"
