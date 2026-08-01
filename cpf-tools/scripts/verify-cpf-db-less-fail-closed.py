#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re
from pathlib import Path


def read(root: Path, rel: str) -> str:
    p = root / rel
    if not p.is_file():
        raise SystemExit(f"missing source: {rel}")
    return p.read_text(encoding="utf-8")


def verify(root: Path) -> dict[str, object]:
    errors: list[str] = []
    adm = read(root, "cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java")
    cmn_ds = read(root, "cpf-common/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java")
    cmn_mb = read(root, "cpf-common/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java")
    cmn_sample = read(root, "cpf-common/src/main/java/com/cpf/common/config/CmnSampleDataSourceConfig.java")

    allowed_match = re.search(r'MEMORY_ALLOWED_PROFILES\s*=\s*(?:java\.util\.)?Set\.of\(([^)]*)\)', adm)
    allowed = set(re.findall(r'"([^"]+)"', allowed_match.group(1))) if allowed_match else set()
    if allowed != {"edu", "test"}:
        errors.append(f"ADM MEMORY profiles must be exactly edu/test, actual={sorted(allowed)}")
    if 'getProperty("cpf.adm.persistence.mode", "DATABASE")' not in adm:
        errors.append("ADM persistence default must be DATABASE")
    for forbidden in ("local", "demo", "library", "prod", "product", "production"):
        if forbidden in allowed:
            errors.append(f"ADM MEMORY must reject product-like profile: {forbidden}")

    product_condition = "'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'"
    if product_condition not in cmn_ds:
        errors.append("CMN DataSource must be product-mode conditional")
    if product_condition not in cmn_mb:
        errors.append("CMN MyBatis must share the CMN DataSource product-mode condition")
    if '@Qualifier("cmnDataSource") DataSource cmnDataSource' not in cmn_mb:
        errors.append("CMN MyBatis must require the canonical cmnDataSource")
    if "new JdbcTemplate" in cmn_ds or "new JdbcTemplate" in cmn_mb:
        errors.append("CMN product DB configuration must not create an unowned JdbcTemplate fallback")

    profile = re.search(r'@Profile\(\{([^}]*)\}\)', cmn_sample)
    profiles = set(re.findall(r'"([^"]+)"', profile.group(1))) if profile else set()
    if profiles != {"edu", "test"}:
        errors.append(f"CMN sample DB profiles must be exactly edu/test, actual={sorted(profiles)}")
    if '@ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true")' not in cmn_sample:
        errors.append("CMN sample DB must require explicit enabled=true")
    if "cmnDataSource" in cmn_sample:
        errors.append("CMN sample DB must not shadow the canonical cmnDataSource bean")

    result = {
        "status": "PASS" if not errors else "FAIL",
        "admMemoryProfiles": sorted(allowed),
        "cmnProductDataSourceConditional": product_condition in cmn_ds,
        "cmnMyBatisConditional": product_condition in cmn_mb,
        "cmnSampleProfiles": sorted(profiles),
        "errors": errors,
    }
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if errors:
        raise SystemExit(1)
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    verify(Path(args.root).resolve())


if __name__ == "__main__":
    main()
