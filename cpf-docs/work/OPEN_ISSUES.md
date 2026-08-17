# CPF Current Open Issues — Session Close

작성 시각: `2026-08-17 20:44:55 +0900`

현재 세션은 개발 변경과 정적/독립 검증을 마감하고, **Java 25 사용자 로컬 및 실 Runtime 검증은 다음 세션으로 인계**한다. 아래 항목은 PASS로 승계하지 않는다.

## OPEN-RUNTIME-001 — Java 25 Gradle Build/Test
- 상태: `미검증`
- 제품/사용자 로컬 공식 기준: **Java 25**
- GPT 실행환경: Java 21 대체 정적/독립 검증만 수행
- 현재 환경 제약: Gradle 9.1 wrapper distribution 외부 DNS 접근 불가
- 다음 세션: 사용자 로컬 Java25에서 최종 검증 한 줄 실행

## OPEN-RUNTIME-002 — Header/Context/Operation live boundary
- 상태: `미검증`
- 확인: external ingress, internal Domain hop canonical six, 400/403/409 Controller-before reject, FileLog/DB Log/ADM correlation

## OPEN-RUNTIME-003 — Policy / LKG / Multi-WAS
- 상태: `미검증`
- 확인: 신규 Operation 자동등록, YML Seed 최초 1회, ADM Policy 보존, Caller→Operation 통제, Channel Store 장애 LKG/maxStale/fail-close, 실시간 policyVersion 동기화

## OPEN-RUNTIME-004 — Runtime instanceId
- 상태: `미검증`
- 기준: `cpf.runtime.instance-id` / `CPF_RUNTIME_INSTANCE_ID`; 미설정 시 실제 Hostname; local/localhost synthetic fallback 금지
- 확인: MBR01/MBR02 명시값, hostname fallback, 동일 Host multi-process distinct

## OPEN-RUNTIME-005 — DB3 live migration
- 상태: `미검증`
- 확인: Oracle/PostgreSQL/MariaDB 중 사용 가능한 공식 Vendor에서 현재 영향 migration install/upgrade/rollback/runtime query smoke

## OPEN-RUNTIME-006 — EDU 35 Java25 실행
- 상태: `미검증`
- 정적 구조: online 20 + batch 15 = 35 확인
- 다음 세션: Java25 compile/test 및 대표 online/batch runtime 실행

## OPEN-RUNTIME-007 — Frontend/Browser
- 상태: `미검증`
- 현재 targeted OpenAPI/consumer 정적 검증은 수행
- 다음 세션: `npm ci` 포함 full verify/browser E2E를 사용자 환경에서 재확인

## OPEN-VERIFY-001 — 전체 Verification suite 재실행
- 상태: `재확인 필요`
- 이번 세션 후반에 발견한 Integrated Logging/DLQ/scratch-cache/annotation-runtime 관련 공통 원인은 집중 검증으로 보정했으나, 수정 후 전체 Verification suite를 끝까지 한 번 더 통합 실행하지 못했다.
- 다음 세션 FinalLocal에서 반드시 재실행한다.
