# QA-12B R5 Test and Evidence

## EVENT-DLQ

- Operations original Harness: FAIL as expected
- Operations patched Harness: PASS 4 scenarios
- Repository original Harness: FAIL as expected, mutations=3
- Repository patched Harness: PASS 4 scenarios, mutations=0
- Patched actual repository source `javac` with Spring/JDBC contract stubs: PASS
- OpenAPI/Orval original verifier: FAIL as expected
- Patched contract verifier: PASS
- TypeScript strict contract compile: PASS 2 scenarios
- JSON parse/schema/hash: PASS
- Java25 Gradle/npm full verify/3-DB/Broker: NOT EXECUTED

Detailed logs: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12B_R5_f97655c/EVENT_DLQ_BEFORE_AFTER.md`
