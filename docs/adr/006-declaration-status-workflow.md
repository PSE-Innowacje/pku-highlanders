# ADR-006: Declaration Status Workflow

## Status
Accepted

## Context
PRD section 11 defines three statuses for declarations. The application needs a clear state machine for the fill → save → submit workflow.

## Decision
Status is an enum (`DeclarationStatus`) with three values and the following transitions:
- `NIE_ZLOZONE` (Not submitted) → initial state after generation
- `NIE_ZLOZONE` → `ROBOCZE` (Draft) — when user saves field values
- `ROBOCZE` → `ROBOCZE` — when user re-saves (edits) field values
- `ROBOCZE` → `ZLOZONE` (Submitted) — when user submits; requires all required fields filled

Terminal state: `ZLOZONE` cannot transition to any other state. Fill and submit buttons are disabled in the UI for this status.

## Consequences
- Submit validates all required fields before allowing transition
- Submit returns a JSON document for browser download
- Backend enforces status transitions; frontend disables buttons for disallowed actions
