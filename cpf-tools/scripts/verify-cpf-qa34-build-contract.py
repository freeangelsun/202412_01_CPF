#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

CANONICAL_PLUGIN = "com.cpf.platform-conventions"
LEGACY_PLUGIN = "com.cpf.domain-conventions"
CANONICAL_BOM = "com.cpf:cpf-platform-bom"
LEGACY_BOM = "com.cpf:cpf-bom"


def text(path: Path) -> str:
    if not path.is_file():
        raise SystemExit(f"missing required file: {path}")
    return path.read_text(encoding="utf-8-sig")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    settings = text(root / "settings.gradle")
    plugin_build = text(root / "cpf-tools/build/gradle-plugin/build.gradle")
    bom_build = text(root / "cpf-tools/build/platform-bom/build.gradle")
    member = text(root / "cpf-member/build.gradle")
    generator = text(root / "cpf-tools/generator/create-domain.ps1")
    verifier = text(root / "cpf-tools/scripts/verify-local-artifact-propagation.ps1")

    if "pluginManagement" not in settings or "includeBuild('cpf-tools/build/gradle-plugin')" not in settings:
        raise SystemExit("canonical convention plugin included build is not wired in pluginManagement")
    if f"id = '{CANONICAL_PLUGIN}'" not in plugin_build and f"id='{CANONICAL_PLUGIN}'" not in plugin_build:
        raise SystemExit("canonical plugin ID is not published")
    if "group = 'com.cpf.gradle'" not in plugin_build:
        raise SystemExit("canonical plugin implementation group mismatch")
    if "artifactId = 'cpf-platform-bom'" not in bom_build:
        raise SystemExit("canonical BOM artifact mismatch")
    for name, material in {"cpf-member": member, "generator": generator}.items():
        if CANONICAL_PLUGIN not in material:
            raise SystemExit(f"{name} does not consume canonical plugin")
        if LEGACY_PLUGIN in material:
            raise SystemExit(f"{name} still consumes legacy plugin")
    if CANONICAL_BOM not in generator or LEGACY_BOM in generator:
        raise SystemExit("generator BOM coordinate is not canonical")
    for required in [CANONICAL_PLUGIN, "com.cpf.gradle", "cpf-platform-bom"]:
        if required not in verifier:
            raise SystemExit(f"artifact verifier missing canonical token: {required}")
    for forbidden in [LEGACY_PLUGIN, "com.cpf.build", "cpf-bom/$version"]:
        if forbidden in verifier:
            raise SystemExit(f"artifact verifier still accepts legacy coordinate: {forbidden}")
    print("CPF canonical build contract: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
