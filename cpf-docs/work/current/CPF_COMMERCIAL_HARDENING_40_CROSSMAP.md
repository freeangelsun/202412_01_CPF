# CPF Commercial Hardening 40 — Canonical Cross-map / Successor Acceptance

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실제 실행 시 최신 Git HEAD와 Runtime Evidence를 사용한다.
> Top-level axes: **정확히 40개**  
> Current status/evidence: `cpf-docs/work/TEST_AND_EVIDENCE.md`, `cpf-docs/work/OPEN_ISSUES.md`, `cpf-docs/work/REQUIREMENT_STATUS.csv`. 별도 Hardening 상태 CSV를 중복 유지하지 않는다.
> 정책: 이 문서는 신규 Requirement 40개를 만드는 문서가 아니다. 기존 Canonical ID에 Acceptance/Defect를 병합한다.  
> 전달 방식: P0/P1/P2로 분할하지 않고 매 회차 전체 40개를 하나의 Full-Scope로 유지.

## 1. [P0] Runtime 장애·복구·UNKNOWN Hardening

- **Canonical 연결:** `ARCH-MSA`, `TX-E2E`, `TEST-FAULT`, `TEST-RUNTIME`
- **완료 의미:** Process kill/timeout/response-loss/부분 성공에서 side-effect 전후 상태를 재현하고 UNKNOWN을 operator-visible 상태로 보존한 뒤 retry/reconcile/compensation으로 수렴
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 2. [P0] 다중 인스턴스 / 분리 WAS / MSA 일관성

- **Canonical 연결:** `ARCH-MSA`, `CPF-LOCK`, `CPF-HEALTH`, `TEST-RUNTIME`
- **완료 의미:** 2+ instance에서 context, cache, lock, session, scheduler, idempotency, health projection이 일관되고 split-brain/stale-writer를 차단
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 3. [P0] Transaction / Outbox / Inbox / Idempotency 통합

- **Canonical 연결:** `TX-LOCAL`, `TX-INBOX`, `TX-E2E`, `EVENT-OUTBOX`, `TEST-FAULT`
- **완료 의미:** Local Tx와 remote side effect 경계를 분리하고 DB→message는 Outbox, message→DB는 Inbox/idempotency, commit ambiguity는 UNKNOWN/Reconcile로 처리
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 4. [P0] Security / Identity 통합 모델

- **Canonical 연결:** `SEC-AUTHN`, `SEC-AUTHZ`, `SEC-APP`, `SEC-AUDIT`, `SEC-SECRET`
- **완료 의미:** user/service/tenant identity와 trust boundary, authN/authZ, credential/secret, audit가 online/batch/message/integration 전 경로에서 동일 의미
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 5. [P0] 위험 운영조치 승인 / SoD / Break-glass

- **Canonical 연결:** `SEC-APPROVAL`, `ADM-APPROVAL`, `ADM-AUDIT`, `BZA-APPROVAL`
- **완료 의미:** 위험 명령은 reason/권한/승인/SoD/break-glass/감사/결과 추적을 제공하고 self-approval 또는 미감사 경로를 금지
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 6. [P0] Starter/API Developer Experience 전수 Audit

- **Canonical 연결:** `STARTER-DX`, `DEVEX-LAYER`, `DEVEX-QUICK`, `DEVEX-UTILITY`
- **완료 의미:** 모든 Public Starter/API를 실제 고객 개발 흐름으로 사용해 boilerplate, 오류메시지, 자동완성, JavaDoc, Native Escape를 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 7. [P0] Public API / SPI / Internal 경계 최종 정리

- **Canonical 연결:** `ARCH-BOUNDARY`, `ARCH-LAYER`, `PROD-PLUGIN`, `RULE-ARCH`
- **완료 의미:** Internal leaf 직접 참조·역방향 의존·중복 public surface를 제거하고 Public API/SPI는 owner/consumer/compatibility를 명확히 함
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 8. [P0] Starter Canonical Catalog 단일화

- **Canonical 연결:** `ARCH-STARTER`, `STARTER-DX`, `RULE-ARCH`
- **완료 의미:** settings/physical path/generator/profile/BOM/publication/provider slot이 application-starters Canonical Catalog 하나를 따르고 duplicate/missing을 fail-fast
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 9. [P0] 고객 실제 개발 표준 흐름 완성

- **Canonical 연결:** `DEVEX-LAYER`, `DEVEX-ANNOTATION`, `DEVEX-VALIDATION`, `DEVEX-ERROR`, `DEVEX-LOGGING`
- **완료 의미:** Controller→Service→Repository/Integration/Messaging/Batch Golden Path가 Annotation/Base/Common Operations로 실제 동작
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 10. [P0] Generator Stateless Lifecycle / DX 완성

- **Canonical 연결:** `DEVEX-CODEGEN`, `SAMPLE-MBR`, `SAMPLE-REF`, `SAMPLE-EDU`
- **완료 의미:** validate/preflight/generate/regenerate/upgrade/restore가 metadata를 고객 project에 남기지 않고 member/external 두 회귀 domain을 동일하게 재현
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 11. [P0] Root Build / Convention / Publication 경로 단일화

- **Canonical 연결:** `REL-BUILD`, `RULE-ARCH`, `RULE-QUALITY`
- **완료 의미:** fresh clone에서 root help/config/build/test/publication이 하나의 convention/plugin 경로로 실행되고 missing apply-from/dual convention 0
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 12. [P0] Education / Sample 실행체계 완성

- **Canonical 연결:** `SAMPLE-EDU`, `DEVEX-TESTKIT`, `DOC-PRODUCT`, `TEST-CONTRACT`
- **완료 의미:** EDU가 Product mimic/test-double false green이 아니라 실제 CPF Public Capability consumer와 provider runtime을 사용
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 13. [P0] Batch Runtime / Scheduler / Worker / Center-Cut 구조 완성

- **Canonical 연결:** `BAT-CORE`, `BAT-JOB`, `BAT-EXECUTOR`, `BAT-SHARED`, `CENTER-RUNNER`, `CENTER-OPS`, `CENTER-UNKNOWN`
- **완료 의미:** api/runtime-support/runtime/control-plane/scheduler/worker/center-cut/agent/testkit IA와 workload/control-plane 경계를 완성
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 14. [P0] cpf-tools / deploy Canonical IA 완성

- **Canonical 연결:** `ARCH-LAYER`, `REL-DEPLOY`, `RULE-ARCH`
- **완료 의미:** tools/deploy를 Canonical IA로 물리 이동하고 old top-level scripts/analysis 및 legacy deploy tree를 완성 source + manifest 방식으로 제거
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 15. [P0] Repository Garbage / Dead Source / False-Green 제거

- **Canonical 연결:** `RULE-QUALITY`, `REQ-REVIEW`, `TEST-EVIDENCE`
- **완료 의미:** stale source/path/duplicate/dead code/marker-only/mock-only/historical evidence를 active surface에서 제거
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 16. [P0] Current Evidence / Exact SHA 신뢰성 체계

- **Canonical 연결:** `TEST-EVIDENCE`, `REQ-REVIEW`, `REQ-GOV`
- **완료 의미:** 모든 PASS는 현재 exact SHA, command, exit code, environment, report/hash로만 성립; 과거 SHA evidence 승계 금지
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 17. [P1] Persistence 상용 기본기 강화

- **Canonical 연결:** `DB-SQL`, `DB-PERF`, `DEVEX-LAYER`, `TEST-RUNTIME`
- **완료 의미:** Repository CRUD/search/page/cursor/bulk/lock/timeout/error mapping/concurrency와 JDBC/MyBatis/JPA provider-neutral semantics를 완성
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 18. [P1] Oracle/PostgreSQL/MariaDB DB3 완성

- **Canonical 연결:** `DB-MULTI-VENDOR`, `DB-INSTALL`, `DB-MIGRATION`, `DB-ROLLBACK`, `DB-FRESH`
- **완료 의미:** Canonical one source에서 3 vendor install/seed/upgrade/rollback-or-forward-recovery/runtime/checksum/drift를 동일 수준 검증
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 19. [P1] Observability E2E 추적

- **Canonical 연결:** `CPF-TRACE`, `DEVEX-LOGGING`, `OPS-METRIC`, `TEST-RUNTIME`
- **완료 의미:** system/instance/transaction/execution/user/job/step correlation을 log/trace/metric/audit/ADM까지 연결
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 20. [P1] Runtime Health / Readiness / Graceful Drain

- **Canonical 연결:** `CPF-HEALTH`, `API-REALTIME`, `TEST-RUNTIME`
- **완료 의미:** liveness/readiness/startup/degraded/drain/unknown과 graceful stop이 instance/consumer/batch/gateway에 연결
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 21. [P1] Cache / Redis / Valkey Hardening

- **Canonical 연결:** `CACHE-REDIS-PROVIDER`, `CPF-LOCK`, `TEST-FAULT`
- **완료 의미:** Caffeine/Redis/Valkey public semantics, TTL/invalidation/single-flight/stale/outage/reconnect/version/multi-instance/provider collision을 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 22. [P1] Messaging 장애대응 표준화

- **Canonical 연결:** `EVENT-BROKER`, `EVENT-OUTBOX`, `EVENT-DLQ`, `EVENT-SCHEMA`, `TEST-BROKER`
- **완료 의미:** publish/consume/correlation/retry/DLQ/duplicate/order/rebalance/outbox/inbox/schema/UNKNOWN를 broker matrix로 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 23. [P1] Integration 장애대응 표준화

- **Canonical 연결:** `ARCH-MSA`, `API-CONTRACT`, `TEST-FAULT`
- **완료 의미:** sync/async call의 timeout/retry/circuit-breaker/bulkhead/rate-limit/auth/error mapping/remote UNKNOWN/reconcile을 표준화
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 24. [P1] ADM Commercial Control Plane 완성

- **Canonical 연결:** `ADM-AUTH`, `ADM-TX`, `ADM-BATCH`, `ADM-CENTER`, `ADM-LOG`, `ADM-AUDIT`, `ADM-RECOVERY`, `ADM-INCIDENT`, `ADM-UX`
- **완료 의미:** 80 requirement route를 Menu→Route→Page→Generated Client→Backend→Permission→E2E로 닫고 위험조치/오류 UX를 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 25. [P1] BZA Business Admin 완성

- **Canonical 연결:** `BZA-BUSINESS`, `BZA-ORG`, `BZA-APPROVAL`, `BZA-SEQUENCE-SAMPLE`
- **완료 의미:** 27 function과 7 target menu group 의미를 실제 route/menu/backend enforcement/audit로 닫음
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 26. [P1] Common Code / Message / Parameter Runtime화

- **Canonical 연결:** `CMN-CODE`, `CMN-MSG`, `CMN-CALENDAR`, `CMN-TEMPLATE`, `CMN-SAMPLE-DB`
- **완료 의미:** cpf-starters/common을 실제 Product Service owner로 유지하고 DB-backed runtime consumer/ADM/BZA와 연결; 기술 helper dumping 금지
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 27. [P1] Config / Profile / Secret Governance

- **Canonical 연결:** `OPS-CONFIG`, `SEC-SECRET`, `ARCH-STARTER`, `RULE-SEC`
- **완료 의미:** profile/config/secret schema·validation·override·refresh·drift·masking·environment separation과 missing config fail-fast
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 28. [P1] API / Event / DB Schema Versioning & Compatibility

- **Canonical 연결:** `API-CONTRACT`, `EVENT-SCHEMA`, `REL-COMPAT`, `DB-MIGRATION`
- **완료 의미:** API/Event/DB의 backward/forward compatibility, deprecation window, mixed-version, rollback/forward recovery를 contract test로 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 29. [P1] Event Schema / Contract Governance

- **Canonical 연결:** `EVENT-SCHEMA`, `TEST-CONTRACT`, `EVENT-BROKER`
- **완료 의미:** schema version/compatibility/generated model/content type/producer-consumer validation/breaking gate를 broker-independent하게 운영
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 30. [P1] Testkit / Contract Test / Fault Injection Harness

- **Canonical 연결:** `DEVEX-TESTKIT`, `TEST-CONTRACT`, `TEST-FAULT`, `TEST-BROKER`
- **완료 의미:** deterministic fixture와 real provider bridge를 분리하고 process/network/DB/broker/time/response-loss fault injection을 제공
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 31. [P1] 성능·확장성 Engineering

- **Canonical 연결:** `DB-PERF`, `OPS-CAPACITY`, `TEST-RUNTIME`, `RULE-QUALITY`
- **완료 의미:** load/concurrency/latency/memory/thread/connection/backpressure와 capacity baseline을 exact environment에서 측정
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 32. [P1] Upgrade / Rollback / Publication / Supply Chain

- **Canonical 연결:** `REL-BUILD`, `REL-DEPLOY`, `REL-MIG`, `REL-COMPAT`, `RULE-SEC`
- **완료 의미:** signed/reproducible artifact, SBOM, lock/BOM, install/upgrade/rollback/forward recovery/mixed-version/supply-chain trust 검증
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 33. [P2] Time / Clock / Timezone / Sequence 표준

- **Canonical 연결:** `CMN-CALENDAR`, `BZA-SEQUENCE-SAMPLE`, `DEVEX-TESTKIT`
- **완료 의미:** Clock/businessDate/timezone/DST/sequence/id generation을 injectable deterministic contract로 제공하고 online/batch 일관성 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 34. [P2] Resource Exhaustion / Backpressure / Overload Protection

- **Canonical 연결:** `API-LIMIT`, `OPS-CAPACITY`, `TEST-FAULT`, `TEST-RUNTIME`
- **완료 의미:** thread/queue/connection/disk/memory/broker/cache exhaustion에서 quota/rate limit/bulkhead/backpressure/degrade/reject/recover를 검증
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 35. [P2] Backup / Restore / DR / Rebuildability

- **Canonical 연결:** `DB-BACKUP`, `OPS-DR`, `REL-MIG`, `TEST-RUNTIME`
- **완료 의미:** backup/restore/PITR/rebuild/DR failover/failback와 config/artifact/DB state의 복원 가능성을 검증
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 36. [P2] Data Privacy / Retention / Masking / Audit Lifecycle

- **Canonical 연결:** `SEC-PRIVACY`, `SEC-AUDIT`, `CPF-LOGDB`, `DEVEX-LOGGING`
- **완료 의미:** PII classification/masking/retention/export/delete/audit integrity와 log/evidence 원문 secret 금지를 lifecycle 전체에 적용
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 37. [P2] Extension / Plugin / Native Escape Hatch 정책

- **Canonical 연결:** `PROD-PLUGIN`, `DEVEX-LAYER`, `ARCH-BOUNDARY`
- **완료 의미:** 공식 extension SPI, compatibility/isolation/lifecycle/permission과 Public Native Escape를 제공하되 internal bypass를 금지
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 38. [P2] Cross-platform CLI / Developer Tooling 완성

- **Canonical 연결:** `DEVEX-CODEGEN`, `REL-BUILD`, `RULE-QUALITY`
- **완료 의미:** Windows/Linux/macOS 명령, path/encoding/exit-code/quoting/long-path를 동일하게 검증하고 사용자 repo 변환 script를 금지
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 39. [P2] Commercial Education / Onboarding / Troubleshooting

- **Canonical 연결:** `SAMPLE-EDU`, `DOC-PRODUCT`, `DEVEX-QUICK`
- **완료 의미:** quickstart→golden path→failure/recovery→advanced/native escape→troubleshooting이 실제 실행 코드와 일치
- **현재 Source 판정:** 개발/Source/Consumer/Test closure 및 현재 작업본 정적 Gate 재검증 완료. 상세 Evidence는 `cpf-docs/work/TEST_AND_EVIDENCE.md`와 `cpf-docs/work/evidence/current/`를 따른다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 40. [P2] Release Readiness / Commercial Acceptance Closure

- **Canonical 연결:** `REL-BUILD`, `REL-DEPLOY`, `TEST-EVIDENCE`, `REQ-REVIEW`
- **완료 의미:** fresh clone→build→publication→generator→DB3→app→transaction→failure/recovery→ADM/BZA→batch→security→upgrade/rollback→evidence 단일 acceptance flow
- **현재 Source 판정:** 개발/Source/Config/Test/Harness closure 완료. 정적 Gate PASS. 실제 외부·다중 인스턴스·Java25·DB·성능·DR 등 환경 의존 Runtime 항목만 `미검증`이며 미실행을 PASS로 기록하지 않는다.
- **필수 증명:** Product Source + 실제 Consumer + 정상/오류/경계/부분실패 + recovery/reconcile + exact-SHA Test/Evidence.
- **False Green 금지:** Interface/DTO/Config/Scenario name/Mock/문서만으로 완료하지 않는다.

## 40개 상한 / 신규 기능 Freeze

- 새 결함은 위 40개 중 owner axis에 병합한다.
- 41번째 top-level axis를 자동 생성하지 않는다.
- 새 Starter/API/Capability는 실제 Public Surface Gap이 재현되고 기존 Capability 조합으로 해결되지 않으며 Owner/Consumer/Test가 명확할 때만 허용한다.
- 그렇지 않으면 Runtime Hardening, DX, Recovery, Compatibility, Evidence를 우선한다.


## Configuration / Invocation Cross-cut Supplement

다음은 신규 41번째 Hardening Axis가 아니다. 기존 40대 Acceptance를 보강한다.

- **#2 Multi-instance:** Domain/Service/Instance Binding, config version/hash drift, drain/zone/version routing.
- **#6 Starter/API DX:** 설정 위치·IDE metadata·typed bean/client·fail-fast까지 개발자 경험에 포함.
- **#8 Starter Catalog:** 64 configPrefix와 Config Catalog/owner/native dependency/cardinality를 1:1 검증.
- **#9 고객 개발 흐름:** MBR→EXS/ACC Typed Domain Call과 External Typed Client 실제 사용.
- **#10 Generator:** logical domainDependencies/externalClients + environment binding skeleton.
- **#23 Integration:** 내부 Domain Call과 External Integration Registry/Policy 분리.
- **#24 ADM:** ops-config/ops-topology/external-institutions/ops-drift actual control-plane.
- **#27 Config/Profile/Secret:** source precedence, secret ref, mutability, atomic refresh, rollback, drift.
- **#28 Compatibility:** config key/schema version/deprecation/alias/mixed-version.
- **#32 Upgrade/Rollback:** config migration/rollback/restart-required compatibility 포함.
- **#38 Tooling:** preflight/doctor diagnostics에서 missing/unknown/effective config 확인.
- **#40 Release Closure:** fresh config setup에서 generated app가 실제 Domain/External 호출까지 성공해야 함.


### Configuration Usability 추가 Cross-cut

새 Hardening Axis가 아니다.

- #6 Starter/API DX: 한글 config comment, IDE metadata, Source Customizer/Options.
- #9 고객 개발 흐름: local safe default + prod fail-fast.
- #27 Config/Profile/Secret: 환경파일 역할, override policy, comment/default drift.
- #31 Performance: pool/thread/concurrency/timeout 주요 option을 안전 범위와 함께 노출.
- #32 Upgrade/Rollback: config key/default 변경 compatibility.
- #38 Tooling: hardcoded/default/profile/comment coverage gate.
- #39 Education: copy 가능한 local/dev/stg/prod sample.
- #40 Release: prod localhost/sample fallback 0.


## Developer-First Call / Result / Transaction / Logging Cross-cut — 재개발 병합

신규 41번째 Axis가 아니다.

- #1 Runtime recovery/UNKNOWN: 표준 `CpfResult<T>`/RecoveryInfo.
- #2 Multi-instance: Domain Typed Client local/remote parity.
- #6 Starter/API DX: 함수 이름/자료형/옵션/한글 JavaDoc.
- #9 Customer development flow: IDE-first Golden Path.
- #18 Evidence: TxId timeline/Result state exact-SHA.
- #19 Observability: 정본 8.1 로그필드 E2E.
- #23 Integration: Domain vs External 분리.
- #27 Config: typed call options.
- #29 Event schema: message receipt/correlation.
- #30 Testkit: business/technical/unknown fault injection.
- #31 Performance: remote-in-local-tx lock/deadline.
- #39 Education: 자료형별 copy-paste example.
- #40 Release: Manual/API/Generator/Runtime parity.
