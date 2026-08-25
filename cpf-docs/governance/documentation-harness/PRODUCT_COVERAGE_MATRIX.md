# CPF Product Coverage Matrix

모든 항목은 현재 Source에서 실제 존재/경계를 다시 확인하고 문서에 반영한다. Source에 새 Public Capability가 나타나 기존 항목에 매핑되지 않으면 자동으로 항목을 만들지 않고 `HARNESS_CHANGE_REQUIRED`를 기록한다.

| ID | 기능/계약 | Primary | Secondary | 추가 노출 |
|---|---|---|---|---|
| `ARCH_OWNER` | Module Ownership/Public API/SPI/Internal | `ARCHITECTURE_DESIGN` | `SPECIFICATION` | `README`, `TECHNICAL_STANDARD` |
| `TOPOLOGY` | Same JVM/분리 WAS/MSA/Multi-instance 및 Domain Invocation parity | `ARCHITECTURE_DESIGN` | `FRAMEWORK_DEVELOPER_GUIDE` | `README`, `SPECIFICATION` |
| `SYSTEM6` | Canonical System6 생성·전파·검증·신뢰경계 | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `ARCHITECTURE_DESIGN`, `OPERATOR_MANUAL` |
| `OPERATION_ID` | operationId 단일 정본/Registry/Policy/Discovery | `SPECIFICATION` | `OPERATOR_MANUAL` | `README`, `FRAMEWORK_DEVELOPER_GUIDE` |
| `INSTANCE_ID` | Runtime instanceId 결정·중복·관측 | `OPERATOR_MANUAL` | `SPECIFICATION` | `README`, `ARCHITECTURE_DESIGN` |
| `RESULT_ERROR` | 표준 Result/Error/Business/Technical/UNKNOWN | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `LOCAL_TX` | Local Transaction REQUIRED/REQUIRES_NEW 및 경계 | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `TECHNICAL_STANDARD` |
| `DISTRIBUTED_TX` | Remote Side Effect/Saga/TCC/XA/Compensation | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `IDEMPOTENCY` | Durable Idempotency/duplicate/retry safety | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `BATCH_DEVELOPER_GUIDE` |
| `RECONCILE` | UNKNOWN/Reconcile/결과 확정 | `OPERATOR_MANUAL` | `FRAMEWORK_DEVELOPER_GUIDE` | `README`, `BATCH_OPERATOR_GUIDE` |
| `ASYNC_WORKFLOW` | Async operation/Workflow/context propagation | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `STARTER` | Public Profile/Starter/Capability/Provider/internal leaf | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_SPECIFICATION` | `README`, `ARCHITECTURE_DESIGN` |
| `COMMON` | cpf-common Framework 공통 기능과 Runtime wiring 경계 | `FRAMEWORK_DEVELOPER_GUIDE` | `ARCHITECTURE_DESIGN` | `README`, `SPECIFICATION` |
| `GENERATED_DOMAIN` | Generated Domain IA/business feature/package/base rules | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_STANDARD` | `README`, `ARCHITECTURE_DESIGN` |
| `DOMAIN_SETUP` | Domain create/setup/sync/diff/remove/verify lifecycle | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_SPECIFICATION` | `README` |
| `PERSISTENCE` | JDBC/MyBatis/JPA/Repository/Paging/Cursor/Bulk/Lock | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_STANDARD`, `DATABASE_STANDARD` |
| `CACHE` | Caffeine/Redis/Valkey/TTL/Invalidation/Multi-instance/Stampede | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `LOCK` | Distributed Lock/optimistic concurrency | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `DATABASE_STANDARD`, `TECHNICAL_STANDARD` |
| `DB3` | Oracle/PostgreSQL/MariaDB logical/physical binding | `DATABASE_STANDARD` | `TECHNICAL_SPECIFICATION` | `README`, `ARCHITECTURE_DESIGN` |
| `DB_LIFECYCLE` | Fresh Init/Migration/Seed/Upgrade/Rollback/Recovery/Runtime Query | `DATABASE_STANDARD` | `TECHNICAL_SPECIFICATION` | `README` |
| `BACKOFFICE` | cpf-backoffice Owner/optional business domain boundary | `ARCHITECTURE_DESIGN` | `FRAMEWORK_DEVELOPER_GUIDE` | `README` |
| `BACKOFFICE_WEB` | DB-less Channel/BFF/browser session/CSRF boundary | `ARCHITECTURE_DESIGN` | `TECHNICAL_SPECIFICATION` | `README` |
| `GATEWAY` | Optional Gateway/route/trust/rate/resilience/admission/drain | `GATEWAY_GUIDE` | `ARCHITECTURE_DESIGN` | `README`, `OPERATOR_MANUAL` |
| `EXT_HTTP` | External HTTP/timeout/retry/deadline/credential/UNKNOWN | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `FIXED_LENGTH` | Fixed-length layout/version/encoding/byte length/masking | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_STANDARD` |
| `GRAPHQL_REALTIME` | GraphQL 및 Realtime/SSE 선택 Capability | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_SPECIFICATION` | `SPECIFICATION` |
| `MESSAGING` | Kafka/RabbitMQ/JMS/IBM MQ/Event Schema/Outbox/Inbox/DLQ/Replay | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `FILE` | Attachment/Object Storage S3/File transfer/checksum/inspection | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `README`, `OPERATOR_MANUAL` |
| `SECURITY` | AuthN/OIDC/JWT/SSO/AuthZ/Permission/Role/Session | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_STANDARD` | `README`, `OPERATOR_MANUAL` |
| `CRYPTO_PRIVACY` | Encryption/Crypto/Secret/Cert/KMS/HSM/Masking/Retention | `TECHNICAL_STANDARD` | `SPECIFICATION` | `OPERATOR_MANUAL`, `README` |
| `APPROVAL_AUDIT` | Approval/SoD/Break-glass/Reason/Audit | `OPERATOR_MANUAL` | `FRAMEWORK_DEVELOPER_GUIDE` | `README`, `TECHNICAL_STANDARD` |
| `BATCH_DEV` | Tasklet/Chunk/Partition/Remote Chunk/Remote Step/On-demand | `BATCH_DEVELOPER_GUIDE` | `SPECIFICATION` | `README` |
| `BATCH_TOPOLOGY` | Control Plane/Scheduler/Worker/Center-Cut/Agent | `BATCH_DEVELOPER_GUIDE` | `BATCH_OPERATOR_GUIDE` | `README`, `ARCHITECTURE_DESIGN` |
| `BATCH_RECOVERY` | Restart/Rerun/Reprocess/Reconcile/Checkpoint/Process kill | `BATCH_OPERATOR_GUIDE` | `BATCH_DEVELOPER_GUIDE` | `README`, `OPERATOR_MANUAL` |
| `BATCH_LEASE` | Lease/Fencing/Heartbeat/stale worker/reassignment | `BATCH_OPERATOR_GUIDE` | `BATCH_DEVELOPER_GUIDE` | `ARCHITECTURE_DESIGN` |
| `ADM` | ADM platform control plane: health/log/trace/config/incident/recovery/runtime/batch/gateway/security | `OPERATOR_MANUAL` | `ARCHITECTURE_DESIGN` | `README`, `TECHNICAL_SPECIFICATION` |
| `OPENAPI_FRONTEND` | OpenAPI operationId/generated client/route coverage/error/accessibility | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` | `ARCHITECTURE_DESIGN`, `OPERATOR_MANUAL` |
| `OBSERVABILITY` | Log/Metric/Trace/Timeline/transaction-operation-instance correlation | `OPERATOR_MANUAL` | `SPECIFICATION` | `README`, `FRAMEWORK_DEVELOPER_GUIDE` |
| `CONFIG` | Safe default/property/profile/policy/per-call precedence/dynamic/secret | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_SPECIFICATION` | `OPERATOR_MANUAL`, `TECHNICAL_STANDARD` |
| `BOOTSTRAP_CLI` | Prerequisite→DB→Migration→Seed→Build/Test→Runtime/Health; CLI inventory | `FRAMEWORK_DEVELOPER_GUIDE` | `TECHNICAL_SPECIFICATION` | `README` |
| `EDU35` | Online 20 + Batch 15 canonical education coverage | `FRAMEWORK_DEVELOPER_GUIDE` | `BATCH_DEVELOPER_GUIDE` | `DELIVERABLE_INDEX` |
| `BUILD_RELEASE` | Java25/build/test/BOM/publication/SBOM/license/vulnerability/secret/reproducibility | `TECHNICAL_SPECIFICATION` | `TECHNICAL_STANDARD` | `ARCHITECTURE_DESIGN` |
| `PUBLIC_WORKSPACE` | Public developer workspace/binary distribution/release acceptance (README에서는 내부 Release 용어를 주 사용자 용어로 쓰지 않음) | `TECHNICAL_SPECIFICATION` | `ARCHITECTURE_DESIGN` | `DELIVERABLE_INDEX` |
| `COMMON_CODE_PARAM` | cpf-common 공통 코드/파라미터/메시지/영업일/Template | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION`, `README` |
| `DATA_QUALITY` | Data Quality Rule/Decision/Correction | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_STANDARD` |
| `ARCHIVE_TABULAR` | Archive/Checksum/Extract 및 Tabular Read/Write | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `NOTIFICATION` | Notification Operations/Provider/Receipt/Reconcile | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `OPERATOR_MANUAL`, `TECHNICAL_SPECIFICATION` |
| `AI_INTEGRATION` | AI Operations/Provider/Policy/Risk/Telemetry/UNKNOWN | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `FEATURE_FLAG_STATE` | Platform State Operations/Feature Flag/State Transition | `OPERATOR_MANUAL` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `RUNTIME_CONTROL` | Managed Runtime Start/Stop/Restart/Drain/Result tracking | `OPERATOR_MANUAL` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION`, `ARCHITECTURE_DESIGN` |
| `SERVICE_REGISTRY` | Service/Channel Registry endpoint/version/zone/weight/health/draining/TTL | `OPERATOR_MANUAL` | `ARCHITECTURE_DESIGN` | `SPECIFICATION` |
| `DYNAMIC_LOG` | Dynamic Log Level/Log Policy/Runtime status | `OPERATOR_MANUAL` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `INCIDENT` | Incident lifecycle/recovery/evidence | `OPERATOR_MANUAL` | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` |
| `DIGITAL_SIGNATURE` | Digital Signature/Key lifecycle/Rekey | `FRAMEWORK_DEVELOPER_GUIDE` | `SPECIFICATION` | `TECHNICAL_STANDARD` |
| `MASKING_SENSITIVE` | Masking Policy/Sensitive Data Access/Approval/Audit | `FRAMEWORK_DEVELOPER_GUIDE` | `OPERATOR_MANUAL` | `SPECIFICATION`, `TECHNICAL_STANDARD` |
| `OPENAPI_OPERATIONS` | OpenAPI snapshot/operationId/generated client/coverage | `SPECIFICATION` | `TECHNICAL_SPECIFICATION` | `FRAMEWORK_DEVELOPER_GUIDE`, `OPERATOR_MANUAL` |
| `JOB_PACK_DEPLOY` | Batch Job Pack/Artifact/Deployment/Approved Launch/Rollback | `BATCH_DEVELOPER_GUIDE` | `BATCH_OPERATOR_GUIDE` | `SPECIFICATION`, `OPERATOR_MANUAL` |
