# ADR-009: Programmatic Keycloak User Seeding via Admin REST API

## Status
Accepted

## Context
Test contractor users (28 entries from kontrahenci.xlsx) need to be created in Keycloak with the Kontrahent role and linked to contractor data in the app DB. Keycloak's `--import-realm` only imports on first startup and doesn't update existing realms.

## Decision
Create users programmatically via Keycloak Admin REST API in a Spring Boot `ApplicationRunner` seeder (`ContractorDataSeeder`). The seeder authenticates as admin, creates users, assigns roles, and seeds the corresponding `contractor_data` and `user_contractor_type_assignments` rows.

## Alternatives Considered
- **Realm JSON import** — Only works on first Keycloak startup. Adding users later requires manual realm re-import or volume deletion.
- **Keycloak Admin CLI scripts** — External dependency, harder to coordinate with Spring Boot startup order.

## Consequences
- Seeder includes a `waitForKeycloak()` retry loop (up to 60s) since Keycloak may not be ready when Spring Boot starts
- Seeder is idempotent: checks `contractorDataRepository.count() > 0` before running
- `@Order(10)` ensures it runs after `ContractorTypeSeeder` (order 1) and `DeclarationTypeSeeder` (order 2)
- Keycloak Admin credentials must be accessible to the Spring Boot application
