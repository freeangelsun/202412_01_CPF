#!/usr/bin/env python3
"""Fail-closed static contract for the Windows DB3 lifecycle runner."""
from __future__ import annotations

import re
from pathlib import Path

root = Path(__file__).resolve().parents[3]
script = root / "cpf-tools/verification/final-dev/run-db3-lifecycle.ps1"
text = script.read_text(encoding="utf-8")
errors: list[str] = []

required_patterns = {
    "mandatory ExpectedHead": r"\[Parameter\(Mandatory\s*=\s*\$true\)\][\s\S]{0,160}\[string\]\$ExpectedHead",
    "git repository root": r"rev-parse\s+--show-toplevel",
    "actual HEAD": r"rev-parse\s+HEAD",
    "HEAD mismatch fail-fast": r"ExpectedHead mismatch",
    "stdin redirect": r"RedirectStandardInput\s*=\s*\$true",
    "password stdin write": r"StandardInput\.WriteLine\(\$Password\)",
    "runner password flag": r"['\"]--password-stdin['\"]",
    "redaction function": r"function\s+Protect-Text",
    "Oracle preflight": r"CPF_RUNTIME_ORACLE_PASSWORD",
    "PostgreSQL preflight": r"CPF_RUNTIME_POSTGRESQL_PASSWORD",
    "MariaDB preflight": r"CPF_RUNTIME_MARIADB_PASSWORD",
    "vendor result summary": r"db3-lifecycle-summary\.json",
    "exit propagation": r"exit\s+\$overallExit",
}
for name, pattern in required_patterns.items():
    if not re.search(pattern, text, re.IGNORECASE | re.DOTALL):
        errors.append(f"missing contract: {name}")

forbidden_patterns = {
    "hard-coded SHA": r"\$(?:Baseline|ExpectedHead)\s*=\s*['\"]?[0-9a-fA-F]{40}",
    "password command argument": r"--password(?:\s+|=)\$(?!stdin)",
    "password added to ArgumentList": r"ArgumentList\.Add\(\s*\$password\s*\)",
    "unredacted Tee logging": r"\bTee-Object\b",
}
for name, pattern in forbidden_patterns.items():
    if re.search(pattern, text, re.IGNORECASE):
        errors.append(f"forbidden contract: {name}")

preflight_at = text.find("$missing =")
loop_at = text.find("foreach ($vendor in $vendors.GetEnumerator())", preflight_at + 1)
invoke_at = text.find("Invoke-LifecycleRunner", loop_at + 1)
if min(preflight_at, loop_at, invoke_at) < 0 or not (preflight_at < loop_at < invoke_at):
    errors.append("environment preflight must complete before the first runner invocation")

if errors:
    print("\n".join(f"FAIL {error}" for error in errors))
    raise SystemExit(1)
print("PASS FDEV-005 runner contract: ExpectedHead, stdin-only secret transport, preflight, redaction, vendor result propagation")
