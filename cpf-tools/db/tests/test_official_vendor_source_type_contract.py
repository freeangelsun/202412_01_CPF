from __future__ import annotations

import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
DB = ROOT / "cpf-tools/db"


class OfficialVendorSourceTypeContractTest(unittest.TestCase):
    def sql_paths(self, vendor: str) -> list[Path]:
        root = DB / "vendor" / vendor
        paths = sorted((root / "source").glob("*.sql")) + sorted((root / "install").glob("*.sql"))
        self.assertTrue(paths, vendor)
        return paths

    def assert_pattern_absent(self, vendor: str, pattern: str) -> None:
        compiled = re.compile(pattern)
        for path in self.sql_paths(vendor):
            self.assertIsNone(
                compiled.search(path.read_text(encoding="utf-8-sig")),
                path.relative_to(ROOT).as_posix(),
            )

    def pattern_count(self, vendor: str, pattern: str) -> int:
        compiled = re.compile(pattern)
        return sum(
            len(compiled.findall(path.read_text(encoding="utf-8-sig")))
            for path in self.sql_paths(vendor)
        )

    def test_bounded_binary_is_rendered_for_each_official_vendor(self):
        self.assert_pattern_absent("postgresql", r"(?i)\bVARBINARY\b")
        self.assertGreaterEqual(
            self.pattern_count("postgresql", r"(?i)\b(?:access_iv|refresh_iv)\s+BYTEA\b"), 4
        )
        self.assert_pattern_absent("oracle", r"(?i)\b(?:VARBINARY|BYTEA)\b")
        self.assertGreaterEqual(
            self.pattern_count("oracle", r"(?i)\b(?:access_iv|refresh_iv)\s+RAW\(32\)(?=\s|,|$)"), 4
        )

    def test_retired_split_schema_projections_are_absent(self):
        for vendor in ("mariadb", "postgresql", "oracle"):
            for name in ("20_cmn_schema.sql", "30_adm_schema.sql"):
                self.assertFalse((DB / "vendor" / vendor / "source" / name).exists(), f"{vendor}/{name}")

    def test_official_generator_owns_both_vendor_conversions(self):
        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("if($u -match '^VARBINARY\\(\\d+\\)$'){return 'BYTEA'}", generator)
        self.assertIn('if($u -match \'^VARBINARY\\((\\d+)\\)$\'){return "RAW($($Matches[1]))"}', generator)

    def test_shared_schema_output_uses_explicit_canonical_order(self):
        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        # logicalDatabases 순회는 seed 표현식의 DB prefix 제거용이었고 renderer 가 이어받았다.
        # 생성기에 남는 계약은 schema 파일 출력 순서다.
        self.assertIn("foreach($sourceFile in $sourceSchemaFiles)", generator)
        self.assertIn(
            "cpfDB|cmnDB|admDB|batDB|bzaDB|refDB",
            (DB / "render_vendor_pack.py").read_text(encoding="utf-8-sig"))
        self.assertNotIn("foreach($db in $fileByDb.Keys)", generator)

    def test_oracle_current_fresh_schema_orders_default_before_not_null(self):
        current_fresh_inputs = (
            DB / "vendor/oracle/source/10_cpf_schema.sql",
            DB / "vendor/oracle/source/40_business_modules_schema.sql",
            DB / "vendor/oracle/install/00_empty_install.sql",
        )
        invalid = re.compile(r"(?i)\bNOT\s+NULL\s+DEFAULT\b")
        for path in current_fresh_inputs:
            self.assertIsNone(
                invalid.search(path.read_text(encoding="utf-8-sig")),
                path.relative_to(ROOT).as_posix(),
            )

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        render_table = generator.split("function Render-Table", 1)[1].split(
            "if($v -eq 'mariadb')", 1
        )[0]
        oracle_branch = render_table.split("if($v -eq 'oracle')", 1)[1].split("}else{", 1)[0]
        self.assertLess(
            oracle_branch.index("$line+=' DEFAULT '"),
            oracle_branch.index("$line+=' NOT NULL'"),
        )

    def test_oracle_current_schema_uses_supported_length_function(self):
        current_oracle_inputs = (
            DB / "generated/current/oracle/cpf-platform-schema.sql",
            DB / "vendor/oracle/source/10_cpf_schema.sql",
            DB / "vendor/oracle/install/00_empty_install.sql",
        )
        invalid = re.compile(r"(?i)\bCHAR_LENGTH\s*\(")
        for path in current_oracle_inputs:
            text = path.read_text(encoding="utf-8-sig")
            self.assertIsNone(invalid.search(text), path.relative_to(ROOT).as_posix())
            self.assertGreaterEqual(len(re.findall(r"(?i)\bLENGTH\s*\(", text)), 4)

        canonical = (DB / "canonical/platform-schema.json").read_text(encoding="utf-8-sig")
        self.assertEqual(4, len(re.findall(r'"oracle": "LENGTH\(', canonical)))

    def test_oracle_current_fresh_schema_maps_time_to_portable_text(self):
        lifecycle_inputs = (
            DB / "vendor/oracle/source/10_cpf_schema.sql",
            DB / "vendor/oracle/install/00_empty_install.sql",
        )
        invalid = re.compile(r"(?im)^\s*[A-Za-z][A-Za-z0-9_]*\s+TIME(?:\s|,|$)")
        for path in lifecycle_inputs:
            text = path.read_text(encoding="utf-8-sig")
            self.assertIsNone(invalid.search(text), path.relative_to(ROOT).as_posix())
            self.assertIn("available_start_time VARCHAR2(15 CHAR)", text)
            self.assertIn("available_end_time VARCHAR2(15 CHAR)", text)

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("if($u -eq 'TIME'){return 'VARCHAR2(15 CHAR)'}", generator)

    def test_oracle_current_schema_omits_unsupported_restrict_clause(self):
        current_oracle_inputs = (
            DB / "generated/current/oracle/cpf-platform-schema.sql",
            DB / "generated/current/oracle/backoffice-schema.sql",
            DB / "vendor/oracle/source/10_cpf_schema.sql",
            DB / "vendor/oracle/source/40_business_modules_schema.sql",
            DB / "vendor/oracle/install/00_empty_install.sql",
        )
        invalid = re.compile(r"(?i)\bON\s+DELETE\s+(?:RESTRICT|NO\s+ACTION)\b")
        for path in current_oracle_inputs:
            self.assertIsNone(
                invalid.search(path.read_text(encoding="utf-8-sig")),
                path.relative_to(ROOT).as_posix(),
            )

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        renderer = (DB / "render_vendor_pack.py").read_text(encoding="utf-8-sig")
        self.assertIn("$onDelete-in@('RESTRICT','NO ACTION')", generator)
        self.assertIn('on_delete in {"RESTRICT", "NO ACTION"}', renderer)

    def test_oracle_values_seed_preserves_parent_before_child_order(self):
        current_oracle_seeds = (
            DB / "generated/current/oracle/cpf-platform-seed.sql",
            DB / "vendor/oracle/source/60_adm_seed_data.sql",
            DB / "vendor/oracle/seed/00_product_seed.sql",
        )
        for path in current_oracle_seeds:
            text = path.read_text(encoding="utf-8-sig")
            first_menu = text.index("MERGE INTO ADM_MENU")
            first_statement = text[first_menu : text.index(";", first_menu)]
            self.assertNotIn("UNION ALL", first_statement, path.relative_to(ROOT).as_posix())
            self.assertGreater(text.count("MERGE INTO ADM_MENU"), 20)
            self.assertLess(text.index("'GATEWAY_DASHBOARD'"), text.index("'GATEWAY_SERVERS'"))

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        renderer = (DB / "render_vendor_pack.py").read_text(encoding="utf-8-sig")
        # Vendor seed SQL 의 단일 소유자는 renderer 다. 생성기가 자체 MERGE 렌더링을 되살리면
        # 같은 파일을 두 규칙으로 쓰던 경쟁 write 가 재발한다.
        self.assertNotIn('return ($merges-join"`n")', generator)
        self.assertIn("return '\\n'.join(statements)", renderer)

    def test_seed_updates_only_reference_columns_supplied_by_the_statement(self):
        seed = json.loads((DB / "canonical/seed-model.json").read_text(encoding="utf-8-sig"))
        invalid: list[str] = []
        conflict_updates: list[str] = []
        for index, statement in enumerate(seed["statements"]):
            source_columns = {str(column).lower() for column in statement.get("columns", [])}
            conflict_columns = {
                str(column).lower() for column in statement.get("conflictColumns", [])
            }
            for update in statement.get("updates", []):
                for referenced in re.findall(
                    r"VALUES\(([A-Za-z0-9_]+)\)", update["expression"], flags=re.I
                ):
                    if referenced.lower() not in source_columns:
                        invalid.append(
                            f"{index}:{statement.get('tableName')}:{update['column']}:{referenced}"
                        )
                if update["column"].lower() in conflict_columns:
                    conflict_updates.append(
                        f"{index}:{statement.get('tableName')}:{update['column']}"
                    )
        self.assertEqual([], invalid)
        self.assertEqual([], conflict_updates)

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        renderer = (DB / "render_vendor_pack.py").read_text(encoding="utf-8-sig")
        diagnostic = "update references VALUES("
        self.assertIn(diagnostic, renderer)
        self.assertNotIn(diagnostic, generator)
        conflict_diagnostic = "is also a conflict column"
        self.assertIn(conflict_diagnostic, renderer)
        self.assertNotIn(conflict_diagnostic, generator)

    def test_oracle_lifecycle_sql_respects_sqlplus_physical_line_limit(self):
        oversized: list[str] = []
        for path in sorted((DB / "vendor/oracle").rglob("*.sql")):
            for line_number, line in enumerate(
                path.read_text(encoding="utf-8-sig").splitlines(), start=1
            ):
                if len(line) > 4999:
                    oversized.append(
                        f"{path.relative_to(ROOT).as_posix()}:{line_number}:{len(line)}"
                    )
        self.assertEqual([], oversized)

        generator = (DB / "generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("$predicates -join \" OR`n               \"", generator)

    def test_oracle_sequence_verify_counts_only_the_managed_namespace(self):
        verify = (DB / "vendor/oracle/verify/00_verify.sql").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("SUBSTR(sequence_name, 1, 7) = 'BAT_SB_'", verify)
        self.assertNotIn("(SELECT COUNT(*) FROM user_sequences) = 3", verify)
        for name in (
            "BAT_SB_JOB_INSTANCE_SEQ",
            "BAT_SB_JOB_EXECUTION_SEQ",
            "BAT_SB_STEP_EXECUTION_SEQ",
        ):
            self.assertIn(name, verify)
        for retired in (
            "BATCH_JOB_INSTANCE_SEQ",
            "BATCH_JOB_EXECUTION_SEQ",
            "BATCH_STEP_EXECUTION_SEQ",
            "BATCH_JOB_SEQ",
        ):
            self.assertIn(retired, verify)


if __name__ == "__main__":
    unittest.main()
