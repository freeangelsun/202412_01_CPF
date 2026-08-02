# CPF QA38 최종 통합 개발·검증·현행화 요청서

> 이 문서가 QA38 개발과 Codex 검수의 **유일한 상세 요청 정본**이다.  
> 과거 개발 GPT 요청서·날짜별 QA37/QA38 문서가 없어도 이 문서와 동봉 Matrix만으로 작업할 수 있어야 한다.

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작성 기준 Commit: `2e93d92393c52b887482731b683db3c3822027b1`
- 최상위 제품 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement: `169개`
- QA38 상세 Requirement: `156개`
- QA38 Scenario: `52개`
- Core/Common→Starter 판정: `30개`
- Git write: 사용자 승인 전 Commit·Push·Branch·Tag·PR·Release 금지
- 전체 상태: `development_status=부분 구현`, `verification_status=미검증`

## 2. QA 역할

QA 담당자는 요청을 옮기는 문서 작성자가 아니라 CPF 최종 제품 품질 책임자다.

1. 사용자가 직접 요구한 항목을 하나도 누락하지 않는다.
2. 실제 Source·Build·SQL·Generator·Consumer·Test·Runtime을 기준으로 판정한다.
3. Interface·Marker·Dependency·Sample만 존재하는 False Closure를 찾는다.
4. 보안·운영·복구·다중 인스턴스·결과 불명 요건을 선제 추가한다.
5. 새 결함은 Final Matrix에 추가하고 Source/Test/Evidence까지 보완한다.
6. 실행하지 않은 검증은 `미검증`이다.
7. 정본 현행화와 과거 문서 삭제까지 완료하기 전 최종이라고 보고하지 않는다.

## 3. 사용자 요구 강제 체크리스트

| ID | 사용자 요구 | 반영 | 최종 매핑 | 비고 |
|---|---|---|---|---|
| USR-001 | Codex 크레딧 부족으로 Docker·Runtime 후순위 가능하나 최종 검증에서 제외 금지 | 강화 | QA38-VERIFY-*\|QA38-CURRENT-009 | Stage 09~15와 exact-SHA 승계 조건 |
| USR-002 | Codex 작업 문서와 남은 개발·미검증 요구 재정리 | 포함 | 전체 Final Matrix | 최종 요청서가 자체 완결 |
| USR-003 | 여러 AI Push와 최신 Git 결과를 최종 기준으로 반영 | 포함 | QA38-GOV-001\|QA38-SRC-010 | Baseline/Commit impact |
| USR-004 | DB 검수는 Vendor별 Empty 상태 | 포함 | QA38-DB-001\|004\|005\|006 | CPF Object count 0 |
| USR-005 | DB 수정은 Canonical Metadata·Generator Query부터 | 포함 | QA38-DB-002\|003 | Vendor SQL 직접 수정 금지 |
| USR-006 | Codex 검수 이력 지속 갱신 | 포함 | QA38-CURRENT-009 | Continuity/History/Defect ledger |
| USR-007 | 가비지·빈 폴더를 그때그때 정리 | 추가 강화 | QA38-CURRENT-004\|010 | Exact delete와 allowlisted empty-dir cleanup |
| USR-008 | MQ·JMS·TCP 등 과거 지원요건 복구 | 포함 | QA38-MSG-*\|QA38-TCP-* | Provider별 계약과 Runtime |
| USR-009 | RabbitMQ·AMQP 사용자 승인 없는 제외 무효 | 교정 | QA38-MSG-004 | 공식 P0 Provider |
| USR-010 | JMS 사용자 승인 없는 제외 무효 | 교정 | QA38-MSG-005 | 공식 P0 Provider |
| USR-011 | IBM MQ를 JMS와 분리 | 포함 | QA38-MSG-006 | IBM MQ 고유 Queue Manager/Channel/TLS/Reason |
| USR-012 | TPC 원문 보존 및 TCP 연결 | 포함 | QA38-TCP-* | Alias는 Continuity, 제품 ID는 EXS-TCP |
| USR-013 | 사용자 추가 요구를 최상위 정본에 합류 | 확인 | Canonical 169 + Final Matrix | EVENT-MQ/JMS/IBM-MQ/AMQP/EXS-TCP/DB-FRESH/ARCH-STARTER |
| USR-014 | 과거 불필요 문서는 현행화 후 삭제 | 추가 강화 | QA38-CURRENT-002\|003\|004\|008\|010 | 단일 Current와 exact Delete |
| USR-015 | cpf-starters 정식 개발 구조 | 포함 | QA38-STARTER-*\|QA38-GROUP-* | 공식 Root/Leaf/Profile/Aggregate/BOM |
| USR-016 | cpf-core 최대한 경량화 | 포함 | QA38-STARTER-ST-001~030 | 선택 Runtime 이관 |
| USR-017 | Domain이 필요한 Starter만 선택 | 포함 | QA38-GROUP-*\|QA38-CONSUMER-* | Minimal/Profile/optional removal |
| USR-018 | 대표 Starter 하나로 의존 Starter 자동 등록 | 포함 | QA38-GROUP-003~006 | Profile/Aggregate/resolved lock |
| USR-019 | 세부 Starter가 많아도 그룹 등록 | 포함 | QA38-GROUP-* | Mega Starter 금지, 승인 Bundle만 |
| USR-020 | Core뿐 아니라 다른 Domain/제품 영향까지 검토 | 포함 | QA38-CONSUMER-* | ADM/BZA/Gateway/Batch/Reference/Generated Domain |
| USR-021 | QA가 요청서만 옮기지 말고 최종 품질 선제 판단 | 포함 | QA38-GOV-*\|QA38-SRC-* | Architecture/Consumer/Failure/Operations |
| USR-022 | Starter 편입 기능 별도 리뷰 | 포함 | Starter Independent Review + Matrix | Core/Common 이관·기존 7개·신규 Capability |
| USR-023 | 다른 GPT 보호 경로 수정·삭제 금지 | 추가 강화 | QA38-CURRENT-007 | 4 Prefix hard guard |
| USR-024 | 다중 GPT 동시 수정 고려 | 추가 강화 | QA38-CURRENT-005\|006 | Archive then replace/delete |
| USR-025 | 최종 QA 요청서에 모두 상세 포함 | 추가 강화 | QA38-CURRENT-001 | 전체 Requirement appendix 포함 |
| USR-026 | 개발 GPT 문서에 있다고 최종 QA에서 생략 금지 | 추가 강화 | QA38-CURRENT-001\|003 | 최종본 자체 완결 |
| USR-027 | 정리 적용 전에 사용자 DB·Docker Asset 보호 | 포함 | QA38-DB-001\|013 + 보호 경로 | 사용자 DB Reset/Drop·Docker prune 금지 |
| USR-028 | 최종 결과물 Root Overlay ZIP·Hash·한 줄 명령 | 포함 | Package/Script | ZIP/Hash/Manifest |

## 4. 보호 경로

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

읽기·참조만 허용한다. 추가·수정·이동·삭제·Stage·자동 포맷·빈 폴더 정리·Delete Manifest 포함을 금지한다.
환경 변경 필요성은 `CPF_QA38_PROTECTED_OWNER_ACTIONS.md`에만 기록한다.

## 5. 다중 GPT 공동 작업

```text
PROTECTED_READ_ONLY
  다른 GPT 전담.

SHARED_MANAGED
  Current·Codex·State. 로컬 수정은 History에 SHA와 함께 보존 후 최종본으로 현행화.

FINAL_PACKAGE_OWNED
  QA38 최종 요청·Matrix·Review·Manifest. 재실행 멱등.

DELETE_CANDIDATE
  정본·History에 흡수된 과거 파일. exact path만 삭제.
```

보호 경로 또는 baseline 이후 다른 GPT가 변경한 삭제 후보가 있으면 적용 전에 중단한다.
로컬 수정 텍스트 삭제 후보는 Consolidated History에 원문과 SHA를 보존한 뒤 삭제한다.

## 6. 최종 Architecture

```text
cpf-core
  topology-independent Public API/SPI
  Identifier·Header·Context·Error·Validation 값 계약
  Provider-neutral Message/File/TCP/Remote Contract
  Security·Masking·Audit 계약
  순수 Java 최소 구현

cpf-common
  실제 고객 업무 공통
  Code·Calendar·Message·Template·업무 Validation
  기술 Provider Runtime 강제 의존 금지

cpf-starter-base
  Core + 최소 Spring Boot 조립
  Web/DB/Broker/Cache/Session/Resource Server/OpenAPI/Batch 강제 전이 금지

cpf-starter-<capability>-<provider>
  선택 Runtime·Provider·AutoConfiguration·Properties
  Failure Mapping·Security·Operations·Test

Generator Capability Profile
  → 승인 Leaf Starter 목록
  → 실제 Build Dependency
  → Config·Test·Operations Metadata
  → resolvedStarters + Profile/Starter Version Lock

Aggregate Starter
  안정된 조합의 전이 Dependency만 제공
  고유 Bean·AutoConfiguration·업무 정책 금지
```

`cpf-starter-all`, `full`, `everything` 형태의 Mega Starter는 금지한다.

## 7. Core/Common → Starter 최종 판정

| 후보 | 현재 Owner | 현재 기능 | 판정 | 목표 Artifact | 남길 계약 | 실제 Consumer | 우선 |
|---|---|---|---|---|---|---|---|
| ST-001 | cpf-core | CpfDataSourceConfig | MOVE | cpf-starter-persistence-jdbc | DB-neutral datasource contract | ADM/BZA/REF/Generated | P0 |
| ST-002 | cpf-core | CpfMyBatisConfig+dependency/resources | MOVE | cpf-starter-persistence-mybatis | Persistence-neutral SPI | REF/Generated/Product DB | P0 |
| ST-003 | cpf-core | CpfAopConfig+ServiceAccessAspect+AspectJ | MOVE | cpf-starter-aop-service-access | Service access/audit contract | Service/ADM/BZA | P0 |
| ST-004 | cpf-core | CpfOpenApiAutoConfiguration+Springdoc+Scalar | MOVE | cpf-starter-openapi-webmvc | OpenAPI metadata contract | ADM/BZA/REF/Domain | P0 |
| ST-005 | cpf-core | CpfSecurityAutoConfiguration | SPLIT_MOVE | cpf-starter-security-resource-server | Security principal/permission contract | API Domain/Gateway | P0 |
| ST-006 | cpf-starters/security | BFF/JDBC Security Runtime | SPLIT_MOVE | cpf-starter-security-session-jdbc | Generic session contract | ADM/BZA | P0 |
| ST-007 | cpf-core | Broker Worker/Bridge/JDBC Reliability Repository | MOVE | cpf-starter-messaging-reliability-jdbc | Provider-neutral message contract | Kafka/Rabbit/JMS/Batch | P0 |
| ST-008 | cpf-core | JdbcCpfChannelRegistryAdapter | MOVE | cpf-starter-channel-registry-jdbc | Channel registry SPI | Runtime/Gateway/Agent | P0 |
| ST-009 | cpf-core | Logging+OTel SDK/Exporter Runtime | SPLIT_MOVE | cpf-starter-observability + cpf-starter-observability-otlp | Trace/metric/log contract | 모든 Runtime | P0 |
| ST-010 | cpf-core | Remote HTTP Runtime | MOVE | cpf-starter-http-client | Typed remote client contract | ADM/Gateway/Batch/Domain | P0 |
| ST-011 | cpf-core/common | Validation Runtime Provider | MOVE | cpf-starter-validation | Validation API/values | ADM/BZA/Domain | P1 |
| ST-012 | cpf-core | Fixed-length Spring Component | SPLIT | cpf-integration-fixedlength-core + cpf-starter-integration-fixedlength | Pure codec contract | TCP/REF/EDU | P0 |
| ST-013 | cpf-core | FileExchange/SFTP Planned Runtime | MOVE_IMPLEMENT | cpf-starter-integration-sftp | File transfer SPI | Batch/Institution/REF | P0 |
| ST-014 | cpf-core | Commons Compress/Archive Runtime | MOVE | cpf-starter-file-archive | Archive/file contract | File/Attachment | P1 |
| ST-015 | cpf-common | Redis/Valkey Connection·Template·Listener | MOVE | cpf-starter-cache-valkey | Business cache abstraction | Common/ADM/BZA/Domain | P0 |
| ST-016 | cpf-common | Caffeine Runtime | MOVE | cpf-starter-cache-caffeine | Business cache abstraction | Common/Domain | P0 |
| ST-017 | cpf-common | POI/XLSX Runtime | MOVE | cpf-starter-tabular-poi | Tabular contract | ADM/BZA/Domain | P1 |
| ST-018 | cpf-core | Service Identity Runtime | MOVE | cpf-starter-security-service-identity | Identity/mTLS/OIDC contract | Gateway/Batch/Agent | P0 |
| ST-019 | cpf-core | Registry/Health technical client | MOVE | cpf-starter-runtime-registry-client | Registry/health contract | Gateway/Runtime/Agent | P0 |
| ST-020 | cpf-starters/secret | Secret Provider Runtime | EXPAND | cpf-starter-secret + provider plugins | Secret registry contract | 모든 Secret Consumer | P0 |
| ST-021 | cpf-starters/featureflag | OpenFeature Runtime | EXPAND | cpf-starter-featureflag + provider plugins | Feature flag contract | ADM/BZA/Domain | P1 |
| ST-022 | cpf-starters/resilience | CircuitBreaker-only Runtime | EXPAND | cpf-starter-resilience | Deadline/retry/unknown-result contract | Gateway/Batch/Domain | P0 |
| ST-023 | cpf-starters/messaging-kafka | Kafka Producer/Consumer Runtime | EXPAND_MIGRATE | cpf-starter-messaging-kafka | Provider-neutral message port | Batch/Domain/REF | P0 |
| ST-024 | 누락 | RabbitMQ/AMQP | NEW | cpf-starter-messaging-rabbitmq | Provider-neutral MQ+AMQP extension | Domain/Batch/Bridge/REF | P0 |
| ST-025 | 누락 | Jakarta JMS | NEW | cpf-starter-messaging-jms | Provider-neutral MQ+JMS extension | Domain/Batch/REF | P0 |
| ST-026 | 누락 | IBM MQ Provider | NEW_PLUGIN | cpf-starter-messaging-ibm-mq | JMS+IBM MQ extension SPI | Institution Domain | P0 |
| ST-027 | 누락 | TCP Transport Runtime | NEW | cpf-starter-integration-tcp | TCP transport contract | Institution/Batch/REF | P0 |
| ST-028 | 누락/부분 | Notification Email/SMS Worker | NEW_SPLIT | cpf-starter-notification + email + sms-spi | Notification contract | ADM/BZA/Domain | P0 |
| ST-029 | 선택 기능 | Quartz Scheduler | NEW_OPTIONAL | cpf-starter-scheduler-quartz | Scheduler SPI | 고급 Batch Consumer | P1 |
| ST-030 | cpf-core/common | Public API/SPI·Identifiers·Context·Error·Masking·업무 Common | KEEP | cpf-core / cpf-common | Topology-independent contract and business common | 모든 Consumer | P0 |

## 8. Messaging·MQ·JMS·IBM MQ·RabbitMQ

Kafka는 Default Messaging Profile이지만 다른 공식 Provider를 제거하지 않는다.

### 공통 Reliability
- Logical Destination과 Provider Binding 분리
- Versioned Envelope·Message ID·Correlation·Attempt
- Outbox·Inbox·Dedup·DLQ·Replay·Reconcile JDBC Ledger
- ACK/Commit/Redelivery/Ordering/TTL/Priority/Schema
- Result Unknown과 무조건 재시도 금지
- TLS/mTLS·Secret Rotation·Masking·Permission·Audit
- Backlog·Lag·DLQ·Replay·승인·결과 추적

### RabbitMQ/AMQP
공식 P0 Starter다. 사용자 승인 없는 제외 결정을 인정하지 않는다.
Exchange·Queue·Binding·Routing, Confirm·Return·Mandatory, ACK/NACK/Requeue, Retry·DLX·DLQ,
Classic/Quorum, Prefetch·Backpressure, Connection Recovery, Multi-instance, Health·Metric·Replay를 제공한다.

### Jakarta JMS
Queue·Topic, Durable Subscription·Selector, Ack Mode·Session Transaction, Redelivery,
Exception Listener, Provider-neutral Error Mapping과 실제 Provider Matrix를 제공한다.

### IBM MQ
JMS와 뭉개지 않는다. Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect·In-doubt·Reconcile을
Provider Extension으로 제공하며 Proprietary Server/Driver를 기본 번들하지 않는다.

### Multi-provider
일반 Domain은 Default Binding 하나를 선택한다. Bridge·Migration·기관연계는 Kafka+RabbitMQ/JMS를
Named Binding으로 함께 사용할 수 있다. 이름 없는 Default Client가 둘 이상이면 Startup fail-closed다.

## 9. TCP·전문·ISO8583

Client/Server·Pool·Drain, Fixed/Length/STX-ETX/CRLF, Fragment/Coalesce, Binary/BCD/Hex,
UTF-8/EUC-KR/EBCDIC, Correlation·Out-of-order, Heartbeat·Half-open·Reconnect,
TLS/mTLS, Resource Limit, Write 후 응답 유실 `UNKNOWN_RESULT`, Reconcile, ISO8583와 Simulator를 구현한다.

`TPC` 원문은 Continuity Alias로 보존하고 제품 Requirement ID는 `EXS-TCP`를 사용한다.

## 10. 파일·외부연계·Notification

SFTP는 `PLANNED` 응답만으로 완료가 아니다. 실제 transfer/resume/checksum/reconcile을 구현한다.
Typed HTTP·SOAP·gRPC·Webhook·WebSocket/SSE와 Notification Outbox/Worker/Retry/DLQ,
Email SMTP/Template/Attachment/Bounce, SMS Provider/Receipt/Unknown Result를 포함한다.

## 11. DB 영구 규칙

각 Vendor Stage는 전용 QA Database/Schema의 `CPF Object count = 0` 증명으로 시작한다.

```text
Canonical Schema·Metadata·Runtime Query
→ Generator·Golden Template
→ MariaDB/PostgreSQL/Oracle Source
→ Install·Migration·Rollback·Runtime Pack
→ Java Consumer·Test
→ 실제 Fresh DB
```

생성 Vendor SQL 우선 수정, 사용자 DB Reset/Drop, 오염 DB Fresh 처리, 한 Vendor만 수정,
빈 Rollback, Generator/cpf-member 수동 이중 보정을 금지한다.

Lifecycle: Fresh, Install, Metadata/Seed, Arbitrary Domain, Runtime Query, Upgrade,
Rollback/Forward Recovery, Reapply, Different-hash, Optional Pack, Drift, Backup/Restore,
Process Kill, Cleanup.

## 12. Codex Stage·크레딧

```text
00 BASELINE
01 CANONICAL
02 STATIC
03 CORE_STARTER
04 MESSAGING_TCP
05 DB_TOOLING
06 DB_MARIADB
07 DB_POSTGRESQL
08 DB_ORACLE
09 JAVA_FULL
10 FRONTEND
11 RUNTIME
12 FAULT_OTEL
13 BROWSER
14 SUPPLY_CHAIN
15 TRUTH
```

Docker·Browser·Supply-chain은 Source 안정화 후 실행하되 생략하지 않는다.
같은 HEAD·Command Hash·Source/Config/SQL/Profile·Environment·Log Hash·Artifact Hash일 때만 PASS를 승계한다.

## 13. 현행화·정리

현행 경로:
- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- Final Requirement/Scenario Matrix
- Pre/Starter/Post Review
- Codex Start/Request/State
- Consolidated History
- Handover

과거 날짜별 Current·Review·Matrix·Manifest·Script는 흡수 후 exact Delete Manifest로 제거한다.

## 14. 완료 판정

모든 P0 개발·검증, Core/Common 이관·Legacy 제거, 실제 Consumer, MQ/JMS/IBM MQ/TCP/File/Notification,
DB 3 Vendor Empty Lifecycle, Generator/Profile/BOM/SBOM, Multi-instance/Fault/Unknown,
Current 단일화·과거 문서 삭제, 보호 경로 0, exact-SHA Evidence가 모두 닫혀야 한다.

## 15. 전체 상세 Requirement Catalog

### Governance

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-GOV-001 | P0 | 00_BASELINE | Latest master exact SHA | 실행 시점 origin/master와 Working Tree/최근 Commit Range를 고정 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 재확인 필요 |
| QA38-GOV-002 | P0 | 00_BASELINE | 다른 GPT 보호 경로 | 4개 보호 Prefix를 읽기 전용으로 강제 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 완료 | 미검증 |
| QA38-GOV-003 | P0 | 00_BASELINE | 개발 GPT 62개 Backlog Import | 로컬 원본 62개를 행 단위 Import·Crosswalk | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 미구현 | 미검증 |
| QA38-GOV-004 | P0 | 00_BASELINE | Canonical 169 확인 | 7개 ID·TPC Alias·Provider Matrix를 로컬 정본과 대조 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-005 | P0 | 01_CANONICAL | 승인 없는 Requirement 제거 금지 | 사용자 승인 Evidence 없는 removed/superseded를 무효화 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-006 | P0 | 01_CANONICAL | 과거 원장 보존 | Canonical/Enterprise/EDU/QA37 유효 Requirement를 대체하지 않음 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-007 | P0 | 01_CANONICAL | 상태 축 분리 | development_status와 verification_status 분리 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 완료 | 재확인 필요 |
| QA38-GOV-008 | P0 | 01_CANONICAL | Source 독립 검토 | 문서·보고가 아닌 실제 Source/Build/SQL/Consumer/Test 검토 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-009 | P0 | 01_CANONICAL | Source Defect와 환경 Blocker | 환경 부재로 구현 누락을 숨기지 않음 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-010 | P0 | 01_CANONICAL | Codex Continuity | 중단 전 exact 재개 지점·명령·상태 기록 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
| QA38-GOV-011 | P0 | 01_CANONICAL | AI 결정 승인 상태 | Architecture Decision에 사용자 승인 상태 기록 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 미구현 | 미검증 |
| QA38-GOV-012 | P0 | 01_CANONICAL | 중복·가비지 관리 | 정본 승격 후 exact allowlist만 정리 | 승인 없는 누락 0, exact SHA와 원장 Crosswalk 100% | Git/CSV/Canonical/Continuity Gate | 부분 구현 | 미검증 |
### Currentization

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-CURRENT-001 | P0 | 01_CANONICAL | 최종 QA 요청서 자체 완결성 | CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md 하나에 Canonical, 개발요건, Starter 이관, Messaging/TCP, DB, 검증 Stage, 완료 기준을 모두 포함하며 과거 개발 GPT 문서가 없어도 작업 가능해야 한다. | 최종 요청서가 외부 과거 요청서의 필수 내용을 참조로만 남기지 않고 자체 포함 | 문서 내용·Requirement ID·링크 독립성 검사 | 완료 | 미검증 |
| QA38-CURRENT-002 | P0 | 01_CANONICAL | Current 진입점 단일화 | CPF_CURRENT_WORK_REQUEST.md와 CODEX_START_HERE.md가 동일한 최종 QA38 요청서·Matrix·Review·Continuity만 가리키도록 현행화한다. | 활성 Current 진입점 1개, Codex 시작점 1개, 서로 다른 활성 요청서 참조 0 | active reference scan | 부분 구현 | 미검증 |
| QA38-CURRENT-003 | P0 | 01_CANONICAL | 과거 Requirement 흡수·Continuity | Canonical 169, 기존 62개 Backlog, 과거 QA37 요구, RabbitMQ/JMS/IBM MQ/TCP 복구 요구를 최종 Matrix와 History에 흡수하고 원 ID 추적을 유지한다. | 복구·승계 대상별 최종 Requirement ID가 존재하고 승인 없는 삭제 0 | coverage reconciliation + ID uniqueness | 완료 | 미검증 |
| QA38-CURRENT-004 | P0 | 15_TRUTH | 대체 완료 과거 파일 exact 삭제 | 최종 정본과 History에 내용이 흡수된 날짜별 Current·Review·Matrix·Manifest·Script를 exact path Delete Manifest로 삭제한다. | 승인된 exact 경로만 삭제, 보호 경로 0, 활성 참조가 남은 파일 삭제 0 | delete preflight + reference scan + git status | 부분 구현 | 미검증 |
| QA38-CURRENT-008 | P0 | 15_TRUTH | 적용 후 참조·중복 검증 | 삭제 후 활성 문서의 끊어진 참조, 과거 Current 파일 참조, 중복 Requirement ID, 중복 Codex 시작점과 남은 날짜별 활성 요청을 검사한다. | broken active reference 0, duplicate active request 0, duplicate requirement ID 0 | git grep/reference graph/CSV schema gate | 부분 구현 | 미검증 |
| QA38-CURRENT-009 | P0 | 15_TRUTH | Codex 이력 지속 갱신 | 실행·결함·검증 History와 Continuity를 각 Stage 종료 및 크레딧 중단 전 갱신한다. | 첫 미완료 Requirement와 정확한 재개 명령이 항상 존재 | continuity schema/hash gate | 부분 구현 | 미검증 |
| QA38-CURRENT-010 | P0 | 15_TRUTH | 빈 폴더·가비지 정리 | exact Delete 완료 후 허용된 과거 작업 폴더만 빈 경우 제거하고 Source·Evidence·보호 경로는 건드리지 않는다. | 허용 목록 외 폴더 삭제 0, 빈 과거 QA 폴더 0 | allowed-empty-directory cleanup + git status | 부분 구현 | 미검증 |
### Multi-GPT

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-CURRENT-005 | P0 | 00_BASELINE | 다중 GPT 경로 분류 | 경로를 PROTECTED_READ_ONLY, SHARED_MANAGED, FINAL_PACKAGE_OWNED, DELETE_CANDIDATE로 분류하고 적용 정책을 고정한다. | 모든 Overlay·Delete 경로가 하나의 Merge Mode를 가지며 미분류 0 | shared path policy validation | 완료 | 미검증 |
| QA38-CURRENT-006 | P0 | 00_BASELINE | 동시 수정 내용 보존 후 현행화 | 공동 관리 Current·Codex·State 또는 삭제 후보가 로컬에서 수정됐으면 내용을 Consolidated History에 SHA와 함께 보존한 뒤 최종 정본으로 교체·삭제한다. | 동시 수정 내용 유실 0, 최종 활성 문서 중복 0 | local-modified fixture + archive/replacement test | 완료 | 미검증 |
### Protected Path

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-CURRENT-007 | P0 | 00_BASELINE | 다른 GPT 보호 경로 전면 차단 | deliverables, guides, environment/docker, docker-development-test 경로는 참조만 허용하고 Overlay·Stage·Delete·빈 폴더 정리에서 제외한다. | 보호 경로 포함 파일·삭제 후보·수정 건수 0 | prefix guard positive/negative fixture | 완료 | 미검증 |
### Source Audit

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-SRC-001 | P0 | 02_STATIC | Dependency Graph | 모든 Module·Starter의 direct/transitive dependency와 API 노출 | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-002 | P0 | 02_STATIC | Import Graph | Provider SDK/Internal package 직접 참조 | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-003 | P0 | 02_STATIC | AutoConfiguration Graph | Imports/Condition/Properties/Bean 충돌·Backoff | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-004 | P0 | 02_STATIC | Consumer Graph | API/SPI/Starter의 실제 Source·Build·Runtime Consumer | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-005 | P0 | 02_STATIC | SQL Graph | Canonical/Generator/Vendor/Mapper/Inline SQL Ownership | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-006 | P0 | 02_STATIC | Generator Graph | Template/Golden/임의 Domain/Profile/Manifest parity | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-007 | P0 | 02_STATIC | Test Graph | Requirement→Unit/Contract/Integration/Runtime/Fault/Browser | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-008 | P0 | 02_STATIC | Artifact Graph | POM/BOM/JAR/WAR/SBOM/Final Content | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-009 | P0 | 02_STATIC | Frontend Graph | ADM/BZA Route/API/Permission/Generated Client | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
| QA38-SRC-010 | P0 | 02_STATIC | Recent Commit Impact | QA37 baseline 이후 전체 Commit의 Source·Docs·Environment 영향 | 미분류 경로·Owner·Consumer 0 | local code/dependency/sql/test scan | 부분 구현 | 미검증 |
### Core/Common→Starter

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-STARTER-001 | P0 | 03_CORE_STARTER | CpfDataSourceConfig MOVE | `cpf-core`의 `CpfDataSourceConfig`를 `cpf-starter-persistence-jdbc` 기준으로 MOVE하고, 남길 계약은 `DB-neutral datasource contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Canonical DB/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-002 | P0 | 03_CORE_STARTER | CpfMyBatisConfig+dependency/resources MOVE | `cpf-core`의 `CpfMyBatisConfig+dependency/resources`를 `cpf-starter-persistence-mybatis` 기준으로 MOVE하고, 남길 계약은 `Persistence-neutral SPI`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Mapper/Runtime Query/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-003 | P0 | 03_CORE_STARTER | CpfAopConfig+ServiceAccessAspect+AspectJ MOVE | `cpf-core`의 `CpfAopConfig+ServiceAccessAspect+AspectJ`를 `cpf-starter-aop-service-access` 기준으로 MOVE하고, 남길 계약은 `Service access/audit contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-004 | P0 | 03_CORE_STARTER | CpfOpenApiAutoConfiguration+Springdoc+Scalar MOVE | `cpf-core`의 `CpfOpenApiAutoConfiguration+Springdoc+Scalar`를 `cpf-starter-openapi-webmvc` 기준으로 MOVE하고, 남길 계약은 `OpenAPI metadata contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-005 | P0 | 03_CORE_STARTER | CpfSecurityAutoConfiguration SPLIT_MOVE | `cpf-core`의 `CpfSecurityAutoConfiguration`를 `cpf-starter-security-resource-server` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Security principal/permission contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Security metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-006 | P0 | 03_CORE_STARTER | BFF/JDBC Security Runtime SPLIT_MOVE | `cpf-starters/security`의 `BFF/JDBC Security Runtime`를 `cpf-starter-security-session-jdbc` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Generic session contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Session Schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-007 | P0 | 03_CORE_STARTER | Broker Worker/Bridge/JDBC Reliability Repository MOVE | `cpf-core`의 `Broker Worker/Bridge/JDBC Reliability Repository`를 `cpf-starter-messaging-reliability-jdbc` 기준으로 MOVE하고, 남길 계약은 `Provider-neutral message contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Outbox/Inbox/DLQ/Replay 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-008 | P0 | 03_CORE_STARTER | JdbcCpfChannelRegistryAdapter MOVE | `cpf-core`의 `JdbcCpfChannelRegistryAdapter`를 `cpf-starter-channel-registry-jdbc` 기준으로 MOVE하고, 남길 계약은 `Channel registry SPI`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Registry schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-009 | P0 | 03_CORE_STARTER | Logging+OTel SDK/Exporter Runtime SPLIT_MOVE | `cpf-core`의 `Logging+OTel SDK/Exporter Runtime`를 `cpf-starter-observability + cpf-starter-observability-otlp` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Trace/metric/log contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Log DB 선택)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-010 | P0 | 03_CORE_STARTER | Remote HTTP Runtime MOVE | `cpf-core`의 `Remote HTTP Runtime`를 `cpf-starter-http-client` 기준으로 MOVE하고, 남길 계약은 `Typed remote client contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-012 | P0 | 03_CORE_STARTER | Fixed-length Spring Component SPLIT | `cpf-core`의 `Fixed-length Spring Component`를 `cpf-integration-fixedlength-core + cpf-starter-integration-fixedlength` 기준으로 SPLIT하고, 남길 계약은 `Pure codec contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Layout metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-013 | P0 | 03_CORE_STARTER | FileExchange/SFTP Planned Runtime MOVE_IMPLEMENT | `cpf-core`의 `FileExchange/SFTP Planned Runtime`를 `cpf-starter-integration-sftp` 기준으로 MOVE_IMPLEMENT하고, 남길 계약은 `File transfer SPI`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Transfer Ledger)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-015 | P0 | 03_CORE_STARTER | Redis/Valkey Connection·Template·Listener MOVE | `cpf-common`의 `Redis/Valkey Connection·Template·Listener`를 `cpf-starter-cache-valkey` 기준으로 MOVE하고, 남길 계약은 `Business cache abstraction`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Cache metadata optional)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-016 | P0 | 03_CORE_STARTER | Caffeine Runtime MOVE | `cpf-common`의 `Caffeine Runtime`를 `cpf-starter-cache-caffeine` 기준으로 MOVE하고, 남길 계약은 `Business cache abstraction`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-018 | P0 | 03_CORE_STARTER | Service Identity Runtime MOVE | `cpf-core`의 `Service Identity Runtime`를 `cpf-starter-security-service-identity` 기준으로 MOVE하고, 남길 계약은 `Identity/mTLS/OIDC contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Identity metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-019 | P0 | 03_CORE_STARTER | Registry/Health technical client MOVE | `cpf-core`의 `Registry/Health technical client`를 `cpf-starter-runtime-registry-client` 기준으로 MOVE하고, 남길 계약은 `Registry/health contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Registry metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-020 | P0 | 03_CORE_STARTER | Secret Provider Runtime EXPAND | `cpf-starters/secret`의 `Secret Provider Runtime`를 `cpf-starter-secret + provider plugins` 기준으로 EXPAND하고, 남길 계약은 `Secret registry contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Secret reference metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-022 | P0 | 03_CORE_STARTER | CircuitBreaker-only Runtime EXPAND | `cpf-starters/resilience`의 `CircuitBreaker-only Runtime`를 `cpf-starter-resilience` 기준으로 EXPAND하고, 남길 계약은 `Deadline/retry/unknown-result contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-023 | P0 | 03_CORE_STARTER | Kafka Producer/Consumer Runtime EXPAND_MIGRATE | `cpf-starters/messaging-kafka`의 `Kafka Producer/Consumer Runtime`를 `cpf-starter-messaging-kafka` 기준으로 EXPAND_MIGRATE하고, 남길 계약은 `Provider-neutral message port`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-024 | P0 | 03_CORE_STARTER | RabbitMQ/AMQP NEW | `누락`의 `RabbitMQ/AMQP`를 `cpf-starter-messaging-rabbitmq` 기준으로 NEW하고, 남길 계약은 `Provider-neutral MQ+AMQP extension`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
| QA38-STARTER-025 | P0 | 03_CORE_STARTER | Jakarta JMS NEW | `누락`의 `Jakarta JMS`를 `cpf-starter-messaging-jms` 기준으로 NEW하고, 남길 계약은 `Provider-neutral MQ+JMS extension`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
| QA38-STARTER-026 | P0 | 03_CORE_STARTER | IBM MQ Provider NEW_PLUGIN | `누락`의 `IBM MQ Provider`를 `cpf-starter-messaging-ibm-mq` 기준으로 NEW_PLUGIN하고, 남길 계약은 `JMS+IBM MQ extension SPI`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
| QA38-STARTER-027 | P0 | 03_CORE_STARTER | TCP Transport Runtime NEW | `누락`의 `TCP Transport Runtime`를 `cpf-starter-integration-tcp` 기준으로 NEW하고, 남길 계약은 `TCP transport contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Request/Reconcile Ledger optional)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
| QA38-STARTER-028 | P0 | 03_CORE_STARTER | Notification Email/SMS Worker NEW_SPLIT | `누락/부분`의 `Notification Email/SMS Worker`를 `cpf-starter-notification + email + sms-spi` 기준으로 NEW_SPLIT하고, 남길 계약은 `Notification contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Outbox/Delivery/Result)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
| QA38-STARTER-030 | P0 | 03_CORE_STARTER | Public API/SPI·Identifiers·Context·Error·Masking·업무 Common KEEP | `cpf-core/common`의 `Public API/SPI·Identifiers·Context·Error·Masking·업무 Common`를 `cpf-core / cpf-common` 기준으로 KEEP하고, 남길 계약은 `Topology-independent contract and business common`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(공통 Metadata만)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-011 | P1 | 03_CORE_STARTER | Validation Runtime Provider MOVE | `cpf-core/common`의 `Validation Runtime Provider`를 `cpf-starter-validation` 기준으로 MOVE하고, 남길 계약은 `Validation API/values`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-014 | P1 | 03_CORE_STARTER | Commons Compress/Archive Runtime MOVE | `cpf-core`의 `Commons Compress/Archive Runtime`를 `cpf-starter-file-archive` 기준으로 MOVE하고, 남길 계약은 `Archive/file contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-017 | P1 | 03_CORE_STARTER | POI/XLSX Runtime MOVE | `cpf-common`의 `POI/XLSX Runtime`를 `cpf-starter-tabular-poi` 기준으로 MOVE하고, 남길 계약은 `Tabular contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-021 | P1 | 03_CORE_STARTER | OpenFeature Runtime EXPAND | `cpf-starters/featureflag`의 `OpenFeature Runtime`를 `cpf-starter-featureflag + provider plugins` 기준으로 EXPAND하고, 남길 계약은 `Feature flag contract`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Flag/Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 부분 구현 | 미검증 |
| QA38-STARTER-029 | P1 | 03_CORE_STARTER | Quartz Scheduler NEW_OPTIONAL | `선택 기능`의 `Quartz Scheduler`를 `cpf-starter-scheduler-quartz` 기준으로 NEW_OPTIONAL하고, 남길 계약은 `Scheduler SPI`으로 제한한다. | Target Artifact·Consumer·DB/Generator 영향(Scheduler schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘 | dependency/API/consumer/fresh fixture/runtime | 미구현 | 미검증 |
### Existing Starter

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-EXISTING-001 | P0 | 03_CORE_STARTER | Security | Session JDBC/Resource Server/Service Identity 분리; ADM/BZA Route 정책 Product 이관 | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-002 | P0 | 03_CORE_STARTER | Kafka | Consumer/Listener/Offset/Rebalance/DLT/Operations 완성; Batch 직접 Kafka 이관 | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-003 | P0 | 03_CORE_STARTER | Cache | Caffeine와 Valkey/Redis 분리; Common 기술 Runtime 제거 | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-004 | P0 | 03_CORE_STARTER | Observability | Observation과 OTLP Exporter 분리; collector 장애 격리 | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-005 | P0 | 03_CORE_STARTER | Resilience | timeout/retry/time-limiter/bulkhead/rate/backpressure/unknown-result | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-006 | P0 | 03_CORE_STARTER | Feature Flag | Provider lifecycle/secure override/audit/multi-instance/actual consumer | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
| QA38-EXISTING-007 | P0 | 03_CORE_STARTER | Secret | Provider catalog/local-dev vs product fail-closed/rotation/revocation/health | 실제 Consumer·Failure·Security·Operations·Publication·optional-removal | ApplicationContextRunner+consumer+runtime+artifact | 부분 구현 | 미검증 |
### Starter Group/Generator

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-GROUP-001 | P0 | 03_CORE_STARTER | Leaf Naming | cpf-starter-<capability>-<provider>와 가독성 있는 package | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-002 | P0 | 03_CORE_STARTER | Base Starter | Core+최소 Boot만 조립, Web/DB/Broker 자동 포함 금지 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-003 | P0 | 03_CORE_STARTER | Capability Profile | Use-case→승인 Leaf Starter 목록 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-004 | P0 | 03_CORE_STARTER | Aggregate Starter | 전이 Dependency만 제공, Source/Bean/Policy 금지 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-005 | P0 | 03_CORE_STARTER | Resolved Lock | resolvedStarters·Profile Version·Artifact Version | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-006 | P0 | 03_CORE_STARTER | Named Binding | 복수 Provider 이름·Default 최대 하나 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-007 | P0 | 03_CORE_STARTER | Conflict Gate | 모호한 Bean/상호 배타 설정 fail-closed | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-008 | P0 | 03_CORE_STARTER | BOM | Version 정렬만, Runtime 비활성 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 부분 구현 | 미검증 |
| QA38-GROUP-009 | P0 | 03_CORE_STARTER | Publication | POM/Sources/JavaDoc/SBOM/Signature/Artifact Catalog | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-010 | P0 | 03_CORE_STARTER | Optional Removal | Leaf/Profile 제거 Compile·Runtime | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-011 | P0 | 03_CORE_STARTER | Footprint | JAR/Dependency/Startup/Memory/Thread Budget | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-012 | P0 | 03_CORE_STARTER | Upgrade | Profile/Starter Mixed Version·Migration·Rollback | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
| QA38-GROUP-013 | P0 | 03_CORE_STARTER | Generator | Build/Config/Test/Operations/Manifest 원자 생성 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 부분 구현 | 미검증 |
| QA38-GROUP-014 | P0 | 03_CORE_STARTER | Legacy Removal | 기존 Core/Common/Product Bean·Dependency·Config·SQL 제거 | Profile/Build/Manifest/Artifact/Runtime 양방향 일치 | fresh generated domain+dependency report+negative combinations | 미구현 | 미검증 |
### Messaging

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-MSG-001 | P0 | 04_MESSAGING_TCP | Provider-neutral Message Envelope·Logical Destination·Binding·ACK/Commit·Unknown Result | Provider-neutral Message Envelope·Logical Destination·Binding·ACK/Commit·Unknown Result | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 부분 구현 | 미검증 |
| QA38-MSG-002 | P0 | 04_MESSAGING_TCP | JDBC Outbox·Inbox·DLQ·Replay·Reconcile Ledger 3 Vendor | JDBC Outbox·Inbox·DLQ·Replay·Reconcile Ledger 3 Vendor | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 부분 구현 | 미검증 |
| QA38-MSG-003 | P0 | 04_MESSAGING_TCP | Kafka Default Profile의 publish/consume/rebalance/offset/DLT/operations | Kafka Default Profile의 publish/consume/rebalance/offset/DLT/operations | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 부분 구현 | 미검증 |
| QA38-MSG-004 | P0 | 04_MESSAGING_TCP | Exchange·Queue·Binding·Routing·Confirm·Return·ACK/NACK·DLX·Quorum | Exchange·Queue·Binding·Routing·Confirm·Return·ACK/NACK·DLX·Quorum | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-005 | P0 | 04_MESSAGING_TCP | Queue·Topic·Durable·Selector·Ack Mode·Transaction·Redelivery | Queue·Topic·Durable·Selector·Ack Mode·Transaction·Redelivery | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-006 | P0 | 04_MESSAGING_TCP | Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect Extension | Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect Extension | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-007 | P0 | 04_MESSAGING_TCP | Kafka+Rabbit/JMS Named Binding·Bridge·Migration·모호성 fail-closed | Kafka+Rabbit/JMS Named Binding·Bridge·Migration·모호성 fail-closed | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-008 | P0 | 04_MESSAGING_TCP | Schema Version·Compatibility·Quarantine | Schema Version·Compatibility·Quarantine | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-009 | P0 | 04_MESSAGING_TCP | TLS/mTLS·Secret Rotation·Masking·Permission·Audit | TLS/mTLS·Secret Rotation·Masking·Permission·Audit | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-010 | P0 | 04_MESSAGING_TCP | Backlog·Lag·DLQ·Replay·Reconcile·Approval·Result Tracking | Backlog·Lag·DLQ·Replay·Reconcile·Approval·Result Tracking | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
| QA38-MSG-011 | P0 | 04_MESSAGING_TCP | Broker outage·response loss·duplicate·consumer kill·multi-instance | Broker outage·response loss·duplicate·consumer kill·multi-instance | Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence | contract+simulator+actual broker/fault | 미구현 | 미검증 |
### TCP/Professional Message

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-TCP-001 | P0 | 04_MESSAGING_TCP | Client/Server·Connection Pool·Graceful Drain | Client/Server·Connection Pool·Graceful Drain | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-002 | P0 | 04_MESSAGING_TCP | Fixed/Length Header/STX-ETX/CRLF·Fragment/Coalesce | Fixed/Length Header/STX-ETX/CRLF·Fragment/Coalesce | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 부분 구현 | 미검증 |
| QA38-TCP-003 | P0 | 04_MESSAGING_TCP | Binary/BCD/Hex·Endian·Unsigned | Binary/BCD/Hex·Endian·Unsigned | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-004 | P0 | 04_MESSAGING_TCP | UTF-8/EUC-KR/EBCDIC Strict Conversion | UTF-8/EUC-KR/EBCDIC Strict Conversion | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 부분 구현 | 미검증 |
| QA38-TCP-005 | P0 | 04_MESSAGING_TCP | 응답 순서 역전·Orphan·Timeout | 응답 순서 역전·Orphan·Timeout | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-006 | P0 | 04_MESSAGING_TCP | Heartbeat·Half-open·Reconnect·Backoff/Jitter | Heartbeat·Half-open·Reconnect·Backoff/Jitter | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-007 | P0 | 04_MESSAGING_TCP | TLS/mTLS·Trust·Identity·Rotation | TLS/mTLS·Trust·Identity·Rotation | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-008 | P0 | 04_MESSAGING_TCP | Connection/Thread/Queue/Buffer/Frame Limit | Connection/Thread/Queue/Buffer/Frame Limit | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-009 | P0 | 04_MESSAGING_TCP | Write 후 응답 유실·UNKNOWN_RESULT·Reconciliation | Write 후 응답 유실·UNKNOWN_RESULT·Reconciliation | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-010 | P0 | 04_MESSAGING_TCP | Bitmap/Field Packager/MAC/PIN Extension SPI | Bitmap/Field Packager/MAC/PIN Extension SPI | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-011 | P0 | 04_MESSAGING_TCP | Session·Backlog·Failure·Replay·Reconcile·Audit | Session·Backlog·Failure·Replay·Reconcile·Audit | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
| QA38-TCP-012 | P0 | 04_MESSAGING_TCP | 정상·Partial·Oversize·Malformed·Delay·Disconnect Scenario | 정상·Partial·Oversize·Malformed·Delay·Disconnect Scenario | Transport·Codec·Consumer·Simulator·Operations·Fault Closure | codec corpus+TCP simulator+actual endpoint/fault | 미구현 | 미검증 |
### External Integration

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-INT-001 | P0 | 04_MESSAGING_TCP | SFTP actual adapter·upload/download/list/move/delete/resume/checksum/reconcile | SFTP actual adapter·upload/download/list/move/delete/resume/checksum/reconcile | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 부분 구현 | 미검증 |
| QA38-INT-003 | P0 | 04_MESSAGING_TCP | Typed HTTP Client·timeout·deadline·idempotency·response loss | Typed HTTP Client·timeout·deadline·idempotency·response loss | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 부분 구현 | 미검증 |
| QA38-INT-008 | P0 | 04_MESSAGING_TCP | Notification Outbox/Worker/Retry/DLQ/Preference/Quiet Hours | Notification Outbox/Worker/Retry/DLQ/Preference/Quiet Hours | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 부분 구현 | 미검증 |
| QA38-INT-009 | P0 | 04_MESSAGING_TCP | SMTP/Provider·Template·Attachment·Bounce·Unknown Result | SMTP/Provider·Template·Attachment·Bounce·Unknown Result | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-010 | P0 | 04_MESSAGING_TCP | Provider SPI·Rate·Receipt·Callback·Unknown Result | Provider SPI·Rate·Receipt·Callback·Unknown Result | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-002 | P1 | 04_MESSAGING_TCP | FTPS/SMB/Object Storage provider SPI와 secure path/resource policy | FTPS/SMB/Object Storage provider SPI와 secure path/resource policy | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-004 | P1 | 04_MESSAGING_TCP | WSDL/Fault/WS-Security extension | WSDL/Fault/WS-Security extension | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-005 | P1 | 04_MESSAGING_TCP | Deadline/Metadata/Status/Streaming/Backpressure | Deadline/Metadata/Status/Streaming/Backpressure | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-006 | P1 | 04_MESSAGING_TCP | Signature/Retry/Idempotency/Delivery Ledger | Signature/Retry/Idempotency/Delivery Ledger | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
| QA38-INT-007 | P1 | 04_MESSAGING_TCP | WebSocket/SSE auth/reconnect/backpressure/scale-out | WebSocket/SSE auth/reconnect/backpressure/scale-out | Public Contract·Adapter·Consumer·Failure·Security·Operations | contract/simulator/available runtime/fault | 미구현 | 미검증 |
### DB Fresh/Generator

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-DB-001 | P0 | 05_DB_TOOLING | Preflight | 전용 QA Database/Schema와 CPF Object count=0 증명 | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-002 | P0 | 05_DB_TOOLING | Generator First | Canonical Schema/Metadata/Runtime Query→Generator/Golden→Vendor Source | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 부분 구현 | 미검증 |
| QA38-DB-003 | P0 | 05_DB_TOOLING | No Direct Edit | 생성 Vendor SQL·기존 사용자 DB 수동 수정 금지 | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 부분 구현 | 미검증 |
| QA38-DB-007 | P0 | 05_DB_TOOLING | Upgrade | 직전 지원 Version→Latest | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-008 | P0 | 05_DB_TOOLING | Rollback | Rollback 또는 명시적 Forward Recovery | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-009 | P0 | 05_DB_TOOLING | Reapply | 동일 Hash Idempotent Reapply | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-010 | P0 | 05_DB_TOOLING | Conflict | Different-hash·Drift·Checksum Negative | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-011 | P0 | 05_DB_TOOLING | Optional Pack | Starter DB Pack On/Off | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-012 | P0 | 05_DB_TOOLING | Backup/Fault | Backup/Restore·Migration Process Kill·Connection Loss | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-013 | P0 | 05_DB_TOOLING | Cleanup | 검수 전용 DB/Schema 정리, 사용자 DB Reset/Drop 금지 | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-014 | P0 | 05_DB_TOOLING | Evidence | Vendor/Image/Command/Object Count/History/Hash/Sanitization | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-004 | P0 | 06_DB_MARIADB | MariaDB Fresh | Install·Metadata·Seed·Arbitrary Domain·Runtime Query | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-005 | P0 | 07_DB_POSTGRESQL | PostgreSQL Fresh | Install·Metadata·Seed·Arbitrary Domain·Runtime Query | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
| QA38-DB-006 | P0 | 08_DB_ORACLE | Oracle Fresh | Install·Metadata·Seed·Arbitrary Domain·Runtime Query | MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence | sequential disposable DB lifecycle | 미구현 | 미검증 |
### Consumer Migration

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-CONSUMER-001 | P0 | 03_CORE_STARTER | ADM | ADM가 `security-session/webmvc/http-client/persistence/validation/openapi/observability/operations`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-002 | P0 | 03_CORE_STARTER | BZA | BZA가 `security-session/webmvc/persistence/validation/openapi/notification`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-003 | P0 | 03_CORE_STARTER | Gateway | Gateway가 `resilience/observability/http-client/service-identity`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-004 | P0 | 03_CORE_STARTER | Batch | Batch가 `messaging provider/reliability/http-client/service-identity/observability`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-005 | P0 | 03_CORE_STARTER | Reference | Reference가 `Rabbit/JMS/TCP/SFTP 등 실제 Starter EDU Profile`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-006 | P0 | 03_CORE_STARTER | cpf-member | cpf-member가 `Generator resolved Starter Golden`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 부분 구현 | 미검증 |
| QA38-CONSUMER-007 | P0 | 03_CORE_STARTER | Generated Domain | Generated Domain가 `Leaf/Profile/Aggregate 선택과 user-owned 보호`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 미구현 | 미검증 |
| QA38-CONSUMER-008 | P0 | 03_CORE_STARTER | Customer Plugin | Customer Plugin가 `IBM MQ/SMS/기관 Provider SPI fixture`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다. | 실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지 | architecture test+focused runtime+artifact | 미구현 | 미검증 |
### Verification

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-VERIFY-001 | P0 | 00_BASELINE | 00_BASELINE | Git·Toolchain·Working Tree·Protected Paths | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 부분 구현 | 미검증 |
| QA38-VERIFY-002 | P0 | 01_CANONICAL | 01_CANONICAL | Canonical 169·62 Backlog·과거 원장 Crosswalk | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 부분 구현 | 미검증 |
| QA38-VERIFY-003 | P0 | 02_STATIC | 02_STATIC | Source/Dependency/Consumer/SQL/Security/Hygiene | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 부분 구현 | 미검증 |
| QA38-VERIFY-004 | P0 | 03_CORE_STARTER | 03_CORE_STARTER | 30 Migration·7 Existing Starter·Group/Generator | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-005 | P0 | 04_MESSAGING_TCP | 04_MESSAGING_TCP | Kafka/Rabbit/JMS/IBM MQ/TCP/Integration | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-006 | P0 | 05_DB_TOOLING | 05_DB_TOOLING | Generator-first·Empty Preflight·Pack/Drift | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-007 | P0 | 06_DB_MARIADB | 06_DB_MARIADB | MariaDB Full Lifecycle | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-008 | P0 | 07_DB_POSTGRESQL | 07_DB_POSTGRESQL | PostgreSQL Full Lifecycle | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-009 | P0 | 08_DB_ORACLE | 08_DB_ORACLE | Oracle Full Lifecycle | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-010 | P0 | 09_JAVA_FULL | 09_JAVA_FULL | Java25 Clean Test/Assemble/Publication | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-011 | P0 | 10_FRONTEND | 10_FRONTEND | ADM/BZA lock/lint/type/unit/build | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 부분 구현 | 미검증 |
| QA38-VERIFY-012 | P0 | 11_RUNTIME | 11_RUNTIME | Kafka/Rabbit/JMS/TCP/SFTP/Notification Actual Runtime | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-013 | P0 | 12_FAULT_OTEL | 12_FAULT_OTEL | Multi-instance·Process Kill·Network·Unknown·OTel | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-014 | P0 | 13_BROWSER | 13_BROWSER | Chromium/Firefox/WebKit | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-015 | P0 | 14_SUPPLY_CHAIN | 14_SUPPLY_CHAIN | BOM/SBOM/License/CVE/Secret/Final Artifact | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
| QA38-VERIFY-016 | P0 | 15_TRUTH | 15_TRUTH | Requirement/Source/Evidence/Handover/Hygiene | 같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS | stage wrapper/execution ledger | 미구현 | 미검증 |
### QA Completion

| ID | 우선 | Stage | 제목 | 개발 요구 | 수용 기준 | 검증 | 개발 상태 | 검증 상태 |
|---|---|---|---|---|---|---|---|---|
| QA38-CURRENT-011 | P0 | 15_TRUTH | 사용자 요구 Acceptance Checklist | 사용자가 직접 요구한 DB 초기화, Starter 별도 리뷰, MQ/JMS/TCP 복구, 정본화, 삭제, 보호 경로, 다중 GPT, Codex 이력, 상세 최종 요청을 항목별 PASS/미완료로 판정한다. | Coverage Reconciliation의 사용자 요구 누락 0 | coverage reconciliation gate | 완료 | 미검증 |
| QA38-CURRENT-012 | P0 | 15_TRUTH | 최종 보고 지연 Gate | 정본·Current·Codex 현행화, Delete Manifest 적용 가능성, 보호 경로 검사, ZIP/Hash가 모두 닫히기 전 최종이라고 보고하지 않는다. | 모든 패키지 Gate PASS 또는 명시적 미검증 표시 | package validation checklist | 완료 | 미검증 |
