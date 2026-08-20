-- MBW employee safe default alignment.
-- Existing rows are preserved; only the default for newly inserted employees changes.

USE backofficeDB;

ALTER TABLE mbw_employee
    ALTER COLUMN employment_status SET DEFAULT 'EMPLOYED';
