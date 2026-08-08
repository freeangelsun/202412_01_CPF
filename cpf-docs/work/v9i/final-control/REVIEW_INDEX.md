# CPF Final Control Index

## Current Status

- Current master / Product QA basis SHA: `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c` (`07_08`)
- Product Source Completion overlay: **applied and pushed**
- Canonical denominator: **169**
- Legacy aliases: **8** (duplicate count prohibited)
- Previous central findings: **56** (revalidation input)
- Previous normalized Central Actions: **31** (revalidation input)
- Developer current result: `cpf-docs/work/v9i/dev-final/**`
- Developer reported source development: Canonical 169/169, Central 31/31, Previous 56/56
- Developer Runtime Qualification: **13/13 미검증**
- Independent final QA: **QA A / QA B pending on `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`**
- Special review control: **1,000 mandatory review points**
- Release status: **UNVERIFIED / RELEASE_BLOCKED until independent QA A/B + required Runtime qualification pass**

## Current Read Order

1. `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md`
2. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
3. `REVIEW_INDEX.md`
4. `CENTRAL_QA_MERGE_REPORT.md`
5. `CENTRAL_FINAL_ACTIONS.csv`
6. `SPECIAL_REVIEW_1000.csv`
7. `ROLE_BOUNDARY.md`
8. `../dev-final/REVIEW_INDEX.md`
9. `../dev-final/**`
10. `../qa/final-a/**`
11. `../qa/final-b/**`
12. current Product Source / Runtime / exact-SHA Evidence

## QA A/B Current Execution Rule

QA A와 QA B는 **영역을 나누지 않는다**.

두 QA 모두 같은 전체 CPF 범위를 독립적으로 검수한다.

- QA A: Canonical → Source → Consumer → Runtime/Evidence 방향 + Special 1000 `0001 → 1000`
- QA B: Special 1000 `1000 → 0001` → Runtime/Evidence → Consumer → Source → Canonical 역방향
- Scope / Acceptance / PASS·FAIL 기준 / Runtime 기준 / Evidence 기준은 동일
- 상대 QA의 PASS를 자동 승계하지 않음
- Developer의 완료/PASS도 QA가 재검증하기 전에는 QA PASS가 아님

## Special Review 1000

`SPECIAL_REVIEW_1000.csv`는 전체 검수 Scope를 대체하지 않는 **특별관리 최소 강제 검수망**이다.

전체 전수검수는 기존처럼 계속 유지한다.

특별관리 1,000은:
- QA A/B 각각 전 항목 판정
- `PASS / FAIL / 미검증 / 재확인 필요`
- `미검수` 잔존 시 QA 완료 불가
- PASS는 Source + Consumer + 해당 Test/Gate + 필요한 Runtime + exact SHA Evidence 필요
- 1,000개 밖에서 발견되는 결함도 제한 없이 Finding으로 편입

사용자 지정 핵심 축은 최우선 검수 대상이다.

- Online Domain A→B→C→D transaction / rollback / transactionId / logging
- Batch→Domain A→B→C transaction / rollback / restart / process-kill
- Framework Utils / Paging / WebClient / Validation / Error / File / Time / ID / Masking
- 한글 중심 Class·Method·주요 로직 Javadoc / Javadoc build
- Controller/API Swagger/OpenAPI
- OpenAPI → Generated Client → ADM/BZA/EDU 실제 Consumer

## Current Product Verification Caution

Developer 결과 문서 일부는 predecessor `08d8beb4a664039904c30aeac07115a04707924a`를 기준으로 작성된 흔적이 있을 수 있다.
실제 Product Source Push successor는 `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`다.

따라서 Developer가 보고한 21/21 PASS, 169/169, 31/31, 56/56은 QA가 `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`에서 독립 재실행·재판정하기 전에는 최종 QA PASS가 아니다.

## TransactionId Canonical Decision

정식 거래 기동 Channel/System은 최초 CPF transactionId를 생성할 수 있다.
이후 동일 거래의 Local/Remote/Gateway/REST/SOAP/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile/Log/ADM Timeline은 동일 transactionId를 유지한다.

Retry는 transactionId를 새로 만들지 않고 attempt/segment로 실행 단위를 구분한다.
비신뢰 주체의 spoof/replay/manipulation만 authenticated Channel/System identity + trust policy로 차단한다.
모든 inbound transactionId 일괄 재생성은 금지한다.

## Role Boundary

- Project Canonical / Central Control / QA Merge: 중앙 관리자
- Product Source: Developer GPT
- Independent full audit: QA A / QA B
- README / Guide / PDF / DOCX: Documentation Finalization

QA와 Developer는 중앙 Canonical/Final Control을 임의 변경하지 않는다.

## Repository Hygiene

Repository는 Current-State 중심으로 관리한다.

금지:
- R1/R2/R3
- REV
- SESSION
- 날짜별 동일 목적 결과
- Checkpoint별 동일 결과
- stale raw evidence / obsolete zip / temp workspace 누적

현재 판단에 필요한 과거 정보는 Current 파일에 흡수하고 Git History를 버전 이력으로 사용한다.

## Current Flow

1. Central Currentization / Cleanup — 완료
2. Product Developer Final Source Completion — `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`에 Push 완료
3. Central QA Control Currentization + Special 1000 — 현재 단계
4. QA A 전체 독립 Audit — 대기/착수
5. QA B 전체 독립 Audit — 대기/착수
6. 중앙 QA A/B Merge + Special 1000 취합
7. 결함이 있으면 Developer 전체 재개발
8. successor SHA에서 QA A/B 재검수
9. Runtime 필수 항목까지 PASS
10. Documentation Finalization 정합 재확인
11. Final Release Adjudication
