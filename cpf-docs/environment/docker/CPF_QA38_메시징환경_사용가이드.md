# CPF QA38 Messaging Provider Docker 편입 가이드

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: Messaging Starter 개발자, Docker 환경 담당자, 통합시험·장애시험 검수자
> **완료 결과**: RabbitMQ·Jakarta JMS·IBM MQ Product 구현이 준비된 Provider만 Docker 환경에 증분 편입하고, 실제 Consumer·Fault·대사 시험을 수행한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 현재 판정

기준 Commit `3b600702502e53877e30cbac594987b371e2186b`의 `settings.gradle`에는 다음 공개 메시징 Starter만 등록돼 있다.

```text
:cpf-starter-messaging-kafka
```

RabbitMQ·Jakarta JMS·IBM MQ Starter는 정식 프로젝트 등록이 확인되지 않았다. 따라서 현재 Docker에 Broker만 설치해 CPF 지원 Provider 또는 검증을 마친 상태로 표시하지 않는다.

## 2. 편입 선행 조건

Provider별로 다음이 존재해야 한다.

```text
Starter Project·게시 좌표
Public AutoConfiguration·Properties
Provider Adapter·SDK 경계
실제 Product Consumer
Named Binding·Default Binding 규칙
Outbox·Inbox·Attempt·Receipt
Retry·DLQ/DLT·Replay
Health·Metric·Trace·Audit
정상·오류·Fault Test
Operations·Reconciliation
```

하나라도 없으면 Docker 편입 상태는 `대기` 또는 `부분 구현`이다.

## 3. Provider별 검증 범위

### 3.1 RabbitMQ

- Exchange: direct·topic·fanout·headers
- Queue·Binding·Routing Key
- Publisher Confirm·Mandatory Return
- Manual ACK·NACK·Requeue
- Retry·DLX·DLQ
- Prefetch·Concurrency·Consumer Cancel
- Connection Recovery·Process Kill
- Duplicate·Ordering·Backlog·Replay
- TLS·Credential Rotation

### 3.2 Jakarta JMS Fixture

JMS Contract 시험에는 Apache ActiveMQ Artemis 같은 개발 Fixture를 사용할 수 있다.

- Queue·Topic
- Durable Subscription
- Selector
- Ack Mode
- Session Transaction
- Redelivery
- Exception Listener
- Consumer Process Kill
- XA 사용 여부와 비사용 근거

Fixture 제품명을 CPF Public API에 노출하지 않는다.

### 3.3 IBM MQ Extension

- Queue Manager·Channel·Queue
- TLS·Credential Rotation
- CCDT 또는 Endpoint 설정
- Reason Code Mapping
- Reconnect·In-doubt
- Transaction·Commit·Backout
- Duplicate·Reconciliation
- Developer Image License·재배포 제한

Proprietary Driver와 Credential을 Repository·문서 ZIP에 포함하지 않는다.

## 4. Docker Source 편입 파일

실제 개발이 완료되면 관리 경로 `cpf-tools/environment/docker-development-test/`에 다음 성격의 파일을 추가한다. 파일명은 Repository 표준과 Owner 승인에 따라 확정한다.

```text
Provider별 Compose
Secret Reference
Broker 초기화 Fixture
Destination Definition
정상·오류 검증 Script
Fault·Process Kill Script
StopAfter·상태 검증
Image·License Manifest
```

문서가 Source보다 먼저 구체적인 미존재 Script 이름을 확정하지 않는다.

## 5. 편입 전 확인 명령

어느 폴더에서 실행해도 되도록 Repository 경로를 지정한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
$source=Join-Path $repo 'cpf-tools\environment\docker-development-test'
git -C $repo rev-parse HEAD
git -C $repo status --short
Get-ChildItem -LiteralPath $source -File | Where-Object Name -Match 'rabbit|jms|artemis|ibm|mq|messaging' | Select-Object Name,FullName
```

결과가 비어 있으면 설치 명령을 만들지 않고 Product·Docker Source 개발을 먼저 요청한다.

## 6. Compose 편입 기준

- Image Tag와 Digest 고정
- License·재배포 조건 기록
- `restart: "no"`
- Host Port `127.0.0.1` 제한
- Secret 파일은 `C:\dev\Docker\Secrets` 참조
- Volume 이름 충돌 없음
- Healthcheck와 실제 Protocol Probe 분리
- 기존 Kafka·DB·Redis·Fixture에 영향 없음
- 필요한 Provider만 선택 기동

## 7. Product 연결

1. Consumer의 실제 Starter·Binding Name을 확인한다.
2. Producer·Consumer의 Schema·Header·Idempotency 계약을 맞춘다.
3. Destination을 초기화한다.
4. 정상 Publish·Receive·Receipt를 확인한다.
5. Broker Down·Network Loss·Process Kill을 주입한다.
6. Retry·DLQ·Replay를 수행한다.
7. Outbox·Inbox·Attempt·업무 원장을 대사한다.
8. ADM·Log·Metric·Trace·Audit를 확인한다.

## 8. UNKNOWN_RESULT와 Reconciliation

Publisher가 전송 후 Confirm·Commit 결과를 받지 못하면 신규 업무 요청을 만들지 않는다.

```text
Outbox·Attempt 조회
→ Broker Destination·Receipt 조회
→ Consumer Inbox·업무 원장 조회
→ 실제 결과 확정
→ 허용된 재전송 또는 운영 확정
```

JMS·IBM MQ의 In-doubt 상태는 Provider Transaction과 업무 원장을 함께 확인한다.

## 9. 증적

```text
Git Commit
Starter·Consumer Artifact Version
Broker Image Tag·Digest·License
Compose Config Hash
Destination Definition
Publish·Receive·ACK/Confirm/Commit
Fault Injection·Process Kill
Retry·DLQ/DLT·Replay
UNKNOWN_RESULT·Reconciliation
ADM·Log·Metric·Trace·Audit
StopAfter Running 0
Secret 미노출
```

## 10. 현재 개발 요청

| ID | 범위 | 판정 | 요청 |
|---|---|---|---|
| `MSG-RMQ-001` | RabbitMQ Starter·Consumer·Operations | 미구현 | Product와 Docker Fixture 동시 구현 |
| `MSG-JMS-001` | Jakarta JMS Contract·Provider | 미구현 | Artemis Fixture와 실제 Consumer 연결 |
| `MSG-IBMMQ-001` | IBM MQ Extension | 미구현 | License·Driver·Reason Code·Reconcile 구현 |
| `DOCKER-MSG-001` | Provider Compose·검증 Script | 대기 | Product Source 완료 후 증분 편입 |
