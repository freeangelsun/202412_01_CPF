#!/usr/bin/env python3
"""Release Asset 전수 분류 보고서.

각 투영 규칙을 canonical metadata 로만 분류한다. 경로/확장자로 추론하지 않는다.
정본: cpf-tools/release/open-git/open-git-surface-policy.json 의 releaseAssetPolicy
계약: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §39
"""

from __future__ import annotations

import argparse
import csv
import io
import json
import subprocess
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

POLICY_REL = "cpf-tools/release/open-git/open-git-surface-policy.json"


def load_policy(root: Path) -> dict:
    return json.loads((root / POLICY_REL).read_text(encoding="utf-8"))


def authority(policy: dict) -> dict:
    value = policy.get("releaseAssetPolicy")
    if not value:
        raise SystemExit("releaseAssetPolicy 가 없다. Release Asset 분류 정본을 먼저 두어야 한다.")
    return value


def asset_class(rule: dict, model: dict) -> str:
    override = str(rule.get(model.get("ruleOverrideKey", "releaseAssetClass"), "")).strip()
    if override:
        return override
    mapping = model["classificationMapping"]
    classification = str(rule.get("classification", ""))
    if classification not in mapping:
        raise SystemExit(f"Release Asset 부류가 정해지지 않은 classification: {classification}")
    return mapping[classification]


def tracked_paths(root: Path) -> set[str]:
    """Development Master 가 실제로 tracking 중인 경로."""
    # 시간 제한은 이 파일에 숫자로 두지 않는다. canonical verifier runner 가 child timeout 을
    # 강제하며(--child-timeout), Release 엔진도 자기 실행 제한 안에서 이 도구를 호출한다.
    #
    # -z 를 쓰는 이유: git 은 기본적으로 non-ASCII 경로를 octal escape 로 인용해서 출력한다.
    # 그대로 비교하면 한글 경로의 산출물이 전부 "미추적" 으로 잘못 보고된다.
    try:
        done = subprocess.run(["git", "-C", str(root), "ls-files", "-z"],
                              capture_output=True)
    except Exception:  # noqa: BLE001 - git 이 없으면 tracking 상태를 비워 둔다
        return set()
    if done.returncode != 0:
        return set()
    decoded = done.stdout.decode("utf-8", errors="surrogateescape")
    return {entry for entry in decoded.split(chr(0)) if entry}


def tracking_state(origin: str, tracked: set[str]) -> str:
    """실제 Master tracking 상태.

    투영 규칙의 source 는 glob 일 수 있다. 문자열 동등 비교만 하면 tracked 인 디렉터리가
    전부 False 로 보고되어 판단을 그르친다.
    """
    if not tracked:
        return "UNKNOWN"
    if "*" in origin:
        prefix = origin.split("*", 1)[0]
        return str(any(entry.startswith(prefix) for entry in tracked))
    return str(origin in tracked)


def main() -> int:
    parser = argparse.ArgumentParser(description="Release Asset 전수 분류 보고서")
    parser.add_argument("--root", default=".", help="Development Master 경로")
    parser.add_argument("--out", help="CSV 출력 경로")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    policy = load_policy(root)
    model = authority(policy)
    classes = model["classes"]
    tracked = tracked_paths(root)

    rows: list[dict[str, str]] = []
    for section in ("sourceRules", "templateRules"):
        for rule in policy.get(section, []):
            origin = str(rule.get("source") or rule.get("pattern") or "")
            name = asset_class(rule, model)
            contract = classes[name]
            source_path = root / origin.split("*", 1)[0].rstrip("/")
            rows.append({
                "canonicalSource": origin,
                "publicTarget": str(rule.get("target", "")),
                "classification": str(rule.get("classification", "")),
                "assetClass": name,
                "masterTracked": str(contract["masterTracked"]),
                "publicRelease": str(contract["publicRelease"]),
                "releaseInputAuthority": str(contract["releaseInputAuthority"]),
                "freshRegenerationRequired": str(contract["freshRegenerationRequired"]),
                "generatorInput": str(rule.get(model.get("generatorInputKey", "generatorInput"), "")),
                "canonicalSourceExists": str(source_path.exists()),
                "masterTrackedActual": tracking_state(origin, tracked),
                "trackingExceptionReason": str(rule.get("trackingExceptionReason", "")),
            })

    # 투영 규칙 밖에서 Release 엔진이 만들어 내는 자산도 같은 표에 넣는다.
    for asset in policy.get("releaseProducedAssets", {}).get("assets", []):
        name = str(asset["releaseAssetClass"])
        contract = classes[name]
        rows.append({
            "canonicalSource": f"(produced by {asset['producedBy']})",
            "publicTarget": str(asset["path"]),
            "classification": "RELEASE_PRODUCED",
            "assetClass": name,
            "masterTracked": str(contract["masterTracked"]),
            "publicRelease": str(contract["publicRelease"]),
            "releaseInputAuthority": str(contract["releaseInputAuthority"]),
            "freshRegenerationRequired": str(contract["freshRegenerationRequired"]),
            "generatorInput": "",
            "canonicalSourceExists": "True",
            "masterTrackedActual": "UNKNOWN",
            "trackingExceptionReason": str(asset.get("note", "")),
        })

    summary: dict[str, int] = {}
    for row in rows:
        summary[row["assetClass"]] = summary.get(row["assetClass"], 0) + 1

    if args.out:
        out = Path(args.out)
        out.parent.mkdir(parents=True, exist_ok=True)
        with io.open(out, "w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()), lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)

    missing = [row["canonicalSource"] for row in rows if row["canonicalSourceExists"] == "False"]

    # 중간 산출물이 Master 에 tracking 되어 있으면 Current Release 정본이 흐려진다.
    transient_paths = [str(asset["path"]) for asset in
                       policy.get("releaseProducedAssets", {}).get("assets", [])
                       if str(asset.get("releaseAssetClass")) == "TRANSIENT_RELEASE_OUTPUT"]
    tracked_transient = sorted({path for path in transient_paths
                                for entry in tracked if entry.startswith(path.rstrip("/") + "/")})

    # Master 보존 제외는 이유를 남긴다. 이유 없는 exception 은 정책이 아니라 사고다.
    missing_reason = [row["publicTarget"] for row in rows
                      if row["assetClass"] == "UNTRACKED_RELEASE_RESULT"
                      and not row["trackingExceptionReason"].strip()]

    failures = {
        "missingCanonicalSource": missing,
        "trackedTransientOutput": tracked_transient,
        "trackingExceptionWithoutReason": missing_reason,
    }
    failed = any(failures.values())
    print(json.dumps({
        "status": "FAIL" if failed else "PASS",
        "ruleCount": len(rows),
        "byAssetClass": summary,
        **failures,
        "out": args.out or "",
    }, ensure_ascii=False))
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
