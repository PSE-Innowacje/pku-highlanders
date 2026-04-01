# ADR-010: Admin-Driven Declaration Generation via Schedule

## Status
Accepted

## Context
Originally, Kontrahent users generated their own declarations via a "Wygeneruj oświadczenia" button. This needed to change so that declaration generation is driven by administrators through schedule entries, and automated via a nightly cron job.

## Decision
- Remove the Kontrahent-facing generation endpoint (`POST /api/declarations/generate`)
- Add an admin endpoint (`POST /api/admin/declaration-types/{code}/generate`) that generates declarations for all users assigned to contractor types linked to the given declaration type
- Add a `schedule_day` column to the `declarations` table to track which schedule entry triggered the generation, enabling multiple declarations per user+type (one per schedule day)
- Add a nightly cron job (`@Scheduled(cron = "0 0 1 * * *")`) that finds schedule entries matching the current day of month and triggers generation

## Alternatives Considered
- **Keep Kontrahent self-service generation** — Doesn't align with the business requirement that administrators control when declarations are created based on the schedule.
- **Generate all declarations at once for the entire month** — Would create declarations for future schedule days that may not be relevant yet.

## Consequences
- `Declaration` entity gains `scheduleDay` field (nullable Integer for backward compatibility)
- Duplicate prevention uses `existsByKeycloakUserIdAndDeclarationTypeIdAndScheduleDay` (3-part key)
- Declaration number format includes `scheduleDay` as the subperiod component
- `@EnableScheduling` added to the main application class
- Kontrahent users see only pre-generated declarations; they cannot create their own
