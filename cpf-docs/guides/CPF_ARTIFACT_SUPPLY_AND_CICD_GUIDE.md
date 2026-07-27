# CPF Artifact 공급·CI/CD 운영 가이드

## 1. 목적

CPF의 `cpf-core`, `cpf-common`, BAT Public Contract/Testkit, Platform BOM, Domain Convention Plugin은
업무 Domain이 임의로 JAR를 복사해서 사용하는 파일 모음이 아니라 **버전·Source Commit·Hash가 추적되는 Public Platform Artifact Set**이다.

이 문서는 개발자 Local, 사내 Nexus/Artifactory가 있는 CI/CD, Registry가 없는 Offline 환경에서 동일한 의존성 계약을 사용하는 방법을 정의한다.

## 2. 기본 원칙

1. 사람이 `cpf-core.jar`, `cpf-common.jar`를 업무 Domain의 `lib`에 수동 복사하지 않는다.
2. 같은 Repository의 Module은 Gradle Project Dependency를 사용한다.
3. 독립 Repository/독립 WAS는 Maven Artifact를 사용한다.
4. `REMOTE`에서는 개발자 Local Repository로 fallback하지 않는다.
5. `OFFLINE`은 임의 JAR 폴더가 아니라 CPF가 생성한 검증 Bundle을 사용한다.
6. Release Artifact는 immutable이 원칙이다. Mutable SNAPSHOT은 Local 개발 범위로 제한한다.
7. Artifact Publication은 Quality Gate와 Artifact identity/hash 검증을 통과한 뒤에만 Consumer Repository에 노출한다.

## 3. Artifact Mode

Artifact Mode는 Gradle property가 환경변수보다 우선한다.

| 모드 | 용도 | 공급원 | Local fallback |
|---|---|---|---|
| `LOCAL_DEV` | 개발자 PC, 독립 Generated Domain 로컬 개발 | `${user.home}/.cpf/repository` 또는 지정 경로 | 해당 없음 |
| `REMOTE` | Jenkins/CI/STG/PROD | Nexus/Artifactory 등 `-PcpfArtifactRepositoryUrl` 또는 `CPF_ARTIFACT_REPOSITORY_URL` | 금지 |
| `OFFLINE` | 원격 Registry가 없는 폐쇄망 서버/빌드 | CPF Offline Artifact Bundle의 `repository` | 금지 |

설정 우선순위:

```text
-PcpfArtifactMode
→ CPF_ARTIFACT_MODE
→ `-PcpfArtifactRepositoryUrl` 또는 `CPF_ARTIFACT_REPOSITORY_URL`이 있으면 REMOTE
→ LOCAL_DEV
```

Repository 경로 우선순위:

```text
LOCAL_DEV : -PcpfLocalArtifactRepository → CPF_LOCAL_ARTIFACT_REPOSITORY → ~/.cpf/repository
OFFLINE   : -PcpfOfflineArtifactRepository → CPF_OFFLINE_ARTIFACT_REPOSITORY
REMOTE    : -PcpfArtifactRepositoryUrl → CPF_ARTIFACT_REPOSITORY_URL
```

## 4. 개발자 Local — 같은 CPF Repository

`cpf-account`, `cpf-member`처럼 CPF Source Tree 안에 있는 Module은 Project Dependency를 사용한다.

```text
cpf-core 수정
   ↓
cpf-common 수정
   ↓
./gradlew :cpf-account:bootJar
   ↓
Gradle가 변경된 Project Dependency를 자동 Compile
   ↓
ACC bootJar의 BOOT-INF/lib에 필요한 CPF JAR 포함
```

개발자는 별도 JAR 복사를 하지 않는다.

## 5. 개발자 Local — 독립 Generated Domain

독립 Repository는 Project Dependency를 사용할 수 없으므로 검증된 Shared Local Maven Repository를 사용한다.

### 5.1 명시 Publish

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts --no-daemon --max-workers=1 `
  -PcpfArtifactMode=LOCAL_DEV
```

처리 순서:

```text
aggregateQualityBuild
→ 격리 staging repository publish
→ POM / Gradle module metadata / BOM / Plugin Marker / SHA-256 검증
→ publisher lock
→ 기존 promoted manifest 제거
→ coordinate/version 단위 교체
→ PROMOTED manifest를 마지막에 공개
→ promoted repository 재검증
```

Quality Gate가 실패하면 Shared Local Repository를 갱신하지 않는다.

### 5.2 자동 Sync

자동 Sync는 기본 `false`다.

```powershell
.\gradlew.bat build -PcpfAutoLocalArtifactSync=true -PcpfArtifactMode=LOCAL_DEV
```

일반 개발에서는 필요할 때 명시 Publish를 권장한다. 자동 Sync는 개발 편의 기능이며 CI/STG/PROD에서 사용하지 않는다.

### 5.3 Local Repository 변경

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts `
  -PcpfArtifactMode=LOCAL_DEV `
  -PcpfLocalArtifactRepository=C:\cpf-cache\repository
```

환경변수 방식:

```powershell
$env:CPF_LOCAL_ARTIFACT_REPOSITORY='C:\cpf-cache\repository'
```

### 5.4 재사용 최적화

`create-domain-repository.ps1`는 Local Repository의 `PROMOTED` manifest와 현재 Git HEAD가 일치하면
고비용 `aggregateQualityBuild`를 반복하지 않고 기존 검증 Artifact를 재사용한다.
현재 Source Commit과 manifest가 다르면 검증 Publish를 다시 수행한다.

## 6. REMOTE — Jenkins / CI/CD

서버 Build는 개발자 PC의 `~/.cpf/repository`를 사용하면 안 된다.

### 6.1 Platform Pipeline

```text
Git checkout CPF
→ Java/Frontend/Test/Gate
→ Platform Artifact 생성
→ Artifact 검증
→ 승인 Registry publish
→ Release/Promotion
```

환경:

```powershell
$env:CPF_ARTIFACT_MODE='REMOTE'
$env:CPF_ARTIFACT_REPOSITORY_URL='https://nexus.example/repository/cpf-releases/'
$env:CPF_ARTIFACT_REPOSITORY_USER='...'
$env:CPF_ARTIFACT_REPOSITORY_PASSWORD='...'
```

Publish:

```powershell
.\gradlew.bat publishCpfPlatformArtifacts --no-daemon --max-workers=1 -PcpfArtifactMode=REMOTE
```

`publishCpfPlatformArtifacts`는 `cpfInternal` Repository 전용 Task만 호출한다.


### 범용 `publish` Task 사용 금지

CPF 제품 배포/공급 절차에서는 Gradle이 자동 생성하는 범용 `publish` 또는 BAT의 과거 `publishStandaloneArtifacts`를 공식 Entry로 사용하지 않는다.
Repository 대상이 여러 개일 때 범용 Task는 의도하지 않은 Local/Staging/Remote Side Effect를 만들 수 있기 때문이다.

공식 Entry는 목적별 Task만 사용한다.

- Local 개발: `publishCpfVerifiedLocalPlatformArtifacts`
- Local 검증: `verifyCpfLocalArtifactPropagation`
- Remote CI/CD: `publishCpfPlatformArtifacts`
- Offline: `buildCpfOfflineArtifactBundle`

BAT의 `publishStandaloneArtifacts`는 모호한 Legacy Entry로 차단하며, 내부 하위 Task는 `...ToCpfLocal`, `...ToCpfStaging`, `...ToCpfInternal`처럼 대상을 명시한다.
Remote URL이 없으면 fail-closed하며 Local Repository로 우회하지 않는다.

### 6.2 업무 Domain Pipeline

```text
Git checkout ACC
→ ACC가 요구하는 CPF Platform Version 확인
→ REMOTE Registry에서 CPF BOM/Core/Common 다운로드
→ Compile/Test
→ bootJar/bootWar
→ CPF dependency version/package 검증
→ 배포
```

ACC 배포 시 CPF Git의 최신 Source를 임의로 다시 Compile해서 섞지 않는다.
검증된 Platform Version을 명시적으로 변경할 때만 ACC가 새 CPF Artifact를 사용한다.

## 7. OFFLINE — Nexus/Artifactory 없는 환경

먼저 검증된 Local Artifact Set으로 Offline Bundle을 만든다.

```powershell
.\gradlew.bat buildCpfOfflineArtifactBundle -PcpfArtifactMode=LOCAL_DEV
```

기본 산출물:

```text
build/cpf-offline/cpf-offline-artifacts-<version>.zip
  repository/
    com/cpf/...
    _cpf/manifests/<version>.json
  metadata/
    cpf-platform.properties
    cpf-stack.properties
  README.txt
  SHA256SUMS.txt
```

서버에서 압축 해제 후:

```powershell
$env:CPF_ARTIFACT_MODE='OFFLINE'
$env:CPF_OFFLINE_ARTIFACT_REPOSITORY='D:\cpf-artifacts\repository'
```

이후 ACC/Generated Domain의 Gradle Build가 Bundle의 Maven Repository를 사용한다.
JAR를 개별 `lib` 폴더로 수동 복사하지 않는다.

## 8. bootJar / bootWar

최종 실행물에는 Gradle Dependency Resolution 결과가 자동 포함된다.

```text
bootJar
  BOOT-INF/lib/cpf-core-<version>.jar
  BOOT-INF/lib/cpf-common-<version>.jar

bootWar
  WEB-INF/lib/cpf-core-<version>.jar
  WEB-INF/lib/cpf-common-<version>.jar
```

Generated Domain은 `verifyCpfPackagedDependencies`를 통해 필요한 CPF Library 포함 여부를 검증한다.
향후 Release Gate에서는 파일명 Prefix뿐 아니라 Artifact Manifest의 exact version/hash와 package 내부 JAR hash까지 연결한다.

## 9. 주요 Gradle Task

| Task | 분류 | 용도 | Repository 변경 |
|---|---|---|---|
| `aggregateQualityBuild` | `CI_RELEASE` | Artifact 공개 전 Compile/Test/Frontend/Gate/Assemble 집계 | 없음 |
| `publishCpfLocalPlatformArtifacts` | 내부 저수준 | 지정 Local Repository에 직접 publish | 있음. 직접 사용 비권장 |
| `publishCpfStagingPlatformArtifacts` | 내부 저수준 | 격리 staging에만 publish | build staging만 변경 |
| `publishCpfVerifiedLocalPlatformArtifacts` | `DEV_ONLY` | Quality + staging + 검증 + Local promotion | Shared Local 변경 |
| `verifyCpfLocalArtifactPropagation` | `DEV_ONLY` | PROMOTED manifest/hash/POM/BOM/plugin 검증 | 없음 |
| `buildCpfOfflineArtifactBundle` | `PRODUCT_ADMIN_TOOL/CI_RELEASE` | 폐쇄망 전달용 Offline Maven Bundle 생성 | build 산출물 생성 |
| `publishCpfPlatformArtifacts` | `CI_RELEASE` | Remote `cpfInternal` Registry 전용 publish | Remote Registry 변경 |
| `checkCpfStackSupport` | `QUICK/CI_RELEASE` | Stack 정본·지원 상태·중복 Version 검증 | 없음 |

## 10. Manifest

Local/Offline Artifact Set은 최소 다음을 기록한다.

- Platform Version
- Source Commit SHA / dirty sourceFingerprint
- Java Version
- Gradle Version
- Spring Boot Version
- 생성 시각
- Artifact coordinate/path
- SHA-256
- Promotion State

`PROMOTED`가 아닌 manifest는 Local/Offline Consumer의 정상 공급원으로 사용하지 않는다.

## 11. 실패 시 동작

| 실패 | 기대 동작 |
|---|---|
| Core/Common Compile 실패 | Shared Local/Remote 공개 금지 |
| Unit/Frontend/Gate 실패 | Shared Local promotion 금지 |
| Staging POM/Hash 불일치 | Promotion 금지 |
| Promotion 중 실패 | 이전 version 디렉터리/manifest rollback |
| REMOTE URL 없음 | 즉시 실패, Local fallback 금지 |
| OFFLINE manifest 없음 | Standalone Build 실패 |
| Local manifest Source SHA 불일치 | 재사용 금지, 검증 Publish 필요 |

## 12. 아직 미검증/후속

현재 Source 구현만으로 다음을 PASS 처리하지 않는다.

- Windows에서 동시 Publisher/Consumer promotion Fault Test
- 실제 Nexus/Artifactory staging/promotion/409/timeout/authentication
- Immutable Release/Snapshot Repository 정책 실증
- bootJar/bootWar 내부 CPF JAR exact hash 검증
- Signature/SBOM/CVE/Provenance Release 통합
- CI Branch Protection Required Check

위 항목은 Change Impact Ledger에서 미검증/후속으로 유지한다.


### 검증 대상 CPF Platform Artifact Set

현재 verified publication은 다음 Version Set을 하나의 Promotion 단위로 검사한다.

- `com.cpf.core:cpf-core`
- `com.cpf.common:cpf-common`
- `com.cpf.batch:cpf-batch-contract`
- `com.cpf.batch:cpf-batch-runtime-common`
- `com.cpf.batch:cpf-batch-testkit`
- `com.cpf.batch:cpf-batch-control-server`
- `com.cpf.batch:cpf-batch-scheduler`
- `com.cpf.batch:cpf-batch-worker`
- `com.cpf.batch:cpf-center-cut-runner`
- `com.cpf.batch:cpf-batch-host-agent`
- `com.cpf:cpf-bom`
- `com.cpf.build:cpf-gradle-plugin`
- `com.cpf.domain-conventions:com.cpf.domain-conventions.gradle.plugin`

각 coordinate/version directory의 POM/JAR/module metadata/sources/Javadoc 등 실제 파일 전체를 Hash Manifest에 포함한다.
Manifest 밖 동일 Version directory가 staging에 존재하면 promotion을 중단한다.

### Manifest와 Working Tree

Local 개발에서는 Commit하지 않은 Core/Common 변경도 테스트할 수 있으므로 `sourceCommit`만으로 Artifact 재사용 여부를 판단하면 안 된다.
Verified Manifest는 `sourceCommit`, `sourceDirty`, `sourceFingerprint`를 함께 기록한다.

- clean tree: fingerprint는 Commit SHA와 동일
- dirty tree: tracked diff와 untracked source hash를 포함한 fingerprint 사용
- Generator가 기존 Local Artifact를 재사용할 때 현재 sourceFingerprint와 Manifest가 다르면 재사용하지 않고 다시 검증·publish한다.

또한 promotion은 staging에 존재하는 모든 디렉터리를 무조건 옮기지 않는다.
Manifest에 검증된 coordinate/version 디렉터리만 promotion하며 Manifest 밖 동일-version Artifact가 발견되면 실패한다.


### Generator 독립 Repository Artifact 옵션

`create-domain-repository.ps1`은 다음 우선순위를 사용한다.

- Local: `-LocalArtifactRepository` → `CPF_LOCAL_ARTIFACT_REPOSITORY` → `${user.home}/.cpf/repository`
- Remote: `-RemoteArtifactRepository` → `CPF_ARTIFACT_REPOSITORY_URL`
- Offline: `-OfflineArtifactRepository` → `CPF_OFFLINE_ARTIFACT_REPOSITORY`

Generated Gradle Repository 내부에서는 동일 의미를 Gradle Property → Environment 순으로 해석한다.

- `-PcpfLocalArtifactRepository` → `CPF_LOCAL_ARTIFACT_REPOSITORY`
- `-PcpfArtifactRepositoryUrl` → `CPF_ARTIFACT_REPOSITORY_URL`
- `-PcpfOfflineArtifactRepository` → `CPF_OFFLINE_ARTIFACT_REPOSITORY`
