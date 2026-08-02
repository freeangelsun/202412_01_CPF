from __future__ import annotations

import importlib.util
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/scripts/verify-cpf-qa32-repository-security.py"
SPEC = importlib.util.spec_from_file_location("qa32_repository_security", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


class Qa32RepositorySecurityTest(unittest.TestCase):
    def test_dynamic_sql_detector_ignores_static_bound_sql_and_d_plus_zero(self):
        self.assertFalse(MODULE.has_dynamic_sql(
            Path("Repository.java"),
            'jdbc.update("UPDATE item SET row_version=row_version+1 WHERE id=?", request.requestedBy());',
        ))
        self.assertFalse(MODULE.has_dynamic_sql(
            Path("seed.sql"),
            "SELECT 'D+0' AS parameter_value FROM dual;",
        ))

    def test_dynamic_sql_detector_rejects_request_concatenation(self):
        unsafe_sources = (
            'String sql = "SELECT * FROM item WHERE state=\'OPEN\' AND id=" + requestId;',
            'String sql = "SELECT * FROM item WHERE id=" + sanitize(requestId);',
            'String sql = "SELECT * FROM item WHERE id=" + prefix + requestId;',
            'String sql = "SELECT * FROM item WHERE id=%s".formatted(requestId);',
            'String sql = String.format("SELECT * FROM item WHERE id=%s", requestId);',
            'builder.append("SELECT * FROM item WHERE id=").append(requestId);',
            'const sql = `SELECT * FROM item WHERE id=${requestId}`;',
        )
        for source in unsafe_sources:
            with self.subTest(source=source):
                self.assertTrue(MODULE.has_dynamic_sql(Path("Unsafe.java"), source))
        self.assertTrue(MODULE.has_dynamic_sql(
            Path("unsafe.sql"),
            "EXECUTE IMMEDIATE 'DELETE FROM item WHERE id=' || request_input;",
        ))

    def test_exact_allowlists_are_live_and_still_match_their_rule(self):
        for rule_name, paths in MODULE.ALLOW.items():
            if rule_name == "DYNAMIC_SQL":
                continue
            rule = MODULE.RULES[rule_name]
            for relative in paths:
                path = ROOT / relative
                self.assertTrue(path.is_file(), f"stale allowlist path: {relative}")
                text = path.read_text(encoding="utf-8", errors="strict")
                self.assertRegex(text, rule, f"unnecessary allowlist path: {rule_name}:{relative}")

    def test_json_report_is_always_utf8_on_windows(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "도커_검증.ps1"
            source.write_text("[IO.File]::ReadAllBytes('input')\n", encoding="utf-8")
            report = root / "report.json"
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--root", str(root), "--json-report", str(report)],
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
            )
            self.assertNotEqual(result.returncode, 0)
            decoded = report.read_bytes().decode("utf-8")
            self.assertIn("도커_검증.ps1", decoded)


if __name__ == "__main__":
    unittest.main()
