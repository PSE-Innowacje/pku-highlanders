# Iteration 06 — Kontrahent can fill settlement declarations assigned to him/her

## Goal
We want to have functionality, where user in role Kontrahent can fill settlement declarations assigned to him/her

## Requirements
- We will extend view "Settlement declarations dashboard" ("Lista oświadczeń")
- Initial status of generated settlement declaration is "Nie złożone"
- Next to each row user can see 2 buttons: "Fill settlement declaration" ("Wypełnij oświadczenie") amd "Submit settlement declaration" ("Wyślij oświadczenie")
- After clicking "Fill settlement declaration" ("Wypełnij oświadczenie") button a popup shows up, where user can fill values in the settlement declaration instance
- After user is done with filling settlement declaration he can save it, which changes status to "Robocze"
- Settlement declaration number and status cannot be edited by user
- After clicking "Submit settlement declaration" ("Wyślij oświadczenie"), where all required fields are filled, a JSON will be generated and dowloaded and status will change to "Złożone"
- User in role Administrator cannot see this view

## Acceptance criteria
- user in role Kontrahent after logging in can go the "Generate settlement declarations" view and generate settlement declarations assigned to him
