# CPF 최상위 목표 정본 Starter 반영 제안

> 대상 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`  
> 이 파일은 정본 대체본이 아니라 다음 QA에서 Requirement Continuity를 보존해 반영할 변경 제안이다.

## 현재 반영 상태

- `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`: Overlay에 정식 정책으로 포함
- `CPF_REPOSITORY_SURFACE_INDEX.md`: Starter Container 역할 반영
- 다음 QA Request·Requirement·Guide 초안: 반영
- **`CPF_FINAL_TARGET_REQUIREMENTS.md` 원본: 이번 Overlay에서 직접 수정하지 않음**
- 기존 `cpf-docs/guides/**`와 Deliverable 원본: 이번 Overlay에서 직접 수정하지 않음

최상위 목표 정본과 역할별 Guide는 Starter Source·BOM·Generator·Gate가 실제로 변경되는 다음 QA의 동일 Commit에서 이 제안을 반영해야 한다.
따라서 현재 상태는 `development_status = 부분 구현`, `verification_status = 미검증`이다.

## 반영할 Architecture 원칙

- `cpf-core`는 topology-independent Public API/SPI와 최소 기술 계약만 소유한다.
- Web, Security, Session, Messaging, Cache, Observability, Resilience, OpenAPI, Persistence 등 선택 Runtime은 Starter 또는 실제 Owner로 분리한다.
- `cpf-starters`는 공식 `FIXED_PRODUCT_CONTAINER`다.
- Generated Domain과 Product Runtime은 필요한 Starter만 명시적으로 선택한다.
- 미선택 Starter의 JAR·Bean·Config·SQL·전이 Dependency를 포함하지 않는다.

## 공식 Module·Ownership 표 반영

`cpf-starters`는 독립 업무 SystemCode를 가진 실행 제품이 아니라, 하위 독립 Library Artifact를 관리하는 Container로 설명한다.

```text
cpf-starters
  Owner: Starter Platform
  Responsibility: 선택 기술 Adapter, AutoConfiguration, Properties, Provider Runtime, Publication
  Consumer: ADM/BZA/Gateway/Batch/Generated Domain/Reference 중 명시적으로 선택한 Runtime
```

## 의존 방향 반영

```text
Generated Domain / Product -> 선택 cpf-starter-* -> cpf-core Public API/SPI
cpf-starter-* -> 외부 기술 Library
```

금지:

- `cpf-core`의 선택 Starter·선택 OSS Runtime 강제 의존
- Starter의 Product/Generated Domain 역방향 의존
- Product 고유 업무·관리·Batch·Gateway 정책의 Starter 소유
- 미선택 Starter의 Artifact·Config·Bean 포함

## 기술 Stack 표 반영

현재 Kafka Primary를 유지하되 Provider Runtime은 `cpf-starter-messaging-kafka`가 소유한다.
RabbitMQ는 Kafka-only, 공식 선택 Starter, Customer Plugin 중 하나를 다음 QA에서 결정한다.
Cache·Security·Observability·Resilience·Feature Flag·Secret도 Core 강제 Runtime이 아닌 선택 Starter로 설명한다.

## Core·Base Starter 최종 반영안

최상위 목표 정본에는 다음 원칙을 반영한다.

1. `cpf-core`는 topology-independent 독립 초경량 계약 JAR로 유지한다.
2. Core는 Spring Boot·Web·DB·Broker·Cache·OpenAPI·Exporter Runtime을 강제 전이하지 않는다.
3. 일반 CPF Boot Runtime의 최소 진입점으로 `cpf-starter-base`를 검토한다.
4. Base Starter는 Core와 최소 Boot 조립만 제공하며 선택 기능을 포함하지 않는다.
5. 비 Spring Consumer·고객 SPI·계약 전용 Module은 Core를 직접 소비할 수 있다.
6. `cpf-common`은 고객 업무 공통이며 Starter 또는 모든 Domain의 필수 기반으로 간주하지 않는다.
7. Generator는 `MINIMAL_CONTRACT_CONSUMER`와 `MINIMAL_BOOT_DOMAIN`을 구분한다.
8. Base/Leaf/Profile/BOM의 해석 결과를 Domain Manifest에 기록한다.

## Starter 선택 편의 계층 반영

Domain이 모든 Starter를 항상 하나씩 직접 선언하도록 강제하지 않는다.
다음 계층을 최상위 목표에 구분해 반영한다.

```text
Leaf Starter
  실제 기술 Adapter·AutoConfiguration의 구현 정본

Capability Profile
  Generator/Build Convention이 사용 사례별 Leaf Starter를 명시적으로 확장

Aggregate Starter
  승인된 안정 조합을 하나의 Dependency로 전이하는 선택 편의 Artifact

Platform BOM
  Version·Compatibility 정렬만 수행하며 Runtime을 활성화하지 않음
```

필수 원칙:

- Leaf Starter는 항상 독립적으로 선택 가능해야 한다.
- Profile 적용 결과는 Domain Manifest와 Build에 최종 Leaf 목록으로 기록한다.
- Profile Version과 Resolved Starter Lock을 보존한다.
- Aggregate Starter는 구현·Bean을 소유하지 않고 승인된 Leaf Dependency만 선언한다.
- Kafka/RabbitMQ, Caffeine/Redis, Session/Resource Server 같은 상호 배타 Provider를 한 묶음에 넣지 않는다.
- `all` 또는 Mega Starter는 금지한다.
- 미선택 Runtime의 JAR·Config·Bean은 최종 Artifact에 포함하지 않는다.

## Generator 반영

- 최소 Domain Profile
- Capability별 Starter 명시 선택
- Domain Manifest에 선택 목록과 Version/Provider 기록
- 미지원 조합 fail-closed
- Build·Config·Test·Guide 원자 생성
- 사용자 수정 영역 보호

## 완료 축 반영

Starter Requirement는 다음을 모두 충족해야 한다.

- 독립 JAR/POM/Sources/JavaDoc
- 실제 Product Consumer
- Optional 제거 Compile·Runtime
- JAR/WAR 포함·미포함 검증
- BOM·SBOM·Version·Compatibility
- 정상·오류·경계·부분 실패·다중 인스턴스·복구
- Generator·Reference·Guide·Deliverable
- exact-SHA Evidence

## Continuity 처리

기존 Requirement ID를 임의로 변경하거나 새 번호를 추측하지 않는다.
다음 QA 착수 시 최신 정본의 관련 Requirement를 찾아 분해·확장하고 Continuity Ledger에 이동·통합 근거를 남긴다.
