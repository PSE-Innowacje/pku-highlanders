# Iteration 11 - extend settlement declaration view

## Goal
Settlement declaration view is extended with contractor data, preview and view is split into 2

## Requirements
- generated settlement declaration (JSON file) should contain extended contractor data (described in PRD 15.4a)
- in the "Settlement declarations dashboard" ("Lista oświadczeń") after submitting settlement declaration, we still want to be able preview it by clicking on the name in Numer oświadczenia column. View the same as for filling settlement declaration, but read only.
- split the "Settlement declarations dashboard" ("Lista oświadczeń") view into 2, one should contain settlement declarations in statuses "Nie złożone" and "Robocze". Name of this view will be "Lista oświadczeń - niezłożone". Second view would contain settlement declarations in status "Złożone" and name of view would be "Lista oświadczeń - złożone"
- some range validations when filling settlement declarations are not working, e.g. when type is number (9,3), I'm able to fill number longer than 9 digits. It should be limited to 9 digits before decimal point and 3 after decimal digits.
- Submitting settlement declaration with validation errors is not possible until fixed.

## Acceptance criteria
As in requirements

## Notes