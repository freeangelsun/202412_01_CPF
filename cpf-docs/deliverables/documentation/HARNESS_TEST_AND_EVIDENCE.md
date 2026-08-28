# Documentation Harness 2.10.0 Test and Evidence

## Source Identity

- Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip`
- SHA-256: `34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5`
- Source files: 10,447
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`

## Harness Change Summary

- Baseline Harness files: 79
- Current Harness files: 86
- Added: 7
- Modified: 67
- Removed: 0
- Unchanged: 12

## Executed Gates

| Gate | Result | Evidence |
|---|---|---|
| JSON parse | PASS | 44 JSON files parsed |
| Python compile | PASS | validators/*.py compile, pycache removed |
| Harness self validation | PASS | `HARNESS=PASS`, `VERSION=2.10.0`, `CURRENT_ONLY=PASS` |
| Strict final acceptance policy | PASS | `STRICT_FINAL_ACCEPTANCE=ENFORCED` |
| Negative/false-green fixtures | PASS | 76/76 |
| Source alignment | PASS | inventory sample refs 414, public developer artifacts 26 |
| Source ZIP validation | PASS | files 10,447 / DOCX 11 / PDF 11 / product visual 9 / cpf-tools/build 168 |
| Fresh clean replay | PASS | Harness 86 files byte-for-byte identical after overlay |
| Existing artifact rejection | PASS | New gate intentionally rejects 20 known readability/actionability defects |

## Existing Artifact Rejection Evidence

The current documents are deliberately **not** marked PASS by Harness 2.10.0. The new validator reports `READABILITY_ACTIONABILITY=FAIL COUNT=20`, including:

- README centered hero/body 519 chars > 260
- README flat list walls (8 and 11 items) and 8 consecutive long bullets
- Framework Developer Guide chapters 3/4/6/7 table/API-summary walls
- JDBC/MyBatis/JPA Selection-to-Action missing for all three choices
- Domain Invocation outside-table working-flow evidence missing for `execute(`, `CpfResult`, `UNKNOWN`, `Reconcile`
- Batch Developer Guide chapters 1/2/3/4/6 table/API-summary walls

This is expected evidence that the 2.9.0 false-green cases can no longer pass.

## Platform-specific Unexecuted Verification

- Windows PowerShell runtime for `*.ps1`: **미검증**. `pwsh`/Windows PowerShell is unavailable in the current Linux execution environment.
- Final DOCX/PDF regeneration and Windows/VS Code visual review: **not part of this Harness-only phase**; current artifacts must first be reworked against Harness 2.10.0.

No unexecuted check is recorded as PASS.
