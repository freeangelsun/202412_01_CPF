# CPF QA36 프로젝트 전체 최종 QA·개발·검증 요청

## 1. 목표

이번 작업은 ADM 또는 EDU 한정 작업이 아니다.
CPF 전체 제품을 최신 master에서 완성하고 Commercial Release Gate를 닫는 프로젝트 전체 작업이다.

## 2. 읽기 순서

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `CPF_20260801_QA36_PRE_DEVELOPMENT_REVIEW.md`
3. `CPF_20260801_QA36_MASTER_REQUIREMENT_INDEX.csv`
4. `CPF_20260801_QA36_CANONICAL_162_REQUIREMENT_MATRIX.csv`
5. `CPF_20260801_QA36_ACTIVE_GAP_REQUIREMENT_MATRIX.csv`
6. `CPF_20260801_QA36_CANONICAL_MANDATORY_SCENARIO_MATRIX.csv`
7. Module/ADM/EDU/Defect/Continuity matrices
8. 실제 Source·SQL·Frontend·Test·Script·Config
9. GPT 작업 지침

## 3. 개발 시작 Gate

- latest origin/master, exact SHA, branch, clean tree 기록
- 기존 1,873/441 원본 Inventory를 Repository 정본에서 로드
- Canonical 162와 중복·분해·승계 Mapping
- Module Surface Inventory와 Public Contract Inventory 완성
- 작업 전 리뷰와 변경 예상 파일·영향도 확정
- Frontend deterministic source failure를 먼저 수정
- P0 source gate 통과 전 expensive runtime으로 이동 금지

## 4. 전체 개발 순서

1. Truth/Continuity/CI
2. Architecture/Public Boundary
3. Build/BOM/Plugin/Publication
4. Core/Common/Starter actual consumers
5. DB 3 Vendor lifecycle
6. Online/Gateway/Event/External/Saga
7. Batch/Scheduler/Worker/Center-Cut/Agent
8. ADM Online·Batch integrated Control Plane
9. BZA optional boundary
10. Generator/Member
11. EDU/Reference 162 coverage
12. Security/Privacy/Audit
13. Observability/Incident/DR
14. Frontend/OpenAPI/3 Browser
15. Supply Chain/Deploy/Compatibility
16. Full Runtime/Fault/Exact-SHA Evidence
17. Independent post-review and Codex package

## 5. 완료 금지

- Canonical 162 중 미완료
- Legacy 1,873/441 미재판정
- 실제 Consumer 없는 API/SPI
- Frontend generated drift
- ADM 87 capability·59 routes 미완료
- EDU 162 coverage 미완료
- Java25/fresh cache/3DB/Kafka/3 Browser/multi-instance/fault 미실행
- latest exact-SHA Evidence/CI Status 없음
- Docs가 Source보다 앞섬
- Artifact catalog/supply-chain/hygiene 누락
