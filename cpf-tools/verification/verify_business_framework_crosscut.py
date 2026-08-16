#!/usr/bin/env python3
"""CPF 업무 Framework 6대 횡단 품질을 Source 기준으로 판정합니다.

문서 선언만으로 PASS하지 않고 실제 Public API, Consumer, Generator 산출물과
운영 조회 경로를 함께 검사합니다. Runtime이 필요한 검증은 별도 Evidence로 관리합니다.
"""
from __future__ import annotations
import argparse, json, re, sys, os, subprocess
from pathlib import Path

_parser = argparse.ArgumentParser()
_parser.add_argument("--root", default=str(Path(__file__).resolve().parents[2]))
_parser.add_argument("--evidence")
_args = _parser.parse_args()
ROOT = Path(_args.root).resolve()
EVIDENCE = Path(_args.evidence).resolve() if _args.evidence else ROOT / "cpf-docs/work/evidence/current/CPF_BUSINESS_FRAMEWORK_CROSSCUT_REVIEW.json"


def source_identity(root: Path) -> str:
    env = os.environ.get("CPF_SOURCE_SHA", "").strip()
    if re.fullmatch(r"[0-9a-fA-F]{40}", env): return env.lower()
    if (root / ".git").exists():
        cp = subprocess.run(["git","rev-parse","HEAD"],cwd=root,text=True,capture_output=True)
        value=(cp.stdout or "").strip()
        if cp.returncode == 0 and re.fullmatch(r"[0-9a-fA-F]{40}", value): return value.lower()
    base=root/"cpf-docs/work/BASE_SHA.txt"
    if base.is_file():
        value=base.read_text(encoding="utf-8",errors="ignore").strip()
        if re.fullmatch(r"[0-9a-fA-F]{40}", value): return value.lower()
    return "UNKNOWN"

def text(path: str) -> str:
    p = ROOT / path
    return p.read_text(encoding="utf-8-sig") if p.is_file() else ""


def exists(path: str) -> bool:
    return (ROOT / path).is_file()


def files_under(path: str, suffix: str = ""):
    p = ROOT / path
    if not p.exists(): return []
    return [x for x in p.rglob("*") if x.is_file() and (not suffix or x.name.endswith(suffix))]


def any_text(paths, pattern: str) -> bool:
    rx = re.compile(pattern, re.I | re.M)
    for p in paths:
        try: s = p.read_text(encoding="utf-8-sig")
        except (UnicodeDecodeError, OSError): continue
        if rx.search(s): return True
    return False


def count_text(paths, pattern: str) -> int:
    rx = re.compile(pattern, re.I | re.M)
    count = 0
    for p in paths:
        try: s = p.read_text(encoding="utf-8-sig")
        except (UnicodeDecodeError, OSError): continue
        count += len(rx.findall(s))
    return count


checks = []
def check(axis, check_id, ok, detail, severity="P0"):
    checks.append({"axis": axis, "checkId": check_id, "status": "PASS" if ok else "FAIL", "severity": severity, "detail": detail})

java_main = [p for top in ["cpf-core", "cpf-starters", "cpf-admin", "cpf-biz-admin", "cpf-gateway", "cpf-batch", "cpf-education", "cpf-member", "cpf-external"] for p in files_under(top, ".java") if "/src/test/" not in p.as_posix()]
active_docs = []
for top in ["cpf-docs/governance", "cpf-docs/architecture", "cpf-docs/development", "cpf-docs/work/current"]:
    active_docs += [p for p in files_under(top) if p.suffix.lower() in {".md", ".csv", ".json", ".yml", ".yaml"}]

# 1) Golden Path
check("GOLDEN_PATH", "GP-PUBLIC-SERVICE-CLIENT", exists("cpf-starters/integration/http/src/main/java/com/cpf/integration/http/api/CpfServiceClient.java"), "typed service client public contract")
internal_imports = count_text([p for p in files_under("cpf-starters/integration") if p.suffix == ".java" and "/src/main/" in p.as_posix()], r"com\.cpf\.platform\.operations\.observability\.internal")
check("GOLDEN_PATH", "GP-NO-CROSS-OWNER-INTERNAL", internal_imports == 0, f"integration main -> observability internal imports={internal_imports}")
check("GOLDEN_PATH", "GP-DOMAIN-TYPED-CONTRACT", exists("cpf-core/src/main/java/com/cpf/core/api/domain/CpfDomainClient.java") and exists("cpf-core/src/main/java/com/cpf/core/api/result/CpfResult.java"), "topology-independent typed Domain Client + 4-state Result contract")
check("GOLDEN_PATH", "GP-DOMAIN-ROUTER", exists("cpf-starters/integration/src/main/java/com/cpf/integration/api/domaincall/CpfDomainClientRouter.java") and exists("cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/domaincall/CpfDomainCallAutoConfiguration.java"), "LOCAL/REMOTE Domain Binding runtime materialized")

# 2) Execution Lifecycle
outcome = text("cpf-starters/integration/src/main/java/com/cpf/integration/api/servicecall/CpfServiceCallOutcome.java")
check("EXECUTION_LIFECYCLE", "EXEC-FOUR-STATE", all(x in outcome for x in ["SUCCESS", "BUSINESS_FAILURE", "TECHNICAL_FAILURE", "UNKNOWN"]), "boundary outcome 4-state")
check("EXECUTION_LIFECYCLE", "EXEC-SINGLE-RESULT-STATUS", "CpfResultStatus" in outcome and "enum ResultStatus" not in outcome, "service-call outcome reuses Core CpfResultStatus; no duplicate four-state enum")
check("EXECUTION_LIFECYCLE", "EXEC-RECOVERY", "recoveryId" in outcome and "recoveryAction" in outcome, "UNKNOWN recovery metadata")
check("EXECUTION_LIFECYCLE", "EXEC-AUTOCONFIG", exists("cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/servicecall/CpfServiceCallAutoConfiguration.java") and "CpfServiceCallAutoConfiguration" in text("cpf-starters/integration/http/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"), "engine/public ports materialized by auto-configuration")

# 3) Common Product Service
for family, symbol in [("CODE", "CpfCodeService"),("MESSAGE", "CpfMessageService"),("PARAMETER", "CpfParameterService"),("CALENDAR", "CpfCalendarService"),("TEMPLATE", "CpfTemplateService")]:
    check("COMMON_PRODUCT_SERVICE", f"COMMON-{family}", any_text(files_under("cpf-starters/common", ".java"), rf"\b{re.escape(symbol)}\b"), f"{family} product service/source exists")
common_main = [p for p in files_under("cpf-starters/common", ".java") if "/src/main/" in p.as_posix()]
direct_time = count_text(common_main, r"(?:Instant|LocalDate|LocalDateTime|OffsetDateTime|ZonedDateTime)\.now\(\)|System\.currentTimeMillis\(\)")
check("COMMON_PRODUCT_SERVICE", "COMMON-DETERMINISTIC-TIME", direct_time == 0, f"common production direct system-time calls={direct_time}", "P1")
edu_common = text("cpf-education/src/main/java/com/cpf/education/common/cmn/controller/EducationCmnEducationController.java")
for symbol in ["CpfCodeService","CpfMessageService","CpfParameterService","CpfCalendarService","CpfTemplateService"]:
    check("COMMON_PRODUCT_SERVICE", f"COMMON-CONSUMER-{symbol.upper()}", symbol in edu_common, f"Education actual consumer uses {symbol}")
logger_api = text("cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/logging/CpfStructuredLogger.java")
check("COMMON_PRODUCT_SERVICE", "COMMON-STRUCTURED-LOGGER", all(x in logger_api for x in ["business(","operation(","security(","error("]), "single structured logging public API; audit remains durable separate contract")
check("COMMON_PRODUCT_SERVICE", "COMMON-STRUCTURED-LOGGER-CONSUMER", "CpfStructuredLogger" in edu_common and "structuredLogger.business" in edu_common, "Education actual consumer uses structured logger")

# 4) Operational Journey
adm_files = files_under("cpf-admin", ".java")
check("OPERATIONAL_JOURNEY", "OPS-TIMELINE-CONSUMER", any_text(adm_files, r"CpfTransactionTimelineQueryPort"), "ADM consumes transaction timeline query port")
check("OPERATIONAL_JOURNEY", "OPS-UNKNOWN-RECONCILE", any_text(adm_files, r"UNKNOWN|Unknown") and any_text(files_under("cpf-starters/platform-operations", ".java"), r"CpfReconciliationPort"), "UNKNOWN + reconciliation operational path")

# 5) Generator-first DX
# Generated Customer Domain은 Online 업무 Source만 생성한다. Batch는 Domain Generator 산출물이 아니라
# 초기 프로젝트 구성에서 cpf-starter-batch/cpf-batch를 선택하는 별도 Framework Capability다.
for domain in ["cpf-member", "cpf-external"]:
    for profile in ["local", "test", "dev", "stg", "prod"]:
        check("GENERATOR_FIRST_DX", f"GEN-{domain}-online-{profile}".upper(), exists(f"{domain}/online/src/main/resources/application-{profile}.yml"), f"{domain}/online {profile} profile")
    forbidden = []
    for module in ["batch", "domain", "jobpack"]:
        base = ROOT / domain / module
        if base.is_dir() and any(x.is_file() for x in base.rglob("*")):
            forbidden.append(module)
    check("GENERATOR_FIRST_DX", f"GEN-{domain}-NO-GENERATED-BATCH".upper(), not forbidden, f"generated forbidden modules={forbidden}")
catalog = json.loads(text("cpf-tools/generator/contracts/cpf-starter-catalog.json") or "{}")
batch_profile = (catalog.get("profileDefinitions") or {}).get("batch") or {}
batch_modules = [m for m in (catalog.get("modules") or []) if m.get("profileId") == "batch" and m.get("visibility") == "public"]
check("GENERATOR_FIRST_DX", "GEN-BATCH-CAPABILITY-SEPARATE",
      exists("cpf-starters/profiles/batch-service/build.gradle")
      and exists("cpf-batch/build.gradle") and exists("cpf-batch/runtime/build.gradle")
      and batch_profile.get("artifactId") == "cpf-starter-batch"
      and len(batch_modules) == 1 and batch_modules[0].get("ownerPath") == "cpf-starters/profiles/batch-service",
      "Batch is selected through public batch profile + cpf-batch runtime, never Generated Domain output")
member_java = [p for p in files_under("cpf-member", ".java") if "/src/main/" in p.as_posix()]
external_java = [p for p in files_under("cpf-external", ".java") if "/src/main/" in p.as_posix()]
check("GENERATOR_FIRST_DX", "GEN-NO-DIRECT-SYSTEM-TIME", not any_text(member_java + external_java, r"Instant\.now\(\)"), "generated retained domains use injected Clock")
generator_engine = text("cpf-tools/generator/engine/cpf_domain_generator.py")
generator_schema = text("cpf-tools/generator/contracts/cpf-domain.schema.json")
logical_binding = all(x in generator_engine and x in generator_schema for x in ["domainDependencies", "externalClients"])
check("GENERATOR_FIRST_DX", "GEN-LOGICAL-BINDINGS", logical_binding, "generator schema/model materializes domainDependencies + externalClients")
generated_domain_sources = member_java + external_java
typed_domain_consumer = any_text(generated_domain_sources, r"interface\s+\w+DomainClient\s+extends\s+CpfDomainClient") and any_text(generated_domain_sources, r"class\s+DomainDependencySampleService") and any_text(generated_domain_sources, r"CpfDomainClientRouter")
check("GENERATOR_FIRST_DX", "GEN-TYPED-DOMAIN-CONSUMER", typed_domain_consumer, "generated MBR/EXS typed Domain Client + adapter + actual consumer")

# 6) Open Extension / Native Escape
cardinality = text("cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfCapabilityBindingCardinality.java") + text("cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfCapabilityBindingRegistry.java")
check("OPEN_EXTENSION", "EXT-CARDINALITY", all(x in cardinality for x in ["SINGLE_DEFAULT_REQUIRED", "NAMED_MULTI_OPTIONAL_DEFAULT", "EXPLICIT_ONLY", "INTERNAL_NO_PUBLIC_BINDING"]), "capability-specific binding cardinality")
check("OPEN_EXTENSION", "EXT-PUBLIC-OBSERVABILITY-PORT", exists("cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/logging/CpfTransactionSegmentPort.java") and exists("cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/logging/CpfIntegrationLogPort.java"), "public observability ports replace cross-owner internal coupling")

# Canonical/hygiene
# 특정 비교 제품명 목록은 Repository Source에 보관하지 않는다. 외부명 검사는 Release sweep에서 입력값으로 수행한다.
stale_current = [p for p in files_under("cpf-docs/work/current") if re.search(r"(?:SESSION\d+|CHECKPOINT|12_0[234]|NEXT31|_REV\d|FINAL_FINAL)", p.name, re.I)]
check("DOCUMENT_GOVERNANCE", "DOC-NO-VERSIONED-CURRENT", not stale_current, f"version/session/checkpoint files in current={len(stale_current)}")
canonical_required = ["cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md", "cpf-docs/work/REQUIREMENT_STATUS.csv", "cpf-docs/work/OPEN_ISSUES.md", "cpf-docs/work/CPF_DELETE_MANIFEST.csv"]
check("DOCUMENT_GOVERNANCE", "DOC-CANONICAL-ENTRYPOINTS", all(exists(x) for x in canonical_required), "canonical current work/status/issues/delete manifest")

axis_status = {}
for axis in sorted({x["axis"] for x in checks}):
    subset = [x for x in checks if x["axis"] == axis]
    axis_status[axis] = "PASS" if all(x["status"] == "PASS" for x in subset) else "PARTIAL"
overall = "PASS" if all(v == "PASS" for v in axis_status.values()) else "PARTIAL"
base_sha = source_identity(ROOT)
result = {"schemaVersion":"1.0", "baselineSha":base_sha, "overall":overall, "axisStatus":axis_status, "checks":checks, "runtimeVerification":"NOT_EXECUTED_IN_JAVA25_ENVIRONMENT"}
EVIDENCE.parent.mkdir(parents=True, exist_ok=True)
EVIDENCE.write_text(json.dumps(result, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
print("CPF_BUSINESS_FRAMEWORK_CROSSCUT=" + overall)
for axis, status in axis_status.items(): print(f"{axis}={status}")
failed = [x for x in checks if x["status"] == "FAIL"]
for item in failed: print(f"FAIL {item['checkId']}: {item['detail']}")
sys.exit(0 if overall == "PASS" else 1)
