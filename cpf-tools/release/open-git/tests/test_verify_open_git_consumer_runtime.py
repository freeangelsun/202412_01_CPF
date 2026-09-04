from __future__ import annotations

import importlib.util
import json
from pathlib import Path

import pytest


ROOT = Path(__file__).resolve().parents[4]
TOOL = ROOT / "cpf-tools/release/open-git/verify_open_git_consumer_runtime.py"
SPEC = importlib.util.spec_from_file_location("cpf_open_git_consumer_runtime", TOOL)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _credentials() -> dict[str, str]:
    return {key: "test-value-which-is-never-rendered" for key in MODULE.REQUIRED_CREDENTIAL_ENV}


def test_required_credentials_fail_closed_without_all_initial_operator_inputs(monkeypatch: pytest.MonkeyPatch) -> None:
    for key in MODULE.REQUIRED_CREDENTIAL_ENV:
        monkeypatch.delenv(key, raising=False)
    with pytest.raises(SystemExit) as failure:
        MODULE.required_credentials()
    assert "CPF_MBW_INITIAL_OPERATOR_LOGIN_ID" in str(failure.value)


def test_backoffice_verifier_requires_cookie_csrf_login_and_authenticated_business_transaction(
        monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    monkeypatch.setattr(MODULE, "start_target", lambda *args, **kwargs: None)
    monkeypatch.setattr(MODULE, "wait_http", lambda *args, **kwargs: 200)
    monkeypatch.setattr(MODULE, "cookie_names", lambda jar: {"XSRF-TOKEN", "CPF_MBW_ACCESS", "CPF_MBW_REFRESH"})
    calls: list[str] = []

    def fake_http(url: str, **kwargs):
        calls.append(url)
        if url.endswith("/mbw/"):
            return 200, b"<div id=\"root\"></div>", {}
        if url.endswith("/backoffice/organizations") and kwargs.get("opener") is None:
            return 401, b"", {}
        if url.endswith("/security/csrf"):
            return 200, json.dumps({"headerName": "X-XSRF-TOKEN", "token": "csrf"}).encode(), {}
        if url.endswith("/auth/login"):
            assert kwargs["headers"]["X-XSRF-TOKEN"] == "csrf"
            assert "Idempotency-Key" in kwargs["headers"]
            return 200, b'{"authenticated":true}', {}
        if url.endswith("/auth/me"):
            return 200, b"{}", {}
        if url.endswith("/backoffice/organizations"):
            return 200, b"[]", {"X-Transaction-Id": "20260904123456789MBWABC12340000001"}
        raise AssertionError(url)

    monkeypatch.setattr(MODULE, "http", fake_http)
    result = MODULE.verify_backoffice(
        tmp_path, {"backoffice": {"port": 8082}, "backoffice-web": {"port": 8092}}, _credentials())
    assert result["login"] == "PASS"
    assert result["authenticatedSession"] == "PASS"
    assert result["businessOperation"] == "MBW_BACKOFFICE_FIND_ORGANIZATIONS"
    assert any(url.endswith("/security/csrf") for url in calls)


def test_backoffice_verifier_rejects_unsuccessful_login(monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    monkeypatch.setattr(MODULE, "start_target", lambda *args, **kwargs: None)
    monkeypatch.setattr(MODULE, "wait_http", lambda *args, **kwargs: 200)
    monkeypatch.setattr(MODULE, "cookie_names", lambda jar: {"XSRF-TOKEN"})

    def fake_http(url: str, **kwargs):
        if url.endswith("/mbw/"):
            return 200, b"<script></script>", {}
        if url.endswith("/backoffice/organizations") and kwargs.get("opener") is None:
            return 401, b"", {}
        if url.endswith("/security/csrf"):
            return 200, b'{"headerName":"X-XSRF-TOKEN","token":"csrf"}', {}
        if url.endswith("/auth/login"):
            return 401, b"{}", {}
        raise AssertionError(url)

    monkeypatch.setattr(MODULE, "http", fake_http)
    with pytest.raises(MODULE.ConsumerRuntimeError, match="login did not succeed: status=401"):
        MODULE.verify_backoffice(
            tmp_path, {"backoffice": {"port": 8082}, "backoffice-web": {"port": 8092}}, _credentials())
