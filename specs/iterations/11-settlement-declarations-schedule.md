# Iteration 11 — Settlement declarations schedule improvements

## Goal
As a user in role Administrator, I want to be able to generate settlement declarations instances

## Requirements
- remove current functionality, where user in role Kontrahent have button "Generate settlement declarations" ("Wygeneruj oświadczenia"), which generates settlement declarations base on user's assigned contractor type, which has assigned settlement declaration types.
- generating settlement declaration instances should be available for user in role Administrator. In the "Edit schedule" dialog user will have button "Generate settlement declarations" ("Wygeneruj oświadczenia"), which will generate settlement declarations based on schedule. It should generate it for all users with assigned contractor type, containing given settlement declaration type.
- settlement declaration instances are generated mostly in the same way as currently, but take into account schedule, e.g. when we have 2 entries: day 1 and day 15, we should have 2 separate settlement declarations. Number of day is included in settlement declaration number (in method generateNumber() subperiod is hardcoded to 1, but it should be number of day.)
- there should be a cron job which runs every night at 1 a.m. and generates settlement declarations applicable for current day, according to the configured schedule.

## Acceptance criteria
As in requirements

## Notes
