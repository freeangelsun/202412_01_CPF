from __future__ import annotations

import csv
import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-requirement-traceability.py"
spec = importlib.util.spec_from_file_location("traceability", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class TraceabilityNegativeTest(unittest.TestCase):
    def write_csv(self, path: Path, rows: list[dict[str, str]]) -> None:
        with path.open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
            writer.writeheader()
            writer.writerows(rows)

    def test_verification_cannot_complete_before_development(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "bad.csv"
            self.write_csv(path, [{"development_status": "부분 구현", "verification_status": "완료"}])
            _, rows = module.read_csv(path)
            with self.assertRaises(module.GateError):
                module.check_statuses(path, rows)

    def test_duplicate_requirement_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "duplicate.csv"
            self.write_csv(path, [{"requirement_id": "A"}, {"requirement_id": "A"}])
            _, rows = module.read_csv(path)
            with self.assertRaises(module.GateError):
                module.unique_ids(path, rows, "requirement_id")


    def result_row(self, **overrides: str) -> dict[str, str]:
        row = {
            "requirement_id": "CPF-SELF-DEV-001",
            "source_type": "SELF",
            "development_status": "완료",
            "verification_status": "미검증",
            "source_paths": "src/source.txt",
            "consumer_paths": "src/consumer.txt",
            "test_paths": "tests/test.txt",
            "evidence_paths": "",
            "source_sha": "",
            "result_sha": "",
        }
        row.update(overrides)
        return row

    def test_completed_row_rejects_missing_file(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "src").mkdir()
            (root / "tests").mkdir()
            (root / "src/source.txt").write_text("source", encoding="utf-8")
            (root / "src/consumer.txt").write_text("consumer", encoding="utf-8")
            path = root / "result.csv"
            self.write_csv(path, [self.result_row()])
            with self.assertRaises(module.GateError):
                module.check_result_matrix(root, path, {"CPF-SELF-DEV-001"}, False, None)

    def test_completed_row_rejects_parent_traversal(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            path = root / "result.csv"
            self.write_csv(path, [self.result_row(source_paths="../outside.txt")])
            with self.assertRaises(module.GateError):
                module.check_result_matrix(root, path, {"CPF-SELF-DEV-001"}, False, None)

    def test_completed_row_accepts_existing_relative_paths(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for relative in ("src/source.txt", "src/consumer.txt", "tests/test.txt"):
                target = root / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(relative, encoding="utf-8")
            path = root / "result.csv"
            self.write_csv(path, [self.result_row()])
            completed, verified = module.check_result_matrix(
                root, path, {"CPF-SELF-DEV-001"}, False, None
            )
            self.assertEqual((completed, verified), (1, 0))

    def test_invalid_status_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "status.csv"
            self.write_csv(path, [{"development_status": "PASS", "verification_status": "미검증"}])
            _, rows = module.read_csv(path)
            with self.assertRaises(module.GateError):
                module.check_statuses(path, rows)


if __name__ == "__main__":
    unittest.main()
