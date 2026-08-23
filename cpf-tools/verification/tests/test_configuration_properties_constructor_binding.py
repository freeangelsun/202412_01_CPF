from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class ConfigurationPropertiesConstructorBindingTest(unittest.TestCase):
    def test_records_with_compatibility_constructors_select_canonical_binding_constructor(self) -> None:
        violations: list[str] = []
        for path in ROOT.rglob("*.java"):
            if any(part in {"build", ".gradle"} for part in path.parts):
                continue
            text = path.read_text(encoding="utf-8")
            if "@ConfigurationProperties" not in text:
                continue
            record_match = re.search(r"\brecord\s+(\w+)", text)
            if not record_match:
                continue
            name = record_match.group(1)
            compatibility_constructors = re.findall(rf"public\s+{re.escape(name)}\s*\(", text)
            if compatibility_constructors and "@ConstructorBinding" not in text:
                violations.append(path.relative_to(ROOT).as_posix())
        self.assertEqual([], violations)


if __name__ == "__main__":
    unittest.main()
