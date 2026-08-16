-- Verify CPF R6 V106 persistent data-quality provider schema
DO $$
BEGIN
 IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE upper(table_name)='CPF_DATA_QUALITY_RULE' AND upper(column_name)='PARAMETERS_PAYLOAD') THEN RAISE EXCEPTION 'PARAMETERS_PAYLOAD missing'; END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE upper(table_name)='CPF_DATA_QUALITY_QUARANTINE' AND upper(column_name)='VIOLATION_PAYLOAD') THEN RAISE EXCEPTION 'VIOLATION_PAYLOAD missing'; END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE upper(table_name)='CPF_DATA_QUALITY_OPERATION') THEN RAISE EXCEPTION 'CPF_DATA_QUALITY_OPERATION missing'; END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE upper(table_name)='CPF_DATA_QUALITY_OPERATION' AND constraint_type='PRIMARY KEY') THEN RAISE EXCEPTION 'operation primary key missing'; END IF;
END $$;
SELECT COUNT(*) AS persistent_dq_operations FROM CPF_DATA_QUALITY_OPERATION;
