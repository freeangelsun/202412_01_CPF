# ADDITIONAL DEVELOPMENT REQUIRED

## QA-B-1000-NEW-001 — P0 — Current exact-SHA Evidence provenance false-green

- Requirement: TEST-EVIDENCE / REL-BUILD / CPF-RV-0048 / CPF-RV-0926~0930
- Reproduction: At HEAD b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb, inspect dev-final/TEST_AND_EVIDENCE.md and PACKAGE_MANIFEST.json, then inspect verify-current-sha-evidence.py scan roots/forbidden SHA logic.
- Actual Source: cpf-docs/work/v9i/dev-final/TEST_AND_EVIDENCE.md; cpf-docs/work/v9i/dev-final/PACKAGE_MANIFEST.json; cpf-tools/verification/final-dev/verify-current-sha-evidence.py
- Consumer/Call path: Release qualification -> evidence provenance -> QA/central completion decision
- Expected: Every current result/evidence/manifest PASS is dynamically bound to actual HEAD b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb; any other target SHA fails.
- Actual: Current dev-final TEST_AND_EVIDENCE/PACKAGE_MANIFEST target 08d8beb4a664039904c30aeac07115a04707924a. verify-current-sha-evidence.py scans verification/** and hard-coded prior SHAs, but does not reject dev-final's 08d8beb4a664039904c30aeac07115a04707924a.
- Root Cause: Evidence provenance gate is path-limited and uses a hard-coded historical denylist instead of a dynamic HEAD equality contract.
- Risk: Previous-SHA success can be promoted to current Product completion; release evidence provenance is invalid.
- Development Acceptance: Bind dev-final/QA current result, package manifest, evidence headers and hashes dynamically to git HEAD; scan current result roots; reject any basis/result/source SHA mismatch. Regenerate all exact-SHA evidence.
- Reverification: Mutate any current evidence target SHA away from b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb; provenance gate and release gate must exit non-zero. Restore and regenerate at exact HEAD.

## QA-B-1000-NEW-002 — P0 — Transaction timeline query failure can be misreported as NOT_APPLICABLE

- Requirement: CPF-TRACE / DATA-LINEAGE / ADM-TIMELINE / CPF-RV-0005 / CPF-RV-0633
- Reproduction: Force one appendIfTable source query in CpfTransactionTimelineQueryFacade to throw RuntimeException while the source is applicable.
- Actual Source: cpf-core/src/main/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineQueryFacade.java; cpf-core/src/test/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineFreshnessTest.java; cpf-tools/verification/final-dev/verify-transaction-freshness-applicability.py
- Consumer/Call path: TransactionId -> timeline facade -> AdmTransactionGroupService -> ADM transaction timeline
- Expected: Applicable source query failure is recorded as FAILED/UNAVAILABLE and makes the one-shot result explicitly partial.
- Actual: appendIfTable catches RuntimeException and drops the failure marker; sourceFreshness can then classify the source as NOT_APPLICABLE. Current test covers explicit failed-source input, not the real query exception path.
- Root Cause: Query execution failure is swallowed before applicability/freshness classification.
- Risk: Real DB/query outage can look like a clean complete transaction timeline, hiding failure/UNKNOWN evidence from operators.
- Development Acceptance: Carry source-specific QUERY_FAILED/TABLE_UNAVAILABLE state into freshness; never convert execution failure to NOT_APPLICABLE. Add per-source exception tests and mutation.
- Reverification: Inject query exception for REMOTE/MESSAGE/DLQ/BATCH/FILE/TRACE/AUDIT individually; result must be PARTIAL with failed source and mutation must be killed.

## QA-B-1000-NEW-003 — P0 — Authorized first-hop transactionId provenance has no proven producer before highest-precedence filter

- Requirement: CPF-TXID / CPF-HEADER / GWY-TRUST / CPF-RV-0001
- Reproduction: Send a valid official starter transactionId over an mTLS-authenticated or trusted-proxy ingress without the custom request attributes cpf.authenticated-system-code/cpf.trusted-transaction-context.
- Actual Source: cpf-core/src/main/java/com/cpf/core/common/transaction/CpfInboundTransactionIdPolicy.java; cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java; cpf-core/src/test/java/com/cpf/core/common/transaction/CpfInboundTransactionIdPolicyTest.java; cpf-tools/verification/final-dev/verify-transactionid-trust-lineage.py
- Consumer/Call path: Ingress -> TransactionContextFilter(@Order HIGHEST_PRECEDENCE) -> CpfInboundTransactionIdPolicy -> transaction context
- Expected: An officially authenticated first-hop Channel/System can generate one valid CPF transactionId; verified mTLS/proxy identity is mapped to authoritative system identity before policy evaluation.
- Actual: trustedTransport accepts mTLS/trusted proxy, but authoritativeStarterSystem only reads custom server attributes; the highest-precedence transaction filter evaluates before a normal downstream producer. Unit tests inject attributes directly and do not prove product wiring.
- Root Cause: Trust decision and authoritative starter-system identity production are separate, but no current product wiring/order proof connects them.
- Risk: Valid official-origin transactions can be rejected; end-to-end lineage contract cannot be certified for real ingress.
- Development Acceptance: Provide a server-side authenticated starter identity adapter/filter before transaction policy evaluation or derive verified mTLS identity safely. Add filter-order/product integration tests. Never trust raw client Channel headers.
- Reverification: mTLS, trusted gateway, internal propagation, spoofed client, altered txid and retry cases must all execute through real filter wiring with same transactionId where authorized.

## QA-B-1000-NEW-004 — P0 — FileLog recovery queue has head-of-line blocking under persistent target failure

- Requirement: CPF-FILELOG / CPF-LOGFAIL / CPF-RV-0039 / CPF-RV-0658 / CPF-RV-0668
- Reproduction: Queue two spool entries: oldest target remains read-only/unavailable; later target is healthy. Run scheduled replay repeatedly.
- Actual Source: cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogRecoverySpool.java; cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java; cpf-core/src/test/java/com/cpf/core/common/logging/file/CpfFileLogRecoveryContractTest.java
- Consumer/Call path: FileLog append failure -> durable spool -> scheduled replay -> hardened writer
- Expected: Per-item retry/backoff is fair; one failed target does not starve unrelated recoverable spool entries.
- Actual: Replay is ordered; the first FileSystemException/IOException touches the failed item and break exits the loop. A persistent oldest failure blocks every later healthy entry and can exhaust the bounded spool.
- Root Cause: Global ordered replay loop uses break on item-local transient failure instead of item-local scheduling/fairness.
- Risk: Unrelated logs can remain stranded or hit terminal loss during a single-target disk/permission incident.
- Development Acceptance: Use per-item next-attempt/backoff and continue bounded replay across independent targets; add target-level circuit/fairness and no-starvation tests.
- Reverification: Failed-head + healthy-tail, disk-full/read-only, restart, concurrent replayer and maxEntries pressure tests prove healthy items progress and no duplicates occur.

## QA-B-1000-NEW-005 — P0 — Persistence MyBatis starter reuses com.cpf.core package namespace and drifts from canonical packageBase

- Requirement: ARCH-BOUNDARY / ARCH-STARTER / RULE-ARCH / PROD-PACKAGE / CPF-RV-0032 / CPF-RV-0051 / CPF-RV-0056
- Reproduction: Compare cpf-starter-catalog.json packageBase for persistence-mybatis with Java package declarations under cpf-starters/data/persistence-mybatis.
- Actual Source: cpf-tools/generator/contracts/cpf-starter-catalog.json; cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/core/config/CpfMyBatisConfig.java; cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/core/mapper/common/logging/TransactionLogMapper.java; cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/starter/persistence/mybatis/logging/JdbcTransactionLineageProjectionAdapter.java; cpf-tools/verification/final-dev/verify-core-persistence-boundary.py
- Consumer/Call path: settings/catalog -> starter packaging -> Spring MapperScan/config -> consumer classpath
- Expected: Starter-owned implementation uses canonical com.cpf.starter.data.persistence.mybatis... namespace; cpf-core namespace is owned by cpf-core only.
- Actual: Starter declares classes in com.cpf.core.config and com.cpf.core.mapper..., and lineage adapter uses com.cpf.starter.persistence.mybatis... while catalog packageBase is com.cpf.starter.data.persistence.mybatis. Existing boundary gate scans only cpf-core source.
- Root Cause: Physical module move was completed without package-namespace ownership migration; boundary gate is module-root-local, not repo-wide.
- Risk: Split-package/ownership ambiguity, catalog drift and accidental core-internal exposure remain in the published classpath.
- Development Acceptance: Move provider config/mapper/adapters under canonical starter namespace (or formally change catalog with architecture approval); update scans/resources; add repo-wide package-owner gate.
- Reverification: Repo-wide package-owner mutation must fail if any downstream module declares com.cpf.core.* or if starter package falls outside its catalog packageBase.

## QA-B-1000-NEW-006 — P1 — Public API/SPI Korean Javadoc contract is not met in newly introduced core contracts

- Requirement: DEVEX-COMMENT / RULE-QUALITY / CPF-RV-0021~0025 / CPF-RV-0901~0910
- Reproduction: Inspect public lineage persistence ports/record and transaction trust API source.
- Actual Source: cpf-core/src/main/java/com/cpf/core/common/logging/spi/CpfTransactionLineageProjectionPort.java; cpf-core/src/main/java/com/cpf/core/common/logging/spi/CpfTransactionLogPersistencePort.java; cpf-core/src/main/java/com/cpf/core/common/logging/lineage/CpfTransactionLineageRecord.java; cpf-core/src/main/java/com/cpf/core/common/transaction/CpfInboundTransactionIdPolicy.java
- Consumer/Call path: Framework customers and provider implementers consuming public API/SPI
- Expected: Korean-centered Javadoc explains responsibility, params/return/throws/null/default, side effects and thread/transaction semantics where applicable.
- Actual: Inspected new public contracts contain English one-line class comments and public methods without the required method contract documentation.
- Root Cause: Functional finalization added contracts without applying the documented API documentation quality gate to the new surface.
- Risk: Commercial framework extension contracts are ambiguous and the explicit source-quality acceptance is unmet.
- Development Acceptance: Add Korean-centered contract Javadocs and method tags/semantics; generate Javadocs under release Java/Gradle and enforce warning/error policy.
- Reverification: Javadoc task at Java25/Gradle9.1 exits 0 and source review proves contract accuracy; removing required docs fails quality gate.

## QA-B-1000-NEW-007 — P0 — HIGH/CRITICAL generated-client gate does not require the typed Orval client it tracks

- Requirement: API-CONTRACT / ADM-UX / ADM-APPROVAL / CPF-RV-0030 / CPF-RV-0043 / CPF-RV-0421 / CPF-RV-0506
- Reproduction: Inspect verify-operation-consumer.mjs: generatedConsumed and typedGeneratedConsumed are both computed, but high-risk enforcement checks only generatedConsumed. Inspect ApprovalsPage import path and generated compatibility request types.
- Actual Source: cpf-admin/frontend/scripts/verify-operation-consumer.mjs; cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue; cpf-admin/frontend/src/generated/cpf-api.ts; cpf-admin/frontend/openapi/cpf-openapi.json
- Consumer/Call path: ADM HIGH/CRITICAL route -> frontend generated client -> backend operation contract
- Expected: HIGH/CRITICAL mutations use a fully typed generated operation/model; generic compatibility Record<string,unknown> request bodies cannot satisfy the typed-client release gate.
- Actual: The gate accepts imports from generated/cpf-api as generatedConsumed even when not typedGeneratedConsumed. ApprovalsPage uses that compatibility client, whose high-risk bodies such as AdmApprovalPolicySaveBody are Record<string,unknown>. Checked-in source OpenAPI is pre-runtime and releaseEligible=false.
- Root Cause: Gate enforcement uses the broader generated set although it separately tracks the typed generated set.
- Risk: High-risk request schema drift can compile and the release gate can report generated-client compliance without typed request protection.
- Development Acceptance: Require typedGeneratedConsumed (Orval or equivalent concrete generated model) for every HIGH/CRITICAL mutation; reject generic Record<string,unknown> high-risk bodies; keep runtime OpenAPI zero-drift mandatory.
- Reverification: Replace one typed high-risk consumer with compatibility generated client: gate must fail. Restore concrete typed model: gate passes only with runtime OpenAPI current-SHA parity.

## Mandatory after source rework

After all P0/P1 are fixed:
1. regenerate current-SHA developer evidence/manifest/hashes;
2. run full source/static/mutation gates;
3. run all 13 mandatory runtime qualification axes;
4. perform independent Codex review;
5. return the same requirement/finding IDs to QA for reinspection.

No gate weakening is accepted as a fix.
