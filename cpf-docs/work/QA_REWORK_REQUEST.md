# CPF QA Rework Request — Final Development Package

개발 GPT가 확인 가능한 Source/Static/Independent Gate 결함은 이번 패키지에 반영했다. QA는 동일 Requirement ID로 아래 **실 Runtime 재검증**을 우선 수행한다.

1. Java25 Root Gradle compile/test/build/publication/SBOM
2. 외부 Inbound 5 Header + Receiver Current Channel 자동설정 및 400/403/409 Controller-before reject
3. Generated Domain `systemCode == Channel Identity` 및 Same JVM/Remote Context parity
4. Operation 자동 Bootstrap, YML 최초 Seed 1회, ADM Policy preserve
5. `operationId + callerChannel` allow/deny, LKG/maxStale/fail-close, Multi-WAS policyVersion
6. DB3 V121~V127 fresh/upgrade/runtime query/rollback
7. Async lease/fencing/cancel/process-kill/recovery/idempotency
8. Frontend actual runtime OpenAPI generated client/npm/browser E2E
9. File/Cache/Message/UNKNOWN 대표 runtime regression

Runtime Evidence가 없는 항목은 PASS로 처리하지 않는다. 과거 Evidence의 PASS를 현재 Source 결과로 승계하지 않는다.
