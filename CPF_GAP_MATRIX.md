# CPF Gap Matrix

## 1. 기준

- 검수 기준 Commit: `8da2e6f395d72ce032e52ea16fef0d1d5f490c5b`
- 실제 작업 시작 기준: 문서 Overlay Push 후 최신 `origin/master`
- 최상위 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 현재 DB: 회사·집 PC 모두 CPF Schema와 Table이 없는 초기 상태
- 허용 상태: 완료, 부분 구현, 미구현, 미검증, 실패, 재확인 필요

현재 수치는 체감 진행률이 아니라 제품 완료 묶음과 실제 Evidence 기준이다.

## 2. 우선 Gap

| ID | 영역 | 현재 상태 | 핵심 Gap | 완료 조건 |
|---|---|---|---|---|
| DOC-001 | 문서 정본 | 부분 구현 | 상세 Guide 복구·분리 후 Source 정합성 미검증 | Link Gate와 Source/API/SQL 대조 |
| GATE-001 | Quality Gate | 실패 | 삭제한 DOCX·Generated·Evidence를 기존 Gate가 참조 가능 | 책임별 Gate 분리와 Clean 실행 |
| NAME-001 | Module/Package/SystemCode | 부분 구현 | 광범위 Rename 후 잔존·손상 가능 | Active Tree 전수 Scan, Build, Runtime |
| HASH-001 | Lockfile/Checksum | 재확인 필요 | 전체 문자열 치환으로 Integrity 손상 가능 | npm ci, Flyway SHA, Binary/Hash 검증 |
| CORE-001 | Core Public API/SPI | 부분 구현 | 경계·실제 Consumer·호환성 미완성 | API/SPI/Internal Gate와 Reference 사용 |
| CMN-001 | cpf-common Ownership | 실패 | 기술 기능, 채번, 고정길이, Log Table 잔존 가능 | DB-less 기본 구조와 Sample Table 1개 |
| DB-001 | Empty Install | 미검증 | 두 PC 모두 DB 삭제 상태 | Schema/User/Table/Seed 재생성 Runtime Evidence |
| DB-002 | Physical Ownership | 부분 구현 | cpfDB/batDB/cmnDB와 Prefix 혼재 | Owner별 Schema/Table/Constraint 정합성 |
| DB-003 | Migration | 재확인 필요 | Historical Flyway 변경 여부와 Re-baseline 정책 미확정 | Checksum 검증, Upgrade/Rollback |
| FIXED-001 | Fixed-Length | 부분 구현 | Common DB/Source와 Core Engine 책임 혼재 | Core Engine + EXS Institution Adapter E2E |
| SEQ-001 | 업무 채번 | 부분 구현 | Common 기본 Runtime 잔존 가능 | CMN 제거, BZA 선택형 Sample, 비의존성 |
| BAT-001 | Batch | 부분 구현 | Job/Step/Agent 실행·복구·운영 연결 미완성 | Runtime, DB, ADM, Failover Evidence |
| CENTER-001 | Center-Cut | 부분 구현 | Runner/Parameter/Item/Attempt/Unknown 복구 미완성 | Multi-instance E2E와 Failed-only Reprocess |
| AGENT-001 | Agent Failover | 미검증 | Lease/Fencing/Drain/Takeover 실검증 부재 | 장애 주입과 중복 방지 Evidence |
| EXS-001 | External | 부분 구현 | Endpoint, Retry, Unknown, Reconciliation 연계 미완성 | 실제 Adapter Runtime과 운영 조회 |
| EVENT-001 | Messaging | 부분 구현 | Real Broker, Outbox/Inbox, DLQ 미검증 | Multi-instance Broker E2E |
| ADM-001 | ADM | 부분 구현 | 실제 Command API·승인·감사·Browser 미검증 | Production Build와 Browser E2E |
| BZA-001 | BZA | 부분 구현 | 업무 Admin 경계와 Optional Sample 미완성 | Server Auth, Browser E2E, Sample 비활성 기본 |
| FRONT-001 | 독립 Frontend 배포 | 미검증 | Web Server/WAS 분리와 Rollback Evidence 부재 | ADM/BZA Artifact 독립 배포 |
| GEN-001 | Generator | 부분 구현 | Rename 이후 Lifecycle 재검증 필요 | create/db-init/verify/runtime/remove/regenerate |
| SEC-001 | Security | 부분 구현 | Permission/Approval/Masking/Audit Runtime 부족 | Negative Test, Audit, Secret Scan |
| OPS-001 | Observability/Recovery | 부분 구현 | Timeline, Unknown, Incident, Recovery 연결 미완성 | Runtime Failure/Recovery Evidence |
| REL-001 | Install/Deploy/Upgrade | 미검증 | Clean install, split deploy, rollback 미실행 | 재현 가능한 Runbook과 Evidence |
| EVID-001 | Evidence | 미구현 | 과거 Stale Evidence 제거 후 신규 근거 없음 | 최신 Commit/환경 기반 Raw+Sanitized Evidence |
| HYGIENE-001 | Repository | 부분 구현 | Root 작업 문서 이동·Script Link 정리 미완성 | Root 최소화, No Garbage, Link Pass |

## 3. 완료 우선순위

1. Baseline, Encoding, Lockfile, Flyway와 Quality Gate 복구
2. 공식 Module/Package/SystemCode와 Ownership 확정
3. Empty MariaDB Install과 Product Meta 재구축
4. Core/Common/External/Batch 책임 재배치
5. ADM/BZA와 Runtime 실제 연결
6. Multi-instance, Failure, Unknown과 Recovery
7. Generator Lifecycle와 Reference Domain 회귀
8. Guide, Matrix와 Evidence 정본화
9. Upgrade, Rollback, Split Deploy와 최종 Product Gate

## 4. 진행률 관리

동일 Gap을 이름만 바꿔 중복 집계하지 않는다. 각 행은 Requirement ID, Owner, Source, SQL, API, Test, Runtime과 Evidence를 연결하고, 완료 상태로 바뀔 때 실제 순감소량을 기록한다.
