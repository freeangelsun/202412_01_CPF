# CPF 통합 잔여 Requirement Matrix — 2026-07-27

## 기준

- 기준 SHA: `9e4edaef24dce901fdcf722e2e6d8c0cf0a623ba` (`20260727_02`)
- 입력: `CPF_FINAL_TARGET_REQUIREMENTS.md`, QA 통합 요구, Codex Continuity, 최신 Git 재검토, 사용자 추가 요구
- 상태: `구현 확인`, `이번 패치 구현·미검증`, `부분 구현`, `미구현`, `통합검증 예정`, `재확인 필요`
- 원칙: 이전 실행 Evidence와 이후 변경 영향이 겹치지 않으면 무조건 재실행하지 않는다. 반대로 이후 ChatGPT/Codex 변경의 직접·간접 영향권에 들어온 과거 PASS는 `재검증 필요`로 다시 연다.

## A. Generated Domain / Platform 분리

| ID | 상태 | 최신 판정 / 남은 작업 | 다음 검증 |
|---|---|---|---|
| GEN-PLAT-001 | 부분 구현 | ADM MBR 결합은 제거 확인. `CpfSystemCodes.inferFromTypeName` 등 package/type 추론 잔존 여부를 실제 Consumer 기준으로 마감 필요 | source consumer search + core focused test |
| GEN-PLAT-002 | 구현 확인/재확인 | `CpfTargetServiceResolver` 고정 `/mbr`/port 추정은 제거 확인. 다른 path/port/class-name 추론 잔존 전수검색 필요 | 저비용 static gate |
| GEN-PLAT-003 | 구현 확인/재확인 | DB Installer가 Profile metadata 기반 Generated Domain 분류 사용. 새 Domain 추가 시 중앙 코드 변경 0인지 임시 2 Domain으로 확인 필요 | PAY/INS install path |
| GEN-PLAT-004 | 미완료 | Root `settings.gradle`에 MBR/ACC 고정 include. MBR 1개 Golden vs MBR/ACC 2 Reference 정책 확정 필요 | architecture decision + build |
| GEN-PLAT-005 | 미완료 | MBR/ACC가 현 Generator normalized tree/hash parity 아님 | generator-first migration |
| GEN-PLAT-006 | 부분 구현 | Generator sample/DB 기능 존재하나 현재 Golden Reference와 parity 미완료 | temp domain CRUD/runtime |
| GEN-PLAT-007 | 통합검증 예정 | 임시 2 Domain create/build/5-vendor render/MariaDB/remove/regenerate 전체 필요 | 첫 Domain 성공 후 두 번째만 수행 |
| GEN-PLAT-008 | 미검증 | Generated Domain 삭제 시 Platform 독립성 최종 Evidence 없음 | remove-one-domain focused build/boot |
| GEN-PLAT-009 | 부분 구현 | ADM MBR 전용 기능 제거. 범용 Capability 기반 업무대상 관리 완결성 부족 | ADM capability flow |
| GEN-PLAT-010 | 부분 구현 | REF MBR service-call 제거/Echo 전환 확인. Generated Domain dependency 0 Gate 필요 | REF focused test/boot |

## B. BAT Legacy Migration

| ID | 상태 | 최신 판정 / 남은 작업 | 다음 검증 |
|---|---|---|---|
| BAT-MIG-001 | 부분 구현 | Legacy migration map 존재, 삭제 146개 파일 전체 disposition 최종 대조 필요 | static inventory only |
| BAT-MIG-002 | 부분 구현 | Standalone Runtime 구현 대폭 보강. Legacy 기능 parity 최종 미확정 | feature-level targeted tests |
| BAT-MIG-003~005 | 부분 구현 | REF Batch EDU 일부 이관. 삭제된 교육 기능 전체 실행 parity 미확정 | EDU catalog/test focused |
| BAT-MIG-006 | 부분 구현 | Job Pack Generator 존재/보강 이력 있으나 QA 필수 sample parity 재확인 필요 | generator jobpack test |
| BAT-MIG-007 | 구현 확인/재확인 | `cpf-batch/src/**` 제거 확인. stale Gate/Docs/empty dir 0 재확인 | low-cost static gate |
| BAT-MIG-008 | 이전 부분 검증 | 5 standalone artifact build 성공 이력 있음. 이번 패치와 BAT runtime source 직접 영향 없음 | 전체 반복 금지; artifact federation 관련 publish만 |
| BAT-MIG-009 | 통합검증 예정 | 2 Scheduler/2 Worker/2 Center-Cut 실제 topology 미검증 | 최종 multi-instance |
| BAT-MIG-010 | 부분 구현 | ADM→BAT Owner API 전환 진행. 전체 Control Plane UX/approval/evidence 미완료 | ADM/BAT focused integration |

## C. Gateway Resilience

| ID | 상태 | 남은 작업 |
|---|---|---|
| GWY-001~004 | 부분 구현 | 실제 Target A down → B failover/outlier/recovery process evidence 필요 |
| GWY-005~009 | 부분 구현 | timeout taxonomy/retry budget/non-idempotent loss/UNKNOWN_RESULT/storm 방지 실제 fault 검증 필요 |
| GWY-010~012 | 부분 구현 | O/S/B 외부→S 차단과 우회 시나리오 실제 검증 필요 |
| GWY-013~016 | 부분 구현 | Header trust/transactionId/W3C/Forwarded spoofing focused 검증 필요 |
| GWY-017~021 | 부분 구현 | 2 Gateway route version/drift/rejoin/state recovery 미검증 |
| GWY-022 | 통합검증 예정 | Runtime Fault Evidence 묶음 필요 |

## D. ADM/BZA

| ID | 상태 | 최신 판정 / 남은 작업 |
|---|---|---|
| ADM-001 | 부분 구현 | Control Plane 기능은 다수 존재하나 Gateway/BAT/Recovery/Deployment까지 Browser-operable 완결성 미확정 |
| ADM-002 | 부분 구현 | MBR 전용 결합 제거 확인. Registry/Capability 기반 일반화 최종 필요 |
| ADM-003 | 이번 패치 구현·미검증 | 연락처는 `adm_operator_profile` 소유, `연락처(휴대폰)`/`내부 전화번호` 분리. 나머지 Directory 연계/Masking 완결성은 계속 P1 |
| ADM-004 | 부분 구현 | Approval/Break-glass 기반은 있으나 QA 위험조치 전체 coverage 재대조 필요 |
| BZA-001 | 부분 구현 | 조직/직원/Assignment 기반 존재. 연락처 내부전화 이번 패치 추가, 전체 모델/이력 Browser 검증 필요 |
| BZA-002~004 | 부분 구현 | Role/Permission/Approval/업무대상 Capability 구현을 QA 시나리오와 대조 필요 |
| BZA-005 | 이번 패치 구현·미검증 | 신규 직원 미입력 재직상태 `EMPLOYED`로 정렬; DB V60 runtime 미검증 |
| BZA-006 | 부분 구현 | 연락처 문자열/분리 보강. Masking/raw permission/download audit 전체 검증 필요 |
| UI-001~005 | 부분 구현 | Server paging/오류/권한/접근성/permission manifest parity Browser 검증 필요 |

## E. Build / Artifact / Deploy

| ID | 상태 | 최신 판정 / 남은 작업 |
|---|---|---|
| BUILD-001~002 | 구현 확인/재확인 | Tooling이 `cpf-tools/build/*`로 이동. isolated publish/consumer 재검증 필요 |
| CPF-BUILD-LOCAL-001 | 이번 패치 구현·미검증 | 성공한 CPF build 후 shared local Maven sync, Generator standalone local resolution, BOM/Convention Plugin, bootJar/bootWar dependency Gate 추가 | Codex focused artifact validation |
| BUILD-003~004 | 부분 구현 | cleanup 안전성 보정 이력 있음. 최신 기준 garbage/hygiene 최종 감사 필요 |
| BUILD-005 | 재확인 필요 | IDE Problems의 실제 오류 vs SQL extension/Language Server stale 분류를 최종 개발 환경에서 확인 |
| 독립 Artifact Federation | 이번 패치 구현·미검증 | Remote registry 우선 + local fallback. 상용은 승인된 Registry 사용 | generated standalone build/package |
| ART-SUPPLY-LOCAL | 부분 구현/후속 | `LOCAL_DEV`는 동일 Repo Project Dependency 또는 shared local CPF repository로 최신 개발 artifact 사용 | source change → domain package focused verify |
| ART-SUPPLY-REMOTE | 부분 구현/후속 | CI/CD는 승인된 Nexus/Artifactory 등 고정 Version만 사용. Local fallback 금지/fail-closed 필요 | CI isolated repository test |
| ART-SUPPLY-OFFLINE | 미구현/준비 | Registry 없는 환경용 version/manifest/checksum/BOM 포함 Offline Library Bundle과 Gradle 자동 packaging 필요 | offline consumer package test |

## F. DB / Query / Migration

| ID | 상태 | 최신 판정 / 남은 작업 |
|---|---|---|
| DB-001 | 정책 유지 | 모든 DB 변경은 Canonical → Generator/Template → Vendor → Migration/Rollback → Sync/Verify → Runtime 순서 |
| DB-002~003 | 부분 구현 | BAT query pack 중앙화 대폭 진행. Platform/BZA 잔여 inline/vendor SQL 및 unused resource 감사 필요 |
| DB-004 | 부분 구현 | 5 Vendor runtime template parity 존재하나 실DB 지원 완료는 MariaDB만. 복사/치환 완료 처리 금지 |
| DB-005 | 미검증 | 전체 Historical Migration chain 미검증 |
| DB-006 | 부분 구현 | Generated Domain DB template 존재. Golden parity/2 Domain lifecycle 필요 |
| DB-007 | 부분 구현 | multi-datasource ownership 결함 일부 수정. failover/read replica consistency 미검증 |
| DB-008 | 부분 검증 | BAT 158/158 MariaDB PREPARE 과거 통과. 이번 패치로 재실행하지 않음 |
| V59 Contact | 이번 패치 구현·미검증 | `adm_operator_profile` + BZA office phone; sync/upgrade/rollback/reapply 필요 |
| V60 Safe Default | 이번 패치 구현·미검증 | BZA 신규 employee default EMPLOYED; sync/lifecycle 필요 |

## G. Defaults / Message / Config

| ID | 상태 | 남은 작업 |
|---|---|---|
| DEF-001~003 | 부분 구현 | EDU/OpenAPI defaults 전수 parity, nullable 선택값 규칙 확대 |
| DEF-004~005 | 부분 구현 | Message placeholder parity, 상태 Code catalog 전수 검증 |
| DEF-006 | 부분 구현 | Config metadata Type/Default/Secret/Dynamic/Restart/Risk/Deprecation 완결성 |

## H. Runtime / Browser / Multi-instance / Fault

| ID | 상태 | 비고 |
|---|---|---|
| TEST-001 | 재검증 필요 | 후반 대규모 변경 이후 최신 Commit 전체 clean/test/assemble 최종 Evidence 없음. 이번 ChatGPT patch 적용 후에는 변경 영향 범위를 먼저 검증하고 최종 통합 Commit에서 1회 수행 |
| TEST-002 | 부분 검증 | 이전 MBR/ADM/BZA/REF/ACC/GWY 실제 boot 기록 존재. BAT 5 Runtime/Generated 2 Domain 최종 필요 |
| TEST-003 | 미검증 | ADM/BZA Browser E2E |
| TEST-004 | 미검증 | Multi-instance |
| TEST-005 | 미검증 | Fault Injection |
| TEST-006 | 부분 구현/미검증 | 영역별 UNKNOWN_RESULT Recovery 실제 E2E 필요 |
| TEST-007 | 정책 유지 | 최신 SHA Evidence만 현재 검증으로 인정 |

## I. Governance / Productization

- Repository/Source Access Governance, CODEOWNERS/SoD/Break-glass: **부분 구현/후속**
- Deployment Cell rolling/canary/blue-green/rollback/reconcile: **부분 구현/후속**
- SBOM/License/CVE/Signature/Release: **부분 구현/최종 제품화 검증**
- EDU/OpenAPI/JavaDoc/Guide/Evidence 정합성: **지속 관리**
- Repository Hygiene: **지속 관리 + 최종 통합 검증**

## Codex 크레딧 절약 규칙

1. Codex 실제 투입 직전 최신 master의 누적 ChatGPT 변경과 Change Ledger를 기준으로 집중 검증 범위를 다시 산정한다.
2. 과거 BAT 158 PREPARE, V58 lifecycle, 기존 single-runtime boot는 직접 영향이 없으면 반복하지 않는다.
3. 실패가 공통 API/BOM/Generator/DB canonical로 확산될 때만 Consumer 검증 범위를 넓힌다. 단, 이후 변경 영향권에 들어온 과거 PASS는 재검증 대상으로 다시 연다.
4. Browser/Multi-instance/Fault는 기능 구조가 안정된 최종 통합 기준 Commit에서 묶어 실행한다.
5. 실행하지 않은 것은 `미검증`으로 남기며 PASS로 추정하지 않는다.


## J. Gate / Tool / Manual / Distribution

| ID | 상태 | 최신 판정 / 남은 작업 |
|---|---|---|
| TOOL-GATE-001 | 미구현/준비 | 전체 PowerShell/Gradle Gate Inventory 작성 및 Owner/Caller/Requirement 연결 필요 |
| TOOL-GATE-002 | 미구현/준비 | 각 Gate를 `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL`로 분류 |
| TOOL-GATE-003 | 미구현/준비 | 중복/Legacy/무호출/일회성 Gate를 안전하게 통합·삭제. 삭제 전 Consumer와 대체 Requirement coverage 확인 |
| TOOL-GATE-004 | 부분 구현/후속 | 개발 대표 Entry를 `QUICK` / `VERIFY` / `FULL`로 표준화하고 고비용 검증을 QUICK에서 제외 |
| TOOL-GATE-005 | 부분 구현/후속 | 가능한 기본 Gate를 Gradle/JVM Portable Entry로 정본화하고 PowerShell은 Windows Wrapper 역할로 정리 |
| TOOL-GATE-006 | 미구현/준비 | 공식 Tool별 옵션/Default/환경변수/입출력/Side Effect/실패/복구/Example 문서와 Script Help 일치 |
| TOOL-GATE-007 | 미구현/준비 | Runtime 배포물에서 `DEV_ONLY`/`CI_RELEASE` Script 제외. 필요한 `PRODUCT_ADMIN_TOOL`만 관리 Tool 패키지로 제공 |
| TOOL-GATE-008 | 지속 관리 | ChatGPT가 삭제를 확정하지 못한 Gate는 삭제 후보로 기록하고 미래 Codex가 최신 master 실제 호출자/coverage 확인 후 제거 |

정본: `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`
