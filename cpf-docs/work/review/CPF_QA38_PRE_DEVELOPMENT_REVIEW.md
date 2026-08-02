# CPF QA38 개발 전 통합 리뷰

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 기준 SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- 입력: POST-QA37 자체 개발 Backlog + QA38 Final Requirement/Scenario Matrix
- 보호 경로: `cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`

## 2. 통합 Root Cause 순서

1. Starter Build Foundation와 BOM/Artifact/Publication 정합성
2. `cpf-core`·`cpf-common` 선택 Runtime 추출과 Dual Primary 제거
3. Versioned Capability Profile·Named Binding·Resolved Starter Lock
4. ADM/BZA/Gateway/Batch/Reference/Member/Generated Domain 실제 Consumer 이관
5. 기존 Security/Kafka/Cache/Observability/Resilience/FeatureFlag/Secret 보완
6. Reliability JDBC → Kafka/RabbitMQ/JMS/IBM MQ
7. TCP/Fixed-length/ISO8583/SFTP/Notification 및 외부 Integration
8. Canonical/Generator → MariaDB/PostgreSQL/Oracle SQL Parity
9. 저비용 Gate → Java/Frontend/DB/Runtime/Fault/Browser/Supply-chain
10. Delete Manifest·빈 폴더·Evidence·Codex 독립 검수 봉인

## 3. 경계와 Owner

- `cpf-core`: Provider-neutral API/SPI/Model 및 순수 Java 계약만 유지
- `cpf-common`: 고객 업무 공통만 유지
- `cpf-starters/*`: 선택 기술 Runtime, AutoConfiguration, Properties, Health/Operations
- `cpf-tools/generator`: Capability Profile과 Provider Binding을 실제 Dependency/Manifest로 생성
- Consumer는 Aggregate/Profile을 선택하되 최종 Leaf Starter가 Build에 명시된다.

## 4. 주요 회귀 위험

- Core에서 파일만 삭제하고 AutoConfiguration imports·Test·Consumer를 남기는 컴파일 회귀
- 새 Starter와 Core가 동시에 Bean을 제공하는 Dual Primary
- 다중 Messaging Provider에서 unnamed/default Client 모호성
- Outbox claim lease·retry·DLQ·reconcile 경쟁 조건
- TCP write 후 response loss를 단순 실패로 재시도해 중복 거래 발생
- Vendor SQL을 먼저 수정해 Generator/Canonical과 Drift 발생
- Overlay가 타 GPT 보호 경로 또는 최신 Push를 덮어쓰는 문제

## 5. 완료 조건

- Interface나 빈 Starter가 아니라 Provider Runtime·Properties·AutoConfiguration·실제 Consumer·Test·Operations를 함께 제공
- 대체된 Core Source/Test/Resource는 exact Delete Manifest로 제거
- `settings.gradle`, BOM, Artifact Catalog, Generator Profile의 Module 집합 일치
- Oracle/PostgreSQL/MariaDB의 Table/Column/Index/State Contract 정적 Parity
- 실행하지 않은 외부 Runtime 검증은 PASS로 기록하지 않음
