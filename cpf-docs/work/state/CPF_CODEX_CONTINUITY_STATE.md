# CPF Codex / ChatGPT Continuity State

## 1. 공통 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- R4 작업 기준 Commit: `536b229bd46411d6be66a5e4697eaca002c50f1a` (`20260724_02`)
- 상태: **대규모 제품 하드닝 WIP — Runtime/실DB 완료본 아님**
- Canonical Requirement: 162개 + Continuity Alias
- 최상위 목표: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 현재 상세 작업: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- 경로 역할 Map: `cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md`
- 장기 결정: `cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md`

회사 Codex / 집 Codex / ChatGPT는 이 파일을 공통 인수인계 정본으로 사용한다.

## 2. R4에서 확정한 제품 정책

### transactionId

- 거래 실행 인스턴스 식별자는 `transactionId` 하나다.
- 외부/선행 요청의 유효 transactionId는 승계한다.
- Scheduler/Batch/Worker/Center-Cut/Agent 등 내부 독립 기동은 Core가 새 transactionId를 생성한다.
- 생성 규격: `17자리 시각 + 3자리 SystemCode + 7자리 wasId + 7자리 sequence = 34자리`.
- 후속 호출은 같은 transactionId를 사용하고 `segmentId/parentSegmentId`로 계층을 표현한다.
- `standardExecutionId`는 실행 정의 ID다.

### Generated Domain

- Generator에 MBR/ACC/EXS/LNG/ING 같은 고정 업무 목록을 두지 않는다.
- 동일 Capability는 Metadata 외 Source/DB/Test 구조가 동일해야 한다.
- 기본 DB는 `${tablePrefix}_sample_item` 한 개의 Golden Sample을 제공한다.
- Controller는 transport Header/거래ID를 Body에서 중복 수신하지 않고 Core Context를 사용한다.
- DB bootstrap은 중앙 Vendor `domain-template` + `initialize-domain-database.ps1`만 사용한다.
- MBR은 최종 Golden Reference Instance로 재정렬한다.
- ACC/EXS는 실제 Consumer 이관 후 고정 Module/Schema 제거 대상이며 현재 즉시 삭제하면 회귀 위험이 있다.

### Repository/DB

- Root 문서 최종 허용: `README.md`만.
- Tool Script: `cpf-tools/scripts/`.
- MariaDB Source SQL: `cpf-tools/db/source/mariadb/`.
- MariaDB generated Vendor Pack: `cpf-tools/db/vendor/mariadb/`.
- DB 변경은 Source SSOT → Vendor Pack → Migration → Mapper → API/UI → Runtime 순서다.

## 3. ChatGPT R4 실제 수정

정적 구현/보강 완료:

- 거래 파일로그 그룹키를 transactionId로 정렬
- Core Transaction Context/Header/Broker/Segment의 새 transactionId 계약 보강
- Generated Domain Mapper 누락 statement와 camelCase alias 보강
- Generated Controller/DTO에서 transport ID 중복 입력 제거 방향으로 Generator Template 보강
- Generator `-ProvisionDatabase` orchestration 추가
- Generated Domain deterministic/parity 검증 Script 추가
- MariaDB Generated Domain Template의 transaction_id/CRUD/Idempotency/Optimistic Lock 계약 보강
- MariaDB split DDL을 Canonical source 경로로 재구성
- BZA 조직 책임/직급/직책/Assignment/Approval Snapshot/Audit/Attachment Governance DDL 보강
- BZA Product Metadata Seed와 Optional 조직 Sample 분리
- Local/REF Runtime fixture를 Optional Seed로 분리
- V38 migration/rollback guard 추가
- DB 초기화 Shell의 MBR 검증 Table과 Canonical manifest 경로 보정
- Root/cpf-tools/scripts/specs relocation operator와 R4 verification Gate 준비
- Generator/DB/Path/3-way handover Guide 현행화

## 4. 정적 DB Baseline

R4 split DDL `CREATE TABLE` 정적 수:

- cpfDB: 35
- cmnDB: 1
- admDB: 25
- batDB: 24
- business modules(MBR/ACC/REF/BZA): 32
- exsDB transitional: 7
- 총: **124**

이 숫자는 정적 Source inventory일 뿐 실제 MariaDB 성공 Evidence가 아니다.

## 5. 현재 중요 Gap

### 부분 구현

- MBR Source에는 과거 회원/Auth 구조가 남아 있어 Golden Generator 결과와 완전 parity가 아님.
- ACC는 `acc_account` Consumer가 있어 즉시 sample-only DB로 바꾸면 회귀함. Consumer migration 필요.
- EXS는 실제 external execution 구현이 남아 있어 Owner 이관 후 제거 필요.
- ADM 일부 조회/제어가 Owner DB 직접 접근 구조를 사용함.
- BZA 조직/결재 DDL은 보강했지만 Engine/API/UI 전체 연결은 미완료.
- ADM 위험조치 Approval Engine/API/UI도 전체 Runtime 미완료.
- Batch/Center-Cut Owner 경계와 Runtime 전체 검증 필요.

### 미검증

- Gradle clean test
- MariaDB Fresh Provision/Empty Install/Product Seed/Verify
- V38 실제 upgrade
- Generated LNG/ING 실제 생성/DB bootstrap/CRUD/delete/regenerate
- Local/Remote/Async/Retry/Target-down transactionId Runtime
- ADM/BZA Browser
- multi-instance/lease/fencing
- PostgreSQL/Oracle/SQL Server/MySQL 실제 Runtime

## 6. 보호해야 할 기존 성공 기반

실제 회귀 Evidence가 나오기 전까지 스타일 이유로 재작성하지 않는다.

- Service Call Engine/Registry/Routing/Failover 기반
- Standard Header/Trace/Segment 기반
- Broker Outbox/Inbox/DLQ/Replay 기반
- Idempotency/Reconciliation 기반
- Fixed-Length Public API/SPI
- Batch bootJar/Worker 기반
- ADM 거래 Timeline/DLQ 운영 기반
- REF EDU 기반

## 7. HOME PC 다음 실행 순서

```powershell
git status
git rev-parse HEAD
# R4 package operator 적용 후
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
.\gradlew.bat clean test --no-daemon
```

사용자가 CPF DB를 직접 삭제한 뒤:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -RequireRun
```

이후 Generator 실검증은 `cpf-docs/development/GENERATOR_DB_LIFECYCLE_TEST_GUIDE.md` 순서를 따른다.

## 8. COMPANY PC 다음 실행 순서

HOME 결과를 그대로 완료로 승계하지 않는다.

- 최신 master/Continuity 확인
- JDK/Gradle/Node/MariaDB 재확인
- 동일 Build/Test
- 회사 환경 DB/Runtime 가능 범위 별도 Evidence

## 9. 절대 금지

- dirty Worktree reset/clean/revert
- Historical Flyway 수정
- Module-local Vendor SQL/MyBatis fallback 복구
- 과거 Global 거래 ID 개념 재도입
- Generator에 고정 업무 Domain 목록 추가
- ACC/EXS Consumer 확인 없이 삭제
- ADM이 다른 Owner DB를 직접 갱신하는 신규 코드
- stale Evidence를 현재 성공 근거로 사용
- 실행하지 않은 검증을 완료 처리
- 사용자 승인 없는 commit/push/branch
