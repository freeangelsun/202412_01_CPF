#!/usr/bin/env python3
"""Master tracking 예외로 둔 Release Artifact 를 tracked manifest 로 남긴다.

파일 자체를 Master 에 두지 않더라도 무엇이 어떤 근거로 빠졌는지는 Master 에서 확인할 수 있어야 한다.
각 Artifact 의 경로 / 좌표 / 버전 / 실측 크기 / SHA-256 / Source Identity / 공개 여부 / 예외 사유를 남긴다.

분류는 확장자가 아니라 canonical Runtime Target Catalog 의 provision=binary 에서 파생한다.
분류할 수 없는 Artifact 는 UNKNOWN 이며 FAIL CLOSED 다.

정본 계약: cpf-tools/release/open-git/open-git-surface-policy.json#releaseAssetPolicy.artifactClassification
Harness Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §39
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import io
import json
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

POLICY_REL = "cpf-tools/release/open-git/open-git-surface-policy.json"
CATALOG_REL = "cpf-tools/runtime/cpf-runtime-target-catalog.json"
REPOSITORY_REL = "cpf-release/binary-repository"
RUNTIME_GROUP = "com/cpf/runtime"

METADATA_SUFFIXES = (".pom", ".sha256", ".sha512", ".md5", ".json", ".module", ".xml", ".asc")


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def binary_artifact_ids(root: Path) -> set[str]:
    """Public Binary Runtime 의 artifactId. 이름 목록을 이 도구가 들고 있지 않는다."""
    catalog = read_json(root / CATALOG_REL)
    return {str(entry.get("artifactId", "")).strip()
            for entry in catalog.get("runtimes", [])
            if entry.get("provision") == "binary" and str(entry.get("artifactId", "")).strip()}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def source_identity(root: Path) -> str:
    identity = root / "cpf-docs/governance/development-harness/current/SOURCE_IDENTITY.json"
    if not identity.is_file():
        return "UNKNOWN"
    return str(read_json(identity).get("contentSha256") or "UNKNOWN")


def classified_by_rule(classification: dict, posix: str, name: str) -> bool:
    """분류 규칙이 이 Artifact 를 덮고 있는가. 확장자 자체로 통과시키지 않는다."""
    for rule in classification["rules"]:
        root = str(rule["match"].get("root", ""))
        if not root.startswith("cpf-release/binary-repository"):
            continue
        prefix = root[len("cpf-release/binary-repository/"):] if "/" in root[len("cpf-release/binary-repository"):] else ""
        if prefix and not posix.startswith(prefix + "/"):
            continue
        kind = str(rule["match"].get("fileKind", ""))
        if kind == "generator-distribution-archive" and name.endswith(".zip"):
            return True
    return False


def coordinate_of(relative: Path) -> tuple[str, str]:
    """Maven 좌표와 버전. 경로 구조가 곧 좌표다."""
    parts = relative.parts
    if len(parts) < 3:
        return "", ""
    version = parts[-2]
    artifact = parts[-3]
    group = ".".join(parts[:-3])
    return f"{group}:{artifact}", version


def main() -> int:
    parser = argparse.ArgumentParser(description="Release Artifact tracking manifest")
    parser.add_argument("--root", default=".")
    parser.add_argument("--out")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    policy = read_json(root / POLICY_REL)
    classification = policy.get("releaseAssetPolicy", {}).get("artifactClassification")
    if not classification:
        print(json.dumps({"status": "FAIL", "message": "artifactClassification 정본이 없다"},
                         ensure_ascii=False))
        return 1

    executable_rule = next((rule for rule in classification["rules"]
                            if rule["id"] == "publicBinaryRuntimeExecutable"), None)
    if not executable_rule:
        print(json.dumps({"status": "FAIL", "message": "publicBinaryRuntimeExecutable 분류가 없다"},
                         ensure_ascii=False))
        return 1

    repository = root / REPOSITORY_REL
    if not repository.is_dir():
        print(json.dumps({"status": "SKIP", "message": "Release Binary Repository 가 아직 없다",
                          "repository": REPOSITORY_REL}, ensure_ascii=False))
        return 0

    artifact_ids = binary_artifact_ids(root)
    identity = source_identity(root)
    rows: list[dict[str, str]] = []
    unknown: list[str] = []

    for path in sorted(repository.rglob("*")):
        if not path.is_file():
            continue
        relative = path.relative_to(repository)
        posix = relative.as_posix()
        if path.name.endswith(METADATA_SUFFIXES):
            continue
        if not path.name.endswith(".jar"):
            if not classified_by_rule(classification, posix, path.name):
                unknown.append(posix)
            continue
        if not posix.startswith(RUNTIME_GROUP + "/"):
            continue
        coordinate, version = coordinate_of(relative)
        artifact = coordinate.split(":")[-1]
        if artifact not in artifact_ids:
            continue
        if path.name.endswith("-plain.jar"):
            continue
        size = path.stat().st_size
        rows.append({
            "artifact_path": REPOSITORY_REL + "/" + posix,
            "coordinate": coordinate,
            "version": version,
            "size_bytes": str(size),
            "sha256": sha256(path),
            "source_identity": identity,
            "master_tracked": "false",
            "public_release": "true",
            "asset_class": executable_rule["assetClass"],
            "tracking_exception_reason": executable_rule["trackingExceptionReason"],
        })

    if args.out and rows:
        out = Path(args.out)
        out.parent.mkdir(parents=True, exist_ok=True)
        with io.open(out, "w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()), lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)

    total = sum(int(row["size_bytes"]) for row in rows)
    print(json.dumps({
        "status": "FAIL" if unknown else "PASS",
        "untrackedExecutableCount": len(rows),
        "untrackedTotalBytes": total,
        "unknownArtifact": unknown,
        "out": args.out or "",
    }, ensure_ascii=False))
    return 1 if unknown else 0


if __name__ == "__main__":
    raise SystemExit(main())
