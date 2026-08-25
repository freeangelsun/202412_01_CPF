from __future__ import annotations

import json
import re
import subprocess
import shutil
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/db/tools/run-db-vendor-lifecycle.ps1"
ADAPTER = ROOT / "cpf-tools/db/tools/invoke-db-lifecycle-docker-client.ps1"
TEXT = SCRIPT.read_text(encoding="utf-8-sig")


class VendorLifecycleOrchestratorTest(unittest.TestCase):
    def test_delegates_to_canonical_install_and_migration_consumers(self):
        self.assertIn("initialize-cpf-database.ps1", TEXT)
        self.assertIn("invoke-platform-database-migration.ps1", TEXT)
        self.assertIn("vendor-pack-manifest.json", TEXT)
        self.assertIn("migration-intent-catalog.json", TEXT)
        self.assertIn("Get-CpfMigrationCatalog", TEXT)

    def test_no_direct_secret_or_sqlplus_transport(self):
        for token in (
            "CPF_ORACLE_PASSWORD",
            "connect $user/",
            "set echo on",
            "Tee-Object -FilePath $log",
            "Run-Sql",
        ):
            self.assertNotIn(token, TEXT)
        self.assertNotRegex(TEXT, re.compile(r"(?i)password\s*=\s*Need"))

    def test_plan_apply_approval_and_unknown_boundaries(self):
        for token in (
            "ConfirmExecute",
            "ApprovalReference",
            "ExpectedPlanSha256",
            "ExpectedRollbackPlanSha256",
            "ExpectedLifecyclePlanSha256",
            "lifecyclePlanSha256",
            "profileSha256",
            "backupManifests",
            "if($executionStarted){'UNKNOWN'}else{'FAILED'}",
            "reconcileRequired=$executionStarted",
            "ConfirmPreCurrentFixture",
            "ConfirmCurrentMigrationApplied",
        ):
            self.assertIn(token, TEXT)

    def test_execution_requires_reviewed_lifecycle_plan_hash(self):
        self.assertIn("ExpectedLifecyclePlanSha256 -notmatch '^[0-9a-fA-F]{64}$'", TEXT)
        self.assertIn("Write-CpfJsonAtomic $lifecyclePlan $planAbsolute", TEXT)
        self.assertIn("Get-FileHash -LiteralPath $planAbsolute -Algorithm SHA256", TEXT)

    def test_versions_are_not_hardcoded(self):
        for version in ("V98", "V99", "V100"):
            self.assertNotIn(version, TEXT)
        for token in ("MigrationVersion", "FromVersion", "ToVersion", "checksums.sha256"):
            self.assertIn(token, TEXT)
        self.assertIn("Migrations below the official root are not declared by a checksum manifest", TEXT)

    def test_docker_adapter_is_noninteractive_and_wrapper_only(self):
        adapter = ADAPTER.read_text(encoding="utf-8-sig")
        self.assertIn("initialize-cpf-database.ps1", adapter)
        self.assertIn("invoke-platform-database-migration.ps1", adapter)
        self.assertNotIn("sqlplus", adapter.lower())
        self.assertNotIn("mariadb --", adapter.lower())
        self.assertNotIn("psql ", adapter.lower())
        self.assertIn("--network none", TEXT)
        self.assertIn("target=/workspace/cpf,readonly", TEXT)
        self.assertNotIn("--privileged", TEXT)
        self.assertNotIn("/var/run/docker.sock", TEXT)


@unittest.skipUnless(shutil.which("pwsh"), "PowerShell runtime required")
class VendorLifecyclePlanTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp = tempfile.TemporaryDirectory()
        cls.work = Path(cls.temp.name)
        base = json.loads(
            (ROOT / "cpf-tools/db/config/database-install.default.json").read_text(encoding="utf-8")
        )
        cls.profiles: dict[str, Path] = {}
        for vendor, port in (("mariadb", 3306), ("postgresql", 5432), ("oracle", 1521)):
            profile = json.loads(json.dumps(base))
            profile["profileName"] = f"d025-{vendor}-static"
            for key, module in profile["modules"].items():
                module["enabled"] = key == "core"
            core = profile["modules"]["core"]
            core["vendor"] = vendor
            core["port"] = port
            core["clientPath"] = ""
            path = cls.work / f"{vendor}-profile.json"
            path.write_text(json.dumps(profile, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            cls.profiles[vendor] = path

    @classmethod
    def tearDownClass(cls):
        cls.temp.cleanup()

    def run_plan(self, vendor: str, mode: str, suffix: str = "") -> tuple[dict, dict, str]:
        stem = f"{vendor}-{mode.lower()}{suffix}"
        plan_path = self.work / f"{stem}-plan.json"
        result_path = self.work / f"{stem}-result.json"
        command = [
            "pwsh",
            "-NoProfile",
            "-File",
            str(SCRIPT),
            "-Vendor",
            vendor,
            "-Mode",
            mode,
            "-Root",
            str(ROOT),
            "-ProfilePath",
            str(self.profiles[vendor]),
            "-ClientAdapter",
            "Static",
            "-LogDir",
            str(self.work),
            "-LifecyclePlanPath",
            str(plan_path),
            "-ResultPath",
            str(result_path),
        ]
        completed = subprocess.run(
            command,
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            check=False,
        )
        self.assertEqual(0, completed.returncode, completed.stdout + completed.stderr)
        return (
            json.loads(plan_path.read_text(encoding="utf-8-sig")),
            json.loads(result_path.read_text(encoding="utf-8-sig")),
            completed.stdout + completed.stderr,
        )

    def test_fresh_install_plan_uses_official_four_stage_order_for_vendor3(self):
        for vendor in self.profiles:
            with self.subTest(vendor=vendor):
                plan, result, output = self.run_plan(vendor, "FreshInstall")
                self.assertEqual(["Provision", "EmptyInstall", "ProductSeed", "Verify"], [x["stage"] for x in plan["stages"]])
                self.assertTrue(all(x["consumer"].endswith("initialize-cpf-database.ps1") for x in plan["stages"]))
                self.assertEqual("PLANNED", result["status"])
                self.assertFalse(result["executionStarted"])
                self.assertIn("No database was changed", output)

    def test_current_edge_upgrade_and_rollback_reapply_are_checksum_discovered(self):
        for vendor in self.profiles:
            for mode, stages in (("Upgrade", ["Upgrade"]), ("RollbackReapply", ["Rollback", "Reapply"])):
                with self.subTest(vendor=vendor, mode=mode):
                    plan, result, _ = self.run_plan(vendor, mode)
                    discovery = plan["discovery"]
                    # core-only 범위(cpfDB)의 currentVersion은 저장소에 cpfDB Migration이 계속 추가되며
                    # 자연히 전진하므로 정확한 숫자를 하드코딩하지 않는다(과거 118 고정값이 그래서 stale
                    # 해졌다). 대신 실제로 지켜야 하는 핵심 불변 계약만 검증한다: core-only 범위는
                    # mbwDB 전용 Migration(133/136/139 등 다른 logical DB 전용)을 절대 선택하지 않는다.
                    self.assertGreaterEqual(discovery["currentVersion"], 138)
                    self.assertEqual([discovery["currentVersion"]], discovery["selectedVersions"])
                    mbwOnlyVersions = {133, 136, 139}
                    self.assertNotIn(discovery["currentVersion"], mbwOnlyVersions)
                    self.assertTrue(all(x["version"] not in mbwOnlyVersions for x in discovery["selectedMigrations"]))
                    currentVersion = discovery["currentVersion"]
                    self.assertTrue(any(f'V{currentVersion}__' in x["path"] for x in discovery["selectedMigrations"]))
                    self.assertTrue(all(str(currentVersion) in x["rollbackPath"] for x in discovery["selectedMigrations"]))
                    self.assertEqual(stages, [x["stage"] for x in plan["stages"]])
                    self.assertTrue(all(re.fullmatch(r"[0-9a-f]{64}", x["planSha256"]) for x in plan["stages"]))
                    self.assertFalse(plan["discovery"]["fullHistoricalLifecycleEvidence"])
                    self.assertEqual("PLANNED", result["status"])

    def test_lifecycle_plan_hash_is_deterministic_and_tamper_fails_before_execution(self):
        first, first_result, _ = self.run_plan("mariadb", "Upgrade", "-deterministic-a")
        second, second_result, _ = self.run_plan("mariadb", "Upgrade", "-deterministic-b")
        self.assertEqual(first, second)
        self.assertEqual(first_result["lifecyclePlanSha256"], second_result["lifecyclePlanSha256"])

        result_path = self.work / "tamper-result.json"
        completed = subprocess.run(
            [
                "pwsh", "-NoProfile", "-File", str(SCRIPT),
                "-Vendor", "mariadb", "-Mode", "Upgrade", "-Root", str(ROOT),
                "-ProfilePath", str(self.profiles["mariadb"]), "-ClientAdapter", "Static",
                "-LogDir", str(self.work), "-ResultPath", str(result_path),
                "-ConfirmExecute", "-ExpectedLifecyclePlanSha256", "0" * 64,
            ],
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            check=False,
        )
        self.assertNotEqual(0, completed.returncode)
        result = json.loads(result_path.read_text(encoding="utf-8-sig"))
        self.assertEqual("FAILED", result["status"])
        self.assertFalse(result["executionStarted"])
        self.assertFalse(result["reconcileRequired"])


if __name__ == "__main__":
    unittest.main()
