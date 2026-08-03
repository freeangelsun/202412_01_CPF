#!/usr/bin/env python3
"""Fail-closed owner-boundary checks for CPF core, admin, and batch modules."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


class GateError(RuntimeError):
    pass


BATCH_COMPONENTS = (
    "control-server",
    "scheduler",
    "worker",
    "center-cut-runner",
    "host-agent",
    "runtime-common",
    "execution-runtime",
    "contract",
)

ADMIN_CROSS_OWNER = re.compile(
    r"batJdbcTemplate|mbrJdbcTemplate|refJdbcTemplate|accJdbcTemplate|"
    r"@Qualifier\(\"(?:bat|mbr|ref|acc)JdbcTemplate\"\)|"
    r"\b(?:FROM|UPDATE|INTO|DELETE\s+FROM)\s+(?:bat|mbr|ref|acc)_",
    re.IGNORECASE,
)
BATCH_CORE_RUNTIME = re.compile(
    r"com\.cpf\.core\.common\.batch\."
    r"(?:CpfBatchFileLogWriter|CpfBatchGhostDetectionService|CpfBatchHeartbeatService|"
    r"CpfBatchLauncher|CpfBatchLockManager|CpfBatchLoggingEventPublisher|"
    r"CpfBatchOperationRepository|CpfBatchRuntimeListener|CpfBatchRuntimeProgress)|"
    r"com\.cpf\.core\.common\.batch\.centercut\.CpfCenterCutService"
)


def require_file(path: Path, label: str) -> None:
    if not path.is_file():
        raise GateError(f"required {label} missing: {path}")


def require_dir(path: Path, label: str) -> None:
    if not path.is_dir():
        raise GateError(f"required {label} missing: {path}")


def scan_java(root: Path, pattern: re.Pattern[str], message: str) -> list[str]:
    findings: list[str] = []
    for path in sorted(root.rglob("*.java")):
        text = path.read_text(encoding="utf-8-sig", errors="replace")
        for number, line in enumerate(text.splitlines(), 1):
            if pattern.search(line):
                findings.append(f"{message}: {path}:{number}: {line.strip()}")
    return findings


def validate(root: Path) -> dict:
    root = root.resolve()
    core_java = root / "cpf-core/src/main/java"
    admin_java = root / "cpf-admin/src/main/java"
    core_build = root / "cpf-core/build.gradle"
    require_dir(core_java, "cpf-core Java source root")
    require_dir(admin_java, "cpf-admin Java source root")
    require_file(core_build, "cpf-core Gradle build")

    batch_roots: list[Path] = []
    for component in BATCH_COMPONENTS:
        component_root = root / "cpf-batch" / component
        source_root = component_root / "src/main/java"
        require_dir(component_root, f"cpf-batch/{component} owner root")
        require_file(component_root / "build.gradle", f"cpf-batch/{component} Gradle build")
        require_dir(source_root, f"cpf-batch/{component} Java source root")
        batch_roots.append(source_root)

    findings = scan_java(admin_java, ADMIN_CROSS_OWNER, "ADM cross-owner DB access")
    for batch_java in batch_roots:
        findings.extend(scan_java(batch_java, BATCH_CORE_RUNTIME, "BAT imports Core-owned runtime compatibility type"))

    for relative in (
        "cpf-core/src/main/java/com/cpf/core/config/CpfBatchAutoConfiguration.java",
        "cpf-core/src/main/java/com/cpf/core/config/CpfCenterCutAutoConfiguration.java",
    ):
        path = root / relative
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8-sig", errors="replace")
        if "legacy-batch-runtime-enabled" not in text:
            findings.append(f"Core compatibility config has no explicit legacy opt-in: {relative}")
        if re.search(r"matchIfMissing\s*=\s*true", text):
            findings.append(f"Core legacy batch runtime is enabled by default: {relative}")

    core_text = core_build.read_text(encoding="utf-8-sig", errors="replace")
    if re.search(r"project\(\s*['\"]:cpf-batch(?:[:'\"])", core_text):
        findings.append("cpf-core must not depend on cpf-batch")

    if findings:
        raise GateError("owner boundary violations: " + " | ".join(findings[:100]))
    return {
        "status": "PASS",
        "core_source_root": str(core_java.relative_to(root)).replace("\\", "/"),
        "admin_source_root": str(admin_java.relative_to(root)).replace("\\", "/"),
        "batch_component_count": len(batch_roots),
        "batch_components": list(BATCH_COMPONENTS),
        "violation_count": 0,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.root)
        if args.json_output:
            output = args.json_output
            if not output.is_absolute():
                output = args.root.resolve() / output
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except (GateError, OSError) as exc:
        print(f"CPF owner boundary gate FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
