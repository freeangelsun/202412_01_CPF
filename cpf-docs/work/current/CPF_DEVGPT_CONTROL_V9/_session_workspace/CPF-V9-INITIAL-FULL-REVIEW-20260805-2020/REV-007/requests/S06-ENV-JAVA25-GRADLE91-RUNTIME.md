# Integration Request S06-ENV-JAVA25-GRADLE91-RUNTIME

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `BUILD_RUNTIME_ENVIRONMENT`
- Priority: `P0`
- Exact path: `Java 25 toolchain; Gradle 9.1.0 distribution; dependency repositories`
- Related IDs: `GATE-16-COMPATIBILITY;GATE-17-SUPPLY-CHAIN;GATE-18-TEST-EVIDENCE;STAB-016;STAB-017;STAB-018;STAB-021;STAB-023`
- Current status: `BLOCKED_EXTERNAL_ENVIRONMENT`
- Reproduction/Evidence: `evidence/RUNTIME_ENVIRONMENT.txt`

## Required implementation and acceptance
Sandbox has Java 21 and DNS cannot resolve Gradle/Adoptium; full build, test, publication and release evidence cannot be produced.

## Completion rule
Implement product source and consumers in the target owner, add positive/negative/recovery assertions, rerun the related Engineering Gates at exact integrated SHA, and return implemented SHA plus evidence. Do not close from this request document alone.
