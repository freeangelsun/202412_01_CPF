# CPF Local Artifact Federation Guide

## 목적

CPF Platform Source와 Generated/Standalone Domain Repository가 분리돼 있어도 동일 CPF version의 public artifact를 사용하도록 한다.

대상 public artifact:

- `com.cpf.core:cpf-core`
- `com.cpf.common:cpf-common`
- `com.cpf.batch:cpf-batch-contract`
- `com.cpf.batch:cpf-batch-testkit`
- `com.cpf:cpf-bom`
- `com.cpf.build:cpf-gradle-plugin`

## Repository 우선순위

1. `CPF_ARTIFACT_REPOSITORY_URL` 원격 Registry
2. `CPF_LOCAL_ARTIFACT_REPOSITORY`
3. `${user.home}/.cpf/repository`
4. Maven Central은 CPF가 아닌 외부 OSS 의존성용 fallback

원격 Registry 인증은 환경변수로만 전달한다.

- `CPF_ARTIFACT_REPOSITORY_USER`
- `CPF_ARTIFACT_REPOSITORY_PASSWORD`

비밀값을 Gradle Script, Generator 출력, Evidence에 기록하지 않는다.

## 로컬 개발 기본 흐름

CPF Root의 일반 `build`가 성공하면 원격 Registry가 없는 기본 로컬 환경에서는 별도 publish invocation이 이어져 public artifact가 shared local Maven repository에 동기화된다. 실패한 build에서는 shared repository publish를 실행하지 않는다.

명시 실행:

```text
gradlew publishCpfLocalPlatformArtifacts --no-daemon --max-workers=1
gradlew verifyCpfLocalArtifactPropagation --no-daemon --max-workers=1
```

특수 CI/검증에서 자동 동기화를 끌 때만 다음 property를 사용한다.

```text
-PcpfAutoLocalArtifactSync=false
```

## Generated Domain

`create-domain-repository.ps1`은 원격 CPF Registry가 없고 build를 수행하는 경우 먼저 CPF public artifact를 local repository에 publish한다.

생성된 독립 Repository는 remote → local → Maven Central 순서로 dependency/plugin을 해석한다.

같은 `-SNAPSHOT` version의 직전 cache를 재사용하지 않도록 standalone build는 `--refresh-dependencies`를 사용한다.

## 최종 Artifact 포함 기준

Generated Domain의 compile 성공만으로 완료 처리하지 않는다.

실행 Artifact에 다음을 확인한다.

- bootJar: `BOOT-INF/lib/cpf-core-*.jar`, `cpf-common-*.jar`
- bootWar: `WEB-INF/lib/cpf-core-*.jar`, `cpf-common-*.jar`
- Batch/Center-Cut capability 사용 시 `cpf-batch-contract-*.jar`

Generated Domain의 `verifyCpfPackagedDependencies`가 위 내용을 검사한다.

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
