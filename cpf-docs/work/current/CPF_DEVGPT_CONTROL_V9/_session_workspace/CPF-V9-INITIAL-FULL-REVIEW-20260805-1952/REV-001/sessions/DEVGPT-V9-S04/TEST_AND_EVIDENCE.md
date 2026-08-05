# TEST AND EVIDENCE — DEVGPT-V9-S04

## Baseline and status boundary

- Baseline origin/master: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Central request_id: `MISSING_IN_ASSIGNMENT_SOURCE`; local tracking `DEVGPT-V9-S04-LOCAL-20260805-1952`
- DevGPT S04: `development_status=완료`, `verification_status=완료`
- QA: `미검수`; `final_completion=false`
- Live Oracle/PostgreSQL/MariaDB, PowerShell and Docker were unavailable. No live-runtime success is claimed; the assignment-approved parser/simulator/harness/static substitute path was used.

## Atomic equality

- Work Items: 93 assigned = 93 judged
- Canonicals: 18 assigned = 18 reviewed
- CPF-FR: 1,492 assigned = 1,492 unique judged
- CPF-SC: 2,015 assigned = 2,015 unique judged
- Gates: 16 assigned = 16 judged
- Not reviewed / missing / duplicate primary / unassigned / orphan / evidence missing / consumer unconfirmed / actionable S04 P0·P1: all `0`

## Latest commands and actual results

- `CPF_REPO_ROOT=<exact-sha+overlay> python -m unittest discover -s cpf-tools/db/tests -p "test_*.py" -v` → Exit 0, **68/68**
- `CPF_REPO_ROOT=<exact-sha+overlay> python -m unittest discover -s cpf-tools/scripts/tests -p "test_*.py" -v` → Exit 0, **34/34**
- `python -m compileall -q cpf-tools/db cpf-tools/scripts` → Exit 0
- Java 21 product compile: Template Store/Management/legacy fail-closed shell → Exit 0
- Java 21 Harness: secure Template Renderer, Calendar product/non-product failure behavior, multi-instance Listener → Exit 0
- `verify-cpf-db-vendor-semantic-parity.py` → Exit 0; **203 tables / 2,960 columns / 341 indexes / 80 UK / 148 FK per vendor**, findings 0
- `verify-cpf-db-schema-governance.py` → Exit 0; canonical schema version 48, 203 tables, 148 FK
- `verify-cpf-db-vendor-manifest.py --metadata-only` → Exit 0; official vendors exactly MariaDB/PostgreSQL/Oracle
- `verify-cpf-db-lifecycle-contract.py` → Exit 0; 3 vendors / 9 ordered stages / development-runtime status separated
- `verify-cpf-db-development-contract.py` → Exit 0; 17 PowerShell lifecycle files, `STATIC_SUBSTITUTE_ONLY`
- `verify-cpf-cmn-runtime-query-contract.py` → Exit 0; 25 Query IDs, authoring/resource/hash/parameter/consumer parity

Primary latest log: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/CPF-V9-INITIAL-FULL-REVIEW-20260805-1952/REV-001/sessions/DEVGPT-V9-S04/evidence/s04_final_revalidation_r12.log`
Java summary: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/CPF-V9-INITIAL-FULL-REVIEW-20260805-1952/REV-001/sessions/DEVGPT-V9-S04/evidence/s04_java_verify_summary_r11.log`
Correction trail: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/CPF-V9-INITIAL-FULL-REVIEW-20260805-1952/REV-001/sessions/DEVGPT-V9-S04/evidence/corrections/CORRECTION_SUMMARY.md`

## Main product changes

- Migration lifecycle fail-closed verifier, partial failure/UNKNOWN reconcile, rollback or explicit forward recovery, checksum-locked reapply.
- DB evidence verifiers fail closed for UNKNOWN/PARTIAL, identity mismatch, secret-bearing evidence, missing approval/SoD and uncompensated multi-resource operations.
- CMN Calendar uses `cmnTransactionManager` and required durable change outbox.
- CMN Sample DB routes Oracle/PostgreSQL/MariaDB SQL dialects fail closed.
- CMN Template now has persistent version lifecycle, SoD, CAS revision, append-only audit, V101/R101/Verify/checksum, durable refresh, secure channel-aware rendering and a disabled legacy persistence shell.
- Calendar/Sample/Template runtime SQL moved to canonical Query ID resources with 25/25 consumer parity.
- Deterministic 3-vendor retention SQL enforces archive-before-purge and legal hold.

## Integrated boundary

Six ICRs remain `요청서 생성·대상 Owner 미적용`. They do not leave an S04 atomic ID unreviewed, but they keep QA final completion false until applied and rerun on integrated latest origin/master.
