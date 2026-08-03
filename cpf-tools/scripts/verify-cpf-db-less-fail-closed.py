#!/usr/bin/env python3
"""Validate fail-closed ADM/CMN persistence ownership and product defaults."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


class GateError(RuntimeError):
    pass


ADM_POLICY = "cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java"
JDBC_OWNER = "cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config"
MYBATIS_OWNER = "cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config"
CMN_DATASOURCE = f"{JDBC_OWNER}/CmnDataSourceConfig.java"
CMN_SAMPLE_DATASOURCE = f"{JDBC_OWNER}/CmnSampleDataSourceConfig.java"
CMN_MYBATIS = f"{MYBATIS_OWNER}/CmnMyBatisConfig.java"
PRODUCT_CONDITION = "'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'"


def read(root: Path, relative: str) -> str:
    path = root / relative
    if not path.is_file():
        raise GateError(f"missing source: {relative}")
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError as exc:
        raise GateError(f"source is not valid UTF-8: {relative}") from exc


def validate(root: Path) -> dict[str, object]:
    root = root.resolve()
    errors: list[str] = []
    adm = read(root, ADM_POLICY)
    cmn_ds = read(root, CMN_DATASOURCE)
    cmn_mb = read(root, CMN_MYBATIS)
    cmn_sample = read(root, CMN_SAMPLE_DATASOURCE)

    stale_owner = root / "cpf-common/src/main/java/com/cpf/common/config"
    if stale_owner.exists():
        stale_runtime = sorted(path.name for path in stale_owner.glob("Cmn*Config.java") if path.is_file())
        if stale_runtime:
            errors.append(
                "CMN runtime persistence configuration must be starter-owned; "
                f"stale cpf-common configs={stale_runtime}"
            )

    allowed_match = re.search(
        r"MEMORY_ALLOWED_PROFILES\s*=\s*(?:java\.util\.)?Set\.of\(([^)]*)\)", adm
    )
    allowed = set(re.findall(r'"([^"]+)"', allowed_match.group(1))) if allowed_match else set()
    if allowed != {"edu", "test"}:
        errors.append(f"ADM MEMORY profiles must be exactly edu/test, actual={sorted(allowed)}")
    if 'getProperty("cpf.adm.persistence.mode", "DATABASE")' not in adm:
        errors.append("ADM persistence default must be DATABASE")
    if "if (mode == Mode.MEMORY)" not in adm or "throw new CpfValidationException" not in adm:
        errors.append("ADM MEMORY mode must fail closed outside the explicit profile allow-list")
    for forbidden in ("local", "demo", "library", "prod", "product", "production"):
        if forbidden in allowed:
            errors.append(f"ADM MEMORY must reject product-like profile: {forbidden}")

    if PRODUCT_CONDITION not in cmn_ds:
        errors.append("CMN DataSource must be product-mode conditional")
    if PRODUCT_CONDITION not in cmn_mb:
        errors.append("CMN MyBatis must share the CMN DataSource product-mode condition")
    if '@Qualifier("cmnDataSource") DataSource cmnDataSource' not in cmn_mb:
        errors.append("CMN MyBatis must require the canonical cmnDataSource")
    if "new JdbcTemplate" in cmn_ds or "new JdbcTemplate" in cmn_mb:
        errors.append("CMN product DB configuration must not create an unowned JdbcTemplate fallback")
    if 'CpfDataSources.resolve(environment, "spring.datasource.cmn")' not in cmn_ds:
        errors.append("CMN product datasource must resolve the canonical spring.datasource.cmn owner binding")
    if '@Bean(name = "cmnDataSource")' not in cmn_ds:
        errors.append("CMN product datasource bean must retain the canonical cmnDataSource name")

    profile = re.search(r"@Profile\(\{([^}]*)\}\)", cmn_sample)
    profiles = set(re.findall(r'"([^"]+)"', profile.group(1))) if profile else set()
    if profiles != {"edu", "test"}:
        errors.append(f"CMN sample DB profiles must be exactly edu/test, actual={sorted(profiles)}")
    explicit_sample_enable = (
        '@ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true")'
    )
    if explicit_sample_enable not in cmn_sample:
        errors.append("CMN sample DB must require explicit enabled=true")
    if '@Bean(name = "cmnDataSource")' in cmn_sample or '@Qualifier("cmnDataSource")' in cmn_sample:
        errors.append("CMN sample DB must not shadow or consume the canonical cmnDataSource bean")
    if 'CpfDataSources.resolve(environment, "spring.datasource.cmn-sample")' not in cmn_sample:
        errors.append("CMN sample datasource must use the isolated spring.datasource.cmn-sample binding")

    result = {
        "status": "PASS" if not errors else "FAIL",
        "admMemoryProfiles": sorted(allowed),
        "cmnProductDataSourceConditional": PRODUCT_CONDITION in cmn_ds,
        "cmnMyBatisConditional": PRODUCT_CONDITION in cmn_mb,
        "cmnSampleProfiles": sorted(profiles),
        "jdbcOwner": JDBC_OWNER,
        "myBatisOwner": MYBATIS_OWNER,
        "errors": errors,
    }
    if errors:
        raise GateError("\n".join(errors))
    return result


def write_json(root: Path, output_value: str | None, result: dict[str, object]) -> None:
    if not output_value:
        return
    output = Path(output_value)
    if not output.is_absolute():
        output = root / output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = args.root.resolve()
    try:
        result = validate(root)
    except (GateError, OSError) as exc:
        result = {"status": "FAIL", "errors": str(exc).splitlines()}
        write_json(root, args.json_output, result)
        print(f"CPF DB-less fail-closed gate FAILED: {exc}", file=sys.stderr)
        return 1
    write_json(root, args.json_output, result)
    print(json.dumps(result, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
