-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.
DECLARE
    cpf_sequence_count NUMBER;
    cpf_sequence_start NUMBER;
BEGIN
    SELECT COUNT(*) INTO cpf_sequence_count
      FROM user_sequences WHERE sequence_name = 'BATCH_JOB_INSTANCE_SEQ';
    IF cpf_sequence_count = 0 THEN
        SELECT GREATEST(NVL(MAX(JOB_INSTANCE_ID), -1) + 1, 0) INTO cpf_sequence_start FROM BATCH_JOB_INSTANCE;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE BATCH_JOB_INSTANCE_SEQ START WITH ' || cpf_sequence_start ||
            ' MINVALUE 0 MAXVALUE 9223372036854775807 INCREMENT BY 1 ORDER NOCYCLE';
    END IF;
END;
/

DECLARE
    cpf_sequence_count NUMBER;
    cpf_sequence_start NUMBER;
BEGIN
    SELECT COUNT(*) INTO cpf_sequence_count
      FROM user_sequences WHERE sequence_name = 'BATCH_JOB_EXECUTION_SEQ';
    IF cpf_sequence_count = 0 THEN
        SELECT GREATEST(NVL(MAX(JOB_EXECUTION_ID), -1) + 1, 0) INTO cpf_sequence_start FROM BATCH_JOB_EXECUTION;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH ' || cpf_sequence_start ||
            ' MINVALUE 0 MAXVALUE 9223372036854775807 INCREMENT BY 1 ORDER NOCYCLE';
    END IF;
END;
/

DECLARE
    cpf_sequence_count NUMBER;
    cpf_sequence_start NUMBER;
BEGIN
    SELECT COUNT(*) INTO cpf_sequence_count
      FROM user_sequences WHERE sequence_name = 'BATCH_STEP_EXECUTION_SEQ';
    IF cpf_sequence_count = 0 THEN
        SELECT GREATEST(NVL(MAX(STEP_EXECUTION_ID), -1) + 1, 0) INTO cpf_sequence_start FROM BATCH_STEP_EXECUTION;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH ' || cpf_sequence_start ||
            ' MINVALUE 0 MAXVALUE 9223372036854775807 INCREMENT BY 1 ORDER NOCYCLE';
    END IF;
END;
/
