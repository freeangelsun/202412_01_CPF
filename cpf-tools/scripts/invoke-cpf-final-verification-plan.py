#!/usr/bin/env python3
"""Execute the validated CPF final verification plan exactly once per command.

The executor is fail-closed, exact-SHA, read-only with respect to the repository,
and writes sanitized command evidence only outside the working tree.
"""
from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Any

SHA_RE = re.compile(r"^[0-9a-f]{40}$")
SECRET_RE = re.compile(r"(?i)(password|secret|token|authorization|cookie|credential)\s*[:=]\s*([^\s,;]+)")


def sanitize(value: str) -> str:
    value = SECRET_RE.sub(lambda m: f"{m.group(1)}=***", value)
    value = re.sub(r"(?i)bearer\s+[A-Za-z0-9._~+/=-]+", "Bearer ***", value)
    return value[-20000:]


def run_capture(command: list[str], cwd: Path, env: dict[str, str] | None = None) -> tuple[int, str, str]:
    cp = subprocess.run(command, cwd=cwd, env=env, text=True, capture_output=True, errors="replace")
    return cp.returncode, sanitize(cp.stdout), sanitize(cp.stderr)


def git(root: Path, *args: str) -> str:
    cp = subprocess.run(["git", "-C", str(root), *args], text=True, capture_output=True, errors="replace")
    if cp.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed(exit={cp.returncode}): {sanitize(cp.stderr)}")
    return cp.stdout.strip()


def clean_status(root: Path) -> list[str]:
    value = git(root, "status", "--porcelain=v1", "--untracked-files=all")
    return [line for line in value.splitlines() if line]


def replace_tokens(value: str, root: Path, sha: str, evidence: Path) -> str:
    return value.replace("{root}", str(root)).replace("{sha}", sha).replace("{evidence}", str(evidence))


def executable_for(command: dict[str, Any], root: Path) -> tuple[list[str], Path]:
    runner = command["runner"]
    path = str(command["path"])
    cwd = root / str(command.get("workingDirectory", ""))
    if runner == "python":
        return [sys.executable, str(root / path)], cwd
    if runner == "node":
        return ["node", str(root / path)], cwd
    if runner == "pwsh":
        return ["pwsh", "-NoProfile", "-File", str(root / path)], cwd
    if runner == "npm":
        return ["npm"], cwd
    if runner == "gradle":
        candidate = root / path
        if not candidate.is_file() and path.lower().endswith(".bat"):
            candidate = root / "gradlew"
        return [str(candidate)], cwd
    raise RuntimeError(f"unsupported runner: {runner}")


def verify_tool(requirement: str) -> tuple[bool, str]:
    if requirement == "java25":
        if not shutil.which("java"):
            return False, "java command missing"
        cp = subprocess.run(["java", "-version"], text=True, capture_output=True, errors="replace")
        text = cp.stdout + cp.stderr
        return (cp.returncode == 0 and re.search(r'(?:version\s+\")?25(?:[.\"\s])', text) is not None, sanitize(text))
    if requirement == "node22":
        if not shutil.which("node"):
            return False, "node command missing"
        cp = subprocess.run(["node", "--version"], text=True, capture_output=True, errors="replace")
        return (cp.returncode == 0 and cp.stdout.strip().lstrip("v").startswith("22."), sanitize(cp.stdout + cp.stderr))
    return False, f"unknown requiredTool={requirement}"


def validate_plan(root: Path, plan: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    stages = plan.get("orderedStages")
    commands = plan.get("commands")
    if not isinstance(stages, list) or not stages or len(stages) != len(set(stages)):
        errors.append("orderedStages must be non-empty and unique")
        return errors
    if not isinstance(commands, list) or not commands:
        errors.append("commands must be non-empty")
        return errors
    seen_ids: set[str] = set()
    last_stage = -1
    signatures: set[tuple[str, str, tuple[str, ...], str]] = set()
    for item in commands:
        if not isinstance(item, dict):
            errors.append("command row must be object")
            continue
        cid = str(item.get("id", ""))
        if not cid or cid in seen_ids:
            errors.append(f"duplicate/blank command id: {cid}")
        seen_ids.add(cid)
        stage = item.get("stage")
        if stage not in stages:
            errors.append(f"{cid}: unknown stage={stage}")
            continue
        index = stages.index(stage)
        if index < last_stage:
            errors.append(f"{cid}: stage order regression")
        last_stage = index
        if item.get("required") is not True:
            errors.append(f"{cid}: final commands must be required")
        args = item.get("args")
        if not isinstance(args, list) or not all(isinstance(x, str) for x in args):
            errors.append(f"{cid}: args must be string array")
            args = []
        signature = (str(stage), str(item.get("path", "")), tuple(args), str(item.get("workingDirectory", "")))
        if signature in signatures:
            errors.append(f"{cid}: duplicate expensive command signature")
        signatures.add(signature)
        runner = item.get("runner")
        if runner not in {"python", "node", "pwsh", "gradle", "npm"}:
            errors.append(f"{cid}: unsupported runner={runner}")
        if runner != "npm":
            path = root / str(item.get("path", ""))
            if runner == "gradle" and not path.is_file() and not (root / "gradlew").is_file():
                errors.append(f"{cid}: Gradle wrapper missing")
            elif runner != "gradle" and not path.is_file():
                errors.append(f"{cid}: executable missing={item.get('path')}")
    return errors


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path.cwd())
    ap.add_argument("--expected-sha", required=True)
    ap.add_argument("--evidence-dir", type=Path, required=True)
    ap.add_argument("--plan", default="cpf-tools/verification/20260801_01/cpf-final-verification-plan.json")
    ap.add_argument("--release", action="store_true")
    ap.add_argument("--allow-environment-blockers", action="store_true")
    args = ap.parse_args()
    root = args.root.resolve()
    evidence = args.evidence_dir.resolve()
    if not SHA_RE.fullmatch(args.expected_sha):
        print("[FAIL] expected SHA format", file=sys.stderr)
        return 1
    if evidence == root or root in evidence.parents:
        print("[FAIL] evidence-dir must be outside repository", file=sys.stderr)
        return 1
    plan_path = root / args.plan
    try:
        plan = json.loads(plan_path.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"[FAIL] invalid plan: {exc}", file=sys.stderr)
        return 1
    plan_errors = validate_plan(root, plan)
    if plan_errors:
        for error in plan_errors:
            print(f"[FAIL] {error}", file=sys.stderr)
        return 1
    try:
        actual_sha = git(root, "rev-parse", "HEAD")
        if actual_sha != args.expected_sha:
            raise RuntimeError(f"exact SHA mismatch expected={args.expected_sha} actual={actual_sha}")
        dirty = clean_status(root)
        if dirty:
            raise RuntimeError("working tree must be clean before final plan: " + "; ".join(dirty[:20]))
    except RuntimeError as exc:
        print(f"[FAIL] {exc}", file=sys.stderr)
        return 1

    evidence.mkdir(parents=True, exist_ok=True)
    started = dt.datetime.now(dt.timezone.utc)
    results: list[dict[str, Any]] = []
    failures: list[str] = []
    blockers: list[str] = []
    executed_ids: set[str] = set()

    for command in plan["commands"]:
        cid = command["id"]
        if cid in executed_ids:
            failures.append(f"duplicate execution attempted: {cid}")
            break
        executed_ids.add(cid)
        step_start = dt.datetime.now(dt.timezone.utc)
        result: dict[str, Any] = {
            "id": cid, "stage": command["stage"], "startedAt": step_start.isoformat(),
            "status": "PENDING", "exitCode": None, "command": None,
        }
        required_tool = command.get("requiredTool")
        if required_tool:
            ok, detail = verify_tool(str(required_tool))
            result["requiredTool"] = required_tool
            result["toolCheck"] = detail
            if not ok:
                msg = f"{cid}: required tool unavailable: {required_tool}"
                blockers.append(msg)
                result.update(status="ENVIRONMENT_BLOCKER", exitCode=125, finishedAt=dt.datetime.now(dt.timezone.utc).isoformat())
                results.append(result)
                continue
        missing_env = [name for name in command.get("requiredEnvironment", []) if not os.environ.get(name)]
        if missing_env:
            msg = f"{cid}: required environment missing: {','.join(missing_env)}"
            blockers.append(msg)
            result.update(status="ENVIRONMENT_BLOCKER", exitCode=125, missingEnvironment=missing_env, finishedAt=dt.datetime.now(dt.timezone.utc).isoformat())
            results.append(result)
            continue
        try:
            prefix, cwd = executable_for(command, root)
            if not cwd.is_dir():
                raise RuntimeError(f"working directory missing: {cwd}")
            pre_args = [replace_tokens(x, root, actual_sha, evidence) for x in command.get("preArgs", [])]
            args_list = [replace_tokens(x, root, actual_sha, evidence) for x in command.get("args", [])]
            if args.release:
                args_list += [replace_tokens(x, root, actual_sha, evidence) for x in command.get("releaseArgs", [])]
            if pre_args:
                pre_command = prefix + pre_args
                code, out, err = run_capture(pre_command, cwd)
                result["preCommand"] = pre_command
                result["preStdout"] = out
                result["preStderr"] = err
                if code:
                    raise RuntimeError(f"pre-command failed(exit={code})")
            full_command = prefix + args_list
            result["command"] = full_command
            code, out, err = run_capture(full_command, cwd)
            result["stdout"] = out
            result["stderr"] = err
            result["exitCode"] = code
            if code:
                raise RuntimeError(f"command failed(exit={code})")
            if git(root, "rev-parse", "HEAD") != actual_sha:
                raise RuntimeError("source SHA changed during validation")
            dirty = clean_status(root)
            if dirty:
                raise RuntimeError("validation changed working tree: " + "; ".join(dirty[:20]))
            result["status"] = "PASS"
        except Exception as exc:
            result["status"] = "FAIL"
            if result.get("exitCode") is None:
                result["exitCode"] = 1
            result["error"] = sanitize(str(exc))
            failures.append(f"{cid}: {sanitize(str(exc))}")
        result["finishedAt"] = dt.datetime.now(dt.timezone.utc).isoformat()
        results.append(result)
        if failures:
            break

    environment_blockers_are_failures = args.release or not args.allow_environment_blockers
    final_failures = list(failures)
    if blockers and environment_blockers_are_failures:
        final_failures.extend(blockers)
    final_dirty: list[str] = []
    try:
        if git(root, "rev-parse", "HEAD") != actual_sha:
            final_failures.append("final source SHA changed")
        final_dirty = clean_status(root)
        if final_dirty:
            final_failures.append("final working tree is dirty")
    except RuntimeError as exc:
        final_failures.append(str(exc))
    summary = {
        "schemaVersion": 1,
        "planId": plan.get("planId"),
        "sourceSha": actual_sha,
        "resultSha": actual_sha if not final_failures else None,
        "startedAt": started.isoformat(),
        "finishedAt": dt.datetime.now(dt.timezone.utc).isoformat(),
        "exitCode": 0 if not final_failures else 1,
        "release": args.release,
        "sanitized": True,
        "commandCount": len(plan["commands"]),
        "executedCommandCount": sum(1 for x in results if x["status"] in {"PASS", "FAIL"}),
        "environmentBlockerCount": len(blockers),
        "failures": final_failures,
        "environmentBlockers": blockers,
        "finalDirtyEntries": final_dirty,
        "steps": results,
    }
    output = evidence / "cpf-final-verification-result.sanitized.json"
    output.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if final_failures:
        print(f"[FAIL] CPF final verification plan evidence={output}", file=sys.stderr)
        return 1
    print(f"[PASS] CPF final verification plan commands={len(results)} evidence={output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
