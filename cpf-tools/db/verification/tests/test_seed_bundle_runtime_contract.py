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

    def test_adm_button_seed_respects_canonical_menu_action_unique_key(self) -> None:
        entries: list[tuple[str, str, str]] = []
        tuple_pattern = re.compile(
            r"\(\s*'(?P<button>[^']+)'\s*,\s*'(?P<menu>[^']+)'\s*,\s*'(?P<action>[^']+)'"
        )
        for statement in self.seed_model["statements"]:
            if statement.get("table") != "ADM_BUTTON":
                continue
            entries.extend(
                (match.group("button"), match.group("menu"), match.group("action"))
                for match in tuple_pattern.finditer(statement.get("source", ""))
            )
        self.assertGreater(len(entries), 0)
        owners: dict[tuple[str, str], str] = {}
        collisions: list[tuple[tuple[str, str], str, str]] = []
        for button, menu, action in entries:
            key = (menu, action)
            if key in owners:
                collisions.append((key, owners[key], button))
            else:
                owners[key] = button
        self.assertEqual([], collisions)
        actions = {button: action for button, _, action in entries}
        self.assertEqual("DOWNLOAD", actions["REMOTE_LOG_DOWNLOAD"])
        self.assertEqual("BUNDLE_DOWNLOAD", actions["REMOTE_LOG_BUNDLE_DOWNLOAD"])
        self.assertEqual("JOB_DOWNLOAD", actions["REMOTE_LOG_JOB_DOWNLOAD"])

    def test_adm_button_actions_reach_every_maintained_product_seed_consumer(self) -> None:
        expected = {
            "REMOTE_LOG_DOWNLOAD": "DOWNLOAD",
            "REMOTE_LOG_BUNDLE_DOWNLOAD": "BUNDLE_DOWNLOAD",
            "REMOTE_LOG_JOB_DOWNLOAD": "JOB_DOWNLOAD",
        }
        for vendor in OFFICIAL:
            consumers = (
                ROOT / f"cpf-tools/db/vendor/{vendor}/source/00_product_seed.sql",
                ROOT / f"cpf-tools/db/vendor/{vendor}/seed/00_product_seed.sql",
                ROOT / f"cpf-tools/db/generated/current/{vendor}/cpf-platform-seed.sql",
            )
            for consumer in consumers:
                text = consumer.read_text(encoding="utf-8-sig")
                for button, action in expected.items():
                    pattern = (
                        rf"'{button}'(?:\s+(?:AS\s+)?BUTTON_ID)?\s*,\s*"
                        rf"'REMOTE_LOG'(?:\s+(?:AS\s+)?MENU_ID)?\s*,\s*"
                        rf"'{action}'"
                    )
                    self.assertRegex(text, pattern, consumer.relative_to(ROOT).as_posix())

    def test_api_permission_routes_have_one_owner_and_aggregate_button_roles(self) -> None:
        route_projections = [
            statement["source"]
            for statement in self.seed_model["statements"]
            if statement.get("table") == "ADM_API_PERMISSION"
            and statement.get("sourceKind") == "select"
        ]
        self.assertEqual(2, len(route_projections))
        for source in route_projections:
            self.assertIn("ROW_NUMBER() OVER", source)
            self.assertIn(
                "PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN",
                source,
            )
            self.assertIn("ORDER BY SORT_ORDER, BUTTON_ID", source)
            self.assertIn("WHERE CPF_ROUTE_OWNER_RANK = 1", source)
        self.assertIn("NOT EXISTS", route_projections[1])
        self.assertIn("existing.HTTP_METHOD", route_projections[1])
        self.assertIn("existing.API_PATH", route_projections[1])

        role_projections = [
            statement["source"]
            for statement in self.seed_model["statements"]
            if statement.get("table") == "ADM_ROLE_API_PERMISSION"
            and "JOIN ADM_API_PERMISSION ap" in statement.get("source", "")
        ]
        self.assertEqual(2, len(role_projections))
        for source in role_projections:
            self.assertIn("JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID", source)
            self.assertIn("ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')", source)
            self.assertIn("ap.API_PATH = b.API_PATTERN", source)
            self.assertIn("CASE WHEN MAX(rb.ALLOW_YN) = 'Y'", source)
            self.assertIn("GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID", source)

    def test_backoffice_permission_seed_uses_schema_owned_identity(self) -> None:
        schema = json.loads(
            (ROOT / "cpf-tools/db/canonical/platform-schema.json").read_text(encoding="utf-8")
        )
        table = next(row for row in schema["tables"] if row["targetTableName"] == "MBW_PERMISSION")
        unique = next(row for row in table["uniqueKeys"] if row["name"] == "uk_mbw_permission_scope")
        expected = [
            "role_code", "menu_code", "button_code", "permission_type", "environment_code"
        ]
        self.assertEqual(expected, unique["columns"])

        product_permissions = [
            statement for statement in self.seed_model["statements"]
            if statement.get("tableName") == "MBW_PERMISSION"
            and statement.get("sourceFile") == "56_backoffice_product_seed.sql"
        ]
        self.assertEqual(3, len(product_permissions))
        for statement in product_permissions:
            self.assertEqual(expected, statement["conflictColumns"])
        combined_source = "\n".join(statement["source"] for statement in product_permissions)
        self.assertNotIn("/api/v1/backoffice/approvals/*/actions", combined_source)
        self.assertIn("/api/v1/backoffice/approvals/*/decisions", combined_source)

        postgresql_upsert = (
            ROOT / "cpf-tools/db/runtime-template/backoffice/vendor/postgresql/repository/operation-save-permission.sql.template"
        ).read_text(encoding="utf-8")
        self.assertIn(
            "ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code)",
            postgresql_upsert,
        )
        oracle_upsert = (
            ROOT / "cpf-tools/db/runtime-template/backoffice/vendor/oracle/repository/operation-save-permission.sql.template"
        ).read_text(encoding="utf-8")
        self.assertIn("target.permission_type = source.permission_type", oracle_upsert)
        self.assertIn("target.environment_code = source.environment_code", oracle_upsert)

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
