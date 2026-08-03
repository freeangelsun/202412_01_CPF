#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re
from pathlib import Path

IMPORT_RE = re.compile(r"(?:from\s+|import\s*\()([\"'])(\.[^\"']+)\1")
OP_RE = re.compile(r"(?:adm|bza)InvokeOperation(?:<[^>]+>)?\(\s*[\"']([^\"']+)[\"']")
FORBIDDEN = ("window.prompt(", "window.confirm(", "prompt(", "confirm(")
EXTENSIONS = (".ts", ".tsx", ".js", ".mjs", ".vue", ".json")

def resolve(base: Path, spec: str) -> Path | None:
    spec = spec.split("?", 1)[0]
    candidate = (base / spec).resolve()
    if candidate.is_file():
        return candidate
    if candidate.suffix:
        return None
    for ext in EXTENSIONS:
        path = Path(str(candidate) + ext)
        if path.is_file():
            return path
    for ext in EXTENSIONS:
        path = candidate / ("index" + ext)
        if path.is_file():
            return path
    return None

def operation_ids(path: Path) -> set[str]:
    if not path.is_file():
        return set()
    text = path.read_text(encoding="utf-8")
    match = re.search(r"export\s+type\s+CpfOperationId\s*=\s*(.*?);", text, re.S)
    return set(re.findall(r"[\"']([^\"']+)[\"']", match.group(1))) if match else set()

def verify(root: Path) -> dict:
    findings: list[dict[str, str]] = []
    files = imports = invocations = 0
    for surface in (root / "cpf-admin/frontend/src", root / "cpf-biz-admin/frontend/src"):
        ops = operation_ids(surface / "generated/cpf-operation-contract.ts")
        if not ops:
            findings.append({"type": "MISSING_OPERATION_CONTRACT", "surface": surface.as_posix()})
        for path in sorted(surface.rglob("*")):
            if not path.is_file() or path.suffix not in {".ts", ".tsx", ".js", ".mjs", ".vue"}:
                continue
            if path.name.endswith(".test.ts") or path.name.endswith(".spec.ts"):
                continue
            files += 1
            text = path.read_text(encoding="utf-8", errors="replace")
            for _, spec in IMPORT_RE.findall(text):
                imports += 1
                if resolve(path.parent, spec) is None:
                    findings.append({"type": "MISSING_RELATIVE_IMPORT", "path": path.relative_to(root).as_posix(), "import": spec})
            for operation_id in OP_RE.findall(text):
                invocations += 1
                if operation_id not in ops:
                    findings.append({"type": "UNKNOWN_OPERATION_ID", "path": path.relative_to(root).as_posix(), "operationId": operation_id})
            for token in FORBIDDEN:
                if token in text:
                    findings.append({"type": "BROWSER_NATIVE_DANGEROUS_CONFIRMATION", "path": path.relative_to(root).as_posix(), "token": token})
    return {"status": "PASS" if not findings else "FAIL", "files": files, "imports": imports, "operationInvocations": invocations, "findings": findings}

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    result = verify(Path(args.root).resolve())
    text = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(text + "\n", encoding="utf-8")
    print(text)
    return 0 if result["status"] == "PASS" else 1

if __name__ == "__main__":
    raise SystemExit(main())
