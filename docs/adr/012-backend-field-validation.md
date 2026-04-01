# ADR-012: Backend Validation for Declaration Field Values

## Status
Accepted

## Context
Declaration type fields have a `dataType` format like `Number (9,3)` meaning up to 9 integer digits and 3 decimal digits. The frontend had HTML `max` attributes on number inputs, but these don't prevent users from typing or pasting values that exceed the limits. Invalid data could be saved and submitted.

## Decision
Add validation at both layers:
- **Backend**: `DeclarationService.validateFieldValue()` parses the `Number (N,D)` format and checks integer/decimal digit counts. Called during both `saveDeclaration()` and `submitDeclaration()`. Throws `BusinessException` with a clear Polish-language message identifying the field.
- **Frontend**: `validateFieldValue()` in `fieldFormulas.ts` performs the same check on every field change, showing inline error messages and disabling the save button when errors exist.

## Alternatives Considered
- **Frontend-only validation** — Bypassable via API calls or browser dev tools.
- **Bean Validation annotations on entity** — Field values are stored as a JSON map (`Map<String, String>`), not as typed entity fields, so standard `@Size`/`@Digits` annotations don't apply.

## Consequences
- Validation runs on both save and submit, ensuring consistency
- Error messages reference field name and code for easy identification
- Frontend provides immediate feedback; backend is the authoritative guard
- Both regex patterns handle the `Number (N,D)` and `Number(N,D)` formats via `\s*`
