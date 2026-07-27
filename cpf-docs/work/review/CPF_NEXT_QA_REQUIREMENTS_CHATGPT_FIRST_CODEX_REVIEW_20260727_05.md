CPF 다음 통합 QA 요구사항

ChatGPT 1차 개발 → Codex 2차 독립 검수·보완 개발

문서 목적최신 CPF master, Codex 작업 이력, 개발 관리/QA 인수인계, 사용자 추가 결정, ChatGPT 후속 구현을 종합하여 다음 개발과 검수 순서를 고정한다.

1차 구현 주체는 다른 ChatGPT 개발 모델이다.Codex는 1차 구현 완료 후 최신 Git에서 독립 검수하고, 결함을 직접 수정·보완 개발한다.

Repository: freeangelsun/202412_01_CPFBranch: master이 문서 작성 시 최신 원격 SHA: 00780dc14ef621578f6f7ca61ef1d0c9973c60e6 (20260727_04)최상위 정본: cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md

1. 실행 주체와 책임

1.1 ChatGPT 1차 개발

다음 ChatGPT 개발 모델은 요구사항 목록만 작성하지 않는다.

최신 master를 다시 확인한다.

Source, SQL, Migration, Generator, Frontend, Script, Config, Test와 문서를 대조한다.

Codex 중단 지점과 기존 ChatGPT 부분 구현을 이어서 실제 개발한다.

변경 전 영향도 분석을 작성한다.

올바른 Owner에서 Source를 수정한다.

Generator 대상이면 생성 산출물이 아니라 Metadata/Template/Tool을 먼저 수정한다.

DB 변경이면 Canonical부터 Migration/Rollback/Seed/Verify까지 함께 변경한다.

Backend 변경이면 API, Consumer, Frontend, Permission과 Test를 함께 수정한다.

실행 가능한 검증은 직접 실행한다.

작업한 Change Set은 부분 구현, 미구현, 재확인 필요 상태로 종료하지 않는다.

외부 환경이 없어 실행 불가능한 항목만 미검증으로 남기고 환경조건·명령·예상 결과를 기록한다.

Codex가 검수할 수 있는 영향도·구현·Evidence·Checklist 문서를 갱신한다.

사용자 승인 없이 Commit, Push, Branch, Tag와 Release를 만들지 않는다.

1.2 Codex 2차 검수·보완

Codex는 ChatGPT 완료 보고를 완료 근거로 사용하지 않는다.

최신 SHA와 ChatGPT 작업 시작/종료 SHA를 확인한다.

Git Diff와 영향도 Ledger를 대조한다.

실제 Consumer와 간접 영향 범위를 다시 찾는다.

Build, Test, DB, Runtime, Browser, Multi-instance와 Fault 검증을 독립 실행한다.

누락·결함·회귀를 보고만 하지 않고 직접 수정·보완한다.

수정 후 동일 검증을 다시 실행한다.

실행하지 않은 항목을 성공으로 판정하지 않는다.

최신 Evidence와 Handover를 갱신한다.

사용자 승인 범위에서만 Commit/Push를 수행한다.

1.3 최종 완료 흐름

ChatGPT 1차 개발
→ ChatGPT 자체 검증
→ 영향도·Implementation Report·Codex Checklist 작성
→ Codex 독립 검수
→ Codex 보완 개발
→ 동일 검증 재실행
→ 최신 SHA Evidence
→ QA 최종 판정

2. 상태 표준

이 문서와 후속 Review에서는 다음 상태만 사용한다.

완료

부분 구현

미구현

미검증

실패

재확인 필요

완료는 Source, Owner, Consumer, Test, Runtime/DB/UI/Evidence가 필요한 범위에서 모두 확인됐을 때만 사용한다.

다음은 단독 완료 근거가 아니다.

Class 또는 파일 존재

정적 검색 0건

Swagger 노출

일부 Unit Test

화면 Mock

DB Table만 존재

Generator가 파일만 생성

과거 Commit Evidence

ChatGPT/Codex 보고

단일 Instance 정상 동작

정상 흐름만 있는 EDU

문서의 완료 표기

3. 모든 변경에 적용하는 공통 필수 지침

3.1 변경 전 영향도 분석

각 Change ID마다 다음을 기록한다.

구분

필수 내용

Requirement

연결된 정본 Requirement ID

기준 SHA

작업 시작 Commit

현재 결함

재현 방법과 실제 오류

목표

변경 후 계약과 운영 동작

Owner

책임 Module/DB/Tool

직접 변경

예상 Source 파일

간접 영향

Consumer와 호출자

API

Public/SPI/Internal, Local/Remote

DB

Schema/Table/Column/Index/FK/Seed

Migration

Upgrade/Rollback/Reapply/Fresh

Generator

Metadata/Template/산출물 영향

Frontend

Route/Menu/API/Permission/UI

Config

Key/Default/Profile/Secret

Runtime

Instance/Cache/Lock/Lease/Retry

보안

Auth/Permission/PII/Masking/Audit

호환성

기존 데이터와 Public Contract

복구

실패·Rollback·Forward Recovery

검증

실행할 명령과 시나리오

Evidence

저장 경로

3.2 누적 Ledger

정본 경로:

cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md

작업마다 초기화하지 않는다. ChatGPT는 작업 전 예상 영향도, 작업 후 실제 Diff를 갱신한다. Codex는 각 Change ID에 확인, 보완, 반려 중 하나를 기록한다.

3.3 Codex 검수 문서

ChatGPT는 종료 전에 다음을 생성하거나 갱신한다.

cpf-docs/work/current/...
cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md
cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md
cpf-docs/work/review/CPF_CHATGPT_*_IMPLEMENTATION_REPORT*.md
cpf-docs/work/review/CPF_CODEX_*_REVIEW_CHECKLIST*.md
cpf-docs/work/review/CPF_UNVERIFIED_SCENARIO_LIST*.md
cpf-docs/evidence/...EVIDENCE_INDEX...
cpf-docs/work/handover/...

Codex Checklist에는 반드시 다음을 포함한다.

이 구현은 ChatGPT의 1차 개발 결과다. 완료 보고를 신뢰하지 말고 최신 Git Diff, 영향도 Ledger, 실제 Consumer, DB, Generator, Frontend, Runtime과 Evidence를 독립 검증한다. 결함과 누락은 보고만 하지 말고 올바른 Owner에서 수정·보완한 뒤 동일 검증을 재실행한다.

3.4 완료 보고 필수사항

기준 SHA

종료 SHA 또는 Working Tree 상태

실제 변경 파일

Requirement별 결과

예상 영향도와 실제 Diff 차이

실행 명령과 환경

Test 수와 결과

DB/Runtime/Browser/Multi-instance/Fault 결과

실패 이력과 수정 내용

미검증 사유

잔여 위험

Evidence 경로

Continuity/Handover 경로

4. 최신 프로젝트 상황

4.1 최신 Commit 반영 내용

00780dc...는 다음을 추가·보강했다.

Stack 지원 상태 정본

Version Single Source

LOCAL_DEV / REMOTE / OFFLINE Artifact 공급 모드

Aggregate Quality Build

격리 Staging

POM/BOM/Plugin Marker/Hash 검증

Manifest Barrier와 Local Promotion/Rollback

Offline Artifact Bundle

Batch Scheduler/JobInstance Lifecycle Guide

Current/Matrix/Ledger/Handover/Checklist 문서 갱신

4.2 현재 QA 판정

영역

현재 상태

다음 조치

최신 Push

완료

00780dc... 확인

Change Set A Source

부분 구현

실행 검증 필요

Java25/Gradle9 전체 Build

미검증

최신 변경 후 실행

Local Staging/Promotion

미검증

실제 Publisher/Consumer/Rollback

Remote Registry

미검증

Nexus/Artifactory

Offline Bundle 소비

미검증

독립 Domain Build

bootJar/bootWar 내부 Hash

미검증

Archive 검사

Spring Boot 지원 Stack

부분 구현

TRANSITION 해소 필요

ADM/BZA Data Safety

부분 구현

원자성·PII·DB Lifecycle

Generated Domain Golden

부분 구현

MBR/ACC parity와 Include

BAT Legacy Migration

부분 구현

기능·EDU parity

Gateway Resilience

부분 구현

Fault/Multi-instance

ADM/BZA Browser 운영성

미검증

E2E

전체 Migration Chain

미검증

Historical lifecycle

최종 Release

부분 구현

CI/SBOM/License/CVE/Signature

4.3 Codex 중단 지점

Codex는 다음을 진행하던 중 사용량 제한으로 중단됐다.

MBR/ACC Generator Golden 불일치 확인

ADM MBR 고정 결합 제거

REF Generated Domain 의존 제거

Installer의 member/account, mbrDB/accDB 고정 코드 확인

Core의 Package/Path/Port 기반 SystemCode 추론 확인

Platform → Generated Domain 의존 0 방향 수정

BAT Legacy src 삭제와 REF EDU 이관 검토

Build Tooling의 cpf-tools/build 이전

마지막 Core/Installer 범용화 수정과 검증 진행 중 종료

다음 ChatGPT는 마지막 변경이 완성됐다고 가정하지 않는다.

5. 전체 실행 우선순위

0. 최신 Baseline과 Change Set A 실행 검증
1. ADM/BZA Data Safety 완전 종료
2. 지원 가능한 최종 Stack Migration 완전 종료
3. Generated Domain Golden 완전 종료
4. BAT Runtime/EDU/Job Pack 완전 종료
5. Gateway Resilience/Operations 완전 종료
6. ADM/BZA 전체 상품화 UX 완전 종료
7. DB 5 Vendor/Migration/Runtime 완전 종료
8. Gate/Tool/CI/Release/Productization 완전 종료
9. 최종 Full Regression, Browser, Multi-instance, Fault
10. Codex 독립 검수·보완

각 단계는 선택한 Change Set을 완료로 닫은 뒤 다음으로 넘어간다. 외부 인프라가 없어 실행 불가능하면 해당 항목만 미검증으로 남기고 상용 Release Gate는 실패 상태를 유지한다.

6. CHANGE SET A-V — Stack·Artifact 공급 실행 검증

A-V-001 최신 Java/Gradle Configuration

다음이 Stack 정본과 일치해야 한다.

Wrapper

Root Build

모든 Module

Included BOM

Convention Plugin

Generator

Exported Standalone Repository

BAT Standalone Build

검증: Java 25, Gradle 9.1.x, configuration, projects, compile, test, assemble, bootJar, bootWar.

A-V-002 Aggregate Quality Build

aggregateQualityBuild는 Compile, Unit/Contract Test, Static Gate, 필요한 Frontend Gate와 Included Build 검증을 Publication 전에 수행한다. 실패 시 Shared Repository 변경 0.

A-V-003 Local Staging/Promotion

검증 Build
→ 격리 Staging
→ POM/Module Metadata/BOM/Plugin Marker/Hash
→ Source Commit/Fingerprint
→ Manifest
→ Lock
→ Promotion
→ PROMOTED

실패 시나리오:

Artifact 누락

BOM/Marker/Hash 불일치

예상 밖 Artifact

Dirty Fingerprint

Promotion 중 실패

동시 Publisher

기존 정상 Version과 Manifest가 보존되어야 한다.

A-V-004 Mixed Artifact 차단

같은 Version의 Core/Common/BOM/Plugin/BAT Artifact가 서로 다른 Source SHA 또는 세대로 혼합되지 않아야 한다. Version Directory 전체를 하나의 Manifest로 소비한다.

A-V-005 LOCAL_DEV

같은 Repo Domain: Project Dependency

독립 Domain: PROMOTED Local Repository

current HEAD와 Manifest 일치 시 재사용

불일치 시 검증 Publish

Auto-sync 기본 off

수동 JAR 복사 금지

A-V-006 REMOTE

승인 Nexus/Artifactory

고정 Version

Local fallback 금지

URL/인증 누락 fail-closed

com.cpf.* Content Filter

Credential Redaction

일반 publish의 Local Side Effect 0

A-V-007 OFFLINE

Bundle에 Artifact Set, BOM, Convention Plugin/Marker, Version, Source SHA, Hash Manifest, Promotion State와 Guide를 포함한다. 인터넷/Local fallback 없이 Generated Domain을 빌드한다.

A-V-008 bootJar/bootWar

Archive에서 Core/Common/Public Contract/BAT Contract, 정확한 Version/Hash, 중복 Version 0, 누락 0을 확인한다.

A-V-009 Security

Dependency Confusion, Checksum, Provenance, Signature 구조, Snapshot/Release 분리, Release 재게시 금지를 검증한다.

A-V-010 완료조건

전체 Build

Included Build

Local Promotion/Rollback

실패 Build no-publish

독립 Domain Local/Offline 소비

bootJar/bootWar 검사

최신 Evidence

Remote Registry 환경이 없으면 해당 항목만 미검증으로 남기고 CI/Release를 차단한다.

7. CHANGE SET B — ADM/BZA Data Safety

다음 ChatGPT의 즉시 개발 대상이며 부분 구현으로 종료하지 않는다.

B-001 ADM 생성 Transaction 원자성

운영자 Identity, Profile, Role Mapping, Permission Projection과 Audit를 하나의 Transaction 또는 명시적 Saga/Compensation으로 처리한다.

검증:

Profile/Role/Audit 실패

Duplicate

Optimistic Conflict

재시도

동일 Idempotency Key

Partial Row 0

B-002 Product DB Fail-closed

Product Runtime은 DB 오류를 Memory Repository로 성공처럼 대체하지 않는다.

Test/EDU Profile만 In-memory 허용

필수 DB 누락 시 기동 실패

연결 실패 시 Readiness DOWN

Write 실패 표준 오류

Product Memory fallback 0

B-003 ADM Identity/Profile 분리

Identity:

Login ID

Credential Metadata

Account Status

MFA/Session

Authentication Subject

Profile/Directory:

이름, 사번, 조직, 직급, 직책

휴대폰, 내부전화

외부 Directory Subject

유효기간

Profile 장애가 인증 결과를 임의 변경하지 않는다.

B-004 BZA 직원/조직

조직 계층/유형/유효기간, 직원/사번/재직/직급/직책, 주 소속/겸직/파견/직무대행, Assignment 유효기간, 조직 책임자와 과거 Snapshot을 지원한다.

B-005 상태 분리

직원 재직 상태와 관리자 계정 상태를 분리한다. useYn 하나로 표현하지 않는다.

B-006 안전한 Default

신규 직원:

재직 EMPLOYED

연락처/이메일/직급/직책 NULL

부서 미배정 관계 또는 NULL

계정 자동 생성 아니오

Role 자동 부여 아니오

신규 관리자:

PENDING_ACTIVATION

로그인 불가

Role 미부여

비밀번호 변경 필요

MFA 정책 적용

빈 문자열, 0, 없음, 미입력, N/A, 문자열 "null" 저장 금지.

B-007 연락처 PII

휴대폰/내부전화 분리, 문자열 저장, 국가번호/Extension, 정규화/표시/원문 구분, Masking, Raw 권한/사유/Audit, Download 권한, Retention, Log/Trace/Evidence Redaction을 구현한다.

B-008 Masked/Raw API

기본 API는 Masked. Raw 조회는 별도 Permission, Reason, transactionId, 대상과 Audit를 요구한다.

B-009 BZA inline SQL 제거

Java Vendor SQL 금지. Query ID, Owner, Parameter/Result Contract, Vendor Resource, Consumer, Test를 갖춘다. 미사용 Resource 제거.

B-010 Core Internal Boundary

ADM/BZA/Generated Domain의 Core Internal 직접 참조 0. 필요한 기능은 Public API/SPI 또는 Owner Adapter로 이동한다.

B-011 V59 Contact Lifecycle

V58 DB
→ V59 Upgrade
→ ADM/BZA Insert/Read
→ Rollback
→ Reapply
→ Fresh Install
→ Manifest/Checksum/Verify

Profile Ownership, Identity 연락처 0, 휴대폰/내부전화, NULL, Index/Length/Charset, Evidence Redaction을 확인한다.

B-012 V60 Safe Default Lifecycle

기존 DB
→ V60 Upgrade
→ 미입력 직원 생성
→ EMPLOYED
→ 기존 Row 불변
→ Rollback
→ Reapply
→ Fresh Install

B-013 ADM/BZA UI

목록/상세/등록/수정/상태/Assignment/Role/Permission/이력/Audit/Server Paging/Filter/Sort/Conflict/Partial Failure/권한 부족/Retry를 실제 API와 연결한다.

B-014 완료조건

Backend Test

Frontend test/typecheck/lint/build

V59/V60 lifecycle

Product DB fail-closed Runtime

생성 Transaction Fault

PII Masking/Audit

Browser 핵심 흐름

최신 Evidence

8. CHANGE SET S — 최종 지원 Stack Migration

TRANSITION을 영구 상태로 두지 않는다. 실제 이관 시점에 공식 System Requirements를 다시 확인해 지원 가능한 Spring Boot 4.x Target을 확정한다.

S-001 Compatibility

Java 25, Gradle 9, Spring Framework/Security/Batch, MyBatis, Flyway, Actuator, Testcontainers, Byte Buddy, springdoc, Servlet/Tomcat/외부 WAS, Jakarta, Logging/Tracing을 검증한다.

S-002 Version Single Source

gradle/cpf-stack.properties와 Root/Module/Included Build/Generator/Exported Repository/Docs Version literal parity.

S-003 Source Migration

Removed/Deprecated API, Security Filter, Actuator Health, Batch Auto Config, MyBatis/Flyway, Error, Test Annotation, Plugin API와 Gradle Warning을 수정한다.

S-004 Topology

Embedded JAR, bootJar, bootWar, Exploded WAR, 외부 WAS, Modular Monolith, 독립 Service, BAT 5 Runtime, Generated Domain.

S-005 DB

MariaDB Fresh/Migration/Flyway/MyBatis/Transaction/Pool/Multi Datasource/Health.

S-006 Frontend/API

CORS, Cookie/Session, Security Header, OpenAPI, Error Body, Download/Upload 회귀.

S-007 Release

모든 완료조건과 최신 Evidence 후에만 stackState=SUPPORTED_GA. Build 성공만으로 변경 금지.

9. CHANGE SET C — Generated Domain Golden

C-001 Platform 고정 Domain 의존 0

Core/Common/ADM/BZA/Gateway/BAT/REF/Installer/Harness/Frontend/Script에서 MBR/ACC/PAY/INS 하드코딩을 제거한다. Profile 데이터, Domain Manifest, Registry와 Test Fixture만 허용한다.

C-002 Path/Port/Class 추론 금지

/mbr, /acc, 특정 Port, Package/Class substring, Schema switch 금지.

표준 Header
→ Registry
→ Versioned Manifest
→ Route Metadata
→ 오류

C-003 Installer 동적 발견

신규 Domain 추가 시 중앙 Installer Source 수정 0. Manifest는 DomainName/SystemCode/Schema/Prefix/Account/Vendor/Migration/Seed/Verify/Dependency/Order를 가진다.

C-004 Root Include 정책

MBR은 Golden Reference. ACC 고정 Include 유지 여부를 Decision으로 확정한다. 두 개 유지 시 normalized parity.

C-005 Normalized Tree/Hash

Metadata 차이를 제외한 File/Layer/API/DTO/Service/Repository/SQL/Migration/Test/OpenAPI/JavaDoc/README/Build/Runtime 동일성.

C-006 Minimal Sample

Create/Read/List/Update/Status or Delete/Search/Sort/Offset/Cursor/Validation/Duplicate/Optimistic Lock/Commit-Rollback/Header/transactionId/Error/OpenAPI/Test.

C-007 Artifact 3모드

같은 Domain을 LOCAL_DEV/REMOTE/OFFLINE에서 동일 Contract/Dependency Set으로 검증한다.

C-008 임시 Domain 2개

Create/Export/Independent Build/bootJar/bootWar/5 Vendor/MariaDB CRUD/Local-Remote/Remove/Regenerate Hash. 임시 흔적 0.

C-009 삭제 안정성

Generated Domain 하나 제거 후 Platform Build/Boot/Frontend/DB Install 성공.

C-010 ADM/REF 중립성

ADM은 Registry/Capability만 사용. REF는 특정 Generated Domain 없이 Boot/Test하고 Self Simulator EDU를 사용한다.

10. CHANGE SET D — BAT Runtime·EDU·Job Pack

D-001 Legacy Inventory

삭제된 cpf-batch/src/**를 Standalone Runtime, Contract, Testkit, REF EDU, Generator Template, 업무 Job Pack, Dead Code, 미대체 Gap으로 파일별 분류한다.

D-002 Runtime parity

Job/Schedule/Calendar/Misfire/Execution/Retry/Restart/Checkpoint/Lock/Lease/Fencing/Worker/Heartbeat/Ghost/Center-Cut/Health/Operation Query/Log/Retention/On-demand/Failure/Smoke를 대조한다.

D-003 EDU parity

REF에 Archive/Compression, Center-Cut, Chunk, Idempotency, Duplicate, Logging, On-demand, Reconciliation, UNKNOWN_RESULT, Restart, Checkpoint, Retry, Service Call, Parameter Validation, Tasklet, Transaction/Partial Rollback, Failure/Heartbeat 예제를 제공한다.

D-004 EDU 위치

cpf-reference/.../reference/batch 경계. Runtime 혼재, com.cpf.batch.edu 복사, 운영 자동 활성화, MBR 전용 의존 금지.

D-005 EDU 완결성

입력 DTO, Default, Validation, 정상/오류/Retry/Recovery, 상태/결과, Test, Guide, OpenAPI/Catalog, 안전한 Log.

D-006 Job Pack Generator

Tasklet/Chunk/Parameter/Idempotency/Retry/Restart/Transaction/Owner Public Contract Call 선택형 생성.

D-007 Scheduler Lifecycle

bat_schedule이 업무 실행 시간 정본이고 dispatch-ms는 Polling. DST/Holiday/Misfire/Window/Cron 변경/2 Scheduler Takeover/Restart-Rerun/대량 Schedule을 검증한다.

D-008 Standalone Runtime

Control Server/Scheduler/Worker/Center-Cut Runner/Host Agent 독립 Build/Boot. Contract/Runtime Common/Testkit Build.

D-009 Multi-instance

Scheduler 2, Worker 2, Runner 2로 Leader/Lease/Fencing/Duplicate/Drain/Takeover/Rejoin/Unknown/Reconciliation.

D-010 ADM BAT Control Plane

ADM은 BAT DB 직접 수정 없이 Owner API로 Topology/Instance/Job/Execution/Deploy/Recovery/Lease/Alert/Audit를 관리한다.

11. CHANGE SET E — Gateway Resilience

우선순위:

1. target-down failover
2. timeout/retry/결과불명
3. O/S/B와 외부→S 차단
4. Header trust/transactionId/trace
5. 다중 Gateway Route 정합성
6. 운영 조회·Health·Route 상태

E-001 Target Down

Connection Refused/DNS/Unhealthy/Readiness/Reset 시 다른 정상 Instance로 Failover.

E-002 Outlier

반복 선택 방지, Ejection, Recovery Probe, Flapping 방지, 재편입.

E-003 All Down

Service/Route Version/Attempt/Error Class/transactionId/Retry 가능 여부를 표준 오류로 반환하고 내부 주소를 노출하지 않는다.

E-004 실제 Failover

A/B 정상 → A 종료 → B Failover → A 복귀 → 안정화 후 재편입.

E-005 Timeout

Connect/TLS/Header/Read/Write/Overall 분리.

E-006 Retry

Method/업무 의미, 비멱등 금지, Idempotency, Budget/Backoff/Jitter/Attempt/Deadline/Storm 방지.

E-007 UNKNOWN_RESULT

UNKNOWN_RESULT, RECONCILING, CONFIRMED_SUCCESS, CONFIRMED_FAILURE, MANUAL_REVIEW. Gateway가 결과를 임의 확정하지 않고 Owner Query/Reconciliation/Compensation을 사용한다.

E-008 O/S/B

외부→S 차단을 Route/Discovery/Rewrite/Alias/Header/Direct/Fallback에서 강제한다.

E-009 우회 Test

Header 위조, Encoded Path, Alias/Rewrite, Forwarded, 직접 Port, Host, 중복 Header.

E-010 Header Trust

외부 신뢰 Header를 Allowlist에 따라 제거/재생성한다.

E-011 transactionId/Trace

유효 외부 transactionId 승계, 34자리, Retry/Failover 동일 ID, Attempt/Segment 분리, W3C Trace/Baggage/PII. 신규 transactionGlobalId 금지.

E-012 Forwarded

Trusted Proxy, Client IP, 중복/위조, Forwarded와 X-Forwarded-* 충돌.

E-013 다중 Gateway

Gateway 2개, 동일 Route Version, Atomic Apply, expectedVersion, Drift, Rejoin, 신규 Instance Sync, Restart Recovery.

E-014 ADM 운영

Instance/Health/Route/Target/Outlier/Circuit/Timeout/Retry/Failover/Unknown/Drift/Volume/Error/Latency/429/503/변경자/사유/승인/Audit.

E-015 Fault Evidence

Target down/all down/connect/read timeout/비멱등 응답 유실/외부→S/Header 위조/2 Gateway drift/Route 변경 중 요청/Restart 복원.

12. CHANGE SET U — ADM/BZA 상품화 UX

UI는 Framework의 얼굴이다.

U-001 ADM

Service/Instance/Gateway/Transaction/Timeline/Async/Unknown/Batch/Center-Cut/Agent/Deploy/Log/Trace/Metric/Alert/Config/Secret Metadata/Approval/Audit/Recovery/Compensation.

U-002 BZA

조직/직원/Role/Permission/Approval/업무대상 Capability/Download/Audit/선택 Sample.

U-003 화면 공통

목록/상세/등록/수정/상태/이력/연결/Server Paging/Filter/Sort/Loading/Empty/Partial Failure/Conflict/권한 부족/Retry/갱신 시각.

U-004 위험조치

Snapshot/Reason/영향/Approval/expectedVersion/Idempotency/결과/Unknown/Audit.

U-005 Permission Manifest

Backend API와 Frontend Permission을 단일 정본 Projection 또는 자동 Parity Gate로 관리한다.

U-006 접근성

Keyboard/Focus/Label/Error/Table Header/Screen Reader/Contrast/Reduced Motion.

U-007 Browser E2E

로그인/Session/권한/Menu/Paging/CRUD/상태/Approval/Audit/Batch/Gateway/Error/Accessibility.

13. CHANGE SET DB — DB·SQL·Migration

DB-001 Canonical First

Canonical Metadata
→ Generator/Template
→ Vendor Source
→ Migration
→ Rollback
→ Seed
→ Verify
→ Runtime Consumer
→ Evidence

DB-002 Query Ownership

Query ID/Owner/Consumer/Parameter/Result/Vendor Resource/Test/사용 여부. Consumer 0 Resource 제거.

DB-003 5 Vendor

Dialect, Paging, Lock, Upsert, Sequence/Identity, Timestamp, JSON, DDL, Error, Migration, Runtime Mapping을 실제 지원한다. 복사/치환만으로 완료 금지.

DB-004 Historical Migration

Fresh Baseline → 모든 Migration → Checksum → Rollback 가능 구간 → Reapply → Drift → Runtime Query.

DB-005 Multi Datasource

최소권한 Account, Prefix 충돌 0, Read/Write, Transaction, Failover, Replica Consistency, Health.

DB-006 Runtime SQL

PREPARE/Parameter/Table/Permission/Mapping/Write/Rollback/Lock/Contention. BAT 158은 직접 영향 없으면 매 변경마다 반복하지 않지만 최종 회귀에서 확인한다.

DB-007 Backup/Restore/DR

Backup/Restore/Encryption/Retention/Restore Drill/RPO/RTO/Evidence.

14. CHANGE SET T — Gate·Tool·CI·Release

T-001 Inventory

모든 PowerShell/Gradle Gate의 Owner/Caller/Requirement/Input/Output/Side Effect/비용/환경.

T-002 분류

DEV_ONLY, CI_RELEASE, PRODUCT_ADMIN_TOOL.

T-003 대표 Entry

QUICK, VERIFY, FULL.

T-004 중복/Legacy 제거

Caller 0, 대체 Gate, Requirement Coverage, Docs/CI 참조, WIP 보호 후 삭제. 무근거 대량 삭제 금지.

T-005 Manual

옵션/Default/환경변수/입출력/Side Effect/실패/복구/예제/Secret 처리. Script Help와 Guide Parity.

T-006 Runtime Package

DEV/CI Script 제외, 필요한 Product Admin Tool만 포함.

T-007 CI Required Checks

Workflow/Branch Protection/CODEOWNERS/Review/Build/Test/Security/Artifact/Migration/Evidence/Release Gate.

T-008 Productization

SBOM/License/CVE/Signature/Provenance/Immutability/Compatibility/Upgrade/Rollback/Release Notes/Support Matrix.

15. 코드·메시지·파라미터·Default

DEF-001 예제값

pageSize/timeout/retry/chunk/concurrency/businessDate/locale/timezone/fileName/encoding/transactionId/idempotencyKey 예시.

DEF-002 안전한 Default

유한 Timeout, 제한 Retry, Queue/File/Download/Concurrency 제한, Agent Allowlist, 위험조치 승인, 관리자 자동 활성화 금지, Secret 원문 표시 금지.

DEF-003 선택값

미입력은 NULL 또는 명시 상태. 가짜 문자열 저장 금지.

DEF-004 Message Catalog

Validation/Not Found/Conflict/Optimistic/Duplicate/Auth/Forbidden/Timeout/Retry Exhausted/Circuit/Unknown/Reconciliation/Partial/Approval/Route/Target/DB/File/Checksum/Lease/Fencing. Placeholder parity Gate.

DEF-005 상태 Code

계정/직원/업무대상/Approval/Execution/Deployment/Recovery 상태 분리.

DEF-006 Config Metadata

Type/Default/Required/Range/Profile/Secret/Dynamic/Restart/Risk/Example/Deprecation/Owner.

16. 최종 통합 검증

16.1 Clean Build

clean
test
assemble
bootJar
bootWar
Frontend test
typecheck
lint
build

메모리 제약 시 직렬 실행과 --max-workers=1을 사용하되 범위를 축소하지 않는다.

16.2 Runtime

ADM, BZA, Gateway 2, REF, Generated Domain 2, BAT Control, Scheduler 2, Worker 2, Runner 2, Host Agent/Test Double.

16.3 Fault

Target/DB/Broker Down, Timeout, Slow, Reset, Partial Commit, Response Loss, Lease Loss, Checksum, Agent Failure.

16.4 Browser

ADM/BZA 핵심 운영 흐름.

16.5 Evidence

최신 SHA, 환경, 명령, 결과, 시간, Secret Redaction, 실패 이력.

17. ChatGPT 1차 개발 산출물

Current Development Request

Remaining Requirement Matrix

Change Impact and Validation Ledger

Continuity State

ChatGPT Implementation Report

Codex Review Checklist

Unverified Scenario List

Evidence Index

Handover

Codex Checklist는 1차 개발 종료 시점의 최신 Diff로 재작성한다.

18. Codex 독립 검수 순서

최신 master/Branch/Working Tree/시작·종료 SHA/Diff/Ledger 확인

누락 Consumer, DB/Migration, Generator, Frontend, Config, Multi-instance, Security/PII, Rollback, Evidence 역검증

변경 Module, Included Build, Artifact, Generated Domain, DB Lifecycle, Runtime, Browser, Multi-instance/Fault, 최종 Full Regression 실행

결함 직접 보완 후 동일 검증 재실행

Review/Gap Matrix/Evidence/Continuity/Remaining Risk 갱신

금지:

Test 기대값만 변경

과거 구조 복원

생성 산출물만 수정

Mock으로 Runtime 숨김

@Primary로 Datasource 결함 숨김

Health 항상 UP

Retry로 UNKNOWN 성공 처리

기능 삭제로 Gap 숨김

19. 최종 완료조건

공식 지원 Stack

Commercial Release Gate

LOCAL/REMOTE/OFFLINE Artifact

실패 Build no-publish

Verified Artifact Set

ADM/BZA Data Safety

PII Masking/Audit

V59/V60 Lifecycle

Generated Domain Golden

Domain 삭제/재생성

BAT Runtime/EDU parity

BAT Multi-instance

Gateway Fault/Unknown/O-S-B

다중 Gateway Route

ADM/BZA Browser

전체 DB Migration

5 Vendor

Backup/Restore/DR

Gate/Tool

CI Required Checks

SBOM/License/CVE/Signature

Repository Hygiene

OpenAPI/JavaDoc/EDU

최신 Evidence

ChatGPT→Codex Handover

Codex 독립 검수·보완 완료

20. 완료 금지조건

TRANSITION Stack

Source만 구현하고 Runtime 미검증

Mixed Artifact 위험

REMOTE/OFFLINE Local fallback

Product DB Memory fallback

Partial DB Write

PII 원문 노출

V59/V60 미실행

Generated Domain 특별취급

Platform 고정 Domain 추론

BAT EDU 미대체

Gateway Unit Test만 존재

Browser/Multi-instance 미검증

UNKNOWN 단순 치환

위험조치 Approval/Audit 누락

가짜 Default 저장

Domain 제거 시 Platform 실패

과거 Evidence 재사용

Ledger/최신 Codex Checklist 누락

미실행 검증 PASS 표기

21. 다음 ChatGPT 시작 지침

이 작업의 1차 구현 주체는 ChatGPT 개발 모델이다.

최신 master, CPF_FINAL_TARGET_REQUIREMENTS.md,
CPF_NEXT_QA_REQUIREMENTS_CHATGPT_FIRST_CODEX_REVIEW_20260727_04.md를 먼저 읽는다.

최신 00780dc...의 Stack/Artifact Source는 실행 검증이 부족하므로
Change Set A-V를 먼저 닫고, 이어 Change Set B ADM/BZA Data Safety를
Source/API/DB/Migration/Rollback/Frontend/Test/Runtime/Evidence까지 완료한다.

작업한 Change Set을 부분 구현으로 종료하지 않는다.
외부 인프라 부재 항목만 미검증으로 남기고 Release Gate를 차단한다.

모든 변경 전 CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md에 영향도를 기록하고,
작업 후 실제 Diff와 Consumer에 맞게 갱신한다.

Generator 대상이면 Metadata/Template/Generator를 수정한다.
DB 대상이면 Canonical부터 Upgrade/Rollback/Reapply/Fresh까지 처리한다.
Frontend 기능은 Backend/API/Permission/Browser까지 연결한다.

종료 시 Implementation Report, Codex Review Checklist,
Continuity, Unverified Scenario List와 Evidence Index를 작성한다.

사용자 승인 없이 Commit, Push, Branch, Tag와 Release를 만들지 않는다.

22. 향후 Codex 시작 지침

이 작업은 ChatGPT 개발 모델의 1차 구현 결과에 대한 2차 독립 검수다.

ChatGPT 완료 보고를 신뢰하지 말고 최신 Git Diff,
CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md,
CPF_FINAL_TARGET_REQUIREMENTS.md,
최신 Codex Review Checklist와 실제 Runtime/Evidence를 대조한다.

Module, API, DB, Migration, Generator, Frontend, Consumer,
다중 인스턴스, 보안, PII, Audit, Artifact, Stack과 Rollback을 전수 확인한다.

발견한 결함은 보고만 하지 말고 올바른 Owner에서 수정·보완하고
동일 검증을 재실행한다.

실행하지 않은 검증은 PASS로 표시하지 않는다.
최종 Review, Evidence, Remaining Matrix와 Continuity를 갱신한다.

23. 참고 Repository 문서

cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md
cpf-docs/architecture/CPF_STACK_SUPPORT_AND_MIGRATION_DECISION.md
cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md
cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md
cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md
cpf-docs/work/current/CPF_NEXT_INTEGRATED_DEVELOPMENT_REQUEST_20260727.md
cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md
cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md
cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md
cpf-docs/work/review/CPF_CHATGPT_2ND_IMPLEMENTATION_REPORT_20260727.md
cpf-docs/work/review/CPF_CODEX_2ND_REVIEW_CHECKLIST_20260727.md
cpf-docs/work/review/CPF_UNVERIFIED_SCENARIO_LIST_20260727_04.md
cpf-docs/work/handover/CPF_CHATGPT_TO_CODEX_HANDOVER_20260727.md

24. 마지막 원칙

최신 Git이 과거 문서보다 우선한다.

정본 Requirement를 조용히 삭제하거나 축소하지 않는다.

기존 성공 기능은 보호하되 변경 영향권에 들어오면 재검증한다.

고비용 검증은 매 작은 변경마다 무조건 반복하지 않지만 최종 Commit에서는 전체 회귀를 수행한다.

Framework 관리·운영 기능은 ADM/BZA에서 실제 사용할 수 있어야 한다.

UI, API, DB, Runtime과 Evidence가 연결되지 않으면 완료가 아니다.

ChatGPT가 개발하고 Codex가 독립 검수·보완하는 이중 Gate를 모든 향후 QA 작업에 적용한다.