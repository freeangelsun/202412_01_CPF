#!/usr/bin/env python3
"""Build and verify the CPF Open Git release projection.

The Open Git release is generated under <private-root>/cpf-release and is never
committed to the private repository.  Every build starts by safely removing the
previous generated release root, then creates a fresh Open Git clone and an
independent Maven-compatible binary repository.

This tool never commits or pushes.
"""
from __future__ import annotations

import argparse
import csv
import fnmatch
import hashlib
import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from collections import deque
from urllib.parse import unquote
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


class OpenGitReleaseError(RuntimeError):
    pass


TOOL_REL = Path("cpf-tools/release/open-git")
SURFACE_POLICY_REL = TOOL_REL / "open-git-surface-policy.json"
ARTIFACT_POLICY_REL = TOOL_REL / "open-git-artifact-policy.json"
PUBLIC_SOURCE_ALLOWLIST_REL = TOOL_REL / "open-git-public-source-allowlist.json"
LEGACY_PUBLIC_REL = Path("cpf-tools/release/public")
RELEASE_DIR_NAME = "cpf-release"
OPEN_GIT_DIR_NAME = "open-git"
BINARY_DIR_NAME = "binary-repository"
REPORTS_DIR_NAME = "reports"
LOGS_DIR_NAME = "logs"
WORK_DIR_NAME = "work"
ACTIVE_LOG_FILE: Path | None = None
ACTIVE_STAGE_NO = 0
ACTIVE_STAGE_LABEL = "초기화"
BUILD_STAGE_TOTAL = 14


def release_stage(index: int, label: str, detail: str = "") -> None:
    global ACTIVE_STAGE_NO, ACTIVE_STAGE_LABEL
    ACTIVE_STAGE_NO = index
    ACTIVE_STAGE_LABEL = label
    suffix = f" - {detail}" if detail else ""
    line = f"[CPF][OPEN-GIT][{index:02d}/{BUILD_STAGE_TOTAL:02d}] {label}{suffix}"
    print(line, flush=True)
    _append_log(line)


def recovery_hint(message: str) -> str:
    lower = message.lower()
    if "cpf-release" in lower and ("gitignore" in lower or "tracked" in lower or "symlink" in lower):
        return "/cpf-release/ 제외와 Canonical integration 상태를 확인하세요. 필요할 때만 호환 setup을 1회 실행합니다."
    if "working tree must be clean" in lower:
        return "Private Working Tree 변경을 검토/정리한 뒤 다시 실행하세요. Tool은 commit/reset/clean을 자동 수행하지 않습니다."
    if "remote" in lower:
        return "--remote 또는 CPF_OPEN_GIT_REMOTE에 승인된 Open Git Repository를 설정한 뒤 다시 실행하세요."
    if "artifact catalog contract" in lower or "publicly publishable" in lower or "maven group" in lower or "publishsources" in lower:
        return "Canonical Artifact Catalog/Publication 계약을 먼저 정합화하세요. 임의 Maven 좌표나 Source 공개로 우회하지 않습니다."
    if "java 25" in lower:
        return "Java 25를 선택한 뒤 java -version을 확인하고 같은 명령을 다시 실행하세요."
    if "binary repository" in lower:
        return "Binary publication 로그와 OPEN_GIT_BINARY_VERIFY_RESULT를 확인한 뒤 누락 Artifact를 정식 Publication에서 수정하세요."
    if "secret" in lower or "leakage" in lower or "forbidden" in lower:
        return "공개 Surface/Artifact Policy를 확인하고 비공개 Source/Secret을 제거한 뒤 다시 생성하세요. Gate를 완화하지 마세요."
    if "clone" in lower or "git diff" in lower:
        return "Open Git remote/권한/네트워크와 staged diff를 확인한 뒤 다시 실행하세요. commit/push는 자동 실행되지 않습니다."
    return "표시된 실패 단계와 cpf-release/logs/open-git-release.log를 확인한 뒤 같은 명령을 다시 실행하세요."


def print_failure_summary(root: Path, message: str, code: int = 1) -> None:
    log = ACTIVE_LOG_FILE
    log_text = str(log) if log is not None else "N/A (Release workspace 생성 전 실패)"
    print("", file=sys.stderr)
    print("CPF OPEN GIT RELEASE 실패", file=sys.stderr)
    print("---------------------------", file=sys.stderr)
    print(f"실패 단계 : {ACTIVE_STAGE_NO:02d}/{BUILD_STAGE_TOTAL:02d} {ACTIVE_STAGE_LABEL}", file=sys.stderr)
    print(f"원인      : {message}", file=sys.stderr)
    print(f"Exit Code : {code}", file=sys.stderr)
    print(f"상세 로그 : {log_text}", file=sys.stderr)
    print(f"다음 조치 : {recovery_hint(message)}", file=sys.stderr)
    print("Commit    : NOT_EXECUTED", file=sys.stderr)
    print("Push      : NOT_EXECUTED", file=sys.stderr)


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8-sig"))


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _append_log(text: str) -> None:
    if ACTIVE_LOG_FILE is None:
        return
    ACTIVE_LOG_FILE.parent.mkdir(parents=True, exist_ok=True)
    with ACTIVE_LOG_FILE.open("a", encoding="utf-8") as handle:
        handle.write(text)
        if text and not text.endswith("\n"):
            handle.write("\n")


def _command_secrets(cmd: list[str]) -> set[str]:
    secrets: set[str] = set()
    for arg in cmd:
        for match in re.finditer(r"(?i)[a-z][a-z0-9+.-]*://([^/@\s]+)@", str(arg)):
            user_info = unquote(match.group(1))
            if ":" in user_info:
                _, password = user_info.split(":", 1)
                if password:
                    secrets.add(password)
            elif user_info:
                secrets.add(user_info)
    return secrets


def _redact_sensitive_text(value: str, secrets: set[str]) -> str:
    redacted = re.sub(
        r"(?i)([a-z][a-z0-9+.-]*://)[^/@\s]+@",
        lambda match: match.group(1) + "***@",
        value,
    )
    for secret in sorted(secrets, key=len, reverse=True):
        redacted = redacted.replace(secret, "***")
    return redacted


def _command_console_label(cmd: list[str]) -> str:
    if not cmd:
        return "명령 실행"
    executable = Path(str(cmd[0])).name.lower()
    args = [str(x).lower() for x in cmd[1:]]
    if executable in {"gradlew", "gradlew.bat"}:
        return "Gradle 빌드/검증/Publication"
    if executable in {"npm", "npm.cmd"}:
        if "ci" in args:
            return "Frontend 의존성 설치(npm ci)"
        return "Frontend 검증(npm)"
    if executable.startswith("python") or executable == Path(sys.executable).name.lower():
        return "CPF 검증 도구 실행(Python)"
    if executable in {"git", "git.exe"}:
        return "Open Git Repository 확인"
    if executable in {"pwsh", "pwsh.exe", "powershell", "powershell.exe"}:
        return "PowerShell 검증 도구 실행"
    return f"{Path(str(cmd[0])).name} 실행"


def _is_gradle_command(cmd: list[str]) -> bool:
    return bool(cmd) and Path(str(cmd[0])).name.lower() in {"gradlew", "gradlew.bat"}


def _should_echo_gradle_line(line: str) -> bool:
    stripped=line.strip()
    if not stripped:
        return False
    important=("FAILURE:", "BUILD FAILED", "BUILD SUCCESSFUL", "* What went wrong:", "Execution failed for task",
               "FAILED", "ERROR", "Exception", "CPF_", "npm error", "npm ERR!")
    return any(token.lower() in stripped.lower() for token in important)


def _utf8_child_env(env: dict[str, str] | None) -> dict[str, str]:
    """자식 프로세스가 UTF-8로 출력하도록 강제한다.

    CPF 도구의 진단 메시지는 한글이다. Windows에서 자식 Python이 locale 인코딩(cp949)으로
    쓰면 이 도구는 errors="replace"로 U+FFFD를 받게 되고, 그 결과가 로그/콘솔로 다시 나갈 때
    Release 전체가 UnicodeEncodeError로 끝난다.
    """
    child = dict(os.environ if env is None else env)
    child.setdefault("PYTHONIOENCODING", "utf-8")
    child.setdefault("PYTHONUTF8", "1")
    return child


def run(cmd: list[str], cwd: Path, *, capture: bool = False, env: dict[str, str] | None = None) -> str:
    env = _utf8_child_env(env)
    secrets = _command_secrets(cmd)
    display_cmd = [_redact_sensitive_text(str(arg), secrets) for arg in cmd]
    command_line = "[CPF][OPEN-GIT][RUN] " + " ".join(display_cmd)
    console_label = _command_console_label(cmd)
    print(f"[CPF][OPEN-GIT][실행] {console_label} (상세 명령/전체 출력: cpf-release/logs/open-git-release.log)", flush=True)
    _append_log(command_line)
    if capture:
        cp = subprocess.run(cmd, cwd=cwd, text=True, encoding="utf-8", errors="replace",
                            capture_output=True, env=env, check=False)
        combined = _redact_sensitive_text((cp.stdout or "") + (cp.stderr or ""), secrets)
        if combined:
            _append_log(combined)
        if cp.returncode:
            if combined:
                sys.stderr.write(combined)
            raise OpenGitReleaseError(f"{console_label} 실패(exit={cp.returncode}). 상세 로그를 확인하세요.")
        return (cp.stdout or "").strip()

    gradle_mode = _is_gradle_command(cmd)
    gradle_tasks = 0
    recent = deque(maxlen=30)
    process = subprocess.Popen(cmd, cwd=cwd, text=True, encoding="utf-8", errors="replace",
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=env, bufsize=1)
    assert process.stdout is not None
    for line in process.stdout:
        safe_line = _redact_sensitive_text(line, secrets)
        _append_log(safe_line.rstrip("\n"))
        recent.append(safe_line)
        if gradle_mode and safe_line.lstrip().startswith("> Task "):
            gradle_tasks += 1
            if gradle_tasks == 1 or gradle_tasks % 100 == 0:
                print(f"[CPF][OPEN-GIT][진행] {ACTIVE_STAGE_NO:02d}/{BUILD_STAGE_TOTAL:02d} {ACTIVE_STAGE_LABEL} - Gradle 내부 작업 {gradle_tasks}건 처리", flush=True)
            continue
        if not gradle_mode or _should_echo_gradle_line(safe_line):
            sys.stdout.write(safe_line)
            sys.stdout.flush()
    code = process.wait()
    if gradle_mode:
        state = "완료" if code == 0 else "실패"
        print(f"[CPF][OPEN-GIT][{state}] {ACTIVE_STAGE_LABEL} - Gradle 내부 작업 {gradle_tasks}건, exit={code}", flush=True)
    if code:
        if gradle_mode:
            tail = "".join(recent)
            if tail:
                sys.stderr.write("[CPF][OPEN-GIT][실패 상세 마지막 출력]\n" + tail)
        raise OpenGitReleaseError(f"{console_label} 실패(exit={code}). 상세 로그를 확인하세요.")
    return ""


def load_module(path: Path, name: str):
    spec = importlib.util.spec_from_file_location(name, path)
    if spec is None or spec.loader is None:
        raise OpenGitReleaseError(f"cannot load release backend: {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def ensure_private_root(root: Path) -> Path:
    root = root.resolve()
    required = [root / "settings.gradle", root / "cpf-tools", root / "cpf-docs"]
    if not all(path.exists() for path in required):
        raise OpenGitReleaseError(f"CPF private root is invalid: {root}")
    return root


def release_root(root: Path) -> Path:
    return root / RELEASE_DIR_NAME


def _is_exact_release_root(root: Path, target: Path) -> bool:
    try:
        return target.resolve(strict=False) == (root.resolve() / RELEASE_DIR_NAME)
    except OSError:
        return False


def verify_release_root_safety(root: Path, *, require_ignore: bool = True) -> Path:
    target = release_root(root)
    if target.is_symlink():
        raise OpenGitReleaseError("refusing release cleanup: cpf-release must not be a symlink")
    if not _is_exact_release_root(root, target):
        raise OpenGitReleaseError("refusing release cleanup: target is not exactly <CPF_ROOT>/cpf-release")
    if require_ignore:
        ignore = root / ".gitignore"
        text = ignore.read_text(encoding="utf-8-sig") if ignore.is_file() else ""
        rules = {line.strip() for line in text.splitlines() if line.strip() and not line.lstrip().startswith("#")}
        if "/cpf-release/" not in rules:
            raise OpenGitReleaseError("/cpf-release/ is not registered in private .gitignore. Run 'cpf-open-git setup' once after Codex work is complete.")
    git = shutil.which("git")
    if git and (root / ".git").exists():
        tracked = run([git, "ls-files", RELEASE_DIR_NAME], root, capture=True)
        if tracked:
            raise OpenGitReleaseError("cpf-release contains private Git tracked paths; refusing generated release cleanup")
    return target


def clean_release_root(root: Path) -> Path:
    target = verify_release_root_safety(root)
    if target.exists():
        shutil.rmtree(target)
    target.mkdir(parents=True, exist_ok=False)
    return target


def canonical_source_state(root: Path) -> dict[str, Any]:
    script = root / "cpf-tools/verification/tools/cpf-source-state.py"
    output = run([sys.executable, "-B", str(script), "--root", str(root), "--scope", "source"], root, capture=True)
    try:
        return json.loads(output.splitlines()[-1])
    except Exception as exc:
        raise OpenGitReleaseError(f"cannot parse CPF source identity: {exc}") from exc


def private_git_context(root: Path) -> dict[str, Any]:
    """Read private Git provenance without mutating or requiring a clean tree.

    The current Local Working Tree is a valid Source Authority for CPF release work.
    Git is provenance only.  cpf-release itself must never be tracked by the private
    development repository.
    """
    git = shutil.which("git")
    if not git or not (root / ".git").exists():
        return {
            "head": "NO_GIT_OFFLINE_SOURCE",
            "branch": "NO_GIT_OFFLINE_SOURCE",
            "statusShort": [],
            "dirty": False,
            "releaseTracked": False,
        }
    inside = run([git, "rev-parse", "--is-inside-work-tree"], root, capture=True)
    if inside.lower() != "true":
        raise OpenGitReleaseError("private source is not a Git working tree")
    tracked = run([git, "ls-files", RELEASE_DIR_NAME], root, capture=True)
    if tracked:
        raise OpenGitReleaseError("cpf-release must not be tracked by the private development repository")
    status_text = run([git, "status", "--short", "--untracked-files=all"], root, capture=True)
    status_lines = [line for line in status_text.splitlines() if line.strip()]
    branch = run([git, "rev-parse", "--abbrev-ref", "HEAD"], root, capture=True)
    head = run([git, "rev-parse", "HEAD"], root, capture=True)
    return {
        "head": head,
        "branch": branch,
        "statusShort": status_lines,
        "dirty": bool(status_lines),
        "releaseTracked": False,
    }


def canonical_remote(root: Path, explicit: str | None) -> str:
    policy = load_json(root / SURFACE_POLICY_REL)
    expected = str(policy.get("repository") or "").strip().strip("/")
    env_name = str(policy.get("gitRemoteEnvironment") or "CPF_OPEN_GIT_REMOTE").strip()
    remote = (explicit or os.environ.get(env_name, "")).strip()
    if not remote:
        raise OpenGitReleaseError(f"Open Git remote is required via --remote or {env_name}; target repository={expected}")
    normalized = remote.rstrip("/").removesuffix(".git").lower()
    expected_lower = expected.lower()
    if not normalized.endswith("/" + expected_lower) and not normalized.endswith(":" + expected_lower):
        raise OpenGitReleaseError(f"Open Git remote target must be {expected}")
    return remote


def artifact_rows(root: Path) -> list[dict[str, Any]]:
    policy = load_json(root / ARTIFACT_POLICY_REL)
    catalog_path = root / str(policy["artifactCatalog"])
    catalog = load_json(catalog_path)
    return list(catalog.get("artifacts") or [])


def _owner_path_matches(owner: str, prefix: str) -> bool:
    normalized_owner = owner.replace("\\", "/").strip("/")
    normalized_prefix = prefix.replace("\\", "/").strip("/")
    return bool(normalized_prefix) and (
        normalized_owner == normalized_prefix or normalized_owner.startswith(normalized_prefix + "/")
    )


def _owner_allowed(owner: str, config: dict[str, Any]) -> bool:
    if any(_owner_path_matches(owner, prefix) for prefix in config.get("denyOwnerPathPrefixes", [])):
        return False
    return any(_owner_path_matches(owner, prefix) for prefix in config.get("allowOwnerPathPrefixes", []))


def artifact_catalog_contract_findings(root: Path) -> list[str]:
    policy = load_json(root / ARTIFACT_POLICY_REL)
    by_id = {str(row.get("artifactId") or ""): row for row in artifact_rows(root)}
    findings: list[str] = []
    public_classes = {"PUBLIC_COMPILE_TIME_JAVA", "PUBLIC_RUNTIME", "PUBLIC_BOM", "PUBLIC_TOOLING", "PUBLIC_STARTER"}
    plugin_id_pattern = re.compile(r"^[a-z][a-z0-9]*(?:[.-][a-z0-9]+)+$")
    for row in artifact_rows(root):
        kind = str(row.get("kind") or "").strip()
        plugin_id = str(row.get("gradlePluginId") or "").strip()
        artifact_id = str(row.get("artifactId") or "").strip()
        if kind == "gradle-plugin" and not plugin_id:
            findings.append(f"Gradle plugin catalog row has no gradlePluginId: {artifact_id}")
        elif plugin_id and kind != "gradle-plugin":
            findings.append(f"gradlePluginId is declared by a non-plugin catalog row: {artifact_id}")
        elif plugin_id and not plugin_id_pattern.fullmatch(plugin_id):
            findings.append(f"invalid Gradle plugin id in artifact catalog: {artifact_id} pluginId={plugin_id}")
    for contract in policy.get("requiredBinaryContracts", []):
        artifact_id = str(contract.get("artifactId") or "").strip()
        row = by_id.get(artifact_id)
        if row is None:
            findings.append(f"required binary artifact missing from catalog: {artifact_id}")
            continue
        publication_class = str(row.get("publicationClass") or "").strip()
        public_group = str(row.get("publicGroupId") or "").strip()
        if publication_class not in public_classes:
            findings.append(f"required binary artifact is not publicly publishable: {artifact_id} publicationClass={publication_class or 'MISSING'}")
        if publication_class != "PUBLIC_BOM" and not public_group:
            findings.append(f"required binary artifact has no Public Maven group: {artifact_id}")
        disclosure = str(contract.get("sourceDisclosure") or "DENY").upper()
        publish_sources = bool(row.get("publishSources"))
        # The canonical public publication can intentionally create documentation
        # artifacts that this stricter Open Git projection removes in stage 8.
        # DENY is therefore enforced on the final repository, not by weakening or
        # contradicting the upstream publication catalog.
        if disclosure == "ALLOW" and not publish_sources:
            findings.append(f"developer source artifact is not cataloged for sources.jar publication: {artifact_id}")
    return findings


def verify_artifact_catalog_contract(root: Path) -> dict[str, Any]:
    findings = artifact_catalog_contract_findings(root)
    if findings:
        raise OpenGitReleaseError("Open Git artifact catalog contract failed:\n" + "\n".join(findings))
    return {"status": "PASS", "requiredBinaryContracts": len(load_json(root / ARTIFACT_POLICY_REL).get("requiredBinaryContracts", []))}


def _derived_gradle_plugin_marker_rows(root: Path) -> dict[tuple[str, str], dict[str, Any]]:
    result: dict[tuple[str, str], dict[str, Any]] = {}
    for row in artifact_rows(root):
        plugin_id = str(row.get("gradlePluginId") or "").strip()
        if not plugin_id:
            continue
        implementation_group = str(row.get("publicGroupId") or "").strip()
        implementation_artifact = str(row.get("artifactId") or "").strip()
        if not implementation_group or not implementation_artifact:
            raise OpenGitReleaseError(
                f"Gradle plugin marker implementation coordinate is incomplete: pluginId={plugin_id}"
            )
        marker_coordinate = (plugin_id, f"{plugin_id}.gradle.plugin")
        marker_row = dict(row)
        marker_row.update({
            "artifactId": marker_coordinate[1],
            "publicGroupId": marker_coordinate[0],
            "derivedPublication": "GRADLE_PLUGIN_MARKER",
            "implementationGroupId": implementation_group,
            "implementationArtifactId": implementation_artifact,
        })
        if marker_coordinate in result:
            raise OpenGitReleaseError(f"duplicate derived Gradle plugin marker coordinate: {marker_coordinate}")
        result[marker_coordinate] = marker_row
    return result


def _artifact_coordinate_map(root: Path) -> dict[tuple[str, str], dict[str, Any]]:
    result: dict[tuple[str, str], dict[str, Any]] = {}
    for row in artifact_rows(root):
        group = str(row.get("publicGroupId") or "").strip()
        artifact = str(row.get("artifactId") or "").strip()
        if group and artifact:
            result[(group, artifact)] = row
    for coordinate, row in _derived_gradle_plugin_marker_rows(root).items():
        if coordinate in result:
            raise OpenGitReleaseError(f"derived Gradle plugin marker collides with catalog coordinate: {coordinate}")
        result[coordinate] = row
    return result


def _repo_coordinate(repo: Path, path: Path) -> tuple[str, str] | None:
    rel = path.relative_to(repo)
    parts = rel.parts
    if len(parts) < 4:
        return None
    artifact = parts[-3]
    group = ".".join(parts[:-3])
    return group, artifact


CHECKSUM_SIDECAR_SUFFIXES = (".md5", ".sha1", ".sha256", ".sha512")
DENIED_JAR_CLASSIFIERS = ("sources", "javadoc")
MAVEN_TIMESTAMP_PATTERN = re.compile(r"(\d{8}\.\d{6}-\d+)")


def public_release_version(development_version: str) -> str:
    """Development SNAPSHOT -> immutable Public Release version."""
    suffix = "-SNAPSHOT"
    return development_version[: -len(suffix)] if development_version.endswith(suffix) else development_version


def _public_artifact_name(name: str, development_version: str, public_version: str) -> str:
    match = MAVEN_TIMESTAMP_PATTERN.search(name)
    projected = name.replace("-" + match.group(1), "") if match else name
    return projected.replace(development_version, public_version)


def _classify_public_artifact(name: str) -> str:
    # Generator 배포본은 OS 별 zip + 그 무결성/메타 json 이 하나의 Public artifact set 이다.
    # 사용자 Golden Path(Generator 실행)에 필수이므로 checksum 판정보다 먼저 분류한다.
    if name.endswith(".zip"):
        return "generator-distribution"
    if name.endswith(".zip.sha256"):
        return "generator-distribution-checksum"
    if name.endswith(".json"):
        return "generator-distribution-manifest"
    if name.endswith(CHECKSUM_SIDECAR_SUFFIXES):
        return "checksum-sidecar"
    if name == "maven-metadata.xml":
        return "maven-metadata"
    if name.endswith(".module"):
        return "gradle-module-metadata"
    if name.endswith(".pom"):
        return "pom"
    if name.endswith(".jar"):
        stem = name[: -len(".jar")]
        for classifier in DENIED_JAR_CLASSIFIERS:
            if stem.endswith("-" + classifier):
                return "denied-classifier-jar"
        return "jar"
    return "unclassified"


def sanitize_binary_repository(root: Path, raw_repo: Path, final_repo: Path, profile: str = "binary",
                               development_version: str = "", source_identity: str = "") -> dict[str, Any]:
    """Allowlist fail-closed projection: staging repository -> Final Public repository."""
    policy = load_json(root / ARTIFACT_POLICY_REL)
    final_tree = policy.get("finalPublicTree") or {}
    allow_module = bool(final_tree.get("admitGradleModuleMetadata", False))
    allow_metadata = bool(final_tree.get("admitMavenMetadata", False))

    if not development_version:
        development_version = platform_version(root)
    public_version = public_release_version(development_version)

    if final_repo.exists():
        shutil.rmtree(final_repo)
    final_repo.mkdir(parents=True)

    coords = _artifact_coordinate_map(root)
    manifest: list[dict[str, Any]] = []
    counters: dict[str, int] = {}
    dropped: list[dict[str, str]] = []

    for source in sorted(p for p in raw_repo.rglob("*") if p.is_file()):
        relative = source.relative_to(raw_repo)
        kind = _classify_public_artifact(source.name)
        admitted = (kind in {"jar", "pom",
                             "generator-distribution",
                             "generator-distribution-checksum",
                             "generator-distribution-manifest"}
                    or (kind == "gradle-module-metadata" and allow_module)
                    or (kind == "maven-metadata" and allow_metadata))
        counters[kind] = counters.get(kind, 0) + 1
        if not admitted:
            dropped.append({"path": relative.as_posix(), "kind": kind})
            continue

        public_name = _public_artifact_name(source.name, development_version, public_version)
        parts = [part.replace(development_version, public_version) for part in relative.parts[:-1]]
        destination = final_repo.joinpath(*parts, public_name)
        destination.parent.mkdir(parents=True, exist_ok=True)
        if kind == "pom":
            projected = source.read_text(encoding="utf-8").replace(
                f"<version>{development_version}</version>", f"<version>{public_version}</version>")
            destination.write_text(projected, encoding="utf-8", newline="\n")
        else:
            shutil.copy2(source, destination)

        relative_out = destination.relative_to(final_repo).as_posix()
        segments = relative_out.split("/")
        group = ".".join(segments[:-3]) if len(segments) >= 3 else ""
        artifact_id = segments[-3] if len(segments) >= 3 else ""
        row = coords.get((group, artifact_id))
        manifest.append({
            "group": group,
            "artifactId": artifact_id,
            "module": str(row.get("ownerPath")) if row else "",
            "version": public_version,
            "classifier": None,
            "type": destination.suffix.lstrip("."),
            "relativePath": relative_out,
            "fileSize": destination.stat().st_size,
            "sha256": sha256(destination),
            "publicationType": str(row.get("publicationClass")) if row else kind,
            "classification": "PUBLIC",
            "sourceIdentitySha256": source_identity,
        })

    manifest_payload = {
        "contract": "CPF_PUBLIC_PACKAGE_MANIFEST",
        "schemaVersion": 1,
        "publicVersion": public_version,
        "developmentVersion": development_version,
        "sourceIdentitySha256": source_identity,
        "artifactCount": len(manifest),
        "artifacts": manifest,
    }
    write_json(final_repo / "package-manifest.json", manifest_payload)

    return {
        "mode": "ALLOWLIST_FAIL_CLOSED",
        "publicVersion": public_version,
        "developmentVersion": development_version,
        "admittedGradleModuleMetadata": allow_module,
        "admittedMavenMetadata": allow_metadata,
        "counters": counters,
        "admittedArtifacts": len(manifest),
        "droppedArtifacts": len(dropped),
        "droppedSample": dropped[:20],
    }


def _pom_coordinate(path: Path) -> tuple[str, str, str, list[tuple[str, str, str]]]:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError as exc:
        raise OpenGitReleaseError(f"invalid POM XML: {path}: {exc}") from exc
    ns = ""
    if root.tag.startswith("{"):
        ns = root.tag.split("}", 1)[0] + "}"
    def text(name: str) -> str:
        node = root.find(ns + name)
        return (node.text or "").strip() if node is not None else ""
    group = text("groupId")
    artifact = text("artifactId")
    version = text("version")
    parent = root.find(ns + "parent")
    if parent is not None:
        if not group:
            node = parent.find(ns + "groupId")
            group = (node.text or "").strip() if node is not None else ""
        if not version:
            node = parent.find(ns + "version")
            version = (node.text or "").strip() if node is not None else ""
    deps: list[tuple[str, str, str]] = []
    dependency_containers = list(root.findall(ns + "dependencies"))
    dependency_containers.extend(root.findall(ns + "dependencyManagement/" + ns + "dependencies"))
    for deps_node in dependency_containers:
        for dep in deps_node.findall(ns + "dependency"):
            def dep_text(name: str) -> str:
                node = dep.find(ns + name)
                return (node.text or "").strip() if node is not None else ""
            deps.append((dep_text("groupId"), dep_text("artifactId"), dep_text("version")))
    return group, artifact, version, deps


def _artifact_exists(repo: Path, group: str, artifact: str, version: str) -> bool:
    if not group or not artifact:
        return False
    base = repo / Path(group.replace(".", "/")) / artifact
    if version and not version.startswith("${"):
        return (base / version).is_dir()
    return base.is_dir() and any(p.is_dir() for p in base.iterdir())


def verify_binary_repository(root: Path, repo: Path, version: str, profile: str = "binary") -> dict[str, Any]:
    if not repo.is_dir():
        raise OpenGitReleaseError(f"Open Git binary repository missing: {repo}")
    policy = load_json(root / ARTIFACT_POLICY_REL)
    coords = _artifact_coordinate_map(root)
    findings: list[str] = []
    source_count = 0
    javadoc_count = 0
    binary_count = 0

    classified_prefixes = [
        ((Path(group.replace(".", "/")) / artifact).as_posix().rstrip("/") + "/", row)
        for (group, artifact), row in coords.items()
    ]
    for path in sorted(p for p in repo.rglob("*") if p.is_file()):
        rel = path.relative_to(repo).as_posix()
        if not any(rel.startswith(prefix) for prefix, _ in classified_prefixes):
            if rel == "package-manifest.json":
                # Final Public Tree 의 canonical Package Manifest. Maven 좌표 밖에 있는 것이 정상이다.
                continue
            findings.append(f"unclassified repository file: {rel}")

    for path in sorted(repo.rglob("*.jar")):
        name = path.name
        coordinate = _repo_coordinate(repo, path)
        row = coords.get(coordinate) if coordinate else None
        if name.endswith("-sources.jar") or name.endswith("-javadoc.jar"):
            kind = "sources" if name.endswith("-sources.jar") else "javadoc"
            if row is None:
                findings.append(f"unclassified {kind} artifact: {path.relative_to(repo)}")
                continue
            owner = str(row.get("ownerPath") or "")
            findings.append(f"forbidden {kind} artifact in Open Git {profile} profile owner={owner}: {path.relative_to(repo)}")
            if kind == "sources": source_count += 1
            else: javadoc_count += 1
        else:
            if row is None:
                findings.append(f"unclassified binary artifact: {path.relative_to(repo)}")
            elif row.get("derivedPublication") == "GRADLE_PLUGIN_MARKER":
                findings.append(f"forbidden binary artifact for POM-only Gradle plugin marker: {path.relative_to(repo)}")
            else:
                binary_count += 1

    poms = sorted(repo.rglob("*.pom"))
    if not poms:
        findings.append("binary repository contains no POM")
    for pom in poms:
        group, artifact, pom_version, deps = _pom_coordinate(pom)
        row = coords.get((group, artifact))
        if not group or not artifact:
            findings.append(f"invalid POM coordinate: {pom.relative_to(repo)}")
        elif row is None:
            findings.append(f"unclassified POM coordinate {group}:{artifact}: {pom.relative_to(repo)}")
        elif row.get("derivedPublication") == "GRADLE_PLUGIN_MARKER":
            expected_dependency = (
                str(row["implementationGroupId"]),
                str(row["implementationArtifactId"]),
                version,
            )
            if pom_version != version:
                findings.append(
                    f"Gradle plugin marker version mismatch expected={version} actual={pom_version}: {pom.relative_to(repo)}"
                )
            if deps != [expected_dependency]:
                findings.append(
                    f"Gradle plugin marker dependency mismatch expected={[expected_dependency]} actual={deps}: {pom.relative_to(repo)}"
                )
        text = pom.read_text(encoding="utf-8", errors="replace")
        if re.search(r"(?i)mavenLocal|private[-_. ]?repo|localhost", text):
            findings.append(f"forbidden private/local repository content: {pom.relative_to(repo)}")
        for dep_group, dep_artifact, dep_version in deps:
            if dep_group.startswith("com.cpf") and not _artifact_exists(repo, dep_group, dep_artifact, dep_version):
                findings.append(f"missing CPF dependency {dep_group}:{dep_artifact}:{dep_version} from {pom.relative_to(repo)}")

    # Required artifacts are checked only when they are classified/published by the current catalog.
    by_id = {str(row.get("artifactId") or ""): row for row in artifact_rows(root)}
    for artifact_id in policy.get("requiredBinaryArtifactIdsWhenPublished", []):
        row = by_id.get(artifact_id)
        if not row or not row.get("publicGroupId"):
            continue
        group = str(row["publicGroupId"])
        base = repo / Path(group.replace(".", "/")) / artifact_id / version
        if not base.is_dir():
            findings.append(f"required published binary artifact missing: {group}:{artifact_id}:{version}")

    marker_rows = _derived_gradle_plugin_marker_rows(root)
    for (group, artifact), _ in marker_rows.items():
        base = repo / Path(group.replace(".", "/")) / artifact / version
        if not base.is_dir():
            findings.append(f"required Gradle plugin marker missing: {group}:{artifact}:{version}")

    if findings:
        raise OpenGitReleaseError("Open Git binary policy failed:\n" + "\n".join(findings[:200]))
    return {
        "status": "PASS",
        "binaryJarCount": binary_count,
        "sourceJarCount": source_count,
        "javadocJarCount": javadoc_count,
        "pomCount": len(poms),
        "gradlePluginMarkerCount": len(marker_rows),
    }


def _public_source_matches(root: Path, allowlist: dict[str, Any]) -> list[Path]:
    files: set[Path] = set()
    for rule in allowlist.get("rules", []):
        pattern = str(rule.get("pattern") or "").replace("\\", "/")
        matches = sorted(p for p in root.glob(pattern) if p.is_file())
        if rule.get("required") and not matches:
            raise OpenGitReleaseError(f"required public source allowlist rule matched 0 files: {pattern}")
        files.update(matches)
    return sorted(files)


def project_optional_framework_sources(root: Path, open_git: Path, profile: str) -> dict[str, Any]:
    target = open_git / "framework-source"
    if target.exists():
        shutil.rmtree(target)
    if profile == "binary":
        return {"profile": profile, "fileCount": 0, "target": None}
    if profile != "source":
        raise OpenGitReleaseError(f"unsupported Open Git release profile: {profile}")
    allowlist = load_json(root / PUBLIC_SOURCE_ALLOWLIST_REL)
    if allowlist.get("defaultPolicy") != "DENY":
        raise OpenGitReleaseError("Open Git public source allowlist must be default-deny")
    forbidden_segments = set(map(str, allowlist.get("forbiddenPathSegments", [])))
    forbidden_prefixes = tuple(map(str, allowlist.get("forbiddenPathPrefixes", [])))
    files = _public_source_matches(root, allowlist)
    copied = 0
    for source in files:
        rel = source.relative_to(root).as_posix()
        parts = set(source.relative_to(root).parts)
        if rel.startswith(forbidden_prefixes) or parts.intersection(forbidden_segments):
            raise OpenGitReleaseError(f"forbidden internal source matched public allowlist: {rel}")
        if source.suffix.lower() != ".java":
            raise OpenGitReleaseError(f"non-Java framework source matched public allowlist: {rel}")
        dest = target / source.relative_to(root)
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, dest)
        copied += 1
    if copied == 0:
        raise OpenGitReleaseError("Open Git source profile produced no framework public source")
    return {"profile": profile, "fileCount": copied, "target": str(target)}


def _is_forbidden_public_document(root: Path, relative_path: str) -> bool:
    """Public Documentation Allowlist 밖의 cpf-docs 파일인지 판정한다."""
    policy = load_json(root / SURFACE_POLICY_REL)
    for prefix in policy.get("forbiddenPathPrefixes", []):
        if relative_path.startswith(prefix):
            return True
    allowed = []
    for rule in policy.get("sourceRules", []):
        pattern = str(rule.get("pattern") or "")
        if not pattern.startswith("cpf-docs/"):
            continue
        allowed.append(pattern[:-3] if pattern.endswith("/**") else pattern)
    if not allowed:
        return True
    return not any(relative_path == item or relative_path.startswith(item.rstrip("/") + "/") for item in allowed)


def verify_framework_source_projection(root: Path, open_git: Path, profile: str) -> dict[str, Any]:
    target = open_git / "framework-source"
    if profile == "binary":
        if target.exists():
            raise OpenGitReleaseError("binary profile must not contain framework-source")
        return {"profile": profile, "fileCount": 0}
    allowlist = load_json(root / PUBLIC_SOURCE_ALLOWLIST_REL)
    expected = {p.relative_to(root).as_posix() for p in _public_source_matches(root, allowlist)}
    if not target.is_dir():
        raise OpenGitReleaseError("source profile framework-source projection missing")
    actual = {p.relative_to(target).as_posix() for p in target.rglob("*.java") if p.is_file()}
    if actual != expected:
        missing = sorted(expected - actual)[:20]
        extra = sorted(actual - expected)[:20]
        raise OpenGitReleaseError(f"source profile allowlist projection drift missing={missing} extra={extra}")
    for rel in sorted(actual):
        parts = set(Path(rel).parts)
        if parts.intersection(set(map(str, allowlist.get("forbiddenPathSegments", [])))):
            raise OpenGitReleaseError(f"internal source leaked into source profile: {rel}")
    non_java = [p.relative_to(target).as_posix() for p in target.rglob("*") if p.is_file() and p.suffix.lower() != ".java"]
    if non_java:
        raise OpenGitReleaseError(f"non-source file leaked into framework-source: {non_java[:20]}")
    return {"profile": profile, "fileCount": len(actual)}


def bundle_public_binary_repository(final_repo: Path, staging: Path) -> dict[str, Any]:
    """Final Public Binary Repository 를 Open Git Tree 안으로 동봉한다.

    open-git-surface-policy 의 binaryRepositoryDirectory 계약은 checkout 안에 Public Binary
    Repository 가 있어야 한다고 규정한다. 이전 구현은 binary-repository 를 open-git 과 형제
    디렉터리로만 만들어, clone 한 사용자는 CPF Binary 를 받을 경로가 없고 README 의
    <cpf-binary-repository-url> placeholder 를 직접 채워야 했다.
    """
    if not final_repo.is_dir():
        raise OpenGitReleaseError(f"final public binary repository is missing: {final_repo}")
    target = staging / BINARY_DIR_NAME
    if target.exists():
        shutil.rmtree(target)
    shutil.copytree(final_repo, target)
    files = [p for p in target.rglob("*") if p.is_file()]
    jars = [p for p in files if p.suffix == ".jar"]
    poms = [p for p in files if p.suffix == ".pom"]
    manifest = target / "package-manifest.json"
    if not manifest.is_file():
        raise OpenGitReleaseError("bundled public binary repository has no package-manifest.json")
    return {
        "directory": BINARY_DIR_NAME,
        "fileCount": len(files),
        "jarCount": len(jars),
        "pomCount": len(poms),
        "packageManifest": str(manifest.relative_to(staging).as_posix()),
    }


def verify_public_binary_repository_tree(repository: Path) -> None:
    """Final Public Binary Repository fail-closed 검증."""
    if not repository.is_dir():
        raise OpenGitReleaseError(f"bundled public binary repository missing: {repository}")
    manifest_path = repository / "package-manifest.json"
    if not manifest_path.is_file():
        raise OpenGitReleaseError("bundled public binary repository has no package-manifest.json")
    manifest = load_json(manifest_path)

    forbidden: list[str] = []
    for path in repository.rglob("*"):
        if not path.is_file() or path == manifest_path:
            continue
        name = path.name
        relative = path.relative_to(repository).as_posix()
        if name.endswith((".zip", ".zip.sha256")) or name.endswith(".json"):
            # Generator OS 배포본과 그 무결성/메타 파일은 승인된 Public artifact 다.
            continue
        if name.endswith(CHECKSUM_SIDECAR_SUFFIXES):
            forbidden.append(f"checksum-sidecar:{relative}")
        elif name == "maven-metadata.xml":
            forbidden.append(f"maven-metadata:{relative}")
        elif name.endswith(".module"):
            forbidden.append(f"gradle-module-metadata:{relative}")
        elif MAVEN_TIMESTAMP_PATTERN.search(name):
            forbidden.append(f"timestamped-artifact:{relative}")
        elif "-SNAPSHOT" in name:
            forbidden.append(f"snapshot-artifact:{relative}")
        elif name.endswith(("-sources.jar", "-javadoc.jar")):
            forbidden.append(f"denied-classifier:{relative}")
    if forbidden:
        raise OpenGitReleaseError(f"forbidden public artifacts in Open Git: {forbidden[:15]}")

    listed = {str(row["relativePath"]) for row in manifest.get("artifacts", [])}
    actual = {p.relative_to(repository).as_posix() for p in repository.rglob("*")
              if p.is_file() and p != manifest_path}
    unlisted = sorted(actual - listed)
    absent = sorted(listed - actual)
    if unlisted:
        raise OpenGitReleaseError(f"public artifact not registered in package manifest: {unlisted[:15]}")
    if absent:
        raise OpenGitReleaseError(f"package manifest entry has no file: {absent[:15]}")
    for row in manifest.get("artifacts", []):
        target = repository / str(row["relativePath"])
        if sha256(target) != str(row["sha256"]):
            raise OpenGitReleaseError(f"package manifest SHA-256 mismatch: {row['relativePath']}")


def verify_public_launcher_parity(open_git: Path) -> None:
    """Windows/Linux launcher 는 같은 lifecycle 집합을 제공해야 한다."""
    bin_dir = open_git / "bin"
    windows = {p.stem for p in bin_dir.glob("cpf-*.ps1")}
    linux = {p.name[: -len(".sh")] for p in bin_dir.glob("cpf-*.sh")}
    if windows != linux:
        raise OpenGitReleaseError(
            f"Windows/Linux launcher parity broken: windows-only={sorted(windows - linux)} "
            f"linux-only={sorted(linux - windows)}")
    required = {"cpf-start", "cpf-stop", "cpf-status", "cpf-restart", "cpf-health", "cpf-log", "cpf-help"}
    missing = sorted(required - windows)
    if missing:
        raise OpenGitReleaseError(f"public runtime launcher missing: {missing}")


def verify_public_readme(open_git: Path) -> None:
    """README 는 checkout 만으로 실행 가능한 Golden Path 를 제시해야 한다."""
    readme = open_git / "README.md"
    text = readme.read_text(encoding="utf-8")
    placeholders = re.findall(r"<[a-z][a-z-]*-(?:url|version)>", text)
    if placeholders:
        raise OpenGitReleaseError(f"README still contains placeholders: {sorted(set(placeholders))}")
    for reference in sorted(set(re.findall(r"`(cpf-docs/[^`]+)`", text))):
        if not (open_git / reference).exists():
            raise OpenGitReleaseError(f"README references a missing document: {reference}")


def verify_open_git_tree(root: Path, open_git: Path, profile: str = "binary") -> dict[str, Any]:
    # Public Product Distribution 필수 구성. Binary 만 있고 실행/문서가 없으면 Release 가 아니다.
    required = ["cpf-education", "bin", "README.md", BINARY_DIR_NAME, "cpf-docs"]
    missing = [item for item in required if not (open_git / item).exists()]
    if missing:
        raise OpenGitReleaseError(f"Open Git required paths missing: {missing}")
    verify_public_binary_repository_tree(open_git / BINARY_DIR_NAME)
    verify_public_launcher_parity(open_git)
    verify_public_readme(open_git)
    forbidden_roots = ["cpf-core", "cpf-common", "cpf-admin", "cpf-biz-admin", "cpf-batch", "cpf-gateway", "cpf-starters", "cpf-tools"]
    leaked = [name for name in forbidden_roots if (open_git / name).exists()]
    if leaked:
        raise OpenGitReleaseError(f"private framework source leaked into Open Git: {leaked}")
    # cpf-docs 는 root 통째 차단이 아니라 Public Documentation Allowlist 로 관리한다.
    # governance/work 같은 내부 관리자료가 한 건이라도 섞이면 Leakage 다.
    leaked_docs = sorted(
        path.relative_to(open_git).as_posix()
        for path in (open_git / "cpf-docs").rglob("*")
        if path.is_file() and _is_forbidden_public_document(root, path.relative_to(open_git).as_posix())
    ) if (open_git / "cpf-docs").is_dir() else []
    if leaked_docs:
        raise OpenGitReleaseError(f"internal documentation leaked into Open Git: {leaked_docs[:10]}")
    framework_source = verify_framework_source_projection(root, open_git, profile)
    if (open_git / "domains").exists():
        raise OpenGitReleaseError("duplicate Open Git domains catalog is forbidden; physical cpf-<domain> projects are authoritative")
    selected_domains=[]; seen_names=set(); seen_codes=set()
    for project in sorted(p for p in open_git.glob("cpf-*") if p.is_dir()):
        contract=project/"gradle.properties"
        if not contract.is_file(): continue
        values={}
        for raw in contract.read_text(encoding="utf-8-sig").splitlines():
            line=raw.strip()
            if not line or line.startswith("#") or "=" not in line: continue
            k,v=line.split("=",1); values[k.strip()]=v.strip()
        if values.get("cpf.domain.contractVersion")!="1": continue
        name=values.get("cpf.domain.name",""); code=values.get("cpf.domain.systemCode","")
        if project.name!=f"cpf-{name}" or not code: raise OpenGitReleaseError(f"Open Git Domain Developer contract invalid: {project.name}")
        if name in seen_names or code in seen_codes: raise OpenGitReleaseError(f"duplicate Open Git Domain identity: {name}/{code}")
        seen_names.add(name); seen_codes.add(code); selected_domains.append(project.name)
        for forbidden in ("cpf-domain.yaml", "cpf-generator.lock.json"):
            if (project / forbidden).exists(): raise OpenGitReleaseError(f"Generator metadata leaked into Open Git: {project.name}/{forbidden}")
    archives = []
    for path in open_git.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in {".jar", ".war"}: continue
        rel = path.relative_to(open_git).as_posix()
        if rel in {"gradle/wrapper/gradle-wrapper.jar", "bin/lib/cpf-cli.jar"}: continue
        # bundled Public Binary Repository 는 Package Manifest 와 Allowlist 로 관리되는 정식
        # 배포물이다. Source Workspace 에 흘러든 누적 JAR 과 구분한다.
        if rel.startswith(BINARY_DIR_NAME + "/"): continue
        archives.append(rel)
    if archives: raise OpenGitReleaseError(f"Open Git Source Workspace contains accumulated CPF/application JAR/WAR: {archives[:20]}")
    edu_build = open_git / "cpf-education/build.gradle"
    if edu_build.is_file() and "project(" in edu_build.read_text(encoding="utf-8"):
        raise OpenGitReleaseError("Open Git EDU build must consume published CPF artifacts, not private project dependencies")
    return {"status":"PASS","profile":profile,"requiredPaths":len(required),"selectedDomains":selected_domains,"domainState":"NOT_SELECTED" if not selected_domains else "SELECTED","forbiddenRootLeakage":0,"jarWarCount":0,"frameworkSourceFiles":framework_source.get("fileCount",0)}


def write_file_manifest(root: Path, target: Path, output: Path) -> int:
    rows = []
    for path in sorted(p for p in target.rglob("*") if p.is_file() and ".git" not in p.relative_to(target).parts):
        rows.append({
            "path": path.relative_to(target).as_posix(),
            "sizeBytes": path.stat().st_size,
            "sha256": sha256(path),
        })
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=["path", "sizeBytes", "sha256"])
        writer.writeheader(); writer.writerows(rows)
    return len(rows)


def write_artifact_manifest(repo: Path, output: Path) -> int:
    rows = []
    for path in sorted(p for p in repo.rglob("*") if p.is_file()):
        rows.append({"path": path.relative_to(repo).as_posix(), "sizeBytes": path.stat().st_size, "sha256": sha256(path)})
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=["path", "sizeBytes", "sha256"])
        writer.writeheader(); writer.writerows(rows)
    return len(rows)


def write_status_md(path: Path, result: dict[str, Any]) -> None:
    lines = [
        "# CPF Open Git Release Status",
        "",
        f"- Status: **{result.get('result', result.get('status'))}**",
        f"- Source Identity: `{result.get('sourceIdentitySha256', '')}`",
        f"- Private Repository Root: `{result.get('privateRepositoryRoot', '')}`",
        f"- Private Git Branch: `{result.get('privateGitBranch', '')}`",
        f"- Private Git SHA: `{result.get('privateGitSha', '')}`",
        f"- Private Working Tree Dirty: {result.get('privateGitDirty', False)}",
        f"- Private cpf-release Tracked: {result.get('privateMasterReleaseTracked', False)}",
        f"- Platform Version: `{result.get('platformVersion', '')}`",
        f"- Release Profile: `{result.get('releaseProfile', 'binary')}`",
        f"- Open Git Files: {result.get('openGitFileCount', 0)}",
        f"- Binary Repository Files: {result.get('binaryFileCount', 0)}",
        f"- Open Git Working Tree Changes: {result.get('changedFiles', 0)}",
        f"- Commit Executed: {result.get('commitExecuted', False)}",
        f"- Push Executed: {result.get('pushExecuted', False)}",
        f"- User Review Required: {result.get('userReviewRequired', True)}",
        "",
        "## Private Git Status --short",
        "",
        *( ["```text", *result.get('privateGitStatusShort', []), "```"] if result.get('privateGitStatusShort') else ["`(clean or Git unavailable in offline source)`"] ),
        "",
        "## Paths",
        "",
        f"- Open Git: `{result.get('openGit', '')}`",
        f"- Binary Repository: `{result.get('binaryRepository', '')}`",
        "",
        "Release Tool은 git add/commit/push를 실행하지 않습니다. VERIFIED 결과를 사용자가 검토한 뒤 cpf-release/open-git에서 직접 Git 반영합니다.",
    ]
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_user_git_commands(path: Path, result: dict[str, Any]) -> None:
    """Write executable user-review Git commands without executing Git writes.

    These commands target only the Fresh Open Git working repository. cpf-release is
    never a Private master commit target. The file is generated only after VERIFIED.
    """
    if result.get("result") not in {"VERIFIED", "VERIFIED_NO_CHANGES"}:
        return
    open_git = str(result.get("openGit") or "").replace("'", "''")
    lines = [
        "# CPF Open Git — User Git Commands",
        "",
        "> Release Tool은 아래 명령을 실행하지 않습니다. 모든 Release Gate PASS와 사용자 검토 후 Open Git에서만 직접 실행합니다.",
        "> `cpf-release/` 결과는 CPF Private master Commit/Push 대상이 아닙니다.",
        "",
        "## PowerShell",
        "",
        "```powershell",
        f"Set-Location -LiteralPath '{open_git}'",
        "git status --short",
        "git diff --check",
        "$branch=(git branch --show-current).Trim(); if([string]::IsNullOrWhiteSpace($branch)){throw 'OPEN GIT BRANCH NOT RESOLVED'}",
        "git add -A",
        "git diff --cached --check",
        "git status --short",
        'git commit -m "CPF Open Git $(Get-Date -Format yyyyMMdd_HHmmss)"',
        "git push origin $branch",
        'Write-Host "PUSHED_SHA=$((git rev-parse HEAD).Trim())"',
        "git status --short",
        "```",
        "",
        "## POSIX shell",
        "",
        "```bash",
        f"cd -- '{open_git}'",
        "git status --short",
        "git diff --check",
        'branch=$(git branch --show-current); test -n "$branch"',
        "git add -A",
        "git diff --cached --check",
        "git status --short",
        'git commit -m "CPF Open Git $(date +%Y%m%d_%H%M%S)"',
        'git push origin "$branch"',
        "printf 'PUSHED_SHA=%s\\n' \"$(git rev-parse HEAD)\"",
        "git status --short",
        "```",
    ]
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")

def platform_version(root: Path) -> str:
    props = root / "gradle/cpf-platform.properties"
    for line in props.read_text(encoding="utf-8-sig").splitlines():
        if line.strip().startswith("platformVersion="):
            value = line.split("=", 1)[1].strip()
            if value: return value
    raise OpenGitReleaseError("platformVersion missing")


def _legacy_backend(root: Path):
    return load_module(root / LEGACY_PUBLIC_REL / "publish-cpf-public-repository.py", "cpf_legacy_public_release_backend")


def build_cross_platform_cli(root: Path, staging: Path, source_identity: str, version: str) -> dict[str, Any]:
    """Build the single Java CPF CLI implementation into the Open Git workspace.

    The default Binary Profile must never project these Java sources. Only the compiled
    cpf-cli.jar plus thin OS wrappers are customer-visible.
    """
    sources = [
        root / "cpf-tools/runtime/cli/java/CpfCli.java",
        root / "cpf-tools/runtime/bootstrap/CpfBootstrap.java",
        root / "cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java",
        root / "cpf-tools/runtime/cli/java/CpfRuntimeTargets.java",
    ]
    missing = [str(path.relative_to(root)) for path in sources if not path.is_file()]
    if missing:
        raise OpenGitReleaseError(f"CPF Java CLI source missing: {missing}")
    java_home = os.environ.get("JAVA_HOME", "").strip()
    javac = Path(java_home) / "bin" / ("javac.exe" if os.name == "nt" else "javac") if java_home else Path(shutil.which("javac") or "")
    jar = Path(java_home) / "bin" / ("jar.exe" if os.name == "nt" else "jar") if java_home else Path(shutil.which("jar") or "")
    if not javac.is_file() or not jar.is_file():
        raise OpenGitReleaseError("Java JDK javac/jar is required to build cpf-cli.jar")
    target = staging / "bin/lib/cpf-cli.jar"
    target.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="cpf-open-git-cli-") as td:
        work = Path(td); classes = work / "classes"; classes.mkdir()
        javac_version = run([str(javac), "-version"], root, capture=True)
        match = re.search(r"(?:javac\s+)?(\d+)(?:[.\s]|$)", javac_version)
        if not match or int(match.group(1)) != 25:
            raise OpenGitReleaseError(f"Java 25 javac is required to build cpf-cli.jar, actual={javac_version or 'UNKNOWN'}")
        run([str(javac), "--release", "25", "-encoding", "UTF-8", "-Xlint:all", "-Werror", "-d", str(classes), *map(str, sources)], root)
        (classes / "cpf-cli.properties").write_text(
            f"version={version}\nsourceIdentitySha256={source_identity}\ncapabilityProfile=PUBLIC\nrequiredJavaFeature=25\n", encoding="utf-8", newline="\n"
        )
        run([str(jar), "--create", "--file", str(target), "--main-class", "CpfCli", "-C", str(classes), "."], root)
    if not target.is_file():
        raise OpenGitReleaseError("cpf-cli.jar build did not produce output")
    return {"status": "PASS", "path": "bin/lib/cpf-cli.jar", "sha256": sha256(target), "sourceIdentitySha256": source_identity}


def verify_cross_platform_cli(open_git: Path, expected_source_identity: str | None = None) -> dict[str, Any]:
    required = ["bin/cpf", "bin/cpf.cmd", "bin/cpf.ps1", "bin/lib/cpf-cli.jar"]
    missing = [path for path in required if not (open_git / path).is_file()]
    if missing:
        raise OpenGitReleaseError(f"CPF cross-platform CLI missing: {missing}")
    # CLI 유출 검사는 bin/ 영역으로 한정한다. Stage 10 은 Generated Domain / Backoffice / EDU 를
    # 의도적으로 projection 하며 verify_open_git_tree 가 cpf-education 을 required 로 요구한다.
    # private framework root 유출은 verify_open_git_tree 의 forbidden_roots 가 별도로 담당한다.
    cli_root = open_git / "bin"
    forbidden_sources = [
        path.relative_to(open_git).as_posix()
        for path in (cli_root.rglob("*.java") if cli_root.is_dir() else [])
        if "framework-source" not in path.relative_to(open_git).parts
    ]
    if forbidden_sources:
        raise OpenGitReleaseError(f"CPF CLI/internal Java source leaked into Open Git: {forbidden_sources[:20]}")
    shell = (open_git / "bin/cpf").read_text(encoding="utf-8")
    cmd = (open_git / "bin/cpf.cmd").read_text(encoding="utf-8")
    ps1 = (open_git / "bin/cpf.ps1").read_text(encoding="utf-8")
    for name, text in (("bin/cpf", shell), ("bin/cpf.cmd", cmd), ("bin/cpf.ps1", ps1)):
        if "cpf-cli.jar" not in text or "java" not in text.lower():
            raise OpenGitReleaseError(f"CPF wrapper is not Java CLI thin wrapper: {name}")
        forbidden_logic = ("docker compose", "gradlew", "domain new", "domain sync")
        if any(token in text.lower() for token in forbidden_logic):
            raise OpenGitReleaseError(f"CPF wrapper contains duplicated business logic: {name}")
    cli_jar = open_git / "bin/lib/cpf-cli.jar"
    with zipfile.ZipFile(cli_jar) as archive:
        names = set(archive.namelist())
        leaked = sorted(name for name in names if name.endswith(".java"))
        if leaked:
            raise OpenGitReleaseError(f"CPF CLI implementation source leaked inside cpf-cli.jar: {leaked[:20]}")
        required_classes = {"CpfCli.class", "CpfBootstrap.class", "CpfGeneratorLauncher.class", "cpf-cli.properties"}
        missing_entries = sorted(required_classes - names)
        if missing_entries:
            raise OpenGitReleaseError(f"cpf-cli.jar canonical implementation entries missing: {missing_entries}")
    java_home = os.environ.get("JAVA_HOME", "").strip()
    java_candidate = Path(java_home) / "bin" / ("java.exe" if os.name == "nt" else "java") if java_home else None
    java = str(java_candidate) if java_candidate and java_candidate.is_file() else shutil.which("java")
    version_result = None
    if java:
        verify_env = dict(os.environ)
        # Release verification must read the identity embedded in this JAR. Full-runtime orchestration
        # may export CPF_SOURCE_IDENTITY for other children; allowing that override here creates a false mismatch.
        verify_env.pop("CPF_SOURCE_IDENTITY", None)
        cp = subprocess.run([java, "-jar", str(cli_jar), "version"], cwd=open_git, env=verify_env,
                            text=True, encoding="utf-8", errors="replace", capture_output=True, check=False)
        if cp.returncode != 0:
            raise OpenGitReleaseError(f"cpf-cli.jar version execution failed: {cp.stdout}{cp.stderr}")
        version_result = cp.stdout
        if expected_source_identity and f"SOURCE_IDENTITY={expected_source_identity}" not in cp.stdout:
            raise OpenGitReleaseError("cpf-cli.jar Source Identity mismatch")
    return {"status": "PASS", "requiredFiles": len(required), "javaExecution": "PASS" if version_result is not None else "NOT_EXECUTED", "sourceLeakage": 0}


def _prepare_workspace(root: Path, staging: Path, source_identity: str, env: dict[str, str]) -> dict[str, Any]:
    prepare = root / LEGACY_PUBLIC_REL / "prepare-cpf-public-workspace.py"
    policy = root / SURFACE_POLICY_REL
    run([
        sys.executable, "-B", str(prepare), "--root", str(root), "--staging", str(staging),
        "--policy", str(policy), "--source-identity", source_identity
    ], root, env=env)
    ready = load_json(staging / ".cpf-public/READY.json")
    if ready.get("status") != "PASS":
        raise OpenGitReleaseError("Open Git staging is not READY")
    return ready


def build_release(root: Path, remote_arg: str | None, generator_artifacts: str | None, *, profile: str = "binary", skip_build: bool = False) -> dict[str, Any]:
    root = ensure_private_root(root)
    if profile not in {"binary", "source"}:
        raise OpenGitReleaseError(f"unsupported Open Git release profile: {profile}")

    # Every accepted build attempt invalidates the previous generated release first.
    # Safety checks run before deletion and cleanup is restricted to exact <CPF_ROOT>/cpf-release.
    release_stage(1, "Release 작업공간 안전 확인", "이전 생성물 전체 재생성 준비")
    release = clean_release_root(root)
    reports = release / REPORTS_DIR_NAME; reports.mkdir()
    logs = release / LOGS_DIR_NAME; logs.mkdir()
    global ACTIVE_LOG_FILE
    ACTIVE_LOG_FILE = logs / "open-git-release.log"
    _append_log(f"[CPF][OPEN-GIT] START {datetime.now(timezone.utc).isoformat()}")
    work = release / WORK_DIR_NAME; work.mkdir()
    raw_repo = work / "binary-repository-raw"
    staging = work / "open-git-staging"
    final_repo = release / BINARY_DIR_NAME
    open_git = release / OPEN_GIT_DIR_NAME

    release_stage(2, "개발 Source 확인", "Local Working Tree / Git provenance / Source Identity / Version / Remote")
    private_git = private_git_context(root)
    private_git_sha = str(private_git["head"])
    source_state = canonical_source_state(root)
    source_identity = str(source_state["contentSha256"])
    version = platform_version(root)
    remote = canonical_remote(root, remote_arg)

    release_stage(3, "공개 Artifact 정책 확인", f"profile={profile} / sources.jar=DENY / javadoc.jar=DENY")
    verify_artifact_catalog_contract(root)

    backend = _legacy_backend(root)
    backend.run = lambda cmd, cwd, capture=False, env=None: run(cmd, Path(cwd), capture=capture, env=env)
    if skip_build:
        raise OpenGitReleaseError("--skip-build is reserved for tests and cannot create a production Open Git release")

    release_stage(4, "Private Release 사전검증", "Secret / Leakage / Canonical release prerequisites")
    backend.private_gates(root, sys.executable)

    release_stage(5, "Framework Binary 생성·Publication", f"version={version}")
    backend.private_build_and_publication(root, raw_repo, version)

    release_stage(6, "Generator Windows/Linux 배포본 생성", "Windows/Linux 공개 Generator 산출물")
    generator_result = backend.publish_generator_distributions(
        root, raw_repo, version, Path(generator_artifacts).resolve() if generator_artifacts else None
    )

    release_stage(7, "원본 Binary Repository 검증", "Publication closure")
    old_verifier = root / LEGACY_PUBLIC_REL / "verify-cpf-public-binary-repository.py"
    run([sys.executable, "-B", str(old_verifier), "--root", str(root), "--repository", str(raw_repo), "--version", version], root)

    release_stage(8, "공개 Artifact 필터 적용", f"profile={profile} / Binary만 유지")
    sanitize_result = sanitize_binary_repository(
        root, raw_repo, final_repo, profile,
        development_version=version, source_identity=source_identity)

    release_stage(9, "최종 Binary Repository 검증", "Maven 좌표 / Dependency / Source disclosure")
    # Final Tree 는 immutable Public version 으로 투영되어 있으므로 development SNAPSHOT
    # 기준으로 검증하면 Gradle plugin marker version 이 어긋난다.
    binary_result = verify_binary_repository(
        root, final_repo, public_release_version(version), profile)

    release_stage(10, "Open Git 공개 Source 구성", "Generated Domain / Backoffice / EDU / Developer Command")
    env = dict(os.environ)
    env["CPF_MAVEN_REPOSITORY_URL"] = final_repo.resolve().as_uri()
    # Fresh Consumer 는 bundled Public Binary Repository 를 쓰므로 immutable Public version 을 본다.
    env["CPF_VERSION"] = public_release_version(version)
    ready = _prepare_workspace(root, staging, source_identity, env)
    cli_result = build_cross_platform_cli(root, staging, source_identity, version)
    bundled_result = bundle_public_binary_repository(final_repo, staging)
    env["CPF_MAVEN_REPOSITORY_URL"] = (staging / BINARY_DIR_NAME).resolve().as_uri()
    source_projection = project_optional_framework_sources(root, staging, profile)
    verify_open_git_tree(root, staging, profile)
    verify_cross_platform_cli(staging, source_identity)

    release_stage(11, "Open Git Fresh Clone 준비", "이전 Open Git Working Copy 재사용 금지")
    git = shutil.which("git") or "git"
    run([git, "clone", "--no-tags", remote, str(open_git)], release)
    if run([git, "status", "--porcelain=v1", "--untracked-files=all"], open_git, capture=True):
        raise OpenGitReleaseError("fresh Open Git clone unexpectedly dirty")
    backend.sync_public_surface(staging, open_git)
    verify_open_git_tree(root, open_git, profile)
    verify_cross_platform_cli(open_git, source_identity)

    release_stage(12, "Fresh Workspace 빌드·테스트", "Open Git + isolated Binary Repository")
    verifier = open_git / "tools" / ("verify-open-git-workspace.ps1" if os.name == "nt" else "verify-open-git-workspace.sh")
    if os.name == "nt":
        shell = shutil.which("pwsh") or shutil.which("powershell")
        if not shell:
            raise OpenGitReleaseError("PowerShell is required for Open Git workspace verification")
        run([shell, "-NoProfile", "-File", str(verifier)], open_git, env=env)
    else:
        run(["bash", str(verifier)], open_git, env=env)

    release_stage(13, "Open Git 변경사항 검증", "Git index/write 없이 diff/status 확인; 사용자 검토 전 add/commit/push 금지")
    run([git, "diff", "--check"], open_git)
    open_git_status = run([git, "status", "--short", "--untracked-files=all"], open_git, capture=True)
    changed = [line for line in open_git_status.splitlines() if line.strip()]

    release_stage(14, "Manifest·SHA·최종 상태 생성", "VERIFIED 결과와 사용자 Git 반영용 read-only 정보 생성")
    open_file_count = write_file_manifest(root, open_git, reports / "OPEN_GIT_FILE_MANIFEST.csv")
    binary_file_count = write_artifact_manifest(final_repo, reports / "OPEN_GIT_ARTIFACT_MANIFEST.csv")
    write_json(reports / "OPEN_GIT_ARTIFACT_FILTER_RESULT.json", sanitize_result)
    write_json(reports / "OPEN_GIT_BINARY_VERIFY_RESULT.json", binary_result)

    result = {
        "schemaVersion": 1,
        "status": "PASS",
        "result": "VERIFIED" if changed else "VERIFIED_NO_CHANGES",
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "sourceIdentitySha256": source_identity,
        "privateRepositoryRoot": str(root),
        "sourceFileCount": source_state.get("fileCount"),
        "privateGitSha": private_git_sha,
        "privateGitBranch": private_git.get("branch"),
        "privateGitStatusShort": private_git.get("statusShort", []),
        "privateGitDirty": private_git.get("dirty", False),
        "privateMasterReleaseTracked": private_git.get("releaseTracked", False),
        "platformVersion": version,
        "releaseProfile": profile,
        "frameworkSourceProjection": source_projection,
        "releaseRoot": str(release),
        "openGit": str(open_git),
        "binaryRepository": str(final_repo),
        "bundledBinaryRepository": bundled_result,
        "openGitFileCount": open_file_count,
        "binaryFileCount": binary_file_count,
        "changedFiles": len(changed),
        "openGitStatusShort": changed,
        "publicStagingFileCount": ready.get("fileCount"),
        "generatorDistribution": generator_result,
        "crossPlatformCli": cli_result,
        "sourceJarCount": binary_result.get("sourceJarCount"),
        "javadocJarCount": binary_result.get("javadocJarCount"),
        "gitAddExecuted": False,
        "commitExecuted": False,
        "pushExecuted": False,
        "userReviewRequired": True,
    }
    write_json(reports / "OPEN_GIT_RELEASE_STATUS.json", result)
    write_status_md(reports / "OPEN_GIT_RELEASE_STATUS.md", result)
    write_user_git_commands(reports / "OPEN_GIT_USER_GIT_COMMANDS.md", result)
    # Successful release leaves only user-visible deliverables/reports/logs.
    shutil.rmtree(work)
    return result


def check_release(root: Path, profile: str = "binary") -> dict[str, Any]:
    root = ensure_private_root(root)
    release = verify_release_root_safety(root)
    open_git = release / OPEN_GIT_DIR_NAME
    binary = release / BINARY_DIR_NAME
    version = platform_version(root)
    open_result = verify_open_git_tree(root, open_git, profile)
    binary_result = verify_binary_repository(root, binary, version, profile)
    result = {"status": "PASS", "releaseProfile": profile, "openGit": open_result, "binaryRepository": binary_result, "commitExecuted": False, "pushExecuted": False}
    print(json.dumps(result, ensure_ascii=False))
    return result


def status_release(root: Path) -> dict[str, Any]:
    root = ensure_private_root(root)
    status_path = release_root(root) / REPORTS_DIR_NAME / "OPEN_GIT_RELEASE_STATUS.json"
    if not status_path.is_file():
        raise OpenGitReleaseError("Open Git release status does not exist. Run build first.")
    result = load_json(status_path)
    print("CPF Open Git Release")
    print("--------------------")
    print(f"Source 검증   : {'PASS' if result.get('sourceIdentitySha256') else 'FAIL'}")
    print(f"Package 상태  : {result.get('status', 'UNKNOWN')}")
    print(f"Release Profile: {result.get('releaseProfile', 'binary')}")
    print(f"Binary 검증   : {'PASS' if result.get('binaryFileCount', 0) else 'FAIL'}")
    print(f"Git 변경 파일 : {result.get('changedFiles', 0)} files")
    print(f"최종 상태     : {result.get('result', result.get('status', 'UNKNOWN'))}")
    print(f"Open Git 경로 : {result.get('openGit', '')}")
    return result


def _insert_once(text: str, anchor: str, block: str, marker: str, *, before: bool = True) -> str:
    if marker in text:
        return text
    if anchor not in text:
        raise OpenGitReleaseError(f"canonical integration anchor missing: {anchor}")
    insertion = block.rstrip() + "\n\n"
    if before:
        return text.replace(anchor, insertion + anchor, 1)
    return text.replace(anchor, anchor + "\n" + insertion, 1)


def _upsert_owned_section(text: str, marker: str, next_anchor: str, block: str) -> str:
    """Insert or currentize a section owned by this Work Package only."""
    desired = block.rstrip() + "\n\n"
    if marker not in text:
        return _insert_once(text, next_anchor, block, marker, before=True)
    start = text.index(marker)
    if next_anchor not in text[start:]:
        raise OpenGitReleaseError(f"canonical integration next anchor missing after owned section: {next_anchor}")
    end = text.index(next_anchor, start)
    current = text[start:end]
    if current == desired:
        return text
    return text[:start] + desired + text[end:]


def setup_integration(root: Path) -> dict[str, Any]:
    """Currentize only generated-release integration owned by this release engine.

    Unified CPF CLI and governance are canonical product source. This compatibility
    setup must never inject a second ``open-git`` CLI or overwrite current steering.
    It may only add the generated ``cpf-release/`` exclusion when absent, then it
    fail-closed validates that ``cpf release open-git`` is already wired through the
    exactly-one Java CLI and canonical command catalog.
    """
    root = ensure_private_root(root)
    changed: list[str] = []

    ignore = root / ".gitignore"
    text = ignore.read_text(encoding="utf-8-sig") if ignore.is_file() else ""
    if "/cpf-release/" not in {line.strip() for line in text.splitlines()}:
        if text and not text.endswith("\n"):
            text += "\n"
        text += "\n# CPF Open Git release output (generated, never private-source tracked)\n/cpf-release/\n"
        ignore.write_text(text, encoding="utf-8")
        changed.append(".gitignore")

    source_state = root / "cpf-tools/verification/tools/cpf-source-state.py"
    source_text = source_state.read_text(encoding="utf-8")
    if '"cpf-release"' not in source_text:
        anchor = 'GENERATED_PARTS = {'
        if anchor not in source_text:
            raise OpenGitReleaseError("cpf-source-state GENERATED_PARTS anchor missing")
        source_text = source_text.replace(anchor, anchor + '\n    "cpf-release",', 1)
        source_state.write_text(source_text, encoding="utf-8")
        changed.append("cpf-tools/verification/tools/cpf-source-state.py")

    java_cli = root / "cpf-tools/runtime/cli/java/CpfCli.java"
    if not java_cli.is_file():
        raise OpenGitReleaseError("Unified CPF Java CLI source is missing")
    java_text = java_cli.read_text(encoding="utf-8")
    if "cpf release open-git" not in java_text or '"release"' not in java_text:
        raise OpenGitReleaseError("Unified CPF CLI does not own 'cpf release open-git'")

    command_catalog = root / "cpf-tools/runtime/cli/contracts/cpf-command-catalog.json"
    if not command_catalog.is_file():
        raise OpenGitReleaseError("Canonical CPF command catalog is missing")
    catalog = load_json(command_catalog)
    release_namespaces = [row for row in catalog.get("internalNamespaces", []) if row.get("namespace") == "release"]
    if len(release_namespaces) != 1 or "open-git" not in release_namespaces[0].get("commands", []):
        raise OpenGitReleaseError("Canonical command catalog does not declare internal 'release open-git'")

    legacy_cli = root / "cpf-tools/runtime/cli/cpf.py"
    if legacy_cli.is_file() and "sub.add_parser('open-git'" in legacy_cli.read_text(encoding="utf-8"):
        raise OpenGitReleaseError("legacy independent 'cpf open-git' surface is still active")

    canonical = root / "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md"
    canonical_text = canonical.read_text(encoding="utf-8")
    if "### 21.3 Open Git Release Packaging" not in canonical_text or "cpf release open-git" not in canonical_text:
        raise OpenGitReleaseError("Current Open Git canonical steering is not integrated")

    work_package = root / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md"
    if not work_package.is_file():
        raise OpenGitReleaseError("Current Open Git work package is missing")

    result = {
        "status": "PASS",
        "changed": changed,
        "canonicalCli": "cpf release open-git",
        "gitAddExecuted": False,
        "commitExecuted": False,
        "pushExecuted": False,
        "userReviewRequired": True,
    }
    print(json.dumps(result, ensure_ascii=False))
    return result


WORK_PACKAGE_TEXT = """# CPF Open Git Release Work Package

- Work Package: `WP-14 — Open Git Release Projection / Packaging / Developer DX`
- Owner: Development GPT / `cpf-tools/release/open-git/**`
- Canonical Requirement: `CPF_FINAL_TARGET_REQUIREMENTS.md` 21.3
- Generated Root: `cpf-release/`
- Private Git: `/cpf-release/` 전체 제외
- Lifecycle: 기존 생성물 안전 전체 제거 → Fresh 생성 → Fresh Open Git clone → 검증 → `VERIFIED` → 사용자 검토 → 사용자 직접 Open Git commit/push
- Automatic commit/push: 금지
- Developer UX: Java 기반 단일 `cpf` CLI. Public `bootstrap/domain-new/domain-sync/build/test/run/stop/reset/status`, Internal `dev/verify/publish/release` Namespace

## 공개 Source

Generated Customer Domain, MBW Backoffice/Backoffice Web 개발 Source, `cpf-education`, Developer Setup/Bootstrap/Build/Test/Domain 명령만 공개한다.

## Binary 공개

Framework 내부 Source Tree는 공개하지 않고 Maven-compatible Binary Repository로 소비한다. 기본 `binary` Profile은 Framework `sources.jar`/`javadoc.jar`를 0건으로 강제한다. Optional `source` Profile은 Canonical Public Source Allowlist의 승인 Source Tree만 별도 Projection한다.

## Acceptance

1. `cpf-release/`가 Private Git/Source Identity에 포함되지 않는다.
2. 재실행 시 이전 `cpf-release/`가 남지 않는다.
3. Open Git Source에 Private Framework Source 및 JAR/WAR가 0건이다.
4. Binary Repository에서 허용되지 않은 sources/javadoc artifact가 0건이다.
5. EDU/Generated Domain/Backoffice/Developer Command가 실제 Open Git Projection에 존재한다.
6. Fresh Open Git clone 검증과 isolated Binary Repository 기반 Build/Test가 성공한다.
7. Secret/Leakage/Manifest/SHA/Git working-tree read-only gate를 통과한다.
8. Tool은 git add/commit/push를 실행하지 않는다. `cpf-release/`는 Private master에 반영하지 않는다.
9. Canonical 개발 명령은 짧은 단일 Dispatcher로 제공되고 기존 개별 Script는 동일 계약의 호환 Wrapper로 동작한다.
10. 모든 장시간 개발 명령은 진행 단계와 로그를 실시간 표시하고 종료 시 PASS/FAIL, ExitCode, 시각, 로그 경로와 다음 행동을 출력한다.
11. `cpf reset`은 명시 확인 전 destructive action을 수행하지 않는다.
"""


def print_release_summary(result: dict[str, Any]) -> None:
    print("CPF Open Git Release")
    print("--------------------")
    print(f"Private Root  : {result.get('privateRepositoryRoot', '')}")
    print(f"Branch        : {result.get('privateGitBranch', '')}")
    print(f"HEAD          : {result.get('privateGitSha', '')}")
    print(f"Source 검증   : {'PASS' if result.get('sourceIdentitySha256') else result.get('status', 'UNKNOWN')}")
    print(f"Package 상태  : {result.get('status', 'UNKNOWN')}")
    print(f"Binary 검증   : {'PASS' if result.get('binaryFileCount', 0) else 'N/A'}")
    print(f"Open Git 검증 : {'PASS' if result.get('openGitFileCount', 0) else 'N/A'}")
    print(f"Git 변경 파일 : {result.get('changedFiles', 0)} files")
    print(f"최종 상태     : {result.get('result', result.get('status', 'UNKNOWN'))}")
    if result.get('openGit'):
        print(f"Open Git 경로 : {result.get('openGit')}")


def _git(open_git: Path, *arguments: str) -> str:
    """Open Git 작업 Repository 안에서만 git 을 읽는다."""
    completed = subprocess.run(["git", *arguments], cwd=open_git, text=True,
                               encoding="utf-8", errors="replace",
                               capture_output=True, check=False)
    if completed.returncode:
        raise OpenGitReleaseError(
            "git " + " ".join(arguments) + f" 실패(exit={completed.returncode}): "
            + (completed.stderr or "").strip())
    return (completed.stdout or "").strip()


def _require_git_write_approval(approved: bool) -> None:
    """명시적 Git Write 승인이 없으면 즉시 중단한다.

    클릭 실수나 자동 실행으로 Commit/Push 가 일어나면 안 된다. Build/Verify/Prepare 는 이 함수를
    호출하지 않으며, 승인값 없이 Commit/Push 에 도달하면 여기서 fail-closed 로 끝난다.
    """
    if not approved:
        raise OpenGitReleaseError(
            "Git Write 가 승인되지 않았습니다. Commit/Push 는 명시적 승인이 필요합니다. "
            "Gradle: -PconfirmGitWrite=true / CLI: --confirm-git-write")


def _open_git_write_preflight(root: Path) -> dict[str, Any]:
    """Commit/Push 직전에 대상·검증 상태를 모두 확인한다.

    하나라도 어긋나면 중단한다. 중간 실패를 전체 PASS 로 처리하지 않는다.
    """
    root = ensure_private_root(root)
    status_path = release_root(root) / REPORTS_DIR_NAME / "OPEN_GIT_RELEASE_STATUS.json"
    if not status_path.is_file():
        raise OpenGitReleaseError(
            "Open Git Release 상태 파일이 없습니다. 먼저 cpfOpenGitPrepare(build+verify)를 수행하세요.")
    status = load_json(status_path)

    open_git = Path(str(status.get("openGit") or ""))
    if not open_git.is_dir():
        raise OpenGitReleaseError(f"Open Git 작업 Repository 를 찾을 수 없습니다: {open_git}")
    # 개발 Master 를 대상으로 삼는 사고를 원천 차단한다.
    expected = (release_root(root) / OPEN_GIT_DIR_NAME).resolve(strict=False)
    if open_git.resolve(strict=False) != expected:
        raise OpenGitReleaseError(
            f"Open Git 대상이 정본 경로가 아닙니다. expected={expected} actual={open_git}")
    if open_git.resolve(strict=False) == root.resolve():
        raise OpenGitReleaseError("Development Master 저장소에는 Open Git Commit/Push 를 하지 않습니다.")
    if not (open_git / ".git").exists():
        raise OpenGitReleaseError(f"Open Git 작업 Repository 가 git 저장소가 아닙니다: {open_git}")

    # Release/검증 결과가 PASS 가 아니면 Git Write 로 진입하지 않는다.
    overall = str(status.get("result") or status.get("status") or "UNKNOWN").upper()
    if overall not in ("PASS", "SUCCESS", "COMPLETED"):
        raise OpenGitReleaseError(f"Open Git Release 결과가 PASS 가 아닙니다: {overall}")
    leakage = int(status.get("leakageCount", 0) or 0)
    if leakage:
        raise OpenGitReleaseError(f"Open Git Leakage 가 0 이 아닙니다: {leakage}")
    if not str(status.get("sourceIdentitySha256") or "").strip():
        raise OpenGitReleaseError("Source identity 가 확인되지 않았습니다.")

    branch = _git(open_git, "branch", "--show-current")
    if not branch:
        raise OpenGitReleaseError("Open Git branch 를 확인할 수 없습니다(detached HEAD).")
    allowed = [value.strip() for value in
               os.environ.get("CPF_OPEN_GIT_ALLOWED_BRANCHES", "main,master").split(",")
               if value.strip()]
    if branch not in allowed:
        raise OpenGitReleaseError(
            f"허용되지 않은 branch 입니다: {branch} (허용: {', '.join(allowed)})")

    remotes = _git(open_git, "remote", "-v")
    if "origin" not in remotes:
        raise OpenGitReleaseError("Open Git remote origin 이 설정되어 있지 않습니다.")
    remote_url = _git(open_git, "remote", "get-url", "origin")
    expected_remote = str(status.get("remote") or "").strip()
    if expected_remote and remote_url.strip() != expected_remote:
        raise OpenGitReleaseError(
            f"Open Git remote 가 정본과 다릅니다. expected={expected_remote} actual={remote_url}")

    changed = _git(open_git, "status", "--short")
    print("CPF Open Git Git Write 사전 점검")
    print("--------------------------------")
    print(f"대상 Repository : {open_git}")
    print(f"branch          : {branch}")
    print(f"remote(origin)  : {remote_url}")
    print(f"Release 결과    : {overall}")
    print(f"Leakage         : {leakage}")
    print(f"Source Identity : {status.get('sourceIdentitySha256')}")
    print(f"변경 파일       : {len(changed.splitlines())} files")
    if changed:
        for line in changed.splitlines()[:50]:
            print(f"  {line}")
    return {"openGit": open_git, "branch": branch, "remote": remote_url,
            "changed": changed, "status": status}


def commit_release(root: Path, *, approved: bool, message: str | None = None) -> dict[str, Any]:
    """검증이 끝난 Open Git Working Tree 를 Commit 한다(승인 필수)."""
    _require_git_write_approval(approved)
    context = _open_git_write_preflight(root)
    open_git: Path = context["openGit"]
    if not context["changed"]:
        print("CPF_OPEN_GIT_COMMIT=SKIPPED reason=NO_CHANGES")
        return {"action": "commit", "result": "SKIPPED", "reason": "NO_CHANGES"}
    _git(open_git, "add", "-A")
    _git(open_git, "diff", "--cached", "--check")
    text = message or ("CPF Open Git " + datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S"))
    _git(open_git, "commit", "-m", text)
    sha = _git(open_git, "rev-parse", "HEAD")
    print(f"CPF_OPEN_GIT_COMMIT=PASS sha={sha} branch={context['branch']}")
    return {"action": "commit", "result": "PASS", "sha": sha, "branch": context["branch"]}


def push_release(root: Path, *, approved: bool) -> dict[str, Any]:
    """검증이 끝난 Open Git Commit 을 remote 로 Push 한다(승인 필수)."""
    _require_git_write_approval(approved)
    context = _open_git_write_preflight(root)
    open_git: Path = context["openGit"]
    if context["changed"]:
        raise OpenGitReleaseError(
            "Commit 되지 않은 변경이 남아 있습니다. cpfOpenGitCommit 을 먼저 수행하세요.")
    sha = _git(open_git, "rev-parse", "HEAD")
    print(f"[Push 대상] sha={sha} branch={context['branch']} remote={context['remote']}")
    _git(open_git, "push", "origin", context["branch"])
    print(f"CPF_OPEN_GIT_PUSH=PASS sha={sha} branch={context['branch']}")
    return {"action": "push", "result": "PASS", "sha": sha, "branch": context["branch"]}


def main() -> int:
    parser = argparse.ArgumentParser(description="CPF Open Git release packaging")
    parser.add_argument("action", nargs="?", default="build",
                        choices=("build", "check", "status", "setup", "commit", "push"))
    # Git Write 는 명시적 승인 없이는 절대 수행하지 않는다(Harness §29.3).
    parser.add_argument("--confirm-git-write", action="store_true",
                        help="Open Git Commit/Push 를 명시적으로 승인합니다.")
    parser.add_argument("--message", help="Open Git Commit 메시지(생략 시 표준 형식)")
    parser.add_argument("--root", default=".")
    parser.add_argument("--remote")
    parser.add_argument("--generator-artifacts")
    parser.add_argument("--profile", default="binary", choices=("binary", "source"), help="Open Git release profile; default is binary")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        if args.action == "build":
            result = build_release(root, args.remote, args.generator_artifacts, profile=args.profile)
        elif args.action == "check":
            result = check_release(root, args.profile)
        elif args.action == "status":
            result = status_release(root)
        elif args.action == "commit":
            result = commit_release(root, approved=args.confirm_git_write, message=args.message)
        elif args.action == "push":
            result = push_release(root, approved=args.confirm_git_write)
        else:
            result = setup_integration(root)
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc), "commitExecuted": False, "pushExecuted": False}
        code = 1
        print_failure_summary(root, str(exc), code)
        try:
            failed_reports = release_root(root) / REPORTS_DIR_NAME
            if failed_reports.is_dir():
                write_json(failed_reports / "OPEN_GIT_RELEASE_STATUS.json", result)
                write_status_md(failed_reports / "OPEN_GIT_RELEASE_STATUS.md", result)
                _append_log(f"[CPF][OPEN-GIT] FAIL {exc}")
        except Exception:
            pass
    # build 요약은 build 에서만 출력한다. Git Write 결과는 각 함수가 자체 표준 출력을 남긴다.
    if args.action == "build" and code == 0:
        print_release_summary(result)
    if args.action not in {"status", "setup", "check"}:
        print(json.dumps(result, ensure_ascii=False))
    elif code:
        print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    # 진행/실패 메시지는 한글이고, 자식 출력은 errors="replace"로 U+FFFD를 포함할 수 있다.
    # Windows 기본 콘솔 인코딩(cp949)으로는 그 문자를 쓸 수 없어 Release가 마지막에
    # UnicodeEncodeError로 죽는다. 플랫폼과 무관하게 UTF-8로 고정한다.
    for _stream in (sys.stdout, sys.stderr):
        try:
            _stream.reconfigure(encoding="utf-8", errors="replace")
        except (AttributeError, ValueError):
            pass
    raise SystemExit(main())
