# CPF 활성 개발·QA 통합 정본

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 개발 기준 SHA: `2a86ff42799eddffaae87d38ae68632726a2c495`
- QA 문서 통합 기준 SHA: `e18dcf95129499a08c50f7ae374f4a6e8f762309`
- 작업 종료 시각: `2026-08-03 KST`
- development_status: `완료`
- verification_status: `재확인 필요`
- 사람이 읽는 개발·QA·Codex 진입점: **이 파일 1개**

Requirement·Scenario·삭제·변경·Hash·Package처럼 도구가 직접 읽는 구조화 파일만 같은 `cpf-docs/work` 디렉터리에 별도로 유지한다. 날짜·QA 회차·세션·R1/R2/FINAL별 중복 Markdown과 신규 디렉터리는 만들지 않는다.

## 1. 최종 우선순위와 보호 경계

요건 충돌 시 다음 순서를 적용했다.

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`와 Architecture·Specification
2. QA39 최종 개발요건과 `CPF_REQUIREMENT_MATRIX.csv`·`CPF_SCENARIO_MATRIX.csv`
3. 개발 GPT 자체 발굴 요건
4. 구현 편의와 과거 완료 보고

QA 요건과 자체 요건이 충돌한 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook 제품화, Resilience·Feature Flag 보호 지시는 적용하지 않고 QA의 완전 제거 결정을 우선했다.

다음 경로는 읽기 전용으로 보호했으며 Overlay와 Delete Manifest 변경 경로가 0건이다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean, 실제 파일 삭제는 수행하지 않았다.

## 2. 작업 전 리뷰와 통합 구현 순서

QA39 44개 Requirement를 파일별로 따로 수정하지 않고 공통 원인 기준으로 다음 순서로 통합했다.

1. 공개 선택면과 Build Graph 정본화
2. 6개 Profile·7개 Capability Group과 내부 Provider Leaf 분리
3. Settings·BOM·Artifact Catalog·Generator Catalog 단일 정책화
4. Generator 최소 표준 상속과 선택 기능만 조립
5. 이용 Domain 선언형 승인 예외와 Build/Runtime Fail-closed
6. Runtime Control God Starter 해체와 Owner Starter별 Applier 이관
7. Messaging·Notification·SFTP의 실제 Consumer·실패·UNKNOWN·복구 연결
8. 고객 Provider Public SPI Conformance 검증
9. 완전 제거·교체 대상 exact-path 삭제 작업목록과 안전 명령
10. False Complete 차단, Matrix·Evidence·Package 정합성

영향 범위는 동일 JVM/MSA, 단일·다중 인스턴스, Process Kill, lease 만료, ACK/응답 손실, 재시도·DLQ·Reconcile, 보안·감사·마스킹, Oracle/PostgreSQL/MariaDB, Generator/Golden Domain/기존 Generated Domain까지 포함했다.

## 3. Architecture 최종 결정

### 3.1 공개 선택면

공개 Profile은 다음 6개만 유지한다.

- `minimal-domain`
- `web-api`
- `secure-api`
- `browser-bff`
- `event-service`
- `batch-service`

공개 Capability Group은 다음 7개다.

- `data`
- `messaging`
- `integration`
- `file`
- `notification`
- `security`
- `platform-operations`

OpenAPI는 `web-api`, Scheduler는 `batch-service`와 `cpf-batch` Runtime이 소유한다. Provider·Codec·Exporter Leaf는 Catalog상 internal이며 생성 개발자가 직접 선택하는 공개 Starter가 아니다.

### 3.2 완전 제거와 내부화

완전 제거 대상은 AOP Service Access, Validation, Resilience, Feature Flag, 미등록 FTPS/gRPC/S3/Realtime WebFlux/SMB/SOAP/Webhook이다. Security/Cache Aggregate, 13개 구 Profile, 공개 Quartz Starter는 각각 Group·Use-case Profile·`cpf-batch` 내부 Runtime으로 대체한다.

물리 삭제는 사용자 승인 전 실행하지 않았다. Settings/BOM/Catalog/Generator/Consumer 참조는 제거했고, 실제 파일은 `CPF_PRODUCT_DELETE_WORK_ITEMS.csv`와 `CPF_DELETE_MANIFEST.txt`의 exact path 및 `CPF_DELETE_ONE_LINE.ps1.txt`로만 처리한다.

### 3.3 Public API·SPI·Internal 경계

- 업무 Consumer는 CPF DTO·Result·Operations만 참조한다.
- KafkaTemplate, RabbitTemplate, JmsTemplate, JdbcTemplate, OpenTelemetrySdk, Redis Client, SftpClient 같은 OSS 타입은 업무 API에 노출하지 않는다.
- 고객 Provider는 CPF Public SPI만 구현하며 `com.cpf.starter.*` 내부 Adapter를 참조하지 않는다.
- Runtime Control Client는 명령·버전·fencing·reconcile 계약만 소유하고 기능별 Applier는 Owner Starter가 제공한다.

## 4. 구현 결과

### 4.1 Generator와 표준 상속

Generator 기본값은 `minimal-domain`이며 다음 필수 표준만 자동 상속한다.

`standard-error`, `header-context`, `transaction-id`, `security-boundary`, `audit`, `masking`, `observability`, `config`, `dependency-version`, `architecture-gate`

DB, Messaging, Batch, File, Notification, Cache 등은 명시 선택한 Profile·Capability·Provider만 Gradle Dependency, Config, Bean, SQL, Migration, resolved lock에 추가한다. 선택하지 않은 기능은 생성되지 않는다. 생성 Build는 Java 25를 독립 선언하고, Profile·Provider Artifact가 실행 산출물에 실제 포함되는지 lock 기준으로 검증한다. Upgrade는 사용자 수정 영역을 보존하고 stale policy/lock을 표준 경로로 갱신한다.

기존 Generated Consumer인 `cpf-member`도 동일한 policy·approved exception registry·resolved lock·packaging verification 규칙으로 맞췄다.

### 4.2 이용 Domain 선언형 예외

외부 OSS 또는 고객 전용 구현 예외는 Platform Source를 수정하지 않고 이용 Domain의 `config/cpf-approved-exceptions.csv`에 선언한다. 예외 ID, Module, Capability, Artifact와 정확한 Version, Owner, 사유, 환경, 보안·라이선스·Supply-chain 검토, 승인자·승인/만료 시각, Rollback·표준 복귀 계획, Config 파일, Evidence, Hash를 필수화했다.

Convention Plugin과 Runtime Verifier는 미등록·미승인·만료·환경 범위 초과·Version Drift·Config Hash Drift를 fail-closed한다. 승인 예외는 `resolved-starter-lock.json`과 Runtime Resource에 동일 ID·Hash로 기록된다.

### 4.3 Messaging Reliability

- Kafka Named Binding의 이름과 default 여부를 설정 기반으로 변경했다.
- 0개 또는 2개 default binding은 fail-closed한다.
- Kafka/RabbitMQ/JMS/IBM MQ는 공통 `CpfBrokerClient`와 표준 Publish Request/Result를 사용한다.
- Outbox Publisher와 Provider Router를 실제 연결했다.
- lease reclaim, retry, DLQ, UNKNOWN 결과 분리, Probe/Reconcile, ACK 손실 후 중복 재발행 방지를 구현했다.
- 동일 messageId의 payload/topic/partition key/attribute가 다르면 멱등 성공으로 오인하지 않고 충돌 처리한다.
- Oracle/PostgreSQL/MariaDB V2 migration/rollback과 필요한 index를 맞췄다.

### 4.4 Notification

- `cpf-batch` Worker의 반복 Dispatch Scheduler를 실제 Consumer로 연결했다.
- 만료 CLAIMED lease 회수, retry, UNKNOWN, Receipt, Reconcile, 승인된 재처리, Audit 추적을 구현했다.
- Provider와 DB 상태를 실제로 확인하는 Health를 제공하고 오류 원문·민감정보는 노출하지 않는다.
- Oracle/PostgreSQL/MariaDB V2 migration/rollback을 맞췄다.

### 4.5 SFTP

- Raw password 설정을 거부하고 Secret Reference와 `CpfSecretProvider`를 강제했다.
- 실제 접속 Health, host key 검증, sanitized endpoint/reason code를 제공한다.
- 원격 POSIX Root 경계, `/safe`와 `/safe2` 구분, `..`, local symlink 탈출, resume 크기 역전, 부분 파일을 검증한다.
- 전송 후 응답 손실은 일반 실패가 아니라 `UNKNOWN` ledger 상태로 남겨 중복 업로드를 차단한다.

### 4.6 Runtime Control과 Batch Ownership

HTTP, Attachment, Channel, Fixed-length, SFTP, Messaging, Observability, Persistence, Secret, Security Runtime Applier를 각 Owner Starter로 이관했다. `runtime-control-client`의 선택 구현 역참조와 프로젝트 순환을 자동 그래프 Gate로 차단한다.

`cpf-batch`의 contract/testkit에는 Runtime Profile을 주입하지 않고 control-server/scheduler/worker 실행 모듈에만 필요한 Profile을 선언했다.

### 4.7 Build·CI·Repository Hygiene

- 최종 Workflow에 QA39 canonical closure와 Provider conformance를 Java 25·Frontend·DB·Supply-chain Gate 앞에 연결했다.
- Starter 신규 등록은 Value Contract, 편의 API, 고객 SPI 또는 명시적 N/A, 실제 Consumer, 제거 대안 비교, Footprint, Runtime Evidence 계획 없이는 실패한다.
- Requirement 완료와 Scenario 완료는 Evidence가 없으면 실패하는 False Complete Gate를 추가했다.
- 과거 QA37 전용 Workflow와 대체 문서는 exact-path 삭제 대상으로 관리한다.

## 5. Requirement·Scenario 상태

- Requirement: 44건
  - development_status `완료`: 44
  - verification_status `완료`: 17
  - verification_status `재확인 필요`: 20
  - verification_status `미검증`: 7
- Scenario: 37건
  - `완료`: 10
  - `재확인 필요`: 14
  - `미검증`: 13

`재확인 필요`와 `미검증`은 Source 미구현이 아니라 사용자 적용, Java 25 Fresh Build, 실제 DB/Broker/SFTP/SMTP/OTLP, 다중 인스턴스, Frontend/Playwright, Supply-chain 또는 GitHub Branch Protection 환경 검증이다. 세부 상태와 Evidence는 `CPF_REQUIREMENT_MATRIX.csv`와 `CPF_SCENARIO_MATRIX.csv`가 정본이다.

## 6. 수행 검증

현재 실행 환경: Linux, OpenJDK/Javac 21.0.10, Python 3.13 계열. PowerShell, Gradle 전체 Repository checkout, Java 25와 외부 Runtime은 제공되지 않았다.

실행 및 결과:

- `python3 cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py`
  - PASS: Profiles 6, Groups 7, Catalog modules 37, delete paths 135, Generated Domain 1, Requirements 44, Scenarios 37
- `python3 cpf-tools/verification/qa39/verify-cpf-provider-conformance.py`
  - PASS: 고객 Broker·Notification Provider Public SPI compile/runtime fixture
- `python3 -m py_compile cpf-tools/verification/qa39/*.py`
  - PASS
- JSON/CSV parser, protected path, secret literal, trailing whitespace Gate
  - PASS: protected 0, secret candidate 0, trailing whitespace 0
- Targeted source/harness 검증
  - RabbitMQ explicit import/named binding compile 경계 PASS
  - Messaging idempotency/lease/UNKNOWN/reconcile Source scenarios PASS
  - Notification lease/UNKNOWN/reprocess Source scenarios PASS
  - SFTP root/symlink/resume/UNKNOWN path scenarios PASS
  - Runtime Control dependency cycle/reverse import Gate PASS
  - Oracle/PostgreSQL/MariaDB QA39 SQL semantic parity PASS

실행하지 못한 검증:

- Java 25 Fresh Gradle `clean test assemble qualityGate`
- ADM/BZA `npm ci`, lint, typecheck, unit, production build, Playwright 3 browser
- Oracle/PostgreSQL/MariaDB 실제 empty schema Fresh/Upgrade/Rollback/Reapply
- Kafka/RabbitMQ/JMS/IBM MQ/TCP/SFTP/SMTP/OTLP 실제 Runtime과 Process Kill·ACK loss·다중 인스턴스
- SBOM/License/Vulnerability/Publication
- GitHub Branch Protection required-check 설정과 exact post-push Workflow 결과

미실행 항목을 성공으로 기록하지 않았다.

## 7. 개발자 독립 자체 리뷰

- QA 우선순위 위반: 없음
- 보호 경로 변경: 없음
- Consumer 없는 신규 Public API: Provider conformance와 실제 Scheduler/Generated Consumer 연결로 차단
- 얇은 OSS Wrapper 신규 추가: 없음; 기존 제거 대상은 Delete Manifest에 포함
- Runtime Control God Starter/순환: Gate상 0건
- Profile와 Leaf 중복 Consumer: Gate상 0건
- 미선택 Capability Dependency/Bean/Config/SQL 전이: Generator Gate상 0건
- 승인 예외 미승인·만료·Version/Hash Drift 허용: Fail-closed 구현
- DB Vendor 범위: Oracle/PostgreSQL/MariaDB만 사용
- Stale QA 문서·Workflow: 통합 정본에 반영하고 exact 삭제 후보로 관리
- Secret 원문·로그 노출: 정적 탐지 0건, Health reason은 sanitized code 사용
- False Complete: Matrix Gate로 차단

남은 위험은 Source 미구현이 아니라 외부 환경과 사용자 승인 작업이다. 가장 먼저 확인할 것은 Delete 명령 적용 후 잔여참조 0건과 Java 25 Fresh Build다.

## 8. 삭제와 정리

- Product/obsolete file exact paths: `CPF_DELETE_MANIFEST.txt`
- 영향·대체·승인·실행 상태: `CPF_PRODUCT_DELETE_WORK_ITEMS.csv`
- 실행 명령: `CPF_DELETE_ONE_LINE.ps1.txt`
- execution_status: `NOT_EXECUTED`

명령은 Repository Root에서 Manifest에 적힌 파일만 `-LiteralPath`로 삭제하고, 보호 경로·Root·상위 경로·Wildcard를 거부한다. 마지막에 비어 있는 디렉터리만 하위부터 제거하며 파일이 남은 폴더는 실패시킨다.

보호 대상은 Source 정본, 6개 공개 Profile, 7개 Capability Group, 유지 Provider 구현, Generator 계약, Matrix/Evidence, 보호 경로다. 삭제 대상은 QA가 완전 제거 또는 공개면 대체로 확정한 Source/Test/Config/Profile/과거 Workflow·중복 QA 파일이다.

## 9. Codex 독립 검수 시작 순서

Codex는 기능을 재설계하지 않고 다음 순서로 한 번씩 검수한다.

1. 기준 SHA와 Working Tree, `CPF_PACKAGE_MANIFEST.json`, `CPF_FILES.sha256`
2. `CPF_REQUIREMENT_MATRIX.csv`와 `CPF_SCENARIO_MATRIX.csv`
3. 삭제 명령 적용 여부와 137개 exact path 부재·잔여참조 0건
4. QA39 canonical closure와 Provider conformance
5. Java 25 Fresh `clean test assemble qualityGate`
6. Generator minimal/selected/approved exception/expired/hash drift/upgrade scenarios
7. Frontend verify와 Playwright
8. DB 3 Vendor lifecycle
9. Messaging·Notification·SFTP·Batch 다중 인스턴스/Fault/Recovery
10. Supply-chain·Publication·Artifact Hash

실행 불가 시 명령, 환경, 오류, Exit Code를 기록하고 Source Defect와 Environment Blocker를 분리한다. 실제 삭제 미실행, JDK25 Build 실패, Provider 0/2 default 허용, 미승인 예외 허용, Matrix Evidence 누락 중 하나라도 있으면 완료 처리하지 않는다.

## 10. 패키지 구성

- Overlay 파일 수: `222`
- 신규로 분류한 파일 수: `84`
- 기존 정본·Source 갱신 파일 수: `138`
- 삭제 대상 exact path 수: `135`
- 새로 만든 독립 사람용 Markdown: `0`
- 새로 만든 기계 Consumer 파일: Provider conformance verifier, canonical closure verifier, Stage/Change/Hash/Package Manifest 및 정책 계약 파일
- 갱신한 사람용 정본: `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
- 통합한 과거 파일: QA39 current/review/handover/state/codex/manifest 중복본과 과거 QA37 Workflow
- 빈 폴더·임시 파일·Stale Evidence: ZIP에서 제외; 실제 Repository 잔여는 Delete 명령과 Codex 검수 대상

Package, Change, Hash, Delete Manifest가 이 문서의 수치와 일치해야 한다.
