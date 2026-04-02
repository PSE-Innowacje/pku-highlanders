# ADR-005: Declaration Field Values Stored as JSON

## Status
Accepted

## Context
Declaration instances need to store user-entered values for dynamic fields defined by the declaration type. Options:
1. Separate `declaration_field_values` table (normalized, one row per field per declaration)
2. JSON column on the `declarations` table (denormalized, all values in one column)

## Decision
Store field values as a JSON text column (`field_values`) on the `declarations` table, using a JPA `@Convert` with `MapToJsonConverter` (Map<String,String> ↔ JSON string). Chose JPA converter over Hibernate `@JdbcTypeCode(SqlTypes.JSON)` due to missing `hibernate-jackson` module in Hibernate 7.2.x / Spring Boot 4.0.5.

## Consequences
- Simple schema — no join needed to load declaration with values
- Field codes are map keys, values are strings (frontend handles type coercion for numbers)
- Custom `MapToJsonConverter` uses Jackson `ObjectMapper` for serialization
- Column type is `text` (not `jsonb`) to avoid Hibernate JSON type registration issues
- Trade-off: no SQL-level querying of individual field values, but not needed for this application
