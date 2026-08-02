# CPF QA38 Starter 독립 리뷰

## 결론

QA38 이전 구조는 `cpf-core`와 `cpf-common`이 선택 기술 Runtime을 직접 소유하고 일부 Starter가 같은 Capability를 다시 제공하는 Dual Primary 상태였다. QA38 Overlay는 계약과 Runtime을 분리하고 실제 Consumer·Generator·Artifact까지 연결했다.

## Core에 유지한 경계

- Identifier, Header, Transaction Context
- Error, Validation 값 계약
- Provider-neutral Broker/File/TCP/Remote API·SPI·Model
- Security·Masking·Audit 계약
- 순수 Java Fixed-length와 Vendor-neutral SQL Catalog 계약

## Starter로 이동한 Runtime

- DataSource/JDBC, MyBatis, Read/Write Routing, Connection Pool Runtime
- AspectJ Service Access와 Logging Aspect
- OpenAPI WebMVC
- Resource Server, JDBC Session, Service Identity
- HTTP/Service Call/Endpoint Registry
- Broker Reliability JDBC와 Provider Adapter
- JDBC Channel Registry
- Redis/Valkey, Caffeine, POI/XLSX, Validation Provider
- OTel/Remote Log/Logging Runtime
- Archive, Attachment, SFTP/File Transfer
- Runtime Control Agent/Plane/Applier
- TCP, ISO8583, Notification, Quartz

## Aggregate/Profile 검토

- Aggregate/Profile은 전이 Dependency와 Version Lock만 제공한다.
- 자체 Java Source, Bean, AutoConfiguration, 업무 정책을 포함하지 않는다.
- 13개 Profile은 승인된 Leaf Starter 목록과 Provider Binding을 명시한다.
- Multi-provider Messaging은 Named Binding을 사용하고 Default는 최대 1개다.

## Consumer Closure

- ADM, BZA, Gateway, Batch, Reference, Member Build가 Profile 또는 Leaf Starter를 실제 소비한다.
- Generator는 `resolvedStarters`, Profile Version, Starter Version Lock을 산출한다.
- Internal package나 Provider SDK를 업무 Domain에서 직접 참조하는 경로를 Gate 대상으로 등록했다.

## 독립 검증 결과

- 구조/Project/Artifact/Profile Gate PASS
- SQL 3 Vendor semantic parity PASS
- Java duplicate member Gate PASS
- Pure Runtime Harness 33개 PASS
- Messaging/Service Identity Harness PASS
- 실제 Java25 전체 Build·DB·Broker·Browser는 환경 검증 잔여이며 PASS로 선언하지 않는다.
