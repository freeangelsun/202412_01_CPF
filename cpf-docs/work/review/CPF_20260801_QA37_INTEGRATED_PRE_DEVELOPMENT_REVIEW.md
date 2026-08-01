# CPF QA37 통합 개발 사전 리뷰

기준 SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

## 통합 대상과 공통 원인

QA37 64건, SELF 30건, EDU32 32건, 고객 Manual EDU135 135건을 하나의 Backlog로 병합한다. 선행 공통 원인은 Root Build 파손, False Green Gate, 실제 Consumer 없는 Bulk Mapping, 생성형 도메인/BZA 결합 위험, DB·Generator Drift, Frontend Fresh Clone 순서, exact-SHA Evidence다.

## Ownership와 Package 경계

- EDU Owner는 `cpf-reference` 하나다.
- EDU DB는 중앙 Vendor Pack `refDB`다.
- ACC·MBR·EXS 등 생성형 도메인과 제품 BZA를 참조하지 않는다.
- Source Package는 기능 기준으로 `online`, `batch`, `platform`, `optional/*`에 배치한다.
- Requirement ID는 Class·Catalog·Resource·Test·Matrix 추적값이며 Package 이름으로 사용하지 않는다.
- Batch는 `com.cpf.reference.batch`와 `CPF_REF_BAT_*` SQL Pack으로 통째 제거 가능해야 한다.

## DB·Generator 영향

- Core EDU: V93/U93 7 Table
- Optional Batch: V94/U94 3 Table
- Query 변경은 Canonical Source, Oracle/PostgreSQL/MariaDB, Install, Upgrade, Rollback, Runtime Query, Verify, Checksum을 함께 변경한다.
- Generator Golden Template에는 REF EDU/Batch Table을 넣지 않고 제외 계약과 Negative Test를 갱신한다.

## 구현 순서

1. Root Build·Included Build 복구
2. Truth Gate와 merged/overlay 판정 분리
3. 기능 중심 Package 재배치
4. Durable REF 실행 기반과 Concrete Consumer Binding
5. Core V93 + Optional Batch V94 3 Vendor Pack
6. Optional Pack 제거 Gate
7. EDU32 merged-root Fail-closed Gate
8. Frontend Fresh Clone Lifecycle
9. Matrix·Evidence·Delete Manifest 정합
10. Codex 단계별 단일 검수

## 완료 조건

merged Source, Java25, Frontend, 3DB V93/V94, Runtime/Fault, Browser, Supply-chain, exact result SHA가 모두 PASS해야 전체 완료다. Overlay 정적 PASS는 완료 근거가 아니다.
