@US06 @EP02 @erp-tenant-service
# User Story: US06 - Manage physical branches and commercial subscriptions per location
Feature: Physical Branches and Location Subscriptions
  As a workshop administrator
  I want to register, update, and manage commercial subscriptions for physical workshop branches
  So that multi-branch operations scale within the contracted SaaS plan limits

  Background:
    Given the "erp-tenant-service" REST API is available at "/api/v1"

  @US06 @happy-path
  # User Story: US06 - Create a physical branch under an authorized workshop
  Scenario: Workshop administrator creates a new physical branch
    Given an authenticated user with role "WORKSHOP_ADMIN" authorized for workshop "workshop-uuid-01"
    When the client sends a "POST" request to "/api/v1/branches" with JSON payload:
      """
      {
        "workshopId": "workshop-uuid-01",
        "name": "Sede Surquillo",
        "address": "Av. Angamos Este 2100",
        "phone": "+51987654321",
        "bayCapacity": 6
      }
      """
    Then the response status code should be 201
    And the JSON response should contain a generated "id"
    And the JSON response field "name" should equal "Sede Surquillo"

  @US06 @negative
  # User Story: US06 - Reject branch query for non-existent branch ID
  Scenario: Fetching a non-existent branch returns HTTP 404 Not Found
    Given an authenticated user with role "WORKSHOP_ADMIN"
    When the client sends a "GET" request to "/api/v1/branches/00000000-0000-0000-0000-000000009999"
    Then the response status code should be 404
    And the JSON response should contain an error message indicating branch not found
