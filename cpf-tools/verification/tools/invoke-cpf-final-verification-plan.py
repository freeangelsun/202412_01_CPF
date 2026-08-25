#!/usr/bin/env python3
"""Execute the canonical CPF final verification plan once per command.

The executor supports both a Git checkout and the user's Local Working Tree ZIP replay.
It does not require a clean Git tree. Instead it computes a deterministic source snapshot
before and after validation and fails if validation mutates source bytes. A Git SHA may be
supplied as additional release evidence, but it is never used to replace the Local Source
identity.
"""
from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Any

GIT_SHA_RE = re.compile(r"^[0-9a-f]{40}$")
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
SECRET_RE = re.compile(r"(?i)(password|secret|token|authorization|cookie|credential)\s*[:=]\s*([^\s,;]+)")
GENERATED_PARTS = {".git", ".gradle", ".idea", ".pytest_cache", "__pycache__", "node_modules", "dist", ".vite", "playwright-report", "test-results", "target", "out", "coverage"}


def sanitize(value: str) -> str:
    value = SECRET_RE.sub(lambda m: f"{m.group(1)}=***", value)
    value = re.sub(r"(?i)bearer\s+[A-Za-z0-9._~+/=-]+", "Bearer ***", value)
    return value[-20000:]


def run_capture(command: list[str], cwd: Path, env: dict[str, str] | None = None) -> tuple[int, str, str]:
    cp = subprocess.run(command, cwd=cwd, env=env, text=True, capture_output=True, errors="replace")
    return cp.returncode, sanitize(cp.stdout), sanitize(cp.stderr)


def git_optional(root: Path, *args: str) -> str | None:
    if not shutil.which("git") or not (root / ".git").exists():
        return None
    cp = subprocess.run(["git", "-C", str(root), *args], text=True, capture_output=True, errors="replace")
    return cp.stdout.strip() if cp.returncode == 0 else None


def source_snapshot(root: Path) -> dict[str, Any]:
    digest = hashlib.sha256()
    files = 0
    total = 0
    for current, dirs, names in os.walk(root):
        current_path = Path(current)
        rel_dir = current_path.relative_to(root).as_posix() if current_path != root else ""
        keep: list[str] = []
        for name in dirs:
            rel = f"{rel_dir}/{name}".lstrip("/")
            if name in GENERATED_PARTS:
                continue
            if name == "build" and rel != "cpf-tools/build":
                continue
            # Eclipse/IDE module-root compiled output is generated, but templates/bin/ is
            # checked-in product source (customer-facing CLI template scripts), matching
            # cpf-source-state.py's identical exception.
            if name == "bin" and Path(rel).parent.name != "templates":
                continue
            keep.append(name)
        dirs[:] = keep
        for name in names:
            path = current_path / name
            rel = path.relative_to(root).as_posix()
            if name == ".coverage" or rel.startswith(".vscode/"):
                continue
            h = hashlib.sha256()
            with path.open("rb") as handle:
                for chunk in iter(lambda: handle.read(1024 * 1024), b""):
                    h.update(chunk)
            size = path.stat().st_size
            digest.update(f"{rel}|{size}|{h.hexdigest()}\n".encode("utf-8"))
            files += 1
            total += size
    return {"contentSha256": digest.hexdigest(), "fileCount": files, "totalBytes": total}


def replace_tokens(value: str, root: Path, git_sha: str, source_sha: str, baseline_zip_sha: str, evidence: Path) -> str:
    return (value.replace("{root}", str(root))
            .replace("{sha}", git_sha)
            .replace("{sourceSha256}", source_sha)
            .replace("{baselineSourceZipSha256}", baseline_zip_sha)
            .replace("{evidence}", str(evidence)))


def executable_for(command: dict[str, Any], root: Path) -> tuple[list[str], Path]:
    runner = command["runner"]
    path = str(command["path"])
    cwd = root / str(command.get("workingDirectory", ""))
    if runner == "python": return [sys.executable, str(root / path)], cwd
    if runner == "node": return ["node", str(root / path)], cwd
    if runner == "pwsh": return ["pwsh", "-NoProfile", "-File", str(root / path)], cwd
    if runner == "npm": return ["npm"], cwd
    if runner == "gradle":
        candidate = root / path
        if not candidate.is_file() and path.lower().endswith(".bat"):
            candidate = root / "gradlew"
        return [str(candidate)], cwd
    raise RuntimeError(f"unsupported runner: {runner}")


def verify_tool(requirement: str) -> tuple[bool, str]:
    if requirement == "java25":
        if not shutil.which("java"): return False, "java command missing"
        cp = subprocess.run(["java", "-version"], text=True, capture_output=True, errors="replace")
        text = cp.stdout + cp.stderr
        return (cp.returncode == 0 and re.search(r'(?:version\s+\")?25(?:[.\"\s])', text) is not None, sanitize(text))
    if requirement == "node22":
        if not shutil.which("node") or not shutil.which("npm"): return False, "node/npm command missing"
        node = subprocess.run(["node", "--version"], text=True, capture_output=True, errors="replace")
        npm = subprocess.run(["npm", "--version"], text=True, capture_output=True, errors="replace")
        try:
            major, minor, *_ = [int(x) for x in node.stdout.strip().lstrip("v").split(".")]
            node_ok = (major == 22 and minor >= 18) or (23 <= major < 25)
        except Exception:
            node_ok = False
        npm_ok = npm.returncode == 0 and npm.stdout.strip() == "10.9.2"
        detail = f"node={node.stdout.strip()} npm={npm.stdout.strip()}"
        return node.returncode == 0 and node_ok and npm_ok, detail
    return False, f"unknown requiredTool={requirement}"


def validate_plan(root: Path, plan: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    stages = plan.get("orderedStages"); commands = plan.get("commands")
    if not isinstance(stages, list) or not stages or len(stages) != len(set(stages)):
        return ["orderedStages must be non-empty and unique"]
    if not isinstance(commands, list) or not commands:
        return ["commands must be non-empty"]
    seen_ids: set[str] = set(); signatures: set[tuple[str, str, tuple[str, ...], str]] = set(); last_stage = -1
    for item in commands:
        if not isinstance(item, dict): errors.append("command row must be object"); continue
        cid = str(item.get("id", ""))
        if not cid or cid in seen_ids: errors.append(f"duplicate/blank command id: {cid}")
        seen_ids.add(cid)
        stage = item.get("stage")
        if stage not in stages: errors.append(f"{cid}: unknown stage={stage}"); continue
        index = stages.index(stage)
        if index < last_stage: errors.append(f"{cid}: stage order regression")
        last_stage = index
        if item.get("required") is not True: errors.append(f"{cid}: final commands must be required")
        argv = item.get("args")
        if not isinstance(argv, list) or not all(isinstance(x, str) for x in argv): errors.append(f"{cid}: args must be string array"); argv = []
        signature = (str(stage), str(item.get("path", "")), tuple(argv), str(item.get("workingDirectory", "")))
        if signature in signatures: errors.append(f"{cid}: duplicate expensive command signature")
        signatures.add(signature)
        runner = item.get("runner")
        if runner not in {"python", "node", "pwsh", "gradle", "npm"}: errors.append(f"{cid}: unsupported runner={runner}")
        if runner != "npm":
            path = root / str(item.get("path", ""))
            if runner == "gradle" and not path.is_file() and not (root / "gradlew").is_file(): errors.append(f"{cid}: Gradle wrapper missing")
            elif runner != "gradle" and not path.is_file(): errors.append(f"{cid}: executable missing={item.get('path')}")
    return errors


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path.cwd())
    ap.add_argument("--expected-sha", help="Optional Git commit reference evidence; never replaces Local Source identity")
    ap.add_argument("--expected-source-sha256", help="Optional expected pre-validation content SHA-256")
    ap.add_argument("--baseline-source-zip-sha256", default=os.getenv("CPF_BASELINE_SOURCE_ZIP_SHA256", ""))
    ap.add_argument("--evidence-dir", type=Path, required=True)
    ap.add_argument("--plan", default="cpf-tools/verification/contracts/cpf-final-verification-plan.json")
    ap.add_argument("--release", action="store_true")
    ap.add_argument("--allow-environment-blockers", action="store_true")
    args = ap.parse_args()
    root = args.root.resolve(); evidence = args.evidence_dir.resolve()
    if args.expected_sha and not GIT_SHA_RE.fullmatch(args.expected_sha): print("[FAIL] expected SHA format", file=sys.stderr); return 1
    if args.expected_source_sha256 and not SHA256_RE.fullmatch(args.expected_source_sha256): print("[FAIL] expected source SHA-256 format", file=sys.stderr); return 1
    if args.baseline_source_zip_sha256 and not SHA256_RE.fullmatch(args.baseline_source_zip_sha256): print("[FAIL] baseline source ZIP SHA-256 format", file=sys.stderr); return 1
    if evidence == root or root in evidence.parents: print("[FAIL] evidence-dir must be outside repository", file=sys.stderr); return 1
    try: plan = json.loads((root / args.plan).read_text(encoding="utf-8"))
    except Exception as exc: print(f"[FAIL] invalid plan: {exc}", file=sys.stderr); return 1
    for error in validate_plan(root, plan): print(f"[FAIL] {error}", file=sys.stderr)
    if validate_plan(root, plan): return 1

    git_sha = git_optional(root, "rev-parse", "HEAD") or ""
    if args.expected_sha and git_sha != args.expected_sha:
        print(f"[FAIL] exact SHA mismatch expected={args.expected_sha} actual={git_sha or 'NO_GIT'}", file=sys.stderr); return 1
    source_before = source_snapshot(root)
    if args.expected_source_sha256 and source_before["contentSha256"] != args.expected_source_sha256:
        print(f"[FAIL] source identity mismatch expected={args.expected_source_sha256} actual={source_before['contentSha256']}", file=sys.stderr); return 1

    evidence.mkdir(parents=True, exist_ok=True)
    started = dt.datetime.now(dt.timezone.utc); results: list[dict[str, Any]] = []; failures: list[str] = []; blockers: list[str] = []; executed_ids: set[str] = set()
    for command in plan["commands"]:
        cid = command["id"]
        if cid in executed_ids: failures.append(f"duplicate execution attempted: {cid}"); break
        executed_ids.add(cid); step_start = dt.datetime.now(dt.timezone.utc)
        result: dict[str, Any] = {"id": cid, "stage": command["stage"], "startedAt": step_start.isoformat(), "status": "PENDING", "exitCode": None, "command": None}
        required_tool = command.get("requiredTool")
        if required_tool:
            ok, detail = verify_tool(str(required_tool)); result["requiredTool"] = required_tool; result["toolCheck"] = detail
            if not ok:
                msg = f"{cid}: required tool unavailable/incompatible: {required_tool} ({detail})"; blockers.append(msg)
                result.update(status="ENVIRONMENT_BLOCKER", exitCode=125, finishedAt=dt.datetime.now(dt.timezone.utc).isoformat()); results.append(result); continue
        missing_env = [name for name in command.get("requiredEnvironment", []) if not os.environ.get(name)]
        if missing_env:
            msg = f"{cid}: required environment missing: {','.join(missing_env)}"; blockers.append(msg)
            result.update(status="ENVIRONMENT_BLOCKER", exitCode=125, missingEnvironment=missing_env, finishedAt=dt.datetime.now(dt.timezone.utc).isoformat()); results.append(result); continue
        try:
            prefix, cwd = executable_for(command, root)
            if not cwd.is_dir(): raise RuntimeError(f"working directory missing: {cwd}")
            tokens = lambda x: replace_tokens(x, root, git_sha, source_before["contentSha256"], args.baseline_source_zip_sha256, evidence)
            pre_args = [tokens(x) for x in command.get("preArgs", [])]; args_list = [tokens(x) for x in command.get("args", [])]
            if args.release: args_list += [tokens(x) for x in command.get("releaseArgs", [])]
            if pre_args:
                pre_command = prefix + pre_args; code, out, err = run_capture(pre_command, cwd); result.update(preCommand=pre_command, preStdout=out, preStderr=err)
                if code: raise RuntimeError(f"pre-command failed(exit={code})")
            full_command = prefix + args_list; result["command"] = full_command; code, out, err = run_capture(full_command, cwd)
            result.update(stdout=out, stderr=err, exitCode=code)
            if code: raise RuntimeError(f"command failed(exit={code})")
            current = source_snapshot(root)
            if current["contentSha256"] != source_before["contentSha256"]: raise RuntimeError("validation changed source bytes")
            result["status"] = "PASS"
        except Exception as exc:
            result["status"] = "FAIL"; result["exitCode"] = result.get("exitCode") if result.get("exitCode") is not None else 1; result["error"] = sanitize(str(exc)); failures.append(f"{cid}: {sanitize(str(exc))}")
        result["finishedAt"] = dt.datetime.now(dt.timezone.utc).isoformat(); results.append(result)
        if failures: break

    source_after = source_snapshot(root); stable = source_after["contentSha256"] == source_before["contentSha256"]
    environment_blockers_are_failures = args.release or not args.allow_environment_blockers
    final_failures = list(failures)
    if blockers and environment_blockers_are_failures: final_failures.extend(blockers)
    if not stable: final_failures.append("final source content identity changed")
    summary = {
        "schemaVersion": 2, "planId": plan.get("planId"), "gitSha": git_sha or None,
        "baselineSourceZipSha256": args.baseline_source_zip_sha256 or None,
        "sourceContentSha256Before": source_before["contentSha256"], "sourceContentSha256After": source_after["contentSha256"],
        "sourceFileCount": source_before["fileCount"], "sourceStable": stable,
        "startedAt": started.isoformat(), "finishedAt": dt.datetime.now(dt.timezone.utc).isoformat(),
        "exitCode": 0 if not final_failures else 1, "release": args.release, "sanitized": True,
        "commandCount": len(plan["commands"]), "executedCommandCount": sum(1 for x in results if x["status"] in {"PASS", "FAIL"}),
        "environmentBlockerCount": len(blockers), "failures": final_failures, "environmentBlockers": blockers, "steps": results,
    }
    output = evidence / "cpf-final-verification-result.sanitized.json"; output.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if final_failures: print(f"[FAIL] CPF final verification plan evidence={output}", file=sys.stderr); return 1
    print(f"[PASS] CPF final verification plan commands={len(results)} evidence={output}"); return 0


if __name__ == "__main__": raise SystemExit(main())
