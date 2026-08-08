<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-hero-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-hero-desktop.png" alt="Core Platform Framework" width="100%">
</picture>

**Core Platform Framework(CPF)**는 온라인·비동기·배치·외부연계·관리자 운영·보안·복구를 하나의 제품 계약으로 연결하는 Business Platform Framework입니다.

[프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf) · [개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf) · [배치 개발](cpf-docs/guides/02_배치개발매뉴얼.pdf) · [ADM 연동 개발](cpf-docs/guides/03_ADM개발자매뉴얼.pdf) · [ADM 운영](cpf-docs/guides/04_ADM운영자매뉴얼.pdf) · [플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf)  
[BZA](cpf-docs/guides/90_BZA매뉴얼.pdf) · [Gateway](cpf-docs/guides/91_Gateway매뉴얼.pdf) · [아키텍처 설계](cpf-docs/deliverables/아키텍처설계서.pdf) · [기술 사양](cpf-docs/deliverables/기술사양서.pdf) · [기술 표준](cpf-docs/deliverables/기술표준서.pdf) · [DB 표준](cpf-docs/deliverables/데이터베이스표준서.pdf)

> **문서 기준** · `master` `b2da6bd720d1` (`07_15`) · Canonical Requirement **180개**. 사용 문서는 완료된 제품에서 수행해야 하는 선택·개발·운영·실패 판단·복구·감사 계약을 기술하며, 실제 Symbol과 Reference 예제를 구분합니다.

</div>

---

## 무엇을 해결하는 플랫폼인가

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-architecture-overview-mobile.png"><img src="cpf-docs/assets/readme/cpf-architecture-overview-desktop.png" alt="CPF Architecture" width="100%"></picture>

CPF의 중심은 특정 라이브러리 묶음이 아니라 **업무 계약과 운영 계약의 일치**입니다. 동일 업무 API는 Modular Monolith의 Local Facade와 분리 WAS/Microservice의 Remote Facade에서 같은 요청·검증·오류·권한·idempotency·audit 의미를 유지합니다. transactionId는 Channel에서 시작해 DB, 외부 API, Message, Batch, File, Retry, UNKNOWN_RESULT, Reconcile과 ADM Timeline까지 이어집니다.

## 제품 구성

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-product-map-mobile.png"><img src="cpf-docs/assets/readme/cpf-product-map-desktop.png" alt="CPF Product Map" width="100%"></picture>

- `cpf-core` — topology-independent Public API/SPI, Context, Error, Validation, Idempotency, Lock, Reliability contract
- `cpf-common` — 고객 공통 정책과 명시된 업무 공통
- `cpf-starters` — data / messaging / integration / file / notification / security / platform-operations capability runtime
- `cpf-batch` — Spring Batch, Scheduler, Center-Cut, Agent, Runner, Worker
- `cpf-admin` — 플랫폼 Control Plane, Timeline, Incident, Recovery, Approval, Runtime control
- `cpf-biz-admin` — 조직·직원·Role·Permission·Data Scope·결재·업무 감사
- `cpf-gateway` — 외부 trust boundary, routing, resilience, security, attempt ledger
- `cpf-tools` — Generator, BOM/Plugin, DB vendor packs, artifact/deploy verification
- `cpf-member` / `cpf-reference` — Golden generated domain / executable reference

공개 Profile은 `minimal-domain`, `web-api`, `secure-api`, `browser-bff`, `event-service`, `batch-service`입니다.

## 배포 Topology가 바뀌어도 업무 계약은 유지

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-topology-mobile.png"><img src="cpf-docs/assets/readme/cpf-topology-desktop.png" alt="CPF Topology" width="100%"></picture>

지원 목표에는 Embedded Boot JAR, External WAS WAR, Modular Monolith, Microservice, 독립 Static Web Artifact, Gateway, Agent/Runner/Worker, Multi-instance/Multi-zone, Rolling/Canary/Blue-Green, Backup/Restore와 DR Failover/Failback이 포함됩니다. Topology 전환을 위해 업무 Source를 다시 작성하는 구조는 CPF 계약에 맞지 않습니다.

## Transaction 전략은 업무 일관성 요구로 선택

| 상황 | 기본 전략 | 운영 시 핵심 확인 |
|---|---|---|
| 단일 DB | LOCAL | commit/rollback, isolation, timeout |
| DB+DB / DB+JMS 강한 원자성 | XA/JTA | 2PC, in-doubt, recovery scan |
| DB+Kafka/RabbitMQ/Event | OUTBOX + INBOX_DEDUP | ACK loss, lease/fencing, duplicate |
| MSA A→B→C 장기 흐름 | SAGA | 역순 compensation, UNKNOWN |
| 잔액·한도·재고 Hold | TCC | Try/Confirm/Cancel idempotency |
| 외부 처리 결과 불명 | UNKNOWN + RECONCILE | 결과 조회 후 retry/compensation |

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-execution-mobile.png"><img src="cpf-docs/assets/readme/cpf-execution-desktop.png" alt="Transaction and Recovery" width="100%"></picture>

## 개발자는 어떻게 시작하나

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-domain-journey-mobile.png"><img src="cpf-docs/assets/readme/cpf-domain-journey-desktop.png" alt="Domain Journey" width="100%"></picture>

1. [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf)에서 도입 범위와 Topology/Profile을 결정합니다.
2. [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf)에서 Generator → API → Domain → Persistence → Transaction → Integration → Security → Test → 운영 인계를 따라갑니다.
3. 정기·대량 처리는 [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.pdf), ADM 연동은 [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.pdf)에서 이어갑니다.
4. Runtime 설치·설정·배포·Backup·DR은 [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf)을 기준으로 합니다.

## 운영자는 무엇을 보나

<picture><source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-operations-mobile.png"><img src="cpf-docs/assets/readme/cpf-operations-desktop.png" alt="CPF Operations" width="100%"></picture>

ADM은 transactionId 기준 Timeline, Runtime/Deployment, Batch, Gateway, Log, Incident, Recovery, Permission, Secret, Approval, Break-glass, Feature Flag 등을 실제 Route로 제공합니다. BZA는 조직/직원/권한/Data Scope/결재/Attachment/Notification/Audit를 담당합니다. 각 화면의 검색·상태·버튼·위험 조치·복구는 해당 운영 매뉴얼에서 설명합니다.

## 신규/보강 Capability

이번 요구사항 정본에는 Transaction Strategy 전체(LOCAL/XA/Outbox/Inbox/Saga/TCC), E2E transaction lineage, Starter Developer Experience, OAuth2/JWT 개발 API, OIDC SSO, KMS/HSM·Digital Signature, tamper-evident Audit, AI Optional Provider contract가 포함됩니다. 세부 사용법은 각 Owner 매뉴얼과 기술 사양에서 다룹니다.

## 공식 문서 지도

| 역할/질문 | Primary Owner |
|---|---|
| CPF 도입 판단·범위·Architecture·Topology | [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf) |
| 온라인/연계 업무 개발 | [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf) |
| Batch 개발·실행·Restart/Reprocess | [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.pdf) |
| 고객 기능의 ADM 연결 | [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.pdf) |
| ADM 실제 화면 운영 | [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.pdf) |
| 설치·Config·DB·배포·관측·DR | [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf) |
| BZA 조직·권한·결재 | [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.pdf) |
| Gateway Route·보안·게시·복구 | [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.pdf) |
| 구조·경계·Trade-off | [아키텍처 설계서](cpf-docs/deliverables/아키텍처설계서.pdf) |
| Public Contract·Capability | [기술 사양서](cpf-docs/deliverables/기술사양서.pdf) |
| 구현 표준 | [기술 표준서](cpf-docs/deliverables/기술표준서.pdf) |
| DB·Migration·Restore | [데이터베이스 표준서](cpf-docs/deliverables/데이터베이스표준서.pdf) |

---

**Core Platform Framework** · Repository `freeangelsun/202412_01_CPF` · Documentation baseline `b2da6bd720d1`
