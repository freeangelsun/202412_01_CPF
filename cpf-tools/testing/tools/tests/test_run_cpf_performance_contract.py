from __future__ import annotations

import importlib.util
import json
import sys
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "run-cpf-performance-contract.py"
spec = importlib.util.spec_from_file_location("performance_contract", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/fail":
            self.send_response(503)
        else:
            self.send_response(200)
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, format, *args):
        pass


@pytest.fixture
def server():
    instance = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
    thread = threading.Thread(target=instance.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{instance.server_port}"
    finally:
        instance.shutdown(); thread.join(timeout=2); instance.server_close()


def profile(url: str, **changes):
    workload = {
        "id": "api-smoke",
        "kind": "http",
        "enabled": True,
        "url": url,
        "method": "GET",
        "concurrency": 2,
        "iterations": 8,
        "timeout_seconds": 2,
        "expected_statuses": [200],
        "budgets": {
            "max_error_rate": 0,
            "max_p95_ms": 1000,
            "min_throughput_per_sec": 0.1,
            "max_latency_drift_pct": 10000,
        },
    }
    workload.update(changes)
    return {"schema_version": "1.1", "workloads": [workload]}


def write(tmp_path: Path, value: dict) -> Path:
    path = tmp_path / "profile.json"
    path.write_text(json.dumps(value), encoding="utf-8")
    return path


def test_http_workload_passes_and_reports_metrics(tmp_path: Path, server: str):
    result = module.execute(write(tmp_path, profile(server + "/ok")))
    assert result["status"] == "PASS"
    item = result["results"][0]
    assert item["metrics"]["iterations"] == 8
    assert item["metrics"]["errors"] == 0
    assert item["metrics"]["throughput_per_sec"] > 0


def test_budget_failure_is_fail_closed(tmp_path: Path, server: str):
    result = module.execute(write(tmp_path, profile(server + "/fail")))
    assert result["status"] == "FAIL"
    assert "error_rate" in result["results"][0]["violations"]
    assert result["results"][0]["error_examples"] == ["HTTP 503"] * 5


def test_command_workload_and_dry_run(tmp_path: Path):
    value = {
        "schema_version": "1.1",
        "workloads": [{
            "id": "batch-command",
            "kind": "command",
            "enabled": True,
            "command": [sys.executable, "-c", "print('ok')"],
            "concurrency": 1,
            "iterations": 3,
            "timeout_seconds": 3,
            "budgets": {"max_error_rate": 0, "max_p95_ms": 3000, "min_throughput_per_sec": 0.01},
        }],
    }
    path = write(tmp_path, value)
    dry = module.execute(path, dry_run=True)
    assert dry["status"] == "PASS" and dry["enabled_workload_count"] == 1
    result = module.execute(path)
    assert result["status"] == "PASS" and result["results"][0]["metrics"]["successes"] == 3


def test_rejects_invalid_profile_unknown_kind_and_duplicate_id(tmp_path: Path):
    with pytest.raises(module.PerformanceError, match="schema_version"):
        module.validate_profile({"schema_version": "2", "workloads": []})
    value = profile("http://localhost")
    value["workloads"][0]["kind"] = "broker-magic"
    with pytest.raises(module.PerformanceError, match="kind"):
        module.validate_profile(value)
    value = profile("http://localhost")
    value["workloads"].append(dict(value["workloads"][0]))
    with pytest.raises(module.PerformanceError, match="duplicate"):
        module.validate_profile(value)


def test_disabled_template_validates_but_cannot_execute(tmp_path: Path, server: str):
    path = write(tmp_path, profile(server, enabled=False))
    assert module.execute(path, dry_run=True)["enabled_workload_count"] == 0
    with pytest.raises(module.PerformanceError, match="no enabled workloads"):
        module.execute(path)


def test_mixed_profile_selected_command_does_not_require_unselected_http_env(tmp_path: Path, monkeypatch):
    monkeypatch.delenv("CPF_PERF_ADM_HEALTH_URL", raising=False)
    value = {
        "schema_version": "1.1",
        "workloads": [
            {
                "id": "adm-api-load", "kind": "http", "enabled": True,
                "url": "${CPF_PERF_ADM_HEALTH_URL}", "method": "GET",
                "concurrency": 1, "iterations": 1, "timeout_seconds": 1,
                "budgets": {"max_error_rate": 0, "max_p95_ms": 1000, "min_throughput_per_sec": 0},
            },
            {
                "id": "broker-backpressure", "kind": "command", "enabled": True,
                "command": [sys.executable, "-c", "print('ok')"],
                "concurrency": 1, "iterations": 1, "timeout_seconds": 2,
                "budgets": {"max_error_rate": 0, "max_p95_ms": 2000, "min_throughput_per_sec": 0},
            },
        ],
    }
    path = write(tmp_path, value)
    result = module.execute(path, {"broker-backpressure"}, dry_run=True)
    assert result["status"] == "PASS" and result["enabled_workload_count"] == 1


def test_selected_http_environment_url_is_expanded_then_strictly_validated(tmp_path: Path, monkeypatch):
    value = profile("${CPF_PERF_ADM_HEALTH_URL}")
    path = write(tmp_path, value)
    monkeypatch.setenv("CPF_PERF_ADM_HEALTH_URL", "not-a-url")
    with pytest.raises(module.PerformanceError, match=r"url must be http\(s\)"):
        module.execute(path, {"api-smoke"}, dry_run=True)
