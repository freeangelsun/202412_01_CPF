# QA13 다음 로컬 FullLocal 실행 요건

## 목적

이번 Overlay 적용 후 로컬은 개발을 수행하지 않는다. Windows/Java25/Docker/Browser에서 최대 Runtime 검증과 실제 Evidence를 생성해 개발 GPT에게 전달한다.

## 실행 전

- PowerShell 7.x
- Java 25
- Docker Desktop/Engine 정상
- Node/npm 사용 가능
- 사용자 기존 DB/Container/로그는 검증기가 임의 삭제하지 않는다.
- FullLocal은 검증기 소유 scratch/container/database/schema만 생성·정리한다.

## 실행 명령

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

## 이번 로컬에서 필수 재검증

1. NXT3 aggregate 22/22.
2. Java25 configuration/build/test/assemble/publication/SBOM.
3. Generator member/external generate/compile/test/regenerate/idempotency.
4. ADM/BZA npm ci, verify, lint, typecheck, unit, build.
5. ADM Route 66 / OpenAPI 323 / generated client / actual consumer parity.
6. Local 1-WAS start/health/representative transaction/restart/shutdown/port reuse.
7. DB3 verifier-owned lifecycle: MariaDB → PostgreSQL → Oracle 순차 실행.
8. Redis/Valkey actual CRUD/reconnect/restart/invalidation/multi-instance.
9. Kafka/Messaging actual produce/consume/restart/DLQ/outbox/inbox/duplicate protection.
10. Batch Scheduler/2-worker/center-cut/process kill/UNKNOWN_RESULT/reconcile/fencing.
11. Gateway OFF/ON, topology, multi-instance, unhealthy route.
12. ADM Browser Capability Fleet/Logs/Failure-Recovery/Config/Audit drill-down.
13. Security/Approval/Runtime Control high-risk command permission/reason/approval/audit.
14. Performance/backpressure/resource workload.

## 로그 Evidence 규칙

과거 누적 로그를 ZIP에 넣지 않는다.

- FullLocal 실행 전 검증기 소유 scratch log만 정리한다.
- 사용자 기존 로그는 삭제하지 않는다.
- 이번 실행 시작시각 이후 생성·변경된 로그 중 대표 거래 transactionId/traceId 관련 최소 원문만 수집한다.
- FileLog: 대표 거래 구조화 line/필드/flush 결과.
- DB Log: 대표 거래 조회 결과.
- ADM: 동일 거래 Observability/Timeline 조회 결과.
- correlation matrix: system/domain/application/instance/starter/capability/provider/operation/transactionId/traceId.
- masking scan: password/token/authorization/secret/개인정보 원문 0.
- 로그 level/sink/header/body capture는 effective policy와 함께 Evidence에 남긴다.

## 결과 전달

생성되는 `CPF_LOCAL_VALIDATION_<timestamp>.zip` 하나와, Overlay 적용 후 현재 전체 Source ZIP을 개발 GPT에 전달한다. 개발 GPT는 결과를 기다리는 동안 다음 사이클 정적/보안/기능 검수를 병행한다.
