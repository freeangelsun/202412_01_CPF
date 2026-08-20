# CPF Docker 연동 및 사용 가이드

> **주 독자**: 개발자·DBA·메시징·연동·QA 담당자
> **완료 결과**: 필요한 서비스만 기동하고 초기화·CPF 설정·정상·장애·중지 시나리오를 수행한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 사용 원칙](#1-사용-원칙)
- [2. Compose 공통 변수](#2-compose-공통-변수)
- [3. DB 3종 선택 기동](#3-db-3종-선택-기동)
- [4. Redis·Kafka·Tooling 기동](#4-rediskafkatooling-기동)
- [5. 확장 Fixture 기동](#5-확장-fixture-기동)
- [6. 초기화와 연결](#6-초기화와-연결)
- [7. 정상 결과](#7-정상-결과)
- [8. 서비스 중지 한 줄](#8-서비스-중지-한-줄)
- [9. 서비스별 정상·오류·복구 절차](#9-서비스별-정상오류복구-절차)
  - [9.1 MariaDB·PostgreSQL·Oracle](#91-mariadbpostgresqloracle)
  - [9.2 Redis/Valkey 계약](#92-redisvalkey-계약)
  - [9.3 Kafka](#93-kafka)
  - [9.4 WireMock](#94-wiremock)
  - [9.5 SFTP](#95-sftp)
  - [9.6 Vault·Keycloak](#96-vaultkeycloak)
  - [9.7 Toxiproxy](#97-toxiproxy)
  - [9.8 OTel Collector](#98-otel-collector)
- [10. 검증 결과 기록 양식](#10-검증-결과-기록-양식)

<!-- CPF-TOC:END -->

## 1. 사용 원칙

설치와 기동을 분리한다. 모든 서비스를 상시 기동하지 않고 시험에 필요한 Compose 조합만 선택한다. Port·Data·Secret·Resource를 시나리오마다 기록한다.

## 2. Compose 공통 변수

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; $envFile=Join-Path $dockerRoot 'Secrets\cpf-runtime.env'; $toolEnv=Join-Path $cpf 'tool-images.env'
```

## 3. DB 3종 선택 기동

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; $envFile=Join-Path $dockerRoot 'Secrets\cpf-runtime.env'; docker compose --env-file $envFile -f (Join-Path $cpf 'compose.yml') up -d mariadb postgresql oracle
```

기동 후 Container Health, Listener, Admin/Migration/Runtime 계정, Charset·Timezone을 확인한 뒤 CPF Migration Tool을 실행한다. 하나의 Vendor만 시험할 때는 필요한 Service만 지정한다.

## 4. Redis·Kafka·Tooling 기동

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; $envFile=Join-Path $dockerRoot 'Secrets\cpf-runtime.env'; $toolEnv=Join-Path $cpf 'tool-images.env'; docker compose --env-file $envFile --env-file $toolEnv -f (Join-Path $cpf 'compose.redis.yml') -f (Join-Path $cpf 'compose.kafka.yml') -f (Join-Path $cpf 'compose.tooling.yml') up -d redis kafka toxiproxy otel-collector
```

## 5. 확장 Fixture 기동

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; $envFile=Join-Path $dockerRoot 'Secrets\cpf-runtime.env'; $toolEnv=Join-Path $cpf 'tool-images.env'; docker compose --env-file $envFile --env-file $toolEnv -f (Join-Path $cpf 'compose.integration.yml') up -d wiremock sftp vault keycloak
```

## 6. 초기화와 연결

1. `initialize-integration-fixtures.ps1`로 승인 Fixture만 생성한다.
2. DB는 CPF Migration Tool로 Schema를 만든다.
3. Kafka Topic/ACL은 해당 Event 시나리오에서 생성한다.
4. Keycloak Realm/Client/User와 Vault Path는 Fixture Manifest와 일치시킨다.
5. CPF Application Config는 Container Host Port와 Secret Reference를 사용한다.
6. Smoke Transaction으로 DB/Broker/REST/SFTP/Security/Trace를 연결한다.

## 7. 정상 결과

- Application Readiness가 필요한 의존성을 확인한다.
- DB Migration/Schema Version이 정본과 일치한다.
- Broker Publish/Consume와 Outbox/Inbox가 Message ID로 대사된다.
- REST/TCP/SFTP는 Attempt/Transfer Ledger와 상대 결과가 일치한다.
- Keycloak/Vault Credential 원문이 Log에 없다.
- Trace가 Application→DB/Broker/External 구간을 연결한다.

## 8. 서비스 중지 한 줄

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; $envFile=Join-Path $dockerRoot 'Secrets\cpf-runtime.env'; $toolEnv=Join-Path $cpf 'tool-images.env'; docker compose --env-file $envFile --env-file $toolEnv -f (Join-Path $cpf 'compose.yml') -f (Join-Path $cpf 'compose.redis.yml') -f (Join-Path $cpf 'compose.kafka.yml') -f (Join-Path $cpf 'compose.integration.yml') -f (Join-Path $cpf 'compose.tooling.yml') stop
```

`down -v`는 데이터 삭제 승인과 Backup이 있을 때만 정확한 Compose Project에 사용한다.

## 9. 서비스별 정상·오류·복구 절차

### 9.1 MariaDB·PostgreSQL·Oracle

1. Container Health와 Listener를 확인한다.
2. Admin/Migration/Runtime 계정을 구분해 접속한다.
3. Fresh Migration과 Schema Version을 확인한다.
4. 업무 Smoke와 대사 Query를 실행한다.
5. Connection Block·Latency·Process Restart·Disk 경계를 주입한다.
6. Application Retry/Pool/Transaction 결과를 확인한다.
7. Backup Restore와 Upgrade/Rollback/Forward Fix를 실행한다.

### 9.2 Redis/Valkey 계약

- Key Namespace·TTL·Serialization·Invalidation·Lock Owner를 확인한다.
- Redis Down/Latency/Eviction/Restart에서 원장 결과가 변하지 않는지 확인한다.
- Session 사용 시 Logout/Revoke·Expiry·DB 복구를 시험한다.

### 9.3 Kafka

- Topic/Partition/Retention/ACL/Consumer Group을 생성한다.
- Outbox Publish·Consumer Inbox·Offset을 Message ID로 대사한다.
- Rebalance·Duplicate·Poison·DLQ·Replay·Broker Restart를 시험한다.

### 9.4 WireMock

- 정상·업무 오류·일시 오류·지연·Response Loss Stub을 분리한다.
- Attempt Ledger와 조회 API를 이용한 UNKNOWN_RESULT 확정을 시험한다.

### 9.5 SFTP

- Upload/Download·Partial·Resume·Checksum·Atomic Rename·Duplicate File을 시험한다.
- Transfer Ledger와 Remote File/Archive를 대사한다.

### 9.6 Vault·Keycloak

- Secret Version/Rotation/Permission과 Token/Session/Audience/Nonce/Expiry를 시험한다.
- Secret·Token·Password가 Log/Trace/Command History에 없는지 확인한다.

### 9.7 Toxiproxy

- DB/Redis/Kafka/WireMock/SFTP/Vault/Keycloak Proxy에 Latency·Timeout·Reset·Bandwidth를 시나리오별 적용한다.
- Toxic 이름과 적용/제거 시간을 기록하고 종료 시 해당 Toxic만 제거한다.

### 9.8 OTel Collector

- Trace/Metric Export·Sampling·Batch·TLS를 확인한다.
- Collector Down/Slow에서 Application 업무가 무제한 Block되지 않고 Drop/Retry/Buffer가 Metric으로 보이는지 확인한다.

## 10. 검증 결과 기록 양식

| 필드 | 내용 |
|---|---|
| scenarioId | 고유 시험 ID |
| sourceSha | 54bcc10887a83b933685bff462c0b0d7df824923 |
| services | 기동 Container/Image Digest |
| application | Artifact/Config/Schema Version |
| input | Secret 제거 요청/Fixture |
| fault | Toxic/Stop/Kill/Timeout |
| expected | 상태·건수·Hash·Metric |
| actual | 실제 결과 |
| recovery | Retry/Restart/Reconcile/Restore |
| evidence | Log/Report/Hash |
| cleanup | 제거한 Fixture/Toxic/Test Data |
