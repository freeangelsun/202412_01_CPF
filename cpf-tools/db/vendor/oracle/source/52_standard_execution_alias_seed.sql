-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=52_standard_execution_alias_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
DELETE FROM cpf_standard_execution_alias WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';
MERGE INTO cpf_standard_execution_alias tgt USING (
SELECT 'BADM-RLG-EX-0001' legacy_execution_id, 'BADMRL0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-CUT-CL-0001' legacy_execution_id, 'BBATCU0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-FL-0001' legacy_execution_id, 'BBATOP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-HB-0001' legacy_execution_id, 'BBATOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-SM-0001' legacy_execution_id, 'BBATOP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-CH-0001' legacy_execution_id, 'BREFAA0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-RT-0001' legacy_execution_id, 'BREFAA0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-TS-0001' legacy_execution_id, 'BREFAA0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0010' legacy_execution_id, 'OADMBA0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0012' legacy_execution_id, 'OADMBA0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0013' legacy_execution_id, 'OADMBA0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0014' legacy_execution_id, 'OADMBA0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0015' legacy_execution_id, 'OADMBA0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0016' legacy_execution_id, 'OADMBA0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0023' legacy_execution_id, 'OADMBA0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0024' legacy_execution_id, 'OADMBA0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0025' legacy_execution_id, 'OADMBA0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0027' legacy_execution_id, 'OADMBA0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0028' legacy_execution_id, 'OADMBA0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0029' legacy_execution_id, 'OADMBA0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0030' legacy_execution_id, 'OADMBA0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0032' legacy_execution_id, 'OADMBA0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0034' legacy_execution_id, 'OADMBA0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0011' legacy_execution_id, 'OADMBA0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0017' legacy_execution_id, 'OADMBA0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0018' legacy_execution_id, 'OADMBA0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0019' legacy_execution_id, 'OADMBA0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0026' legacy_execution_id, 'OADMBA0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0020' legacy_execution_id, 'OADMBA0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0021' legacy_execution_id, 'OADMBA0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0022' legacy_execution_id, 'OADMBA0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0031' legacy_execution_id, 'OADMBA0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0033' legacy_execution_id, 'OADMBA0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-01-0010' legacy_execution_id, 'OADMCD0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-01-0011' legacy_execution_id, 'OADMCD0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-02-0012' legacy_execution_id, 'OADMCD0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-03-0013' legacy_execution_id, 'OADMCD0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-04-0014' legacy_execution_id, 'OADMCD0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-01-0010' legacy_execution_id, 'OADMCF0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-01-0011' legacy_execution_id, 'OADMCF0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-02-0012' legacy_execution_id, 'OADMCF0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-03-0013' legacy_execution_id, 'OADMCF0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-04-0014' legacy_execution_id, 'OADMCF0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0010' legacy_execution_id, 'OADMCT0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0020' legacy_execution_id, 'OADMCT0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0030' legacy_execution_id, 'OADMCT0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0040' legacy_execution_id, 'OADMCT0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0050' legacy_execution_id, 'OADMCT0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0060' legacy_execution_id, 'OADMCT0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0070' legacy_execution_id, 'OADMCT0070' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-01-0001' legacy_execution_id, 'OADMDW0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-01-0002' legacy_execution_id, 'OADMDW0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-02-0003' legacy_execution_id, 'OADMDW0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-EXE-01-0001' legacy_execution_id, 'OADMEX0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-EXE-01-0002' legacy_execution_id, 'OADMEX0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0010' legacy_execution_id, 'OADMLG0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0011' legacy_execution_id, 'OADMLG0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0018' legacy_execution_id, 'OADMLG0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0020' legacy_execution_id, 'OADMLG0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0021' legacy_execution_id, 'OADMLG0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0012' legacy_execution_id, 'OADMLG0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0013' legacy_execution_id, 'OADMLG0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0014' legacy_execution_id, 'OADMLG0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0016' legacy_execution_id, 'OADMLG0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0018' legacy_execution_id, 'OADMLG0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0015' legacy_execution_id, 'OADMLG0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0017' legacy_execution_id, 'OADMLG0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0019' legacy_execution_id, 'OADMLG0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-01-0010' legacy_execution_id, 'OADMMS0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-01-0011' legacy_execution_id, 'OADMMS0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-02-0012' legacy_execution_id, 'OADMMS0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-03-0013' legacy_execution_id, 'OADMMS0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-04-0014' legacy_execution_id, 'OADMMS0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0010' legacy_execution_id, 'OADMNT0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0011' legacy_execution_id, 'OADMNT0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0014' legacy_execution_id, 'OADMNT0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-02-0012' legacy_execution_id, 'OADMNT0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-02-0016' legacy_execution_id, 'OADMNT0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-03-0013' legacy_execution_id, 'OADMNT0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-03-0015' legacy_execution_id, 'OADMNT0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0010' legacy_execution_id, 'OADMOB0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0011' legacy_execution_id, 'OADMOB0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0012' legacy_execution_id, 'OADMOB0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0001' legacy_execution_id, 'OADMOP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0002' legacy_execution_id, 'OADMOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0010' legacy_execution_id, 'OADMOP0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0020' legacy_execution_id, 'OADMOP0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0030' legacy_execution_id, 'OADMOP0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0034' legacy_execution_id, 'OADMOP0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0035' legacy_execution_id, 'OADMOP0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0036' legacy_execution_id, 'OADMOP0036' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0040' legacy_execution_id, 'OADMOP0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0041' legacy_execution_id, 'OADMOP0041' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0042' legacy_execution_id, 'OADMOP0042' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0043' legacy_execution_id, 'OADMOP0043' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0050' legacy_execution_id, 'OADMOP0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-02-0031' legacy_execution_id, 'OADMOP0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-02-0042' legacy_execution_id, 'OADMOP0044' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0032' legacy_execution_id, 'OADMOP0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0037' legacy_execution_id, 'OADMOP0037' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0038' legacy_execution_id, 'OADMOP0038' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0039' legacy_execution_id, 'OADMOP0039' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0043' legacy_execution_id, 'OADMOP0045' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0044' legacy_execution_id, 'OADMOP0046' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0045' legacy_execution_id, 'OADMOP0047' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-04-0022' legacy_execution_id, 'OADMOP0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-04-0044' legacy_execution_id, 'OADMOP0048' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-05-0011' legacy_execution_id, 'OADMOP0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-05-0021' legacy_execution_id, 'OADMOP0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0033' legacy_execution_id, 'OADMOP0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0040' legacy_execution_id, 'OADMOP0049' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0042' legacy_execution_id, 'OADMOP0051' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0010' legacy_execution_id, 'OADMPE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0011' legacy_execution_id, 'OADMPE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0014' legacy_execution_id, 'OADMPE0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0015' legacy_execution_id, 'OADMPE0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0019' legacy_execution_id, 'OADMPE0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0020' legacy_execution_id, 'OADMPE0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0024' legacy_execution_id, 'OADMPE0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0025' legacy_execution_id, 'OADMPE0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0029' legacy_execution_id, 'OADMPE0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0030' legacy_execution_id, 'OADMPE0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0034' legacy_execution_id, 'OADMPE0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0016' legacy_execution_id, 'OADMPE0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0021' legacy_execution_id, 'OADMPE0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0026' legacy_execution_id, 'OADMPE0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0031' legacy_execution_id, 'OADMPE0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0012' legacy_execution_id, 'OADMPE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0013' legacy_execution_id, 'OADMPE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0017' legacy_execution_id, 'OADMPE0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0018' legacy_execution_id, 'OADMPE0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0022' legacy_execution_id, 'OADMPE0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0023' legacy_execution_id, 'OADMPE0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0027' legacy_execution_id, 'OADMPE0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0028' legacy_execution_id, 'OADMPE0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0032' legacy_execution_id, 'OADMPE0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0033' legacy_execution_id, 'OADMPE0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0035' legacy_execution_id, 'OADMPE0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0001' legacy_execution_id, 'OADMRE0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0002' legacy_execution_id, 'OADMRE0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0003' legacy_execution_id, 'OADMRE0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0004' legacy_execution_id, 'OADMRE0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0006' legacy_execution_id, 'OADMRE0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0007' legacy_execution_id, 'OADMRE0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0009' legacy_execution_id, 'OADMRE0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0010' legacy_execution_id, 'OADMRE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0011' legacy_execution_id, 'OADMRE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0005' legacy_execution_id, 'OADMRE0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0008' legacy_execution_id, 'OADMRE0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0012' legacy_execution_id, 'OADMRE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0013' legacy_execution_id, 'OADMRE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-CR-0001' legacy_execution_id, 'OADMRL0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DL-0001' legacy_execution_id, 'OADMRL0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DL-0002' legacy_execution_id, 'OADMRL0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DW-0001' legacy_execution_id, 'OADMRL0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-IS-0001' legacy_execution_id, 'OADMRL0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0001' legacy_execution_id, 'OADMRL0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0002' legacy_execution_id, 'OADMRL0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0003' legacy_execution_id, 'OADMRL0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0004' legacy_execution_id, 'OADMRL0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-01-0010' legacy_execution_id, 'OADMSE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-01-0012' legacy_execution_id, 'OADMSE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0011' legacy_execution_id, 'OADMSE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0013' legacy_execution_id, 'OADMSE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0014' legacy_execution_id, 'OADMSE0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0015' legacy_execution_id, 'OADMSE0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0010' legacy_execution_id, 'OADMSV0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0020' legacy_execution_id, 'OADMSV0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0030' legacy_execution_id, 'OADMSV0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0040' legacy_execution_id, 'OADMSV0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0050' legacy_execution_id, 'OADMSV0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0060' legacy_execution_id, 'OADMSV0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0070' legacy_execution_id, 'OADMSV0070' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0001' legacy_execution_id, 'OADMTR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0002' legacy_execution_id, 'OADMTR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0003' legacy_execution_id, 'OADMTR0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0004' legacy_execution_id, 'OADMTR0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0005' legacy_execution_id, 'OADMTR0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0006' legacy_execution_id, 'OADMTR0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-01-0010' legacy_execution_id, 'OADMTR0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-01-0011' legacy_execution_id, 'OADMTR0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-04-0013' legacy_execution_id, 'OADMTR0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-05-0012' legacy_execution_id, 'OADMTR0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBAT-OPR-01-0003' legacy_execution_id, 'OBATOP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBAT-OPR-02-0002' legacy_execution_id, 'OBATOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ADM-01-1001' legacy_execution_id, 'OBZAAD1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ADM-03-1002' legacy_execution_id, 'OBZAAD1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-01-0001' legacy_execution_id, 'OBZAAP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-01-0003' legacy_execution_id, 'OBZAAP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-02-0002' legacy_execution_id, 'OBZAAP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-05-0004' legacy_execution_id, 'OBZAAP0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-01-0001' legacy_execution_id, 'OBZAAT0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-02-0002' legacy_execution_id, 'OBZAAT0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-DL-0003' legacy_execution_id, 'OBZAAT0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUD-01-0001' legacy_execution_id, 'OBZAUD0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0004' legacy_execution_id, 'OBZAAU0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0005' legacy_execution_id, 'OBZAAU0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0007' legacy_execution_id, 'OBZAAU0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0001' legacy_execution_id, 'OBZAAU0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0002' legacy_execution_id, 'OBZAAU0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0003' legacy_execution_id, 'OBZAAU0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-03-0006' legacy_execution_id, 'OBZAAU0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-04-0008' legacy_execution_id, 'OBZAAU0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-CUS-01-1001' legacy_execution_id, 'OBZACU1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DSH-01-0001' legacy_execution_id, 'OBZADS0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DWN-01-0002' legacy_execution_id, 'OBZADW0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DWN-01-1001' legacy_execution_id, 'OBZADW1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-EMP-01-0001' legacy_execution_id, 'OBZAEM0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-EMP-03-0002' legacy_execution_id, 'OBZAEM0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MNU-01-1001' legacy_execution_id, 'OBZAMN1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MNU-03-1002' legacy_execution_id, 'OBZAMN1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MSK-02-1001' legacy_execution_id, 'OBZAMS1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-01-0001' legacy_execution_id, 'OBZANT0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-02-0002' legacy_execution_id, 'OBZANT0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-03-0003' legacy_execution_id, 'OBZANT0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORD-01-1001' legacy_execution_id, 'OBZAOR1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORG-01-0001' legacy_execution_id, 'OBZAOR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORG-03-0002' legacy_execution_id, 'OBZAOR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-0002' legacy_execution_id, 'OBZAPE0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-0003' legacy_execution_id, 'OBZAPE0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-1001' legacy_execution_id, 'OBZAPE1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-02-0004' legacy_execution_id, 'OBZAPE0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-03-1002' legacy_execution_id, 'OBZAPE1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PRD-01-1001' legacy_execution_id, 'OBZAPR1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ROL-01-1001' legacy_execution_id, 'OBZARO1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ROL-03-1002' legacy_execution_id, 'OBZARO1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-01-0001' legacy_execution_id, 'OBZASC0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-03-0002' legacy_execution_id, 'OBZASC0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-04-0003' legacy_execution_id, 'OBZASC0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SET-01-1001' legacy_execution_id, 'OBZASE1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-USR-QY-0000' legacy_execution_id, 'OBZAUS0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-USR-QY-0001' legacy_execution_id, 'OBZAUS0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0001' legacy_execution_id, 'OREFAA0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0002' legacy_execution_id, 'OREFAA0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0003' legacy_execution_id, 'OREFAA0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0099' legacy_execution_id, 'OREFAA0099' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0001' legacy_execution_id, 'OREFAA0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0010' legacy_execution_id, 'OREFAA0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0020' legacy_execution_id, 'OREFAA0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0030' legacy_execution_id, 'OREFAA0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0001' legacy_execution_id, 'OREFAA0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0002' legacy_execution_id, 'OREFAA0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0003' legacy_execution_id, 'OREFAA0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-04-0001' legacy_execution_id, 'OREFAA0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-04-0002' legacy_execution_id, 'OREFAA0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-0001' legacy_execution_id, 'OREFAA0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-0002' legacy_execution_id, 'OREFAA0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-9001' legacy_execution_id, 'OREFAA9001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-0001' legacy_execution_id, 'OREFAA0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-0010' legacy_execution_id, 'OREFAA0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-9001' legacy_execution_id, 'OREFAA9002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0001' legacy_execution_id, 'OREFAA0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0002' legacy_execution_id, 'OREFAA0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0003' legacy_execution_id, 'OREFAA0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0004' legacy_execution_id, 'OREFAA0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0005' legacy_execution_id, 'OREFAA0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0006' legacy_execution_id, 'OREFAA0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0007' legacy_execution_id, 'OREFAA0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0008' legacy_execution_id, 'OREFAA0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0009' legacy_execution_id, 'OREFAA0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0010' legacy_execution_id, 'OREFAA0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0011' legacy_execution_id, 'OREFAA0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0012' legacy_execution_id, 'OREFAA0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0013' legacy_execution_id, 'OREFAA0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0015' legacy_execution_id, 'OREFAA0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0016' legacy_execution_id, 'OREFAA0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0017' legacy_execution_id, 'OREFAA0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0018' legacy_execution_id, 'OREFAA0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0019' legacy_execution_id, 'OREFAA0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0020' legacy_execution_id, 'OREFAA0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0030' legacy_execution_id, 'OREFAA0036' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0031' legacy_execution_id, 'OREFAA0037' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0032' legacy_execution_id, 'OREFAA0038' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0033' legacy_execution_id, 'OREFAA0039' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0034' legacy_execution_id, 'OREFAA0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0035' legacy_execution_id, 'OREFAA0041' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0036' legacy_execution_id, 'OREFAA0042' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0040' legacy_execution_id, 'OREFAA0043' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0051' legacy_execution_id, 'OREFAA0051' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0060' legacy_execution_id, 'OREFAA0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0072' legacy_execution_id, 'OREFAA0072' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0073' legacy_execution_id, 'OREFAA0073' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0080' legacy_execution_id, 'OREFAA0080' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0001' legacy_execution_id, 'OREFAA0044' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0002' legacy_execution_id, 'OREFAA0045' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0003' legacy_execution_id, 'OREFAA0046' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0001' legacy_execution_id, 'OREFAA0047' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0002' legacy_execution_id, 'OREFAA0048' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0003' legacy_execution_id, 'OREFAA0049' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0004' legacy_execution_id, 'OREFAA0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0005' legacy_execution_id, 'OREFAA0052' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0006' legacy_execution_id, 'OREFAA0053' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0007' legacy_execution_id, 'OREFAA0054' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0008' legacy_execution_id, 'OREFAA0055' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-14-0001' legacy_execution_id, 'OREFAA0056' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-15-0001' legacy_execution_id, 'OREFAA0057' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0001' legacy_execution_id, 'OREFAA0058' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0002' legacy_execution_id, 'OREFAA0059' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0003' legacy_execution_id, 'OREFAA0061' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0004' legacy_execution_id, 'OREFAA0062' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0005' legacy_execution_id, 'OREFAA0063' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0006' legacy_execution_id, 'OREFAA0064' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-17-0001' legacy_execution_id, 'OREFAA0065' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-17-0002' legacy_execution_id, 'OREFAA0066' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0001' legacy_execution_id, 'OREFQR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0002' legacy_execution_id, 'OREFQR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0003' legacy_execution_id, 'OREFQR0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0004' legacy_execution_id, 'OREFQR0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0005' legacy_execution_id, 'OREFQR0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
) src ON (tgt.legacy_execution_id = src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id = src.standard_execution_id, tgt.migration_reason = src.migration_reason, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
