#!/usr/bin/env python3
"""Run low-cost CPF verification by changed capability.

This is the middle tier between cpfVerifyFast and cpfVerifyFullLocal. It does not weaken
FullLocal; it selects existing fail-closed gates for developer feedback and always includes
cross-cutting catalog/dependency/adoption closure.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import tempfile
from pathlib import Path

BASE_GATES = (
    "cpf-tools/verification/verify_starter_catalog.py",
    "cpf-tools/verification/verify_gradle_project_dependency_closure.py",
    "cpf-tools/verification/verify_cpf_developer_adoption_contract.py",
)
CAPABILITY_GATES = {
    "core": (),
    "db": ("cpf-tools/db/verification/verify-cpf-db-development-contract.py",),
    "cache": (
        "cpf-tools/verification/tools/verify-cpf-cache-capability.py",
        "cpf-tools/verification/tools/verify-cpf-cache-durable-lifecycle.py",
    ),
    "messaging": ("cpf-tools/verification/run_message_context_runtime_tests.py",),
    "batch": (
        "cpf-tools/verification/verify_cpf_batch_developer_top50.py",
        "cpf-tools/verification/tools/verify-cpf-batch-fail-closed.py",
        "cpf-tools/verification/tools/verify-cpf-batch-no-remote-kafka.py",
        "cpf-tools/verification/tools/verify-cpf-batch-unknown-reconciliation.py",
    ),
    "security": (
        "cpf-tools/security/tools/verify-cpf-controller-permission-contract.py",
        "cpf-tools/verification/tools/verify-cpf-approval-state-machine.py",
    ),
    "integration": ("cpf-tools/verification/verify_integration_closure_contract.py",),
    "frontend": ("cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py",),
    "generator": ("cpf-tools/generator/verification/verify-cpf-generator-lifecycle.py",),
    "gateway": ("cpf-tools/verification/verify_gateway_closure.py",),
    "openapi": ("cpf-tools/verification/openapi/verify-cpf-openapi-controller-coverage.py",),
}


def _unique(values: list[str]) -> list[str]:
    return list(dict.fromkeys(values))


def build_plan(capabilities: list[str]) -> list[str]:
    gates = list(BASE_GATES)
    for capability in capabilities:
        gates.extend(CAPABILITY_GATES[capability])
    return _unique(gates)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--capability", action="append", choices=sorted(CAPABILITY_GATES))
    parser.add_argument("--list", action="store_true")
    parser.add_argument("--plan", action="store_true", help="print selected gates without executing")
    parser.add_argument("--output-json")
    args = parser.parse_args()
    if args.list:
        print("\n".join(sorted(CAPABILITY_GATES)))
        return 0
    capabilities = _unique(args.capability or ["core"])
    plan = build_plan(capabilities)
    if args.plan:
        print(json.dumps({"capabilities": capabilities, "gates": plan}, ensure_ascii=False, indent=2))
        return 0

    root = Path(args.root).resolve()
    rows: list[dict] = []
    failed = False
    for relative in plan:
        script = root / relative
        if not script.is_file():
            rows.append({"gate": relative, "status": "FAIL", "rc": 127, "reason": "missing gate"})
            failed = True
            continue
        commands: list[list[str]] = []
        if relative.endswith("verify-cpf-cache-durable-lifecycle.py"):
            report = Path(tempfile.gettempdir()) / "cpf-targeted-cache-durable.json"
            commands.append([sys.executable, "-B", str(script), "--repo-root", str(root), "--report-json", str(report)])
        elif relative.endswith("verify-cpf-openapi-controller-coverage.py"):
            commands.extend([
                [sys.executable, "-B", str(script), "--root", str(root), "--module", "cpf-admin", "--openapi", str(root / "cpf-admin/frontend/openapi/cpf-openapi.json")],
                [sys.executable, "-B", str(script), "--root", str(root), "--module", "cpf-backoffice", "--openapi", str(root / "cpf-backoffice/openapi/cpf-openapi.json")],
            ])
        else:
            commands.append([sys.executable, "-B", str(script), "--root", str(root)])

        outputs: list[str] = []
        returncode = 0
        for command in commands:
            probe = subprocess.run(command, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            if probe.returncode != 0 and "unrecognized arguments: --root" in probe.stdout and len(commands) == 1:
                command = [sys.executable, "-B", str(script)]
                probe = subprocess.run(command, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            outputs.append(probe.stdout)
            if probe.returncode != 0:
                returncode = probe.returncode
        combined = "\n".join(outputs)
        status = "PASS" if returncode == 0 else "FAIL"
        rows.append({"gate": relative, "status": status, "rc": returncode, "output": combined[-8000:]})
        failed |= returncode != 0
        print(f"[{status}] {relative}")
    result = {"status": "FAIL" if failed else "PASS", "capabilities": capabilities, "gates": rows}
    if args.output_json:
        target = Path(args.output_json)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
