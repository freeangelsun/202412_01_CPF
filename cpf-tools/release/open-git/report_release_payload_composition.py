#!/usr/bin/env python3
"""Release Payload Composition 분석.

Release 산출물의 크기를 "대용량 바이너리" 한 마디로 판정하지 않는다. byte 를 다음으로 분해한다.

1. CPF 자체 개발 Artifact
2. OSS dependency 의 별도 repository 복제본
3. executable/fat JAR 내부에 포함된 OSS dependency
4. 동일 dependency 의 Runtime 별 중복
5. Sources/Javadoc/POM/metadata
6. Release 에 실제 필요한 파일
7. Fresh Consumer 에만 필요한 파일
8. Offline Consumer 를 위해 필요한 파일

원칙: 공개 OSS repository 에서 정상 resolve 되는 dependency 를 공개 binary-repository 에 불필요하게
중복 저장하지 않는다. 다만 Product Contract 가 완전 Offline/Fresh Consumer 를 요구해 bundling 이
필요하면 임의로 제거하지 않는다. 이 도구는 측정만 하고 Architecture 를 바꾸지 않는다.

Harness Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §39
"""

from __future__ import annotations

import argparse
import collections
import json
import sys
import zipfile
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPOSITORY_REL = "cpf-release/binary-repository"
CATALOG_REL = "cpf-tools/runtime/cpf-runtime-target-catalog.json"

CPF_GROUP_PREFIX = "com/cpf/"
EMBEDDED_LIB_PREFIXES = ("BOOT-INF/lib/", "WEB-INF/lib/", "lib/")
METADATA_SUFFIXES = (".pom", ".sha256", ".sha512", ".md5", ".json", ".module", ".xml", ".asc")
DOC_CLASSIFIERS = ("-sources.jar", "-javadoc.jar")


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def binary_artifact_ids(root: Path) -> set[str]:
    catalog = read_json(root / CATALOG_REL)
    return {str(entry.get("artifactId", "")).strip()
            for entry in catalog.get("runtimes", [])
            if entry.get("provision") == "binary" and str(entry.get("artifactId", "")).strip()}


def is_cpf_authored_entry(name: str) -> bool:
    """fat JAR 안에서 CPF 가 직접 만든 부분인가."""
    if name.startswith(EMBEDDED_LIB_PREFIXES):
        base = name.rsplit("/", 1)[-1]
        return base.startswith("cpf-")
    return True


def analyse_fat_jar(path: Path) -> dict:
    """fat JAR 내부를 CPF 자체 / 내장 OSS 로 나눈다. 압축 전 크기로 센다."""
    cpf_bytes = 0
    oss_bytes = 0
    embedded: dict[str, int] = {}
    with zipfile.ZipFile(path) as archive:
        for info in archive.infolist():
            if info.is_dir():
                continue
            size = info.file_size
            name = info.filename
            if name.startswith(EMBEDDED_LIB_PREFIXES) and name.endswith(".jar"):
                base = name.rsplit("/", 1)[-1]
                if base.startswith("cpf-"):
                    cpf_bytes += size
                else:
                    oss_bytes += size
                    embedded[base] = size
                continue
            if is_cpf_authored_entry(name):
                cpf_bytes += size
            else:
                oss_bytes += size
    return {"cpfBytes": cpf_bytes, "ossBytes": oss_bytes, "embedded": embedded}


def main() -> int:
    parser = argparse.ArgumentParser(description="Release Payload Composition")
    parser.add_argument("--root", default=".")
    parser.add_argument("--out")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    repository = root / REPOSITORY_REL
    if not repository.is_dir():
        print(json.dumps({"status": "SKIP", "message": "Release Binary Repository 가 없다"},
                         ensure_ascii=False))
        return 0

    artifact_ids = binary_artifact_ids(root)

    on_disk_total = 0
    metadata_bytes = 0
    doc_bytes = 0
    vendored_oss_bytes = 0
    cpf_library_bytes = 0
    generator_bytes = 0
    fat_jar_on_disk = 0

    fat_jars: list[dict] = []
    embedded_index: dict[str, list[int]] = collections.defaultdict(list)

    for path in sorted(repository.rglob("*")):
        if not path.is_file():
            continue
        size = path.stat().st_size
        on_disk_total += size
        posix = path.relative_to(repository).as_posix()

        if path.name.endswith(METADATA_SUFFIXES):
            metadata_bytes += size
            continue
        if path.name.endswith(DOC_CLASSIFIERS):
            doc_bytes += size
            continue
        if not posix.startswith(CPF_GROUP_PREFIX):
            # com/cpf 밖에 있는 Artifact = OSS 별도 복제본.
            vendored_oss_bytes += size
            continue
        if path.name.endswith(".zip"):
            generator_bytes += size
            continue
        if not path.name.endswith(".jar"):
            continue

        artifact = path.relative_to(repository).parts[-3]
        if artifact in artifact_ids and not path.name.endswith("-plain.jar"):
            fat_jar_on_disk += size
            detail = analyse_fat_jar(path)
            fat_jars.append({
                "artifact": artifact,
                "path": REPOSITORY_REL + "/" + posix,
                "onDiskBytes": size,
                "cpfBytes": detail["cpfBytes"],
                "ossBytes": detail["ossBytes"],
                "embeddedCount": len(detail["embedded"]),
            })
            for name, embedded_size in detail["embedded"].items():
                embedded_index[name].append(embedded_size)
        else:
            cpf_library_bytes += size

    duplicate_bytes = 0
    duplicate_top: list[dict] = []
    for name, sizes in embedded_index.items():
        if len(sizes) > 1:
            waste = sum(sizes) - max(sizes)
            duplicate_bytes += waste
            duplicate_top.append({"dependency": name, "copies": len(sizes), "duplicateBytes": waste})
    duplicate_top.sort(key=lambda row: -row["duplicateBytes"])

    cpf_embedded = sum(row["cpfBytes"] for row in fat_jars)
    oss_embedded = sum(row["ossBytes"] for row in fat_jars)

    result = {
        "status": "PASS",
        "measuredAt": "on-disk bytes; fat jar 내부는 압축 해제 기준",
        "onDiskTotalBytes": on_disk_total,
        "cpfAuthoredBinaryBytes": cpf_library_bytes + cpf_embedded,
        "cpfAuthoredDetail": {
            "standaloneLibraryJarBytes": cpf_library_bytes,
            "embeddedInFatJarBytes": cpf_embedded,
        },
        "ossSeparatelyVendoredBytes": vendored_oss_bytes,
        "ossEmbeddedInFatJarBytes": oss_embedded,
        "duplicateEmbeddedDependencyBytes": duplicate_bytes,
        "metadataBytes": metadata_bytes,
        "docsBytes": doc_bytes,
        "generatorDistributionBytes": generator_bytes,
        "fatJarOnDiskBytes": fat_jar_on_disk,
        "fatJarCount": len(fat_jars),
        "fatJars": sorted(fat_jars, key=lambda row: -row["onDiskBytes"]),
        "topDuplicateDependencies": duplicate_top[:15],
    }

    if args.out:
        out = Path(args.out)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    summary = {key: result[key] for key in (
        "onDiskTotalBytes", "cpfAuthoredBinaryBytes", "ossSeparatelyVendoredBytes",
        "ossEmbeddedInFatJarBytes", "duplicateEmbeddedDependencyBytes",
        "metadataBytes", "docsBytes", "fatJarCount")}
    summary["status"] = "PASS"
    summary["out"] = args.out or ""
    print(json.dumps(summary, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
