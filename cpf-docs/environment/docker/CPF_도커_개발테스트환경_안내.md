# CPF Docker 개발·시험 환경 안내

> **주 독자**: 개발·QA·DBA·플랫폼 운영자
> **완료 결과**: CPF 전체 Module을 정상·오류·부분 실패·재시작·대사까지 시험할 환경 범위를 결정한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 환경 목표](#1-환경-목표)
- [2. 현재 Source가 설치하는 서비스](#2-현재-source가-설치하는-서비스)
- [3. 신규 Starter 검증에 추가할 서비스](#3-신규-starter-검증에-추가할-서비스)
- [4. Resource·Port·보호 범위](#4-resourceport보호-범위)
- [5. 검증 Matrix](#5-검증-matrix)

<!-- CPF-TOC:END -->

## 1. 환경 목표

이 환경은 단순 DB 묶음이 아니라 CPF Core/Common, 38개 Starter, 13개 Profile, ADM/BZA/Gateway/Batch/Generated Domain을 설치·기동·연동·장애 주입·정상화하는 시험 기반이다.

## 2. 현재 Source가 설치하는 서비스

| 서비스 | Image/Container | 기본 Port | 시험 목적 | 설치 상태 |
|---|---|---|---|---|
| MariaDB | mariadb:12.3.2 / cpf-mariadb | 3306 | Fresh/Upgrade/Rollback/Restore | 전체 설치 |
| PostgreSQL | postgres:18.4-trixie / cpf-postgresql | 5432 | 동일 | 전체 설치 |
| Oracle | Oracle Free 26ai / cpf-oracle | 1521 | 동일 | 전체 설치 |
| Redis | redis:8.8.1 / cpf-redis | 6379 | Cache·Lock·Session·Invalidation | 전체 설치 |
| Kafka | apache/kafka:4.3.1 / cpf-kafka | 9092 | Event·Outbox·Inbox·DLQ | 전체 설치 |
| WireMock | wiremock 3.13.2 / cpf-wiremock | Source Compose | REST·Webhook·Timeout | 확장 설치 |
| SFTP | CPF Fixture / cpf-sftp | 22 mapped | Transfer·Resume·Checksum | 확장 설치 |
| Vault | hashicorp/vault 1.21.x / cpf-vault | 8200 | Secret·Rotation | 확장 설치 |
| Keycloak | keycloak 26.6.1 / cpf-keycloak | 8080 mapped | OIDC·Session·Service Identity | 확장 설치 |
| Toxiproxy | 2.12.0 / cpf-toxiproxy | 8474 | Network Fault | 전체 설치 |
| OTel Collector | 0.157.0 / cpf-otel-collector | 4317/4318 등 | Trace·Metric Export | 전체 설치 |

## 3. 신규 Starter 검증에 추가할 서비스

| Capability | 권장 Open Fixture | 필수 시험 | 주의 |
|---|---|---|---|
| RabbitMQ | RabbitMQ Management | Confirm·Return·Quorum·ACK/NACK·DLQ·Network Fault | 실제 Consumer와 Reliability Ledger 연결 |
| Jakarta JMS | Apache ActiveMQ Artemis | Queue/Topic·Transaction·Ack·Redelivery·Durable·DLQ | JMS 계약 시험 |
| IBM MQ | 외부 승인 IBM MQ 또는 제공된 개발 Image/Endpoint | CCDT/Channel·TLS·Reason Code·In-doubt·Reconcile | License·Driver·Credential을 Bundle에 포함하지 않음 |
| TCP/ISO8583 | CPF TCP Simulator | Frame4종·DLE·Secondary Bitmap·MAC·Half-open·Orphan | TLS/mTLS와 기관 조회 |
| Notification Email | Mailpit/MailHog 계열 Fixture | Accept·Bounce/Failure·Receipt·Duplicate·Quiet Hours | 실제 Provider Adapter 계약 |
| SMS | Mock Provider/Webhook Receipt | Idempotency·Receipt·Timeout·Duplicate | SMS SPI Provider별 |
| Object Storage | S3-compatible Fixture | Multipart·Checksum·Version·Retention·Network Fault | 업무 Consumer 연결 후 |
| Malware Scan | 승인 Scanner Fixture | Safe/Quarantine/Reject·Timeout | 실제 Attachment 상태 연결 |

## 4. Resource·Port·보호 범위

- Docker Desktop Linux/amd64
- Java 25·Node 22·PowerShell 7.6.4·Playwright 1.62.0 Toolchain
- DB 3개와 Oracle Memory/Disk 여유
- 모든 Port는 Loopback Binding을 우선한다.
- Secret·Image Lock·Output은 `C:\dev\Docker` 아래 정확한 소유 경로에 둔다.
- Repository Source, 다른 Docker 프로젝트, 전체 Cache/Volume을 정리하지 않는다.

## 5. 검증 Matrix

| 계층 | 정상 | 오류·경계 | 복구 |
|---|---|---|---|
| DB | Fresh/Upgrade/Query | Lock·권한·Disk·부분 Migration | Rollback/Forward Fix/Restore |
| Broker | Publish/Consume | ACK Loss·Rebalance·DLQ·Queue Full | Replay·Reconcile |
| HTTP/Webhook | 200/Receipt | Timeout·5xx·Malformed·Response Loss | Attempt 조회·Retry |
| TCP | Frame/Correlation | Fragment·Merge·Oversize·Half-open | Reconnect·Unknown Result |
| SFTP | Upload/Download | Partial·Checksum·Disconnect | Resume·Atomic Rename |
| Security | Login/Token/HMAC | Expiry·Nonce·Audience·Rotation | 재인증·Key Rotation |
| Observability | Log/Metric/Trace | Collector Down·Backpressure | Buffer/Drop Metric·Recovery |
| Product | ADM/BZA/Gateway/Batch | Permission·Partial·Process Kill | Operation·LKG·Restart·Reconcile |
