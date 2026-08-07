#!/usr/bin/env python3
"""REV-004 overlay source-closure gate. This does not replace full-repository build/runtime gates."""
from __future__ import annotations
import json, re, sys, subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
errors: list[str] = []
notes: list[str] = []

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        errors.append(f"missing file: {rel}")
        return ""
    return path.read_text(encoding="utf-8")

def require(rel: str, patterns: dict[str, str]) -> None:
    text = read(rel)
    for label, pattern in patterns.items():
        if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
            errors.append(f"{rel}: missing {label}")

ops = [
    "admIntegrationCryptoStatus", "admIntegrationTimeHealth", "admIntegrationDataQualityValidate",
    "admIntegrationDataQualityCorrectionApprovalRequest", "admIntegrationDataQualityCorrectionExecute",
    "admIntegrationDataQualityReplay", "admIntegrationWebhookDlq", "admIntegrationWebhookReplay",
]


require("cpf-admin/src/main/java/com/cpf/admin/AdmApplication.java", {
    "session cookie OpenAPI scheme": r'@SecurityScheme\(name = "admSessionCookie"[\s\S]*SecuritySchemeIn\.COOKIE[\s\S]*paramName = "JSESSIONID"',
    "CSRF header OpenAPI scheme": r'@SecurityScheme\(name = "admCsrfHeader"[\s\S]*SecuritySchemeIn\.HEADER[\s\S]*paramName = "X-XSRF-TOKEN"',
})
require("cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java", {
    "session security requirement": r'@SecurityRequirement\(name = "admSessionCookie"\)',
    "reconcile operation": r'@PostMapping\("/requests/\{id\}/reconcile"\)[\s\S]*operationId="admApprovalReconcile"',
    "server session actor": r'@RequestAttribute\("adm\.operatorId"\)',
})

require("cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmApprovalSnapshotIntegrity.java", {
    "canonical key sorting": r"TreeMap<String, Object>",
    "Unicode normalized duplicate fail-close": r"duplicate JSON key after Unicode normalization",
    "millisecond time canonicalization": r"truncatedTo\(ChronoUnit\.MILLIS\)",
    "SHA-256": r"MessageDigest\.getInstance\(\"SHA-256\"\)",
    "constant-time compare": r"MessageDigest\.isEqual",
    "safe invalid audit hash": r"safeStored\s*=\s*wellFormed\s*\?\s*stored\.toLowerCase\(\)\s*:\s*\"INVALID\"",
})
require("cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java", {
    "pre-reservation integrity check": r"verifySnapshotOrAudit\(id,doc,operator,\"APPROVED\"\)",
    "atomic execution reservation": r"reserveExecution\(",
    "post-reservation integrity check": r"reservedVerification=snapshotIntegrity\.verify\(reserved\)",
    "UNKNOWN preservation": r"markExecutionUnknown",
    "observation-only reconciliation": r"public Map<String,Object> reconcile[\s\S]*port\.reconcile",
    "reconcile never calls execute": r"port\.reconcile\(approvedCommand",
    "canonical expiry": r"canonicalExpireAt",
})
service_text = read("cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java")
reconcile_match = re.search(r"public Map<String,Object> reconcile\(.*?\n    }\n\n    private static AdmApprovedOperationCommand", service_text, re.DOTALL)
if not reconcile_match:
    errors.append("approval reconcile method boundary missing")
elif "port.execute(" in reconcile_match.group(0):
    errors.append("approval reconcile must not invoke Owner mutation execute")

require("cpf-core/src/main/java/com/cpf/core/api/data/quality/CpfDataQualityOperations.java", {
    "public correction authorization removed": r"Public callers can validate, inspect, replay and reconcile",
    "versioned replay contract": r"record ReplayCommand",
})
quality_api = read("cpf-core/src/main/java/com/cpf/core/api/data/quality/CpfDataQualityOperations.java")
for forbidden in ("boolean approved", "correctAuthorized", "CorrectionAuthorization"):
    if forbidden in quality_api: errors.append(f"public data-quality API still exposes {forbidden}")
require("cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java", {
    "pre-reservation integrity audit transaction": r"Propagation\.REQUIRES_NEW[\s\S]*recordIntegrityFailure",
    "post-reservation atomic integrity transition": r"recordExecutionIntegrityFailure[\s\S]*SNAPSHOT_HASH_MISMATCH",
    "single-use reservation": r"APPROVAL_STATUS='APPROVED' AND VERSION_NO=\?",
    "strict unknown transition": r"approval UNKNOWN transition failed",
})
require("cpf-admin/src/main/java/com/cpf/admin/approval/owner/DataQualityCorrectionApprovalOwnerCommandAdapter.java", {
    "owner-side reserved command re-read": r"findReservedExecutionCommand",
    "owner-side hash verification": r"snapshotIntegrity\.verify\(reserved\)",
    "maker-checker separation": r"requestedBy\(\)\.equals\(command\.approvedBy\(\)\)",
    "version conflict code": r"DQ-VERSION-CONFLICT",
    "before/after audit hash": r"beforeHash[\s\S]*afterHash",
    "side effect applied reconciliation": r"SIDE_EFFECT_APPLIED",
    "side effect not applied reconciliation": r"SIDE_EFFECT_NOT_APPLIED",
    "ambiguous reconciliation remains unknown": r"DQ-RECONCILE-AMBIGUOUS",
})
require("cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java", {
    "HTTP 409 contract": r"HttpStatus\.CONFLICT",
    "version conflict mapping": r"DQ-VERSION-CONFLICT",
    "server session actor": r"@RequestAttribute\(\"adm\.operatorId\"\)",
})
require("cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts", {
    "SHA-256 fingerprint": r"crypto\.subtle\.digest\(\"SHA-256\"",
    "session scope": r"sessionStorage",
    "pending and confirmed lifecycle": r'\"pending\"\s*\|\s*\"confirmed\"',
    "confirmed state guard": r"Approval idempotency state changed before confirmation",
    "Unicode collision fail-close": r"Duplicate draft key after Unicode normalization",
})
page = read("cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue")
api = read("cpf-admin/frontend/src/features/integration-closure/integrationClosureApi.ts")
route_path = ROOT / "cpf-admin/frontend/src/app/routes.ts"
route = route_path.read_text(encoding="utf-8") if route_path.is_file() else ""
if not route: notes.append("baseline route registry not included in overlay; full-repository route parity NOT_EXECUTED")
route_contract = read("cpf-admin/frontend/src/generated/adm-route-operation-contract.ts")
op_contract = read("cpf-admin/frontend/src/generated/cpf-operation-contract.ts")
for op in ops:
    if op not in api: errors.append(f"API facade missing operation consumer: {op}")
    if route and op not in route: errors.append(f"route registry missing operation: {op}")
    if op not in route_contract: errors.append(f"route operation contract missing: {op}")
    if op not in op_contract: errors.append(f"generated operation contract missing: {op}")
for action in ["cryptoStatus", "timeHealth", "validate", "requestCorrectionApproval", "executeCorrectionApproval", "replayQuality", "webhookDlq", "replayWebhook"]:
    if f"integrationClosureApi.{action}" not in page: errors.append(f"Vue page missing actual call: {action}")
for ui in ["401", "403", "409", "429", "500", "503", "window.confirm", 'role="alert"', "aria-live", "dlqRows.length === 0"]:
    if ui not in page: errors.append(f"Vue operational state missing: {ui}")
idempotency_test = read("cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.test.ts")
if "corrected" in idempotency_test and ("not.toContain" not in idempotency_test or "never stores corrected payload" not in idempotency_test):
    errors.append("idempotency storage test does not prove corrected payload exclusion")

spec_path = ROOT / "cpf-admin/frontend/openapi/cpf-openapi.json"
try:
    spec = json.loads(spec_path.read_text(encoding="utf-8"))
except Exception as exc:
    errors.append(f"invalid OpenAPI JSON: {exc}")
    spec = {}
schemes = spec.get("components", {}).get("securitySchemes", {})
expected_schemes = {
    "admSessionCookie": {"type": "apiKey", "in": "cookie", "name": "JSESSIONID"},
    "admCsrfHeader": {"type": "apiKey", "in": "header", "name": "X-XSRF-TOKEN"},
}
for name, expected in expected_schemes.items():
    actual = schemes.get(name, {})
    for key, value in expected.items():
        if actual.get(key) != value:
            errors.append(f"OpenAPI security scheme drift: {name}.{key}={actual.get(key)!r}, expected {value!r}")
if "admSession" in schemes:
    errors.append("legacy bearer/session scheme admSession remains in OpenAPI")

found: dict[str, dict] = {}
for path, item in spec.get("paths", {}).items():
    for method, operation in item.items():
        if isinstance(operation, dict) and operation.get("operationId") in ops:
            found[operation["operationId"]] = operation
if set(found) != set(ops): errors.append(f"OpenAPI operation set mismatch: missing={sorted(set(ops)-set(found))} extra={sorted(set(found)-set(ops))}")
for op, operation in found.items():
    security = operation.get("security", [])
    if not any("admSessionCookie" in row for row in security): errors.append(f"{op}: session cookie security missing")
    if op not in {"admIntegrationCryptoStatus", "admIntegrationTimeHealth", "admIntegrationWebhookDlq"} and not any("admCsrfHeader" in row for row in security):
        errors.append(f"{op}: CSRF security missing")
    for status in ["400", "401", "403", "404", "409", "429", "500", "503"]:
        if status not in operation.get("responses", {}): errors.append(f"{op}: response {status} missing")
all_operation_ids = []
for item in spec.get("paths", {}).values():
    for method, operation in item.items():
        if method in {"get","post","put","patch","delete","head","options","trace"} and isinstance(operation, dict):
            if operation.get("operationId"): all_operation_ids.append(operation["operationId"])
if len(all_operation_ids) != len(set(all_operation_ids)): errors.append("OpenAPI operationId duplicate")
if spec.get("x-cpf-openapi-operation-count") != len(all_operation_ids): errors.append("OpenAPI operation count extension drift")
if "admApprovalReconcile" not in all_operation_ids: errors.append("OpenAPI approval reconcile operation missing")
reconcile_operation = spec.get("paths", {}).get("/adm/api/approvals/requests/{id}/reconcile", {}).get("post", {})
if not any("admSessionCookie" in row and "admCsrfHeader" in row for row in reconcile_operation.get("security", [])): errors.append("approval reconcile session security missing")
if "Mutation을 자동 재실행하지 않습니다" not in reconcile_operation.get("description", ""): errors.append("approval reconcile non-retry contract missing")


# Caller-minted authorization and stale fixed-count contracts are forbidden repository-wide.
for forbidden in ["CorrectionAuthorization", "correctAuthorized"]:
    hits = []
    for product_root in ("cpf-core", "cpf-common", "cpf-admin", "cpf-biz-admin", "cpf-batch", "cpf-starters"):
        root = ROOT / product_root
        if not root.exists(): continue
        for path in root.rglob("*"):
            if path.is_file() and path.suffix.lower() in {".java", ".kt", ".groovy", ".ts", ".vue"}:
                if forbidden in path.read_text(encoding="utf-8", errors="replace"):
                    hits.append(path.relative_to(ROOT).as_posix())
    if hits: errors.append(f"caller-minted authorization token remains {forbidden}: {hits}")
for rel in [
    "cpf-tools/verification/qa38/verify-qa38-structure.py",
    "cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py",
    "cpf-tools/scripts/verify-cpf-qa34-build-contract.py",
    "build.gradle",
]:
    text = read(rel)
    if re.search(r"(?:modules|internal|profiles)\.size\(\)\s*(?:==|!=)\s*(?:38|32|6)|38-module", text):
        errors.append(f"stale fixed starter count in {rel}")
    if re.search(r"BASE_SHA\s*=|(?i:(?:C|D|E|F):[\\/](?![nrt]))", text):
        errors.append(f"hard-coded baseline/path in {rel}")

active_artifacts: set[str] = set()
catalog_paths = [
    "cpf-tools/config/cpf-starter-catalog.json",
    "cpf-tools/generator/contracts/cpf-starter-catalog.json",
]
try:
    catalogs = [json.loads(read(path)) for path in catalog_paths]
    if catalogs[0] != catalogs[1]: errors.append("starter canonical/config catalogs differ")
    catalog = catalogs[0]
    modules = catalog.get("modules", [])
    public = [m for m in modules if m.get("visibility") == "public"]
    internal = [m for m in modules if m.get("visibility") == "internal"]
    retained = catalog.get("retainedInactiveRoots", [])
    if (len(modules), len(public), len(internal), len(retained)) != (39, 6, 33, 1):
        errors.append(f"starter partition mismatch: modules={len(modules)} public={len(public)} internal={len(internal)} retained={len(retained)}")
    expected_retained = {
        "path": "cpf-starters/openapi-webmvc", "artifactId": "cpf-starter-openapi-webmvc",
        "replacementArtifactId": "cpf-starter-profile-web-api", "replacementOwnerPath": "cpf-starters/profiles/web-api",
        "status": "PENDING_USER_DELETE_APPROVAL", "active": False, "includeInSettings": False,
        "publishable": False, "consumerAllowed": False, "approvalRequired": True,
    }
    if not retained or any(retained[0].get(k) != v for k, v in expected_retained.items()):
        errors.append("retained inactive root contract mismatch")
    if any(m.get("artifactId") == "cpf-starter-openapi-webmvc" for m in modules): errors.append("retired artifact is active")
    release = json.loads(read("cpf-tools/release/cpf-final-artifact-catalog.json"))
    release_starters = {a.get("artifactId") for a in release.get("artifacts", []) if a.get("kind") in {"starter-profile", "internal-starter"}}
    active_artifacts = {m.get("artifactId") for m in modules}
    if release_starters != active_artifacts:
        errors.append(f"release/active starter mismatch missing={sorted(active_artifacts-release_starters)} extra={sorted(release_starters-active_artifacts)}")
    if set(release.get("removedArtifactIds") or []) & active_artifacts:
        errors.append("removed release artifact is active")
except Exception as exc:
    errors.append(f"starter catalog check failed: {exc}")

settings = read("settings.gradle")
if re.search(r"include[^\n]*openapi-webmvc", settings): errors.append("retained starter is included in settings")
for rel in ["cpf-tools/build/platform-bom/public-bom/build.gradle", "cpf-tools/build/platform-bom/internal-bom/build.gradle"]:
    if "cpf-starter-openapi-webmvc" in read(rel): errors.append(f"retired artifact exposed by {rel}")

platform_properties = read("gradle/cpf-platform.properties")
active_components = set(re.findall(r"^component\.([^=]+)=inherit$", platform_properties, re.MULTILINE))
expected_components = active_artifacts | {"cpf-core", "cpf-common", "cpf-admin", "cpf-biz-admin", "cpf-batch", "cpf-reference", "cpf-gateway"}
if active_components != expected_components:
    errors.append(f"platform component drift missing={sorted(expected_components-active_components)} extra={sorted(active_components-expected_components)}")
if "component.cpf-starter-openapi-webmvc=inherit" in platform_properties:
    errors.append("retired OpenAPI artifact remains an active platform component")
if "legacyComponent.cpf-starter-openapi-webmvc=cpf-starter-profile-web-api" not in platform_properties:
    errors.append("OpenAPI compatibility alias is not explicit")
require("cpf-tools/build/platform-bom/internal-bom/build.gradle", {
    "catalog-derived internal constraints": r"internalModules.*visibility.*internal[\s\S]*constraints",
    "internal exact equality": r"missing=expected\.toSet\(\)-actual\.toSet\(\)[\s\S]*extra=actual\.toSet\(\)-expected\.toSet\(\)",
    "duplicate detection": r"duplicateActual",
})
require("cpf-tools/build/platform-bom/public-bom/build.gradle", {
    "catalog-derived public constraints": r"publicModules.*visibility.*public[\s\S]*constraints",
    "public exact equality": r"missing=expected-actual\.toSet\(\)[\s\S]*extra=actual\.toSet\(\)-expected",
    "internal leak detection": r"internalConstraintLeak",
})

require("cpf-tools/verification/final-dev/run-db3-lifecycle.ps1", {
    "mandatory ExpectedHead": r"Parameter\(Mandatory\s*=\s*\$true\)[\s\S]{0,160}\$ExpectedHead",
    "git root discovery": r"rev-parse\s+--show-toplevel",
    "stdin secret transport": r"RedirectStandardInput\s*=\s*\$true[\s\S]*--connection-json-stdin[\s\S]*StandardInput\.WriteLine\(\$ConnectionJson\)",
    "three official vendors": r"CPF_RUNTIME_ORACLE_PASSWORD[\s\S]*CPF_RUNTIME_POSTGRESQL_PASSWORD[\s\S]*CPF_RUNTIME_MARIADB_PASSWORD",
    "redacted evidence": r"Protect-Text",
    "child environment allowlist": r"Environment\.Clear\(\)[\s\S]*JAVA_HOME[\s\S]*CPF_DB_RUNNER_CHILD",
    "timeout kill": r"WaitForExit\(\$TimeoutSeconds \* 1000\)[\s\S]*Kill\(\$true\)",
    "exit propagation": r"exit\s+\$overallExit",
})

for path in ROOT.rglob("*"):
    if path.is_file():
        rel = path.relative_to(ROOT).as_posix()
        if "__pycache__" in rel or rel.endswith(".pyc"): errors.append(f"temporary bytecode in overlay: {rel}")
        if len(rel) > 220: errors.append(f"Windows-risk path length {len(rel)}: {rel}")
        protected_restore = {
            "cpf-docs/assets/manuals/cpf-document-quality-r9.svg": "2979b5f65e7b8ace8a735cd5eae501c6b60cc851be2f31fd441383e7a2d498d5"
        }
        if rel in protected_restore:
            import hashlib
            if hashlib.sha256(path.read_bytes()).hexdigest() != protected_restore[rel]:
                errors.append(f"protected restoration hash mismatch: {rel}")
        elif rel.startswith(("cpf-docs/deliverables/", "cpf-docs/guides/", "cpf-docs/assets/manuals/", "cpf-docs/environment/docker/", "cpf-tools/environment/docker-development-test/")):
            errors.append(f"protected path modified without restoration allowlist: {rel}")

marker_path = ROOT / "cpf-admin/frontend/src/generated/.cpf-openapi-source.json"
try:
    import hashlib
    marker = json.loads(marker_path.read_text(encoding="utf-8"))
    openapi_bytes = spec_path.read_bytes()
    if marker.get("openApiSha256") != hashlib.sha256(openapi_bytes).hexdigest(): errors.append("generated marker OpenAPI hash drift")
    if marker.get("openApiOperationCount") != len(all_operation_ids): errors.append("generated marker operation count drift")
    sorted_ids = sorted(all_operation_ids)
    if marker.get("openApiOperationIdsSha256") != hashlib.sha256("\n".join(sorted_ids).encode()).hexdigest(): errors.append("generated marker operation ID hash drift")
    rows = sorted(marker.get("generatedFiles", []), key=lambda item: item.get("path") or "")
    tracked = {item.get("path"): item.get("sha256") for item in rows}
    expected_set_hash = hashlib.sha256("\n".join(f"{item.get('path')}:{item.get('sha256')}" for item in rows).encode()).hexdigest()
    if marker.get("generatedFileSetSha256") != expected_set_hash:
        errors.append("generated marker file-set hash drift")
    generated_root = ROOT / "cpf-admin/frontend/src/generated"
    for generated in generated_root.rglob("*"):
        if generated.is_file() and generated.name != ".cpf-openapi-source.json" and generated.suffix in {".ts", ".tsx"}:
            rel = generated.relative_to(ROOT / "cpf-admin/frontend").as_posix()
            actual = hashlib.sha256(generated.read_bytes()).hexdigest()
            if tracked.get(rel) != actual: errors.append(f"generated marker artifact hash drift: {rel}")
except Exception as exc:
    errors.append(f"generated marker validation failed: {exc}")

# R6 behavior/source contract gates
for gate in (
    ROOT / "cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py",
    ROOT / "cpf-tools/verification/final-dev/verify-r6-approval-contract.py",
    ROOT / "cpf-tools/verification/final-dev/verify-r6-sql-parity.py",
    ROOT / "cpf-tools/verification/final-dev/verify-r6-overlay-hygiene.py",
):
    if gate.is_file():
        result = subprocess.run([sys.executable, str(gate), str(ROOT)], cwd=ROOT, text=True, capture_output=True)
        if result.returncode != 0:
            errors.append(f"{gate.name} failed: {result.stdout}{result.stderr}")
    else:
        errors.append(f"missing R6 gate: {gate.relative_to(ROOT)}")

if errors:
    print("\n".join(f"FAIL {error}" for error in errors))
    raise SystemExit(1)
print(f"PASS REV-004 overlay source closure integrationOperations={len(ops)} openapiOperations={len(all_operation_ids)} starterModules=39")
print("NOT_EXECUTED full repository Gradle/Node/DB3 runtime/Playwright gates require complete checkout and external services")
for note in notes: print(f"NOTE {note}")
