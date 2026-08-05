from __future__ import annotations

import hashlib
import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "verify_migration_lifecycle.py"
SPEC = importlib.util.spec_from_file_location("verify_migration_lifecycle", MODULE_PATH)
assert SPEC and SPEC.loader
MOD = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MOD
SPEC.loader.exec_module(MOD)

SHA = "a" * 40
VENDORS = ("mariadb", "postgresql", "oracle")


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_text(path: Path, text: str = "fixture\n") -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def write_sql(path: Path, body: str = "select 1;\n") -> None:
    write_text(path, body)


def write_sql_with_checksum(pack: Path, name: str, body: str = "select 1;\n") -> None:
    pack.mkdir(parents=True, exist_ok=True)
    target = pack / name
    target.write_text(body, encoding="utf-8")
    digest = hashlib.sha256(body.encode("utf-8")).hexdigest()
    manifest = pack / "checksums.sha256"
    previous = manifest.read_text(encoding="utf-8") if manifest.exists() else ""
    manifest.write_text(previous + f"{digest} *{name}\n", encoding="utf-8")


class Fixture:
    def __init__(self, root: Path) -> None:
        self.root = root
        db = root / "cpf-tools/db"
        scripts = root / "cpf-tools/scripts"
        write_text(scripts / "runtime.ps1", "Write-Output 'fixture'\n")
        write_text(scripts / "static.ps1", "Write-Output 'fixture'\n")
        write_text(db / "verify_migration_lifecycle.py", "# fixture verifier\n")
        write_text(scripts / "initialize-domain-database.ps1", "Write-Output 'fixture'\n")
        write_json(db / "cpf-backup-lifecycle-contract.json", {"schemaVersion": 1})
        write_json(db / "cpf-pitr-lifecycle-contract.json", {"schemaVersion": 1})

        lifecycle = {
            "schemaVersion": 1,
            "officialVendors": list(VENDORS),
            "orderedStages": [
                "baseline-install",
                "sequential-upgrade",
                "runtime-query",
                "schema-drift",
                "reverse-rollback",
                "forward-reapply",
                "backup-restore",
                "point-in-time-recovery",
            ],
            "runtimeExecutor": "cpf-tools/scripts/runtime.ps1",
            "backupContract": "cpf-tools/db/cpf-backup-lifecycle-contract.json",
            "pitrContract": "cpf-tools/db/cpf-pitr-lifecycle-contract.json",
            "requiredStaticGates": [
                "cpf-tools/scripts/static.ps1",
                "cpf-tools/db/verify_migration_lifecycle.py",
            ],
            "evidenceRequired": [
                "sourceSha",
                "vendor",
                "databaseVersion",
                "command",
                "startedAt",
                "endedAt",
                "exitCode",
                "stageResults",
                "schemaHashBefore",
                "schemaHashAfter",
                "sanitized",
            ],
            "migrationDiscoveryPolicy": {
                "mode": "ALL_CHECKSUM_MANIFEST_MIGRATIONS",
                "hardCodedVersionAllowlistForbidden": True,
                "recursiveLogicalDatabasePacks": True,
                "rollbackOrForwardRecoveryRequired": True,
                "unknownResultFailClosed": True,
                "reapplyChecksumLocked": True,
            },
            "runtimeExecutorRequirements": {
                "migrationSelection": "ALL_DISCOVERED_FROM_CHECKSUM_MANIFEST",
                "partialFailureCheckpoint": True,
                "unknownResultReconcileBeforeRetry": True,
                "reverseRollbackThenForwardReapply": True,
                "backupManifestRequiredForDestructiveTransition": True,
            },
            "vendorContracts": {},
        }
        vendors = {}
        for vendor in VENDORS:
            root_rel = f"cpf-tools/db/vendor/{vendor}"
            if vendor == "mariadb":
                migration_rel = f"{root_rel}/migration/flyway"
                rollback_rel = f"{root_rel}/rollback"
                pack_name = "root"
                migration_pack = db / f"vendor/{vendor}/migration/flyway"
                rollback_pack = db / f"vendor/{vendor}/rollback"
            else:
                migration_rel = f"{root_rel}/migration/flyway/{{logicalDatabase}}"
                rollback_rel = f"{root_rel}/rollback/{{logicalDatabase}}"
                pack_name = "cpfDB"
                migration_pack = db / f"vendor/{vendor}/migration/flyway/cpfDB"
                rollback_pack = db / f"vendor/{vendor}/rollback/cpfDB"
            lifecycle["vendorContracts"][vendor] = {
                "migrationRoot": migration_rel,
                "rollbackRoot": rollback_rel,
            }
            lifecycle_paths = {
                "provision": f"{root_rel}/provision/00_provision.sql",
                "emptyInstall": f"{root_rel}/install/00_empty_install.sql",
                "productSeed": f"{root_rel}/seed/00_product_seed.sql",
                "optionalSampleSeed": f"{root_rel}/seed/00_optional_sample_seed.sql",
                "testSeed": f"{root_rel}/seed/00_test_seed.sql",
                "verify": f"{root_rel}/verify/00_verify.sql",
                "migration": migration_rel,
                "rollback": rollback_rel,
            }
            vendors[vendor] = {
                "pack": f"{root_rel}/pack.json",
                "vendorRoot": root_rel,
                "lifecycle": lifecycle_paths,
                "runtimeRoot": f"{root_rel}/runtime",
                "domainTemplateRoot": f"{root_rel}/domain-template",
            }
            for key in ("provision", "emptyInstall", "productSeed", "optionalSampleSeed", "testSeed", "verify"):
                write_sql(root / lifecycle_paths[key])
            for module in ("cpf", "cmn"):
                write_sql(db / f"vendor/{vendor}/runtime/{module}/query.sql")
            write_text(db / f"vendor/{vendor}/domain-template/template.sql", "select '${CPF_DOMAIN}';\n")
            write_sql_with_checksum(migration_pack, "V1__baseline.sql")
            write_sql(rollback_pack / "R1__baseline.sql", "drop table example;\n")
            pack = {
                "schemaVersion": 4,
                "vendor": vendor,
                "status": "완료",
                "runtimeVerification": "미검증",
                "canonicalConsumerRoot": root_rel,
                "lifecycleStatus": {
                    "provision": "완료",
                    "install": "완료",
                    "seed": "완료",
                    "migration": "완료",
                    "verify": "완료",
                    "rollback": "완료",
                },
                "runtimeModules": {
                    "cpf": {"ownerArtifact": "cpf-core"},
                    "cmn": {"ownerArtifact": "cpf-common"},
                },
                "generatedDomainContract": {
                    "metadataDriven": True,
                    "fixedDomainList": False,
                    "templateRoot": "domain-template",
                    "databaseBootstrapScript": "cpf-tools/scripts/initialize-domain-database.ps1",
                },
                "migrationLocationPattern": migration_rel,
                "rollbackLocationPattern": rollback_rel,
                "fixturePackName": pack_name,
            }
            write_json(db / f"vendor/{vendor}/pack.json", pack)

        write_json(
            db / "vendor-pack-manifest.json",
            {
                "schemaVersion": 2,
                "supportedVendors": list(VENDORS),
                "officialVendors": list(VENDORS),
                "candidateVendors": [],
                "vendors": vendors,
            },
        )
        write_json(db / "cpf-db-lifecycle-contract.json", lifecycle)
        self.policy = db / "migration-lifecycle-policy.json"
        write_json(self.policy, self.valid_policy())

    @staticmethod
    def valid_policy() -> dict:
        return {
            "schemaVersion": 1,
            "officialVendors": list(VENDORS),
            "stateModel": {
                "states": [
                    "NOT_STARTED",
                    "RUNNING",
                    "APPLIED",
                    "ROLLED_BACK",
                    "FORWARD_RECOVERED",
                    "FAILED",
                    "UNKNOWN",
                ]
            },
            "unknownResultPolicy": {
                "failClosed": True,
                "reconcileRequired": True,
                "automaticRetryAllowed": False,
                "reconcileIdentity": "vendor|logicalDatabase|version|checksumSha256",
            },
            "partialFailurePolicy": {
                "checkpointRequired": True,
                "stopAtFirstFailedStatement": True,
                "subsequentMigrationBlocked": True,
                "requiredEvidence": ["failedStatementHash"],
            },
            "reapplyPolicy": {
                "checksumMustMatch": True,
                "duplicateApplyMustFailClosed": True,
                "driftResult": "FAILED_RECONCILIATION_REQUIRED",
            },
            "unpairedMigrationDefault": {
                "id": "TEST-FORWARD",
                "strategy": "FORWARD_RECOVERY",
                "reason": "restore and checksum-locked reapply",
                "recoveryPlan": [
                    "cpf-tools/db/cpf-backup-lifecycle-contract.json",
                    "cpf-tools/db/cpf-pitr-lifecycle-contract.json",
                ],
            },
            "overrides": [],
        }

    def vendor_pack(self, vendor: str) -> tuple[Path, dict]:
        path = self.root / f"cpf-tools/db/vendor/{vendor}/pack.json"
        return path, json.loads(path.read_text(encoding="utf-8"))

    def manifest(self) -> tuple[Path, dict]:
        path = self.root / "cpf-tools/db/vendor-pack-manifest.json"
        return path, json.loads(path.read_text(encoding="utf-8"))

    def contract(self) -> tuple[Path, dict]:
        path = self.root / "cpf-tools/db/cpf-db-lifecycle-contract.json"
        return path, json.loads(path.read_text(encoding="utf-8"))


class VerifyMigrationLifecycleTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.fixture = Fixture(self.root)

    def tearDown(self) -> None:
        self.temp.cleanup()

    def verify(self):
        return MOD.verify(self.root, self.fixture.policy, SHA)

    def test_complete_three_vendor_lifecycle_passes(self):
        report = self.verify()
        self.assertEqual("PASS", report["result"])
        self.assertEqual(3, report["migrationCount"])
        self.assertEqual(3, report["rollbackClassified"])
        self.assertEqual(0, report["forwardRecoveryClassified"])
        self.assertEqual(set(VENDORS), set(report["vendorMigrationCounts"]))
        self.assertEqual(18, sum(report["vendorLifecycleFileCounts"].values()))
        self.assertEqual(40, len(report["sourceSha"]))

    def test_checksum_drift_fails(self):
        target = self.root / "cpf-tools/db/vendor/mariadb/migration/flyway/V1__baseline.sql"
        target.write_text("select 2;\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "checksum mismatch"):
            self.verify()

    def test_missing_checksum_entry_fails(self):
        pack = self.root / "cpf-tools/db/vendor/mariadb/migration/flyway"
        (pack / "V2__extra.sql").write_text("select 2;\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "missing from checksum manifest"):
            self.verify()

    def test_duplicate_flyway_version_fails(self):
        pack = self.root / "cpf-tools/db/vendor/mariadb/migration/flyway"
        write_sql_with_checksum(pack, "V1__duplicate.sql", "select 3;\n")
        with self.assertRaisesRegex(MOD.ContractError, "duplicate Flyway version"):
            self.verify()

    def test_unpaired_migration_uses_explicit_forward_recovery(self):
        rollback = self.root / "cpf-tools/db/vendor/mariadb/rollback/R1__baseline.sql"
        rollback.unlink()
        report = self.verify()
        row = next(item for item in report["migrations"] if item["vendor"] == "mariadb")
        self.assertEqual("FORWARD_RECOVERY", row["strategy"])
        self.assertEqual("TEST-FORWARD", row["ruleId"])

    def test_rollback_in_other_logical_database_does_not_match(self):
        root = self.root / "cpf-tools/db/vendor/postgresql"
        (root / "rollback/cpfDB/R1__baseline.sql").unlink()
        other_pack = root / "migration/flyway/refDB"
        write_sql_with_checksum(other_pack, "V1__reference.sql", "select 9;\n")
        other_rollback = root / "rollback/refDB"
        write_sql(other_rollback / "R1__reference.sql", "select 8;\n")
        report = self.verify()
        rows = [item for item in report["migrations"] if item["vendor"] == "postgresql"]
        cpf = next(item for item in rows if item["pack"] == "cpfDB")
        ref = next(item for item in rows if item["pack"] == "refDB")
        self.assertEqual("FORWARD_RECOVERY", cpf["strategy"])
        self.assertEqual("ROLLBACK", ref["strategy"])

    def test_unpaired_migration_without_recovery_plan_fails(self):
        policy = Fixture.valid_policy()
        policy["unpairedMigrationDefault"]["recoveryPlan"] = []
        write_json(self.fixture.policy, policy)
        (self.root / "cpf-tools/db/vendor/mariadb/rollback/R1__baseline.sql").unlink()
        with self.assertRaisesRegex(MOD.ContractError, "lacks recoveryPlan"):
            self.verify()

    def test_unknown_result_cannot_auto_retry(self):
        policy = Fixture.valid_policy()
        policy["unknownResultPolicy"]["automaticRetryAllowed"] = True
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "must not permit automatic retry"):
            self.verify()

    def test_unknown_result_requires_stable_identity(self):
        policy = Fixture.valid_policy()
        policy["unknownResultPolicy"]["reconcileIdentity"] = "version"
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "identity is not stable"):
            self.verify()

    def test_partial_failure_requires_checkpoint(self):
        policy = Fixture.valid_policy()
        policy["partialFailurePolicy"]["checkpointRequired"] = False
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "checkpointRequired"):
            self.verify()

    def test_partial_failure_blocks_following_migrations(self):
        policy = Fixture.valid_policy()
        policy["partialFailurePolicy"]["subsequentMigrationBlocked"] = False
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "subsequentMigrationBlocked"):
            self.verify()

    def test_ambiguous_override_fails_closed(self):
        policy = Fixture.valid_policy()
        policy["overrides"] = [
            {"id": "ONE", "vendors": ["mariadb"], "versions": [1], "strategy": "ROLLBACK", "reason": "one"},
            {"id": "TWO", "vendors": ["mariadb"], "migrationPattern": "V1__*.sql", "strategy": "ROLLBACK", "reason": "two"},
        ]
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "ambiguous recovery policy"):
            self.verify()

    def test_reversed_version_range_fails(self):
        policy = Fixture.valid_policy()
        policy["overrides"] = [
            {"id": "BAD", "vendors": ["mariadb"], "versions": "9-1", "strategy": "ROLLBACK", "reason": "bad"}
        ]
        write_json(self.fixture.policy, policy)
        with self.assertRaisesRegex(MOD.ContractError, "reversed versions selector"):
            self.verify()

    def test_missing_vendor_pack_fails(self):
        (self.root / "cpf-tools/db/vendor/oracle/pack.json").unlink()
        with self.assertRaisesRegex(MOD.ContractError, "oracle pack missing"):
            self.verify()

    def test_malformed_checksum_line_fails(self):
        manifest = self.root / "cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"
        manifest.write_text("not-a-checksum *V1__baseline.sql\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "malformed checksum line"):
            self.verify()

    def test_missing_lifecycle_verify_fails(self):
        (self.root / "cpf-tools/db/vendor/oracle/verify/00_verify.sql").unlink()
        with self.assertRaisesRegex(MOD.ContractError, "oracle lifecycle verify missing"):
            self.verify()

    def test_empty_lifecycle_install_fails(self):
        (self.root / "cpf-tools/db/vendor/postgresql/install/00_empty_install.sql").write_text("", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "lifecycle emptyInstall is empty"):
            self.verify()

    def test_comment_only_verify_fails(self):
        (self.root / "cpf-tools/db/vendor/mariadb/verify/00_verify.sql").write_text("-- no assertions\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "has no executable SQL"):
            self.verify()

    def test_runtime_module_consumer_missing_fails(self):
        target = self.root / "cpf-tools/db/vendor/mariadb/runtime/cmn/query.sql"
        target.unlink()
        with self.assertRaisesRegex(MOD.ContractError, "runtime module consumer missing or empty"):
            self.verify()

    def test_domain_template_empty_fails(self):
        target = self.root / "cpf-tools/db/vendor/oracle/domain-template/template.sql"
        target.unlink()
        with self.assertRaisesRegex(MOD.ContractError, "domainTemplateRoot is empty"):
            self.verify()

    def test_unsupported_vendor_fails(self):
        path, manifest = self.fixture.manifest()
        manifest["supportedVendors"].append("mysql")
        write_json(path, manifest)
        with self.assertRaisesRegex(MOD.ContractError, "supportedVendors must be exactly"):
            self.verify()

    def test_candidate_vendor_fails_release_gate(self):
        path, manifest = self.fixture.manifest()
        manifest["candidateVendors"] = ["mysql"]
        write_json(path, manifest)
        with self.assertRaisesRegex(MOD.ContractError, "candidateVendors must be empty"):
            self.verify()

    def test_required_static_gates_must_consume_verifier(self):
        path, contract = self.fixture.contract()
        contract["requiredStaticGates"] = ["cpf-tools/scripts/static.ps1"]
        write_json(path, contract)
        with self.assertRaisesRegex(MOD.ContractError, "does not consume"):
            self.verify()

    def test_policy_path_cannot_escape_repository(self):
        outside = self.root.parent / "outside-policy.json"
        write_json(outside, Fixture.valid_policy())
        try:
            with self.assertRaisesRegex(MOD.ContractError, "escapes repository root"):
                MOD.verify(self.root, outside, SHA)
        finally:
            outside.unlink(missing_ok=True)

    def test_multiple_rollbacks_for_same_pack_version_fail(self):
        root = self.root / "cpf-tools/db/vendor/mariadb/rollback"
        write_sql(root / "U1__second.sql", "drop table other;\n")
        with self.assertRaisesRegex(MOD.ContractError, "multiple rollback artifacts"):
            self.verify()

    def test_comment_only_rollback_fails(self):
        target = self.root / "cpf-tools/db/vendor/mariadb/rollback/R1__baseline.sql"
        target.write_text("-- no recovery\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "no executable content"):
            self.verify()

    def test_migration_location_pattern_mismatch_fails(self):
        path, pack = self.fixture.vendor_pack("oracle")
        pack["migrationLocationPattern"] = "wrong"
        write_json(path, pack)
        with self.assertRaisesRegex(MOD.ContractError, "migrationLocationPattern mismatch"):
            self.verify()

    def test_runtime_module_owner_mismatch_fails(self):
        path, pack = self.fixture.vendor_pack("postgresql")
        pack["runtimeModules"]["cmn"]["ownerArtifact"] = "cpf-core"
        write_json(path, pack)
        with self.assertRaisesRegex(MOD.ContractError, "runtime module owner mismatch"):
            self.verify()

    def test_special_pack_is_checksum_locked(self):
        vendor_root = self.root / "cpf-tools/db/vendor/oracle"
        migration_pack = vendor_root / "migration/flyway/refDB"
        write_sql_with_checksum(migration_pack, "V93__operation.sql", "create table op(id int);\n")
        write_sql(vendor_root / "rollback/refDB/U93__operation.sql", "drop table op;\n")
        write_sql(vendor_root / "source/57_operation.sql")
        write_sql(vendor_root / "install/01_operation.sql")
        write_sql(vendor_root / "runtime/ref/operation.sql")
        write_sql(vendor_root / "verify/93_operation.sql")
        path, pack = self.fixture.vendor_pack("oracle")
        pack["operationLedger"] = {
            "canonicalSource": "source/57_operation.sql",
            "freshInstall": "install/01_operation.sql",
            "migration": "migration/flyway/refDB/V93__operation.sql",
            "rollback": "rollback/refDB/U93__operation.sql",
            "runtimeQueries": "runtime/ref/operation.sql",
            "verify": "verify/93_operation.sql",
            "checksumManifest": "migration/flyway/refDB/checksums.sha256",
        }
        write_json(path, pack)
        report = self.verify()
        self.assertEqual(4, report["migrationCount"])
        migration = vendor_root / "migration/flyway/refDB/V93__operation.sql"
        migration.write_text("select 9;\n", encoding="utf-8")
        with self.assertRaisesRegex(MOD.ContractError, "checksum mismatch"):
            self.verify()

    def test_special_pack_missing_consumer_fails(self):
        vendor_root = self.root / "cpf-tools/db/vendor/oracle"
        migration_pack = vendor_root / "migration/flyway/refDB"
        write_sql_with_checksum(migration_pack, "V93__operation.sql")
        for relative in (
            "rollback/refDB/U93__operation.sql",
            "source/57_operation.sql",
            "install/01_operation.sql",
            "verify/93_operation.sql",
        ):
            write_sql(vendor_root / relative)
        path, pack = self.fixture.vendor_pack("oracle")
        pack["operationLedger"] = {
            "canonicalSource": "source/57_operation.sql",
            "freshInstall": "install/01_operation.sql",
            "migration": "migration/flyway/refDB/V93__operation.sql",
            "rollback": "rollback/refDB/U93__operation.sql",
            "runtimeQueries": "runtime/ref/missing.sql",
            "verify": "verify/93_operation.sql",
            "checksumManifest": "migration/flyway/refDB/checksums.sha256",
        }
        write_json(path, pack)
        with self.assertRaisesRegex(MOD.ContractError, "runtimeQueries missing or empty"):
            self.verify()

    def test_cli_success_writes_sanitized_report(self):
        report = self.root / "report.json"
        completed = subprocess.run(
            [
                sys.executable,
                str(MODULE_PATH),
                "--root",
                str(self.root),
                "--policy",
                "cpf-tools/db/migration-lifecycle-policy.json",
                "--source-sha",
                SHA,
                "--report",
                str(report),
            ],
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
        )
        self.assertEqual(0, completed.returncode, completed.stderr)
        payload = json.loads(report.read_text(encoding="utf-8"))
        self.assertEqual("PASS", payload["result"])
        self.assertTrue(payload["sanitized"])
        self.assertIn("[CPF][DB][PASS]", completed.stderr)

    def test_cli_contract_failure_uses_exit_two(self):
        report = self.root / "failure.json"
        (self.root / "cpf-tools/db/vendor/mariadb/pack.json").unlink()
        completed = subprocess.run(
            [
                sys.executable,
                str(MODULE_PATH),
                "--root",
                str(self.root),
                "--source-sha",
                SHA,
                "--report",
                str(report),
            ],
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
        )
        self.assertEqual(2, completed.returncode)
        payload = json.loads(report.read_text(encoding="utf-8"))
        self.assertEqual("FAIL", payload["result"])
        self.assertEqual(2, payload["exitCode"])

    def test_invalid_sha_fails_closed(self):
        with self.assertRaisesRegex(MOD.ContractError, "exact 40-character"):
            MOD.verify(self.root, self.fixture.policy, "short")



class RepositoryIntegrationContractTest(unittest.TestCase):
    def test_existing_gradle_db_readiness_consumer_invokes_lifecycle_verifier(self):
        repo_root = Path(__file__).resolve().parents[3]
        gate = repo_root / "cpf-tools/scripts/check-official-db-vendor-readiness.ps1"
        body = gate.read_text(encoding="utf-8-sig")
        for token in (
            "cpf-tools/db/verify_migration_lifecycle.py",
            "cpf-tools/scripts/verify-cpf-db-vendor-manifest.py",
            "cpf-tools/scripts/verify-cpf-db-lifecycle-contract.py",
            "cpf-tools/scripts/verify-cpf-db-development-contract.py",
            "cpf-tools/scripts/verify-cpf-db-schema-governance.py",
            "cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py",
            "--source-sha",
            "--report",
            "$LASTEXITCODE -ne 0",
            "build/reports/cpf-db",
        ):
            self.assertIn(token, body)
        self.assertIn("git -C $Root rev-parse HEAD", body)

    def test_runtime_matrix_discovers_versions_and_requires_restore_pitr_evidence(self):
        repo_root = Path(__file__).resolve().parents[3]
        matrix = (repo_root / "cpf-tools/scripts/invoke-cpf-qa34-db-runtime-matrix.ps1").read_text(encoding="utf-8-sig")
        for token in (
            "verify_migration_lifecycle.py",
            "$UpgradeBaselineVersion",
            "$versionsByVendor",
            "$rollbackVersionsByVendor",
            "$forwardRecoveryVersionsByVendor",
            "$BackupRestoreEvidencePath",
            "$PitrEvidencePath",
            "sourceSha -ne $sha",
            "approvalReference",
            "PITR evidence must be an executed PITR result",
        ):
            self.assertIn(token, matrix)
        self.assertNotIn("$versions=@(83,86,87,88,89,90,91)", matrix.replace(" ", ""))

    def test_pitr_result_is_exact_sha_bound(self):
        repo_root = Path(__file__).resolve().parents[3]
        script = (repo_root / "cpf-tools/scripts/invoke-cpf-pitr-restore.ps1").read_text(encoding="utf-8-sig")
        self.assertIn("sourceSha=(Get-CpfGitHeadOrUnknown $rootPath)", script)
        self.assertIn("schemaVersion=2", script)

    def test_vendor_lifecycle_does_not_echo_or_persist_credentials(self):
        repo_root = Path(__file__).resolve().parents[3]
        script = (repo_root / "cpf-tools/scripts/run-db-vendor-lifecycle.ps1").read_text(encoding="utf-8-sig")
        self.assertIn("set echo off verify off define off", script)
        self.assertIn("Remove-Item Env:\\PGPASSWORD", script)
        self.assertIn("Remove-Item Env:\\MYSQL_PWD", script)
        self.assertIn("$clientExitCode=$LASTEXITCODE", script)
        self.assertNotIn("set echo on feedback on serveroutput on","connect", script.replace(" ", ""))



class RuntimeEvidenceHardeningTest(unittest.TestCase):
    @staticmethod
    def load_script_module(name: str):
        repo_root = Path(__file__).resolve().parents[3]
        path = repo_root / "cpf-tools/scripts" / name
        spec = importlib.util.spec_from_file_location(name.replace("-", "_").replace(".py", ""), path)
        assert spec and spec.loader
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        return module

    def test_performance_evidence_requires_exact_source_result_identity(self):
        module = self.load_script_module("verify-cpf-db-performance-evidence.py")
        policy = {
            "officialVendors": ["mariadb"],
            "requiredEvidenceFields": [],
            "representativeDataScales": {"SMALL": {}},
            "statisticsPolicy": {"evidenceFields": []},
            "queryClasses": {},
            "resultStates": ["PASS", "FAIL", "UNKNOWN"],
        }
        evidence = {
            "vendor": "mariadb",
            "sourceSha": "a" * 40,
            "resultSha": "b" * 40,
            "databaseVersion": "11.4",
            "startedAt": "2026-08-05T00:00:00Z",
            "endedAt": "2026-08-05T00:00:01Z",
            "sanitized": True,
            "dataScale": "SMALL",
            "statistics": {"statisticsExitCode": 0},
            "queryResults": [],
        }
        result = module.evaluate(policy, evidence)
        self.assertIn("resultSha must equal sourceSha", result["reasons"])

    def test_performance_plan_hash_must_be_exact_sha256(self):
        module = self.load_script_module("verify-cpf-db-performance-evidence.py")
        policy = {
            "officialVendors": ["mariadb"],
            "requiredEvidenceFields": [],
            "representativeDataScales": {"SMALL": {}},
            "statisticsPolicy": {"evidenceFields": []},
            "queryClasses": {"LOOKUP": {}},
            "resultStates": ["PASS", "FAIL", "UNKNOWN"],
        }
        evidence = {
            "vendor": "mariadb",
            "sourceSha": "a" * 40,
            "resultSha": "a" * 40,
            "databaseVersion": "11.4",
            "startedAt": "2026-08-05T00:00:00Z",
            "endedAt": "2026-08-05T00:00:01Z",
            "sanitized": True,
            "dataScale": "SMALL",
            "statistics": {"statisticsExitCode": 0},
            "queryResults": [{
                "queryId": "Q1", "queryClass": "LOOKUP", "status": "PASS",
                "latencyMs": 1, "examinedRows": 1, "planSha256": "not-a-hash"
            }],
        }
        result = module.evaluate(policy, evidence)
        self.assertTrue(any("planSha256 must be exact" in reason for reason in result["reasons"]))

    def test_runtime_approval_wrappers_bind_clean_head_and_approver(self):
        repo_root = Path(__file__).resolve().parents[3]
        for name in (
            "invoke-cpf-db-performance-gate.ps1",
            "invoke-cpf-datasource-runtime-gate.ps1",
            "invoke-cpf-data-observability-gate.ps1",
        ):
            body = (repo_root / "cpf-tools/scripts" / name).read_text(encoding="utf-8-sig")
            for token in (
                "git -C $root rev-parse HEAD",
                "status --porcelain=v1 --untracked-files=all",
                "Evidence sourceSha mismatch",
                "approvedBy",
                "approvalTimestamp",
                "inputEvidenceSha256",
                "[Text.UTF8Encoding]::new($false)",
            ):
                self.assertIn(token, body, name)

if __name__ == "__main__":
    unittest.main()
