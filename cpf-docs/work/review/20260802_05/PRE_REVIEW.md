# POST-QA37 통합 작업 사전 리뷰

## 기준

- Remote `master`: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- 검토 입력: 최신 Git commits, canonical governance, QA37 Codex history, actual source/build/config paths
- 작업 유형: Source 수정 전 통합 Requirement·Architecture·Verification 재정렬
- Git write: 수행하지 않음

## 해결할 Requirement·Defect

- `ARCH-STARTER`: Core 경량화와 Starter 계층 미완결
- `DB-FRESH`: 실제 3 Vendor Fresh lifecycle 미검증
- `EVENT-MQ`, `EVENT-JMS`, `EVENT-IBM-MQ`, `EVENT-AMQP`, `EXS-TCP`: 정본 및 구현 누락
- QA37 exact-SHA verification gap
- Current/Continuity 문서의 stale baseline
- Date-stamped work package와 현행 정본 중복
- Generator/Domain이 Starter Profile을 실제 소비하지 않는 구조

## Owner와 경계

- `cpf-core`: provider-neutral API/SPI/model only
- `cpf-starters`: reusable technical runtime and AutoConfiguration
- `cpf-common`: customer business common, not technical dumping ground
- `cpf-admin`, `cpf-biz-admin`, `cpf-batch`, `cpf-gateway`: product-specific policy/runtime
- Generated Domain: selected Starter consumer and customer adapter owner
- `cpf-tools`: generator, DB canonical artifacts, build/release/verification

## 회귀 위험

- Core package relocation breaks published API and every consumer.
- AutoConfiguration migration can create dual primary or missing beans.
- Starter transitivity can silently pull Web/DB/Security.
- Messaging provider semantics differ for transaction/ack/order/redelivery.
- DB lifecycle changes can alter historical migration checksums.
- Existing QA37 PASS is not exact latest SHA.

## 구현 순서

1. Canonical requirement recovery and ownership.
2. Dependency/consumer inventory and split plan.
3. Leaf Starter migration with compatibility facade only where required.
4. Generator Profile and Domain lock.
5. Messaging/TCP provider vertical slices.
6. Canonical-first DB lifecycle tooling.
7. Focused tests, then one integrated lifecycle per stage.
8. Current/History cleanup and exact-SHA evidence.

## 보호 대상

- Existing successful SCG primary, Spring Batch primary, EDU135, generated-domain 2-table contract.
- Official DB vendors only Oracle/PostgreSQL/MariaDB.
- Historical migration content/checksum.
- User databases, Docker images/containers/volumes/secrets.
- README, guides, deliverables and manuals unless directly in approved scope.
