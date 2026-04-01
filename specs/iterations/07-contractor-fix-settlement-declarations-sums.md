# Iteration 07 — When Kontrahent fills settlement declarations, some fields are read only sums

## Goal
When Kontrahent fills settlement declarations, some fields are read only sums

## Requirements
- In settlement declarations some fields are only sums of other fields, which should be summed automatically and are not editable
- Identify such fields by this pattern: "(suma 2.1–2.4)", which means this field is sum of field with numbers 2.1, 2.2, 2.3 and 2.4 etc.
- Fields should have validations, e.g. only positive values
- Where field have some defined range in "Typ danych" column in PRD number 15.6, limit to this range only
- Type "Number (9,3)" treat as: 9 digits before comma (dot), 3 digits after

## Acceptance criteria
- Some fields in settlement declarations are not editable sums of other fields
- Validations are applied
