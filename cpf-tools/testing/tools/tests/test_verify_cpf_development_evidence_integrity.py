import csv
import hashlib
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

MOD = Path(__file__).parents[3] / "verification" / "tools" / "verify-cpf-development-evidence-integrity.py"
spec = importlib.util.spec_from_file_location("evidence_gate", MOD)
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)

BASELINE = "c" * 64
META = {
    "review/PACKAGE_MANIFEST.json",
    "review/CHANGE_MANIFEST.csv",
    "review/SHA256SUMS.txt",
}
SOURCE_EXCLUDED = META | {
    "review/QA_FINDING_REVALIDATION.csv",
    "review/TEST_AND_EVIDENCE.md",
}


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def identity(entries):
    data = "".join(f"{sha}  {path}\n" for path, sha in sorted(entries)).encode()
    return hashlib.sha1(data).hexdigest(), hashlib.sha256(data).hexdigest()


class T(unittest.TestCase):
    def fixture(self, stale=False, missing=False):
        td = tempfile.TemporaryDirectory()
        root = Path(td.name)
        review = root / "review"
        evidence = root / "evidence"
        review.mkdir()
        evidence.mkdir()
        (evidence / "F1.log").write_text("PASS\n", encoding="utf-8")
        (root / "source.txt").write_text("source\n", encoding="utf-8")

        with (review / "REQUIREMENT_STATUS.csv").open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["requirement_id", "development_status", "verification_status"])
            writer.writeheader()
            writer.writerow({"requirement_id": "R1", "development_status": "완료", "verification_status": "완료"})
        (review / "TEST_AND_EVIDENCE.md").write_text("clean\n", encoding="utf-8")
        self.write_finding(review, evidence, "0" * 40, "x", missing=missing)
        self.refresh_metadata(root)

        if stale:
            path = review / "QA_FINDING_REVALIDATION.csv"
            rows = list(csv.DictReader(path.open(encoding="utf-8")))
            rows[0]["source_head"] = "b" * 40
            with path.open("w", newline="", encoding="utf-8") as f:
                writer = csv.DictWriter(f, fieldnames=rows[0].keys())
                writer.writeheader()
                writer.writerows(rows)
            self.refresh_package_only(root)
        return td, root

    def write_finding(self, review, evidence, source_head, result_identity, missing=False):
        fields = [
            "finding_id", "개발GPT_상태", "source_head", "result_identity",
            "positive_exit_code", "negative_exit_code", "regression_exit_code",
            "evidence_paths", "execution_command", "미완료사유",
        ]
        with (review / "QA_FINDING_REVALIDATION.csv").open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=fields)
            writer.writeheader()
            writer.writerow({
                "finding_id": "F1", "개발GPT_상태": "완료", "source_head": source_head,
                "result_identity": result_identity, "positive_exit_code": "0", "negative_exit_code": "0",
                "regression_exit_code": "0", "evidence_paths": "evidence/missing.log" if missing else "evidence/F1.log",
                "execution_command": "python gate.py --case F1", "미완료사유": "",
            })

    def compute_source_identity(self, root):
        entries = []
        for path in sorted(p for p in root.rglob("*") if p.is_file()):
            rel = path.relative_to(root).as_posix()
            if rel in SOURCE_EXCLUDED:
                continue
            entries.append((rel, digest(path)))
        return identity(entries), len(entries)

    def refresh_metadata(self, root):
        review = root / "review"
        # Write a temporary empty change file before computing source identity; it is excluded.
        self.write_change(review, root)
        (review / "SHA256SUMS.txt").write_text("", encoding="utf-8")
        (review / "PACKAGE_MANIFEST.json").write_text("{}", encoding="utf-8")

        (source_hashes, source_count) = self.compute_source_identity(root)
        path = review / "QA_FINDING_REVALIDATION.csv"
        rows = list(csv.DictReader(path.open(encoding="utf-8")))
        rows[0]["source_head"] = source_hashes[0]
        rows[0]["result_identity"] = f"CONTENT_SHA1_{source_hashes[0]};CONTENT_SHA256_{source_hashes[1]}"
        with path.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=rows[0].keys())
            writer.writeheader()
            writer.writerows(rows)
        self.refresh_package_only(root, source_hashes, source_count)

    def write_change(self, review, root):
        target = root / "evidence/F1.log"
        with (review / "CHANGE_MANIFEST.csv").open("w", newline="", encoding="utf-8") as f:
            fields = ["path", "change_type", "size_bytes", "sha256", "baseline_sha256"]
            writer = csv.DictWriter(f, fieldnames=fields)
            writer.writeheader()
            writer.writerow({
                "path": "evidence/F1.log", "change_type": "MODIFIED", "size_bytes": target.stat().st_size,
                "sha256": digest(target), "baseline_sha256": "d" * 64,
            })

    def refresh_package_only(self, root, source_hashes=None, source_count=None):
        review = root / "review"
        self.write_change(review, root)
        if source_hashes is None:
            source_hashes, source_count = self.compute_source_identity(root)
        payload_paths = sorted(
            p.relative_to(root).as_posix()
            for p in root.rglob("*") if p.is_file() and p.relative_to(root).as_posix() not in META
        )
        files = [
            {"path": rel, "sizeBytes": (root / rel).stat().st_size, "sha256": digest(root / rel)}
            for rel in payload_paths
        ]
        payload_hashes = identity([(item["path"], item["sha256"]) for item in files])
        payload_bytes = sum(item["sizeBytes"] for item in files)
        manifest = {
            "schemaVersion": 6,
            "baselineSourceZipSha256": BASELINE,
            "gitExactSha": "UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT",
            "sourceIdentity": {
                "sha1": source_hashes[0], "sha256": source_hashes[1], "fileCount": source_count,
                "excludedPaths": sorted(SOURCE_EXCLUDED),
            },
            "packageMetadataExcludedPaths": sorted(META),
            "packagePayloadIdentity": {
                "sha1": payload_hashes[0], "sha256": payload_hashes[1],
                "fileCount": len(files), "totalBytes": payload_bytes,
            },
            "desiredState": {"fullFileCount": len(files) + len(META)},
            "files": files,
        }
        manifest_path = review / "PACKAGE_MANIFEST.json"
        manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
        sha_paths = payload_paths + ["review/PACKAGE_MANIFEST.json", "review/CHANGE_MANIFEST.csv"]
        (review / "SHA256SUMS.txt").write_text(
            "".join(f"{digest(root / rel)}  {rel}\n" for rel in sorted(set(sha_paths)))
        )

    def test_positive(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        result = m.verify(root, Path("review"), None, None, 1, 1)
        self.assertEqual(result["status"], "PASS")

    def test_runtime_identity_is_optional_and_non_substitutive(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        runtime = "d" * 40
        result = m.verify(root, Path("review"), runtime, runtime, 1, 1)
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["runtimeSourceSha"], runtime)

    def test_missing_evidence_fails(self):
        td, root = self.fixture(missing=True)
        self.addCleanup(td.cleanup)
        with self.assertRaises(m.GateError):
            m.verify(root, Path("review"), None, None, 1, 1)

    def test_stale_finding_identity_fails(self):
        td, root = self.fixture(stale=True)
        self.addCleanup(td.cleanup)
        with self.assertRaises(m.GateError) as ctx:
            m.verify(root, Path("review"), None, None, 1, 1)
        self.assertIn("stale source identity", str(ctx.exception))

    def test_corrupted_sha256sums_fails_closed(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        path = root / "review/SHA256SUMS.txt"
        lines = path.read_text(encoding="utf-8").splitlines()
        lines[0] = "0" * 64 + lines[0][64:]
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        with self.assertRaises(m.GateError) as ctx:
            m.verify(root, Path("review"), None, None, 1, 1)
        self.assertIn("SHA256SUMS hash mismatch", str(ctx.exception))

    def test_stale_change_manifest_hash_fails_closed(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        path = root / "review/CHANGE_MANIFEST.csv"
        rows = list(csv.DictReader(path.open(encoding="utf-8")))
        rows[0]["sha256"] = "0" * 64
        with path.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=rows[0].keys())
            writer.writeheader()
            writer.writerows(rows)
        # Update package + SHA list so the verifier reaches the change-manifest semantic check.
        self.refresh_package_only(root)
        rows = list(csv.DictReader(path.open(encoding="utf-8")))
        rows[0]["sha256"] = "0" * 64
        with path.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=rows[0].keys())
            writer.writeheader()
            writer.writerows(rows)
        # SHA list must reflect the corrupted manifest file itself.
        manifest = json.loads((root / "review/PACKAGE_MANIFEST.json").read_text(encoding="utf-8"))
        payload = [item["path"] for item in manifest["files"]]
        paths = payload + ["review/PACKAGE_MANIFEST.json", "review/CHANGE_MANIFEST.csv"]
        (root / "review/SHA256SUMS.txt").write_text(
            "".join(f"{digest(root / rel)}  {rel}\n" for rel in sorted(set(paths)))
        )
        with self.assertRaises(m.GateError) as ctx:
            m.verify(root, Path("review"), None, None, 1, 1)
        self.assertIn("change manifest hash mismatch", str(ctx.exception))

    def test_unaccounted_payload_file_fails(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        (root / "rogue.txt").write_text("rogue", encoding="utf-8")
        with self.assertRaises(m.GateError) as ctx:
            m.verify(root, Path("review"), None, None, 1, 1)
        self.assertTrue("source identity mismatch" in str(ctx.exception) or "payload inventory mismatch" in str(ctx.exception))

    def test_invalid_git_provenance_fails(self):
        td, root = self.fixture()
        self.addCleanup(td.cleanup)
        path = root / "review/PACKAGE_MANIFEST.json"
        manifest = json.loads(path.read_text(encoding="utf-8"))
        manifest["gitExactSha"] = "fake-head"
        path.write_text(json.dumps(manifest), encoding="utf-8")
        # Update checksum only so semantic provenance validation is reached.
        lines = []
        for rel in [line.split(None, 1)[1] for line in (root / "review/SHA256SUMS.txt").read_text(encoding="utf-8").splitlines()]:
            lines.append(f"{digest(root / rel)}  {rel}")
        (root / "review/SHA256SUMS.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
        with self.assertRaises(m.GateError) as ctx:
            m.verify(root, Path("review"), None, None, 1, 1)
        self.assertIn("gitExactSha", str(ctx.exception))


if __name__ == "__main__":
    unittest.main()
