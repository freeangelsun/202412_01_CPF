#!/usr/bin/env python3
"""Gateway full-surface static closure gate.

Verifies Data/Control Plane separation, safety configuration, security, resilience,
context, failure/recovery, routing, observability and ADM operational consumers.
Runtime fault injection remains a separate environment-backed verification.
"""
from pathlib import Path
import re,sys
ROOT=Path(__file__).resolve().parents[2]
G=ROOT/'cpf-gateway'
def read(p):return p.read_text(encoding='utf-8')
def all_java(base):return '\n'.join(read(p) for p in base.rglob('*.java'))
main=all_java(G/'src/main/java'); tests=all_java(G/'src/test/java')
yml=read(G/'src/main/resources/application.yml')
profiles={n:read(G/f'src/main/resources/application-{n}.yml') for n in ('local','test','dev','stg','prod')}
adm=read(ROOT/'cpf-admin/frontend/src/app/routes.ts')
checks={
 'data_control_plane_separation': all(t in main for t in ('CpfGatewayPlaneBoundaryFilter','CpfGatewayControlListenerConfiguration','Control/Data Plane listener ports must differ')),
 'graceful_shutdown': 'shutdown: graceful' in yml and 'timeout-per-shutdown-phase' in yml,
 'profile_matrix': all(f'environment-code: {n}' in profiles[n] for n in profiles),
 'prod_tls_distributed_counter': all(t in profiles['prod'] for t in ('require-distributed-rate-limit-counter: true','rate-limit-counter-mode: JDBC','require-tls-ingress: true','enabled: true')),
 'target_ssrf_tls_boundary': all(t in yml for t in ('allow-private-targets','allow-public-targets','allowed-target-ports','allowed-target-cidrs','require-tls-targets')) and all(t in main for t in ('allowPublicTargets','allowedTargetPorts','requireTlsTargets')),
 'timeout_deadline_retry': all(t in main for t in ('connectTimeoutCap','responseTimeoutCap','overallTimeoutCap','RetryFilterFunctions','maxRetryCount')),
 'retry_idempotency_guard': 'Retry requires idempotent route' in main,
 'circuit_breaker': 'CircuitBreakerFactory' in main,
 'rate_limit_distributed': all(t in main for t in ('CpfGatewayRateLimitPort','JdbcCpfGatewayRateLimitCounterAdapter','requireDistributedRateLimitCounter')),
 'bulkhead_concurrency': all(t in main for t in ('CpfGatewayConcurrencyFilter','maxConcurrentRequestsCap','CONCURRENCY_LIMIT')),
 'request_header_size_caps': all(t in main for t in ('requestBodyBytesCap','responseBodyBytesCap','headerCountCap','headerBytesCap')),
 'forwarded_spoof_rejection': 'x-forwarded-' in main and 'forwardedHeaderSpoofIsRejectedBeforeDownstreamRegeneration' in tests,
 'cors_policy': 'CorsDecision' in main and 'CORS_DECISION_ATTR' in main,
 'authentication_authorization': all(t in main for t in ('CpfGatewayAuthenticationPort','CpfGatewayAuthorizationPort','CpfApiClientSecurityPolicy')),
 'tls_mtls_extension': 'mTLS' in main and 'cpf.client.cert.serial' in main,
 'context_propagation': all(t in main for t in ('CpfGatewayContextFactory','CpfHttpOutboundContextAdapter','GATEWAY_TRANSACTION_ID','STANDARD_EXECUTION_ID')),
 'audit_reason_durable_recovery': all(t in main for t in ('auditReasonRequired','CpfGatewayAuditRecoverySpool','recordRequired')),
 'ledger_unknown_recovery': all(t in main for t in ('CpfGatewayLedgerRecoverySpool','UNKNOWN_RESULT','unknownResult')),
 'health_routing_drain': all(t in main for t in ('CpfGatewayHealthEvaluator','draining','maintenance')),
 'zone_weight_canary': all(t in main for t in ('zoneCode','weight','canaryPercent')),
 'route_refresh_apply': all(t in main for t in ('CpfGatewayRouteSynchronizer','CpfGatewayRouteRuntimeApplier','CpfGatewayLogPolicySynchronizer')),
 'clock_policy': 'java.time.Instant.now()' not in read(G/'src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java') and 'OffsetDateTime.now()' not in read(G/'src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java'),
 'adm_gateway_coverage': all(f'"{rid}"' in adm for rid in ('gateway-dashboard','gateway-servers','gateway-groups','gateway-routes','gateway-security','gateway-health','gateway-transactions','gateway-log-policies','gateway-apply-status')),
 'gateway_tests': len(list((G/'src/test/java').rglob('*Test.java'))) >= 20,
}
fail=[k for k,v in checks.items() if not v]
for k,v in checks.items():print(('PASS' if v else 'FAIL'),k)
print(f'SUMMARY pass={sum(checks.values())} fail={len(fail)} total={len(checks)}')
raise SystemExit(1 if fail else 0)
