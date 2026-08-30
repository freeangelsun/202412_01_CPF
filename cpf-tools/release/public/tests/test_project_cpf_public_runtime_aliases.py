from __future__ import annotations

import hashlib
import importlib.util
import json
import xml.etree.ElementTree as ET
from pathlib import Path


SCRIPT = Path(__file__).parents[2] / "tools" / "project-cpf-public-runtime-aliases.py"
SPEC = importlib.util.spec_from_file_location("project_cpf_public_runtime_aliases", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _write_metadata(path: Path, group: str, artifact: str, version: str, resolved: str) -> None:
    path.write_text(
        f"""<?xml version="1.0" encoding="UTF-8"?>
<metadata modelVersion="1.1.0">
  <groupId>{group}</groupId><artifactId>{artifact}</artifactId><version>{version}</version>
  <versioning><lastUpdated>20260830000000</lastUpdated>
    <snapshot><timestamp>20260830.000000</timestamp><buildNumber>1</buildNumber></snapshot>
    <snapshotVersions>
      <snapshotVersion><classifier>sources</classifier><extension>jar</extension><value>{resolved}</value><updated>20260830000000</updated></snapshotVersion>
      <snapshotVersion><extension>jar</extension><value>{resolved}</value><updated>20260830000000</updated></snapshotVersion>
      <snapshotVersion><extension>module</extension><value>{resolved}</value><updated>20260830000000</updated></snapshotVersion>
      <snapshotVersion><extension>pom</extension><value>{resolved}</value><updated>20260830000000</updated></snapshotVersion>
    </snapshotVersions>
  </versioning>
</metadata>
""",
        encoding="utf-8",
    )


def test_snapshot_runtime_alias_is_projected_without_secondary_gradle_publication(tmp_path: Path):
    root = tmp_path / "root"
    repository = tmp_path / "repo"
    catalog_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    catalog_path.parent.mkdir(parents=True)
    catalog_path.write_text(
        json.dumps(
            {
                "artifacts": [
                    {
                        "artifactId": "cpf-starter-sample",
                        "publicationClass": "PUBLIC_COMPILE_TIME_JAVA",
                        "publicGroupId": "com.cpf.starter",
                        "publicProjectPath": ":sample",
                        "publishSources": True,
                        "publishJavadoc": True,
                    },
                    {
                        "artifactId": "cpf-sample-runtime",
                        "publicationClass": "PUBLIC_RUNTIME",
                        "publicGroupId": "com.cpf.runtime",
                        "publicProjectPath": ":sample",
                        "publishSources": False,
                        "publishJavadoc": False,
                    },
                ]
            }
        ),
        encoding="utf-8",
    )

    version = "1.0.0-SNAPSHOT"
    resolved = "1.0.0-20260830.000000-1"
    primary_dir = repository / "com/cpf/starter/cpf-starter-sample" / version
    primary_dir.mkdir(parents=True)
    primary_jar = primary_dir / f"cpf-starter-sample-{resolved}.jar"
    primary_jar.write_bytes(b"canonical-primary-binary")
    (primary_dir / f"cpf-starter-sample-{resolved}.pom").write_text(
        "<project><modelVersion>4.0.0</modelVersion><groupId>com.cpf.starter</groupId>"
        "<artifactId>cpf-starter-sample</artifactId><version>1.0.0-SNAPSHOT</version></project>",
        encoding="utf-8",
    )
    _write_metadata(primary_dir / "maven-metadata.xml", "com.cpf.starter", "cpf-starter-sample", version, resolved)
    primary_artifact_dir = primary_dir.parent
    (primary_artifact_dir / "maven-metadata.xml").write_text(
        "<metadata><groupId>com.cpf.starter</groupId><artifactId>cpf-starter-sample</artifactId>"
        "<versioning><latest>1.0.0-SNAPSHOT</latest><versions><version>1.0.0-SNAPSHOT</version>"
        "</versions></versioning></metadata>",
        encoding="utf-8",
    )

    result = MODULE.project_aliases(root, repository, version)
    assert result["status"] == "PASS"
    assert result["aliasCount"] == 1

    alias_dir = repository / "com/cpf/runtime/cpf-sample-runtime" / version
    alias_jar = alias_dir / f"cpf-sample-runtime-{resolved}.jar"
    alias_pom = alias_dir / f"cpf-sample-runtime-{resolved}.pom"
    assert alias_jar.read_bytes() == primary_jar.read_bytes()
    pom = ET.parse(alias_pom).getroot()
    assert pom.findtext("groupId") == "com.cpf.runtime"
    assert pom.findtext("artifactId") == "cpf-sample-runtime"

    metadata = ET.parse(alias_dir / "maven-metadata.xml").getroot()
    entries = {
        (item.findtext("classifier") or "", item.findtext("extension") or "")
        for item in metadata.findall("./versioning/snapshotVersions/snapshotVersion")
    }
    assert entries == {("", "jar"), ("", "pom")}
    assert not list(alias_dir.glob("*-sources.jar"))
    assert not list(alias_dir.glob("*-javadoc.jar"))
    assert not list(alias_dir.glob("*.module"))
    for payload in (alias_jar, alias_pom, alias_dir / "maven-metadata.xml"):
        assert payload.with_name(payload.name + ".sha256").read_text(encoding="ascii") == hashlib.sha256(payload.read_bytes()).hexdigest()


def test_projection_refuses_stale_runtime_alias_target(tmp_path: Path):
    root = tmp_path / "root"
    repository = tmp_path / "repo"
    catalog_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    catalog_path.parent.mkdir(parents=True)
    rows = [
        {"artifactId": "primary", "publicationClass": "PUBLIC_COMPILE_TIME_JAVA", "publicGroupId": "com.cpf.starter", "publicProjectPath": ":x"},
        {"artifactId": "alias", "publicationClass": "PUBLIC_RUNTIME", "publicGroupId": "com.cpf.runtime", "publicProjectPath": ":x"},
    ]
    catalog_path.write_text(json.dumps({"artifacts": rows}), encoding="utf-8")
    primary_dir = repository / "com/cpf/starter/primary/1.0.0"
    primary_dir.mkdir(parents=True)
    (primary_dir / "primary-1.0.0.jar").write_bytes(b"primary")
    (primary_dir / "primary-1.0.0.pom").write_text(
        "<project><modelVersion>4.0.0</modelVersion><groupId>com.cpf.starter</groupId>"
        "<artifactId>primary</artifactId><version>1.0.0</version></project>",
        encoding="utf-8",
    )
    (repository / "com/cpf/runtime/alias/1.0.0").mkdir(parents=True)
    try:
        MODULE.project_aliases(root, repository, "1.0.0")
        assert False, "stale alias target must fail closed"
    except MODULE.ProjectionError as exc:
        assert "already exists" in str(exc)
