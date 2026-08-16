-- BZA employee safe default alignment.
-- Existing rows are preserved; only the default for newly inserted employees changes.

USE bzaDB;

ALTER TABLE bza_employee
    ALTER COLUMN employment_status SET DEFAULT 'EMPLOYED';
