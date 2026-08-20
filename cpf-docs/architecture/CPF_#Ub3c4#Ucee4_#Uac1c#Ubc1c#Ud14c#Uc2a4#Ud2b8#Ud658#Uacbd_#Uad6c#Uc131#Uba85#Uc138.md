# CPF 도커 개발·테스트 환경 구성 명세

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`

## 1. 구성 원칙

- 공식 DB는 Oracle·PostgreSQL·MariaDB 3종만 사용한다.
- Kafka가 공식 MQ/Broker다.
- 선택 Runtime과 외부연계 Fixture는 Core에 강제하지 않고 필요한 Consumer에서 사용한다.
- 모든 Container는 `restart: no`, Host Port는 `127.0.0.1` Bind다.
- Secret은 Repository 밖 `C:\dev\Docker\Secrets`에서 관리한다.
- 업무 Schema·User·Seed·Kafka Topic은 Repository Source가 소유한다.

## 2. 전체 구성

| 분류 | 구성 |
|---|---|
| DB | Oracle, PostgreSQL, MariaDB |
| Cache | Redis |
| MQ/Broker | Kafka |
| 외부 REST Mock | WireMock |
| 파일 연계 | SFTP Fixture |
| Secret Provider Fixture | Vault Dev |
| Identity Fixture | Keycloak Local |
| 장애 조건 | Toxiproxy |
| Telemetry | OpenTelemetry Collector Contrib |
| 보안·SBOM | Trivy |
| OSS 정책 | OSS Review Toolkit |
| Backend | Java 25, PowerShell 7.6.4, Python 3, Git |
| Frontend | Node.js 22, npm |
| Browser | Playwright Chromium·Firefox·WebKit |
| DB Client | MariaDB Client, psql, SQL*Plus |
| Utility | Docker CLI·Compose, curl, jq, openssl, zip, unzip, OpenSSH Client, sshpass |

## 3. Image

필수 Image 수는 18개다. 설치 시 실제 RepoDigest와 Image ID를 다음 파일에 기록한다.

```text
C:\dev\Docker\CPF\image-lock-complete.json
```

확장 연동 Image 기준:

```text
wiremock/wiremock:3.13.2
hashicorp/vault:1.21.4
quay.io/keycloak/keycloak:26.6.1
alpine:3.23.5
cpf-sftp-fixture:alpine3.23
cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1
```

Registry fallback을 사용하면 Tag보다 Lock의 Digest를 우선한다.

## 4. Container

```text
cpf-mariadb
cpf-postgresql
cpf-oracle
cpf-redis
cpf-kafka
cpf-wiremock
cpf-sftp
cpf-vault
cpf-keycloak
cpf-toxiproxy
cpf-otel-collector
```

설치 직후 모두 Created/Stopped다.

## 5. Volume·Bind Data

Docker Volume:

```text
cpf-mariadb-data
cpf-postgresql-data
cpf-oracle-data
cpf-redis-data
cpf-kafka-data
cpf-sftp-data
cpf-keycloak-data
```

Bind Data:

```text
C:\dev\Docker\CPF\fixtures\wiremock
C:\dev\Docker\CPF\fixtures\keycloak
C:\dev\Docker\CPF\output\otel
C:\dev\Docker\CPF\output\integration
C:\dev\Docker\CPF\output\trivy
C:\dev\Docker\CPF\output\ort
```

## 6. Port

| Service | 직접 Port | Proxy Port |
|---|---:|---:|
| MariaDB | 3306 | 13306 |
| PostgreSQL | 5432 | 15432 |
| Oracle | 1521 | 11521 |
| Redis | 6379 | 16379 |
| Kafka | 9092 | 19093 |
| WireMock | 18080 | 18090 |
| SFTP | 2222 | 12222 |
| Vault | 8200 | 18200 |
| Keycloak | 18081 | 18091 |
| Toxiproxy API | 8474 | - |
| OTLP gRPC | 4317 | - |
| OTLP HTTP | 4318 | - |
| Collector Metric | 8888 | - |

## 7. 계정·Secret 파일

```text
C:\dev\Docker\Secrets\cpf-runtime.env
C:\dev\Docker\Secrets\redis-password.txt
C:\dev\Docker\Secrets\sftp-password.txt
C:\dev\Docker\Secrets\vault-token.txt
C:\dev\Docker\Secrets\keycloak-admin-password.txt
C:\dev\Docker\Secrets\keycloak-test-password.txt
C:\dev\Docker\Secrets\keycloak-service-client-secret.txt
```

Secret 값은 Image Lock·Manifest·Evidence에 포함하지 않는다.

## 8. 의도적 제외

RabbitMQ·ActiveMQ·IBM MQ·JMS Broker, MySQL·MSSQL·CPF용 H2, MinIO/S3, Prometheus/Grafana, Nexus/Artifactory, ClamAV, SMTP·LDAP, Kubernetes Toolchain은 현재 기본 설치 대상이 아니다. 실제 공식 Adapter·Consumer·Requirement 또는 조직 설비가 확정될 때 별도 승인한다.

External WAS는 정본 목표지만 현재 Source에 WAR Packaging과 Servlet Initializer Consumer가 없다. Tomcat Image만 준비하는 것은 Runtime 완료 근거가 되지 않으므로 기본 환경에 넣지 않고 Source Packaging Gap으로 추적한다. Frontend 독립 Web Server도 공식 Framework 배포 계약이 확정된 뒤 추가한다.

## 9. 변경 경계

- 증분 설치는 기존 DB·Redis·Kafka Container·Volume·Secret을 삭제하지 않는다.
- 기존 실행 중 Service를 임의 중지하지 않는다.
- SFTP·WireMock·Vault·Keycloak을 별도 Compose로 관리한다.
- Base 설치 Script는 새 PC 전체 구성에만 사용한다.
- 버전 변경 시 Compose, Dockerfile, 설치 Script, 상태 Script, Image Lock, 모든 Docker 가이드를 함께 갱신한다.

## 10. 다른 Local Compose와의 관계

`deploy/local/docker-compose.local.yml`은 경량 개발용 선택 자산이고, 본 명세의 전체 환경은 통합 Runtime·Evidence 정본이다. 두 Compose는 동일 Container 이름과 Port를 사용하므로 동시에 실행하지 않는다. 공식 DB 3종과 외부연계·장애·복구 판정에는 `cpf-tools/environment/docker-development-test` 구성을 사용한다.
