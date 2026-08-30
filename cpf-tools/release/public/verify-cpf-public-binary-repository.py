#!/usr/bin/env python3
"""Verify the isolated CPF Public Maven-compatible repository.

The verifier treats ``cpf-final-artifact-catalog.json`` as the publication
classification owner.  Public compile-time Java artifacts must provide the same
release version of main JAR, POM, sources JAR and Javadoc JAR.  Runtime/BOM/tooling
artifacts are explicit exceptions and must not accidentally acquire source/Javadoc
publication just because they are Java/Gradle projects.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path


class VerificationError(RuntimeError):
    pass


RUNTIME_ALIASES = {
    "cpf-base-runtime",
    "cpf-data",
    "cpf-data-persistence",
    "cpf-file",
    "cpf-integration",
    "cpf-messaging",
    "cpf-platform-operations",
    "cpf-platform-operations-observability",
    "cpf-security",
    "cpf-web-runtime",
    "cpf-cache-spring-data-redis",
    "cpf-integration-http-runtime",
    "cpf-integration-resilience-runtime",
    "cpf-platform-operations-runtime-control",
    "cpf-security-resource-server-runtime",
    "cpf-batch-runtime",
}
PUBLIC_CONTRACT_GROUPS = {
    "cpf-core": "com.cpf.core",
    "cpf-testkit": "com.cpf.testkit",
    "cpf-batch-api": "com.cpf.batch",
}
FORBIDDEN_ARTIFACTS = {
    "cpf-internal-platform-bom",
    "cpf-backoffice",
    "cpf-biz-admin",
    "cpf-biz-channel",
    "cpf-biz-frontend",
}
REQUIRED_GENERATOR_CLASSIFIERS = ("windows-x64", "linux-x64")

FORBIDDEN_POM_TEXT = (
    re.compile(r"com\.cpf\.internal", re.I),
    re.compile(r"(?:^|[/\\])cpf-(?:core|admin|backoffice|tools|starters)(?:[/\\]|$)", re.I),
    re.compile(r"(?:[A-Za-z]:\\Users\\|/home/[^/]+/|/Users/[^/]+/)", re.I),
)
FORBIDDEN_DOC_TEXT = (
    re.compile(r"com\.cpf(?:\.[A-Za-z0-9_]+)*\.internal(?:\.|/)", re.I),
    re.compile(r"freeangelsun/202412_01_CPF", re.I),
    re.compile(r"(?:[A-Za-z]:\\Users\\|/home/[^/]+/|/Users/[^/]+/)", re.I),
)


def _ns(tag: str) -> str:
    return tag.split("}", 1)[0] + "}" if tag.startswith("{") else ""


def _child_text(node: ET.Element, name: str) -> str:
    ns = _ns(node.tag)
    found = node.find(f"{ns}{name}")
    return (found.text or "").strip() if found is not None else ""


def _pom_coordinate(path: Path) -> tuple[str, str, str, list[tuple[str, str, str]]]:
    root = ET.parse(path).getroot()
    group = _child_text(root, "groupId")
    artifact = _child_text(root, "artifactId")
    version = _child_text(root, "version")
    parent = root.find(f"{_ns(root.tag)}parent")
    if parent is not None:
        if not group:
            group = _child_text(parent, "groupId")
        if not version:
            version = _child_text(parent, "version")
    deps: list[tuple[str, str, str]] = []
    ns = _ns(root.tag)
    for parent_name in ("dependencies", "dependencyManagement"):
        containers = root.findall(f"{ns}dependencies") if parent_name == "dependencies" else root.findall(f"{ns}dependencyManagement/{ns}dependencies")
        for container in containers:
            for dep in container.findall(f"{ns}dependency"):
                deps.append((_child_text(dep, "groupId"), _child_text(dep, "artifactId"), _child_text(dep, "version")))
    return group, artifact, version, deps


def _maven_artifact_exists(repository: Path, group: str, artifact: str, version: str) -> bool:
    if not version or version.startswith("${"):
        return True
    base = repository / Path(group.replace(".", "/")) / artifact / version
    if not base.is_dir():
        return False
    return any(p.suffix in {".pom", ".jar", ".zip", ".module"} for p in base.iterdir() if p.is_file())


def _published_artifact_path(
    repository: Path,
    group: str,
    artifact: str,
    version: str,
    extension: str,
    classifier: str = "",
) -> Path:
    """Resolve a release or timestamped Maven Snapshot artifact from metadata."""
    base = repository / Path(group.replace(".", "/")) / artifact / version
    classifier_suffix = f"-{classifier}" if classifier else ""
    if not version.endswith("-SNAPSHOT"):
        return base / f"{artifact}-{version}{classifier_suffix}.{extension}"

    metadata_path = base / "maven-metadata.xml"
    try:
        metadata = ET.parse(metadata_path).getroot()
    except (OSError, ET.ParseError) as exc:
        raise VerificationError(f"invalid Maven snapshot metadata {group}:{artifact}:{version} path={metadata_path}: {exc}") from exc
    actual = (_child_text(metadata, "groupId"), _child_text(metadata, "artifactId"), _child_text(metadata, "version"))
    expected = (group, artifact, version)
    if actual != expected:
        raise VerificationError(f"Maven snapshot metadata coordinate mismatch expected={expected} actual={actual} path={metadata_path}")

    matches: list[str] = []
    ns = _ns(metadata.tag)
    for item in metadata.findall(f"{ns}versioning/{ns}snapshotVersions/{ns}snapshotVersion"):
        if _child_text(item, "extension") != extension:
            continue
        if _child_text(item, "classifier") != classifier:
            continue
        value = _child_text(item, "value")
        if value:
            matches.append(value)
    if len(matches) != 1:
        raise VerificationError(
            f"Maven snapshot metadata must map exactly one artifact {group}:{artifact}:{version} "
            f"extension={extension} classifier={classifier or '<none>'} matches={matches}"
        )
    resolved = matches[0]
    snapshot_base = version[: -len("-SNAPSHOT")]
    if not resolved.startswith(snapshot_base + "-") or "/" in resolved or "\\" in resolved or resolved in {".", ".."}:
        raise VerificationError(f"invalid Maven snapshot value coordinate={group}:{artifact}:{version} value={resolved!r}")
    return base / f"{artifact}-{resolved}{classifier_suffix}.{extension}"


def _load_public_classification(root: Path) -> tuple[dict[str, dict], dict[str, str], dict[str, dict]]:
    policy_path = root / "cpf-tools/release/public/cpf-public-java-publication-policy.json"
    policy = json.loads(policy_path.read_text(encoding="utf-8-sig"))
    if policy.get("policyId") != "CPF-PUBLIC-JAVA-PUBLICATION":
        raise VerificationError("invalid Public Java publication policy id")
    catalog_rel = str(policy.get("artifactCatalog") or "cpf-tools/release/cpf-final-artifact-catalog.json")
    catalog_path = root / catalog_rel
    catalog = json.loads(catalog_path.read_text(encoding="utf-8-sig"))
    rows = catalog.get("artifacts") or []
    compile_class = str(policy.get("compileTimePublicationClass") or "PUBLIC_COMPILE_TIME_JAVA")
    compile_time = {
        str(row["artifactId"]): dict(row)
        for row in rows
        if str(row.get("publicationClass") or "") == compile_class
    }
    if not compile_time:
        raise VerificationError("canonical artifact catalog has no public compile-time Java artifacts")
    for aid, row in compile_time.items():
        if row.get("publishSources") is not True or row.get("publishJavadoc") is not True:
            raise VerificationError(f"compile-time publication must require sources+javadoc artifact={aid}")
        if not str(row.get("publicGroupId") or "").strip():
            raise VerificationError(f"compile-time publication group missing artifact={aid}")
    groups = {aid: str(row["publicGroupId"]) for aid, row in compile_time.items()}
    exceptions: dict[str, dict] = {}
    for row in rows:
        cls = str(row.get("publicationClass") or "")
        if cls not in {"PUBLIC_RUNTIME", "PUBLIC_BOM", "PUBLIC_TOOLING"}:
            continue
        aid = str(row["artifactId"])
        if row.get("publishSources") is True or row.get("publishJavadoc") is True:
            raise VerificationError(f"non compile-time publication exception must not require sources/javadoc artifact={aid}")
        if not str(row.get("documentationArtifactException") or "").strip():
            raise VerificationError(f"documentation artifact exception reason missing artifact={aid}")
        exceptions[aid] = dict(row)
    overlap = sorted(set(compile_time) & set(exceptions))
    if overlap:
        raise VerificationError(f"compile-time/publication-exception overlap={overlap}")
    return compile_time, groups, exceptions


def _load_gradle_plugin_markers(root: Path) -> dict[tuple[str, str], tuple[str, str]]:
    catalog = json.loads((root / "cpf-tools/release/cpf-final-artifact-catalog.json").read_text(encoding="utf-8-sig"))
    markers: dict[tuple[str, str], tuple[str, str]] = {}
    for row in catalog.get("artifacts") or []:
        plugin_id = str(row.get("gradlePluginId") or "").strip()
        if not plugin_id:
            continue
        implementation = (str(row.get("publicGroupId") or "").strip(), str(row.get("artifactId") or "").strip())
        if not all(implementation):
            raise VerificationError(f"Gradle plugin implementation coordinate missing pluginId={plugin_id}")
        marker = (plugin_id, f"{plugin_id}.gradle.plugin")
        if marker in markers:
            raise VerificationError(f"duplicate Gradle plugin marker coordinate={marker}")
        markers[marker] = implementation
    if not markers:
        raise VerificationError("canonical artifact catalog has no Gradle plugin marker")
    return markers


def _gradle_plugin_marker_findings(
    group: str,
    artifact: str,
    pom_version: str,
    dependencies: list[tuple[str, str, str]],
    version: str,
    markers: dict[tuple[str, str], tuple[str, str]],
) -> list[str]:
    implementation = markers.get((group, artifact))
    if implementation is None:
        return []
    findings: list[str] = []
    expected_dependency = (*implementation, version)
    if pom_version != version:
        findings.append(
            f"Gradle plugin marker version mismatch expected={version} actual={pom_version} coordinate={group}:{artifact}"
        )
    if dependencies != [expected_dependency]:
        findings.append(
            f"Gradle plugin marker dependency mismatch expected={[expected_dependency]} actual={dependencies} coordinate={group}:{artifact}"
        )
    return findings

def _scan_zip_leakage(path: Path, *, javadoc: bool) -> list[str]:
    findings: list[str] = []
    with zipfile.ZipFile(path) as zf:
        for info in zf.infolist():
            name = info.filename.replace("\\", "/")
            lowered = f"/{name.lower()}"
            if "/internal/" in lowered or "/com/cpf/internal/" in lowered:
                findings.append(f"internal path entry={name}")
                continue
            if info.is_dir() or info.file_size > 2_000_000:
                continue
            suffix = Path(name).suffix.lower()
            if suffix not in ({".java"} if not javadoc else {".html", ".js", ".json", ".txt"}):
                continue
            text = zf.read(info).decode("utf-8", errors="replace")
            for pattern in FORBIDDEN_DOC_TEXT:
                if pattern.search(text):
                    findings.append(f"forbidden content pattern={pattern.pattern} entry={name}")
                    break
    return findings


def _verify_java_publication_set(repository: Path, version: str, compile_time: dict[str, dict], groups: dict[str, str]) -> tuple[list[str], int, int]:
    findings: list[str] = []
    source_count = 0
    javadoc_count = 0
    for artifact in sorted(compile_time):
        group = groups[artifact]
        expected = {
            "pom": _published_artifact_path(repository, group, artifact, version, "pom"),
            "main": _published_artifact_path(repository, group, artifact, version, "jar"),
            "sources": _published_artifact_path(repository, group, artifact, version, "jar", "sources"),
            "javadoc": _published_artifact_path(repository, group, artifact, version, "jar", "javadoc"),
        }
        for kind, path in expected.items():
            if not path.is_file():
                findings.append(f"missing {kind} artifact {group}:{artifact}:{version} path={path.relative_to(repository) if path.is_absolute() and repository in path.parents else path}")
        if expected["pom"].is_file():
            pg, pa, pv, _ = _pom_coordinate(expected["pom"])
            if (pg, pa, pv) != (group, artifact, version):
                findings.append(f"POM version/coordinate mismatch expected={group}:{artifact}:{version} actual={pg}:{pa}:{pv}")
        if expected["sources"].is_file():
            source_count += 1
            try:
                findings.extend(f"{group}:{artifact}:sources {x}" for x in _scan_zip_leakage(expected["sources"], javadoc=False))
            except zipfile.BadZipFile:
                findings.append(f"invalid sources JAR {group}:{artifact}:{version}")
        if expected["javadoc"].is_file():
            javadoc_count += 1
            try:
                findings.extend(f"{group}:{artifact}:javadoc {x}" for x in _scan_zip_leakage(expected["javadoc"], javadoc=True))
            except zipfile.BadZipFile:
                findings.append(f"invalid javadoc JAR {group}:{artifact}:{version}")
    return findings, source_count, javadoc_count


def _verify_generator_distributions(repository: Path, version: str) -> list[str]:
    """Public Workspace가 Windows/Linux에서 resolve할 Generator tooling을 검증합니다."""
    findings: list[str] = []
    base = repository / "com/cpf/tooling/cpf-generator-cli" / version
    for classifier in REQUIRED_GENERATOR_CLASSIFIERS:
        stem = f"cpf-generator-cli-{version}-{classifier}"
        archive = base / f"{stem}.zip"
        checksum = base / f"{stem}.zip.sha256"
        manifest = base / f"{stem}.json"
        for path in (archive, checksum, manifest):
            if not path.is_file():
                findings.append(f"missing generator distribution classifier={classifier} file={path.relative_to(repository)}")
        if not archive.is_file() or not checksum.is_file() or not manifest.is_file():
            continue
        digest = hashlib.sha256(archive.read_bytes()).hexdigest()
        expected = checksum.read_text(encoding="ascii").strip().split()[0].lower()
        if digest != expected:
            findings.append(f"generator checksum mismatch classifier={classifier} expected={expected} actual={digest}")
        try:
            data = json.loads(manifest.read_text(encoding="utf-8"))
        except Exception as exc:
            findings.append(f"invalid generator manifest classifier={classifier}: {exc}")
            continue
        coordinate = (data.get("artifactId"), data.get("version"), data.get("classifier"))
        if coordinate != ("cpf-generator-cli", version, classifier):
            findings.append(f"generator manifest coordinate mismatch classifier={classifier} actual={coordinate}")
        if data.get("sha256") != digest:
            findings.append(f"generator manifest checksum mismatch classifier={classifier}")
    return findings


def verify(root: Path, repository: Path, version: str) -> dict:
    if not repository.is_dir():
        raise VerificationError(f"public binary repository missing: {repository}")
    compile_time, groups, documentation_exceptions = _load_public_classification(root)
    gradle_plugin_markers = _load_gradle_plugin_markers(root)

    starter_catalog = json.loads((root / "cpf-tools/generator/contracts/cpf-starter-catalog.json").read_text(encoding="utf-8-sig"))
    public_starters = {
        (m["groupId"], m["artifactId"])
        for m in starter_catalog.get("modules", [])
        if m.get("visibility") == "public" and m.get("publicationRequired") is True
    }
    expected = set(public_starters) | {(g, a) for a, g in PUBLIC_CONTRACT_GROUPS.items()} | {("com.cpf", "cpf-platform-bom")} | {("com.cpf.runtime", a) for a in RUNTIME_ALIASES}
    expected.update(gradle_plugin_markers)
    expected.update(gradle_plugin_markers.values())

    poms = sorted(repository.rglob("*.pom"))
    if not poms:
        raise VerificationError("public binary repository contains no POM")

    coordinates: dict[tuple[str, str, str], Path] = {}
    dependency_findings: list[str] = []
    leakage_findings: list[str] = []
    for pom in poms:
        text = pom.read_text(encoding="utf-8", errors="replace")
        for pattern in FORBIDDEN_POM_TEXT:
            if pattern.search(text):
                leakage_findings.append(f"forbidden POM content pattern={pattern.pattern} file={pom.relative_to(repository)}")
        group, artifact, pom_version, deps = _pom_coordinate(pom)
        if not group or not artifact:
            leakage_findings.append(f"invalid POM coordinate file={pom.relative_to(repository)}")
            continue
        coordinates[(group, artifact, pom_version)] = pom
        if group == "com.cpf.internal" or group.startswith("com.cpf.internal."):
            leakage_findings.append(f"internal CPF group published {group}:{artifact}:{pom_version}")
        if artifact in FORBIDDEN_ARTIFACTS:
            leakage_findings.append(f"forbidden/stale CPF artifact published {group}:{artifact}:{pom_version}")
        if group == "com.cpf.runtime" and artifact not in RUNTIME_ALIASES:
            leakage_findings.append(f"unclassified CPF runtime alias {group}:{artifact}:{pom_version}")
        dependency_findings.extend(
            _gradle_plugin_marker_findings(
                group, artifact, pom_version, deps, version, gradle_plugin_markers
            )
        )
        for dg, da, dv in deps:
            if dg == "com.cpf.internal" or dg.startswith("com.cpf.internal."):
                dependency_findings.append(f"internal dependency {group}:{artifact} -> {dg}:{da}:{dv}")
            if da in FORBIDDEN_ARTIFACTS:
                dependency_findings.append(f"stale/forbidden dependency {group}:{artifact} -> {dg}:{da}:{dv}")
            if dg.startswith("com.cpf") and not _maven_artifact_exists(repository, dg, da, dv):
                dependency_findings.append(f"unresolved CPF dependency {group}:{artifact} -> {dg}:{da}:{dv}")

    actual_versioned = {(g, a) for (g, a, v) in coordinates if v == version}
    missing = sorted(expected - actual_versioned)

    java_findings, source_count, javadoc_count = _verify_java_publication_set(repository, version, compile_time, groups)
    generator_findings = _verify_generator_distributions(repository, version)

    # Runtime/BOM/tooling exceptions must not accidentally acquire Java documentation artifacts.
    forbidden_runtime_docs: list[str] = []
    for artifact, row in sorted(documentation_exceptions.items()):
        group = str(row.get("publicGroupId") or row.get("groupId") or "")
        if not group:
            continue
        base = repository / Path(group.replace(".", "/")) / artifact / version
        if not base.is_dir():
            continue
        for classifier in ("sources", "javadoc"):
            for candidate in base.glob(f"{artifact}-*-{classifier}.jar"):
                forbidden_runtime_docs.append(candidate.relative_to(repository).as_posix())

    if missing or leakage_findings or dependency_findings or java_findings or generator_findings or forbidden_runtime_docs:
        details = {
            "missing": missing,
            "leakage": leakage_findings,
            "dependencyFindings": dependency_findings,
            "javaPublicationFindings": java_findings,
            "generatorDistributionFindings": generator_findings,
            "forbiddenRuntimeDocumentationArtifacts": forbidden_runtime_docs,
        }
        raise VerificationError(json.dumps(details, ensure_ascii=False))

    return {
        "status": "PASS",
        "version": version,
        "pomCount": len(poms),
        "publicStarterCount": len(public_starters),
        "runtimeSupportCount": len(RUNTIME_ALIASES),
        "expectedCoordinateCount": len(expected),
        "publicCompileTimeJavaCount": len(compile_time),
        "sourceJarCount": source_count,
        "javadocJarCount": javadoc_count,
        "versionParityFindings": 0,
        "documentationLeakage": 0,
        "internalCoordinateLeak": 0,
        "unresolvedCpfDependency": 0,
        "generatorDistributionCount": len(REQUIRED_GENERATOR_CLASSIFIERS),
        "gradlePluginMarkerCount": len(gradle_plugin_markers),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--repository", required=True)
    parser.add_argument("--version", required=True)
    args = parser.parse_args()
    try:
        result = verify(Path(args.root).resolve(), Path(args.repository).resolve(), args.version.strip())
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
