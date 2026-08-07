# CPF 고객사 README·매뉴얼·설계 산출물 작성 및 관리 표준

> Repository: `freeangelsun/202412_01_CPF`
> Branch: `master`
> 문서 표준 기준 Commit: `cd5baccb02245a980e5998aa0dc9bac579fc019f` (`07_04`)
> 최상위 요구사항 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 1. 문서의 정체성과 독자

CPF 문서는 CPF 내부 개발일지가 아니라 고객사가 CPF 제품을 선택·개발·운영·복구·감사·확장하는 데 사용하는 제품 문서다. 고객은 CPF 제품 본체를 다시 개발하지 않는다.

주 독자는 도입 검토자, 업무/배치 개발자, ADM 연동 개발자와 운영자, 플랫폼 운영자·DBA·보안·DR 담당자, BZA 조직/권한/결재 담당자, Gateway API/보안/운영 담당자다.

## 2. 사실 우선순위와 기준 Repository

1. 최신 `origin/master`의 실제 Source·SQL·API·Config·Frontend·Script·Test
2. 최상위 제품 요구사항과 Architecture/Specification
3. 공식 사용자 문서

작업 시작마다 `origin/master` SHA를 기록한다. 과거 ZIP·다른 Branch·이전 대화만으로 현행 기능을 확정하지 않는다. Local Working Tree의 기존 변경은 보호한다. 사용자 승인 없이 Commit/Push/Branch/Tag/PR/Merge/Rebase/Cherry-pick/Revert/Stash/Checkout/Switch/Restore/Reset/Clean/Remote 변경을 수행하지 않는다.

## 3. 공식 사용자 문서와 배포 형식

공식 사용자 진입 문서는 다음 9개 역할만 사용한다.

- `README.md`
- `cpf-docs/guides/00_프레임워크안내.pdf`
- `cpf-docs/guides/01_개발자매뉴얼.pdf`
- `cpf-docs/guides/02_배치개발매뉴얼.pdf`
- `cpf-docs/guides/03_ADM개발자매뉴얼.pdf`
- `cpf-docs/guides/04_ADM운영자매뉴얼.pdf`
- `cpf-docs/guides/05_플랫폼운영매뉴얼.pdf`
- `cpf-docs/guides/90_BZA매뉴얼.pdf`
- `cpf-docs/guides/91_Gateway매뉴얼.pdf`

각 Guide는 같은 basename의 `.docx` 편집본을 함께 제공한다. 설계 산출물 5종(`산출물목록`, `아키텍처설계서`, `기술사양서`, `기술표준서`, `데이터베이스표준서`)도 `.pdf` 정식 열람본과 같은 basename의 `.docx` 편집본을 함께 제공한다.

**README만 Markdown으로 유지한다. Guide 8종과 설계 산출물 5종의 `.md` Authoring 파일은 Repository에 보존하지 않는다. README의 공식 매뉴얼·설계 링크는 PDF를 가리킨다.**

`cpf-docs/guides/README.md`와 별도 Quick Start/EDU/Reference/Case/Troubleshooting/Runbook 사용자 Guide를 만들지 않는다. 해당 내용은 담당 매뉴얼 내부에 포함한다.

## 4. README 보호

README는 제품 브로셔, Architecture 시각 소개, 공식 PDF 진입점, 최소 빠른 시작 역할을 한다. 승인 없이 Hero, Architecture, Product Map, Topology, Execution/Operations 흐름, Domain Journey, Guide Map, Desktop/Mobile 구성을 전면 재작성하지 않는다. 사실 오류·깨진 링크·정본 경로를 기존 구조 안에서 최소 수정한다.

README가 직접 참조하는 `cpf-docs/assets/readme/**`, `cpf-docs/assets/manuals/cpf-reader-start.svg`, `cpf-docs/guides/png/cpf-guide-map.png`는 사용 중 자산으로 보호한다.

## 5. 모든 기능 설명이 답해야 할 질문

각 기능은 목적, 대상 역할, Owner Module, 실제 Consumer, Public API/SPI/Internal 경계, Source/Config/SQL/화면 위치, 선행조건, 입력·기본값·범위, 전체 흐름, 단계별 절차, 정상 결과와 상태 변화, Log/Metric/Trace, 오류·동시성·Timeout·응답 유실·부분 실패, Retry/Restart/Reprocess/Reconcile/Compensation/Rollback, Permission/Data Scope/Masking/Reason/Approval/Audit, Test, ADM 확인, 미검증·제한사항을 설명한다.

“지원한다/관리한다/처리한다/등록한다/확인한다”만으로 완료 처리하지 않는다. 누가 어떤 권한으로 어디에 무엇을 입력하고 어떤 상태로 바뀌며 실패하면 무엇으로 정상화를 판정하는지 쓴다.

## 6. 상태 표현과 검증

허용 상태는 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`다. 직접 실행하지 않은 Runtime/DB/Browser/다중 인스턴스/장애 Test는 성공으로 기록하지 않는다. Static Source 정합성과 Runtime 검증을 분리한다.

## 7. 제품 본체와 EDU/Reference 경계

ADM, BZA, Gateway, Batch Runtime은 제품 Module이 소유한다. 제품 내부 기능을 이름만 바꾼 Generic Handler/JDBC Example로 Reference에 복제하지 않는다. `cpf-reference`/EDU는 도입 개발자가 실제 사용하는 Public API·SPI·Extension Point·Integration Contract·Generator 산출물 사용법을 교육한다.

EDU 개수는 품질 점수가 아니다. 각 항목이 교육 대상 사용자, 공개 계약, 실제 Consumer, 교육 필요성을 설명하지 못하면 Architecture 재분류 대상이다. 기존 EDU ID의 유지/통합/Product 귀속/공식 Extension Sample/삭제 후보 판정은 QA 결과를 따른다.

## 8. 거래·로그·추적 표준

최상위 `transactionId`는 Local/Remote, Gateway, Message, File, Batch, 외부연계에서도 유지한다. Retry는 attempt로 구분하고 `segmentId/parentSegmentId`, `traceId/spanId`, request/idempotency 및 Batch/Remote 식별자를 연결한다.

File Log는 표준 경로·파일명·UTF-8 구조화 Record·Rotation·Compression·Retention·권한, bounded queue/backpressure/fallback, disk-full/write-failure, shutdown drain, spool/replay/dedup/checksum/gap/loss metric을 검증한다. DB Timeline은 transactionId Index, segment hierarchy, append/idempotency, outage recovery, retention/partition/archive/purge, Audit tamper evidence를 검증한다. Secret/Token/PII 원문을 기록하지 않는다.

ADM은 transactionId 하나로 가능한 전체 계보를 연결하고 Source 누락·지연에는 Partial/Stale을 표시한다.

## 9. 매뉴얼별 최소 범위

### 9.1 00 프레임워크 안내
제품 범위/비범위, Architecture, Module Ownership, 의존 방향, MSA/Modular Monolith, Local/Remote, 다중 인스턴스, 온라인/비동기/Batch/보안/DB Lifecycle/Generator, 제품-EDU 경계, 문서 지도를 다룬다.

### 9.2 01 개발자 매뉴얼
환경/Build, Generator, 신규 Domain, API/Application/Domain/Persistence, Transaction/동시성, Local/Remote, 멱등/UNKNOWN_RESULT, Kafka/Outbox/Inbox, File/Attachment/외부연계, Security/Audit/Secret SPI, DB Migration, OpenAPI/Generated Client, Test, ADM 확인, 배포 인계와 실제 Public Consumer EDU를 다룬다.

### 9.3 02 배치 개발 매뉴얼
Spring Batch Primary Engine, Job/Step/Tasklet/Chunk, Reader/Processor/Writer, Parameter/Metadata/Checkpoint, Stop/Restart/Abandon, Partition/Remote Worker, Scheduler/Misfire, Center-Cut, Artifact/Job Pack, Runner/Worker/Agent, Lease/Claim/Fencing, Dry Run, 승인/실행/UNKNOWN/Reprocess/Reconcile, transaction lineage, ADM 운영을 다룬다.

### 9.4 03 ADM 개발자 매뉴얼
완성된 ADM 제품에 고객 Owner Query/Command를 Same-JVM/Remote로 연결하고 Timeout/Expected Version/Idempotency/UNKNOWN/Reconciliation, Permission/Masking/Reason/Approval/Audit, canonical OpenAPI/Generated Client, Route/Menu/State/Table/Form, Browser Fault Test와 고객 Extension 경계를 다룬다.

### 9.5 04 ADM 운영자 매뉴얼
실제 Route/Component/Permission을 전수 대조한다. 각 화면의 검색 Field/기본값/Column/상세 Field/상태/Button/활성조건/입력/Reason/Approval/Expected Version/응답 유실/부분 적용/Retry/Reprocess/Reconcile/Rollback/Audit를 다룬다. 가상 화면 목록으로 대신하지 않는다.

### 9.6 05 플랫폼 운영 매뉴얼
지원 환경/Artifact/Checksum, 계정/Directory, 전체 Leaf Property·환경변수·Profile, Secret/Certificate, DB3 설치/Migration/Drift, Kafka, 설치, 기동/종료/Health, 배포, Config Partial Apply, Log/Metric/Trace, Capacity, Backup/Restore, Upgrade/Rollback, DR와 장애 Runbook을 다룬다.

### 9.7 90 BZA 매뉴얼
BZA 도입/설치, 초기 관리자, 조직/직원/사용자, Role/Permission/Data Scope, 결재/위임/대결, Attachment/Notification, Session/Masking/Audit/Export, 업무 Domain 연계, Backup/Restore/Upgrade/Rollback을 제품 Source 기준으로 다룬다.

### 9.8 91 Gateway 매뉴얼
선택/설치, Route/Predicate/Filter/Rewrite, Target/Discovery/LB, AuthN/AuthZ/HMAC/Audience/Body Hash/Nonce/SSRF/TLS, Timeout/Retry/Circuit/Bulkhead, Idempotency/Attempt/UNKNOWN, Validation/Approval/Publish, ACK/NACK/Partial, LKG/Rollback, Scale-out/Drift/Reconcile, transaction lineage, Probe/Health/ADM/Runbook을 다룬다.

## 10. Property 문서화

Leaf Property는 Key, 환경변수, Type, Default, 필수, 범위, Consumer, Profile, 재기동, Secret, 오류 증상, 확인 명령/정상 결과, Rollback을 포함한다. Prefix만 나열해 전체 Reference로 부르지 않는다.

## 11. 화면 문서화

Route Registry와 실제 Vue Component를 함께 확인한다. `expectedOperationIds`를 Button Permission과 동일하다고 간주하지 않는다. Server Session의 실제 Operation/Button 권한, 활성조건, Version/Approval/Idempotency와 Runtime 오류 처리까지 쓴다.

## 12. OpenAPI·Generated Code

Canonical OpenAPI, Generated Client, Runtime Route/Operation이 같은 계약을 사용해야 한다. Generated Client를 수기 변경해 Drift를 숨기지 않는다. canonical-compat generation/verification, operation consumer, runtime OpenAPI parity 같은 Gate의 Source 존재와 실제 실행 결과를 분리해 기록한다.

## 13. PDF·DOCX 제작 표준

13개 문서는 DOCX를 편집 정본으로 생성하고 그 DOCX에서 PDF를 변환한다. 제목/Heading 계층, TOC, 반복 Table Header, 표 행 분할 방지, Page Header/Footer, 한글 글꼴, 코드/경로 가독성을 유지한다. PDF/DOCX 내부에 Authoring Markdown Anchor나 Backtick Artifact를 노출하지 않는다.

변환 후 DOCX와 PDF를 모두 페이지 이미지로 렌더하고 전 페이지를 확인한다. 잘림, 겹침, 깨진 글리프, 표 폭 침범, 빈 페이지, 과도한 반쪽 공백이 있으면 수정 후 재렌더한다.

## 14. Source Trace와 문서 변경 Trigger

Source/API/SQL/Config/Frontend/Permission/Route/Test 변경이 사용자 절차·입력·상태·복구·보안에 영향을 주면 담당 PDF/DOCX를 같은 변경 단위에서 현행화한다. 문서의 기준 Commit을 기록한다.

## 15. 삭제·가비지 규칙

Guide/설계 `.md` Authoring 파일, 이전 문서 Revision Evidence, 현재 README/PDF/DOCX에서 참조하지 않는 문서 전용 이미지와 이번 작업의 tmp/bak/중간 Script/중복 ZIP은 정확한 삭제 Manifest로 관리한다. Source, 추적 파일, 다른 작업자의 변경, 전체 미추적 파일을 광범위하게 삭제하지 않는다. `git clean`과 wildcard 전체 삭제를 사용하지 않는다.

README가 참조하는 브로셔 자산은 삭제하지 않는다. 빈 폴더는 하위부터, 실제로 비었을 때만 제거한다.

## 16. 최종 검증

작업 종료 전 `git status --short`, `git diff --name-status`, `git diff --stat`, `git diff --check`, 미추적 파일, README 링크, PDF/DOCX Pair, 문서 수, 이미지 참조, 삭제 Manifest, 민감정보 패턴을 확인한다. Local Working Tree를 직접 확인하지 못한 경우 `미검증`으로 명시한다.

## 17. 전달 패키지

문서 변경 산출물은 Repository Root 상대경로를 유지한 단일 ZIP으로 제공한다. ZIP에는 최종 신규/수정 파일과 정확한 삭제 Manifest만 넣고 build/log/tmp/bak/.git/IDE/과거본을 넣지 않는다. ZIP SHA-256, 기준 Repository/Branch/Commit, 파일 수와 경로, 신규/수정/삭제, 적용/검증/미검증/Rollback, Commit·Push 미수행 여부를 기록한다.


## 18. Current Release 상태와 문서 표현

현재 기준 Commit은 `cd5baccb02245a980e5998aa0dc9bac579fc019f` (`07_04`)이다. 이 Commit에는 전체 프로젝트 Finalization Mandate와 QA Scope 보정이 추가되었고, R6J가 검수한 Product Source는 `3ed676061246c9db3e44f29e254c0393ecca3929` (`07_02`)와 동일하다. R6J 중앙 판정은 `미통과 — RELEASE_BLOCKED`이며 56개 통합 Finding(P0 44/P1 11/P2 1), 34개 직접 재개발 항목이 존재한다.

공식 PDF/DOCX는 현재 Source가 제공하는 기능과 현재 미검증/실패/재확인 필요 경계를 함께 기록한다. QA Finding 전체를 README에 노출하지 않지만, 사용자 절차의 안전성에 영향을 주는 Release workflow, transaction/file/DB logging, Approval UNKNOWN/reconcile, ADM/BZA action permission, OpenAPI drift, EDU security, false-green Gate와 target runtime 미검증은 담당 매뉴얼에서 숨기지 않는다.

`RELEASE_BLOCKED` 상태에서 문서를 Release 승인서처럼 표현하지 않는다. 후속 Product Source가 변경되면 exact result SHA를 다시 기록하고 해당 PDF/DOCX를 같은 변경 단위에서 현행화한다. Source 개선만 확인된 항목은 Runtime/Evidence가 없으면 `완료`로 승격하지 않는다.


### 18.1 07_04 Scope Correction

34개 직접 재개발 항목은 전체 프로젝트 Scope가 아니다. 프로젝트 완료 범위는 중앙 Requirement 93/93, Finding 56/56, known direct rework 34/34, 최상위 Requirement 전체, Runtime/GA 전체와 개발·QA 중 자체 발견 결함 전체다. 문서도 이 전체 범위의 Source 변화와 상태 변화에 따라 다시 현행화한다.
