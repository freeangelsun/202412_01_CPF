#!/usr/bin/env python3
"""Run CPF performance-contract workloads with bounded load/soak execution.

Profiles are fail-closed. HTTP and command workloads can use either a fixed iteration
count or a duration-based soak. The runner keeps at most ``sample_reservoir`` latency
values and at most ``concurrency`` in-flight operations, so the verifier itself does
not grow linearly with a long-running soak.
"""
from __future__ import annotations

import argparse
import concurrent.futures
from collections import deque
from dataclasses import dataclass
import json
import math
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
from typing import Iterable


class PerformanceError(RuntimeError):
    pass


@dataclass(frozen=True)
class Sample:
    ok: bool
    latency_ms: float
    detail: str


def _number(value: object, field: str, minimum: float | None = None) -> float:
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        raise PerformanceError(f"{field} must be numeric")
    result = float(value)
    if not math.isfinite(result):
        raise PerformanceError(f"{field} must be finite")
    if minimum is not None and result < minimum:
        raise PerformanceError(f"{field} must be >= {minimum}")
    return result


def _integer(value: object, field: str, minimum: int = 1) -> int:
    if isinstance(value, bool) or not isinstance(value, int) or value < minimum:
        raise PerformanceError(f"{field} must be an integer >= {minimum}")
    return value


def _validate_budgets(value: object, field: str) -> dict:
    if not isinstance(value, dict):
        raise PerformanceError(f"{field} must be an object")
    required = ("max_error_rate", "max_p95_ms", "min_throughput_per_sec")
    missing = [name for name in required if name not in value]
    if missing:
        raise PerformanceError(f"{field} missing: {', '.join(missing)}")
    result = {
        "max_error_rate": _number(value["max_error_rate"], f"{field}.max_error_rate", 0),
        "max_p95_ms": _number(value["max_p95_ms"], f"{field}.max_p95_ms", 0),
        "min_throughput_per_sec": _number(value["min_throughput_per_sec"], f"{field}.min_throughput_per_sec", 0),
    }
    if result["max_error_rate"] > 1:
        raise PerformanceError(f"{field}.max_error_rate must be <= 1")
    if "max_latency_drift_pct" in value:
        result["max_latency_drift_pct"] = _number(value["max_latency_drift_pct"], f"{field}.max_latency_drift_pct", 0)
    if "max_rss_growth_mb" in value:
        result["max_rss_growth_mb"] = _number(value["max_rss_growth_mb"], f"{field}.max_rss_growth_mb", 0)
    return result


def validate_profile(profile: object, *, allow_environment_tokens: bool = False) -> dict:
    if not isinstance(profile, dict):
        raise PerformanceError("profile must be an object")
    if str(profile.get("schema_version", "")) != "1.1":
        raise PerformanceError("schema_version must be '1.1'")
    workloads = profile.get("workloads")
    if not isinstance(workloads, list) or not workloads:
        raise PerformanceError("workloads must be a non-empty array")
    ids: set[str] = set()
    normalized: list[dict] = []
    for index, item in enumerate(workloads):
        field = f"workloads[{index}]"
        if not isinstance(item, dict):
            raise PerformanceError(f"{field} must be an object")
        workload_id = str(item.get("id", "")).strip()
        if not workload_id:
            raise PerformanceError(f"{field}.id is required")
        if workload_id in ids:
            raise PerformanceError(f"duplicate workload id: {workload_id}")
        ids.add(workload_id)
        kind = str(item.get("kind", "")).strip()
        if kind not in {"http", "command"}:
            raise PerformanceError(f"{field}.kind must be http or command")
        enabled = bool(item.get("enabled", True))
        concurrency = _integer(item.get("concurrency", 1), f"{field}.concurrency")
        timeout_seconds = _number(item.get("timeout_seconds", 10), f"{field}.timeout_seconds", 0.001)
        reservoir = _integer(item.get("sample_reservoir", 10000), f"{field}.sample_reservoir", 100)
        if reservoir > 100000:
            raise PerformanceError(f"{field}.sample_reservoir must be <= 100000")
        has_iterations = "iterations" in item
        has_duration = "duration_seconds" in item
        if has_iterations == has_duration:
            raise PerformanceError(f"{field} must define exactly one of iterations or duration_seconds")
        iterations = _integer(item["iterations"], f"{field}.iterations") if has_iterations else None
        duration_seconds = _number(item["duration_seconds"], f"{field}.duration_seconds", 0.1) if has_duration else None
        min_samples = _integer(item.get("min_samples", concurrency), f"{field}.min_samples")
        budgets = _validate_budgets(item.get("budgets"), f"{field}.budgets")
        normalized_item = {
            "id": workload_id,
            "kind": kind,
            "enabled": enabled,
            "concurrency": concurrency,
            "iterations": iterations,
            "duration_seconds": duration_seconds,
            "min_samples": min_samples,
            "timeout_seconds": timeout_seconds,
            "sample_reservoir": reservoir,
            "budgets": budgets,
        }
        if kind == "http":
            url = str(item.get("url", "")).strip()
            unresolved_env_url = bool(re.fullmatch(r"\$\{[A-Z][A-Z0-9_]*\}", url))
            if not url.startswith(("http://", "https://")) and not (allow_environment_tokens and unresolved_env_url):
                raise PerformanceError(f"{field}.url must be http(s)")
            method = str(item.get("method", "GET")).upper()
            if method not in {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD"}:
                raise PerformanceError(f"{field}.method is unsupported")
            statuses = item.get("expected_statuses", [200])
            if not isinstance(statuses, list) or not statuses or any(not isinstance(v, int) for v in statuses):
                raise PerformanceError(f"{field}.expected_statuses must be an integer array")
            headers = item.get("headers", {})
            if not isinstance(headers, dict) or any(not isinstance(k, str) or not isinstance(v, str) for k, v in headers.items()):
                raise PerformanceError(f"{field}.headers must be a string map")
            body = item.get("body")
            if body is not None and not isinstance(body, str):
                raise PerformanceError(f"{field}.body must be a string")
            normalized_item.update(url=url, method=method, expected_statuses=statuses, headers=headers, body=body)
        else:
            command = item.get("command")
            if not isinstance(command, list) or not command or any(not isinstance(v, str) or not v for v in command):
                raise PerformanceError(f"{field}.command must be a non-empty string array")
            cwd = item.get("cwd")
            if cwd is not None and not isinstance(cwd, str):
                raise PerformanceError(f"{field}.cwd must be a string")
            normalized_item.update(command=command, cwd=cwd)
        normalized.append(normalized_item)
    return {"schema_version": "1.1", "workloads": normalized}


def _http_sample(workload: dict) -> Sample:
    body = workload.get("body")
    request = urllib.request.Request(
        workload["url"],
        data=body.encode("utf-8") if body is not None else None,
        headers=workload["headers"],
        method=workload["method"],
    )
    started = time.perf_counter()
    try:
        with urllib.request.urlopen(request, timeout=workload["timeout_seconds"]) as response:
            response.read(1024 * 1024)
            status = response.status
        ok = status in workload["expected_statuses"]
        detail = f"HTTP {status}"
    except urllib.error.HTTPError as exc:
        exc.read(1024 * 1024)
        ok = exc.code in workload["expected_statuses"]
        detail = f"HTTP {exc.code}"
    except Exception as exc:
        ok = False
        detail = f"{type(exc).__name__}: {exc}"
    return Sample(ok=ok, latency_ms=(time.perf_counter() - started) * 1000, detail=detail)


def _command_sample(workload: dict, profile_root: Path) -> Sample:
    cwd = (profile_root / workload["cwd"]).resolve() if workload.get("cwd") else profile_root
    started = time.perf_counter()
    try:
        result = subprocess.run(
            workload["command"], cwd=cwd, timeout=workload["timeout_seconds"],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False,
        )
        ok = result.returncode == 0
        detail = f"exit {result.returncode}"
    except subprocess.TimeoutExpired:
        ok = False
        detail = "timeout"
    except OSError as exc:
        ok = False
        detail = f"{type(exc).__name__}: {exc}"
    return Sample(ok=ok, latency_ms=(time.perf_counter() - started) * 1000, detail=detail)


def _percentile(values: list[float], percentile: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    rank = max(0, math.ceil(percentile * len(ordered)) - 1)
    return ordered[rank]


class BoundedAccumulator:
    def __init__(self, capacity: int) -> None:
        self.capacity = capacity
        self.samples: list[float] = []
        self.first: list[float] = []
        self.last: deque[float] = deque(maxlen=min(1000, capacity))
        self.total = 0
        self.errors = 0
        self.error_examples: list[str] = []

    def add(self, sample: Sample) -> None:
        self.total += 1
        if not sample.ok:
            self.errors += 1
            if len(self.error_examples) < 5:
                self.error_examples.append(sample.detail)
        if len(self.first) < min(1000, self.capacity):
            self.first.append(sample.latency_ms)
        self.last.append(sample.latency_ms)
        if len(self.samples) < self.capacity:
            self.samples.append(sample.latency_ms)
        else:
            # Deterministic bounded reservoir. Replacing a rotating slot keeps memory bounded
            # and maintains observations from the entire run without randomness in evidence.
            self.samples[(self.total - 1) % self.capacity] = sample.latency_ms

    def drift(self) -> float:
        if not self.first or not self.last:
            return 0.0
        first = sum(self.first) / len(self.first)
        last = sum(self.last) / len(self.last)
        if first == 0:
            return 0.0 if last == 0 else float("inf")
        return ((last - first) / first) * 100


def run_workload(workload: dict, profile_root: Path) -> dict:
    def invoke() -> Sample:
        return _http_sample(workload) if workload["kind"] == "http" else _command_sample(workload, profile_root)

    accumulator = BoundedAccumulator(workload["sample_reservoir"])
    started = time.perf_counter()
    deadline = started + workload["duration_seconds"] if workload["duration_seconds"] is not None else None
    target_iterations = workload["iterations"]
    submitted = 0
    with concurrent.futures.ThreadPoolExecutor(max_workers=workload["concurrency"]) as executor:
        pending: set[concurrent.futures.Future[Sample]] = set()
        initial = workload["concurrency"] if target_iterations is None else min(workload["concurrency"], target_iterations)
        for _ in range(initial):
            pending.add(executor.submit(invoke)); submitted += 1
        while pending:
            done, pending = concurrent.futures.wait(pending, return_when=concurrent.futures.FIRST_COMPLETED)
            for future in done:
                accumulator.add(future.result())
                should_submit = False
                if target_iterations is not None:
                    should_submit = submitted < target_iterations
                elif deadline is not None:
                    should_submit = time.perf_counter() < deadline
                if should_submit:
                    pending.add(executor.submit(invoke)); submitted += 1
    elapsed = max(time.perf_counter() - started, 1e-9)
    if accumulator.total < workload["min_samples"]:
        raise PerformanceError(f"{workload['id']} produced {accumulator.total} samples; min_samples={workload['min_samples']}")
    latencies = accumulator.samples
    metrics = {
        "iterations": accumulator.total,
        "sample_reservoir_size": len(latencies),
        "sample_reservoir_capacity": accumulator.capacity,
        "successes": accumulator.total - accumulator.errors,
        "errors": accumulator.errors,
        "error_rate": accumulator.errors / accumulator.total,
        "elapsed_seconds": elapsed,
        "target_duration_seconds": workload["duration_seconds"],
        "throughput_per_sec": accumulator.total / elapsed,
        "p50_ms": _percentile(latencies, 0.50),
        "p95_ms": _percentile(latencies, 0.95),
        "p99_ms": _percentile(latencies, 0.99),
        "max_sampled_ms": max(latencies, default=0.0),
        "latency_drift_pct": accumulator.drift(),
        "bounded_in_flight": workload["concurrency"],
    }
    budgets = workload["budgets"]
    violations: list[str] = []
    if metrics["error_rate"] > budgets["max_error_rate"]: violations.append("error_rate")
    if metrics["p95_ms"] > budgets["max_p95_ms"]: violations.append("p95_ms")
    if metrics["throughput_per_sec"] < budgets["min_throughput_per_sec"]: violations.append("throughput_per_sec")
    if "max_latency_drift_pct" in budgets and metrics["latency_drift_pct"] > budgets["max_latency_drift_pct"]:
        violations.append("latency_drift_pct")
    return {
        "id": workload["id"], "kind": workload["kind"],
        "executionMode": "SOAK" if workload["duration_seconds"] is not None else "ITERATIONS",
        "status": "PASS" if not violations else "FAIL", "metrics": metrics,
        "budgets": budgets, "violations": violations, "error_examples": accumulator.error_examples,
    }


_ENV_TOKEN = re.compile(r"\$\{([A-Z][A-Z0-9_]*)\}")


def _expand_environment(value):
    if isinstance(value, str):
        def repl(match):
            name = match.group(1)
            raw = os.environ.get(name)
            if raw is None or not raw.strip():
                raise PerformanceError(f"required performance environment variable is missing: {name}")
            return raw.strip()
        return _ENV_TOKEN.sub(repl, value)
    if isinstance(value, list): return [_expand_environment(v) for v in value]
    if isinstance(value, dict): return {k: _expand_environment(v) for k, v in value.items()}
    return value


def execute(profile_path: Path, workload_ids: set[str] | None = None, dry_run: bool = False) -> dict:
    try:
        raw = json.loads(profile_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise PerformanceError(f"cannot read profile: {exc}") from exc
    # Mixed profiles may contain HTTP workloads whose URL is supplied only in runtime environments.
    # Validate the complete profile structurally while allowing a single ${ENV} URL token, select first,
    # expand only the selected workloads, then re-run strict semantic validation on the expanded subset.
    profile = validate_profile(raw, allow_environment_tokens=True)
    known_ids = {item["id"] for item in profile["workloads"]}
    if workload_ids:
        unknown = workload_ids - known_ids
        if unknown: raise PerformanceError("unknown workload ids: " + ", ".join(sorted(unknown)))
    selected_raw = [
        item for item in raw["workloads"]
        if bool(item.get("enabled", True)) and (not workload_ids or str(item.get("id", "")).strip() in workload_ids)
    ]
    selected_expanded = [_expand_environment(item) for item in selected_raw]
    selected = validate_profile({"schema_version": "1.1", "workloads": selected_expanded})["workloads"] if selected_expanded else []
    if dry_run:
        return {"status": "PASS", "mode": "DRY_RUN", "profile": str(profile_path), "workload_count": len(profile["workloads"]), "enabled_workload_count": len(selected)}
    if not selected:
        raise PerformanceError("no enabled workloads selected")
    results = [run_workload(item, profile_path.parent) for item in selected]
    return {"status": "PASS" if all(item["status"] == "PASS" for item in results) else "FAIL", "mode": "EXECUTE", "profile": str(profile_path), "results": results}


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--profile", required=True, type=Path)
    parser.add_argument("--workload", action="append", default=[])
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--output-json", type=Path)
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        result = execute(args.profile, set(args.workload) or None, args.dry_run)
    except PerformanceError as exc:
        result = {"status": "FAIL", "error": str(exc)}; code = 2
    else:
        code = 0 if result["status"] == "PASS" else 1
    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
