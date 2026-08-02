# CPF 다음 QA 요청서 — Core 경량화와 Starter 전수 세분화

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- 요청 작성 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 실제 착수 기준: 착수 시 최신 `origin/master` exact SHA로 재확인
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Architecture 정책: `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`

## 2. 목표

`cpf-core`를 topology-independent 계약 중심으로 경량화한다.
Web, Security, Session, Messaging, Cache, Observability, Resilience, OpenAPI, Persistence 등 선택 Runtime은 Starter 또는 실제 Owner로 분리한다.
Generated Domain과 Product는 필요한 Starter만 명시적으로 선택해 JAR/WAR에 포함한다.

현재 7개 Starter만 다듬는 작업이 아니다. CPF 전체 Source·Build·Dependency·Config·SQL·Test·Generator·Reference·Guide·Deliverable을 대상으로 Starter 적합성을 전수 판정한다.

## 3. 작업 전 필수 리뷰

개발 전에 다음 파일을 작성하고 기준선으로 고정한다.

1. `PRE_REVIEW.md`
2. `FRAMEWORK_CAPABILITY_OWNERSHIP.csv`
3. `CORE_RUNTIME_DEPENDENCY_INVENTORY.csv`
4. `STARTER_DEPENDENCY_AND_CONSUMER_GRAPH.csv`
5. `STARTER_MIGRATION_PLAN.csv`
6. `SELF_DEVELOPMENT_REQUIREMENTS.csv`

각 Capability를 다음 중 하나로 분류한다.

```text
CORE_CONTRACT
COMMON_BUSINESS
PRODUCT_OWNER
STARTER_TECH_ADAPTER
CUSTOMER_PLUGIN
GENERATOR_TOOL
REFERENCE_ONLY
REMOVE_CANDIDATE
```

## 4. 개발 범위

### 4.1 Core 경량화

- Core Public API/SPI가 실제로 필요한 외부 Type만 남긴다.
- MyBatis, AspectJ, Servlet/WebMVC, OpenAPI UI, OTel SDK/Exporter, HTTP Client 등 선택 Runtime을 전수 분석한다.
- 이관 시 Consumer·Config·Test·JavaDoc와 기존 Primary 제거를 함께 처리한다.

### 4.2 현재 Starter

Security, Messaging Kafka, Cache, Observability, Resilience, Feature Flag, Secret을 정상·오류·경계·부분 실패·다중 인스턴스·운영·보안·배포까지 검수한다.

### 4.3 Starter 신규 후보

Web MVC, OpenAPI, HTTP Client, Persistence MyBatis, Security Resource Server, JDBC Idempotency/Outbox/Inbox 등을 검토한다.
후보라는 이유만으로 모듈을 만들지 말고 Consumer와 선택성이 입증된 경우에만 구현한다.

### 4.4 RabbitMQ

Kafka Primary를 유지하면서 다음 중 하나를 Architecture Decision으로 확정한다.

- Kafka-only
- 공식 RabbitMQ Starter
- Customer Plugin SPI

공식 채택 시 Kafka와 동등한 ACK·DLQ·Retry·Duplicate·Ordering·Unknown Result·Reconcile·운영 검증을 제공한다.

### 4.5 Generator와 Domain

- 최소 Domain Profile에는 불필요한 Starter를 넣지 않는다.
- Capability 선택을 Manifest·Build·Config·Test·Guide에 원자 반영한다.
- 조합 충돌과 미지원 Provider를 생성 전에 fail-closed 한다.
- Golden Domain 한 개만으로 완료 처리하지 않고 임의 Domain·복수 조합을 검증한다.

### 4.6 Publication과 배포

- 각 GA Starter 독립 JAR/POM/Sources/JavaDoc
- Platform BOM·Version Manifest·SBOM·Provenance
- LOCAL_DEV/REMOTE/OFFLINE Fresh Clone 소비
- JAR/WAR 포함과 미선택 Starter 제외
- Mixed Version, Upgrade, Rollback

### 4.7 Starter 선택·Profile·Bundle

Domain이 모든 Starter를 반드시 개별 등록하도록 고정하지 않는다.
다음 네 계층을 분리하여 구현 여부를 결정한다.

- Leaf Starter: 실제 Adapter·AutoConfiguration 구현 정본
- Capability Profile: Generator/Build Convention이 Leaf Starter를 명시적으로 확장
- Aggregate Starter: 필요성이 입증된 안정 조합을 한 Dependency로 제공
- Platform BOM: Version 정렬만 수행하고 Runtime을 활성화하지 않음

필수 요구:

1. Generator Profile을 선택해도 생성된 Build와 Manifest에는 최종 Leaf Starter 목록이 기록된다.
2. Profile Version과 Resolved Starter Lock을 저장한다.
3. Aggregate Starter는 고유 Source·Bean·AutoConfiguration을 갖지 않고 Leaf Dependency만 선언한다.
4. 개별 Leaf Starter 선택은 항상 가능해야 한다.
5. Messaging·Cache·Security·Persistence의 상호 배타 Provider 충돌을 fail-closed 한다.
6. `all/full/everything` Mega Starter를 만들지 않는다.
7. Product 고유 조합은 범용 Starter가 아니라 Product Build/Convention Owner가 관리한다.
8. Profile Catalog·Generator·Aggregate POM·BOM·Guide·Manifest Drift를 Gate로 차단한다.

### 4.8 Core 독립 계약과 Base Starter

- `cpf-core`는 독립 계약 Artifact로 유지하고 Starter 내부로 흡수하지 않는다.
- 비 Spring Consumer가 Core만으로 Fresh Compile·Test·Publication 소비 가능해야 한다.
- `cpf-starter-base` 도입 여부와 Artifact 이름을 ADR로 확정한다.
- Base Starter는 Core + 최소 Boot 조립만 소유한다.
- Base에 Web·DB·Messaging·Cache·Session·OpenAPI·Exporter·Batch를 넣지 않는다.
- 일반 Boot Domain과 계약 전용 Consumer의 Dependency 모델을 Generator Profile로 구분한다.
- `cpf-common`을 Base 또는 모든 Domain에 강제하지 않는다.

## 5. 이관 원칙

1. Target 구현과 실제 Consumer를 먼저 완성한다.
2. 정상·오류·경계·부분 실패를 검증한다.
3. Generator·Reference·Guide를 이관한다.
4. 기존 구현과 Dependency의 Dual Primary를 제거한다.
5. Optional removal과 Fresh Clone을 통과한다.
6. 직접 실행하지 않은 항목은 `미검증`이다.

## 6. 가이드·산출물

가이드와 Deliverable 작업은 `GUIDE_AND_DELIVERABLES_STARTER_UPDATE_REQUEST.md`를 따른다.
Starter 구조가 세분화되면 기존 문구를 누적하지 말고 역할별 정본을 갱신한다.

## 7. 필수 개발요건

`NEXT_QA_CORE_LIGHTWEIGHT_STARTER_REQUIREMENTS.csv`의 전 항목을 기준으로 수행한다.
P0 미완료·실패·미검증이 존재하면 전체 완료가 아니다.

## 8. 결과물

Repository Root Overlay ZIP으로 다음을 제공한다.

- 작업 전 리뷰·Capability/Dependency/Consumer Inventory
- 자체 개발요건
- Source·Build·Config·Test·Generator 변경
- Guide·Deliverable 변경
- 작업 후 독립 리뷰
- Codex 검수 패키지
- exact-SHA Evidence·Artifact Hash
- Delete Manifest와 안전한 PowerShell 한 줄 명령

Commit·Push·삭제는 사용자 승인 없이 수행하지 않는다.
