from __future__ import annotations

import importlib.util
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
VERIFIER = ROOT / "cpf-tools/verification/tools/verify-cpf-physical-db-consolidation.py"


def load_verifier():
    spec = importlib.util.spec_from_file_location("cpf_physical_db_gate", VERIFIER)
    module = importlib.util.module_from_spec(spec)
    assert spec and spec.loader
    spec.loader.exec_module(module)
    return module


def test_current_repository_physical_db_contract_passes():
    module = load_verifier()
    failures, info = module.verify(ROOT)
    assert failures == []
    assert info == {}


def test_legacy_jdbc_target_is_detected(tmp_path: Path):
    module = load_verifier()
    sample = "spring.datasource.url=jdbc:mariadb://localhost:3306/admDB\n"
    assert module.actionable_legacy_hits(sample) == ["admDB"]


def test_historical_plain_metadata_is_not_actionable():
    module = load_verifier()
    sample = "legacyLogicalDatabase=batDB\nimmutable migration owner refDB\n"
    assert module.actionable_legacy_hits(sample) == []


def test_canonical_policy_is_exact_current_four_without_reference_fixture():
    data = json.loads((ROOT / "cpf-tools/db/canonical/platform-schema.json").read_text(encoding="utf-8"))
    policy = data["canonicalPolicy"]
    assert set(policy["productionPhysicalTargets"]) == {"cpfDB", "mbwDB", "mbrDB", "exsDB"}
    assert set(policy["removedProductionPhysicalTargets"]) == {"cmnDB", "admDB", "batDB", "bzaDB", "refDB"}
    assert "REFERENCE_FIXTURE" not in policy["platformDatabaseArchitecture"]
