#!/usr/bin/env python3
"""Fail-closed checks for the CPF developer/adoption Golden Path contract."""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

VALID_USAGE = {"golden", "capability", "advanced", "internal"}
EXPECTED_CALLS = {"application", "cpf-domain", "external-integration"}
EXPECTED_TIERS = {"fast", "targeted", "fullLocal"}
FORBIDDEN_SOURCE_PATTERNS = {
    "WebClient.builder": re.compile(r"\bWebClient\s*\.\s*builder\s*\("),
    "new RestTemplate": re.compile(r"\bnew\s+RestTemplate\s*\("),
    "RedisTemplate": re.compile(r"\bRedisTemplate\b"),
    "KafkaTemplate": re.compile(r"\bKafkaTemplate\b"),
}
BUSINESS_ROOTS = ("cpf-member", "cpf-external")
GENERATOR_TEMPLATE_ROOTS = (
    "cpf-tools/generator/templates",
    "cpf-tools/generator/resources",
)


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def scan_forbidden(root: Path) -> list[str]:
    violations: list[str] = []
    for root_name in BUSINESS_ROOTS + GENERATOR_TEMPLATE_ROOTS:
        base = root / root_name
        if not base.exists():
            continue
        for file in base.rglob("*.java"):
            if "/src/test/" in file.as_posix():
                continue
            text = file.read_text(encoding="utf-8", errors="ignore")
            for label, pattern in FORBIDDEN_SOURCE_PATTERNS.items():
                if pattern.search(text):
                    violations.append(f"{file.relative_to(root).as_posix()}: direct {label}")
    return violations


def validate_developer_shell_text(powershell: str, shell: str) -> list[str]:
    """Verify both compatibility shells delegate targeted work to the one Java CLI."""
    errors: list[str] = []
    if "'verify-targeted'=@('dev','targeted-test')" not in powershell.replace(" ", ""):
        errors.append("PowerShell developer shell lacks Unified CLI targeted verification mapping")
    if "@ArgsFromCli" not in powershell:
        errors.append("PowerShell developer shell does not forward targeted capability arguments")
    normalized_shell = " ".join(shell.split())
    if 'verify-targeted) exec "$CLI" dev targeted-test "$@"' not in normalized_shell:
        errors.append("POSIX developer shell lacks Unified CLI targeted verification mapping/argument forwarding")
    for label, text in (("PowerShell", powershell), ("POSIX", shell)):
        if "cpfVerifyTargeted" in text or "gradlew" in text:
            errors.append(f"{label} developer shell duplicates the canonical targeted Gradle engine")
    return errors


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    errors: list[str] = []

    contract_path = root / "cpf-tools/generator/contracts/cpf-developer-adoption-contract.json"
    catalog_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    if not contract_path.is_file() or not catalog_path.is_file():
        print("FAIL developer adoption contract/catalog missing")
        return 1
    contract = load_json(contract_path)
    catalog = load_json(catalog_path)

    if {row.get("id") for row in contract.get("callTaxonomy", [])} != EXPECTED_CALLS:
        errors.append("callTaxonomy must contain exactly application/cpf-domain/external-integration")
    if set((contract.get("validationTiers") or {}).keys()) != EXPECTED_TIERS:
        errors.append("validationTiers must contain fast/targeted/fullLocal")
    levels = contract.get("knowledgeLevels") or []
    if [row.get("level") for row in levels] != [1, 2, 3]:
        errors.append("knowledgeLevels must be ordered 1/2/3")
    if len(contract.get("top20GoldenPath") or []) != 20:
        errors.append("top20GoldenPath must contain exactly 20 items")
    capability_profiles_path = root / "cpf-tools/generator/contracts/capability-profiles.json"
    capability_profiles = load_json(capability_profiles_path) if capability_profiles_path.is_file() else {}
    adoption_profiles = capability_profiles.get("adoptionEntryProfiles") or {}
    if set(adoption_profiles) != {"minimal", "standard", "full-platform"}:
        errors.append("capability-profiles adoptionEntryProfiles must contain minimal/standard/full-platform")
    public_profiles = set(catalog.get("publicProfiles") or [])
    public_artifacts = {str(m.get("artifactId")) for m in catalog.get("modules") or [] if m.get("visibility") == "public"}
    provider_slots = set((catalog.get("providerSlots") or {}).keys())
    for name, profile in adoption_profiles.items():
        for key in ("basePublicProfile",):
            value = profile.get(key)
            if value and value not in public_profiles:
                errors.append(f"adoption profile {name} references unknown public profile: {value}")
        for value in profile.get("additionalPublicProfiles") or []:
            if value not in public_profiles:
                errors.append(f"adoption profile {name} references unknown additional profile: {value}")
        for value in profile.get("addPublicArtifacts") or []:
            if value not in public_artifacts:
                errors.append(f"adoption profile {name} references unknown public artifact: {value}")
        for value in profile.get("optionalProviderSlots") or []:
            if value not in provider_slots:
                errors.append(f"adoption profile {name} references unknown provider slot: {value}")

    for row in contract.get("top20GoldenPath") or []:
        for key in ("source", "secondarySource"):
            value = row.get(key)
            if value and not (root / value).is_file():
                errors.append(f"Golden Path source missing: {value}")
    for row in contract.get("extensionPoints") or []:
        source = row.get("source")
        if not source or not (root / source).is_file():
            errors.append(f"extension source missing: {source}")
    for row in contract.get("advancedApis") or []:
        source = row.get("source")
        if not source or not (root / source).is_file():
            errors.append(f"advanced API source missing: {source}")

    modules = catalog.get("modules") or []
    for module in modules:
        artifact = module.get("artifactId")
        level = module.get("usageLevel")
        recommended = module.get("recommended")
        if level not in VALID_USAGE:
            errors.append(f"invalid/missing usageLevel: {artifact}={level}")
        if not isinstance(recommended, bool):
            errors.append(f"recommended must be boolean: {artifact}")
        if module.get("visibility") == "internal" and level != "internal":
            errors.append(f"internal module must have usageLevel=internal: {artifact}")
        if module.get("visibility") == "public" and level == "internal":
            errors.append(f"public module cannot have usageLevel=internal: {artifact}")
        if level == "golden" and module.get("visibility") != "public":
            errors.append(f"golden module must be public: {artifact}")
    if not any(m.get("usageLevel") == "golden" for m in modules):
        errors.append("catalog has no golden Starter entry points")
    if not any(m.get("usageLevel") == "capability" for m in modules):
        errors.append("catalog has no capability Starter entries")

    root_conventions = (root / "cpf-tools/build/cpf-root-conventions.gradle").read_text(encoding="utf-8")
    developer_shell = (root / "cpf-tools/build/tools/cpf-dev.ps1").read_text(encoding="utf-8-sig")
    developer_shell_sh = (root / "cpf-tools/build/tools/cpf-dev.sh").read_text(encoding="utf-8")
    for token in ("cpfVerifyFast", "cpfVerifyTargeted", "cpfVerifyFullLocal"):
        if token not in root_conventions:
            errors.append(f"root verification tier missing: {token}")
    errors.extend(validate_developer_shell_text(developer_shell, developer_shell_sh))

    lifecycle = (root / "cpf-tools/generator/verification/verify-cpf-generator-lifecycle.py").read_text(encoding="utf-8")
    for token in ("dry-run", "diff", "regenerate", "upgrade", "remove", "restore"):
        if token not in lifecycle:
            errors.append(f"generator safety operation missing: {token}")

    errors.extend(scan_forbidden(root))

    report_tool = root / "cpf-tools/verification/tools/report-cpf-public-function-catalog.py"
    report = subprocess.run(
        [sys.executable, "-B", str(report_tool), "--root", str(root), "--check"],
        cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    )
    if report.returncode != 0:
        errors.append("Public Function TOP 100 stale/missing: " + report.stdout.strip())

    quick = root / "cpf-docs/development/CPF_STARTER_QUICK_SELECT.md"
    golden = root / "cpf-docs/development/CPF_DEVELOPER_GOLDEN_PATH.md"
    for path in (quick, golden):
        if not path.is_file():
            errors.append(f"developer adoption guide missing: {path.relative_to(root)}")
    if quick.is_file():
        quick_text = quick.read_text(encoding="utf-8")
        for module in modules:
            if module.get("visibility") == "public" and module.get("userSelectable") is True:
                artifact = str(module.get("artifactId") or "")
                if artifact and artifact not in quick_text:
                    errors.append(f"Quick Select missing public Starter: {artifact}")
    upgrade_tool = root / "cpf-tools/verification/tools/report-cpf-upgrade-impact.py"
    if not upgrade_tool.is_file():
        errors.append("upgrade impact inspection tool missing")
    if golden.is_file():
        text = golden.read_text(encoding="utf-8")
        for token in ("Application 내부 호출", "CPF Domain 호출", "외부 연계 호출", "CPF REQUIRED", "NATIVE ALLOWED", "DIRECT USE FORBIDDEN", "cpfVerifyTargeted"):
            if token not in text:
                errors.append(f"Golden Path guide missing marker: {token}")

    if errors:
        print("FAIL CPF developer/adoption contract")
        for error in errors:
            print(" -", error)
        return 1
    counts = {level: sum(1 for m in modules if m.get("usageLevel") == level) for level in VALID_USAGE}
    print(f"PASS CPF developer/adoption contract modules={len(modules)} usage={counts} top20=20")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
