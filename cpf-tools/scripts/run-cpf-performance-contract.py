#!/usr/bin/env python3
"""Run CPF performance-contract workloads with deterministic budget evaluation.

The runner is dependency-free and supports HTTP and process workloads. It is intended
for local/CI smoke budgets and authoritative target-environment load/soak profiles.
Profiles are fail-closed: unknown fields are tolerated for forward compatibility, but
unknown workload kinds, missing budgets, disabled-only execution, and malformed input
are rejected.
"""
from __future__ import annotations

import argparse
import concurrent.futures
import json
import math
import subprocess
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path
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
        result["max_latency_drift_pct"] = _number(
            value["max_latency_drift_pct"], f"{field}.max_latency_drift_pct", 0
        )
    return result


def validate_profile(profile: object) -> dict:
    if not isinstance(profile, dict):
        raise PerformanceError("profile must be an object")
    if str(profile.get("schema_version", "")) != "1.0":
        raise PerformanceError("schema_version must be '1.0'")
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
        iterations = _integer(item.get("iterations", 1), f"{field}.iterations")
        timeout_seconds = _number(item.get("timeout_seconds", 10), f"{field}.timeout_seconds", 0.001)
        budgets = _validate_budgets(item.get("budgets"), f"{field}.budgets")
        normalized_item = {
            "id": workload_id,
            "kind": kind,
            "enabled": enabled,
            "concurrency": concurrency,
            "iterations": iterations,
            "timeout_seconds": timeout_seconds,
            "budgets": budgets,
        }
        if kind == "http":
            url = str(item.get("url", "")).strip()
            if not url.startswith(("http://", "https://")):
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
            normalized_item.update(
                url=url,
                method=method,
                expected_statuses=statuses,
                headers=headers,
                body=body,
            )
        else:
            command = item.get("command")
            if not isinstance(command, list) or not command or any(not isinstance(v, str) or not v for v in command):
                raise PerformanceError(f"{field}.command must be a non-empty string array")
            cwd = item.get("cwd")
            if cwd is not None and not isinstance(cwd, str):
                raise PerformanceError(f"{field}.cwd must be a string")
            normalized_item.update(command=command, cwd=cwd)
        normalized.append(normalized_item)
    return {"schema_version": "1.0", "workloads": normalized}


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
            response.read()
            status = response.status
        ok = status in workload["expected_statuses"]
        detail = f"HTTP {status}"
    except urllib.error.HTTPError as exc:
        exc.read()
        ok = exc.code in workload["expected_statuses"]
        detail = f"HTTP {exc.code}"
    except Exception as exc:  # network timeouts and connection failures are workload results
        ok = False
        detail = f"{type(exc).__name__}: {exc}"
    latency_ms = (time.perf_counter() - started) * 1000
    return Sample(ok=ok, latency_ms=latency_ms, detail=detail)


def _command_sample(workload: dict, profile_root: Path) -> Sample:
    cwd = profile_root / workload["cwd"] if workload.get("cwd") else profile_root
    started = time.perf_counter()
    try:
        result = subprocess.run(
            workload["command"],
            cwd=cwd,
            timeout=workload["timeout_seconds"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            check=False,
        )
        ok = result.returncode == 0
        detail = f"exit {result.returncode}"
    except subprocess.TimeoutExpired:
        ok = False
        detail = "timeout"
    except OSError as exc:
        ok = False
        detail = f"{type(exc).__name__}: {exc}"
    latency_ms = (time.perf_counter() - started) * 1000
    return Sample(ok=ok, latency_ms=latency_ms, detail=detail)


def _percentile(values: list[float], percentile: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    rank = max(0, math.ceil(percentile * len(ordered)) - 1)
    return ordered[rank]


def _latency_drift(values: list[float]) -> float:
    if len(values) < 4:
        return 0.0
    split = max(1, len(values) // 4)
    first = sum(values[:split]) / split
    last = sum(values[-split:]) / split
    if first == 0:
        return 0.0 if last == 0 else float("inf")
    return ((last - first) / first) * 100


def run_workload(workload: dict, profile_root: Path) -> dict:
    operation = _http_sample if workload["kind"] == "http" else None

    def invoke() -> Sample:
        return operation(workload) if operation else _command_sample(workload, profile_root)

    started = time.perf_counter()
    samples: list[Sample] = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=workload["concurrency"]) as executor:
        futures = [executor.submit(invoke) for _ in range(workload["iterations"])]
        for future in concurrent.futures.as_completed(futures):
            samples.append(future.result())
    elapsed = max(time.perf_counter() - started, 1e-9)
    latencies = [sample.latency_ms for sample in samples]
    errors = sum(1 for sample in samples if not sample.ok)
    metrics = {
        "iterations": len(samples),
        "successes": len(samples) - errors,
        "errors": errors,
        "error_rate": errors / len(samples),
        "elapsed_seconds": elapsed,
        "throughput_per_sec": len(samples) / elapsed,
        "p50_ms": _percentile(latencies, 0.50),
        "p95_ms": _percentile(latencies, 0.95),
        "p99_ms": _percentile(latencies, 0.99),
        "max_ms": max(latencies, default=0.0),
        "latency_drift_pct": _latency_drift(latencies),
    }
    budgets = workload["budgets"]
    violations: list[str] = []
    if metrics["error_rate"] > budgets["max_error_rate"]:
        violations.append("error_rate")
    if metrics["p95_ms"] > budgets["max_p95_ms"]:
        violations.append("p95_ms")
    if metrics["throughput_per_sec"] < budgets["min_throughput_per_sec"]:
        violations.append("throughput_per_sec")
    if "max_latency_drift_pct" in budgets and metrics["latency_drift_pct"] > budgets["max_latency_drift_pct"]:
        violations.append("latency_drift_pct")
    error_examples = [sample.detail for sample in samples if not sample.ok][:5]
    return {
        "id": workload["id"],
        "kind": workload["kind"],
        "status": "PASS" if not violations else "FAIL",
        "metrics": metrics,
        "budgets": budgets,
        "violations": violations,
        "error_examples": error_examples,
    }


def execute(profile_path: Path, workload_ids: set[str] | None = None, dry_run: bool = False) -> dict:
    try:
        raw = json.loads(profile_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise PerformanceError(f"cannot read profile: {exc}") from exc
    profile = validate_profile(raw)
    selected = [
        item for item in profile["workloads"]
        if item["enabled"] and (not workload_ids or item["id"] in workload_ids)
    ]
    if workload_ids:
        unknown = workload_ids - {item["id"] for item in profile["workloads"]}
        if unknown:
            raise PerformanceError("unknown workload ids: " + ", ".join(sorted(unknown)))
    if dry_run:
        return {
            "status": "PASS",
            "mode": "DRY_RUN",
            "profile": str(profile_path),
            "workload_count": len(profile["workloads"]),
            "enabled_workload_count": len(selected),
        }
    if not selected:
        raise PerformanceError("no enabled workloads selected")
    results = [run_workload(item, profile_path.parent) for item in selected]
    return {
        "status": "PASS" if all(item["status"] == "PASS" for item in results) else "FAIL",
        "mode": "EXECUTE",
        "profile": str(profile_path),
        "results": results,
    }


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
        result = {"status": "FAIL", "error": str(exc)}
        code = 2
    else:
        code = 0 if result["status"] == "PASS" else 1
    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
