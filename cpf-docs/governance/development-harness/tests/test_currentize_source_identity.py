from __future__ import annotations
import importlib.util
import json
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "validators" / "currentize_source_identity.py"
spec = importlib.util.spec_from_file_location("currentize_source_identity", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def test_historical_reviewer_provenance_is_never_rewritten(tmp_path: Path):
    p = tmp_path / "CODEX_FINDING_CLOSURE.csv"
    p.write_text("work_item_id,source_identity\nWP-X,old\n", encoding="utf-8")
    assert module.rewrite_csv_source_identity(p, "new") is False
    assert "old" in p.read_text(encoding="utf-8")


def test_current_authority_source_identity_is_rewritten(tmp_path: Path):
    p = tmp_path / "CURRENT_WORK_ITEM_REGISTRY.csv"
    p.write_text("work_item_id,source_identity\nWP-X,old\n", encoding="utf-8")
    assert module.rewrite_csv_source_identity(p, "new") is True
    assert ",new" in p.read_text(encoding="utf-8-sig")


def test_merge_control_baseline_is_currentized_without_rewriting_session_provenance(tmp_path: Path):
    p = tmp_path / "CURRENT_MERGE_CONTROL_STATE.json"
    p.write_text('{"merge_baseline_source_identity":"old","last_merged_session_key":"session-a"}\n', encoding="utf-8")
    assert module.update_merge_control_state(p, "a" * 64) is True
    text = p.read_text(encoding="utf-8")
    assert '"merge_baseline_source_identity": "' + ("a" * 64) + '"' in text
    assert '"last_merged_session_key": "session-a"' in text
    assert module.update_merge_control_state(p, "a" * 64) is False


def test_role_owned_execution_summary_is_preserved_when_requested(tmp_path: Path):
    p = tmp_path / "DEVGPT_CURRENT_EXECUTION_SUMMARY.json"
    original = {"sourceIdentitySha256": "old", "sourceFileCount": 1, "status": "VERIFICATION_PENDING"}
    p.write_text(json.dumps(original), encoding="utf-8")
    source = {"contentSha256": "new", "fileCount": 2}

    assert module.currentize_role_execution_summary(p, source, preserve_role_evidence=True) is False
    assert json.loads(p.read_text(encoding="utf-8")) == original


def test_role_owned_execution_summary_is_currentized_only_without_preserve_flag(tmp_path: Path):
    p = tmp_path / "DEVGPT_CURRENT_EXECUTION_SUMMARY.json"
    p.write_text('{"sourceIdentitySha256":"old","sourceFileCount":1}', encoding="utf-8")
    source = {"contentSha256": "new", "fileCount": 2}

    assert module.currentize_role_execution_summary(p, source, preserve_role_evidence=False) is True
    assert json.loads(p.read_text(encoding="utf-8")) == {
        "sourceIdentitySha256": "new",
        "sourceFileCount": 2,
    }


def test_identity_file_records_exact_working_tree_and_head(tmp_path: Path):
    p = tmp_path / "SOURCE_IDENTITY.json"
    p.write_text(
        json.dumps({"baselineProductContentSha256": "preserve", "currentWorkingTreeStatus": "old"}),
        encoding="utf-8",
    )
    source = {
        "contentSha1": "1" * 40,
        "contentSha256": "2" * 64,
        "fileCount": 8470,
        "totalBytes": 123456,
        "identityPolicy": "GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES",
    }

    module.update_identity_file(p, source, "3" * 40)

    result = json.loads(p.read_text(encoding="utf-8"))
    assert result["baselineProductContentSha256"] == "preserve"
    assert result["finalReplayProductContentSha256"] == "2" * 64
    assert result["finalReplayProductFileCount"] == 8470
    assert result["currentWorkingTreeProductContentSha1"] == "1" * 40
    assert result["currentWorkingTreeProductContentSha256"] == "2" * 64
    assert result["currentWorkingTreeProductFileCount"] == 8470
    assert result["currentWorkingTreeProductTotalBytes"] == 123456
    assert result["currentWorkingTreeGitSha"] == "3" * 40
    assert result["currentWorkingTreeStatus"] == "IN_PROGRESS"
