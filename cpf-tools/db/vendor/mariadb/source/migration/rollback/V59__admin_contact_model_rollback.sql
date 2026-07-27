-- V59 rollback: remove contact model additions only.

USE bzaDB;
ALTER TABLE bza_employee
    DROP COLUMN IF EXISTS office_phone_no;

USE admDB;
ALTER TABLE adm_operator_profile
    DROP COLUMN IF EXISTS OFFICE_PHONE_NO,
    DROP COLUMN IF EXISTS MOBILE_NO;
