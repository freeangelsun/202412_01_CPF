from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class ClassBasedAopFinalTypeContractTest(unittest.TestCase):
    def test_transactional_production_types_are_not_final(self) -> None:
        violations: list[str] = []
        for path in ROOT.rglob("*.java"):
            normalized = path.as_posix()
            if "/src/main/java/" not in normalized or any(part in {"build", ".gradle"} for part in path.parts):
                continue
            text = path.read_text(encoding="utf-8")
            if "@Transactional" not in text:
                continue
            if re.search(r"(?m)^\s*(?:public\s+)?final\s+class\s+[A-Za-z0-9_]+", text):
                violations.append(path.relative_to(ROOT).as_posix())
        self.assertEqual([], violations)


if __name__ == "__main__":
    unittest.main()
