# CPF Tools 매뉴얼 — 생성·빌드·DB·실행·검증·패키징 도구를 처음부터 끝까지 사용하는 절차

> **주 독자**: 업무 개발자, 빌드·배포 담당자, DBA, 플랫폼 운영자, 검수자
> **완료 결과**: 목적에 맞는 Tool을 선택하고 Dry Run·Apply·결과 검증·재실행·Rollback·정리를 수행한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. Tool 사용 공통 원칙](#1-tool-사용-공통-원칙)
- [2. 공통 Preflight 한 줄 명령](#2-공통-preflight-한-줄-명령)
- [3. Tool 지도](#3-tool-지도)
- [4. Domain Generator 종단간](#4-domain-generator-종단간)
  - [4.1 Parameter](#41-parameter)
  - [4.2 Dry Run 한 줄 명령](#42-dry-run-한-줄-명령)
  - [4.3 Apply 전 Gate](#43-apply-전-gate)
  - [4.4 Apply와 검증](#44-apply와-검증)
- [5. Build·Artifact Mode](#5-buildartifact-mode)
  - [5.1 전체 Build 한 줄](#51-전체-build-한-줄)
  - [5.2 결과](#52-결과)
- [6. DB Tool](#6-db-tool)
  - [6.1 전체 초기화 한 줄](#61-전체-초기화-한-줄)
  - [6.2 운영 순서](#62-운영-순서)
- [7. Local Runtime Tool](#7-local-runtime-tool)
- [8. OpenAPI·Generated Client](#8-openapigenerated-client)
- [9. Docker Tool](#9-docker-tool)
  - [전체 설치 한 줄](#전체-설치-한-줄)
  - [확장 환경 한 줄](#확장-환경-한-줄)
- [10. Verification Tool 선택](#10-verification-tool-선택)
- [11. Package Tool과 Root Overlay ZIP](#11-package-tool과-root-overlay-zip)
- [12. 재실행·Rollback·Cleanup](#12-재실행rollbackcleanup)
- [13. Tool 결과 Manifest](#13-tool-결과-manifest)
- [14. 완료 체크리스트](#14-완료-체크리스트)
- [15. Tool별 실행 카드](#15-tool별-실행-카드)
  - [15.1 Generator](#151-generator)
  - [15.2 Gradle Wrapper/Root Build](#152-gradle-wrapperroot-build)
  - [15.3 Build Plugin](#153-build-plugin)
  - [15.4 Platform BOM](#154-platform-bom)
  - [15.5 DB Initialize](#155-db-initialize)
  - [15.6 Local Start](#156-local-start)
  - [15.7 Local Status](#157-local-status)
  - [15.8 Local Stop](#158-local-stop)
  - [15.9 OpenAPI Export/Validate](#159-openapi-exportvalidate)
  - [15.10 Generated Client](#1510-generated-client)
  - [15.11 Docker Full Install](#1511-docker-full-install)
  - [15.12 Docker Incremental](#1512-docker-incremental)
  - [15.13 Verification Scripts](#1513-verification-scripts)
  - [15.14 Supply Chain](#1514-supply-chain)
  - [15.15 Packaging](#1515-packaging)
- [16. 명령 작성 표준](#16-명령-작성-표준)
- [17. Tool 사용성 검수 시나리오](#17-tool-사용성-검수-시나리오)
- [18. Tool 오류 보고 예시](#18-tool-오류-보고-예시)

<!-- CPF-TOC:END -->

## 1. Tool 사용 공통 원칙

- 현재 폴더에 의존하지 않고 `$repo` 절대경로와 `git -C $repo`를 사용한다.
- 실행 전에 `param`, Help, 입력 정본, 변경 경로를 확인한다.
- Dry Run·Preview·Validate가 있으면 Apply보다 먼저 실행한다.
- 기존 Working Tree 변경과 다른 작업자의 미추적 파일을 보호한다.
- `git reset --hard`, `git clean`, 광범위한 Wildcard 삭제를 사용하지 않는다.
- 성공은 Exit Code뿐 아니라 생성 파일·Hash·Build·DB·Health·업무 결과로 판정한다.

## 2. 공통 Preflight 한 줄 명령

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}; git -C $repo remote -v; git -C $repo branch --show-current; git -C $repo fetch origin master; git -C $repo rev-parse HEAD; git -C $repo rev-parse origin/master; git -C $repo status --short; git -C $repo diff --name-status; git -C $repo diff --stat; git -C $repo ls-files --others --exclude-standard; java -version; & (Join-Path $repo 'gradlew.bat') --version; pwsh --version
```

## 3. Tool 지도

| 업무 | 정본 경로 | 입력 | 결과 | 다음 확인 |
|---|---|---|---|---|
| Domain 생성 | `cpf-tools/generator/create-domain.ps1` | Domain/System/DB/Profile/Provider | Source·Config·SQL·Test·Manifest | Compile·Test·Manifest |
| Build Plugin | `cpf-tools/build/gradle-plugin/` | Stack·Artifact Mode | 공통 Build/Gate | Task·Artifact |
| Platform BOM | `cpf-tools/build/platform-bom/` | Stack Version | Version 정렬 | Dependency Report |
| DB 초기화 | `cpf-tools/scripts/initialize-cpf-database.ps1` | Vendor·접속·범위 | Schema·Migration | Flyway·대사 |
| Local Runtime | `cpf-tools/scripts/start/status/stop-cpf-local.ps1` | Profile·Port·Config | Process·PID·Log | Readiness·Smoke |
| OpenAPI/Client | Product frontend scripts·OpenAPI | Backend Contract | Generated Client | Diff·Compile·Browser |
| Verification | `cpf-tools/verification/`, `cpf-tools/scripts/verify-*` | 정본·Source | Report·Evidence | Exit·Coverage |
| Docker | `cpf-tools/environment/docker-development-test/` | DockerRoot·RepoRoot | Created/Stopped 환경 | Compose·Health |
| Packaging | `cpf-tools/scripts/package-*` | Base/Head·Manifest | Root Overlay ZIP | Path·Hash·CRC |
| Supply Chain | `cpf-tools/supply-chain/`, Trivy·ORT | Artifact/SBOM | 취약점·License Report | Policy Gate |

## 4. Domain Generator 종단간

### 4.1 Parameter

| Parameter | Type/범위 | Default | 설명 |
|---|---|---|---|
| DomainName | 2~30 영문 소문자·숫자 | 없음 | 업무 Domain |
| SystemCode | 조직 코드 규칙 | 없음 | Transaction/Module 식별 |
| Port | 1024..65535 | 8080 | Local Port |
| Database | Y/N | Y | DB 사용 |
| DatabaseVendor | mariadb/postgresql/oracle | mariadb | Vendor Pack |
| DependencyModel | root-project/published-artifact | root-project | Dependency 공급 |
| PlatformVersion | Version | 1.0.0-SNAPSHOT | 게시 Artifact Version |
| CapabilityProfile | 13개 Profile | MINIMAL_BOOT_DOMAIN | 기능 조합 |
| ProviderBindings | capability=provider | 없음 | Named Provider |
| Online/Batch/CenterCut/External/Messaging/File/SecurityAudit/Ui/BzaMenu | Y/N | 기능별 | 추가 생성 범위 |
| ProductionProfile | Y/N | N | 운영 Fail-closed Profile |
| ProvisionDatabase | Switch | false | DB Provision |
| DryRun/GeneratePatch/Apply | Switch | false | 실행 Mode |

### 4.2 Dry Run 한 줄 명령

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -Root $repo -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -CapabilityProfile SECURE_RESOURCE_API -ProviderBindings 'cache=valkey' -DryRun
```

### 4.3 Apply 전 Gate

- Domain/Module/Package/SystemCode/Port/Schema/Table Prefix 충돌이 없다.
- Capability Profile과 Provider Binding이 Catalog 허용 목록에 있다.
- `resolvedStarters`와 Version Lock이 생성 계획에 나온다.
- 중앙 Domain Template의 2개 Physical Table 계약과 3 Vendor 의미가 유지된다.
- 생성·수정·보호 파일 Manifest가 분리된다.
- 기존 파일을 덮는 항목은 Diff와 승인이 있다.

### 4.4 Apply와 검증

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -Root $repo -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -CapabilityProfile SECURE_RESOURCE_API -ProviderBindings 'cache=valkey' -Apply; if($LASTEXITCODE -ne 0){throw 'Generator 실패'}; & (Join-Path $repo 'gradlew.bat') :cpf-payment:clean :cpf-payment:test :cpf-payment:assemble
```

실제 Project 이름은 Manifest를 따른다. 재실행은 같은 입력에서 중복 파일·SQL·Dependency가 생기지 않아야 한다. 충돌하면 자동 덮어쓰지 않고 Patch/Diff를 검토한다.

## 5. Build·Artifact Mode

| Mode | 입력 | Repository | 사용 시점 | 실패 |
|---|---|---|---|---|
| LOCAL_DEV | `CPF_ARTIFACT_MODE=LOCAL_DEV` | `~/.cpf/repository` | 같은 PC 개발 | 로컬 Artifact 없음 |
| REMOTE | `CPF_ARTIFACT_REPOSITORY_URL` + Credential | 승인 원격 Repository | 공유 개발·CI·배포 | URL/Credential/Hash 오류 |
| OFFLINE | `CPF_OFFLINE_ARTIFACT_REPOSITORY` | 봉인 Bundle | 망분리 | 누락 Artifact 즉시 실패 |

### 5.1 전체 Build 한 줄

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; & (Join-Path $repo 'gradlew.bat') clean build --stacktrace; if($LASTEXITCODE -ne 0){throw 'Gradle Build 실패'}
```

### 5.2 결과

- Java 25 Toolchain과 Stack Version이 정본과 일치한다.
- Unit·Contract·Architecture·Generated Contract Gate가 통과한다.
- JAR/WAR/Static/Worker Artifact와 Sources·JavaDoc·POM·SBOM이 생성된다.
- Manifest에 Source SHA·Artifact Mode·Version·Hash가 기록된다.
- 실패 시 첫 실패 Task와 Report를 보존하고 후속 Task 성공으로 덮지 않는다.

## 6. DB Tool

### 6.1 전체 초기화 한 줄

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\initialize-cpf-database.ps1') -All -RequireRun; if($LASTEXITCODE -ne 0){throw 'DB 초기화 실패'}
```

### 6.2 운영 순서

1. Backup·접속·권한·Timezone·Charset를 확인한다.
2. Admin이 Database/Schema/User를 준비한다.
3. Migration 계정으로 Fresh/Upgrade Migration을 적용한다.
4. Runtime 계정의 최소 권한을 검증한다.
5. Flyway History·Schema Hash·Constraint·Index·초기 데이터를 확인한다.
6. 대사 Query와 Application Smoke를 실행한다.
7. Rollback 가능 변경은 복원 시험, 불가능 변경은 Forward Fix Runbook을 확인한다.

## 7. Local Runtime Tool

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1'); if($LASTEXITCODE -ne 0){throw 'Local 기동 실패'}; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
```

종료:

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\stop-cpf-local.ps1'); if($LASTEXITCODE -ne 0){throw 'Local 종료 실패'}
```

Status가 Running이어도 DB/Broker/Secret/Readiness/업무 Smoke를 확인한다. Port 충돌·Stale PID·부분 기동·응답 유실은 Process를 무조건 Kill하지 않고 상태 파일·Log·Health로 판정한다.

## 8. OpenAPI·Generated Client

1. Backend Controller·DTO·Error Contract를 변경한다.
2. Canonical OpenAPI를 생성한다.
3. OpenAPI Validation과 Operation ID 중복을 검사한다.
4. ADM/BZA Frontend Generated Client와 Route Operation Contract를 생성한다.
5. 수동 수정이 없는지 Diff로 확인한다.
6. Typecheck·Unit·Production Build·Browser Contract Test를 실행한다.
7. Backend Permission·Frontend Guard·Button Operation을 대조한다.

Generated 파일만 수정하지 않는다. 원본 Controller/OpenAPI/Generator를 수정하고 재생성한다.

## 9. Docker Tool

### 전체 설치 한 줄

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1') -RepoRoot $repo; if($LASTEXITCODE -ne 0){throw 'Docker 전체 설치 실패'}
```

### 확장 환경 한 줄

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\environment\docker-development-test\CPF_도커_확장연동환경_증분설치.ps1') -RepoRoot $repo; if($LASTEXITCODE -ne 0){throw 'Docker 확장 설치 실패'}
```

설치 Tool은 Container를 Created/Stopped 상태로 준비한다. 서비스 기동과 업무 Schema·Seed·Topic 생성은 검증 시나리오가 명시적으로 수행한다.

## 10. Verification Tool 선택

| 변경 | 필수 Gate | 추가 Runtime |
|---|---|---|
| Core/Public API | Compile·ArchUnit·Compatibility·Core-only Consumer | Local/Remote Contract |
| Starter | Context·Property·Consumer·Removal·Artifact | 실제 Provider·Fault |
| DB | SQL Parse·3 Vendor Parity·Migration Inventory | Fresh/Upgrade/Rollback/Restore |
| ADM/BZA | OpenAPI·Generated Client·Route/Permission | Browser 3종·Response Loss |
| Gateway | Route/Policy Contract·Security Corpus | Scale-out·Partial Apply·LKG |
| Batch | Job/Step Contract·Metadata | Process Kill·Worker Loss·Fencing |
| Docker | Compose Config·Image Lock·Stopped Verification | Service Health·Toxiproxy |
| 문서 | Link·Anchor·Identifier·Command·Completeness | 초보 사용자 수행성 |

## 11. Package Tool과 Root Overlay ZIP

- Repository Root 상대경로를 유지한다.
- 임의 최상위 폴더를 만들지 않는다.
- 최종 신규·수정 파일만 포함한다.
- 삭제는 정확한 Manifest로 별도 제공한다.
- `.git`, build, log, tmp, bak, IDE, Cache, Secret, 과거 ZIP을 제외한다.
- ZIP 파일 수·경로·SHA-256·CRC·압축 해제 Byte 일치를 확인한다.

## 12. 재실행·Rollback·Cleanup

| 상황 | 재실행 | Rollback | 금지 |
|---|---|---|---|
| Generator 중단 | Manifest와 기존 파일 Diff 후 같은 입력 | 생성 전 Backup과 신규 경로만 제거 | 전체 Domain 폴더 삭제 |
| Build 실패 | 첫 실패 Task부터 원인 수정 | Source 변경 되돌리기 | Cache 전체 삭제로 은폐 |
| Migration 실패 | Flyway 상태·Commit 여부 확인 | Backup/승인 Rollback 또는 Forward Fix | History 수동 변조 |
| Local Runtime 실패 | Stale PID·Port·Log 확인 | 이전 Config/Artifact | 무조건 Process Kill |
| Docker 설치 실패 | Created Container·Image Lock·Secret 확인 | 이번 Compose Resource만 Down/Remove | Volume/전체 Docker Prune |
| Package 실패 | Manifest와 허용 경로 수정 | 생성 ZIP 삭제 | Repository Root 가비지 생성 |

## 13. Tool 결과 Manifest

| 필드 | 내용 |
|---|---|
| tool | Script/Task 경로 |
| sourceSha | 실행 Source SHA |
| startedAt/endedAt | 시간과 Timezone |
| arguments | Secret 제거 입력 |
| environment | OS/JDK/Gradle/Docker/DB |
| exitCode | 실제 종료 코드 |
| created/modified/deleted | 정확한 상대경로 |
| resultHash | 파일/Report SHA-256 |
| normalResult | 판정 근거 |
| rollback | 정확한 복원 절차 |
| unverified | 실행하지 않은 항목 |

## 14. 완료 체크리스트

- [ ] 절대경로와 현재 위치 독립 명령을 사용했다.
- [ ] 기존 변경·미추적 파일을 보호했다.
- [ ] Dry Run·Preview·Diff를 먼저 확인했다.
- [ ] 입력·Default·허용 범위와 Secret을 검토했다.
- [ ] Exit Code 외 생성 파일·Hash·Build·DB·Health를 확인했다.
- [ ] 같은 입력 재실행에서 중복·손상이 없다.
- [ ] Rollback과 Cleanup이 정확한 경로만 대상으로 한다.
- [ ] 결과 Manifest와 운영 인계를 남겼다.

## 15. Tool별 실행 카드

### 15.1 Generator

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/generator/create-domain.ps1` |
| 입력 | Domain·System·DB·Profile·Provider |
| 출력 | Source·Config·SQL·Test·Manifest |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Dry Run 불일치, 경로 충돌, Profile 오류 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 생성 Manifest 신규 경로 제거·Backup 복원 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.2 Gradle Wrapper/Root Build

| 항목 | 내용 |
|---|---|
| 정본 경로 | `gradlew.bat / build.gradle / settings.gradle` |
| 입력 | Task·Artifact Mode·Stack |
| 출력 | Compile/Test/Artifact/Report |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Toolchain·Dependency·Test·Artifact |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | Source 수정 또는 이전 Artifact Mode |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.3 Build Plugin

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/build/gradle-plugin` |
| 입력 | Project Metadata·Gate |
| 출력 | 공통 Task·Manifest |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Plugin Version·Task 실패 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 이전 Plugin Version |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.4 Platform BOM

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/build/platform-bom` |
| 입력 | Stack Version |
| 출력 | Dependency Constraints |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Version Conflict |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 이전 BOM Lock |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.5 DB Initialize

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/initialize-cpf-database.ps1` |
| 입력 | Vendor·접속·Scope |
| 출력 | Schema·Flyway History |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | 권한·Lock·부분 Migration |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | Backup/승인 Rollback·Forward Fix |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.6 Local Start

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/start-cpf-local.ps1` |
| 입력 | Profile·Port·Config |
| 출력 | PID·Log·Process |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Port·Secret·DB/Broker·부분 기동 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 정상 Stop·이전 Config/Artifact |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.7 Local Status

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/status-cpf-local.ps1` |
| 입력 | Runtime Registry/PID |
| 출력 | Process·Health 요약 |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Stale PID·응답 없음 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | Registry 대사·재기동 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.8 Local Stop

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/stop-cpf-local.ps1` |
| 입력 | Grace/Target |
| 출력 | Drain·종료 |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Timeout·진행 Operation |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 강제 종료 전 대사 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.9 OpenAPI Export/Validate

| 항목 | 내용 |
|---|---|
| 정본 경로 | `Product OpenAPI scripts` |
| 입력 | Controller·DTO·Security |
| 출력 | Canonical OpenAPI |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Operation ID/Schema/Permission |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 원본 수정 후 재생성 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.10 Generated Client

| 항목 | 내용 |
|---|---|
| 정본 경로 | `ADM/BZA frontend scripts` |
| 입력 | OpenAPI |
| 출력 | Client·Operation/Route Contract |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | 수동 Diff·Type Error |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 원본/Generator 복원 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.11 Docker Full Install

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/environment/docker-development-test/CPF_도커_개발테스트환경_전체설치.ps1` |
| 입력 | DockerRoot·RepoRoot·Secret |
| 출력 | Image/Container/Lock |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Backend·Image·Compose·Resource |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | CPF 소유 Container/File만 복원 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.12 Docker Incremental

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/environment/docker-development-test/CPF_도커_확장연동환경_증분설치.ps1` |
| 입력 | Base 환경 |
| 출력 | WireMock/SFTP/Vault/Keycloak |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Base 누락·Secret·Image |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 증분 소유 Resource만 제거 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.13 Verification Scripts

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/verify-* / cpf-tools/verification` |
| 입력 | Source·Contract·Environment |
| 출력 | Report·Evidence |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | 누락 환경·Gate 실패 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 원인 수정 후 같은 SHA 재실행 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.14 Supply Chain

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/supply-chain / Trivy / ORT` |
| 입력 | Artifact·SBOM |
| 출력 | Vulnerability/License/Provenance |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | Policy 위반·DB 미갱신 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 승인 예외/Artifact 재생성 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.

### 15.15 Packaging

| 항목 | 내용 |
|---|---|
| 정본 경로 | `cpf-tools/scripts/package-*` |
| 입력 | Base/Head·Manifest·Output |
| 출력 | Root Overlay ZIP |
| Preflight | Repository·Branch·SHA·Working Tree·Tool Version·입력 파일 |
| Dry Run/조회 | 변경 전에 Help·Plan·Diff·Status를 확인 |
| 정상 결과 | Exit 0 + 출력 Manifest/Hash + 해당 Build/DB/Health/업무 판정 |
| 주요 실패 | 경로·Hash·가비지 |
| 재실행 | 최초 실패 상태와 생성 Manifest를 확인하고 같은 입력으로 멱등 재실행 |
| Rollback | 생성 ZIP/Temp 정확히 삭제 |
| 보호 대상 | 기존 Source·다른 작업자 변경·전체 미추적 파일·다른 Docker Project |

사용자는 다음을 기록한다: 실행 SHA, 명령, 인자(Secret 제거), 환경, 시작/종료 시각, Exit Code, 생성·수정·삭제 경로, Report/Artifact Hash, 실패 단계, Rollback 결과.


## 16. 명령 작성 표준

모든 사용자 실행 명령은 한 줄로 제공한다. Repository 절대경로를 변수에 넣고 `git -C`, `Join-Path`, `-LiteralPath`를 사용한다. 현재 폴더를 바꾸는 `Set-Location`에 의존하지 않는다. Secret을 Command Line 인자로 남기지 않고 Secure Prompt 또는 Repository 밖 Secret 파일을 사용한다.

## 17. Tool 사용성 검수 시나리오

| 사용자 | 과제 | 통과 조건 |
|---|---|---|
| 신규 개발자 | Domain Dry Run→Apply→Build | 추가 설명 없이 Manifest와 Artifact를 확인 |
| DBA | 빈 DB→Migration→대사→Rollback/Restore | 3 Vendor 의미와 오류를 판단 |
| Frontend 개발자 | OpenAPI→Client→Route Contract→Build | 수동 Generated 수정 없음 |
| 운영자 | Local/Docker 설치→기동→상태→중지 | Port/Secret/Data 보호 |
| QA | Gate 선택→실행→Evidence | 미실행과 실패를 구분 |
| Release | Artifact→SBOM→Package→Hash | Root Overlay와 Manifest 일치 |

## 18. Tool 오류 보고 예시

| 필드 | 예 |
|---|---|
| Tool | cpf-tools/generator/create-domain.ps1 |
| Source SHA | 54bcc10887a83b933685bff462c0b0d7df824923 |
| Command | Secret 제거 한 줄 명령 |
| Expected | Dry Run에 resolvedStarters 4개 |
| Actual | Provider Binding 허용 목록 오류 |
| First Failure | Profile resolution 단계 |
| Created Files | 없음 또는 Manifest 목록 |
| Protected Changes | 기존 Working Tree 유지 |
| Next Action | Provider 값 수정 후 Dry Run 재실행 |
| Rollback | 적용 전이므로 없음 |
