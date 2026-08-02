# CPF Starters 매뉴얼 — Capability Profile과 Leaf Starter를 선택·적용·제거·검증하는 절차

> **주 독자**: 업무 개발자, 아키텍트, 빌드·플랫폼 운영자, 검수자
> **완료 결과**: 업무 요구를 13개 Profile 또는 38개 Leaf·Aggregate 실행 모듈로 해석하고, 선택·설정·실행·제거·Rollback을 끝낸다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. Starter를 사용하는 이유](#1-starter를-사용하는-이유)
- [2. 선택 절차](#2-선택-절차)
- [3. 13개 Versioned Capability Profile](#3-13개-versioned-capability-profile)
  - [3.1 Provider Binding](#31-provider-binding)
- [4. 38개 Leaf·Aggregate 실행 모듈 Catalog](#4-38개-leafaggregate-실행-모듈-catalog)
- [5. Build 적용](#5-build-적용)
  - [5.1 Root Build](#51-root-build)
  - [5.2 게시 Artifact](#52-게시-artifact)
- [6. Generator 적용 한 줄 명령](#6-generator-적용-한-줄-명령)
- [7. Starter 공통 완료 Gate](#7-starter-공통-완료-gate)
- [8. Base·Profile Binding](#8-baseprofile-binding)
  - [8.1 적용 절차](#81-적용-절차)
  - [8.2 정상 결과](#82-정상-결과)
- [9. Persistence JDBC·MyBatis](#9-persistence-jdbcmybatis)
  - [9.1 적용 절차](#91-적용-절차)
  - [9.2 정상 결과](#92-정상-결과)
- [10. Security Resource Server·Session·Service Identity](#10-security-resource-serversessionservice-identity)
  - [10.1 적용 절차](#101-적용-절차)
  - [10.2 정상 결과](#102-정상-결과)
- [11. Messaging Reliability·Kafka·RabbitMQ·JMS·IBM MQ](#11-messaging-reliabilitykafkarabbitmqjmsibm-mq)
  - [11.1 적용 절차](#111-적용-절차)
  - [11.2 정상 결과](#112-정상-결과)
- [12. Cache Caffeine·Valkey](#12-cache-caffeinevalkey)
  - [12.1 적용 절차](#121-적용-절차)
  - [12.2 정상 결과](#122-정상-결과)
- [13. Observability·OTLP](#13-observabilityotlp)
  - [13.1 적용 절차](#131-적용-절차)
  - [13.2 정상 결과](#132-정상-결과)
- [14. HTTP Client·Resilience](#14-http-clientresilience)
  - [14.1 적용 절차](#141-적용-절차)
  - [14.2 정상 결과](#142-정상-결과)
- [15. SFTP·File Archive·Attachment·Tabular](#15-sftpfile-archiveattachmenttabular)
  - [15.1 적용 절차](#151-적용-절차)
  - [15.2 정상 결과](#152-정상-결과)
- [16. TCP·Fixed Length·ISO8583](#16-tcpfixed-lengthiso8583)
  - [16.1 적용 절차](#161-적용-절차)
  - [16.2 정상 결과](#162-정상-결과)
- [17. Notification·Email·SMS SPI](#17-notificationemailsms-spi)
  - [17.1 적용 절차](#171-적용-절차)
  - [17.2 정상 결과](#172-정상-결과)
- [18. Scheduler Quartz](#18-scheduler-quartz)
  - [18.1 적용 절차](#181-적용-절차)
  - [18.2 정상 결과](#182-정상-결과)
- [19. Feature Flag·Secret](#19-feature-flagsecret)
  - [19.1 적용 절차](#191-적용-절차)
  - [19.2 정상 결과](#192-정상-결과)
- [20. Source-backed Property 표](#20-source-backed-property-표)
- [21. Removal·Provider 교체 절차](#21-removalprovider-교체-절차)
- [22. 완료 체크리스트](#22-완료-체크리스트)
- [23. Starter별 적용·검증·제거 Reference](#23-starter별-적용검증제거-reference)
  - [23.1 `cpf-starter-base`](#231-cpf-starter-base)
  - [23.2 `cpf-starter-persistence-jdbc`](#232-cpf-starter-persistence-jdbc)
  - [23.3 `cpf-starter-persistence-mybatis`](#233-cpf-starter-persistence-mybatis)
  - [23.4 `cpf-starter-aop-service-access`](#234-cpf-starter-aop-service-access)
  - [23.5 `cpf-starter-openapi-webmvc`](#235-cpf-starter-openapi-webmvc)
  - [23.6 `cpf-starter-security-resource-server`](#236-cpf-starter-security-resource-server)
  - [23.7 `cpf-starter-security-session-jdbc`](#237-cpf-starter-security-session-jdbc)
  - [23.8 `cpf-starter-security-service-identity`](#238-cpf-starter-security-service-identity)
  - [23.9 `cpf-starter-security`](#239-cpf-starter-security)
  - [23.10 `cpf-starter-messaging-reliability-jdbc`](#2310-cpf-starter-messaging-reliability-jdbc)
  - [23.11 `cpf-starter-channel-registry-jdbc`](#2311-cpf-starter-channel-registry-jdbc)
  - [23.12 `cpf-starter-cache-caffeine`](#2312-cpf-starter-cache-caffeine)
  - [23.13 `cpf-starter-cache-valkey`](#2313-cpf-starter-cache-valkey)
  - [23.14 `cpf-starter-cache`](#2314-cpf-starter-cache)
  - [23.15 `cpf-starter-observability`](#2315-cpf-starter-observability)
  - [23.16 `cpf-starter-observability-otlp`](#2316-cpf-starter-observability-otlp)
  - [23.17 `cpf-starter-http-client`](#2317-cpf-starter-http-client)
  - [23.18 `cpf-integration-fixedlength-core`](#2318-cpf-integration-fixedlength-core)
  - [23.19 `cpf-starter-integration-fixedlength`](#2319-cpf-starter-integration-fixedlength)
  - [23.20 `cpf-starter-integration-sftp`](#2320-cpf-starter-integration-sftp)
  - [23.21 `cpf-starter-tabular-poi`](#2321-cpf-starter-tabular-poi)
  - [23.22 `cpf-starter-file-archive`](#2322-cpf-starter-file-archive)
  - [23.23 `cpf-starter-validation`](#2323-cpf-starter-validation)
  - [23.24 `cpf-starter-attachment`](#2324-cpf-starter-attachment)
  - [23.25 `cpf-starter-runtime-control-client`](#2325-cpf-starter-runtime-control-client)
  - [23.26 `cpf-starter-messaging-kafka`](#2326-cpf-starter-messaging-kafka)
  - [23.27 `cpf-starter-messaging-rabbitmq`](#2327-cpf-starter-messaging-rabbitmq)
  - [23.28 `cpf-starter-messaging-jms`](#2328-cpf-starter-messaging-jms)
  - [23.29 `cpf-starter-messaging-ibm-mq`](#2329-cpf-starter-messaging-ibm-mq)
  - [23.30 `cpf-starter-integration-tcp`](#2330-cpf-starter-integration-tcp)
  - [23.31 `cpf-starter-integration-iso8583`](#2331-cpf-starter-integration-iso8583)
  - [23.32 `cpf-starter-notification`](#2332-cpf-starter-notification)
  - [23.33 `cpf-starter-notification-email`](#2333-cpf-starter-notification-email)
  - [23.34 `cpf-notification-sms-spi`](#2334-cpf-notification-sms-spi)
  - [23.35 `cpf-starter-scheduler-quartz`](#2335-cpf-starter-scheduler-quartz)
  - [23.36 `cpf-starter-resilience`](#2336-cpf-starter-resilience)
  - [23.37 `cpf-starter-featureflag`](#2337-cpf-starter-featureflag)
  - [23.38 `cpf-starter-secret`](#2338-cpf-starter-secret)
- [24. Profile별 종단간 Smoke 시나리오](#24-profile별-종단간-smoke-시나리오)
- [25. Starter 문서화 양식](#25-starter-문서화-양식)

<!-- CPF-TOC:END -->

## 1. Starter를 사용하는 이유

CPF Core는 기술 중립 API·SPI를 제공하고, DB·보안·Broker·Cache·관측·연계 Provider Runtime은 Starter가 소유한다. Starter를 선택한다는 것은 단순 Dependency 추가가 아니라 다음 계약을 선택하는 일이다.

- AutoConfiguration과 Bean
- Configuration Properties와 Validation
- Provider SDK와 연결·Resource
- Migration·Rollback SQL
- Health·Metric·Trace·Log
- 운영 조회·조치
- Unit·Contract·Integration·Fault Test
- Artifact·Version·SBOM

업무 상태·승인·보상·대사 정책은 Starter가 아니라 업무 Owner가 소유한다.

## 2. 선택 절차

1. 업무 결과를 온라인 API, DB, 보안, 메시징, Cache, 관측, 파일, TCP, 알림, 일정으로 분해한다.
2. 가장 가까운 Capability Profile을 선택한다.
3. Profile Catalog의 `profileVersion`, `resolvedStarters`, `allowedProviderBindings`를 확인한다.
4. Provider Binding을 한 개로 고정하고 상호 배타 Provider의 동시 Default를 금지한다.
5. Generator Manifest와 Build Dependency를 대조한다.
6. 각 Starter의 Property·Secret·Migration·Health를 준비한다.
7. Context Runner·Removal·Runtime·Fault Test를 실행한다.
8. Runtime Classpath·JAR/POM/SBOM에서 선택하지 않은 Provider가 없는지 확인한다.
9. 운영 담당자에게 Key·Default·Secret·Health·Metric·Rollback을 인계한다.

## 3. 13개 Versioned Capability Profile

| Profile ID | Profile Version | resolvedStarters | 선택 기준 |
|---|---|---|---|
| MINIMAL_BOOT_DOMAIN | `2026.08.02` | `cpf-starter-base` | 최소 Boot |
| DOMAIN_WEB_API | `2026.08.02` | `base`<br>`validation`<br>`openapi-webmvc`<br>`http-client` | 일반 Web API |
| SECURE_RESOURCE_API | `2026.08.02` | `domain-web-api`<br>`security-resource-server`<br>`security-service-identity`<br>`observability` | 보호 API |
| BROWSER_BFF_SESSION | `2026.08.02` | `security`<br>`persistence-jdbc`<br>`observability` | Browser BFF |
| PERSISTENCE_MYBATIS | `2026.08.02` | `persistence-jdbc`<br>`persistence-mybatis` | MyBatis DB |
| EVENT_KAFKA | `2026.08.02` | `messaging-reliability-jdbc`<br>`messaging-kafka`<br>`observability`<br>`resilience` | Kafka Event |
| EVENT_RABBITMQ | `2026.08.02` | `messaging-reliability-jdbc`<br>`messaging-rabbitmq`<br>`observability`<br>`resilience` | RabbitMQ Event |
| EVENT_JMS_IBM_MQ | `2026.08.02` | `messaging-reliability-jdbc`<br>`messaging-jms`<br>`messaging-ibm-mq`<br>`observability`<br>`resilience` | JMS/IBM MQ |
| INTEGRATION_TCP | `2026.08.02` | `integration-tcp`<br>`fixedlength-core`<br>`integration-fixedlength`<br>`integration-iso8583`<br>`observability`<br>`resilience` | TCP 전문 |
| OBSERVABLE_RESILIENT_SERVICE | `2026.08.02` | `observability`<br>`observability-otlp`<br>`resilience` | 관측·복원력 |
| NOTIFICATION_SERVICE | `2026.08.02` | `notification`<br>`notification-email`<br>`sms-spi`<br>`observability`<br>`resilience` | 알림 |
| SCHEDULED_SERVICE | `2026.08.02` | `scheduler-quartz`<br>`observability`<br>`resilience` | 일정 |
| SFTP_INTEGRATION | `2026.08.02` | `integration-sftp`<br>`file-archive`<br>`observability`<br>`resilience` | SFTP 파일 |

### 3.1 Provider Binding

| Capability | 허용 Provider | 예 | 오류 조건 |
|---|---|---|---|
| messaging | kafka/rabbitmq/jms/ibm-mq | `messaging=rabbitmq` | Profile 허용 목록 밖, 중복 Default |
| cache | caffeine/valkey | `cache=valkey` | Provider 두 개 Default |
| notification | email/sms | `notification=email` | Provider Bean 없음 또는 둘 이상 Default |

## 4. 38개 Leaf·Aggregate 실행 모듈 Catalog

| Gradle Project | 물리 경로 | 제공 결과 | 설정 정본 |
|---|---|---|---|
| `:cpf-starter-base` | `cpf-starters/base` | 최소 Boot·Capability Binding Registry | `cpf.starter.base` |
| `:cpf-starter-persistence-jdbc` | `cpf-starters/persistence-jdbc` | DataSource·JDBC Runtime | Source Properties |
| `:cpf-starter-persistence-mybatis` | `cpf-starters/persistence-mybatis` | MyBatis Persistence | Source Properties |
| `:cpf-starter-aop-service-access` | `cpf-starters/aop-service-access` | Service Access AOP | Source Policy |
| `:cpf-starter-openapi-webmvc` | `cpf-starters/openapi-webmvc` | OpenAPI WebMVC | Source Properties |
| `:cpf-starter-security-resource-server` | `cpf-starters/security-resource-server` | OAuth2 Resource Server | Source Properties |
| `:cpf-starter-security-session-jdbc` | `cpf-starters/security` | Browser Session JDBC | Source Properties |
| `:cpf-starter-security-service-identity` | `cpf-starters/security-service-identity` | Service Identity·HMAC | Source Properties |
| `:cpf-starter-security` | `cpf-starters/security-aggregate` | Security Aggregate | Leaf 전이만 |
| `:cpf-starter-messaging-reliability-jdbc` | `cpf-starters/messaging-reliability-jdbc` | Outbox·Inbox·DLQ·Replay Ledger | `cpf.messaging.reliability` |
| `:cpf-starter-channel-registry-jdbc` | `cpf-starters/channel-registry-jdbc` | Channel Registry JDBC | Source Properties |
| `:cpf-starter-cache-caffeine` | `cpf-starters/cache` | Local Cache | Source Properties |
| `:cpf-starter-cache-valkey` | `cpf-starters/cache-valkey` | Valkey/Redis Cache·Invalidation | Source Properties |
| `:cpf-starter-cache` | `cpf-starters/cache-aggregate` | Cache Aggregate | Leaf 전이만 |
| `:cpf-starter-observability` | `cpf-starters/observability` | 관측 계약·Logging·Metric·Trace | Source Properties |
| `:cpf-starter-observability-otlp` | `cpf-starters/observability-otlp` | OTLP Exporter | Source Properties |
| `:cpf-starter-http-client` | `cpf-starters/http-client` | Typed HTTP Client·Service Call | `cpf.http-client` |
| `:cpf-integration-fixedlength-core` | `cpf-starters/integration-fixedlength-core` | 순수 Fixed-length/Binary Codec | 없음 |
| `:cpf-starter-integration-fixedlength` | `cpf-starters/integration-fixedlength` | Fixed-length AutoConfiguration | Source Properties |
| `:cpf-starter-integration-sftp` | `cpf-starters/integration-sftp` | SFTP·Transfer Ledger | `cpf.integration.sftp` |
| `:cpf-starter-tabular-poi` | `cpf-starters/tabular-poi` | XLSX Streaming | Source Properties |
| `:cpf-starter-file-archive` | `cpf-starters/file-archive` | Archive·Checksum·Traversal 차단 | Source Properties |
| `:cpf-starter-validation` | `cpf-starters/validation` | Validation Provider | Source Properties |
| `:cpf-starter-attachment` | `cpf-starters/attachment` | Attachment Storage Adapter | Source Properties |
| `:cpf-starter-runtime-control-client` | `cpf-starters/runtime-control-client` | Runtime Control Client | Source Properties |
| `:cpf-starter-messaging-kafka` | `cpf-starters/messaging-kafka` | Kafka Provider Binding | Source Properties |
| `:cpf-starter-messaging-rabbitmq` | `cpf-starters/messaging-rabbitmq` | RabbitMQ Provider Binding | `cpf.messaging.rabbitmq` |
| `:cpf-starter-messaging-jms` | `cpf-starters/messaging-jms` | Jakarta JMS Provider Binding | `cpf.messaging.jms` |
| `:cpf-starter-messaging-ibm-mq` | `cpf-starters/messaging-ibm-mq` | IBM MQ Plugin Boundary | `cpf.messaging.ibm-mq` |
| `:cpf-starter-integration-tcp` | `cpf-starters/integration-tcp` | TCP Client/Server·Correlation·Unknown Result | `cpf.integration.tcp` |
| `:cpf-starter-integration-iso8583` | `cpf-starters/integration-iso8583` | ISO8583 Codec·MAC | Source Contract |
| `:cpf-starter-notification` | `cpf-starters/notification` | Notification Outbox·Preference·Worker | Source Properties |
| `:cpf-starter-notification-email` | `cpf-starters/notification-email` | Email Provider | Source Properties |
| `:cpf-notification-sms-spi` | `cpf-starters/notification-sms-spi` | SMS Provider SPI·Idempotency | Source Contract |
| `:cpf-starter-scheduler-quartz` | `cpf-starters/scheduler-quartz` | Quartz Scheduler Adapter | Source Properties |
| `:cpf-starter-resilience` | `cpf-starters/resilience` | Circuit·Retry·Bulkhead | Source Properties |
| `:cpf-starter-featureflag` | `cpf-starters/featureflag` | OpenFeature·Override Audit | Source Properties |
| `:cpf-starter-secret` | `cpf-starters/secret` | Secret Provider·Rotation | Source Properties |

## 5. Build 적용

### 5.1 Root Build

```groovy
implementation project(':cpf-starter-profile-secure-resource-api')
implementation project(':cpf-starter-profile-persistence-mybatis')
```

### 5.2 게시 Artifact

```groovy
implementation platform('com.cpf:cpf-platform-bom:<platform-version>')
implementation 'com.cpf.starter:cpf-starter-profile-secure-resource-api'
implementation 'com.cpf.starter:cpf-starter-profile-persistence-mybatis'
```

BOM은 Version만 정렬한다. 기능을 활성화하거나 Provider를 대신 선택하지 않는다.

## 6. Generator 적용 한 줄 명령

```powershell
$repo='C:\dev\projects\jck412_01_CPF'; pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') -Root $repo -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -CapabilityProfile SECURE_RESOURCE_API -ProviderBindings 'cache=valkey' -DryRun
```

Dry Run의 Profile·Provider·`resolvedStarters`·Version Lock·생성 경로를 확인한 뒤 동일 입력에 `-Apply`를 사용한다. 생성 후 Manifest와 `dependencies`, Runtime Classpath, SBOM을 대조한다.

## 7. Starter 공통 완료 Gate

| Gate | 확인 방법 | 실패 판정 |
|---|---|---|
| Dependency | Gradle dependencies·Runtime Classpath | 선택하지 않은 Provider 포함 |
| AutoConfiguration | ApplicationContextRunner·Bean 목록 | Bean 0 또는 중복 Primary |
| Properties | Metadata·기동 Validation | Default/범위/Secret 누락 |
| Consumer | 실제 Product/Domain 호출 | Interface만 있고 소비 없음 |
| SQL | 3 Vendor Migration·Rollback | Logical Contract 불일치 |
| Runtime | 실제 Provider Smoke·Fault | 가짜 성공·In-memory 운영 대체 |
| Removal | Starter 제거 후 Compile·Context | 잔존 Import·ClassNotFound |
| Operations | Health·Metric·Audit·Reconcile | 결과 판정 경로 없음 |
| Artifact | POM·Sources·JavaDoc·SBOM | Version·Hash 불일치 |
## 8. Base·Profile Binding

`cpf.starter.base.strict=true`, Profile ID와 Version을 검증한다. Manifest와 Runtime Binding Registry의 Default Provider가 일치해야 한다. 두 번째 Default 등록은 후보 Map 검증 후 원자적으로 거부돼야 한다.

### 8.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 8.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 9. Persistence JDBC·MyBatis

DataSource·Transaction·Mapper를 제공한다. 업무 SQL·Schema Ownership은 Domain이 유지하며 Starter는 연결·Template·Validation을 소유한다. 3 Vendor Driver·Flyway Module과 Connection Pool을 환경별로 검증한다.

### 9.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 9.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 10. Security Resource Server·Session·Service Identity

Resource API는 Audience·Scope·Permission을 Fail-closed하고 Browser BFF는 JDBC Session·CSRF·Cookie를 적용한다. Service Identity는 HMAC Rotation·Audience·Nonce·Expiry와 Credential 가림을 확인한다.

### 10.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 10.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 11. Messaging Reliability·Kafka·RabbitMQ·JMS·IBM MQ

업무 Transaction과 Outbox를 함께 Commit하고 Provider Binding을 통해 발행한다. Inbox/Dedup·DLQ·Replay·Lease·Claim·UNKNOWN_RESULT는 Reliability JDBC가 공통 소유한다.

### 11.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 11.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 12. Cache Caffeine·Valkey

Local Caffeine과 분산 Valkey를 구분한다. Namespace·TTL·Invalidation·Lock·원본 Version을 정하고 Cache 장애가 업무 원장을 바꾸지 않도록 한다.

### 12.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 12.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 13. Observability·OTLP

공통 Correlation과 Provider-neutral 관측을 기본 Starter가, OTLP SDK/Exporter를 OTLP Leaf가 소유한다. Collector 장애·Backpressure·Sampling·PII를 시험한다.

### 13.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 13.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 14. HTTP Client·Resilience

Connect 3초, Request 10초, 최대 응답 4 MiB 기본을 검토하고 전체 Deadline·Retry Budget·Circuit·Bulkhead를 호출 체인에 배분한다. Side Effect 호출 Timeout은 자동 Retry보다 결과 조회를 우선한다.

### 14.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 14.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 15. SFTP·File Archive·Attachment·Tabular

Streaming·크기 제한·Checksum·Path Traversal 차단·Atomic Rename·Transfer Ledger·악성 파일 상태·다운로드 Audit를 적용한다.

### 15.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 15.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 16. TCP·Fixed Length·ISO8583

Frame 4종, Binary/BCD/Hex/Endian, Secondary Bitmap, MAC, Correlation, Orphan, Timeout, TLS/mTLS와 UNKNOWN_RESULT를 한 계약으로 검증한다.

### 16.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 16.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 17. Notification·Email·SMS SPI

Notification Outbox·Preference·Quiet Hours·Provider Receipt·Idempotency를 공통으로 관리하고 Email/SMS Provider 실패와 중복 발송을 구분한다.

### 17.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 17.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 18. Scheduler Quartz

Schedule Version·Timezone·Misfire·Cluster Lock·중복 Trigger를 관리한다. Batch Job 실행 정본과 Scheduler Trigger 정본을 혼용하지 않는다.

### 18.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 18.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.

## 19. Feature Flag·Secret

Feature Flag 평가는 Context·Default·Override Approval·Audit를 기록한다. Secret은 Reference만 Config에 두고 Rotation·Health·권한·폐기를 관리한다.

### 19.1 적용 절차

1. 필요 Profile/Leaf를 Build에 선언한다.
2. Properties Source에서 Key·Type·Default·Validation을 추출한다.
3. 환경 Overlay와 Secret Reference를 구성한다.
4. Context Test로 AutoConfiguration과 Bean 단일성을 확인한다.
5. 실제 Consumer가 공개 API/SPI를 호출하는지 확인한다.
6. 정상·오류·Timeout·부분 실패·Process Kill을 시험한다.
7. Health·Metric·Trace·Audit와 결과 대사 방법을 확인한다.
8. Starter 제거·Provider 교체·LKG 복원 절차를 기록한다.

### 19.2 정상 결과

- 필요한 Bean만 활성화된다.
- 선택하지 않은 Provider JAR·Bean·SQL·Secret 요구가 없다.
- 오류는 기동 검증 또는 표준 오류로 Fail-closed한다.
- 실제 Consumer·Operation·Audit로 결과를 확인한다.
- 제거·교체 후 잔존 Import와 Stale Schema가 없다.


## 20. Source-backed Property 표

| Prefix | 주요 Default | 필수 조건 | Rollback |
|---|---|---|---|
| `cpf.starter.base` | strict=true, profile-id=MINIMAL_BOOT_DOMAIN, profile-version=1.0 | Profile ID/Version 비어 있지 않음 | 이전 Manifest/Profile 복원 |
| `cpf.http-client` | connect=3s, request=10s, max-response=4MiB | 양의 Duration, 1024B 이상 | 이전 Timeout/Limit 복원 |
| `cpf.messaging.reliability` | enabled=true, schema-required=true, claim=100, lease=30s, replay=500 | claim 1..1000, replay 1..5000 | Worker 중지 후 이전값 |
| `cpf.messaging.rabbitmq` | topic, #, durable/quorum, prefetch50, concurrency1, 1MiB, confirm10s | binding/exchange/queue | 이전 Binding/Topology |
| `cpf.messaging.jms` | session-transacted=true, ack-mode=2, 1MiB | binding/destination | 이전 ConnectionFactory/Binding |
| `cpf.messaging.ibm-mq` | tls-required=true, 1MiB | Queue Manager, Destination, CCDT 또는 Channel+Connection | 이전 CCDT/Channel/Certificate |
| `cpf.integration.sftp` | port22, connect10s, operation30s, buffer64KiB, max1GiB, ledger=true | host/user/secret | 전송 중지·이전 Endpoint |
| `cpf.integration.tcp` | CLIENT, host127.0.0.1, pool4, connect3s, response10s, idle60s, LENGTH_HEADER, max1MiB | port, TLS 시 Store/Secret | 신규 연결 차단·이전 Endpoint/Key |

## 21. Removal·Provider 교체 절차

1. 신규 Provider를 비Default로 배포하고 연결·권한·Health를 확인한다.
2. Dual-write가 허용된 기능만 제한된 Shadow Test를 수행한다.
3. Consumer 중지 위치·Outbox·Inbox·Offset/Queue Depth를 고정한다.
4. Default Binding을 Versioned Config와 승인으로 전환한다.
5. 결과 미확정·DLQ·In-flight를 대사한다.
6. 구 Provider의 신규 유입을 차단하고 보존 기간 후 Resource를 폐기한다.
7. Build에서 구 Starter를 제거하고 Compile·Context·Runtime Classpath·SBOM을 검증한다.
8. Migration 제거가 아니라 데이터 보존·Archive·Forward Cleanup 정책을 따른다.

## 22. 완료 체크리스트

- [ ] Profile ID·Version·`resolvedStarters`가 Catalog와 일치한다.
- [ ] Provider Binding이 하나이며 중복 Default를 Fail-closed한다.
- [ ] 모든 Leaf에 Consumer·Properties·AutoConfiguration·Test·Operations가 있다.
- [ ] 3 Vendor SQL이 있는 Starter는 Migration·Rollback 의미가 같다.
- [ ] 선택하지 않은 Runtime·Secret·Container가 유입되지 않는다.
- [ ] 정상·오류·Timeout·응답 유실·부분 실패·Process Kill을 시험했다.
- [ ] 제거·교체·Rollback과 운영 인계를 작성했다.

## 23. Starter별 적용·검증·제거 Reference

각 절은 Build 좌표를 추가하는 데서 끝나지 않는다. 실제 Consumer가 생성되고 정상·오류·제거 시험과 운영 인계가 모두 있어야 한다.

### 23.1 `cpf-starter-base`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/base` |
| 제공 결과 | 최소 Boot와 Capability Binding Registry |
| 실제 Consumer | Profile Manifest·Generator·모든 Consumer |
| 설정 정본 | cpf.starter.base |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Profile ID/Version·Default Binding 단일성; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Profile/Manifest 복원 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.2 `cpf-starter-persistence-jdbc`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/persistence-jdbc` |
| 제공 결과 | DataSource·JDBC Transaction 기반 |
| 실제 Consumer | DB 사용 Domain·ADM/BZA/Batch |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Connection·Transaction·Health; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 JDBC 설정·Driver |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.3 `cpf-starter-persistence-mybatis`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/persistence-mybatis` |
| 제공 결과 | MyBatis Mapper·Session |
| 실제 Consumer | MyBatis Domain·Product |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Mapper·TypeHandler·3 Vendor Query; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Mapper/Starter |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.4 `cpf-starter-aop-service-access`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/aop-service-access` |
| 제공 결과 | Service 접근 Log·Audit AOP |
| 실제 Consumer | 공개 Service Consumer |
| 설정 정본 | Policy Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | 중복 Aspect·민감정보 가림; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Starter 제거·명시 호출 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.5 `cpf-starter-openapi-webmvc`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/openapi-webmvc` |
| 제공 결과 | WebMVC OpenAPI 노출·생성 |
| 실제 Consumer | 온라인 API·ADM/BZA/Gateway |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Operation ID·Security·Generated Client; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 OpenAPI Artifact |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.6 `cpf-starter-security-resource-server`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/security-resource-server` |
| 제공 결과 | OAuth2 Resource Server |
| 실제 Consumer | 보호 API |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Issuer/JWK·Audience·Permission·Expiry; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 인증 Provider |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.7 `cpf-starter-security-session-jdbc`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/security` |
| 제공 결과 | Browser Session JDBC |
| 실제 Consumer | ADM·BZA BFF |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Cookie·CSRF·Session DB·Revoke; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Session Config/Schema |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.8 `cpf-starter-security-service-identity`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/security-service-identity` |
| 제공 결과 | Service HMAC Identity |
| 실제 Consumer | Remote Service·Gateway |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Key Version·Audience·Nonce·Clock; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Key Version 공존 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.9 `cpf-starter-security`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/security-aggregate` |
| 제공 결과 | 승인 Security Leaf 조합 |
| 실제 Consumer | Browser BFF |
| 설정 정본 | Leaf 설정 |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Aggregate 자체 Bean 0; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Leaf 개별 선언 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.10 `cpf-starter-messaging-reliability-jdbc`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/messaging-reliability-jdbc` |
| 제공 결과 | Outbox·Inbox·DLQ·Replay·Lease |
| 실제 Consumer | 모든 Broker Consumer |
| 설정 정본 | cpf.messaging.reliability |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | 3 Vendor Schema·Claim·Fencing·Replay; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Worker Drain·이전 Schema/Config |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.11 `cpf-starter-channel-registry-jdbc`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/channel-registry-jdbc` |
| 제공 결과 | Channel/Endpoint Registry |
| 실제 Consumer | 연계·Gateway·ADM |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Version·Health·Drift·Audit; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Registry Snapshot |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.12 `cpf-starter-cache-caffeine`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/cache` |
| 제공 결과 | Process Local Cache |
| 실제 Consumer | 읽기 중심 Domain |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | TTL·Size·Invalidation·Removal; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Cache 비활성/이전 TTL |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.13 `cpf-starter-cache-valkey`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/cache-valkey` |
| 제공 결과 | 분산 Cache·Invalidation |
| 실제 Consumer | 다중 Instance Domain/Product |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Namespace·TTL·Network·Serialization; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Endpoint/Local Fallback |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.14 `cpf-starter-cache`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/cache-aggregate` |
| 제공 결과 | 승인 Cache Leaf 조합 |
| 실제 Consumer | Profile Consumer |
| 설정 정본 | Leaf 설정 |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Default Provider 하나; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Leaf 개별 선언 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.15 `cpf-starter-observability`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/observability` |
| 제공 결과 | 공통 Log·Metric·Trace 문맥 |
| 실제 Consumer | 모든 Runtime |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Correlation·Masking·Cardinality; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 관측 비활성 아닌 이전 Sink |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.16 `cpf-starter-observability-otlp`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/observability-otlp` |
| 제공 결과 | OTLP Exporter |
| 실제 Consumer | Collector 연동 Runtime |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Endpoint·TLS·Batch·Backpressure; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Exporter 비활성·이전 Endpoint |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.17 `cpf-starter-http-client`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/http-client` |
| 제공 결과 | Typed HTTP Client |
| 실제 Consumer | Remote Facade·Webhook·REST |
| 설정 정본 | cpf.http-client |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Timeout·Body Limit·TLS·Error Mapping; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Timeout/Client |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.18 `cpf-integration-fixedlength-core`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/integration-fixedlength-core` |
| 제공 결과 | 순수 Fixed/Binary Codec |
| 실제 Consumer | TCP·File 전문 |
| 설정 정본 | 없음 |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Length·Charset·BCD/Hex/Endian; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Layout Version |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.19 `cpf-starter-integration-fixedlength`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/integration-fixedlength` |
| 제공 결과 | Fixed-length AutoConfiguration |
| 실제 Consumer | 전문 Adapter |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Layout Registry·Validation·Masking; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Layout/Bean |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.20 `cpf-starter-integration-sftp`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/integration-sftp` |
| 제공 결과 | SFTP Client·Transfer Ledger |
| 실제 Consumer | 파일 연계 Domain |
| 설정 정본 | cpf.integration.sftp |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Known Host·Resume·Checksum·Atomic Rename; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 전송 Drain·이전 Endpoint |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.21 `cpf-starter-tabular-poi`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/tabular-poi` |
| 제공 결과 | XLSX Streaming Reader/Writer |
| 실제 Consumer | File Batch·Export |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Row Limit·Formula·Memory·Temp; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Format/CSV 대체 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.22 `cpf-starter-file-archive`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/file-archive` |
| 제공 결과 | Archive·Checksum·Traversal 방어 |
| 실제 Consumer | 첨부·SFTP·Batch |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Zip Bomb·Path·Streaming·Cleanup; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Archive Policy |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.23 `cpf-starter-validation`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/validation` |
| 제공 결과 | 공통 Validation Provider |
| 실제 Consumer | Web/Batch/Message Input |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Group·Locale·Error Contract; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Validator |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.24 `cpf-starter-attachment`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/attachment` |
| 제공 결과 | Attachment Storage·Security State |
| 실제 Consumer | BZA·업무 Domain |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Upload·Scan·Quarantine·Download Audit; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Storage Adapter |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.25 `cpf-starter-runtime-control-client`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/runtime-control-client` |
| 제공 결과 | Runtime Change Agent Client |
| 실제 Consumer | ADM Control 대상 Runtime |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Capability·Preview·Apply·ACK/NACK; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | LKG Config/이전 Agent |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.26 `cpf-starter-messaging-kafka`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/messaging-kafka` |
| 제공 결과 | Kafka Binding |
| 실제 Consumer | Event Domain·Reference |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | ACK·Offset·Rebalance·DLQ; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Consumer Drain·이전 Topic/Binding |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.27 `cpf-starter-messaging-rabbitmq`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/messaging-rabbitmq` |
| 제공 결과 | RabbitMQ Binding |
| 실제 Consumer | Queue Event Domain |
| 설정 정본 | cpf.messaging.rabbitmq |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Confirm·Return·Quorum·ACK/NACK·DLQ; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Queue Drain·이전 Binding |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.28 `cpf-starter-messaging-jms`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/messaging-jms` |
| 제공 결과 | Jakarta JMS Binding |
| 실제 Consumer | JMS Provider Consumer |
| 설정 정본 | cpf.messaging.jms |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Transaction·Ack·Redelivery·Durable; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Consumer Stop·이전 Destination |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.29 `cpf-starter-messaging-ibm-mq`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/messaging-ibm-mq` |
| 제공 결과 | IBM MQ Plugin Boundary |
| 실제 Consumer | 기관 MQ 연계 |
| 설정 정본 | cpf.messaging.ibm-mq |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | CCDT/Channel·TLS·Reason Code·In-doubt; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 CCDT/Channel/Key |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.30 `cpf-starter-integration-tcp`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/integration-tcp` |
| 제공 결과 | TCP Client/Server·Correlation |
| 실제 Consumer | 기관 전문 연계 |
| 설정 정본 | cpf.integration.tcp |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Frame·Half-open·Orphan·TLS·Unknown; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 연결 Drain·이전 Endpoint |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.31 `cpf-starter-integration-iso8583`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/integration-iso8583` |
| 제공 결과 | ISO8583 Bitmap·Field·MAC |
| 실제 Consumer | 금융 전문 연계 |
| 설정 정본 | Contract Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Primary/Secondary Bitmap·BCD·MAC·Mask; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Layout/MAC Key |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.32 `cpf-starter-notification`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/notification` |
| 제공 결과 | Notification Outbox·Preference·Worker |
| 실제 Consumer | BZA·업무 알림 |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Quiet Hours·Dedup·Receipt·Retry; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Worker Drain·이전 Policy |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.33 `cpf-starter-notification-email`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/notification-email` |
| 제공 결과 | Email Provider |
| 실제 Consumer | Notification Service |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Accept/Delivery·Template·Attachment; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 SMTP/Provider |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.34 `cpf-notification-sms-spi`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/notification-sms-spi` |
| 제공 결과 | SMS Provider SPI |
| 실제 Consumer | 조직별 SMS Adapter |
| 설정 정본 | SPI Contract |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Idempotency·Receipt·Masking; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Provider 교체/이전 Credential |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.35 `cpf-starter-scheduler-quartz`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/scheduler-quartz` |
| 제공 결과 | Quartz Scheduler Adapter |
| 실제 Consumer | Scheduled Service |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Cluster·Misfire·Timezone·Lock; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | Trigger Pause·이전 Schedule |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.36 `cpf-starter-resilience`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/resilience` |
| 제공 결과 | Circuit·Retry·Bulkhead |
| 실제 Consumer | Remote/Broker/Integration |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Deadline Budget·Retryable·Queue Bound; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Policy Version |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.37 `cpf-starter-featureflag`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/featureflag` |
| 제공 결과 | OpenFeature 평가·Override Audit |
| 실제 Consumer | 모든 선택 기능 |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Default·Context·Override·Secure Fail; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Flag Version |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

### 23.38 `cpf-starter-secret`

| 항목 | 내용 |
|---|---|
| 물리 경로 | `cpf-starters/secret` |
| 제공 결과 | Secret Provider·Rotation |
| 실제 Consumer | 모든 Credential Consumer |
| 설정 정본 | Properties Source |
| 생성·변경 산출물 | Build Dependency·AutoConfiguration·Properties Metadata·필요 Resource/SQL·Test |
| 적용 순서 | Profile/Leaf 선택 → 설정/Secret → Context → Consumer → Runtime → Fault → Artifact |
| 정상 판정 | Reference·ACL·Version·Rotation·Health; Health·Metric·Trace·Audit와 업무 결과가 일치 |
| 실패 대응 | 기동 Validation·Provider 연결·권한·Timeout·부분 실패를 분리하고 결과 미확정은 대사 |
| 제거/되돌리기 | 이전 Secret Version 공존 |

실행 확인:

1. Gradle Dependency와 Runtime Classpath를 확인한다.
2. AutoConfiguration Condition과 생성 Bean을 확인한다.
3. Property Default·환경 Override·잘못된 값 기동 실패를 시험한다.
4. 실제 Product/Domain Consumer가 Public API/SPI를 호출한다.
5. 정상·권한·Timeout·Process Kill·Provider 장애를 시험한다.
6. Starter 제거 후 Compile·Context·Migration·Runtime Classpath를 확인한다.
7. 운영 인계에 Key·Secret·Health·Metric·Rollback을 기록한다.

## 24. Profile별 종단간 Smoke 시나리오

| Profile | Smoke 입력 | 정상 결과 | Fault | 복구 |
|---|---|---|---|---|
| MINIMAL_BOOT_DOMAIN | 기동·Version 조회 | Base Profile/Version 일치 | 잘못된 Profile Version | 기동 Fail-closed |
| DOMAIN_WEB_API | Validation 포함 Query/Command | OpenAPI·Error Contract | Oversize/Timeout | 입력 수정·Operation 조회 |
| SECURE_RESOURCE_API | 유효/무효 Token·Service Identity | Audience·Permission·Trace | Expired/Nonce Replay | 재인증·Key Rotation |
| BROWSER_BFF_SESSION | Login·Session·CSRF | JDBC Session·Logout/Revoke | DB Down·Session Expiry | 재로그인·Session 대사 |
| PERSISTENCE_MYBATIS | CRUD·Version Conflict | 3 Vendor Query 의미 | Deadlock·Constraint | 제한 Retry·재조회 |
| EVENT_KAFKA | Outbox→Consumer | Inbox·Offset·Audit | ACK Loss·Rebalance | Message ID 대사 |
| EVENT_RABBITMQ | Outbox→Quorum Queue | Confirm·ACK·Inbox | Connection/Confirm Loss | Queue/Inbox 대사 |
| EVENT_JMS_IBM_MQ | JMS/MQ Destination | Commit·Reason Code·Inbox | In-doubt/Channel Down | QM/Attempt 대사 |
| INTEGRATION_TCP | Frame/ISO 요청 | Correlation·MAC·응답 | Fragment·Half-open·Response Loss | Unknown Result 조회 |
| OBSERVABLE_RESILIENT_SERVICE | Remote Call·Trace | OTLP·Circuit·Metric | Collector/Target Down | Buffer/Circuit Recovery |
| NOTIFICATION_SERVICE | Email/SMS 요청 | Receipt·Preference·Audit | Timeout·Duplicate | Receipt 대사·Dedup |
| SCHEDULED_SERVICE | Trigger·RunOnce | 한 번 실행·Audit | Misfire·Node Kill | 정책 실행·Cluster Lock |
| SFTP_INTEGRATION | Upload/Download | Ledger·Checksum·Rename | Disconnect·Mismatch | Resume/Quarantine |

## 25. Starter 문서화 양식

새 Starter 또는 Provider를 추가할 때 다음 항목을 같은 변경에 작성한다.

- Capability·선택 기준·비선택 조건
- Gradle Project·Artifact GAV·Version/BOM
- Public API/SPI/Internal Package
- AutoConfiguration·Condition·Bean·Backoff
- Property 전체 Key·Type·Default·범위·환경변수·Secret·재기동
- Resource·Migration·Rollback·3 Vendor 의미
- Product Consumer·Generator Profile·Provider Binding
- Health·Metric·Trace·Log·Audit·ADM Operation
- Unit·Context·Contract·Integration·Fault·Removal·Artifact Test
- Upgrade·Provider 교체·Rollback·Data Cleanup
