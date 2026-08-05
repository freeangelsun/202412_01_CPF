# CPF V8 상세 개발 작업 목록 적용 정책

## 목적

이 디렉터리는 일반 작업 지침이 아니라 CPF 제품 Requirement를 실제 Source·SQL·API·Test·Config·Frontend·Script로 구현하기 위한 상세 실행 Backlog다.

- Repository: `freeangelsun/202412_01_CPF`
- Baseline 후보: `faedf43a7baffdad456bf40f8e46d622db9cfc76` (`04-04`)
- QA 혼합 Push 비교: `f97655c1299936a1101bc3ec10239265ec3b502e` (`04-03`)
- 생성일: `2026-08-05`
- Canonical Requirement: 169개
- 실제 실행 Work Package: 생성 Ledger 기준
- 세션 수: 고정하지 않음

## 강제 우선순위

1. CPF Final Target와 승인 Architecture·ADR·Specification
2. 공식 Module·Package·DB/State Ownership
3. Public API·SPI·Internal 계약
4. Acceptance Criteria와 Scenario
5. 보안·권한·감사·마스킹·DB Vendor·Migration·Rollback·Evidence 규격
6. 이 Backlog의 필수 개발 결과
7. 이 Backlog의 비강제 구현 제안

구현 제안이 1~6과 충돌하면 제안을 적용하지 않는다.

## 정본 ID와 실제 작업 단위 분리

Canonical 169개는 영속 추적 ID다. 실제 개발은 더 작은 Work Package로 분리한다.

```text
Canonical Requirement
→ Contract/Ownership
→ Default Implementation/Consumer
→ Failure/Recovery
→ Security/Operations
→ DB/Migration
→ Generation/Compatibility
→ Verification/Evidence
```

모든 Requirement가 모든 축을 갖는 것은 아니다. Work Type과 실제 위험에 따라 필요한 축만 생성했다.

## 완료 원칙

- Work Package를 완료해도 Canonical 전체가 자동 완료되지 않는다.
- Canonical에 연결된 모든 Work Package, CPF-FR, CPF-SC와 적용 Gate가 완료돼야 한다.
- 환경이 부족해도 Source·Test·Harness 구현을 끝까지 수행한다.
- QA 최신 통합 Git 통과 전 최종 완료가 아니다.
- 사용자 승인 없이 Commit·Push·Branch·Tag·PR·Release·Reset·Restore·Stash·삭제를 수행하지 않는다.


## AI 권장 읽기 순서

전체 775개 Work Item이나 모든 상세 파일을 한 번에 Context에 넣지 않는다.

```text
1. 00_APPLICATION_POLICY.md
2. 01_COMMON_ENGINEERING_GATES.md에서 해당 Gate만 검색
3. WORK_ITEM_INDEX.csv에서 priority·dependency·owner·axis로 3~15개 Work Item 선택
4. Index의 markdown_file에 지정된 상세 Part만 읽기
5. Index의 ledger_part에 지정된 넓은 상태 원장만 갱신
6. 연결 CPF-FR·CPF-SC와 실제 Source·Test·Evidence 확인
```

`WORK_ITEM_INDEX.csv`는 탐색용이고 `ledgers/*_WORK_ITEMS.csv`가 Work Item 전체 컬럼을 보존한다. 분할 과정에서 행이나 컬럼을 삭제하지 않았다.
