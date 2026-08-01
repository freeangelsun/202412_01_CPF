# CPF QA37 Source Readiness 및 Codex 검수 준비 보고

Baseline: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

## 판정

현재 산출물은 **Source Closure Candidate / Codex Verification Ready**다. 전체 제품 완료나 Runtime 성공을 의미하지 않는다.

## 이번 보정의 실제 Source 범위

- EDU Owner: `cpf-reference` 단일 모듈
- EDU DB Owner: 중앙 Vendor Pack `refDB`
- 생성형 도메인 ACC·MBR·EXS 등 의존: 0건
- 제품 BZA 실행 의존: 0건
- 기능 중심 Package:
  - `com.cpf.reference.online`: 45
  - `com.cpf.reference.batch`: 30
  - `com.cpf.reference.platform`: 15
  - `com.cpf.reference.optional.operations`: 17
  - `com.cpf.reference.optional.backoffice`: 14
  - `com.cpf.reference.optional.gateway`: 14
- 숫자 ID 전용 Package: 0건. Requirement ID는 Class·Catalog·Resource·Test·Matrix에서만 추적한다.
- Batch DB Pack: `CPF_REF_BAT_JOB_EXECUTION`, `CPF_REF_BAT_CHECKPOINT`, `CPF_REF_BAT_TARGET_RESULT`
- Core EDU DB Pack: V93/U93 7 Table
- Optional Batch DB Pack: V94/U94 3 Table
- Query 변경 전파: Canonical refDB → Oracle/PostgreSQL/MariaDB → Install/Upgrade/Rollback/Runtime/Verify/Checksum → Generator 제외 계약

개발자 탐색 문서: `cpf-reference/src/main/resources/edu/PACKAGE_INDEX.md`

## 실제 수행한 Overlay 검증

- `verify-cpf-reference-package-layout.py`: PASS
- `verify-cpf-reference-feature-isolation.py`: PASS
- `verify-cpf-reference-feature-removal.py`: PASS
- `verify-cpf-qa37-consumer-bindings.py`: PASS
- `verify-cpf-qa37-db-generator-parity.py --mode overlay`: PASS
  - 3 Vendor
  - Core 7 Table
  - Optional Batch 3 Table
  - Generated Domain Table 2개 유지, REF EDU/Batch 유입 0건
- `verify-cpf-qa37-manual-edu-135.py --compile`: PASS
  - Handler 135
  - ID별 Test 675
  - 계약·입력·권한·정상·멱등·대표 장애·복구 135/135
  - Java 21 `javac` Compile/Self-test
- EDU32 Overlay Contract Gate: PASS. 이는 merged-root PASS가 아니다.

## 완료로 기록하지 않는 항목

- 최신 master에 Overlay를 적용한 merged-root EDU32 Source/Test/Public Contract 해석
- Java 25 Fresh Cache 전체 Build·Test·Publication
- Node 22.18 ADM/BZA Clean Verify
- Oracle·PostgreSQL·MariaDB 실제 V93/V94 Install·Upgrade·Rollback·Reapply
- Kafka·Redis·Fault·Multi-instance·Browser·Supply-chain
- 사용자 Push 후 exact result SHA Evidence

현재 전체 상태는 `미검증`이다. Codex는 선행 Stage 0을 먼저 실행하고 실패 시 비싼 후속 Stage를 시작하지 않는다.
