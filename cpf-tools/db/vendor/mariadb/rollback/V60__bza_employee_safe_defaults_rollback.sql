-- V60 rollback: restore the pre-QA employee status default without changing existing rows.

USE bzaDB;

ALTER TABLE bza_employee
    ALTER COLUMN employment_status SET DEFAULT 'ACTIVE';
