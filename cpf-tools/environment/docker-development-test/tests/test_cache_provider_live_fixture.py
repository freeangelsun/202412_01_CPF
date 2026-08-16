from __future__ import annotations

import importlib.util
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


FIXTURE_ROOT = Path(__file__).resolve().parents[1]
COMPOSE = FIXTURE_ROOT / "compose.cache-provider-live.yml"
PROBE = FIXTURE_ROOT / "probe-cache-provider-live.py"
RUNNER = FIXTURE_ROOT / "run-cache-provider-live.ps1"


def load_probe():
    spec = importlib.util.spec_from_file_location("cpf_cache_live_probe", PROBE)
    if spec is None or spec.loader is None:
        raise AssertionError("cannot load cache live probe")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class CacheProviderLiveFixtureStaticTest(unittest.TestCase):
    def test_compose_is_project_scoped_digest_pinned_and_secret_backed(self) -> None:
        text = COMPOSE.read_text(encoding="utf-8")
        self.assertIn(
            "valkey/valkey:9.1.1-alpine@sha256:ee91f7a174ac4d6a6b0685b3a60e321f0a9dbbb691f9b0e285be2ba1d1be8328",
            text,
        )
        self.assertIn(
            "redis:8.8.1-trixie@sha256:3eafabb4c93fcb8b36b666e07a43f096cb157bc6b07dce4b2492b895c63cf37f",
            text,
        )
        self.assertIn("published: ${CPF_CACHE_LIVE_REDIS_PORT:?", text)
        self.assertIn("published: ${CPF_CACHE_LIVE_VALKEY_PORT:?", text)
        self.assertEqual(2, text.count("host_ip: 127.0.0.1"))
        self.assertNotIn("container_name:", text)
        self.assertNotIn("name: cpf-", text)
        self.assertIn("cache_password:", text)
        self.assertIn("driver: bridge", text)
        self.assertNotIn("redis-password.txt", text)

    def test_runner_owns_interrupts_and_exact_cleanup_only(self) -> None:
        text = RUNNER.read_text(encoding="utf-8")
        for token in (
            'foreach ($provider in @("redis", "valkey"))',
            '-Arguments @("stop", "--timeout", "5", $descriptor.containerId)',
            '-Arguments @("start", $descriptor.containerId)',
            '"down", "--volumes", "--remove-orphans"',
            'label=com.docker.compose.project=$projectName',
            "Remove-Item -LiteralPath $passwordFile -Force",
            "provider-switch-redis-as-valkey.json",
            "provider-switch-valkey-as-redis.json",
            "redisValkeyEquivalenceClaimed = $false",
            "$logText.Contains($secretForLeakCheck, [StringComparison]::Ordinal)",
        ):
            self.assertIn(token, text)
        for forbidden in ("docker system prune", "docker volume prune", "docker network prune"):
            self.assertNotIn(forbidden, text.lower())
        self.assertNotIn("git rev-parse", text.lower())
        self.assertNotIn("git status", text.lower())
        self.assertIn("sourceStateStable", text)

    def test_native_identity_is_not_treated_as_equivalent(self) -> None:
        probe = load_probe()
        self.assertEqual("valkey", probe.detect_provider("server_name:valkey\nvalkey_version:9.1.1\n"))
        self.assertEqual("redis", probe.detect_provider("redis_version:8.8.1\n"))
        self.assertEqual("unknown", probe.detect_provider("server_name:other\n"))
        encoded = probe.encode_command("SET", "한글", b"value")
        self.assertTrue(encoded.startswith(b"*3\r\n"))
        self.assertIn("한글".encode("utf-8"), encoded)

    def test_missing_secret_fails_closed_without_echoing_secret(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-cache-live-negative-") as temp:
            output = Path(temp) / "negative.json"
            missing = Path(temp) / "does-not-exist.secret"
            completed = subprocess.run(
                [
                    sys.executable,
                    "-B",
                    str(PROBE),
                    "--provider",
                    "valkey",
                    "--port",
                    "1",
                    "--password-file",
                    str(missing),
                    "--namespace",
                    "negative",
                    "--mode",
                    "identity",
                    "--evidence-output",
                    str(output),
                ],
                capture_output=True,
                text=True,
                timeout=10,
            )
            self.assertNotEqual(0, completed.returncode)
            self.assertTrue(output.is_file())
            self.assertNotIn("password", completed.stdout.lower())


if __name__ == "__main__":
    unittest.main()
