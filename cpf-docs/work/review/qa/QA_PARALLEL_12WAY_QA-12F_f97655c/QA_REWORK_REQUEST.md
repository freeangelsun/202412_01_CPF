# QA REWORK REQUEST — QA-12F

- Baseline: `f97655c1299936a1101bc3ec10239265ec3b502e`
- 개발GPT 재개발 요청 Finding: `24`
- 행별 상세 상태: `REQUIREMENT_STATUS.csv`, `SCENARIO_STATUS.csv`
- 공통 영향 연결: `FINDING_IMPACT.csv`
- 직접수정 항목은 이 문서의 재개발 완료로 간주하지 않으며 개발GPT/Codex 교차검토 후 QA 재검수한다.

## QA12F-RES-001 — RECOVERY_PROBE_END_TO_END_MISSING
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `7`건
- 영향 Scenario: `7`건
- 현재 Source/호출 문제: Recovery Probe 전용 durable state/worker/domain·batch consumer 및 실제 3-vendor/multi-instance runtime 없음
- Root Cause: Recovery Probe-specific query/command, durable probe state, worker claim/lease and Domain/Batch/ADM consumer path were not found.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-003 — STRUCTURED_LOG_SCHEMA_VERSION_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `16`건
- 영향 Scenario: `30`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: Structured log record exposes apiVersion but no explicit immutable schema/version compatibility contract for all producers/consumers.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-004 — ASYNCHRONOUS_WRITER_INLINE_DB_PATH
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: Event listener performs DB save/fallback inline without bounded async queue, rejection/backpressure and shutdown drain contract.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-005 — SPOOL_FSYNC_AND_DOUBLE_FAILURE_DURABILITY_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `24`건
- 영향 Scenario: `45`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: Durable spool has claim/lease/retry, but explicit fsync and durable signal for simultaneous DB+spool failure were not demonstrated.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-006 — DYNAMIC_LOG_LEVEL_NO_CAS_PARTIAL_DIVERGENCE
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: Rule revision/CAS is absent and runtime→DB→broadcast→audit partial failure can leave instances divergent without durable reconcile.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-007 — LOG_POLICY_NO_CAS_DISTRIBUTION_CONVERGENCE
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: Version/checksum exists, but expected-version CAS, durable multi-instance ACK/NACK and rollback convergence were not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-LOG-008 — GENERAL_RETENTION_PROVIDER_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: 실제 비동기 executor, DB+spool 이중 장애, Windows ACL, multi-instance policy convergence 미검증
- Root Cause: File-specific retention exists, but one canonical retention provider/consumer path for all required logs/artifacts was not identified.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-TRACE-001 — OPERATION_SPAN_CONSUMER_COMPLETENESS_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `72`건
- 영향 Scenario: `135`건
- 현재 Source/호출 문제: 특정 Local/Remote/Message/Batch/File span consumer 완결성, baggage allowlist, cardinality budget, 실제 OTLP exporter runtime 미검증
- Root Cause: Generic OTel adapter exists, but dedicated Local/Remote/Message/Batch/File span lifecycle consumers were not completely connected and evidenced.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-TRACE-002 — SAMPLER_POLICY_EXPORTER_CONVERGENCE_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `16`건
- 영향 Scenario: `30`건
- 현재 Source/호출 문제: 특정 Local/Remote/Message/Batch/File span consumer 완결성, baggage allowlist, cardinality budget, 실제 OTLP exporter runtime 미검증
- Root Cause: CPF sampling/boost decisions and exporter sampler convergence/persistence across instances were not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-TRACE-003 — BAGGAGE_ALLOWLIST_CARDINALITY_BUDGET_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `16`건
- 영향 Scenario: `30`건
- 현재 Source/호출 문제: 특정 Local/Remote/Message/Batch/File span consumer 완결성, baggage allowlist, cardinality budget, 실제 OTLP exporter runtime 미검증
- Root Cause: Sensitive-key filtering exists, but explicit baggage allowlist and bounded attribute cardinality/value length policy are absent.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-MASK-003 — MASKING_POLICY_VERSION_CAS_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: Raw View Approval·Browser zeroization·policy CAS/distribution/actual browser E2E 미검증
- Root Cause: Immutable snapshot replacement exists, but explicit revision/CAS/persistence/distribution convergence is absent.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-MASK-004 — RAW_VIEW_BROWSER_ZEROIZATION_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `16`건
- 영향 Scenario: `30`건
- 현재 Source/호출 문제: Raw View Approval·Browser zeroization·policy CAS/distribution/actual browser E2E 미검증
- Root Cause: Mandatory approval/short-lived raw view and browser memory zeroization were not proven by an actual frontend workflow/runtime test.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-FILE-001 — FSYNC_ATOMIC_PROCESS_KILL_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `24`건
- 영향 Scenario: `45`건
- 현재 Source/호출 문제: fsync, atomic-move unsupported fallback, real SFTP resume/range/cancel, disk-full/process-kill runtime 미검증
- Root Cause: Temporary/rollback/checksum guards exist, but explicit fsync and strict atomic publish are absent; unsupported atomic move falls back to non-atomic move.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-QUERY-001 — CANONICAL_QUERY_CONTRACT_PROVIDER_MISSING
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `136`건
- 영향 Scenario: `255`건
- 현재 Source/호출 문제: 17 query feature contracts lack one canonical registry/provider/consumer/evidence chain
- Root Cause: No versioned Query ID/parameter/result/sort/filter/paging/mutation contract with a universal provider and all-DB-consumer path was identified.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-QUERY-002 — QUERY_PLAN_AND_ORPHAN_OPERATIONS_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `24`건
- 영향 Scenario: `45`건
- 현재 Source/호출 문제: 17 query feature contracts lack one canonical registry/provider/consumer/evidence chain
- Root Cause: Plan baseline, slow-query and orphan-query detection lack one canonical operations store/query/control/evidence chain.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-EVT-001 — EVENT_ENVELOPE_VERSION_ALLOWLIST_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `48`건
- 영향 Scenario: `90`건
- 현재 Source/호출 문제: envelope version/TTL/attribute+serializer allowlist/schema compatibility and actual consumer concurrency/multi-instance runtime incomplete
- Root Cause: Message ID/topic/key/payload exist, but canonical envelope version, TTL, attribute/serializer allowlist and schema compatibility enforcement are incomplete.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-EVT-002 — EVENT_CONSUMER_CONCURRENCY_RUNTIME_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `56`건
- 영향 Scenario: `105`건
- 현재 Source/호출 문제: envelope version/TTL/attribute+serializer allowlist/schema compatibility and actual consumer concurrency/multi-instance runtime incomplete
- Root Cause: Consumer ACK/redelivery/backpressure/concurrency/multi-instance correlation require actual provider runtime and were not fully implemented uniformly.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-KAFKA-001 — KAFKA_CONSUMER_OPERATIONS_RUNTIME_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `128`건
- 영향 Scenario: `240`건
- 현재 Source/호출 문제: actual broker TLS/topic provisioning/transaction/consumer ACK-rebalance/backpressure/health/metrics runtime 미검증
- Root Cause: Producer and UNKNOWN mapping exist; complete consumer transaction/ACK/redelivery/ordering/backpressure/health/reconnect/metrics runtime is not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-RABBIT-001 — RABBIT_CONSUMER_OPERATIONS_RUNTIME_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `128`건
- 영향 Scenario: `240`건
- 현재 Source/호출 문제: actual exchange/queue, TLS, consumer ACK/redelivery/backpressure/reconnect/metrics runtime 미검증
- Root Cause: Publisher confirms exist; complete consumer ACK/redelivery/ordering/backpressure/reconnect/metrics runtime is not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-JMS-001 — JMS_CONSUMER_OPERATIONS_RUNTIME_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `128`건
- 영향 Scenario: `240`건
- 현재 Source/호출 문제: actual provider destination/transaction/consumer ACK/redelivery/reconnect/metrics runtime 미검증
- Root Cause: Provider-neutral producer exists; provider transaction/consumer ACK/redelivery/backpressure/reconnect/metrics path is not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-IBMMQ-001 — IBM_MQ_CONSUMER_OPERATIONS_RUNTIME_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `128`건
- 영향 Scenario: `240`건
- 현재 Source/호출 문제: actual queue manager TLS/transaction/consumer/reconnect/metrics runtime 미검증
- Root Cause: Reason mapping and producer exist; queue-manager consumer/transaction/reconnect/metrics path is not proven.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-OUTBOX-001 — REPLAY_APPROVAL_BYPASSABLE
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `8`건
- 영향 Scenario: `15`건
- 현재 Source/호출 문제: actual 3-vendor concurrent claim/fencing, mandatory replay approval boundary, cleanup/timeline runtime incomplete
- Root Cause: Repository replay changes state without an approval token or mandatory two-person approval contract.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-OUTBOX-002 — OUTBOX_CLEANUP_TIMELINE_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `16`건
- 영향 Scenario: `30`건
- 현재 Source/호출 문제: actual 3-vendor concurrent claim/fencing, mandatory replay approval boundary, cleanup/timeline runtime incomplete
- Root Cause: Cleanup retention and complete operations timeline APIs were not identified in the reference repository.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

## QA12F-EXT-001 — EXTERNAL_CALLBACK_RECONCILE_CONFIRM_GAP
- 재현: Baseline `f97655c1299936a1101bc3ec10239265ec3b502e` Source와 연결 Scenario를 검토하고 `SOURCE_TRACE_MATRIX.csv` Evidence를 대조.
- 영향 Requirement: `24`건
- 영향 Scenario: `45`건
- 현재 Source/호출 문제: real institution endpoints, TLS/host key, half-open, response-loss, resume/checksum, callback/reconcile/manual-confirm runtime 미검증
- Root Cause: Institution callback, durable external reconciliation and manual confirmation do not have one mandatory state/audit/approval path.
- 수정 대상: ``
- 필수 Test: 누락 Public 계약/Provider/Consumer/저장소/운영 경로 구현 후 contract+fault+runtime+regression Test
- 성공 기대: Public API→Provider→state→consumer→operations→recovery가 exact SHA에서 완결된다.
- 실패 기준: Interface/Mock-only, consumer 부재, 상태 미저장, UNKNOWN/recovery 미완결 또는 보안/감사 누락.
- Evidence: `cpf-docs/evidence/qa/QA_PARALLEL_12WAY_QA-12F_f97655c/SOURCE_TRACE_MATRIX.csv`
- 미조치 위험: Framework 소비자가 직접 OSS/API/DB를 우회하거나 장애 시 중복·유실·통제 실패.

