"""CPF Runtime Target Catalog 계약.

Gradle Task, Public CLI, Windows/Linux launcher 가 각자 Target 목록을 들고 있으면 의미가
갈라진다. 하나의 canonical catalog 만 두고, Generated Domain/Backoffice 처럼 존재 여부가
변하는 Component 는 이름을 박지 않고 discovery 규칙으로만 표현한다.

실측 결함: cpf-backoffice-web 이 canonical runtime port table 에 없어 자체적으로 8090 을 써서
cpf-admin 과 충돌했다. ALL/DEV 를 동시에 띄우면 뒤에 뜨는 쪽이 실패한다.
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
CATALOG = ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"
SKIP_PARTS = {"build", "bin", ".git", "node_modules", "cpf-release", "__pycache__"}


def load() -> dict:
    return json.loads(CATALOG.read_text(encoding="utf-8-sig"))


def test_catalog_header_and_shape():
    data = load()
    assert data["contract"] == "CPF_RUNTIME_TARGET_CATALOG"
    assert set(data["aggregates"]) == {"ALL", "DEV", "ONLINE"}
    assert set(data["lifecycleCapabilities"]) == {"http-server", "worker", "one-shot"}
    for runtime in data["runtimes"]:
        assert runtime["capability"] in data["lifecycleCapabilities"]
        assert runtime["provision"] in {"binary", "source"}


def test_no_duplicate_default_port_across_targets():
    """서로 다른 Target 이 같은 기본 port 를 쓰면 ALL/DEV 동시 기동이 깨진다."""
    ports: dict[int, list[str]] = {}
    for runtime in load()["runtimes"]:
        port = runtime.get("port")
        if port is None:
            continue
        ports.setdefault(int(port), []).append(runtime["target"])
    duplicates = {port: targets for port, targets in ports.items() if len(targets) > 1}
    assert not duplicates, f"duplicate default ports: {duplicates}"


def test_catalog_ports_match_actual_application_yml():
    """catalog 가 실제 Source 와 갈라지면 launcher status/health 가 엉뚱한 port 를 본다."""
    mismatches = []
    for runtime in load()["runtimes"]:
        port = runtime.get("port")
        if port is None:
            continue
        yml = ROOT / runtime["owner"] / "src/main/resources/application.yml"
        if not yml.is_file():
            continue
        text = yml.read_text(encoding="utf-8", errors="replace")
        match = re.search(r"^server:\s*$(?:\n(?!\S).*)*?\n\s{2}port:\s*(\S+)", text, re.M)
        if not match:
            continue
        default = re.search(r":(\d+)\}", match.group(1)) or re.search(r"^(\d+)$", match.group(1))
        if default and int(default.group(1)) != int(port):
            mismatches.append(f"{runtime['target']}: catalog={port} source={default.group(1)}")
    assert not mismatches, mismatches


def test_generated_domain_targets_are_never_hardcoded():
    """Generated Domain 이름을 catalog 나 launcher 에 박으면 추가/삭제가 수동 작업이 된다."""
    raw = CATALOG.read_text(encoding="utf-8-sig")
    dynamic = load()["dynamicRuntimes"]
    assert dynamic["source"] == "discovery.domain"
    assert "{domainName}" in dynamic["targetPattern"]
    for discovered in ("member", "external"):
        assert f'"{discovered}-online"' not in raw, f"generated domain hardcoded: {discovered}"


def test_backoffice_domain_and_web_are_independent_components():
    """한쪽을 지워도 다른 쪽 Target 이 남아야 한다."""
    runtimes = {r["target"]: r for r in load()["runtimes"]}
    assert runtimes["backoffice"]["component"] == "domain"
    assert runtimes["backoffice-web"]["component"] == "web"
    assert runtimes["backoffice"]["optional"] is True
    assert runtimes["backoffice-web"]["optional"] is True


def test_public_binary_runtimes_match_artifact_catalog():
    """Binary 제공 Target 은 Artifact Catalog 의 PUBLIC_RUNTIME publication 과 일치해야 한다."""
    catalog = json.loads(
        (ROOT / "cpf-tools/release/cpf-final-artifact-catalog.json").read_text(encoding="utf-8-sig"))
    published = {
        a["artifactId"] for a in catalog["artifacts"]
        if a.get("publicationClass") == "PUBLIC_RUNTIME" and a.get("runtimeProvision") == "binary"
    }
    binary_targets = {r["target"] for r in load()["runtimes"] if r["provision"] == "binary"}
    assert len(published) == len(binary_targets), (
        f"binary runtime count mismatch: catalog={len(published)} targets={len(binary_targets)}")
