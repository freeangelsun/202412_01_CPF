from __future__ import annotations

import importlib.util
import json
import sys
import zipfile
from datetime import datetime, timezone
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "generate-cpf-support-bundle.py"
spec = importlib.util.spec_from_file_location("support_bundle", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)


def manifest(files, **overrides):
    value = {
        "request_id": "SUP-001",
        "consent": {
            "approved_by": "operator-1",
            "approved_at": "2026-08-05T00:00:00Z",
            "reason": "incident diagnosis",
            "expires_at": "2026-08-06T00:00:00Z",
        },
        "max_total_bytes": 1024 * 1024,
        "files": files,
    }
    value.update(overrides)
    return value


def run(tmp_path: Path, value: dict):
    root = tmp_path / "root"
    root.mkdir(exist_ok=True)
    mf = tmp_path / "manifest.json"
    mf.write_text(json.dumps(value), encoding="utf-8")
    out = tmp_path / "bundle.zip"
    result = module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    return root, out, result


def test_masks_sensitive_values_and_writes_integrity_manifest(tmp_path: Path):
    root = tmp_path / "root"
    (root / "logs").mkdir(parents=True)
    (root / "logs/app.log").write_text(
        'password=plain\n{"token":"abc123"}\nhttps://user:pass@example.test/x\n'
        'authorization: Bearer value\n',
        encoding="utf-8",
    )
    mf = tmp_path / "manifest.json"
    mf.write_text(json.dumps(manifest([{"path": "logs/app.log", "kind": "log"}])), encoding="utf-8")
    out = tmp_path / "bundle.zip"
    result = module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    assert result["status"] == "PASS"
    assert result["masked_values"] == 4
    with zipfile.ZipFile(out) as zf:
        text = zf.read("bundle/log/logs/app.log").decode()
        assert "plain" not in text and "abc123" not in text and "user:pass" not in text
        assert "Bearer value" not in text and " value" not in text
        assert "authorization: ***MASKED***" in text
        assert text.count("***MASKED***") >= 4
        manifest_value = json.loads(zf.read("bundle-manifest.json"))
        assert manifest_value["file_count"] == 1
        assert manifest_value["files"][0]["masked_values"] == 4
        assert zf.read("bundle-manifest.sha256").decode().endswith("  bundle-manifest.json\n")



def test_masks_full_authorization_and_cookie_header_values(tmp_path: Path):
    root = tmp_path / "root"; root.mkdir()
    (root / "headers.txt").write_text(
        "Authorization: Bearer secret-token with-spaces\n"
        "Proxy-Authorization: Basic dXNlcjpwYXNz\n"
        "Cookie: session=abc; csrf=def\n"
        "Set-Cookie: session=abc; HttpOnly; Secure\n",
        encoding="utf-8",
    )
    mf = tmp_path / "manifest.json"
    mf.write_text(json.dumps(manifest([{"path": "headers.txt", "kind": "log"}])), encoding="utf-8")
    out = tmp_path / "bundle.zip"
    result = module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    assert result["status"] == "PASS"
    with zipfile.ZipFile(out) as zf:
        text = zf.read("bundle/log/headers.txt").decode()
    assert "secret-token" not in text
    assert "dXNlcjpwYXNz" not in text
    assert "session=abc" not in text
    assert text.splitlines() == [
        "Authorization: ***MASKED***",
        "Proxy-Authorization: ***MASKED***",
        "Cookie: ***MASKED***",
        "Set-Cookie: ***MASKED***",
    ]

def test_rejects_missing_or_expired_consent(tmp_path: Path):
    root = tmp_path / "root"; root.mkdir(); (root / "health.txt").write_text("UP", encoding="utf-8")
    mf = tmp_path / "manifest.json"; out = tmp_path / "bundle.zip"
    value = manifest([{"path": "health.txt", "kind": "health"}])
    value.pop("consent")
    mf.write_text(json.dumps(value), encoding="utf-8")
    with pytest.raises(module.BundleError, match="consent object"):
        module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    value = manifest([{"path": "health.txt", "kind": "health"}])
    value["consent"]["expires_at"] = "2026-08-05T00:30:00Z"
    mf.write_text(json.dumps(value), encoding="utf-8")
    with pytest.raises(module.BundleError, match="expired"):
        module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))


def test_rejects_path_escape_binary_and_size_overflow(tmp_path: Path):
    root = tmp_path / "root"; root.mkdir(); (root / "ok.txt").write_text("ok", encoding="utf-8")
    mf = tmp_path / "manifest.json"; out = tmp_path / "bundle.zip"
    mf.write_text(json.dumps(manifest([{"path": "../ok.txt", "kind": "log"}])), encoding="utf-8")
    with pytest.raises(module.BundleError, match="unsafe relative path"):
        module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    (root / "binary.bin").write_bytes(b"a\x00b")
    mf.write_text(json.dumps(manifest([{"path": "binary.bin", "kind": "diagnostic"}])), encoding="utf-8")
    with pytest.raises(module.BundleError, match="binary"):
        module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    mf.write_text(json.dumps(manifest([{"path": "ok.txt", "kind": "diagnostic"}], max_total_bytes=1)), encoding="utf-8")
    with pytest.raises(module.BundleError, match="max_total_bytes"):
        module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))


def test_rejects_residual_private_key(tmp_path: Path):
    root = tmp_path / "root"; root.mkdir()
    private_key_fixture = "-----BEGIN " + "PRIVATE KEY-----\nabc\n-----END " + "PRIVATE KEY-----\n"
    (root / "config.txt").write_text(private_key_fixture, encoding="utf-8")
    mf = tmp_path / "manifest.json"
    mf.write_text(json.dumps(manifest([{"path": "config.txt", "kind": "config"}])), encoding="utf-8")
    out = tmp_path / "bundle.zip"
    result = module.generate_bundle(root, mf, out, now=datetime(2026, 8, 5, 1, tzinfo=timezone.utc))
    assert result["masked_values"] == 1
    with zipfile.ZipFile(out) as zf:
        assert b"PRIVATE KEY" not in zf.read("bundle/config/config.txt")


def test_output_is_deterministic_for_fixed_time(tmp_path: Path):
    root = tmp_path / "root"; root.mkdir(); (root / "version.txt").write_text("1.0\n", encoding="utf-8")
    mf = tmp_path / "manifest.json"; mf.write_text(json.dumps(manifest([{"path": "version.txt", "kind": "version"}])), encoding="utf-8")
    now = datetime(2026, 8, 5, 1, tzinfo=timezone.utc)
    out1 = tmp_path / "a.zip"; out2 = tmp_path / "b.zip"
    r1 = module.generate_bundle(root, mf, out1, now=now)
    r2 = module.generate_bundle(root, mf, out2, now=now)
    assert out1.read_bytes() == out2.read_bytes()
    assert r1["bundle_sha256"] == r2["bundle_sha256"]
