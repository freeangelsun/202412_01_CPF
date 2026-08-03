#!/usr/bin/env python3
"""Build reproducible Work Package source/test inventory for developer pre-review.

This is not a QA pass/fail gate. It resolves actual files available in an exact/sparse
source snapshot, records which implementation aspects are present in those files, and
keeps Requirement completion as a separate per-row decision.
"""
from __future__ import annotations

import argparse
import csv
import glob
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

TEXT_EXTENSIONS = {".java", ".kt", ".groovy", ".ts", ".tsx", ".js", ".vue", ".py", ".sql", ".json", ".gradle", ".kts", ".ps1", ".sh", ".md", ".template"}
SOURCE_EXTENSIONS = {".java", ".kt", ".groovy", ".ts", ".tsx", ".js", ".vue", ".py"}
IGNORED_PARTS = {"build", "dist", "node_modules", "coverage", "test-results", "playwright-report", ".pytest_cache", "__pycache__", ".gradle"}
STOP_TOKENS = {
    "p09", "p10", "p11", "p12", "p13", "p14", "p15", "cpf", "com", "java", "latest",
    "core", "source", "runtime", "test", "product", "actual", "전체", "기능", "결과", "실제",
}
OWNER_PREFIX = {
    "cpf-admin": "cpf-admin/",
    "cpf-biz-admin": "cpf-biz-admin/",
    "cpf-core": "cpf-core/",
    "cpf-common": "cpf-common/",
    "cpf-batch": "cpf-batch/",
    "cpf-gateway": "cpf-gateway/",
    "cpf-reference": "cpf-reference/",
    "cpf-member": "cpf-member/",
    "repository-wide test ownership": "cpf-tools/",
    "cpf-tools release/deploy": "cpf-tools/",
    "cpf-starters + cpf-tools": "cpf-starters/",
}
ASPECT_PATTERNS = {
    "unknown": (r"UNKNOWN_RESULT", r"\bunknown\b", r"결과 불명"),
    "idempotency": (r"idempoten", r"멱등"),
    "concurrency": (r"optimistic", r"compare.?and.?set", r"expectedVersion", r"synchronized", r"동시"),
    "retry": (r"\bretry", r"backoff", r"재시도"),
    "recovery": (r"reconcile", r"recover", r"rollback", r"compensat", r"복구", r"대사"),
    "security": (r"authoriz", r"authentic", r"permission", r"@PreAuthorize", r"권한", r"인증"),
    "audit": (r"\baudit", r"감사"),
    "masking": (r"mask", r"sanitize", r"redact", r"마스킹"),
    "database": (r"DataSource", r"Jdbc", r"Repository", r"\bSELECT\b", r"\bUPDATE\b", r"\bINSERT\b"),
    "migration": (r"migration", r"flyway", r"V\d+__", r"마이그레이션"),
    "rollback": (r"rollback", r"R\d+__", r"롤백"),
    "generator": (r"generator", r"template", r"generated", r"생성"),
    "frontend": (r"\.vue$", r"frontend/", r"aria-", r"role=", r"operationId"),
    "openapi": (r"openapi", r"swagger", r"operationId"),
    "test": (r"/src/test/", r"Test\.(java|kt)$", r"\.test\.(ts|tsx|js)$", r"pytest", r"unittest"),
}
MANUAL_GATE_MAPPING = {
    "P09-GATE": ["cpf-tools/scripts/verify-cpf-frontend-consumer-closure.py", "cpf-tools/scripts/verify-cpf-operator-trust-boundary.py"],
    "P10-GATE": ["cpf-tools/scripts/verify-cpf-execution-scope-exhaustive.py", "cpf-tools/scripts/verify-cpf-requirement-traceability.py"],
    "P11-GATE": ["cpf-tools/scripts/verify-cpf-operator-trust-boundary.py", "cpf-tools/scripts/verify-cpf-transaction-id-standard.py"],
    "P12-GATE": ["cpf-tools/scripts/verify-cpf-development-evidence-integrity.py", "cpf-tools/scripts/verify-cpf-starter-catalog-truth.py"],
    "P13-GATE": [
        "cpf-tools/scripts/verify-cpf-development-evidence-integrity.py",
        "cpf-tools/scripts/tests/test_verify_cpf_development_evidence_integrity.py",
        "cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py",
        "cpf-tools/scripts/tests/test_verify_cpf_db_vendor_semantic_parity.py",
        "cpf-tools/scripts/run-db-vendor-lifecycle.ps1",
    ],
    "P14-GATE": [
        "cpf-tools/scripts/verify-cpf-execution-scope-exhaustive.py",
        "cpf-tools/scripts/tests/test_verify_cpf_execution_scope_exhaustive.py",
    ],
    "P15-GATE": [
        "cpf-tools/scripts/verify-cpf-development-evidence-integrity.py",
        "cpf-tools/scripts/tests/test_verify_cpf_development_evidence_integrity.py",
        "cpf-tools/scripts/verify-cpf-requirement-traceability.py",
        "cpf-tools/scripts/tests/test_verify_cpf_requirement_traceability.py",
        "cpf-tools/scripts/verify-cpf-development-traceability-closure.py",
        "cpf-tools/scripts/tests/test_verify_cpf_development_traceability_closure.py",
    ],
}


def read_parts(pattern: str) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for name in sorted(glob.glob(pattern)):
        with open(name, encoding="utf-8-sig", newline="") as handle:
            rows.extend({key: (value or "").strip() for key, value in row.items()} for row in csv.DictReader(handle))
    return rows


def tokenize(value: str) -> set[str]:
    return {
        token.lower()
        for token in re.split(r"[^A-Za-z0-9가-힣]+", value or "")
        if len(token) >= 3 and token.lower() not in STOP_TOKENS
    }


def load_inventory(root: Path) -> list[dict[str, str]]:
    inventory: list[dict[str, str]] = []
    for path in sorted(root.rglob("*")):
        if not path.is_file() or any(part in IGNORED_PARTS for part in path.parts):
            continue
        relative = path.relative_to(root).as_posix()
        if relative.startswith(("cpf-docs/evidence/", "cpf-docs/work/review/")):
            continue
        if relative.startswith("cpf-docs/work/current/") and path.suffix.lower() in {".csv", ".json"}:
            continue
        text = ""
        if path.suffix.lower() in TEXT_EXTENSIONS:
            text = path.read_text(encoding="utf-8-sig", errors="ignore")[:500_000]
        text_lower = text.lower()
        content_tokens = set(re.findall(r"[a-z0-9가-힣]{3,}", text_lower)) if text_lower else set()
        corpus = relative + "\n" + text
        aspects = {
            aspect
            for aspect, patterns in ASPECT_PATTERNS.items()
            if any(re.search(pattern, corpus, flags=re.IGNORECASE) for pattern in patterns)
        }
        inventory.append({"path": relative, "path_lower": relative.lower(), "text": text, "text_lower": text_lower, "tokens": content_tokens, "suffix": path.suffix.lower(), "aspects": aspects})
    return inventory


def required_aspects(requirements: list[dict[str, str]]) -> set[str]:
    value = " ".join(
        " ".join(row.get(key, "") for key in ("requirement", "acceptance_criteria", "verification_method", "function_type", "feature"))
        for row in requirements
    ).lower()
    required: set[str] = set()
    aliases = {
        "unknown": ("unknown", "결과 불명"),
        "idempotency": ("idempoten", "멱등"),
        "concurrency": ("동시", "concurr", "optimistic", "cas"),
        "retry": ("retry", "재시도"),
        "recovery": ("recover", "reconcile", "rollback", "복구", "대사"),
        "security": ("security", "보안", "권한", "인증"),
        "audit": ("audit", "감사"),
        "masking": ("mask", "마스킹"),
        "database": ("database", "db", "sql", "repository"),
        "migration": ("migration", "마이그레이션", "upgrade"),
        "rollback": ("rollback", "롤백"),
        "generator": ("generator", "generated", "생성"),
        "frontend": ("frontend", "browser", "화면"),
        "openapi": ("openapi", "swagger"),
        "test": ("test", "검증"),
    }
    for aspect, terms in aliases.items():
        if any(term in value for term in terms):
            required.add(aspect)
    return required


def aspect_presence(selected: list[dict[str, str]]) -> set[str]:
    found: set[str] = set()
    for item in selected:
        cached = item.get("aspects")
        if cached is not None:
            found.update(cached)
            continue
        corpus = item["path"] + "\n" + item.get("text", "")
        for aspect, patterns in ASPECT_PATTERNS.items():
            if any(re.search(pattern, corpus, flags=re.IGNORECASE) for pattern in patterns):
                found.add(aspect)
    return found


def score_files(work_package: str, requirements: list[dict[str, str]], inventory: list[dict[str, str]]) -> list[dict[str, str]]:
    tokens = tokenize(work_package)
    for row in requirements[:20]:
        for key in ("capability", "feature", "function_type", "owner_package", "actual_consumer", "change_target"):
            tokens.update(tokenize(row.get(key, "")))
    owners = Counter(row.get("owner_module", "") for row in requirements)
    owner = owners.most_common(1)[0][0] if owners else ""
    prefixes = [OWNER_PREFIX[name] for name in re.split(r"\s*\+\s*", owner) if name in OWNER_PREFIX]
    if not prefixes and owner in OWNER_PREFIX:
        prefixes = [OWNER_PREFIX[owner]]
    scored: list[tuple[float, dict[str, str]]] = []
    for item in inventory:
        score = 0.0
        if any(item["path_lower"].startswith(prefix.lower()) for prefix in prefixes):
            score += 6.0
        for token in tokens:
            if token in item["path_lower"]:
                score += 3.0
        score += min(4.0, 0.35 * len(tokens.intersection(item.get("tokens", set()))))
        if "/src/main/" in item["path_lower"]:
            score += 1.0
        if "/src/test/" in item["path_lower"] or re.search(r"(?:test|spec)\.[^.]+$", item["path_lower"]):
            score += 0.75
        if score >= 6.0:
            scored.append((score, item))
    ranked = [item for _, item in sorted(scored, key=lambda entry: (-entry[0], entry[1]["path"]))]
    selected: list[dict[str, str]] = []
    selected_paths: set[str] = set()

    def add(item: dict[str, str]) -> None:
        if item["path"] not in selected_paths:
            selected.append(item)
            selected_paths.add(item["path"])

    for item in ranked[:16]:
        add(item)

    # A fixed top-N list can hide the only Test/SQL/security/recovery file behind
    # many similarly scored owner files. Rescue one exact candidate for every
    # Requirement-required aspect before recording it as uncovered.
    required = required_aspects(requirements)
    observed = aspect_presence(selected)
    for aspect in sorted(required - observed):
        candidate = next((item for item in ranked if aspect in aspect_presence([item])), None)
        if candidate is not None:
            add(candidate)
            observed.add(aspect)
        if len(selected) >= 32:
            break
    return selected


def classify(path: str) -> str:
    lower = path.lower()
    if "/src/test/" in lower or re.search(r"(?:test|spec)\.[^.]+$", lower):
        return "test"
    if lower.endswith((".sql", ".sql.template")) or "/db/" in lower:
        return "sql"
    if lower.endswith((".vue", ".ts", ".tsx", ".js")) and "frontend/" in lower:
        return "frontend"
    if lower.endswith((".py", ".ps1", ".sh")) or "/scripts/" in lower or "/verification/" in lower:
        return "script"
    if Path(lower).suffix in SOURCE_EXTENSIONS:
        return "main"
    return "other"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--execution-glob", required=True)
    parser.add_argument("--requirement-glob", required=True)
    parser.add_argument("--scenario-glob", required=True)
    parser.add_argument("--source-root", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--summary-output", required=True)
    parser.add_argument("--start-row", type=int, default=20_001)
    parser.add_argument("--expected-requirements", type=int, default=10_558)
    parser.add_argument("--expected-work-packages", type=int, default=291)
    parser.add_argument("--baseline-sha", required=True)
    args = parser.parse_args()

    execution = read_parts(args.execution_glob)
    requirements = {row["requirement_id"]: row for row in read_parts(args.requirement_glob)}
    scenarios = defaultdict(list)
    for row in read_parts(args.scenario_glob):
        scenarios[row["linked_requirement_id"]].append(row)
    scope = execution[args.start_row - 1 :]
    if len(scope) != args.expected_requirements:
        raise SystemExit(f"scope count mismatch expected={args.expected_requirements} actual={len(scope)}")
    grouped: dict[str, list[dict[str, str]]] = defaultdict(list)
    grouped_execution: dict[str, list[dict[str, str]]] = defaultdict(list)
    for row in scope:
        grouped[row["work_package_id"]].append(requirements[row["requirement_id"]])
        grouped_execution[row["work_package_id"]].append(row)
    if len(grouped) != args.expected_work_packages:
        raise SystemExit(f"work package count mismatch expected={args.expected_work_packages} actual={len(grouped)}")

    source_root = Path(args.source_root).resolve()
    inventory = load_inventory(source_root)
    output_rows: list[dict[str, object]] = []
    unresolved: list[str] = []
    for work_package, req_rows in grouped.items():
        selected = score_files(work_package, req_rows, inventory)
        if work_package in MANUAL_GATE_MAPPING:
            selected_by_path = {item["path"]: item for item in selected}
            for mapped_path in MANUAL_GATE_MAPPING[work_package]:
                mapped = next((item for item in inventory if item["path"] == mapped_path), None)
                if mapped is not None:
                    selected_by_path[mapped_path] = mapped
            selected = list(selected_by_path.values())
        if not selected:
            unresolved.append(work_package)
        categories = Counter(classify(item["path"]) for item in selected)
        required = required_aspects(req_rows)
        present = aspect_presence(selected)
        missing = sorted(required - present)
        exec_rows = grouped_execution[work_package]
        output_rows.append({
            "work_package_id": work_package,
            "first_execution_order": exec_rows[0]["execution_order"],
            "last_execution_order": exec_rows[-1]["execution_order"],
            "requirement_count": len(req_rows),
            "scenario_count": sum(len(scenarios[row["requirement_id"]]) for row in req_rows),
            "owner_modules": ";".join(sorted({row.get("owner_module", "") for row in req_rows if row.get("owner_module")})),
            "capabilities": ";".join(sorted({row.get("capability", "") for row in req_rows if row.get("capability")})),
            "features": ";".join(sorted({row.get("feature", "") for row in req_rows if row.get("feature")})),
            "source_resolution": "EXACT_SNAPSHOT_FILES" if selected else "UNRESOLVED",
            "selected_file_count": len(selected),
            "main_source_count": categories["main"],
            "test_file_count": categories["test"],
            "frontend_file_count": categories["frontend"],
            "sql_file_count": categories["sql"],
            "script_file_count": categories["script"],
            "actual_source_files": ";".join(item["path"] for item in selected),
            "required_aspects": ";".join(sorted(required)),
            "observed_aspects": ";".join(sorted(present)),
            "uncovered_aspects": ";".join(missing),
            "developer_review_result": "SOURCE_INVENTORY_CONNECTED" if selected else "SOURCE_INVENTORY_UNRESOLVED",
            "completion_effect": "Developer pre-review evidence only; Requirement completion is decided per REQUIREMENT_STATUS.csv row",
            "baseline_sha": args.baseline_sha,
        })

    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(output_rows[0]))
        writer.writeheader()
        writer.writerows(output_rows)
    summary = {
        "status": "PASS" if not unresolved else "PARTIAL",
        "baselineSha": args.baseline_sha,
        "requirements": len(scope),
        "workPackages": len(output_rows),
        "sourceInventoryFiles": len(inventory),
        "resolvedWorkPackages": sum(row["source_resolution"] == "EXACT_SNAPSHOT_FILES" for row in output_rows),
        "unresolvedWorkPackages": unresolved,
        "workPackagesWithMainSource": sum(int(row["main_source_count"]) > 0 for row in output_rows),
        "workPackagesWithTests": sum(int(row["test_file_count"]) > 0 for row in output_rows),
        "workPackagesWithFrontend": sum(int(row["frontend_file_count"]) > 0 for row in output_rows),
        "workPackagesWithSql": sum(int(row["sql_file_count"]) > 0 for row in output_rows),
        "workPackagesWithUncoveredAspects": sum(bool(row["uncovered_aspects"]) for row in output_rows),
    }
    summary_path = Path(args.summary_output)
    summary_path.parent.mkdir(parents=True, exist_ok=True)
    summary_path.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    return 0 if not unresolved else 1


if __name__ == "__main__":
    raise SystemExit(main())
