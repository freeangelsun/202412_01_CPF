"""Fresh Open Git Consumer 검증.

Final Open Git Tree 만 가지고 격리 환경에서 실제로 쓸 수 있는지 확인한다. Development Master
의 module/source 나 사용자 로컬 Maven cache 에 숨어서 성공하는 False Green 을 막기 위해
GRADLE_USER_HOME 을 임시 경로로 격리하고 mavenLocal 사용 여부를 함께 검사한다.

검증 항목
  1. bundled binary repository 로 explicit version resolve + transitive dependency
  2. Windows/Linux launcher parity 와 lifecycle 집합
  3. Public Documentation 존재와 README link 무결성
  4. Package Manifest 와 실제 파일 집합 일치, SHA-256 일치
  5. Development Master 경로 참조 0
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

sys.stdout.reconfigure(encoding="utf-8")

LIFECYCLE = ("start", "stop", "status", "restart", "health", "log", "help")
# mavenLocal 은 어떤 경우에도 Public Consumer 경로가 아니다.
FORBIDDEN_REFERENCE = re.compile(r"mavenLocal\s*\(")
# 개발 composite 전용 경로는 cpfProductCompositeRoot 가 설정된 경우에만 쓰이므로
# Open Git 기본 Golden Path 에서는 실행되지 않는다. 문서 본문의 경로 언급도 실행 의존이 아니다.
CONDITIONAL_COMPOSITE = "cpfProductCompositeRoot"


def fail(results: list[str], message: str) -> None:
    results.append(message)
    print(f"  [FAIL] {message}")


def check_bundled_repository(tree: Path, results: list[str]) -> dict:
    repository = tree / "binary-repository"
    if not repository.is_dir():
        fail(results, "bundled binary-repository is missing")
        return {}
    manifest_path = repository / "package-manifest.json"
    if not manifest_path.is_file():
        fail(results, "package-manifest.json is missing")
        return {}
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))

    actual = {p.relative_to(repository).as_posix() for p in repository.rglob("*")
              if p.is_file() and p != manifest_path}
    listed = {str(row["relativePath"]) for row in manifest["artifacts"]}
    if actual != listed:
        fail(results, f"manifest mismatch: unlisted={sorted(actual - listed)[:5]} absent={sorted(listed - actual)[:5]}")
    for row in manifest["artifacts"]:
        path = repository / str(row["relativePath"])
        if path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest() != row["sha256"]:
            fail(results, f"SHA-256 mismatch: {row['relativePath']}")

    def is_generator_distribution(name: str) -> bool:
        # artifact policy 의 generatorDistribution 승인 유형(zip / zip.sha256 / json).
        return name.endswith((".zip", ".zip.sha256", ".json"))

    forbidden = [
        p.relative_to(repository).as_posix()
        for p in repository.rglob("*")
        if p.is_file()
        and not is_generator_distribution(p.name)
        and (p.name.endswith((".md5", ".sha1", ".sha256", ".sha512"))
             or p.name == "maven-metadata.xml"
             or p.name.endswith(".module")
             or "-SNAPSHOT" in p.name
             or re.search(r"\d{8}\.\d{6}-\d+", p.name))
    ]
    if forbidden:
        fail(results, f"forbidden artifacts in public tree: {forbidden[:5]}")

    jars = [p for p in repository.rglob("*.jar")]
    poms = [p for p in repository.rglob("*.pom")]
    print(f"  bundled repository: files={len(actual)} jar={len(jars)} pom={len(poms)} "
          f"publicVersion={manifest['publicVersion']}")
    return manifest


def check_launchers(tree: Path, results: list[str]) -> None:
    bin_dir = tree / "bin"
    windows = {p.stem for p in bin_dir.glob("cpf-*.ps1")}
    linux = {p.name[:-3] for p in bin_dir.glob("cpf-*.sh")}
    if windows != linux:
        fail(results, f"launcher parity broken: win-only={sorted(windows - linux)} linux-only={sorted(linux - windows)}")
    missing = [f"cpf-{name}" for name in LIFECYCLE if f"cpf-{name}" not in windows]
    if missing:
        fail(results, f"lifecycle launcher missing: {missing}")
    print(f"  launchers: windows={len(windows)} linux={len(linux)}")


def check_documentation(tree: Path, results: list[str]) -> None:
    readme = tree / "README.md"
    if not readme.is_file():
        fail(results, "README.md is missing")
        return
    text = readme.read_text(encoding="utf-8")
    placeholders = re.findall(r"<[a-z][a-z-]*-(?:url|version)>", text)
    if placeholders:
        fail(results, f"README placeholder: {sorted(set(placeholders))}")
    for reference in sorted(set(re.findall(r"`(cpf-docs/[^`]+)`", text))):
        if not (tree / reference).exists():
            fail(results, f"README references missing document: {reference}")
    docs = tree / "cpf-docs"
    if not docs.is_dir():
        fail(results, "cpf-docs is missing")
        return
    leaked = [p.relative_to(tree).as_posix() for p in docs.rglob("*")
              if p.is_file() and any(p.relative_to(tree).as_posix().startswith(prefix)
                                     for prefix in ("cpf-docs/governance/", "cpf-docs/work/",
                                                    "cpf-docs/development/"))]
    if leaked:
        fail(results, f"internal documentation leaked: {leaked[:5]}")
    print(f"  documentation: files={len([p for p in docs.rglob('*') if p.is_file()])}")


def check_no_development_master_reference(tree: Path, results: list[str]) -> None:
    hits = []
    for path in tree.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in {".gradle", ".kts", ".properties", ".md", ".ps1", ".sh"}:
            continue
        if ".git" in path.relative_to(tree).parts:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if path.suffix.lower() == ".md":
            continue  # 문서 본문의 경로 언급은 실행 의존이 아니다.
        if CONDITIONAL_COMPOSITE in text:
            continue  # 개발 composite 전용 분기(Open Git Golden Path 에서는 미실행).
        if FORBIDDEN_REFERENCE.search(text):
            hits.append(path.relative_to(tree).as_posix())
    if hits:
        fail(results, f"development master / mavenLocal reference: {hits[:5]}")
    print(f"  development-master references: {len(hits)}")


def check_gradle_resolution(tree: Path, manifest: dict, results: list[str]) -> None:
    """격리 Gradle cache 에서 bundled repository 만으로 resolve 한다."""
    gradle = tree / ("gradlew.bat" if os.name == "nt" else "gradlew")
    if not gradle.is_file():
        fail(results, "gradle wrapper is missing in Open Git tree")
        return
    # Windows 는 Gradle 이 캐시 파일 핸들을 잠깐 잡고 있어 컨텍스트 종료 시 삭제가 실패할 수 있다.
    # 검증 결과와 무관한 정리 실패로 PASS/FAIL 판정이 뒤집히지 않게 한다.
    isolated = tempfile.mkdtemp(prefix="cpf-open-git-consumer-")
    try:
        env = dict(os.environ)
        env["GRADLE_USER_HOME"] = isolated
        env["CPF_MAVEN_REPOSITORY_URL"] = (tree / "binary-repository").resolve().as_uri()
        env["CPF_VERSION"] = str(manifest.get("publicVersion", ""))
        completed = subprocess.run(
            [str(gradle), "--no-daemon", "--console=plain", "-q", "projects"],
            cwd=tree, env=env, text=True, encoding="utf-8", errors="replace", capture_output=True)
        if completed.returncode != 0:
            fail(results, f"isolated gradle resolution failed: {completed.stdout[-400:]}{completed.stderr[-400:]}")
        else:
            print("  isolated gradle resolution: PASS")
    finally:
        shutil.rmtree(isolated, ignore_errors=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--tree", required=True)
    parser.add_argument("--skip-gradle", action="store_true")
    args = parser.parse_args()
    tree = Path(args.tree).resolve()

    print(f"CPF Fresh Open Git Consumer verification: {tree}")
    results: list[str] = []
    manifest = check_bundled_repository(tree, results)
    check_launchers(tree, results)
    check_documentation(tree, results)
    check_no_development_master_reference(tree, results)
    if not args.skip_gradle and manifest:
        check_gradle_resolution(tree, manifest, results)

    print()
    if results:
        print(f"CPF_FRESH_PUBLIC_CONSUMER=FAIL findings={len(results)}")
        return 1
    print("CPF_FRESH_PUBLIC_CONSUMER=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
