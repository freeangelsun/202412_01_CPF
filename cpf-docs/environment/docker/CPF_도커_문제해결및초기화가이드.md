# CPF Docker 문제 해결 및 초기화 가이드

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: Docker 환경 운영자, 장애 대응 담당자, 통합시험 검수자
> **완료 결과**: Docker Engine·Compose·Container·Network·Secret·Product Consumer 중 최초 실패 위치를 식별하고, 정확한 대상만 정상화하거나 승인된 범위로 초기화한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 공통 변수

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
$root='C:\dev\Docker\CPF'
$secretRoot='C:\dev\Docker\Secrets'
$envFile=Join-Path $secretRoot 'cpf-runtime.env'
```

## 2. 진단 순서

```text
Git Commit·Source Hash
→ Docker Engine·Compose
→ Compose config
→ Image·Digest
→ Container State·Exit Code
→ Health·Readiness
→ Port·DNS·Network·TLS
→ Secret·Permission
→ Product Client·Consumer
→ Operation·DB·Broker·Audit 대사
```

Container Log만 보고 Product 결함을 확정하지 않는다.

## 3. 기본 진단 명령

```powershell
docker version
docker compose version
git -C $repo rev-parse HEAD
docker ps -a --filter 'name=cpf-' --format '{{.Names}}\t{{.Status}}\t{{.Image}}'
docker network ls
docker volume ls --filter 'name=cpf-'
```

특정 Container:

```powershell
$container='cpf-kafka'
docker inspect $container
docker logs --tail 200 $container
```

Log를 공유하기 전에 Token·Password·개인정보·Connection String을 제거한다.

## 4. Compose config 실패

확인:

- 환경변수 파일 존재
- 필수 환경변수 누락
- Secret 파일 경로
- YAML Syntax
- Compose 파일 순서
- 중복 Port·Volume·Container Name

```powershell
docker compose --env-file $envFile `
  -f (Join-Path $root 'compose.yml') `
  -f (Join-Path $root 'compose.redis.yml') `
  -f (Join-Path $root 'compose.kafka.yml') `
  -f (Join-Path $root 'compose.integration.yml') `
  -f (Join-Path $root 'compose.tooling.yml') config --quiet
```

## 5. Port 충돌

```powershell
Get-NetTCPConnection -State Listen | Where-Object LocalPort -in 3306,5432,1521,6379,9092,18080,2222,8200,18081,4317,4318,8888 | Select-Object LocalAddress,LocalPort,OwningProcess
```

다른 Process를 임의 종료하지 않는다. 대상 Owner를 확인하고 Port 또는 작업 시간을 조정한다.

## 6. Container Exit Code

| Exit | 해석 | 확인 |
|---:|---|---|
| 0 | 정상 종료 가능 | Script 단계·StopAfter |
| 137 | SIGKILL·OOM 가능 | Memory·Docker Event·Log |
| 143 | SIGTERM 가능 | 정상 Stop인지 확인 |
| 기타 | EntryPoint·Config·Runtime 오류 | 최초 Error·Health Log |

Exit 143을 무조건 실패 또는 성공으로 판정하지 않는다.

## 7. Secret·인증 문제

- Secret 파일 존재와 ACL을 확인한다.
- 파일 내용은 출력하지 않는다.
- Container Mount 경로와 Application Reference를 비교한다.
- Rotation 중 Old/New Version과 적용 Target을 확인한다.
- 잘못된 Secret으로 Readiness가 닫히는지 확인한다.

## 8. DB 문제

- 대상 Vendor와 Port·계정·Database·Schema
- Migration History
- Lock·Session·Disk
- Runtime 계정 권한
- Drift
- Backup·Restore 상태

사용자 DB를 Drop하거나 Volume을 삭제하기 전에 정확한 대상·Backup·승인을 확인한다.

## 9. Redis·Kafka 문제

### Redis

- Password Secret
- AOF·Disk
- Maxmemory Policy
- Connection Pool
- Lock·Fencing·Invalidation 원장

### Kafka

- Broker·Controller Health
- Advertised Listener
- Topic 존재
- Producer Receipt
- Consumer Group·Lag
- Retry·DLT
- Outbox·Inbox·업무 원장 대사

## 10. WireMock·SFTP·Vault·Keycloak 문제

| Fixture | 확인 |
|---|---|
| WireMock | Mapping·Delay·Response Template·Volume Mount |
| SFTP | User·Secret·Directory 권한·sshd 상태 |
| Vault | Token Reference·Address·Dev Server 상태 |
| Keycloak | Realm Import·Admin Secret·Required Action·Client Secret |

## 11. Toxiproxy Fault 잔존

Toxiproxy 설정을 조회하고 활성 Fault를 제거한다. Fault 제거 후 Product Connection을 새로 맺고 Backlog·UNKNOWN_RESULT를 대사한다.

## 12. 데이터 초기화 원칙

기본 금지:

```text
docker system prune
docker volume prune
Docker Factory Reset
전체 Image 삭제
전체 CPF Container 삭제
사용자 DB Drop·Reset
git clean
```

초기화가 필요한 경우 다음을 먼저 기록한다.

```text
정확한 Service·Container·Volume
영향 데이터
Backup ID·Restore 절차
재생성 Script
승인자
실행 후 검증
```

## 13. 대상 한정 Container 재생성 예

Volume을 보존하고 특정 Container만 재생성한다.

```powershell
$service='wiremock'
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') stop $service
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') rm -f $service
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') create $service
```

실제 Service와 Compose 파일을 확인한 뒤 사용한다.

## 14. 정상화 판정

- Compose config 성공
- Container Health·Readiness
- 실제 Product Put/Get·Publish/Receive·Token·Secret 조회
- Retry·DLQ·Receipt·Operation 상태
- DB·Broker·업무 원장 대사
- Fault 제거
- Log·Metric·Trace·Audit 연결
- Running CPF Container 0 또는 승인된 실행 목록
- Secret·Volume·사용자 데이터 보존

## 15. 결함 보고 형식

```text
기준 Commit
Docker·Compose Version
Service·Container·Image Digest
실행 명령·시작/종료 시각·Exit Code
Expected / Actual
최초 실패 위치
Product Consumer·Operation ID
Sanitized Log Hash
재현 여부
보호 대상·초기화 미수행 범위
```
