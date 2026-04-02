# Iteration 03 — Administrator can view settlement declaration types

## Goal
We want to have functionality, where user in role Administrator can view settlement types (PRD point 15.6)

## Requirements
- We have fixed list of settlement declarations (see PRD point 15.6)
- Please change settlement declarations names e.g. "a) Opłata Przejściowa — OSDp / OSDn" -> "OP.a", "f) Opłata kogeneracyjna — Odbiorcy końcowi / Wytwórcy / Magazyny" -> "OKO.f". Valid acronyms list mapping is in PRD point 5.
- In PRD point 15.6 you have also data structures for each settlement declaration.
- Please let Administrator navigate from list of settlement declarations to settlement declaration preview.
- Settlement declaration should have fields with data types as defined in tables in PRD 15.6 for each settlement declaration.
- Please ignore column "Uwagi"
- application will have menu on left hand side, where user can navigate between viewes.
- user in role Administrator will have new view "Settlement declarations" ("Typy oświadczeń") in webapp where he can view settlement types.
- user in role Kontrahent does not have access to this view

## Acceptance criteria
- user in role Administrator after logging in can go the Settlement declarations list view.
- user in role Administrator can navigate to settlement declaration preview by clicking preview button on Settlement declarations list.
