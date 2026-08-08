# CPF Final QA A/B 중앙 Merge 및 Current QA Control

- Previous QA Source basis: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2`
- Previous Central Currentization basis: `64dcb0c1383a74008698053bb4832af9c04e9fd6`
- Documentation successor: `08d8beb4a664039904c30aeac07115a04707924a`
- **Current Product / Final QA basis: `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c` (`07_08`)**
- Current release judgment: **UNVERIFIED / RELEASE_BLOCKED**
- Final QA mode: **QA A/B same full scope, reverse execution order only**
- Special control: **Optimized 1,000 mandatory cross-cutting review points**

## 1. Previous Central Merge

이전 QA A/B 결과는:
- QA A new findings: 25
- QA B new findings: 8
- Raw: 33
- Root-cause overlap: 2
- Central normalized actions: **31 (P0 22 / P1 9)**

였다.

이 31개는 전체 Product Scope의 상한이 아니다.

전체 검수는:
- Canonical 169
- Previous Findings 56
- Central Actions 31
- Developer self-found
- Product Source 전체
- Runtime Qualification
- Special Review 1,000
- QA 신규 발견 전체

를 포함한다.

## 2. Developer Rework Push State

Developer Product Source Completion 결과가 `cpf-docs/work/v9i/dev-final/**`과 Product Source에 반영되어 `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`로 Push됐다.

`07_08`은 단순 Evidence successor가 아니라 실제 Product Source 변경을 포함한다.
중앙에서 확인한 Commit 규모는 +9,108 / -2,411이며 ADM/BZA, cpf-core, transactionId, FileLog, Approval, OpenAPI, EDU 등 실제 Source가 변경되었다.

따라서 QA는 predecessor `08d8beb4...` 기준 Developer PASS를 자동 승계하지 않고 `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`에서 재검증한다.

Developer 현재 보고:
- Canonical Source development: 169/169
- Central Action development: 31/31
- Previous Finding development: 56/56
- Runtime Qualification: 13/13 미검증
- Developer independent gates: reported 21/21 PASS

위 수치는 **Developer 상태**이며 QA 최종 판정이 아니다.

## 3. Final QA A/B 운영

QA A와 QA B는 영역을 분할하지 않는다.

동일 전체 Scope를 서로 독립적으로 검수한다.

### QA A
Canonical → Architecture → Source → Consumer → Transaction/Batch/DB3 → ADM/BZA/EDU → Runtime/Evidence → Special 1000 `0001 → 1000`

### QA B
Special 1000 `1000 → 0001` → Runtime/Evidence → ADM/BZA/EDU → DB3/Transaction → Consumer → Source → Architecture → Canonical

다른 것은 순서뿐이다.

- Acceptance 동일
- Scope 동일
- Runtime 기준 동일
- Evidence 기준 동일
- PASS/FAIL 기준 동일

## 4. Special Review 1000

`SPECIAL_REVIEW_1000.csv`를 중앙 특별관리 원장으로 사용한다.

이 원장은 Canonical Requirement를 대체하지 않는다.
전체 전수검수를 축소하지 않는다.

각 Review ID:
- `CPF-RV-0001` ~ `CPF-RV-1000`
- QA A status
- QA B status
- Central status
- Developer rework status
- last verified SHA

를 추적한다.

특히 사용자 지정 Mandatory 항목은 최상위:
- Online multi-domain transaction / rollback / logging
- Batch multi-domain transaction / rollback / restart
- Utils / Paging / WebClient / common APIs
- Korean Javadoc / comments / Javadoc build
- Swagger/OpenAPI / generated client / real consumer

로 유지한다.

## 5. Central Adjudication Rule

QA 결과 수신 후 중앙은:
1. 동일 SHA 확인
2. QA A/B raw finding 보존
3. Duplicate/root cause 병합
4. 어느 한쪽 Finding도 임의 삭제·약화 금지
5. 충돌은 Canonical + Source + Runtime으로 판정
6. Special 1000 A/B 상태 취합
7. P0/P1 및 미검증 Runtime 분리
8. Developer 재개발 요청 생성
9. successor SHA에서 다시 QA

한다.

## 6. Release Gate

다음 중 하나라도 존재하면 Release Block:
- P0 open
- P1 open
- 필수 Runtime 미검증
- Canonical mismatch
- Consumer 단절
- DB3 unresolved
- Security unresolved
- Special 1000 미검수
- Special 1000 FAIL
- exact SHA Evidence mismatch
- false-green gate

`1000/1000 PASS` 자체만으로 Release PASS가 아니다.
전체 Source 전수검수에서 신규 결함이 있으면 그대로 Release Block이다.

## 7. Current Next Action

현재 다음 액션은 **QA A와 QA B를 `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c` 기준으로 즉시 독립 전체 검수시키는 것**이다.

QA 결과가 도착하면 중앙관리자는:
- 전체 Finding Merge
- Special 1000 취합
- Requirement/Runtime 상태 판정
- Developer 재개발 지침
- Current 중앙문서 현행화

를 수행한다.
