#!/usr/bin/env python3
"""Runtime Artifact 의 Dependency Ownership 분석.

Git LFS 채택은 Packaging 품질 검증을 대체하지 않는다(Harness §39). Artifact 가 커도 되지만,
그 안에 들어간 이유는 설명 가능해야 한다. 이 도구는 fat JAR 내부를 읽어 다음을 찾는다.

1. Wrong Scope        : test/dev 전용 dependency 가 production runtime 에 들어옴
2. Duplicate Version  : 같은 library 가 서로 다른 version 으로 동시 포함
3. Cross-role Leakage : 한 Runtime 이 자기 역할과 무관한 다른 Runtime 의 module 을 품음
4. Dual Web Stack     : Servlet(Tomcat) 과 Reactive(Netty) stack 동시 포함
5. DB Driver Spread   : vendor driver 가 어느 Runtime 까지 퍼져 있는가
6. Frontend Dev Waste : source map / dev asset / node_modules 잔재

이 도구는 **측정만 한다.** dependency 를 제거하거나 packaging 을 바꾸지 않는다.
grep 한 번으로 "안 쓰니 삭제"로 판정하지 않는다(Harness §39.6, Spring 은 AutoConfiguration/SPI/
Reflection 으로 쓰인다). 판정은 SUSPECT 까지이고 삭제 여부는 Source Owner 가 결정한다.

Harness Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §39
"""

from __future__ import annotations

import argparse
import collections
import json
import re
import sys
import zipfile
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

CATALOG_REL = "cpf-tools/runtime/cpf-runtime-target-catalog.json"
REPOSITORY_REL = "cpf-release/binary-repository"
RUNTIME_GROUP = "com/cpf/runtime"
LIB_PREFIXES = ("BOOT-INF/lib/", "WEB-INF/lib/", "lib/")

# jar 파일명에서 "이름 + 버전" 을 가른다. 버전은 숫자로 시작하는 첫 조각부터다.
VERSIONED = re.compile(r"^(?P<name>.+?)-(?P<version>\d[^/]*)\.jar$")

# 아래 표식은 "그 자체로 위반" 이 아니라 "설명이 필요한 상태" 를 가리킨다.
TEST_SCOPE_MARKERS = ("junit", "mockito", "assertj", "hamcrest", "testcontainers",
                      "spring-test", "spring-boot-test", "byte-buddy-agent", "awaitility")
DEV_SCOPE_MARKERS = ("spring-boot-devtools", "spring-boot-configuration-processor")
SERVLET_MARKERS = ("tomcat-embed-core", "jetty-server", "undertow-core")
REACTIVE_MARKERS = ("reactor-netty-http", "netty-codec-http")
DB_DRIVER_MARKERS = {"ojdbc": "oracle", "postgresql": "postgresql", "mariadb-java-client": "mariadb"}
FRONTEND_WASTE = (".map", "/node_modules/", ".ts.map", ".css.map")


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def runtime_artifact_ids(root: Path) -> dict[str, str]:
    """artifactId -> target 이름. 이름 목록을 이 도구가 들고 있지 않는다."""
    catalog = read_json(root / CATALOG_REL)
    return {str(entry.get("artifactId", "")).strip(): str(entry.get("target", ""))
            for entry in catalog.get("runtimes", [])
            if entry.get("provision") == "binary" and str(entry.get("artifactId", "")).strip()}


def split_version(jar_name: str) -> tuple[str, str]:
    match = VERSIONED.match(jar_name)
    if not match:
        return jar_name[:-4] if jar_name.endswith(".jar") else jar_name, ""
    return match.group("name"), match.group("version")


def inspect(path: Path) -> dict:
    libraries: dict[str, str] = {}
    frontend_waste = 0
    frontend_bytes = 0
    with zipfile.ZipFile(path) as archive:
        for info in archive.infolist():
            if info.is_dir():
                continue
            name = info.filename
            if name.startswith(LIB_PREFIXES) and name.endswith(".jar"):
                base = name.rsplit("/", 1)[-1]
                library, version = split_version(base)
                libraries[base] = version
                continue
            lowered = name.lower()
            if "/static/" in lowered or "/public/" in lowered:
                frontend_bytes += info.file_size
                if any(marker in lowered for marker in FRONTEND_WASTE):
                    frontend_waste += info.file_size
    return {"libraries": libraries, "frontendBytes": frontend_bytes,
            "frontendWasteBytes": frontend_waste}


def analyse(path: Path, target: str, artifact_ids: dict[str, str]) -> dict:
    detail = inspect(path)
    libraries = detail["libraries"]

    by_name: dict[str, set[str]] = collections.defaultdict(set)
    for base, version in libraries.items():
        name, _ = split_version(base)
        by_name[name].add(version)

    duplicates = sorted(f"{name} {sorted(versions)}"
                        for name, versions in by_name.items() if len(versions) > 1)

    test_scope = sorted(base for base in libraries
                        if any(marker in base for marker in TEST_SCOPE_MARKERS))
    dev_scope = sorted(base for base in libraries
                       if any(marker in base for marker in DEV_SCOPE_MARKERS))

    servlet = sorted(base for base in libraries
                     if any(marker in base for marker in SERVLET_MARKERS))
    reactive = sorted(base for base in libraries
                      if any(marker in base for marker in REACTIVE_MARKERS))

    drivers = sorted({vendor for base in libraries
                      for marker, vendor in DB_DRIVER_MARKERS.items() if marker in base})

    # 다른 Public Binary Runtime 의 실행 module 을 품고 있는가.
    cross_role = sorted(base for base in libraries
                        for other, other_target in artifact_ids.items()
                        if other_target != target and base.startswith(other + "-"))

    return {
        "target": target,
        "artifact": path.name,
        "dependencyCount": len(libraries),
        "duplicateVersions": duplicates,
        "testScopeSuspect": test_scope,
        "devScopeSuspect": dev_scope,
        "servletStack": servlet,
        "reactiveStack": reactive,
        "dualWebStack": bool(servlet) and bool(reactive),
        "dbDrivers": drivers,
        "crossRoleRuntimeModules": cross_role,
        "frontendBytes": detail["frontendBytes"],
        "frontendWasteBytes": detail["frontendWasteBytes"],
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Runtime dependency ownership analysis")
    parser.add_argument("--root", default=".")
    parser.add_argument("--out")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    repository = root / REPOSITORY_REL
    if not repository.is_dir():
        print(json.dumps({"status": "SKIP", "message": "Release Binary Repository 가 없다"},
                         ensure_ascii=False))
        return 0

    artifact_ids = runtime_artifact_ids(root)
    rows: list[dict] = []
    for path in sorted((repository / RUNTIME_GROUP).rglob("*.jar")):
        if path.name.endswith("-plain.jar"):
            continue
        artifact = path.relative_to(repository).parts[-3]
        if artifact not in artifact_ids:
            continue
        rows.append(analyse(path, artifact_ids[artifact], artifact_ids))

    findings = {
        "wrongScopeTest": [row["target"] for row in rows if row["testScopeSuspect"]],
        "wrongScopeDev": [row["target"] for row in rows if row["devScopeSuspect"]],
        "duplicateVersion": [row["target"] for row in rows if row["duplicateVersions"]],
        "dualWebStack": [row["target"] for row in rows if row["dualWebStack"]],
        "crossRoleRuntimeModules": [row["target"] for row in rows if row["crossRoleRuntimeModules"]],
        "frontendWaste": [row["target"] for row in rows if row["frontendWasteBytes"]],
    }
    failed = any(findings.values())

    result = {
        "status": "FINDING" if failed else "PASS",
        "note": "SUSPECT 는 삭제 지시가 아니다. Spring 은 AutoConfiguration/SPI/Reflection 으로 쓰인다.",
        "runtimeCount": len(rows),
        "findings": findings,
        "runtimes": rows,
    }
    if args.out:
        out = Path(args.out)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(json.dumps({"status": result["status"], "runtimeCount": len(rows),
                      "findings": findings, "out": args.out or ""}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
