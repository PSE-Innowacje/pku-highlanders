# Iteration 08 — Administrator can manage settlement declarations schedule calendar (Harmonogram składania oświadczeń)

## Goal
We want to have functionality, where user in role Administrator can configure and manage settlement declarations schedule calendar (Harmonogram składania oświadczeń) with detailed schedule entries

## Requirements
- We will modify view settlement declarations ("Typy oświadczeń")
- On the view a settlement declarations add new button "Edit schedule" (Edytuj harmonogram)
- After user clicks "Edit schedule" open new dialog with schedule entries table
- The schedule entries table contains the following columns:
  - "Pozycja terminarza" — dropdown field with enum values based on PRD point 12, ignore "Typy" column
  - "Dzień" — editable field with selection of day in range from 1 to 31
  - "Godzina" — editable field with 24-hour format selection
  - "Typ dnia" — dropdown field with values based on PRD point 13
- User can add new rows in schedule entries table
- User can edit "Dzień", "Godzina" and "Typ dnia" fields
- User can save changes in schedule entries
- User in role Kontrahent cannot see this view

## Acceptance criteria
- user in role Administrator can open "Edit schedule" dialog for given settlement declaration
- user in role Administrator can edit "Dzień", "Godzina" and "Typ dnia" fields
- user in role Administrator can save changes in schedule entries