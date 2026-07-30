# CPF 20260730_01 최종 폐쇄 작업요건 취합 기준선

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 확인 SHA: `4732d17259e39da93e781fd14cd545b3c897fa87`
- 성격: Codex 사용량 제한으로 중단된 WIP 복구 + 최종 검증개발 + Evidence 폐쇄
- 현재 원자 작업요건: **36건**
  - 개발·Architecture 보완: 10건
  - 실행 검증·회귀검증: 15건
  - 정본·Evidence·Hygiene 폐쇄: 11건

> 통합 QA Ledger 2,715행은 별도 검증 체크포인트다. 36건과 합산하지 않는다. QA 추가 요청은 이 기준선과 ID/Root Cause/Owner/Evidence 기준으로 중복 제거한 뒤 최종 건수를 확정한다.

## A. 개발·Architecture 보완 — 10건

| ID | 요건 | 현재 판정 |
|---|---|---|
| DEV-01 | ADM Runtime Control 기본 운영 화면을 Capability Schema 기반 Typed Form·구조화 Preview/Status/Audit UI로 완결하고 기본 Raw JSON 입력·출력을 제거한다. | 부분 구현 확인 |
| DEV-02 | BZA Menu를 Recursive Tree CRUD, Parent 이동, Reorder, Cycle 방지, 하위 영향 확인, Optimistic Lock, 권한·감사까지 완결한다. | 부분 구현 확인 |
| DEV-03 | `check-work-context-sha.ps1`을 Gradle Quality/Final Closure Gate와 `verify-cpf-final-completion.ps1`에 연결해 Current/Handover/Evidence/Matrix의 Exact-SHA 불일치를 실패 처리한다. | 미연결 확인 |
| DEV-04 | Report·Matrix·Evidence Gate가 ID 문자열 존재만 보지 않고 Exact SHA, 실제 명령, 종료 코드, 환경, 상태, Evidence 유효성을 검사하도록 강화한다. | 부분 구현 확인 |
| DEV-05 | 중단된 Runtime Query `PORTABLE_ONLY` Gate를 완결한다. HTTP 메서드 문자열 오탐을 제거하고 Java Inline SQL은 ANSI/JDBC 중립 구문만 허용하며 Vendor 차이는 Runtime Query Pack으로 강제한다. | 중단 WIP |
| DEV-06 | UTF-8 정책과 Gate를 PowerShell 7 실행 기준으로 확정한다. PS1 UTF-8 no-BOM 지원과 기존 한글 QA CSV의 선택적 BOM을 일관되게 처리한다. | 중단 WIP |
| DEV-07 | Generator `ProductionProfile`과 `sampleitem`의 제품 의미를 확정한다. 필요 시 `FeatureName`·Package·Route·Table 입력 계약을 추가하고 Guide/Test와 일치시킨다. | 재확인 필요 |
| DEV-08 | 외부 Controller/Public Contract에 남은 `Map<String,String>` Request/Response를 Typed DTO, Validation, OpenAPI 예제로 교체한다. 내부 SQL Parameter Map은 Internal로 한정한다. | 부분 구현/재확인 |
| DEV-09 | MBR/ACC/EXS 고정 Domain 가정을 Build, Script, Seed, Profile, Verify, QA Matrix에서 제거하고 `domain-manifest.json` 기반 동적 판정으로 통일한다. | 대규모 수정 주장, 최종 회귀 미검증 |
| DEV-10 | Canonical DB 생성 순서, Platform 160 Table 기준, 3 Vendor Pack Parity, Historical Migration 불변성, Manifest/Provision/Seed/Verify 동기화를 최종 확정한다. | 대규모 수정 주장, 최종 회귀 미검증 |

## B. 실행 검증·회귀검증 — 15건

| ID | 요건 | 필수 결과 |
|---|---|---|
| VAL-01 | 최신 SHA에서 Root `clean test assemble` 실행 | 전체 성공 로그와 종료 코드 |
| VAL-02 | ADM Frontend Production Build, ESLint, Vitest, Typecheck | 명령별 Evidence |
| VAL-03 | BZA Frontend Production Build, ESLint, Vitest, Typecheck | 명령별 Evidence |
| VAL-04 | Architecture, Dependency, Security, SQL, Generator, Hygiene 등 전체 Static Gate 실행 | 실패 0, False Green 검토 |
| VAL-05 | `verify-cpf-final-completion.ps1` 최종 실행 | 2,715 Ledger 포함 실제 PASS |
| VAL-06 | 기존 MariaDB 보존 DB에서 Drift→Backup→Upgrade→Verify→Rollback→Re-apply→Seed→Runtime Query→Restore 검증 | 데이터 보존 및 Exact-SHA Evidence |
| VAL-07 | 기존 DB와 분리된 신규 MariaDB Database/Profile에서 Clean Install·Reinstall·Verify | 완전 신규 설치 성공 |
| VAL-08 | PostgreSQL Install·Migration·Upgrade·Rollback·Runtime Query 검증 | 서버 부재 시 구현 완료 근거와 실행 절차 분리 |
| VAL-09 | Oracle Install·Migration·Upgrade·Rollback·Runtime Query 검증 | 서버 부재 시 구현 완료 근거와 실행 절차 분리 |
| VAL-10 | Generator 임의 Domain 2종 이상 Create→Verify→Test→Boot JAR/WAR→Remove 및 MBR Golden Parity | 사용자 영역 보호와 3 Vendor Artifact 포함 |
| VAL-11 | Redis 정상·Down·Timeout·Recovery·Invalidation·다중 Instance·Lock/Fencing·ADM Control 검증 | Fail-open/closed 정책별 Evidence |
| VAL-12 | BAT Multi-worker·Center-Cut·Lease/Fencing·Partial Failure·Unknown·Retry·Recovery 검증 | 중복 실행·Ghost Lock 방지 Evidence |
| VAL-13 | ADM/BZA Browser E2E 및 Role별 READ/WRITE/DELETE·위험 Action 음성 Test | 버튼 미노출 + Backend 403 + Audit |
| VAL-14 | File/Attachment/CSV/XLSX 대용량·Streaming·Injection·Zip Bomb·Path Traversal·중단복구·권한·감사 검증 | 보안·복구 Evidence |
| VAL-15 | Local/Remote Service Call과 Gateway의 Timeout·Retry·Circuit·Target-down·Result Unknown·Trace 연계 검증 | 동일 JVM/분리 WAS 계약 Evidence |

## C. 정본·Evidence·Hygiene 폐쇄 — 11건

| ID | 요건 | 현재 문제 |
|---|---|---|
| DOC-01 | `CPF_CURRENT_WORK_REQUEST.md` 최신화 | 과거 SHA·과거 Codex 역할 잔존 |
| DOC-02 | `CPF_NEXT_WORK_REQUEST.md` 정리 | 실제 잔여 작업과 불일치 |
| DOC-03 | 최종 개발 보고서 작성/갱신 | 최신 변경·검증 결과 미반영 |
| DOC-04 | 최종 Handover 작성/갱신 | 최신 SHA·Push·검증 상태 미반영 |
| DOC-05 | Continuity State와 Decision Log 최종 Checkpoint 기록 | 최신 우선 Checkpoint가 부분 구현 |
| DOC-06 | Final Target 162 Traceability 최신 SHA 재평가 | 다수 미검증 및 Stale SHA |
| DOC-07 | Enterprise Requirement 816 Closure 최신 SHA 재평가 | 816건 전부 미검증 상태 |
| DOC-08 | QA Scenario 387 Matrix 최신 SHA 재평가 | 387건 전부 미검증 상태 |
| DOC-09 | 통합 QA Master Ledger 2,715행을 Exact-SHA 실제 Evidence로 폐쇄 | 2,715행 전부 미검증 |
| DOC-10 | Evidence Index와 실행 원본 정리: 명령, Profile, 환경, 시작·종료 시각, Expected/Actual, Exit Code, DB 결과, 로그, 민감정보 제거 | 최신 SHA Evidence 부재 |
| DOC-11 | build/tmp/probe/log/zip/patch/빈 구조/Dead Code/Stale Evidence 정리 후 `git diff --check`, Working Tree Clean, Local/Remote SHA 일치, Commit/Push | 중단 WIP 정리 여부 미확인 |

## QA 추가 요청 취합 규칙

QA 목록을 받으면 각 항목을 다음 순서로 처리한다.

1. 기존 36건의 ID와 동일 Root Cause인지 확인
2. 동일 원인이면 기존 ID에 QA ID를 병합하고 건수를 늘리지 않음
3. 별도 Owner·Consumer·Failure Mode라면 신규 원자 요건으로 추가
4. 단순 Runtime Scenario는 개발요건이 아니라 해당 `VAL-*` 또는 2,715 Ledger Scenario로 연결
5. 문서 표현만 다른 중복 Gap은 하나로 통합
6. 최종 목록에는 Requirement → Source/API/SQL/Test/Evidence와 구현 → Requirement/Owner/Consumer의 양방향 추적을 포함

## 현재 판정

- 완료 승인: 불가
- Source 상태: 부분 구현 및 중단 WIP 포함
- 검증 상태: 미검증
- 우선 작업: 신규 기능 확대가 아니라 DEV-01~DEV-10과 Final Gate/Evidence 폐쇄
