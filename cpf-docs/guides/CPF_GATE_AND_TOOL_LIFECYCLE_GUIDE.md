# CPF Gate·Tool Lifecycle 및 배포 정책 가이드

## 1. 목적

CPF의 Gate와 PowerShell/Gradle Tool은 개발 중 품질을 높이고 설치·배포·운영을 표준화하기 위한 제품 자산이다.
그러나 Gate 수가 계속 증가하면 중복·Legacy·임시 Script가 남아 실제 사용자가 어떤 명령을 사용해야 하는지 알기 어렵고,
운영 배포물에 개발용 검증 Script가 불필요하게 섞일 수 있다.

따라서 모든 Gate/Tool은 **명확한 Owner, 용도, 실행 시점, 배포 여부, 삭제 조건**을 가져야 한다.
이 문서는 Gate/Tool의 Lifecycle과 정리 기준의 정본이다.

## 2. 역할 분류

모든 Gate/Tool은 다음 셋 중 하나로 분류한다.

| 분류 | 목적 | 대표 사용 위치 | 운영 Runtime 배포 |
|---|---|---|---|
| `DEV_ONLY` | 개발 중 Source/Architecture/정합성 빠른 검증 | 개발자 PC, PR 전 | 금지 |
| `CI_RELEASE` | Build/Release/배포 전 품질 차단 | Jenkins/CI | Runtime 배포 금지 |
| `PRODUCT_ADMIN_TOOL` | 설치·Upgrade·Rollback·DB Verify·Generator 등 고객/관리자 기능 | 관리자 PC/배포 서버 | 관리 Tool 패키지로 제공 가능 |

분류가 없는 Script는 정식 Tool로 인정하지 않는다.

## 3. Gate 실행 레벨

개발자가 수십 개 Script 이름을 외우지 않도록 대표 Entry를 3단계로 표준화한다.

### 3.1 QUICK

개발 중 수시 실행하는 저비용 Gate다.

포함 예:

- Java/Gradle 기본 정적 검증
- Module Ownership/Dependency Boundary
- Public API/SPI/Internal import
- Generator/Generated Domain static parity
- DB metadata/schema/checksum static consistency
- Route/Permission manifest
- Secret/Repository Hygiene
- PowerShell syntax/기본 계약
- 문서/정본 경로 정합성

다음과 같은 고비용 검증은 QUICK에 포함하지 않는다.

- 실제 전체 DB Fresh Install
- 5 Vendor Runtime DB
- Browser E2E
- Multi-instance/Fault Injection
- 전체 Generator Lifecycle

### 3.2 VERIFY

하나의 개발 작업 단위가 끝났을 때 실행한다.

`QUICK + 변경 영향 Module Test + Packaging + 필요한 Generated Domain/DB focused lifecycle`을 기본으로 한다.

### 3.3 FULL

통합 검증/Release 후보 Commit에서 실행한다.

`VERIFY + 실제 DB Lifecycle + Browser + Multi-instance + Fault + Generator Create/Remove/Regenerate + Install/Upgrade/Rollback`을 포함한다.

FULL은 매 개발 변경마다 반복하지 않는다.

## 4. Gate Inventory 필수 항목

Gate/Tool Inventory에는 최소 다음을 기록한다.

- Script/Gradle Task 이름과 경로
- Owner 영역
- 역할 분류: `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL`
- 검증 Requirement/정책
- 직접 호출자와 상위 Aggregate Gate
- 읽기 전용/변경 작업 여부
- 입력 옵션과 Default
- 필요한 환경변수
- 생성/변경 산출물
- 실패 조건과 종료코드
- Evidence 생성 여부
- 운영 배포 포함 여부
- 중복/대체 Gate
- 상태: 유지 / 통합 후보 / 삭제 후보 / 재확인 필요

## 5. 불필요 Gate 정리 원칙

Gate 수를 늘리는 것이 품질 목표가 아니다. 다음에 해당하면 통합 또는 삭제 대상으로 분류한다.

- 동일 Requirement를 다른 정본 Gate가 완전히 검사하는 중복 Gate
- 삭제된 Architecture/Legacy Module을 전제로 하는 Gate
- 과거 일회성 Migration/개발 작업 때문에 만든 임시 Gate
- 호출자와 Consumer가 없고 상위 Aggregate에서도 사용하지 않는 Gate
- Source 구조 변경 후 항상 SKIP되거나 실질적으로 아무것도 검사하지 않는 Gate
- 이름만 다르고 검사 내용이 동일한 Gate
- 현재 제품 정책과 충돌하는 Gate

단순히 오래되었다는 이유로 삭제하지 않는다.

## 6. 안전한 삭제 절차

ChatGPT 또는 Codex가 Gate/Tool을 삭제할 때 다음을 반드시 확인한다.

1. 최신 master에서 Script/Task의 실제 호출자 검색
2. 어떤 Requirement를 보호하는 Gate인지 확인
3. 동일 Requirement가 다른 Gate로 대체되는지 확인
4. CI/Gradle/PowerShell/Guide/Handover에서 참조 여부 검색
5. 대체 Gate가 없다면 먼저 대체 검증을 구현
6. Script/Task와 stale 문서/호출부를 함께 제거
7. QUICK 또는 해당 focused VERIFY 실행
8. 삭제 이유와 영향 범위를 Handover/Continuity에 기록

검증 없이 대량 삭제하지 않는다.

## 7. 최종 제품 배포 정책

### 7.1 Runtime 배포물

업무 WAS/BootJar/BootWar에는 Runtime에 필요한 Library와 Config만 포함한다.
개발용 Gate Script를 함께 배포하지 않는다.

### 7.2 관리자 Tool 배포물

다음은 제품 관리 기능이므로 별도 관리 Tool 패키지로 제공할 수 있다.

- DB Install/Upgrade/Rollback/Verify
- Schema Drift Check
- Generated Domain Generator
- Offline Artifact Bundle 설치/검증
- 배포 사전점검/복구 Tool

### 7.3 개발·CI 전용

Architecture/Hygiene/Source Documentation/Static Dependency/전체 Test Aggregation 등은 Repository와 CI에만 남긴다.

## 8. PowerShell/Tool 매뉴얼 필수 표준

공식 Tool은 구현만 존재해서는 안 된다. 관리자나 개발자가 실제로 사용할 수 있도록 Guide에 최소 다음을 제공한다.

1. 목적과 사용 시점
2. 실행 대상 환경
3. 필수 옵션
4. 선택 옵션과 Default
5. 옵션 간 조합/제약
6. 환경변수
7. 입력 파일/프로필
8. 출력/생성 파일
9. DB/File/Repository 변경 영향
10. 정상 실행 예
11. 실패 예와 대표 원인
12. 재실행 가능 여부와 멱등성
13. Rollback/복구 방법
14. 권한/보안 주의사항
15. 운영환경 사용 가능/금지 여부
16. Evidence 생성 위치

Script의 `Get-Help`/Usage와 문서의 옵션 설명이 서로 다르면 결함으로 처리한다.

## 9. Portable Entry 정책

가능한 Gate는 Gradle/JVM을 Portable 정본 Entry로 두고 PowerShell은 Windows 개발/관리 편의 Wrapper 역할을 맡는다.

예:

```text
gradlew cpfQuickCheck
gradlew cpfVerify
gradlew cpfFullVerify
```

Windows에서는 필요하면 다음과 같이 Wrapper를 제공할 수 있다.

```powershell
pwsh -File .\cpf-tools\scripts\check-cpf-development.ps1
```

Linux/Jenkins가 PowerShell 설치 여부 때문에 기본 품질 Gate를 실행하지 못하는 구조를 만들지 않는다.
단, DB/Windows 운영 관리처럼 PowerShell 자체가 제품 Tool의 정식 실행환경인 경우는 예외다.

## 10. Local / Remote / Offline Artifact 공급과 Tool 책임

CPF Artifact 공급은 다음 세 모드를 지원하는 것을 목표로 한다.

- `LOCAL_DEV`: CPF Source 변경을 로컬 개발 Domain에 빠르게 반영
- `REMOTE`: Jenkins/CI가 승인된 Nexus/Artifactory 등의 immutable version을 사용
- `OFFLINE`: 원격 Registry가 없는 환경에서 checksum/manifest가 포함된 CPF Offline Library Bundle 사용

CI/STG/PROD에서는 개발자 Local Repository fallback을 허용하지 않고 fail-closed해야 한다.
OFFLINE에서도 수동 JAR 복사를 표준으로 삼지 않고 Gradle이 검증된 Bundle을 자동 패키징해야 한다.

## 11. Codex 후속 검수 필수사항

향후 Codex 작업 요청서에는 다음을 반드시 포함한다.

- 최신 master에서 Gate/PowerShell/Gradle Task Inventory 작성
- 각 항목을 `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL`로 분류
- 호출자 0, Requirement 대체 완료, stale 참조까지 확인된 Gate는 삭제
- 중복 Gate는 대표 Gate로 통합
- QUICK/VERIFY/FULL Aggregate Entry가 실제 하위 Gate와 일치하는지 검증
- 공식 Tool의 옵션/Default/Example/Side Effect/Recovery 문서화 누락 검출
- Runtime 배포물에 `DEV_ONLY`/`CI_RELEASE` Script가 포함되지 않는지 검증
- 관리 Tool 패키지는 필요한 `PRODUCT_ADMIN_TOOL`만 포함하는지 검증

## 12. 변경 영향 기반 재검증

과거 PASS 항목이라도 이후 Source/DB/Generator/Build/Gate 변경의 영향권에 들어오면 재검증 상태로 다시 연다.
반대로 변경과 무관한 고비용 검증을 습관적으로 전부 재실행하지 않는다.

즉 검증 상태는 다음처럼 관리한다.

- 기존 검증 완료, 현재 변경 영향 없음
- 기존 검증 완료, 변경 영향으로 재검증 필요
- 신규 미검증
- 통합검증 예정

이 기준은 ChatGPT/Codex/다른 PC 세션 모두 동일하게 적용한다.

## 13. 20260727 Artifact/Stack Gate 분류

| Entry | 분류 | 상위 Level | Side Effect | 배포 |
|---|---|---|---|---|
| `checkCpfStackSupport` | `DEV_ONLY` + `CI_RELEASE` | QUICK | 없음 | Runtime 제외 |
| `aggregateQualityBuild` | `CI_RELEASE` | VERIFY | build 산출물 | Runtime 제외 |
| `publishCpfStagingPlatformArtifacts` | 내부 Tool | VERIFY 내부 | 격리 staging 변경 | Runtime 제외 |
| `publishCpfVerifiedLocalPlatformArtifacts` | `DEV_ONLY` | VERIFY | Shared Local Maven 변경 | Runtime 제외 |
| `verifyCpfLocalArtifactPropagation` | `DEV_ONLY` | VERIFY | 없음 | Runtime 제외 |
| `publishCpfPlatformArtifacts` | `CI_RELEASE` | Release | Remote Registry 변경 | Runtime 제외 |
| `buildCpfOfflineArtifactBundle` | `CI_RELEASE` + `PRODUCT_ADMIN_TOOL` | Release/Distribution | Offline Bundle 생성 | 별도 관리/배포 Tool 산출물 |
| `promote-cpf-verified-local-artifacts.ps1` | 내부 Tool | VERIFY 내부 | Shared Local Maven 변경 | Runtime 제외 |
| `verify-local-artifact-propagation.ps1` | `DEV_ONLY` + `CI_RELEASE` | VERIFY | 없음 | Runtime 제외 |

저수준 Staging/Promotion Task는 사용자가 개별 실행하는 Public Tool로 홍보하지 않는다.
상위 검증 Entry가 호출하는 내부 Tool로 관리한다.

## 14. Artifact Gate 삭제·통합 시 주의

Artifact 관련 Gate는 단순 파일 존재검사로 축소하면 안 된다. 최소 보호 Requirement는 다음과 같다.

- Exact coordinate/version
- POM identity
- Gradle module metadata identity
- Platform BOM exact constraint
- Gradle Plugin Marker → Implementation exact version
- SHA-256
- Source Commit Manifest
- Promotion State
- REMOTE Local fallback 금지

향후 더 강한 Release Gate가 위 Requirement를 완전히 흡수하면 작은 Gate를 삭제할 수 있으나,
Caller와 Requirement Coverage를 확인하지 않고 Script만 제거하지 않는다.
