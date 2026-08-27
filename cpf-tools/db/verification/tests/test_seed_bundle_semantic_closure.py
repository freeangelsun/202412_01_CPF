from __future__ import annotations

import json
import re
import unittest
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
VENDORS = ("mariadb", "postgresql", "oracle")
BUNDLES = {
    "product": "productSeedFiles",
    "optional_sample": "optionalSampleSeedFiles",
    "test": "testSeedFiles",
}


def section_sources(text: str) -> list[str]:
    return re.findall(r"(?m)^-- vendor=[a-z]+; source=([^\r\n]+)$", text)


def split_source_sections(text: str) -> dict[str, str]:
    matches = list(re.finditer(r"(?m)^-- vendor=[a-z]+; source=([^\r\n]+)$", text))
    result: dict[str, str] = {}
    for i, match in enumerate(matches):
        end = matches[i + 1].start() if i + 1 < len(matches) else len(text)
        result[match.group(1).strip()] = text[match.end():end]
    return result


def split_sql(text: str) -> list[str]:
    statements: list[str] = []
    current: list[str] = []
    quote: str | None = None
    index = 0
    while index < len(text):
        char = text[index]
        if quote:
            current.append(char)
            if char == quote:
                if index + 1 < len(text) and text[index + 1] == quote:
                    current.append(text[index + 1])
                    index += 1
                else:
                    quote = None
        elif char in ("'", '"'):
            quote = char
            current.append(char)
        elif char == ";":
            statement = "".join(current).strip()
            if statement:
                statements.append(statement)
            current.clear()
        else:
            current.append(char)
        index += 1
    tail = "".join(current).strip()
    if tail:
        statements.append(tail)
    return statements


def canonical_physical_mutation_count(row: dict, vendor: str) -> int:
    if row.get("statementKind") != "insert":
        return 0
    if vendor != "oracle" or row.get("sourceKind") != "values":
        return 1
    source = str(row.get("source") or "")
    count = 0
    depth = 0
    quote = None
    i = 0
    while i < len(source):
        ch = source[i]
        if quote is not None:
            if ch == quote:
                if i + 1 < len(source) and source[i + 1] == quote:
                    i += 2
                    continue
                quote = None
            i += 1
            continue
        if ch in ("'", '"'):
            quote = ch
        elif ch == "(":
            if depth == 0:
                count += 1
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth < 0:
                raise AssertionError("unbalanced canonical VALUES source")
        i += 1
    if depth != 0 or count < 1:
        raise AssertionError("invalid canonical VALUES source")
    return count


class SeedBundleSemanticClosureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.plan = json.loads((ROOT / "cpf-tools/db/config/database-source-plan.json").read_text(encoding="utf-8"))
        cls.model = json.loads((ROOT / "cpf-tools/db/canonical/seed-model.json").read_text(encoding="utf-8"))

    def test_source_and_runtime_seed_bundles_are_byte_identical(self) -> None:
        for vendor in VENDORS:
            for bundle in BUNDLES:
                source = ROOT / f"cpf-tools/db/vendor/{vendor}/source/00_{bundle}_seed.sql"
                runtime = ROOT / f"cpf-tools/db/vendor/{vendor}/seed/00_{bundle}_seed.sql"
                self.assertTrue(source.is_file(), source)
                self.assertTrue(runtime.is_file(), runtime)
                self.assertEqual(source.read_bytes(), runtime.read_bytes(), f"{vendor}/{bundle}")

    def test_generated_section_order_matches_source_plan(self) -> None:
        for vendor in VENDORS:
            for bundle, plan_key in BUNDLES.items():
                path = ROOT / f"cpf-tools/db/vendor/{vendor}/source/00_{bundle}_seed.sql"
                actual = section_sources(path.read_text(encoding="utf-8"))
                self.assertEqual(self.plan[vendor][plan_key], actual, f"{vendor}/{bundle}")

    def test_per_source_statement_counts_match_canonical_model(self) -> None:
        for vendor in VENDORS:
            expected: Counter[str] = Counter()
            for row in self.model["statements"]:
                if row.get("statementKind") == "insert":
                    expected[row["sourceFile"]] += canonical_physical_mutation_count(row, vendor)
            actual: Counter[str] = Counter()
            for bundle in BUNDLES:
                text = (ROOT / f"cpf-tools/db/vendor/{vendor}/source/00_{bundle}_seed.sql").read_text(encoding="utf-8")
                for source, section in split_source_sections(text).items():
                    actual[source] += sum(1 for statement in split_sql(section) if re.search(r"\b(?:INSERT|MERGE)\b", statement, re.IGNORECASE))
            self.assertEqual(expected, actual, vendor)

    def test_canonical_statements_have_no_exact_duplicate_identity(self) -> None:
        identities = []
        for row in self.model["statements"]:
            if row.get("statementKind") != "insert":
                continue
            identities.append((
                row.get("sourceFile"), row.get("logicalDatabase"), row.get("statementKind"),
                row.get("tableName") or row.get("table"),
                tuple(row.get("conflictColumns") or ()),
                re.sub(r"\s+", " ", str(row.get("source", "")).strip()),
            ))
        duplicates = [identity for identity, count in Counter(identities).items() if count > 1]
        self.assertEqual([], duplicates)


if __name__ == "__main__":
    unittest.main()
