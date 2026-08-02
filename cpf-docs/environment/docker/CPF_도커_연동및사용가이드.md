# CPF Docker 연동 및 사용 가이드

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: CPF 개발자, 통합시험 검수자, Docker 환경 운영자
> **완료 결과**: 필요한 Runtime만 선택 기동하고 Product Consumer의 실제 연결·정상·오류·대사 시험을 수행한 뒤 데이터 보존 상태로 종료한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 공통 변수와 Compose 호출

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
$root='C:\dev\Docker\CPF'
$secretRoot='C:\dev\Docker\Secrets'
$envFile=Join-Path $secretRoot 'cpf-runtime.env'
if(-not(Test-Path -LiteralPath $root -PathType Container)){throw "Runtime Root가 없습니다: $root"}
if(-not(Test-Path -LiteralPath $envFile -PathType Leaf)){throw "환경변수 파일이 없습니다: $envFile"}
```

상태 조회:

```powershell
docker compose --env-file $envFile `
  -f (Join-Path $root 'compose.yml') `
  -f (Join-Path $root 'compose.redis.yml') `
  -f (Join-Path $root 'compose.kafka.yml') `
  -f (Join-Path $root 'compose.integration.yml') `
  -f (Join-Path $root 'compose.tooling.yml') ps -a
```

## 2. DB Vendor 선택 기동

### MariaDB

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.yml') up -d mariadb
```

PostgreSQL·Oracle은 Service 이름을 각각 `postgresql`, `oracle`로 바꾼다.

### 검증

- 전용 QA DB·Schema인지 확인
- CPF Object Count 0 또는 승인 Baseline
- Fresh Install·Verify
- Runtime Query
- Upgrade·Rollback/Forward Recovery·Reapply
- Drift
- Backup·격리 Restore·Application Query

### 중지

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.yml') stop mariadb
```

## 3. Redis

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.redis.yml') up -d redis
docker compose --env-file $envFile -f (Join-Path $root 'compose.redis.yml') ps redis
```

Product Test:

- Cache Hit·Miss·TTL
- Negative Cache
- Single-flight
- Distributed Lock·Fencing
- Invalidation·Reconcile
- Redis Down·Timeout·Restart
- Secret 누락·잘못된 Password

Container `PONG`만으로 Cache Starter 기능을 완료 처리하지 않는다.

## 4. Kafka

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.kafka.yml') up -d kafka
docker compose --env-file $envFile -f (Join-Path $root 'compose.kafka.yml') ps kafka
```

Product Test:

- Topic·Partition·Consumer Group
- Producer Receipt
- Outbox Publish
- Inbox Deduplication
- Retry·DLT
- Consumer Process Kill
- Ordering·Duplicate·Backlog
- Timeout·UNKNOWN_RESULT·Reconcile

Kafka Topic 자동 생성을 전제로 하지 않는다. Product Fixture 또는 승인된 초기화 Script가 Topic을 만든다.

## 5. Integration Fixture

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') up -d wiremock sftp vault keycloak
```

모든 Fixture를 필요로 하지 않으면 Service 이름을 필요한 항목만 지정한다.

## 6. Tooling Fixture

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.tooling.yml') up -d toxiproxy otel-collector
```

Toxiproxy Fault는 한 번에 하나씩 적용하고 제거 명령·시간·대상 Proxy를 기록한다.

## 7. Product Runtime 연결

1. 대상 Consumer의 실제 Property Prefix와 Named Binding을 확인한다.
2. Host 실행이면 `127.0.0.1:<host-port>`를 사용한다.
3. Container 간 연결이면 Compose Network의 Service 이름과 내부 Port를 사용한다.
4. Secret은 File 또는 승인 Provider Reference로 전달한다.
5. Health뿐 아니라 Product API·Message·File·Auth 계약을 실행한다.
6. Operation·DB·Broker·Audit를 대사한다.

## 8. Fault Injection

| Fault | 도구 | 확인 |
|---|---|---|
| Latency | Toxiproxy·WireMock | Timeout Budget·Retry |
| Connection Reset | Toxiproxy | Failure Class·Circuit |
| 5xx·Schema 오류 | WireMock | Validation·UNKNOWN_RESULT |
| Process Kill | `docker stop` 대상 한정 | Restart·Lease·Consumer Recovery |
| Secret 오류 | 잘못된 Test Reference | Fail-closed·Readiness |
| Broker Backlog | Consumer 중지 | Lag·Retry·정상화 시간 |
| DB Lock·Loss | QA DB·Toxiproxy | Transaction·Reconcile |

## 9. 작업 종료

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.tooling.yml') stop toxiproxy otel-collector
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') stop wiremock sftp vault keycloak
docker compose --env-file $envFile -f (Join-Path $root 'compose.kafka.yml') stop kafka
docker compose --env-file $envFile -f (Join-Path $root 'compose.redis.yml') stop redis
# DB는 이번 시험에서 시작한 Vendor만 중지한다.
```

`down -v`를 기본 종료 명령으로 사용하지 않는다.

Running Container 확인:

```powershell
docker ps --filter 'name=cpf-' --format '{{.Names}}\t{{.Status}}'
```

정상 종료 목표는 이번 시험에서 필요한 CPF Container가 모두 중지되고 Volume·Secret·사용자 데이터가 보존된 상태다.

## 10. Evidence

- Git Commit·Compose Hash
- Image Tag·Digest
- 시작·종료 시각
- Product Test 명령·Exit Code
- Fault 적용·제거 기록
- Operation·DB·Broker·Audit 대사
- Sanitized Log Hash
- Running Container Count
