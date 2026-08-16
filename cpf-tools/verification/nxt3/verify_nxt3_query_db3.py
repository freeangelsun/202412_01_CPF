#!/usr/bin/env python3
"""NXT3-QUERY-001: Repository SQL/Query의 DB3 중립성과 단일 Owner를 검증한다."""
from __future__ import annotations

import argparse
import fnmatch
import json
import os
import re
import tempfile
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Iterable

def source_identity(root: Path) -> str:
    import os
    env = os.environ.get("CPF_SOURCE_SHA", "").strip()
    if re.fullmatch(r"[0-9a-fA-F]{40}", env):
        return env.lower()
    base = root / "cpf-docs/work/BASE_SHA.txt"
    if base.is_file():
        value = base.read_text(encoding="utf-8", errors="ignore").strip()
        if re.fullmatch(r"[0-9a-fA-F]{40}", value):
            return value.lower()
    return "UNKNOWN"

OFFICIAL = ("oracle", "postgresql", "mariadb")
TEXT_EXT = {".java", ".kt", ".groovy", ".xml", ".sql", ".yaml", ".yml", ".properties", ".json", ".py", ".ps1", ".sh"}
SKIP_PARTS = {".git", ".gradle", "node_modules", "dist", "out", "target", "bin"}
SKIP_PREFIXES = ("cpf-docs/", "cpf-tools/verification/")
SQL_EXT = {".sql", ".xml"}
UNSUPPORTED = re.compile(r"(?i)(?<![A-Za-z0-9_])(mysql|mssql|sqlserver|sql_server|h2)(?![A-Za-z0-9_])")
RAW_BRANCH = re.compile(
    r"(?is)(?:if|else\s+if|switch|case|when).{0,220}(?:databaseVendor|dbVendor|databaseProductName|dialect|vendor).{0,140}(?:oracle|postgres(?:ql)?|mariadb)"
    r"|(?:databaseVendor|dbVendor|databaseProductName|dialect|vendor).{0,140}(?:==|equals\s*\(|contains\s*\(|matches\s*\().{0,140}(?:oracle|postgres(?:ql)?|mariadb)"
)
SQL_LOOKING = re.compile(r"(?is)\b(select|insert|update|delete|merge)\b.{0,240}\b(from|into|set|using|where|values)\b")
DIALECT = {
    "mysql_limit": re.compile(r"(?i)\blimit\s+(?:\d+|[#:$]{1,2}\{|[A-Za-z_])"),
    "mysql_dupkey": re.compile(r"(?i)\bon\s+duplicate\s+key\b"),
    "mysql_auto_increment": re.compile(r"(?i)\bauto_increment\b"),
    "postgres_returning": re.compile(r"(?i)\breturning\s+[A-Za-z_*]"),
    "postgres_on_conflict": re.compile(r"(?i)\bon\s+conflict\b"),
    "postgres_cast": re.compile(r"::\s*[A-Za-z][A-Za-z0-9_]*"),
    "oracle_rownum": re.compile(r"(?i)\brownum\b"),
    "oracle_nvl": re.compile(r"(?i)\bnvl\s*\("),
    "oracle_dual": re.compile(r"(?i)\bfrom\s+dual\b"),
    "oracle_nextval": re.compile(r"(?i)\b[A-Za-z0-9_]+\.nextval\b"),
    "oracle_fetch_next": re.compile(r"(?i)\bfetch\s+next\b"),
    "mariadb_concat": re.compile(r"(?i)\bconcat\s*\("),
}
VENDOR_SEGMENT = re.compile(r"/(oracle|postgresql|mariadb)/", re.I)


@dataclass
class Finding:
    code: str
    path: str
    detail: str


@dataclass
class Inventory:
    path: str
    owner: str
    classification: str
    sqlLike: bool
    vendorSegment: str | None
    testId: str | None


def _match(path: str, pattern: str) -> bool:
    # fnmatch의 ** 동작은 경로 구분자를 포함하므로 Repository 상대경로 규칙에 충분하다.
    return fnmatch.fnmatchcase(path, pattern)


def _load_policy(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    vendors = tuple(str(x).lower() for x in data.get("officialVendors", []))
    if vendors != OFFICIAL:
        raise ValueError(f"officialVendors는 정확히 {OFFICIAL} 이어야 합니다: {vendors}")
    for section in ("rendererOwners", "explicitVendorOverrides"):
        for row in data.get(section, []):
            if not row.get("pattern") or not row.get("owner") or not row.get("reason") or not row.get("testId"):
                raise ValueError(f"{section} entry는 pattern/owner/reason/testId가 모두 필요합니다: {row}")
    return data


def _read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig", errors="replace")


def _semantic_text(text: str) -> str:
    """Vendor/SQL 검사는 설명 주석을 실행 코드로 오인하지 않는다."""
    text = re.sub(r"(?s)/\*.*?\*/", " ", text)
    text = re.sub(r"(?s)<!--.*?-->", " ", text)
    kept = []
    for line in text.splitlines():
        if re.match(r"^\s*(?://|#|--|\*)", line):
            continue
        kept.append(line)
    return "\n".join(kept)


def _sql_surface(path: Path, semantic: str) -> str:
    """Java/Kotlin/Groovy에서는 SQL 문자열만 Dialect 검사해 :: 메서드 참조 같은 오탐을 막는다."""
    if path.suffix.lower() not in {".java", ".kt", ".groovy"}:
        return semantic
    chunks = re.findall(r'(?s)"""(.*?)"""', semantic)
    # 단일 문자열 SQL도 보완한다. 문자열 연결 자체는 portable query 여부 판정에만 사용한다.
    chunks += re.findall(r'"([^"\n]*(?:SELECT|INSERT|UPDATE|DELETE|MERGE)[^"\n]*)"', semantic, flags=re.I)
    sql = "\n".join(x for x in chunks if SQL_LOOKING.search(x))
    return sql


def _iter_files(root: Path) -> Iterable[Path]:
    """Walk only product source trees.

    pathlib.rglob() descends into node_modules/.gradle/build before a file-level
    filter can reject them.  A FullLocal checkout can therefore turn this
    static gate into a multi-minute scan of generated artifacts.  Prune
    transient directories before descent while keeping cpf-tools/build/**,
    which is canonical Gradle plugin/BOM source.
    """
    for current, dirs, files in os.walk(root):
        current_path = Path(current)
        rel_dir = current_path.relative_to(root).as_posix() if current_path != root else ""

        kept_dirs = []
        for name in dirs:
            child_rel = f"{rel_dir}/{name}".lstrip("/")
            if name in SKIP_PARTS:
                continue
            if name == "build" and child_rel != "cpf-tools/build":
                continue
            if child_rel.startswith(SKIP_PREFIXES):
                continue
            kept_dirs.append(name)
        dirs[:] = kept_dirs

        if rel_dir.startswith(SKIP_PREFIXES):
            dirs[:] = []
            continue

        for name in files:
            p = current_path / name
            if p.suffix.lower() not in TEXT_EXT:
                continue
            rel = p.relative_to(root).as_posix()
            if rel.startswith(SKIP_PREFIXES):
                continue
            if "/verification/" in "/" + rel and p.suffix.lower() in {".json", ".yaml", ".yml"}:
                continue
            yield p


def _owner_matches(rel: str, policy: dict) -> list[dict]:
    rows = []
    for section in ("rendererOwners", "explicitVendorOverrides"):
        for row in policy.get(section, []):
            if _match(rel, row["pattern"]):
                rows.append({**row, "section": section})
    return rows


def _vendor_segment(rel: str) -> str | None:
    m = VENDOR_SEGMENT.search("/" + rel)
    return m.group(1).lower() if m else None


def audit(root: Path, policy_path: Path) -> tuple[list[Finding], list[Inventory], dict]:
    policy = _load_policy(policy_path)
    findings: list[Finding] = []
    inventory: list[Inventory] = []
    scanned = 0
    sql_files = 0
    branch_files = 0
    duplicate_groups: dict[str, set[str]] = {}
    neg_allow = tuple(policy.get("unsupportedVendorNegativeContractAllow", []))

    for p in _iter_files(root):
        rel = p.relative_to(root).as_posix()
        text = _read_text(p)
        semantic = _semantic_text(text)
        sql_surface = _sql_surface(p, semantic)
        scanned += 1
        sql_like = bool(SQL_LOOKING.search(sql_surface))
        vendor = _vendor_segment(rel)
        matches = _owner_matches(rel, policy)

        if len(matches) > 1:
            findings.append(Finding("OWNER_AMBIGUOUS", rel, ",".join(x["owner"] for x in matches)))
            owner = "AMBIGUOUS"
            classification = "AMBIGUOUS"
            test_id = None
        elif matches:
            owner = matches[0]["owner"]
            classification = matches[0]["section"]
            test_id = matches[0]["testId"]
        else:
            owner = "PORTABLE_CANONICAL_SQL" if sql_like else "NON_SQL_SOURCE"
            classification = "portable" if sql_like else "non-sql"
            test_id = "NXT3-QUERY-001-PORTABLE" if sql_like else None

        if sql_like:
            sql_files += 1
            inventory.append(Inventory(rel, owner, classification, True, vendor, test_id))

        negative_contract = any(_match(rel, pattern) for pattern in neg_allow)

        # Application/Generated/Education/Runtime source의 vendor branch는 등록된 owner가 아니면 금지한다.
        if p.suffix.lower() in {".java", ".kt", ".groovy", ".py", ".ps1", ".sh"} and RAW_BRANCH.search(semantic):
            branch_files += 1
            if classification not in {"rendererOwners", "explicitVendorOverrides"} and not negative_contract:
                findings.append(Finding("RAW_VENDOR_BRANCH", rel, "Application/Business Source의 직접 Vendor 분기"))

        # 지원 종료 Vendor 명칭은 negative contract allowlist에서만 허용한다.
        unsupported = sorted({m.group(1).lower() for m in UNSUPPORTED.finditer(semantic)})
        if unsupported and not negative_contract:
            # 제품/운영 Source가 비공식 Vendor를 실제 지원하는 경로는 금지한다.
            # 단, unsupported Vendor reject test와 MariaDB image 내부 기술 식별자는 Policy에 근거를 둔 negative contract로 분리한다.
            findings.append(Finding("UNSUPPORTED_VENDOR", rel, ",".join(unsupported)))

        if not sql_like:
            continue

        dialect_hits = [name for name, rx in DIALECT.items() if rx.search(sql_surface)]
        if dialect_hits and classification not in {"rendererOwners", "explicitVendorOverrides"} and not negative_contract:
            findings.append(Finding("RAW_DIALECT", rel, ",".join(dialect_hits)))

        if vendor and classification == "portable":
            findings.append(Finding("VENDOR_PATH_WITHOUT_OWNER", rel, f"vendor={vendor}"))

        # Business Source에서 oracle/postgresql/mariadb 3벌로 같은 logical path를 관리하는 패턴을 검출한다.
        if vendor and classification not in {"rendererOwners", "explicitVendorOverrides"}:
            logical = VENDOR_SEGMENT.sub("/{vendor}/", "/" + rel).lstrip("/")
            duplicate_groups.setdefault(logical, set()).add(vendor)

    for logical, vendors in sorted(duplicate_groups.items()):
        if vendors == set(OFFICIAL):
            findings.append(Finding("BUSINESS_SQL_DB3_COPY", logical, "oracle/postgresql/mariadb 3벌 Business SQL"))

    summary = {
        "executionSourceSha": source_identity(root),
        "requirement": "NXT3-QUERY-001",
        "root": str(root),
        "officialVendors": list(OFFICIAL),
        "filesScanned": scanned,
        "sqlQueryFiles": sql_files,
        "rawVendorBranchCandidates": branch_files,
        "inventoryEntries": len(inventory),
        "failures": len(findings),
        "status": "PASS" if not findings else "FAIL",
    }
    return findings, inventory, summary


def self_test() -> dict:
    """Gate가 정상/오류/경계 조건을 실제로 구분하는지 synthetic repository로 검증한다."""
    cases: list[tuple[str, bool]] = []
    policy = {
        "schemaVersion": "1.0",
        "officialVendors": list(OFFICIAL),
        "rendererOwners": [{"pattern":"cpf-tools/db/**","owner":"CPF_DATA_DIALECT_RENDERER","reason":"test renderer","testId":"T-RENDER"}],
        "explicitVendorOverrides": [{"pattern":"cpf-starters/data/vendor/oracle/**","owner":"CPF_DATA_PROVIDER_OVERRIDE","reason":"provider-native query","testId":"T-OVERRIDE"}],
        "unsupportedVendorNegativeContractAllow": ["tests/**"],
    }
    with tempfile.TemporaryDirectory(prefix="cpf-query-db3-selftest-") as td:
        root = Path(td)
        pp = root / "policy.json"
        pp.write_text(json.dumps(policy, ensure_ascii=False), encoding="utf-8")

        def run(name: str, files: dict[str,str], expect_pass: bool) -> None:
            case = root / name
            case.mkdir()
            for rel, content in files.items():
                p = case / rel; p.parent.mkdir(parents=True, exist_ok=True); p.write_text(content, encoding="utf-8")
            # policy를 case 밖에 두면 root-relative 읽기에 영향 없음
            f, _, _ = audit(case, pp)
            actual = not f
            cases.append((name, actual == expect_pass))

        run("portable", {"app/query.xml":"<select>SELECT * FROM T WHERE id=#{id}</select>"}, True)
        run("raw_branch", {"app/Q.java":"class Q { void x(){ if (vendor.equals(\"oracle\")) {} } }"}, False)
        run("raw_dialect", {"app/query.xml":"<select>SELECT * FROM T LIMIT 10</select>"}, False)
        run("unsupported", {"app/a.yml":"database: mysql"}, False)
        run("negative_contract", {"tests/reject_vendor.py":"assert reject(\"mysql\")"}, True)
        run("comment_only_vendor", {"app/Q.java":"// if vendor == oracle / postgresql / mariadb\nclass Q {}"}, True)
        run("renderer", {"cpf-tools/db/oracle/a.sql":"SELECT * FROM T WHERE ROWNUM &lt; 2"}, True)
        run("override", {"cpf-starters/data/vendor/oracle/a.sql":"SELECT * FROM T WHERE ROWNUM &lt; 2"}, True)

    failed = [name for name, ok in cases if not ok]
    return {"status":"PASS" if not failed else "FAIL", "cases":[{"name":n,"pass":ok} for n,ok in cases], "failures":failed}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--policy", default="cpf-tools/db/contracts/query-db3-policy.json")
    ap.add_argument("--json-out")
    ap.add_argument("--inventory-out")
    ap.add_argument("--self-test", action="store_true")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    if args.self_test:
        result = self_test()
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0 if result["status"] == "PASS" else 1
    policy_path = Path(args.policy)
    if not policy_path.is_absolute():
        policy_path = root / policy_path
    findings, inventory, summary = audit(root, policy_path)
    result = {**summary, "findings":[asdict(x) for x in findings]}
    if args.json_out:
        out = Path(args.json_out); out.parent.mkdir(parents=True, exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    if args.inventory_out:
        out = Path(args.inventory_out); out.parent.mkdir(parents=True, exist_ok=True); out.write_text(json.dumps([asdict(x) for x in inventory],ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(f"CPF_NXT3_QUERY_DB3={summary['status']} scanned={summary['filesScanned']} sql={summary['sqlQueryFiles']} failures={summary['failures']}")
    for f in findings[:200]:
        print(f"{f.code} {f.path} :: {f.detail}")
    return 0 if not findings else 1


if __name__ == "__main__":
    raise SystemExit(main())
