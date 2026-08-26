Feature: Orders API

  Background:
    Given The following documents exist in the "orders" collection:
      | orderId | amount |
      | 1       | 42     |
      | 2       | 7      |

  Scenario: List all orders
    When I send a GET request to "/orders/v1/orders"
    Then the response status is 200
    And the response json path "$" has 2 elements
    And the response json path "$[0].orderId" is "1"

  Scenario: Get one order by its identifier
    When I send a GET request to "/orders/v1/orders/2"
    Then the response status is 200
    And the response json path "$.amount" is "7"

  Scenario: Unknown orders are a 404, in the platform's error shape
    When I send a GET request to "/orders/v1/orders/999"
    Then the response status is 404
    And the response content type is "application/problem+json"
    And the response json path "$.status" is "404"
    And the response json path "$.title" is "Not Found"
    And the response json path "$.instance" is "/orders/v1/orders/999"
    And the response json path "$.service" is "sample-service"

