#!/usr/bin/env python3
from __future__ import annotations

import csv
import hashlib
import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[3]
CATALOG = ROOT / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
PROFILES = ROOT / "cpf-tools/generator/contracts/capability-profiles.json"
DELETE_PATHS = ROOT / "cpf-docs/work/CPF_DELETE_MANIFEST.txt"
DELETE_WORK_ITEMS = ROOT / "cpf-docs/work/CPF_PRODUCT_DELETE_WORK_ITEMS.csv"
DELETE_ONE_LINE = ROOT / "cpf-docs/work/CPF_DELETE_ONE_LINE.ps1.txt"
ARTIFACT_CATALOG = ROOT / "cpf-tools/release/cpf-final-artifact-catalog.json"
REQUIREMENT_MATRIX = ROOT / "cpf-docs/work/CPF_REQUIREMENT_MATRIX.csv"
SCENARIO_MATRIX = ROOT / "cpf-docs/work/CPF_SCENARIO_MATRIX.csv"
EXPECTED_PROFILES = ["minimal-domain", "web-api", "secure-api", "browser-bff", "event-service", "batch-service"]
EXPECTED_GROUPS = ["data", "messaging", "integration", "file", "notification", "security", "platform-operations"]
EXPECTED_STANDARDS = [
    "standard-error", "header-context", "transaction-id", "security-boundary", "audit", "masking",
    "observability", "config", "dependency-version", "architecture-gate",
]
PROTECTED_PREFIXES = (
    "cpf-docs/deliverables/", "cpf-docs/guides/", "cpf-docs/environment/docker/",
    "cpf-tools/environment/docker-development-test/",
)
EXCEPTION_FIELDS = [
    "exception_id", "module", "capability", "artifact", "version", "owner", "reason",
    "standard_path_gap", "environments", "security_impact", "license_review", "supply_chain_review",
    "operations_responsibility", "approved_by", "approved_at", "expires_at", "rollback", "return_plan",
    "rule_ids", "config_files", "evidence_path", "status", "config_hash",
]
HASH_FIELDS = EXCEPTION_FIELDS[:-1]
FORBIDDEN_ARTIFACTS = {
    "org.springframework.kafka:spring-kafka": "messaging",
    "org.springframework.amqp:spring-rabbit": "messaging",
    "org.springframework:spring-jms": "messaging",
    "jakarta.jms:jakarta.jms-api": "messaging",
    "org.springframework:spring-jdbc": "data",
    "org.springframework.data:spring-data-redis": "data",
    "io.lettuce:lettuce-core": "data",
    "redis.clients:jedis": "data",
    "io.opentelemetry:opentelemetry-sdk": "platform-operations",
    "org.apache.sshd:sshd-sftp": "file",
    "com.github.mwiede:jsch": "file",
    "io.github.resilience4j:resilience4j-spring-boot3": "integration",
    "dev.openfeature:sdk": "platform-operations",
}
FORBIDDEN_SYMBOLS = {
    "KafkaTemplate": "messaging", "RabbitTemplate": "messaging", "JmsTemplate": "messaging",
    "JdbcTemplate": "data", "OpenTelemetrySdk": "platform-operations", "RedisTemplate": "data",
    "LettuceConnectionFactory": "data", "SftpClient": "file", "SecurityFilterChain": "security",
    "CircuitBreakerFactory": "integration", "ClientProvider": "platform-operations",
}


def fail(message: str) -> None:
    print(f"[CPF][QA39][FAIL] {message}", file=sys.stderr)
    raise SystemExit(1)


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")
    if not isinstance(value, dict):
        fail(f"JSON object required: {path.relative_to(ROOT)}")
    return value


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def parse_properties(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            fail(f"invalid property line: {path.relative_to(ROOT)}: {raw}")
        key, value = line.split("=", 1)
        result[key.strip()] = value.strip()
    return result


def parse_time(value: str) -> datetime:
    candidate = value.strip().replace("Z", "+00:00")
    parsed = datetime.fromisoformat(candidate)
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return parsed.astimezone(timezone.utc)


def safe_module_path(module_root: Path, relative: str, label: str) -> Path:
    candidate = (module_root / relative).resolve()
    try:
        candidate.relative_to(module_root.resolve())
    except ValueError:
        fail(f"approved exception {label} escapes module: {relative}")
    return candidate


def exception_hash(row: dict[str, str], module_root: Path) -> str:
    digest = hashlib.sha256()
    canonical = "\x1f".join(
        row[field].strip().replace("\r\n", "\n").replace("\r", "\n")
        for field in HASH_FIELDS
    )
    digest.update(canonical.encode("utf-8"))
    digest.update(b"\x00")
    config_files = sorted({item.strip().replace("\\", "/") for item in row["config_files"].split(";") if item.strip()})
    if not config_files:
        fail(f"approved exception config_files is empty: {row['exception_id']}")
    for relative in config_files:
        config = safe_module_path(module_root, relative, "config")
        if not config.is_file():
            fail(f"approved exception config missing: {relative}")
        digest.update(relative.encode("utf-8"))
        digest.update(b"\x00")
        digest.update(config.read_bytes())
        digest.update(b"\x00")
    return digest.hexdigest()


def read_exceptions(path: Path, module_root: Path, manifest: dict[str, Any]) -> list[dict[str, str]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        if reader.fieldnames != EXCEPTION_FIELDS:
            fail(f"approved exception header mismatch: {path.relative_to(ROOT)}")
        rows = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
    now = datetime.now(timezone.utc)
    seen: set[str] = set()
    allowed_modules = {module_root.name, str(manifest.get("domainName", "")), str(manifest.get("projectName", ""))}
    for row in rows:
        blank = [field for field in EXCEPTION_FIELDS if not row[field]]
        if blank:
            fail(f"blank approved exception fields {blank}: {path.relative_to(ROOT)}")
        if row["exception_id"] in seen:
            fail(f"duplicate approved exception id: {row['exception_id']}")
        seen.add(row["exception_id"])
        if row["module"] not in allowed_modules:
            fail(f"approved exception module mismatch: {row['exception_id']}")
        if row["capability"] not in EXPECTED_GROUPS:
            fail(f"approved exception capability mismatch: {row['exception_id']}")
        if not re.fullmatch(r"[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+", row["artifact"]):
            fail(f"approved exception artifact must be exact group:name: {row['exception_id']}")
        if row["status"] != "APPROVED":
            fail(f"approved exception status is not APPROVED: {row['exception_id']}")
        if parse_time(row["approved_at"]) > now or parse_time(row["expires_at"]) <= now:
            fail(f"approved exception is not currently valid: {row['exception_id']}")
        if not {item.strip() for item in row["rule_ids"].split(";") if item.strip()}:
            fail(f"approved exception rule_ids is empty: {row['exception_id']}")
        config_files = {item.strip().replace("\\", "/") for item in row["config_files"].split(";") if item.strip()}
        if not config_files or any(not item.startswith("src/main/resources/") for item in config_files):
            fail(f"approved exception config must be under src/main/resources: {row['exception_id']}")
        if exception_hash(row, module_root) != row["config_hash"].lower():
            fail(f"approved exception hash mismatch: {row['exception_id']}")
        evidence = safe_module_path(module_root, row["evidence_path"], "evidence")
        if not evidence.is_file():
            fail(f"approved exception evidence missing: {row['exception_id']}")
    return rows


def validate_policy_contract(data: dict[str, Any], label: str) -> None:
    inheritance = data.get("standardInheritancePolicy") or {}
    if inheritance.get("policyVersion") != "1.0":
        fail(f"{label} standard inheritance policy version mismatch")
    if inheritance.get("defaultProfile") != "minimal-domain":
        fail(f"{label} default profile must be minimal-domain")
    if inheritance.get("mandatoryStandards") != EXPECTED_STANDARDS:
        fail(f"{label} mandatory standards mismatch")
    if inheritance.get("optionalCapabilities") != EXPECTED_GROUPS:
        fail(f"{label} optional capability list mismatch")
    if inheritance.get("unselectedCapabilityPolicy") != "NO_DEPENDENCY_NO_BEAN_NO_CONFIG_NO_SQL":
        fail(f"{label} unselected capability policy mismatch")
    if inheritance.get("failClosed") is not True:
        fail(f"{label} policy must be fail-closed")
    exception = data.get("approvedExternalExceptionPolicy") or {}
    if exception.get("requiredFields") != EXCEPTION_FIELDS:
        fail(f"{label} approved exception fields mismatch")
    for key in ("versionDriftPolicy", "expiredPolicy", "unregisteredPolicy"):
        if exception.get(key) != "FAIL_CLOSED":
            fail(f"{label} {key} must be FAIL_CLOSED")


def validate_canonical_catalogs() -> tuple[dict[str, Any], dict[str, Any], list[str]]:
    catalog = load_json(CATALOG)
    profiles = load_json(PROFILES)
    if catalog.get("publicProfiles") != EXPECTED_PROFILES:
        fail(f"public profiles mismatch: {catalog.get('publicProfiles')}")
    if [item.get("id") for item in catalog.get("capabilityGroups", [])] != EXPECTED_GROUPS:
        fail("capability groups mismatch in starter catalog")
    if profiles.get("publicProfiles") != EXPECTED_PROFILES:
        fail("public profiles mismatch in capability profile catalog")
    profile_groups = profiles.get("capabilityGroups", [])
    normalized_profile_groups = [item.get("id") if isinstance(item, dict) else item for item in profile_groups]
    if normalized_profile_groups != EXPECTED_GROUPS:
        fail("capability groups mismatch in capability profile catalog")
    validate_policy_contract(catalog, "starter catalog")
    validate_policy_contract(profiles, "profile catalog")

    profile_entries = profiles.get("profiles", [])
    if [entry.get("publicName") for entry in profile_entries] != EXPECTED_PROFILES:
        fail("profile entries are not exactly the six public profiles")
    minimal = next(entry for entry in profile_entries if entry["publicName"] == "minimal-domain")
    if minimal.get("mandatoryCapabilityGroups") or minimal.get("requiredProviderBindings"):
        fail("minimal-domain must not select optional capability/provider")
    if minimal.get("resolvedStarters") != ["cpf-starter-profile-minimal-domain"]:
        fail("minimal-domain must resolve only the minimal public profile")
    event = next(entry for entry in profile_entries if entry["publicName"] == "event-service")
    if event.get("requiredProviderBindings") != ["messaging"]:
        fail("event-service must fail closed without an explicit messaging provider")

    composition = profiles.get("capabilityComposition") or {}
    if composition != (catalog.get("capabilityComposition") or {}):
        fail("capability composition drift between profile and starter catalogs")
    if list(composition) != EXPECTED_GROUPS:
        fail("capability composition must cover exactly the seven public groups")
    expected_defaults = {
        "data": {"data": "mybatis"},
        "messaging": {},
        "integration": {"integration-transport": "http"},
        "file": {"file": "sftp"},
        "notification": {"notification": "email"},
        "security": {},
        "platform-operations": {},
    }
    expected_required_slots = {
        "data": ["data"], "messaging": ["messaging"],
        "integration": ["integration-transport"], "file": ["file"],
        "notification": ["notification"], "security": ["security-mode"], "platform-operations": [],
    }
    for group in EXPECTED_GROUPS:
        item = composition[group]
        if item.get("defaultProviderBindings") != expected_defaults[group]:
            fail(f"capability default Provider drift: {group}")
        if item.get("requiredProviderSlots") != expected_required_slots[group]:
            fail(f"capability required Provider slot drift: {group}")
        if not isinstance(item.get("runtimeProjects"), list) or not isinstance(item.get("runtimeCoordinates"), list):
            fail(f"capability runtime composition is invalid: {group}")
        if len(item["runtimeProjects"]) != len(item["runtimeCoordinates"]):
            fail(f"capability runtime project/coordinate count drift: {group}")

    admission = catalog.get("starterAdmissionPolicy") or {}
    expected_admission = {
        "policyVersion": "1.0",
        "failClosed": True,
        "valueCatalog": "cpf-docs/work/CPF_STARTER_VALUE_CATALOG.csv",
        "requiredFields": [
            "current_value", "public_api_spi", "actual_consumer", "qa39_action",
            "target_public_surface", "evidence",
        ],
        "requiredBeforeRegistration": [
            "value-contract", "convenience-api", "extension-spi-or-explicit-not-applicable",
            "actual-consumer", "removal-alternative-comparison", "footprint-classification",
            "runtime-evidence-plan",
        ],
    }
    if admission != expected_admission:
        fail("Starter admission policy mismatch")
    release_catalog = load_json(ARTIFACT_CATALOG)
    if release_catalog.get("starterAdmissionPolicy") != admission:
        fail("Starter admission policy drift between canonical and release catalogs")

    value_catalog_path = ROOT / admission["valueCatalog"]
    if not value_catalog_path.is_file():
        fail("Starter Value Catalog is missing")
    with value_catalog_path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        value_rows = [
            {key: (value or "").strip() for key, value in row.items()}
            for row in reader
        ]
    value_by_id = {row.get("starter_id", ""): row for row in value_rows}
    if len(value_by_id) != len(value_rows):
        fail("duplicate starter_id in Starter Value Catalog")

    modules = catalog.get("modules", [])
    for module in modules:
        artifact = module["artifactId"]
        row = value_by_id.get(artifact)
        if row is None:
            fail(f"retained artifact has no Starter Value Contract row: {artifact}")
        blank = [field for field in admission["requiredFields"] if not row.get(field)]
        if blank:
            fail(f"Starter Value Contract fields missing {artifact}: {blank}")
        if row.get("registered") != "Y":
            fail(f"retained artifact is not registered in Starter Value Catalog: {artifact}")
        if row.get("development_status") == "미구현":
            fail(f"retained artifact remains source-unimplemented: {artifact}")
        if row.get("qa39_action", "").startswith("REMOVE"):
            fail(f"retained artifact has removal action in Starter Value Catalog: {artifact}")
        if row.get("actual_consumer") in {"없음", "미확인"}:
            fail(f"retained artifact has no actual Consumer: {artifact}")
        if row.get("public_api_spi") in {"없음", "미확인"}:
            fail(f"retained artifact has no Public API/SPI admission evidence: {artifact}")

    project_paths = [module["projectPath"] for module in modules]
    owner_paths = [module["ownerPath"] for module in modules]
    artifact_ids = [module["artifactId"] for module in modules]
    module_project_set = set(project_paths)
    for group, item in composition.items():
        unknown_runtime_projects = sorted(set(item.get("runtimeProjects", [])) - module_project_set)
        if unknown_runtime_projects:
            fail(f"capability runtime references unknown projects: {group} -> {unknown_runtime_projects}")
        for slot, provider in (item.get("defaultProviderBindings") or {}).items():
            if provider not in ((profiles.get("providerSlots") or {}).get(slot) or {}):
                fail(f"capability default Provider is absent from providerSlots: {group}/{slot}={provider}")
    for label, values in (("projectPath", project_paths), ("ownerPath", owner_paths), ("artifactId", artifact_ids)):
        duplicates = sorted({value for value in values if values.count(value) > 1})
        if duplicates:
            fail(f"duplicate {label}: {duplicates}")
    removed_ids = set(catalog.get("removedArtifactIds", []))
    leaked = sorted(removed_ids.intersection(artifact_ids))
    if leaked:
        fail(f"removed artifacts remain in canonical catalog: {leaked}")
    full_repository = (ROOT / "cpf-core/build.gradle").is_file() and (ROOT / "cpf-common/build.gradle").is_file()
    missing_module_builds = []
    for module in modules:
        build = ROOT / module["ownerPath"] / "build.gradle"
        if not build.is_file():
            missing_module_builds.append(module["ownerPath"])
    if full_repository and missing_module_builds:
        fail(f"catalog modules have no build.gradle: {missing_module_builds}")
    if not full_repository and missing_module_builds:
        print(f"[CPF][QA39][OVERLAY] deferred module existence checks={len(missing_module_builds)}")

    settings = (ROOT / "settings.gradle").read_text(encoding="utf-8")
    if "cpf-starter-catalog.json" not in settings or "JsonSlurper" not in settings:
        fail("settings.gradle does not consume canonical Starter Catalog")
    bom = (ROOT / "cpf-tools/build/platform-bom/build.gradle").read_text(encoding="utf-8")
    if "cpf-starter-catalog.json" not in bom or "${project.version}" in bom:
        fail("BOM is not canonical-catalog driven or contains literal project.version")
    artifact_catalog = load_json(ARTIFACT_CATALOG)
    if artifact_catalog.get("canonicalStarterCatalog") != "cpf-tools/generator/contracts/cpf-starter-catalog.json":
        fail("release artifact catalog does not point to the canonical Starter Catalog")
    if artifact_catalog.get("publicSelectionSurface") != {
            "profiles": EXPECTED_PROFILES, "capabilityGroups": EXPECTED_GROUPS}:
        fail("release artifact catalog public selection surface drift")
    canonical_artifacts = {
        module["artifactId"]: module
        for module in modules if module.get("publicationRequired", True)
    }
    release_artifacts = {
        item.get("artifactId"): item
        for item in artifact_catalog.get("artifacts", []) if item.get("artifactId")
    }
    missing = sorted(set(canonical_artifacts) - set(release_artifacts))
    if missing:
        fail(f"release catalog omits canonical Starter artifacts: {missing}")
    drift = []
    for artifact_id, module in canonical_artifacts.items():
        release = release_artifacts[artifact_id]
        if release.get("ownerPath") != module.get("ownerPath"):
            drift.append((artifact_id, "ownerPath", module.get("ownerPath"), release.get("ownerPath")))
        for field in ("visibility", "role"):
            if release.get(field) is not None and release.get(field) != module.get(field):
                drift.append((artifact_id, field, module.get(field), release.get(field)))
    if drift:
        fail(f"release/canonical Starter metadata drift: {drift}")
    unexpected_starters = sorted(
        artifact_id for artifact_id in release_artifacts
        if artifact_id.startswith("cpf-starter-")
        and artifact_id not in canonical_artifacts
        and artifact_id not in removed_ids
    )
    if unexpected_starters:
        fail(f"release catalog exposes non-canonical Starter artifacts: {unexpected_starters}")
    if set(artifact_catalog.get("removedArtifactIds", [])) != removed_ids:
        fail("release/canonical removed artifact IDs drift")
    if artifact_catalog.get("removedRepositoryRoots") != catalog.get("removedRepositoryRoots"):
        fail("release/canonical removed repository roots drift")

    root_build = (ROOT / "build.gradle").read_text(encoding="utf-8")
    if "checkQa39CanonicalStarterClosure" not in root_build or "qualityGate" not in root_build:
        fail("root qualityGate is not connected to QA39 canonical closure")
    return catalog, profiles, sorted(removed_ids)



def resolve_capabilities(
        profiles: dict[str, Any], profile_name: str,
        requested: set[str], explicit: dict[str, str]) -> tuple[set[str], dict[str, str], set[str]]:
    profile = next(item for item in profiles["profiles"] if item["publicName"] == profile_name)
    groups = set(profile.get("mandatoryCapabilityGroups", [])) | set(requested)
    allowed = set(profile.get("allowedCapabilityGroups", []))
    if not groups <= allowed:
        raise ValueError(f"disallowed groups: {groups - allowed}")
    composition = profiles["capabilityComposition"]
    changed = True
    while changed:
        changed = False
        for group in tuple(groups):
            for required in composition[group].get("requiresCapabilities", []):
                if required not in groups:
                    groups.add(required)
                    changed = True
    bindings = dict(explicit)
    for slot, provider in (profile.get("defaultProviderBindings") or {}).items():
        required_group = {
            "data": "data", "cache": "data", "messaging": "messaging",
            "integration-transport": "integration", "integration-codec": "integration",
            "file": "file", "notification": "notification",
            "observability": "platform-operations", "security-mode": "security",
        }.get(slot)
        if required_group in groups:
            bindings.setdefault(slot, provider)
    runtimes: set[str] = set()
    for group in sorted(groups):
        item = composition[group]
        runtimes.update(item.get("runtimeCoordinates", []))
        for slot, provider in item.get("defaultProviderBindings", {}).items():
            bindings.setdefault(slot, provider)
        missing = set(item.get("requiredProviderSlots", [])) - set(bindings)
        if missing:
            raise ValueError(f"missing providers: {group} -> {sorted(missing)}")
    slot_groups = {
        "data": "data", "cache": "data", "messaging": "messaging",
        "integration-transport": "integration", "integration-codec": "integration",
        "file": "file", "notification": "notification", "observability": "platform-operations",
        "security-mode": "security",
    }
    for slot, provider in bindings.items():
        if slot_groups.get(slot) not in groups:
            raise ValueError(f"provider without active capability: {slot}={provider}")
        allowed_values = set(profile.get("allowedProviderBindings", {}).get(slot, []))
        if provider not in allowed_values:
            raise ValueError(f"provider not allowed by profile: {slot}={provider}")
    return groups, bindings, runtimes


def validate_resolution_semantics(profiles: dict[str, Any]) -> None:
    groups, bindings, runtimes = resolve_capabilities(profiles, "minimal-domain", set(), {})
    if groups or bindings or runtimes:
        fail("minimal-domain pulls optional capability/provider/runtime")

    groups, bindings, runtimes = resolve_capabilities(profiles, "minimal-domain", {"data"}, {})
    if groups != {"data"} or bindings != {"data": "mybatis"} or runtimes:
        fail("data capability default resolution drift")

    try:
        resolve_capabilities(profiles, "event-service", set(), {})
    except ValueError as exc:
        if "messaging" not in str(exc):
            fail(f"event-service failed for wrong reason: {exc}")
    else:
        fail("event-service must reject a missing messaging provider")

    groups, bindings, runtimes = resolve_capabilities(
            profiles, "event-service", set(), {"messaging": "kafka"})
    if groups != {"data", "messaging"} or bindings.get("data") != "mybatis" or bindings.get("messaging") != "kafka":
        fail("event-service Provider resolution drift")
    if "com.cpf.starter:cpf-starter-messaging-reliability-jdbc" not in runtimes:
        fail("messaging capability omits reliability runtime")

    groups, bindings, runtimes = resolve_capabilities(profiles, "minimal-domain", {"integration"}, {})
    if bindings != {"integration-transport": "http"} or groups != {"integration"}:
        fail("integration capability default resolution drift")
    if any("messaging" in value or "notification" in value for value in runtimes):
        fail("integration capability pulled unrelated runtime")

    groups, bindings, runtimes = resolve_capabilities(profiles, "minimal-domain", {"file"}, {})
    if groups != {"file"} or bindings != {"file": "sftp"}:
        fail("file capability default resolution drift")
    forbidden_file_leaks = {"cpf-starter-file-attachment", "cpf-starter-file-archive", "cpf-starter-file-tabular-poi"}
    if any(any(leak in coordinate for leak in forbidden_file_leaks) for coordinate in runtimes):
        fail("file transfer selection pulled unselected attachment/archive/tabular runtime")

    groups, bindings, runtimes = resolve_capabilities(profiles, "minimal-domain", {"notification"}, {})
    if groups != {"data", "notification"}:
        fail("notification capability prerequisite drift")
    if bindings.get("data") != "mybatis" or bindings.get("notification") != "email":
        fail("notification capability Provider default drift")
    if "com.cpf.starter:cpf-starter-notification-dispatch" not in runtimes:
        fail("notification capability runtime is missing")

    try:
        resolve_capabilities(profiles, "minimal-domain", {"security"}, {})
    except ValueError as exc:
        if "security-mode" not in str(exc):
            fail(f"security capability failed for wrong reason: {exc}")
    else:
        fail("security capability must require an explicit mode outside a security use-case profile")

    groups, bindings, runtimes = resolve_capabilities(profiles, "secure-api", set(), {})
    if groups != {"security"} or bindings.get("security-mode") != "resource-server":
        fail("secure-api security mode resolution drift")
    if "com.cpf.starter:cpf-starter-security-secret" not in runtimes:
        fail("secure-api security capability omits secret runtime")

    groups, bindings, runtimes = resolve_capabilities(profiles, "browser-bff", set(), {})
    if groups != {"data", "security"} or bindings.get("security-mode") != "browser-session":
        fail("browser-bff security/data mode resolution drift")

    groups, bindings, runtimes = resolve_capabilities(
            profiles, "minimal-domain", {"data"}, {"cache": "valkey"})
    if groups != {"data"} or bindings != {"data": "mybatis", "cache": "valkey"}:
        fail("optional cache Provider must remain inside the selected data capability")

    groups, bindings, runtimes = resolve_capabilities(
            profiles, "minimal-domain", {"integration"},
            {"integration-codec": "iso8583"})
    if groups != {"integration"} or bindings.get("integration-transport") != "http"             or bindings.get("integration-codec") != "iso8583":
        fail("optional integration codec resolution drift")

    try:
        resolve_capabilities(profiles, "minimal-domain", set(), {"cache": "valkey"})
    except ValueError as exc:
        if "provider without active capability" not in str(exc):
            fail(f"orphan Provider failed for wrong reason: {exc}")
    else:
        fail("Provider selection without its Capability must fail closed")

def validate_generator_and_enforcement() -> None:
    generator = (ROOT / "cpf-tools/generator/create-domain.ps1").read_text(encoding="utf-8")
    required_tokens = [
        'CapabilityProfile = "minimal-domain"', "ApprovedExceptionRegistry", "cpf-approved-exceptions.csv",
        "generated-domain-policy.properties", "exceptionRegistrySha256", "approvedExceptions",
        "requiredStandardsCsv", "UpgradeSourceDomainPath", "capabilityComposition",
        "approvedExceptionDependencies", "TargetEnvironment", "cpfTargetEnvironment",
        "legacyCapabilityGroups", "ProvisionDatabase는 Data Capability", "cpfPlatformVersion",
        "security-mode", "embeddedRuntimeProjects",
        "JavaLanguageVersion.of(25)", "packagedArtifactIds", "verifyCpfPackagedDependencies",
    ]
    missing = [token for token in required_tokens if token not in generator]
    if missing:
        fail(f"Generator standard inheritance/exception tokens missing: {missing}")
    if '$batchDependency = "" else' in generator:
        fail("Generator contains invalid PowerShell else expression")
    if generator.count('@"') != generator.count('"@'):
        fail("Generator PowerShell double-quoted here-string is unbalanced")
    if generator.count("@'") != generator.count("'@"):
        fail("Generator PowerShell single-quoted here-string is unbalanced")
    if "$requestedCapabilityGroups + $legacyCapabilityGroups" not in generator:
        fail("Generator legacy flags do not converge on canonical Capability resolution")
    upgrade = ROOT / "cpf-tools/generator/upgrade-domain.ps1"
    if not upgrade.is_file() or "preservedUserFiles" not in upgrade.read_text(encoding="utf-8"):
        fail("Generated Domain upgrade preservation script is missing/incomplete")
    plugin = ROOT / "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java"
    support = ROOT / "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfGeneratedDomainPolicySupport.java"
    runtime = ROOT / "cpf-starters/base/src/main/java/com/cpf/starter/base/CpfGeneratedDomainPolicyRuntimeVerifier.java"
    for path in (plugin, support, runtime):
        if not path.is_file():
            fail(f"standard inheritance enforcement source missing: {path.relative_to(ROOT)}")
    required_data_contracts = [
        ROOT / "cpf-core/src/main/java/com/cpf/core/api/database/CpfDataOperations.java",
        ROOT / "cpf-core/src/main/java/com/cpf/core/api/database/CpfJdbcOperations.java",
        ROOT / "cpf-starters/persistence-jdbc/src/main/java/com/cpf/starter/persistence/jdbc/CpfSpringJdbcOperations.java",
        ROOT / "cpf-starters/persistence-mybatis/src/main/java/com/cpf/starter/persistence/mybatis/CpfMyBatisDataOperations.java",
    ]
    for path in required_data_contracts:
        if not path.is_file():
            fail(f"Generated Domain data contract/adapter missing: {path.relative_to(ROOT)}")
    duplicate_data_adapters = [
        ROOT / "cpf-starters/persistence-jdbc/src/main/java/com/cpf/starter/persistence/jdbc/DefaultCpfJdbcOperations.java",
        ROOT / "cpf-starters/persistence-mybatis/src/main/java/com/cpf/starter/persistence/mybatis/DefaultCpfMyBatisOperations.java",
    ]
    if any(path.exists() for path in duplicate_data_adapters):
        fail("duplicate Generated Domain data adapters remain")
    mybatis_imports = ROOT / "cpf-starters/persistence-mybatis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
    if "CpfDomainMyBatisAutoConfiguration" not in mybatis_imports.read_text(encoding="utf-8"):
        fail("Generated Domain MyBatis AutoConfiguration is not registered")
    plugin_text = plugin.read_text(encoding="utf-8")
    task_text = (ROOT / "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfGeneratedDomainPolicyTask.java").read_text(encoding="utf-8")
    for token in ("verifyCpfGeneratedDomainPolicy", "inheritGeneratedDomainFoundation"):
        if token not in plugin_text:
            fail(f"Convention Plugin enforcement token missing: {token}")
    enforcement_text = task_text + "\n" + support.read_text(encoding="utf-8")
    for token in ("DIRECT_KAFKA", "CONFIG_KAFKA_BYPASS", "INTERNAL_PROVIDER_IMPORT",
                  "cpfTargetEnvironment", "cpf-approved-exceptions.csv", "exceptionRegistrySha256"):
        if token not in enforcement_text:
            fail(f"Generated Domain build gate token missing: {token}")
    base_auto = (ROOT / "cpf-starters/base/src/main/java/com/cpf/starter/base/CpfBaseAutoConfiguration.java").read_text(encoding="utf-8")
    if "generatedDomainPolicyRuntimeVerifier.verify()" not in base_auto:
        fail("cpf-starter-foundation-base startup does not invoke Generated Domain Runtime Gate")



def validate_dependency_graph_and_ownership(catalog: dict[str, Any]) -> None:
    """Detect project cycles and enforce runtime/client and batch ownership boundaries."""
    owner_to_project = {
        str(module["ownerPath"]).replace("\\", "/").rstrip("/"): str(module["projectPath"])
        for module in catalog.get("modules", [])
    }
    graph: dict[str, set[str]] = {}
    excluded_parts = {"build", ".gradle", "node_modules", "dist", "coverage"}
    for build in ROOT.rglob("build.gradle"):
        rel = build.relative_to(ROOT)
        if excluded_parts.intersection(rel.parts):
            continue
        rel_parent = rel.parent.as_posix()
        if rel_parent == "cpf-tools/build/gradle-plugin":
            # Independent plugin build with its own settings.gradle.
            continue
        project_path = owner_to_project.get(rel_parent)
        if project_path is None:
            project_path = ":" if rel_parent == "." else ":" + rel_parent.replace("/", ":")
        source = build.read_text(encoding="utf-8")
        graph[project_path] = set(re.findall(
            r"""project\s*\(\s*['"](:[^'"]+)['"]\s*\)""", source
        ))

    # Detect cycles only across projects visible in this workspace. The same gate runs
    # after Overlay application against the full Repository and therefore becomes exhaustive.
    visible = set(graph)
    visiting: set[str] = set()
    visited: set[str] = set()
    stack: list[str] = []

    def visit(node: str) -> None:
        if node in visited:
            return
        if node in visiting:
            start = stack.index(node)
            cycle = stack[start:] + [node]
            fail("project dependency cycle: " + " -> ".join(cycle))
        visiting.add(node)
        stack.append(node)
        for target in sorted(graph.get(node, ())):
            if target in visible:
                visit(target)
        stack.pop()
        visiting.remove(node)
        visited.add(node)

    for project in sorted(graph):
        visit(project)

    runtime_project = ":cpf-starter-platform-operations-runtime-control-client"
    prohibited_runtime_targets = {
        ":cpf-starter-integration-http-client",
        ":cpf-starter-file-sftp",
        ":cpf-starter-messaging-reliability-jdbc",
        ":cpf-starter-platform-operations-observability",
        ":cpf-starter-data-persistence-jdbc",
        ":cpf-starter-integration-webhook",
        ":cpf-starter-file-attachment",
    }
    leaked_targets = sorted(graph.get(runtime_project, set()) & prohibited_runtime_targets)
    if leaked_targets:
        fail(f"runtime-control-client depends on owner implementation Starters: {leaked_targets}")

    runtime_source_root = ROOT / "cpf-starters/runtime-control-client/src/main/java"
    prohibited_import = re.compile(
        r"(?m)^\s*import\s+(?:com\.cpf\.starter\.(?:http|sftp|messaging|observability|persistence|webhook|attachment)"
        r"|com\.cpf\.core\.common\.(?:http|broker|filetransfer|logging|remotelog|database))\."
    )
    if runtime_source_root.is_dir():
        violations: list[str] = []
        for source in runtime_source_root.rglob("*.java"):
            content = source.read_text(encoding="utf-8")
            if prohibited_import.search(content):
                violations.append(source.relative_to(ROOT).as_posix())
        if violations:
            fail("runtime-control-client imports owner implementation packages: " + ", ".join(violations))

    legacy_owner_files = [
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfApiClientRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfAttachmentPolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfBrokerConsumerRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfBrokerRetryDlqRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfChannelPolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfConnectionPoolRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfDbReadRoutingRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfDownloadPolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfDynamicLogLevelRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfExternalInstitutionRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfFilePolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfFixedLayoutRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfFixedLengthLayoutPayloadDecoder.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfMaskingPolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfPasswordPolicyRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfSchemaRegistryRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfSecurityMaterialRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfServiceRegistryRuntimeVerifierApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfSftpTransferRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfTraceSamplingRuntimeApplier.java",
        "cpf-starters/runtime-control-client/src/main/java/com/cpf/core/common/runtimecontrol/applier/CpfWebhookCallbackRuntimeApplier.java",
    ]
    remaining_legacy = [path for path in legacy_owner_files if (ROOT / path).is_file()]
    if remaining_legacy:
        fail("runtime-control-client legacy owner files remain: " + ", ".join(remaining_legacy))

    # Product consumers choose exactly one public use-case Profile and add only selected internal providers.
    consumer_builds = [
        "cpf-admin/build.gradle", "cpf-biz-admin/build.gradle", "cpf-gateway/build.gradle",
        "cpf-reference/build.gradle", "cpf-member/build.gradle",
        "cpf-batch/control-server/build.gradle", "cpf-batch/scheduler/build.gradle",
        "cpf-batch/worker/build.gradle",
    ]
    for relative in consumer_builds:
        build = ROOT / relative
        if not build.is_file():
            continue
        content = build.read_text(encoding="utf-8")
        profiles_used = re.findall(r"""project\(\s*['"]:cpf-starter-profile-([^'"]+)['"]\s*\)""", content)
        if len(profiles_used) != 1:
            fail(f"consumer must select exactly one public Profile: {relative} -> {profiles_used}")
        if any(name not in EXPECTED_PROFILES for name in profiles_used):
            fail(f"consumer selected a removed/non-public Profile: {relative} -> {profiles_used}")
        if ":cpf-starter-messaging-" in content and ":cpf-starter-messaging-reliability-jdbc" not in content:
            fail(f"messaging Provider consumer omits CPF reliability runtime: {relative}")
    generated_member = ROOT / "cpf-member/build.gradle"
    if generated_member.is_file():
        member_build = generated_member.read_text(encoding="utf-8")
        for forbidden in ("project(':cpf-core')", "project(':cpf-common')"):
            if forbidden in member_build:
                fail(f"Generated Domain manually redeclares inherited foundation dependency: {forbidden}")

    # Consumer rule: exactly one public use-case Profile and no direct declaration of a
    # leaf already embedded by that Profile. Optional Capability Providers remain explicit.
    profile_catalog = load_json(PROFILES)
    profile_by_project = {
        ":" + str(item["aggregateProject"]).lstrip(":"): item
        for item in profile_catalog.get("profiles", [])
    }
    consumers = (
        "cpf-admin/build.gradle",
        "cpf-biz-admin/build.gradle",
        "cpf-gateway/build.gradle",
        "cpf-reference/build.gradle",
        "cpf-member/build.gradle",
    )
    for relative in consumers:
        build = ROOT / relative
        if not build.is_file():
            continue
        declared = set(re.findall(
            r"""project\s*\(\s*['"](:[^'"]+)['"]\s*\)""",
            build.read_text(encoding="utf-8"),
        ))
        selected_profiles = sorted(declared & set(profile_by_project))
        if len(selected_profiles) != 1:
            fail(f"Consumer must select exactly one public Profile {relative}: {selected_profiles}")
        profile = profile_by_project[selected_profiles[0]]
        embedded = set(profile.get("embeddedRuntimeProjects", []))
        duplicate_leaves = sorted(declared & embedded)
        if duplicate_leaves:
            fail(f"Consumer directly repeats Profile-embedded leaf {relative}: {duplicate_leaves}")

    # Contract/Testkit must remain libraries and never receive executable/runtime Profiles.
    for relative in ("cpf-batch/contract/build.gradle", "cpf-batch/testkit/build.gradle"):
        build = ROOT / relative
        if not build.is_file():
            continue
        content = build.read_text(encoding="utf-8")
        forbidden = []
        if "org.springframework.boot" in content:
            forbidden.append("Spring Boot plugin")
        if "cpf-starter-profile-" in content:
            forbidden.append("Runtime Profile")
        if relative.endswith("contract/build.gradle") and "dependencies {" in content:
            forbidden.append("Contract dependencies")
        if forbidden:
            fail(f"Batch library pollution {relative}: {forbidden}")

def validate_delete_closure(catalog: dict[str, Any], removed_ids: list[str]) -> list[str]:
    if not DELETE_PATHS.is_file():
        fail("canonical exact-path delete manifest is missing")
    if not DELETE_WORK_ITEMS.is_file():
        fail("canonical product delete work items are missing")
    paths = [line.strip().replace("\\", "/") for line in DELETE_PATHS.read_text(encoding="utf-8").splitlines()
             if line.strip() and not line.lstrip().startswith("#")]
    if len(paths) != len(set(paths)):
        fail("duplicate path in canonical delete manifest")
    if any(path.startswith(PROTECTED_PREFIXES) for path in paths):
        fail("protected owner path leaked into delete manifest")

    required_columns = [
        "delete_id", "exact_repository_path", "target_type", "deletion_reason",
        "related_requirement", "owner", "current_consumer", "replacement_or_migration",
        "core_api_impact", "build_impact", "bom_catalog_impact", "generator_impact",
        "config_sql_impact", "test_impact", "documentation_impact", "evidence_impact",
        "residual_reference_check", "empty_directory_check", "approval_status", "execution_status",
    ]
    with DELETE_WORK_ITEMS.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        if reader.fieldnames != required_columns:
            fail("product delete work item schema mismatch")
        work_items = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
    if not work_items or any(any(not row[column] for column in required_columns) for row in work_items):
        fail("product delete work item contains blank required values")
    ids = [row["delete_id"] for row in work_items]
    if len(ids) != len(set(ids)):
        fail("duplicate product delete work item id")
    product_files = {
        row["exact_repository_path"].replace("\\", "/")
        for row in work_items if row["target_type"] == "FILE"
    }
    product_roots = sorted({
        row["exact_repository_path"].replace("\\", "/")
        for row in work_items if row["target_type"] == "EMPTY_DIRECTORY"
    })
    invalid_types = sorted({row["target_type"] for row in work_items} - {"FILE", "EMPTY_DIRECTORY"})
    if invalid_types:
        fail(f"invalid product delete target type: {invalid_types}")
    if not product_files <= set(paths):
        fail(f"product files missing from exact delete manifest: {sorted(product_files - set(paths))}")
    if product_roots != sorted(catalog.get("removedRepositoryRoots", [])):
        fail("product delete roots drift from canonical Starter Catalog")
    if any(row["execution_status"] != "NOT_EXECUTED" for row in work_items):
        fail("Overlay must not claim or execute product deletion")
    if any(path.startswith(PROTECTED_PREFIXES) for path in product_files | set(product_roots)):
        fail("protected owner path leaked into product delete work items")

    for path in paths:
        if "*" in path or "?" in path or Path(path).is_absolute() or ".." in Path(path).parts:
            fail(f"unsafe delete path: {path}")
        if (ROOT / path).exists():
            fail(f"approved delete path still exists; run exact-path cleanup first: {path}")

    expected_work_fields = [
        "delete_id", "exact_repository_path", "target_type", "deletion_reason",
        "related_requirement", "owner", "current_consumer", "replacement_or_migration",
        "core_api_impact", "build_impact", "bom_catalog_impact", "generator_impact",
        "config_sql_impact", "test_impact", "documentation_impact", "evidence_impact",
        "residual_reference_check", "empty_directory_check", "approval_status",
        "execution_status",
    ]
    if not DELETE_WORK_ITEMS.is_file():
        fail("canonical product delete work items are missing")
    with DELETE_WORK_ITEMS.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        if reader.fieldnames != expected_work_fields:
            fail(f"delete work item schema mismatch: {reader.fieldnames}")
        work_items = [
            {key: (value or "").strip() for key, value in row.items()}
            for row in reader
        ]
    ids = [row["delete_id"] for row in work_items]
    if len(ids) != len(set(ids)):
        fail("duplicate delete_id in product delete work items")
    file_items = [row for row in work_items if row["target_type"] == "FILE"]
    directory_items = [row for row in work_items if row["target_type"] == "EMPTY_DIRECTORY"]
    unsupported_types = sorted({
        row["target_type"] for row in work_items
        if row["target_type"] not in {"FILE", "EMPTY_DIRECTORY"}
    })
    if unsupported_types:
        fail(f"unsupported delete target types: {unsupported_types}")
    blank_rows = [
        row["delete_id"] for row in work_items
        if any(not row[field] for field in expected_work_fields)
    ]
    if blank_rows:
        fail(f"delete work items contain blank mandatory fields: {blank_rows}")
    missing_manifest_paths = sorted({
        row["exact_repository_path"].replace("\\", "/") for row in file_items
        if row["exact_repository_path"].replace("\\", "/") not in paths
    })
    if missing_manifest_paths:
        fail(f"product FILE work items missing from exact delete manifest: {missing_manifest_paths}")
    directory_roots = [row["exact_repository_path"].replace("\\", "/").rstrip("/")
                       for row in directory_items]
    for root in directory_roots:
        if not any(path.startswith(root + "/") for path in paths):
            fail(f"empty-directory cleanup has no exact file children: {root}")
    if not DELETE_ONE_LINE.is_file():
        fail("canonical exact-path PowerShell cleanup command is missing")
    cleanup = DELETE_ONE_LINE.read_text(encoding="utf-8")
    required_cleanup_tokens = [
        "-LiteralPath", "PathType Leaf", "target_type -eq 'EMPTY_DIRECTORY'",
        "-Directory -Recurse", "-contains '..'", "Unexpected files remain; deletion stopped",
    ]
    missing_cleanup_tokens = [token for token in required_cleanup_tokens if token not in cleanup]
    if missing_cleanup_tokens:
        fail(f"exact-path cleanup command is incomplete: {missing_cleanup_tokens}")
    forbidden_cleanup_tokens = ["git clean", "git reset", "git restore", "Remove-Item *"]
    leaked_cleanup_tokens = [token for token in forbidden_cleanup_tokens if token in cleanup]
    if leaked_cleanup_tokens:
        fail(f"unsafe cleanup command tokens: {leaked_cleanup_tokens}")

    search_roots = [ROOT / name for name in (
        "settings.gradle", "build.gradle", "cpf-admin", "cpf-biz-admin", "cpf-gateway",
        "cpf-reference", "cpf-member", "cpf-batch", "cpf-starters",
    )]
    violations: list[str] = []
    # Scan only exact removed artifact identifiers. Path basenames such as build.gradle,
    # validation, or persistence-mybatis are not safe search tokens and caused false positives.
    # Exact repository paths are already enforced above by existence checks.
    artifact_patterns = [
        (artifact, re.compile(r"(?<![A-Za-z0-9_.-])" + re.escape(artifact) + r"(?![A-Za-z0-9_.-])"))
        for artifact in removed_ids
    ]
    excluded_parts = {"build", ".gradle", "node_modules", "dist", "coverage", "test-results", "playwright-report"}
    for root in search_roots:
        files = [root] if root.is_file() else list(root.rglob("*")) if root.exists() else []
        for file in files:
            if not file.is_file() or excluded_parts.intersection(file.parts):
                continue
            rel = file.relative_to(ROOT).as_posix()
            try:
                text = file.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            for token, pattern in artifact_patterns:
                if pattern.search(text):
                    violations.append(f"{rel}: {token}")
                    if len(violations) >= 100:
                        break
    if violations:
        fail("removed artifact/path references remain outside canonical decision manifests:\n" + "\n".join(violations))
    return paths



def validate_changed_sql_vendor_parity() -> None:
    """Validate Oracle/PostgreSQL/MariaDB semantic parity for QA39 SQL changes."""
    official = ("oracle", "postgresql", "mariadb")
    module_roots = (
        ROOT / "cpf-starters/messaging-reliability-jdbc/src/main/resources/db",
        ROOT / "cpf-starters/notification/src/main/resources/db",
        ROOT / "cpf-starters/integration-sftp/src/main/resources/db",
    )

    def semantic_operations(sql: str) -> set[tuple[str, str, str]]:
        normalized = re.sub(r"--.*?$", "", sql, flags=re.MULTILINE)
        normalized = re.sub(r"\s+", " ", normalized).strip()
        operations: set[tuple[str, str, str]] = set()
        for match in re.finditer(
                r"CREATE\s+TABLE\s+([A-Za-z0-9_]+)\s*\((.*?)\)\s*;",
                normalized, flags=re.IGNORECASE):
            table = match.group(1).lower()
            body = match.group(2)
            columns = []
            for item in body.split(","):
                item = item.strip()
                if not item or re.match(r"(?:CONSTRAINT|PRIMARY|UNIQUE|FOREIGN|CHECK)\b", item, re.I):
                    continue
                name_match = re.match(r"([A-Za-z0-9_]+)\s+", item)
                if name_match:
                    columns.append(name_match.group(1).lower())
            operations.add(("CREATE_TABLE", table, ",".join(columns)))
        for match in re.finditer(
                r"CREATE\s+(?:UNIQUE\s+)?INDEX\s+([A-Za-z0-9_]+)\s+ON\s+"
                r"([A-Za-z0-9_]+)\s*\(([^)]*)\)\s*;",
                normalized, flags=re.IGNORECASE):
            index = match.group(1).lower()
            table = match.group(2).lower()
            columns = ",".join(part.strip().lower() for part in match.group(3).split(","))
            operations.add(("CREATE_INDEX", index, f"{table}:{columns}"))
        for match in re.finditer(
                r"DROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?([A-Za-z0-9_]+)",
                normalized, flags=re.IGNORECASE):
            operations.add(("DROP_TABLE", match.group(1).lower(), ""))
        for match in re.finditer(
                r"DROP\s+INDEX\s+(?:IF\s+EXISTS\s+)?([A-Za-z0-9_]+)",
                normalized, flags=re.IGNORECASE):
            operations.add(("DROP_INDEX", match.group(1).lower(), ""))
        return operations

    for db_root in module_roots:
        if not db_root.is_dir():
            continue
        unexpected = sorted(
            path.name for path in db_root.iterdir()
            if path.is_dir() and path.name.lower() not in official
        )
        if unexpected:
            fail(f"unsupported DB Vendor directories in {db_root.relative_to(ROOT)}: {unexpected}")
        for kind in ("migration", "rollback"):
            file_sets: dict[str, set[str]] = {}
            semantics: dict[str, dict[str, set[tuple[str, str, str]]]] = {}
            for vendor in official:
                folder = db_root / vendor / kind
                if not folder.is_dir():
                    fail(f"missing official DB directory: {folder.relative_to(ROOT)}")
                files = {path.name for path in folder.glob("*2__*.sql")}
                file_sets[vendor] = files
                semantics[vendor] = {
                    path.name: semantic_operations(path.read_text(encoding="utf-8"))
                    for path in folder.glob("*2__*.sql")
                }
            baseline_files = file_sets[official[0]]
            if any(file_sets[vendor] != baseline_files for vendor in official[1:]):
                fail(f"QA39 SQL file parity drift {db_root.relative_to(ROOT)}/{kind}: {file_sets}")
            for name in sorted(baseline_files):
                baseline = semantics[official[0]][name]
                for vendor in official[1:]:
                    if semantics[vendor][name] != baseline:
                        fail(
                            f"QA39 SQL semantic parity drift {db_root.relative_to(ROOT)}/{kind}/{name}: "
                            f"{official[0]}={baseline}, {vendor}={semantics[vendor][name]}"
                        )

def validate_generated_domains() -> int:
    manifests = [path for path in ROOT.rglob("manifest/domain-manifest.json")
                 if not any(part in {"build", ".gradle", "node_modules", "dist", "coverage"} for part in path.parts)]
    validated = 0
    for manifest_path in manifests:
        module_root = manifest_path.parent.parent
        manifest = load_json(manifest_path)
        if manifest.get("domainType") != "GENERATED_DOMAIN":
            continue
        validated += 1
        policy_path = module_root / "src/main/resources/META-INF/cpf/generated-domain-policy.properties"
        config_path = module_root / "config/cpf-approved-exceptions.csv"
        resource_path = module_root / "src/main/resources/META-INF/cpf/cpf-approved-exceptions.csv"
        lock_path = module_root / "manifest/resolved-starter-lock.json"
        for required in (policy_path, config_path, resource_path, lock_path):
            if not required.is_file():
                fail(f"Generated Domain policy file missing: {required.relative_to(ROOT)}")
        policy = parse_properties(policy_path)
        if policy.get("policyVersion") != "1.0" or policy.get("failClosed") != "true":
            fail(f"Generated Domain policy version/failClosed mismatch: {module_root.relative_to(ROOT)}")
        profile = str(manifest.get("capabilityProfile", ""))
        if profile not in EXPECTED_PROFILES or policy.get("profile") != profile:
            fail(f"Generated Domain profile drift: {module_root.relative_to(ROOT)}")
        capabilities = [str(item) for item in manifest.get("resolvedCapabilityGroups", [])]
        if set(capabilities) != {item for item in policy.get("capabilities", "").split(",") if item}:
            fail(f"Generated Domain capability drift: {module_root.relative_to(ROOT)}")
        if policy.get("requiredStandards", "").split(",") != EXPECTED_STANDARDS:
            fail(f"Generated Domain standard inheritance incomplete: {module_root.relative_to(ROOT)}")
        if sha256(config_path) != sha256(resource_path) or sha256(config_path) != policy.get("exceptionRegistrySha256"):
            fail(f"Generated Domain exception Registry drift: {module_root.relative_to(ROOT)}")
        rows = read_exceptions(config_path, module_root, manifest)
        lock = load_json(lock_path)
        if lock.get("exceptionRegistrySha256") != sha256(config_path):
            fail(f"Generated Domain exception lock hash drift: {module_root.relative_to(ROOT)}")
        locked = {item.get("exceptionId"): item for item in lock.get("approvedExceptions", [])}
        if set(locked) != {row["exception_id"] for row in rows}:
            fail(f"Generated Domain approved exception id drift: {module_root.relative_to(ROOT)}")
        approved_artifacts = {row["artifact"]: row for row in rows}
        build_text = (module_root / "build.gradle").read_text(encoding="utf-8")
        if "JavaLanguageVersion.of(25)" not in build_text or "rootProject.ext.cpfJavaVersion" in build_text:
            fail(f"Generated Domain Java 25 inheritance drift: {module_root.relative_to(ROOT)}")
        if "cpfResolvedStarterLock" not in build_text or "verifyCpfPackagedDependencies" not in build_text:
            fail(f"Generated Domain packaged dependency verification is not lock-driven: {module_root.relative_to(ROOT)}")
        profile_entry = next(
            item for item in load_json(PROFILES)["profiles"]
            if item["publicName"] == profile
        )
        profile_project = ":" + str(profile_entry["aggregateProject"]).lstrip(":")
        allowed_projects = {profile_project, *[str(item) for item in lock.get("providerProjects", [])]}
        declared_projects = set(re.findall(
            r"""project\s*\(\s*['"](:cpf-starter-[^'"]+)['"]\s*\)""", build_text
        ))
        unexpected_projects = sorted(declared_projects - allowed_projects)
        if unexpected_projects:
            fail(f"Generated Domain pulled unselected Starter projects: {module_root.relative_to(ROOT)} -> {unexpected_projects}")
        if profile_project not in declared_projects:
            fail(f"Generated Domain does not declare selected public Profile: {module_root.relative_to(ROOT)} -> {profile_project}")
        for artifact, capability in FORBIDDEN_ARTIFACTS.items():
            if artifact in build_text and artifact not in approved_artifacts:
                fail(f"unapproved direct OSS dependency in {module_root.relative_to(ROOT)}: {artifact}")
            if artifact in build_text:
                version = approved_artifacts[artifact]["version"]
                if f"{artifact}:{version}" not in build_text:
                    fail(f"approved OSS version drift in {module_root.relative_to(ROOT)}: {artifact}")
        approved_capabilities = {row["capability"] for row in rows}
        for source_root in (module_root / "src/main/java", module_root / "src/main/kotlin"):
            if not source_root.is_dir():
                continue
            for source in source_root.rglob("*"):
                if not source.is_file():
                    continue
                text = source.read_text(encoding="utf-8")
                internal_imports = re.findall(r"(?m)^\s*import\s+(com\.cpf\.starter\.[A-Za-z0-9_.$]+)\s*;", text)
                forbidden_imports = sorted(set(internal_imports))
                if forbidden_imports:
                    fail(f"Generated Domain imports internal CPF provider: {source.relative_to(ROOT)} -> {forbidden_imports}")
                for symbol, capability in FORBIDDEN_SYMBOLS.items():
                    if re.search(rf"\b{re.escape(symbol)}\b", text) and capability not in approved_capabilities:
                        fail(f"unapproved direct OSS symbol {symbol}: {source.relative_to(ROOT)}")
    return validated



def validate_completion_matrices() -> tuple[int, int]:
    allowed_statuses = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
    with REQUIREMENT_MATRIX.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    expected_header = [
        "requirement_id", "priority", "owner", "requirement", "change_target", "consumer",
        "acceptance_criteria", "verification_method", "evidence", "regression_protection",
        "development_status", "verification_status",
    ]
    if not rows or list(rows[0]) != expected_header:
        fail("Requirement Matrix header mismatch or no rows")
    ids: set[str] = set()
    for row in rows:
        requirement_id = row["requirement_id"].strip()
        if not re.fullmatch(r"QA39-\d{3}", requirement_id) or requirement_id in ids:
            fail(f"invalid or duplicate requirement id: {requirement_id}")
        ids.add(requirement_id)
        development = row["development_status"].strip()
        verification = row["verification_status"].strip()
        if development not in allowed_statuses or verification not in allowed_statuses:
            fail(f"invalid requirement status: {requirement_id} -> {development}/{verification}")
        evidence = row["evidence"].strip()
        if development == "완료":
            blanks = [field for field in ("change_target", "consumer", "acceptance_criteria", "verification_method", "evidence") if not row[field].strip()]
            if blanks:
                fail(f"False Complete requirement {requirement_id}: blank {blanks}")
            if any(token in evidence for token in ("미작성", "예정", "TBD", "TODO")):
                fail(f"False Complete requirement {requirement_id}: placeholder evidence")
        if verification == "완료":
            if not evidence or not any(token in evidence for token in ("PASS", "완료", "0건", "보호 경로")):
                fail(f"False Verified requirement {requirement_id}: executable PASS evidence required")
            if any(token in evidence for token in ("미실행", "미검증", "실패")):
                fail(f"False Verified requirement {requirement_id}: non-success evidence")

    with SCENARIO_MATRIX.open(encoding="utf-8-sig", newline="") as handle:
        scenarios = list(csv.DictReader(handle))
    expected_scenario_header = ["scenario_id", "scenario", "acceptance", "requirement_ids", "verification_status", "evidence"]
    if not scenarios or list(scenarios[0]) != expected_scenario_header:
        fail("Scenario Matrix header mismatch or no rows")
    scenario_ids: set[str] = set()
    for row in scenarios:
        scenario_id = row["scenario_id"].strip()
        if not scenario_id or scenario_id in scenario_ids:
            fail(f"invalid or duplicate scenario id: {scenario_id}")
        scenario_ids.add(scenario_id)
        status = row["verification_status"].strip()
        if status not in allowed_statuses:
            fail(f"invalid scenario status: {scenario_id} -> {status}")
        linked = {item.strip() for item in re.split(r"[,;]", row["requirement_ids"]) if item.strip()}
        unknown = sorted(linked - ids)
        if unknown:
            fail(f"Scenario links unknown requirements: {scenario_id} -> {unknown}")
        evidence = row["evidence"].strip()
        if status == "완료":
            if not evidence or not any(token in evidence for token in ("PASS", "완료", "0건")):
                fail(f"False Verified scenario {scenario_id}: PASS evidence required")
            if any(token in evidence for token in ("미실행", "미검증", "실패")):
                fail(f"False Verified scenario {scenario_id}: non-success evidence")
    return len(rows), len(scenarios)

def main() -> None:
    catalog, profiles, removed_ids = validate_canonical_catalogs()
    validate_resolution_semantics(profiles)
    validate_generator_and_enforcement()
    validate_dependency_graph_and_ownership(catalog)
    validate_changed_sql_vendor_parity()
    delete_paths = validate_delete_closure(catalog, removed_ids)
    generated_domains = validate_generated_domains()
    requirement_count, scenario_count = validate_completion_matrices()
    print(
        "[CPF][QA39][PASS] "
        f"profiles={len(EXPECTED_PROFILES)}, groups={len(EXPECTED_GROUPS)}, "
        f"modules={len(catalog.get('modules', []))}, deletePaths={len(delete_paths)}, "
        f"generatedDomains={generated_domains}, requirements={requirement_count}, scenarios={scenario_count}"
    )


if __name__ == "__main__":
    main()
