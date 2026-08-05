# CPF 누락 기능·Requirement 승격 검토 V7

이 파일은 단순 후보 보관소가 아니다. 외부 표준, CPF Stack 정책과 상용 Framework 운영 관점에서 기존 Canonical에 부족한 항목을 다시 판정했다.

- `PROMOTE_NEW_CANONICAL`: REQ-GAP 절차로 신규 Canonical ID 등록을 우선 검토
- `MERGE_STRENGTHEN`: 기존 Canonical Acceptance와 Work Package에 통합
- `REVIEW_FOR_CANONICAL_SPLIT`: 규제/제품 정책을 확인한 뒤 분리 여부 결정
- `DEFER_OUT_OF_SCOPE`: 현재 제품 범위에는 넣지 않으며 재개 조건을 명시

## GAP-CACHE — Cache Capability

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core cache SPI + leaf providers + actual common/domain consumer |

### 검토 근거

Stack policy already names Caffeine/local and optional distributed providers, but no independent canonical acceptance covers key/version/TTL/eviction/invalidation/stampede/negative cache/multi-instance semantics.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-FEATURE-FLAG — OpenFeature Feature Flag

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core SPI + OpenFeature starter + ADM operations |

### 검토 근거

Stack policy names OpenFeature but evaluation context, provider lifecycle, hooks/events, secure override, audit, stale cache and fail-safe default are not independently testable.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-SESSION-BFF — General Session/BFF

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core security API + web/session starter + generated/customer BFF |

### 검토 근거

ADM operator session exists, but customer/BFF session fixation, rotation, revocation, CSRF, distributed storage and topology parity need a product-wide contract.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-CRYPTO-AGILITY — Crypto Agility·PQC Readiness

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core crypto SPI + security/release gates |

### 검토 근거

Secret/certificate requirements do not fully define algorithm allowlist, provider policy, envelope encryption, rekey, deprecated algorithm block, PQC readiness and CBOM.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-VULN-RESPONSE — Vulnerability·KEV Response Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-tools security + release/product governance |

### 검토 근거

Scanning exists but triage, exploitability, KEV/critical patch SLA, exception expiry, backport, disclosure, customer advisory and root-cause prevention are not complete.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-THREAT-MODEL — Threat Modeling·Abuse Cases

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | architecture/security governance + quality gate |

### 검토 근거

Trust boundaries appear in requirements, but change-triggered data flow/abuse case/mitigation/residual-risk review is not a standalone, testable lifecycle.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-PERF-SOAK — Load·Stress·Soak·Leak Regression

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | repository-wide performance test kit + CI/release profile |

### 검토 근거

DB performance and capacity exist, but API/Broker/Batch/Frontend/load/soak/leak regression budgets are not unified as a repository-wide test product.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-WEBHOOK — Webhook/Callback Delivery

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core API contract + generated/customer adapter starter |

### 검토 근거

API-ASYNC mentions callback, but signature, replay defense, endpoint validation, SSRF, retry/order, delivery ledger and reconciliation need explicit contracts.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-ASYNCAPI-SCHEMA — AsyncAPI·Event Schema Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | event contract + provider starters + quality/generator |

### 검토 근거

Event contracts lack a canonical AsyncAPI 3.1 source, schema registry/compatibility mode, subject/channel naming, deployment gate and consumer migration.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-DATA-ENCRYPTION — Field/File Data Encryption·Tokenization

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | data owner + crypto SPI + DB migration |

### 검토 근거

Backup encryption alone does not cover field classification, tokenization/searchability, rekey, key version, migration and application/query impacts.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-DATA-QUALITY — Data Quality·Quarantine·Correction

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | data owner + operations/admin |

### 검토 근거

Lineage mentions quality rules but lacks rule lifecycle, threshold, quarantine, correction, replay, owner approval and SLA.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-TIME — Time·Clock·Timezone Standard

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-core time API/SPI + operations health |

### 검토 근거

Lease/deadline/audit depend on time but UTC/business timezone, monotonic clock, skew, NTP health, serialization and test clock are not standalone.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-NOTIFICATION — Notification Delivery Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-common policy + channel SPI/providers + ADM |

### 검토 근거

CMN-TEMPLATE lacks channel delivery, outbox, preference, rate, retry, bounce/receipt, masking and audit contracts.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-SUPPORT-BUNDLE — Sanitized Support Bundle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-admin operations + cpf-tools diagnostics |

### 검토 근거

Incident/runbook requirements lack a safe product for version/config/topology/health/log/metric/thread-dump collection, masking, consent, size, expiry and integrity.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-SUPPLIER-DUE-DILIGENCE — Supplier/Product Due Diligence

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | release/product governance + dependency onboarding |

### 검토 근거

SBOM/provenance exist, but NIST SP 1326 supplier provenance, resilience, foundational practice and tier assessment are not explicit.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-TELEMETRY-LIFECYCLE — Telemetry Schema/Stability Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | cpf-core observability + operations |

### 검토 근거

Merge into CPF-TRACE/OPS-METRIC/REL-COMPAT: OTel group stability, version pin, opt-in, dual emit, dashboard migration and cardinality contract.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-DESIGN-SYSTEM — ADM/BZA Design System·I18N

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | shared frontend design system |

### 검토 근거

Merge into ADM-UX/CMN-MSG: component/token/form/table/error, locale/timezone/number/date, visual regression and accessibility reuse.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-OPENAPI-PROFILE — OpenAPI Supported Profile Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | API quality/release |

### 검토 근거

Merge into API-CONTRACT/REL-COMPAT: 3.1.2/3.2.0 support decision, generator capability, reference cycles, sanitization and migration.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-CBOM — Cryptographic BOM

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | security + supply chain |

### 검토 근거

Merge into SEC-CERT/SEC-SECRET/REL-BUILD: CycloneDX 1.7 cryptographic asset inventory, deprecated crypto and PQC readiness.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-DEPENDENCY-POLICY — Dependency·License·Supplier Policy

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | quality/release governance |

### 검토 근거

Merge into RULE-SEC/RULE-QUALITY/REL-BUILD: source, supplier, license, vulnerability, maintenance/EOL, provenance and waiver expiry.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-UPGRADE-ASSISTANT — CPF Framework Upgrade Assistant

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `PROMOTE_NEW_CANONICAL` |
| 예상 Owner | cpf-tools migration/upgrade tooling |

### 검토 근거

Migration guides exist, but customer project inventory, compatibility checks, generated config/SQL/API changes, dry-run, rollback plan and report are not a product capability.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-CONFIG-SCHEMA — Config Schema Evolution

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `MERGE_STRENGTHEN` |
| 예상 Owner | core config + runtime control |

### 검토 근거

Merge into CORE-CONFIG/OPS-CONFIG/REL-COMPAT: typed schema, secret classification, version migration, deprecation, unknown key fail policy and generated docs.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-DATA-RESIDENCY — Data Residency·Localization

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 관리자 권고 | `REVIEW_FOR_CANONICAL_SPLIT` |
| 예상 Owner | privacy/data/product policy |

### 검토 근거

Privacy/multitenancy mention isolation but regional storage/processing/backup/export policy and migration evidence may require a separate optional capability.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
## GAP-AI-SECURITY — AI Security Verification

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| 관리자 권고 | `DEFER_OUT_OF_SCOPE` |
| 예상 Owner | future product governance |

### 검토 근거

Do not add OWASP AISVS/NIST AI requirements until CPF introduces an AI-enabled product capability. Keep a trigger to re-open when AI enters scope.

### 필수 처리

- [ ] 기존 169개 Canonical과 의미·Owner·Acceptance·Source Consumer 중복을 검산한다.
- [ ] 권고에 따라 신규 등록 또는 기존 Requirement 강화 방안을 결정한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·동시성·다중 인스턴스·보안·운영 Scenario를 정의한다.
- [ ] Source·SQL/API·Test·Config·Frontend·Script·Generator·DB Vendor·Migration 영향을 산정한다.
- [ ] 채택/병합/보류 근거와 Continuity 관계를 기록한다.

---
