#!/usr/bin/env python3
"""QA32 ADOPT_NOW OSS의 실제 Primary Consumer 이관과 Legacy 제거를 검사한다."""
from __future__ import annotations
import argparse, csv, hashlib, json, re, sys
from pathlib import Path

REQUIRED_FILES = {
    "Spring Batch Primary": [
        "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java",
        "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchJobFactory.java",
        "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchRemoteWorkerConfiguration.java",
        "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchKafkaRemoteConfiguration.java",
        "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/DbSchedulerPrimaryConfiguration.java",
    ],
    "SCG MVC Primary": [
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryRouteConfiguration.java",
    ],
    "BFF Session": [
        "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfBffSessionBridgeFilter.java",
        "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfBffCredentialResponseAdvice.java",
        "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfBffCsrfFilter.java",
    ],
    "Frontend Primary": [
        "cpf-admin/frontend/src/app/router.ts", "cpf-admin/frontend/src/stores/admConsoleStore.ts",
        "cpf-biz-admin/frontend/src/app/router.ts",
    ],
    "Resource Streaming": [
        "cpf-core/src/main/java/com/cpf/core/api/archive/CpfArchiveService.java",
        "cpf-core/src/main/java/com/cpf/core/api/attachment/CpfAttachmentStoragePort.java",
    ],
}

REQUIRED_MARKERS = {
    "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchJobFactory.java": [
        "RemotePartitioningManagerStepBuilder", "RemoteChunkingManagerStepBuilder", "RemoteStep", "TaskExecutorPartitionHandler", ".split(taskExecutor)", "conditionalJob"
    ],
    "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java": [
        "JobOperator", "operator.start", "operator.restart", "operator.stop", "operator.abandon", "operator.recover"
    ],
    "cpf-gateway/build.gradle": ["spring-cloud-starter-gateway-server-webmvc"],
    "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java": ["HandlerFunctions.http", "recordAttempt"],
    "cpf-starters/security/build.gradle": ["spring-session-jdbc", "spring-security-web"],
    "cpf-admin/frontend/package.json": ["element-plus", "@tanstack/vue-table", "vue-router", "pinia", "@tanstack/vue-query", "zod", "orval", "@playwright/test"],
    "cpf-biz-admin/frontend/package.json": ["element-plus", "@tanstack/vue-table", "vue-router", "pinia", "@tanstack/vue-query", "zod", "orval", "@playwright/test"],
}

FORBIDDEN_EXISTING = [
    "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayController.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayPublicController.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/transport/JdkCpfGatewayHttpExchangeAdapter.java",
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRuntime.java",
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutDispatcher.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRuntime.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/internal/JdbcWorkerExecutionRepository.java",
    "cpf-admin/frontend/src/app/admConsoleMixin.ts",
]

TEXT_EXT = {".java", ".kt", ".kts", ".gradle", ".ts", ".vue", ".js", ".mjs", ".py", ".ps1", ".yml", ".yaml", ".json", ".properties", ".sql"}
IGNORE_DIRS = {".git", ".gradle", "build", "node_modules", "dist", "coverage", "playwright-report", "test-results"}
FORBIDDEN_PATTERNS = [
    (re.compile(r"location\.hash|hashchange"), "legacy hash router"),
    (re.compile(r"localStorage\.(?:setItem|getItem)\([^\n]*(?:token|access|refresh)", re.I), "browser token persistence"),
    (re.compile(r"Class\.forName\s*\("), "unapproved dynamic class loading"),
    (re.compile(r"readAllBytes\s*\("), "unbounded byte-array read"),
    (re.compile(r"Runtime\.getRuntime\(\)\.exec"), "uncontrolled process execution"),
    (re.compile(r"-ExecutionPolicy\s+Bypass", re.I), "PowerShell policy bypass"),
]
ALLOW_PATTERN_PATHS = {
    "cpf-tools/scripts/verify-cpf-qa32-primary-engines.py",
    "cpf-tools/scripts/test-cpf-qa32-negative-fixtures.py",
    "cpf-tools/scripts/verify-cpf-qa33-repository-closure.py",
    "cpf-tools/config/qa33-repository-closure-policy.json",
    "cpf-admin/frontend/scripts/verify-primary-frontend.mjs",
    "cpf-biz-admin/frontend/scripts/verify-primary-frontend.mjs",
}

def iter_text(root: Path):
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in TEXT_EXT: continue
        if any(part in IGNORE_DIRS for part in path.parts): continue
        yield path

def verify_checksums(root: Path, failures: list[str]):
    for checksum in root.glob("cpf-tools/db/vendor/**/migration/flyway/**/checksums.sha256"):
        for line_no, line in enumerate(checksum.read_text(encoding="utf-8").splitlines(), 1):
            parts = line.split()
            if len(parts) < 2: continue
            sql = checksum.parent / parts[-1].lstrip("*")
            if sql.exists():
                actual = hashlib.sha256(sql.read_bytes()).hexdigest()
                if actual != parts[0]: failures.append(f"checksum mismatch: {sql.relative_to(root)}:{line_no}")

def main() -> int:
    ap = argparse.ArgumentParser(); ap.add_argument("--root", default="."); ap.add_argument("--json-report")
    args = ap.parse_args(); root = Path(args.root).resolve(); failures=[]; checks=0
    for area, files in REQUIRED_FILES.items():
        for rel in files:
            checks += 1
            if not (root/rel).is_file(): failures.append(f"missing {area}: {rel}")
    for rel, markers in REQUIRED_MARKERS.items():
        p=root/rel; text=p.read_text(encoding="utf-8") if p.is_file() else ""
        for marker in markers:
            checks += 1
            if marker not in text: failures.append(f"missing marker {marker!r}: {rel}")
    for rel in FORBIDDEN_EXISTING:
        checks += 1
        if (root/rel).exists(): failures.append(f"legacy primary remains: {rel}")
    for path in iter_text(root):
        rel=path.relative_to(root).as_posix()
        if rel in ALLOW_PATTERN_PATHS: continue
        text=path.read_text(encoding="utf-8", errors="ignore")
        for pattern, label in FORBIDDEN_PATTERNS:
            checks += 1
            if pattern.search(text): failures.append(f"{label}: {rel}")
    # migration parity
    versions={}
    for vendor in ("oracle","postgresql","mariadb"):
        found={p.name.split("__",1)[0] for p in (root/f"cpf-tools/db/vendor/{vendor}").rglob("V8[2-5]__*.sql")}
        versions[vendor]=found; checks += 1
        if found != {"V82","V83","V84","V85"}: failures.append(f"vendor migration parity {vendor}: {sorted(found)}")
    verify_checksums(root, failures)
    report={"checks":checks,"failures":failures,"status":"PASS" if not failures else "FAIL"}
    if args.json_report:
        out=Path(args.json_report); out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(report,ensure_ascii=False,indent=2))
    return 0 if not failures else 1
if __name__ == "__main__": raise SystemExit(main())
