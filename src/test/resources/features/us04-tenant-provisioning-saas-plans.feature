@US04 @EP02 @erp-tenant-service
# User Story: US04 - Provision organizations (tenants) and manage SaaS plans
Feature: Multi-Tenant Organization Provisioning and SaaS Plans
  As a SuperAdmin or workshop administrator
  I want to query SaaS subscription plans, provision isolated tenants, update plans/status, and customize branding
  So that each organization operates in an isolated workspace with enforced plan quotas

  Background:
    Given the "erp-tenant-service" REST API is available at "/api/v1"

  @US04 @happy-path
  # User Story: US04 - SuperAdmin provisions a new tenant organization
  Scenario: SuperAdmin provisions a new isolated tenant with an active plan
    Given an authenticated user with role "SUPERADMIN"
    When the client sends a "POST" request to "/api/v1/tenants" with JSON payload:
      """
      {
        "name": "Motores del Sur SAC",
        "planCode": "PROFESSIONAL",
        "adminEmail": "admin@motoresdelsur.pe"
      }
      """
    Then the response status code should be 201
    And the JSON response should contain a generated "id"
    And the JSON response field "status" should equal "ACTIVE"
    And the JSON response field "planCode" should equal "PROFESSIONAL"

  @US04 @happy-path
  # User Story: US04 - Workshop admin updates tenant branding
  Scenario: Workshop admin customizes tenant corporate branding
    Given an authenticated user with role "WORKSHOP_ADMIN" for tenant "tenant-uuid-01"
    When the client sends a "PATCH" request to "/api/v1/tenants/tenant-uuid-01/branding" with JSON payload:
      """
      {
        "primaryColor": "#1E3A8A",
        "logoUrl": "https://cdn.driveos.pe/logos/motores.png"
      }
      """
    Then the response status code should be 200
    And the JSON response field "primaryColor" should equal "#1E3A8A"

  @US04 @negative
  # User Story: US04 - Reject tenant provisioning with invalid plan code
  Scenario: Provisioning a tenant with an unknown SaaS plan returns HTTP 400
    Given an authenticated user with role "SUPERADMIN"
    When the client sends a "POST" request to "/api/v1/tenants" with JSON payload:
      """
      {
        "name": "Taller Sin Plan",
        "planCode": "INVALID_PLAN_XYZ",
        "adminEmail": "admin@invalid.pe"
      }
      """
    Then the response status code should be 400
    And the JSON response should contain an error message indicating an invalid subscription plan
