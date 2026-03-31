# ADR-004: Declaration Types Seeded from PRD

## Status
Accepted

## Context
PRD section 15.6 defines 15 settlement declaration types (a-o), each with a fixed set of fields. These are static reference data.

## Decision
Store declaration types and their fields in the database (`declaration_types` + `declaration_type_fields` tables), seeded on startup by `DeclarationTypeSeeder`. Each type has a code (e.g. "OP.a", "OZE.b") derived from the fee type acronym (PRD §5) and the subsection letter.

## Consequences
- 15 types with 67 total fields seeded on first startup
- Fields define the dynamic form structure: position, code, data type, name, required flag, unit
- ManyToMany relationship between ContractorType and DeclarationType allows admins to assign which types apply to which contractors
- Declaration instances reference a type and inherit its field structure
