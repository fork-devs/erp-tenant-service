@US05 @EP02 @erp-tenant-service
# User Story: US05 - Manage workshop owners and automotive workshops lifecycle
Feature: Workshop Owners and Automotive Workshops Lifecycle
  As an organization owner or administrator
  I want to register, query, and update owner profiles and automotive workshops
  So that the legal and commercial structure of the tenant is properly formalized

  Background:
    Given the "erp-tenant-service" REST API is available at "/api/v1"

  @US05 @happy-path
  # User Story: US05 - Register an owner profile and automotive workshop
  Scenario: Authenticated owner registers a workshop with valid RUC and legal name
    Given an authenticated user "user-owner-01" with role "WORKSHOP_ADMIN"
    And a valid owner profile "owner-uuid-01" linked to "user-owner-01"
    When the client sends a "POST" request to "/api/v1/workshops" with JSON payload:
      """
      {
        "ownerId": "owner-uuid-01",
        "businessName": "Autocentro Precision EIRL",
        "ruc": "20512345678",
        "address": "Av. Javier Prado Este 1420, Lima"
      }
      """
    Then the response status code should be 201
    And the JSON response should contain a generated "id"
    And the JSON response field "ruc" should equal "20512345678"

  @US05 @negative
  # User Story: US05 - Reject workshop creation with malformed RUC
  Scenario: Registering a workshop with an invalid RUC format returns HTTP 400
    Given an authenticated user "user-owner-01" with role "WORKSHOP_ADMIN"
    When the client sends a "POST" request to "/api/v1/workshops" with JSON payload:
      """
      {
        "ownerId": "owner-uuid-01",
        "businessName": "Taller RUC Invalido",
        "ruc": "123",
        "address": "Lima"
      }
      """
    Then the response status code should be 400
    And the JSON response should contain a validation error for "ruc"
