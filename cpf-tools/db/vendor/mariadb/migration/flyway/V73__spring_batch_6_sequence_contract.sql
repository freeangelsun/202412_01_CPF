-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.
-- Spring Batch runtime must be stopped while sequence objects are replaced.
-- Existing sequence next values and persisted Spring Batch IDs are preserved as an exact monotonic lower bound.
USE batDB;

SET @cpf_sequence_start = GREATEST(
    1,
    (SELECT COALESCE(MAX(JOB_INSTANCE_ID), 0) + 1 FROM BATCH_JOB_INSTANCE)
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_INSTANCE_SEQ')
     LIMIT 1
);
SET @cpf_observed_next = 0;
SET @cpf_capture_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE'
        THEN 'SELECT NEXT VALUE FOR BATCH_JOB_INSTANCE_SEQ INTO @cpf_observed_next'
    WHEN @cpf_object_kind = 'BASE TABLE'
        THEN 'SELECT COALESCE(MAX(ID), 0) + 1 INTO @cpf_observed_next FROM BATCH_JOB_INSTANCE_SEQ'
    ELSE 'SELECT 0 INTO @cpf_observed_next'
END;
PREPARE cpf_sequence_stmt FROM @cpf_capture_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_sequence_start = GREATEST(
    @cpf_sequence_start,
    @cpf_observed_next
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_SEQ')
     LIMIT 1
);
SET @cpf_observed_next = 0;
SET @cpf_capture_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE'
        THEN 'SELECT NEXT VALUE FOR BATCH_JOB_SEQ INTO @cpf_observed_next'
    WHEN @cpf_object_kind = 'BASE TABLE'
        THEN 'SELECT COALESCE(MAX(ID), 0) + 1 INTO @cpf_observed_next FROM BATCH_JOB_SEQ'
    ELSE 'SELECT 0 INTO @cpf_observed_next'
END;
PREPARE cpf_sequence_stmt FROM @cpf_capture_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_sequence_start = GREATEST(
    @cpf_sequence_start,
    @cpf_observed_next
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_INSTANCE_SEQ')
     LIMIT 1
);
SET @cpf_drop_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE' THEN 'DROP SEQUENCE BATCH_JOB_INSTANCE_SEQ'
    WHEN @cpf_object_kind = 'BASE TABLE' THEN 'DROP TABLE BATCH_JOB_INSTANCE_SEQ'
    ELSE 'SELECT 1'
END;
PREPARE cpf_sequence_stmt FROM @cpf_drop_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_SEQ')
     LIMIT 1
);
SET @cpf_drop_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE' THEN 'DROP SEQUENCE BATCH_JOB_SEQ'
    WHEN @cpf_object_kind = 'BASE TABLE' THEN 'DROP TABLE BATCH_JOB_SEQ'
    ELSE 'SELECT 1'
END;
PREPARE cpf_sequence_stmt FROM @cpf_drop_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_create_sql = CONCAT(
    'CREATE SEQUENCE BATCH_JOB_INSTANCE_SEQ START WITH ',
    CAST(@cpf_sequence_start AS CHAR),
    ' MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB'
);
PREPARE cpf_sequence_stmt FROM @cpf_create_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;

SET @cpf_sequence_start = GREATEST(
    1,
    (SELECT COALESCE(MAX(JOB_EXECUTION_ID), 0) + 1 FROM BATCH_JOB_EXECUTION)
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_EXECUTION_SEQ')
     LIMIT 1
);
SET @cpf_observed_next = 0;
SET @cpf_capture_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE'
        THEN 'SELECT NEXT VALUE FOR BATCH_JOB_EXECUTION_SEQ INTO @cpf_observed_next'
    WHEN @cpf_object_kind = 'BASE TABLE'
        THEN 'SELECT COALESCE(MAX(ID), 0) + 1 INTO @cpf_observed_next FROM BATCH_JOB_EXECUTION_SEQ'
    ELSE 'SELECT 0 INTO @cpf_observed_next'
END;
PREPARE cpf_sequence_stmt FROM @cpf_capture_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_sequence_start = GREATEST(
    @cpf_sequence_start,
    @cpf_observed_next
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_JOB_EXECUTION_SEQ')
     LIMIT 1
);
SET @cpf_drop_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE' THEN 'DROP SEQUENCE BATCH_JOB_EXECUTION_SEQ'
    WHEN @cpf_object_kind = 'BASE TABLE' THEN 'DROP TABLE BATCH_JOB_EXECUTION_SEQ'
    ELSE 'SELECT 1'
END;
PREPARE cpf_sequence_stmt FROM @cpf_drop_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_create_sql = CONCAT(
    'CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH ',
    CAST(@cpf_sequence_start AS CHAR),
    ' MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB'
);
PREPARE cpf_sequence_stmt FROM @cpf_create_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;

SET @cpf_sequence_start = GREATEST(
    1,
    (SELECT COALESCE(MAX(STEP_EXECUTION_ID), 0) + 1 FROM BATCH_STEP_EXECUTION)
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_STEP_EXECUTION_SEQ')
     LIMIT 1
);
SET @cpf_observed_next = 0;
SET @cpf_capture_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE'
        THEN 'SELECT NEXT VALUE FOR BATCH_STEP_EXECUTION_SEQ INTO @cpf_observed_next'
    WHEN @cpf_object_kind = 'BASE TABLE'
        THEN 'SELECT COALESCE(MAX(ID), 0) + 1 INTO @cpf_observed_next FROM BATCH_STEP_EXECUTION_SEQ'
    ELSE 'SELECT 0 INTO @cpf_observed_next'
END;
PREPARE cpf_sequence_stmt FROM @cpf_capture_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_sequence_start = GREATEST(
    @cpf_sequence_start,
    @cpf_observed_next
);
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('BATCH_STEP_EXECUTION_SEQ')
     LIMIT 1
);
SET @cpf_drop_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE' THEN 'DROP SEQUENCE BATCH_STEP_EXECUTION_SEQ'
    WHEN @cpf_object_kind = 'BASE TABLE' THEN 'DROP TABLE BATCH_STEP_EXECUTION_SEQ'
    ELSE 'SELECT 1'
END;
PREPARE cpf_sequence_stmt FROM @cpf_drop_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_create_sql = CONCAT(
    'CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH ',
    CAST(@cpf_sequence_start AS CHAR),
    ' MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB'
);
PREPARE cpf_sequence_stmt FROM @cpf_create_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
