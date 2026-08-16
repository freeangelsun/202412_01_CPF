# CPF 다음 로컬 개발·검증 요건

## 적용 기준

- 결과 Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- 결과 Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- Overlay 삭제 대상: 0
- 과거 Content SHA를 Overlay 적용 차단 조건으로 사용하지 않는다.

## 1회 FullLocal 실행

프로젝트 Root에서 다음 한 줄을 실행한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

필수 환경: PowerShell 7.x, Java25, Docker Desktop/Engine, npm/Node. FullLocal이 직접 소유한 Container만 restart/cleanup한다.

## 반드시 실행될 범위

1. NXT3 aggregate와 Java25 Gradle configuration/build/test/publication/SBOM
2. ADM/BZA npm ci → verify/lint/typecheck/unit/build → Browser E2E/A11y
3. Local 1-WAS start → representative transaction → restart/shutdown
4. 동일 transactionId/traceId의 FileLog ↔ DB Log ↔ ADM Timeline 실제 상관관계
5. MariaDB → PostgreSQL → Oracle 순차 install/seed/query/upgrade/rollback/reapply
6. Redis/Valkey CRUD/reconnect/invalidation/multi-instance
7. Kafka produce/consume/restart/DLQ/retry/outbox/inbox/duplicate protection
8. Batch Scheduler + 2 Worker + Center-Cut + Process Kill + UNKNOWN_RESULT + reconcile + fencing
9. Gateway OFF/ON, separated topology, multi-instance routing/health
10. Security adversarial + secret masking + canonical Performance/Backpressure workload
11. Evidence/Manifest integrity와 managed-state before/after

## 판정

- `READY`, `PLANNED`, `NOT_EXECUTED`는 PASS가 아니다.
- Docker가 정상인데 Docker Runtime 단계가 SKIP되면 Orchestrator 결함으로 재개발한다.
- 1-WAS가 정상인데 Integrated Logging이 실행되지 않으면 Orchestrator 결함으로 재개발한다.
- FAIL이 있어도 독립 단계는 계속하고 결과 ZIP을 만든다.
- FAIL이 남으면 FullLocal exit는 non-zero여야 한다.
- 필수 Runtime 미검증이 남으면 전체 완료가 아니다.

## 다음 전달 자료

`CPF_LOCAL_VALIDATION_<timestamp>.zip` 하나를 다음 개발/QA 입력으로 전달한다. 실패는 개별 증상이 아니라 Root Cause 단위로 다시 묶어 Source/Test/Verifier/Generator/Frontend/SQL/Evidence를 함께 보정한다.
