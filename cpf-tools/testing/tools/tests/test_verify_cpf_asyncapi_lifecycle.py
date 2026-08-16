from __future__ import annotations

import importlib.util
import json
import sys
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-asyncapi-lifecycle.py"
spec = importlib.util.spec_from_file_location("asyncapi_lifecycle", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)
SHA = "a" * 40


def document(version="1.0.0"):
    return {
        "asyncapi": "3.1.0",
        "info": {"title": "test", "version": version},
        "channels": {"events": {"address": "cpf.test.events", "messages": {"TestEvent": {"$ref": "#/components/messages/TestEvent"}}}},
        "operations": {"sendTestEvent": {"action": "send", "channel": {"$ref": "#/channels/events"}, "messages": [{"$ref": "#/channels/events/messages/TestEvent"}]}},
        "components": {
            "messages": {"TestEvent": {"messageId": "TestEvent.v1", "correlationId": {"location": "$message.header#/correlationId"}, "payload": {"$ref": "#/components/schemas/TestPayload"}}},
            "schemas": {"TestPayload": {"type": "object", "required": ["messageId"], "properties": {"messageId": {"type": "string"}}}}
        }
    }


def setup(tmp_path: Path, doc=None, entry_changes=None):
    repo = tmp_path / "repo"; contract = repo / "cpf-tools/contracts/asyncapi"; contract.mkdir(parents=True)
    (repo / "source.java").write_text("class Source {}\n", encoding="utf-8")
    (repo / "consumer.java").write_text("class Consumer {}\n", encoding="utf-8")
    doc_path = contract / "event.asyncapi.json"; doc_path.write_text(json.dumps(doc or document()), encoding="utf-8")
    entry = {"path": "cpf-tools/contracts/asyncapi/event.asyncapi.json", "owner": "event-owner", "compatibility": "BACKWARD", "source_contracts": ["source.java"], "consumers": ["consumer.java"], "previous_schema_version": None}
    if entry_changes: entry.update(entry_changes)
    catalog = {"schema_version": "1.0", "source_commit_sha": SHA, "supported_asyncapi_versions": ["3.1.0"], "source_inventory": [], "documents": [entry]}
    catalog_path = contract / "catalog.json"; catalog_path.write_text(json.dumps(catalog), encoding="utf-8")
    return repo, catalog_path


def test_valid_document_source_and_consumer(tmp_path: Path):
    repo, catalog = setup(tmp_path)
    result = module.verify(repo, catalog)
    assert result["status"] == "PASS"
    assert result["documents"][0]["consumer_count"] == 1


def test_inventory_can_prove_exact_sha_path(tmp_path: Path):
    repo, catalog = setup(tmp_path, entry_changes={"source_contracts": ["remote/source.java"]})
    value = json.loads(catalog.read_text())
    value["source_inventory"] = [{"path": "remote/source.java", "type": "blob", "sha": SHA}]
    catalog.write_text(json.dumps(value), encoding="utf-8")
    assert module.verify(repo, catalog)["status"] == "PASS"


def test_rejects_unsupported_version_missing_consumer_and_duplicate_message_id(tmp_path: Path):
    doc = document(); doc["asyncapi"] = "2.6.0"
    repo, catalog = setup(tmp_path, doc)
    with pytest.raises(module.AsyncApiError, match="unsupported"):
        module.verify(repo, catalog)
    repo, catalog = setup(tmp_path / "second", entry_changes={"consumers": []})
    with pytest.raises(module.AsyncApiError, match="non-empty"):
        module.verify(repo, catalog)
    doc = document(); doc["components"]["messages"]["Second"] = dict(doc["components"]["messages"]["TestEvent"])
    repo, catalog = setup(tmp_path / "third", doc)
    with pytest.raises(module.AsyncApiError, match="messageId"):
        module.verify(repo, catalog)


def test_schema_change_requires_migration_and_evidence(tmp_path: Path):
    repo, catalog = setup(tmp_path, entry_changes={"previous_schema_version": "0.9.0"})
    with pytest.raises(module.AsyncApiError, match="requires READY/COMPLETE migration"):
        module.verify(repo, catalog)
    value = json.loads(catalog.read_text())
    value["documents"][0]["migration"] = {"status": "READY", "evidence_paths": ["missing.md"]}
    catalog.write_text(json.dumps(value), encoding="utf-8")
    with pytest.raises(module.AsyncApiError, match="migration evidence missing"):
        module.verify(repo, catalog)


def test_rejects_unsafe_and_invalid_inventory(tmp_path: Path):
    repo, catalog = setup(tmp_path)
    value = json.loads(catalog.read_text()); value["source_inventory"] = [{"path": "../x", "type": "blob", "sha": SHA}]
    catalog.write_text(json.dumps(value), encoding="utf-8")
    with pytest.raises(module.AsyncApiError, match="unsafe repository path"):
        module.verify(repo, catalog)
