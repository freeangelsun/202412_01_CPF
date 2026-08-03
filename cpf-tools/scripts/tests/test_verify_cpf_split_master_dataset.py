from __future__ import annotations

import csv
import hashlib
import importlib.util
import json
import tempfile
import unittest
import sys
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-split-master-dataset.py"
SPEC = importlib.util.spec_from_file_location("cpf_split_master", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


def write_csv(path: Path, fields: list[str], rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


class SplitMasterDatasetTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.original = {key: value.copy() for key, value in MODULE.DATASETS.items()}
        MODULE.DATASETS = {
            "requirement": {
                "index": "current/requirements.csv", "name": "REQ", "id_column": "requirement_id", "minimum_count": 2
            },
            "scenario": {
                "index": "current/scenarios.csv", "name": "SCN", "id_column": "scenario_id", "minimum_count": 2
            },
            "execution": {
                "index": "current/execution.csv", "name": "EXE", "id_column": "execution_order", "minimum_count": 2
            },
        }
        self._create_valid_fixture()

    def tearDown(self) -> None:
        MODULE.DATASETS = self.original
        self.temp.cleanup()

    def _index(self, kind: str, part: str, first_id: str, last_id: str, header: list[str]) -> None:
        part_path = self.root / part
        row = {
            "dataset_manifest_version": "2",
            "dataset_name": MODULE.DATASETS[kind]["name"],
            "dataset_kind": "split_csv_logical_master_index",
            "logical_record_count": "2",
            "logical_header_sha256": MODULE.normalized_header_sha(header),
            "part_sequence": "1",
            "part_path": part,
            "part_record_count": "2",
            "first_record_id": first_id,
            "last_record_id": last_id,
            "size_bytes": str(part_path.stat().st_size),
            "sha256": sha(part_path),
            "load_order": "part_sequence ASC",
            "assembly_rule": "all parts",
            "consumer_rule": "index is not data",
        }
        write_csv(self.root / MODULE.DATASETS[kind]["index"], MODULE.INDEX_COLUMNS, [row])

    def _create_valid_fixture(self) -> None:
        req_fields = ["requirement_id", "development_status", "verification_status", "개발GPT_상태", "QA_검수결과"]
        req_part = "current/requirements.parts/part.csv"
        write_csv(self.root / req_part, req_fields, [
            {"requirement_id": "R1", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
            {"requirement_id": "R2", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
        ])
        self._index("requirement", req_part, "R1", "R2", req_fields)

        scn_fields = ["scenario_id", "linked_requirement_id", "execution_status"]
        scn_part = "current/scenarios.parts/part.csv"
        write_csv(self.root / scn_part, scn_fields, [
            {"scenario_id": "S1", "linked_requirement_id": "R1", "execution_status": "미검증"},
            {"scenario_id": "S2", "linked_requirement_id": "R2", "execution_status": "미검증"},
        ])
        self._index("scenario", scn_part, "S1", "S2", scn_fields)

        exe_fields = ["execution_order", "requirement_id", "phase_id", "work_package_id", "development_status", "verification_status"]
        exe_part = "current/execution.parts/part.csv"
        write_csv(self.root / exe_part, exe_fields, [
            {"execution_order": "00-1", "requirement_id": "R1", "phase_id": "P00", "work_package_id": "W1", "development_status": "재확인 필요", "verification_status": "미검증"},
            {"execution_order": "00-2", "requirement_id": "R2", "phase_id": "P00", "work_package_id": "W1", "development_status": "재확인 필요", "verification_status": "미검증"},
        ])
        self._index("execution", exe_part, "00-1", "00-2", exe_fields)

    def _validate_all(self, repair: bool = False):
        results = {}
        rows = {}
        for kind in ("requirement", "scenario", "execution"):
            results[kind], rows[kind], _ = MODULE.validate_dataset(self.root, kind, repair)
        MODULE.validate_statuses(rows["requirement"], rows["scenario"], rows["execution"])
        MODULE.validate_cross_links(rows["requirement"], rows["scenario"], rows["execution"])
        return results, rows

    def test_valid_split_dataset_and_scope(self) -> None:
        _, rows = self._validate_all()
        scope = MODULE.build_scope(rows["execution"], rows["scenario"], 2)
        self.assertEqual(2, scope["requirement_count"])
        self.assertEqual(2, scope["scenario_count"])
        self.assertEqual("W1", scope["last_work_package_id"])

    def test_repository_byte_metadata_mismatch_fails_closed(self) -> None:
        part = self.root / "current/requirements.parts/part.csv"
        part.write_bytes(part.read_bytes() + b"\n")
        with self.assertRaisesRegex(MODULE.ValidationError, "metadata mismatch"):
            MODULE.validate_dataset(self.root, "requirement", False)

    def test_repair_updates_only_index_metadata(self) -> None:
        part = self.root / "current/requirements.parts/part.csv"
        raw = part.read_bytes().replace(b"R2", b"R2") + b"\n"
        part.write_bytes(raw)
        index = self.root / "current/requirements.csv"
        fields, rows = MODULE.read_index(index)
        rows[0]["part_record_count"] = "3"
        rows[0]["first_record_id"] = "WRONG-FIRST"
        rows[0]["last_record_id"] = "WRONG-LAST"
        write_csv(index, fields, rows)
        MODULE.validate_dataset(self.root, "requirement", True)
        MODULE.validate_dataset(self.root, "requirement", False)
        _, repaired = MODULE.read_index(index)
        self.assertEqual("2", repaired[0]["part_record_count"])
        self.assertEqual("R1", repaired[0]["first_record_id"])
        self.assertEqual("R2", repaired[0]["last_record_id"])

    def test_canonical_primary_id_gap_is_rejected(self) -> None:
        part = self.root / "current/requirements.parts/part.csv"
        fields = ["requirement_id", "development_status", "verification_status", "개발GPT_상태", "QA_검수결과"]
        write_csv(part, fields, [
            {"requirement_id": "CPF-FR-000001", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
            {"requirement_id": "CPF-FR-000003", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
        ])
        self._index("requirement", "current/requirements.parts/part.csv", "CPF-FR-000001", "CPF-FR-000003", fields)
        with self.assertRaisesRegex(MODULE.ValidationError, "continuity broken"):
            MODULE.validate_dataset(self.root, "requirement", False)

    def test_parent_traversal_is_rejected(self) -> None:
        index = self.root / "current/requirements.csv"
        fields, rows = MODULE.read_index(index)
        rows[0]["part_path"] = "../outside.csv"
        write_csv(index, fields, rows)
        with self.assertRaisesRegex(MODULE.ValidationError, "parent traversal"):
            MODULE.validate_dataset(self.root, "requirement", False)

    def test_duplicate_primary_id_is_rejected(self) -> None:
        part = self.root / "current/requirements.parts/part.csv"
        fields = ["requirement_id", "development_status", "verification_status", "개발GPT_상태", "QA_검수결과"]
        write_csv(part, fields, [
            {"requirement_id": "R1", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
            {"requirement_id": "R1", "development_status": "재확인 필요", "verification_status": "미검증", "개발GPT_상태": "미완료", "QA_검수결과": ""},
        ])
        self._index("requirement", "current/requirements.parts/part.csv", "R1", "R1", fields)
        with self.assertRaisesRegex(MODULE.ValidationError, "duplicate"):
            MODULE.validate_dataset(self.root, "requirement", False)


    def test_historical_package_identity_and_membership_are_preserved(self) -> None:
        results, rows = self._validate_all()
        package_path = self.root / "cpf-docs/work/manifest/CPF_PACKAGE_MANIFEST.json"
        package_path.parent.mkdir(parents=True, exist_ok=True)
        tracked = "current/requirements.csv"
        package = {
            "packageId": "HISTORICAL-PACKAGE",
            "baselineSha": "1" * 40,
            "files": [{
                "path": tracked,
                "sha256": "0" * 64,
                "size": 0,
            }],
        }
        package_path.write_text(json.dumps(package), encoding="utf-8")
        new_gate = self.root / "cpf-tools/scripts/new-session-gate.py"
        new_gate.parent.mkdir(parents=True, exist_ok=True)
        new_gate.write_text("print('pass')\n", encoding="utf-8")
        summary = {"status": "PASS", "repository_root": "."}
        MODULE.update_package_manifest(self.root, results, summary, [tracked, "cpf-tools/scripts/new-session-gate.py"])
        repaired = json.loads(package_path.read_text(encoding="utf-8"))
        self.assertEqual("HISTORICAL-PACKAGE", repaired["packageId"])
        self.assertEqual("1" * 40, repaired["baselineSha"])
        self.assertEqual(MODULE.VERIFIED_AGAINST_SHA, repaired["verifiedAgainstSha"])
        self.assertEqual([tracked], [item["path"] for item in repaired["files"]])
        self.assertEqual(MODULE.sha256_file(self.root / tracked), repaired["files"][0]["sha256"])

    def test_unknown_scenario_requirement_link_is_rejected(self) -> None:
        _, rows = self._validate_all()
        rows["scenario"][0]["linked_requirement_id"] = "UNKNOWN"
        with self.assertRaisesRegex(MODULE.ValidationError, "unknown requirements"):
            MODULE.validate_cross_links(rows["requirement"], rows["scenario"], rows["execution"])


if __name__ == "__main__":
    unittest.main()
