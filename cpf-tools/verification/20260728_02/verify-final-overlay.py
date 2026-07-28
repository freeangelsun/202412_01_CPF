#!/usr/bin/env python3
"""CPF 20260728_02 root overlay 정적 검증기.

외부 패키지 없이 JSON/SQL checksum/Java package/path/필수 Runtime 연결/민감정보 및
중간 산출물 잔존 여부를 검증합니다. 실제 Gradle, DB, WAS, Browser 검증을 대체하지 않습니다.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

EXPECTED_CHANGE_STATES = {
    "SCHEDULED", "APPLYING", "PARTIAL", "SUCCESS", "FAILED", "CANCELLED",
    "EXPIRED", "ROLLBACK_PENDING", "ROLLED_BACK", "SUPERSEDED",
    "UNKNOWN_RESULT", "RECOVERED",
}
EXPECTED_DELIVERY_STATES = {
    "PENDING", "CLAIMED", "ACKED", "FAILED", "POISONED", "UNKNOWN_RESULT",
    "RESTART_REQUIRED", "CANCELLED", "EXPIRED", "SUPERSEDED",
}
EXPECTED_DRIFT_STATES = {
    "IN_SYNC", "PENDING", "DRIFT", "UNKNOWN", "UNKNOWN_RESULT",
    "PENDING_RESTART", "EXCLUDED",
}
REQUIRED_FILES = [
    "cpf-core/src/main/java/com/cpf/core/api/runtimecontrol/CpfRuntimeStateCatalog.java",
    "cpf-core/src/main/java/com/cpf/core/common/runtimecontrol/CpfRuntimeControlPlaneService.java",
    "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/transport/JdkCpfGatewayHttpExchangeAdapter.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/transport/CpfGatewayReplayableBody.java",
    "cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue",
    "cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/BatchRuntimePolicyApplier.java",
    "cpf-tools/generator/contracts/central-domain-template-contract.json",
    "cpf-tools/generator/contracts/domain-metadata.schema.json",
    "cpf-tools/generator/create-domain.ps1",
]
FORBIDDEN_NAMES = {
    "CPF_INTERMEDIATE_FILE_INVENTORY_20260728.md",
    "CPF_INTERMEDIATE_NOT_FINAL_20260728.md",
    "CPF_INTERMEDIATE_REMAINING_WORK_20260728.md",
    "CPF_INTERMEDIATE_ROOT_OVERLAY_GUIDE_20260728.md",
    "CPF_INTERMEDIATE_SHA256SUMS_20260728.txt",
    "CPF_INTERMEDIATE_VALIDATION_LEDGER_20260728.md",
    "CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md",
    "CPF_FINAL_COMPLETION_PACKAGE_MANIFEST_20260728.md",
}
TEXT_SUFFIXES = {".java", ".kt", ".groovy", ".xml", ".json", ".yml", ".yaml", ".sql", ".md", ".txt", ".ps1", ".ts", ".vue", ".properties"}
SKIP_DIRS = {".git", ".gradle", "build", "out", "target", "node_modules", "dist", ".idea", ".vscode", "tmp", "temp"}



def iter_paths(root: Path):
    for path in root.rglob("*"):
        rel_parts = path.relative_to(root).parts
        if any(part in SKIP_DIRS for part in rel_parts):
            continue
        yield path


def iter_files(root: Path, pattern: str | None = None):
    for path in iter_paths(root):
        if not path.is_file():
            continue
        if pattern is None or path.match(pattern):
            yield path

def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def enum_values(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8")
    body = text.split("{", 1)[1].rsplit("}", 1)[0]
    body = re.sub(r"/\*.*?\*/|//.*", "", body, flags=re.S)
    return {token for token in re.findall(r"\b[A-Z][A-Z0-9_]+\b", body) if token not in {"ROOT"}}


def package_matches(root: Path, path: Path) -> bool:
    if path.suffix != ".java":
        return True
    text = path.read_text(encoding="utf-8")
    match = re.search(r"^\s*package\s+([\w.]+)\s*;", text, flags=re.M)
    if not match:
        return False
    rel = path.relative_to(root).as_posix()
    marker = "/src/"
    if marker not in rel:
        return True
    source_tail = rel.split(marker, 1)[1]
    for prefix in ("main/java/", "test/java/"):
        if source_tail.startswith(prefix):
            expected = source_tail[len(prefix):].rsplit("/", 1)[0].replace("/", ".")
            return match.group(1) == expected
    return True


def balanced_java(text: str) -> bool:
    # 문자열/문자/주석을 제거한 뒤 괄호 균형만 저비용 확인합니다.
    text = re.sub(r'"(?:\\.|[^"\\])*"', '""', text)
    text = re.sub(r"'(?:\\.|[^'\\])'", "''", text)
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    text = re.sub(r"//[^\n]*", "", text)
    pairs = {')': '(', ']': '[', '}': '{'}
    stack: list[str] = []
    for ch in text:
        if ch in "([{":
            stack.append(ch)
        elif ch in pairs:
            if not stack or stack.pop() != pairs[ch]:
                return False
    return not stack


def verify_checksum_file(checksum_file: Path) -> list[str]:
    errors: list[str] = []
    for line_no, raw in enumerate(checksum_file.read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line:
            continue
        match = re.fullmatch(r"([0-9a-fA-F]{64})\s+\*?(.+)", line)
        if not match:
            errors.append(f"checksum format: {checksum_file}:{line_no}")
            continue
        expected, name = match.groups()
        target = checksum_file.parent / name
        if target.exists() and sha256(target) != expected.lower():
            errors.append(f"checksum mismatch: {target}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[3])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    root = args.root.resolve()
    errors: list[str] = []
    warnings: list[str] = []
    passed: list[str] = []

    for rel in REQUIRED_FILES:
        if not (root / rel).is_file():
            errors.append(f"required file missing: {rel}")
    if not errors:
        passed.append("required overlay files")

    for path in root.rglob("*"):
        if path.name in FORBIDDEN_NAMES:
            errors.append(f"intermediate artifact remains: {path.relative_to(root)}")
        if path.is_file() and path.suffix.lower() in {".bak", ".tmp", ".class"}:
            errors.append(f"development residue: {path.relative_to(root)}")
        if (path.is_file() and path.suffix.lower() == ".log"
                and "evidence" not in path.relative_to(root).parts):
            errors.append(f"development residue: {path.relative_to(root)}")

    for rel in (
        "cpf-tools/generator/contracts/central-domain-template-contract.json",
        "cpf-tools/generator/contracts/domain-metadata.schema.json",
    ):
        try:
            json.loads((root / rel).read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"invalid json {rel}: {exc}")
    if not any("invalid json" in item for item in errors):
        passed.append("generator JSON syntax")

    contract = json.loads((root / "cpf-tools/generator/contracts/central-domain-template-contract.json").read_text(encoding="utf-8"))
    contract_text = json.dumps(contract, ensure_ascii=False)
    for token in ("runtimeAgentContract", "runtime-agent", "CPF_RUNTIME_CONTROL_AGENT_TOKEN", "failClosed"):
        if token not in contract_text:
            errors.append(f"generator runtime-agent contract missing: {token}")
    schema = json.loads((root / "cpf-tools/generator/contracts/domain-metadata.schema.json").read_text(encoding="utf-8"))
    schema_text = json.dumps(schema, ensure_ascii=False)
    for vendor in ("mariadb", "postgresql", "oracle"):
        if vendor not in schema_text.lower():
            errors.append(f"official DB vendor missing in schema: {vendor}")
    for unsupported in ('"mysql"', '"sqlserver"', '"mssql"', '"h2"'):
        if unsupported in schema_text.lower():
            errors.append(f"unsupported DB vendor remains in schema: {unsupported}")

    enum_checks = [
        ("CpfRuntimeChangeState.java", EXPECTED_CHANGE_STATES),
        ("CpfRuntimeDeliveryState.java", EXPECTED_DELIVERY_STATES),
        ("CpfRuntimeDriftState.java", EXPECTED_DRIFT_STATES),
    ]
    enum_dir = root / "cpf-core/src/main/java/com/cpf/core/api/runtimecontrol"
    for name, expected in enum_checks:
        actual = enum_values(enum_dir / name)
        if actual != expected:
            errors.append(f"runtime enum parity mismatch {name}: actual={sorted(actual)}")
    if not any("runtime enum parity" in item for item in errors):
        passed.append("runtime canonical enum parity")

    pairs = [
        (
            "cpf-tools/db/vendor/mariadb/source/migration/flyway/V66__adm_runtime_control_menu.sql",
            "cpf-tools/db/vendor/mariadb/migration/flyway/V66__adm_runtime_control_menu.sql",
        ),
        (
            "cpf-tools/db/vendor/mariadb/source/migration/rollback/R66__adm_runtime_control_menu.sql",
            "cpf-tools/db/vendor/mariadb/rollback/R66__adm_runtime_control_menu.sql",
        ),
    ]
    for left, right in pairs:
        if (root / left).read_bytes() != (root / right).read_bytes():
            errors.append(f"MariaDB source/runtime drift: {left} != {right}")
    for checksum in iter_files(root, "**/checksums.sha256"):
        errors.extend(verify_checksum_file(checksum))
    if not any("checksum" in item or "source/runtime drift" in item for item in errors):
        passed.append("DB source/runtime and checksum parity")

    sql_text = "\n".join(path.read_text(encoding="utf-8") for path in iter_files(root, "**/*adm_runtime_control_menu.sql"))
    for permission in (
        "RUNTIME_CONTROL_READ", "RUNTIME_CONTROL_PREVIEW", "RUNTIME_CONTROL_WRITE",
        "RUNTIME_CONTROL_CONTROL", "RUNTIME_CONTROL_GROUP_WRITE", "RUNTIME_CONTROL_GROUP_DELETE",
    ):
        if permission not in sql_text:
            errors.append(f"runtime control permission missing: {permission}")

    for java_file in iter_files(root, "**/*.java"):
        if not package_matches(root, java_file):
            errors.append(f"Java package/path mismatch: {java_file.relative_to(root)}")
        if not balanced_java(java_file.read_text(encoding="utf-8")):
            errors.append(f"Java delimiter mismatch: {java_file.relative_to(root)}")
    if not any("Java package" in item or "Java delimiter" in item for item in errors):
        passed.append("Java package/path and delimiter")

    gateway_service = (root / "cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java").read_text(encoding="utf-8")
    gateway_adapter = (root / "cpf-gateway/src/main/java/com/cpf/gateway/transport/JdkCpfGatewayHttpExchangeAdapter.java").read_text(encoding="utf-8")
    gateway_security = (root / "cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfApiClientSecurityPolicy.java").read_text(encoding="utf-8")
    for token, text in (
        ("StreamingResponseBody", gateway_service),
        ("CpfGatewayReplayableBody", gateway_service),
        ("HttpClient", gateway_adapter),
        ("new CpfGatewayPrincipal(true", gateway_security),
    ):
        if token not in text:
            errors.append(f"gateway runtime connection missing: {token}")

    scheduler = (root / "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java").read_text(encoding="utf-8")
    worker = (root / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRuntime.java").read_text(encoding="utf-8")
    batch_policy = (root / "cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/BatchRuntimePolicy.java").read_text(encoding="utf-8")
    if "schedulerEnabled" not in scheduler or "calendarEnabled" not in scheduler:
        errors.append("Batch scheduler does not consume schedule/calendar runtime policy")
    if "workerConcurrencyLimit" not in worker or "workerEnabled" not in worker:
        errors.append("Batch worker does not consume runtime policy")
    batch_applier = (root / "cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/BatchRuntimePolicyApplier.java").read_text(encoding="utf-8")
    if ("BATCH_PARTITION" in batch_policy or "maxPartitions" in batch_policy
            or "BATCH_PARTITION" in batch_applier or "replacePartition" in batch_applier):
        errors.append("Batch partition policy has no proven durable Consumer and must not be exposed")
    if "logCollectionEnabled" not in batch_applier or "agentLogCollectionEnabled" not in batch_applier:
        errors.append("Batch Agent log collection runtime policy is not fully applied")
    for token in ("0L", "MAX_CONCURRENCY"):
        if token not in batch_policy:
            errors.append(f"Batch runtime canonical default is missing: {token}")
    if "version == previous.version()" not in batch_policy or "next.equals(previous)" not in batch_policy:
        errors.append("Batch runtime idempotent replay guard is missing")

    vue = (root / "cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue").read_text(encoding="utf-8")
    routes = (root / "cpf-admin/frontend/src/app/routes.ts").read_text(encoding="utf-8")
    for tag in ("<template>", "<script", "</script>", "</template>"):
        if tag not in vue:
            errors.append(f"RuntimeControlPage missing Vue section: {tag}")
    if "RuntimeControlPage.vue" not in routes or "runtimeControl" not in routes:
        errors.append("ADM runtime control route missing")
    for token in (
        "/adm/api/runtime-control/groups",
        "/members",
        "expectedVersion",
        "requestedBy",
    ):
        if token not in vue:
            errors.append(f"ADM runtime group UI connection missing: {token}")

    secret_patterns = [
        re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
        re.compile(r"\bgh[pousr]_[A-Za-z0-9]{30,}\b"),
        re.compile(r"(?i)\b(?:password|secret|token)\s*[:=]\s*[\"'][^\"'${}<>\s]{8,}[\"']"),
    ]
    for path in iter_files(root):
        if not path.is_file() or path.suffix.lower() not in TEXT_SUFFIXES:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        for pattern in secret_patterns:
            if pattern.search(text):
                errors.append(f"possible secret: {path.relative_to(root)} ({pattern.pattern})")
    if not any("possible secret" in item for item in errors):
        passed.append("targeted secret scan")

    # 명시적으로 이 검증기가 대체하지 못하는 검증을 경고로 남깁니다.
    warnings.extend([
        "Java 25 / Gradle 9.1 전체 모듈 build·test는 별도 실행 필요",
        "MariaDB·PostgreSQL·Oracle install/upgrade/rollback 실DB 검증은 별도 실행 필요",
        "ADM Browser와 Gateway 대용량/multipart/range/timeout 실Runtime 검증은 별도 실행 필요",
        "다중 인스턴스·부분 실패·재시작 복구 검증은 별도 실행 필요",
    ])

    status = "PASS" if not errors else "FAIL"
    result = {"status": status, "root": str(root), "passed": passed, "warnings": warnings, "errors": errors}
    rendered = json.dumps(result, ensure_ascii=False, indent=2)
    print(rendered)
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(rendered + "\n", encoding="utf-8")
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
