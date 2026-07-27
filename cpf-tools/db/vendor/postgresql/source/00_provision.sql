-- CPF PostgreSQL V63 provision contract
-- Physical database and runtime/migration roles are created by initialize-cpf-vendor-database.ps1.
-- This SQL creates the CPF logical schemas inside the selected physical database.
CREATE SCHEMA IF NOT EXISTS cpfDB;
CREATE SCHEMA IF NOT EXISTS cmnDB;
CREATE SCHEMA IF NOT EXISTS admDB;
CREATE SCHEMA IF NOT EXISTS batDB;
CREATE SCHEMA IF NOT EXISTS bzaDB;
CREATE SCHEMA IF NOT EXISTS mbrDB;
CREATE SCHEMA IF NOT EXISTS accDB;
CREATE SCHEMA IF NOT EXISTS refDB;
