@US08 @EP03 @erp-tenant-service
# User Story: US08 - Manage employee records, role matrix, and branch performance history
Feature: Employee Lifecycle, Role Matrix, and Branch History
  As a workshop administrator
  I want to register, update, deactivate employees, inspect available role matrices, and view branch history
  So that operational staff assignments remain organized and least-privilege compliant

  Background:
    Given the "erp-tenant-service" REST API is available at "/api/v1"

  @US08 @happy-path
  # User Story: US08 - Register an employee profile and query role matrix
  Scenario: Workshop administrator creates an employee profile and retrieves role matrix
    Given an authenticated user with role "WORKSHOP_ADMIN" authorized for user "user-tech-01"
    When the client sends a "POST" request to "/api/v1/employees" with JSON payload:
      """
      {
        "userId": "user-tech-01",
        "branchId": "branch-uuid-01",
        "position": "Senior Mechanic",
        "specialtyId": "spec-uuid-01"
      }
      """
    Then the response status code should be 201
    And the JSON response should contain a generated "id"
    When the client sends a "GET" request to "/api/v1/profiles/roles"
    Then the response status code should be 200
    And the JSON response should contain a list of assignable roles

  @US08 @negative
  # User Story: US08 - Reject employee lookup for non-existent employee ID
  Scenario: Requesting an unknown employee returns HTTP 404 Not Found
    Given an authenticated user with role "WORKSHOP_ADMIN"
    When the client sends a "GET" request to "/api/v1/employees/00000000-0000-0000-0000-000000009999"
    Then the response status code should be 404
    And the JSON response should contain an error message indicating employee not found
