from __future__ import annotations

import hashlib
import importlib.util
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest import mock


ROOT = Path(__file__).resolve().parents[3]
SPEC = importlib.util.spec_from_file_location(
    "verify_canonical_vendor_render_immutable_test",
    ROOT / "cpf-tools/db/verify_canonical_vendor_render.py",
)
assert SPEC and SPEC.loader
VERIFIER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFIER)


class ImmutableBaselineRegistryTest(unittest.TestCase):
    def _fixture(self, root: Path) -> tuple[dict, Path, Path]:
        tree = root / "cpf-tools/db/vendor/mariadb/migration"
        tree.mkdir(parents=True)
        sql = tree / "V1__baseline.sql"
        sql.write_text("SELECT 1;\n", encoding="utf-8")
        manifest = tree / "checksums.sha256"
        manifest.write_text(f"{hashlib.sha256(sql.read_bytes()).hexdigest()} *{sql.name}\n", encoding="utf-8")
        registry = root / "cpf-docs/deliverables/SHA256SUMS.txt"
        registry.parent.mkdir(parents=True)
        self._write_registry(root, registry, (sql, manifest))
        contract = {
            "baselineCommit": "a" * 40,
            "trees": [
                {
                    "vendor": "mariadb",
                    "path": "cpf-tools/db/vendor/mariadb/migration",
                    "gitTreeSha": "b" * 40,
                }
            ],
        }
        return contract, sql, registry

    @staticmethod
    def _write_registry(root: Path, registry: Path, files: tuple[Path, ...]) -> None:
        lines = [
            f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.relative_to(root).as_posix()}"
            for path in sorted(files)
        ]
        registry.write_text("\n".join(lines) + "\n", encoding="utf-8")

    def test_missing_local_git_baseline_uses_canonical_digest_registry(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            contract, _, _ = self._fixture(root)
            errors: list[str] = []
            with mock.patch.object(
                VERIFIER.subprocess,
                "run",
                return_value=SimpleNamespace(returncode=128, stdout="", stderr="missing"),
            ):
                provenance = VERIFIER.verify_immutable_migration_history(root, contract, errors.append)
            self.assertEqual("UNAVAILABLE", provenance)
            self.assertEqual([], errors)

    def test_digest_registry_rejects_mutated_released_sql(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            contract, sql, _ = self._fixture(root)
            sql.write_text("SELECT 2;\n", encoding="utf-8")
            errors: list[str] = []
            VERIFIER.verify_canonical_immutable_digest_registry(root, contract, errors.append)
            self.assertTrue(any("canonical immutable digest mismatch" in error for error in errors), errors)
            self.assertTrue(any("versioned file checksum mismatch" in error for error in errors), errors)

    def test_digest_registry_rejects_sql_omitted_from_manifest(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            contract, sql, registry = self._fixture(root)
            omitted = sql.parent / "V2__omitted.sql"
            omitted.write_text("SELECT 2;\n", encoding="utf-8")
            self._write_registry(root, registry, (sql, sql.parent / "checksums.sha256", omitted))
            errors: list[str] = []
            VERIFIER.verify_canonical_immutable_digest_registry(root, contract, errors.append)
            self.assertTrue(any("versioned file missing from checksum manifest" in error for error in errors), errors)

    def test_current_canonical_digest_registry_is_complete(self):
        contract = VERIFIER.load(ROOT / "cpf-tools/db/canonical/immutable-migration-checksums.json")
        errors: list[str] = []
        VERIFIER.verify_canonical_immutable_digest_registry(ROOT, contract, errors.append)
        self.assertEqual([], errors)


if __name__ == "__main__":
    unittest.main()
