#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

GARBAGE_PARTS = {"__pycache__", "node_modules", ".gradle", "build", "dist", "coverage", "test-results"}
GARBAGE_SUFFIXES = {".pyc", ".log", ".tmp", ".bak", ".orig", ".rej", ".patch", ".hprof"}
VENDORS = ("mariadb", "postgresql", "oracle")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def check_garbage(root: Path, failures: list[str]) -> None:
    for path in root.rglob("*"):
        relative = path.relative_to(root)
        tooling_source = len(relative.parts) >= 2 and relative.parts[0] == "cpf-tools" and relative.parts[1] == "build"
        if not tooling_source and any(part in GARBAGE_PARTS for part in relative.parts):
            failures.append(f"garbage path:{relative.as_posix()}")
        if path.is_file() and path.suffix.lower() in GARBAGE_SUFFIXES:
            failures.append(f"garbage file:{relative.as_posix()}")
        if path.is_file():
            try:
                text = path.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            container_prefix = "/mnt" + "/data/"
            if re.search(re.escape(container_prefix) + r"(?:CPF|cpf|overlay|work)[^\s\'\"]*", text) or re.search(r"(?i)[A-Z]:\\(?:WORK|dev|temp|users)\\", text):
                failures.append(f"absolute work path:{relative.as_posix()}")


def check_java_packages(root: Path, failures: list[str]) -> None:
    for path in root.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        match = re.search(r"(?m)^package\s+([A-Za-z0-9_.]+)\s*;", text)
        if not match:
            failures.append(f"java package missing:{path.relative_to(root).as_posix()}")
            continue
        marker = "/src/main/java/" if "/src/main/java/" in path.as_posix() else "/src/test/java/"
        if marker not in path.as_posix():
            continue
        expected = path.as_posix().split(marker, 1)[1].rsplit("/", 1)[0].replace("/", ".")
        if match.group(1) != expected:
            failures.append(
                f"java package mismatch:{path.relative_to(root).as_posix()}:{match.group(1)}!={expected}"
            )



def check_internal_imports(root: Path, failures: list[str], overlay: bool) -> None:
    """전체 Repository에서 com.cpf 내부 import가 실제 top-level type을 가리키는지 검사합니다.

    Overlay 단독에는 변경되지 않은 Base Source가 없으므로 오탐 방지를 위해 적용 후 전체 Root에서만 실행합니다.
    """
    if overlay:
        return
    declared: set[str] = set()
    java_files = list(root.rglob("*.java"))
    for path in java_files:
        text = path.read_text(encoding="utf-8", errors="replace")
        package = re.search(r"(?m)^package\s+([A-Za-z0-9_.]+)\s*;", text)
        if package:
            declared.add(f"{package.group(1)}.{path.stem}")
    for path in java_files:
        text = path.read_text(encoding="utf-8", errors="replace")
        for imported in re.findall(r"(?m)^import\s+(?!static\s)(com\.cpf\.[A-Za-z0-9_.]+)\s*;", text):
            if imported not in declared:
                failures.append(
                    f"internal import target missing:{path.relative_to(root).as_posix()}:{imported}"
                )

def check_build_graph(root: Path, failures: list[str], overlay: bool) -> None:
    settings = root / "settings.gradle"
    if not settings.is_file():
        if not overlay:
            failures.append("settings.gradle missing")
        return
    text = settings.read_text(encoding="utf-8")
    for included in ("cpf-tools/build/platform-bom", "cpf-tools/build/gradle-plugin"):
        if f"includeBuild '{included}'" not in text and f'includeBuild "{included}"' not in text:
            failures.append(f"included build missing:{included}")
        if not (root / included / "settings.gradle").is_file() or not (root / included / "build.gradle").is_file():
            failures.append(f"included build source missing:{included}")
    bom = root / "cpf-tools/build/platform-bom/build.gradle"
    if bom.is_file():
        content = bom.read_text(encoding="utf-8")
        if re.search(r"(?m)^def\s+components\s*=", content):
            failures.append("BOM components variable shadows Gradle SoftwareComponentContainer")
        if "from project.components.javaPlatform" not in content:
            failures.append("BOM publication does not publish javaPlatform component")
    root_build = root / "build.gradle"
    if root_build.is_file():
        content = root_build.read_text(encoding="utf-8")
        global_driver = re.search(
            r"(?s)subprojects\s*\{.*?runtimeOnly\s+['\"]org\.mariadb\.jdbc:mariadb-java-client",
            content,
        )
        if global_driver:
            failures.append("MariaDB driver is globally injected into all subprojects")


def read_checksums(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        match = re.match(r"^([0-9a-fA-F]{64})\s+\*?(.+)$", line)
        if match:
            result[Path(match.group(2).strip()).name] = match.group(1).lower()
    return result


def check_migrations(root: Path, failures: list[str]) -> None:
    migration_pattern = re.compile(r"^V(?:83|8[4-9]|9[0-1])__.+\.sql$")
    for path in (candidate for candidate in root.rglob("V*.sql") if migration_pattern.match(candidate.name)):
        candidates = []
        parent = path.parent
        candidates.append(parent / "checksums.sha256")
        # MariaDB rollback/runtime layouts and Oracle/PostgreSQL admDB/batDB layouts use local manifests.
        manifest = next((candidate for candidate in candidates if candidate.is_file()), None)
        if manifest is None:
            failures.append(f"checksum manifest missing:{path.relative_to(root).as_posix()}")
            continue
        entries = read_checksums(manifest)
        expected = entries.get(path.name)
        actual = sha256(path)
        if expected is None:
            failures.append(f"checksum row missing:{path.relative_to(root).as_posix()}")
        elif expected != actual:
            failures.append(f"checksum mismatch:{path.relative_to(root).as_posix()}")


def check_frontend_lock(root: Path, failures: list[str], overlay: bool) -> None:
    # Root Overlay는 승인 Registry에서 생성해야 하는 lock artifact를 의도적으로 포함하지 않을 수 있다.
    # Full Repository Gate에서만 package/lock 정합성과 package entry를 강제한다.
    if overlay:
        return
    for frontend in (root / "cpf-admin/frontend", root / "cpf-biz-admin/frontend"):
        package = frontend / "package.json"
        lock = frontend / "package-lock.json"
        if not package.is_file() or not lock.is_file():
            failures.append(f"frontend package/lock missing:{frontend.relative_to(root).as_posix()}")
            continue
        package_data = json.loads(package.read_text(encoding="utf-8"))
        lock_data = json.loads(lock.read_text(encoding="utf-8"))
        root_entry = lock_data.get("packages", {}).get("", {})
        for section in ("dependencies", "devDependencies", "optionalDependencies"):
            expected = package_data.get(section, {}) or {}
            actual = root_entry.get(section, {}) or {}
            if expected != actual:
                failures.append(f"frontend lock root drift:{frontend.relative_to(root).as_posix()}:{section}")
            for name in expected:
                key = f"node_modules/{name}"
                if key not in lock_data.get("packages", {}):
                    failures.append(f"frontend lock package missing:{frontend.relative_to(root).as_posix()}:{name}")



def check_frontend_primary(root: Path, failures: list[str], overlay: bool) -> None:
    for frontend in (root / "cpf-admin/frontend", root / "cpf-biz-admin/frontend"):
        package = frontend / "package.json"
        if not package.exists() and overlay:
            continue
        relative = frontend.relative_to(root).as_posix()
        required = (
            frontend / "openapi/cpf-openapi.json",
            frontend / "scripts/verify-generated-client.mjs",
            frontend / "src/shared/orval-mutator.ts",
        ) if overlay else (
            frontend / "orval.config.ts",
            frontend / "openapi/cpf-openapi.json",
            frontend / "scripts/verify-generated-client.mjs",
            frontend / "src/shared/orval-mutator.ts",
        )
        for path in required:
            if not path.is_file():
                failures.append(f"frontend generated-client contract missing:{relative}:{path.relative_to(frontend).as_posix()}")
        generated = frontend / "src/generated"
        if not overlay and (not generated.is_dir() or not any(generated.rglob("*.ts"))):
            failures.append(f"frontend generated client missing:{relative}")

        source_root = frontend / "src"
        if source_root.is_dir():
            for path in source_root.rglob("*.ts"):
                rel = path.relative_to(frontend).as_posix()
                if rel in {"src/shared/orval-mutator.ts", "src/shared/cpfApi.test.ts"}:
                    continue
                text = path.read_text(encoding="utf-8")
                if re.search(r"\bfetch\s*\(", text):
                    failures.append(f"frontend direct fetch remains:{relative}:{rel}")
                if "Promise.allSettled(" in text and not (
                    "initialization.record" in text and "initialization.complete()" in text
                ):
                    failures.append(f"frontend required initialization may swallow failure:{relative}:{rel}")


def check_security_contract(root: Path, failures: list[str]) -> None:
    bridge = root / "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfBffSessionBridgeFilter.java"
    advice = root / "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfBffCredentialResponseAdvice.java"
    config = root / "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfServerSessionSecurityAutoConfiguration.java"
    for path, markers in (
        (bridge, ("CREDENTIAL_HANDLE", "INTERNAL_REFRESH_TOKEN_ATTRIBUTE", "Browser Authorization header is prohibited")),
        (advice, ("concurrentSessions.register", "sanitized.remove(\"sessionId\")", "vault.rotate")),
        (config, ("CookieCsrfTokenRepository", "CpfTrustedOriginFilter", "FindByIndexNameSessionRepository")),
    ):
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for marker in markers:
            if marker not in text:
                failures.append(f"security marker missing:{path.relative_to(root).as_posix()}:{marker}")
    if bridge.is_file() and re.search(r"session\.setAttribute\([^\n]*(ACCESS_TOKEN|REFRESH_TOKEN)", bridge.read_text(encoding="utf-8")):
        failures.append("raw credential is stored in HTTP session")





def check_session_migration(root: Path, failures: list[str]) -> None:
    paths = (
        root / "cpf-tools/db/vendor/mariadb/migration/flyway/V83__spring_session_jdbc_bff.sql",
        root / "cpf-tools/db/vendor/postgresql/migration/flyway/admDB/V83__spring_session_jdbc_bff.sql",
        root / "cpf-tools/db/vendor/oracle/migration/flyway/admDB/V83__spring_session_jdbc_bff.sql",
    )
    for path in paths:
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8").upper()
        conditional = "IF NOT EXISTS" in text or "SQLCODE != -955" in text
        if not conditional:
            failures.append(f"session migration is unconditional:{path.relative_to(root).as_posix()}")
    verifier = root / "cpf-starters/security/src/main/java/com/cpf/starter/security/CpfSessionReadinessVerifier.java"
    if verifier.is_file():
        text = verifier.read_text(encoding="utf-8")
        for marker in ("SPRING_SESSION", "SPRING_SESSION_ATTRIBUTES", "index"):
            if marker.lower() not in text.lower():
                failures.append(f"session drift verifier marker missing:{marker}")


def check_safe_rollbacks(root: Path, failures: list[str]) -> None:
    paths = (
        root / "cpf-tools/db/vendor/mariadb/rollback/R82__spring_batch_primary_control_link.sql",
        root / "cpf-tools/db/vendor/postgresql/rollback/batDB/R82__spring_batch_primary_control_link.sql",
        root / "cpf-tools/db/vendor/oracle/rollback/batDB/R82__spring_batch_primary_control_link.sql",
    )
    for path in paths:
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8").lower()
        if "drop table" in text:
            failures.append(f"destructive default rollback:{path.relative_to(root).as_posix()}")
        if "_r82_archive" not in text:
            failures.append(f"rollback retention archive missing:{path.relative_to(root).as_posix()}")
        destructive = path.parent / "destructive/D82__drop_archived_spring_batch_control.sql"
        if not destructive.is_file() or "CPF_DESTRUCTIVE_ROLLBACK_APPROVAL_REQUIRED" not in destructive.read_text(encoding="utf-8"):
            failures.append(f"approved destructive rollback separation missing:{path.relative_to(root).as_posix()}")


def check_architecture_ownership(root: Path, failures: list[str]) -> None:
    core_build = root / "cpf-core/build.gradle"
    if core_build.is_file():
        text = core_build.read_text(encoding="utf-8")
        forbidden_runtime = (
            "implementation 'org.springframework.kafka:spring-kafka'",
            "runtimeOnly 'org.springframework.kafka:spring-kafka'",
            "implementation 'org.springframework.boot:spring-boot-starter-amqp'",
            "runtimeOnly 'org.springframework.boot:spring-boot-starter-amqp'",
            "implementation 'io.opentelemetry:opentelemetry-sdk'",
            "implementation 'io.opentelemetry:opentelemetry-exporter-otlp'",
            "implementation 'org.springframework.boot:spring-boot-starter-webflux'",
        )
        for marker in forbidden_runtime:
            if marker in text:
                failures.append(f"core runtime ownership violation:{marker}")

    core_bridge = root / "cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerBridgeAdapter.java"
    if core_bridge.is_file():
        text = core_bridge.read_text(encoding="utf-8")
        for forbidden in ("org.springframework.kafka", "org.springframework.amqp", "RabbitTemplate", "KafkaTemplate"):
            if forbidden in text:
                failures.append(f"core broker adapter owns remote runtime:{forbidden}")
        if 'havingValue = "IN_MEMORY"' not in text:
            failures.append("core broker adapter is not limited to IN_MEMORY topology")

    kafka_bridge = root / "cpf-starters/messaging-kafka/src/main/java/com/cpf/starter/kafka/KafkaCpfBrokerBridgeAdapter.java"
    kafka_auto = root / "cpf-starters/messaging-kafka/src/main/java/com/cpf/starter/kafka/CpfKafkaAutoConfiguration.java"
    kafka_client = root / "cpf-starters/messaging-kafka/src/main/java/com/cpf/starter/kafka/KafkaCpfBrokerClient.java"
    for path, markers in (
        (kafka_bridge, ("implements CpfBrokerBridgePort", "result is UNKNOWN")),
        (kafka_auto, ('havingValue = "KAFKA"', "CpfBrokerBridgePort")),
        (kafka_client, ("TimeoutException", "ExecutionException", "Thread.currentThread().interrupt()")),
    ):
        if path.is_file():
            text = path.read_text(encoding="utf-8")
            for marker in markers:
                if marker not in text:
                    failures.append(f"kafka starter ownership marker missing:{path.relative_to(root).as_posix()}:{marker}")

    common_imports = root / "cpf-common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
    if common_imports.is_file() and "com.cpf.common.cache.CpfCacheAutoConfiguration" in common_imports.read_text(encoding="utf-8"):
        failures.append("cpf-common still auto-activates cache runtime")

    common_build = root / "cpf-common/build.gradle"
    if common_build.is_file():
        text = common_build.read_text(encoding="utf-8")
        for marker in (
            "implementation 'com.github.ben-manes.caffeine:caffeine",
            "api 'com.github.ben-manes.caffeine:caffeine",
            "implementation 'org.springframework.boot:spring-boot-starter-data-redis",
            "api 'org.springframework.boot:spring-boot-starter-data-redis",
        ):
            if marker in text:
                failures.append(f"cpf-common cache runtime ownership violation:{marker}")

    cache_build = root / "cpf-starters/cache/build.gradle"
    cache_auto = root / "cpf-starters/cache/src/main/java/com/cpf/starter/cache/CpfCacheAutoConfiguration.java"
    if cache_build.is_file():
        text = cache_build.read_text(encoding="utf-8")
        for marker in ("caffeine", "spring-boot-starter-data-redis"):
            if marker not in text:
                failures.append(f"cache starter dependency missing:{marker}")
    if cache_auto.is_file():
        text = cache_auto.read_text(encoding="utf-8")
        for marker in ("@Import(com.cpf.common.cache.CpfCacheAutoConfiguration.class)", 'havingValue = "CAFFEINE"'):
            if marker not in text:
                failures.append(f"cache starter owner marker missing:{marker}")



def check_bootstrap_recovery(root: Path, failures: list[str]) -> None:
    runner = root / "cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/service/BzaBootstrapRunner.java"
    repository = root / "cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/service/BzaBootstrapApprovalRepository.java"
    test = root / "cpf-biz-admin/src/test/java/com/cpf/bizadmin/auth/service/BzaBootstrapRunnerSecurityTest.java"
    for path, markers in (
        (runner, ("claimOwnerId()", "claim-lease-seconds", "verifySecretAcl", "reconcileComplete", "approvals.cleanup", "destroySecrets")),
        (repository, ("CLAIM_OWNER_ID", "CLAIM_EXPIRES_AT", "CLEANUP_STATUS", "reconcileComplete")),
        (test, ("rejectsSecretReadableByGroupOrOthers", "acceptsOwnerOnlySecretAndDestroysIt")),
    ):
        if not path.is_file():
            if not (path == test and not (root / "cpf-biz-admin/src").exists()):
                failures.append(f"bootstrap recovery source missing:{path.relative_to(root).as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        for marker in markers:
            if marker not in text:
                failures.append(f"bootstrap recovery marker missing:{path.relative_to(root).as_posix()}:{marker}")

    migration_paths = (
        root / "cpf-tools/db/vendor/mariadb/migration/flyway/V91__bza_bootstrap_claim_recovery.sql",
        root / "cpf-tools/db/vendor/postgresql/migration/flyway/bzaDB/V91__bza_bootstrap_claim_recovery.sql",
        root / "cpf-tools/db/vendor/oracle/migration/flyway/bzaDB/V91__bza_bootstrap_claim_recovery.sql",
    )
    for path in migration_paths:
        if path.is_file():
            text = path.read_text(encoding="utf-8").upper()
            for marker in ("CLAIM_OWNER_ID", "CLAIM_EXPIRES_AT", "CLEANUP_STATUS", "IX_BZA_BOOTSTRAP_CLAIM_LEASE"):
                if marker not in text:
                    failures.append(f"bootstrap migration marker missing:{path.relative_to(root).as_posix()}:{marker}")

    canonical = root / "cpf-tools/db/canonical/platform-schema.json"
    if canonical.is_file():
        data = json.loads(canonical.read_text(encoding="utf-8"))
        tables = [table for table in data.get("tables", []) if table.get("name") == "bza_bootstrap_approval"]
        if len(tables) != 1:
            failures.append("canonical bootstrap table missing or duplicated")
        else:
            columns = {column.get("name") for column in tables[0].get("columns", [])}
            for column in ("claim_owner_id", "claim_expires_at", "cleanup_status", "cleanup_failure_code", "cleanup_updated_at"):
                if column not in columns:
                    failures.append(f"canonical bootstrap column missing:{column}")
        if data.get("tableCount") != len(data.get("tables", [])):
            failures.append("canonical schema tableCount drift")



def check_operational_state_machines(root: Path, failures: list[str]) -> None:
    contracts = (
        (
            root / "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentEngine.java",
            ("compensateKnown", "retainUnknown", "reconcileLockResult", "DEPLOYMENT_LOCK_RELEASE"),
        ),
        (
            root / "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentExecutionRepository.java",
            ("requestHash", "assertSameRequest", "reconcileTerminal"),
        ),
        (
            root / "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactVerifier.java",
            ("keyId", "isRevoked", "getNotBefore", "getNotAfter", "Ed25519"),
        ),
        (
            root / "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactStateStore.java",
            ("HmacSHA256", "ATOMIC_MOVE", "requireSecureFile", "setOwnerOnly"),
        ),
        (
            root / "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactInstaller.java",
            ("X-Checksum-Sha256", "Content-Type", "getArtifactAllowedHosts", "rollback"),
        ),
        (
            root / "cpf-core/src/main/java/com/cpf/core/common/archive/LocalCpfArchiveService.java",
            ("ExtractionTransaction", "ARCHIVE_DUPLICATE_CANONICAL_ENTRY", "publish", "rollback"),
        ),
        (
            root / "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerRecoverySpool.java",
            ("ATOMIC_MOVE", "capBytes", "sanitize"),
        ),
    )
    for path, markers in contracts:
        if not path.is_file():
            if (root.joinpath("cpf-batch").exists() or root.joinpath("cpf-core").exists() or root.joinpath("cpf-gateway").exists()):
                failures.append(f"operational state-machine source missing:{path.relative_to(root).as_posix()}")
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        for marker in markers:
            if marker not in text:
                failures.append(f"operational state-machine marker missing:{path.relative_to(root).as_posix()}:{marker}")

    tests = (
        root / "cpf-batch/control-server/src/test/java/com/cpf/batch/control/deploy/DeploymentEngineStateMachineTest.java",
        root / "cpf-batch/control-server/src/test/java/com/cpf/batch/control/deploy/RuntimeLifecycleSecurityTest.java",
        root / "cpf-batch/host-agent/src/test/java/com/cpf/batch/agent/internal/ArtifactInstallerSecurityTest.java",
        root / "cpf-core/src/test/java/com/cpf/core/common/archive/LocalCpfArchiveServiceStreamingTest.java",
        root / "cpf-batch/execution-runtime/src/test/java/com/cpf/batch/execution/CpfRemoteChunkItemProcessorTest.java",
    )
    for path in tests:
        if not path.is_file():
            failures.append(f"operational state-machine test missing:{path.relative_to(root).as_posix()}")

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--overlay", action="store_true")
    parser.add_argument("--json-report")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []
    check_garbage(root, failures)
    check_java_packages(root, failures)
    check_internal_imports(root, failures, args.overlay)
    check_build_graph(root, failures, args.overlay)
    check_migrations(root, failures)
    check_safe_rollbacks(root, failures)
    check_session_migration(root, failures)
    check_frontend_lock(root, failures, args.overlay)
    check_frontend_primary(root, failures, args.overlay)
    check_security_contract(root, failures)
    check_bootstrap_recovery(root, failures)
    check_architecture_ownership(root, failures)
    check_operational_state_machines(root, failures)
    report = {
        "schemaVersion": 1,
        "root": str(root),
        "overlayMode": args.overlay,
        "failures": sorted(set(failures)),
        "status": "PASS" if not failures else "FAIL",
    }
    if args.json_report:
        target = Path(args.json_report)
        if not target.is_absolute():
            target = root / target
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
