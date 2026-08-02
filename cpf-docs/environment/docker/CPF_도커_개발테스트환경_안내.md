# CPF Docker 개발·시험 환경 안내

상위 메뉴: [Docker 문서](README.md)

## 1. 목적

CPF의 Source·SQL·Generator·Frontend·Batch·Messaging·Cache·외부연계·보안·관측을 동일한 로컬 Container 환경에서 재현한다. Container 상태가 아니라 실제 연결·오류·부분 실패·복구를 실행하는 것이 목적이다.

## 2. 기준과 현재 범위

- 기준 Commit: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- DB: MariaDB, PostgreSQL, Oracle
- Cache: Redis
- 현재 공식 Messaging Source: Kafka Starter
- 확장 Fixture: WireMock, SFTP, Vault, Keycloak
- Fault/Observability: Toxiproxy, OpenTelemetry Collector
- Tooling: Java·Node·Playwright·PowerShell·DB Client·Trivy·ORT

## 3. QA38 Messaging 증분 범위

> **1차 문서 상태**: 아래 RabbitMQ·JMS·IBM MQ 항목은 QA38 개발과 연계할 설치·시험 목표다. 기준 Commit에는 해당 Product Starter가 등록돼 있지 않으며, 이 문서 1차 ZIP에는 Docker Source Script를 포함하지 않는다. 실행 전 `cpf-tools/environment/docker-development-test/`에 명시된 Script·Compose·Fixture가 적용됐는지 확인한다.

| Runtime | 환경 목표 | Product 상태와의 관계 |
|---|---|---|
| RabbitMQ | Source 적용 후 선택 Container 추가 | `cpf-starter-messaging-rabbitmq` 구현·검증용 |
| Apache ActiveMQ Artemis | Source 적용 후 Jakarta JMS Contract Fixture | JMS Product Provider가 구현돼야 사용 가능 |
| IBM MQ Advanced for Developers | Source 적용·License 승인 후 선택 | Proprietary Driver/License를 Framework 기본 Artifact에 포함하지 않음 |

Kafka Default가 RabbitMQ·JMS·IBM MQ 제외를 의미하지 않는다. 다만 Container만 추가하고 Product 기능을 완료로 표시하지 않는다.

## 4. 설치 경로

```text
Repository : cpf-tools/environment/docker-development-test/
Runtime    : C:\dev\Docker\CPF
Secret     : C:\dev\Docker\Secrets
Output     : C:\dev\Docker\CPF\output
```

## 5. Service 지도

| Service | Container | Host Port | 기본 상태 | 용도 |
|---|---|---:|---|---|
| MariaDB | `cpf-mariadb` | 3306 | Created/Stopped | Vendor Lifecycle |
| PostgreSQL | `cpf-postgresql` | 5432 | Created/Stopped | Vendor Lifecycle |
| Oracle | `cpf-oracle` | 1521 | Created/Stopped | Vendor Lifecycle |
| Redis | `cpf-redis` | 6379 | Created/Stopped | Cache/Lock/Invalidation |
| Kafka | `cpf-kafka` | Source 기준 | Created/Stopped | Kafka Contract |
| RabbitMQ | `cpf-rabbitmq` | 5672/15672 | Source 적용 전 미구현 | AMQP Contract |
| Artemis | `cpf-artemis` | 61616/8161 | Source 적용 전 미구현 | Jakarta JMS Contract |
| IBM MQ | `cpf-ibmmq` | 1414/9443 | Source 적용 전 미구현 | IBM MQ Extension |
| WireMock | `cpf-wiremock` | 18080 | Created/Stopped | REST 오류·지연 Fixture |
| SFTP | `cpf-sftp` | 2222 | Created/Stopped | File Transfer |
| Vault | `cpf-vault` | 8200 | Created/Stopped | Secret Provider |
| Keycloak | `cpf-keycloak` | 18081 | Created/Stopped | OIDC/OAuth2/JWT |
| Toxiproxy | `cpf-toxiproxy` | Source 기준 | Created/Stopped | 지연·단절·Reset |
| OTel Collector | `cpf-otel-collector` | Source 기준 | Created/Stopped | Trace·Metric 수집 |

Port는 다른 환경과 충돌할 수 있으므로 Compose `config` 결과를 기준으로 확인한다.

## 6. 계정·Secret

문서에는 ID와 파일 경로만 기록한다.

| Runtime | ID | Secret 파일 |
|---|---|---|
| RabbitMQ | `cpf` | `rabbitmq-password.txt` |
| Artemis | `cpf` | `artemis-password.txt` |
| IBM MQ Admin | Image 정책 | `ibmmq-admin-password.txt` |
| IBM MQ App | `app` | `ibmmq-app-password.txt` |
| SFTP | `cpf-sftp` | `sftp-password.txt` |
| Keycloak Admin | `cpf-admin` | `keycloak-admin-password.txt` |
| Keycloak Test | `cpf-reviewer` | `keycloak-test-password.txt` |
| Vault | Token ID 비공개 | `vault-token.txt` |

## 7. 운영 원칙

1. 필요한 Service만 시작한다.
2. 작업 종료 시 시작한 Service만 중지한다.
3. 기존 Volume·Image·Secret을 삭제하지 않는다.
4. 데이터 초기화는 대상·영향·Backup을 확인한 뒤 별도 승인한다.
5. Container Exit 143은 Stop 과정의 SIGTERM인지 실패인지 Log와 종료 절차로 판정한다.
6. Runtime Source와 `C:\dev\Docker\CPF` 실행본 Hash를 비교한다.

## 8. 완료 판정

- Compose config 성공
- Container 이름·Port 충돌 없음
- Restart Policy `no`
- Health/Readiness 또는 실제 연결 성공
- Publish/Consume, Put/Get, Token, Secret, Fault 시나리오 성공
- StopAfter 후 Running Container 0
- Secret Pattern Scan 0
- 실행 Log의 Exit Code와 Hash 기록
