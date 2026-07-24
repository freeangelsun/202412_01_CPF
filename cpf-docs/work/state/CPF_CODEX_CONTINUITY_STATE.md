# CPF Codex Continuity State

## 1. Repository 공통 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수/Overlay 기준 Commit: `22b1874e67547372b51a4bcd21f47aea6fcb5c25`
- 상태 성격: 대규모 Architecture/DB 전환 WIP — 완료본 아님
- Canonical Requirement: 162개
- Legacy Alias: 8개(완료율 중복 집계 금지)
- 최상위 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- Requirement Mapping: `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
- 장기 결정: `CPF_CODEX_DECISION_LOG.md`

이 파일은 회사 PC/집 PC/여러 Codex 계정이 공유하는 영속 인수인계 정본이다. 기록이 없다는 이유만으로 과거 구현이 없었다고 판단하지 않으며 Git/과거 요청/Evidence를 역추적한다.

## 2. Protected Baseline — 다시 Greenfield로 만들지 말 것

- Service Call Engine / Registry / Health / Routing / Failover / Circuit 기반
- Standard Header / transactionGlobalId / Segment / Trace
- Broker Outbox / Inbox / DLQ / Replay 기반
- Idempotency / Reconciliation 기반
- Core Fixed-Length API/SPI와 Reference EDU 연결 방향
- Generator Metadata + Central Vendor Domain Template 방향
- `cmnDB.cmn_sample_item` minimal sample 정책
- `batDB` 물리 Ownership 분리 방향

각 항목은 최신 회귀 Evidence가 없으면 `완료`로 승격하지 않지만 스타일 선호 때문에 재작성하지 않는다.

## 3. 이번 Overlay가 직접 보강한 Source/SQL/문서

- Requirement 133→126 유실을 재조정하여 162 canonical + 8 alias로 복구
- Module-local Vendor SQL/MyBatis를 제거할 Cleanup allowlist 제공
- Production DB resource resolver/catalog를 Central Vendor Pack 필수 fail-fast로 변경
- `CpfDatabaseVendor.flywayLocation()`의 Module-local classpath 계약은 호환 API로 deprecated 처리하고 신규 Consumer 사용을 금지
- 로컬 Runtime Harness `scripts/runtime-start-services.ps1`가 중앙 Vendor Pack을 `CPF_DB_VENDOR`/`CPF_DB_RESOURCE_ROOT`로 Java child process에 주입하도록 보강
- 해당 resolver/catalog unit test를 중앙 Pack 기준으로 변경
- ADM 운영자 조직 Profile + 플랫폼 위험조치 Approval DDL baseline 추가
- BZA 조직 유효기간, 직급/직책, Assignment, 다중 Role, Approval Policy/Participant/Delegation DDL baseline 추가
- ADM/BZA Approval public enum baseline과 JavaDoc 추가
- ADM/BZA Approval DDL에 상태/유효기간/ALL·ANY·N_OF_M/Y-N 등 무결성 Check와 Immutable History 삭제 제한 보강
- 기존 BZA 사람별 direct-line 경로는 신규 Target column에 맞춰 INSERT를 정렬하고, 실제 미구현 ANY/N_OF_M을 ALL처럼 처리하지 않도록 ALL-only fail-closed로 보정
- generated MariaDB Empty Install/Verify를 split DDL에서 정적 재생성
- 개발 Evidence 기본 출력 위치를 `build/cpf-evidence/current`로 변경
- 일반 qualityGate와 최종 DOCX publicationGate 분리
- Gradle PowerShell 실행 기본을 `pwsh`로 전환

이 항목들은 실제 Build/DB/Runtime을 실행하지 않았으므로 Runtime 완료 근거가 아니다.

## 4. 이번 검수에서 확정된 주요 Gap

- generated/central 설치 정본과 split DDL이 기존 Commit에서 불일치했음
- `mbrDB` minimal sample DDL과 `cpf-member` Mapper/Auth Source가 서로 다른 Table 계약을 사용
- ACC가 공통 minimal transaction sample로 완전히 전환되지 않음
- ADM이 BAT/REF 등 Owner DB를 직접 접근/수정하는 Architecture 위반
- ADM 위험조치 Approval Engine/API/UI는 미구현
- BZA Approval은 사람별 Line skeleton 수준이며 ALL/ANY/N_OF_M, 부서합의, 위임/부재, 정책 Version/Simulation이 실제 Engine에 연결되지 않음
- Batch/Center-Cut Runtime Ownership 일부가 cpf-core에 남아 있고 독립 CenterCutRunner 미구현
- Multi-DB는 중앙 Pack 골격만 존재하며 비-MariaDB 실제 제품 SQL/Runtime은 미구현/미검증
- Historical Flyway V6/V29 checksum 불일치 미해결
- `cpf-docs/evidence/20260722_01` Stale Evidence가 재유입됨

## 5. PC별 Local Environment 상태

### HOME PC

- Windows 개발 노트북
- 사용자 보고 Repository 경로: `C:\dev\projects\jck\202412_01_CPF`
- 사용자 보고 JDK: `C:\dev\java\jdk-25.0.3.9-hotspot` 계열. 실제 시작 시 `java -version`/`JAVA_HOME` 재확인
- Gradle Wrapper / Node/npm / Local MariaDB 사용 환경
- 2026-07-24 이전 WIP에서 MariaDB 12.3.2에 최적화 전 127-table Fresh Provision/Empty Install/Product Seed/Verify를 실제 수행한 기록이 있음
- 해당 127-table 결과는 현재 Overlay/Schema의 성공 Evidence가 아님
- 최신 123-table 정적 Overlay 구조는 아직 Reset/Fresh Install/Runtime 미실행
- Credential 값은 이 문서에 기록하지 않음

### COMPANY PC

- 과거 CPF 작업/검증 이력이 존재하므로 새 Codex가 과거 Git/Evidence/요청서를 반드시 확인
- 사용자 보고 JDK: `D:\Java\jdk-25.0.3.9-hotspot` 계열. 실제 시작 시 `java -version`/`JAVA_HOME` 재확인
- 직전 인수인계 기준 CPF 소유 Schema/Table은 정리/삭제한 상태였으나, 현재 실제 Local DB 상태는 이 Overlay 작성 시 직접 재확인하지 않았으므로 `재확인 필요`
- HOME의 DB/Runtime 성공을 COMPANY 성공으로 승계 금지

## 6. DB 정적 Baseline

Overlay split DDL 기준 정적 Table 수:

- cpfDB 35
- cmnDB 1
- admDB 25
- batDB 24
- refDB 3
- mbrDB 1
- bzaDB 25
- accDB 2
- exsDB 7
- 총 123

123은 정적 `CREATE TABLE` inventory이며 실제 MariaDB 설치 성공 수치가 아니다. Sequence/Constraint/Index/Seed/Grant도 Runtime에서 별도 검증한다.

## 7. 다음 Codex 시작 Gate

1. `git status`, HEAD, `origin/master`, ahead/behind 확인
2. 사용자가 Overlay/Cleanup을 적용했는지 실제 Diff 확인
3. Final Target → Continuity Ledger → Current Request → Decision Log → 이 파일 → Git/Evidence 순으로 대조
4. 삭제된 Module-local Vendor resource를 복구하지 않음
5. `pwsh -File scripts/build-all-install-sql.ps1`로 generated parity 확인
6. `scripts/runtime-start-services.ps1` 포함 Build/Test/Runtime Harness가 선택된 Central Pack만 사용함을 확인하고 오류를 신규 Schema 기준으로 수정
7. MariaDB allowlist Reset dry-run → 명시 apply → Fresh Install → Seed → Verify
8. Module Runtime/API/Browser/Failure 검증

## 8. 의미 있는 Checkpoint마다 반드시 기록

- PC 구분(HOME/COMPANY)
- 작업 계정/세션을 식별할 수 있는 비민감 별칭
- 시작/종료 Requirement ID
- 기존 구현 `유지/보완/확장/교체/제거` 판정
- 실제 변경 파일/Module
- 구현 Architecture와 선택 이유/대안
- 실제 실행 명령과 PASS/FAIL
- DB/Runtime 상태
- Evidence 경로
- 미검증/Blocker
- 다음 PC/Codex의 첫 작업
- 반복하면 안 되는 완료 작업

크레딧 종료가 예상되면 새 범위를 시작하지 말고 이 파일을 먼저 최신화한다.

## 9. 다시 하면 안 되는 것

- 사용자 변경을 reset/clean/revert로 폐기
- 삭제한 Module-local Vendor SQL/MyBatis fallback 복구
- Stale Evidence 복구
- Historical Flyway checksum 맞춤 편집
- Requirement ID Mapping 없는 삭제/rename
- MBR/ACC/REF/PAY/INS 고정 목록 Generator
- ADM의 다른 Owner DB 직접 수정
- Batch/Center-Cut Runtime을 cpf-core에 더 적치
- 실행하지 않은 Vendor/Browser/DB/Multi-instance 검증 완료 처리
- 사용자 승인 없는 commit/push/branch 생성

## 10. 정확한 다음 작업 순서

1. Overlay/Cleanup Diff 검토와 central resource compile/test 교정
2. MBR/ACC minimal sample Source/Mapper/API 정합성 완결
3. ADM Owner Command/Query Boundary 교정
4. ADM Approval Engine/API/UI/권한/Audit 구현
5. BZA 조직/직원/Assignment/다중 Role + Approval Engine/API/UI 구현
6. Batch/CenterCut Runtime Ownership/CenterCutRunner 완결
7. MariaDB Fresh Install + 전체 Runtime
8. Generator arbitrary domain lifecycle
9. Historical Migration/Upgrade/Rollback/Backup/Restore
10. 전체 162 Requirement 회귀와 신규 Evidence/Guide 동기화
