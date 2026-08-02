# CPF QA39 Post-Development Independent Review of QA38 Result

## 1. 결론

개발 GPT의 QA38 결과는 전체 완료가 아니다.

- `development_status = 부분 구현`
- `verification_status = 실패`
- 실제 Runtime/DB/Frontend/Supply-chain = 미검증

QA38 문서의 `개발 완료 156/156`은 현재 Source와 일치하지 않으므로 폐기해야 한다.

## 2. 전체 완료가 아닌 이유

### 2.1 Build 자체가 보장되지 않음

RabbitMQ AutoConfiguration은 두 wildcard import가 동시에 `Queue`를 제공하는데 단순명 `Queue`를 사용한다. fresh Java compile 전에 제거돼야 하는 정적 compile blocker다.

### 2.2 제품 Build graph 밖 Source 존재

FTPS, gRPC, S3, Realtime WebFlux, SMB, SOAP, Webhook Source가 추가됐지만 `settings.gradle` 공식 project mapping에 없다. 이런 Source는 컴파일·시험·게시·BOM·Consumer가 없으므로 제품 구현으로 인정할 수 없다.

### 2.3 Starter 선택면 과세분화

공식 Starter 49개와 미등록 Integration Source가 한 폴더에 혼재한다. 개발자가 사용할 Capability와 내부 Provider Leaf가 분리되지 않았으며, Security/Cache Aggregate는 선택하지 않은 Provider까지 끌어온다.

### 2.4 OSS 대비 CPF 가치가 부족한 Starter 존재

AOP service-access는 stack trace 문자열 검사, Validation은 Validator bean 하나, Resilience는 circuit breaker 한 메서드, Feature Flag는 OpenFeature boolean 호출 wrapper에 가깝다. 이들은 프레임워크가 무거워지는 비용을 정당화할 제품 가치가 없다.

### 2.5 Messaging 다중 Provider 정책 불완전

Kafka Binding은 설정값을 사용하지 않고 항상 Kafka를 default로 등록한다. RabbitMQ는 compile blocker가 있고 JMS/IBM MQ는 실제 runtime/transaction/ack/reconcile Evidence가 없다. Provider-neutral API 의도는 있으나 실제 교체 가능성은 검증되지 않았다.

### 2.6 Reliability Consumer 연결 불완전

Messaging outbox worker, Notification worker는 클래스/Bean이 있지만 실제 Scheduler/Owner/운영 API 연결이 불완전하다. Notification은 expired lease를 회수하지 않으며 UNKNOWN 결과를 completed/sent로 처리할 수 있다.

### 2.7 Quartz Cluster 미성립

`clustered=true` 설정만 있고 JDBC JobStore, Vendor schema, multi-instance failover Evidence가 없다.

### 2.8 Consumer 의존성 구조 불량

Batch는 모든 하위 프로젝트에 Kafka와 Scheduled Profile을 강제한다. ADM/BZA/Gateway 등은 Profile가 포함한 Leaf를 다시 직접 참조한다. 선택성, 최소 footprint, Ownership 원칙과 맞지 않는다.

### 2.9 정본과 Evidence 불일치

Current는 부분 구현/미검증인데 Continuity와 Handover는 156/156 개발 완료를 선언한다. Package Manifest는 과거 SHA를 기준으로 하며 Hash Manifest가 존재하지 않는 Evidence를 참조한다.

### 2.10 독립 실행 근거 없음

현재 Commit에 GitHub Actions status/workflow run이 확인되지 않았다. Java25 전체 Gradle build, 3 Vendor DB, 외부 Provider Runtime, Frontend, Fault, Browser, Supply-chain은 실행되지 않았다.

## 3. 왜 이런 미완료가 남았는가

1. Requirement를 실제 실행 흐름보다 파일/클래스 생성 단위로 닫았다.
2. Starter 수와 Source 양을 기능 완성도로 오인했다.
3. 공개 사용자 경험보다 내부 세분화를 우선했다.
4. 각 Starter의 “OSS 직접 사용 대비 가치” Gate가 없었다.
5. Consumer와 Runtime Evidence 전에 Matrix를 일괄 완료로 상향했다.
6. Catalog/BOM/State/Handover를 단일 생성 정본으로 관리하지 않았다.
7. CI가 required gate가 아니어서 compile/packaging/evidence 결함이 master에 들어왔다.

## 4. 다음에 미완료를 남기지 않는 방법

- Starter 추가 금지 기간을 두고 기존 49개를 Value Gate로 재판정한다.
- 공개 Surface를 6개 Profile과 7개 Capability Group로 축소한다.
- Value가 없는 Starter는 호환성/교체비용을 고려하지 않고 제거한다.
- Provider Leaf는 내부 Artifact로 숨기고 Generator가 resolved lock을 만든다.
- Requirement 완료 전에 실제 Consumer와 운영 API를 연결한다.
- P0 Stage는 fresh compile, Consumer graph, Catalog parity, Evidence integrity를 통과해야 다음 Stage로 진행한다.
- Runtime/DB/Fault Evidence 전에는 verification_status를 완료로 올리지 않는다.
- Current/Matrix/State/Handover/Codex는 exact SHA 하나에서 자동 생성한다.
- 신규 Starter PR은 Value Contract와 제거 대안 비교가 없으면 Catalog 등록을 막는다.

## 5. 보호한 범위

QA39 Overlay는 보호 경로 4개를 변경하지 않는다. Commit, Push, 삭제도 수행하지 않는다.

## 6. 상세 자료

- `CPF_QA39_SOURCE_REVIEW_FINDINGS.csv`
- `CPF_QA39_STARTER_ARCHITECTURE_AND_VALUE_REVIEW.md`
- `CPF_QA39_STARTER_VALUE_CATALOG.csv`
- `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`

## 7. `6a9890ef19ae54e6e3186ca011d5d7f984d49d9c` 상충 요청 및 `9a9634eb1f28071d47c205cc35227b6d013a4536` 최신 기준 검토

`6a9890ef19ae54e6e3186ca011d5d7f984d49d9c` Commit은 QA39 통합 요청과 자체요건을 추가했으나 미등록 7개 Integration 모듈을 제품화하고 Resilience/Feature Flag를 보호하도록 지시한다. 이는 사용자와 QA가 확정한 완전 제거 결정과 충돌한다. 따라서 이번 R3 Overlay가 해당 요청·자체요건·Continuity를 대체하며, QA 최종요건 우선 규칙을 추가했다.

또한 개발 GPT의 상세 Implementation Report와 독립 Self Review를 완료 조건으로 추가했다. QA는 이를 그대로 승인하지 않지만 변경 범위와 실행 결과를 빠르게 확인하는 검수 인덱스로 사용한다.
