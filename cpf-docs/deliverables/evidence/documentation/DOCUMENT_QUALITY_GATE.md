# CPF 공식 문서 품질 Gate

- 기준 Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `bd5fcd7707cfbba366071e7e4556c5cbb9b974bc`
- 공식 사용자 문서: README 포함 7종
- 문서별 immutable 작성 지침: 8종

## 산출물

| 번호 | 문서 | 형식 | 페이지 | 상태 |
|---:|---|---|---:|---|
| 01 | README | Markdown | - | PASS |
| 02 | 프레임워크 개발자 가이드 | DOCX/PDF | 13 | PASS |
| 03 | 배치 개발자 가이드 | DOCX/PDF | 11 | PASS |
| 04 | 운영자 매뉴얼 | DOCX/PDF | 12 | PASS |
| 05 | 배치 운영 가이드 | DOCX/PDF | 13 | PASS |
| 06 | Gateway 개발·사용 가이드 | DOCX/PDF | 10 | PASS |
| 07 | Specification / 기술 명세 | DOCX/PDF | 15 | PASS |

- DOCX/PDF 총 페이지: **74**
- 07 Specification은 첫 장에 **CPF 전체 Architecture 그림 1장**을 배치해 기술 계약의 전체 위치를 먼저 파악하도록 보강함

## 기계 검증

- README 상대 링크/이미지: 13개 확인, 누락 0건
- 공식 DOCX/PDF 존재: 6쌍 + README
- Canonical DOCX renderer: 6/6 PASS
- 02~07 정식 CPF CI Header 적용 및 재렌더: 6/6 PASS
- 02~07 전체 DOCX/PDF: 74/74페이지 전수 시각 QA PASS
- 수정된 07 Specification: 15/15페이지 시각 QA PASS
- 문서별 지침 SHA-256: 8/8 PASS
- `com.customer` 사용자 문서 잔존: 0건
- MySQL/MSSQL/SQL Server/H2 공식 사용자 문서 잔존: 0건
- `사용 Surface`, `Public Surface`, `Escape Hatch`, `Bounded Bulk`: 0건
- 질문형 유치 제목 패턴: 0건
- Delete Manifest 후보: **186개**. 자동 삭제 `DELETE` **114개**, 보호 경로 `KEEP_PROTECTED` **72개**, 최종 Overlay 충돌 0건
- Active reference로 보존한 Legacy: **15개** (`PRESERVED_REFERENCED_LEGACY.csv`)
- 삭제 명령은 Manifest exact path만 제거하고, 지정된 문서 관리 Root 아래 **빈 폴더만** 후처리
- Git commit/push/branch/tag/PR: 수행하지 않음

## 문서별 Gate

- README: 정식 통합 Architecture → 기본형/선택형/생성형 → Orchestration → Gateway → Batch → 기능 선택 → 개발/운영 → Quick Start → 7종 Navigation 흐름
- 개발자: Quick Finder, Base 공통 메서드, Persistence, `@CpfTx`, `REQUIRES_NEW`, Typed Domain Client, 외부연계, Idempotency/Concurrency, Security/Audit, Messaging, DB Lifecycle, Recovery, Test, Source Navigation
- 배치 개발: 처리모델, Annotation/SPI, 실행 요청/Control API, Parameter Schema, Metadata, Chunk/TX, Restart/Rerun/Reprocess/Reconcile, Partition/Worker/Lease/Fencing, Scheduler/Misfire, Center-Cut, Agent/Artifact, Property, ADM, Fault/Test, 인계
- 운영자: Runtime/Health, Trace, UNKNOWN/부분 실패, Gateway, BZA, Config Partial Apply, 배포/재기동, DB/Backup/Restore/DR, Log/Metric/Trace/Audit, Runbook, ADM/BZA 메뉴 Reference, Evidence Pack
- 배치 운영: 메뉴/Architecture, 상태, Execution/Step/Count, 수동 실행/Parameter, 실패 분석, Retry/Restart/Rerun/Reprocess/Reconcile/Abandon, Stop, UNKNOWN, Partition/Worker, Scheduler, Center-Cut, Agent/Deployment, Permission/Audit, 장애 판단, 일일 운영
- Gateway: 선택형 Topology, Public API/SPI, Route Spec, Registry/Binding, Target/Health/Canary, Security/Header, Rate, Timeout/Retry/UNKNOWN, Safety Config, Stored/Effective/Runtime/Instance, ADM, Test, Source Navigation
- Specification: **전체 Architecture Context**, Stack, Ownership, Base/Persistence API, `@CpfTx`, Result, Domain Client, Annotation Catalog, Batch API/Request/Parameter/Control/Config, Gateway Route/API/Config, Security, DB Lifecycle, Runtime/Topology, Error/Recovery, UI Contract, Source Index

## 미검증/QA 경계

- 이번 세션은 문서 작성·정합성·렌더·링크·삭제 안전성 검증이다. 제품 Runtime 전체 Build/DB3/Browser/Fault를 새로 실행한 것으로 기록하지 않는다.
- Codex 독립검수 및 QA 최종 승인은 아직 수행되지 않았다. 문서 작업자의 자체검수 PASS와 QA 최종 완료를 구분한다.
