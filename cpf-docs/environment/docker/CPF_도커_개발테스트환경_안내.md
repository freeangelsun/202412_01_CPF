# CPF Docker 개발·시험 환경 안내

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: Docker 환경 운영자, CPF 개발자, 통합시험·장애시험 검수자
> **완료 결과**: 현재 사용 가능한 Runtime과 미편입 Provider를 구분하고, 필요한 서비스만 선택해 검증 범위와 보호 대상을 결정한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 목적

이 환경은 CPF의 Source·SQL·Generator·Frontend·Batch·Messaging·Cache·File·외부 연계·Security·Observability를 로컬 Container에서 재현한다. Container 기동이 아니라 실제 Product Consumer의 정상·오류·부분 실패·응답 유실·재시작·대사를 검증하는 것이 목적이다.

## 2. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b`
- Docker Source: `cpf-tools/environment/docker-development-test/`
- 실행본: `C:\dev\Docker\CPF`
- Secret: `C:\dev\Docker\Secrets`

## 3. Service 지도

| Service | Compose Source | Container | Host Port | 검증 목적 | 현재 판정 |
|---|---|---|---:|---|---|
| MariaDB | `compose.yml` | `cpf-mariadb` | 3306 | Fresh·Upgrade·Recovery·Restore | Source 존재, 최신 실행 미검증 |
| PostgreSQL | `compose.yml` | `cpf-postgresql` | 5432 | Fresh·Upgrade·Recovery·Restore | Source 존재, 최신 실행 미검증 |
| Oracle | `compose.yml` | `cpf-oracle` | 1521 | Fresh·Upgrade·Recovery·Restore | Source 존재, 최신 실행 미검증 |
| Redis | `compose.redis.yml` | `cpf-redis` | 6379 | Cache·Lock·Invalidation | Source 존재, 최신 실행 미검증 |
| Kafka | `compose.kafka.yml` | `cpf-kafka` | 9092 | Publish·Consume·Retry·DLT | Source 존재, 최신 실행 미검증 |
| WireMock | `compose.integration.yml` | `cpf-wiremock` | 18080 | REST 오류·지연·Schema Fault | Source 존재 |
| SFTP | `compose.integration.yml` | `cpf-sftp` | 2222 | Put·Get·Checksum·Atomic Rename | Source 존재 |
| Vault | `compose.integration.yml` | `cpf-vault` | 8200 | Secret 조회·만료·Rotation | Source 존재 |
| Keycloak | `compose.integration.yml` | `cpf-keycloak` | 18081 | OIDC·OAuth2·JWT·Session | Source 존재 |
| Toxiproxy | `compose.tooling.yml` | `cpf-toxiproxy` | Source 기준 | Latency·Reset·Timeout | Source 존재 |
| OTel Collector | `compose.tooling.yml` | `cpf-otel-collector` | 4317·4318·8888 | Trace·Metric·Backpressure | Source 존재 |

Port는 Compose `config` 결과와 Host 충돌을 확인해 확정한다.

## 4. 아직 설치 완료로 표시하지 않는 범위

| Runtime | 현재 이유 | 편입 조건 |
|---|---|---|
| RabbitMQ | 정식 Starter·Consumer 미등록 | Product Starter·Adapter·Consumer·Test 후 Compose 편입 |
| Jakarta JMS/Artemis | JMS Provider Product 경로 미등록 | Provider Contract·Consumer·Operations 후 편입 |
| IBM MQ | Starter·Driver·License·Consumer 미등록 | 별도 Extension·License 승인·Fault Test 후 선택 편입 |
| TCP·ISO8583 Simulator | 정식 Runtime·Consumer 재확인 필요 | Codec·Connection·Correlation·Reconcile 구현 후 편입 |
| Email·SMS Provider | Notification Starter·Receipt 경로 재확인 필요 | 실제 Provider·Receipt·중복 방지 후 편입 |
| Object Storage·Malware Scan | File Product Consumer 재확인 필요 | Storage·Scanner·Retention·Fault Test 후 편입 |

## 5. 경로·Secret 원칙

```text
Repository : C:\dev\projects\jck\202412_01_CPF
Runtime    : C:\dev\Docker\CPF
Secret     : C:\dev\Docker\Secrets
Output     : C:\dev\Docker\CPF\output
```

현재 Compose가 참조하는 Secret 파일 예:

```text
redis-password.txt
sftp-password.txt
vault-token.txt
keycloak-admin-password.txt
keycloak-test-password.txt
keycloak-service-client-secret.txt
```

Secret 존재 여부만 확인하고 값은 출력하지 않는다.

## 6. 환경 선택 기준

| 시험 | 필요한 Runtime |
|---|---|
| DB Vendor Lifecycle | 대상 DB 하나, 필요 시 3개 순차 실행 |
| Cache·Lock | Redis |
| Kafka Messaging | Kafka + 대상 Consumer |
| REST Fault | WireMock 또는 Toxiproxy |
| SFTP | SFTP + File Consumer |
| Secret | Vault + Secret Starter Consumer |
| 인증·Session | Keycloak + Security Consumer |
| Observability | OTel Collector + Instrumented Consumer |
| Network Fault | Toxiproxy + 대상 Runtime |

모든 Service를 동시에 기동하는 것을 기본값으로 삼지 않는다.

## 7. 검증 단계

```text
Compose config
→ Image·Digest
→ Container Created/Stopped
→ 선택 Service Start
→ Health·Readiness
→ Product Client Contract
→ Fault Injection
→ Retry·Reconcile·Rollback
→ Evidence
→ 선택 Service Stop
→ Running CPF Container 0
```

## 8. 완료 판정

- Compose config Exit Code 0
- Container 이름·Port·Network 충돌 없음
- Restart Policy `no`
- Secret 원문 출력 없음
- 실제 Product Consumer의 연결·인증·업무 효과 확인
- 정상·오류·Timeout·Process Kill·부분 실패 실행
- Operation·DB·Broker·Audit 대사
- 작업 종료 후 Running CPF Container 0
- Volume·사용자 데이터 보존
- 실행하지 않은 Provider는 `미검증`
