from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
LIFECYCLE = ROOT / "cpf-tools/generator/verify-domain-lifecycle.ps1"
GENERATOR = ROOT / "cpf-tools/generator/create-domain.ps1"
PROFILES = ROOT / "cpf-tools/generator/contracts/capability-profiles.json"


def _literal_argument(script: str, name: str) -> str:
    pattern = re.compile(rf"'-{re.escape(name)}'\s*,\s*'([^']+)'", re.IGNORECASE)
    match = pattern.search(script)
    assert match is not None, f"missing literal lifecycle argument: {name}"
    return match.group(1)


def test_lifecycle_uses_a_profile_compatible_batch_generation_plan() -> None:
    lifecycle = LIFECYCLE.read_text(encoding="utf-8")
    generator = GENERATOR.read_text(encoding="utf-8")
    profiles = json.loads(PROFILES.read_text(encoding="utf-8"))

    public_profiles = {entry["publicName"] for entry in profiles["profiles"]}
    profile = _literal_argument(lifecycle, "CapabilityProfile")
    flags = {
        name: _literal_argument(lifecycle, name)
        for name in ("Online", "Database", "Batch", "CenterCut", "Ui", "BzaMenu")
    }

    assert profile in public_profiles
    assert profile == "batch-service"
    assert flags["Database"] == "Y"
    assert flags["Batch"] == "Y"
    assert flags["CenterCut"] == "Y"
    assert flags["Online"] == "N"
    assert flags["Ui"] == "N"
    assert flags["BzaMenu"] == "N"

    # Keep this test tied to the product generator's fail-closed profile rules.
    assert "Batch/Center-Cut 생성은 batch-service Profile을 사용해야 합니다" in generator
    assert "Online API 생성은 web-api, secure-api, browser-bff Profile" in generator
    assert "UI 생성은 Online API Profile이 필요합니다" in generator

    # The lifecycle must verify the registration contract without embedding ADM/BZA UI
    # into the generated batch runtime.
    assert "$manifest.serviceRegistration.candidate -ne $true" in lifecycle
    assert "$manifest.bzaMenuEnabled -eq $true" in lifecycle
    assert "Online/UI/BZA remain external to the generated batch runtime" in lifecycle
