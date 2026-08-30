#!/usr/bin/env python3
"""Project catalog-owned runtime aliases into an isolated Maven repository.

Gradle cannot publish a project dependency when its target project exposes two
Maven publications with different coordinates. CPF therefore publishes one
primary component per Source project and materializes runtime aliases only after
that primary artifact set is present in the isolated staging repository.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any


class ProjectionError(RuntimeError):
    pass


PUBLIC_CLASSES = {
    "PUBLIC_COMPILE_TIME_JAVA",
    "PUBLIC_RUNTIME",
    "PUBLIC_BOM",
    "PUBLIC_TOOLING",
    "PUBLIC_STARTER",
}
PRIORITY = {
    "PUBLIC_COMPILE_TIME_JAVA": 0,
    "PUBLIC_STARTER": 1,
    "PUBLIC_RUNTIME": 2,
    "PUBLIC_TOOLING": 3,
    "PUBLIC_BOM": 4,
}
CHECKSUMS = {
    "md5": hashlib.md5,
    "sha1": hashlib.sha1,
    "sha256": hashlib.sha256,
    "sha512": hashlib.sha512,
}


def _text(node: ET.Element, name: str) -> str:
    found = node.find(name)
    return (found.text or "").strip() if found is not None else ""


def _require_child(node: ET.Element, name: str, path: Path) -> ET.Element:
    found = node.find(name)
    if found is None:
        raise ProjectionError(f"required XML element missing name={name} path={path}")
    return found


def _parse_xml(path: Path) -> ET.ElementTree:
    try:
        return ET.parse(path)
    except (OSError, ET.ParseError) as exc:
        raise ProjectionError(f"invalid Maven XML path={path}: {exc}") from exc


def _write_xml(tree: ET.ElementTree, path: Path) -> None:
    root = tree.getroot()
    if root.tag.startswith("{"):
        ET.register_namespace("", root.tag.split("}", 1)[0][1:])
    tree.write(path, encoding="utf-8", xml_declaration=True)


def _write_checksums(path: Path) -> None:
    payload = path.read_bytes()
    for suffix, constructor in CHECKSUMS.items():
        path.with_name(path.name + f".{suffix}").write_text(constructor(payload).hexdigest(), encoding="ascii")


def _coordinate_dir(repository: Path, group: str, artifact: str, version: str) -> Path:
    return repository / Path(group.replace(".", "/")) / artifact / version


def _snapshot_values(metadata_path: Path) -> tuple[str, dict[str, str]]:
    root = _parse_xml(metadata_path).getroot()
    version = _text(root, "version")
    values: dict[str, str] = {}
    for item in root.findall("./versioning/snapshotVersions/snapshotVersion"):
        classifier = _text(item, "classifier")
        extension = _text(item, "extension")
        if not classifier and extension in {"jar", "pom"}:
            values[extension] = _text(item, "value")
    if not version or set(values) != {"jar", "pom"}:
        raise ProjectionError(
            f"primary snapshot metadata must map exactly main jar+pom path={metadata_path} values={values}"
        )
    return version, values


def _primary_files(repository: Path, row: dict[str, Any], version: str) -> tuple[Path, Path, Path | None, dict[str, str]]:
    group = str(row["publicGroupId"])
    artifact = str(row["artifactId"])
    base = _coordinate_dir(repository, group, artifact, version)
    if not base.is_dir():
        raise ProjectionError(f"primary publication directory missing coordinate={group}:{artifact}:{version}")
    if version.endswith("-SNAPSHOT"):
        metadata = base / "maven-metadata.xml"
        actual_version, values = _snapshot_values(metadata)
        if actual_version != version:
            raise ProjectionError(
                f"primary snapshot version mismatch expected={version} actual={actual_version} path={metadata}"
            )
        jar = base / f"{artifact}-{values['jar']}.jar"
        pom = base / f"{artifact}-{values['pom']}.pom"
        return jar, pom, metadata, values
    return base / f"{artifact}-{version}.jar", base / f"{artifact}-{version}.pom", None, {
        "jar": version,
        "pom": version,
    }


def _rewrite_pom(source: Path, target: Path, group: str, artifact: str) -> None:
    tree = _parse_xml(source)
    root = tree.getroot()
    namespace = root.tag.split("}", 1)[0] + "}" if root.tag.startswith("{") else ""
    group_node = _require_child(root, namespace + "groupId", source)
    artifact_node = _require_child(root, namespace + "artifactId", source)
    group_node.text = group
    artifact_node.text = artifact
    _write_xml(tree, target)


def _rewrite_snapshot_metadata(source: Path, target: Path, group: str, artifact: str) -> None:
    tree = _parse_xml(source)
    root = tree.getroot()
    _require_child(root, "groupId", source).text = group
    _require_child(root, "artifactId", source).text = artifact
    versions = _require_child(_require_child(root, "versioning", source), "snapshotVersions", source)
    for item in list(versions):
        classifier = _text(item, "classifier")
        extension = _text(item, "extension")
        if classifier or extension not in {"jar", "pom"}:
            versions.remove(item)
    remaining = {
        _text(item, "extension") for item in versions.findall("snapshotVersion") if not _text(item, "classifier")
    }
    if remaining != {"jar", "pom"}:
        raise ProjectionError(f"projected snapshot metadata is incomplete path={source} extensions={remaining}")
    _write_xml(tree, target)


def _rewrite_artifact_metadata(source: Path, target: Path, group: str, artifact: str) -> None:
    tree = _parse_xml(source)
    root = tree.getroot()
    _require_child(root, "groupId", source).text = group
    _require_child(root, "artifactId", source).text = artifact
    _write_xml(tree, target)


def _project_alias(repository: Path, primary: dict[str, Any], alias: dict[str, Any], version: str) -> dict[str, str]:
    alias_class = str(alias.get("publicationClass") or "")
    if alias_class != "PUBLIC_RUNTIME":
        raise ProjectionError(
            f"multi-coordinate Source owner alias must be PUBLIC_RUNTIME artifact={alias.get('artifactId')} class={alias_class}"
        )
    if alias.get("publishSources") is True or alias.get("publishJavadoc") is True:
        raise ProjectionError(f"runtime alias must not publish documentation artifact={alias.get('artifactId')}")

    primary_jar, primary_pom, snapshot_metadata, resolved = _primary_files(repository, primary, version)
    for source in (primary_jar, primary_pom):
        if not source.is_file():
            raise ProjectionError(f"primary publication file missing path={source}")

    group = str(alias["publicGroupId"])
    artifact = str(alias["artifactId"])
    target_dir = _coordinate_dir(repository, group, artifact, version)
    if target_dir.exists():
        raise ProjectionError(f"runtime alias target already exists; refusing stale merge path={target_dir}")
    target_dir.mkdir(parents=True)

    target_jar = target_dir / f"{artifact}-{resolved['jar']}.jar"
    target_pom = target_dir / f"{artifact}-{resolved['pom']}.pom"
    shutil.copyfile(primary_jar, target_jar)
    _rewrite_pom(primary_pom, target_pom, group, artifact)
    _write_checksums(target_jar)
    _write_checksums(target_pom)

    if snapshot_metadata is not None:
        target_snapshot_metadata = target_dir / "maven-metadata.xml"
        _rewrite_snapshot_metadata(snapshot_metadata, target_snapshot_metadata, group, artifact)
        _write_checksums(target_snapshot_metadata)

    primary_artifact_dir = primary_jar.parent.parent
    primary_artifact_metadata = primary_artifact_dir / "maven-metadata.xml"
    if primary_artifact_metadata.is_file():
        alias_artifact_dir = target_dir.parent
        alias_artifact_metadata = alias_artifact_dir / "maven-metadata.xml"
        _rewrite_artifact_metadata(primary_artifact_metadata, alias_artifact_metadata, group, artifact)
        _write_checksums(alias_artifact_metadata)

    return {
        "source": f"{primary['publicGroupId']}:{primary['artifactId']}:{version}",
        "target": f"{group}:{artifact}:{version}",
        "jar": target_jar.relative_to(repository).as_posix(),
        "pom": target_pom.relative_to(repository).as_posix(),
    }


def project_aliases(root: Path, repository: Path, version: str) -> dict[str, Any]:
    catalog_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    try:
        catalog = json.loads(catalog_path.read_text(encoding="utf-8-sig"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ProjectionError(f"invalid artifact catalog path={catalog_path}: {exc}") from exc

    by_project: dict[str, list[dict[str, Any]]] = {}
    for raw in catalog.get("artifacts") or []:
        row = dict(raw)
        if str(row.get("publicationClass") or "") not in PUBLIC_CLASSES:
            continue
        project = str(row.get("publicProjectPath") or "").strip()
        if not project or not str(row.get("publicGroupId") or "").strip():
            continue
        by_project.setdefault(project, []).append(row)

    projected: list[dict[str, str]] = []
    for project, rows in sorted(by_project.items()):
        if len(rows) < 2:
            continue
        ordered = sorted(
            rows,
            key=lambda row: (PRIORITY.get(str(row.get("publicationClass") or ""), 9), str(row.get("artifactId") or "")),
        )
        primary = ordered[0]
        for alias in ordered[1:]:
            projected.append(_project_alias(repository, primary, alias, version))

    result = {"status": "PASS", "version": version, "aliasCount": len(projected), "aliases": projected}
    print(json.dumps(result, ensure_ascii=False))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--repository", required=True)
    parser.add_argument("--version", required=True)
    args = parser.parse_args()
    try:
        project_aliases(Path(args.root).resolve(), Path(args.repository).resolve(), args.version.strip())
        return 0
    except Exception as exc:
        print(json.dumps({"status": "FAIL", "message": str(exc)}, ensure_ascii=False))
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
