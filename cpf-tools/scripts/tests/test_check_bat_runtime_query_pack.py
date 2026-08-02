from __future__ import annotations

import re
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "check-bat-runtime-query-pack.ps1"


class BatRuntimeQueryInlineSqlPatternTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        text = SCRIPT.read_text(encoding="utf-8")
        assignment = re.search(r"\$inlineSqlPattern\s*=\s*'([^']*)'", text)
        if assignment is None:
            raise AssertionError("inline SQL pattern assignment is missing")
        cls.pattern = re.compile(assignment.group(1))

    def test_accepts_sql_literals_for_every_owned_statement_kind(self) -> None:
        samples = (
            'jdbc.query("""\nSELECT * FROM bat_job\n""")',
            'jdbc.update("INSERT INTO bat_job(job_id) VALUES (?)")',
            'jdbc.update("UPDATE bat_job SET use_yn=? WHERE job_id=?")',
            'jdbc.update("DELETE FROM bat_lock WHERE lock_key=?")',
            'jdbc.update("MERGE INTO bat_job target USING source ON (1=1)")',
        )
        for sample in samples:
            with self.subTest(sample=sample):
                self.assertIsNotNone(self.pattern.search(sample))

    def test_rejects_http_delete_method_literals(self) -> None:
        samples = (
            'if ("GET".equals(method) || "DELETE".equals(method)) {}',
            'Set.of("GET", "POST", "PUT", "PATCH", "DELETE")',
        )
        for sample in samples:
            with self.subTest(sample=sample):
                self.assertIsNone(self.pattern.search(sample))


if __name__ == "__main__":
    unittest.main()
