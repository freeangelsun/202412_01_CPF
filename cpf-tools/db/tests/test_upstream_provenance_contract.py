"""v2 upstream provenance 계약과 그 게이트의 negative mutation 검증.

`platform-non-table-objects.json` v1 -> v2 전환에서 `upstreamReference`(Spring Batch 공식
artifact/version/SHA)와 `migration`(released V73 좌표)이 함께 제거되어, 공식 artifact provenance
검증과 released migration 내용 검증이 입력을 잃고 게이트가 fail-closed 로 멈춰 있었다. v2 구조에
맞는 `upstreamProvenance` / `historicalMigration` 계약으로 현행화했고, 이 테스트는 그 검증이
실제로 살아 있는지를 mutation 으로 확인한다.

게이트는 저장소 경로를 직접 읽으므로, 각 mutation 은 계약 파일 원본 바이트를 보관했다가
finally 에서 복원한다.
"""
from __future__ import annotations

import json
import shutil
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONTRACT = ROOT / "cpf-tools/db/canonical/platform-non-table-objects.json"
GATE = ROOT / "cpf-tools/verification/tools/check-spring-batch-sequence-contract.ps1"
PWSH = shutil.which("pwsh") or shutil.which("powershell")


def run_gate(require_jar: bool = False) -> subprocess.CompletedProcess:
    args = [PWSH, "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", str(GATE), "-Root", str(ROOT)]
    if require_jar:
        args.append("-RequireOfficialJar")
    return subprocess.run(args, capture_output=True, text=True, encoding="utf-8", errors="replace")


def load_contract() -> dict:
    return json.loads(CONTRACT.read_text(encoding="utf-8-sig"))


def batch_artifact(contract: dict) -> dict:
    return next(a for a in contract["upstreamProvenance"]["artifacts"] if a["id"] == "spring-batch-core")


@unittest.skipIf(PWSH is None, "PowerShell is unavailable")
class UpstreamProvenanceContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self._original = CONTRACT.read_bytes()

    def tearDown(self) -> None:
        CONTRACT.write_bytes(self._original)

    def _mutate(self, mutator) -> subprocess.CompletedProcess:
        contract = load_contract()
        mutator(contract)
        CONTRACT.write_text(json.dumps(contract, ensure_ascii=False, indent=2) + "\n",
                            encoding="utf-8", newline="\n")
        return run_gate()

    # --- 계약 자체의 형태 -------------------------------------------------
    def test_contract_declares_upstream_and_historical_provenance(self):
        contract = load_contract()
        self.assertEqual(2, int(contract["schemaVersion"]))
        provenance = contract["upstreamProvenance"]
        self.assertEqual("CPF_UPSTREAM_ARTIFACT_PROVENANCE", provenance["contract"])
        artifact = batch_artifact(contract)
        self.assertEqual("org.springframework.batch", artifact["group"])
        self.assertEqual(
            {"mariadb", "postgresql", "oracle"},
            {r["vendor"] for r in artifact["resources"]})
        for resource in artifact["resources"]:
            self.assertRegex(resource["sha256"], r"^[0-9a-f]{64}$")
        historical = contract["historicalMigration"]
        self.assertEqual("CPF_PLATFORM_NON_TABLE_HISTORICAL_MIGRATION", historical["contract"])
        self.assertTrue(historical["policy"]["byteImmutable"])
        self.assertFalse(historical["policy"]["regeneratedByRenderer"])

    def test_referenced_historical_artifacts_exist(self):
        for release in load_contract()["historicalMigration"]["released"]:
            for vendor, artifacts in release["artifacts"].items():
                for role, relative in artifacts.items():
                    self.assertTrue((ROOT / relative).is_file(), f"{vendor}.{role}={relative}")

    def test_contract_version_matches_canonical_stack(self):
        version = batch_artifact(load_contract())["version"]
        stack = (ROOT / "gradle/cpf-stack.properties").read_text(encoding="utf-8-sig")
        declared = [line.split("=", 1)[1].strip()
                    for line in stack.splitlines() if line.strip().startswith("springBatchVersion")]
        self.assertEqual([version], declared)

    # --- positive ---------------------------------------------------------
    def test_gate_passes_with_official_artifact_verified(self):
        result = run_gate(require_jar=True)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn('"officialJarVerified": true', result.stdout)
        self.assertIn("CPF_UPSTREAM_ARTIFACT_PROVENANCE", result.stdout)

    # --- negative mutation ------------------------------------------------
    def test_gate_fails_when_upstream_resource_sha_is_tampered(self):
        def mutate(contract):
            batch_artifact(contract)["resources"][0]["sha256"] = "0" * 64
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_artifact_version_diverges_from_stack(self):
        def mutate(contract):
            batch_artifact(contract)["version"] = "6.0.3"
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_declared_sequences_drift(self):
        def mutate(contract):
            batch_artifact(contract)["declaresSequences"] = ["BATCH_JOB_INSTANCE_SEQ"]
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_a_vendor_resource_is_dropped(self):
        def mutate(contract):
            artifact = batch_artifact(contract)
            artifact["resources"] = [r for r in artifact["resources"] if r["vendor"] != "oracle"]
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_provenance_contract_header_is_wrong(self):
        def mutate(contract):
            contract["upstreamProvenance"]["contract"] = "SOMETHING_ELSE"
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_historical_migration_coordinate_is_wrong(self):
        def mutate(contract):
            release = contract["historicalMigration"]["released"][0]
            release["artifacts"]["mariadb"]["migration"] = "cpf-tools/db/vendor/mariadb/migration/flyway/V999__missing.sql"
        self.assertNotEqual(0, self._mutate(mutate).returncode)

    def test_gate_fails_when_historical_release_is_removed(self):
        def mutate(contract):
            contract["historicalMigration"]["released"] = []
        self.assertNotEqual(0, self._mutate(mutate).returncode)


if __name__ == "__main__":
    unittest.main()
