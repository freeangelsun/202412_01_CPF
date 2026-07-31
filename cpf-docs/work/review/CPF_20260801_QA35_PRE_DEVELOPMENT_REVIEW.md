# CPF QA35 작업 전 리뷰

## 1. 작업 목표
QA35는 “다음 결함 몇 건 수정”이 아니라 CPF 제품 마감을 위한 최종 Closure 작업이다.

1. QA34의 잘못된 개발 완료 판정을 바로잡는다.
2. Frontend OpenAPI·Generated Client·Fresh Clone Gate를 실제로 닫는다.
3. ADM 59개 메뉴를 상용 운영도구 수준으로 전수 검증한다.
4. CPF Framework 기능과 162 Requirement를 EDU 예제에 전수 연결한다.
5. Source·Consumer·Runtime·Evidence가 같은 exact SHA를 가리키게 한다.
6. 동일 결함이 다음 QA 회차로 재발하지 않도록 CI와 Impact Gate를 만든다.

## 2. 최상위 Requirement
- Canonical Requirement Count: 162개
- 공식 EDU Owner: `cpf-reference`
- 공식 ADM Owner: `cpf-admin`
- 공통 완료 축: Ownership, Consumer, 정상, 오류, 동시성, Multi-instance, 결과불명, 복구, Security, Audit/Operations, Resource, DB, Compatibility, Test, Documentation, Evidence, Hygiene

## 3. 현재 상태
| 영역 | development_status | verification_status | 근거 |
|---|---|---|---|
| Build Plugin/BOM 방향 | 부분 구현 | 미검증 | Source 존재, fresh-cache 미실행 |
| ADM/BZA OpenAPI | 실패 | 미검증 | stale/partial snapshot과 validator 충돌 |
| Generated Client | 실패 | 미검증 | schema2 marker·필수 artifact 누락 |
| ADM Route/Menu | 부분 구현 | 미검증 | 59 Route 존재, 제품 계약 Matrix 부재 |
| EDU 전체 Coverage | 미구현 | 미검증 | 162 Requirement/Public API mapping 부재 |
| 3DB/Kafka/Browser | 부분 구현 | 미검증 | Runner 존재, exact-SHA 실행 없음 |
| Supply Chain | 부분 구현 | 미검증 | Artifact Catalog 불완전 |
| Final Release | 실패 | 미검증 | CI/Evidence 없음 |

## 4. Owner와 경계
- `cpf-core`: topology-independent 계약
- `cpf-common`: 고객 공통 정책
- `cpf-admin`: 플랫폼 운영 Control Plane
- `cpf-biz-admin`: 고객 업무 관리자
- `cpf-batch`: Batch Runtime
- `cpf-gateway`: Gateway Runtime
- `cpf-reference`: EDU·Reference Runtime
- `cpf-member`: Generator Golden Consumer

ADM은 Owner DB를 직접 갱신하지 않고 Public Operations Command/Query Contract를 소비해야 한다. EDU는 Internal Package를 소비하지 않고 Public API/SPI의 실제 Consumer여야 한다.

## 5. 개발 순서
### Phase 0 — Truth Reset
QA34 상태·Current Request·Guide를 실제 Source에 맞게 재분류한다.

### Phase 1 — Deterministic Source Closure
OpenAPI/Generated Artifact 계약을 고치고 외부 Runtime 없이 fresh clone frontend/build를 먼저 통과시킨다.

### Phase 2 — ADM Product Closure
59개 Route의 기능·API·권한·상태·상호작용·감사 Matrix를 완성하고 실제 Backend E2E를 통과시킨다.

### Phase 3 — EDU Product Closure
162 Requirement/Public API를 EDU Sample/Runtime/ADM/Evidence에 연결한다.

### Phase 4 — Runtime and Supply Chain
3DB·Kafka·Browser·Multi-instance·Process Kill·Artifact Catalog를 actual runtime으로 검증한다.

### Phase 5 — Independent Exact-SHA
최종 Commit 이후 별도 fresh clone에서 1회 독립 검증하고 Evidence를 생성한다.

## 6. 개발 금지 조건
- Script/Interface/Marker만 추가하고 완료 처리
- Runtime 실행을 다음 회차로 이월
- Snapshot/Generated Artifact를 재생성하지 않은 채 Generator 완료 처리
- Route 존재를 ADM 기능 완료로 처리
- EDU 문서나 Sample 이름만으로 Coverage 완료 처리
- 외부 Fixture를 정본 없이 수동 준비
- 광범위한 삭제·Git 정리
- 사용자 승인 없는 Commit/Push

## 7. 회귀 위험
- BFF Session/Credential 보안 약화
- Gateway/Agent pinned identity 제거
- Build tooling Source가 ignore에 다시 숨음
- ADM generic API helper 제거 과정에서 download/stream 기능 파손
- EDU 격리 과정에서 product seed나 demo profile 혼동
- 기존 Migration file 수정
- Browser test를 mock-only로 회귀

## 8. 필수 Evidence
- Source SHA / Result SHA
- clean/dirty
- 명령·환경·Profile·Fixture hash
- 시작/종료 시각·Exit Code
- Requirement·Scenario·Artifact mapping
- Browser/DB/Broker/Instance 정보
- sanitized 여부
- 최종 Artifact SHA/SBOM


## 9. 외부 최소 기능선 검수 추가

사용자 제공 화면 44장을 검수해 87개 최소 Capability와 42개 Target Menu를 추가했다.
기존 ADM 59 Route는 기능 수가 적어서가 아니라, Route별 실제 업무 결과와
Online·Batch 통합 흐름이 증명되지 않아 `부분 구현·미검증`이다.

특히 거래 Profile/DBIO/Pipeline, Online runtime low-level monitoring,
Batch Job Definition/DAG/Emergency control, Datasource/Receiver, Analytics,
Global Search/Cross-domain Timeline을 P0로 재분류한다.
