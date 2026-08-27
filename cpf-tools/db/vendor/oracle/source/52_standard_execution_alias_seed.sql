-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=52_standard_execution_alias_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
DELETE FROM CPF_STANDARD_EXECUTION_ALIAS WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BADM-RLG-EX-0001' AS legacy_execution_id, 'BADMRL0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-CUT-CL-0001' AS legacy_execution_id, 'BBATCU0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-FL-0001' AS legacy_execution_id, 'BBATOP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-HB-0001' AS legacy_execution_id, 'BBATOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-SM-0001' AS legacy_execution_id, 'BBATOP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-CH-0001' AS legacy_execution_id, 'BREFAA0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-RT-0001' AS legacy_execution_id, 'BREFAA0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-TS-0001' AS legacy_execution_id, 'BREFAA0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0010' AS legacy_execution_id, 'OADMBA0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0012' AS legacy_execution_id, 'OADMBA0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0013' AS legacy_execution_id, 'OADMBA0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0014' AS legacy_execution_id, 'OADMBA0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0015' AS legacy_execution_id, 'OADMBA0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0016' AS legacy_execution_id, 'OADMBA0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0023' AS legacy_execution_id, 'OADMBA0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0024' AS legacy_execution_id, 'OADMBA0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0025' AS legacy_execution_id, 'OADMBA0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0027' AS legacy_execution_id, 'OADMBA0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0028' AS legacy_execution_id, 'OADMBA0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0029' AS legacy_execution_id, 'OADMBA0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0030' AS legacy_execution_id, 'OADMBA0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0032' AS legacy_execution_id, 'OADMBA0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0034' AS legacy_execution_id, 'OADMBA0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0011' AS legacy_execution_id, 'OADMBA0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0017' AS legacy_execution_id, 'OADMBA0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0018' AS legacy_execution_id, 'OADMBA0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0019' AS legacy_execution_id, 'OADMBA0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0026' AS legacy_execution_id, 'OADMBA0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0020' AS legacy_execution_id, 'OADMBA0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0021' AS legacy_execution_id, 'OADMBA0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0022' AS legacy_execution_id, 'OADMBA0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0031' AS legacy_execution_id, 'OADMBA0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0033' AS legacy_execution_id, 'OADMBA0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-01-0010' AS legacy_execution_id, 'OADMCD0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-01-0011' AS legacy_execution_id, 'OADMCD0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-02-0012' AS legacy_execution_id, 'OADMCD0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-03-0013' AS legacy_execution_id, 'OADMCD0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-04-0014' AS legacy_execution_id, 'OADMCD0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-01-0010' AS legacy_execution_id, 'OADMCF0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-01-0011' AS legacy_execution_id, 'OADMCF0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-02-0012' AS legacy_execution_id, 'OADMCF0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-03-0013' AS legacy_execution_id, 'OADMCF0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-04-0014' AS legacy_execution_id, 'OADMCF0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0010' AS legacy_execution_id, 'OADMCT0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0020' AS legacy_execution_id, 'OADMCT0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0030' AS legacy_execution_id, 'OADMCT0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0040' AS legacy_execution_id, 'OADMCT0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0050' AS legacy_execution_id, 'OADMCT0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0060' AS legacy_execution_id, 'OADMCT0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0070' AS legacy_execution_id, 'OADMCT0070' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-01-0001' AS legacy_execution_id, 'OADMDW0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-01-0002' AS legacy_execution_id, 'OADMDW0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-02-0003' AS legacy_execution_id, 'OADMDW0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-EXE-01-0001' AS legacy_execution_id, 'OADMEX0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-EXE-01-0002' AS legacy_execution_id, 'OADMEX0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0010' AS legacy_execution_id, 'OADMLG0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0011' AS legacy_execution_id, 'OADMLG0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0018' AS legacy_execution_id, 'OADMLG0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0020' AS legacy_execution_id, 'OADMLG0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0021' AS legacy_execution_id, 'OADMLG0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0012' AS legacy_execution_id, 'OADMLG0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0013' AS legacy_execution_id, 'OADMLG0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0014' AS legacy_execution_id, 'OADMLG0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0016' AS legacy_execution_id, 'OADMLG0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0018' AS legacy_execution_id, 'OADMLG0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0015' AS legacy_execution_id, 'OADMLG0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0017' AS legacy_execution_id, 'OADMLG0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0019' AS legacy_execution_id, 'OADMLG0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-01-0010' AS legacy_execution_id, 'OADMMS0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-01-0011' AS legacy_execution_id, 'OADMMS0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-02-0012' AS legacy_execution_id, 'OADMMS0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-03-0013' AS legacy_execution_id, 'OADMMS0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-04-0014' AS legacy_execution_id, 'OADMMS0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0010' AS legacy_execution_id, 'OADMNT0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0011' AS legacy_execution_id, 'OADMNT0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0014' AS legacy_execution_id, 'OADMNT0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-02-0012' AS legacy_execution_id, 'OADMNT0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-02-0016' AS legacy_execution_id, 'OADMNT0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-03-0013' AS legacy_execution_id, 'OADMNT0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-03-0015' AS legacy_execution_id, 'OADMNT0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0010' AS legacy_execution_id, 'OADMOB0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0011' AS legacy_execution_id, 'OADMOB0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0012' AS legacy_execution_id, 'OADMOB0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0001' AS legacy_execution_id, 'OADMOP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0002' AS legacy_execution_id, 'OADMOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0010' AS legacy_execution_id, 'OADMOP0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0020' AS legacy_execution_id, 'OADMOP0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0030' AS legacy_execution_id, 'OADMOP0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0034' AS legacy_execution_id, 'OADMOP0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0035' AS legacy_execution_id, 'OADMOP0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0036' AS legacy_execution_id, 'OADMOP0036' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0040' AS legacy_execution_id, 'OADMOP0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0041' AS legacy_execution_id, 'OADMOP0041' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0042' AS legacy_execution_id, 'OADMOP0042' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0043' AS legacy_execution_id, 'OADMOP0043' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0050' AS legacy_execution_id, 'OADMOP0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-02-0031' AS legacy_execution_id, 'OADMOP0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-02-0042' AS legacy_execution_id, 'OADMOP0044' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0032' AS legacy_execution_id, 'OADMOP0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0037' AS legacy_execution_id, 'OADMOP0037' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0038' AS legacy_execution_id, 'OADMOP0038' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0039' AS legacy_execution_id, 'OADMOP0039' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0043' AS legacy_execution_id, 'OADMOP0045' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0044' AS legacy_execution_id, 'OADMOP0046' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0045' AS legacy_execution_id, 'OADMOP0047' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-04-0022' AS legacy_execution_id, 'OADMOP0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-04-0044' AS legacy_execution_id, 'OADMOP0048' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-05-0011' AS legacy_execution_id, 'OADMOP0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-05-0021' AS legacy_execution_id, 'OADMOP0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0033' AS legacy_execution_id, 'OADMOP0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0040' AS legacy_execution_id, 'OADMOP0049' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0042' AS legacy_execution_id, 'OADMOP0051' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0010' AS legacy_execution_id, 'OADMPE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0011' AS legacy_execution_id, 'OADMPE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0014' AS legacy_execution_id, 'OADMPE0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0015' AS legacy_execution_id, 'OADMPE0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0019' AS legacy_execution_id, 'OADMPE0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0020' AS legacy_execution_id, 'OADMPE0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0024' AS legacy_execution_id, 'OADMPE0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0025' AS legacy_execution_id, 'OADMPE0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0029' AS legacy_execution_id, 'OADMPE0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0030' AS legacy_execution_id, 'OADMPE0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0034' AS legacy_execution_id, 'OADMPE0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0016' AS legacy_execution_id, 'OADMPE0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0021' AS legacy_execution_id, 'OADMPE0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0026' AS legacy_execution_id, 'OADMPE0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0031' AS legacy_execution_id, 'OADMPE0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0012' AS legacy_execution_id, 'OADMPE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0013' AS legacy_execution_id, 'OADMPE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0017' AS legacy_execution_id, 'OADMPE0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0018' AS legacy_execution_id, 'OADMPE0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0022' AS legacy_execution_id, 'OADMPE0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0023' AS legacy_execution_id, 'OADMPE0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0027' AS legacy_execution_id, 'OADMPE0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0028' AS legacy_execution_id, 'OADMPE0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0032' AS legacy_execution_id, 'OADMPE0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0033' AS legacy_execution_id, 'OADMPE0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0035' AS legacy_execution_id, 'OADMPE0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0001' AS legacy_execution_id, 'OADMRE0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0002' AS legacy_execution_id, 'OADMRE0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0003' AS legacy_execution_id, 'OADMRE0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0004' AS legacy_execution_id, 'OADMRE0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0006' AS legacy_execution_id, 'OADMRE0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0007' AS legacy_execution_id, 'OADMRE0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0009' AS legacy_execution_id, 'OADMRE0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0010' AS legacy_execution_id, 'OADMRE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0011' AS legacy_execution_id, 'OADMRE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0005' AS legacy_execution_id, 'OADMRE0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0008' AS legacy_execution_id, 'OADMRE0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0012' AS legacy_execution_id, 'OADMRE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0013' AS legacy_execution_id, 'OADMRE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-CR-0001' AS legacy_execution_id, 'OADMRL0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DL-0001' AS legacy_execution_id, 'OADMRL0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DL-0002' AS legacy_execution_id, 'OADMRL0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DW-0001' AS legacy_execution_id, 'OADMRL0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-IS-0001' AS legacy_execution_id, 'OADMRL0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0001' AS legacy_execution_id, 'OADMRL0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0002' AS legacy_execution_id, 'OADMRL0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0003' AS legacy_execution_id, 'OADMRL0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0004' AS legacy_execution_id, 'OADMRL0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-01-0010' AS legacy_execution_id, 'OADMSE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-01-0012' AS legacy_execution_id, 'OADMSE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0011' AS legacy_execution_id, 'OADMSE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0013' AS legacy_execution_id, 'OADMSE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0014' AS legacy_execution_id, 'OADMSE0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0015' AS legacy_execution_id, 'OADMSE0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0010' AS legacy_execution_id, 'OADMSV0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0020' AS legacy_execution_id, 'OADMSV0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0030' AS legacy_execution_id, 'OADMSV0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0040' AS legacy_execution_id, 'OADMSV0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0050' AS legacy_execution_id, 'OADMSV0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0060' AS legacy_execution_id, 'OADMSV0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0070' AS legacy_execution_id, 'OADMSV0070' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0001' AS legacy_execution_id, 'OADMTR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0002' AS legacy_execution_id, 'OADMTR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0003' AS legacy_execution_id, 'OADMTR0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0004' AS legacy_execution_id, 'OADMTR0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0005' AS legacy_execution_id, 'OADMTR0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0006' AS legacy_execution_id, 'OADMTR0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-01-0010' AS legacy_execution_id, 'OADMTR0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-01-0011' AS legacy_execution_id, 'OADMTR0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-04-0013' AS legacy_execution_id, 'OADMTR0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-05-0012' AS legacy_execution_id, 'OADMTR0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OBAT-OPR-01-0003' AS legacy_execution_id, 'OBATOP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OBAT-OPR-02-0002' AS legacy_execution_id, 'OBATOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ADM-01-1001' AS legacy_execution_id, 'OMBWAD1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ADM-03-1002' AS legacy_execution_id, 'OMBWAD1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-01-0001' AS legacy_execution_id, 'OMBWAP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-01-0003' AS legacy_execution_id, 'OMBWAP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-02-0002' AS legacy_execution_id, 'OMBWAP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-05-0004' AS legacy_execution_id, 'OMBWAP0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-01-0001' AS legacy_execution_id, 'OMBWAT0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-02-0002' AS legacy_execution_id, 'OMBWAT0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-DL-0003' AS legacy_execution_id, 'OMBWAT0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUD-01-0001' AS legacy_execution_id, 'OMBWUD0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0004' AS legacy_execution_id, 'OMBWAU0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0005' AS legacy_execution_id, 'OMBWAU0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0007' AS legacy_execution_id, 'OMBWAU0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0001' AS legacy_execution_id, 'OMBWAU0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0002' AS legacy_execution_id, 'OMBWAU0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0003' AS legacy_execution_id, 'OMBWAU0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-03-0006' AS legacy_execution_id, 'OMBWAU0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-04-0008' AS legacy_execution_id, 'OMBWAU0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-CUS-01-1001' AS legacy_execution_id, 'OMBWCU1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DSH-01-0001' AS legacy_execution_id, 'OMBWDS0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DWN-01-0002' AS legacy_execution_id, 'OMBWDW0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DWN-01-1001' AS legacy_execution_id, 'OMBWDW1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-EMP-01-0001' AS legacy_execution_id, 'OMBWEM0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-EMP-03-0002' AS legacy_execution_id, 'OMBWEM0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MNU-01-1001' AS legacy_execution_id, 'OMBWMN1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MNU-03-1002' AS legacy_execution_id, 'OMBWMN1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MSK-02-1001' AS legacy_execution_id, 'OMBWMS1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-01-0001' AS legacy_execution_id, 'OMBWNT0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-02-0002' AS legacy_execution_id, 'OMBWNT0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-03-0003' AS legacy_execution_id, 'OMBWNT0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORD-01-1001' AS legacy_execution_id, 'OMBWOR1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORG-01-0001' AS legacy_execution_id, 'OMBWOR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORG-03-0002' AS legacy_execution_id, 'OMBWOR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-0002' AS legacy_execution_id, 'OMBWPE0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-0003' AS legacy_execution_id, 'OMBWPE0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-1001' AS legacy_execution_id, 'OMBWPE1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-02-0004' AS legacy_execution_id, 'OMBWPE0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-03-1002' AS legacy_execution_id, 'OMBWPE1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PRD-01-1001' AS legacy_execution_id, 'OMBWPR1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ROL-01-1001' AS legacy_execution_id, 'OMBWRO1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ROL-03-1002' AS legacy_execution_id, 'OMBWRO1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-01-0001' AS legacy_execution_id, 'OMBWSC0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-03-0002' AS legacy_execution_id, 'OMBWSC0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-04-0003' AS legacy_execution_id, 'OMBWSC0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SET-01-1001' AS legacy_execution_id, 'OMBWSE1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-USR-QY-0000' AS legacy_execution_id, 'OMBWUS0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-USR-QY-0001' AS legacy_execution_id, 'OMBWUS0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0001' AS legacy_execution_id, 'OEDUAA0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0002' AS legacy_execution_id, 'OREFAA0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0003' AS legacy_execution_id, 'OREFAA0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0099' AS legacy_execution_id, 'OREFAA0099' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0001' AS legacy_execution_id, 'OREFAA0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0010' AS legacy_execution_id, 'OREFAA0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0020' AS legacy_execution_id, 'OREFAA0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0030' AS legacy_execution_id, 'OREFAA0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0001' AS legacy_execution_id, 'OREFAA0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0002' AS legacy_execution_id, 'OREFAA0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0003' AS legacy_execution_id, 'OREFAA0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-04-0001' AS legacy_execution_id, 'OREFAA0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-04-0002' AS legacy_execution_id, 'OREFAA0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-0001' AS legacy_execution_id, 'OREFAA0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-0002' AS legacy_execution_id, 'OREFAA0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-9001' AS legacy_execution_id, 'OREFAA9001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-0001' AS legacy_execution_id, 'OREFAA0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-0010' AS legacy_execution_id, 'OREFAA0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-9001' AS legacy_execution_id, 'OREFAA9002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0001' AS legacy_execution_id, 'OREFAA0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0002' AS legacy_execution_id, 'OREFAA0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0003' AS legacy_execution_id, 'OREFAA0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0004' AS legacy_execution_id, 'OREFAA0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0005' AS legacy_execution_id, 'OREFAA0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0006' AS legacy_execution_id, 'OREFAA0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0007' AS legacy_execution_id, 'OREFAA0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0008' AS legacy_execution_id, 'OREFAA0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0009' AS legacy_execution_id, 'OREFAA0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0010' AS legacy_execution_id, 'OREFAA0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0011' AS legacy_execution_id, 'OREFAA0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0012' AS legacy_execution_id, 'OREFAA0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0013' AS legacy_execution_id, 'OREFAA0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0015' AS legacy_execution_id, 'OREFAA0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0016' AS legacy_execution_id, 'OREFAA0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0017' AS legacy_execution_id, 'OREFAA0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0018' AS legacy_execution_id, 'OREFAA0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0019' AS legacy_execution_id, 'OREFAA0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0020' AS legacy_execution_id, 'OREFAA0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0030' AS legacy_execution_id, 'OREFAA0036' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0031' AS legacy_execution_id, 'OREFAA0037' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0032' AS legacy_execution_id, 'OREFAA0038' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0033' AS legacy_execution_id, 'OREFAA0039' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0034' AS legacy_execution_id, 'OREFAA0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0035' AS legacy_execution_id, 'OREFAA0041' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0036' AS legacy_execution_id, 'OREFAA0042' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0040' AS legacy_execution_id, 'OREFAA0043' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0051' AS legacy_execution_id, 'OREFAA0051' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0060' AS legacy_execution_id, 'OREFAA0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0072' AS legacy_execution_id, 'OREFAA0072' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0073' AS legacy_execution_id, 'OREFAA0073' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0080' AS legacy_execution_id, 'OREFAA0080' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0001' AS legacy_execution_id, 'OREFAA0044' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0002' AS legacy_execution_id, 'OREFAA0045' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0003' AS legacy_execution_id, 'OREFAA0046' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0001' AS legacy_execution_id, 'OREFAA0047' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0002' AS legacy_execution_id, 'OREFAA0048' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0003' AS legacy_execution_id, 'OREFAA0049' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0004' AS legacy_execution_id, 'OREFAA0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0005' AS legacy_execution_id, 'OREFAA0052' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0006' AS legacy_execution_id, 'OREFAA0053' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0007' AS legacy_execution_id, 'OREFAA0054' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0008' AS legacy_execution_id, 'OREFAA0055' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-14-0001' AS legacy_execution_id, 'OREFAA0056' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-15-0001' AS legacy_execution_id, 'OREFAA0057' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0001' AS legacy_execution_id, 'OREFAA0058' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0002' AS legacy_execution_id, 'OREFAA0059' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0003' AS legacy_execution_id, 'OREFAA0061' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0004' AS legacy_execution_id, 'OREFAA0062' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0005' AS legacy_execution_id, 'OREFAA0063' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0006' AS legacy_execution_id, 'OREFAA0064' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-17-0001' AS legacy_execution_id, 'OREFAA0065' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-17-0002' AS legacy_execution_id, 'OREFAA0066' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0001' AS legacy_execution_id, 'OREFQR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0002' AS legacy_execution_id, 'OREFQR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0003' AS legacy_execution_id, 'OREFQR0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0004' AS legacy_execution_id, 'OREFQR0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0005' AS legacy_execution_id, 'OREFQR0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
