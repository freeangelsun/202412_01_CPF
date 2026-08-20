from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
OFFICIAL = ("mariadb", "postgresql", "oracle")
SEED_KINDS = ("product", "optional_sample", "test")


def split_sql(text: str) -> list[str]:
    statements: list[str] = []
    current: list[str] = []
    quote: str | None = None
    index = 0
    while index < len(text):
        char = text[index]
        if quote is not None:
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


def is_seed_mutation(statement: str) -> bool:
    return re.search(r"\b(?:INSERT|MERGE)\b", statement, re.IGNORECASE) is not None


def is_retry_safe(vendor: str, statement: str) -> bool:
    normalized = statement.upper()
    if vendor == "mariadb":
        return "ON DUPLICATE KEY UPDATE" in normalized or "NOT EXISTS" in normalized
    if vendor == "postgresql":
        return "ON CONFLICT" in normalized or "NOT EXISTS" in normalized
    return re.search(r"(?im)^\s*MERGE\s+INTO", statement) is not None or "NOT EXISTS" in normalized


class SeedBundleRuntimeContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.seed_model = json.loads(
            (ROOT / "cpf-tools/db/canonical/seed-model.json").read_text(encoding="utf-8")
        )

    def _bundle(self, vendor: str, kind: str) -> str:
        return (
            ROOT / f"cpf-tools/db/vendor/{vendor}/seed/00_{kind}_seed.sql"
        ).read_text(encoding="utf-8")

    def test_all_canonical_insert_mutations_are_bundled_for_every_vendor(self) -> None:
        canonical_insert_count = sum(
            1 for row in self.seed_model["statements"] if row["statementKind"] == "insert"
        )
        self.assertGreater(canonical_insert_count, 0)
        self.assertEqual(
            canonical_insert_count,
            sum(1 for row in self.seed_model["statements"] if row.get("statementKind") == "insert"),
        )
        for vendor in OFFICIAL:
            mutation_count = 0
            for kind in SEED_KINDS:
                mutation_count += sum(
                    1 for statement in split_sql(self._bundle(vendor, kind))
                    if is_seed_mutation(statement)
                )
            self.assertEqual(canonical_insert_count, mutation_count, vendor)

    def test_every_bundled_seed_mutation_is_retry_safe(self) -> None:
        for vendor in OFFICIAL:
            for kind in SEED_KINDS:
                unsafe = [
                    statement[:240]
                    for statement in split_sql(self._bundle(vendor, kind))
                    if is_seed_mutation(statement) and not is_retry_safe(vendor, statement)
                ]
                self.assertEqual([], unsafe, f"{vendor}/{kind}")

    def test_install_consumers_execute_product_seed_and_fail_closed(self) -> None:
        initializer = (
            ROOT / "cpf-tools/db/tools/initialize-cpf-database.ps1"
        ).read_text(encoding="utf-8")
        runner = (
            ROOT / "cpf-tools/db/tools/invoke-official-db-vendor-sql.ps1"
        ).read_text(encoding="utf-8")

        self.assertIn('SeedMode = "profile"', initializer)
        self.assertIn('00_product_seed.sql', initializer)
        self.assertIn('Mode productSeed', initializer)
        self.assertIn('if ($LASTEXITCODE -ne 0)', initializer)
        self.assertIn('부분 설치 DB를 감지했습니다', initializer)
        self.assertIn("productSeed='productSeed'", runner)
        self.assertIn('\\set ON_ERROR_STOP on', runner)
        self.assertIn('WHENEVER SQLERROR EXIT SQL.SQLCODE', runner)
        self.assertIn('Verify output contract violation', runner)

    def test_product_bundle_has_gateway_permissions_for_every_vendor(self) -> None:
        for vendor in OFFICIAL:
            product = self._bundle(vendor, "product")
            for token in (
                "61_adm_gateway_seed.sql",
                "GATEWAY_DASHBOARD",
                "API_GATEWAY_READ",
                "API_GATEWAY_ROUTE_STATE",
            ):
                self.assertIn(token, product, f"{vendor}: {token}")


if __name__ == "__main__":
    unittest.main()
