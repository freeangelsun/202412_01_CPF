# CPF QA39 Starter Architecture and Product Value Review

## 1. 판정 기준

교체 비용과 기존 구현량은 고려하지 않는다. 현재 제품 가치만 본다.

각 Starter는 다음 중 하나 이상을 실제로 제공해야 한다.

1. OSS 타입을 숨기는 안정적 CPF Public API/SPI
2. Provider 교체를 가능하게 하는 Binding/Port
3. 거래 Header, Context, 멱등성, Outbox, Reconcile
4. 보안, 권한, 감사, 마스킹, Secret 연계
5. 다중 인스턴스, Process Kill, 부분 실패, 결과불명 처리
6. 운영 조회, 승인, 재처리, 상태 추적
7. Generator와 실제 Consumer의 자동 조립
8. 검증 가능한 Runtime Evidence

단순 dependency 추가, properties mapping, Bean 하나, wrapper 메서드 하나는 가치로 인정하지 않는다.

## 2. 수량 판정

- 공식 Starter project: 49개
- Leaf/Aggregate: 36개
- Profile: 13개
- 추가 내부 library/SPI: 2개
- settings 미등록 Integration Source: 7개

사용자가 보는 물리 단위는 지나치게 많고 공개 Capability와 내부 Provider가 뒤섞여 있다.

## 3. 구조 결론

### 유지할 개념

- Data
- Messaging
- Integration
- File
- Notification
- Security
- Platform Operations

### 내부 Leaf로 숨길 대상

- JDBC/MyBatis
- Caffeine/Valkey
- Kafka/RabbitMQ/JMS/IBM MQ
- OTLP exporter
- SFTP provider
- Fixed-length/ISO8583 codec
- Email/SMS provider
- Session JDBC/Resource Server/Service Identity
- Quartz provider가 실제 가치를 충족하는 경우

### 제거 대상

- AOP Service Access
- Validation
- Resilience
- Feature Flag
- 현재 Security Aggregate
- 현재 Cache Aggregate
- 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook 모듈
- 실제 JDBC Cluster 가치를 제공하지 못하는 Quartz
- exporter 조립 외 가치가 없는 OTLP 공개 Starter

## 4. Messaging의 존재 이유

Messaging Starter가 의미 있으려면 개발자는 `KafkaTemplate`, `RabbitTemplate`, `JmsTemplate`을 직접 사용하지 않고 CPF API만 사용해야 한다.

필수 CPF 가치:

- 동일 Publish/Consume API
- Named Binding과 Default fail-closed
- 표준 transaction/idempotency/header propagation
- Outbox/Inbox와 exactly-once가 아닌 명확한 delivery semantics
- ACK/NACK/timeout을 SUCCESS/FAILED/UNKNOWN으로 분리
- Reconcile 전에 무조건 재시도하지 않음
- Provider별 운영 상태, DLQ, replay, 승인
- Customer Provider Plugin SPI
- Generator가 Provider를 lock하고 업무 Source는 변경하지 않음

현재 코드는 이 방향의 일부 요소를 갖지만 전체 연결과 Runtime Evidence가 없어 부분 구현이다.

## 5. 공개 Surface 제안

일반 개발자는 개별 Leaf 이름이 아니라 다음만 선택한다.

- Profile: minimal-domain, web-api, secure-api, browser-bff, event-service, batch-service
- Capability: persistence, cache, messaging, file-processing, file-exchange, transaction-integration, notification, observability, secret, platform-operations

Provider는 `messaging=kafka`, `cache=valkey`, `persistence=mybatis` 같은 binding으로 선택한다. Generator가 내부 Artifact와 version lock을 결정한다.

## 6. 세분화 장단점 판정

Leaf 분리는 Provider 독립 배포와 optional dependency에 유리하다. 그러나 현재는 Leaf가 사용자 공개 목록에 그대로 노출되고 Aggregate가 상호 배타 Provider를 다시 묶어 장점이 상쇄됐다.

따라서 물리 Artifact 분리는 내부적으로 유지할 수 있으나 공개 선택면은 축소해야 한다. “폴더가 많다”보다 “개발자가 무엇을 선택해야 하는지 알 수 없다”가 현재 핵심 결함이다.

## 7. 상세 행별 판정

`cpf-docs/quality/CPF_QA39_STARTER_VALUE_CATALOG.csv`를 정본으로 사용한다.

## 8. Final rule

유지 Group은 업무 개발자가 사용하는 편의 Public API와 고객 Provider SPI를 제공해야 한다. OSS API를 그대로 한 번 호출하는 Wrapper는 유지하지 않는다. QA 개발요건과 자체요건이 충돌하면 QA 개발요건이 우선한다.
