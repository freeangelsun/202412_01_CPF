-- Must return zero rows after successful install.
SELECT 'CPF_FIELD_ENCRYPTION_LEDGER' AS MISSING_OBJECT WHERE NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE upper(table_name)='CPF_FIELD_ENCRYPTION_LEDGER');
