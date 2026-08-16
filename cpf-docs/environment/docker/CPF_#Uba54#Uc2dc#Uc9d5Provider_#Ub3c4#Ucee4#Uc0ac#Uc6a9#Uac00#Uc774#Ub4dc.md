# CPF Messaging Provider Docker 사용 가이드

> **주 독자**: 메시징 개발자·운영자·QA
> **완료 결과**: Kafka·RabbitMQ·JMS·IBM MQ의 Provider별 Runtime·Reliability·Fault·Reconcile 시험을 수행한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 공통 시험 구조](#1-공통-시험-구조)
- [2. Kafka](#2-kafka)
- [3. RabbitMQ](#3-rabbitmq)
- [4. Jakarta JMS·Artemis](#4-jakarta-jmsartemis)
- [5. IBM MQ](#5-ibm-mq)
- [6. 공통 Fault Matrix](#6-공통-fault-matrix)
- [7. Docker 편입 완료 조건](#7-docker-편입-완료-조건)
- [8. Provider Compose 적용 절차](#8-provider-compose-적용-절차)
  - [8.1 사전 검증](#81-사전-검증)
  - [8.2 Created/Stopped 설치](#82-createdstopped-설치)
  - [8.3 선택 기동과 상태 확인](#83-선택-기동과-상태-확인)
  - [8.4 CPF Consumer Smoke](#84-cpf-consumer-smoke)
  - [8.5 안전한 중지](#85-안전한-중지)
  - [8.6 Rollback](#86-rollback)
- [9. Provider별 완료 증적](#9-provider별-완료-증적)

<!-- CPF-TOC:END -->

## 1. 공통 시험 구조

```text
Business Transaction + Outbox
→ Publisher Claim/Lease
→ Named Provider Binding
→ Broker Destination
→ Consumer Inbox/Dedup
→ Business Side Effect
→ ACK/Offset/Commit
→ ADM/Metric/Audit/Reconcile
```

## 2. Kafka

- Topic·Partition·Replication·Retention·Compression·ACL·Consumer Group을 정의한다.
- Publish ACK, Offset Commit, Rebalance, Duplicate Delivery, DLT, Replay, Process Kill을 시험한다.
- Ordering Key와 Partition 변경의 업무 영향을 확인한다.

## 3. RabbitMQ

| 설정 | 기본/결정 |
|---|---|
| Binding | rabbitmq |
| Exchange Type | topic |
| Routing Key | # |
| Durable/Quorum | true/true |
| Prefetch | 50 |
| Concurrency | 1 |
| Max Payload | 1 MiB |
| Confirm Timeout | 10s |

Exchange·Queue·DLX/DLQ를 생성하고 Publisher Confirm/Return, Consumer ACK/NACK/Reject, Quorum Leader 전환, Connection Blocked, Channel Cancel, Network Loss를 시험한다.

## 4. Jakarta JMS·Artemis

- Destination, Queue/Topic, Durable Subscription, Session Transaction, Acknowledgement Mode, Redelivery, DLQ를 구성한다.
- 기본 `session-transacted=true`, Ack Mode 2, 최대 Payload 1 MiB를 검토한다.
- Commit 전/후 Process Kill과 Broker Failover에서 중복·유실을 Inbox로 차단한다.

## 5. IBM MQ

| 설정 | 필수 |
|---|---|
| queue-manager | 예 |
| destination | 예 |
| CCDT | CCDT 또는 Channel+Connection Name |
| channel/connection-name | CCDT 미사용 시 |
| tls-required | 기본 true |
| binding-name | ibm-mq |
| max-payload | 기본 1 MiB |

IBM MQ Runtime/Driver/Image/License는 승인된 외부 공급 경로를 사용한다. Reason Code를 Retryable/Final/In-doubt로 Mapping하고 MQPUT/MQGET·Syncpoint·Backout Queue·Channel/TLS·Queue Manager 전환·응답 유실을 시험한다.

## 6. 공통 Fault Matrix

| 장애 | 관찰 | 다음 행동 |
|---|---|---|
| Broker Down | Outbox Pending·Circuit | Broker 복구 후 같은 Message ID Publish |
| ACK Loss | Outbox/Provider/Inbox 불일치 가능 | 세 원장 대사 |
| Duplicate Delivery | Inbox Existing | 저장된 결과 반환 |
| Poison Message | Retry 초과·DLQ | 원인 수정·승인 Replay |
| Schema Incompatible | Quarantine | Writer/Reader 호환 수정 |
| Consumer Kill | Lease/Offset/Redelivery | 새 Consumer 인계·Dedup |
| Partial Target | Target별 상태 | 실패 Target만 재처리 |
| Result Unknown | Provider/Inbox 조회 | 확정 전 신규 Message 금지 |

## 7. Docker 편입 완료 조건

- Compose·Image Lock·Secret·Health가 있다.
- 실제 Starter와 Product Consumer가 연결된다.
- Reliability JDBC 3 Vendor Migration이 적용된다.
- 정상·중복·Timeout·ACK Loss·Process Kill·DLQ·Replay가 자동화된다.
- ADM에서 Outbox·Inbox·DLQ·Unknown Result를 같은 Message ID로 조회한다.
- 정확한 시작·중지·초기화·Cleanup 명령과 결과를 문서화한다.

## 8. Provider Compose 적용 절차

Provider별 Compose 파일은 Repository 밖 Docker Runtime Root에 두고, 기존 Base Compose와 함께 검증한다. 아래 명령은 현재 위치와 관계없이 실행하며 `$providerCompose`에는 승인된 Provider Compose의 절대경로를 넣는다.

### 8.1 사전 검증

```powershell
$dockerRoot='C:\dev\Docker';$cpfRoot=Join-Path $dockerRoot 'CPF';$secretRoot=Join-Path $dockerRoot 'Secrets';$providerCompose='C:\dev\Docker\CPF\compose.messaging-provider.yml';$envFile=Join-Path $secretRoot 'cpf-runtime.env';if(-not(Test-Path -LiteralPath $providerCompose -PathType Leaf)){throw "Provider Compose가 없습니다: $providerCompose"};docker compose --env-file $envFile -f (Join-Path $cpfRoot 'compose.yml') -f (Join-Path $cpfRoot 'compose.redis.yml') -f (Join-Path $cpfRoot 'compose.kafka.yml') -f $providerCompose config --quiet;if($LASTEXITCODE -ne 0){throw 'Provider Compose 검증 실패'}
```

정상 결과는 Exit Code 0이며, Secret 원문이 Console에 출력되지 않고 Host Port는 Loopback 또는 승인된 Interface에만 Binding된다.

### 8.2 Created/Stopped 설치

```powershell
$dockerRoot='C:\dev\Docker';$cpfRoot=Join-Path $dockerRoot 'CPF';$secretRoot=Join-Path $dockerRoot 'Secrets';$providerCompose='C:\dev\Docker\CPF\compose.messaging-provider.yml';$envFile=Join-Path $secretRoot 'cpf-runtime.env';docker compose --env-file $envFile -f (Join-Path $cpfRoot 'compose.yml') -f (Join-Path $cpfRoot 'compose.redis.yml') -f (Join-Path $cpfRoot 'compose.kafka.yml') -f $providerCompose create;if($LASTEXITCODE -ne 0){throw 'Provider Container 생성 실패'};docker compose --env-file $envFile -f $providerCompose ps --all
```

정상 결과는 대상 Container가 `Created` 또는 `Exited (0)` 상태이고, 업무 Queue·Topic·Destination·권한은 초기화 Script 실행 전 임의로 생성되지 않는 것이다.

### 8.3 선택 기동과 상태 확인

```powershell
$providerCompose='C:\dev\Docker\CPF\compose.messaging-provider.yml';docker compose -f $providerCompose up -d rabbitmq artemis;if($LASTEXITCODE -ne 0){throw 'Provider 선택 기동 실패'};docker compose -f $providerCompose ps;docker compose -f $providerCompose logs --tail 100 rabbitmq artemis
```

서비스 이름은 승인된 Compose의 실제 이름을 사용한다. IBM MQ는 조직이 승인한 Image·License·CCDT·TLS 자료가 준비된 경우에만 같은 절차로 기동한다.

### 8.4 CPF Consumer Smoke

1. Starter Profile과 Named Binding을 선택한다.
2. Reliability JDBC Migration을 적용한다.
3. 정상 Message를 한 건 발행하고 Outbox·Broker·Inbox·업무 원장을 같은 Message ID로 조회한다.
4. 같은 Message를 다시 전달해 업무 부수 효과가 한 번만 발생하는지 확인한다.
5. Consumer 종료·Broker 차단·ACK 유실을 재현하고 `UNKNOWN_RESULT`·DLQ·Replay·Reconcile을 확인한다.
6. ADM에서 Outbox·Inbox·DLQ·Attempt·Unknown Result를 같은 식별자로 조회한다.

### 8.5 안전한 중지

```powershell
$providerCompose='C:\dev\Docker\CPF\compose.messaging-provider.yml';docker compose -f $providerCompose stop;if($LASTEXITCODE -ne 0){throw 'Provider 중지 실패'};docker compose -f $providerCompose ps --all
```

중지 전에 Producer 유입을 차단하고, In-flight·Outbox Pending·Consumer Lag·Unacked·Open Transaction을 기록한다.

### 8.6 Rollback

1. 새 Producer 유입을 차단한다.
2. In-flight와 결과 미확정 Message를 기존 Provider·Outbox·Inbox 원장으로 대사한다.
3. Application의 Default Binding과 Provider Version을 LKG로 되돌린다.
4. 새 Provider Container만 중지한다. Volume 삭제나 전체 `down -v`는 사용하지 않는다.
5. LKG Consumer Smoke와 Backlog 대사를 통과한 뒤 유입을 재개한다.

```powershell
$providerCompose='C:\dev\Docker\CPF\compose.messaging-provider.yml';docker compose -f $providerCompose stop;if($LASTEXITCODE -ne 0){throw 'Provider Rollback 중지 실패'};docker compose -f $providerCompose ps --all
```

## 9. Provider별 완료 증적

| 증적 | 필수 내용 |
|---|---|
| Image Lock | Image ID·Digest·Version·공급 출처 |
| Compose | 서비스·Port·Volume·Secret·Health·`restart: no` |
| 초기화 | Exchange·Queue·Destination·ACL·DLQ·TLS·Fixture |
| 정상 흐름 | Message ID·Outbox·Provider ACK·Inbox·업무 결과 |
| 장애 | ACK 유실·중복·Process Kill·Network Block·Poison·Replay |
| 대사 | Pending·Unacked·Lag·DLQ·Unknown Result·업무 합계 |
| 보안 | 계정·권한·TLS·Secret Rotation·원문 미노출 |
| 종료 | In-flight 0 또는 인계 기록·Container 정지 |
| Rollback | LKG Binding·Provider Version·재기동·Smoke |
