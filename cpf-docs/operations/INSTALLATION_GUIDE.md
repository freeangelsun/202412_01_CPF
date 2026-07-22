# CPF Installation Guide

## 1. Scope

이 문서는 CPF의 신규 설치와 초기 검증 절차를 설명합니다.

## 2. Supported Deployment Models

- Executable JAR
- External WAS WAR
- Docker Container
- Kubernetes
- Modular Monolith
- Independent Microservices

## 3. Prerequisites

### Runtime

- JDK 25
- Linux 또는 Windows Server
- NTP 시간 동기화
- 충분한 file descriptor
- 운영 Log와 spool 전용 Disk

### Database

- MariaDB
- MariaDB — 필수 실검증
- PostgreSQL, Oracle, SQL Server — Vendor별 설치·Migration·Runtime Evidence가 있는 Release에서만 지원

### Optional Infrastructure

- Redis
- Kafka 또는 호환 Broker
- SFTP
- Secret Manager
- Reverse Proxy 또는 Load Balancer

## 4. Service Accounts

서비스별 최소 권한 계정을 분리합니다.

- Application DB user
- Migration DB user
- Batch DB user
- Read-only operation user
- Broker producer/consumer
- SFTP account
- Secret access identity

Application 계정에 DDL 권한을 부여하지 않습니다.

## 5. Configuration

환경별 Profile:

- `local`
- `dev`
- `stg`
- `prod`

설정 우선순위:

1. 안전한 Framework default
2. Profile configuration
3. Environment variable
4. Secret provider
5. 허용된 동적 운영 설정

Password, Token, Private Key와 원문 Secret을 Repository에 저장하지 않습니다.

## 6. Database Installation

```bash
./gradlew cpfInstallDb \
  -PcpfDbVendor=mariadb \
  -PcpfProfile=prod
```

필수 검증:

- schema version
- table·index·constraint
- seed
- 권한
- 재실행 멱등성
- timezone과 charset
- 개인정보 Column
- Migration history

```bash
./gradlew cpfVerifyDb \
  -PcpfDbVendor=mariadb \
  -PcpfProfile=prod
```

## 7. Build Artifacts

```bash
./gradlew clean build
./gradlew assemble
```

검증:

- JAR/WAR
- frontend asset
- sources
- JavaDoc
- checksum
- SBOM
- third-party license
- signature 또는 provenance

## 8. Executable JAR

```bash
java -jar cpf-gateway/build/libs/cpf-gateway.jar \
  --spring.profiles.active=prod
```

서비스별 JVM memory, GC, timezone, log path와 secret provider를 지정합니다.

## 9. External WAS

- WAR packaging
- Servlet context
- JNDI datasource
- classloader policy
- shared library 충돌
- reverse proxy header
- session policy
- graceful shutdown

WAS별 설치 예제는 `cpf-deployment/was`를 사용합니다.

## 10. Docker

```bash
docker compose -f cpf-deployment/docker/compose.yml up -d
```

Image에는 Secret과 운영 설정을 포함하지 않습니다.

검증:

- non-root
- read-only filesystem
- writable log/spool volume
- healthcheck
- resource limit
- image scan

## 11. Kubernetes

- Deployment
- Service
- ConfigMap
- Secret reference
- readiness
- liveness
- startup probe
- PodDisruptionBudget
- HPA
- topology spread
- persistent spool volume
- NetworkPolicy

DB Migration은 Application Pod와 분리된 Job으로 실행합니다.

## 12. Installation Order

1. DB backup 기준점
2. schema·migration
3. Secret·certificate
4. cpf-core dependent service
5. business services
6. cpf-batch
7. cpf-admin·cpf-biz-admin
8. cpf-gateway routing
9. readiness
10. Runtime smoke
11. Monitoring·alert
12. 운영 인수

## 13. Post-install Verification

- 모든 Service health
- Registry
- OpenAPI
- 표준 Header
- Local/Remote 호출
- DB read/write
- file log
- DB log
- Batch worker
- External mock
- Admin login·권한
- Audit
- Masking
- backup

## 14. Hardening

- 기본 Password 제거
- Test endpoint 비활성
- 불필요 Port 차단
- TLS 강제
- mTLS 적용
- 관리자 접근망 제한
- Cookie 보안
- CSP
- Audit 보존
- Log 권한
- File upload 제한
- Secret rotation


## 12. 빈 DB 재구축 계약

회사 PC와 집 PC 모두 기존 CPF Schema/Table이 없다는 전제로 첫 설치를 수행합니다. 기존 Dump나 로컬 잔존 Object를 복구하지 않고 정본 설치 Script가 다음을 생성해야 합니다.

1. 정확한 Schema Allowlist
2. Service User와 최소 Grant
3. Table, PK/FK/UK/Check/Index/Constraint
4. Mandatory Product Meta
5. 선택 EDU/Sample/Test Seed
6. Verify Query와 Schema Version

첫 Cycle은 Reset 없이 Empty Install로 성공해야 합니다.

## 13. 설치 책임 분리

```text
provision
install
product-seed
optional-sample-seed
verify
reset-dry-run/reset-apply
```

`00_all_install.sql` 같은 단일 파괴적 Script를 정본 제품 설치로 사용하지 않습니다. 다른 Application Schema를 wildcard로 삭제하지 않습니다.

## 14. cmnDB와 Owner Schema

- `cmnDB`: 최소 Sample Table 1개
- Batch Runtime: `cpf-batch` Owner Schema
- Fixed-Length Dynamic Layout가 필요하면 Core Owner Schema
- 기관별 External State: `cpf-external` Owner Schema
- BZA Sequence Sample: 선택 설치와 Sample 명칭

## 15. 설치 Evidence

- 빈 DB 전/후 Schema Inventory
- Table/Constraint/Index count와 naming
- Seed row와 idempotent rerun
- Service User permission positive/negative
- 전체 Module Boot
- API/Batch/Admin smoke
- reinstall
