# Iteration 05 — Kontrahent can view and generate settlement declarations assigned to him/her

## Goal
We want to have functionality, where user in role Kontrahent can view list of settlement declarations assigned to him/her and generate them

## Requirements
- On the UI the menu header for User in role Kontrahent will be "PKU Rozliczenia"
- We will have new view "Settlement declarations dashboard" ("Lista oświadczeń")
- User in role Kontrahent can see settlement declarations list assigned to him
- For a time being we want to have a button above this list "Generate settlement declarations" ("Wygeneruj oświadczenia"), which will generate settlement declarations base on user's assigned contractor type, which has assigned settlement declaration types.
- Generated settlement declaration is an instance of settlement declaration type (separate for each user)
- Each settlement declaration list element is an instance of settlement declaration type
- Apart from fields from settlement declaration type, each instance will have some number (format as in PRD point 7, for now don't bother with data, because not everything is available, just generate random according to format), status (PRD point 11)
- User in role Administrator cannot see this view

## Acceptance criteria
- user in role Kontrahent after logging in can go the "Generate settlement declarations" view and generate settlement declarations assigned to him
