# CPF Current Handover

> Currentization basis: `b2da6bd720d1a8506db6bddf5d2e35feb9dca964` (`07_15`)  
> 이전 Developer 실행 기준: `9f16468cccae71523f65f0aefcd94322788c4dd0`  
> 다음 실행자는 최신 `origin/master`, exact SHA, Working Tree부터 다시 확인한다.

## 1. 직전 개발 반영 상태

`07_15` commit에서 세션 17 변경이 실제 master에 반영된 것을 확인했다.

직전 개발 보고:

- Core Hardening 180/180 직접검수
- Fundamental Baseline 240/240
- Persistence 35/35
- 발견 Gap 21 / 수정 21
- Developer-remediable 남음 0
- Runtime-only 10

이 수치는 **Developer 자체검수 결과**이며 QA PASS가 아니다.

실제 반영된 주요 영역은 JPA Optional, JTA/XA, TCC, Inbox/Dedup, Saga 보강,
AI Optional, OIDC login, audit-jdbc, SOAP, DB3 transaction/security SQL,
XA crash recovery harness, Generator persistence 선택 보강이다.

## 2. 왜 신규 개발 Cycle을 여는가

직전 작업은 Transaction/Persistence/Security hardening을 강화했지만 다음 Architecture 개선은 별도 신규 작업으로 남는다.

- Core Slimming
- Core Runtime/Provider Ownership 이동
- Unified Utility / Pure Foundation
- transactionId Contract/Implementation 분리
- Runtime Health / Instance Operations
- Valkey Distributed Session
- S3-compatible Object Storage
- Event Schema Governance
- GraphQL Optional
- SSE/WebSocket Realtime
- Verification Tool/Gate Currentization
- 개발/QA 문서 통합·가비지 정리

신규 개발 정본은 `CPF_CURRENT_WORK_REQUEST.md`다.

## 3. 직전 Runtime-only 10건 — PASS 승계 금지

| ID | 미검증 항목 | 필요 환경 |
|---|---|---|
| RT-01 | Java25/Gradle 9.1 fresh full build/test | Java25 + full dependency environment |
| RT-02 | XA DB+DB DB3 | live XA-capable DB resources |
| RT-03 | XA DB+JMS kill/recovery | live XA JMS + DB |
| RT-04 | broker ACK loss/kill/multi-instance | live Kafka/Rabbit/JMS/IBM MQ |
| RT-05 | Saga/TCC kill/restart | live DB3 |
| RT-06 | JPA DB3/JTA | live DB3 + Java25 |
| RT-07 | OIDC live SSO | Keycloak/Entra/Okta tenant |
| RT-08 | PKCS#11 KMS/HSM | actual provider/token |
| RT-09 | SOAP timeout/UNKNOWN | controlled SOAP upstream |
| RT-10 | Generator PowerShell E2E | Windows/PowerShell environment |

신규 Source 이동의 영향을 받으면 Harness/Config를 먼저 갱신하고 다시 실행한다.

## 4. 현재 Open Work

모든 신규 Requirement는 `REQUIREMENT_STATUS.csv`에서 `미완료/미검증`으로 시작한다.
이전 PASS를 신규 Requirement에 매핑하여 승계하지 않는다.

P0:

- Architecture/Core Slimming
- Utility/Foundation
- transactionId ownership
- Health/Instance Operations
- Persistence/JPA parity
- OIDC/SSO 재검수 + Distributed Session
- Object Storage
- Event Schema
- Canonical/Current documentation
- Repository/Tool Hygiene

P1:

- GraphQL Optional
- Realtime
- Valkey Lock/Lease
- Testkit
- Java25 Modern Execution

gRPC/R2DBC는 실제 Product Consumer Requirement가 생기기 전 기술 보유 목적으로 추가하지 않는다.

## 5. 문서 정리 정책

과거 V9I/REV/SESSION/FINAL/Checkpoint 문서는 Current Owner로 필요한 의미를 흡수한 뒤 삭제한다.
Git history가 원문을 보존한다.

남겨야 하는 핵심 Current Owner:

- `CPF_CURRENT_WORK_REQUEST.md`
- `REQUIREMENT_STATUS.csv`
- `REVIEW_INDEX.md`
- `TEST_AND_EVIDENCE.md`
- `HANDOVER.md`
- `CPF_CHANGE_MANIFEST.csv`
- `CPF_DELETE_MANIFEST.csv`

대용량 Requirement/Scenario Master는 논리 Dataset으로 별도 유지한다.

Developer/QA/Codex가 세션별 결과문서를 다시 생성하지 않게 `CPF_DOCUMENTATION_STANDARD.md`를 준수한다.

## 6. Tool/Gate 정리

현재 `cpf-tools/verification`에는 날짜형 캠페인 폴더, `qa38`, `qa39`, `final-dev`, `java21`,
`final_dev_campaign.py` 등 과거 성격의 도구와 현재 제품 Gate가 혼재한다.

다음 Developer는 이름만 보고 삭제하지 말고 CI/Gradle/`verify-full-product.ps1`/Runbook/Script
Consumer를 전수검색한 뒤 Current Gate에 흡수된 consumer-less helper를 정확히 제거한다.

제품 Runtime/CI에 실제 사용되는 Gate는 보호한다.

## 7. Developer 완료 후 QA

이번 Cycle은 Developer 완료 후 바로 다음 Developer 반복으로 가지 않는다.

1. 최신 successor master exact SHA 확인
2. QA A — 전체 100% 직접 전수
3. QA B — 동일 전체 100% 독립 전수
4. A/B exact ID Cross Validation
5. disagreement/FAIL은 같은 ID 재개발 또는 재검수
6. Runtime-only를 Codex/실환경에서 실행
7. QA 최종판정

QA 표준: `cpf-docs/work/handover/CPF_QA_SESSION_HANDOVER_STANDARD.md`

## 8. Codex 역할

Codex credit은 Runtime-only 검증 중심으로 사용한다.

우선순위:

- Java25/Gradle fresh build
- DB3 lifecycle
- XA/JTA kill/recovery
- Broker multi-instance/fault
- JPA DB3
- Browser ADM/BZA
- OIDC/HSM/SOAP 실환경
- Generated Domain fresh lifecycle

Source 미구현을 Codex Runtime 반복으로 해결하지 않는다.

## 9. Git/Delete Safety

사용자 승인 없이 Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean/File Delete를 수행하지 않는다.

삭제는 `CPF_DELETE_MANIFEST.csv` exact path만 사용한다.
`git clean`, `git reset --hard`, `git restore .` 금지.

## 10. 완료 표현

Developer-remediable Gap이 0이고 Source/Consumer/Test/Harness가 완성되었으며
외부 Runtime만 남았을 때만:

`현행 요건상 개발GPT 추가 구현 없음 / Remaining=Runtime-only verification`

으로 기록한다.

QA A/B 통과 전에는 제품 최종 완료라고 표현하지 않는다.
