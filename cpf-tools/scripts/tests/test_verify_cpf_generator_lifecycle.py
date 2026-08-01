from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-generator-lifecycle.py"
spec = importlib.util.spec_from_file_location("generator_lifecycle", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class GeneratorLifecycleTest(unittest.TestCase):
    def fixture(self) -> tuple[Path, dict, tempfile.TemporaryDirectory]:
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        paths = {
            "create": "cpf-tools/scripts/create-domain.ps1",
            "database": "cpf-tools/scripts/initialize-domain-database.ps1",
            "remove": "cpf-tools/scripts/remove-domain.ps1",
            "lifecycle": "cpf-tools/generator/verify-domain-lifecycle.ps1",
        }
        contents = {
            "create": "generator-ownership.json createdFiles sha256 Generated file already exists databaseRemovalPolicy",
            "database": 'ValidateSet("bootstrap", "migration", "verify", "rollback") ConfirmRollback database-profile.json Assert-CpfSupportedDatabaseVendor',
            "remove": "changedGeneratedFiles userOwnedFiles externalReferences blockReasons databaseObjectsRemoved = $false DryRun",
            "lifecycle": ".cpf-disposable-generator-validation-root " + " ".join(module.STAGES) + " generator-lifecycle-result.sanitized.json normalizedSha256 changedGeneratedFiles userOwnedFiles externalReferences",
        }
        for name, rel in paths.items():
            path = root / rel
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents[name], encoding="utf-8")
        contract = {
            "schemaVersion": 1,
            "supportedVendors": list(module.VENDORS),
            "disposableRootMarker": ".cpf-disposable-generator-validation-root",
            "stages": list(module.STAGES),
            "requiredScripts": paths,
            "requiredGeneratedFiles": ["manifest/domain-manifest.json", "manifest/generator-ownership.json", "deploy/database/database-profile.json", "build.gradle"],
            "userProtection": {
                "changedGeneratedFileBlocksRemoval": True,
                "userOwnedFileBlocksRemoval": True,
                "externalReferenceBlocksRemoval": True,
                "databaseObjectsNeverAutoDropped": True,
            },
            "parity": {"requireIdenticalFileSet": True, "requireIdenticalNormalizedSha256": True},
            "releaseEvidence": {"requiredStatus": "PASS", "filePattern": "{vendor}/generator-lifecycle-result.sanitized.json"},
        }
        return root, contract, temp

    def test_valid_static_contract(self):
        root, contract, temp = self.fixture()
        self.addCleanup(temp.cleanup)
        module.validate_contract(root, contract)

    def test_wrong_stage_order_rejected(self):
        root, contract, temp = self.fixture()
        self.addCleanup(temp.cleanup)
        contract["stages"] = list(reversed(module.STAGES))
        with self.assertRaises(module.ContractError):
            module.validate_contract(root, contract)

    def test_unsupported_vendor_rejected(self):
        root, contract, temp = self.fixture()
        self.addCleanup(temp.cleanup)
        contract["supportedVendors"].append("h2")
        with self.assertRaises(module.ContractError):
            module.validate_contract(root, contract)

    def test_missing_user_protection_token_rejected(self):
        root, contract, temp = self.fixture()
        self.addCleanup(temp.cleanup)
        remove = root / contract["requiredScripts"]["remove"]
        remove.write_text("changedGeneratedFiles userOwnedFiles blockReasons databaseObjectsRemoved = $false DryRun", encoding="utf-8")
        with self.assertRaises(module.ContractError):
            module.validate_contract(root, contract)

    def test_release_evidence_requires_exact_sha_and_artifact(self):
        root, contract, temp = self.fixture()
        self.addCleanup(temp.cleanup)
        evidence_root = root / "external-evidence"
        sha = "a" * 40
        for vendor in module.VENDORS:
            artifact = root / f"{vendor}.txt"
            artifact.write_text(vendor, encoding="utf-8")
            import hashlib
            digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
            path = evidence_root / vendor / "generator-lifecycle-result.sanitized.json"
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps({
                "status": "PASS", "vendor": vendor, "sourceSha": sha, "resultSha": sha,
                "sanitized": True, "cleanBefore": True, "cleanAfter": True,
                "userProtectionVerified": True, "parityVerified": True,
                "stages": [{"id": stage, "status": "PASS", "exitCode": 0, "assertions": ["ok"]} for stage in module.STAGES],
                "artifacts": [{"path": artifact.name, "sha256": digest}],
            }), encoding="utf-8")
        module.validate_release_evidence(root, contract, sha, evidence_root)
        bad = evidence_root / "oracle/generator-lifecycle-result.sanitized.json"
        data = json.loads(bad.read_text(encoding="utf-8"))
        data["cleanAfter"] = False
        bad.write_text(json.dumps(data), encoding="utf-8")
        with self.assertRaises(module.ContractError):
            module.validate_release_evidence(root, contract, sha, evidence_root)


if __name__ == "__main__":
    unittest.main()
