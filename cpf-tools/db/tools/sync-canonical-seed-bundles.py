#!/usr/bin/env python3
"""Synchronize official DB seed compatibility sources and lifecycle bundles.

Canonical authority:
  * cpf-tools/db/canonical/seed-model.json
  * cpf-tools/db/canonical/platform-schema.json
  * cpf-tools/db/config/database-source-plan.json

This tool owns only derived seed inputs under cpf-tools/db/vendor/<vendor>/source
and their byte-identical runtime mirrors under .../seed. Historical migrations are
never modified here.
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT_DEFAULT = Path(__file__).resolve().parents[3]
OFFICIAL = ("mariadb", "postgresql", "oracle")
BUNDLES = {
    "00_product_seed.sql": "productSeedFiles",
    "00_optional_sample_seed.sql": "optionalSampleSeedFiles",
    "00_test_seed.sql": "testSeedFiles",
}


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def normalize(text: str) -> str:
    return text.rstrip() + "\n"


def render_sources(root: Path, vendor: str) -> dict[str, str]:
    # 같은 이유로 renderer import 가 Source Tree 에 .pyc 를 남기지 않게 한다.
    sys.dont_write_bytecode = True
    sys.path.insert(0, str(root / "cpf-tools/db"))
    try:
        import render_vendor_pack as renderer
    finally:
        sys.path.pop(0)

    seed = load_json(root / "cpf-tools/db/canonical/seed-model.json")
    schema = load_json(root / "cpf-tools/db/canonical/platform-schema.json")
    source_files = list(seed["canonicalPolicy"]["sourceFiles"])
    if len(source_files) != len(set(source_files)):
        raise ValueError("canonical seed sourceFiles contains duplicates")

    name_map = {t.get("currentName", t["name"]): t["targetTableName"] for t in schema["tables"]}
    name_map.update({t["name"]: t["targetTableName"] for t in schema["tables"]})
    buffers: dict[str, list[str]] = {
        name: [
            "-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json",
            f"-- vendor={vendor}; source={name}",
            "-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.",
            "-- DO NOT EDIT generated seed directly.",
            "",
        ]
        for name in source_files
    }
    current_db: dict[str, str | None] = {name: None for name in source_files}
    variables: dict[str, str] = {}

    for statement in seed["statements"]:
        source = statement.get("sourceFile")
        if source not in buffers:
            raise ValueError(f"canonical statement has unowned sourceFile: {source}")
        logical_db = statement.get("logicalDatabase")
        if logical_db and current_db[source] != logical_db:
            buffers[source].append(f"-- CPF_LOGICAL_DATABASE={logical_db}")
            if vendor == "mariadb":
                buffers[source].append(f"USE {logical_db};")
            current_db[source] = logical_db
        rendered = renderer.render_seed_statement(vendor, statement, name_map, variables)
        if rendered:
            buffers[source].append(rendered)

    return {name: normalize("\n".join(lines)) for name, lines in buffers.items()}


def bundle(vendor: str, names: list[str], sources: dict[str, str]) -> str:
    body = [
        f"-- CPF generated lifecycle bundle; vendor={vendor}",
        "-- Source plan: cpf-tools/db/config/database-source-plan.json",
    ]
    for name in names:
        if name not in sources:
            raise ValueError(f"bundle references unknown canonical seed source: {vendor}/{name}")
        body.extend(["", f"-- ===== BEGIN {name} =====", sources[name].rstrip(), f"-- ===== END {name} ====="])
    return normalize("\n".join(body))


def expected_outputs(root: Path, vendor: str) -> dict[Path, str]:
    plan = load_json(root / "cpf-tools/db/config/database-source-plan.json")
    sources = render_sources(root, vendor)
    assigned: list[str] = []
    outputs: dict[Path, str] = {}
    for bundle_name, key in BUNDLES.items():
        names = list(plan[vendor][key])
        assigned.extend(names)
        text = bundle(vendor, names, sources)
        outputs[root / f"cpf-tools/db/vendor/{vendor}/source/{bundle_name}"] = text
        outputs[root / f"cpf-tools/db/vendor/{vendor}/seed/{bundle_name}"] = text
    canonical_sources = list(load_json(root / "cpf-tools/db/canonical/seed-model.json")["canonicalPolicy"]["sourceFiles"])
    if sorted(assigned) != sorted(canonical_sources) or len(assigned) != len(set(assigned)):
        raise ValueError(f"{vendor}: source plan must own every canonical seed source exactly once")
    for name, text in sources.items():
        outputs[root / f"cpf-tools/db/vendor/{vendor}/source/{name}"] = text
    return outputs


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=str(ROOT_DEFAULT))
    parser.add_argument("--vendor", choices=OFFICIAL)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    vendors = (args.vendor,) if args.vendor else OFFICIAL
    drift: list[str] = []
    written = 0
    for vendor in vendors:
        for path, expected in expected_outputs(root, vendor).items():
            actual = path.read_text(encoding="utf-8-sig") if path.is_file() else None
            if actual != expected:
                if args.check:
                    drift.append(path.relative_to(root).as_posix())
                else:
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(expected, encoding="utf-8", newline="\n")
                    written += 1
    if drift:
        print("CPF_CANONICAL_SEED_BUNDLES=FAIL drift=" + ",".join(drift[:30]))
        return 1
    print(f"CPF_CANONICAL_SEED_BUNDLES=PASS vendors={','.join(vendors)} written={written} check={args.check}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
