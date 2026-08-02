# CPF Local Artifact Federation Guide

## 목적

CPF Platform Source와 Generated/Standalone Domain Repository가 분리돼 있어도 동일 CPF version의 public artifact를 사용하도록 한다.

대상 public artifact:

- `com.cpf.core:cpf-core`
- `com.cpf.common:cpf-common`
- `com.cpf.batch:cpf-batch-contract`
- `com.cpf.batch:cpf-batch-testkit`
- `com.cpf.starter:cpf-starter-security`
- `com.cpf.starter:cpf-starter-messaging-kafka`
- `com.cpf.starter:cpf-starter-cache`
- `com.cpf.starter:cpf-starter-observability`
- `com.cpf.starter:cpf-starter-resilience`
- `com.cpf.starter:cpf-starter-featureflag`
- `com.cpf.starter:cpf-starter-secret`
- `com.cpf:cpf-platform-bom`
- `com.cpf.gradle:cpf-gradle-plugin`
- Plugin Marker `com.cpf.platform-conventions:com.cpf.platform-conventions.gradle.plugin`

7개 Starter는 `cpf-starters/`가 소유하는 선택형 Boot AutoConfiguration 제품이다. Artifact Catalog에 각각 등록되고 `com.cpf:cpf-platform-bom`에서 동일 Platform Version으로 제약되며, 소비자는 필요한 Starter만 선택한다.

## Artifact Mode와 Repository 선택

CPF Artifact 공급원은 우선순위 fallback이 아니라 `CPF_ARTIFACT_MODE`로 하나만 선택한다.

| Mode | CPF Artifact 공급원 | 필수 설정 |
|---|---|---|
| `LOCAL_DEV` | `CPF_LOCAL_ARTIFACT_REPOSITORY`, 미지정 시 `${user.home}/.cpf/repository` | 없음 |
| `REMOTE` | 승인된 원격 Registry | `CPF_ARTIFACT_REPOSITORY_URL` |
| `OFFLINE` | 격리된 Offline Maven Repository | `CPF_OFFLINE_ARTIFACT_REPOSITORY` |

Mode를 명시하지 않으면 원격 URL이 있을 때 `REMOTE`, 없으면 `LOCAL_DEV`를 사용한다. `REMOTE`와 `OFFLINE`에서 필요한 공급원이 없으면 즉시 실패하며 개발자 Local Repository로 우회하지 않는다. Maven Central은 `OFFLINE`이 아닐 때 CPF가 아닌 외부 OSS 의존성에만 사용한다.

원격 Registry 인증은 환경변수로만 전달한다.

- `CPF_ARTIFACT_REPOSITORY_USER`
- `CPF_ARTIFACT_REPOSITORY_PASSWORD`

비밀값을 Gradle Script, Generator 출력, Evidence에 기록하지 않는다.

## 로컬 개발 기본 흐름

일반 Root `build`는 기본값으로 Shared Local Maven Repository를 변경하지 않는다. 검증된 Platform Artifact Set을 Local Repository로 동기화할 때는 다음 공식 상위 Task를 사용한다.

명시 실행:

```text
gradlew publishCpfVerifiedLocalPlatformArtifacts --no-daemon --max-workers=1 -PcpfArtifactMode=LOCAL_DEV
gradlew verifyCpfLocalArtifactPropagation --no-daemon --max-workers=1
```

`publishCpfVerifiedLocalPlatformArtifacts`는 Quality Gate, 격리 Staging Publication, POM/Hash/BOM/Plugin 검증 후 Manifest Barrier를 통해 Local Repository로 승격한다. `publishCpfLocalPlatformArtifacts`는 이 절차를 생략하는 Low-level Task이므로 일반 개발 흐름에서 직접 사용하지 않는다.

Root `build` 성공 후 같은 검증 Publication을 자동 실행하려는 `LOCAL_DEV` 편의 흐름에서만 다음 opt-in property를 사용한다. 기본값은 `false`이며 `REMOTE`/`OFFLINE`에서는 실행되지 않는다.

```text
-PcpfAutoLocalArtifactSync=true
```

Root Publication Set은 Core/Common/Batch public artifact, `cpf-starters/` 아래 7개 Starter, `cpf-tools/build/platform-bom`의 BOM, `cpf-tools/build/gradle-plugin`의 Convention Plugin과 Marker를 함께 취급한다. Local/Staging/Internal 흐름에서 같은 제품 집합을 사용한다.

## Generated Domain

`create-domain-repository.ps1`은 원격 CPF Registry가 없고 build를 수행하는 경우 먼저 CPF public artifact를 local repository에 publish한다.

생성된 독립 Repository도 선택된 Artifact Mode의 CPF 공급원 하나만 사용한다. Canonical Build 계약은 Plugin ID `com.cpf.platform-conventions`, Implementation `com.cpf.gradle:cpf-gradle-plugin`, BOM `com.cpf:cpf-platform-bom`이다.

같은 `-SNAPSHOT` version의 직전 cache를 재사용하지 않도록 standalone build는 `--refresh-dependencies`를 사용한다.

## 최종 Artifact 포함 기준

Generated Domain의 compile 성공만으로 완료 처리하지 않는다.

실행 Artifact에 다음을 확인한다.

- bootJar: `BOOT-INF/lib/cpf-core-*.jar`, `cpf-common-*.jar`
- bootWar: `WEB-INF/lib/cpf-core-*.jar`, `cpf-common-*.jar`
- Batch/Center-Cut capability 사용 시 `cpf-batch-contract-*.jar`

Generated Domain의 기본 `verifyCpfPackagedDependencies`가 위 내용을 검사한다. Starter를 선택한 독립 Consumer는 Archive/Dependency Graph Gate에서도 선택한 `cpf-starter-*.jar`의 포함 여부를 확인한다.

## 운영/상용 배포

상용 환경에서는 local repository에 의존하지 않고 승인된 Artifact Registry를 기본으로 사용한다.
BOM으로 version을 고정하고 Convention Plugin으로 Java/Repository/Archive 규칙을 공유한다.

배포 Artifact는 Source tree에서 임의 JAR 복사로 조립하지 않는다. Maven coordinate 기반 dependency resolution과 bootJar/bootWar packaging Gate를 사용한다.

## 완료 판정

다음이 모두 확인돼야 한다.

1. CPF public artifact publish 성공
2. local/remote repository에서 동일 platform version 해석
3. Generated Domain 독립 clean build
4. bootJar/bootWar dependency 포함
5. 독립 Repository 삭제/재생성 후 동일 결과
6. 민감정보가 로그/Evidence에 미노출
