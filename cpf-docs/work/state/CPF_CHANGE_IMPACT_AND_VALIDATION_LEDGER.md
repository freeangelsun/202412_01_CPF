# CPF Change Impact and Validation Ledger

## 2026-07-27 — Change Set B closure / start SHA 00780dc14ef621578f6f7ca61ef1d0c9973c60e6

| Change ID | Requirement | Owner | 직접 영향 | Consumer/간접 영향 | DB/UI/Security | 구현 | 검증 | Evidence |
|---|---|---|---|---|---|---|---|---|
| CHG-20260727-ADM-DATA-SAFETY-001 | B-001~003,006 | cpf-admin | persistence/config/operator service/dto/controller/health | auth/session/audit/frontend | admDB V61, PII, fail-closed | 완료 | Static 완료 / Runtime 미검증 | `CPF_CHATGPT_STATIC_VALIDATION_20260727_05.txt` |
| CHG-20260727-PII-001 | B-007~008 | cpf-core + ADM/BZA | sensitive data API/raw request/masked projection | frontend/audit/permission | PII raw reason, no-store, audit | 완료 | Static 완료 / Browser 미검증 | 동일 |
| CHG-20260727-BZA-STATUS-001 | B-004~006,013 | cpf-biz-admin | employment/admin status/service/auth/UI | directory/approval/support | bzaDB V61, status constraints | 완료 | Static 완료 / Runtime 미검증 | 동일 |
| CHG-20260727-BZA-SQL-OWNERSHIP-001 | B-009~010 | cpf-core + cpf-biz-admin + cpf-tools | Public SQL Catalog, BZA repositories/resources | all BZA repository consumers | MariaDB runtime query resources | 완료 | inline/internal static 0 / DB runtime 미검증 | 동일 |
| CHG-20260727-DB-V61-001 | B-011~012 | cpf-tools/db | canonical schema, V61 forward/rollback/checksum/bundles/verify | ADM/BZA runtime | MariaDB lifecycle | 완료 | Hash/parity 완료 / actual DB 미검증 | 동일 |

### Reopened regression targets

이번 변경으로 다음 과거 PASS는 영향권에 들어가므로 향후 targeted regression 대상으로 다시 연다.

- cpf-core public API compile/test
- cpf-admin compile/test/frontend/auth/runtime
- cpf-biz-admin compile/test/frontend/auth/approval/directory/support
- MariaDB Fresh/Upgrade/Rollback/Verify manifest
- permission seed/API-method parity

BAT Query Pack/Gateway/Generated Domain Source는 직접 변경하지 않았으므로 매 작은 작업마다 고비용 전체 실행하지 않는다. 단 Stack/DB/Final Regression 영향권에 들어오면 재개방한다.

---

## Historical Ledger preserved from 00780dc baseline

이 문서는 작업마다 초기화하지 않는 누적 변경/검증 Ledger다.

## 상태 정의

- `구현`: Source 변경 완료
- `정적 검토`: 파일/계약 수준 확인
- `검증 완료`: 실제 명령 실행 Evidence 존재
- `부분 검증`: 일부만 실행
- `미검증`: 실행하지 않음
- `재검증 필요`: 이후 변경으로 기존 Evidence 영향 가능

## CHG-20260727-ARTIFACT-001 — Local Artifact Federation

> **History:** 이 Change는 `20260727_02` 당시 1차 구현 기록이다. Publication 안전정책은 아래 `CHG-20260727-ARTIFACT-002`가 현재 정책이며 ARTIFACT-001의 auto-sync/fallback 설명을 대체한다.

| 항목 | 내용 |
|---|---|
| Requirement | CPF-BUILD-LOCAL-001, BUILD-001, 독립 Domain Build/Artifact Federation |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | Root Build / `cpf-tools/build` / `cpf-tools/generator` |
| 문제 | 독립 Generated Domain은 `CPF_ARTIFACT_REPOSITORY_URL`이 없으면 Maven Central만 사용하여 로컬에서 막 빌드한 CPF Core/Common/BOM/Plugin을 자동 소비할 수 없었음 |
| 목표 | 성공한 CPF build → shared local Maven repo → Generator/독립 Domain → bootJar/bootWar까지 동일 public artifact 자동 전파 |
| 변경 | shared local repo, explicit local publish task, 성공 build 확인 후 auto-sync wrapper, Generator pre-publish, standalone repository local resolution, convention plugin 적용, bootJar/bootWar package guard |
| 직접 파일 | `build.gradle`, `cpf-batch/build.gradle`, `cpf-tools/build/*`, `cpf-tools/generator/*`, `cpf-tools/scripts/verify-local-artifact-propagation.ps1` |
| Consumer | Generated Domain, 독립 Domain Repository, BAT public contract consumer, external WAS WAR consumer |
| DB 영향 | 없음 |
| Generator 영향 | 있음 — published-artifact 생성/Export 경로 변경 |
| 배포 영향 | 있음 — bootJar/bootWar public dependency 포함 Gate 강화 |
| 회귀 위험 | publication task 이름, included-build task wiring, local/remote repository precedence |
| ChatGPT 검증 | 정적 문자열/괄호/파일 구조 검토만 수행 |
| 미실행 | Gradle publish, Generated Domain standalone build, bootJar/bootWar ZIP 실제 확인 |
| Codex 집중검증 | `verifyCpfLocalArtifactPropagation`, PAY/INS 또는 임시 2 Domain 생성/독립 build/package |
| 상태 | 구현 / 실행 미검증 |

## CHG-20260727-CONTACT-001 — ADM/BZA Contact Semantics

| 항목 | 내용 |
|---|---|
| Requirement | ADM-003, BZA-001, BZA-006, UI 공통, 사용자 추가 연락처 요구 |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | `cpf-admin`, `cpf-biz-admin`, MariaDB canonical DB |
| 문제 | BZA mobile은 존재하나 UI 노출/명칭 부족, 내부 전화번호 없음. ADM 운영자는 연락처 모델 자체 없음 |
| 목표 | `연락처(휴대폰)`과 `내부 전화번호` 분리, 문자열 저장, 선택값 null, ADM 인증 Identity와 Directory Profile 저장 책임 분리 |
| 변경 | ADM DTO/API projection + `adm_operator_profile` persistence + UI, BZA employee service/repository/UI, generic table label, 계약 회귀 Test, canonical schema, V59/rollback |
| DB 영향 | `adm_operator_profile.MOBILE_NO`, `adm_operator_profile.OFFICE_PHONE_NO`, `bza_employee.office_phone_no`; `adm_operator` Identity에는 연락처 미저장 |
| Migration | `V59__admin_contact_model.sql` |
| Rollback | `V59__admin_contact_model_rollback.sql` |
| Generator 영향 | Generated Domain 없음. Platform DB canonical artifact sync 영향 있음 |
| Vendor 영향 | MariaDB canonical 변경. Candidate 4 Vendor 실DB source는 현재 완결되지 않아 임의 SQL 복제 금지 |
| 보안 영향 | 연락처 개인정보. 로그/Evidence 원문 노출 금지, 후속 masking/download 권한 검증 필요 |
| 역호환 | Java record에 구 생성자 호환 overload 유지 |
| ChatGPT 검증 | 정적 parity gate 작성, Source/SQL 구조 검토 |
| 미실행 | Java compile/test, Frontend build, DB sync, MariaDB V59 lifecycle |
| Codex 집중검증 | ADM/BZA module test + frontend + V59 upgrade/rollback/reapply |
| 상태 | 구현 / 실행 미검증 |

## CHG-20260727-BZA-DEFAULT-001 — Employee Safe Default

| 항목 | 내용 |
|---|---|
| Requirement | BZA-005, DEF-003 |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | `cpf-biz-admin`, MariaDB canonical DB |
| 문제 | QA는 직원 재직 기본값 `EMPLOYED`를 요구하지만 Backend/DDL은 `ACTIVE`였음 |
| 목표 | 기존 Row는 유지하고 신규 미입력 Default만 `EMPLOYED`로 정렬 |
| 변경 | Service default, canonical DDL default, V60 forward/rollback, static gate |
| DB 영향 | `bza_employee.employment_status` default만 변경; 기존 데이터 update 없음 |
| Migration | `V60__bza_employee_safe_defaults.sql` |
| Rollback | `V60__bza_employee_safe_defaults_rollback.sql` |
| ChatGPT 검증 | Source/checksum 정적 검토 |
| 미실행 | BZA compile/test, DB sync, MariaDB V60 lifecycle |
| Codex 집중검증 | `check-bza-safe-defaults.ps1`, BZA test, V60 upgrade/rollback/reapply |
| 상태 | 구현 / 실행 미검증 |

## REV-20260727-CODEX-001 — 이전 Codex 결과 재분류

| 항목 | 판정 |
|---|---|
| BAT 158 query pack | 이전 실제 PREPARE 완료, 이번 변경 영향 없음 |
| V58 lifecycle | 이전 실제 upgrade/rollback/reapply 완료, 이번 V59/V60과 별개 |
| MBR/ADM/BZA/REF/ACC/GWY 단일 Runtime | 이전 실제 boot/probe 기록 존재 |
| Build tooling relocation | Source/경로 이동 확인됨, 이번 artifact federation 변경 때문에 해당 부분만 재검증 |
| ADM MBR 결합 제거 | 최신 master에서 삭제 확인 |
| REF MBR service-call 제거 | Echo 대체 파일 확인, dependency-0 focused gate 필요 |
| MBR/ACC Golden parity | 실패/미완료 유지 |
| Browser/Multi-instance | 미검증 유지 |

## Codex 판정 기록 규칙

Codex는 위 Change ID별로 `확인`, `보완`, `반려` 중 하나를 기록한다.
전체 Repository를 다시 처음부터 조사하지 말고, 변경 파일의 실제 Consumer가 Ledger보다 넓을 때만 범위를 확장한다.

## CHG-20260727-STACK-001 — Supported Stack Decision / Version Single Source

| 항목 | 내용 |
|---|---|
| Requirement | QA-STACK-001~003 |
| 시작 SHA | `702bf83580b9c4db2dbba6482ece233e00842f1b` |
| 종료 SHA | 미커밋 Patch |
| Owner | Root Build / `gradle` / Generator / Build Tooling |
| 문제 | Java 25 + Gradle 9.1.0 + Spring Boot 3.4.13은 Boot 3.4 공식 지원 Matrix 밖이며 Version literal이 여러 Build/Generator 경로에 분산됨 |
| 목표 | 현재 상태를 숨기지 않고 `TRANSITION`으로 차단, Java/Gradle/Spring Version 단일 정본, Boot 4.x 별도 Migration Decision |
| 직접 파일 | `gradle/cpf-stack.properties`, `settings.gradle`, `build.gradle`, module build files, Generator/Exporter, Stack Decision Guide |
| Consumer | 모든 Java Module, Generated Domain, Included BOM/Plugin, bootJar/bootWar, CI/Release |
| DB | 직접 없음. Boot/Flyway/MyBatis Migration 시 후속 영향 |
| Runtime | 현재 Version은 변경하지 않음. `commercialReleaseGate`만 fail-closed 강화 |
| 검증 | 공식 Spring/Gradle 지원범위 확인 + 정적 Source 검토 |
| 미검증 | Java25/Gradle9.1 configuration, Boot4 migration, external WAS |
| 상태 | 구현 / Runtime 미검증 |

## CHG-20260727-ARTIFACT-002 — Verified Artifact Supply Modes

`CHG-20260727-ARTIFACT-001`의 기본 방향을 유지하되 QA에서 발견된 publication side effect와 partial set 위험을 보완한다.

| 항목 | 내용 |
|---|---|
| Requirement | QA-ART-001~010, ART-SUPPLY-LOCAL/REMOTE/OFFLINE |
| 시작 SHA | `702bf83580b9c4db2dbba6482ece233e00842f1b` |
| 종료 SHA | 미커밋 Patch |
| Owner | Root Build / `cpf-tools/build` / `cpf-tools/generator` / Artifact Scripts |
| 문제 | Root build 의미 불충분, Local auto-sync side effect, sequential mutable publication, file-existence-only verification, REMOTE에서 일반 `publish` 사용 시 Local side effect 가능 |
| 목표 | `LOCAL_DEV/REMOTE/OFFLINE` 배타 공급, aggregate quality, isolated staging, exact identity/hash/sourceFingerprint 검증, manifest-backed promotion, Remote-only publication |
| 직접 파일 | `build.gradle`, `cpf-batch/build.gradle`, `cpf-tools/build/*`, `CpfDomainConventionPlugin.groovy`, Generator/Exporter, artifact scripts |
| Consumer | Generated standalone Domain, 독립 WAS, BAT Contract consumer, CI/CD, Offline server |
| Build | `aggregateQualityBuild`, staging/local/internal repository task 분리 |
| Security | Remote content filter, Local fallback 차단, credential 원문 미기록 |
| Recovery | Shared Local promotion 실패 시 기존 version directory/manifest rollback |
| 검증 | 정적 Task/Script/manifest/POM/BOM/marker/hash contract 점검 |
| 미검증 | 실제 Gradle publish, Windows concurrent publish/consumer, remote Nexus/Artifactory, bootJar/bootWar exact inner hash |
| 상태 | 구현 / Runtime 미검증; ART-003/004/009/010 일부 후속 |

## CHG-20260727-BASE-001 — Latest Baseline / QA Merge

| 항목 | 내용 |
|---|---|
| Requirement | QA-BASE-001~003 |
| 시작 SHA | `702bf83580b9c4db2dbba6482ece233e00842f1b` |
| 종료 SHA | 미커밋 Patch |
| Owner | `cpf-docs/work/*` |
| 목표 | QA의 `9e4edaef...` 기준을 최신 `702bf835...` 이후 작업으로 재산정하고 History SHA와 현재 검수 SHA를 구분 |
| 재개방 | Build/Plugin/BOM/Generator/Packaging 과거 PASS는 이번 변경 영향으로 재검증 필요 |
| 유지 | BAT 158 Query Pack/V58 SQL 자체는 직접 영향 없음. 최종 aggregate 회귀에서만 확인 |
| CI | GitHub Workflow/Branch Protection은 이번 Patch에서 완료하지 않음 |
| 상태 | 문서 구현 / CI 미구현 |

## CHG-20260727-DOC-BAT-001 — Batch Instance Lifecycle Guide

| 항목 | 내용 |
|---|---|
| Requirement | QA-BAT, Tool Manual, 사용자 Batch Instance 질문 |
| 시작 SHA | `702bf83580b9c4db2dbba6482ece233e00842f1b` |
| Owner | BAT Scheduler/Worker Documentation |
| Source 근거 | `SchedulerDispatchService`, `scheduler-find-due`, `scheduler-execution-insert`, `JobPackDispatcher`, `bat_schedule` DDL |
| 판정 | 일 초기화 선생성 아님. DB Schedule due-time에 CPF Execution 생성, Worker launch 시 Spring Batch JobInstance 생성 |
| 설정 Owner | 업무 Cron/Calendar/Window=`bat_schedule`; Scheduler polling=`cpf.batch.scheduler.dispatch-ms` |
| 미검증 | Misfire, DST, 2 Scheduler takeover, Restart/Rerun identity, 대량 schedule performance |
| 상태 | 문서화 / Runtime 재확인 필요 |

## 20260727_04 이후 재검증 재개방

- `전체 Compile/Test`: Build/Stack 변경 영향으로 **재검증 필요**
- `Included BOM/Convention Plugin`: Publication/Version 변경 영향으로 **재검증 필요**
- `Generated Domain standalone build/package`: Generator/Artifact Mode 변경 영향으로 **재검증 필요**
- `bootJar/bootWar`: Artifact supply/version 변경 영향으로 **재검증 필요**
- `BAT 158 SQL PREPARE`: SQL 직접 변경 없음. **기존 검증 유지**, 최종 clean regression만
- `V58 lifecycle`: Migration 직접 변경 없음. **기존 검증 유지**, V59/V60 focused lifecycle과 최종 upgrade chain에서 재확인
