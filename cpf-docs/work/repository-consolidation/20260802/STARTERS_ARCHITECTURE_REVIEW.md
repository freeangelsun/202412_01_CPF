# `cpf-starters` 정식 Architecture Review

## 1. 확인 사실

- 최초 추가: 2026-07-31 10:37:42 KST
- 최초 Commit: `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)
- 분석 기준 master: `1eda8e12fe123281748a4388938c62f11819da1e`
- 현재 `settings.gradle`은 7개 Starter Project를 `cpf-starters/**`에 매핑한다.
- Starter는 `java-library`·`maven-publish` 기반 독립 JAR로 만들어지고 이를 선택한 Product JAR/WAR에 포함된다.
- `cpf-admin`·`cpf-biz-admin`은 Security Starter를 소비한다.
- Gateway는 Resilience·Observability Starter를 소비한다.
- Batch Control Server·Scheduler·Worker는 Kafka·Observability 등을 소비한다.

## 2. 공식 결정

```text
cpf-starters root = 유지
분류 = FIXED_PRODUCT_CONTAINER
Architecture = Lightweight Core + Explicit Opt-in Starter
```

Starter 개념은 CPF의 선택 기술 Runtime을 Core에서 분리하고 Domain/Product가 필요한 기능만 사용하게 하는 구조에 부합한다.
폴더 전체 삭제 또는 Core/Common으로 일괄 복귀는 하지 않는다.

## 3. 현재 구조의 중요 결함

1. Final Root Gate의 고정 Root 목록에 `cpf-starters`가 빠져 Generated Domain으로 오판할 위험이 있다.
2. Platform BOM은 현재 Starter Artifact 제약을 충분히 포함하지 않는다.
3. Core에 MyBatis, AspectJ, Web/OpenAPI UI, OTel SDK·Exporter 등 선택 Runtime 성격 Dependency가 남아 있다.
4. Security Starter가 `/adm/**`, `/bza/**` 경로 정책까지 소유해 범용 기술과 Product 정책이 혼재한다.
5. Cache Starter가 `cpf-common`의 Runtime AutoConfiguration을 Import하고 Caffeine·Redis를 함께 API 전이한다.
6. Kafka Consumer가 Starter와 Spring Integration Kafka Dependency를 동시에 직접 선언해 역할 중복 가능성이 있다.
7. Feature Flag는 실제 Product Consumer·Provider·운영 감사가 확인되지 않았다.
8. Secret Starter는 Registry가 있으나 Provider·Rotation·Readiness·운영 Closure가 필요하다.
9. Generated Golden Domain과 Reference가 Starter 선택 조합을 충분히 대표하지 않는다.

## 4. 방향

- Core는 Public API/SPI와 최소 계약만 남기도록 경량화한다.
- 선택 Runtime 구현은 Starter 또는 실제 Owner로 이동한다.
- Generated Domain은 Generator에서 필요한 Starter만 명시적으로 선택한다.
- 현재 7개는 일괄 승인하지 않고 유지·세분화·이관·제거를 개별 판정한다.
- 새 Starter는 Consumer·실패 처리·Publication·BOM·Optional removal·Guide까지 완료될 때만 GA다.

## 5. RabbitMQ

현재 Kafka가 Primary이고 RabbitMQ 구현은 없다. 이는 현재 정본 위반은 아니지만 상용 Framework의 선택 Adapter 전략은 다음 QA에서 확정해야 한다.

- Kafka-only
- 공식 `cpf-starter-messaging-rabbitmq`
- Customer Plugin SPI

RabbitMQ를 채택하면 Kafka 수준의 ACK·DLQ·중복·결과불명·재처리·운영 검증을 요구한다.

## 6. 상태

```text
development_status = 부분 구현
verification_status = 재확인 필요
next_action = Core 경량화·Starter 세분화 전수 QA
```

## 그룹 등록 방식 재검토

기존 검토는 Leaf Starter의 개별 선택을 중심으로 했으며 그룹 등록 모델은 충분히 정의하지 않았다.
보완 판정:

- 신규 Domain: Generator Capability Profile 권장
- Profile은 최종 Leaf Dependency로 확장하고 Manifest에 기록
- 외부 Consumer: 필요성이 입증되면 Aggregate Starter 선택 제공
- BOM: Version 정렬 전용
- Mega Starter: 금지
- 상호 배타 Provider: 생성/Build 단계 fail-closed
