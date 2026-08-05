#!/usr/bin/env python3
from __future__ import annotations

import csv
import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parents[2] / "devgpt-control-v9"
sys.path.insert(0, str(SCRIPT_DIR))

import validate_development_management as validator
from apply_qa_reopen_feed import apply_feed, validate_feed
from merge_session_results import merge_rows, validate_merge_rows
from build_full_assignment import canonical_ids_from_row, select_mapping
from devgpt_control_lib import (
    SCENARIO_REQUIREMENT_ALIASES,
    choose_primary,
    read_split_index,
    topo_sort,
    write_csv,
)
from generate_development_requests import (
    assign_entities_to_sessions,
    development_request_id,
    exact_cleanup_command,
    load_explicit_session_plan,
    prepare_immutable_output,
    relative_campaign_root,
    relative_session_root,
)


class ManagementTests(unittest.TestCase):
    def test_choose_primary_explicit(self):
        row = {"requirement_id": "CPF-FR-000001", "canonical_requirement_id": "ARCH-MSA", "work_item_id": "WP-2"}
        candidates = [
            {"entity_id": "WP-1", "axis": "CONTRACT_OWNERSHIP"},
            {"entity_id": "WP-2", "axis": "VERIFICATION_EVIDENCE"},
        ]
        primary, support, basis, score, manual = choose_primary(row, candidates, {"WP-1", "WP-2"})
        self.assertEqual("WP-2", primary)
        self.assertEqual("EXPLICIT_SOURCE_MAPPING", basis)
        self.assertFalse(manual)

    def test_choose_primary_semantic(self):
        row = {"acceptance_criteria": "audit masking secret security authorization 검증"}
        candidates = [
            {"entity_id": "WP-A", "axis": "IMPLEMENTATION_CONSUMER", "axis_title": "구현"},
            {"entity_id": "WP-B", "axis": "OPERATIONS_SECURITY", "axis_title": "보안 감사 마스킹"},
        ]
        primary, _, basis, score, _ = choose_primary(row, candidates, {"WP-A", "WP-B"})
        self.assertEqual("WP-B", primary)
        self.assertGreater(score, 0)

    def test_plural_canonical_ids_accept_slash_delimiter(self):
        known = {"CAN-A", "CAN-B"}
        import re
        pattern = re.compile(r"(CAN-A|CAN-B)")
        canonical, declared = canonical_ids_from_row(
            {"canonical_requirement_ids": "CAN-A / CAN-B"}, known, pattern
        )
        self.assertEqual(["CAN-A", "CAN-B"], canonical)
        self.assertEqual(["CAN-A", "CAN-B"], declared)

    def test_linked_requirement_id_is_supported_parent_key(self):
        self.assertIn("linked_requirement_id", SCENARIO_REQUIREMENT_ALIASES)

    def test_canonical_candidates_respect_function_axis(self):
        items = [
            {"entity_id": "WP-CONTRACT", "canonical_requirement_id": "CAN-A", "axis": "CONTRACT_OWNERSHIP", "axis_title": "contract owner", "markdown_file": "10_ARCH.md"},
            {"entity_id": "WP-IMPL", "canonical_requirement_id": "CAN-A", "axis": "IMPLEMENTATION_CONSUMER", "axis_title": "consumer implementation", "markdown_file": "10_ARCH.md"},
            {"entity_id": "WP-SEC", "canonical_requirement_id": "CAN-A", "axis": "OPERATIONS_SECURITY", "axis_title": "security audit masking", "markdown_file": "10_ARCH.md"},
        ]
        by_id = {row["entity_id"]: row for row in items}
        by_canonical = {"CAN-A": items}
        row = {"function_type": "CONSUMER", "acceptance_criteria": "security-aware actual consumer implementation"}
        primary, _, _, _, _, _ = select_mapping(row, ["CAN-A"], items, by_id, by_canonical, set(by_id))
        self.assertEqual("WP-IMPL", primary)

    def test_global_fallback_is_deterministic_and_canonical_bounded(self):
        items = [
            {"entity_id": "WP-A1", "canonical_requirement_id": "CAN-A", "axis": "GATE_ENGINE", "axis_title": "quality test", "markdown_file": "90_API.md"},
            {"entity_id": "WP-A2", "canonical_requirement_id": "CAN-A", "axis": "VERIFICATION_EVIDENCE", "axis_title": "evidence", "markdown_file": "90_API.md"},
            {"entity_id": "WP-B1", "canonical_requirement_id": "CAN-B", "axis": "CONTRACT_OWNERSHIP", "axis_title": "contract", "markdown_file": "10_ARCH.md"},
        ]
        by_id = {row["entity_id"]: row for row in items}
        by_canonical = {"CAN-A": items[:2], "CAN-B": items[2:]}
        row = {"requirement_group": "TEST", "function_type": "VALIDATION", "acceptance_criteria": "quality test gate evidence"}
        result1 = select_mapping(row, [], items, by_id, by_canonical, set(by_id))
        result2 = select_mapping(row, [], items, by_id, by_canonical, set(by_id))
        self.assertEqual(result1, result2)
        primary, supporting, basis, _, manual, primary_canonical = result1
        self.assertEqual("CAN-A", primary_canonical)
        self.assertTrue(basis.startswith("GLOBAL_"))
        self.assertTrue(manual)
        self.assertNotIn("WP-B1", supporting)

    def test_topological_sort(self):
        order, cycles = topo_sort(["A", "B", "C"], [("A", "B"), ("B", "C")])
        self.assertEqual(["A", "B", "C"], order)
        self.assertEqual([], cycles)

    def test_development_request_id_is_immutable_and_session_scoped(self):
        request_id = development_request_id("CPF-V9-CAMPAIGN", 2, "DEVGPT-V9-S06")
        self.assertEqual("CPF-V9-CAMPAIGN-REV-002-DEVGPT-V9-S06", request_id)
        self.assertNotEqual(request_id, development_request_id("CPF-V9-CAMPAIGN", 3, "DEVGPT-V9-S06"))
        self.assertNotEqual(request_id, development_request_id("CPF-V9-CAMPAIGN", 2, "DEVGPT-V9-S05"))

    def test_explicit_session_plan_requires_exact_active_coverage(self):
        with tempfile.TemporaryDirectory() as td:
            plan = Path(td) / "plan.csv"
            write_csv(
                plan,
                [
                    {"entity_id": "E-1", "session_id": "DEVGPT-V9-S01", "session_role": "개발GPT", "integration_owner": "DEVGPT-V9-S06", "allowed_change_paths": "cpf-core/**"},
                    {"entity_id": "E-2", "session_id": "DEVGPT-V9-S06", "session_role": "개발GPT", "integration_owner": "DEVGPT-V9-S06", "allowed_change_paths": "cpf-tools/**"},
                ],
                ["entity_id", "session_id", "session_role", "integration_owner", "allowed_change_paths"],
            )
            loaded = load_explicit_session_plan(plan, {"E-1", "E-2"})
            assignments, metadata = assign_entities_to_sessions(
                ["E-2", "E-1"], {"E-1": {}, "E-2": {}}, 8, loaded
            )
            self.assertEqual({"E-2": "DEVGPT-V9-S06", "E-1": "DEVGPT-V9-S01"}, assignments)
            self.assertEqual("DEVGPT-V9-S06", metadata["E-2"]["integration_owner"])

            write_csv(
                plan,
                [{"entity_id": "E-1", "session_id": "DEVGPT-V9-S01"}],
                ["entity_id", "session_id"],
            )
            with self.assertRaisesRegex(RuntimeError, "missing=E-2"):
                load_explicit_session_plan(plan, {"E-1", "E-2"})

    def test_explicit_session_plan_rejects_duplicate_unknown_and_empty_session(self):
        with tempfile.TemporaryDirectory() as td:
            plan = Path(td) / "plan.csv"
            write_csv(
                plan,
                [
                    {"entity_id": "E-1", "session_id": "DEVGPT-V9-S01"},
                    {"entity_id": "E-1", "session_id": "DEVGPT-V9-S06"},
                    {"entity_id": "E-X", "session_id": "DEVGPT-V9-S03"},
                    {"entity_id": "E-2", "session_id": ""},
                ],
                ["entity_id", "session_id"],
            )
            with self.assertRaisesRegex(RuntimeError, "duplicate=E-1") as ctx:
                load_explicit_session_plan(plan, {"E-1", "E-2"})
            message = str(ctx.exception)
            self.assertIn("inactive_or_unknown=E-X", message)
            self.assertIn("empty_session=E-2", message)

    def test_immutable_campaign_output(self):
        with tempfile.TemporaryDirectory() as td:
            out = Path(td) / "campaign" / "REV-001"
            prepare_immutable_output(out)
            (out / "existing.txt").write_text("preserve", encoding="utf-8")
            with self.assertRaises(RuntimeError):
                prepare_immutable_output(out)

    def test_session_output_is_versioned_and_cleanup_is_exact(self):
        path = relative_session_root(
            "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9", "DEV-20260805-R01", "DEV-CPF_CORE-001", 2
        )
        self.assertEqual(
            "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/DEV-20260805-R01/REV-002/sessions/DEV-CPF_CORE-001",
            path,
        )
        command = exact_cleanup_command(path)
        self.assertIn("Test-Path -LiteralPath", command)
        self.assertIn(path, command)
        self.assertNotIn("git clean", command)

    def test_campaign_root_is_inside_v9_namespace(self):
        path = relative_campaign_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9", "DEV-20260805-R01", 3)
        self.assertEqual(
            "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/DEV-20260805-R01/REV-003",
            path,
        )
        self.assertNotIn("CPF_DEVELOPMENT_MANAGEMENT_V8", path)
        self.assertNotIn("development-session-results", path)

    def test_campaign_and_session_share_one_cleanup_root(self):
        campaign = relative_campaign_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9", "CAMP-1", 1)
        session = relative_session_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9", "CAMP-1", "SESSION-1", 1)
        self.assertTrue(session.startswith(campaign + "/sessions/"))
        command = exact_cleanup_command(campaign)
        self.assertIn(campaign, command)
        self.assertNotIn("*", command)

    def test_split_dataset_validation(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            part = root / "part.csv"
            part.write_text("id,value\n1,a\n2,b\n", encoding="utf-8")
            sha = hashlib.sha256(part.read_bytes()).hexdigest()
            index = root / "index.csv"
            with index.open("w", encoding="utf-8-sig", newline="") as handle:
                writer = csv.DictWriter(
                    handle,
                    fieldnames=["logical_record_count", "part_sequence", "part_path", "part_record_count", "sha256"],
                )
                writer.writeheader()
                writer.writerow(
                    {"logical_record_count": 2, "part_sequence": 1, "part_path": "part.csv", "part_record_count": 2, "sha256": sha}
                )
            rows, meta = read_split_index(index, root)
            self.assertEqual(2, len(rows))
            self.assertEqual(2, meta["logical_rows"])

    def test_merge_rejects_cross_session_or_request(self):
        rows = [{"entity_id": "E-1", "assigned_session_id": "S-1", "개발GPT_수행상태": "미완료", "개발GPT_자체검수상태": "미완료"}]
        result = [{"entity_id": "E-1", "development_request_id": "REQ-1"}]
        with self.assertRaises(Exception):
            validate_merge_rows(result, {"E-1": rows[0]}, "S-2", "REQ-1")
        with self.assertRaises(Exception):
            validate_merge_rows(result, {"E-1": rows[0]}, "S-1", "REQ-2")

    def test_incomplete_merge_preserves_assignment_and_complete_merge_releases_it(self):
        base = {
            "entity_id": "E-1", "assigned_session_id": "S-1", "개발GPT_수행상태": "미완료",
            "개발GPT_자체검수상태": "미완료", "개발GPT_완료revision": "0", "state_revision": "1",
            "evidence_valid": "false", "impact_invalidated": "false", "qa_reopen_action": "",
            "개발GPT_작업대상상태": "작업 대상", "changed_paths": "", "evidence_ref": "", "open_issue": "", "next_action": ""
        }
        rows = [dict(base)]
        incomplete = [{"entity_id": "E-1", "development_request_id": "REQ-1", "개발GPT_수행상태": "미완료", "개발GPT_자체검수상태": "미완료"}]
        merge_rows(rows, incomplete, "S-1", "REQ-1")
        self.assertEqual("S-1", rows[0]["assigned_session_id"])
        rows = [dict(base)]
        complete = [{"entity_id": "E-1", "development_request_id": "REQ-1", "개발GPT_수행상태": "완료", "개발GPT_자체검수상태": "완료", "evidence_ref": "evidence/x", "completion_candidate_sha": "a" * 40}]
        merge_rows(rows, complete, "S-1", "REQ-1")
        self.assertEqual("", rows[0]["assigned_session_id"])
        self.assertEqual("완료 스킵", rows[0]["개발GPT_작업대상상태"])

    def test_qa_reopen_duplicate_is_rejected_and_redevelop_resets_assignment(self):
        row = {
            "entity_id": "E-1", "assigned_session_id": "S-1", "assignment_revision": "2",
            "qa_reopen_revision": "0", "state_revision": "1", "개발GPT_수행상태": "완료",
            "개발GPT_자체검수상태": "완료", "owner_resolved": "true", "external_blocked": "false",
            "impact_invalidated": "false", "evidence_valid": "true", "open_issue": "", "next_action": ""
        }
        feed = [{"target_entity_id": "E-1", "action": "REDEVELOP", "reason_summary": "regression"}]
        apply_feed([row], feed)
        self.assertEqual("", row["assigned_session_id"])
        self.assertEqual("재개발 요청", row["개발GPT_수행상태"])
        self.assertEqual("미완료", row["개발GPT_자체검수상태"])
        with self.assertRaises(Exception):
            validate_feed(feed + feed, {"E-1": row})

    def test_powershell_wrappers_reference_existing_validator(self):
        validator_wrappers = [
            SCRIPT_DIR / "initialize-development-management.ps1",
            SCRIPT_DIR / "validate-development-management.ps1",
            SCRIPT_DIR / "apply-qa-reopen-feed.ps1",
            SCRIPT_DIR / "merge-development-results.ps1",
        ]
        all_wrappers = validator_wrappers + [SCRIPT_DIR / "bootstrap-and-generate-first-campaign.ps1"]
        validator_path = SCRIPT_DIR / "validate_development_management.py"
        self.assertTrue(validator_path.is_file())
        for wrapper in validator_wrappers:
            content = wrapper.read_text(encoding="utf-8-sig")
            self.assertIn("validate_development_management.py", content)
        for wrapper in all_wrappers:
            content = wrapper.read_text(encoding="utf-8-sig")
            self.assertNotIn("validate_devgpt_control_v9.py", content)
            self.assertNotIn("initialize-devgpt-control-v9.ps1", content)

    def test_full_assignment_validator_recomputes_maps(self):
        with tempfile.TemporaryDirectory() as td:
            mgmt = Path(td)
            generated = mgmt / "generated"
            generated.mkdir()
            index = [
                {"entity_id": "WP-A", "entity_type": "WORK_PACKAGE", "canonical_requirement_id": "CAN-A"},
                {"entity_id": "WP-B", "entity_type": "WORK_PACKAGE", "canonical_requirement_id": "CAN-B"},
            ]
            req_rows = [
                {"requirement_id": "FR-1", "canonical_requirement_ids": "CAN-A", "primary_entity_id": "WP-A", "supporting_entity_ids": "", "mapping_status": "ASSIGNED"},
                {"requirement_id": "FR-2", "canonical_requirement_ids": "CAN-B", "primary_entity_id": "WP-B", "supporting_entity_ids": "", "mapping_status": "ASSIGNED"},
            ]
            sc_rows = [
                {"scenario_id": "SC-1", "requirement_id": "FR-1", "canonical_requirement_ids": "CAN-A", "primary_entity_id": "WP-A", "supporting_entity_ids": "", "mapping_status": "ASSIGNED"},
                {"scenario_id": "SC-2", "requirement_id": "FR-2", "canonical_requirement_ids": "CAN-B", "primary_entity_id": "WP-B", "supporting_entity_ids": "", "mapping_status": "ASSIGNED"},
            ]
            write_csv(generated / "REQUIREMENT_WORK_ITEM_MAP.csv", req_rows)
            write_csv(generated / "SCENARIO_WORK_ITEM_MAP.csv", sc_rows)
            data = {
                "status": "PASS", "baseline_sha": "abc", "canonical_total": 2, "work_package_total": 2,
                "requirement_total": 2, "scenario_total": 2,
                **{key: 0 for key in validator.FULL_ASSIGNMENT_ZERO_KEYS},
                "requirements": {"actual": 2, "unassigned": 0},
                "scenarios": {"actual": 2, "unassigned": 0},
            }
            original = dict(validator.EXPECTED)
            validator.EXPECTED.update({"canonical": 2, "work_packages": 2, "requirements": 2, "scenarios": 2})
            try:
                self.assertEqual([], validator.validate_full_assignment_artifacts(mgmt, data, index))
                forged = dict(data)
                forged["orphan_scenarios"] = 1
                errors = validator.validate_full_assignment_artifacts(mgmt, forged, index)
                self.assertTrue(any("orphan_scenarios" in error for error in errors))
                sc_rows[1]["primary_entity_id"] = "WP-A"
                write_csv(generated / "SCENARIO_WORK_ITEM_MAP.csv", sc_rows)
                errors = validator.validate_full_assignment_artifacts(mgmt, data, index)
                self.assertTrue(any("differs from parent" in error for error in errors))
            finally:
                validator.EXPECTED.clear()
                validator.EXPECTED.update(original)


if __name__ == "__main__":
    unittest.main()
