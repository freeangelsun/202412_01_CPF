# 과거 사용자 요구 복구 리뷰

## 발견

최신 Final Target과 Continuity Ledger에는 Kafka 중심 Event 요구가 있었지만 MQ, JMS, IBM MQ, RabbitMQ, TCP가 없었다.
Starter 정책에는 RabbitMQ가 '결정 필요'로만 남았고 JMS/IBM MQ/TCP는 정본화되지 않았다.
검토한 QA37 이전 작업 문서와 Customer Manual EDU에서도 해당 용어를 찾지 못했다.

따라서 과거 어느 문서에서 삭제됐는지까지는 현재 Repository 자료만으로 확정할 수 없다. 하지만 사용자가 다시 명시한 요구가 Final Target에 흡수되지 않은 Requirement Continuity 결함은 명확하다.

## 복구

- `EVENT-MQ`
- `EVENT-JMS`
- `EVENT-IBM-MQ`
- `EVENT-AMQP`
- `EXS-TCP`
- 검색 Alias: MQ, JMS, IBM MQ, RabbitMQ, TCP, 원문 TPC

## 구현 경계

- Core: Envelope, Port, Error/Unknown-result, Idempotency contract
- Messaging reliability JDBC Starter: outbox/inbox/DLQ/replay worker and ledger
- JMS Starter: provider-neutral Jakarta JMS
- IBM MQ Starter: JMS provider extension
- RabbitMQ Starter: AMQP provider
- TCP Starter: persistent connection runtime
- Generated/customer Domain: destination/layout/mapping/business handler

## 완료 금지

Dependency나 AutoConfiguration만 추가하고 완료 처리하지 않는다.
Actual provider, duplicate/order/outage/recovery/multi-instance, security and operation evidence가 필요하다.
