# CPF 도커 개발·테스트 환경 구성 명세

## 1. 전체 구성

| 분류 | 구성 |
|---|---|
| DB | Oracle, PostgreSQL, MariaDB |
| Infra | Redis, Kafka |
| 장애 조건 | Toxiproxy |
| Telemetry | OpenTelemetry Collector Contrib |
| 보안·SBOM | Trivy |
| OSS 정책 | OSS Review Toolkit |
| Backend | Java 25, PowerShell 7.6.4, Python 3, Git |
| Frontend | Node.js 22, npm |
| Browser | Playwright 1.62.0, Chromium, Firefox, WebKit |
| DB Client | MariaDB Client, psql, SQL*Plus |
| Utility | Docker CLI·Compose, curl, jq, openssl, zip, unzip |

## 2. Image

필수 Base Image 8개에 Tool Image 4개와 통합 Runner 1개를 추가한다. 기존 PC의 Legacy Runner Image 3개는 삭제하지 않고 그대로 보존한다.

```text
ghcr.io/shopify/toxiproxy:2.12.0 또는 공식 latest
otel/opentelemetry-collector-contrib:0.157.0
aquasec/trivy:0.70.0
ghcr.io/oss-review-toolkit/ort:87.3.0 또는 공식 latest
cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0
```

실제 RepoDigest와 Image ID:

```text
C:\dev\Docker\CPF\image-lock-complete.json
```

## 3. Container

```text
cpf-mariadb
cpf-postgresql
cpf-oracle
cpf-redis
cpf-kafka
cpf-toxiproxy
cpf-otel-collector
```

설치 직후에는 모두 Created/Stopped다.

## 4. Volume

```text
cpf-mariadb-data
cpf-postgresql-data
cpf-oracle-data
cpf-redis-data
cpf-kafka-data
```

Tool Output은 Bind Directory를 사용한다.

## 5. Tool Output

```text
C:\dev\Docker\CPF\output\otel
C:\dev\Docker\CPF\output\trivy
C:\dev\Docker\CPF\output\ort
C:\dev\Docker\CPF\cache\trivy
```

## 6. Network

```text
cpf_default
```

## 7. Port

| Service | Port |
|---|---:|
| MariaDB | 3306 |
| PostgreSQL | 5432 |
| Oracle | 1521 |
| Redis | 6379 |
| Kafka | 9092 |
| Toxiproxy API | 8474 |
| MariaDB Proxy | 13306 |
| PostgreSQL Proxy | 15432 |
| Oracle Proxy | 11521 |
| Redis Proxy | 16379 |
| Kafka Proxy | 19093 |
| OTLP gRPC | 4317 |
| OTLP HTTP | 4318 |
| Collector Metric | 8888 |

모든 Host Port는 `127.0.0.1`에 Bind한다.

## 8. Source Ownership

Docker는 Engine·Toolchain·관리자 접속 기반만 제공한다.

CPF 업무 Database·Schema·User·Migration·Seed·Kafka Topic은 Repository Source가 관리한다.

## 9. 설치 Script의 변경 경계

설치 Script가 새로 관리하는 외부 Runtime 파일:

```text
Dockerfile.full-toolchain
compose.tooling.yml
otel-collector-config.yml
toxiproxy.json
cpf-tooling.ps1
run-trivy.ps1
run-ort.ps1
run-full-toolchain.ps1
verify-complete-environment.ps1
tool-images.env
image-lock-complete.json
```

기존 Base Compose·Secret·Image·Volume은 자동 삭제하지 않는다.

## 10. Version 변경 시 함께 갱신

- Dockerfile
- Tooling Compose
- Image Lock
- 전체 구축 가이드
- 연동 및 사용 가이드
- 문제 해결 가이드
- 구성 명세
- 설치 Script
