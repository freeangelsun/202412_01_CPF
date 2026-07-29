# CPF 최종 전수검수·보완개발·완결 처리 요청서

## 1. 작업 목적

이번 작업은 CPF(Core Platform Framework)의 프로젝트 마무리 전수검수다.

단순 리뷰, 오류 목록 작성, 부분 수정 또는 후속 작업 이관으로 종료하지 않는다. 최신 `origin/master` 전체를 CPF 최종 목표 정본과 대조하고, 발견되는 결함·누락·회귀·잘못된 Ownership·가비지를 직접 보완하여 개발 완료 상태로 종료한다.

Codex의 역할은 다음을 모두 포함한다.

1. 전체 Repository 최종 검수
2. 발견 결함의 직접 보완개발
3. 테스트 및 Gate 수정·재실행
4. Source·SQL·API·Frontend·Generator·문서·Evidence 정합화
5. Repository Cleanup
6. Commit 및 `origin/master` Push
7. 최종 인수인계 작성

기존 문서에 “Codex는 검수만 수행”, “Source 수정 금지”, “Commit/Push 금지”라는 과거 지시가 있더라도 이번 요청이 우선한다.

---

## 2. 기준 Repository

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 이 요청서 작성 시 확인 SHA: `472fe115e52f56f2583adab5981a764d830a8888`
- 최우선 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

반드시 최신 `origin/master`를 Pull한 후 실제 시작 SHA를 기록한다. 위 SHA와 달라졌다면 최신 SHA를 기준으로 작업한다.

작업 시작 명령:

```powershell
git status --short
git branch --show-current
git fetch origin
git checkout master
git pull --ff-only origin master
git rev-parse HEAD
```

기존 로컬 변경이 있다면 무조건 삭제하거나 덮어쓰지 말고 소유자와 목적을 확인한다.

---

## 3. 필수 참조 문서

아래 문서를 먼저 읽고 역할을 파악한 후 Source를 검수한다. 문서의 완료 표시보다 실제 Git 구현을 우선한다.

### 최상위 정본

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md`
3. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
4. `cpf-docs/governance/CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
5. `cpf-docs/governance/DOCUMENT_GOVERNANCE_GUIDE.md`

### 현재 작업 및 인수인계

1. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
2. `cpf-docs/work/current/CPF_20260729_04_FINAL_HANDOVER.md`
3. `cpf-docs/work/current/CPF_20260729_04_NEXT_SESSION_DEVELOPMENT_HANDOVER.md`
4. `cpf-docs/work/current/CPF_20260729_04_FINAL_DEVELOPMENT_REPORT.md`
5. `cpf-docs/work/current/CPF_20260729_04_CODEX_FINAL_REVIEW_REQUEST.md`
6. `cpf-docs/work/current/CPF_INTEGRATED_VERIFICATION_PLAN.md`
7. `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260728_02.md`
8. `cpf-docs/work/current/CPF_20260729_05_FINAL_DELIVERY_NOTE.md`

### 추적 및 QA 자료

1. `cpf-docs/quality/CPF_FINAL_TARGET_162_TRACEABILITY_20260729_04.csv`
2. `cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_816_DEVELOPMENT_CLOSURE_20260729_04.csv`
3. `cpf-docs/quality/qa-20260729/CPF_QA_387_FINAL_VALIDATION_MATRIX_20260729_04.csv`

파일이 이동되었거나 이름이 변경되었다면 Repository에서 해당 정본의 최신 위치를 찾아 사용한다.

Requirement → Source/API/SQL/Test/Runtime/Evidence 및 구현 → Requirement/Owner/Consumer/운영 기능의 양방향 추적을 수행한다.

---

## 4. 완료 원칙

이번 작업은 프로젝트 마무리 검수다. 다음 상태로 종료하지 않는다.

- 부분 구현
- 미구현
- TODO/FIXME
- 임시 구현
- 인터페이스만 있고 Consumer가 없는 상태
- 빈 Adapter 또는 Mock 전용 제품 구현
- UnsupportedOperationException으로 기능 회피
- 예외를 삼키고 성공 처리
- 오류를 빈 목록 또는 0건으로 위장
- 실패를 무시하는 Script
- 현재 값을 기준으로 자동 갱신하여 통과시키는 False Green Gate
- 후속 개발 요청서만 작성하고 종료
- 환경이 없다는 이유로 Source·SQL·Config·Test를 미구현

오류를 발견하면 목록만 작성하지 말고 직접 수정한다. 최초 실패 한 건만 고치지 말고 동일한 Root Cause와 패턴을 Repository 전체에서 검색해 함께 수정한다.

기능을 수정할 때 다음 영향을 하나의 완료 단위로 검토한다.

- Owner Source
- Public API/SPI/Internal 경계
- 실제 Consumer
- 정상·오류·경계·부분 실패
- Retry·Recovery·Reprocess·Compensation
- Idempotency·Concurrency·다중 인스턴스
- Config
- SQL·Migration·Install·Upgrade·Rollback
- Generator 및 생성 산출물
- Frontend
- Security·Authorization·Audit·Masking
- Test
- EDU·OpenAPI·JavaDoc·Guide
- Evidence

---

## 5. 외부 서버 및 로컬 DB 처리

현재 환경에 WAS, Oracle/PostgreSQL 서버, Redis Cluster, 메시징 서버, Browser 자동화 환경, 다중 인스턴스 장비가 없더라도 개발 자체는 완결한다.

환경이 없는 항목은 다음 방식으로 처리한다.

1. Source·설정·운영 기능·복구 기능까지 완전 구현
2. Unit/Slice/Contract/Architecture Test 수행
3. Fake, Stub, 실패 주입, Test Profile 등으로 검증 가능한 범위를 최대화
4. Static Gate, Compile, Frontend Build, SQL Validation 수행
5. 실제 환경 실행 명령과 기대 결과 작성
6. 실행하지 못한 Runtime 검증만 “환경 제약 미실행”으로 분리

환경 부재는 미구현 사유가 아니다.

### 로컬 DB 중요 규칙

현재 로컬 DB는 임의로 삭제하거나 초기화하지 않는다.

먼저 현재 DB를 대상으로 다음을 검증한다.

- 기존 Schema와 최신 정본 간 Drift
- 누락 Migration
- Upgrade
- Checksum
- Seed/Metadata 중복
- Install Script 재실행 멱등성
- Index/FK/Column 정합성
- 기존 데이터 보존

Clean Install 검증은 기존 DB와 분리된 새 Database 또는 Schema를 생성해 수행한다.

기존 DB 삭제가 정말 필요하면 반드시 먼저 Dump 또는 복구 가능한 백업을 만들고, 삭제 이유·백업 경로·복구 명령을 Evidence에 남긴다.

공식 지원 DB Vendor는 다음 3종뿐이다.

- Oracle
- PostgreSQL
- MariaDB

MySQL, MSSQL, H2를 공식 지원 Vendor로 취급하는 Source, Script, SQL, Guide, Matrix가 남아 있으면 제거하거나 비공식 Test 용도로 명확히 격리한다.

---

## 6. 전수검수 범위

최근 변경 파일만 검수하지 말고 Repository 전체를 검수한다. 최근 대규모 수정으로 회귀 가능성이 높으므로 전체 정본 기준으로 판정한다.

### 6.1 Build와 Architecture

- Java 25 및 Gradle 9.1 호환성
- Root `settings.gradle`, `build.gradle`
- Module 누락·중복
- 순환/역방향 의존
- Public API, SPI, Internal 경계
- 실제 Consumer 없는 추상화
- Dead Code와 중복 구현
- Module Ownership
- 동일 JVM과 분리 WAS 양쪽 호출
- Runtime Classpath와 배포 Artifact

### 6.2 Module Ownership

- `cpf-core`: topology-independent 핵심 계약
- `cpf-common`: 고객 업무 공통
- `cpf-admin`: 플랫폼 운영·관리
- `cpf-biz-admin`: 업무 관리자
- `cpf-batch`: Batch·Scheduler·Worker·Agent·Center-Cut Runtime
- `cpf-gateway`
- Generated Domain과 Golden Template
- `cpf-tools`
- `deploy`

`cpf-core`에 특정 업무·Admin·Batch Runtime·선택 제품 기능을 무분별하게 두지 않는다. 외부 Module이 Internal Package를 직접 Import하지 않도록 한다.

### 6.3 거래와 장애 대응

- 동일 JVM/Remote 호출 계약
- Timeout
- Retry
- Circuit Breaker
- 결과 불명 거래
- Idempotency
- Transaction ID
- Distributed Trace
- Partial Failure
- Compensation
- Recovery/Reprocess
- 다중 인스턴스
- Instance 식별과 운영 추적

### 6.4 보안·권한·감사

- 인증·세션
- READ/WRITE/DELETE
- 위험 작업 권한·사유·승인
- Before/After Audit
- Audit 실패 정책
- Fail-closed
- 민감정보 마스킹
- 로그·Evidence·화면 Secret/개인정보 노출
- 운영 기능 실패와 원 업무 Transaction 경계

### 6.5 ADM/BZA

- Backend API와 Frontend Route/Method 일치
- Feature 단위 구조
- 검색·Paging·상세조회
- 상태·오류·충돌 표현
- 낙관적 Lock
- 권한별 버튼 제어
- Calendar writable/delete/reason/audit
- 사용자·역할·권한·조직·직원
- 결재·첨부·알림·감사
- Session·Runtime Control·Cache
- File Job
- Backup/Restore/DR
- Instance Health/Registry
- Frontend Production Build
- 외부 CDN/Font/Script 비의존
- 접근성·반응형

### 6.6 Cache/Redis

- Public API/SPI Ownership
- Local/Redis Provider
- Cache Aside
- Invalidation과 다중 인스턴스 전파
- Redis 장애 시 정책
- Timeout/Retry/Connection
- Serialization/Namespace/TTL
- Stampede 방지
- Distributed Lock와 Fencing
- Metrics/Health
- ADM 운영 제어
- Product Profile 안전성

Redis가 없어도 단위·계약·실패 주입 테스트는 완료한다.

### 6.7 Batch

- BAT Runtime Ownership
- Standalone Agent
- Script/Properties/Log
- Scheduler/Worker/Center-Cut
- Job/Execution/Restart
- Retry/Recovery
- Lock/Ghost Lock/Lease/Fencing
- Partial Success
- Operator 식별
- 다중 인스턴스
- DB 장애와 0건 구분
- 미지원 Vendor Fail-closed
- 운영 조회·제어

요청 Body의 `requestUser`를 운영자 식별로 신뢰하지 않는다.

### 6.8 Runtime Control

- Typed Contract
- Preview/Apply
- Approval/Audit
- Durable Delivery
- Timeout/Retry
- Partial Failure/Unknown
- Rollback/Reconcile
- 다중 인스턴스별 결과
- 위험 조치 사유와 권한

### 6.9 File/첨부/CSV/XLSX/File Job

- Streaming
- 대용량/메모리 제한
- CSV/Formula Injection
- Zip Bomb
- Path Traversal
- 확장자/MIME/크기 검증
- 암호화
- 임시 파일 정리
- Lease/Fencing
- 중단 복구/재처리/멱등성
- 다운로드 권한/Audit/Masking
- Frontend 연결

### 6.10 DB/SQL/Migration

- Canonical Source
- Oracle/PostgreSQL/MariaDB Parity
- DDL/DML/Index/FK/Metadata/Seed
- Install/Reinstall
- Migration/Checksum
- Upgrade/Rollback/Recovery
- Drift Detection
- 존재하지 않는 Column 참조
- Vendor별 타입·Paging·Lock·Reserved Word 차이
- SQL 중복 정본
- Generator 영향

### 6.11 Generator와 Generated Domain

- 단일 Golden Template
- DomainName/SystemCode 구분
- 충돌 사전 검증
- ACC/MBR/REF/EXS 정합성
- `com.cpf.core.common.*` 직접 Import 금지
- CRUD/Validation/Paging/Header/Error
- Transaction ID/Idempotency/Request Hash
- Optimistic Lock
- 3 Vendor SQL/Migration/Rollback
- Test/EDU/OpenAPI
- 사용자 수정 영역 보호
- 재생성 덮어쓰기 방지
- Memory Adapter 실제 CRUD 또는 Product 비활성화

EXS는 고정 Module이 아니라 Generated Domain 정책을 유지한다.

### 6.12 API·표준·문서

- 단일 `transactionId`
- 표준 Header/Error
- Local/Remote 동일 계약
- OpenAPI와 JavaDoc
- EDU가 제품 API 사용
- 설치·Migration·Upgrade·Rollback Guide
- Batch/Agent/Generator/운영 Guide
- README와 실제 기능 일치
- 삭제된 Legacy 참조 제거

### 6.13 Test와 Gate

- Unit/Integration/Contract/Architecture Test
- Dependency Test
- Generator Test
- SQL Validation
- Frontend Test/Build
- Secret Gate
- Repository Hygiene Gate
- Evidence Gate
- Migration Checksum Gate
- False Green 방지

Test 삭제, Assertion 약화, 예외 삼킴, `--no-verify` 우회로 통과시키지 않는다.

---

## 7. 이전 QA 회귀 필수 확인

다음 항목은 수정 완료 보고를 신뢰하지 말고 실제 Source와 Test로 재확인한다.

1. Audit 실패 삼킴 제거
2. Calendar writable·권한·삭제 API 정합성
3. DB-less Mode와 Product Profile Fail-closed
4. Calendar 동시성·created_by·updated_by·Operator 전달
5. Batch DB 오류와 0건 구분
6. Ghost Lock·Partial Success·Owner Check
7. Batch `requestUser` 신뢰 제거
8. Generator Internal Package Import 제거
9. Memory Adapter Fake CRUD 제거
10. Verify Script False Green 제거
11. Runtime Control Typed Contract와 Durable Delivery
12. Redis 장애·복구·다중 인스턴스
13. File Job 보안·중단 복구
14. BZA Permission Manifest와 Recursive Tree
15. 공식 DB 3종 Parity
16. Root Runtime 물리 이관과 중복 제거

---

## 8. 권장 작업 순서

### 1단계: 기준선 확보

- 최신 master Pull
- 시작 SHA
- `git status`
- 문서 정본과 역할 확인
- 최근 Commit과 전체 Module Inventory

### 2단계: 저비용 Static 전수검수

- Package/Module Ownership
- Dependency/Internal Import
- TODO/FIXME/UnsupportedOperationException
- 예외 삼킴/빈 결과 위장
- Dead Code/중복 Source
- Secret
- Root Hygiene
- Vendor 문자열
- SQL 구조 오류
- Frontend Route/API 불일치
- Legacy 경로

### 3단계: Clean Build/Test

기본 명령:

```powershell
.\gradlew.bat clean test assemble --no-daemon
```

공식 Final Gate Script가 있으면 내용을 먼저 검토하고 실행한다. Gate가 잘못되었다면 Gate부터 수정하고 재실행한다.

Frontend는 각 공식 Frontend의 install/build/test 절차를 실제 Package Manager 기준으로 수행한다.

### 4단계: Root Cause 보완개발

한 건씩 땜질하지 말고 동일 패턴을 전수 수정한다.

### 5단계: 환경 제약 대체 검증

외부 서버 없이 가능한 Unit/Contract/Static/Failure Injection 검증을 최대화한다.

### 6단계: 전체 회귀검증

수정 후 전체 Clean Build/Test/Gate를 다시 실행한다.

### 7단계: Cleanup

가비지·빈 구조·Stale 문서·잘못된 Evidence를 제거한다.

### 8단계: 문서/Evidence 갱신

실제 실행 결과만 기록한다.

### 9단계: Commit/Push

모든 검증과 정리 후 `master`에 Commit하고 Push한다.

---

## 9. Repository Cleanup

작업 완료 전 다음을 전수 확인해 제거한다.

- `build/`, `target/`, `out/`
- 로컬 `.gradle/`
- `node_modules/`, Frontend `dist/`
- log/tmp/temp/bak/old/orig/rej
- Patch/ZIP/Overlay/Extract 중간 산출물
- Crash Dump/DB Dump
- IDE 임시 파일
- 빈 Evidence와 오래된 PASS Marker
- Dead Code
- 사용하지 않는 Sample/EDU
- 중복 Script/SQL/문서
- Root에 잘못 놓인 Runtime/배포 파일
- 의미 없는 `.gitkeep`
- 삭제된 Source를 참조하는 문서
- 빈 Module/Resource 구조

삭제 전 실제 Consumer와 Ownership을 확인한다. 재발 가능성이 있으면 `.gitignore` 또는 Hygiene Gate를 보완한다.

---

## 10. Evidence와 문서

실제 실행한 항목만 PASS로 기록한다.

Evidence 필수 항목:

- 시작 SHA/최종 SHA
- 실행 명령
- Profile/환경/도구 버전
- 시작·종료 시각
- Requirement/Scenario ID
- Expected/Actual
- 종료 코드
- 민감정보 제거 여부

과거 Commit의 Evidence를 현재 Commit 결과로 재사용하지 않는다.

환경 제약 항목은 다음을 구분한다.

- 개발 완료
- 자동 검증 완료
- 실제 Runtime 환경 미실행

정본 문서는 중복 생성하지 말고 역할별 기존 문서를 갱신한다.

필수 갱신:

- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- 최종 개발 보고서
- 최종 인수인계
- 필요한 Traceability/QA Matrix
- 실제 Evidence

인수인계에는 최종 SHA, Push 결과, Working Tree, Build/Test/Gate, 환경 제약, Architecture 결정, 남은 개발 항목 수를 기록한다.

---

## 11. Commit 및 Push

Commit 전:

```powershell
git status --short
git diff --check
git diff --stat
```

모든 수정과 검증이 완료되면:

```powershell
git add -A
git commit -m "Complete CPF final full validation and remediation"
git push origin master
```

Push 후:

```powershell
git status --short
git rev-parse HEAD
git rev-parse origin/master
git log -1 --oneline
```

완료 조건:

- Local HEAD와 `origin/master` 일치
- Working Tree Clean
- Build/Test/Gate 결과 기록
- Push 성공
- 최종 SHA가 보고서와 인수인계에 일치

강제 Push, History Rewrite, 별도 Branch/PR 생성, `--no-verify` 우회는 금지한다.

---

## 12. 최종 완료 조건

다음을 모두 충족해야 완료다.

1. 최신 master 전체 전수검수
2. 발견 결함 직접 수정
3. 부분 구현·미구현·임시 구현 제거
4. Source·Consumer·Test·Config·SQL·Generator·Frontend·Guide 정합화
5. Oracle/PostgreSQL/MariaDB 영향 검토
6. Cache/Redis 장애 대응 완결
7. Batch/Runtime Control/File Job/ADM/BZA 회귀검수
8. 가능한 전체 Clean Build/Test/Gate 통과
9. 외부 환경 없는 항목의 개발 및 대체 자동 검증 완료
10. False Green 제거
11. Repository Cleanup
12. 문서와 Evidence 갱신
13. Commit
14. `origin/master` Push
15. Local/Remote SHA 일치
16. Working Tree Clean
17. 최종 인수인계 완료

최종 개발 미완료 항목과 부분 구현 항목은 `0`이어야 한다.

실제 외부 환경이 없어 실행하지 못한 Runtime 검증은 숨기지 말고 별도로 기록하되, 해당 제품 구현 자체는 완료 상태여야 한다.

---

## 13. 최종 응답 형식

```text
## 최종 결과
- 시작 SHA:
- 최종 SHA:
- Push 결과:
- Working Tree:
- 변경 파일 수:
- 추가/수정/삭제 파일 수:

## 전수검수 결과
- Build:
- Backend Test:
- Frontend Test:
- Architecture/Dependency Gate:
- DB/SQL/Migration Gate:
- Generator Gate:
- Security Gate:
- Hygiene Gate:
- Evidence Gate:

## 주요 보완개발
- Module:
- 문제:
- Root Cause:
- 수정:
- 검증:

## 환경 제약 검증
- 실행하지 못한 항목:
- 미실행 사유:
- 개발 완료 근거:
- 대체 자동 검증:
- 실제 환경 실행 명령:

## Cleanup
- 삭제 경로:
- 삭제 사유:
- 재발 방지:

## 최종 판정
- 개발 미완료 항목 수:
- 부분 구현 항목 수:
- 미해결 Build/Test 오류 수:
- 남은 Repository 가비지 수:
- 최종 판정:
```

오류를 숨겨 완료로 보고하지 않는다. 그러나 구현 가능한 내용은 후속으로 넘기지 말고 이번 작업에서 모두 완결한다.
