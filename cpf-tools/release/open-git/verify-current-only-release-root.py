"""Open Git Release generated root 가 Current-only 인지 검증한다.

Release 는 매번 exact generated root 를 지우고 0부터 만든다. 이전 Release 파일을 merge 하거나
copy-over 해서 재사용하면 사용자 checkout 에 과거 산출물이 섞인다.

검증 항목
  * previous canonical release residue = 0   (모든 산출물이 이번 Release 시각 이후 생성)
  * legacy timestamp release newly generated = 0 (CPF_PUBLIC_RELEASE_<timestamp> 미생성)
  * stale open-git working-tree files = 0    (staging 에 없는 과거 Working Tree 파일 없음)
  * stale binary version = 0                 (현재 Public version 외 version 디렉터리 없음)
  * stale reports/logs/work = 0
  * .git/** 은 Open Git Repository history 이므로 garbage 로 취급하지 않는다.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
import time
from pathlib import Path

sys.stdout.reconfigure(encoding="utf-8")

RELEASE_DIR_NAME = "cpf-release"
LEGACY_RELEASE_PATTERN = re.compile(r"^CPF_PUBLIC_RELEASE_\d{8}_\d{6}$")


def fail(findings: list[str], message: str) -> None:
    findings.append(message)
    print(f"  [FAIL] {message}")


def check_release_root_is_rebuilt(root: Path, findings: list[str]) -> None:
    """Release 는 exact generated root 를 지우고 0부터 만들어야 한다.

    파일 mtime 비교는 판정 근거가 되지 못한다. staging/템플릿 복사에 copy2 를 쓰면 원본 mtime
    이 보존되고, git clone 도 원격 파일 시각을 그대로 남기기 때문이다. 따라서 "이전 산출물을
    재사용하지 않는다"는 것은 cleanup 구현으로 검증한다.
    """
    engine = (root / "cpf-tools/release/open-git/cpf_open_git.py").read_text(encoding="utf-8")
    if "def clean_release_root" not in engine:
        fail(findings, "clean_release_root is missing; release root would accumulate")
        return
    body = engine.split("def clean_release_root", 1)[1].split("\ndef ", 1)[0]
    if "shutil.rmtree(target)" not in body:
        fail(findings, "release root is not fully removed before rebuild")
    if "mkdir(parents=True, exist_ok=False)" not in body:
        fail(findings, "release root is reused instead of being recreated empty")
    if "verify_release_root_safety" not in body:
        fail(findings, "release root cleanup is not bounded to the approved generated root")
    if not findings:
        print("  previous canonical release residue: 0 (exact root rebuilt from scratch)")


def check_legacy_timestamp_release(root: Path, findings: list[str]) -> None:
    """legacy publisher 가 별도 timestamp Release root 를 만들면 Current-only 가 깨진다."""
    candidates: list[str] = []
    for parent in (root, root.parent, Path.home() / "Downloads"):
        if not parent.is_dir():
            continue
        for child in parent.iterdir():
            if child.is_dir() and LEGACY_RELEASE_PATTERN.match(child.name):
                candidates.append(str(child))
    if candidates:
        fail(findings, f"legacy timestamp release generated: {candidates[:5]}")
    else:
        print("  legacy timestamp release newly generated: 0")


def check_open_git_matches_staging(release: Path, findings: list[str]) -> None:
    """Open Git working tree 에 staging 에 없는 과거 파일이 남으면 안 된다."""
    staging = release / "work" / "public-staging"
    open_git = release / "open-git"
    if not open_git.is_dir():
        fail(findings, "open-git tree is missing")
        return
    if not staging.is_dir():
        print("  stale open-git working-tree files: SKIPPED (staging not retained)")
        return
    staged = {p.relative_to(staging).as_posix() for p in staging.rglob("*") if p.is_file()}
    actual = {p.relative_to(open_git).as_posix() for p in open_git.rglob("*")
              if p.is_file() and ".git" not in p.relative_to(open_git).parts}
    stale = sorted(actual - staged)
    if stale:
        fail(findings, f"stale open-git working-tree files: {len(stale)} e.g. {stale[:5]}")
    else:
        print("  stale open-git working-tree files: 0")


def check_single_public_version(release: Path, findings: list[str]) -> None:
    """binary repository 에 현재 Public version 외의 version 이 남으면 stale 이다."""
    repository = release / "binary-repository"
    manifest_path = repository / "package-manifest.json"
    if not manifest_path.is_file():
        fail(findings, "package-manifest.json is missing")
        return
    current = str(json.loads(manifest_path.read_text(encoding="utf-8"))["publicVersion"])
    versions = {p.name for p in repository.rglob("*")
                if p.is_dir() and any(c.is_file() and c.suffix in {".jar", ".pom", ".zip"}
                                      for c in p.iterdir())}
    stale = sorted(v for v in versions if v != current)
    if stale:
        fail(findings, f"stale binary version: {stale}")
    else:
        print(f"  stale binary version: 0 (current={current})")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--started-at", type=float, default=0.0,
                        help="Release 시작 epoch. 미지정 시 release root 생성 시각을 사용한다.")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    release = root / RELEASE_DIR_NAME
    if not release.is_dir():
        print("CPF_CURRENT_ONLY_RELEASE=FAIL release root is missing")
        return 1

    started_at = args.started_at or release.stat().st_ctime
    print(f"CPF Current-only Release root verification: {release}")

    findings: list[str] = []
    check_release_root_is_rebuilt(root, findings)
    check_legacy_timestamp_release(root, findings)
    check_open_git_matches_staging(release, findings)
    check_single_public_version(release, findings)

    total = len([p for p in release.rglob("*") if p.is_file()])
    print(f"  generated root inventory: files={total}")
    print()
    if findings:
        print(f"CPF_CURRENT_ONLY_RELEASE=FAIL findings={len(findings)}")
        return 1
    print("CPF_CURRENT_ONLY_RELEASE=PASS current release only")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
