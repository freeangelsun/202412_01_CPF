# CPF Open Issues — Final Development Package

작성 시각: `2026-08-18 10:42:05 +0900`

정적/독립적으로 검증 가능한 Source 결함은 이번 작업에서 보정했다. 아래는 **사용자 Java25/실 Runtime 환경이 있어야 닫을 수 있는 검증 항목**이며 PASS로 기록하지 않는다.

## OPEN-RUNTIME-001 — Java25 Root Gradle Build/Test
- 상태: `미검증`
- GPT 환경: Gradle 9.1 wrapper distribution 캐시 없음 + 외부 DNS 접근 불가.
- 필요: `help`, compile/test/build, quality/publication/SBOM/generated projects를 사용자 Java25에서 실행.

## OPEN-RUNTIME-002 — Channel/Header/Operation live boundary
- 외부 직접 Inbound 필수 Header 5개, Current Channel 미전달 정상 처리.
- Receiver Generated Domain `systemCode`로 currentChannel 자동 설정.
- targetChannel/currentChannel mismatch 및 targetOperation mismatch Controller-before reject.
- Same JVM/Remote Context 의미 동등성.

## OPEN-RUNTIME-003 — Operation/Channel Policy Multi-WAS
- 신규 Operation 자동등록, YML Seed 최초 1회, ADM Policy 보존.
- `operationId + callerChannel` allow/deny, registered/enabled semantics.
- LKG/maxStale/fail-close 및 policyVersion 다중 WAS propagation.

## OPEN-RUNTIME-004 — Runtime instance identity
- `cpf.runtime.instance-id` → `CPF_RUNTIME_INSTANCE_ID` → 실제 hostname.
- 동일 Host 다중 WAS는 explicit unique instanceId 사용.

## OPEN-RUNTIME-005 — DB3 live migration
- Oracle/PostgreSQL/MariaDB Fresh/Upgrade/V121~V127/Runtime Query/Rollback 실제 실행.

## OPEN-RUNTIME-006 — Async/Recovery process runtime
- lease/fencing/heartbeat/cancel, process kill, duplicate submit, timeout/expiry, UNKNOWN/reconcile.

## OPEN-RUNTIME-007 — Frontend/Docker/Browser
- `npm ci`/build, actual runtime OpenAPI Orval replacement, Browser E2E, Docker/Redis/Valkey.
