# CPF QA37→QA38 통합 History

과거 날짜별 문서를 삭제한 뒤에도 Requirement·결정·검증 이력을 보존한다.

- QA37 PASS는 exact-SHA 조건 없이는 현재 PASS가 아니다.
- Canonical 169 유지
- RabbitMQ/AMQP·JMS 승인 없는 제외 무효
- IBM MQ/JMS 분리
- TPC Alias→EXS-TCP
- Core→Starter 30개
- Profile/Aggregate/Resolved Lock
- DB Fresh·Generator First
- Codex Stage 00~15
- 보호 경로 Read Only
- Current/Codex 단일 진입점

```text
development_status = 부분 구현
verification_status = 미검증
```

## 동시 작업 Snapshot

적용 시 로컬 수정된 대체 대상은 Path·SHA·원문이 아래에 추가된다.

<!-- CPF_QA38_CONCURRENT_SNAPSHOT_APPEND -->


### Snapshot: cpf-docs/quality/CPF_20260802_06_QA38_CANONICAL_MERGE_CROSSWALK.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
canonical_or_alias_id,disposition,input_source,final_scope,qa38_mapping,merge_rule
ARCH-STARTER,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,Starter Root·Core 경량화·Leaf/Profile/Aggregate/BOM,QA38-STARTER-*,로컬 정본 행과 1:1 대조 후 편입
DB-FRESH,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,Vendor별 Empty DB·Generator First·Fresh lifecycle,QA38-DB-*,3 Vendor 실제 Evidence 필요
EVENT-MQ,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,Provider-neutral MQ contract와 reliability ledger,QA38-MSG-COMMON-*,Kafka 전용 표현 금지
EVENT-JMS,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,Jakarta JMS Runtime,QA38-MSG-JMS-*,Artemis/IBM MQ Provider 분리
EVENT-IBM-MQ,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,IBM MQ JMS Extension·Queue Manager·Channel·TLS·Reason Code,QA38-MSG-IBMMQ-*,Driver/Server 기본 번들 금지
EVENT-AMQP,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,RabbitMQ/AMQP Exchange·Queue·Binding·Confirm·ACK·DLX,QA38-MSG-RABBIT-*,사용자 승인 없는 제외 무효
EXS-TCP,KEEP_AND_EXPAND,개발 GPT 로컬 169 정본,TCP Transport·Framing·Heartbeat·Reconnect·Unknown Result,QA38-TCP-*,TPC Alias 보존
TPC,ALIAS_ONLY,사용자 원문,EXS-TCP 검색 Alias,EXS-TCP,별도 제품 용어 확인 시 분리
EVENT-BROKER,EXPAND_EXISTING,Canonical 162,Kafka 전용이 아닌 Provider Matrix 계약,QA38-MSG-COMMON-*,Kafka는 Default Profile
TEST-BROKER,EXPAND_EXISTING,Canonical 162,Kafka·RabbitMQ·JMS·IBM MQ Contract/Runtime Matrix,QA38-VERIFY-BROKER-*,Provider별 상태 분리

``n

### Snapshot: cpf-docs/quality/CPF_20260802_06_QA38_CORE_TO_STARTER_FINAL_REVIEW.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
candidate_id,current_owner,current_element,disposition,target_artifact,remaining_contract,actual_consumers,db_generator_impact,priority,development_status,verification_status
ST-001,cpf-core,CpfDataSourceConfig,MOVE,cpf-starter-persistence-jdbc,DB-neutral datasource contract,ADM/BZA/REF/Generated,Canonical DB/3 Vendor,P0,부분 구현,미검증
ST-002,cpf-core,CpfMyBatisConfig+dependency/resources,MOVE,cpf-starter-persistence-mybatis,Persistence-neutral SPI,REF/Generated/Product DB,Mapper/Runtime Query/3 Vendor,P0,부분 구현,미검증
ST-003,cpf-core,CpfAopConfig+ServiceAccessAspect+AspectJ,MOVE,cpf-starter-aop-service-access,Service access/audit contract,Service/ADM/BZA,Audit metadata,P0,부분 구현,미검증
ST-004,cpf-core,CpfOpenApiAutoConfiguration+Springdoc+Scalar,MOVE,cpf-starter-openapi-webmvc,OpenAPI metadata contract,ADM/BZA/REF/Domain,없음,P0,부분 구현,미검증
ST-005,cpf-core,CpfSecurityAutoConfiguration,SPLIT_MOVE,cpf-starter-security-resource-server,Security principal/permission contract,API Domain/Gateway,Security metadata,P0,부분 구현,미검증
ST-006,cpf-starters/security,BFF/JDBC Security Runtime,SPLIT_MOVE,cpf-starter-security-session-jdbc,Generic session contract,ADM/BZA,Session Schema 3 Vendor,P0,부분 구현,미검증
ST-007,cpf-core,Broker Worker/Bridge/JDBC Reliability Repository,MOVE,cpf-starter-messaging-reliability-jdbc,Provider-neutral message contract,Kafka/Rabbit/JMS/Batch,Outbox/Inbox/DLQ/Replay 3 Vendor,P0,부분 구현,미검증
ST-008,cpf-core,JdbcCpfChannelRegistryAdapter,MOVE,cpf-starter-channel-registry-jdbc,Channel registry SPI,Runtime/Gateway/Agent,Registry schema 3 Vendor,P0,부분 구현,미검증
ST-009,cpf-core,Logging+OTel SDK/Exporter Runtime,SPLIT_MOVE,cpf-starter-observability + cpf-starter-observability-otlp,Trace/metric/log contract,모든 Runtime,Log DB 선택,P0,부분 구현,미검증
ST-010,cpf-core,Remote HTTP Runtime,MOVE,cpf-starter-http-client,Typed remote client contract,ADM/Gateway/Batch/Domain,없음,P0,부분 구현,미검증
ST-011,cpf-core/common,Validation Runtime Provider,MOVE,cpf-starter-validation,Validation API/values,ADM/BZA/Domain,없음,P1,부분 구현,미검증
ST-012,cpf-core,Fixed-length Spring Component,SPLIT,cpf-integration-fixedlength-core + cpf-starter-integration-fixedlength,Pure codec contract,TCP/REF/EDU,Layout metadata,P0,부분 구현,미검증
ST-013,cpf-core,FileExchange/SFTP Planned Runtime,MOVE_IMPLEMENT,cpf-starter-integration-sftp,File transfer SPI,Batch/Institution/REF,Transfer Ledger,P0,부분 구현,미검증
ST-014,cpf-core,Commons Compress/Archive Runtime,MOVE,cpf-starter-file-archive,Archive/file contract,File/Attachment,없음,P1,부분 구현,미검증
ST-015,cpf-common,Redis/Valkey Connection·Template·Listener,MOVE,cpf-starter-cache-valkey,Business cache abstraction,Common/ADM/BZA/Domain,Cache metadata optional,P0,부분 구현,미검증
ST-016,cpf-common,Caffeine Runtime,MOVE,cpf-starter-cache-caffeine,Business cache abstraction,Common/Domain,없음,P0,부분 구현,미검증
ST-017,cpf-common,POI/XLSX Runtime,MOVE,cpf-starter-tabular-poi,Tabular contract,ADM/BZA/Domain,없음,P1,부분 구현,미검증
ST-018,cpf-core,Service Identity Runtime,MOVE,cpf-starter-security-service-identity,Identity/mTLS/OIDC contract,Gateway/Batch/Agent,Identity metadata,P0,부분 구현,미검증
ST-019,cpf-core,Registry/Health technical client,MOVE,cpf-starter-runtime-registry-client,Registry/health contract,Gateway/Runtime/Agent,Registry metadata,P0,부분 구현,미검증
ST-020,cpf-starters/secret,Secret Provider Runtime,EXPAND,cpf-starter-secret + provider plugins,Secret registry contract,모든 Secret Consumer,Secret reference metadata,P0,부분 구현,미검증
ST-021,cpf-starters/featureflag,OpenFeature Runtime,EXPAND,cpf-starter-featureflag + provider plugins,Feature flag contract,ADM/BZA/Domain,Flag/Audit metadata,P1,부분 구현,미검증
ST-022,cpf-starters/resilience,CircuitBreaker-only Runtime,EXPAND,cpf-starter-resilience,Deadline/retry/unknown-result contract,Gateway/Batch/Domain,없음,P0,부분 구현,미검증
ST-023,cpf-starters/messaging-kafka,Kafka Producer/Consumer Runtime,EXPAND_MIGRATE,cpf-starter-messaging-kafka,Provider-neutral message port,Batch/Domain/REF,Reliability Ledger,P0,부분 구현,미검증
ST-024,누락,RabbitMQ/AMQP,NEW,cpf-starter-messaging-rabbitmq,Provider-neutral MQ+AMQP extension,Domain/Batch/Bridge/REF,Reliability Ledger,P0,미구현,미검증
ST-025,누락,Jakarta JMS,NEW,cpf-starter-messaging-jms,Provider-neutral MQ+JMS extension,Domain/Batch/REF,Reliability Ledger,P0,미구현,미검증
ST-026,누락,IBM MQ Provider,NEW_PLUGIN,cpf-starter-messaging-ibm-mq,JMS+IBM MQ extension SPI,Institution Domain,Reliability Ledger,P0,미구현,미검증
ST-027,누락,TCP Transport Runtime,NEW,cpf-starter-integration-tcp,TCP transport contract,Institution/Batch/REF,Request/Reconcile Ledger optional,P0,미구현,미검증
ST-028,누락/부분,Notification Email/SMS Worker,NEW_SPLIT,cpf-starter-notification + email + sms-spi,Notification contract,ADM/BZA/Domain,Outbox/Delivery/Result,P0,미구현,미검증
ST-029,선택 기능,Quartz Scheduler,NEW_OPTIONAL,cpf-starter-scheduler-quartz,Scheduler SPI,고급 Batch Consumer,Scheduler schema 3 Vendor,P1,미구현,미검증
ST-030,cpf-core/common,Public API/SPI·Identifiers·Context·Error·Masking·업무 Common,KEEP,cpf-core / cpf-common,Topology-independent contract and business common,모든 Consumer,공통 Metadata만,P0,부분 구현,미검증

``n

### Snapshot: cpf-docs/quality/CPF_20260802_06_QA38_DEVELOPER_BACKLOG_IMPORT_STATUS.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
input_id,reported_input,source_status,merge_status,required_action
DEV-GPT-CANONICAL-169,Canonical 169,VERIFIED_IN_GIT_AT_2E93D923,PENDING_LOCAL_IMPORT,7개 ID와 Alias/Provider Matrix 비교
DEV-GPT-CORE-STARTER-30,Core→Starter 30개,VERIFIED_IN_GIT_AT_2E93D923,PENDING_LOCAL_IMPORT,Starter Review와 행 단위 Crosswalk
DEV-GPT-BACKLOG-62,Self Development 62개,VERIFIED_IN_GIT_AT_2E93D923,PENDING_LOCAL_IMPORT,최종 Matrix 누락 0 확인
DEV-GPT-CODEX-START,CODEX_START_HERE,VERIFIED_IN_GIT_AT_2E93D923,PENDING_LOCAL_IMPORT,Final Review Index와 병합
DEV-GPT-CODEX-REQUEST,QA38 Verification Remediation Request,VERIFIED_IN_GIT_AT_2E93D923,PENDING_LOCAL_IMPORT,Final Request와 병합
DEV-GPT-STAGES-00-15,Codex Stage 00~15,VERIFIED_IN_GIT_AT_2E93D923,ACCEPTED_WITH_EXTENSION,보호 경로·Rabbit/JMS/TCP Owner Stage 추가

``n

### Snapshot: cpf-docs/quality/CPF_20260802_06_QA38_FINAL_REQUIREMENTS.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
requirement_id,priority,codex_stage,category,canonical_requirement,owner,title,requirement,source_basis,development_status,verification_status,acceptance_criteria,verification_method,required_evidence,regression_guard,protected_owner_action
QA38-GOV-001,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,Latest master exact SHA,실행 시점 origin/master와 Working Tree/최근 Commit Range를 고정,latest Git,부분 구현,재확인 필요,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-002,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,다른 GPT 보호 경로,4개 보호 Prefix를 읽기 전용으로 강제,사용자 지시,완료,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-003,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,개발 GPT 62개 Backlog Import,로컬 원본 62개를 행 단위 Import·Crosswalk,unpushed user input,미구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-004,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,Canonical 169 확인,7개 ID·TPC Alias·Provider Matrix를 로컬 정본과 대조,unpushed user input,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-005,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,승인 없는 Requirement 제거 금지,사용자 승인 Evidence 없는 removed/superseded를 무효화,과거 누락 원인,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-006,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,과거 원장 보존,Canonical/Enterprise/EDU/QA37 유효 Requirement를 대체하지 않음,과거 QA,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-007,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,상태 축 분리,development_status와 verification_status 분리,프로젝트 정책,완료,재확인 필요,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-008,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,Source 독립 검토,문서·보고가 아닌 실제 Source/Build/SQL/Consumer/Test 검토,사용자 지시,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-009,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,Source Defect와 환경 Blocker,환경 부재로 구현 누락을 숨기지 않음,QA 품질,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-010,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,Codex Continuity,중단 전 exact 재개 지점·명령·상태 기록,QA37 중단,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-011,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,AI 결정 승인 상태,Architecture Decision에 사용자 승인 상태 기록,과거 제외 문제,미구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-GOV-012,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,중복·가비지 관리,정본 승격 후 exact allowlist만 정리,사용자 지시,부분 구현,미검증,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate,SHA·원본 행 Hash·Crosswalk·승인 Ledger,다른 GPT 작업·과거 Requirement 보호,
QA38-SRC-001,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Dependency Graph,모든 Module·Starter의 direct/transitive dependency와 API 노출,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-002,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Import Graph,Provider SDK/Internal package 직접 참조,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-003,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,AutoConfiguration Graph,Imports/Condition/Properties/Bean 충돌·Backoff,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-004,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Consumer Graph,API/SPI/Starter의 실제 Source·Build·Runtime Consumer,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-005,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,SQL Graph,Canonical/Generator/Vendor/Mapper/Inline SQL Ownership,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-006,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Generator Graph,Template/Golden/임의 Domain/Profile/Manifest parity,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-007,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Test Graph,Requirement→Unit/Contract/Integration/Runtime/Fault/Browser,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-008,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Artifact Graph,POM/BOM/JAR/WAR/SBOM/Final Content,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-009,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Frontend Graph,ADM/BZA Route/API/Permission/Generated Client,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-SRC-010,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,Recent Commit Impact,QA37 baseline 이후 전체 Commit의 Source·Docs·Environment 영향,latest source + developer GPT summary,부분 구현,미검증,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan,path/blob/import/dependency/consumer manifest,정적 문자열만으로 완료 금지,
QA38-STARTER-001,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CpfDataSourceConfig MOVE,"`cpf-core`의 `CpfDataSourceConfig`를 `cpf-starter-persistence-jdbc` 기준으로 MOVE하고, 남길 계약은 `DB-neutral datasource contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/REF/Generated,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Canonical DB/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-002,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CpfMyBatisConfig+dependency/resources MOVE,"`cpf-core`의 `CpfMyBatisConfig+dependency/resources`를 `cpf-starter-persistence-mybatis` 기준으로 MOVE하고, 남길 계약은 `Persistence-neutral SPI`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=REF/Generated/Product DB,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Mapper/Runtime Query/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-003,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CpfAopConfig+ServiceAccessAspect+AspectJ MOVE,"`cpf-core`의 `CpfAopConfig+ServiceAccessAspect+AspectJ`를 `cpf-starter-aop-service-access` 기준으로 MOVE하고, 남길 계약은 `Service access/audit contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Service/ADM/BZA,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-004,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CpfOpenApiAutoConfiguration+Springdoc+Scalar MOVE,"`cpf-core`의 `CpfOpenApiAutoConfiguration+Springdoc+Scalar`를 `cpf-starter-openapi-webmvc` 기준으로 MOVE하고, 남길 계약은 `OpenAPI metadata contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/REF/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-005,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CpfSecurityAutoConfiguration SPLIT_MOVE,"`cpf-core`의 `CpfSecurityAutoConfiguration`를 `cpf-starter-security-resource-server` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Security principal/permission contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=API Domain/Gateway,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Security metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-006,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,BFF/JDBC Security Runtime SPLIT_MOVE,"`cpf-starters/security`의 `BFF/JDBC Security Runtime`를 `cpf-starter-security-session-jdbc` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Generic session contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Session Schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-007,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Broker Worker/Bridge/JDBC Reliability Repository MOVE,"`cpf-core`의 `Broker Worker/Bridge/JDBC Reliability Repository`를 `cpf-starter-messaging-reliability-jdbc` 기준으로 MOVE하고, 남길 계약은 `Provider-neutral message contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Kafka/Rabbit/JMS/Batch,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Outbox/Inbox/DLQ/Replay 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-008,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,JdbcCpfChannelRegistryAdapter MOVE,"`cpf-core`의 `JdbcCpfChannelRegistryAdapter`를 `cpf-starter-channel-registry-jdbc` 기준으로 MOVE하고, 남길 계약은 `Channel registry SPI`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Runtime/Gateway/Agent,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Registry schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-009,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Logging+OTel SDK/Exporter Runtime SPLIT_MOVE,"`cpf-core`의 `Logging+OTel SDK/Exporter Runtime`를 `cpf-starter-observability + cpf-starter-observability-otlp` 기준으로 SPLIT_MOVE하고, 남길 계약은 `Trace/metric/log contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=모든 Runtime,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Log DB 선택)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-010,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Remote HTTP Runtime MOVE,"`cpf-core`의 `Remote HTTP Runtime`를 `cpf-starter-http-client` 기준으로 MOVE하고, 남길 계약은 `Typed remote client contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/Gateway/Batch/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-011,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Validation Runtime Provider MOVE,"`cpf-core/common`의 `Validation Runtime Provider`를 `cpf-starter-validation` 기준으로 MOVE하고, 남길 계약은 `Validation API/values`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-012,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Fixed-length Spring Component SPLIT,"`cpf-core`의 `Fixed-length Spring Component`를 `cpf-integration-fixedlength-core + cpf-starter-integration-fixedlength` 기준으로 SPLIT하고, 남길 계약은 `Pure codec contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=TCP/REF/EDU,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Layout metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-013,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,FileExchange/SFTP Planned Runtime MOVE_IMPLEMENT,"`cpf-core`의 `FileExchange/SFTP Planned Runtime`를 `cpf-starter-integration-sftp` 기준으로 MOVE_IMPLEMENT하고, 남길 계약은 `File transfer SPI`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Batch/Institution/REF,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Transfer Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-014,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Commons Compress/Archive Runtime MOVE,"`cpf-core`의 `Commons Compress/Archive Runtime`를 `cpf-starter-file-archive` 기준으로 MOVE하고, 남길 계약은 `Archive/file contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=File/Attachment,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-015,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Redis/Valkey Connection·Template·Listener MOVE,"`cpf-common`의 `Redis/Valkey Connection·Template·Listener`를 `cpf-starter-cache-valkey` 기준으로 MOVE하고, 남길 계약은 `Business cache abstraction`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Common/ADM/BZA/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Cache metadata optional)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-016,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Caffeine Runtime MOVE,"`cpf-common`의 `Caffeine Runtime`를 `cpf-starter-cache-caffeine` 기준으로 MOVE하고, 남길 계약은 `Business cache abstraction`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Common/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-017,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,POI/XLSX Runtime MOVE,"`cpf-common`의 `POI/XLSX Runtime`를 `cpf-starter-tabular-poi` 기준으로 MOVE하고, 남길 계약은 `Tabular contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-018,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Service Identity Runtime MOVE,"`cpf-core`의 `Service Identity Runtime`를 `cpf-starter-security-service-identity` 기준으로 MOVE하고, 남길 계약은 `Identity/mTLS/OIDC contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Gateway/Batch/Agent,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Identity metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-019,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Registry/Health technical client MOVE,"`cpf-core`의 `Registry/Health technical client`를 `cpf-starter-runtime-registry-client` 기준으로 MOVE하고, 남길 계약은 `Registry/health contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Gateway/Runtime/Agent,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Registry metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-020,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Secret Provider Runtime EXPAND,"`cpf-starters/secret`의 `Secret Provider Runtime`를 `cpf-starter-secret + provider plugins` 기준으로 EXPAND하고, 남길 계약은 `Secret registry contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=모든 Secret Consumer,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Secret reference metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-021,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,OpenFeature Runtime EXPAND,"`cpf-starters/featureflag`의 `OpenFeature Runtime`를 `cpf-starter-featureflag + provider plugins` 기준으로 EXPAND하고, 남길 계약은 `Feature flag contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Flag/Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-022,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,CircuitBreaker-only Runtime EXPAND,"`cpf-starters/resilience`의 `CircuitBreaker-only Runtime`를 `cpf-starter-resilience` 기준으로 EXPAND하고, 남길 계약은 `Deadline/retry/unknown-result contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Gateway/Batch/Domain,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-023,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Kafka Producer/Consumer Runtime EXPAND_MIGRATE,"`cpf-starters/messaging-kafka`의 `Kafka Producer/Consumer Runtime`를 `cpf-starter-messaging-kafka` 기준으로 EXPAND_MIGRATE하고, 남길 계약은 `Provider-neutral message port`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Batch/Domain/REF,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-024,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,RabbitMQ/AMQP NEW,"`누락`의 `RabbitMQ/AMQP`를 `cpf-starter-messaging-rabbitmq` 기준으로 NEW하고, 남길 계약은 `Provider-neutral MQ+AMQP extension`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Domain/Batch/Bridge/REF,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-025,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Jakarta JMS NEW,"`누락`의 `Jakarta JMS`를 `cpf-starter-messaging-jms` 기준으로 NEW하고, 남길 계약은 `Provider-neutral MQ+JMS extension`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Domain/Batch/REF,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-026,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,IBM MQ Provider NEW_PLUGIN,"`누락`의 `IBM MQ Provider`를 `cpf-starter-messaging-ibm-mq` 기준으로 NEW_PLUGIN하고, 남길 계약은 `JMS+IBM MQ extension SPI`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Institution Domain,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-027,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,TCP Transport Runtime NEW,"`누락`의 `TCP Transport Runtime`를 `cpf-starter-integration-tcp` 기준으로 NEW하고, 남길 계약은 `TCP transport contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=Institution/Batch/REF,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Request/Reconcile Ledger optional)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-028,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Notification Email/SMS Worker NEW_SPLIT,"`누락/부분`의 `Notification Email/SMS Worker`를 `cpf-starter-notification + email + sms-spi` 기준으로 NEW_SPLIT하고, 남길 계약은 `Notification contract`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=ADM/BZA/Domain,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Outbox/Delivery/Result)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-029,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Quartz Scheduler NEW_OPTIONAL,"`선택 기능`의 `Quartz Scheduler`를 `cpf-starter-scheduler-quartz` 기준으로 NEW_OPTIONAL하고, 남길 계약은 `Scheduler SPI`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=고급 Batch Consumer,미구현,미검증,Target Artifact·Consumer·DB/Generator 영향(Scheduler schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-STARTER-030,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,Public API/SPI·Identifiers·Context·Error·Masking·업무 Common KEEP,"`cpf-core/common`의 `Public API/SPI·Identifiers·Context·Error·Masking·업무 Common`를 `cpf-core / cpf-common` 기준으로 KEEP하고, 남길 계약은 `Topology-independent contract and business common`으로 제한한다.",developer GPT 30 candidate + latest source; consumers=모든 Consumer,부분 구현,미검증,Target Artifact·Consumer·DB/Generator 영향(공통 Metadata만)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime,POM/JAR/API diff·consumer test·artifact hash,기존 기능·API 호환 및 미선택 Domain 영향 0,
QA38-EXISTING-001,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Security,Session JDBC/Resource Server/Service Identity 분리; ADM/BZA Route 정책 Product 이관,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-002,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Kafka,Consumer/Listener/Offset/Rebalance/DLT/Operations 완성; Batch 직접 Kafka 이관,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-003,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Cache,Caffeine와 Valkey/Redis 분리; Common 기술 Runtime 제거,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-004,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Observability,Observation과 OTLP Exporter 분리; collector 장애 격리,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-005,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Resilience,timeout/retry/time-limiter/bulkhead/rate/backpressure/unknown-result,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-006,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Feature Flag,Provider lifecycle/secure override/audit/multi-instance/actual consumer,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-EXISTING-007,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,Secret,Provider catalog/local-dev vs product fail-closed/rotation/revocation/health,latest settings/source review,부분 구현,미검증,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact,starter별 log/report/POM/SBOM,현재 7개 Starter 성공 기능 보호,
QA38-MSG-001,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,Provider-neutral Message Envelope·Logical Destination·Binding·ACK/Commit·Unknown Result,Provider-neutral Message Envelope·Logical Destination·Binding·ACK/Commit·Unknown Result,canonical recovery + developer GPT architecture,부분 구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-002,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,JDBC Outbox·Inbox·DLQ·Replay·Reconcile Ledger 3 Vendor,JDBC Outbox·Inbox·DLQ·Replay·Reconcile Ledger 3 Vendor,canonical recovery + developer GPT architecture,부분 구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-003,P0,04_MESSAGING_TCP,Messaging,EVENT-BROKER,Messaging/Starter/DB Platform,Kafka Default Profile의 publish/consume/rebalance/offset/DLT/operations,Kafka Default Profile의 publish/consume/rebalance/offset/DLT/operations,canonical recovery + developer GPT architecture,부분 구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-004,P0,04_MESSAGING_TCP,Messaging,EVENT-AMQP,Messaging/Starter/DB Platform,Exchange·Queue·Binding·Routing·Confirm·Return·ACK/NACK·DLX·Quorum,Exchange·Queue·Binding·Routing·Confirm·Return·ACK/NACK·DLX·Quorum,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-005,P0,04_MESSAGING_TCP,Messaging,EVENT-JMS,Messaging/Starter/DB Platform,Queue·Topic·Durable·Selector·Ack Mode·Transaction·Redelivery,Queue·Topic·Durable·Selector·Ack Mode·Transaction·Redelivery,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-006,P0,04_MESSAGING_TCP,Messaging,EVENT-IBM-MQ,Messaging/Starter/DB Platform,Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect Extension,Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect Extension,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-007,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,Kafka+Rabbit/JMS Named Binding·Bridge·Migration·모호성 fail-closed,Kafka+Rabbit/JMS Named Binding·Bridge·Migration·모호성 fail-closed,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-008,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,Schema Version·Compatibility·Quarantine,Schema Version·Compatibility·Quarantine,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-009,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,TLS/mTLS·Secret Rotation·Masking·Permission·Audit,TLS/mTLS·Secret Rotation·Masking·Permission·Audit,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-010,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,Backlog·Lag·DLQ·Replay·Reconcile·Approval·Result Tracking,Backlog·Lag·DLQ·Replay·Reconcile·Approval·Result Tracking,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-MSG-011,P0,04_MESSAGING_TCP,Messaging,TEST-BROKER,Messaging/Starter/DB Platform,Broker outage·response loss·duplicate·consumer kill·multi-instance,Broker outage·response loss·duplicate·consumer kill·multi-instance,canonical recovery + developer GPT architecture,미구현,미검증,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault,broker config/log/ledger/metric/audit/hash,"Kafka Default 유지, Rabbit/JMS/IBM MQ 삭제 금지",Docker Owner가 Rabbit/JMS fixture 검토
QA38-TCP-001,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Client/Server·Connection Pool·Graceful Drain,Client/Server·Connection Pool·Graceful Drain,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-002,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Fixed/Length Header/STX-ETX/CRLF·Fragment/Coalesce,Fixed/Length Header/STX-ETX/CRLF·Fragment/Coalesce,past EDU 018~029 + developer GPT EXS-TCP,부분 구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-003,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Binary/BCD/Hex·Endian·Unsigned,Binary/BCD/Hex·Endian·Unsigned,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-004,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,UTF-8/EUC-KR/EBCDIC Strict Conversion,UTF-8/EUC-KR/EBCDIC Strict Conversion,past EDU 018~029 + developer GPT EXS-TCP,부분 구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-005,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,응답 순서 역전·Orphan·Timeout,응답 순서 역전·Orphan·Timeout,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-006,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Heartbeat·Half-open·Reconnect·Backoff/Jitter,Heartbeat·Half-open·Reconnect·Backoff/Jitter,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-007,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,TLS/mTLS·Trust·Identity·Rotation,TLS/mTLS·Trust·Identity·Rotation,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-008,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Connection/Thread/Queue/Buffer/Frame Limit,Connection/Thread/Queue/Buffer/Frame Limit,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-009,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Write 후 응답 유실·UNKNOWN_RESULT·Reconciliation,Write 후 응답 유실·UNKNOWN_RESULT·Reconciliation,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-010,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Bitmap/Field Packager/MAC/PIN Extension SPI,Bitmap/Field Packager/MAC/PIN Extension SPI,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-011,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,Session·Backlog·Failure·Replay·Reconcile·Audit,Session·Backlog·Failure·Replay·Reconcile·Audit,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-TCP-012,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,정상·Partial·Oversize·Malformed·Delay·Disconnect Scenario,정상·Partial·Oversize·Malformed·Delay·Disconnect Scenario,past EDU 018~029 + developer GPT EXS-TCP,미구현,미검증,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault,packet/session/ledger/metric/audit,TPC Alias 보존·직접 Socket 사용 금지,Docker Owner가 TCP fixture 검토
QA38-INT-001,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,SFTP actual adapter·upload/download/list/move/delete/resume/checksum/reconcile,SFTP actual adapter·upload/download/list/move/delete/resume/checksum/reconcile,historical requirements + commercial framework quality,부분 구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-002,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,FTPS/SMB/Object Storage provider SPI와 secure path/resource policy,FTPS/SMB/Object Storage provider SPI와 secure path/resource policy,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-003,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,Typed HTTP Client·timeout·deadline·idempotency·response loss,Typed HTTP Client·timeout·deadline·idempotency·response loss,historical requirements + commercial framework quality,부분 구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-004,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,WSDL/Fault/WS-Security extension,WSDL/Fault/WS-Security extension,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-005,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,Deadline/Metadata/Status/Streaming/Backpressure,Deadline/Metadata/Status/Streaming/Backpressure,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-006,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,Signature/Retry/Idempotency/Delivery Ledger,Signature/Retry/Idempotency/Delivery Ledger,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-007,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,WebSocket/SSE auth/reconnect/backpressure/scale-out,WebSocket/SSE auth/reconnect/backpressure/scale-out,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-008,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,Notification Outbox/Worker/Retry/DLQ/Preference/Quiet Hours,Notification Outbox/Worker/Retry/DLQ/Preference/Quiet Hours,historical requirements + commercial framework quality,부분 구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-009,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,SMTP/Provider·Template·Attachment·Bounce·Unknown Result,SMTP/Provider·Template·Attachment·Bounce·Unknown Result,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-INT-010,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,Provider SPI·Rate·Receipt·Callback·Unknown Result,Provider SPI·Rate·Receipt·Callback·Unknown Result,historical requirements + commercial framework quality,미구현,미검증,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault,transfer/delivery ledger·log·artifact hash,PLANNED-only·화면-only 완료 금지,Docker Owner가 fixture 추가 검토
QA38-GROUP-001,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Leaf Naming,cpf-starter-<capability>-<provider>와 가독성 있는 package,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-002,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Base Starter,"Core+최소 Boot만 조립, Web/DB/Broker 자동 포함 금지",developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-003,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Capability Profile,Use-case→승인 Leaf Starter 목록,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-004,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Aggregate Starter,"전이 Dependency만 제공, Source/Bean/Policy 금지",developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-005,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Resolved Lock,resolvedStarters·Profile Version·Artifact Version,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-006,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Named Binding,복수 Provider 이름·Default 최대 하나,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-007,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Conflict Gate,모호한 Bean/상호 배타 설정 fail-closed,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-008,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,BOM,"Version 정렬만, Runtime 비활성",developer GPT group design + QA extension,부분 구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-009,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Publication,POM/Sources/JavaDoc/SBOM/Signature/Artifact Catalog,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-010,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Optional Removal,Leaf/Profile 제거 Compile·Runtime,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-011,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Footprint,JAR/Dependency/Startup/Memory/Thread Budget,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-012,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Upgrade,Profile/Starter Mixed Version·Migration·Rollback,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-013,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Generator,Build/Config/Test/Operations/Manifest 원자 생성,developer GPT group design + QA extension,부분 구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-GROUP-014,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,Legacy Removal,기존 Core/Common/Product Bean·Dependency·Config·SQL 제거,developer GPT group design + QA extension,미구현,미검증,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations,resolved manifest/POM/JAR/test hash,Mega Starter·silent transitive change 금지,
QA38-DB-001,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Preflight,전용 QA Database/Schema와 CPF Object count=0 증명,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-002,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Generator First,Canonical Schema/Metadata/Runtime Query→Generator/Golden→Vendor Source,developer GPT DB rules + user directive,부분 구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-003,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,No Direct Edit,생성 Vendor SQL·기존 사용자 DB 수동 수정 금지,developer GPT DB rules + user directive,부분 구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-004,P0,06_DB_MARIADB,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,MariaDB Fresh,Install·Metadata·Seed·Arbitrary Domain·Runtime Query,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-005,P0,07_DB_POSTGRESQL,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,PostgreSQL Fresh,Install·Metadata·Seed·Arbitrary Domain·Runtime Query,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-006,P0,08_DB_ORACLE,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Oracle Fresh,Install·Metadata·Seed·Arbitrary Domain·Runtime Query,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-007,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Upgrade,직전 지원 Version→Latest,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-008,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Rollback,Rollback 또는 명시적 Forward Recovery,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-009,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Reapply,동일 Hash Idempotent Reapply,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-010,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Conflict,Different-hash·Drift·Checksum Negative,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-011,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Optional Pack,Starter DB Pack On/Off,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-012,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Backup/Fault,Backup/Restore·Migration Process Kill·Connection Loss,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-013,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Cleanup,"검수 전용 DB/Schema 정리, 사용자 DB Reset/Drop 금지",developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-DB-014,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,Evidence,Vendor/Image/Command/Object Count/History/Hash/Sanitization,developer GPT DB rules + user directive,미구현,미검증,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle,DB/schema/object/history/artifact hashes,기존 사용자 DB·수동 Vendor SQL 보호,Docker Owner가 Empty DB hook/evidence 검토
QA38-CONSUMER-001,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,ADM,ADM가 `security-session/webmvc/http-client/persistence/validation/openapi/observability/operations`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-002,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,BZA,BZA가 `security-session/webmvc/persistence/validation/openapi/notification`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-003,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,Gateway,Gateway가 `resilience/observability/http-client/service-identity`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-004,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,Batch,Batch가 `messaging provider/reliability/http-client/service-identity/observability`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-005,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,Reference,Reference가 `Rabbit/JMS/TCP/SFTP 등 실제 Starter EDU Profile`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-006,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,cpf-member,cpf-member가 `Generator resolved Starter Golden`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,부분 구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-007,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,Generated Domain,Generated Domain가 `Leaf/Profile/Aggregate 선택과 user-owned 보호`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,미구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-CONSUMER-008,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,Customer Plugin,Customer Plugin가 `IBM MQ/SMS/기관 Provider SPI fixture`를 실제 Build·Source·Config·Runtime에서 사용하고 Provider SDK 직접 참조를 제거한다.,latest source + developer GPT,미구현,미검증,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact,consumer graph/build/config/test/runtime,기존 업무·운영 기능 회귀 금지,
QA38-VERIFY-001,P0,00_BASELINE,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,00_BASELINE,Git·Toolchain·Working Tree·Protected Paths,developer GPT stage plan + QA extension,부분 구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-002,P0,01_CANONICAL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,01_CANONICAL,Canonical 169·62 Backlog·과거 원장 Crosswalk,developer GPT stage plan + QA extension,부분 구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-003,P0,02_STATIC,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,02_STATIC,Source/Dependency/Consumer/SQL/Security/Hygiene,developer GPT stage plan + QA extension,부분 구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-004,P0,03_CORE_STARTER,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,03_CORE_STARTER,30 Migration·7 Existing Starter·Group/Generator,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-005,P0,04_MESSAGING_TCP,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,04_MESSAGING_TCP,Kafka/Rabbit/JMS/IBM MQ/TCP/Integration,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-006,P0,05_DB_TOOLING,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,05_DB_TOOLING,Generator-first·Empty Preflight·Pack/Drift,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-007,P0,06_DB_MARIADB,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,06_DB_MARIADB,MariaDB Full Lifecycle,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-008,P0,07_DB_POSTGRESQL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,07_DB_POSTGRESQL,PostgreSQL Full Lifecycle,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-009,P0,08_DB_ORACLE,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,08_DB_ORACLE,Oracle Full Lifecycle,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-010,P0,09_JAVA_FULL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,09_JAVA_FULL,Java25 Clean Test/Assemble/Publication,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-011,P0,10_FRONTEND,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,10_FRONTEND,ADM/BZA lock/lint/type/unit/build,developer GPT stage plan + QA extension,부분 구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-012,P0,11_RUNTIME,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,11_RUNTIME,Kafka/Rabbit/JMS/TCP/SFTP/Notification Actual Runtime,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-013,P0,12_FAULT_OTEL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,12_FAULT_OTEL,Multi-instance·Process Kill·Network·Unknown·OTel,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-014,P0,13_BROWSER,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,13_BROWSER,Chromium/Firefox/WebKit,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-015,P0,14_SUPPLY_CHAIN,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,14_SUPPLY_CHAIN,BOM/SBOM/License/CVE/Secret/Final Artifact,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리
QA38-VERIFY-016,P0,15_TRUTH,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,15_TRUTH,Requirement/Source/Evidence/Handover/Hygiene,developer GPT stage plan + QA extension,미구현,미검증,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger,command/log/report/artifact SHA,과거 SHA·다른 환경 PASS 승계 금지,보호 환경 변경은 Owner 요청으로 분리

``n

### Snapshot: cpf-docs/work/codex/qa38/CODEX_START_HERE.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF QA38 Codex Start Here

## Mission

QA37에서 남은 실제 미검증과 Source Gap을 이어서 검수하고, 결함을 발견하면 보완 개발과 재검증까지 수행한다.

Review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
Execution baseline: actual clean `HEAD == origin/master`

## First read

1. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
2. `cpf-docs/work/current/CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
3. `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
4. `cpf-docs/work/codex/qa38/CPF_CODEX_QA38_VERIFICATION_REMEDIATION_REQUEST.md`
5. `cpf-docs/work/codex/qa38/STAGE_PLAN.csv`
6. `cpf-docs/work/codex/qa38/VERIFICATION_HISTORY.csv`

Do not reread all historical QA documents before a failure requires them.

## Critical resume rule

A historical PASS may skip analysis only when current HEAD, command hash, environment/profile and relevant artifact hash match.
It cannot be used as current completion evidence after Source or Git SHA changed.

## Highest priority

1. Confirm all pushes and Working Tree.
2. Finish Core/Starter/Generator source development.
3. Finish official Fresh DB lifecycle tooling.
4. Start each Vendor from zero CPF objects.
5. Continue first incomplete expensive stage.
6. Repair, do not only report.
7. Keep execution, defect and verification history updated.

## Protected

No commit/push/reset/restore/stash/clean.
No user DB reset.
No Docker prune/down -v/image/volume/secret deletion.
No Vendor SQL first.
No old Primary restoration.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.

``n

### Snapshot: cpf-docs/work/codex/qa38/CPF_CODEX_QA38_VERIFICATION_REMEDIATION_REQUEST.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF QA38 전수검수·보완 개발·최종 봉인 요청서

## 1. 기준

- Repository: `C:\dev\projects\jck\202412_01_CPF`
- Branch: `master`
- Review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Docker: `C:\dev\Docker\CPF`
- Official DB: Oracle, PostgreSQL, MariaDB
- Codex role: independent verifier and source remediator
- Git write: prohibited

작업 시작 시 actual HEAD와 origin/master를 확인한다. 다르면 actual clean remote HEAD를 기준으로 하고 이유를 기록한다.

## 2. QA37 이력 사용

QA37 focused/static PASS는 Root Cause 반복 분석을 줄이는 참고 자료다.
다음 조건을 모두 만족할 때만 Stage skip이 가능하다.

- current gitHead 동일
- command hash 동일
- relevant source/config/sql/profile 동일
- environment/vendor 동일
- exit 0
- log and log hash 존재
- artifact stage는 artifact hash 존재

그 외에는 미검증이다.

## 3. 검수만 하고 끝내지 않기

Source Defect 발견 시:

```text
root cause
→ owner/consumer/impact
→ source/sql/test/config/generator fix
→ targeted verification
→ upper lifecycle once
→ matrix/evidence/history
```

부분 구현, marker-only, consumerless interface, one-vendor-only fix를 남기지 않는다.

## 4. Stage order

`STAGE_PLAN.csv` 순서대로 진행한다.
앞 Stage 실패 시 뒤 비싼 Stage를 실행하지 않는다.
동일 Root Cause 실패는 defect ledger 한 건으로 묶는다.

### Core·Starter

- Core published POM and runtime classpath inventory
- non-Boot Core consumer
- AutoConfiguration and concrete adapter migration
- all real consumers
- generator profiles and resolved lock
- aggregate starter no-bean rule
- provider conflict negative matrix
- footprint budget

### Messaging·TCP

- Kafka actual runtime
- JMS common adapter
- IBM MQ provider
- RabbitMQ provider
- persistent TCP
- ACK/transaction/order/redelivery/DLQ/outage/recovery/unknown/multi-instance
- TLS/secret rotation/masking/readiness/operations

### Fresh DB

Before any DB start:

1. Run canonical/generator/vendor static sync.
2. Confirm official reset/provision path.
3. Snapshot initial container state.
4. Start only one Vendor.
5. Use dedicated QA database/schema.
6. Prove CPF object count is zero.

Then run Fresh Install, metadata/seed, arbitrary generated Domain, runtime query, upgrade, rollback, reapply, different-hash conflict, optional pack off/on, drift and cleanup.

If any official path is missing, implement it before manual execution. Manual SQL is prohibited.

### Final

After Source stable:

- Java 25 fresh lifecycle
- ADM/BZA clean verify
- 3DB actual lifecycle
- runtime/fault/multi-instance
- Playwright Chromium/Firefox/WebKit
- Trivy, secret, SBOM, ORT, license
- final exact-SHA evidence

## 5. History

Continuously update:

- `C:\dev\Docker\CPF\output\codex\qa38\execution-ledger.csv`
- `C:\dev\Docker\CPF\output\codex\qa38\defect-ledger.csv`
- repository `VERIFICATION_HISTORY.csv`
- `CPF_CODEX_CONTINUITY_STATE.md`
- `CPF_CODEX_DECISION_LOG.md`

Repository history contains sanitized summaries and hashes, not secrets or oversized raw logs.

## 6. End states

If source changes are made:

- remediation development: completed only after targeted and upper tests
- final exact-SHA seal: waiting for user commit/push
- overall: `재확인 필요`

After user push, verify manifest equality and run final canonical plan once.
Only then may overall state become `완료`.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.

``n

### Snapshot: cpf-docs/work/codex/qa38/FINAL_OPEN_ISSUES.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Final Open Issues

- 개발 GPT Canonical 169/30/62는 Git 반영 확인; QA38 최종 Crosswalk 검증 대기
- latest Git Canonical은 162개이며 Local 169와 Merge 필요
- Core/Common 선택 Runtime 실제 이관 미구현
- 현재 7개 Starter 보완 미완료
- RabbitMQ/JMS/IBM MQ/TCP/Notification 신규 Runtime 미구현
- Generator Profile/Aggregate/resolvedStarters Lock 미구현
- 3 Vendor Empty DB 실제 Lifecycle 미검증
- 보호 Docker 환경에 Rabbit/JMS Broker Fixture 미포함 — Owner 요청
- Runtime/Fault/OTel/Browser/Supply-chain 미검증
- 제품 Source 변경 후 Guide/Deliverable Owner 정합 작업 필요

``n

### Snapshot: cpf-docs/work/codex/qa38/FINAL_REQUIREMENT_STATUS.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
requirement_id,priority,codex_stage,category,canonical_requirement,owner,development_status,verification_status,title,acceptance_criteria,verification_method
QA38-GOV-001,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,재확인 필요,Latest master exact SHA,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-002,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,완료,미검증,다른 GPT 보호 경로,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-003,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,미구현,미검증,개발 GPT 62개 Backlog Import,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-004,P0,00_BASELINE,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,Canonical 169 확인,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-005,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,승인 없는 Requirement 제거 금지,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-006,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,과거 원장 보존,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-007,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,완료,재확인 필요,상태 축 분리,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-008,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,Source 독립 검토,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-009,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,Source Defect와 환경 Blocker,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-010,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,Codex Continuity,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-011,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,미구현,미검증,AI 결정 승인 상태,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-GOV-012,P0,01_CANONICAL,Governance,ARCH-STARTER|DB-FRESH|Requirement Continuity,QA/Governance,부분 구현,미검증,중복·가비지 관리,"승인 없는 누락 0, exact SHA와 원장 Crosswalk 100%",Git/CSV/Canonical/Continuity Gate
QA38-SRC-001,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Dependency Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-002,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Import Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-003,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,AutoConfiguration Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-004,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Consumer Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-005,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,SQL Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-006,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Generator Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-007,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Test Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-008,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Artifact Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-009,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Frontend Graph,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-SRC-010,P0,02_STATIC,Source Audit,ARCH-STARTER,QA/Architecture,부분 구현,미검증,Recent Commit Impact,미분류 경로·Owner·Consumer 0,local code/dependency/sql/test scan
QA38-STARTER-001,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CpfDataSourceConfig MOVE,Target Artifact·Consumer·DB/Generator 영향(Canonical DB/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-002,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CpfMyBatisConfig+dependency/resources MOVE,Target Artifact·Consumer·DB/Generator 영향(Mapper/Runtime Query/3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-003,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CpfAopConfig+ServiceAccessAspect+AspectJ MOVE,Target Artifact·Consumer·DB/Generator 영향(Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-004,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CpfOpenApiAutoConfiguration+Springdoc+Scalar MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-005,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CpfSecurityAutoConfiguration SPLIT_MOVE,Target Artifact·Consumer·DB/Generator 영향(Security metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-006,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,BFF/JDBC Security Runtime SPLIT_MOVE,Target Artifact·Consumer·DB/Generator 영향(Session Schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-007,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Broker Worker/Bridge/JDBC Reliability Repository MOVE,Target Artifact·Consumer·DB/Generator 영향(Outbox/Inbox/DLQ/Replay 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-008,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,JdbcCpfChannelRegistryAdapter MOVE,Target Artifact·Consumer·DB/Generator 영향(Registry schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-009,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Logging+OTel SDK/Exporter Runtime SPLIT_MOVE,Target Artifact·Consumer·DB/Generator 영향(Log DB 선택)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-010,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Remote HTTP Runtime MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-011,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Validation Runtime Provider MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-012,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Fixed-length Spring Component SPLIT,Target Artifact·Consumer·DB/Generator 영향(Layout metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-013,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,FileExchange/SFTP Planned Runtime MOVE_IMPLEMENT,Target Artifact·Consumer·DB/Generator 영향(Transfer Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-014,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Commons Compress/Archive Runtime MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-015,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Redis/Valkey Connection·Template·Listener MOVE,Target Artifact·Consumer·DB/Generator 영향(Cache metadata optional)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-016,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Caffeine Runtime MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-017,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,POI/XLSX Runtime MOVE,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-018,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Service Identity Runtime MOVE,Target Artifact·Consumer·DB/Generator 영향(Identity metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-019,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Registry/Health technical client MOVE,Target Artifact·Consumer·DB/Generator 영향(Registry metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-020,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Secret Provider Runtime EXPAND,Target Artifact·Consumer·DB/Generator 영향(Secret reference metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-021,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,OpenFeature Runtime EXPAND,Target Artifact·Consumer·DB/Generator 영향(Flag/Audit metadata)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-022,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,CircuitBreaker-only Runtime EXPAND,Target Artifact·Consumer·DB/Generator 영향(없음)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-023,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Kafka Producer/Consumer Runtime EXPAND_MIGRATE,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-024,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,RabbitMQ/AMQP NEW,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-025,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,Jakarta JMS NEW,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-026,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,IBM MQ Provider NEW_PLUGIN,Target Artifact·Consumer·DB/Generator 영향(Reliability Ledger)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-027,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,TCP Transport Runtime NEW,Target Artifact·Consumer·DB/Generator 영향(Request/Reconcile Ledger optional)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-028,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,Notification Email/SMS Worker NEW_SPLIT,Target Artifact·Consumer·DB/Generator 영향(Outbox/Delivery/Result)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-029,P1,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,미구현,미검증,Quartz Scheduler NEW_OPTIONAL,Target Artifact·Consumer·DB/Generator 영향(Scheduler schema 3 Vendor)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-STARTER-030,P0,03_CORE_STARTER,Core/Common→Starter,ARCH-STARTER,Core/Common/Starter Owners,부분 구현,미검증,Public API/SPI·Identifiers·Context·Error·Masking·업무 Common KEEP,Target Artifact·Consumer·DB/Generator 영향(공통 Metadata만)·Legacy 제거와 optional-removal이 모두 닫힘,dependency/API/consumer/fresh fixture/runtime
QA38-EXISTING-001,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Security,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-002,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Kafka,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-003,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Cache,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-004,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Observability,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-005,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Resilience,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-006,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Feature Flag,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-EXISTING-007,P0,03_CORE_STARTER,Existing Starter,ARCH-STARTER,Starter Owner,부분 구현,미검증,Secret,실제 Consumer·Failure·Security·Operations·Publication·optional-removal,ApplicationContextRunner+consumer+runtime+artifact
QA38-MSG-001,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,부분 구현,미검증,Provider-neutral Message Envelope·Logical Destination·Binding·ACK/Commit·Unknown Result,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-002,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,부분 구현,미검증,JDBC Outbox·Inbox·DLQ·Replay·Reconcile Ledger 3 Vendor,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-003,P0,04_MESSAGING_TCP,Messaging,EVENT-BROKER,Messaging/Starter/DB Platform,부분 구현,미검증,Kafka Default Profile의 publish/consume/rebalance/offset/DLT/operations,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-004,P0,04_MESSAGING_TCP,Messaging,EVENT-AMQP,Messaging/Starter/DB Platform,미구현,미검증,Exchange·Queue·Binding·Routing·Confirm·Return·ACK/NACK·DLX·Quorum,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-005,P0,04_MESSAGING_TCP,Messaging,EVENT-JMS,Messaging/Starter/DB Platform,미구현,미검증,Queue·Topic·Durable·Selector·Ack Mode·Transaction·Redelivery,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-006,P0,04_MESSAGING_TCP,Messaging,EVENT-IBM-MQ,Messaging/Starter/DB Platform,미구현,미검증,Queue Manager·Channel·TLS·CCDT·Reason Code·Reconnect Extension,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-007,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,미구현,미검증,Kafka+Rabbit/JMS Named Binding·Bridge·Migration·모호성 fail-closed,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-008,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,미구현,미검증,Schema Version·Compatibility·Quarantine,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-009,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,미구현,미검증,TLS/mTLS·Secret Rotation·Masking·Permission·Audit,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-010,P0,04_MESSAGING_TCP,Messaging,EVENT-MQ,Messaging/Starter/DB Platform,미구현,미검증,Backlog·Lag·DLQ·Replay·Reconcile·Approval·Result Tracking,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-MSG-011,P0,04_MESSAGING_TCP,Messaging,TEST-BROKER,Messaging/Starter/DB Platform,미구현,미검증,Broker outage·response loss·duplicate·consumer kill·multi-instance,Provider별 계약·실제 Consumer·3 Vendor Reliability·운영·Fault Evidence,contract+simulator+actual broker/fault
QA38-TCP-001,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Client/Server·Connection Pool·Graceful Drain,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-002,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,부분 구현,미검증,Fixed/Length Header/STX-ETX/CRLF·Fragment/Coalesce,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-003,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Binary/BCD/Hex·Endian·Unsigned,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-004,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,부분 구현,미검증,UTF-8/EUC-KR/EBCDIC Strict Conversion,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-005,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,응답 순서 역전·Orphan·Timeout,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-006,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Heartbeat·Half-open·Reconnect·Backoff/Jitter,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-007,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,TLS/mTLS·Trust·Identity·Rotation,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-008,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Connection/Thread/Queue/Buffer/Frame Limit,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-009,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Write 후 응답 유실·UNKNOWN_RESULT·Reconciliation,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-010,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Bitmap/Field Packager/MAC/PIN Extension SPI,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-011,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,Session·Backlog·Failure·Replay·Reconcile·Audit,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-TCP-012,P0,04_MESSAGING_TCP,TCP/Professional Message,EXS-TCP,Integration/Starter,미구현,미검증,정상·Partial·Oversize·Malformed·Delay·Disconnect Scenario,Transport·Codec·Consumer·Simulator·Operations·Fault Closure,codec corpus+TCP simulator+actual endpoint/fault
QA38-INT-001,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,부분 구현,미검증,SFTP actual adapter·upload/download/list/move/delete/resume/checksum/reconcile,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-002,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,FTPS/SMB/Object Storage provider SPI와 secure path/resource policy,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-003,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,부분 구현,미검증,Typed HTTP Client·timeout·deadline·idempotency·response loss,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-004,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,WSDL/Fault/WS-Security extension,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-005,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,Deadline/Metadata/Status/Streaming/Backpressure,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-006,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,Signature/Retry/Idempotency/Delivery Ledger,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-007,P1,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,WebSocket/SSE auth/reconnect/backpressure/scale-out,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-008,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,부분 구현,미검증,Notification Outbox/Worker/Retry/DLQ/Preference/Quiet Hours,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-009,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,SMTP/Provider·Template·Attachment·Bounce·Unknown Result,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-INT-010,P0,04_MESSAGING_TCP,External Integration,ARCH-STARTER,Integration/Notification,미구현,미검증,Provider SPI·Rate·Receipt·Callback·Unknown Result,Public Contract·Adapter·Consumer·Failure·Security·Operations,contract/simulator/available runtime/fault
QA38-GROUP-001,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Leaf Naming,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-002,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Base Starter,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-003,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Capability Profile,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-004,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Aggregate Starter,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-005,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Resolved Lock,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-006,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Named Binding,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-007,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Conflict Gate,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-008,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,부분 구현,미검증,BOM,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-009,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Publication,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-010,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Optional Removal,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-011,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Footprint,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-012,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Upgrade,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-013,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,부분 구현,미검증,Generator,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-GROUP-014,P0,03_CORE_STARTER,Starter Group/Generator,ARCH-STARTER,Starter Architecture/Generator,미구현,미검증,Legacy Removal,Profile/Build/Manifest/Artifact/Runtime 양방향 일치,fresh generated domain+dependency report+negative combinations
QA38-DB-001,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Preflight,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-002,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,부분 구현,미검증,Generator First,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-003,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,부분 구현,미검증,No Direct Edit,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-004,P0,06_DB_MARIADB,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,MariaDB Fresh,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-005,P0,07_DB_POSTGRESQL,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,PostgreSQL Fresh,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-006,P0,08_DB_ORACLE,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Oracle Fresh,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-007,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Upgrade,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-008,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Rollback,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-009,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Reapply,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-010,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Conflict,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-011,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Optional Pack,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-012,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Backup/Fault,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-013,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Cleanup,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-DB-014,P0,05_DB_TOOLING,DB Fresh/Generator,DB-FRESH,DB Platform/Generator,미구현,미검증,Evidence,MariaDB/PostgreSQL/Oracle 각각 Empty lifecycle와 exact Evidence,sequential disposable DB lifecycle
QA38-CONSUMER-001,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,ADM,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-002,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,BZA,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-003,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,Gateway,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-004,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,Batch,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-005,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,Reference,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-006,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,부분 구현,미검증,cpf-member,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-007,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,미구현,미검증,Generated Domain,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-CONSUMER-008,P0,03_CORE_STARTER,Consumer Migration,ARCH-STARTER,Product Owner,미구현,미검증,Customer Plugin,"실제 Consumer와 optional-removal, Route/Permission/Owner 책임 유지",architecture test+focused runtime+artifact
QA38-VERIFY-001,P0,00_BASELINE,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,부분 구현,미검증,00_BASELINE,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-002,P0,01_CANONICAL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,부분 구현,미검증,01_CANONICAL,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-003,P0,02_STATIC,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,부분 구현,미검증,02_STATIC,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-004,P0,03_CORE_STARTER,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,03_CORE_STARTER,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-005,P0,04_MESSAGING_TCP,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,04_MESSAGING_TCP,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-006,P0,05_DB_TOOLING,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,05_DB_TOOLING,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-007,P0,06_DB_MARIADB,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,06_DB_MARIADB,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-008,P0,07_DB_POSTGRESQL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,07_DB_POSTGRESQL,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-009,P0,08_DB_ORACLE,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,08_DB_ORACLE,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-010,P0,09_JAVA_FULL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,09_JAVA_FULL,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-011,P0,10_FRONTEND,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,부분 구현,미검증,10_FRONTEND,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-012,P0,11_RUNTIME,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,11_RUNTIME,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-013,P0,12_FAULT_OTEL,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,12_FAULT_OTEL,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-014,P0,13_BROWSER,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,13_BROWSER,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-015,P0,14_SUPPLY_CHAIN,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,14_SUPPLY_CHAIN,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger
QA38-VERIFY-016,P0,15_TRUTH,Verification,TEST-BROKER|DB-FRESH|ARCH-STARTER,Codex/QA,미구현,미검증,15_TRUTH,같은 exact SHA·Command Hash·환경·Log·Artifact Hash의 PASS,stage wrapper/execution ledger

``n

### Snapshot: cpf-docs/work/codex/qa38/FINAL_REVIEW_INDEX.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Final Codex Review Index

## 첫 읽기

1. `cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_PATHS.md`
2. `cpf-docs/work/current/CPF_20260802_06_QA38_FINAL_INTEGRATED_REQUEST.md`
3. 이 파일

## 선행 Import

1. 개발 GPT Local Canonical 169
2. Core→Starter 30개 원본
3. Self Development Backlog 62개
4. 기존 CODEX_START_HERE와 Remediation Request

Import 결과를 `CPF_20260802_06_QA38_DEVELOPER_BACKLOG_IMPORT_STATUS.csv`에 갱신한다.

## 검수 순서

1. Canonical Merge Review
2. Final Requirements
3. Core→Starter Final Review
4. Source/Consumer/DB/Generator Graph
5. Stage 00~15
6. Test and Evidence
7. Open Issues
8. Handover·Hygiene

## 크레딧 절약

- exact-SHA 유효 PASS만 승계
- 실패 Root Cause 수정 전 재실행 금지
- Focused Test 후 상위 Lifecycle 한 번
- 보호 환경 변경은 Owner 작업으로 넘김
- 같은 Source 탐색 결과를 Manifest로 재사용

## 완료 차단

- 보호 경로 변경
- 62개 원본 Import 누락
- 실제 Consumer 없는 Starter
- DB Empty 증명 없음
- Runtime 미실행을 완료로 기록
- Rabbit/JMS/TCP를 환경 부재로 삭제

``n

### Snapshot: cpf-docs/work/codex/qa38/FINAL_TEST_AND_EVIDENCE.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Final Test and Evidence

## 이 패키지에서 확인

- latest master `2e93d92393c52b887482731b683db3c3822027b1`
- latest Canonical이 162개임
- latest settings에 공식 Starter 7개 등록
- latest Commit의 보호 영역 변경 범위
- 사용자 제공 개발 GPT 요약 병합
- Final Requirement ID 중복·상태 검사
- Core→Starter Review 30개 구조 검사
- 보호 Prefix Overlay 0
- ZIP CRC·내부 SHA-256
- Apply Script 정적 Guard

## 미실행

- 개발 GPT Local 169/30/62 원본 행 Import
- Java/Frontend Build
- DB 3 Vendor Empty Lifecycle
- Kafka/Rabbit/JMS/IBM MQ/TCP/SFTP/Notification Runtime
- Multi-instance/Fault/OTel/Browser/Supply-chain
- PowerShell 실제 적용

미실행은 모두 `미검증`이다.

``n

### Snapshot: cpf-docs/work/codex/qa38/STAGE_PLAN.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
stage_order,stage_id,scope,prerequisite,completion_rule
00,BASELINE,Git/head/tree/commit range/required docs/Docker read-only snapshot,always,No expensive work if source baseline invalid
01,CANONICAL,Final Target/Ledger/Current/State/Matrix integrity and recovered requirements,00,169 IDs and trace links
02,STATIC,diff/hygiene/secret/ownership/dependency/public boundary/generator/source closure,01,one integrated low-cost pass
03,CORE_STARTER,"Core split, leaf starters, consumers, profiles, aggregate, BOM/publication",02,actual source remediation and variant tests
04,MESSAGING_TCP,Kafka/JMS/IBM MQ/RabbitMQ/TCP vertical slices,03,actual provider/fault tests
05,DB_TOOLING,canonical/generator/lifecycle tooling static/dry-run,03,no manual SQL; all vendor paths
06,DB_MARIADB,zero-object fresh lifecycle,05,actual MariaDB
07,DB_POSTGRESQL,zero-object fresh lifecycle,06,actual PostgreSQL
08,DB_ORACLE,zero-object fresh lifecycle,07,actual Oracle
09,JAVA_FULL,Java25 fresh build/test/publication,03-08,single upper lifecycle
10,FRONTEND,ADM/BZA npm ci/lint/type/unit/build,09,single clean verify
11,RUNTIME,Kafka/Redis/Batch/Scheduler/Gateway/multi-instance/process kill,09,actual Docker services
12,FAULT_OTEL,Toxiproxy and OpenTelemetry outage/masking,11,fault/recovery evidence
13,BROWSER,Chromium/Firefox/WebKit,10-12,route/session/errors/a11y/responsive
14,SUPPLY_CHAIN,Trivy/secret/SBOM/ORT/license/artifact hashes,09-13,final artifacts only
15,TRUTH,Matrix/Evidence/continuity/current/delete review/exact SHA,all,zero source defects and mandatory unverified

``n

### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_CHANGE_MANIFEST.csv
- SHA-256: $sha
- Preserved before QA38 currentization

``text
path,change_type,purpose,owner,impact
cpf-docs/quality/CPF_20260802_06_QA38_CANONICAL_MERGE_CROSSWALK.csv,ADD,Final QA matrix,QA,No product source/protected path changes
cpf-docs/quality/CPF_20260802_06_QA38_CORE_TO_STARTER_FINAL_REVIEW.csv,ADD,Starter review/requirements,Architecture/QA,No product source/protected path changes
cpf-docs/quality/CPF_20260802_06_QA38_DEVELOPER_BACKLOG_IMPORT_STATUS.csv,ADD,Final QA matrix,QA,No product source/protected path changes
cpf-docs/quality/CPF_20260802_06_QA38_FINAL_REQUIREMENTS.csv,ADD,Final QA matrix,QA,No product source/protected path changes
cpf-docs/work/codex/qa38/FINAL_OPEN_ISSUES.md,ADD,Codex review package,QA,No product source/protected path changes
cpf-docs/work/codex/qa38/FINAL_REQUIREMENT_STATUS.csv,ADD,Codex review package,QA,No product source/protected path changes
cpf-docs/work/codex/qa38/FINAL_REVIEW_INDEX.md,ADD,Codex review package,QA,No product source/protected path changes
cpf-docs/work/codex/qa38/FINAL_TEST_AND_EVIDENCE.md,ADD,Codex review package,QA,No product source/protected path changes
cpf-docs/work/current/CPF_20260802_06_QA38_FINAL_INTEGRATED_REQUEST.md,ADD,Final QA current package,QA,No product source/protected path changes
cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_OWNER_ACTIONS.md,ADD,Protection/Owner coordination,QA/Governance,No product source/protected path changes
cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_PATHS.md,ADD,Protection/Owner coordination,QA/Governance,No product source/protected path changes
cpf-docs/work/handover/CPF_20260802_06_QA38_FINAL_HANDOVER.md,ADD,Handover,QA,No product source/protected path changes
cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_CANONICAL_MERGE_REVIEW.md,ADD,Independent review,Architecture/QA,No product source/protected path changes
cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_DEVELOPER_GPT_MERGE_REVIEW.md,ADD,Independent review,Architecture/QA,No product source/protected path changes
cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_FINAL_REVIEW.md,ADD,Independent review,Architecture/QA,No product source/protected path changes
cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_STARTER_FINAL_REVIEW.md,ADD,Starter review/requirements,Architecture/QA,No product source/protected path changes
cpf-tools/scripts/apply-cpf-qa38-final-requirements-review.ps1,ADD,Safe overlay apply script,Tooling,No product source/protected path changes

``n

### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_FILES.sha256
- SHA-256: $sha
- Preserved before QA38 currentization
- Binary content is in temp backup.


### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_FINAL_INTEGRATED_REQUEST.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF QA38 최종 통합 개발·검수 요청

- Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch/SHA: `master` / `2e93d92393c52b887482731b683db3c3822027b1`
- QA38 Final Requirement: `144개`
- Core→Starter Review: `30개`
- 입력: latest Git + 개발 GPT 미Push 요약 + 과거 Requirement 복구
- Git Commit/Push/Branch/Tag/Release: 사용자 승인 전 금지

## 역할

개발 GPT와 Codex는 문서에 적힌 항목만 처리하는 실행자가 아니다.
CPF 최종 상용 품질 책임자로서 Source·Consumer·Failure·Security·Operations·Generator·DB·Artifact의
누락을 선제적으로 발견하고 같은 Requirement에 추가한다.

## 보호 경로

다음은 읽기·참조만 허용한다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

변경 필요 시 Owner Action 문서에 기록한다. 적용 Script·Delete Manifest·Codex 자동 보정 금지.

## 시작 순서

1. latest `origin/master`, Working Tree, 최근 Commit
2. 보호 경로 확인
3. 개발 GPT 로컬 169·30·62·Codex 문서 Import
4. Canonical Merge Crosswalk
5. Final Requirement Matrix
6. Core→Starter Final Review
7. 첫 미완료 P0

## 핵심 Architecture

```text
cpf-core
  Public API/SPI·Identifier·Header·Context·Error·Masking·Audit·Provider-neutral 계약

cpf-common
  실제 고객 업무 공통만

cpf-starter-*
  선택 Runtime·Provider·AutoConfiguration

Generator Profile
  → Leaf Starter
  → Build Dependency
  → resolvedStarters
  → Profile/Starter Version Lock
```

Kafka는 Default Profile이다. RabbitMQ/AMQP, JMS, IBM MQ, TCP를 삭제하지 않는다.

## DB

각 Vendor 검수 전:

```text
전용 QA DB/Schema
CPF Object count = 0
Canonical/Generator First
사용자 DB Reset/Drop 금지
생성 Vendor SQL 직접 수정 금지
```

## Stage

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

비싼 Runtime Stage는 Source 안정화 후 실행하되 생략하지 않는다.

## 완료 조건

- 개발 GPT 원본 62개 Import 누락 0
- Canonical 169 Crosswalk 완료
- Final Requirement P0의 development_status/verification_status 모두 완료
- Core/Common 선택 Runtime 제거
- 실제 Starter Consumer·Generator·DB·Artifact Closure
- RabbitMQ/JMS/IBM MQ/TCP Runtime 또는 명시적 환경 Blocker와 Source 완료 분리
- 보호 경로 변경 0
- latest exact-SHA Evidence

``n

### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_PACKAGE_MANIFEST.json
- SHA-256: $sha
- Preserved before QA38 currentization

``text
{
  "schema": "cpf.root-overlay-package",
  "schemaVersion": 1,
  "packageName": "CPF_20260802_QA38_FINAL_INTEGRATED_REQUIREMENTS_REVIEW_MERGE_SAFE_ROOT_OVERLAY",
  "repository": "freeangelsun/202412_01_CPF",
  "branch": "master",
  "baselineSha": "2e93d92393c52b887482731b683db3c3822027b1",
  "generatedAtKst": "2026-08-02T17:03:24.144554+09:00",
  "requirementCount": 144,
  "coreToStarterReviewCount": 30,
  "canonicalCrosswalkCount": 10,
  "protectedPrefixes": [
    "cpf-docs/deliverables/",
    "cpf-docs/guides/",
    "cpf-docs/environment/docker/",
    "cpf-tools/environment/docker-development-test/"
  ],
  "protectedPathFileCount": 0,
  "deleteCandidateCount": 0,
  "developmentStatus": "부분 구현",
  "verificationStatus": "미검증",
  "inputTrust": {
    "latestGit": "VERIFIED",
    "canonical169": "VERIFIED_IN_GIT",
    "coreToStarter30": "VERIFIED_IN_GIT",
    "developerBacklog62": "VERIFIED_IN_GIT"
  },
  "included": [
    "final integrated QA request",
    "final requirement matrix",
    "canonical merge crosswalk",
    "developer GPT merge/import review",
    "core to starter final review",
    "protected path rules and owner actions",
    "Codex review package",
    "handover and safe apply script"
  ],
  "excluded": [
    "product source implementation",
    "canonical automatic modification",
    "protected paths",
    "file deletion",
    "git commit/push",
    "runtime execution"
  ],
  "files": [
    {
      "path": "cpf-docs/quality/CPF_20260802_06_QA38_CANONICAL_MERGE_CROSSWALK.csv",
      "sha256": "302d3540952bfddddf24c475dff71c304bb32c3c95d17ef6674c43a0ffb4dab4",
      "size": 1535
    },
    {
      "path": "cpf-docs/quality/CPF_20260802_06_QA38_CORE_TO_STARTER_FINAL_REVIEW.csv",
      "sha256": "70ca90abca7844d5f83df65396ea05fd545e8adf36011b2cdeaf6dee7afb33f2",
      "size": 5528
    },
    {
      "path": "cpf-docs/quality/CPF_20260802_06_QA38_DEVELOPER_BACKLOG_IMPORT_STATUS.csv",
      "sha256": "cb57fda433b64e0a72d70a614ae21377bf5a9eb3c781517c2667d7a27b7e5389",
      "size": 834
    },
    {
      "path": "cpf-docs/quality/CPF_20260802_06_QA38_FINAL_REQUIREMENTS.csv",
      "sha256": "efbe2e8784eb2ee87077e395e5dc402c5f011effccc41e8fe27236be549a46f1",
      "size": 72080
    },
    {
      "path": "cpf-docs/work/codex/qa38/FINAL_OPEN_ISSUES.md",
      "sha256": "e240fd88892589c2ae313d16867f71729442b563683edce8d8b224cc81540ba8",
      "size": 650
    },
    {
      "path": "cpf-docs/work/codex/qa38/FINAL_REQUIREMENT_STATUS.csv",
      "sha256": "c451206051b6199c31e649f5d10e348af41eddd329f617a877e2e6e9d78950fc",
      "size": 37381
    },
    {
      "path": "cpf-docs/work/codex/qa38/FINAL_REVIEW_INDEX.md",
      "sha256": "919e708435a1674c17bf8b90a5c718e2ebc9afb9a2dedc44df4f67dda79480a2",
      "size": 1143
    },
    {
      "path": "cpf-docs/work/codex/qa38/FINAL_TEST_AND_EVIDENCE.md",
      "sha256": "3ac67c747de260d07f8fa792a4cdceccc363ca0681d1acae88623fe299994733",
      "size": 748
    },
    {
      "path": "cpf-docs/work/current/CPF_20260802_06_QA38_CHANGE_MANIFEST.csv",
      "sha256": "61e32a83000a7eb2e983e63df2843204073f598fc747014adbbaccea358511cb",
      "size": 2424
    },
    {
      "path": "cpf-docs/work/current/CPF_20260802_06_QA38_FINAL_INTEGRATED_REQUEST.md",
      "sha256": "f1c50f9487c90544d176bd43ef58897700242155f87051c048386735d6da2374",
      "size": 2609
    },
    {
      "path": "cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_OWNER_ACTIONS.md",
      "sha256": "a1317783b0925c7ae8f51f6d08f4c2c5c5034c9ff7684a1d28914cc20386cfc0",
      "size": 1407
    },
    {
      "path": "cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_PATHS.md",
      "sha256": "3cd5ab335eaf8864170f5b111d21ba2233cc56a53ce35c425b072164240ecbf2",
      "size": 626
    },
    {
      "path": "cpf-docs/work/handover/CPF_20260802_06_QA38_FINAL_HANDOVER.md",
      "sha256": "f58c7ea0ca9ba215bdd2c7d73051d5fad97250ddb5b8c59fc389bb04a8f22062",
      "size": 672
    },
    {
      "path": "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_CANONICAL_MERGE_REVIEW.md",
      "sha256": "3c7de5cef718eb24d85b11768e95b01877a57ed471badd384b5cc40e54f016a3",
      "size": 1962
    },
    {
      "path": "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_DEVELOPER_GPT_MERGE_REVIEW.md",
      "sha256": "5f8505258a1d5eff6621fa8f6fd21f521134da0a8290d6eac472a0f91233f068",
      "size": 2352
    },
    {
      "path": "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_FINAL_REVIEW.md",
      "sha256": "9330e87ac9ccc8c31ee2ed493cf663391adb53dd34fddb7e873a0d6485296f04",
      "size": 2065
    },
    {
      "path": "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_STARTER_FINAL_REVIEW.md",
      "sha256": "15f95c0fe41afe17efa0051d19f672353ddcaaf7f5d8212ba4ed1ba0901824a8",
      "size": 2460
    },
    {
      "path": "cpf-tools/scripts/apply-cpf-qa38-final-requirements-review.ps1",
      "sha256": "9b9f07e11221920923a46e778f79f0c8f604f74a651e2684b8293d7a092c5357",
      "size": 7951
    }
  ],
  "mergeSafeMarkdownPaths": [
    "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_CANONICAL_MERGE_REVIEW.md",
    "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_DEVELOPER_GPT_MERGE_REVIEW.md",
    "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_FINAL_REVIEW.md",
    "cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_STARTER_FINAL_REVIEW.md"
  ],
  "applyBehavior": {
    "mergeManagedMarkdown": "BACKUP_AND_MARKER_MERGE",
    "otherLocalChanges": "FAIL_BEFORE_APPLY",
    "protectedPaths": "FAIL_BEFORE_APPLY",
    "delete": "NONE"
  }
}

``n

### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_OWNER_ACTIONS.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 보호 영역 Owner 작업 요청

- latest reviewed master: `2e93d92393c52b887482731b683db3c3822027b1`
- 이 문서는 요청만 기록하며 보호 영역을 수정하지 않는다.

## Docker·환경 Owner 확인 요청

현재 latest Commit의 보호 영역에는 WireMock·SFTP Fixture·Vault·Keycloak·Toxiproxy·OTel 등이
반영돼 있다. RabbitMQ·ActiveMQ/Artemis·IBM MQ/JMS Broker는 기본 설치 대상에서 제외돼 있다.

QA38 기능 지원 Requirement와 환경 기본 번들은 별개다. 다음을 Owner가 검토한다.

1. RabbitMQ 실제 Runtime Test를 위한 격리 Compose/Profile 또는 외부 연결 절차
2. Jakarta JMS Contract Test Provider로 Artemis 등 사용 가능 여부
3. IBM MQ는 Proprietary Server/Driver 기본 번들 없이 고객 제공 환경 연결 절차
4. TCP 전문 Simulator와 Half-open·Response-loss·TLS Fault Fixture
5. SMTP/SMS Provider Simulator
6. MariaDB·PostgreSQL·Oracle의 Empty DB Preflight Hook과 Evidence 출력
7. 각 추가 Fixture의 Image Digest Lock·Secret 외부화·Host Port·Toxiproxy 연동
8. Container가 없는 환경의 Contract/Simulator Test와 실제 Runtime Test 상태 분리

Owner가 변경할 수 있는 경로:

- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`
- 관련 Docker Guide·Deliverable

QA38 담당자는 위 경로를 직접 수정하지 않는다.

``n

### Snapshot: cpf-docs/work/current/CPF_20260802_06_QA38_PROTECTED_PATHS.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF 다른 GPT 소유 보호 경로

다음 경로는 다른 GPT가 관리한다. QA38 작업은 읽기·참조만 허용한다.

cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**

금지:

- 신규 파일 추가
- 내용 수정·덮어쓰기
- 이동·이름 변경
- 삭제
- Delete Manifest 포함
- Overlay ZIP 포함
- 적용 Script의 Copy/Remove 대상
- Codex 자동 보정

결함·누락을 발견하면 `CPF_20260802_06_QA38_PROTECTED_OWNER_ACTIONS.md`에 요청만 남긴다.
인수인계 시 이 보호 규칙을 그대로 승계한다.

``n

### Snapshot: cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF Current Work Request — Post-QA37 Integrated Remediation Primary

- Review baseline `origin/master`: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Branch: `master`
- Current package: `CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
- Current overall state: `부분 구현 / 미검증`
- Codex QA37 focused/static results: reference only; latest exact-SHA final closure is not established
- Official DB Vendors: Oracle, PostgreSQL, MariaDB
- Official Root: `cpf-starters/`
- Permanent direction: Lightweight `cpf-core` + explicit Leaf Starter + Generator Capability Profile + limited Aggregate Starter

## First read order

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
3. `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`
4. `cpf-docs/work/current/CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
5. `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
6. `cpf-docs/work/codex/qa38/CODEX_START_HERE.md`

## Current integrated scope

- Reconcile all pushes after QA37 with actual Source/SQL/Test/Config/Frontend/Generator
- Finish Core-to-Starter modularization and real Domain consumers
- Implement Capability Profile and approved Aggregate Starter mechanism
- Restore MQ/JMS/IBM MQ/RabbitMQ/TCP requirements and implementations
- Execute Generator-first Fresh DB lifecycle for Oracle/PostgreSQL/MariaDB
- Complete Java, Frontend, DB, Runtime, Fault, Browser and Supply-chain exact-SHA validation
- Consolidate current/canonical documents and remove only approved stale artifacts
- Update Codex execution/defect/verification history continuously

## Non-negotiable rules

- Fix Canonical Metadata/Generator first; never patch Vendor SQL first.
- Each DB validation starts from a dedicated CPF QA Database/Schema with CPF Object count 0.
- Existing user databases and Docker assets are protected.
- Findings are not completion; actual source remediation and revalidation are required.
- Do not reuse prior PASS as current success unless exact HEAD, command, environment and artifact hashes match.
- No partial implementation, marker-only Starter, consumerless abstraction or dual primary.
- No Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean without explicit user approval.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.

``n

### Snapshot: cpf-docs/work/handover/CPF_20260802_06_QA38_FINAL_HANDOVER.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Final Handover

- 기준 SHA: `2e93d92393c52b887482731b683db3c3822027b1`
- Final Requirement: `144개`
- Core→Starter Review: `30개`

## 첫 작업

1. 보호 경로 확인
2. 개발 GPT Local 169/30/62 Import
3. Canonical Crosswalk
4. Stage 02 Source Graph
5. Stage 03 Core/Starter

## 절대 원칙

- 보호 경로 수정·삭제 금지
- 사용자 승인 없는 Requirement 삭제 금지
- Kafka Default를 Rabbit/JMS/TCP 제외로 해석 금지
- DB는 Empty 상태·Generator First
- Source/Runtime 미검증을 완료로 기록 금지
- Git reset/restore/stash/clean 금지

## 첫 미완료

`QA38-GOV-003` — 개발 GPT 62개 Backlog 행 단위 Import

``n

### Snapshot: cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_CANONICAL_MERGE_REVIEW.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Canonical 169 병합 리뷰

## 최신 Git 상태

원격 `master` `2e93d92393c52b887482731b683db3c3822027b1`의 `CPF_FINAL_TARGET_REQUIREMENTS.md`는 아직 162개다.
개발 GPT가 보고한 169개 정본은 아직 Push되지 않았으므로 Git에서 행 단위 검증할 수 없다.

## 병합 원칙

개발 GPT의 다음 7개 Requirement는 유효한 복구 입력으로 수용한다.

- ARCH-STARTER
- DB-FRESH
- EVENT-MQ
- EVENT-JMS
- EVENT-IBM-MQ
- EVENT-AMQP
- EXS-TCP

`TPC`는 원문 Alias로 보존하고 현재는 `EXS-TCP`에 연결한다.

기존 `EVENT-BROKER`, `TEST-BROKER`는 Kafka 전용 의미에서 Provider Matrix 계약으로 확장한다.

## 축소 금지

169개 정본은 과거 162개에 7개를 추가하는 최소 Canonical Layer다.
QA38의 상세 Requirement·Failure·Consumer·Generator·운영 요건을 169개로 대체하거나 삭제하지 않는다.

```text
Canonical 169
  제품 목표와 상위 계약

QA38 Final Requirement Matrix
  구현·검증·운영·실패·Consumer·Evidence 세부 기준
```

## Codex 첫 병합 Gate

1. 로컬 `CPF_FINAL_TARGET_REQUIREMENTS.md`가 실제 169개인지 확인
2. 7개 ID·TPC Alias·EVENT-BROKER·TEST-BROKER 내용을 Crosswalk와 비교
3. 사용자 승인 없는 `removed/superseded`가 없는지 확인
4. QA38 상세 Matrix의 모든 P0가 Canonical 169 중 하나에 연결되는지 확인
5. 다른 GPT의 Local 변경을 덮어쓰지 않고 Merge Diff를 별도 제시

이 패키지는 Canonical 파일을 자동 수정하지 않는다.


## 최신 정본 상태 교정

Commit `2e93d92393c52b887482731b683db3c3822027b1`에서 Canonical Requirement 169개와 다음 복구 항목을 확인했다.

- ARCH-STARTER
- DB-FRESH
- EVENT-MQ
- EVENT-JMS
- EVENT-IBM-MQ
- EVENT-AMQP
- EXS-TCP
- TPC → EXS-TCP Alias
- EVENT-BROKER·TEST-BROKER Provider Matrix 확장

따라서 Canonical 169 병합은 `완료`, Runtime 검증은 `미검증`으로 분리한다.

``n

### Snapshot: cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_DEVELOPER_GPT_MERGE_REVIEW.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 개발 GPT 결과 병합 리뷰

## 입력 상태

개발 GPT 산출물은 최신 master Commit `2e93d92393c52b887482731b683db3c3822027b1`에 Push되어 Git에서 확인됐다.

- Canonical 162 → 169
- Core→Starter 이동·분리·유지 후보 30개
- `CPF_20260802_05_POST_QA37_SELF_DEVELOPMENT_REQUIREMENTS.csv` 62개
- QA38 Codex Start/Request와 Stage 00~15
- DB Fresh·Generator First 절차

따라서 이 리뷰는 사용자가 제공한 요약을 `VERIFIED_IN_GIT_AT_2E93D923`으로 병합한다.
행 단위 원본 검증은 Codex Stage 00에서 수행한다.

## 수용

- 7개 Canonical Requirement와 TPC Alias
- Provider Matrix로 확장된 EVENT-BROKER/TEST-BROKER
- 30개 Core→Starter 후보
- Profile→Leaf Expansion→resolvedStarters→Version Lock
- Aggregate Starter는 Dependency 전용
- Mega Starter 금지
- JMS와 IBM MQ의 책임 분리
- RabbitMQ의 AMQP 고유 의미
- TCP의 영속 연결·Framing·Heartbeat·Unknown Result
- Vendor별 Empty DB와 Generator First
- Stage 00~15 순서

## 보강

개발 GPT의 62개 Backlog는 구현 우선순위층으로 사용하고 다음을 추가한다.

- 사용자 승인 없는 Requirement 삭제 통제
- 현재 7개 Starter의 세분화·보완
- Starter 실제 Consumer Migration
- RabbitMQ/JMS/TCP 외 누락 Integration·Notification Capability
- Named Multi-provider Binding
- DB·Generator·Artifact·BOM·SBOM·Upgrade/Rollback
- Legacy·Dual Primary 제거
- Multi-instance·Fault·Unknown Result·Operations
- exact-SHA Runtime Evidence
- 보호 경로 Owner 요청

## 대체하지 않는 것

62개 Backlog가 QA38 최종 Matrix를 대체하지 않는다.
Codex는 62개 원본 행을 `CPF_20260802_06_QA38_DEVELOPER_BACKLOG_IMPORT_STATUS.csv`에 Import하고,
최종 Requirement와 1:1 또는 N:1 Crosswalk를 작성한다.

원본에만 있고 최종 Matrix에 없는 행은 자동 누락으로 처리하고 새 Requirement를 추가한다.


## 최신 Git 재확인

- Commit: `2e93d92393c52b887482731b683db3c3822027b1`
- Canonical Requirement: 169개 반영 확인
- Core→Starter Migration Review: 30개 반영 확인
- Post-QA37 Self Development Requirement: 62개 반영 확인
- QA38 Codex 시작 문서와 Stage 00~15 반영 확인

따라서 이전 `USER_PROVIDED_UNPUSHED_INPUT` 상태는 폐기하고 `VERIFIED_IN_GIT`으로 갱신한다.

``n

### Snapshot: cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_FINAL_REVIEW.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF QA38 최종 통합 리뷰

## 검토 기준

- latest origin/master: `2e93d92393c52b887482731b683db3c3822027b1`
- latest Git Canonical: 162개
- 개발 GPT 보고: Local Canonical 169, Core→Starter 30, Backlog 62, Stage 00~15
- QA38 Final Requirement: 144개
- 보호 경로: 4개 Prefix

## 실제 Git과 개발 GPT 입력 차이

개발 GPT의 Canonical 169개, Core→Starter 30개, Self Development 62개는 최신 Git에서 확인됐다.
이번 패키지는 사용자 제공 요약을 반영하고, Codex Stage 00~01에서 로컬 원본을 반드시 Import한다.

Latest Commit은 보호 경로의 Docker·가이드·산출물 변경이 중심이다.
이 패키지는 해당 경로를 변경하거나 삭제하지 않는다.

## 병합 결과

- Canonical 7개 복구 ID 수용
- TPC Alias→EXS-TCP
- EVENT-BROKER/TEST-BROKER Provider Matrix 확장
- 30개 Core→Starter 후보 수용·보강
- 62개 Backlog를 구현 우선순위 입력으로 보존
- Kafka Default와 Rabbit/JMS/IBM MQ/TCP 공식 지원 양립
- DB Fresh·Generator First 영구 규칙
- Starter Profile/Aggregate/Resolved Lock
- 16개 Codex Stage
- 보호 Owner Action 분리

## 중요한 추가 보강

- 현재 7개 Starter의 실제 Gap
- Consumer Migration
- Named Multi-provider Binding
- Reliability JDBC Starter
- Notification·SFTP 실제 Runtime
- Artifact/BOM/SBOM·Upgrade/Rollback
- Legacy·Dual Primary 제거
- Fault·Unknown Result·Operations
- exact-SHA Evidence

## 현재 상태

```text
development_status = 부분 구현
verification_status = 미검증
```

문서·요건·리뷰 패키지는 완료했지만 Source 개발과 Runtime 검증은 수행하지 않았다.


## 다중 GPT 동일 경로 병합 정책

동일 리뷰 파일이 다른 GPT에 의해 먼저 생성·수정된 경우:

1. 기존 파일을 임시 Backup한다.
2. 전체 파일을 덮어쓰지 않는다.
3. QA38 Merge Marker 구간만 갱신한다.
4. Marker 밖의 다른 GPT 작성 내용은 보존한다.
5. CSV·Source·보호 경로는 자동 텍스트 병합하지 않는다.

``n

### Snapshot: cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_STARTER_FINAL_REVIEW.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# QA38 Starter 편입 최종 독립 리뷰

## 판정

개발 GPT의 30개 후보를 수용하고 최신 Source 관찰과 상용 품질 기준으로 보강했다.

- Core/Common 이동·분리·유지 후보: 30개
- P0는 Dependency 이동만이 아니라 Source Package·Consumer·Generator·DB·Artifact·Legacy 제거까지 포함
- Starter Module 존재만으로 완료 처리 금지

## Core에 남길 것

- Public API/SPI
- Identifier·Header·Context
- Error·Validation 값 계약
- Provider-neutral Message Envelope/Port
- Security·Masking·Audit 계약
- 순수 Java Fixed-length/File 계약
- topology-independent Local/Remote 계약

Spring Component·AutoConfiguration·Provider SDK·JDBC Repository·Worker는 Core에 남기지 않는다.

## 신규 복구

- RabbitMQ/AMQP
- Jakarta JMS
- IBM MQ Provider Extension
- TCP
- Notification Email/SMS
- SFTP 실제 Adapter
- 필요 시 Cloud Queue·Redis Streams·FTPS/SMB/Object Storage는 Capability Catalog에서 후속 P1로 유지

## 현재 7개 Starter

Security, Kafka, Cache, Observability, Resilience, Feature Flag, Secret는 정식 Root에 존재하지만
세분화·Consumer·Failure·Operations·Artifact Closure가 필요하다.

## 그룹 선택

```text
Generator Capability Profile
→ 승인된 Leaf Starter
→ 실제 Build Dependency
→ Domain Manifest resolvedStarters
→ Profile Version + Starter Version Lock
```

안정 조합만 Aggregate Starter로 제공하고 자체 Bean·AutoConfiguration·업무 정책을 금지한다.
`all`, `full`, `everything` Mega Starter는 금지한다.

## 예시

```text
cpf-starter-bundle-event-jms-ibm-mq
  → cpf-starter-messaging-reliability-jdbc
  → cpf-starter-messaging-jms
  → cpf-starter-messaging-ibm-mq
  → cpf-starter-observability
  → cpf-starter-resilience
```

## 완료 Gate

1. Core/Common에서 기존 Runtime 제거
2. Public API/SPI 호환
3. 실제 Consumer 이관
4. Generator Profile·Lock
5. DB Generator-first
6. POM/BOM/SBOM/Artifact
7. 정상·오류·경계·Fault·Unknown Result
8. Operations·Security·Audit
9. Optional-removal
10. latest exact-SHA Evidence


## 최신 Git 검증 상태

`cpf-docs/work/review/20260802_05/CORE_TO_STARTER_MIGRATION_REVIEW.csv`의 30개 후보가
Commit `2e93d92393c52b887482731b683db3c3822027b1`에 반영된 것을 확인했다. QA38 최종 리뷰는 이를 대체하지 않고,
Consumer·DB·Generator·Artifact·Fault·Operations 완료 Gate를 추가한다.

``n

### Snapshot: cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF Codex Continuity State

## Current authoritative snapshot — 2026-08-02 post-push reconciliation

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- confirmed `origin/master`: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- local Working Tree: `재확인 필요` — this review used remote Git state, not the user's local filesystem
- active package: `POST-QA37 integrated remediation / next Codex QA38`
- Source status: `부분 구현`
- exact-SHA final verification: `미검증`
- Oracle/PostgreSQL/MariaDB actual Fresh Lifecycle: `미검증`
- Kafka/JMS/IBM MQ/RabbitMQ/TCP runtime: Kafka focused source tests exist; full provider/runtime matrix `미구현 또는 미검증`
- Browser 3-engine, Toxiproxy, OTel, multi-instance, supply-chain at latest SHA: `미검증`
- `cpf-starters/`: official fixed product root
- permanent DB rule: Canonical/Generator first; each vendor starts from a dedicated CPF QA DB/Schema with CPF Object count 0
- permanent Git rule: no Codex commit/push without explicit user approval

### Supersession notice

The historical QA37 body below records valuable focused PASS and defect history at `1eda8e12...` and a dirty WIP. Multiple user pushes and later documentation/consolidation commits produced `38089a96e3f4c7c2ba05cda549785b47f67cd462`. Therefore those results are inputs for impact analysis, not current exact-SHA completion evidence.

### Next exact work

1. Verify local `HEAD == origin/master`, clean/dirty state and all commits since `1eda8e12...`.
2. Merge the external QA37 defect/execution ledgers into the repository verification history without claiming missing logs as PASS.
3. Finish P0 Core-to-Starter source migration and real Consumer conversion.
4. Implement MQ/JMS/IBM MQ/RabbitMQ/TCP provider requirements.
5. Complete Generator Profile/Aggregate/BOM/Domain lock.
6. Fix official Fresh DB lifecycle orchestration before starting any DB.
7. Run one vendor at a time from object count 0, then Java/Frontend/Runtime/Fault/Browser/Supply-chain.
8. Update Matrix/Evidence and final exact-SHA state.

---

## Historical QA37 state retained for traceability

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 HEAD / `origin/master`: `1eda8e12fe123281748a4388938c62f11819da1e`
- 작업 환경: 집 PC Windows / PowerShell, Java 25.0.3, Gradle 9.1.0, Docker 29.6.2
- Active work package: `QA37`
- 현재 작업 단계: `01_STATIC Source Closure 보완` — ADM/BZA/Gateway/QA30/QA31/QA32/Enterprise 정적 Gate 통과, Source Closure의 Windows·WIP 계약과 Starter 제품 편입 잔여 보완 중
- Source overall status: `부분 구현`
- Repository full verification: `미검증`
- DB/Runtime/3DB/Kafka/Browser/Multi-instance/Supply-chain: `미검증`
- Commit/Push: `미구현` — 현 QA37 요청서에 따라 Codex가 수행하지 않음
- Worktree: `부분 구현` — QA37 보완 변경이 누적된 Dirty WIP이며 임의 reset/revert/stash/clean 금지

## Current Canonical Pointers

1. `cpf-docs/work/current/CPF_CODEX_FINAL_FULL_VALIDATION_AND_REMEDIATION_REQUEST_20260729.md`
2. `cpf-docs/work/current/CPF_CODEX_1ST_FULL_VALIDATION_AND_REPAIR_REQUEST_20260726.md`
3. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
4. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
5. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
6. 외부 실행 원장: `C:\dev\Docker\CPF\output\codex\qa37\execution-ledger.csv`
7. 외부 결함 원장: `C:\dev\Docker\CPF\output\codex\qa37\defect-ledger.csv`

## 완료한 작업

- `완료`: HEAD와 `origin/master`가 기준 SHA와 동일함을 확인하고 Dirty WIP를 보존함.
- `완료`: exact SHA Preflight PASS. Evidence JSON은
  `C:\dev\Docker\CPF\output\codex\qa37\preflight\cpf-codex-preflight-1eda8e12fe12-20260802_011326.json`,
  SHA-256은 `7674bba288f0ee3b9fa79d987771f1ed940ed0ea02baf33f99414e7062`.
- `완료`: QA37 Stage Wrapper가 exact HEAD/Command/Worktree/환경/Log/Artifact hash를 검증하도록 보완하고 focused test 13/13 PASS.
- `완료`: Source Closure/Secret Scanner/Generator BOM/Platform BOM/Composite Build 결함을 보완하고 해당 focused 검증 PASS.
- `완료`: `cpf-core` test compile 및 영향 9개 class 36/36 PASS.
- `완료`: `cpf-batch:execution-runtime` Spring Batch 6 보정, compile PASS, 전체 test 14/14 PASS, QA33 control-plane gate PASS.
- `완료`: REF legacy batch package를 canonical optional batch package로 이동하고 feature-isolation gate PASS.
- `완료`: REF module-local 임시 DDL fixture를 제거하고 Canonical Seed Metadata에 Mapper test data를 등록한 뒤 MariaDB/PostgreSQL/Oracle 공식 Test Seed로 생성함.
- `완료`: REF Center-Cut inline MariaDB SQL을 중앙 `ref` Vendor Runtime Query Pack으로 이관. 13 key × 3 Vendor 정적 parity PASS.
- `완료`: 중앙 Runtime Query Pack에서 누락된 활성 CPF/BZA query 5개를 Canonical contract/template에 복구하고 sync/check PASS.
- `완료`: `cpf-starter-security` Boot 4 BFF Filter/Authentication/Binding/Jackson ownership을 보완하고 전체 26/26 tests PASS.
- `완료`: BZA의 BFF single-flight refresh, stale-session 격리, credential 비저장 계약을 구현하고 focused 4/4 tests PASS.
- `완료`: BZA controller-source OpenAPI 84개 Operation과 26개 Route Operation 계약을 생성·검증하고 transport 재귀 누출을 차단함.
- `완료`: pinned npm 10.9.2 기준 BZA `verify` 전체 PASS. Lock/License/SBOM/OpenAPI/Generator/Consumer/Lint/Typecheck/Vitest 13/13/Production Build/35-file Bundle Manifest가 모두 Exit 0이며 Bundle SHA-256은 `4a4ae80879806b8344a3d5d3e2cd2c5c236379892311383a432a0875721bc72b`.
- `완료`: `cpf-batch:control-server`의 Jackson/Spring 7 API/승인 컨텍스트/배포 canonical hash와 stale test contract를 보완하고 전체 34/34 tests PASS.
- `완료`: `cpf-common`의 Public response API, Redis/MariaDB test ownership, POI close lifecycle을 보완하고 신규 XLSX streaming round-trip을 포함한 전체 29/29 tests PASS.
- `완료`: BAT Runtime Query를 현재 Spring Batch Primary Engine과 Execution Runtime Consumer 기준으로 재정렬. 삭제된 Custom Worker Query 18개와 미사용 Scheduler Insert 1개를 제거하고 Execution Runtime 19개를 중앙 Catalog로 이관하여 최종 210 Statement × 3 Vendor, Generated SQL 630개, Java inline SQL 0건을 확인함.
- `완료`: Canonical Schema 46에 누락됐던 `cpf_batch_approved_launch`, `cpf_batch_execution_control`, `cpf_batch_execution_link`를 추가하고 기존 Epoch와 함께 BAT 제어 4 Table을 정본화함. 3 Vendor BAT Source는 각 56 Table이며 Append-only V95로 Historical Upgrade를 보정함.
- `완료`: Generated Domain DB 계약을 임의 8 Table이 아닌 공통 Sample 1 + Idempotency Ledger 1의 정확히 2 Table로 복구하고 존재하지 않던 Operation Template 참조를 제거함. Golden Path와 `cpf-member` Generator-owned Database parity PASS.
- `완료`: 공식 Rollback Root를 `cpf-tools/db/vendor/<vendor>/rollback`으로 단일화하고 PostgreSQL/Oracle 및 REF WIP의 중복 `migration/rollback` 56개 Resource를 내용 보존 이동한 뒤 빈 중복 Root를 삭제함.
- `완료`: Historical V69/V74의 Empty-string NOT NULL과 Canonical nullable 의미 Drift를 Metadata-first V96 Generator로 보정. 30 Column × 3 Vendor Migration/Rollback과 Checksum을 생성했으며 Oracle R96은 표현 불가능한 과거 상태로 데이터 변조하지 않고 fail-closed함.
- `완료`: QA30가 `.git`/ignored Build/IDE Cache를 Source로 오인하던 결함, 낡은 BAT/Gateway Anchor, Test Sentinel Secret 오탐을 보정하고 현재 WIP 전체 정적 Gate `errorCount=0` PASS.
- `완료`: 폐기된 Core 내부 Gateway Route/Catalog/Authorization 중복 모델과 Test를 제거하고 공개 `com.cpf.core.api.gateway` 계약만 유지함. Release Signer도 정규 Java package path로 이동하고 직접 `javac --release 25` PASS.
- `완료`: Gateway를 SCG MVC `CpfScgPrimaryHandler`/`CpfScgPrimaryRouteConfiguration` 단일 Primary로 정리하고 legacy Controller/ProxyService/Transport를 제거함. Recovery Spool의 OffsetDateTime 직렬화 offset 보존 결함을 고쳐 Gateway 전체 35/35 tests PASS.
- `완료`: QA30/QA31/QA32 Gate를 현재 SCG Primary와 Windows UTF-8/LF canonical hash/current-worktree scope에 맞게 보완. QA30 PASS, QA31 96 checks/0 failures, QA32 32,341 checks/0 failures.
- `완료`: Generator 실행 결과를 Generated Domain 제품 Root가 아닌 `build/reports/create-domain/<module>/create-domain-result.json`으로 중앙화하고 `cpf-member`를 official sync 35건으로 재생성한 뒤 parity/Golden Path/Hygiene PASS.
- `완료`: ADM/BZA의 raw operational JSON `<pre>` 표시를 bounded depth/item 및 sensitive masking을 제공하는 `CpfStructuredData`로 표준화하고 양쪽 component test/typecheck/lint와 Integrated Architecture/UI/Hygiene Gate를 통과함.
- `완료`: Enterprise Closing Static Gate PASS — Ownership, Consumer, Frontend Route, Hygiene, Public Boundary, 3 Vendor migration checksum, MariaDB source/runtime migration·rollback parity 포함.
- `완료`: `cpf-starters`를 Final Artifact Catalog 기반 7개 정식 제품 Root/Artifact로 fixed-root, Local/Staging/Internal publication, Platform BOM, artifact propagation, ownership/taxonomy/public-boundary/inventory에 완전 편입. Kafka Starter test와 관련 Gate PASS.
- `완료`: R10 Product Gate의 stale exact prose와 ignored output recursive scan을 semantic policy + Git source inventory로 보완하여 전체 Gate를 9.6초에 PASS.

## 진행 중인 작업

- `완료`: REF QA32-061 typed contributor/metadata scan/bounded process output focused 8/8 PASS 후 전체 1,016 tests 중 실제 실행 1,014건 PASS. DB 환경 조건부 2건은 `미검증` 유지.
- `완료`: `cpf-batch:worker` Spring Batch 단일 Primary Engine 전환과 STDIN JSON stream 보완 후 전체 38/38 tests PASS.
- `완료`: `cpf-batch:runtime-common` direct dependency/Boot 4 `RestClientCustomizer` 보완 후 compile 및 10/10 tests PASS.
- `완료`: BAT 210개 canonical runtime statement를 3 Vendor 630개 Resource로 sync/check하고 inline SQL 0건, Query Contract integrity, Execution Runtime 19/19, Control-server 34/34, Scheduler 전체 test, QA30/QA33를 통과함.
- `완료`: ADM에도 BZA 공통 Orval mutator, license/SBOM, Vitest 경계, bundle manifest, route traversal 보정을 반영하고 전체 frontend verify 및 추가 structured component test/typecheck/lint를 통과함.
- `완료`: `cpf-core`의 QA32 unbounded test stream 3건을 bounded read로 보완하고 focused 10/10 tests PASS.
- `완료`: Center-cut legacy independent runtime/dispatcher를 제거하고 Spring Batch StepHandler/execution-bound claim/fencing/lease/runtime-state 경계로 이관. Center-cut 7/7와 Execution Runtime 15/15 tests PASS.
- `완료`: Spring Batch 6 제거 예정 `JobExplorer`를 `JobRepository` read 계약으로 교체하고 repository-backed reconcile 테스트를 추가함.
- `부분 구현`: QA37 결함 원장 `QA37-CODEX-001` 이후 Source Defect를 root cause 단위로 기록 중.
- `부분 구현`: Source Closure가 의도적 tracked deletion/merged overlay remediation을 오판하던 계약과 Windows `javac` command-line 한계를 보완했으며 focused unittest 25/25 및 EDU135 독립 compile/runtime PASS. `cpf-starters` publication/BOM/Architecture Gate 편입을 보완 중.

## 아직 시작하지 않은 작업

- `미검증`: 보완 완료 후 exact command hash로 `01_STATIC` 상위 Lifecycle 1회 재실행.
- `미검증`: Java 25 전체 clean build/test 및 Optional Profile.
- `완료`: ADM/BZA Frontend 현재 Worktree 기준 전체 verify 및 추가 structured component 검증.
- `미검증`: DB Static 전체 Gate와 MariaDB → PostgreSQL → Oracle 순차 Lifecycle.
- `미검증`: 기존 MariaDB Drift/Migration/Upgrade와 분리된 신규 DB Clean Install.
- `미검증`: Runtime/Kafka/Multi-instance/Fault Injection/OTel/Browser.
- `미검증`: Supply-chain, Repository Hygiene, Truth/Evidence/Handover 최종 동기화.

## 변경 중인 주요 파일/모듈

- `cpf-tools/scripts/verify-cpf-qa37-source-closure.py`, `invoke-cpf-codex-stage.ps1` 및 focused tests
- `settings.gradle`, root `build.gradle`, `cpf-tools/build/platform-bom/build.gradle`
- `cpf-core` logging/public service-call API 및 영향 tests
- `cpf-reference` build, Batch 6 sample, attachment/file consumer, Center-Cut repository/tests
- `cpf-tools/db/canonical/seed-model.json`
- `cpf-tools/db/metadata/platform-runtime-query-contract.json`
- `cpf-tools/db/runtime-template/{cpf,bza,ref}` 및 3 Vendor generated runtime/source/lifecycle SQL
- `cpf-tools/db/metadata/platform-nullable-empty-string-repair.json`, `sync-platform-nullable-empty-string-repair.ps1`, 3 Vendor V96/R96
- `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/rollback` — 공식 단일 Rollback Root
- `cpf-batch:execution-runtime`, `cpf-batch:runtime-common`, `cpf-batch:worker`
- `cpf-batch:control-server`, `cpf-batch:scheduler`, `cpf-common` 및 영향 tests
- `cpf-tools/scripts/test-cpf-qa32-negative-fixtures.py` — transient cache 제외 및 clean baseline 선검증
- `cpf-starters/security` BFF filter chain/Boot 4 binding/tests
- `cpf-biz-admin/frontend` OpenAPI/route generator/BFF session/test/build pipeline
- `cpf-admin/frontend` BZA와 공통 generator/mutator/license/test/build 보정 — 전체 verify 및 structured component 검증 완료
- `cpf-admin/frontend`, `cpf-biz-admin/frontend`의 `CpfStructuredData.vue`/tests와 raw operational result 화면
- `cpf-gateway` SCG MVC 단일 Primary 및 recovery spool
- `cpf-tools/scripts/verify-cpf-qa37-source-closure.py`, `verify-cpf-qa37-manual-edu-135.py`, `verify-cpf-reference-feature-removal.py`와 회귀 test
- `cpf-starters`, root publication aggregate, Platform BOM, Architecture/Inventory Gate — 정식 제품 Root 전체 편입 보완 중

## 실제 실행한 검증

- `완료`: Preflight exact SHA PASS.
- `실패`: 최초 `01_STATIC` 실행은 legacy REF batch package에서 중단. Log:
  `C:\dev\Docker\CPF\output\codex\qa37\logs\01_STATIC-20260802_021516_458-37020.log`,
  SHA-256 `8394185c5e79ae09c6c9b9962da9f4531b2372d968ba194c608ec4998f47585c`.
- `완료`: Source Closure unit 24/24 및 QA34 포함 27 tests PASS; repository safe secret probe `files=6428`, finding file 0.
- `완료`: Core targeted 36/36 PASS.
- `완료`: Generator QA34 BOM contract 3/3, golden path, composite identity, Platform BOM publication contract PASS.
- `완료`: `:cpf-batch:execution-runtime:test` 14/14 PASS.
- `완료`: `:cpf-batch:runtime-common:test` 10/10 PASS.
- `완료`: REF feature-isolation PASS.
- `완료`: `:cpf-reference:clean :cpf-reference:compileJava` 및 focused 047~049 tests 4/4 PASS; removal 예정 API 경고 제거.
- `실패`: REF 전체 test 1,013건 중 2건 실패, 2건 DB 환경 조건부 skip. 실패 2건은 054/055로 등록해 구현 보완 중이며 skip은 실제 DB 검증 완료로 승계하지 않음.
- `완료`: REF file-transfer checksum focused test 2/2 PASS. 공개 `CpfFileRequest` checksum 계약에 맞춰 fixture 보정.
- `완료`: Platform Runtime Query sync/check 218 statements / 654 generated repository files PASS; REF 13 × 3 Vendor parity PASS.
- `실패`: `check-query-contract-integrity`는 REF가 아니라 BAT orphan/inline SQL 계약 결함에서 중단했으며 046/050~053으로 보완 중.
- `완료`: QA32 negative fixture Python AST 및 `git diff --check` PASS. 실제 negative lifecycle은 QA32 Primary Engine 기준선 결함을 먼저 닫은 뒤 1회 실행.
- `완료`: `:cpf-starter-security:test` 전체 26/26 PASS.
- `완료`: BZA focused BFF recovery tests 4/4 PASS.
- `완료`: BZA `corepack npm run verify` 전체 PASS — OpenAPI 84, Route 26, Consumer 78, eslint 0 findings, typecheck PASS, Vitest 13/13, production bundle 35 files.
- `완료`: Core bounded attachment/ZIP manifest/GZIP verification focused tests 10/10 PASS.
- `완료`: REF 최신 전체 lifecycle PASS — 720 suites, 총 1,016 tests, 실행 1,014, 실패 0, 오류 0, skip 2. Skip은 `ReferenceCenterCutAdapterTest`, `ReferenceQueryEducationMapperSliceTest`의 DB 조건부 항목.
- `완료`: `:cpf-batch:execution-runtime:test :cpf-batch:center-cut-runner:test` 결합 lifecycle PASS — 각각 15/15, 7/7, 실패/오류/skip 0.
- `완료`: BAT Runtime Query sync/check PASS — 210 statements, MariaDB/PostgreSQL/Oracle 3 Vendor, 630 generated files, 4개 BAT owner scope inline SQL 0건. Query Contract integrity와 checker unittest 2/2도 PASS.
- `완료`: `:cpf-batch:control-server:test` 전체 34/34 PASS — SQL Catalog consumer, 승인 컨텍스트 전달, canonical deployment hash, lock-store UNKNOWN_RESULT 포함.
- `완료`: `:cpf-common:test` 전체 29/29 PASS — XLSX streaming round-trip 신규 regression 포함.
- `완료`: `:cpf-batch:scheduler:test` 전체 Gradle Task PASS 및 `:cpf-core:compileJava` PASS (Java 25, `--no-daemon --max-workers=2`).
- `완료`: QA33 Batch Control Plane Gate PASS. Evidence Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa33-batch-control-plane-rollback-root-20260802_115227.log`, SHA-256 `947b5d224cb74057fc186f2d226c5afbc59ad29728cdc5f6cc2974607127a052`.
- `완료`: 공식 DB Artifact 전체 Pipeline PASS — Canonical 192 Table, Platform Runtime 218 × 3 Vendor, BAT Runtime 210 × 3 Vendor, V95/V96, Checksum, Schema Drift, Profile, Generated Domain parity. Evidence Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\database-artifact-sync-v96-20260802_120232.log`, SHA-256 `d19b64bf07a8970c09de41a8e124c247237fd5b64c9e369e0dc25226de9e799b`.
- `완료`: QA30 현재 WIP 전체 Static Gate PASS (`errorCount=0`, Canonical 192/Schema 46, 3 Vendor Source 각 192). Evidence JSON/Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa30-static-remediated-20260802_120440.json`, SHA-256 `3dd47aa4e2c7816510b45a4590554e052b85efcf97f5331a56ae3a87e4e98e84`.
- `완료`: Gateway 전체 35/35 PASS. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\gateway-full-test-pass-candidate-20260802_133017.log`, SHA-256 `88583035ff74dc081145a55412655c4239bbbb73b52a6be92788f318ac33af28`.
- `완료`: QA32 전체 32,341 checks/0 failures. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa32-primary-engines-pass-candidate-20260802_133131.log`, SHA-256 `4ecd17f99002c5d03e00d86c84badbe61e272861d0f7ddfb5095e05101897d1f`.
- `완료`: 최신 QA30 Gate PASS. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa30-completion-pass-candidate-20260802_133410.log`, SHA-256 `3dd47aa4e2c7816510b45a4590554e052b85efcf97f5331a56ae3a87e4e98e84`.
- `완료`: QA31 96 checks/0 failures. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa31-development-pass-candidate-20260802_133737.log`, SHA-256 `d0a27338c3080761853e90d078c8e92a2f0cc5221373b77d8921f504416b8d1b`; JSON SHA-256 `3f22af278e361ac1b98d8cf8bad3c37199e70aa2e43e28467186abedc2b3d560`.
- `완료`: ADM/BZA `CpfStructuredData` focused tests, typecheck, lint PASS; raw `<pre>` scan 0건. Integrated Architecture/UI/Hygiene와 Enterprise Closing Static Gate Exit 0.
- `완료`: Source Closure 회귀 unittest 25/25 PASS, REF optional contributor removal Gate PASS, EDU135 independent compile/runtime PASS(135 normal/validation/authorization/duplicate/recovery, 24 exhaustive failure, 6 persistence/concurrency).
- `재확인 필요`: BZA build는 Exit 0이나 initial JS chunk 1,070.34 kB(gzip 344.64 kB)에 대한 Vite 500 kB 성능 경고가 있음. 기능 실패나 IDE compile error로 기록하지 않으며 Frontend 성능 단계에서 실제 budget 기준으로 판정한다.

## DB / Runtime 현재 상태

- `재확인 필요`: 현재 로컬 MariaDB의 기존 CPF Schema/데이터 상태는 QA37 DB Stage 시작 직전에 read-only Drift로 확인해야 함.
- `완료`: 이번 Checkpoint까지 기존 로컬 DB에 DDL/DML/Reset/Drop을 실행하지 않음.
- `완료`: Clean Install은 기존 DB와 분리된 신규 Database/Schema로만 검증한다는 원칙 유지.
- `완료`: 3 Vendor Canonical Source/Install/Migration/Rollback/Runtime Query/Checksum/Drift 정적 Pipeline은 통과함.
- `미검증`: MariaDB/PostgreSQL/Oracle 실제 Lifecycle 및 Runtime.
- `재확인 필요`: Docker Engine은 가용하나 현재 실행 중 CPF Container는 0개이고 `cpf-mariadb`는 `Exited(0)`. 로컬 3306/Windows MariaDB Service는 탐지되지 않아 실제 DB Stage는 공식 Docker `mariadb` 최소 Target을 사용해야 함.

## Blocker와 미검증

- `완료`: REF/Center-cut/Core/BAT/ADM/Gateway 및 QA30/QA31/QA32 영향 검증 PASS.
- `완료`: `cpf-starters` 정식 Root의 publication/BOM/Architecture/Inventory 편입과 focused 상위 Gate PASS.
- `부분 구현`: 공식 DB Tool이 `refDB/V93,V94`와 `rollback/refDB/U93,U94`, optional expected schema, REF runtime query, live different-hash conflict를 연결하지 못하는 `QA37-CODEX-124`를 보완 중. 수동 SQL로 우회하거나 DB를 먼저 기동하지 않음.
- `부분 구현`: QA32가 quoted/comma PowerShell argument의 `-ExecutionPolicy`, `Bypass`를 놓쳐 active canonical consumer 8개 파일 70곳이 false-green이 된 `QA37-CODEX-125`를 보완 대기. `AllSigned`와 과거 실행 Evidence는 변경하지 않음.
- `완료`: 대규모 Canonical projection과 V95/V96를 포함한 3 Vendor Source/Lifecycle diff는 전체 Artifact Sync/Drift/Parity Gate PASS.
- `미검증`: Docker DB/WAS/Kafka/Browser 외부 Lifecycle은 아직 시작하지 않음.

## 다음 정확한 작업 순서

1. 공식 optional DB Lifecycle의 migration/rollback discovery, expected schema overlay, REF runtime query와 different-hash conflict Consumer를 Canonical-first로 보완하고 3 Vendor 정적/dry-run test를 통과시킨다.
2. Active Gradle/Generator/DB/Final Verification Consumer에서 ExecutionPolicy Bypass 인자만 제거하고 QA32/QA33 탐지식과 negative fixture를 보강한다.
3. Repository Source Closure를 재실행하여 다음 최초 Root Cause가 있으면 영향 범위만 보완한다.
4. 모든 관련 Source Defect가 targeted PASS가 된 뒤 아래 exact command hash로 `01_STATIC -AllowRerun`을 한 번만 실행한다.
   - Hash: `bd9eac218f4b7e105a5801abdf266536615d98d1589d3afb83d9bae531954482`
   - Command: `git diff --check` → 실패 시 종료 → `python .\cpf-tools\scripts\verify-cpf-qa37-source-closure.py --root .` → exit code 전달
5. `01_STATIC` PASS 후 기존 MariaDB Read-only Drift와 분리 Clean Install/V93/V94/Runtime/Rollback/Reapply를 공식 Docker `mariadb` 최소 Target으로 실행한다.
6. MariaDB PASS 후 canonical plan의 첫 미완료 Stage부터 계속한다. 유효한 exact PASS만 생략한다.

## 다시 수행하면 안 되는 작업 / 확정 사항

- `cpf-starters/`는 기준 SHA와 Product Surface Policy/Architecture/Release Artifact Catalog가 일치하는 정식 선택 Runtime 제품 Root이다. `cpf-tools/`로 이동하거나 미사용처럼 삭제하지 않는다. 7개 하위 모듈 모두 Boot AutoConfiguration 제품이며 Source 빈 폴더는 없음.
- Generator 결과 JSON은 제품/Generated Domain Root에 보관하지 않고 중앙 ignored `build/reports/create-domain/<module>`에서만 관리한다.
- ADM/BZA 운영 객체는 raw JSON `<pre>`로 노출하지 않고 bounded/masked structured renderer를 사용한다.
- Gateway Primary는 SCG MVC Handler/Route Configuration 하나이며 제거한 legacy Controller/ProxyService/Transport를 복원하지 않는다.
- exact SHA Preflight는 유효 PASS이므로 관련 입력이 바뀌지 않는 한 재실행하지 않는다.
- 이미 PASS한 Core focused 36 tests와 execution-runtime 14 tests를 무의미하게 반복하지 않는다.
- 삭제가 확정된 `JobPackDispatcher`/`JdbcWorkerExecutionRepository`를 복원하지 않는다. Spring Batch가 유일한 Primary Execution Engine이다.
- module-local REF DDL fixture와 MariaDB 전용 test fixture pack을 복원하지 않는다.
- Vendor SQL부터 수동 수정하지 않는다. Canonical Schema/Metadata/Template에서 생성한다.
- Historical Migration 본문/checksum을 수정하지 않는다.
- Generated Domain의 정본 DB 구조는 업무 이름과 무관하게 Sample Table 1 + Idempotency Ledger 1의 정확히 2 Table이다. 존재하지 않는 Operation Template이나 고정 Domain 목록을 다시 추가하지 않는다.
- 공식 Rollback Consumer Root는 `cpf-tools/db/vendor/<vendor>/rollback`이다. 삭제한 중복 `migration/rollback` Root를 복원하지 않는다.
- V69/V74 Historical SQL은 변경하지 않는다. Canonical nullable 의미는 Metadata-first Generated V96으로만 보정하며 Oracle R96의 fail-closed 복구 정책을 임의 Sentinel 변환으로 바꾸지 않는다.
- MBR/ACC/REF/PAY 같은 Domain/SystemCode 고정 목록을 Generator/DB Tool에 추가하지 않는다.
- 과거 SHA/다른 Command/다른 환경 PASS를 현재 QA37 성공으로 승계하지 않는다.
- 사용자 데이터가 있는 기존 DB를 reset/drop하지 않는다.
- Codex는 현 QA37 요청서에 따라 commit/push/branch/tag/PR을 수행하지 않는다.

``n

### Snapshot: cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md
- SHA-256: $sha
- Preserved before QA38 currentization

``text
# CPF Codex Decision Log

이 문서는 새 PC나 새 세션에서도 반드시 유지해야 하는 Architecture, Ownership와 Migration 결정만 기록한다. 단순 진행 상황은 `CPF_CODEX_CONTINUITY_STATE.md`에서 관리한다.

## DEC-001 공식 식별 체계

- 상태: `완료`
- 결정: 정식 명칭은 **Core Platform Framework**다. 공식 Module은 `cpf-*`, Java root package는 `com.cpf.<domain>`, 내부 SystemCode는 3자리 대문자를 사용한다. Core SystemCode는 `CPF`다.
- 이유: 사람이 읽는 DomainName과 내부 식별자를 분리하고 Source, API, SQL, Config와 운영 식별자의 충돌을 막는다.

## DEC-002 Module 의존성과 데이터 소유권

- 상태: `완료`
- 결정: Business Domain → `cpf-common` → `cpf-core` 방향을 유지한다. `cpf-core`의 업무/Common/Admin/Batch 역참조, 업무 Module 간 DB 직접 접근, Admin의 Owner DB 직접 갱신과 순환 의존을 금지한다.
- 이유: Public Contract, 실제 Owner와 장애·복구 경계를 명확히 하고 배포 topology가 계약을 바꾸지 않게 한다.

## DEC-003 `cmnDB` 최소화

- 상태: `완료`
- 결정: `cmnDB` Schema는 생성하지만 기본 제품 Table은 DB 연결, Migration, CRUD, 검색, Offset/Slice/Cursor, Validation, Duplicate, Optimistic Lock와 Transaction을 검증하는 sample table 1개만 둔다. `cpf-common`은 DB-less 기본 사용이 가능해야 한다.
- 이유: Common은 고객 업무 공통 Extension이며 기술 Engine이나 추정성 업무 데이터의 저장소가 아니다.

## DEC-004 업무 채번

- 상태: `완료`
- 결정: `cmn_sequence*`와 업무 채번 Runtime은 Core/Common 기본 제품에서 제거한다. BZA에는 운영 기본 비활성인 선택형 Customization Sample만 둘 수 있으며 온라인 업무가 BZA에 의존하면 안 된다.
- 이유: 업무 번호 정책은 고객 또는 업무 Domain 소유이고 Framework 기술 ID와 성격이 다르다.

## DEC-005 Fixed-Length와 External

- 상태: `완료`
- 현재 적용: External 고정 Module 소유 부분은 DEC-024로 대체됨
- 결정: 범용 고정길이 Layout/Field/Group/Parser/Writer/Validation/Masking/Encoding API·SPI는 `cpf-core`가 소유한다. 기관별 Layout, Mapping, Endpoint, Authentication, Adapter, Retry, Unknown Result와 Reconciliation은 고객/업무 Generated Domain이 소유하며, 대외 업무 Domain이 필요하면 `external/EXS`도 동일 Golden Generator로 생성한다.
- 이유: 재사용 가능한 기술 Contract와 기관별 업무·운영 정책을 분리한다.

## DEC-006 Batch Physical Ownership

- 상태: `완료`
- 결정: Batch, Scheduler, Agent, Runner, Worker와 Center-Cut Runtime state는 `cpf-batch`가 소유하며 권장 물리 Schema/Prefix는 `batDB`/`bat_*`다.
- 이유: Core Schema 혼재를 제거하고 실행, 복구, 보존과 운영 책임을 실제 Module Owner에 맞춘다.

## DEC-007 Empty Install 책임 분리

- 상태: `완료`
- 결정: 최초 설치는 Reset 없이 빈 MariaDB에서 성공해야 한다. Provision, non-destructive install, product seed, optional sample/EDU/test seed, verify와 allowlisted reset을 분리한다. Secret은 외부 입력으로만 받는다.
- 이유: 재현 가능한 신규 설치, 최소 권한, 비파괴성, 운영 seed와 시험 데이터의 분리를 보장한다.

## DEC-008 Historical Migration 보호

- 상태: `완료`
- 결정: 기존 Flyway 파일은 적용 이력과 checksum 감사를 마칠 때까지 불변으로 취급한다. Pre-release 단일 re-baseline은 고객/운영 적용 이력 없음, 모든 개발 DB 폐기, checksum 처리·upgrade/rollback 대안과 Empty Install Evidence를 갖춘 뒤 사용자 명시 승인이 있을 때만 가능하다.
- 이유: 현재 DB가 비어 있다는 사실은 과거 Migration 변경 권한을 의미하지 않으며, checksum과 업그레이드 경로 손상을 막아야 한다.

## DEC-009 ADM/BZA 경계와 Frontend 배포

- 상태: `완료`
- 결정: ADM은 Platform Control Plane, BZA는 Customer Business Admin이다. 위험 조치는 Owner Command API, 권한, 승인, 사유와 Audit를 사용한다. ADM/BZA Frontend는 서로 및 Java WAS와 독립 Build/Deploy/Rollback을 지원한다.
- 이유: 운영 상태 직접 수정과 권한 우회를 막고 독립 배포·복구 경계를 보장한다.

## DEC-010 문서와 Evidence 수명주기

- 상태: `완료`
- 결정: 구현 중 정본은 Markdown과 실제 Source/SQL/API/Test다. Generated Matrix와 DOCX/PDF는 Source와 Evidence가 안정된 뒤 재생성한다. 삭제된 Stale Evidence, 조기 문서와 중복 산출물을 복구하지 않는다.
- 이유: 과거 환경·Commit의 산출물이 현재 완료 근거로 오인되는 것을 방지한다.

## DEC-011 DB Bootstrap과 Runtime 권한 경계

- 상태: `완료`
- 결정: Local/DEV/Codex는 DB preflight 후 명시적인 Provision → Empty Install → Product Seed → Verify를 수행하고 Runtime을 기동한다. 운영 Application은 관리자 권한으로 Schema/User를 암묵 생성하지 않으며 사전 설치된 DB에 최소 Runtime 권한으로 연결한다.
- 이유: DB가 없는 새 PC에서도 검증을 계속하되, 설치 자동화와 운영 Runtime의 권한·책임을 섞지 않기 위해서다.

## DEC-012 Multi-Vendor DB 격리

- 상태: `완료`
- 현재 적용: 공식 Vendor 목록은 DEC-034로 대체됨
- 결정: 공식 지원 구조는 MariaDB, MySQL, PostgreSQL, Oracle, SQL Server를 대상으로 한다. Platform Module과 모든 Generated Domain(`external/EXS` 포함)에서 Vendor 선택을 `cpf.db.vendor`와 Driver/Datasource/Migration/SQL resource로 격리하며 Controller, Service, Domain, API와 일반 Repository 호출 계약에는 Vendor 분기를 두지 않는다. Vendor별 물리 SQL은 달라도 논리 Schema, 상태, Seed, API와 Repository 의미는 동일해야 한다.
- 이유: 고객 DB 전환이 Java 업무 Source 수정이나 Module fork를 요구하지 않게 하고 동일 Binary/Source의 배포 가능성을 보장하기 위해서다.

## DEC-013 Minimal Transaction Reference Schema

- 상태: `완료`
- 현재 적용: MBR/ACC/REF를 동일 고정 Reference로 보는 부분은 DEC-024로 대체됨
- 결정: 모든 Generated Domain은 Domain별 임의 원장 대신 동일한 Minimal Transaction Golden Schema Template을 사용한다. Schema/SystemCode/Table prefix만 Metadata로 달라지고 CRUD, 검색, 정렬, Offset/Slice/Cursor, Validation, Duplicate, Optimistic Lock, Transaction, 호출·Header·Idempotency·Audit 경로를 같은 논리 계약으로 검증한다. `external/EXS` 역시 예외 Template을 두지 않으며 기관별 특화 Adapter/업무 데이터가 필요하면 Generated Domain의 확장 경계에서 추가한다.
- 이유: Framework 거래 처리 검증을 업무 예시 차이에서 분리하고 Vendor 및 Generator lifecycle parity를 자동 검증하기 위해서다.

## DEC-014 Vendor SQL Resource Pack 선택

- 상태: `완료`
- 결정: DB 차이는 Vendor별 Provision/Install/Product Seed/Migration/Verify/Rollback과 Runtime MyBatis/Repository Query SQL resource pack으로 분리한다. DB 초기화 Shell은 `cpf.db.vendor`와 같은 단일 Vendor 선택을 받아 해당 pack으로 DB 생성부터 초기 데이터·검증까지 실행하고 Runtime도 같은 Vendor query resource를 선택한다. 이 원칙은 모든 공식 Module과 Generator 신규 Domain에 동일하게 적용한다. 업무 Java Source는 Vendor 선택으로 변경하지 않으며, Source 파일을 덮어쓰는 대신 패키징된 resource location 또는 생성된 격리 실행 directory를 선택한다.
- 이유: DB 교체 시 Java 업무 Source 수정·fork를 막고 설치 SQL과 실행 Query가 서로 다른 Vendor를 가리키는 구성 오류를 fail-closed로 차단하기 위해서다.

## DEC-015 Vendor SQL Pack의 중앙 물리 소유권

- 상태: `완료`
- 결정: Vendor별 SQL 정본은 개별 제품 Module의 `src/main/resources`가 아니라 `cpf-tools/db/vendor/<vendor>` 중앙 Pack이 소유한다. Pack 내부에서 `provision/install/seed/migration/runtime/<module>/verify/rollback`으로 기능과 Module Ownership을 구분한다. 초기화 Tool은 한 Vendor Pack 전체를 선택하고, Runtime에는 선택 Vendor의 외부 resource root 또는 격리된 generated-resources/classpath overlay만 연결한다. 선택 과정에서 Git Source Tree를 덮어쓰거나 Diff를 만들지 않으며 Java Service/Controller/Domain/Repository 업무 Source와 제품 Module artifact에는 공식 Vendor SQL을 반복 적재하지 않는다. Generator도 신규 Domain Module에 Vendor 디렉터리를 복제하지 않고 중앙 Template/Pack에 Domain resource를 등록한다. 과도기에는 Consumer 확인 후 제거하도록 했으나 이 이행 규칙은 DEC-019에서 대체됐다. 현재는 중앙 Pack이 정본이며 Module-local fallback을 제거해 fail-fast로 숨은 Consumer를 노출한다.
- 이유: Vendor SQL의 중복·drift와 모든 Vendor resource의 불필요한 Runtime 활성화를 막고, 동일 Java Source/Artifact에 설치 설정과 선택 Vendor Pack만 결합하는 배포 경계를 만들기 위해서다.

## DEC-016 생성형 Domain의 Metadata·Template 확장

- 상태: `완료`
- 결정: MBR/ACC/REF/PAY/INS 등은 Generator 지원 대상의 고정 목록이 아니라 현재 또는 설명용 예시다. Generated Domain은 `DomainName`, `SystemCode`, `ModuleName`, `PackageName`, `SchemaName`, `TablePrefix` Metadata를 공통 Minimal Transaction Domain Template에 적용한다. 신규 Domain/SystemCode 추가는 Metadata 등록과 Generator 실행만으로 이루어지며 중앙 Tool의 switch/if 또는 Java Source 수정을 요구하지 않는다. Vendor별 생성형 DDL/Seed/Runtime Query/Verify는 `cpf-tools/db/vendor/<vendor>/domain-template` 중앙 Template이 생성하고, 결과를 Domain Module 내부에 5벌 복제하지 않는다. 기본 Sample은 특정 회원·계좌·보험 업무가 아니라 CRUD, Search, Paging, Validation, Commit/Rollback, Optimistic Lock, Duplicate, Local/Remote Call, Standard Header, transactionId, Error Mapping, Idempotency와 Audit/Masking을 검증하는 동일 논리 모델이다. CPF 고정 Platform Module resource와 무제한 확장되는 Generated Domain Metadata/Template의 Ownership을 분리한다.
- 이유: 현재 Repository의 예시 Domain에 Generator가 종속되는 것을 막고, 임의 고객 Domain을 동일 Java 구조와 Vendor Template 계약으로 재현 가능하게 생성하기 위해서다.

## DEC-017 현행 설치 DB 객체의 최소화 판정

- 상태: `완료`
- 현재 적용: Legacy fixed 업무 Domain retirement는 DEC-024의 Golden Generated Domain 정책으로 대체됨
- 결정: 현재 Empty Install의 Table, Sequence, Constraint, Index와 Product Seed는 과거 Dump나 Historical Migration에 존재한다는 이유만으로 유지하지 않는다. 각 객체는 최신 정본의 Owner 책임과 실제 Java/MyBatis/Repository/Installer/Framework 동적 Consumer 중 하나로 존재 이유가 확인되어야 한다. 소비자가 없고 활성 원장과 중복되는 객체는 현행 설치 경로에서 제거하며, 정본 요구가 있으나 Consumer가 미완성인 객체는 삭제 대신 `부분 구현`으로 관리한다. Generated Domain 기본 구조는 단일 `*_sample_item` Golden Template을 따르고, 기관/고객 특화 원장은 기본 Platform install에 넣지 않는다. MariaDB Spring Batch 객체는 사용 중인 Spring Batch Version의 공식 MariaDB Schema 계약을 따른다.
- 이유: 추정성 Schema와 중복 원장, 사용되지 않는 Seed·Index를 제품 Baseline에 고착시키지 않으면서도 보안·운영 정본 객체를 단순 문자열 검색만으로 잘못 삭제하지 않기 위해서다.


## DEC-018 Requirement ID 영구 연속성

- 상태: 완료
- 결정: 한 번 등록된 Requirement ID는 Mapping 없이 삭제하거나 Rename하지 않는다. 통합/분해/Owner 변경은 Continuity Ledger에 Old→New 관계를 남기며 완료율은 Canonical ID만 집계한다. 133→126 감소 과정의 유실을 보정하여 현재 Canonical Count를 162개로 관리한다.
- 이유: PC/세션/Codex 교체 때 과거 요구가 조용히 사라지거나 같은 Gap이 새 이름으로 재개발되는 것을 막는다.

## DEC-019 Central Vendor Pack Fail-Fast

- 상태: 완료
- 결정: 제품 Runtime의 Vendor SQL/MyBatis 정본은 `cpf-tools/db/vendor/<vendor>` 중앙 Pack 하나다. Production resolver/catalog는 `cpf.db.resource-root`가 없거나 Pack이 불완전하면 fail-fast 한다. Module-local vendor SQL을 호환 fallback으로 복구하지 않는다.
- 이유: 과도기 fallback이 중앙 Pack 연결 오류를 숨기고 같은 SQL을 Module×Vendor로 복제하게 만드는 문제를 제거한다.

## DEC-020 ADM과 BZA Approval 분리

- 상태: 완료
- 결정: ADM Approval은 플랫폼 위험조치 Dual Control/SoD/Break-glass와 Owner Command 실행을 소유하고, BZA Approval은 고객 업무 조직/직원/부서합의 결재를 소유한다. 두 Engine/Table/Policy는 공유하지 않는다.
- 이유: 보안 Control Plane과 고객 업무 결재의 책임, 데이터, 감사, 확장 모델이 다르다.

## DEC-021 BZA 조직/직원과 결재 Snapshot

- 상태: 완료
- 결정: BZA는 조직 Hierarchy, 직원, 직급, 직책, 유효기간 기반 Assignment와 복수 Role을 지원한다. Approval Policy와 Instance를 분리하고 Instance 생성 시 조직/직급/직책/참여자 Snapshot을 고정한다. ALL/ANY/N_OF_M, 필수/선택 부서, 위임/대결/부재를 실제 Engine으로 구현한다.
- 이유: 조직개편 이후에도 과거 결재를 재현하고 기업 결재 요구를 사람별 고정 Line 구조에 묶지 않기 위해서다.

## DEC-022 ADM Operator Directory 경계

- 상태: 완료
- 결정: `adm_operator`는 Authentication Identity다. 조직/사번/직급/직책/외부 Directory Subject는 별도 Profile/Directory Port로 관리하고 DB default adapter와 LDAP/AD/IAM/HR 확장을 허용한다. ADM이 기업 HR 원장을 소유하지 않는다.
- 이유: 승인/Audit에 필요한 조직 문맥은 확보하되 플랫폼 관리자가 고객 HR Master와 결합되지 않게 한다.

## DEC-023 transactionId 단일 전역 거래 식별자

- 상태: `완료`
- 결정: CPF 거래 실행 인스턴스는 `transactionId` 하나로 식별한다. 외부/선행 호출의 유효 transactionId를 승계하고, 내부 독립 기동은 Core가 34자리 transactionId를 신규 생성한다. 동일 업무 흐름의 Local/Remote/Async/Retry/Batch/Worker/Center-Cut은 같은 transactionId를 유지하며 호출 계층은 `segmentId/parentSegmentId`로 표현한다. `standardExecutionId`는 실행 정의 ID로 분리한다.
- 이유: Global/root/parent/child 거래 식별자와 업무 거래 정의 ID가 혼용되어 로그 그룹 추적, DB 검색, 호출 전파와 개발자 이해가 흔들리는 문제를 제거한다.

## DEC-024 Golden Generated Domain과 Legacy Fixed Domain Retirement

- 상태: `완료`
- 결정: Generated Domain의 정본은 특정 MBR/ACC/EXS 구현이 아니라 단일 Golden Template이다. 임의 DomainName/SystemCode를 Metadata로 적용하고 동일 Capability 결과는 normalize parity가 같아야 한다. 기존 수작업 fixed 업무 Module은 Consumer를 안전하게 이관한 뒤 retirement하며, `external/EXS`가 필요하면 fixed Module 복구가 아니라 동일 Generator로 다시 생성한다.
- 이유: 현재 수작업 Domain을 Template로 승격하면 과거 가비지/업무특화 구조가 신규 고객 Domain에 복제되고, 반대로 성급한 삭제는 기존 성공 기능을 회귀시킨다.

## DEC-025 Canonical Repository Path

- 상태: `완료`
- 결정: 제품 Root의 문서 파일은 최종적으로 `README.md`만 유지한다. Tool Script는 `cpf-tools/scripts`, Vendor별 사람이 수정하는 Platform DB Source와 배포/Runtime Pack은 모두 `cpf-tools/db/vendor/<vendor>` 경계가 소유하며 Source는 그 아래 `source/`에 둔다. Root 작업문서, 기존 Root `scripts`, 독립 `cpf-tools/db/source/<vendor>` tree는 역할별 Canonical Path로 이동하고 모든 Gradle/CI/Guide 참조를 함께 보정한다.
- 이유: 작업문서/SQL/Script가 Root에 산재하고 같은 역할의 정본이 중복되는 문제를 제거한다.

## DEC-026 Vendor-first Schema/Metadata Change Order

- 상태: `완료`
- 현재 적용: DEC-027의 Canonical Metadata/Generator-first 순서로 대체됨
- 결정: Table/Column/Index/Seed/기준 Metadata 변경은 DB Source/Vendor 정본부터 시작하고 generated bundle, migration/rollback, Mapper/Repository, Service/API/UI, Test/Runtime/Evidence 순서로 전파한다. Product Seed에는 설치 직후 필요한 권한/메뉴/정책 Metadata를 제공하고 Local/EDU/고객 조직 Sample은 Optional Seed로 분리한다.
- 이유: Java나 파생 SQL부터 수정해 Source/Install/Runtime 계약이 갈라지고 Fresh Install 때 Metadata가 비는 재발을 막는다.

## DEC-027 Canonical Metadata / Generator-first DB Change Order

- 상태: `완료`
- 결정: DB Query, Schema 또는 Metadata를 변경할 때의 정식 순서는 `Requirement / Data Model → Canonical Schema / Metadata → Generator / Domain Template → Generated Domain 산출 기준 → Vendor Source SQL → Migration → Install → Upgrade → Rollback → Seed → Verify → Test → Evidence`다. Platform 고정 제품 DB와 Generated Domain Template의 Ownership은 분리하되, 파생 Vendor SQL이나 Migration만 먼저 고쳐 정본과 산출물이 갈라지는 변경은 허용하지 않는다. Vendor별 물리 차이는 중앙 Pack에서 해결하고 Java 업무 Source는 DB-neutral하게 유지한다.
- 이유: 최신 사용자 검수 요청의 명시적 보정이며, MariaDB 단독 Hotfix가 Generator와 나머지 Vendor Pack에 Drift를 만드는 일을 막기 위해서다.

## DEC-028 DB 연결 보안과 설치 Verify 계약

- 상태: `완료`
- 결정: DB TLS mode는 Client/OS의 암묵 기본값에 맡기지 않고 설치 Profile에 `disabled`, `preferred`, `required`, `verify-full` 중 하나로 명시한다. Git 추적 Local Development Profile만 `disabled`를 사용하며 Production Template은 `verify-full`을 사용한다. 공식 Installer는 Canonical Schema Manifest 기준으로 실제 Table, Column 순서, 선언 Index와 FK를 대조하고, Product Seed 이후 중앙 Vendor Verify Pack의 모든 `check_name/passed` 결과가 1일 때만 완료로 판정한다.
- 이유: PC 재부팅이나 Client Version에 따라 TLS negotiation 결과가 달라지는 문제와, Table 존재 확인만으로 Stale/누락 Schema·잘못된 Seed를 설치 성공으로 기록하는 문제를 막는다.

## DEC-029 Migration Version 선택 기준

- 상태: `완료`
- 결정: 신규 Platform Migration version은 현재 수정 가능한 Source subset의 마지막 번호가 아니라 중앙 Runtime Lifecycle Pack에 보존된 전체 Historical Migration의 최고 version을 기준으로 선택한다. Historical V55/V56이 Runtime Pack에 이미 있던 상태에서 ADM transactionId 표준화는 V57로 추가하며, 기존 Migration 본문이나 checksum을 덮어쓰지 않는다.
- 이유: Source subset만 보고 번호를 선택하면 Runtime Historical version과 충돌해 Flyway 적용 순서와 checksum 무결성이 깨진다.

## DEC-030 Platform Table Lifecycle / Audit 정책

- 상태: `완료`
- 결정: Platform Table의 기본 정책은 `full-audit`이지만 Append-only 기록, 상태 전이 원장, Lock/Claim/Lease, Aggregate Projection, 채번, 정적 호환성 계약과 Spring Batch Framework Table은 `cpf-tools/db/metadata/platform-table-lifecycle-policy.json`에 lifecycle 유형, 필수 semantic actor/time/fencing Column과 사유를 명시해야만 예외로 허용한다. 신규 Table은 명시적 예외가 없으면 공통 Audit 계약을 적용하며, 미등록 Table·알 수 없는 정책·사유/필수 semantic Column 누락·삭제 Table의 stale 정책은 Gate에서 실패한다.
- 이유: 모든 운영/이력/Lease Table에 `created_by/created_at/updated_by/updated_at`을 기계적으로 추가하면 실제 lifecycle 의미와 중복되고 불필요한 저장 구조가 된다. 반대로 예외를 코드에 하드코딩하면 신규 Table이 검토 없이 빠지므로 Canonical Metadata를 fail-closed 정본으로 둔다.

## DEC-031 Comment Migration Delta와 Rollback 보존

- 상태: `완료`
- 결정: Schema Comment Migration Metadata에는 Canonical DDL 전체가 아니라 해당 Version이 새로 추가하는 Table/Column Comment Delta만 기록한다. 이전 Migration이나 기존 설치에 이미 있던 Comment는 신규 Migration/Rollback 대상에서 제외한다. V58은 Metadata Generator로 Forward/Rollback을 생성하고, 실제 MariaDB에서 Upgrade → Rollback → Re-upgrade 동안 Comment Delta, Column/Index/FK 정의 Hash와 `FOREIGN_KEY_CHECKS` 복원을 검증한다.
- 이유: 전체 Canonical Comment를 신규 Version Delta로 오인하면 Rollback이 V57 이전에 존재하던 설명까지 삭제한다. 실제 V57 DB Baseline이 이 위험을 검출했으며, Delta-only Metadata가 Historical 상태를 보존한다.

## DEC-032 Build Tooling 물리 소유권

- 상태: `완료`
- 결정: CPF Convention Gradle Plugin과 Platform BOM은 제품 Runtime Module이 아니므로
  Repository Root가 아니라 각각 `cpf-tools/build/gradle-plugin`,
  `cpf-tools/build/platform-bom`이 소유한다. Root `settings.gradle`은 이 두 격리
  Composite Build를 직접 참조한다. `cpf-tools/build`의 추적 Source만 `.gitignore`
  예외로 두고 각 격리 Build의 `.gradle`, `build`, `bin` 산출물은 계속 제외한다.
- 이유: Repository Root에는 제품 식별·Build 진입에 필요한 최소 구조만 유지하고,
  Build Support Unit의 소유권을 Tooling 경계에 모으면서 clone 직후에도 Composite
  Build가 재현되도록 하기 위해서다.

## DEC-033 선택 Generated Domain의 Platform/EDU 비종속과 Self Sample

- 상태: `완료`
- 결정: `cpf-admin`과 다른 고정 Platform Module은 MBR/ACC/PAY 같은 특정 Generated
  Domain의 Java Type, URL, DB, DataSource, 메뉴 또는 필수 Readiness에 종속되지 않는다.
  `cpf-reference`의 Local/Remote/Header/transactionId/Error 교육은 REF가 소유한 중립
  Self Simulator를 사용하고 특정 Generated Domain의 존재를 전제로 하지 않는다.
  각 Generated Domain은 동일 Generator-owned Minimal Transaction Source 구조와
  `${tablePrefix}_sample_item` 한 개를 자체 보유하며 Local/Remote 검증도 자기
  Public/Internal Sample 경계를 사용한다. 서로 다른 Domain 간 parity는 Generator
  검증이 임시 Domain을 생성한 동안에만 수행하고 종료 시 모두 제거한다. 기존
  MBR/ACC 업무특화 Source/Table은 Consumer를 Platform/REF에서 제거하고 필요한 고객
  확장 Ownership을 분리한 뒤 Golden Template 전환 과정에서 retirement한다.
- 이유: Generated Domain은 선택적으로 삭제·재생성될 수 있어야 하며, ADM/REF가 특정
  예시 Domain을 요구하면 제품 Platform 기동과 EDU가 고객 업무 Module 수명주기에
  결합된다. 한 개의 중립 Self Sample만 정본으로 두면 이름에 따른 업무 가정과
  Schema/Source drift 없이 임의 Domain을 동일하게 검증할 수 있다.

## DEC-034 공식 DB Vendor 3종

- 상태: `완료`
- 결정: CPF의 공식 DB Vendor는 MariaDB, PostgreSQL, Oracle 정확히 3종이다.
  MySQL과 SQL Server는 제품 Vendor 선택, 설치 Profile, Generator 산출 대상,
  Runtime Query Pack 및 완료율에서 제외한다. MariaDB Tool이 사용하는 `mysql` 호환
  CLI/환경 변수 명칭은 MariaDB Client 호환 구현으로만 유지하며 MySQL 제품 지원으로
  해석하지 않는다.
- 이유: 실제 구현·Canonical Manifest·검증 범위를 공식 지원 선언과 일치시키고,
  미구현 Vendor 디렉터리나 fallback을 완료로 오인하는 것을 막기 위해서다.

## DEC-035 BZA 업무 결재 API 정본

- 상태: `완료`
- 결정: BZA 정책 기반 업무 결재 API의 정본 Root는
  `/api/bza/approvals/**`다. 상신/Inbox/정책/위임/결정은 이 Root 아래에서만 제공하며,
  직접 Table 상태를 갱신하던 `/api/bza/backoffice/approvals/**`는 영구 폐기 410
  경계로 유지한다. Java Controller, Frontend, Permission Manifest와 Product Seed의
  Menu/API Pattern은 같은 복수형 Root를 사용한다.
- 이유: 새 정책/Snapshot Engine과 폐기된 직접 결재 API가 동시에 활성화되는 것을
  막고, 인증 Route와 DB 권한 Seed가 실제 Endpoint를 동일하게 가리키도록 하기 위해서다.

## DEC-036 중앙 Vendor SQL Catalog Provider의 공개 조립 경계

- 상태: `완료`
- 결정: 중앙 Runtime Query Pack을 소비하는 Platform/Generated Domain Runtime은
  `CpfVendorSqlCatalogProvider` 공개 SPI만 주입받는다. Core의 Public Boundary
  AutoConfiguration이 Environment 기반 fail-closed 기본 Provider를 제공하되,
  제품 또는 배포 환경이 명시한 사용자 정의 Provider가 있으면 대체하지 않는다.
  개별 BAT/업무 Module이 Core 내부 Catalog 구현을 직접 참조하거나 Vendor별
  Provider를 중복 구현하지 않는다.
- 이유: CPF Core DataSource 전체 구성을 사용하지 않는 BAT Scheduler/Worker/
  Control Server도 동일 중앙 Pack을 시작 시 결정적으로 선택해야 하며, Provider
  누락으로 기동이 실패하거나 Module별 Vendor 해석이 갈라지는 것을 막기 위해서다.

## DEC-037 Platform Canonical DB와 Generated Domain DB의 물리 분리

- 상태: `완료`
- 결정: `platform-schema.json`, Platform Default/Production DB Profile과 Platform
  Empty Install/Seed/Verify에는 고정 제품 Module인 CPF/CMN/ADM/BZA/BAT/REF만 둔다.
  MBR은 Generator Golden Reference Instance지만 DB 생성·삭제·재생성은 다른 임의
  Generated Domain과 동일한 Domain Manifest와 중앙 Vendor Domain Template이
  소유한다. ACC/PAY/INS 등은 Platform Canonical/Profile에 등록하지 않으며 신규
  Domain 추가를 위해 Platform Tool Source나 고정 Module 배열을 수정하지 않는다.
  공식 3 Vendor의 Platform Source/Lifecycle은 같은 Canonical Schema/Seed에서
  생성하고 MariaDB도 예외적인 수작업 정본으로 두지 않는다.
- 이유: 선택적으로 제거 가능한 Generated Domain을 Platform 설치 대상에 포함하면
  Clean Install과 Verify가 특정 예시 Domain 수명주기에 종속되고 Vendor Pack에
  고정 Domain SQL이 다시 누적된다. Platform 160 Table과 Generated Domain
  `${tablePrefix}_sample_item` 수명주기를 분리해야 무제한 Metadata 확장이 가능하다.

## DEC-038 기존 DB Upgrade의 명시 Routing·Provision·Schema Parity

- 상태: `완료`
- 결정: Platform Migration Runner는 Version/Module/Profile/Checksum/Backup Manifest,
  중지·복구 준비 확인과 정확한 Plan SHA가 모두 일치할 때만 Apply한다. MariaDB
  Historical V64~V72처럼 `USE`가 없는 배포 이력은 Migration 본문을 수정하지 않고
  checksum-fixed 중앙 Routing Metadata로 Owner DB를 선언하며, V69 같은 복수 Owner
  Migration은 모든 Owner를 함께 선택하지 않으면 fail-closed한다. 기존 완전 DB의
  Service User 비밀번호·Grant 회전은 설치를 재실행하거나 부분 DDL을 수행하지 않는
  공식 `provision-only` 모드로 처리한다. Fresh Install의 Column/Index/FK 정의는
  immutable Historical Upgrade 결과와 같아야 한다.
- 이유: DB명 추정, 관리자 계정으로 Migration 우회, 백업 없는 Apply 또는 Fresh와
  Upgrade의 물리 Drift는 데이터 손상과 복구 불능을 만든다. 실제 V58→V73 Upgrade,
  V73 Rollback/Re-apply와 Canonical Manifest 대조에서 이 경계가 필요함을 확인했다.

## DEC-039 Profile/Canonical 기반 DB Provision·Verify Projection

- 상태: `완료`
- 결정: 공식 3 Vendor의 현재 Provision, Install, Product Seed, Verify와 중앙
  Lifecycle Bundle은 사람이 서로 다른 파일을 병행 편집하지 않고 DB Install
  Profile, Canonical Schema/Metadata 및 Generator 산출물로 투영한다. 동기화 순서는
  `Canonical/Vendor Source 생성 → Lifecycle Pack 조립 → Aggregate Install SQL
  조립`으로 고정한다. 하나의 Logical DB Section이 여러 Source 파일에 반복되면
  Runner는 모든 Section을 선언 순서대로 실행하며, Verify는 실행 결과가 없거나
  하나라도 `passed != 1`이면 실패한다. Immutable Historical Migration은 이
  Projection 대상에서 제외하고 기존 Checksum을 보존한다.
- 이유: Aggregate Bundle을 먼저 만들면 직전 세대 Source가 포함되고, 반복 Section의
  첫 블록만 실행하면 Install/Seed 일부가 조용히 누락된다. 또한 Verify 출력 미수집을
  성공으로 처리하면 타 Vendor 설치가 실제 검증 없이 완료로 기록될 수 있으므로
  Canonical Projection과 fail-closed 실행 계약을 명시한다.

## DEC-040 Platform Lifecycle과 Generated Domain Lifecycle 분리

- 상태: `완료`
- 결정: Platform Provision/Install/Seed/Verify와 기본 배포·Runtime Baseline은
  `CPF/CMN/ADM/BZA/BAT/REF` 고정 제품 Owner만 포함한다. Generated Domain은
  `ACC/MBR/EXS` 같은 이름 목록이 아니라 유효한 `domain-manifest.json`,
  Generator Ownership Metadata와 중앙 Vendor Domain Template로 동적 발견하고
  별도 Lifecycle Overlay로 적용한다. Platform Reset/Verify는 Generated Domain
  Database의 공존 여부와 무관하게 동일해야 한다.
- 이유: 선택적으로 삭제·재생성되는 Generated Domain을 Platform Profile에 넣으면
  제품 설치와 운영 Gate가 예시 Domain의 수명주기에 종속되고 무제한 Metadata 확장이
  불가능하다. Platform 고정 제품과 사용자 생성 업무의 소유권·복구 경계를 분리해야
  Source 수정 없는 신규 Domain 추가와 안전한 독립 Reset을 함께 보장할 수 있다.

## DEC-041 Immutable Historical Migration 무결성 검증

- 상태: `완료`
- 결정: 이미 배포된 Historical Migration은 현재 Canonical Schema/Comment Metadata로
  다시 생성하지 않는다. 생성 당시의 Forward/Rollback SQL과 대응 Metadata를
  SHA-256 Snapshot 및 Statement 단위로 검증하고, 현행 요구 변경은 새로운 Version의
  Migration과 현재 Install Projection으로만 반영한다.
- 이유: 현재 Canonical에서 과거 Migration을 재생성하면 이후 추가·삭제된 Table,
  Column, Comment가 과거 Version에 역투영되어 Checksum과 Upgrade/Rollback 재현성이
  깨진다. Historical SQL 불변성과 현재 Canonical 정합성은 별도 Gate로 검증해야 한다.

## DEC-042 REF Runtime Query와 DB Test Lifecycle 소유권

- 상태: `완료`
- 결정: `cpf-reference`의 Center-Cut/Repository SQL도 다른 Platform Owner와 동일하게
  `CpfVendorSqlCatalogProvider.forModule("ref")`를 통해 중앙
  `cpf-tools/db/runtime-template/ref`와 공식 3 Vendor Runtime Query Pack에서만
  공급한다. Java Source에는 `LIMIT`, `ON DUPLICATE KEY`, Oracle/PostgreSQL 분기를
  두지 않는다. REF DB Test는 module-local 또는 임의 Vendor `CREATE TABLE` fixture를
  실행하지 않고 Canonical Schema/Seed Metadata에서 생성된 공식
  `Provision → Install → Test Seed → Verify`가 끝난 격리 DB를 사용한다.
- 이유: MariaDB 전용 SQL과 부분 DDL fixture가 Java/Product JAR 또는 Test Source에
  남으면 Fresh Install을 우회하고 PostgreSQL/Oracle parity와 Canonical Index/Type이
  즉시 갈라진다. REF도 고정 Platform Owner이므로 중앙 Catalog와 공식 Lifecycle의
  동일한 fail-closed 경계를 적용해야 한다.

## DEC-043 BAT Worker Primary Execution Engine

- 상태: `완료`
- 결정: BAT Job/Step/Checkpoint/Retry/Restart/JobRepository lifecycle의 유일한
  Primary Engine은 Spring Batch다. `JobPackDispatcher`, `WorkerRuntime`,
  `JdbcWorkerExecutionRepository` 기반의 병렬 Custom Engine은 복원하지 않는다.
  CPF는 승인된 Definition Snapshot, Fencing/Epoch, Idempotency, Runtime
  Admission/Drain/Capacity, Spring Execution Link와 Unknown Reconciliation만
  소유한다. Kafka Remote Worker는 이 경계를 통해 Spring Batch Step 실행을
  수신하며 `bat_worker` 또는 custom lease table을 Job lifecycle 정본으로 사용하지 않는다.
- 이유: 삭제된 Dispatcher를 Legacy Gate 때문에 복원하면 Spring Batch와 별도
  Job/Step 상태기가 동시에 실행되어 Retry/Restart/Fencing 결과가 서로 달라진다.
  기존 WIP의 문제는 삭제 자체가 아니라 Consumer/Test/Gate/SQL 이관을 끝내지 않은
  것이므로 새 Spring Batch 경로를 완성하고 Stale 요구를 제거해야 한다.

## DEC-044 Generated Domain Minimal DB Topology

- 상태: `완료`
- 결정: 모든 임의 Generated Domain의 기본 DB는 업무명과 무관하게
  `${tablePrefix}_sample_item` 한 개와 `${tablePrefix}_idempotency_ledger` 한 개,
  정확히 두 Table로 생성한다. Generator/DB Tool은 Domain/SystemCode 고정 목록이나
  Domain별 Operation Table 집합을 갖지 않으며, 존재하지 않는 Template을 완료 조건으로
  요구하지 않는다. 고객 업무 Table은 생성 이후 고객 확장 Ownership이다.
- 이유: CPF 생성형 Sample의 목적은 특정 회원·계좌 업무 모델이 아니라 동일한 거래
  Processor 흐름을 검증하는 것이다. 추가 지원 Ledger를 기본 생성하면 Domain 간 구조가
  갈라지고 삭제·재생성 비용과 3 Vendor 중복만 늘어난다.

## DEC-045 BAT Control Plane SQL·Schema Ownership

- 상태: `완료`
- 결정: BAT Runtime Java는 `CpfVendorSqlCatalogProvider.forModule("bat")`의 중앙 Query
  Key만 소비한다. Spring Batch가 Primary Engine이므로 삭제된 Custom Worker
  Execution/Lease SQL은 복원하지 않는다. CPF가 소유하는 승인 Launch, Execution
  Control, Spring Execution Link, Fencing Epoch는 Canonical lowercase 물리 Table로
  Fresh Install에 포함하며 Historical MariaDB case 차이는 Append-only V95에서만 보정한다.
- 이유: Java inline SQL과 Fresh Install 누락은 Vendor 중립 Artifact를 깨뜨리고,
  Custom Worker 상태기계를 되살리면 Spring Batch와 Dual Primary가 된다. Canonical
  Schema와 중앙 Query Pack을 함께 소유해야 Clean Install/Upgrade/Runtime이 일치한다.

## DEC-046 Vendor Rollback Root와 Empty-string Nullability Repair

- 상태: `완료`
- 결정: 공식 Rollback Consumer Root는 모든 Vendor에서
  `cpf-tools/db/vendor/<vendor>/rollback`이며 PostgreSQL/Oracle은 그 아래
  `{logicalDatabase}`로 Owner를 구분한다. `migration/rollback` 복제 Root는 사용하지
  않는다. Immutable V69/V74의 Empty-string NOT NULL과 현재 Canonical nullable 의미
  차이는 Canonical Schema 46을 입력으로 하는 Generated V96으로 3 Vendor에 보정한다.
  Oracle은 빈 문자열과 NULL을 구분할 수 없어 R96에서 가짜 Sentinel을 기록하지 않고
  사전 Backup 복구를 요구하며 fail-closed한다.
- 이유: Pack Metadata와 실행 Tool의 Rollback Root가 다르면 정상 Rollback 파일이 있어도
  Runtime에서 발견되지 않는다. 또한 Historical 본문을 수정하면 Checksum 재현성이
  깨지고, Oracle의 표현 불가능한 과거 상태를 강제 복원하면 실제 데이터가 변조된다.

## DEC-047 CPF Starter 제품 Root와 Artifact 소유권

- 상태: `완료`
- 결정: `cpf-starters/`는 Generated Domain이나 임시 Root가 아니라 CPF가 배포하는
  선택형 Boot AutoConfiguration 제품 Root다. Product Surface Policy, Gradle Settings,
  Architecture/Inventory Gate, Platform BOM, Local/Staging/Internal Publication과
  Artifact 전파 검증은 동일한 7개 Starter 제품 집합을 사용한다.
- 이유: Starter를 Generated Domain으로 오인하거나 publication/BOM에서 빼면 Source
  Gate와 실제 배포 Artifact가 서로 다른 false-green 상태가 된다. 정식 제품 Root와
  공개 Artifact 집합을 한 계약으로 묶어야 고객 Runtime 조립이 재현 가능하다.

## DEC-048 Gateway 단일 Primary

- 상태: `완료`
- 결정: Gateway의 유일한 Primary Ingress/Proxy Engine은 Spring Cloud Gateway MVC의
  `CpfScgPrimaryHandler`와 `CpfScgPrimaryRouteConfiguration`이다. 제거한 legacy
  Controller, ProxyService, JDK transport 계층은 복원하지 않으며 Audit/Ledger Recovery도
  SCG completion 경계와 동일한 canonical time serialization을 사용한다.
- 이유: 두 Proxy Engine을 함께 유지하면 Authorization, retry, body capture, ledger의
  실행 순서와 장애 의미가 갈라진다. SCG 단일 경계가 Route/Filter/Recovery 계약의
  일관성과 Primary Engine 검증 가능성을 보장한다.

## DEC-049 Generated Domain 실행 결과 소유권

- 상태: `완료`
- 결정: Generator 실행 결과와 drift report는 생성된 제품 Module의 소유 파일이 아니다.
  `build/reports/create-domain/<module>` 같은 중앙 ignored 작업 결과 경로에만 생성하고,
  Generated Domain ownership/manifest에는 포함하지 않는다.
- 이유: 실행별 결과 JSON을 제품 Root에 넣으면 재생성할 때 Source Diff와 Ownership
  Drift가 생기며 Domain 삭제도 불완전해진다. 제품 산출물과 도구 실행 Evidence를
  분리해야 Generator가 arbitrary Domain에 대해 멱등적으로 동작한다.

## DEC-050 ADM/BZA 운영 데이터 표시 표준

- 상태: `완료`
- 결정: ADM/BZA는 운영 결과 객체를 raw JSON `<pre>`로 직접 노출하지 않는다. 공통
  structured renderer가 nested record/list를 bounded depth/item으로 표시하고 민감 Field를
  masking하며, 단순 인증 오류는 semantic alert text로 표시한다.
- 이유: 무제한 raw serialization은 대형 Payload로 UI를 정지시키고 비밀·개인정보를
  그대로 노출할 수 있다. 구조화·상한·마스킹을 공통화해야 운영 가독성과 보안 경계를
  동시에 유지할 수 있다.


## 2026-07-25 — Vendor source ownership / EXS / Frontend packaging

- EXS는 고정 Platform Module이 아니라 Generated Domain only로 확정한다. `external/EXS`도 PAY/INS/CRM과 동일 Golden Generator를 사용한다.
- Platform Vendor canonical source는 `cpf-tools/db/vendor/<vendor>/source` 경계에서 관리한다. 특정 Vendor 전용 top-level source tree를 만들지 않는다.
- 지원하지 않는 Platform Vendor는 MariaDB 복사본/fallback 없이 fail-closed한다.
- ADM/BZA frontend는 self-contained static artifact이며 외부 CDN/remote CSS/font/icon에 Runtime 의존하지 않는다. App Shell, feature package, route registry, state/API boundary, code splitting을 표준으로 한다.
- 환경별 Docker Compose는 Repository Root가 아니라 `deploy/`가 소유한다.

## 2026-08-02 — POST-QA37 canonical decisions

### DEC-STARTER-ROOT

- `cpf-starters/` is a permanent official product root.
- Purpose: keep `cpf-core` topology-independent and lightweight.
- Optional technical runtime belongs in Leaf Starters or real owner modules.
- Generated/Business Domains select only required capabilities.

### DEC-STARTER-GROUP

- A representative Starter may bring dependent Starters through Gradle transitive dependencies.
- Default product mechanism is a versioned Generator Capability Profile that expands to explicit Leaf Starters and stores `resolvedStarters`.
- Aggregate Starter is permitted only for stable, surveyed combinations and owns no Bean/AutoConfiguration.
- Mega Starter is prohibited.

### DEC-MESSAGING-PROVIDERS

- Kafka remains an official provider.
- JMS, IBM MQ, RabbitMQ/AMQP and persistent TCP requirements are restored to the canonical catalog.
- Generic broker contracts stay in Core; runtime/provider implementations belong in Starters.
- JMS and IBM MQ are separate layers.
- User input `TPC` is retained as a search alias and provisionally maps to `EXS-TCP` until clarified.

### DEC-DB-FRESH-GENERATOR-FIRST

- Every Codex DB verification starts with a dedicated CPF QA Database/Schema containing zero CPF objects.
- Existing user DBs are protected and never reset.
- Canonical Metadata/Generator is changed before Vendor SQL.
- No manual SQL may bypass a missing official lifecycle path.
- Each official Vendor runs Fresh Install, metadata/seed, upgrade, rollback, reapply, drift, runtime query and cleanup.

### DEC-CODEX-EVIDENCE

- QA37 focused PASS records remain historical inputs.
- Multiple pushes invalidate exact-SHA completion claims.
- Current completion requires latest HEAD, clean tree, command/environment/artifact hashes and all mandatory stages.

``n

### Snapshot: cpf-tools/scripts/apply-cpf-qa38-final-requirements-review.ps1
- SHA-256: $sha
- Preserved before QA38 currentization

``text
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$RepositoryRoot,
    [Parameter(Mandatory=$true)][string]$OverlayRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-Git {
    param([string[]]$Arguments, [switch]$AllowFailure)
    $out = @(& git -C $script:Repo @Arguments 2>&1)
    $code = $LASTEXITCODE
    if (-not $AllowFailure -and $code -ne 0) {
        throw "git $($Arguments -join ' ') failed ($code): $($out -join [Environment]::NewLine)"
    }
    [pscustomobject]@{ExitCode=$code; Output=$out; Text=($out -join [Environment]::NewLine).Trim()}
}

function Normalize-RelativePath {
    param([string]$Value)
    $p = $Value.Replace('\','/').Trim()
    if ([string]::IsNullOrWhiteSpace($p) -or [IO.Path]::IsPathRooted($p) -or $p -match '(^|/)\.\.(/|$)') {
        throw "Unsafe path: $Value"
    }
    $p
}

function Merge-MarkdownReview {
    param(
        [string]$ExistingPath,
        [string]$PackagePath,
        [string]$RelativePath
    )
    $start = '<!-- CPF_QA38_MERGE_SAFE_START -->'
    $end = '<!-- CPF_QA38_MERGE_SAFE_END -->'
    $packageText = Get-Content -LiteralPath $PackagePath -Raw -Encoding UTF8

    $supplement = @"
$start

## QA38 최종 통합 병합 구간

이 구간은 `CPF_20260802_QA38_FINAL_INTEGRATED_REQUIREMENTS_REVIEW_MERGE_SAFE_ROOT_OVERLAY`이 관리한다.

- 기준 Commit: `2e93d92393c52b887482731b683db3c3822027b1`
- Canonical 169·Core→Starter 30·Self Development 62 Git 반영 확인
- 사용자 승인 없는 RabbitMQ/AMQP·JMS·TCP 요구 제외 금지
- Starter Consumer·DB·Generator·Artifact·Fault·Operations 완료 Gate 유지
- 보호 경로 수정·삭제 금지
- development_status: 부분 구현
- verification_status: 미검증

패키지 기준 전체 리뷰 원문은 적용 전에 생성한 Backup과 ZIP 내부에서 확인한다.

$end
"@

    $existing = Get-Content -LiteralPath $ExistingPath -Raw -Encoding UTF8
    $pattern = [regex]::Escape($start) + '.*?' + [regex]::Escape($end)
    if ([regex]::IsMatch($existing,$pattern,[Text.RegularExpressions.RegexOptions]::Singleline)) {
        $merged = [regex]::Replace(
            $existing,
            $pattern,
            [Text.RegularExpressions.MatchEvaluator]{ param($m) $supplement.Trim() },
            [Text.RegularExpressions.RegexOptions]::Singleline
        )
        Write-Host "[MERGE_UPDATE_MARKER] $RelativePath"
    } else {
        $merged = $existing.TrimEnd() + [Environment]::NewLine + [Environment]::NewLine + $supplement.Trim() + [Environment]::NewLine
        Write-Host "[MERGE_APPEND_MARKER] $RelativePath"
    }
    [IO.File]::WriteAllText($ExistingPath,$merged,[Text.UTF8Encoding]::new($false))
}

$script:Repo = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$overlay = (Resolve-Path -LiteralPath $OverlayRoot).Path

if (-not (Test-Path -LiteralPath (Join-Path $script:Repo '.git'))) { throw 'Not a Git repository root.' }
if ((Invoke-Git @('rev-parse','--abbrev-ref','HEAD')).Text -ne 'master') { throw 'Current branch must be master.' }
$remote = (Invoke-Git @('remote','get-url','origin')).Text
if ($remote -notmatch 'freeangelsun/202412_01_CPF(?:\.git)?$') { throw "Unexpected origin: $remote" }

Invoke-Git @('fetch','origin','master') | Out-Null
$baseline = '2e93d92393c52b887482731b683db3c3822027b1'
if ((Invoke-Git @('merge-base','--is-ancestor',$baseline,'HEAD') -AllowFailure).ExitCode -ne 0) {
    throw "Local HEAD does not contain package baseline. Pull/reconcile normally first: $baseline"
}
if ((Invoke-Git @('merge-base','--is-ancestor','origin/master','HEAD') -AllowFailure).ExitCode -ne 0) {
    throw 'origin/master is ahead of local HEAD. Reconcile normally before apply.'
}

$protected = @(
    'cpf-docs/deliverables/',
    'cpf-docs/guides/',
    'cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/'
)
$mergeManaged = @(
    'cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_CANONICAL_MERGE_REVIEW.md',
    'cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_DEVELOPER_GPT_MERGE_REVIEW.md',
    'cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_FINAL_REVIEW.md',
    'cpf-docs/work/review/20260802_06/CPF_20260802_06_QA38_STARTER_FINAL_REVIEW.md'
)

$hashRelative = 'cpf-docs/work/current/CPF_20260802_06_QA38_FILES.sha256'
$hashFile = Join-Path $overlay $hashRelative.Replace('/',[IO.Path]::DirectorySeparatorChar)
if (-not (Test-Path -LiteralPath $hashFile -PathType Leaf)) { throw "Missing hash file: $hashFile" }

$entries = New-Object System.Collections.Generic.List[object]
foreach ($line in Get-Content -LiteralPath $hashFile -Encoding UTF8) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    if ($line -notmatch '^([0-9a-fA-F]{64})  (.+)$') { throw "Invalid hash line: $line" }
    $expected = $Matches[1].ToLowerInvariant()
    $rel = Normalize-RelativePath $Matches[2]
    foreach ($prefix in $protected) {
        if ($rel.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)) {
            throw "Protected path found in overlay. Nothing changed: $rel"
        }
    }
    $src = Join-Path $overlay $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw "Overlay file missing: $rel" }
    $actual = (Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne $expected) { throw "Hash mismatch: $rel" }
    $entries.Add([pscustomobject]@{Relative=$rel; Source=$src; Hash=$actual})
}

$entries.Add([pscustomobject]@{
    Relative=$hashRelative
    Source=$hashFile
    Hash=(Get-FileHash -LiteralPath $hashFile -Algorithm SHA256).Hash.ToLowerInvariant()
})

$conflicts = New-Object System.Collections.Generic.List[string]
foreach ($entry in $entries) {
    if ($mergeManaged -contains $entry.Relative) { continue }
    $target = Join-Path $script:Repo $entry.Relative.Replace('/',[IO.Path]::DirectorySeparatorChar)
    $status = (Invoke-Git @('status','--porcelain=v1','--',$entry.Relative)).Text
    if (-not [string]::IsNullOrWhiteSpace($status)) {
        if ((Test-Path -LiteralPath $target -PathType Leaf) -and
            ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -eq $entry.Hash)) {
            continue
        }
        $conflicts.Add($entry.Relative)
    }
}
if ($conflicts.Count -gt 0) {
    throw "Non-mergeable local changes conflict with QA38 package. Nothing changed.`n$(($conflicts | Sort-Object -Unique) -join [Environment]::NewLine)"
}

$backup = Join-Path $env:TEMP ('cpf-qa38-merge-safe-backup-' + (Get-Date -Format 'yyyyMMdd_HHmmss'))
New-Item -ItemType Directory -Path $backup -Force | Out-Null

foreach ($entry in $entries) {
    $target = Join-Path $script:Repo $entry.Relative.Replace('/',[IO.Path]::DirectorySeparatorChar)
    if (Test-Path -LiteralPath $target -PathType Leaf) {
        $backupTarget = Join-Path $backup $entry.Relative.Replace('/',[IO.Path]::DirectorySeparatorChar)
        New-Item -ItemType Directory -Path (Split-Path -Parent $backupTarget) -Force | Out-Null
        Copy-Item -LiteralPath $target -Destination $backupTarget -Force
    }

    if (($mergeManaged -contains $entry.Relative) -and (Test-Path -LiteralPath $target -PathType Leaf)) {
        Merge-MarkdownReview -ExistingPath $target -PackagePath $entry.Source -RelativePath $entry.Relative
        continue
    }

    New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force | Out-Null
    Copy-Item -LiteralPath $entry.Source -Destination $target -Force
    Write-Host "[APPLY] $($entry.Relative)"
}

Invoke-Git @('diff','--check') | Out-Null
Write-Host "QA38 merge-safe package applied."
Write-Host "Merge-managed review files: $($mergeManaged.Count)"
Write-Host "Protected paths modified: 0"
Write-Host "Deleted files: 0"
Write-Host "Backup: $backup"
Write-Host "No commit or push was performed."
(Invoke-Git @('status','--short')).Output | ForEach-Object { Write-Host $_ }

``n