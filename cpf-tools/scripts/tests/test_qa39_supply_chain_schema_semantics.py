from __future__ import annotations

import json
import subprocess
from pathlib import Path

QA39 = Path(__file__).parents[1] / "Qa39Tool.java"


def write(root: Path, relative: str, text: str) -> None:
    path = root / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def compile_tool(tmp_path: Path) -> Path:
    classes = tmp_path / "classes"
    classes.mkdir()
    result = subprocess.run(["javac", "-d", str(classes), str(QA39)], text=True, capture_output=True)
    assert result.returncode == 0, result.stdout + result.stderr
    return classes


def fixture(root: Path, schema_version: object) -> None:
    write(root, "settings.gradle", "rootProject.name='fixture'\n")
    catalog = {
        "schemaVersion": schema_version,
        "catalogId": "CPF-FINAL-ARTIFACT-CATALOG",
        "baselinePolicy": "GIT_HEAD_RUNTIME",
        "baselineSha": "RUNTIME_GIT_HEAD",
        "canonicalStarterCatalog": "cpf-tools/generator/contracts/cpf-starter-catalog.json",
        "publicSelectionSurface": {"directInternalArtifactSelection": False},
        "officialDatabaseVendors": ["Oracle", "PostgreSQL", "MariaDB"],
        "artifacts": [
            {
                "artifactId": f"cpf-db-{vendor}",
                "kind": "database-pack",
                "ownerPath": f"cpf-tools/db/vendor/{vendor}",
                "outputPattern": f"out/{vendor}.zip",
                "producer": "fixture",
                "consumer": "fixture",
                "requiredAttestations": ["sha256"],
            }
            for vendor in ("oracle", "postgresql", "mariadb")
        ],
    }
    write(root, "cpf-tools/release/cpf-final-artifact-catalog.json", json.dumps(catalog))
    policy = {
        "allowedLicenses": ["Apache-2.0"],
        "conditionalLicenses": [],
        "deniedLicenses": ["UNKNOWN"],
        "failClosed": True,
        "requiredTools": [
            {"name": name}
            for name in ("cyclonedx-gradle", "ort", "syft", "grype", "cpf-release-signer")
        ],
    }
    write(root, "cpf-tools/supply-chain/cpf-supply-chain-policy.json", json.dumps(policy))
    write(root, "cpf-tools/supply-chain/approved-primary-oss.csv", "component,version,source_url,license\nfixture,1.0,https://example.test,Apache-2.0\n")
    write(root, "cpf-docs/legal/THIRD_PARTY_NOTICES_QA32.md", "fixture")
    env = {
        "schemaVersion": 1,
        "sanitized": True,
        "sourceSha": "0" * 40,
        "tools": [
            {"name": "java", "version": "25"},
            {"name": "gradle-wrapper", "version": "9.1.0"},
            {"name": "node", "version": "22"},
            {"name": "npm", "version": "10"},
            {"name": "python", "version": "3.13"},
            {"name": "powershell", "version": "7"},
        ],
        "databases": [
            {"vendor": "oracle", "available": False},
            {"vendor": "postgresql", "available": False},
            {"vendor": "mariadb", "available": False},
        ],
        "browsers": [
            {"name": "chromium", "available": False},
            {"name": "firefox", "available": False},
            {"name": "webkit", "available": False},
        ],
    }
    write(root, "cpf-tools/governance/cpf-runtime-environment-manifest.template.json", json.dumps(env))
    write(root, "cpf-tools/governance/cpf-runtime-environment-manifest.schema.json", "{}")


def run_gate(tmp_path: Path, schema_version: object) -> subprocess.CompletedProcess[str]:
    repo = tmp_path / "repo"
    fixture(repo, schema_version)
    classes = compile_tool(tmp_path)
    return subprocess.run(
        ["java", "-cp", str(classes), "Qa39Tool", "supply-chain", "--root", str(repo)],
        text=True,
        capture_output=True,
    )


def test_supply_chain_accepts_catalog_v2_runtime_baseline_contract(tmp_path: Path) -> None:
    result = run_gate(tmp_path, "2.0.0")
    assert result.returncode == 0, result.stdout + result.stderr
    assert '"status": "PASS"' in result.stdout




def test_supply_chain_ignores_verification_only_gradle_projects(tmp_path: Path) -> None:
    repo = tmp_path / "repo"
    fixture(repo, "2.0.0")
    write(
        repo,
        "settings.gradle",
        "rootProject.name='fixture'\ninclude 'cpf-tools:verification:core-only-consumer'\n",
    )
    classes = compile_tool(tmp_path)
    result = subprocess.run(
        ["java", "-cp", str(classes), "Qa39Tool", "supply-chain", "--root", str(repo)],
        text=True,
        capture_output=True,
    )
    assert result.returncode == 0, result.stdout + result.stderr
    assert '"status": "PASS"' in result.stdout

def test_supply_chain_rejects_unsupported_catalog_schema(tmp_path: Path) -> None:
    result = run_gate(tmp_path, "9.9.9")
    assert result.returncode == 1
    assert "unsupported or empty final artifact catalog" in result.stderr
