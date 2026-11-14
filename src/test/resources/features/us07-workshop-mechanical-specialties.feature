@US07 @EP03 @erp-tenant-service
# User Story: US07 - Manage the workshop catalog of mechanical specialties
Feature: Workshop Mechanical Specialties Catalog
  As a workshop administrator
  I want to create, query, update, delete, and audit certified mechanical specialties
  So that workshop services and technician competencies are clearly classified

  Background:
    Given the "erp-tenant-service" REST API is available at "/api/v1"

  @US07 @happy-path
  # User Story: US07 - Register a new mechanical specialty in a workshop
  Scenario: Workshop administrator creates a new mechanical specialty
    Given an authenticated user with role "WORKSHOP_ADMIN" for workshop "workshop-uuid-01"
    When the client sends a "POST" request to "/api/v1/workshops/workshop-uuid-01/specialties" with JSON payload:
      """
      {
        "name": "ABS Braking Systems",
        "description": "Diagnostics and repair of electronic ABS and EBD brake modules"
      }
      """
    Then the response status code should be 201
    And the JSON response should contain a generated "id"
    And the JSON response field "name" should equal "ABS Braking Systems"

  @US07 @negative
  # User Story: US07 - Reject empty specialty name
  Scenario: Creating a specialty without a name returns HTTP 400 Bad Request
    Given an authenticated user with role "WORKSHOP_ADMIN" for workshop "workshop-uuid-01"
    When the client sends a "POST" request to "/api/v1/workshops/workshop-uuid-01/specialties" with JSON payload:
      """
      {
        "name": "",
        "description": "Missing specialty title"
      }
      """
    Then the response status code should be 400
    And the JSON response should contain a validation error for "name"
