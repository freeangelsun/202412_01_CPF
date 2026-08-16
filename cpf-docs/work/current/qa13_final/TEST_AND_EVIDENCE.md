# QA13 개발 GPT 최종 개발·자체검수 Evidence

## 기준

- 입력 개발 기준 Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- 입력 개발 기준 Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- 최종 개발본 Content SHA-1: `6371c50581487bf4061415e60487e3cf27383f28`
- 최종 개발본 Content SHA-256: `56f9edce7dc5f60eb1f38d10d46a14da41812968013f093c5762a0d97c7e6565`
- 사용자 Git write/commit/push/delete/history rewrite: 수행하지 않음.
- 이번 Overlay 제품 삭제 대상: 0건.

## 입력 FullLocal 결과

`CPF_LOCAL_VALIDATION_20260816_171430.zip` 기준 총 145 Stage:

- PASS 112
- FAIL 23
- SKIP_ENV 3
- NOT_EXECUTED 7

FAIL/NOT_EXECUTED는 첫 오류만 수선하지 않고 NXT3/Source mutation, Java compile/test, Frontend, Generator, DB3, Cache/Runtime fault, Local 1-WAS, Logging/Browser/Performance 연쇄 실패로 Root Cause를 묶어 개발했다.

## 이번 개발 핵심

1. NXT3 생성 cache 오탐 및 Python bytecode 자기오염 방지.
2. Overlay package provenance와 현재 Runtime Source identity 분리. Manifest/file hash 검증은 fail-closed 유지.
3. Java/Spring API drift, Provider dependency, checked-close, Education Source compile 계약 보완.
4. ADM/BZA generated client/OpenAPI/Route consumer closure 보완.
5. Public Starter 24개 + 전체 Canonical Catalog 64개 Runtime Capability 자동등록 계약 추가.
6. 신규 Starter가 추가돼도 ADM 공통 Capability 관리면에 자동 편입되는 Catalog→metadata→Runtime Inventory→ADM 구조 추가.
7. ADM IA를 운영 현황/로그·추적/장애·복구/설정·정책/감사·변경이력 중심으로 재분류하고, Batch/Gateway/Security·Approval/Deployment만 고유 Workflow를 유지.
8. Capability Fleet/Explorer Backend, OpenAPI, generated client, ADM consumer, DB3 seed/upgrade/rollback 추가.
9. Runtime Health 계약 중복을 canonical API로 통합.
10. 사용 Starter/Capability/Provider/Operation 등 시스템 메타데이터 자동 수집 및 로그/DB/ADM 조회 연결 보완.
11. Password PBKDF2 AutoConfiguration, Secret/pepper fail-closed 계약 추가.
12. JDBC role DataSource 자동조립 및 다중 DataSource ambiguity 보완.
13. verifier-owned DB3 격리 lifecycle, Local runtime DB/BZA bootstrap/cleanup 보완.
14. Cache Live/QA39 Runtime이 stale installed helper가 아니라 현재 Source 정본을 사용하도록 currentize.
15. FileLog/DB Log/ADM correlation Evidence를 이번 실행 거래만 최소 수집하도록 FullLocal 보완.
16. ADM Runtime Control 4개 mutating operation을 typed consumer로 연결; Maintenance는 direct mutation 대신 Approval Owner 경로를 canonical route contract로 사용.
17. Route 개수 `65` 하드코딩 제거. Canonical route registry/operation set parity로 검증.

## 현재 작업본 실제 재검수

- ADM route source consumer: PASS, routes=66, operations=323, missing component=0
- ADM capability registry: PASS, routes=66/generated=66
- CPF Capability Management: PASS, publicStarters=24, automaticRegistration=YES, admCommonIA=YES
- Developer/Adoption Contract: PASS, modules=64, golden=7, capability=17, internal=40, TOP20=20
- Starter Catalog: PASS, modules=64, public=24, internal=40
- Gradle project dependency closure: PASS, references=352, undeclared=0, cycle=0
- Route operation contract fixture: PASS, routes=66
- Focused QA13 regression: 35/35 PASS
- Verification: 45/45 PASS
- DB Verification: 75/75 PASS
- Generator: 27 PASS / 10 environment SKIP / subtests 6 PASS
- Runtime tools: 65 PASS / 2 environment SKIP / subtests 7 PASS
- Release focused: 14 PASS
- Testing Tools full split execution: 379 PASS / 22 conditional SKIP / FAIL 0
- NXT3 aggregate는 Assistant Linux에서 child-process 종료 지연이 있었으나, 전체 22 Gate를 개별 실행해 모두 PASS 확인. `cpf verify all` PASS.

## 아직 Runtime Evidence가 필요한 항목

현재 Assistant 환경에는 Windows PowerShell 7 + Java25 + Docker Desktop + 실제 Browser 실행환경이 없어 다음은 PASS로 기록하지 않는다.

- Java25 Root Gradle build/test/publication/SBOM
- ADM/BZA npm ci/verify/typecheck/unit/build + Browser E2E/A11y
- Local 1-WAS actual lifecycle
- FileLog ↔ DB Log ↔ ADM Timeline 실제 동일 transactionId/traceId correlation
- Oracle/PostgreSQL/MariaDB verifier-owned live lifecycle
- Redis/Valkey live restart/reconnect/multi-instance
- Kafka/JMS/RabbitMQ/IBM MQ live reliability 범위
- Batch 2-worker/process kill/UNKNOWN/reconcile
- Gateway/Topology multi-instance
- Security adversarial/masking live scan
- Performance/backpressure live workload
- Windows fresh extract lifecycle

따라서 개발 GPT Source/Static/independent gate 범위는 완료이나 전체 QA 최종상태는 `RUNTIME_REVERIFY_REQUIRED`이다.
