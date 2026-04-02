# ADR-001: Keycloak for User Management

## Status
Accepted

## Context
The application needs user authentication, role management, and a user directory. Options considered:
1. Custom user table in PostgreSQL with Spring Security
2. Keycloak as external identity provider

## Decision
Use Keycloak 26.x as the identity provider. Users and roles (Administrator, Kontrahent) are managed in Keycloak. The backend acts as an OAuth2 Resource Server validating JWTs. The frontend uses `keycloak-js` for login flow with PKCE.

## Consequences
- No User entity in the backend database — Keycloak is the source of truth for identity
- User references in the database use Keycloak UUIDs (`sub` claim from JWT)
- Backend calls Keycloak Admin API (via `KeycloakAdminService`) to list users by role
- Admin credentials for Keycloak API are stored in `application.yml`
- Realm configuration is reproducible via `keycloak/pku-realm.json` import on startup
