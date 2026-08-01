# CPF 도커 연동 및 사용 가이드

상위 메뉴: [CPF Docker 가이드](README.md)

> 대상: CPF 개발자, QA 담당자, 운영 지원자, 자동화 도구  
> 목적: 설치된 Docker 개발·테스트 환경의 구성요소와 용도를 이해하고 필요한 항목만 선택해 사용한다.

## 1. 이 환경을 구성하는 프로그램과 역할

CPF Docker 환경은 단순히 Database Container만 실행하는 구성이 아니다. 개발, DB 설치, Batch, Messaging, Cache, 장애 상황, Telemetry, Frontend, Browser, Supply-chain 작업을 동일한 환경에서 수행할 수 있도록 Service와 Toolchain을 함께 제공한다.

모든 프로그램을 항상 실행할 필요는 없다. 수행할 작업에 필요한 Service만 시작하고, 명령형 Tool은 작업할 때만 일회성 Container로 실행한다.

### 1.1 Host 실행 기반

| 프로그램 | 역할 | 필요한 이유 |
|---|---|---|
| Windows 10/11 | 개발 PC 운영체제 | CPF Repository, IDE, PowerShell, Docker Desktop을 실행한다. |
| WSL 2 | Linux Container Backend | Windows에서 Linux Container를 안정적으로 실행하는 기반이다. |
| Docker Desktop | Container 실행·Image·Volume·Network 관리 | DB, Redis, Kafka, Toxiproxy, OpenTelemetry Collector와 Tool Image를 실행한다. |
| Docker Engine | 실제 Container Runtime | Image를 Container로 만들고 Network·Volume·Port를 관리한다. |
| Docker Compose | 여러 Service의 구성과 수명주기 관리 | CPF DB 3종, Redis, Kafka, Toxiproxy, Telemetry Service를 일관된 이름과 Port로 실행한다. |
| PowerShell 7 | 설치·상태·기동·중지·초기화 Script 실행 | Windows에서 한 줄 명령으로 환경을 관리하고 CPF의 기존 PowerShell Tool을 실행한다. |
| Git | Source 기준과 변경 상태 확인 | 기준 Commit, Working Tree, Diff, 변경 파일을 확인하고 실행 결과의 기준점을 남긴다. |

### 1.2 Docker Service 프로그램

| 프로그램 | Container | 주요 용도 | 평상시 실행 여부 |
|---|---|---|---|
| MariaDB | `cpf-mariadb` | 공식 지원 DB 중 MariaDB용 Schema·SQL·Migration·Runtime Query 테스트 | MariaDB 작업 때만 |
| PostgreSQL | `cpf-postgresql` | 공식 지원 DB 중 PostgreSQL용 Schema·SQL·Migration·Runtime Query 테스트 | PostgreSQL 작업 때만 |
| Oracle AI Database Free | `cpf-oracle` | 공식 지원 DB 중 Oracle용 User·Schema·Grant·SQL·Migration 테스트 | Oracle 작업 때만 |
| Redis | `cpf-redis` | Cache, TTL, 분산 상태, Lock, 재시작 후 Persistence 관련 테스트 | Redis 연계 작업 때만 |
| Apache Kafka | `cpf-kafka` | Messaging, Event, Batch Worker, Consumer Group, Retry·Recovery 테스트 | Kafka·Batch 작업 때만 |
| Toxiproxy | `cpf-toxiproxy` | DB·Redis·Kafka 연결에 지연, Timeout, 연결 차단, Reset을 주입 | 장애 상황 테스트 때만 |
| OpenTelemetry Collector | `cpf-otel-collector` | CPF가 전송하는 Trace·Metric·Log를 OTLP로 수신하고 파일·Console로 확인 | Telemetry 작업 때만 |

### 1.3 통합 Toolchain Runner

통합 Runner Image:

```text
cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0
```

| 포함 프로그램 | 용도 | 주로 사용하는 영역 |
|---|---|---|
| Java 25 | Gradle Build, Unit Test, Integration Test, Runtime 실행 | 모든 Backend Module |
| Gradle Wrapper | Repository에 선언된 정확한 Gradle Version과 Task 실행 | 전체 Java Build·Publication |
| Node.js 22·npm | ADM/BZA Frontend 의존성 설치, lint, typecheck, unit test, build | `cpf-admin`, `cpf-biz-admin` Frontend |
| Playwright 1.62.0 | Chromium·Firefox·WebKit Browser 자동화 | ADM/BZA 화면과 Backend 연동 |
| Python 3 | Repository의 Python Gate·분석·변환 Script 실행 | `cpf-tools`, CI 성격의 Source Gate |
| Git | 기준 SHA·Diff·Working Tree 확인 | 전체 Repository |
| MariaDB Client | MariaDB SQL File 실행과 접속 확인 | MariaDB DB Lifecycle |
| PostgreSQL `psql` | PostgreSQL SQL File 실행과 접속 확인 | PostgreSQL DB Lifecycle |
| Oracle SQL*Plus | Oracle Provision·Install·Seed·Upgrade·Rollback SQL 실행 | Oracle DB Lifecycle |
| Docker CLI·Compose | Runner 내부에서 Host Docker Engine 제어 | Container 기반 통합 작업 |
| `curl` | HTTP API·Health Endpoint·Artifact 다운로드 | Gateway, ADM/BZA, Runtime |
| `jq` | JSON 응답과 JSON Evidence 처리 | API·Script·Evidence |
| `openssl` | 인증서, TLS, Hash, 서명 관련 확인 | Security·Gateway·Supply-chain |
| `zip`·`unzip` | Artifact·Overlay·결과 묶음 처리 | 배포·인수인계·Evidence |

### 1.4 명령형 품질·공급망 Tool

다음 도구는 상시 실행 Container가 아니다. 필요한 작업 시 `docker run --rm` 방식으로 실행하고 종료한다.

| 프로그램 | Image | 주요 용도 | 결과 경로 |
|---|---|---|---|
| Trivy | `aquasec/trivy:0.70.0` | Source·Dependency·Container 관련 취약점, 설정 오류, Secret, CycloneDX SBOM 확인 | `C:\dev\Docker\CPF\output\trivy` |
| OSS Review Toolkit | `ghcr.io/oss-review-toolkit/ort:87.3.0` | Open Source Dependency 분석, License 정책, 승인 목록과의 정합성 확인 | `C:\dev\Docker\CPF\output\ort` |

## 2. CPF Module과 Docker 구성요소의 관계

아래 표는 대표적인 연계를 설명한다. 실제 Service 사용 여부는 각 Module의 활성 Profile과 실제 Consumer 연결을 기준으로 판단한다.

| CPF Module 또는 영역 | 기본 역할 | 주로 사용하는 Docker 구성 | 사용하는 이유 |
|---|---|---|---|
| `cpf-core` | Topology-independent 핵심 계약과 기술 기반 | Java Toolchain | 순수 계약·Unit Test는 외부 Service 없이 수행할 수 있다. Adapter나 통합 경로를 테스트할 때 관련 Starter의 Service를 사용한다. |
| `cpf-common` | 고객 업무 공통 기능 | 공식 DB 1종, 필요 시 Redis·Kafka | 공통 Code·Message·업무 데이터, Cache, Event 연계를 테스트한다. |
| `cpf-admin` | 플랫폼 운영·관리 Backend와 ADM | 공식 DB 1종, OpenTelemetry, Node, Playwright | 운영 데이터, 관리 API, 상태 정보, ADM Frontend와 Browser 연동을 테스트한다. |
| `cpf-biz-admin` | 고객 업무 관리자 Backend와 BZA | 공식 DB 1종, Node, Playwright | 조직·사용자·권한·결재·Audit 등 BZA 업무와 화면 연동을 테스트한다. |
| `cpf-batch` | Batch·Worker·Scheduler·Center-Cut Runtime | 공식 DB 1종, Kafka, 필요 시 Redis, Toxiproxy | Job Repository, Worker Event, Scheduler, Retry, 재시작, 부분 실패와 Recovery를 테스트한다. |
| `cpf-gateway` | 내부 API Routing과 진입 경계 | OpenTelemetry, Toxiproxy, 필요 시 Redis | Route, Timeout, Resilience, 추적 정보와 외부 의존 장애 상황을 테스트한다. |
| `cpf-reference` | 기준정보 Domain | 공식 DB 1종 | 기준정보 Schema, CRUD, Install·Upgrade·Rollback과 Runtime Query를 테스트한다. |
| `cpf-member` 및 생성 Domain | Generator 기반 업무 Domain | 공식 DB 1종, 선택적으로 Kafka·Redis | Golden Template에서 생성된 Domain의 SQL·CRUD·Messaging·Cache 정합성을 테스트한다. |
| `cpf-starters/cache` | Cache 연동 Starter | Redis | Cache Hit/Miss, TTL, Serialization, 장애 시 Fallback 정책을 테스트한다. |
| `cpf-starters/messaging-kafka` | Kafka Messaging Starter | Kafka, Toxiproxy | Produce·Consume, Consumer Group, Retry, Duplicate, Unknown Result를 테스트한다. |
| `cpf-starters/observability` | Trace·Metric·Log 연동 Starter | OpenTelemetry Collector | OTLP Export와 Transaction 식별·추적 정보를 확인한다. |
| `cpf-starters/resilience` | Timeout·Retry·Circuit Breaker 기반 | Toxiproxy와 대상 Service | 실제 지연·차단·Reset 조건에서 정책이 동작하는지 테스트한다. |
| `cpf-tools` | Generator, DB Lifecycle, Gate, Packaging | Python, Git, DB Client, Docker CLI, `jq`, 압축 도구 | Source·SQL·Generator·Artifact 관련 Script를 실행한다. |
| Supply-chain 영역 | SBOM·취약점·License 정책 | Trivy, OSS Review Toolkit | 배포 전 Open Source와 Artifact 위험을 확인한다. |

## 3. 작업별로 무엇을 켜야 하는가

| 수행할 작업 | 필요한 Service·Tool | 실행하지 않아도 되는 항목 |
|---|---|---|
| Java Unit Test | 통합 Runner 또는 Host Java 25 | DB·Redis·Kafka·Tooling Service |
| MariaDB SQL·Migration | MariaDB + MariaDB Client | PostgreSQL·Oracle |
| PostgreSQL SQL·Migration | PostgreSQL + `psql` | MariaDB·Oracle |
| Oracle SQL·Migration | Oracle + SQL*Plus | MariaDB·PostgreSQL |
| Redis Cache | Redis | DB·Kafka는 해당 기능이 요구하지 않으면 불필요 |
| Kafka Messaging | Kafka | Redis·DB는 Consumer 구현에 따라 선택 |
| Batch 통합 테스트 | 대상 DB + Kafka, 필요 시 Redis | 사용하지 않는 나머지 DB |
| 장애·재시도·복구 | 대상 Service + Toxiproxy | OpenTelemetry는 추적 정보가 필요할 때만 |
| Trace·Metric·Log 확인 | 대상 Application + OpenTelemetry Collector | Toxiproxy는 장애 조건이 없으면 불필요 |
| ADM/BZA Frontend Build | Node.js·npm | DB는 Backend 연동이 없는 정적 Build에서는 불필요 |
| Browser End-to-End | Node.js, Playwright, Backend, 해당 DB·Infra | 사용하지 않는 DB Vendor |
| Source 취약점·SBOM | Trivy | Base Service 전체 |
| OSS Dependency·License | OSS Review Toolkit | Base Service 전체 |
| API·JSON·TLS 확인 | `curl`, `jq`, `openssl` | 작업과 무관한 Service |

## 4. 기본 Service 상태와 실행

상태 확인:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action status
```

DB·Kafka·Redis 실행은 `cpf-env.ps1`을 사용한다.

### 4.1 MariaDB + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-mariadb
```

### 4.2 PostgreSQL + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-postgresql
```

### 4.3 Oracle + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-oracle
```

### 4.4 Redis + Kafka

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target infra
```

### 4.5 전체 Base Service

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target all
```

> 공식 DB 3종을 항상 동시에 실행할 필요는 없다. Vendor별 SQL·Migration 작업은 대상 DB 한 종류만 선택하는 것을 기본으로 한다.

## 5. 장애 조건과 Telemetry Tool

상태:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action status
```

Toxiproxy와 OpenTelemetry Collector:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target tools
```

MariaDB·Kafka·Toxiproxy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-mariadb
```

PostgreSQL·Kafka·Toxiproxy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-postgresql
```

Oracle·Kafka·Toxiproxy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-oracle
```

Redis·Kafka·Toxiproxy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-infra
```

장애 조건 제거:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action reset-faults
```

## 6. Toxiproxy 연결 Port

| 대상 | 직접 Port | Proxy Port |
|---|---:|---:|
| MariaDB | 3306 | 13306 |
| PostgreSQL | 5432 | 15432 |
| Oracle | 1521 | 11521 |
| Redis | 6379 | 16379 |
| Kafka | 9092 | 19093 |

Toxiproxy 관리 API:

```text
http://127.0.0.1:8474
```

직접 Port는 정상 연결 테스트에 사용하고 Proxy Port는 지연·차단·Reset 같은 장애 조건 테스트에 사용한다.

## 7. OpenTelemetry Collector

OTLP gRPC:

```text
http://127.0.0.1:4317
```

OTLP HTTP:

```text
http://127.0.0.1:4318
```

Collector 자체 Metric:

```text
http://127.0.0.1:8888/metrics
```

수집 결과:

```text
C:\dev\Docker\CPF\output\otel
```

OpenTelemetry Collector는 CPF Application의 기능을 대신하지 않는다. `cpf-starters/observability` 또는 Application 설정에서 OTLP Endpoint를 Collector로 지정했을 때 전송되는 Trace·Metric·Log를 받는 역할이다.

## 8. 통합 Toolchain Runner

실행:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-full-toolchain.ps1" -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

Runner 안에서는 Repository가 `/workspace/cpf`에 연결되고 Docker Socket을 통해 필요한 Service를 제어할 수 있다.

대표 사용:

```text
Gradle Build·Test
Frontend npm ci·lint·typecheck·test·build
Playwright 3 Browser
DB Lifecycle Script
Python Gate
API·JSON·TLS 확인
Artifact 압축과 Hash
```

## 9. Trivy

실행:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-trivy.ps1" -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

수행 항목:

```text
취약점 확인
설정 오류 확인
Secret Pattern 확인
CycloneDX SBOM 생성
```

결과:

```text
C:\dev\Docker\CPF\output\trivy
```

Trivy 결과에는 경로와 Dependency 정보가 포함될 수 있으므로 외부 제공 전 민감정보와 불필요한 로컬 경로를 제거한다.

## 10. OSS Review Toolkit

도구 요구사항 확인:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-ort.ps1" -Action requirements
```

Dependency 분석:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-ort.ps1" -Action analyze -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

ORT는 다음 Repository 정책과 함께 사용한다.

```text
cpf-tools/supply-chain/approved-primary-oss.csv
cpf-tools/supply-chain/license-policy.yml
cpf-tools/supply-chain/ort/evaluator.rules.kts
```

분석은 Repository를 임시 작업경로로 복사해 실행하므로 원본 Working Tree를 오염시키지 않는다.

## 11. CPF DB Source 연동

관련 정본:

```text
cpf-tools/config/database-install.default.json
cpf-tools/db/vendor-pack-manifest.json
cpf-tools/db/vendor/
cpf-tools/scripts/initialize-cpf-database.ps1
cpf-tools/scripts/invoke-official-db-vendor-sql.ps1
cpf-tools/scripts/initialize-generated-domain-databases.ps1
```

공식 지원 DB:

```text
Oracle
PostgreSQL
MariaDB
```

Docker Compose는 DB Engine과 관리자 접속 기반만 제공한다. CPF 업무 Database·Schema·Migration User·Runtime User·Table·Index·Seed는 Repository DB Source가 생성해야 한다.

## 12. 로그와 결과 경로

| 항목 | 확인 위치 |
|---|---|
| Base Service Log | `cpf-env.ps1 -Action logs` 또는 `docker logs` |
| Tooling Service Log | `cpf-tooling.ps1 -Action logs -Target tools` |
| OpenTelemetry 결과 | `C:\dev\Docker\CPF\output\otel` |
| Trivy 결과 | `C:\dev\Docker\CPF\output\trivy` |
| ORT 결과 | `C:\dev\Docker\CPF\output\ort` |
| Trivy Cache | `C:\dev\Docker\CPF\cache\trivy` |
| 실제 Image Lock | `C:\dev\Docker\CPF\image-lock-complete.json` |

Output과 Cache는 Source가 아니므로 Repository에 자동 Commit하지 않는다.

## 13. 작업 종료

Tool Service 중지:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action stop -Target tools
```

Base Service 중지:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action stop
```

단순 중지는 DB·Redis·Kafka Volume 데이터를 유지한다. 새로운 초기 상태가 필요한 경우에만 `CPF_도커_문제해결및초기화가이드.md`의 데이터 초기화 절차를 사용한다.
