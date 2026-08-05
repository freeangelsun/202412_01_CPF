from __future__ import annotations

import importlib.util
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-threat-models.py"
spec = importlib.util.spec_from_file_location("threat_models", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)

NOW = datetime(2026, 8, 5, 1, tzinfo=timezone.utc)


def model():
    return {
        "schema_version": "1.0",
        "model_id": "TM-TEST",
        "version": "1.0.0",
        "title": "Test model",
        "owner": "quality-owner",
        "reviewers": ["security-reviewer"],
        "scope_paths": ["cpf-tools/scripts/**"],
        "review_triggers": ["trust boundary change", "new external input"],
        "assets": [{"id": "A1", "name": "evidence", "classification": "internal"}],
        "trust_boundaries": [{"id": "TB1", "name": "operator to tool"}],
        "data_flows": [{"id": "DF1", "source": "operator", "destination": "tool", "assets": ["A1"], "crosses_boundary": "TB1"}],
        "mitigations": [{"id": "M1", "control": "input validation", "evidence_paths": ["cpf-tools/scripts/tool.py"]}],
        "threats": [{
            "id": "T1", "category": "tampering", "abuse_case": "inject unsafe path",
            "affected_assets": ["A1"], "mitigations": ["M1"], "status": "mitigated",
            "residual_risk": {"severity": "low", "reason": "validated"}
        }],
        "reviewed_at": "2026-08-05T00:00:00Z",
        "expires_at": "2027-08-05T00:00:00Z"
    }


def setup_repo(tmp_path: Path, value=None):
    repo = tmp_path / "repo"; manifests = repo / "cpf-tools/security/threat-models"
    scripts = repo / "cpf-tools/scripts"; manifests.mkdir(parents=True); scripts.mkdir(parents=True)
    (scripts / "tool.py").write_text("print('ok')\n", encoding="utf-8")
    (manifests / "test.json").write_text(json.dumps(value or model()), encoding="utf-8")
    return repo, manifests


def test_valid_model_and_changed_path_coverage(tmp_path: Path):
    repo, manifests = setup_repo(tmp_path)
    result = module.verify(repo, manifests, ["cpf-tools/scripts/tool.py"], now=NOW)
    assert result["status"] == "PASS"
    assert result["models"][0]["threat_count"] == 1


def test_changed_path_without_model_is_rejected(tmp_path: Path):
    repo, manifests = setup_repo(tmp_path)
    with pytest.raises(module.ThreatModelError, match="lack threat-model coverage"):
        module.verify(repo, manifests, ["cpf-admin/frontend/src/app.ts"], now=NOW)


def test_unknown_mitigation_and_missing_evidence_are_rejected(tmp_path: Path):
    value = model(); value["threats"][0]["mitigations"] = ["M404"]
    repo, manifests = setup_repo(tmp_path, value)
    with pytest.raises(module.ThreatModelError, match="unknown mitigations"):
        module.verify(repo, manifests, [], now=NOW)
    value = model(); value["mitigations"][0]["evidence_paths"] = ["missing.txt"]
    repo, manifests = setup_repo(tmp_path / "second", value)
    with pytest.raises(module.ThreatModelError, match="evidence missing"):
        module.verify(repo, manifests, [], now=NOW)


def test_expired_or_high_accepted_risk_is_rejected(tmp_path: Path):
    value = model(); value["expires_at"] = "2026-08-05T00:30:00Z"
    repo, manifests = setup_repo(tmp_path, value)
    with pytest.raises(module.ThreatModelError, match="expired"):
        module.verify(repo, manifests, [], now=NOW)
    value = model(); value["threats"][0]["status"] = "accepted"; value["threats"][0]["residual_risk"] = {"severity":"high","accepted_by":"risk-owner","reason":"temporary"}
    repo, manifests = setup_repo(tmp_path / "second", value)
    with pytest.raises(module.ThreatModelError, match="cannot be accepted"):
        module.verify(repo, manifests, [], now=NOW)


def test_path_escape_and_duplicate_model_id_are_rejected(tmp_path: Path):
    value = model(); value["mitigations"][0]["evidence_paths"] = ["../secret.txt"]
    repo, manifests = setup_repo(tmp_path, value)
    with pytest.raises(module.ThreatModelError, match="unsafe repository path"):
        module.verify(repo, manifests, [], now=NOW)
    repo, manifests = setup_repo(tmp_path / "second")
    (manifests / "duplicate.json").write_text(json.dumps(model()), encoding="utf-8")
    with pytest.raises(module.ThreatModelError, match="duplicate model_id"):
        module.verify(repo, manifests, [], now=NOW)
