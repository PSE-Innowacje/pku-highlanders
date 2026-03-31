# ADR-007: Schedule Entries per Declaration Type

## Status
Accepted

## Context
Administrators need to define schedule calendars for each declaration type — specifying deadlines for submission, invoice issuance, corrective submissions, and post-correction invoices (PRD §12). Each entry has a position type, day of month, hour, and day type (calendar/working day from PRD §13).

## Decision
Create a `schedule_entries` table with a foreign key to `declaration_types`. Each declaration type can have multiple schedule entries. Entries are managed via replace-all semantics: `PUT /api/admin/declaration-types/{code}/schedule` replaces all existing entries for that type. This avoids complex partial-update logic and aligns with how admins will use the UI (edit all entries at once in a modal).

## Consequences
- `ScheduleEntry` entity with @ManyToOne to DeclarationType, cascade ALL + orphanRemoval
- Schedule positions are stored as strings (not enum) for flexibility, but UI constrains to the 4 values from PRD §12
- Day types are strings constrained by the UI to "Dzień kalendarzowy" or "Dzień roboczy" (PRD §13)
- Replace-all on save means the UI always sends the complete list — simpler than tracking adds/edits/deletes
