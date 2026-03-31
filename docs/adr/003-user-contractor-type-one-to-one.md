# ADR-003: One-to-One User to Contractor Type Assignment

## Status
Accepted

## Context
Each Kontrahent user needs to be associated with a contractor type. Initially designed as 1-to-N (user has multiple types), later simplified to 1-to-1 per business requirement update.

## Decision
`user_contractor_type_assignments` table with a unique constraint on `keycloak_user_id`. Each user maps to exactly one contractor type. The `keycloak_user_id` is a VARCHAR(36) storing the Keycloak UUID — not a foreign key to any users table since users live in Keycloak.

## Consequences
- Simple dropdown (select) in the admin UI instead of multi-select
- Contractor type determines which declaration types the user can generate
- Assignment chain: User → ContractorType → DeclarationTypes (via ManyToMany)
