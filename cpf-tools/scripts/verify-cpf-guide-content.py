#!/usr/bin/env python3
import argparse, json, re, sys
from pathlib import Path

BASE_SHA = "1536a0d59004ebade7dcb29383cbe2e758547f8e"
CANONICAL = [
    "cpf-docs/guides/README.md",
    "cpf-docs/guides/00_프레임워크안내.md",
    "cpf-docs/guides/01_개발자매뉴얼.md",
    "cpf-docs/guides/02_배치개발매뉴얼.md",
    "cpf-docs/guides/03_ADM개발자매뉴얼.md",
    "cpf-docs/guides/04_ADM운영자매뉴얼.md",
    "cpf-docs/guides/05_플랫폼운영매뉴얼.md",
    "cpf-docs/guides/90_BZA매뉴얼.md",
    "cpf-docs/guides/91_게이트웨이매뉴얼.md",
]
MIN_LINES = {
    "00_프레임워크안내.md": 220,
    "01_개발자매뉴얼.md": 550,
    "02_배치개발매뉴얼.md": 500,
    "03_ADM개발자매뉴얼.md": 480,
    "04_ADM운영자매뉴얼.md": 4800,
    "05_플랫폼운영매뉴얼.md": 2200,
    "90_BZA매뉴얼.md": 1600,
    "91_게이트웨이매뉴얼.md": 450,
}
ADM_ROUTES = ["dashboard", "topology", "capacity", "logs", "transactionGroups", "transactions", "remoteLogs", "auditLogs", "logLevel", "logPolicies", "standardExecutions", "channelPolicy", "serviceRegistry", "runtimeControl", "maintenance", "cache", "configs", "responseCodes", "businessCalendar", "recoveryCenter", "incidents", "reliability", "notifications", "batch", "batch-overview", "batch-runtime", "batch-instances", "batch-scheduler", "batch-worker-pools", "batch-center-cut", "batch-agents", "batch-job-packs", "batch-executions", "batch-deployment", "batch-recovery", "batch-leases", "batch-alerts", "batch-audit", "workers", "downloads", "file-jobs", "messages", "codes", "gateway-dashboard", "gateway-servers", "gateway-groups", "gateway-routes", "gateway-security", "gateway-health", "gateway-transactions", "gateway-log-policies", "gateway-apply-status", "permissions", "password", "security", "operators", "secrets", "approvals", "breakGlass"]
BZA_ROUTES = ["dashboard", "organizations", "employees", "positions", "jobTitles", "assignments", "organizationResponsibilities", "users", "roles", "userRoles", "menus", "permissions", "permissionTools", "approvalInbox", "approvalSubmissions", "approvalPolicies", "approvalSimulation", "approvalDelegations", "sessions", "audits", "notifications", "attachments", "savedSearches", "settings", "downloads", "downloadAudits"]
PROPERTY_KEYS = ["CPF_ARTIFACT_MODE", "cpfArtifactRepositoryUrl", "cpfLocalArtifactRepository", "cpfOfflineArtifactRepository", "cpfSourceSha", "cpf.security.session.cookie-name", "cpf.security.session.timeout", "cpf.security.session.secure", "cpf.security.session.same-site", "cpf.security.session.cookie-path", "cpf.security.session.fail-closed", "cpf.messaging.kafka.acknowledgement-timeout", "cpf.messaging.kafka.maximum-payload-bytes", "cpf.messaging.kafka.require-idempotence", "cpf.cache.caffeine.maximum-size", "cpf.cache.caffeine.maximum-payload-bytes", "cpf.batch.execution.default-chunk-size", "cpf.batch.execution.max-partition-count", "cpf.batch.execution.remote-poll-interval-ms", "cpf.batch.execution.remote-timeout-ms", "cpf.batch.execution.remote-chunk-max-wait-timeouts", "cpf.batch.execution.remote-chunk-throttle-limit", "cpf.batch.remote.kafka.request-topic", "cpf.batch.remote.kafka.reply-topic", "cpf.batch.remote.kafka.consumer-group", "cpf.batch.remote.kafka.reply-group", "cpf.batch.remote.kafka.role", "cpf.batch.remote.kafka.send-timeout", "spring.batch.job.enabled", "spring.batch.jdbc.initialize-schema", "cpf.batch.remote.transport", "cpf.batch.remote.worker-enabled", "cpf.agent.artifact-repository-base-url", "cpf.agent.artifact-public-key-path", "cpf.agent.require-signature", "cpf.agent.max-artifact-bytes", "cpf.agent.max-log-archive-bytes", "cpf.agent.process-timeout-seconds", "cpf.agent.max-process-output-bytes", "cpf.agent.log-archive-ttl-seconds", "cpf.agent.allowed-client-subjects"]
FORBIDDEN = ["TODO", "TBD", "추후 작성", "내용 보강 필요", "Lorem ipsum"]
REQUIRED_MARKERS = {
    "01_개발자매뉴얼.md": ["create-domain.ps1", "UNKNOWN_RESULT", "Outbox", "Idempotency", "OpenAPI", "Playwright"],
    "02_배치개발매뉴얼.md": ["JobOperator", "JobRepository", "ExecutionContext", "fencingToken", "/internal/v1/center-cut"],
    "03_ADM개발자매뉴얼.md": ["Vue Router", "Pinia", "TanStack", "Orval", "Permission", "UNKNOWN_RESULT"],
    "04_ADM운영자매뉴얼.md": ["operationId", "승인", "감사", "UNKNOWN_RESULT", "Reconcile"],
    "05_플랫폼운영매뉴얼.md": ["Property Reference", "Migration Drift", "Backup", "Rollback", "UNKNOWN_RESULT"],
    "90_BZA매뉴얼.md": ["Bootstrap", "menuCode", "Data Scope", "Approval", "Audit"],
    "91_게이트웨이매뉴얼.md": ["CpfScgPrimaryHandler", "Attempt Ledger", "ACK/NACK", "UNKNOWN_RESULT", "Rollback"],
}

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--json-out")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    errors = []
    metrics = {}

    for rel in CANONICAL:
        p = root / rel
        if not p.is_file():
            errors.append(f"missing: {rel}")
            continue
        text = p.read_text(encoding="utf-8")
        lines = text.splitlines()
        metrics[rel] = {
            "lines": len(lines),
            "characters": len(text),
            "headings": sum(1 for x in lines if x.startswith("#")),
            "codeFences": text.count("```") // 2,
        }
        if rel != "cpf-docs/guides/README.md" and BASE_SHA not in text:
            errors.append(f"base SHA missing: {rel}")
        minimum = MIN_LINES.get(p.name)
        if minimum and len(lines) < minimum:
            errors.append(f"too short: {rel} {len(lines)} < {minimum}")
        for word in FORBIDDEN:
            if word in text:
                errors.append(f"forbidden placeholder {word}: {rel}")
        for marker in REQUIRED_MARKERS.get(p.name, []):
            if marker not in text:
                errors.append(f"required marker {marker} missing: {rel}")
        if text.count("```") % 2:
            errors.append(f"unbalanced code fence: {rel}")

    adm = root / "cpf-docs/guides/04_ADM운영자매뉴얼.md"
    if adm.is_file():
        text = adm.read_text(encoding="utf-8")
        for route in ADM_ROUTES:
            expected = "`/`" if route == "dashboard" else f"`/{route}`"
            if expected not in text:
                errors.append(f"ADM route missing: {route}")

    bza = root / "cpf-docs/guides/90_BZA매뉴얼.md"
    if bza.is_file():
        text = bza.read_text(encoding="utf-8")
        for route in BZA_ROUTES:
            if route not in text:
                errors.append(f"BZA route missing: {route}")

    ops = root / "cpf-docs/guides/05_플랫폼운영매뉴얼.md"
    if ops.is_file():
        text = ops.read_text(encoding="utf-8")
        for key in PROPERTY_KEYS:
            if key not in text:
                errors.append(f"property missing: {key}")

    link_pattern = re.compile(r"\[[^\]]+\]\(([^)#]+\.md)(?:#[^)]+)?\)")
    for rel in CANONICAL:
        p = root / rel
        if not p.is_file():
            continue
        text = p.read_text(encoding="utf-8")
        for target in link_pattern.findall(text):
            if "://" in target:
                continue
            resolved = (p.parent / target).resolve()
            if not resolved.is_file():
                errors.append(f"broken md link: {rel} -> {target}")

    report = {
        "schemaVersion": 1,
        "baseSha": BASE_SHA,
        "status": "PASS" if not errors else "FAIL",
        "errors": errors,
        "metrics": metrics,
        "admRouteCount": len(ADM_ROUTES),
        "bzaRouteCount": len(BZA_ROUTES),
        "propertyCount": len(PROPERTY_KEYS),
    }
    if args.json_out:
        out = Path(args.json_out)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if not errors else 1

if __name__ == "__main__":
    sys.exit(main())
