from __future__ import annotations

import importlib.util
import json
import sys
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-openapi-profile-lifecycle.py"
spec = importlib.util.spec_from_file_location("openapi_profile", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)

SHA = "a" * 40


def document(version="3.1.0"):
    return {
        "openapi": version,
        "info": {"title": "Test", "version": "1.0.0", "description": "safe **markdown**"},
        "paths": {"/v1/items": {"get": {"operationId": "listItems", "responses": {"200": {"description": "ok"}}}}},
        "components": {"schemas": {"Item": {"type": "object", "properties": {"id": {"type": "string"}}}}},
    }


def profile():
    return {
        "schema_version": "1.0",
        "source_commit_sha": SHA,
        "versions": [
            {"version": "3.1.*", "canonical_patch": "3.1.2", "status": "SUPPORTED"},
            {"version": "3.2.*", "canonical_patch": "3.2.0", "status": "MIGRATION_PREVIEW"},
        ],
        "reference_policy": {"cycle_policy": "REJECT", "allow_external_refs": False},
        "sanitization": {"mode": "FAIL_CLOSED", "reject_active_content": True},
        "generator_capabilities": [{
            "generator": "cpf-typescript-client", "status": "SUPPORTED", "openapi_profiles": ["3.1.*"],
            "evidence_paths": ["cpf-tools/scripts/generator.py"]
        }],
        "migration": {"from": "3.1.*", "to": "3.2.*", "status": "PLANNED", "evidence_paths": ["cpf-tools/contracts/openapi/migration.json"]},
        "documents": [{"path": "api/openapi.json", "owner": "api-owner", "consumers": ["frontend/client.ts"]}],
    }


def setup_repo(tmp_path: Path, doc=None, prof=None):
    repo = tmp_path / "repo"
    (repo / "api").mkdir(parents=True)
    (repo / "cpf-tools/scripts").mkdir(parents=True)
    (repo / "cpf-tools/contracts/openapi").mkdir(parents=True)
    (repo / "frontend").mkdir(parents=True)
    (repo / "api/openapi.json").write_text(json.dumps(doc or document()), encoding="utf-8")
    (repo / "cpf-tools/scripts/generator.py").write_text("print('ok')\n", encoding="utf-8")
    (repo / "cpf-tools/contracts/openapi/migration.json").write_text("{}\n", encoding="utf-8")
    (repo / "frontend/client.ts").write_text("export {}\n", encoding="utf-8")
    profile_path = repo / "cpf-tools/contracts/openapi/cpf-openapi-profile.json"
    profile_path.write_text(json.dumps(prof or profile()), encoding="utf-8")
    return repo, profile_path


def test_supported_31_document_passes(tmp_path: Path):
    repo, profile_path = setup_repo(tmp_path)
    result = module.verify(repo, profile_path)
    assert result["status"] == "PASS"
    assert result["documents"][0]["openapi_version"] == "3.1.0"
    assert result["documents"][0]["operation_count"] == 1


def test_32_document_fails_while_preview(tmp_path: Path):
    repo, profile_path = setup_repo(tmp_path, doc=document("3.2.0"))
    with pytest.raises(module.OpenApiProfileError, match="not supported for release"):
        module.verify(repo, profile_path)


def test_reference_cycle_and_external_ref_fail_closed(tmp_path: Path):
    doc = document(); doc["components"]["schemas"] = {
        "A": {"$ref": "#/components/schemas/B"}, "B": {"$ref": "#/components/schemas/A"}
    }
    repo, profile_path = setup_repo(tmp_path, doc=doc)
    with pytest.raises(module.OpenApiProfileError, match="reference cycles rejected"):
        module.verify(repo, profile_path)
    doc = document(); doc["components"]["schemas"]["Item"] = {"$ref": "https://evil.example/schema.json"}
    repo, profile_path = setup_repo(tmp_path / "external", doc=doc)
    with pytest.raises(module.OpenApiProfileError, match="external \\$ref is forbidden"):
        module.verify(repo, profile_path)


def test_active_content_and_duplicate_operation_id_rejected(tmp_path: Path):
    doc = document(); doc["info"]["description"] = "<script>alert(1)</script>"
    repo, profile_path = setup_repo(tmp_path, doc=doc)
    with pytest.raises(module.OpenApiProfileError, match="unsafe Markdown/HTML"):
        module.verify(repo, profile_path)
    doc = document(); doc["paths"]["/v1/other"] = {"post": {"operationId": "listItems", "responses": {"200": {"description": "ok"}}}}
    repo, profile_path = setup_repo(tmp_path / "duplicate", doc=doc)
    with pytest.raises(module.OpenApiProfileError, match="duplicate operationId"):
        module.verify(repo, profile_path)


def test_generator_and_migration_evidence_fail_closed(tmp_path: Path):
    value = profile(); value["generator_capabilities"][0]["openapi_profiles"] = ["3.2.*"]
    repo, profile_path = setup_repo(tmp_path, prof=value)
    with pytest.raises(module.OpenApiProfileError, match="claims unsupported"):
        module.verify(repo, profile_path)
    value = profile(); value["migration"]["evidence_paths"] = ["missing.json"]
    repo, profile_path = setup_repo(tmp_path / "missing", prof=value)
    with pytest.raises(module.OpenApiProfileError, match="migration evidence missing"):
        module.verify(repo, profile_path)
