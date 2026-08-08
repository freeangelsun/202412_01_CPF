#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SHA_RE = re.compile(r"(?<![0-9a-f])[0-9a-f]{40}(?![0-9a-f])", re.I)
CURRENT_ROOTS = (
    "cpf-docs/work/v9i/dev-final",
    "cpf-tools/verification/final-dev",
)
TEXT_SUFFIXES = {".md", ".csv", ".json", ".txt", ".log", ".exit", ".py", ".ps1", ".sh"}
CURRENT_SHA_KEYS = {
    "productsourcesha", "product_source_sha", "sourcesha", "source_sha",
    "resultsha", "result_sha", "executionsha", "execution_sha",
    "checkoutsha", "checkout_sha", "targetsha", "target_sha",
}
BASIS_SHA_KEYS = {"basissha", "basis_sha", "development_basis_sha", "basis_checkout_sha"}
PENDING_VALUES = {"", "pending", "pending_successor_sha", "uncommitted_overlay", "null", "none", "n/a", "미검증"}


class E(RuntimeError):
    pass


def req(value, message):
    if not value:
        raise E(message)


def git_head(root: Path) -> str:
    try:
        return subprocess.check_output(
            ["git", "-C", str(root), "rev-parse", "HEAD"], text=True
        ).strip().lower()
    except FileNotFoundError as exc:
        raise E("git unavailable") from exc
    except subprocess.CalledProcessError as exc:
        raise E("git HEAD unavailable") from exc


def normalize_key(key: str) -> str:
    return key.lower().replace("-", "_").replace(" ", "")


def iter_current_files(root: Path):
    for rel in CURRENT_ROOTS:
        base = root / rel
        if not base.exists():
            continue
        for path in base.rglob("*"):
            if path.is_file() and path.suffix.lower() in TEXT_SUFFIXES:
                yield path


def allowed_historical_line(line: str) -> bool:
    lower = line.lower()
    return any(tag in lower for tag in (
        "historical", "previous", "predecessor", "history", "과거", "qa audited basis",
        "qa basis", "source baseline", "prior sha", "previous sha"
    ))


def inspect_json(path: Path, expected: str, mode: str):
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception:
        return
    errors: list[str] = []

    def walk(value, key=""):
        if isinstance(value, dict):
            for k, v in value.items():
                walk(v, str(k))
        elif isinstance(value, list):
            for v in value:
                walk(v, key)
        elif isinstance(value, str):
            nk = normalize_key(key)
            candidate = value.strip().lower()
            if nk in BASIS_SHA_KEYS:
                if SHA_RE.fullmatch(candidate) and candidate != expected:
                    errors.append(f"basis {key}={candidate}")
            elif nk in CURRENT_SHA_KEYS:
                if mode == "post-apply":
                    if not SHA_RE.fullmatch(candidate) or candidate != expected:
                        errors.append(f"current {key}={candidate}")
                elif mode == "pre-push-overlay":
                    # A future successor SHA cannot be known before the overlay is applied/committed.
                    if SHA_RE.fullmatch(candidate) and candidate != expected:
                        errors.append(f"invented successor/current {key}={candidate}")

    walk(data)
    req(not errors, f"provenance mismatch in {path}: {', '.join(errors)}")


def inspect_text(path: Path, expected: str, mode: str):
    text = path.read_text(encoding="utf-8-sig", errors="ignore")
    for line_no, line in enumerate(text.splitlines(), 1):
        normalized = line.lower()
        compact = normalize_key(normalized)
        basis_line = any(k in compact for k in BASIS_SHA_KEYS)
        current_line = any(k in compact for k in CURRENT_SHA_KEYS)
        if not basis_line and not current_line:
            continue
        if allowed_historical_line(normalized):
            continue
        shas = [s.lower() for s in SHA_RE.findall(normalized)]
        if basis_line:
            for sha in shas:
                if sha != expected:
                    raise E(f"basis provenance mismatch {path}:{line_no} sha={sha}")
        if current_line:
            if mode == "post-apply":
                for sha in shas:
                    if sha != expected:
                        raise E(f"current provenance mismatch {path}:{line_no} sha={sha}")
            else:
                # Pre-push documents may name the development basis but must not claim a different successor SHA.
                for sha in shas:
                    if sha != expected:
                        raise E(f"invented successor provenance {path}:{line_no} sha={sha}")


def inspect_package_provenance(root: Path, expected: str, mode: str):
    package = root / "cpf-docs/work/v9i/dev-final/PACKAGE_MANIFEST.json"
    req(package.exists(), "developer PACKAGE_MANIFEST.json missing")
    data = json.loads(package.read_text(encoding="utf-8-sig"))
    prov = data.get("provenance") if isinstance(data, dict) else None
    req(isinstance(prov, dict), "PACKAGE_MANIFEST provenance object missing")
    basis = str(prov.get("basis_checkout_sha", prov.get("development_basis_sha", ""))).lower()
    req(basis == expected, f"package basis_checkout_sha mismatch expected={expected} actual={basis}")
    req("product_source_sha" in prov, "package product_source_sha missing")
    req("execution_sha" in prov, "package execution_sha missing")
    req("packaging_recording_sha" in prov, "package packaging_recording_sha missing")
    status = str(prov.get("post_apply_exact_sha_status", "")).strip()
    if mode == "pre-push-overlay":
        for key in ("product_source_sha", "execution_sha", "packaging_recording_sha"):
            value = prov.get(key)
            candidate = "null" if value is None else str(value).strip().lower()
            req(candidate in PENDING_VALUES or candidate == expected,
                f"pre-push {key} must be basis/pending, got={candidate}")
        req(status in {"미검증", "PENDING", "pending", "UNVERIFIED"},
            "pre-push exact-SHA status must remain 미검증/PENDING")
        req(str(prov.get("successor_sha", "")).strip().lower() in PENDING_VALUES,
            "pre-push overlay must not invent successor_sha")
    else:
        for key in ("product_source_sha", "execution_sha", "packaging_recording_sha"):
            req(str(prov.get(key, "")).lower() == expected,
                f"post-apply {key} mismatch expected={expected} actual={prov.get(key)}")
        req(str(prov.get("successor_sha", "")).lower() == expected,
            "post-apply successor_sha must equal checkout HEAD")
        req(status.upper() in {"PASS", "완료"},
            "post-apply exact-SHA status must be PASS/완료")


def inspect_release_gate(root: Path):
    release = root / "cpf-tools/verification/final-dev/run-r6-release-gates.ps1"
    req(release.exists(), "release gate missing")
    release_text = release.read_text(encoding="utf-8", errors="ignore")
    req("sourceSha=$head" in release_text, "release evidence rows are not sourceSha-bound")
    req("ExpectedHead mismatch" in release_text, "release gate does not fail closed on HEAD mismatch")


def verify(root: Path, expected: str, mode: str, actual_head: str | None = None):
    req(SHA_RE.fullmatch(expected) is not None, "expected head must be 40 hex chars")
    actual = (actual_head or git_head(root)).lower()
    req(actual == expected, f"HEAD mismatch expected={expected} actual={actual}")
    scanned = 0
    for path in iter_current_files(root):
        scanned += 1
        if path.suffix.lower() == ".json":
            inspect_json(path, expected, mode)
        inspect_text(path, expected, mode)
    req(scanned > 0, "no current evidence/result files scanned")
    inspect_package_provenance(root, expected, mode)
    inspect_release_gate(root)
    return scanned


def self_test(root: Path, expected: str):
    # Self-test does not create a project commit. It copies the current verification tree and
    # drives the pure verifier with an injected checkout head so mutation handling can be tested
    # even in an offline/sparse developer environment.
    with tempfile.TemporaryDirectory(prefix="cpf-sha-selftest-") as td:
        probe = Path(td) / "repo"
        (probe / "cpf-docs/work/v9i/dev-final").mkdir(parents=True)
        (probe / "cpf-tools/verification/final-dev").mkdir(parents=True)
        release_src = root / "cpf-tools/verification/final-dev/run-r6-release-gates.ps1"
        req(release_src.exists(), "release gate missing for self-test")
        shutil.copy2(release_src, probe / "cpf-tools/verification/final-dev/run-r6-release-gates.ps1")
        manifest = {
            "provenance": {
                "basis_checkout_sha": expected,
                "product_source_sha": expected,
                "execution_sha": expected,
                "packaging_recording_sha": expected,
                "successor_sha": expected,
                "post_apply_exact_sha_status": "PASS",
            }
        }
        (probe / "cpf-docs/work/v9i/dev-final/PACKAGE_MANIFEST.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
        (probe / "cpf-docs/work/v9i/dev-final/TEST_AND_EVIDENCE.md").write_text(
            f"source_sha={expected}\nexecution_sha={expected}\nresult_sha={expected}\n", encoding="utf-8")
        verify(probe, expected, "post-apply", actual_head=expected)

        wrong = "0" * 40 if expected != "0" * 40 else "1" * 40
        evidence = probe / "cpf-docs/work/v9i/dev-final/TEST_AND_EVIDENCE.md"
        evidence.write_text(
            f"source_sha={wrong}\nexecution_sha={expected}\nresult_sha={expected}\n", encoding="utf-8")
        try:
            verify(probe, expected, "post-apply", actual_head=expected)
        except E:
            pass
        else:
            raise E("mutation target/source SHA mismatch was not detected")

        manifest["provenance"].update({
            "product_source_sha": None,
            "execution_sha": None,
            "packaging_recording_sha": None,
            "successor_sha": None,
            "post_apply_exact_sha_status": "미검증",
        })
        (probe / "cpf-docs/work/v9i/dev-final/PACKAGE_MANIFEST.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
        evidence.write_text(f"basis_checkout_sha={expected}\n", encoding="utf-8")
        verify(probe, expected, "pre-push-overlay", actual_head=expected)

        manifest["provenance"]["successor_sha"] = wrong
        (probe / "cpf-docs/work/v9i/dev-final/PACKAGE_MANIFEST.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
        try:
            verify(probe, expected, "pre-push-overlay", actual_head=expected)
        except E:
            pass
        else:
            raise E("pre-push invented successor SHA was not detected")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path("."))
    ap.add_argument("--expected-head", required=True)
    ap.add_argument("--mode", choices=("pre-push-overlay", "post-apply"), default="post-apply")
    ap.add_argument("--self-test", action="store_true")
    ap.add_argument("--composite-basis", action="store_true", help="allow explicit expected-head as the checkout identity only for pre-push exact-SHA composite validation")
    args = ap.parse_args()
    root = args.root.resolve()
    expected = args.expected_head.lower()
    if args.self_test:
        self_test(root, expected)
        print(f"[CPF][FINAL][EVIDENCE-SHA][PASS] selfTest=true expected={expected} mutationKilled=true")
        return
    if args.composite_basis:
        req(args.mode == "pre-push-overlay", "--composite-basis is allowed only for pre-push-overlay mode")
        scanned = verify(root, expected, args.mode, actual_head=expected)
    else:
        scanned = verify(root, expected, args.mode)
    print(f"[CPF][FINAL][EVIDENCE-SHA][PASS] mode={args.mode} sourceSha={expected} scanned={scanned}")


if __name__ == "__main__":
    try:
        main()
    except E as exc:
        print("[CPF][FINAL][EVIDENCE-SHA][FAIL] " + str(exc), file=sys.stderr)
        raise SystemExit(1)
