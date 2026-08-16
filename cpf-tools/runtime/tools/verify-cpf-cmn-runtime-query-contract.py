#!/usr/bin/env python3
"""Validate cpf-common CMN runtime SQL authoring/resource/consumer parity."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path

OFFICIAL_VENDORS = {"mariadb", "postgresql", "oracle"}
CONSUMER_PATHS = {
    "CmnJdbcCalendarStore": "cpf-starters/common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java",
    "CmnSampleItemService": "cpf-education/src/main/java/com/cpf/education/common/sample/CmnSampleItemService.java",
    "CmnSampleSqlDialect": "cpf-education/src/main/java/com/cpf/education/common/sample/CmnSampleSqlDialect.java",
    "CmnJdbcTemplateStore": "cpf-starters/common/src/main/java/com/cpf/common/template/CmnJdbcTemplateStore.java",
}
JAVA_STRING = re.compile(r'(?s)"""(.*?)"""|"((?:\\.|[^"\\])*)"')


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def evaluate(root: Path) -> dict:
    findings: list[str] = []
    contract_path = root / "cpf-tools/db/metadata/platform-runtime-query-contract.json"
    if not contract_path.is_file():
        return {"status": "FAIL", "findings": [f"missing contract: {contract_path.relative_to(root)}"]}
    try:
        contract = json.loads(contract_path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        return {"status": "FAIL", "findings": [f"invalid contract json: {exc}"]}
    modules = [m for m in contract.get("modules", []) if m.get("module") == "cmn"]
    if len(modules) != 1:
        return {"status": "FAIL", "findings": [f"cmn module count must be 1, actual={len(modules)}"]}
    module = modules[0]
    if module.get("ownerArtifact") != "cpf-starter-common":
        findings.append("cmn ownerArtifact must be cpf-starter-common")
    if module.get("inlineSqlPolicy") != "FORBIDDEN":
        findings.append("cmn inlineSqlPolicy must be FORBIDDEN")
    if set(contract.get("vendors", [])) != OFFICIAL_VENDORS:
        findings.append(f"official vendor set mismatch: {contract.get('vendors')}")

    author_root = root / module.get("templateRoot", "")
    runtime_root = root / module.get("generatedPackPath", "")
    author_files = {p.relative_to(author_root).as_posix(): p for p in author_root.rglob("*.sql")} if author_root.is_dir() else {}
    runtime_files = {p.relative_to(runtime_root).as_posix(): p for p in runtime_root.rglob("*.sql")} if runtime_root.is_dir() else {}
    if not author_files:
        findings.append("cmn authoring SQL pack is empty")
    if set(author_files) != set(runtime_files):
        findings.append(f"authoring/runtime resource set mismatch: authorOnly={sorted(set(author_files)-set(runtime_files))}, runtimeOnly={sorted(set(runtime_files)-set(author_files))}")

    statements = module.get("statements", [])
    keys = [str(s.get("key", "")) for s in statements]
    resources = [str(s.get("resource", "")) for s in statements]
    if len(keys) != len(set(keys)):
        findings.append("duplicate CMN query key")
    if len(resources) != len(set(resources)):
        findings.append("duplicate CMN SQL resource declaration")
    if set(resources) != set(author_files):
        findings.append(f"declared/resource set mismatch: undeclared={sorted(set(author_files)-set(resources))}, missing={sorted(set(resources)-set(author_files))}")

    consumer_cache: dict[str, str] = {}
    statement_results = []
    for statement in statements:
        key = str(statement.get("key", ""))
        resource = str(statement.get("resource", ""))
        consumer = str(statement.get("consumer", ""))
        item_findings: list[str] = []
        ap = author_files.get(resource)
        rp = runtime_files.get(resource)
        if ap is None or rp is None:
            item_findings.append("resource missing")
        else:
            if ap.stat().st_size == 0 or rp.stat().st_size == 0:
                item_findings.append("blank SQL resource")
            if sha256(ap) != sha256(rp):
                item_findings.append("authoring/runtime hash mismatch")
            sql = ap.read_text(encoding="utf-8-sig").strip()
            if "${" in sql:
                item_findings.append("unresolved token")
            actual_params = sql.count("?")
            if actual_params != statement.get("parameterCount"):
                item_findings.append(f"parameter count mismatch expected={statement.get('parameterCount')} actual={actual_params}")
        consumer_path_text = CONSUMER_PATHS.get(consumer)
        if not consumer_path_text:
            item_findings.append(f"unknown consumer {consumer}")
        else:
            if consumer not in consumer_cache:
                cp = root / consumer_path_text
                consumer_cache[consumer] = cp.read_text(encoding="utf-8-sig") if cp.is_file() else ""
            source = consumer_cache[consumer]
            if not source:
                item_findings.append("consumer source missing")
            elif resource not in source:
                item_findings.append("consumer does not reference exact queryId")
        statement_results.append({"key": key, "resource": resource, "consumer": consumer, "status": "PASS" if not item_findings else "FAIL", "findings": item_findings})
        findings.extend(f"{key}: {finding}" for finding in item_findings)

    for consumer, source in consumer_cache.items():
        if source:
            literals = [next(group for group in match.groups() if group is not None).strip().upper()
                        for match in JAVA_STRING.finditer(source)]
            if any(re.match(r"^(SELECT|INSERT|UPDATE|DELETE|MERGE)\s", literal) for literal in literals):
                findings.append(f"{consumer}: inline SQL literal detected")

    vendor_groups = {"offset": set(), "cursor": set()}
    for resource in resources:
        match = re.fullmatch(r"sample/(offset|cursor)-(mariadb|postgresql|oracle)\.sql", resource)
        if match:
            vendor_groups[match.group(1)].add(match.group(2))
    for group, vendors in vendor_groups.items():
        if vendors != OFFICIAL_VENDORS:
            findings.append(f"sample {group} vendor coverage mismatch: {sorted(vendors)}")

    return {
        "status": "PASS" if not findings else "FAIL",
        "contract": str(contract_path.relative_to(root)),
        "statementCount": len(statements),
        "authoringResourceCount": len(author_files),
        "runtimeResourceCount": len(runtime_files),
        "findings": findings,
        "statements": statement_results,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    result = evaluate(Path(args.root).resolve())
    rendered = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    return 0 if result["status"] == "PASS" else 1

if __name__ == "__main__":
    raise SystemExit(main())
