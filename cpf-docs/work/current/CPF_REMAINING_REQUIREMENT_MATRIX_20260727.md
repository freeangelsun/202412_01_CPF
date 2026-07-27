# CPF 통합 Requirement Closure Matrix — 2026-07-27

## 1. 기준

- 작업 시작 SHA: `00780dc14ef621578f6f7ca61ef1d0c9973c60e6` (`20260727_04`)
- 현재 산출물: `CPF_20260727_05_ROOT_PATCH` — 미Commit
- 최상위 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA: `CPF_NEXT_QA_REQUIREMENTS_CHATGPT_FIRST_CODEX_REVIEW_20260727_05.md`

이 Matrix는 **구현 상태**와 **실행 검증 상태**를 분리한다.
선택한 Change Set의 구현을 `부분 구현`으로 종료하지 않는다. 외부 환경이 없어 실행하지 못한 항목만 `미검증`으로 표시한다.

## 2. Change Set B — 구현 Closure

| Requirement | 구현 상태 | 결과 |
|---|---|---|
| B-001 ADM 생성 Transaction 원자성 | 완료 | Identity/Profile/Role을 `admTransactionManager` 경계에서 처리. `operationId` unique 멱등 생성 |
| B-002 Product DB Fail-closed | 완료 | DATABASE 기본/제품 필수. DB 오류를 Memory 성공으로 전환하지 않음. MEMORY는 명시 Demo Profile 전용 |
| B-003 ADM Identity/Profile 분리 | 완료 | 인증 Identity와 Display/연락처 Profile 분리 유지, Profile Projection |
| B-004 BZA 직원/조직 | 완료 | 기존 Directory의 hierarchy/assignment/position/job-title/responsibility/history 기능을 보호하고 안전상태 계약 연결 |
| B-005 상태 분리 | 완료 | `BzaEmploymentStatus` / `BzaAdminAccountStatus` 별도 Catalog |
| B-006 안전한 Default | 완료 | 직원 EMPLOYED, 관리자 PENDING_ACTIVATION, 신규 관리자 Role 자동 부여 없음, 연락처 optional/NULL |
| B-007 연락처 PII | 완료 | 문자열/정규화/Masking/Raw 권한/사유/Audit/Redaction |
| B-008 Masked/Raw API | 완료 | 기본 Masked, Raw는 별도 POST body reason + 권한 + Audit + `Cache-Control: no-store` |
| B-009 BZA inline SQL 제거 | 완료 | BZA Java inline SQL 0, Vendor Query Resource와 Query ID로 이관 |
| B-010 Core Internal Boundary | 완료 | ADM/BZA `com.cpf.core.common.*` 직접 import 0, Public DB Catalog 사용 |
| B-011 V59 Contact Lifecycle 연계 | 완료 | V59 계약을 유지하고 V61 상태/PII 계약과 Fresh/Upgrade 정본 연결 |
| B-012 V60 Safe Default 연계 | 완료 | V60 EMPLOYED default를 유지하고 V61에서 legacy ACTIVE를 명시 Migration으로 정규화 |
| B-013 ADM/BZA UI | 완료 | Masked 목록, Raw 조회, 상태 Catalog, expectedVersion, 명시 clear, PENDING 관리자 UX 연결 |
| B-014 구현 완료조건 | 완료 | Source/API/DB/Migration/Rollback/Frontend/Test/Gate/Guide/Evidence 경계까지 반영 |

## 3. Change Set B — 실행 검증

| 검증 | 상태 | 완료 조건 |
|---|---|---|
| 저비용 Static Contract | 완료 | Java parser/중복 선언, TS/Vue syntax, SQL ownership, V61 hash/parity, PII URL leak, internal import 검사 |
| Java 25 full Gradle | 미검증 | `clean test assemble` 및 영향 Module test 0 failure |
| PowerShell Gate | 미검증 | `check-admin-data-safety.ps1` PASS |
| MariaDB V59→V60→V61 Upgrade | 미검증 | schema/data/runtime probe PASS |
| V61 Rollback/Reapply | 미검증 | safe rollback 후 reapply, fake Role/data loss 없음 |
| Fresh Install | 미검증 | Latest schema == migration final contract |
| ADM/BZA Runtime DB Fault | 미검증 | Product fail-closed, partial row 0, readiness DOWN |
| Browser E2E | 미검증 | Masked/Raw/Reason/Permission/Conflict/Status 흐름 PASS |

`미검증`은 구현을 미완료로 되돌리는 의미가 아니다. Commercial Release 판정만 차단한다.

## 4. A-V Stack/Artifact

A-V Source 안전장치는 `20260727_04`에서 구현 완료. 다음 실제 검증은 Java25/Gradle9.1, staging/promotion rollback, Local/Offline standalone, bootJar/bootWar hash, Remote Registry이다. 실행하지 않은 항목은 미검증으로 유지한다.

## 5. 후속 Change Set 계약

아래는 아직 이번 작업의 상태 판정 대상이 아닌 **다음 폐쇄 개발 단위**다. 착수하면 구현 가능한 항목을 남기지 않고 완료한 뒤 다음 단계로 이동한다.

1. `S` — Spring Boot 4.x 공식 지원 Stack 완전 Migration
2. `C` — Generated Domain Golden/MBR/ACC/Installer/SystemCode 추론 제거/2 Domain lifecycle
3. `D` — BAT Legacy disposition, Runtime/EDU parity, JobPack, Multi-instance
4. `E` — Gateway target-down/timeout/retry/UNKNOWN_RESULT/O-S-B/Header trust/2 Gateway
5. `U` — ADM/BZA commercial UX, Browser, approval/risky action/observability
6. `DB` — 5 Vendor, Historical Migration, runtime SQL, Backup/Restore/DR
7. `T` — Gate/Tool Inventory/삭제, QUICK/VERIFY/FULL, CI, SBOM/License/CVE/Signature/Provenance
8. 최종 Full Regression — Browser/Multi-instance/Fault/Historical/Release Evidence

## 6. 완료 왜곡 금지

- 외부 실행 미검증을 PASS로 쓰지 않는다.
- 미래 Change Set을 현재 완료로 선기록하지 않는다.
- 과거 Evidence를 현재 SHA PASS로 승계하지 않는다.
- 구현된 Change Set을 문서 관성 때문에 다시 `부분 구현`으로 낮추지 않는다.
