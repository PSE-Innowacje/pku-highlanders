# ADR-008: Contractor Data Stored in Application Database

## Status
Accepted

## Context
Users in the Kontrahent role need extended profile data (KRS, NIP, address, agreement number, contractor name, etc.) beyond what Keycloak stores (username, email, first/last name, roles). We needed to decide where to store this domain-specific data.

## Decision
Store extended contractor data in a dedicated `contractor_data` table in the application PostgreSQL database, linked to Keycloak users via `keycloak_user_id`. Keycloak remains responsible only for authentication and role management.

## Alternatives Considered
- **Keycloak user attributes** — Keycloak supports custom user attributes, but querying/filtering on them is limited, they mix auth with domain concerns, and they would couple our business logic to Keycloak's API for every read.
- **Extend Keycloak with custom SPI** — Too complex for the data requirements, introduces deployment coupling.

## Consequences
- Clear separation: Keycloak handles auth, app DB handles domain data
- `ContractorData` entity with 1:1 relationship to Keycloak user via `keycloak_user_id`
- The application must resolve Keycloak user IDs to contractor data via joins across two data sources
- Test user seeding creates users in Keycloak programmatically via Admin REST API, then seeds `contractor_data` rows in the app DB
