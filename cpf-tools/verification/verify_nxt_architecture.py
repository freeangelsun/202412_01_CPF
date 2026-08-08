#!/usr/bin/env python3
from __future__ import annotations
import csv, re, subprocess, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CORE = ROOT / "cpf-core"
FOUNDATION = ROOT / "cpf-starters/foundation/core"
TESTKIT = ROOT / "cpf-tools/testing/cpf-testkit"

FORBIDDEN_CORE_IMPORTS = (
    "org.springframework.web", "jakarta.servlet", "io.opentelemetry",
    "org.springframework.batch", "software.amazon.awssdk",
    "org.springframework.data.redis", "org.springframework.graphql",
)
FORBIDDEN_FOUNDATION_IMPORTS = (
    "org.springframework", "jakarta.", "io.opentelemetry",
    "software.amazon.awssdk", "org.apache.", "redis.clients.",
)

# Root is a governed product layout. New tracked top-level entries require explicit user approval
# and a canonical architecture change. Build/IDE directories are intentionally not part of git.
ALLOWED_TRACKED_ROOT = {
    ".editorconfig", ".gitattributes", ".github", ".gitignore", "README.md",
    "build.gradle", "gradle", "gradlew", "gradlew.bat", "settings.gradle",
    "cpf-admin", "cpf-batch", "cpf-biz-admin", "cpf-common", "cpf-core",
    "cpf-docs", "cpf-gateway", "cpf-member", "cpf-reference",
    "cpf-starters", "cpf-tools", "deploy",
}
FORBIDDEN_LEGACY_ROOT = {"cpf-foundation", "cpf-testkit"}

OWNER_API_PREFIXES = {
    "api/admin/": "MOVE_OWNER:cpf-admin",
    "api/batch/": "MOVE_OWNER:cpf-batch",
    "api/centercut/": "MOVE_OWNER:cpf-batch",
    "api/gateway/": "MOVE_OWNER:cpf-gateway",
    "api/ai/": "MOVE_CAPABILITY:ai",
    "api/archive/": "MOVE_CAPABILITY:file/archive",
    "api/attachment/": "MOVE_CAPABILITY:file/attachment-or-object-storage",
    "api/filetransfer/": "MOVE_CAPABILITY:file/transfer",
    "api/fixedlength/": "MOVE_CAPABILITY:integration/fixedlength-core",
    "api/notification/": "MOVE_CAPABILITY:notification",
    "api/remotelog/": "MOVE_CAPABILITY:platform-operations/observability",
    "api/tabular/": "MOVE_CAPABILITY:file/tabular",
    "api/webhook/": "MOVE_CAPABILITY:webhook",
    "api/health/": "MOVE_CAPABILITY:platform-operations/health",
    "api/runtimecontrol/": "MOVE_CAPABILITY:platform-operations/runtime-control",
    "api/featureflag/": "MOVE_CAPABILITY:platform-operations/feature-flag",
    "api/cache/": "MOVE_CAPABILITY:data/cache",
    "api/broker/": "MOVE_CAPABILITY:messaging/broker",
    "api/workflow/": "MOVE_CAPABILITY:workflow",
}

# These package families are eligible for Core, not automatically admitted.
# Each class still needs a class-level semantic reason below.
CORE_KERNEL_PREFIXES = (
    "api/error/", "api/transaction/", "api/execution/", "api/lineage/",
    "api/tenant/", "api/base/", "api/version/",
)

LOGGING_OPERATION_WORDS = (
    "Dynamic", "LogPolicy", "FileLog", "Async", "Recovery", "Remote",
    "RuntimeStatus", "Bundle", "Search", "Download", "Node", "Writer",
    "Operations", "Artifact", "Registry", "Preview",
)

def java_files(base: Path):
    return sorted(base.rglob("*.java")) if base.exists() else []

def imports(text: str):
    return re.findall(r"^import\s+([^;]+);", text, flags=re.M)

def core_rel(path: Path) -> str:
    return path.relative_to(CORE / "src/main/java/com/cpf/core").as_posix()

def class_name(path: Path) -> str:
    return path.stem

def tracked_root_entries() -> set[str]:
    try:
        cp = subprocess.run(
            ["git", "ls-files"], cwd=ROOT, check=True,
            text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )
        return {line.split("/", 1)[0] for line in cp.stdout.splitlines() if line.strip()}
    except Exception:
        # A missing git executable must not create a false PASS.
        return set()

def classification(path: Path, text: str) -> tuple[str, str]:
    rel = core_rel(path)
    name = class_name(path)

    for prefix, decision in OWNER_API_PREFIXES.items():
        if rel.startswith(prefix):
            return decision, f"owner-specific/optional API package: {prefix}"

    # Session/Object Storage/Event Schema are explicit examples of contracts that were
    # incorrectly added to Core in 07_18.
    if rel.startswith("api/security/") and "Session" in name:
        return "MOVE_CAPABILITY:security/session", "distributed session is optional capability"
    if rel.startswith("api/reliability/") and name.startswith("CpfEventSchema"):
        return "MOVE_CAPABILITY:messaging/schema-governance", "event schema is optional capability"

    # Logging context can be Core only when it is truly transaction/execution semantics.
    if rel.startswith("api/logging/") or rel.startswith("common/logging/"):
        if "TransactionContext" in name or "ExecutionContext" in name or "Lineage" in name:
            return "MOVE_CORE_PACKAGE:transaction-or-execution-context", "kernel context is mispackaged under logging"
        if any(word in name for word in LOGGING_OPERATION_WORDS):
            return "MOVE_CAPABILITY:platform-operations/observability", "logging runtime/operations are not Core"
        return "REVIEW_REQUIRED", "logging class requires explicit Kernel admission proof"

    if rel.startswith("config/"):
        if "AutoConfiguration" in name or "@AutoConfiguration" in text or "@Configuration" in text:
            return "MOVE_STARTER", "Core must not assemble Spring runtime"
        return "REVIEW_REQUIRED", "Core config package requires explicit Kernel admission proof"

    if "/internal/" in f"/{rel}" or rel.startswith("service/") or rel.startswith("common/") or rel.startswith("spi/"):
        if any(x in text for x in ("JdbcTemplate", "DataSource", "EntityManager")):
            return "MOVE_PROVIDER", "persistence runtime/provider implementation"
        if any(x in text for x in ("OncePerRequestFilter", "WebMvcConfigurer", "ResponseBodyAdvice", "WebFilter")):
            return "MOVE_STARTER", "web runtime adapter"
        if any(x in text for x in ("io.opentelemetry", "OpenTelemetry")):
            return "MOVE_PROVIDER", "telemetry provider implementation"
        return "REVIEW_REQUIRED", "implementation/SPI package is not admitted to Core by default"

    if rel.startswith("api/util/"):
        return "MOVE_FOUNDATION_OR_CAPABILITY", "utility requires Foundation/Capability ownership"

    # Only narrow Kernel package families are eligible for KEEP_CORE.
    if rel.startswith(CORE_KERNEL_PREFIXES):
        return "KEEP_CORE", "global CPF Kernel semantics candidate"

    # Security/reliability classes are class-level decisions, never package-wide automatic KEEP.
    if rel.startswith("api/security/"):
        if any(k in name for k in ("Identity", "Principal", "SecurityContext", "AuthenticatedSystemIdentity")):
            return "KEEP_CORE", "minimum global identity/security context"
        return "REVIEW_REQUIRED", "security class is not globally required by default"

    if rel.startswith("api/reliability/"):
        if any(k in name for k in ("Idempot", "Unknown", "Reconcile", "Outcome", "Deadline")):
            return "KEEP_CORE", "global reliability semantics"
        return "REVIEW_REQUIRED", "reliability class requires explicit global semantic proof"

    return "REVIEW_REQUIRED", "NO automatic KEEP_CORE; explicit Core Admission proof required"

def main() -> int:
    failures = []
    rows = []

    tracked = tracked_root_entries()
    if not tracked:
        failures.append("ROOT_LAYOUT_UNVERIFIED:git ls-files unavailable")
    for entry in sorted(tracked - ALLOWED_TRACKED_ROOT):
        failures.append(f"UNAPPROVED_TRACKED_ROOT_ENTRY:{entry}")
    for entry in sorted(FORBIDDEN_LEGACY_ROOT):
        if (ROOT / entry).exists():
            failures.append(f"LEGACY_ROOT_MODULE_PRESENT:{entry}")

    if not FOUNDATION.is_dir():
        failures.append("FOUNDATION_PHYSICAL_OWNER_MISSING:cpf-starters/foundation/core")
    if not TESTKIT.is_dir():
        failures.append("TESTKIT_PHYSICAL_OWNER_MISSING:cpf-tools/testing/cpf-testkit")

    for path in java_files(CORE / "src/main/java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        decision, reason = classification(path, text)
        bad = [i for i in imports(text) if i.startswith(FORBIDDEN_CORE_IMPORTS)]
        rel_repo = path.relative_to(ROOT).as_posix()
        if bad:
            failures.append(f"CORE_FORBIDDEN_IMPORT:{rel_repo}:{','.join(bad)}")
        if decision != "KEEP_CORE":
            failures.append(f"CORE_ADMISSION_FAIL:{rel_repo}:{decision}:{reason}")
        rows.append((rel_repo, decision, reason, ";".join(bad)))

    for path in java_files(FOUNDATION / "src/main/java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        bad = [i for i in imports(text) if i.startswith(FORBIDDEN_FOUNDATION_IMPORTS)]
        if bad:
            failures.append(f"FOUNDATION_FORBIDDEN_IMPORT:{path.relative_to(ROOT)}:{','.join(bad)}")

    # No external module may depend on Core internal packages.
    for path in java_files(ROOT):
        if str(path).startswith(str(CORE)):
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        if "import com.cpf.core.internal" in text:
            failures.append(f"EXTERNAL_INTERNAL_REFERENCE:{path.relative_to(ROOT)}")

    out = ROOT / "cpf-docs/work/CPF_CORE_SLIMMING_AUDIT.csv"
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["path", "classification", "reason", "forbidden_imports"])
        w.writerows(rows)

    if failures:
        print("NXT_ARCHITECTURE_GATE=FAIL")
        print("\n".join(failures))
        return 1

    print(f"NXT_ARCHITECTURE_GATE=PASS core_classes={len(rows)}")
    return 0

if __name__ == "__main__":
    sys.exit(main())
