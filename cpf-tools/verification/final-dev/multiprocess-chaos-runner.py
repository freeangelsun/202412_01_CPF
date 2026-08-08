#!/usr/bin/env python3
"""CPF ADM multi-instance/process-kill qualification runner.

The runner owns the orchestration logic. Live infrastructure is supplied only as
addresses/credentials/process control commands; an opaque external test program
is not accepted as evidence. It verifies one shared approval DB through two ADM
instances and records machine-readable evidence bound to the candidate SHA.
"""
from __future__ import annotations

import argparse
import concurrent.futures
import json
import os
import shlex
import subprocess
import sys
import threading
import time
from dataclasses import dataclass
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any, Callable
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlparse
from urllib.request import Request, urlopen

REQUIRED_SCENARIOS = ("multi-instance-claim", "process-kill", "unknown-reconcile")


class QualificationError(RuntimeError):
    pass


@dataclass(frozen=True)
class HttpResult:
    status: int
    body: Any


def _env(name: str, *, required: bool = True, default: str = "") -> str:
    value = os.environ.get(name, default).strip()
    if required and not value:
        raise QualificationError(f"missing required environment variable: {name}")
    return value


def _headers() -> dict[str, str]:
    cookie = _env("CPF_CHAOS_ADM_COOKIE")
    csrf_name = _env("CPF_CHAOS_CSRF_HEADER_NAME", required=False, default="X-CSRF-TOKEN")
    csrf_token = _env("CPF_CHAOS_CSRF_TOKEN")
    return {"Accept": "application/json", "Cookie": cookie, csrf_name: csrf_token}


def _request(base: str, method: str, path: str, *, query: dict[str, str] | None = None,
             timeout: float = 20.0) -> HttpResult:
    url = base.rstrip("/") + path
    if query:
        url += "?" + urlencode(query)
    req = Request(url, method=method, headers=_headers())
    try:
        with urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            body = json.loads(raw) if raw.strip() else None
            return HttpResult(resp.status, body)
    except HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        try:
            body = json.loads(raw) if raw.strip() else None
        except json.JSONDecodeError:
            body = raw
        return HttpResult(exc.code, body)
    except (URLError, TimeoutError) as exc:
        raise QualificationError(f"HTTP {method} {url} failed: {exc}") from exc


def _find_key(value: Any, key: str) -> Any:
    if isinstance(value, dict):
        for k, v in value.items():
            if k.lower() == key.lower():
                return v
        for v in value.values():
            found = _find_key(v, key)
            if found is not None:
                return found
    elif isinstance(value, list):
        for item in value:
            found = _find_key(item, key)
            if found is not None:
                return found
    return None


def _detail(base: str, request_id: str) -> dict[str, Any]:
    result = _request(base, "GET", f"/adm/api/approvals/requests/{request_id}")
    if result.status != 200 or not isinstance(result.body, dict):
        raise QualificationError(f"approval detail failed id={request_id} status={result.status}")
    return result.body


def _approval_status(body: Any) -> str:
    return str(_find_key(body, "approvalStatus") or "").upper()


def _execution_status(body: Any) -> str:
    return str(_find_key(body, "executionStatus") or "").upper()


def _execute(base: str, request_id: str, reason: str, *, timeout: float = 30.0) -> HttpResult:
    return _request(base, "POST", f"/adm/api/approvals/requests/{request_id}/execute",
                    query={"reason": reason}, timeout=timeout)


def _reconcile(base: str, request_id: str, reason: str) -> HttpResult:
    return _request(base, "POST", f"/adm/api/approvals/requests/{request_id}/reconcile",
                    query={"reason": reason}, timeout=30.0)


def _wait_status(base: str, request_id: str, accepted: set[str], timeout_seconds: float) -> dict[str, Any]:
    deadline = time.monotonic() + timeout_seconds
    last: dict[str, Any] | None = None
    while time.monotonic() < deadline:
        last = _detail(base, request_id)
        states = {_approval_status(last), _execution_status(last)}
        if states & accepted:
            return last
        time.sleep(0.25)
    raise QualificationError(
        f"timeout waiting id={request_id} accepted={sorted(accepted)} "
        f"approval={_approval_status(last)} execution={_execution_status(last)}")


def _run_control(command: str) -> None:
    if not command.strip():
        raise QualificationError("process control command is empty")
    completed = subprocess.run(command, shell=True, text=True, capture_output=True, timeout=60)
    if completed.returncode != 0:
        raise QualificationError(
            f"process control command failed rc={completed.returncode}: "
            f"{completed.stdout[-1000:]} {completed.stderr[-1000:]}")


def scenario_multi_instance_claim(a: str, b: str, request_id: str) -> dict[str, Any]:
    reason = "CPF chaos multi-instance duplicate claim verification"
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures = [pool.submit(_execute, base, request_id, reason) for base in (a, b)]
        results = [f.result() for f in futures]
    statuses = [r.status for r in results]
    # One request may complete before the competitor arrives, but both callers may never
    # independently mutate the owner. The loser must be a state conflict/validation rejection.
    accepted_loser = {409, 422}
    successes = sum(status == 200 for status in statuses)
    if successes != 1 or not any(status in accepted_loser for status in statuses):
        raise QualificationError(f"multi-instance claim was not fenced: http_statuses={statuses}")
    final = _detail(b, request_id)
    if _approval_status(final) not in {"SUCCEEDED", "FAILED", "UNKNOWN"}:
        raise QualificationError(f"unexpected final approval state after claim race: {_approval_status(final)}")
    return {"httpStatuses": statuses, "finalApprovalStatus": _approval_status(final),
            "finalExecutionStatus": _execution_status(final)}


def scenario_process_kill(a: str, b: str, request_id: str,
                          kill: Callable[[], None], restart: Callable[[], None] | None = None) -> dict[str, Any]:
    reason = "CPF chaos process kill after fenced reservation"
    execute_result: list[HttpResult | Exception] = []

    def invoke() -> None:
        try:
            execute_result.append(_execute(a, request_id, reason, timeout=120.0))
        except Exception as exc:  # process loss is expected to break the client connection
            execute_result.append(exc)

    thread = threading.Thread(target=invoke, daemon=True)
    thread.start()
    _wait_status(b, request_id, {"EXECUTING", "RUNNING"},
                 float(_env("CPF_CHAOS_EXECUTING_WAIT_SECONDS", required=False, default="30")))
    kill()
    thread.join(timeout=5.0)
    if restart:
        restart()
    # The database lease recovery loop converts the abandoned RUNNING reservation to UNKNOWN.
    unknown = _wait_status(b, request_id, {"UNKNOWN"},
                           float(_env("CPF_CHAOS_UNKNOWN_WAIT_SECONDS", required=False, default="420")))
    return {"approvalStatus": _approval_status(unknown), "executionStatus": _execution_status(unknown),
            "executeClientResult": type(execute_result[0]).__name__ if execute_result else "connection-lost"}


def scenario_unknown_reconcile(b: str, request_id: str) -> dict[str, Any]:
    before = _detail(b, request_id)
    if "UNKNOWN" not in {_approval_status(before), _execution_status(before)}:
        raise QualificationError("reconcile scenario requires durable UNKNOWN before observation")
    command_before = _find_key(before, "commandRequestId")
    result = _reconcile(b, request_id, "CPF chaos reconcile observes owner without replay")
    if result.status != 200:
        raise QualificationError(f"UNKNOWN reconcile failed status={result.status}")
    after = _detail(b, request_id)
    command_after = _find_key(after, "commandRequestId")
    if command_before and command_after and str(command_before) != str(command_after):
        raise QualificationError("reconcile changed commandRequestId; possible mutation replay")
    if _approval_status(after) not in {"SUCCEEDED", "FAILED", "UNKNOWN"}:
        raise QualificationError(f"reconcile returned invalid state: {_approval_status(after)}")
    return {"before": _approval_status(before), "after": _approval_status(after),
            "commandRequestIdStable": str(command_before) == str(command_after)}


def run_live(args: argparse.Namespace) -> dict[str, Any]:
    a = _env("CPF_CHAOS_INSTANCE_A_BASE_URL")
    b = _env("CPF_CHAOS_INSTANCE_B_BASE_URL")
    if a.rstrip("/") == b.rstrip("/"):
        raise QualificationError("two distinct ADM instance base URLs are required")
    claim_id = _env("CPF_CHAOS_APPROVAL_CLAIM_ID")
    kill_id = _env("CPF_CHAOS_APPROVAL_KILL_ID")
    kill_cmd = _env("CPF_CHAOS_INSTANCE_A_KILL_CMD")
    restart_cmd = _env("CPF_CHAOS_INSTANCE_A_RESTART_CMD", required=False)
    requested = tuple(x.strip() for x in args.scenarios.split(",") if x.strip())
    unknown = sorted(set(requested) - set(REQUIRED_SCENARIOS))
    if unknown:
        raise QualificationError(f"unsupported approval chaos scenarios: {unknown}")

    rows: list[dict[str, Any]] = []
    for scenario in requested:
        if scenario == "multi-instance-claim":
            detail = scenario_multi_instance_claim(a, b, claim_id)
        elif scenario == "process-kill":
            detail = scenario_process_kill(a, b, kill_id,
                                           lambda: _run_control(kill_cmd),
                                           (lambda: _run_control(restart_cmd)) if restart_cmd else None)
        elif scenario == "unknown-reconcile":
            detail = scenario_unknown_reconcile(b, kill_id)
        else:  # guarded above
            raise AssertionError(scenario)
        rows.append({"id": scenario, "status": "PASS", "detail": detail})
    return {"sourceSha": args.baseline_sha.lower(), "status": "PASS", "scenarios": rows,
            "instanceA": a, "instanceB": b}


# ---------------------------- deterministic self-test ----------------------------
class _FakeState:
    def __init__(self) -> None:
        self.lock = threading.Lock()
        self.rows = {
            "101": {"approvalStatus": "APPROVED", "executionStatus": "READY", "commandRequestId": "cmd-101"},
            "202": {"approvalStatus": "APPROVED", "executionStatus": "READY", "commandRequestId": "cmd-202"},
        }
        self.kill_event = threading.Event()


class _FakeHandler(BaseHTTPRequestHandler):
    state: _FakeState

    def log_message(self, *_: Any) -> None:
        return

    def _json(self, code: int, body: Any) -> None:
        raw = json.dumps(body).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def do_GET(self) -> None:  # noqa: N802
        m = __import__("re").match(r"/adm/api/approvals/requests/(\d+)$", urlparse(self.path).path)
        if not m:
            self._json(404, {})
            return
        with self.state.lock:
            self._json(200, {"data": dict(self.state.rows[m.group(1)])})

    def do_POST(self) -> None:  # noqa: N802
        path = urlparse(self.path).path
        m = __import__("re").match(r"/adm/api/approvals/requests/(\d+)/(execute|reconcile)$", path)
        if not m:
            self._json(404, {})
            return
        rid, op = m.group(1), m.group(2)
        if op == "execute":
            with self.state.lock:
                row = self.state.rows[rid]
                if row["approvalStatus"] != "APPROVED":
                    self._json(409, {"code": "STATE_CONFLICT"})
                    return
                row["approvalStatus"] = "EXECUTING"
                row["executionStatus"] = "RUNNING"
            if rid == "202":
                self.state.kill_event.wait(timeout=5)
                # Simulate DB-side lease sweeper after instance loss.
                with self.state.lock:
                    row = self.state.rows[rid]
                    row["approvalStatus"] = "UNKNOWN"
                    row["executionStatus"] = "UNKNOWN"
                self._json(503, {"code": "INSTANCE_KILLED"})
                return
            time.sleep(0.2)
            with self.state.lock:
                row = self.state.rows[rid]
                row["approvalStatus"] = "SUCCEEDED"
                row["executionStatus"] = "SUCCEEDED"
            self._json(200, {"data": dict(row)})
            return
        with self.state.lock:
            row = self.state.rows[rid]
            if row["approvalStatus"] != "UNKNOWN":
                self._json(409, {})
                return
            row["approvalStatus"] = "SUCCEEDED"
            row["executionStatus"] = "SUCCEEDED"
            self._json(200, {"data": dict(row)})


def self_test() -> None:
    state = _FakeState()
    _FakeHandler.state = state
    server = ThreadingHTTPServer(("127.0.0.1", 0), _FakeHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    base = f"http://127.0.0.1:{server.server_port}"
    os.environ.update({
        "CPF_CHAOS_ADM_COOKIE": "SESSION=test",
        "CPF_CHAOS_CSRF_TOKEN": "test",
        "CPF_CHAOS_EXECUTING_WAIT_SECONDS": "2",
        "CPF_CHAOS_UNKNOWN_WAIT_SECONDS": "2",
    })
    try:
        claim = scenario_multi_instance_claim(base, base + "/", "101")
        if claim["httpStatuses"].count(200) != 1:
            raise QualificationError("self-test duplicate claim assertion failed")
        killed = scenario_process_kill(base, base, "202", state.kill_event.set)
        if killed["approvalStatus"] != "UNKNOWN":
            raise QualificationError("self-test kill did not converge to UNKNOWN")
        reconciled = scenario_unknown_reconcile(base, "202")
        if not reconciled["commandRequestIdStable"] or reconciled["after"] != "SUCCEEDED":
            raise QualificationError("self-test reconcile assertion failed")
    finally:
        server.shutdown()
        server.server_close()
    print("[CPF][MULTIPROCESS-CHAOS][SELF-TEST][PASS]")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", default=".")
    parser.add_argument("--baseline-sha", default="")
    parser.add_argument("--evidence-dir", default="build/evidence/r6-release/multiprocess")
    parser.add_argument("--summary-json", default="")
    parser.add_argument("--scenarios", default=",".join(REQUIRED_SCENARIOS))
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test()
        return 0
    if len(args.baseline_sha) != 40 or any(ch not in "0123456789abcdefABCDEF" for ch in args.baseline_sha):
        raise QualificationError("--baseline-sha must be a 40-hex exact SHA")
    summary = run_live(args)
    out = Path(args.summary_json or (Path(args.evidence_dir) / "chaos-summary.json"))
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"[CPF][MULTIPROCESS-CHAOS][PASS] sourceSha={summary['sourceSha']} scenarios={len(summary['scenarios'])}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except QualificationError as exc:
        print(f"[CPF][MULTIPROCESS-CHAOS][FAIL] {exc}", file=sys.stderr)
        raise SystemExit(2)
