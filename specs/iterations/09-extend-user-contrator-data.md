# Iteration 09 — user in role Kontrahent has more data

## Goal
user in role Kontrahent has to have more data and we want to import some users into the system

## Requirements
- user in role Kontrahent has more data about himself. Currently we have only login, password. We need more data about user, eg. assigned contractor ("Nazwa kontrahenta"), "Skrót kontrahenta" etc. Data fields are in PRD point 6.
- user in role Kontrahent has can have another attribute: "Agreement number" ("Numer umowy")
- user in role Administrator should be able to edit attribute: "Agreement number" ("Numer umowy") in the "User contractor types" ("Przypisz typy kontrahentów") view
  - create test users as in file: specs/data/kontrahenci.xlsx. Test users will have default password "test". If some data is missing, generate random.
- user in role Administrator can view detailed data about users in the "User contractor types" ("Przypisz typy kontrahentów") view. When he clicks user name, some modal dialog with data shows up.
- when user logs in into the system, on the dashboard we want to see his name, not his login. Below he should also see his "Numer umowy" and "Nazwa kontrahenta"

## Acceptance criteria
- contractor user has more data and test users are imported
