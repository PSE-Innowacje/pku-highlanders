# ADR-002: Contractor Types with System Flag

## Status
Accepted

## Context
The application has 5 predefined contractor types from the PRD (OSDp, OSDn, OK, Wyt, Mag). Admins should be able to add new types but not modify/delete the predefined ones.

## Decision
Use a `system` boolean flag on the `contractor_types` table. Rows with `system=true` are seeded on startup and cannot be edited or deleted. The service layer enforces this business rule.

## Consequences
- `ContractorTypeSeeder` (ApplicationRunner) seeds 5 types on first startup
- Create/update/delete operations check the `system` flag before proceeding
- Simple and effective — no separate tables or complex authorization logic needed
