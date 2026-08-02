# CPF 도커 개발·테스트 환경 안내

상위 메뉴: [CPF Docker 가이드](README.md)

## 1. 목적

이 환경은 CPF Source·SQL·Frontend·Batch·Messaging·Cache·외부연계·보안·관측·공급망을 동일한 Docker 기반에서 개발하고 재현하기 위한 공통 실행 기반이다. 단순 Image 보유나 Container 상태 확인이 아니라 실제 CPF Consumer를 연결해 정상·오류·부분 실패·재시도·복구를 실행하는 데 사용한다.

## 2. 제공 범위

### 상시 준비 Service

- Oracle, PostgreSQL, MariaDB
- Redis
- Kafka — CPF 공식 MQ/Broker
- WireMock — 외부 REST/FIXED 응답·지연·503·Connection Reset Fixture
- SFTP Fixture — 파일 송수신·ACK/NACK·재처리 경로
- Vault Dev Fixture — Secret Provider·Rotation·Revocation 연결용
- Keycloak Local Fixture — OIDC/OAuth2/JWT·운영자/Service 인증 연결용
- Toxiproxy — DB·Redis·Kafka·외부연계 지연·차단·Reset
- OpenTelemetry Collector — Trace·Metric·Log 수집

### 명령형 Toolchain

- Java 25, Gradle 실행 기반
- Node.js 22, npm
- Playwright Chromium·Firefox·WebKit
- PowerShell 7.6.4, Python 3, Git
- MariaDB Client, `psql`, SQL*Plus
- Docker CLI·Compose, `curl`, `jq`, `openssl`, `zip`, `unzip`
- OpenSSH Client·`sshpass` — SFTP Fixture 연결 확인
- Trivy, OSS Review Toolkit

## 3. 기본 설치에서 제외한 Runtime

| 항목 | 제외 이유 |
|---|---|
| RabbitMQ·ActiveMQ·IBM MQ·JMS Broker | Kafka가 공식 Messaging Primary이며 실제 공식 Consumer가 없음 |
| MySQL·MSSQL·H2 | CPF 공식 DB Vendor가 아님. Keycloak 내부 개발 저장소는 CPF DB Evidence로 계산하지 않음 |
| MinIO·S3·Ceph | 공식 Object Storage Provider와 실제 Consumer가 아직 확정되지 않음 |
| Prometheus·Grafana | 현재 OTel Collector와 Sanitized Output으로 연동 확인 가능. 운영 Stack 확정 전 강제하지 않음 |
| Nexus·Artifactory | REMOTE Artifact Registry는 조직별 외부 설비이며 LOCAL_DEV·OFFLINE과 분리 |
| ClamAV | Virus Policy 계약은 있으나 실제 Scanner Adapter·Consumer가 확정되지 않음 |
| External WAS·Tomcat | 정본에는 WAR 지원 목표가 있으나 현재 Source에 `war`/`bootWar`/`providedRuntime`/`SpringBootServletInitializer` 연결이 없어 빈 WAS만 설치하면 False Green이 됨 |
| Nginx 등 독립 Web Server | Frontend 독립 Artifact 요구는 있으나 공식 Server 제품·배포 계약이 아직 확정되지 않음 |
| Kubernetes·kind·k3d·Helm | 현재 Repository에 실제 배포 Manifest와 실행 Consumer가 없어 Docker 기본 환경과 별도 범위로 유지 |
| SMTP·LDAP·별도 Directory Server | 실제 공식 Adapter·Consumer·Runtime Scenario가 확정되지 않음 |

제외 항목은 기능을 포기한다는 뜻이 아니다. 실제 Owner·Consumer·Dependency·운영 계약이 확정되지 않은 제품을 기본 설치에 임의 포함하지 않는다는 뜻이다.

## 4. 전수 재판정 원칙

환경에 없는 제품은 다음 세 조건을 모두 만족할 때 기본 설치에 추가한다.

1. 최상위 정본에 실제 Runtime Scenario가 있다.
2. Repository Source·Adapter·Build·Script 또는 실제 Consumer가 있다.
3. 설치 후 정상·오류·복구를 판정할 실행 절차가 있다.

WireMock·SFTP·Vault·Keycloak은 위 조건을 만족하므로 확장 환경에 추가했다. Kafka가 이미 공식 MQ 역할을 하므로 별도 MQ Broker는 추가하지 않았다. External WAS는 정본 목표는 있으나 현재 WAR Source 연결이 없으므로 환경 문제가 아니라 Source Packaging Gap으로 분류한다. 빈 Tomcat Container의 기동만으로 External WAS 지원 완료를 표시하지 않는다.

## 5. 설치 경로

Repository 정본:

```text
cpf-tools/environment/docker-development-test/
```

실행 환경:

```text
C:\dev\Docker\CPF
```

Secret:

```text
C:\dev\Docker\Secrets
```

## 6. 설치 선택

- 새 PC 또는 Base 환경이 없는 PC: `CPF_도커_개발테스트환경_전체설치.ps1`
- 기존 7개 Container 환경을 이미 구성한 PC: `CPF_도커_확장연동환경_증분설치.ps1`
- 설치 완료 후 실제 Fixture 준비: `initialize-integration-fixtures.ps1`

기존 PC에서 전체 설치 Script를 반복하지 않는다. 증분 설치는 기존 DB·Redis·Kafka·Image·Volume·Secret을 삭제하거나 재생성하지 않고 WireMock·SFTP·Vault·Keycloak만 추가한다.

## 7. 계정과 Secret 원칙

문서에는 계정명과 Secret 파일 위치만 기록한다. 값은 기록하지 않는다.

| 용도 | 계정·식별자 | Secret 위치 |
|---|---|---|
| DB 관리자 | Vendor 기본 관리자 | `cpf-runtime.env`의 `CPF_ADMIN_PASSWORD` |
| Redis | 별도 사용자 없음 | `redis-password.txt` |
| SFTP | `cpf-sftp` | `sftp-password.txt` |
| Keycloak 관리자 | `cpf-admin` | `keycloak-admin-password.txt` |
| Keycloak 테스트 사용자 | `cpf-reviewer` | `keycloak-test-password.txt` |
| Keycloak Service Client | `cpf-service-local` | `keycloak-service-client-secret.txt` |
| Vault Dev Token | Token 식별자 없음 | `vault-token.txt` |

## 8. 자동 실행자·독립 점검자의 시작 기준

```text
먼저 cpf-docs/guides/docker/README.md와 CPF_도커_개발테스트환경_안내.md를 읽는다. 새 PC이면 전체 구축 가이드, 기존 PC이면 증분 설치 절차를 사용한다. 필요한 Service만 시작하고 실제 CPF Source·SQL·Migration·Client·Consumer를 연결한다. Secret 원문을 출력하거나 Evidence에 포함하지 않는다. 성공한 저비용 Gate를 반복하지 않고 동일 SHA에서 DB·Kafka·Redis·외부연계·장애·복구 Runtime을 수행한다. 실행하지 않은 항목은 완료로 표시하지 않는다.
```

## 9. 다른 Compose와의 경계

Repository의 `deploy/local/docker-compose.local.yml`은 MariaDB·Redis·Kafka만 빠르게 올리는 경량 Local 자산이다. 동일한 Container 이름과 Host Port를 사용하므로 이 전체 개발·테스트 환경과 동시에 실행하지 않는다.

공식 DB 3종, 장애·복구, 외부연계, Browser, 공급망과 통합 Evidence에는 다음 경로를 정본으로 사용한다.

```text
cpf-tools/environment/docker-development-test/
```

`deploy/local` 실행 결과를 Oracle·PostgreSQL·확장 연동 또는 전체 Docker 완료 Evidence로 승계하지 않는다.
