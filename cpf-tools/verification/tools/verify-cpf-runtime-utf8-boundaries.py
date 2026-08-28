#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

PRODUCT_ROOTS = ("cpf-tools", "cpf-batch", "cpf-admin", "cpf-backoffice", "cpf-gateway", "cpf-starters")
EXCLUDED_PARTS = {"build", ".gradle", ".git", "node_modules", "__pycache__", ".pytest_cache", "evidence"}
MOJIBAKE_LITERALS = ("\ufffd", "?ㅼ", "?쒕", "?댁", "吏", "?듬땲")
PSI_TOKEN = "ProcessStartInfo"
START_PROCESS_TOKEN = "Start-Process"
CHILD_UTF8_TOKENS = ("$CpfUtf8ChildJavaOptions", "PYTHONUTF8", "PYTHONIOENCODING")


def iter_product_ps1(root: Path):
    for top in PRODUCT_ROOTS:
        base = root / top
        if not base.exists():
            continue
        for path in base.rglob("*.ps1"):
            rel = path.relative_to(root)
            if any(part in EXCLUDED_PARTS for part in rel.parts):
                continue
            yield path, rel.as_posix()


def verify(root: Path) -> dict:
    failures: list[str] = []
    process_files = 0
    redirected_files = 0
    start_process_files = 0
    mojibake_files = 0
    for path, rel in iter_product_ps1(root):
        text = path.read_text(encoding="utf-8-sig", errors="strict")
        hits = [marker for marker in MOJIBAKE_LITERALS if marker in text]
        if hits:
            mojibake_files += 1
            failures.append(f"MOJIBAKE_SOURCE:{rel}:{','.join(hits)}")
        has_psi = PSI_TOKEN in text
        has_start_process = START_PROCESS_TOKEN in text
        if has_psi:
            process_files += 1
            redirected = "RedirectStandardOutput" in text or "RedirectStandardError" in text
            if redirected:
                redirected_files += 1
                if "RedirectStandardOutput" in text and "StandardOutputEncoding" not in text:
                    failures.append(f"PROCESS_STDOUT_ENCODING_MISSING:{rel}")
                if "RedirectStandardError" in text and "StandardErrorEncoding" not in text:
                    failures.append(f"PROCESS_STDERR_ENCODING_MISSING:{rel}")
        if has_start_process:
            start_process_files += 1
            # Start-Process does not expose StandardOutputEncoding/StandardErrorEncoding.
            # The child itself must therefore be forced to emit UTF-8 bytes before file redirection.
            if any(token not in text for token in CHILD_UTF8_TOKENS):
                failures.append(f"START_PROCESS_CHILD_UTF8_MISSING:{rel}")
            if "mariadb" in text.lower() and "RedirectStandardInput" in text and "--default-character-set=utf8mb4" not in text:
                failures.append(f"MARIADB_CLIENT_CHARSET_MISSING:{rel}")
            if "pg_" in text.lower() and "PGCLIENTENCODING" not in text:
                failures.append(f"POSTGRES_CLIENT_ENCODING_MISSING:{rel}")
            if ("sqlplus" in text.lower() or "expdp" in text.lower() or "impdp" in text.lower()) and "NLS_LANG" not in text:
                failures.append(f"ORACLE_CLIENT_ENCODING_MISSING:{rel}")

    mandatory = {
        "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1": (
            "[Console]::OutputEncoding", "$OutputEncoding", "StandardOutputEncoding", "StandardErrorEncoding",
            "PYTHONUTF8", "PYTHONIOENCODING", "-Dfile.encoding=UTF-8",
        ),
        "cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1": (
            "[Console]::OutputEncoding", "$OutputEncoding", "PYTHONUTF8", "PYTHONIOENCODING",
        ),
        "cpf-tools/environment/docker-development-test/CPF_도커_개발테스트환경_전체설치.ps1": (
            "[Console]::OutputEncoding", "$OutputEncoding", "PYTHONUTF8", "PYTHONIOENCODING", "-Dfile.encoding=UTF-8",
        ),
    }
    for rel, tokens in mandatory.items():
        path = root / rel
        if not path.is_file():
            failures.append(f"MANDATORY_ENTRYPOINT_MISSING:{rel}")
            continue
        text = path.read_text(encoding="utf-8-sig", errors="strict")
        for token in tokens:
            if token not in text:
                failures.append(f"MANDATORY_UTF8_TOKEN_MISSING:{rel}:{token}")

    return {
        "status": "PASS" if not failures else "FAIL",
        "processStartInfoFiles": process_files,
        "redirectedProcessFiles": redirected_files,
        "startProcessFiles": start_process_files,
        "mojibakeSourceFiles": mojibake_files,
        "failures": failures,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output", default="")
    args = parser.parse_args()
    result = verify(Path(args.root).resolve())
    encoded = json.dumps(result, ensure_ascii=False, indent=2)
    print(encoded)
    if args.json_output:
        Path(args.json_output).write_text(encoded + "\n", encoding="utf-8")
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
