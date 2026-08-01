# CPF QA37 작업 후 독립 리뷰

기준 SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

## 실제 Source 보정

- EDU135를 `cpf-reference` 단일 Owner와 `refDB`로 고정했다.
- Package를 기능 중심으로 재배치했다: Online 45, Batch 30, Platform 15, Optional Operations 17, Backoffice 14, Gateway 14.
- 숫자 Requirement Package를 제거하고 Package/물리 경로 1:1 Gate를 추가했다.
- JDBC·HTTP·File·Process·Outbox·Spring Batch·REF Gateway Concrete Consumer Binding을 강제했다.
- 생성형 도메인과 제품 BZA 실행 의존을 제거했다.
- Core V93/U93 7 Table과 Optional Batch V94/U94 3 Table을 Oracle·PostgreSQL·MariaDB에 동시 반영했다.
- Generator는 Golden Domain 2 Table을 유지하며 `CPF_EDU_*`, `CPF_REF_BAT_*` 생성 금지를 검증한다.
- Batch Job은 `CPF_REF_BAT_JOB_EXECUTION`, `CPF_REF_BAT_CHECKPOINT`, `CPF_REF_BAT_TARGET_RESULT`를 사용한다.

## 수행한 검증

- Package Layout Gate: PASS
- Generated Domain/BZA Isolation: PASS
- Optional Feature Removal Static Contract: PASS
- Consumer Binding Static Closure: 135/135 PASS
- Manual EDU135 Java21 Compile·Self-test: PASS
- 3 Vendor Core/Batch Static DB·Generator Parity: PASS
- EDU32 Overlay Contract/Self-test: PASS
- Truth Gate: PASS, 전체 상태 `미검증`

## 미실행 검증

- applied merged Repository Source Closure
- Java25 Fresh Gradle Lifecycle
- Node22.18 ADM/BZA Clean Verify
- 3 Vendor 실제 V93/V94 Install·Rollback·Reapply와 Batch Off
- Kafka·Redis·Fault·Multi-instance·OTel·Browser
- Supply-chain과 exact result SHA

현재 전체 판정은 `미검증`이다. Source Candidate와 Codex 검수 준비까지만 확인됐다.

## 완료 오판 재발 방지

- Overlay와 merged-root Gate를 분리했다.
- Handler/Test 개수 대신 Concrete Consumer를 검사한다.
- Runtime 미실행 항목을 Matrix에서 `미검증`으로 내렸다.
- 전체 조건 미충족 시 `FULL/COMPLETION` Package ID와 완료 문구를 Truth Gate가 거부한다.

## Hygiene

- README와 연결 Guide·Manual은 보호한다.
- stale current-work 문서 50개를 Delete Manifest에 기록하고 자동 삭제하지 않는다.
- 생성 산출물·빈 폴더 Cleanup은 Source와 `cpf-tools/build`를 보호한다.
