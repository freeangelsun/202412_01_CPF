# CPF 도커 연동 및 사용 가이드

상위 메뉴: [CPF Docker 가이드](README.md)

## 1. 사용 원칙

이 환경은 필요한 Service만 선택 기동한다. 동일 SHA에서 저비용 Source Gate와 Build를 먼저 완료한 뒤 DB·Kafka·Redis·외부연계·장애·복구 Runtime을 한 번씩 실행하는 것을 기본 순서로 한다.

Container 상태 확인이나 Image Version 출력만으로 기능 검수를 완료 처리하지 않는다. 실제 CPF Source·SQL·Migration·Client·Consumer를 연결해야 한다.

## 2. Service와 역할

| Service | Container | 역할 |
|---|---|---|
| MariaDB | `cpf-mariadb` | 공식 DB Vendor Runtime |
| PostgreSQL | `cpf-postgresql` | 공식 DB Vendor Runtime |
| Oracle | `cpf-oracle` | 공식 DB Vendor Runtime |
| Redis | `cpf-redis` | Distributed Cache·Lock·Counter Fixture |
| Kafka | `cpf-kafka` | 공식 MQ/Broker, Event·Batch Remote Transport |
| WireMock | `cpf-wiremock` | 외부 REST 성공·오류·지연·Reset Fixture |
| SFTP | `cpf-sftp` | 파일 송수신·ACK/NACK·재처리 Fixture |
| Vault | `cpf-vault` | Secret Provider Dev Fixture |
| Keycloak | `cpf-keycloak` | OIDC/OAuth2/JWT Identity Fixture |
| Toxiproxy | `cpf-toxiproxy` | DB·Broker·외부연계 장애 주입 |
| OTel Collector | `cpf-otel-collector` | OTLP Trace·Metric·Log 수집 |

Kafka가 CPF의 MQ다. RabbitMQ·ActiveMQ·IBM MQ는 기본 대상이 아니다.

### 연결 위치 기준

| Service | Windows Host에서 연결 | CPF Docker Network에서 연결 |
|---|---|---|
| MariaDB | `127.0.0.1:3306` | `cpf-mariadb:3306` |
| PostgreSQL | `127.0.0.1:5432` | `cpf-postgresql:5432` |
| Oracle | `127.0.0.1:1521` | `cpf-oracle:1521` |
| Redis | `127.0.0.1:6379` | `cpf-redis:6379` |
| Kafka | `127.0.0.1:9092` | `cpf-kafka:19092` |
| WireMock | `127.0.0.1:18080` | `cpf-wiremock:8080` |
| SFTP | `127.0.0.1:2222` | `cpf-sftp:22` |
| Vault | `127.0.0.1:8200` | `cpf-vault:8200` |
| Keycloak | `127.0.0.1:18081` | `cpf-keycloak:8080` |
| Toxiproxy | `127.0.0.1:<proxy-port>` | `cpf-toxiproxy:<proxy-port>` |
| OTel Collector | `127.0.0.1:4317/4318` | `cpf-otel-collector:4317/4318` |

Host에서 실행하는 CPF Process와 통합 Runner Container의 주소를 혼용하지 않는다. Kafka는 Listener가 다르므로 Host는 9092, Docker Network는 19092를 사용한다.

## 3. Toolchain

| Tool | 용도 |
|---|---|
| Java 25 | Gradle Build·Test·Runtime |
| Node.js 22·npm | ADM/BZA lint·typecheck·unit·build |
| Playwright | Chromium·Firefox·WebKit E2E |
| PowerShell 7.6.4 | Repository 관리·DB·환경 Script |
| Python 3 | Gate·정합성·Evidence Script |
| MariaDB Client·psql·SQL*Plus | 공식 DB 3종 Lifecycle |
| OpenSSH Client·sshpass | SFTP Fixture 실제 송수신 |
| Docker CLI·Compose | Service 선택 기동 |
| Trivy | 취약점·Secret·Misconfiguration·SBOM |
| OSS Review Toolkit | OSS Dependency·License 정책 |
| curl·jq·openssl | API·JSON·TLS·서명 확인 |

통합 Runner:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-full-toolchain.ps1" -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

## 4. 기본 상태

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action status
```

전체 환경 정지 상태 확인:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\verify-complete-environment.ps1" -RequireStopped
```

## 5. 작업별 선택 기동

### MariaDB

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target mariadb
```

### PostgreSQL

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target postgresql
```

### Oracle

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target oracle
```

### Redis + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target infra
```

### Batch + MariaDB + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-mariadb
```

PostgreSQL과 Oracle은 각각 `batch-postgresql`, `batch-oracle`을 사용한다.

### 외부연계 Fixture

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target external
```

### Vendor별 전체 통합

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target integration-mariadb
```

PostgreSQL과 Oracle은 각각 `integration-postgresql`, `integration-oracle`을 사용한다.

### 시작한 Group만 중지

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action stop -Target integration-mariadb
```

## 6. Module별 대표 연계

| CPF 영역 | 필요한 Runtime | 확인할 내용 |
|---|---|---|
| `cpf-core` | 기본 Java, 적용 시 Fixture | Public API/SPI와 Adapter 계약. 선택 Runtime 강제 금지 |
| `cpf-common` | 공식 DB, Redis 선택 | 업무 공통 데이터, Cache Aside, Lock·Invalidation |
| `cpf-admin` | 공식 DB, Keycloak, OTel, Playwright | 운영자 인증·Session·권한·운영 API·ADM E2E |
| `cpf-biz-admin` | 공식 DB, Keycloak, Playwright | 조직·사용자·결재·Audit·BZA E2E |
| `cpf-gateway` | WireMock, Keycloak, Redis 선택, Toxiproxy, OTel | Route·Trust·SSRF·Timeout·Retry·Unknown Result |
| `cpf-batch` | 공식 DB, Kafka, SFTP 선택, Toxiproxy | Spring Batch Repository·Remote Worker·Restart·Reconcile |
| 생성 Domain | 공식 DB, Kafka·Redis·WireMock·SFTP 선택 | Generator 결과, CRUD, Event, 외부 REST/File Adapter |
| Cache Starter | Redis 또는 Caffeine | Hit/Miss·TTL·Serialization·Multi-instance·Fallback |
| Kafka Starter | Kafka | ACK·Transaction·Duplicate·Ordering·Rebalance·DLT |
| Observability Starter | OTel Collector | 식별자·Masking·Trace·Metric·Log |
| Security Starter | Keycloak·공식 DB | OIDC/OAuth2/JWT·JDBC Session·Fail-closed |
| Secret Starter | Vault·File·Env | Provider 선택·Rotation·Revocation·원문 비노출 |
| Resilience Starter | WireMock·Toxiproxy | 단계별 Timeout·Retry·Circuit Breaker |

## 7. DB 검수

각 Vendor는 다음을 실제로 실행한다.

```text
Fresh Install
Seed
Runtime Query
Upgrade
Rollback
Reapply
Schema Drift
Unsupported Vendor Fail-closed
```

공식 DB 3종을 동시에 켜 둘 필요는 없다. 한 Vendor를 완료한 뒤 중지하고 다음 Vendor를 실행한다. Keycloak 내부 개발 저장소는 CPF DB Evidence로 계산하지 않는다.

## 8. Kafka 검수

Kafka는 다음 시나리오를 실제 Topic·Producer·Consumer로 확인한다.

```text
ACK와 Transaction
Stable Message ID
Partition·Ordering
Consumer Group·Rebalance
Duplicate·Idempotent Consumer
Retry Topic·DLT
Broker Outage
Consumer Process Kill
Response Loss·Unknown Result·Reconcile
```

Docker 설치 단계에서는 Topic을 만들지 않는다. Topic 이름과 생성 정책은 Repository Source가 소유한다.

## 9. Redis 검수

```text
Cache Hit·Miss
TTL·Eviction
Serialization·Payload Limit
Distributed Lock·Lease·Fencing
Multi-instance Invalidation
Redis Down·Timeout·Recovery
Product Profile Fail-closed
```

## 10. 외부연계 검수

WireMock·SFTP·Vault·Keycloak 사용법과 계정 식별자는 [확장 연동 서비스 사용 가이드](CPF_도커_확장연동서비스_사용가이드.md)를 따른다.

기본 Fixture 초기화:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\initialize-integration-fixtures.ps1" -StopAfter
```

## 11. 장애 조건

Tool 상태:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action status
```

DB·Kafka 장애:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-mariadb
```

외부연계 장애:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-external
```

장애 조건 제거:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action reset-faults
```

Proxy Port는 구성 명세와 확장 연동 가이드를 따른다.

## 12. OpenTelemetry

| 항목 | Endpoint |
|---|---|
| OTLP gRPC | `http://127.0.0.1:4317` |
| OTLP HTTP | `http://127.0.0.1:4318` |
| Collector Metric | `http://127.0.0.1:8888/metrics` |

수집 결과:

```text
C:\dev\Docker\CPF\output\otel
```

Transaction·Segment·Attempt·Job·Execution·Item·Agent 식별자가 연결되는지 확인하고 Secret·Token·PII 원문이 없는지 점검한다.

## 13. Frontend·Browser

정적 Build만 수행할 때 DB는 불필요하다. Backend E2E는 대상 DB와 인증·외부 Fixture를 함께 시작한다.

```text
npm ci
lint
typecheck
unit test
production build
Chromium
Firefox
WebKit
```

401·403·404·409·429·500·503, Session expiry, deep link, keyboard와 responsive 동작을 확인한다.

## 14. Supply Chain

Trivy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-trivy.ps1" -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

ORT:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-ort.ps1" -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

결과는 `C:\dev\Docker\CPF\output` 아래에 두고 Repository에 그대로 추가하지 않는다. 필요한 Evidence만 Sanitizing 후 정본 경로에 기록한다.

## 15. Evidence 최소 항목

```text
기준 Commit SHA
실행 명령
Profile·환경
Tool·Runtime Version
시작·종료 시각
Exit Code
Container·Image Digest
Requirement·Scenario
실제 결과
생성 Artifact와 SHA-256
Working Tree 상태
Secret·PII 제거 여부
최종 Service 상태
```

Runtime을 실행하지 못한 항목은 `미검증`, `실패`, `환경 차단`, `재확인 필요` 중 하나로 기록한다.
