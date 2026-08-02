# CPF QA38 Messaging Fixture 사용 가이드

상위 메뉴: [Docker 문서](README.md)

## 1. 목적

RabbitMQ·Jakarta JMS·IBM MQ Provider 개발을 위한 재현 가능한 Broker 환경을 준비한다. 이 환경은 Product Starter·Consumer 구현을 대신하지 않는다.

## 2. 설치 전 Source 확인

이 문서 1차 ZIP은 문서만 제공한다. 다음 파일이 Repository에 적용된 뒤 명령을 실행한다.

```powershell
$required=@(
  '.\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1',
  '.\cpf-tools\environment\docker-development-test\compose.messaging-qa38.yml',
  '.\cpf-tools\environment\docker-development-test\initialize-messaging-fixtures.ps1',
  '.\cpf-tools\environment\docker-development-test\verify-messaging-environment.ps1'
);$missing=@($required|Where-Object{-not(Test-Path -LiteralPath $_ -PathType Leaf)});if($missing.Count){throw "QA38 Messaging Docker Source가 적용되지 않았습니다: $($missing -join ', ')"}
```

파일이 없으면 `DOCKER-001`·`MSG-001` 개발 요청을 먼저 처리한다.

## 3. 설치

RabbitMQ·Artemis:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1 -RepoRoot (Get-Location).Path
```

IBM MQ 포함:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1 -RepoRoot (Get-Location).Path -IncludeIbmMq -AcceptIbmMqDeveloperLicense
```

## 4. Fixture 초기화

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\initialize-messaging-fixtures.ps1 -StopAfter
```

## 5. RabbitMQ Scenario

- direct/topic/fanout/headers Exchange
- Queue·Binding·Routing Key
- Publisher Confirm·Mandatory Return
- Manual ACK/NACK·Requeue
- Retry·DLX·DLQ
- Prefetch·Concurrency·Consumer Cancel
- Connection Recovery·Process Kill
- Duplicate·Ordering·Backlog·Replay

기본 Fixture는 Direct Exchange, Primary Queue, DLQ를 준비한다. 나머지는 Product Contract Test에서 생성·삭제하고 실행 후 정리한다.

## 6. JMS/Artemis Scenario

- Queue·Topic
- Durable Subscription
- Selector
- Ack Mode
- Session Transaction
- Redelivery
- Exception Listener
- Consumer Process Kill

## 7. IBM MQ Scenario

- Queue Manager·Channel
- TLS·Credential Rotation
- CCDT/Endpoint
- Reason Code Mapping
- Reconnect·In-doubt
- Duplicate·Reconciliation

IBM MQ License·Image·Driver는 이용 조직 정책을 따른다. Proprietary Driver를 Overlay ZIP에 포함하지 않는다.

## 8. 완료 판정

각 Provider에서 다음을 Evidence로 남긴다.

```text
Image Digest
Broker Version
Destination Definition
Publish/Receive
ACK/Confirm/Commit
Failure Injection
Retry/DLQ
Process Kill
UNKNOWN_RESULT/Reconcile
StopAfter Running 0
```
