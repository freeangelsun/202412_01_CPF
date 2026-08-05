from __future__ import annotations

import base64
import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools" / "scripts" / "cpf-backup-crypto.py"


class CpfBackupCryptoTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.key_env = "CPF_TEST_BACKUP_KEY"
        os.environ[self.key_env] = base64.b64encode(bytes(range(32))).decode("ascii")

    def tearDown(self):
        os.environ.pop(self.key_env, None)
        self.temp.cleanup()

    def run_crypto(self, *args: str, expected: int = 0):
        proc = subprocess.run(
            [sys.executable, str(SCRIPT), *args],
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            env=os.environ.copy(),
            check=False,
        )
        self.assertEqual(expected, proc.returncode, msg=f"stdout={proc.stdout}\nstderr={proc.stderr}")
        return proc

    def test_round_trip_and_no_plaintext_in_ciphertext(self):
        source = self.root / "source.sql"
        source.write_bytes((b"SECRET-DATABASE-CONTENT\n" * 8000) + os.urandom(131071))
        encrypted = self.root / "backup.cpfbak"
        restored = self.root / "restored.sql"
        enc = self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env)
        metadata = json.loads(enc.stdout)
        self.assertEqual("AES-256-GCM-CHUNKED", metadata["algorithm"])
        self.assertNotIn(b"SECRET-DATABASE-CONTENT", encrypted.read_bytes())
        self.run_crypto("decrypt", "--input", str(encrypted), "--output", str(restored), "--key-env", self.key_env)
        self.assertEqual(source.read_bytes(), restored.read_bytes())

    def test_tamper_is_rejected_without_output_artifact(self):
        source = self.root / "source.bin"
        source.write_bytes(os.urandom(200000))
        encrypted = self.root / "backup.cpfbak"
        output = self.root / "output.bin"
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env)
        damaged = bytearray(encrypted.read_bytes())
        damaged[-25] ^= 0x80
        encrypted.write_bytes(damaged)
        self.run_crypto("decrypt", "--input", str(encrypted), "--output", str(output), "--key-env", self.key_env, expected=2)
        self.assertFalse(output.exists())
        self.assertFalse(any(self.root.glob("output.bin.tmp-*")))

    def test_wrong_key_is_rejected(self):
        source = self.root / "source.bin"
        source.write_bytes(os.urandom(100000))
        encrypted = self.root / "backup.cpfbak"
        output = self.root / "output.bin"
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env)
        os.environ[self.key_env] = base64.b64encode(os.urandom(32)).decode("ascii")
        self.run_crypto("decrypt", "--input", str(encrypted), "--output", str(output), "--key-env", self.key_env, expected=2)
        self.assertFalse(output.exists())

    def test_missing_or_invalid_key_fails_closed(self):
        source = self.root / "source.bin"
        source.write_bytes(b"x")
        encrypted = self.root / "backup.cpfbak"
        del os.environ[self.key_env]
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env, expected=2)
        self.assertFalse(encrypted.exists())
        os.environ[self.key_env] = base64.b64encode(b"short").decode("ascii")
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env, expected=2)
        self.assertFalse(encrypted.exists())

    def test_existing_output_is_not_overwritten_without_explicit_flag(self):
        source = self.root / "source.bin"
        source.write_bytes(b"source")
        encrypted = self.root / "backup.cpfbak"
        encrypted.write_bytes(b"existing")
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env, expected=2)
        self.assertEqual(b"existing", encrypted.read_bytes())

    def test_posix_artifacts_are_owner_only(self):
        if os.name != "posix":
            self.skipTest("POSIX file mode assertion")
        source = self.root / "source.bin"
        source.write_bytes(os.urandom(70000))
        encrypted = self.root / "backup.cpfbak"
        restored = self.root / "restored.bin"
        self.run_crypto("encrypt", "--input", str(source), "--output", str(encrypted), "--key-env", self.key_env)
        self.run_crypto("decrypt", "--input", str(encrypted), "--output", str(restored), "--key-env", self.key_env)
        self.assertEqual(0o600, encrypted.stat().st_mode & 0o777)
        self.assertEqual(0o600, restored.stat().st_mode & 0o777)


if __name__ == "__main__":
    unittest.main()
