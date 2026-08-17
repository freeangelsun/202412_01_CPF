# CPF Starter Architecture and Lifecycle Policy

- Currentization source/basis SHA: `d50b8468094a412923ab4a3d63013216eeb88e31` (`10_13`)
- 본 정책은 기존 `cpf-common` 독립 Domain, `cpf-starter-profile-*` 공개 명칭, Capability별 `*/core` 구조를 **명시적으로 대체**한다.

## 1. 기본 원칙

CPF 개발자의 진입 모델은 **Starter 하나를 선택하고 필요한 Capability/Provider를 추가하는 방식**으로 단일화한다. Source Owner와 Runtime 조립 Owner를 분리한다.

- `cpf-core` = 최소 Kernel. Starter를 모른다.
- `base` = Stateless Framework Foundation/DX/기본 Runtime.
- `common` = Stateful CPF Common Product Services.
- Capability = Web/Data/Messaging/Integration/File/Notification/Security/Platform Operations.
- Provider = Redis/Valkey/JDBC/JPA/Kafka 등 기술 Adapter.
- Profile = 한 Deployable의 대표 use-case 조합. **Deployable당 exactly-one Top-level Profile**.

## 2. Target Physical Layout

```text
cpf-starters/
├─ base/
├─ common/
├─ web/
├─ data/
│  ├─ persistence/
│  │  ├─ jdbc/
│  │  ├─ mybatis/
│  │  └─ jpa/
│  ├─ cache/
│  │  ├─ spring-data-redis/   # internal shared runtime
│  │  ├─ caffeine/
│  │  ├─ redis/
│  │  └─ valkey/
│  └─ lock/
├─ messaging/
├─ integration/
├─ file/
├─ notification/
├─ security/
├─ platform-operations/
└─ profiles/
```

`10_06` 기준 `cpf-starters/foundation` 물리 Root는 이미 제거되어 있다. `cpf-core` 외 `*/core` Module/Directory 및 `foundation` Owner Group의 재도입을 허용하지 않는다. 역할은 `api`, `spi`, `runtime`, `provider`, `autoconfigure`, `internal`처럼 실제 책임으로 명명한다.

## 3. Base와 Common

### Base

`cpf-starters/base`는 모든 CPF Application의 가벼운 Foundation이다. Context Runtime, 표준 Validation, Logging/Masking hook, Base Service, 기본 Annotation/DX, Config diagnostics를 소유한다. DB/Kafka/Redis/Valkey/S3/OIDC 등을 무조건 활성화하지 않는다.

`10_06`에서 Pure Foundation의 물리 Owner는 `cpf-starters/base`로 currentize되어 있고 `cpf-starters/foundation/**`은 존재하지 않는다. 이후 Gate는 이관 작업을 반복하지 않고 **foundation 물리 Root/ownerGroup 재등장 0**을 검증한다. `com.cpf.foundation.*` Java Namespace를 유지할 경우에는 Base가 소유하는 Public API Namespace임을 Catalog/JavaDoc/Generator에서 일관되게 선언하고, 물리 `foundation` Module 재생성 근거로 사용하지 않는다.

### Common

`10_06` 기준 독립 Root `cpf-common`은 제거되어 있고 `cpf-starters/common`이 CPF Common Product Service를 소유한다. 이후 Gate는 `cpf-common` 재등장 0과 Common Consumer/DB Runtime 정합성을 검증한다.

- code/reference-data
- parameter
- message/error-message/response-code catalog
- calendar
- template

Common은 고객 Application이 직접 사용하는 Public Capability다. 고객별 공통 업무는 CPF Source가 아니라 `<customer>-common`이 소유한다.

Common Full Runtime은 DB를 사용한다. `cpf-starter-common`은 CPF Data JDBC 기반을 필수 조립하고 `cpfDB`의 `CMN_*` Table을 사용한다. MyBatis/JPA는 Business Domain 선택 확장으로 JDBC 기반과 공존 가능하다. DB 부재를 Memory 성공으로 조용히 대체하지 않는다. 명시적으로 정의된 DB-less fallback만 예외다.

## 4. 개발자 공개 Starter 명칭

`10_06` Catalog의 Public Profile/Provider 이름은 one-shot currentization이 반영된 상태다. 구 `cpf-starter-profile-*`는 active coordinate로 재등장시키지 않으며 `removedArtifactIds`/negative gate/history에서만 허용한다.

### 기본/Profile 공개명

| 현재 공개명 | Target 공개명 |
|---|---|
| `cpf-starter` | `cpf-starter` |
| 신규 | `cpf-starter-common` |
| `cpf-starter-profile-web-api` | `cpf-starter-web-api` |
| `cpf-starter-profile-secure-api` | `cpf-starter-secure-api` |
| `cpf-starter-profile-browser-bff` | `cpf-starter-bff` |
| `cpf-starter-profile-event-service` | `cpf-starter-event` |
| `cpf-starter-profile-batch-service` | `cpf-starter-batch` |

### 공개 Provider 명명 규칙

개발자가 명시적으로 선택해야 하는 Provider는 `cpf-starter-<기능>-<provider>`를 사용한다.

- `cpf-starter-data-jdbc`
- `cpf-starter-data-mybatis`
- `cpf-starter-data-jpa`
- `cpf-starter-cache-caffeine`
- `cpf-starter-cache-redis`
- `cpf-starter-cache-valkey`
- `cpf-starter-lock-valkey`
- `cpf-starter-session-jdbc`
- `cpf-starter-session-valkey`
- `cpf-starter-messaging-kafka`
- `cpf-starter-messaging-rabbitmq`
- `cpf-starter-messaging-jms`
- `cpf-starter-messaging-ibm-mq`
- `cpf-starter-object-storage-s3`
- `cpf-starter-graphql`
- `cpf-starter-realtime`
- `cpf-starter-oidc`

Internal shared leaf는 Public BOM에 노출하지 않는다. 예: `data/cache/spring-data-redis`는 Redis/Valkey 공통 구현이지만 개발자가 직접 선택하는 Artifact가 아니다.

## 5. Public/Profile/Provider 규칙

- `cpf-starter` = 기본 개발 진입점. Base + Common + Common 구동에 필요한 Data JDBC 기반을 조립한다.
- `cpf-starter-common` = Common 기능을 명시적으로 사용할 때의 Public Entry.
- Profile은 Base/Common과 필요한 Capability를 조립한다.
- 하나의 Deployable에서 Top-level Profile을 둘 이상 직접 의존하지 않는다.
- `secure-api`가 Web 기능을 필요로 하면 `secure-api` Profile 내부에서 Web Capability를 조립한다. Application이 `web-api + secure-api`를 동시에 추가하지 않는다.
- Generated Domain은 Public Starter/Profile/Provider만 직접 참조한다. Internal Leaf 직접 dependency는 Build Gate에서 실패한다.
- `currentization_source_sha` Catalog에서 `cpf-starter-integration-http`, `cpf-starter-integration-resilience`는 Internal-only leaf다. Generated Domain이 직접 참조하지 않는다. 필요한 HTTP/Resilience 기능은 Public Profile/Composition이 내부에서 조립한다.
- 필요한 Capability의 Public Composition이 없으면 Generated Domain에 Internal dependency를 넣지 말고 Starter Framework 쪽 Public Surface를 구현·Catalog/BOM/Publication/Test까지 완성한다.
- Provider 충돌은 Fail-Fast한다. Redis/Valkey는 명시 선택하며 자동 추측하지 않는다.

## 6. Web Capability

`10_06` 기준 `cpf-starters/web` 물리 Capability가 존재한다. Controller Base, Controller Annotation, HTTP Context/Header, Web Error Mapping, DTO Web Validation integration의 Owner는 Web이며 Profile은 이를 조립만 한다. `profiles/web-api`를 구현 Owner로 되돌리지 않는다.

## 7. Redis/Valkey

Redis는 공식 Provider이며 Valkey는 계속 지원한다.

```text
data/cache/spring-data-redis  -> 공통 Spring Data Redis protocol/runtime (internal)
data/cache/redis              -> Redis provider
data/cache/valkey             -> Valkey provider
```

두 Provider가 Cache semantics를 복제하지 않는다. Connection, TTL, Serialization, Invalidation, Reconnect, Provider collision, Multi-instance를 검증한다. Source 존재를 Runtime PASS로 승계하지 않으며 Live Redis/Valkey outage/reconnect가 미실행이면 해당 Runtime만 `미검증`으로 유지한다.

## 8. Starter 완료 Gate

Starter는 다음이 모두 연결돼야 완료다.

Catalog → Physical Path → settings.gradle → build.gradle → BOM/Publication → AutoConfiguration Metadata → Config → Generator/Profile → Generated Consumer → Reference/EDU → Unit/Integration/Runtime Test → Evidence.

폴더/Artifact/Metadata 하나만 존재하면 완료가 아니다. Duplicate Artifact ID, Internal Public 노출, profile 중복 조합, 미선택 Provider footprint, stale predecessor FQCN은 0이어야 한다.


## Generated Domain Public Starter 경계

Generated Customer Domain이 직접 의존할 수 있는 Artifact는 **최신 Canonical Starter Catalog에서 `visibility=public`인 Public Profile/Provider/Entry만**이다.

`currentization_source_sha`의 Catalog 기준 대표 직접 참조 가능 Artifact:

```text
cpf-starter-secure-api
cpf-starter-batch
cpf-starter-data-mybatis
```

반면 다음은 해당 Catalog에서 Internal-only leaf이므로 Generated Domain 직접 dependency로 생성하지 않는다.

```text
cpf-starter-integration-http
cpf-starter-integration-resilience
```

HTTP/Resilience 기능은 Public Profile/Composition 내부에서 Internal leaf를 소비하도록 구성한다. 필요한 Capability가 현재 Public Surface에 노출되어 있지 않다면 Generated Domain이 Internal 경계를 뚫는 방식으로 우회하지 않는다. **Starter Framework 측 Public Composition/Profile/Provider Gap을 먼저 구현·Catalog/BOM/Publication/Test와 함께 닫은 뒤 Generated Domain이 그 Public Surface를 소비**해야 한다.

Generator Template/Generated build.gradle의 Internal Artifact 직접 참조는 Gate 실패다.

## Currentization SHA 의미

- `currentization_source_sha`: `d50b8468094a412923ab4a3d63013216eeb88e31` (`10_13`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.

## Current-State 관리 Application과 업무 Domain Runtime 경계

ADM(`cpf-admin`), BZA(`cpf-biz-admin`), Gateway는 생성형 업무 Domain의 Online Transaction Runtime이 아니다. 자체 관리 API에는 `@CpfOnlineTransaction`, CPF 표준 거래 Header 6개, 업무 Operation Caller/Target 통제를 강제하지 않는다. 이들 Application은 Spring Web/Security/Validation/OpenAPI와 각 Owner Module의 Public Starter/API를 사용한다.

관리 Application Source가 `cpf-core` internal 구현이나 업무 Domain internal package를 직접 import하는 것은 금지한다. Public Starter가 내부적으로 topology-independent `cpf-core` 계약을 transitive하게 소비하거나, 정당한 `cpf-core` Public Contract를 직접 소비하는 것은 허용한다.

관리 API가 실제 업무 Domain Operation을 호출할 때는 관리 Controller 자체를 업무 거래로 바꾸지 않는다. `관리 Controller -> Domain Client/Public API -> 업무 Domain Operation`의 outbound 경계부터 CPF Domain Client가 거래 Context를 생성·전파하며, 원격 호출은 Framework가 표준 거래 Header를 serialize하고 동일 JVM은 논리 Context로 전달한다.

Fixed Length/Webhook/HTTP 등 업무 개발자가 사용하는 기능은 **Public Contract/API와 Internal/Provider 구현을 분리**한다. 업무 Domain과 EDU는 Public 계약만 의존하며 Internal Starter/package를 직접 import하지 않는다.

