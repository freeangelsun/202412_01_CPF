#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re
from pathlib import Path

IMPORT_RE = re.compile(r"(?:from\s+|import\s*\()([\"'])(\.[^\"']+)\1")
OP_RE = re.compile(r'admInvokeOperation(?:<[^>]+>)?\(\s*["\']([^"\']+)["\']')
ADM_INVOKE_RE = re.compile(r'\badmInvokeOperation(?:<[^>]+>)?\(')
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
    adm_consumer_calls = 0
    surfaces = [
        {
            "name": "ADM",
            "root": root / "cpf-admin/frontend/src",
            "operationContract": root / "cpf-admin/frontend/src/generated/cpf-operation-contract.ts",
            "operationPattern": OP_RE,
        },
        {
            "name": "BACKOFFICE_REFERENCE",
            "root": root / "cpf-backoffice-web/frontend/src",
            "operationContract": None,
            "operationPattern": None,
        },
    ]
    for descriptor in surfaces:
        surface = descriptor["root"]
        if not surface.is_dir():
            # Backoffice reference frontend is optional; ADM is not.
            if descriptor["name"] == "ADM":
                findings.append({"type": "MISSING_FRONTEND_SURFACE", "surface": surface.as_posix()})
            continue
        ops = operation_ids(descriptor["operationContract"]) if descriptor["operationContract"] else set()
        if descriptor["operationContract"] and not ops:
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
            if descriptor["operationPattern"]:
                # Count actual consumer call-sites even when the canonical operationId comes from a generated descriptor variable.
                # Literal operationIds, when used, are additionally validated against the generated contract.
                calls = len(ADM_INVOKE_RE.findall(text))
                if path.name == "cpfApi.ts":
                    calls = max(0, calls - 1)  # helper function declaration is not a consumer.
                adm_consumer_calls += calls
                for operation_id in descriptor["operationPattern"].findall(text):
                    invocations += 1
                    if operation_id not in ops:
                        findings.append({"type": "UNKNOWN_OPERATION_ID", "path": path.relative_to(root).as_posix(), "operationId": operation_id})
            for token in FORBIDDEN:
                if token in text:
                    findings.append({"type": "BROWSER_NATIVE_DANGEROUS_CONFIRMATION", "path": path.relative_to(root).as_posix(), "token": token})

    # The external Backoffice reference must consume the generated BFF client; raw CPF Java/backend coupling is forbidden.
    backoffice = root / "cpf-backoffice-web/frontend"
    backoffice_consumers = 0
    if backoffice.is_dir():
        generated = backoffice / "src/generated/backoffice-api.ts"
        transport = backoffice / "src/shared/api/channelHttpClient.ts"
        if not generated.is_file() or "AUTO-GENERATED" not in generated.read_text(encoding="utf-8", errors="replace"):
            findings.append({"type": "BACKOFFICE_GENERATED_CHANNEL_CLIENT_MISSING", "surface": backoffice.as_posix()})
        if not transport.is_file() or "invokeBackoffice" not in transport.read_text(encoding="utf-8", errors="replace"):
            findings.append({"type": "BACKOFFICE_CHANNEL_TRANSPORT_MISSING", "surface": backoffice.as_posix()})
        for path in sorted((backoffice / "src/features").rglob("*.ts")) if (backoffice / "src/features").is_dir() else []:
            text = path.read_text(encoding="utf-8", errors="replace")
            if "generated/backoffice-api" in text:
                backoffice_consumers += 1
        if backoffice_consumers == 0:
            findings.append({"type": "BACKOFFICE_GENERATED_CLIENT_CONSUMER_MISSING", "surface": backoffice.as_posix()})
    if files == 0 or imports == 0:
        findings.append({"type": "VACUOUS_FRONTEND_CLOSURE", "files": str(files), "imports": str(imports)})
    if (root / "cpf-admin/frontend/src").is_dir() and adm_consumer_calls == 0:
        findings.append({"type": "ADM_OPERATION_CONSUMER_MISSING", "surface": "cpf-admin/frontend"})
    return {"status": "PASS" if not findings else "FAIL", "files": files, "imports": imports, "operationInvocations": adm_consumer_calls, "literalOperationInvocations": invocations, "backofficeGeneratedConsumers": backoffice_consumers, "findings": findings}

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
