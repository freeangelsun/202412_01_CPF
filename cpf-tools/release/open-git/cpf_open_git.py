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
import xml.etree.ElementTree as ET
from urllib.parse import unquote
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


class OpenGitReleaseError(RuntimeError):
    pass


TOOL_REL = Path("cpf-tools/release/open-git")
SURFACE_POLICY_REL = TOOL_REL / "open-git-surface-policy.json"
ARTIFACT_POLICY_REL = TOOL_REL / "open-git-artifact-policy.json"
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
        return "Codex 종료 후 'cpf open-git setup'을 1회 적용하고 /cpf-release/ 제외 상태를 확인하세요."
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
    print("CPF OPEN GIT RELEASE FAILED", file=sys.stderr)
    print("---------------------------", file=sys.stderr)
    print(f"Stage     : {ACTIVE_STAGE_NO:02d}/{BUILD_STAGE_TOTAL:02d} {ACTIVE_STAGE_LABEL}", file=sys.stderr)
    print(f"Reason    : {message}", file=sys.stderr)
    print(f"ExitCode  : {code}", file=sys.stderr)
    print(f"Log       : {log_text}", file=sys.stderr)
    print(f"Next      : {recovery_hint(message)}", file=sys.stderr)
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


def run(cmd: list[str], cwd: Path, *, capture: bool = False, env: dict[str, str] | None = None) -> str:
    secrets = _command_secrets(cmd)
    display_cmd = [_redact_sensitive_text(str(arg), secrets) for arg in cmd]
    command_line = "[CPF][OPEN-GIT][RUN] " + " ".join(display_cmd)
    print(command_line, flush=True)
    _append_log(command_line)
    if capture:
        # CPF 도구/Gradle 출력에는 한글이 포함된다. text=True에 encoding을 지정하지 않으면
        # Windows 기본 로케일(cp949 등)로 디코딩하다 UnicodeDecodeError로 실패한다.
        cp = subprocess.run(cmd, cwd=cwd, text=True, encoding="utf-8", errors="replace",
                            capture_output=True, env=env, check=False)
        combined = _redact_sensitive_text((cp.stdout or "") + (cp.stderr or ""), secrets)
        if combined:
            _append_log(combined)
        if cp.returncode:
            sys.stderr.write(combined)
            raise OpenGitReleaseError(f"command failed exit={cp.returncode}: {display_cmd}")
        return (cp.stdout or "").strip()

    process = subprocess.Popen(cmd, cwd=cwd, text=True, encoding="utf-8", errors="replace",
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=env, bufsize=1)
    assert process.stdout is not None
    for line in process.stdout:
        safe_line = _redact_sensitive_text(line, secrets)
        sys.stdout.write(safe_line)
        sys.stdout.flush()
        _append_log(safe_line.rstrip("\n"))
    code = process.wait()
    if code:
        raise OpenGitReleaseError(f"command failed exit={code}: {display_cmd}")
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
    output = run([sys.executable, str(script), "--root", str(root), "--scope", "source"], root, capture=True)
    try:
        return json.loads(output.splitlines()[-1])
    except Exception as exc:
        raise OpenGitReleaseError(f"cannot parse CPF source identity: {exc}") from exc


def require_clean_private_git(root: Path) -> str:
    git = shutil.which("git")
    if not git or not (root / ".git").exists():
        # ZIP/offline QA mode: canonical source identity remains authoritative.
        return "NO_GIT_OFFLINE_SOURCE"
    inside = run([git, "rev-parse", "--is-inside-work-tree"], root, capture=True)
    if inside.lower() != "true":
        raise OpenGitReleaseError("private source is not a Git working tree")
    dirty = run([git, "status", "--porcelain=v1", "--untracked-files=all"], root, capture=True)
    if dirty:
        raise OpenGitReleaseError("private source working tree must be clean before Open Git release preparation")
    return run([git, "rev-parse", "HEAD"], root, capture=True)


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


def _artifact_coordinate_map(root: Path) -> dict[tuple[str, str], dict[str, Any]]:
    result: dict[tuple[str, str], dict[str, Any]] = {}
    for row in artifact_rows(root):
        group = str(row.get("publicGroupId") or "").strip()
        artifact = str(row.get("artifactId") or "").strip()
        if group and artifact:
            result[(group, artifact)] = row
    return result


def _repo_coordinate(repo: Path, path: Path) -> tuple[str, str] | None:
    rel = path.relative_to(repo)
    parts = rel.parts
    if len(parts) < 4:
        return None
    artifact = parts[-3]
    group = ".".join(parts[:-3])
    return group, artifact


def sanitize_binary_repository(root: Path, raw_repo: Path, final_repo: Path) -> dict[str, Any]:
    if final_repo.exists():
        shutil.rmtree(final_repo)
    shutil.copytree(raw_repo, final_repo)
    policy = load_json(root / ARTIFACT_POLICY_REL)
    coords = _artifact_coordinate_map(root)
    removed: list[dict[str, str]] = []
    kept_sources = 0
    kept_javadocs = 0
    unknown_docs: list[str] = []

    for path in sorted(final_repo.rglob("*.jar")):
        name = path.name
        kind = "sources" if name.endswith("-sources.jar") else "javadoc" if name.endswith("-javadoc.jar") else ""
        if not kind:
            continue
        coordinate = _repo_coordinate(final_repo, path)
        row = coords.get(coordinate) if coordinate else None
        if row is None:
            unknown_docs.append(path.relative_to(final_repo).as_posix())
            path.unlink()
            continue
        owner = str(row.get("ownerPath") or "")
        config = policy["sourceJarPolicy" if kind == "sources" else "javadocJarPolicy"]
        if _owner_allowed(owner, config):
            if kind == "sources":
                kept_sources += 1
            else:
                kept_javadocs += 1
            continue
        removed.append({"path": path.relative_to(final_repo).as_posix(), "kind": kind, "ownerPath": owner, "artifactId": str(row.get("artifactId") or "")})
        path.unlink()

    return {
        "removedSourceOrJavadoc": removed,
        "unknownDocumentationArtifactsRemoved": unknown_docs,
        "keptSources": kept_sources,
        "keptJavadocs": kept_javadocs,
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


def verify_binary_repository(root: Path, repo: Path, version: str) -> dict[str, Any]:
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
            config = policy["sourceJarPolicy" if kind == "sources" else "javadocJarPolicy"]
            if not _owner_allowed(owner, config):
                findings.append(f"forbidden {kind} artifact owner={owner}: {path.relative_to(repo)}")
            if kind == "sources": source_count += 1
            else: javadoc_count += 1
        else:
            if row is None:
                findings.append(f"unclassified binary artifact: {path.relative_to(repo)}")
            else:
                binary_count += 1

    poms = sorted(repo.rglob("*.pom"))
    if not poms:
        findings.append("binary repository contains no POM")
    for pom in poms:
        group, artifact, _, deps = _pom_coordinate(pom)
        if not group or not artifact:
            findings.append(f"invalid POM coordinate: {pom.relative_to(repo)}")
        elif (group, artifact) not in coords:
            findings.append(f"unclassified POM coordinate {group}:{artifact}: {pom.relative_to(repo)}")
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

    if findings:
        raise OpenGitReleaseError("Open Git binary policy failed:\n" + "\n".join(findings[:200]))
    return {
        "status": "PASS",
        "binaryJarCount": binary_count,
        "sourceJarCount": source_count,
        "javadocJarCount": javadoc_count,
        "pomCount": len(poms),
    }


def verify_open_git_tree(open_git: Path) -> dict[str, Any]:
    required = ["cpf-member", "cpf-external", "cpf-backoffice", "cpf-backoffice-web", "cpf-education", "bin"]
    missing = [item for item in required if not (open_git / item).exists()]
    if missing:
        raise OpenGitReleaseError(f"Open Git required paths missing: {missing}")
    forbidden_roots = ["cpf-core", "cpf-common", "cpf-admin", "cpf-biz-admin", "cpf-batch", "cpf-gateway", "cpf-starters", "cpf-tools", "cpf-docs"]
    leaked = [name for name in forbidden_roots if (open_git / name).exists()]
    if leaked:
        raise OpenGitReleaseError(f"private framework source leaked into Open Git: {leaked}")
    if (open_git / "domains").exists():
        raise OpenGitReleaseError("duplicate Open Git domains catalog is forbidden; physical cpf-<domain> projects are authoritative")
    for project in ("cpf-member", "cpf-external", "cpf-backoffice"):
        contract = open_git / project / "gradle.properties"
        if not contract.is_file() or "cpf.domain.contractVersion=1" not in contract.read_text(encoding="utf-8-sig"):
            raise OpenGitReleaseError(f"Open Git Domain Developer contract missing/invalid: {project}/gradle.properties")
        for forbidden in ("cpf-domain.yaml", "cpf-generator.lock.json"):
            if (open_git / project / forbidden).exists():
                raise OpenGitReleaseError(f"Generator metadata leaked into Open Git: {project}/{forbidden}")
    archives = []
    for path in open_git.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in {".jar", ".war"}:
            continue
        rel = path.relative_to(open_git).as_posix()
        if rel == "gradle/wrapper/gradle-wrapper.jar":
            continue
        archives.append(rel)
    if archives:
        raise OpenGitReleaseError(f"Open Git Source Workspace contains accumulated CPF/application JAR/WAR: {archives[:20]}")
    edu_build = open_git / "cpf-education/build.gradle"
    if edu_build.is_file() and "project(" in edu_build.read_text(encoding="utf-8"):
        raise OpenGitReleaseError("Open Git EDU build must consume published CPF artifacts, not private project dependencies")
    return {"status": "PASS", "requiredPaths": len(required), "forbiddenRootLeakage": 0, "jarWarCount": 0}


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
        f"- Private Git SHA: `{result.get('privateGitSha', '')}`",
        f"- Platform Version: `{result.get('platformVersion', '')}`",
        f"- Open Git Files: {result.get('openGitFileCount', 0)}",
        f"- Binary Repository Files: {result.get('binaryFileCount', 0)}",
        f"- Changed Files Ready to Commit: {result.get('changedFiles', 0)}",
        f"- Commit Executed: {result.get('commitExecuted', False)}",
        f"- Push Executed: {result.get('pushExecuted', False)}",
        "",
        "## Paths",
        "",
        f"- Open Git: `{result.get('openGit', '')}`",
        f"- Binary Repository: `{result.get('binaryRepository', '')}`",
        "",
        "Open Git commit/push는 자동 실행하지 않습니다. `READY_TO_COMMIT` 이후 사용자가 직접 확인하고 수행합니다.",
    ]
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


def _prepare_workspace(root: Path, staging: Path, source_identity: str, env: dict[str, str]) -> dict[str, Any]:
    prepare = root / LEGACY_PUBLIC_REL / "prepare-cpf-public-workspace.py"
    policy = root / SURFACE_POLICY_REL
    run([
        sys.executable, str(prepare), "--root", str(root), "--staging", str(staging),
        "--policy", str(policy), "--source-identity", source_identity
    ], root, env=env)
    ready = load_json(staging / ".cpf-public/READY.json")
    if ready.get("status") != "PASS":
        raise OpenGitReleaseError("Open Git staging is not READY")
    return ready


def build_release(root: Path, remote_arg: str | None, generator_artifacts: str | None, *, skip_build: bool = False) -> dict[str, Any]:
    root = ensure_private_root(root)

    # Every accepted build attempt invalidates the previous generated release first.
    # Safety checks run before deletion and cleanup is restricted to exact <CPF_ROOT>/cpf-release.
    release_stage(1, "Release Root 안전 확인", "이전 생성물 전체 재생성 준비")
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

    release_stage(2, "Private Source 확인", "Clean Working Tree / Source Identity / Version / Remote")
    private_git_sha = require_clean_private_git(root)
    source_state = canonical_source_state(root)
    source_identity = str(source_state["contentSha256"])
    version = platform_version(root)
    remote = canonical_remote(root, remote_arg)

    release_stage(3, "Artifact 공개 정책 확인", "Binary / sources.jar / javadoc.jar Default-Deny")
    verify_artifact_catalog_contract(root)

    backend = _legacy_backend(root)
    backend.run = lambda cmd, cwd, capture=False, env=None: run(cmd, Path(cwd), capture=capture, env=env)
    if skip_build:
        raise OpenGitReleaseError("--skip-build is reserved for tests and cannot create a production Open Git release")

    release_stage(4, "Private Release Gate", "Secret / Leakage / Canonical release prerequisites")
    backend.private_gates(root, sys.executable)

    release_stage(5, "Framework Binary Publication", f"version={version}")
    backend.private_build_and_publication(root, raw_repo, version)

    release_stage(6, "Generator Distribution", "Windows/Linux 공개 Generator 산출물")
    generator_result = backend.publish_generator_distributions(
        root, raw_repo, version, Path(generator_artifacts).resolve() if generator_artifacts else None
    )

    release_stage(7, "Raw Binary Repository 검증", "Publication closure")
    old_verifier = root / LEGACY_PUBLIC_REL / "verify-cpf-public-binary-repository.py"
    run([sys.executable, str(old_verifier), "--root", str(root), "--repository", str(raw_repo), "--version", version], root)

    release_stage(8, "Artifact 공개 필터 적용", "허용된 Binary/Source/Javadoc만 유지")
    sanitize_result = sanitize_binary_repository(root, raw_repo, final_repo)

    release_stage(9, "최종 Binary Repository 검증", "Maven 좌표 / Dependency / Source disclosure")
    binary_result = verify_binary_repository(root, final_repo, version)

    release_stage(10, "Open Git Source Projection", "Generated Domain / Backoffice / EDU / Developer Command")
    env = dict(os.environ)
    env["CPF_MAVEN_REPOSITORY_URL"] = final_repo.resolve().as_uri()
    env["CPF_VERSION"] = version
    ready = _prepare_workspace(root, staging, source_identity, env)
    verify_open_git_tree(staging)

    release_stage(11, "Open Git Fresh Clone", "이전 Open Git Working Copy 재사용 금지")
    git = shutil.which("git") or "git"
    run([git, "clone", "--no-tags", remote, str(open_git)], release)
    if run([git, "status", "--porcelain=v1", "--untracked-files=all"], open_git, capture=True):
        raise OpenGitReleaseError("fresh Open Git clone unexpectedly dirty")
    backend.sync_public_surface(staging, open_git)
    verify_open_git_tree(open_git)

    release_stage(12, "Fresh Workspace Build/Test", "Open Git + isolated Binary Repository")
    verifier = open_git / "tools" / ("verify-open-git-workspace.ps1" if os.name == "nt" else "verify-open-git-workspace.sh")
    if os.name == "nt":
        shell = shutil.which("pwsh") or shutil.which("powershell")
        if not shell:
            raise OpenGitReleaseError("PowerShell is required for Open Git workspace verification")
        run([shell, "-NoProfile", "-File", str(verifier)], open_git, env=env)
    else:
        run(["bash", str(verifier)], open_git, env=env)

    release_stage(13, "Open Git Staged Diff 검증", "git add -A / diff --cached --check; commit/push 미실행")
    run([git, "add", "-A"], open_git)
    run([git, "diff", "--cached", "--check"], open_git)
    changed = [line for line in run([git, "diff", "--cached", "--name-only"], open_git, capture=True).splitlines() if line.strip()]

    release_stage(14, "Manifest / SHA / Status", "사용자 검토 가능한 최종 Release 결과 생성")
    open_file_count = write_file_manifest(root, open_git, reports / "OPEN_GIT_FILE_MANIFEST.csv")
    binary_file_count = write_artifact_manifest(final_repo, reports / "OPEN_GIT_ARTIFACT_MANIFEST.csv")
    write_json(reports / "OPEN_GIT_ARTIFACT_FILTER_RESULT.json", sanitize_result)
    write_json(reports / "OPEN_GIT_BINARY_VERIFY_RESULT.json", binary_result)

    result = {
        "schemaVersion": 1,
        "status": "PASS",
        "result": "READY_TO_COMMIT" if changed else "NO_CHANGES",
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "sourceIdentitySha256": source_identity,
        "sourceFileCount": source_state.get("fileCount"),
        "privateGitSha": private_git_sha,
        "platformVersion": version,
        "releaseRoot": str(release),
        "openGit": str(open_git),
        "binaryRepository": str(final_repo),
        "openGitFileCount": open_file_count,
        "binaryFileCount": binary_file_count,
        "changedFiles": len(changed),
        "publicStagingFileCount": ready.get("fileCount"),
        "generatorDistribution": generator_result,
        "sourceJarCount": binary_result.get("sourceJarCount"),
        "javadocJarCount": binary_result.get("javadocJarCount"),
        "commitExecuted": False,
        "pushExecuted": False,
    }
    write_json(reports / "OPEN_GIT_RELEASE_STATUS.json", result)
    write_status_md(reports / "OPEN_GIT_RELEASE_STATUS.md", result)
    # Successful release leaves only user-visible deliverables/reports/logs.
    shutil.rmtree(work)
    return result


def check_release(root: Path) -> dict[str, Any]:
    root = ensure_private_root(root)
    release = verify_release_root_safety(root)
    open_git = release / OPEN_GIT_DIR_NAME
    binary = release / BINARY_DIR_NAME
    version = platform_version(root)
    open_result = verify_open_git_tree(open_git)
    binary_result = verify_binary_repository(root, binary, version)
    result = {"status": "PASS", "openGit": open_result, "binaryRepository": binary_result, "commitExecuted": False, "pushExecuted": False}
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
    print(f"Source        : {'PASS' if result.get('sourceIdentitySha256') else 'FAIL'}")
    print(f"Package       : {result.get('status', 'UNKNOWN')}")
    print(f"Binary        : {'PASS' if result.get('binaryFileCount', 0) else 'FAIL'}")
    print(f"Git Diff      : {result.get('changedFiles', 0)} files")
    print(f"Status        : {result.get('result', result.get('status', 'UNKNOWN'))}")
    print(f"Path          : {result.get('openGit', '')}")
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
    """Safely integrate the generated-release exclusion and canonical requirement.

    The function performs narrow textual insertions only; it never overwrites the
    whole file from a stale baseline. This is intentionally safe to run after Codex.
    """
    root = ensure_private_root(root)
    changed: list[str] = []

    ignore = root / ".gitignore"
    text = ignore.read_text(encoding="utf-8-sig") if ignore.is_file() else ""
    if "/cpf-release/" not in {line.strip() for line in text.splitlines()}:
        if text and not text.endswith("\n"): text += "\n"
        text += "\n# CPF Open Git release output (generated, never private-source tracked)\n/cpf-release/\n"
        ignore.write_text(text, encoding="utf-8")
        changed.append(".gitignore")

    source_state = root / "cpf-tools/verification/tools/cpf-source-state.py"
    text = source_state.read_text(encoding="utf-8")
    if '"cpf-release"' not in text:
        anchor = 'GENERATED_PARTS = {'
        if anchor not in text:
            raise OpenGitReleaseError("cpf-source-state GENERATED_PARTS anchor missing")
        text = text.replace(anchor, anchor + '\n    "cpf-release",', 1)
        source_state.write_text(text, encoding="utf-8")
        changed.append("cpf-tools/verification/tools/cpf-source-state.py")

    cli = root / "cpf-tools/runtime/cli/cpf.py"
    cli_text = cli.read_text(encoding="utf-8")
    if "sub.add_parser('open-git'" not in cli_text:
        import_anchor = "import argparse, json, os, shutil, sys, tempfile, uuid"
        parser_anchor = "    vsub.add_parser('all')"
        dispatch_anchor = "    ns=p.parse_args(); root=repo_root(ns.root)"
        if import_anchor not in cli_text or parser_anchor not in cli_text or dispatch_anchor not in cli_text:
            raise OpenGitReleaseError("CPF CLI integration anchor changed; refusing broad rewrite")
        cli_text = cli_text.replace(import_anchor, import_anchor + ", subprocess", 1)
        cli_text = cli_text.replace(
            parser_anchor,
            parser_anchor + "\n\n    open_git=sub.add_parser('open-git',help='Open Git release package')\n    open_git.add_argument('command',nargs='?',default='build',choices=['build','check','status'])",
            1,
        )
        dispatch = dispatch_anchor + "\n    if ns.group=='open-git':\n        tool=root/'cpf-tools/release/open-git/cpf_open_git.py'\n        if not tool.is_file(): raise DomainError(f'Open Git release tool이 없습니다: {tool}')\n        return subprocess.run([sys.executable,str(tool),ns.command,'--root',str(root)],cwd=root,check=False).returncode"
        cli_text = cli_text.replace(dispatch_anchor, dispatch, 1)
        cli.write_text(cli_text, encoding="utf-8")
        changed.append("cpf-tools/runtime/cli/cpf.py")

    canonical = root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"
    text = canonical.read_text(encoding="utf-8")
    marker = "### 21.3 Open Git Release Packaging"
    block = """### 21.3 Open Git Release Packaging

Open Git 릴리즈는 Private CPF Source에서 생성하는 **검증된 Projection**이며 독립 개발 정본이 아니다.

- Private Repository Root의 생성 전용 디렉터리는 `cpf-release/`로 고정하고 Private Git 및 Source Identity에서 제외한다.
- 실행할 때마다 기존 `cpf-release/`를 안전하게 전체 제거한 뒤 신규 생성한다. 이전 실행의 stale 파일을 재사용하지 않는다.
- 최종 로컬 구조는 `cpf-release/open-git`, `cpf-release/binary-repository`, `cpf-release/reports`, `cpf-release/logs`를 기본으로 한다.
- `open-git`은 Generated Customer Domain, Backoffice 고객 개발 Source, EDU Source, Developer Setup/Bootstrap/Build/Test/Domain 명령, 공개 문서·설정만 포함한다.
- `cpf-core`, `cpf-common`, ADM, Gateway, Batch Runtime, Starter/Internal Provider 등 Framework 내부 구현 Source Tree는 Open Git에 포함하지 않는다.
- Binary Repository는 Framework 사용에 필요한 Public BOM/API/SPI/Starter/Runtime/Generator artifact를 Maven-compatible 구조로 제공한다.
- `sources.jar`/`javadoc.jar`도 Source 공개로 간주한다. 기본은 DENY이며 Common과 Public Starter 계열처럼 명시적으로 허용된 개발 계약만 공개한다. ADM/Gateway/Batch/Internal Runtime 계열은 binary-only다.
- Open Git Source Workspace에는 누적 CPF JAR/WAR를 포함하지 않는다. Binary Repository는 별도 형제 Deliverable로 생성한다.
- 공개 Surface와 Artifact는 Default-Deny 정책으로 분류하며 Private Source, internal/provider, governance/QA/evidence, secret/credential leakage를 Release Blocker로 처리한다.
- Open Git Working Repository는 매 Release마다 Remote에서 fresh clone하고 검증된 Projection으로 동기화한다.
- Release Tool은 `git add -A`, `git diff --cached --check`, `READY_TO_COMMIT`까지만 수행한다. commit/push는 자동 실행하지 않고 사용자가 최종 확인 후 직접 수행한다.
- Release 담당자 명령은 짧고 일관되게 `cpf open-git`, `cpf open-git check`, `cpf open-git status`를 Canonical UX로 한다. 내부 구현 파일명은 Owner와 역할을 명확히 드러내되 사용자에게 장황한 경로 호출을 요구하지 않는다.
- Open Git 개발자 Workspace는 `cpf bootstrap`, `cpf build`, `cpf test`, `cpf verify`, `cpf domain new`, `cpf domain sync`, `cpf status`, `cpf stop`, `cpf reset`을 Canonical 개발 명령으로 제공한다. 기존 개별 Script는 호환 Wrapper로만 둘 수 있으며 서로 다른 실행 계약을 중복 구현하지 않는다.
- 개발자 명령은 장시간 실행 중 현재 단계/전체 단계와 실제 하위 실행 로그를 콘솔에 계속 표시하고 Timestamp 로그를 동시에 남긴다. 종료 시 PASS/FAIL, ExitCode, 시작/완료 시각, 로그 전체 경로, 실패 원인과 다음 행동을 표시한다.
- `cpf bootstrap`은 Fresh Clone 개발자가 한 번에 환경 구성과 Build/Test/Runtime Health까지 진행할 수 있어야 하며 기본 성공 기준은 `CPF LOCAL DEVELOPMENT READY`다. `cpf reset`은 명시적 사용자 확인 없이는 Local Data 삭제를 시작하지 않는다.
"""
    text2 = _upsert_owned_section(text, marker, "## 22. EDU Canonical 35", block)
    if text2 != text:
        canonical.write_text(text2, encoding="utf-8")
        changed.append("cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md")

    work_package = root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md"
    if not work_package.exists():
        work_package.write_text(WORK_PACKAGE_TEXT, encoding="utf-8")
        changed.append("cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md")

    result = {"status": "PASS", "changed": changed, "commitExecuted": False, "pushExecuted": False}
    print(json.dumps(result, ensure_ascii=False))
    return result


WORK_PACKAGE_TEXT = """# CPF Open Git Release Work Package

- Work Package: `WP-14 — Open Git Release Projection / Packaging / Developer DX`
- Owner: Development GPT / `cpf-tools/release/open-git/**`
- Canonical Requirement: `CPF_FINAL_TARGET_REQUIREMENTS.md` 21.3
- Generated Root: `cpf-release/`
- Private Git: `/cpf-release/` 전체 제외
- Lifecycle: 기존 생성물 안전 전체 제거 → Fresh 생성 → Fresh Open Git clone → 검증 → `READY_TO_COMMIT`
- Automatic commit/push: 금지
- Developer UX: `cpf bootstrap/build/test/verify/domain/status/stop/reset` 단일 명령 체계, 진행 단계 + Timestamp Log + PASS/FAIL + ExitCode + 다음 행동 출력

## 공개 Source

Generated Customer Domain, MBW Backoffice/Backoffice Web 개발 Source, `cpf-education`, Developer Setup/Bootstrap/Build/Test/Domain 명령만 공개한다.

## Binary 공개

Framework 내부 Source Tree는 공개하지 않고 Maven-compatible Binary Repository로 소비한다. `sources.jar`와 `javadoc.jar`는 공개 Source로 간주해 Default-Deny하며 Common/Public Starter 계열만 명시 허용한다. Core/ADM/Gateway/Batch/Internal Runtime은 binary-only다.

## Acceptance

1. `cpf-release/`가 Private Git/Source Identity에 포함되지 않는다.
2. 재실행 시 이전 `cpf-release/`가 남지 않는다.
3. Open Git Source에 Private Framework Source 및 JAR/WAR가 0건이다.
4. Binary Repository에서 허용되지 않은 sources/javadoc artifact가 0건이다.
5. EDU/Generated Domain/Backoffice/Developer Command가 실제 Open Git Projection에 존재한다.
6. Fresh Open Git clone 검증과 isolated Binary Repository 기반 Build/Test가 성공한다.
7. Secret/Leakage/Manifest/SHA/Git diff gate를 통과한다.
8. Tool은 commit/push를 실행하지 않는다.
9. Canonical 개발 명령은 짧은 단일 Dispatcher로 제공되고 기존 개별 Script는 동일 계약의 호환 Wrapper로 동작한다.
10. 모든 장시간 개발 명령은 진행 단계와 로그를 실시간 표시하고 종료 시 PASS/FAIL, ExitCode, 시각, 로그 경로와 다음 행동을 출력한다.
11. `cpf reset`은 명시 확인 전 destructive action을 수행하지 않는다.
"""


def print_release_summary(result: dict[str, Any]) -> None:
    print("CPF Open Git Release")
    print("--------------------")
    print(f"Source        : {'PASS' if result.get('sourceIdentitySha256') else result.get('status', 'UNKNOWN')}")
    print(f"Package       : {result.get('status', 'UNKNOWN')}")
    print(f"Binary        : {'PASS' if result.get('binaryFileCount', 0) else 'N/A'}")
    print(f"Open Git      : {'PASS' if result.get('openGitFileCount', 0) else 'N/A'}")
    print(f"Git Diff      : {result.get('changedFiles', 0)} files")
    print(f"Status        : {result.get('result', result.get('status', 'UNKNOWN'))}")
    if result.get('openGit'):
        print(f"Path          : {result.get('openGit')}")


def main() -> int:
    parser = argparse.ArgumentParser(description="CPF Open Git release packaging")
    parser.add_argument("action", nargs="?", default="build", choices=("build", "check", "status", "setup"))
    parser.add_argument("--root", default=".")
    parser.add_argument("--remote")
    parser.add_argument("--generator-artifacts")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        if args.action == "build":
            result = build_release(root, args.remote, args.generator_artifacts)
        elif args.action == "check":
            result = check_release(root)
        elif args.action == "status":
            result = status_release(root)
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
    if args.action == "build" and code == 0:
        print_release_summary(result)
    if args.action not in {"status", "setup", "check"}:
        print(json.dumps(result, ensure_ascii=False))
    elif code:
        print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
