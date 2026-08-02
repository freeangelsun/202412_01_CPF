#!/usr/bin/env python3
"""Integrated low-cost QA37 source closure.

The QA37 package manifests describe a root overlay, not the complete repository.
``--overlay-package`` therefore requires the directory to contain exactly the
declared overlay files.  The default merged-repository mode requires every
declared overlay file to exist but permits unrelated repository files.
"""
from __future__ import annotations

import argparse
import csv
import json
import re
import subprocess
import sys
from pathlib import Path, PurePosixPath


BASE = "1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04"
PACKAGE_MANIFEST = "cpf-docs/work/manifest/CPF_20260801_QA37_PACKAGE_MANIFEST.json"
ROOT_OVERLAY_MANIFEST = "cpf-docs/work/manifest/CPF_20260801_QA37_ROOT_OVERLAY_MANIFEST.json"
CHANGE_MANIFEST = "cpf-docs/work/manifest/CPF_20260801_QA37_CHANGE_MANIFEST.csv"
DELETE_MANIFEST = "cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt"
PROTECTED = {
    "README.md",
    "cpf-docs/guides/00_프레임워크안내.md",
    "cpf-docs/guides/01_개발자매뉴얼.md",
    "cpf-docs/guides/02_배치개발매뉴얼.md",
    "cpf-docs/guides/03_ADM개발자매뉴얼.md",
    "cpf-docs/guides/04_ADM운영자매뉴얼.md",
    "cpf-docs/guides/05_플랫폼운영매뉴얼.md",
    "cpf-docs/guides/90_BZA매뉴얼.md",
    "cpf-docs/guides/91_Gateway매뉴얼.md",
}
BANNED_DIRS = {
    "build",
    "node_modules",
    "dist",
    "coverage",
    "playwright-report",
    "test-results",
    "__pycache__",
}
STRONG_SECRET_PATTERNS = {
    "aws-access-key": re.compile(r"(?:AKIA|ASIA)[0-9A-Z]{16}"),
    "private-key": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
    "well-known-token": re.compile(
        r"(?:github_pat_[A-Za-z0-9_]{30,}|gh[pousr]_[A-Za-z0-9]{30,}|"
        r"xox[baprs]-[A-Za-z0-9-]{20,}|sk-(?:proj-)?[A-Za-z0-9_-]{24,}|"
        r"eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,})"
    ),
}
SECRET_ASSIGNMENT_PATTERN = re.compile(
    r"""
    (?<![A-Za-z0-9_$.-])
    (?P<lhs_quote>["']?)
    (?P<lhs>
        (?:\$env:|\$)?
        (?:[A-Za-z_][A-Za-z0-9_$.-]*?)?
        (?:password|passwd|secret|token|api[_-]?key)
        [A-Za-z0-9_$.-]*
    )
    (?P=lhs_quote)
    \s*(?P<operator>[:=])(?!=)\s*
    (?P<rhs>"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|[^;,\r\n]+)
    """,
    re.IGNORECASE | re.VERBOSE,
)
QUOTED_LITERAL_PATTERN = re.compile(
    r"^(?:\"(?P<double>(?:\\.|[^\"\\])*)\"|'(?P<single>(?:\\.|[^'\\])*)')$"
)
CONFIG_SUFFIXES = {".env", ".ini", ".json", ".properties", ".toml", ".yaml", ".yml"}
METADATA_LHS_SUFFIXES = {
    "ALGORITHM",
    "CLAIM",
    "COOKIE",
    "COOKIENAME",
    "ENV",
    "ENVNAME",
    "FIELD",
    "FILE",
    "HASH",
    "HEADER",
    "HEADERNAME",
    "LABEL",
    "MAXLENGTH",
    "MESSAGE",
    "MINLENGTH",
    "PARAMETER",
    "PARAMETERNAME",
    "PATH",
    "PATTERN",
    "POLICY",
    "PROPERTY",
    "PROPERTYNAME",
    "PROVIDER",
    "REF",
    "REFERENCE",
    "REFERENCES",
    "REGEX",
    "RESULTTOKEN",
    "TTL",
    "TTLSECONDS",
    "URL",
}
CANONICAL_PLACEHOLDERS = {
    "CHANGE_ME",
    "REDACTED",
    "__SET_BY_ENV__",
    "__REPLACE_BY_ENV__",
    "__REPLACE_BY_SECRET_PROVIDER__",
    "__SET_BY_SECRET_PROVIDER__",
}
KNOWN_CHANGE_TYPES = {"ADD_OR_REPLACE", "DELETE_REVIEW_REQUIRED"}
LEGACY_PROJECT_EXEC_PATTERN = re.compile(r"(?<![A-Za-z0-9_$.])exec\s*\{")
LEGACY_STACK_MAP_PATTERN = re.compile(r"\brootProject\.ext\.cpfStack\s*\[")


class OverlayFileContract:
    """The file-set contract declared by the QA37 overlay manifests."""

    def __init__(
        self,
        package: dict,
        overlay: dict,
        rows: list[dict[str, str]],
        add_or_replace: set[str],
        exclusions: set[str],
    ) -> None:
        self.package = package
        self.overlay = overlay
        self.rows = rows
        self.add_or_replace = frozenset(add_or_replace)
        self.exclusions = frozenset(exclusions)
        self.expected_files = self.add_or_replace | self.exclusions


def fail(message: str) -> None:
    print("[CPF][QA37][SOURCE][FAIL] " + message, file=sys.stderr)
    raise SystemExit(1)


def run(command: list[object], timeout: int = 180) -> None:
    printable = [str(value) for value in command]
    print("[CPF][QA37][SOURCE][RUN] " + " ".join(printable))
    result = subprocess.run(printable, text=True, capture_output=True, timeout=timeout)
    if result.stdout:
        print(result.stdout, end="")
    if result.stderr:
        print(result.stderr, end="", file=sys.stderr)
    if result.returncode:
        fail(f"command failed exit={result.returncode}: {printable}")


def read_json(path: Path) -> object:
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        fail(f"JSON invalid {path}: {exc}")
    return value


def check_csv(path: Path) -> None:
    try:
        with path.open(encoding="utf-8-sig", newline="") as handle:
            rows = list(csv.reader(handle))
        if not rows or not rows[0]:
            fail(f"CSV empty {path}")
    except Exception as exc:
        fail(f"CSV invalid {path}: {exc}")


def safe_relative_path(value: str) -> bool:
    path = PurePosixPath(value)
    return (
        bool(value)
        and not path.is_absolute()
        and ".." not in path.parts
        and "\\" not in value
        and not value.startswith("/")
    )


def _quote_before(line: str, position: int) -> str | None:
    """Return the active source quote before position, if any."""
    quote: str | None = None
    escaped = False
    for character in line[:position]:
        if escaped:
            escaped = False
            continue
        if character == "\\":
            escaped = True
            continue
        if quote:
            if character == quote:
                quote = None
        elif character in {"'", '"'}:
            quote = character
    return quote


def _comment_before(line: str, position: int) -> bool:
    """Return whether position is inside a line/block comment prefix."""
    quote: str | None = None
    escaped = False
    index = 0
    while index < position:
        character = line[index]
        if escaped:
            escaped = False
            index += 1
            continue
        if character == "\\":
            escaped = True
            index += 1
            continue
        if quote:
            if character == quote:
                quote = None
            index += 1
            continue
        if character in {"'", '"'}:
            quote = character
            index += 1
            continue
        if character == "#" or line.startswith(("//", "--", "/*"), index):
            return True
        index += 1
    return False


def _literal_value(rhs: str) -> tuple[str, bool]:
    value = rhs.strip()
    quoted = QUOTED_LITERAL_PATTERN.fullmatch(value)
    if quoted:
        return (quoted.group("double") if quoted.group("double") is not None else quoted.group("single")), True
    return value, False


def _is_config_context(path: Path) -> bool:
    name = path.name.lower()
    return path.suffix.lower() in CONFIG_SUFFIXES or name == ".env" or name.startswith(".env.")


def _strip_inline_config_comment(rhs: str) -> str:
    """Strip an unquoted config comment while retaining punctuation in values."""
    quote: str | None = None
    escaped = False
    for index, character in enumerate(rhs):
        if escaped:
            escaped = False
            continue
        if character == "\\":
            escaped = True
            continue
        if quote:
            if character == quote:
                quote = None
            continue
        if character in {"'", '"'}:
            quote = character
            continue
        if character == "#" and (index == 0 or rhs[index - 1].isspace()):
            return rhs[:index].rstrip()
    return rhs.strip()


def _is_placeholder_or_sentinel(value: str) -> bool:
    candidate = value.strip()
    upper = candidate.upper()
    lower = candidate.lower()
    if not candidate or upper in CANONICAL_PLACEHOLDERS:
        return True
    if candidate.startswith("${") and candidate.endswith("}"):
        expression = candidate[2:-1]
        variable, separator, default = expression.partition(":")
        if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_.-]*", variable):
            return False
        if not separator:
            return True
        return _is_placeholder_or_sentinel(default) or _is_environment_or_provider_reference(
            default
        )
    if candidate.startswith("<") and candidate.endswith(">"):
        return True
    if candidate.startswith("{{") and candidate.endswith("}}"):
        return True
    if re.fullmatch(r"[*xX\u2022]{3,}", candidate):
        return True
    if re.fullmatch(r"[A-Za-z0-9._-]{0,4}[*xX\u2022]{3,}[A-Za-z0-9._-]{0,4}", candidate):
        return True
    return bool(
        re.fullmatch(
            r"(?:(?:change|replace|set)-this(?:[-_][A-Za-z0-9._-]+)?|your-[A-Za-z0-9._-]+)",
            lower,
        )
    )


def _is_environment_or_provider_reference(value: str) -> bool:
    candidate = value.strip()
    if re.fullmatch(r"\$(?:env:)?[A-Za-z_][A-Za-z0-9_]*", candidate, re.I):
        return True
    if re.fullmatch(r"%[A-Za-z_][A-Za-z0-9_]*%", candidate):
        return True
    if re.fullmatch(
        r"(?:secretProvider|provider|vault)\.[A-Za-z_][A-Za-z0-9_]*\([^\r\n]*\)",
        candidate,
        re.I,
    ):
        return True
    return bool(
        re.fullmatch(
            r"(?:vault|aws-secretsmanager|azure-keyvault|gcp-secret-manager)://[^\s]+",
            candidate,
            re.I,
        )
    )


def _is_metadata_lhs(lhs: str) -> bool:
    normalized = re.sub(r"[^A-Za-z0-9]", "", lhs).upper()
    if any(normalized.endswith(suffix) for suffix in METADATA_LHS_SUFFIXES):
        return True
    return normalized.endswith("KEY") and "PASSWORD" in normalized


def _is_non_secret_literal_label(lhs: str, value: str) -> bool:
    normalized = re.sub(r"[^A-Za-z0-9]", "", lhs).upper()
    return normalized == "APIKEY" and bool(re.fullmatch(r"X-[A-Za-z0-9-]+", value, re.I))


def _is_test_context(path: Path) -> bool:
    lowered_parts = {part.lower() for part in path.parts}
    name = path.name.lower()
    return bool(lowered_parts & {"test", "tests"}) or any(
        marker in name for marker in (".test.", ".spec.", "test.java", "tests.java")
    )


def _is_synthetic_test_fixture(path: Path, lhs: str, value: str) -> bool:
    translated = value.lower().translate(str.maketrans("013457@", "oieasta"))
    if any(
        marker in translated
        for marker in ("test", "dummy", "example", "fake", "fixture", "mock", "sample")
    ):
        return True
    normalized_lhs = re.sub(r"[^A-Za-z0-9]", "", lhs).upper()
    if normalized_lhs.startswith(
        ("DUMMY", "EXPECTED", "FAKE", "FIXTURE", "INITIAL", "MOCK", "RAW", "SAMPLE", "SECOND", "TEST", "THIRD")
    ):
        return True
    if re.fullmatch(r"[a-z]+(?:-[a-z0-9]+)+", value):
        return True
    return False


def _looks_like_hard_coded_credential(
    path: Path, lhs: str, value: str, quoted: bool
) -> bool:
    if any(pattern.search(value) for pattern in STRONG_SECRET_PATTERNS.values()):
        return True
    if (
        _is_placeholder_or_sentinel(value)
        or _is_metadata_lhs(lhs)
        or _is_non_secret_literal_label(lhs, value)
        or (_is_config_context(path) and _is_environment_or_provider_reference(value))
    ):
        return False
    if _is_test_context(path) and _is_synthetic_test_fixture(path, lhs, value):
        return False

    length = len(value)
    if _is_config_context(path):
        if path.suffix.lower() == ".json" and not quoted:
            # Valid JSON credentials are strings; arrays/objects/scalars are structure.
            return False
        return length >= 6 and not any(character.isspace() for character in value)
    if quoted and re.fullmatch(r"(?:CPF|QA37|TEST)_[A-Z0-9_]+", value):
        # A quoted identifier used by verification code, not a credential value.
        return False
    if not quoted:
        # Source/SQL identifiers, method calls and expressions are not credentials.
        return False
    if length < 8 or any(character.isspace() for character in value):
        return False
    character_classes = sum(
        (
            any(character.islower() for character in value),
            any(character.isupper() for character in value),
            any(character.isdigit() for character in value),
            any(not character.isalnum() and not character.isspace() for character in value),
        )
    )
    has_punctuation = any(
        not character.isalnum() and not character.isspace() for character in value
    )
    return length >= 16 or character_classes >= 3 or has_punctuation


def find_secret_reasons(path: Path, text: str) -> set[str]:
    """Find only high-confidence secrets without returning credential values."""
    reasons = {
        name for name, pattern in STRONG_SECRET_PATTERNS.items() if pattern.search(text)
    }
    for line in text.splitlines():
        stripped = line.lstrip()
        if stripped.startswith(("#", "//", "/*", "*", "--")):
            continue
        for match in SECRET_ASSIGNMENT_PATTERN.finditer(line):
            if _quote_before(line, match.start()) is not None or _comment_before(
                line, match.start()
            ):
                continue
            lhs = match.group("lhs")
            rhs = match.group("rhs")
            if _is_config_context(path):
                rhs = _strip_inline_config_comment(rhs)
            value, quoted = _literal_value(rhs)
            if _looks_like_hard_coded_credential(path, lhs, value, quoted):
                reasons.add("hard-coded-credential-assignment")
    return reasons


def load_overlay_file_contract(root: Path) -> OverlayFileContract:
    """Load and validate the deterministic overlay file-count contract."""
    package = read_json(root / PACKAGE_MANIFEST)
    overlay = read_json(root / ROOT_OVERLAY_MANIFEST)
    if not isinstance(package, dict) or not isinstance(overlay, dict):
        fail("package and root overlay manifests must be JSON objects")
    change_path = root / CHANGE_MANIFEST
    try:
        with change_path.open(encoding="utf-8-sig", newline="") as handle:
            reader = csv.DictReader(handle)
            if not reader.fieldnames or not {"path", "change_type"}.issubset(reader.fieldnames):
                fail("change manifest requires path and change_type columns")
            raw_rows = list(reader)
    except OSError as exc:
        fail(f"change manifest unreadable {change_path}: {exc}")

    rows: list[dict[str, str]] = []
    change_paths: list[str] = []
    for row_number, row in enumerate(raw_rows, 2):
        relative = str(row.get("path") or "").strip()
        change_type = str(row.get("change_type") or "").strip()
        if change_type not in KNOWN_CHANGE_TYPES:
            fail(f"unknown change_type at row {row_number}: {change_type or '<empty>'}")
        if not safe_relative_path(relative):
            fail(f"unsafe change manifest path at row {row_number}: {relative}")
        normalized = dict(row)
        normalized["path"] = relative
        normalized["change_type"] = change_type
        rows.append(normalized)
        change_paths.append(relative)
    if len(change_paths) != len(set(change_paths)):
        fail("duplicate change manifest path")

    add_paths = [
        row["path"]
        for row in rows
        if row["change_type"] == "ADD_OR_REPLACE"
    ]
    raw_exclusions = overlay.get("changeManifestExclusions")
    if not isinstance(raw_exclusions, list) or not all(isinstance(value, str) for value in raw_exclusions):
        fail("root overlay changeManifestExclusions must be a string array")
    exclusions = [value.strip() for value in raw_exclusions]

    for label, paths in (("ADD_OR_REPLACE", add_paths), ("declared exclusion", exclusions)):
        invalid = [path for path in paths if not safe_relative_path(path)]
        if invalid:
            fail(f"unsafe {label} path: {invalid[0]}")
        if len(paths) != len(set(paths)):
            fail(f"duplicate {label} path")
    overlap = set(change_paths) & set(exclusions)
    if overlap:
        fail(f"change manifest and declared exclusion overlap: {sorted(overlap)[0]}")

    contract = OverlayFileContract(package, overlay, rows, set(add_paths), set(exclusions))
    declared_count = len(contract.expected_files)
    for label, manifest in (("package", package), ("root overlay", overlay)):
        if manifest.get("fileCount") != declared_count:
            fail(
                f"{label} fileCount drift manifest={manifest.get('fileCount')} "
                f"declaredOverlay={declared_count}"
            )
    return contract


def validate_delete_review_contract(
    contract: OverlayFileContract, delete_paths: list[str]
) -> None:
    declared = {
        row["path"]
        for row in contract.rows
        if row["change_type"] == "DELETE_REVIEW_REQUIRED"
    }
    actual = set(delete_paths)
    if declared != actual:
        fail(
            "change manifest delete coverage drift "
            f"missing={sorted(actual - declared)[:5]} "
            f"extra={sorted(declared - actual)[:5]}"
        )


def validate_overlay_file_presence(
    root: Path, contract: OverlayFileContract, overlay_package: bool
) -> None:
    """Apply exact-package or merged-repository file-set semantics."""
    if not overlay_package:
        # The overlay manifest is an immutable delivery-package contract.  Once
        # merged, later remediation may legitimately replace, relocate, or
        # delete an overlay path.  Current repository contracts are validated
        # by the source/consumer/DB/generator/matrix checks below; requiring all
        # historical overlay paths here would force stale files back into the
        # product tree.
        return

    missing = sorted(
        relative
        for relative in contract.expected_files
        if not (root / Path(*PurePosixPath(relative).parts)).is_file()
    )
    if missing:
        fail(f"declared overlay file missing: {missing[0]}")

    actual = {
        path.relative_to(root).as_posix()
        for path in root.rglob("*")
        if path.is_file()
    }
    if actual != contract.expected_files:
        fail(
            "overlay package file-set drift "
            f"missing={sorted(contract.expected_files - actual)[:5]} "
            f"extra={sorted(actual - contract.expected_files)[:5]}"
        )


def git_source_files(root: Path) -> list[Path]:
    """Return existing tracked and non-ignored worktree files.

    Tracked deletions are an intentional part of a remediation worktree and have
    no bytes for syntax or secret inspection.  Deletion policy is enforced by
    the repository-hygiene/change-manifest gates, so source traversal must not
    mistake an index entry deleted from the worktree for a corrupt source file.
    """
    result = subprocess.run(
        [
            "git",
            "-C",
            str(root),
            "ls-files",
            "--cached",
            "--others",
            "--exclude-standard",
            "-z",
        ],
        capture_output=True,
        timeout=60,
    )
    if result.returncode:
        stderr = result.stderr.decode("utf-8", errors="replace").strip()
        fail(f"cannot enumerate Git source files: {stderr}")

    deleted_result = subprocess.run(
        ["git", "-C", str(root), "ls-files", "--deleted", "-z"],
        capture_output=True,
        timeout=60,
    )
    if deleted_result.returncode:
        stderr = deleted_result.stderr.decode("utf-8", errors="replace").strip()
        fail(f"cannot enumerate deleted Git source files: {stderr}")
    deleted = {
        raw.decode("utf-8", errors="strict")
        for raw in deleted_result.stdout.split(b"\0")
        if raw
    }

    files: list[Path] = []
    seen: set[str] = set()
    for raw in result.stdout.split(b"\0"):
        if not raw:
            continue
        relative = raw.decode("utf-8", errors="strict")
        if not safe_relative_path(relative):
            fail(f"unsafe Git source path: {relative}")
        if relative in seen:
            fail(f"duplicate Git source path: {relative}")
        seen.add(relative)
        path = root / Path(*PurePosixPath(relative).parts)
        if path.is_file():
            files.append(path)
        elif relative in deleted:
            continue
        elif not path.is_dir():
            fail(f"Git source file missing from worktree: {relative}")
    return files


def source_files(
    root: Path, overlay_package: bool, contract: OverlayFileContract
) -> list[Path]:
    if overlay_package:
        return [
            root / Path(*PurePosixPath(relative).parts)
            for relative in sorted(contract.expected_files)
        ]
    return git_source_files(root)


def check_build(root: Path) -> None:
    path = root / "build.gradle"
    if not path.is_file():
        fail("root build.gradle missing")
    text = path.read_text(encoding="utf-8", errors="replace")
    if len(text.splitlines()) < 1000:
        fail("root build.gradle is not platform root contract")
    for token in [
        "allprojects",
        "subprojects",
        "qualityGate",
        "publishing",
        "cpfSourceSha",
        "cpfArtifactMode",
        "qa37SourceClosure",
        "qa37JavaLifecycle",
    ]:
        if token not in text:
            fail("root build.gradle missing " + token)
    if (
        "JavaLanguageVersion.of(rootProject.ext.cpfJavaVersion)" not in text
        or "ext.cpfJavaVersion" not in text
    ):
        fail("root build.gradle missing Java 25 toolchain indirection")
    for build_script in (source for source in git_source_files(root) if source.name == "build.gradle"):
        build_text = build_script.read_text(encoding="utf-8", errors="replace")
        relative = build_script.relative_to(root).as_posix()
        if LEGACY_PROJECT_EXEC_PATTERN.search(build_text):
            fail(f"{relative} uses removed Gradle Project.exec API; use providers.exec")
        if LEGACY_STACK_MAP_PATTERN.search(build_text):
            fail(f"{relative} uses removed cpfStack map; export a named canonical stack property")
    required = [
        "cpf-tools/build/gradle-plugin/build.gradle",
        "cpf-tools/build/gradle-plugin/settings.gradle",
        "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java",
        "cpf-tools/build/gradle-plugin/src/test/java/com/cpf/gradle/CpfPlatformConventionPluginTest.java",
        "cpf-tools/build/platform-bom/build.gradle",
        "cpf-tools/build/platform-bom/settings.gradle",
    ]
    for relative in required:
        if not (root / relative).is_file():
            fail("included build source missing " + relative)
    bza_build = root / "cpf-biz-admin/build.gradle"
    if bza_build.is_file() and path.read_bytes() == bza_build.read_bytes():
        fail("root build.gradle still equals BZA build")


def check_frontend(root: Path, overlay_package: bool) -> None:
    for module in ["cpf-admin", "cpf-biz-admin"]:
        base = root / module / "frontend"
        package = read_json(base / "package.json")
        lock = read_json(base / "package-lock.json")
        if package.get("packageManager") != "npm@10.9.2":
            fail(module + " packageManager mismatch")
        if package.get("engines", {}).get("node") != ">=22.18.0 <25":
            fail(module + " Node engine mismatch")
        verify = package.get("scripts", {}).get("verify", "")
        ordered = [
            "verify:lock",
            "verify:installed",
            "verify:primary",
            "test:openapi:lifecycle",
            "generate:api",
            "verify:generated",
            "verify:consumer",
            "lint",
            "typecheck",
            "test",
            "build:prod",
        ]
        commands = [part.strip() for part in verify.split("&&")]
        expected = ["npm run " + value for value in ordered]
        if commands != expected:
            fail(module + " frontend verify lifecycle order invalid: " + repr(commands))
        npmrc = (base / ".npmrc").read_text(encoding="utf-8")
        if "strict-peer-deps=true" not in npmrc or "legacy-peer-deps=false" not in npmrc:
            fail(module + " npm peer policy invalid")
        if lock.get("lockfileVersion") != 3:
            fail(module + " lockfileVersion must be 3")
        root_package = (lock.get("packages") or {}).get("", {})
        if (
            root_package.get("dependencies") != package.get("dependencies")
            or root_package.get("devDependencies") != package.get("devDependencies")
        ):
            fail(module + " package/lock dependency drift")
        if not overlay_package:
            lifecycle = base / "scripts/test-openapi-lifecycle.mjs"
            if lifecycle.is_file():
                run(["node", str(lifecycle)], 60)


def check_package(root: Path, overlay_package: bool) -> OverlayFileContract:
    contract = load_overlay_file_contract(root)
    validate_overlay_file_presence(root, contract, overlay_package)
    package = contract.package
    overlay = contract.overlay
    if package.get("baseSha") != BASE:
        fail("package manifest base SHA mismatch")
    if package.get("overallStatus") not in {"미검증", "재확인 필요"}:
        fail("package overall status must remain unverified before exact result evidence")
    package_id = str(package.get("packageId", "")).upper()
    if "FULL" in package_id or "COMPLETION" in package_id:
        fail("package id overclaims completion")
    excluded = set(package.get("protectedDocsExcluded") or [])
    if not PROTECTED.issubset(excluded):
        fail("protected README/Guide exclusion contract incomplete")

    delete_path = root / DELETE_MANIFEST
    delete = [
        value.strip()
        for value in delete_path.read_text(encoding="utf-8-sig").splitlines()
        if value.strip()
    ]
    if not delete or delete == ["NONE"]:
        fail("stale tracked document delete manifest missing")
    if len(delete) != len(set(delete)):
        fail("delete manifest contains duplicates")
    validate_delete_review_contract(contract, delete)
    if package.get("deleteCandidateCount") != len(delete):
        fail("package deleteCandidateCount drift")
    protected_tokens = [
        "CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md",
        "CPF_20260801_QA37_EDU_SOURCE_CLOSURE_AND_RECOVERY_REQUEST.md",
        "CPF_20260801_QA37_DEVELOPMENT_GPT_PROMPT.md",
        "CPF_FINAL_TARGET_REQUIREMENTS.md",
        "README.md",
        "cpf-docs/guides/",
    ]
    for relative in delete:
        if not relative.startswith("cpf-docs/work/current/"):
            fail("delete target outside reviewed current-work scope: " + relative)
        if any(token in relative for token in protected_tokens):
            fail("protected delete target: " + relative)

    review = root / "cpf-docs/quality/CPF_20260801_QA37_STALE_CURRENT_DOCUMENT_REVIEW.csv"
    with review.open(encoding="utf-8-sig", newline="") as handle:
        reviewed = {row.get("path") for row in csv.DictReader(handle)}
    if set(delete) != reviewed:
        fail("delete manifest and stale-document review drift")
    if overlay.get("deleteManifest") != DELETE_MANIFEST:
        fail("root overlay deleteManifest path invalid")
    change_text = (root / CHANGE_MANIFEST).read_text(encoding="utf-8-sig")
    if "CPF_20260801_QA37_COMPLETION_REPORT.md" in change_text:
        fail("stale completion report remains in change manifest")
    workflow = (root / ".github/workflows/cpf-qa37-source-closure.yml").read_text(
        encoding="utf-8"
    )
    if "gradlew.bat tasks" in workflow or "qualityGate qa37SourceClosure" in workflow:
        fail("CI repeats Gradle task discovery or frontend-containing qualityGate")
    if "clean qa37JavaLifecycle" not in workflow:
        fail("CI Java lifecycle task missing")
    return contract


def check_syntax(root: Path, files: list[Path]) -> None:
    for path in files:
        if path.suffix.lower() == ".json":
            read_json(path)
        elif path.suffix.lower() == ".csv":
            check_csv(path)

        relative = path.relative_to(root).as_posix()
        parts = PurePosixPath(relative).parts[:-1]
        for index, part in enumerate(parts):
            if part not in BANNED_DIRS:
                continue
            intentional_tools_build = (
                part == "build"
                and index == 1
                and parts[:2] == ("cpf-tools", "build")
            )
            if not intentional_tools_build:
                fail("generated garbage directory includes tracked file: " + relative)


def check_secrets(root: Path, files: list[Path]) -> None:
    findings = []
    binary_suffixes = {".zip", ".jar", ".class", ".png", ".jpg", ".jpeg", ".gif", ".ico"}
    for path in files:
        if path.suffix.lower() in binary_suffixes:
            continue
        if path.stat().st_size > 3_000_000:
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        if find_secret_reasons(path, text):
            findings.append(path.relative_to(root).as_posix())
    if findings:
        fail("possible secret material: " + ", ".join(findings[:10]))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--overlay-package", action="store_true")
    parser.add_argument("--skip-java-compile", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()

    check_build(root)
    contract = check_package(root, args.overlay_package)
    check_frontend(root, args.overlay_package)
    files = source_files(root, args.overlay_package, contract)
    check_syntax(root, files)
    check_secrets(root, files)
    python = sys.executable
    if args.overlay_package:
        run(
            [
                python,
                str(root / "cpf-tools/scripts/verify-cpf-qa37-package-integrity.py"),
                "--root",
                str(root),
            ],
            180,
        )

    # Truth and ownership gates are prerequisites. Stop before compilation/DB/Frontend if they fail.
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-qa37-completion-truth.py"),
            "--root",
            str(root),
        ],
        120,
    )
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-reference-package-layout.py"),
            "--root",
            str(root),
        ],
        120,
    )
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-reference-feature-isolation.py"),
            "--root",
            str(root),
        ],
        120,
    )
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-reference-feature-removal.py"),
            "--root",
            str(root),
        ],
        120,
    )
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-qa37-consumer-bindings.py"),
            "--root",
            str(root),
        ],
        120,
    )
    manual = [
        python,
        str(root / "cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py"),
        "--root",
        str(root),
    ]
    if not args.skip_java_compile:
        manual.append("--compile")
    run(manual, 240)
    run(
        [
            python,
            str(root / "cpf-tools/scripts/verify-cpf-qa37-db-generator-parity.py"),
            "--root",
            str(root),
            "--mode",
            "overlay" if args.overlay_package else "auto",
        ],
        180,
    )
    edu = [
        python,
        str(root / "cpf-tools/scripts/verify-cpf-qa37-edu32-source-closure.py"),
        "--root",
        str(root),
        "--self-test",
        "--overlay-contract" if args.overlay_package else "--merged-root",
    ]
    run(edu, 240)
    if args.overlay_package:
        print(f"[CPF][QA37][PACKAGE-CONTRACT][PASS] mergedSourceClosure=NOT_EXECUTED baseSha={BASE}")
    else:
        print(f"[CPF][QA37][MERGED-SOURCE][PASS] baseSha={BASE}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
