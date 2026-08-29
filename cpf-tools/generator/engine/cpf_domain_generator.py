#!/usr/bin/env python3
"""CPF Generated Customer Domain의 OS-neutral Canonical Generator Core.

이 모듈은 Domain metadata -> Source/DB/Test/Manifest 생성의 단일 소유자다.
PowerShell/Bash/Batch launcher는 이 Core만 호출하며 Java/SQL Template 로직을 소유하지 않는다.
"""
from __future__ import annotations

import dataclasses
import hashlib
import json
import os
import re
import shutil
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable

from cpf_korean_comment_policy import ensure_java_korean_contract_comments

SUPPORTED_VENDORS = ("oracle", "postgresql", "mariadb")
SCHEMA_REL = Path("cpf-tools/generator/contracts/cpf-domain.schema.json")
CATALOG_REL = Path("cpf-tools/generator/contracts/cpf-starter-catalog.json")
STACK_REL = Path("gradle/cpf-stack.properties")
GENERATOR_VERSION = "6.4.1"
GRADLE_DAEMON_JVMARGS = "-Xms250m -Xmx1000m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8"
MANAGED_GENERATOR_REL = Path("cpf-docs/work/evidence/generated/domain-generator")


def managed_generator_root(workspace_root: Path) -> Path:
    """Generator-owned mutable state and QA output root를 반환합니다.

    Product repository root에는 ``build/``를 만들지 않습니다. CI나 packaged CLI가 별도
    disposable volume을 요구하면 ``CPF_GENERATOR_WORK_ROOT``로 명시할 수 있고, 기본값은
    Source Identity와 Git 관리에서 제외된 repository managed-evidence owner입니다.
    """
    configured = os.environ.get("CPF_GENERATOR_WORK_ROOT", "").strip()
    if configured:
        return Path(configured).expanduser().resolve()
    return (workspace_root.resolve() / MANAGED_GENERATOR_REL).resolve()


def _generator_resource_root(workspace_root: Path) -> Path:
    """Generator 계약/Template의 읽기 전용 Resource Root를 결정합니다.

    Private Source 회귀검증에서는 workspace root 자체를 사용하고, Published Generator
    Binary는 CPF_GENERATOR_RESOURCE_ROOT 또는 PyInstaller의 embedded resource root를
    사용합니다. Public Developer Workspace가 Private cpf-tools Source를 포함할 필요가
    없도록 Workspace와 Generator Resource ownership을 분리합니다.
    """
    configured = os.environ.get("CPF_GENERATOR_RESOURCE_ROOT", "").strip()
    if configured:
        root = Path(configured).expanduser().resolve()
        if root.is_dir():
            return root
        raise DomainError(f"CPF_GENERATOR_RESOURCE_ROOT가 유효하지 않습니다: {root}")
    embedded = getattr(sys, "_MEIPASS", None)
    if embedded:
        candidate = Path(embedded).resolve() / "cpf-generator-resources"
        if candidate.is_dir():
            return candidate
    return workspace_root.resolve()


def _canonical_domain_root(workspace_root: Path, domain_name: str) -> Path:
    """Developer-Facing Generated Business Domain의 canonical source root를 반환합니다."""
    return workspace_root / f"cpf-{domain_name}"


def _workspace_contract_paths(workspace_root: Path) -> list[Path]:
    """Developer-Facing Gradle Domain 계약만 Workspace authority로 읽습니다."""
    contracts = []
    for path in workspace_root.glob("cpf-*/gradle.properties"):
        if path.is_file() and "cpf.domain.contractVersion=" in path.read_text(encoding="utf-8-sig"):
            contracts.append(path)
    return sorted(contracts, key=lambda p: p.parent.name)


class DomainError(RuntimeError):
    pass


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _scalar(value: str) -> Any:
    value = value.strip()
    if value in ("true", "True"): return True
    if value in ("false", "False"): return False
    if value in ("null", "Null", "NULL", "~"): return None
    if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'": return value[1:-1]
    if re.fullmatch(r"-?\d+", value): return int(value)
    return value


def load_yaml_subset(path: Path) -> dict[str, Any]:
    """외부 YAML Runtime 없이 CPF canonical subset(map/scalar/list[str])을 결정적으로 읽는다.

    Generated Domain dependency operation 목록을 지원하되 anchor/tag/multiline 같은 복잡한 YAML 기능은
    의도적으로 허용하지 않는다. 동일 Definition이 Public binary/Private source에서 같은 의미로 읽혀야 한다.
    """
    tokens: list[tuple[int, int, str]] = []
    for line_no, raw in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
        if not raw.strip() or raw.lstrip().startswith("#"):
            continue
        leading = raw[: len(raw) - len(raw.lstrip(" "))]
        if "\t" in leading:
            raise DomainError(f"tab indentation은 허용하지 않습니다: {path}:{line_no}")
        indent = len(leading)
        if indent % 2:
            raise DomainError(f"indent는 2칸 단위여야 합니다: {path}:{line_no}")
        text = raw.strip()
        if text.startswith(("&", "*", "!", "|", ">")):
            raise DomainError(f"지원하지 않는 YAML 기능입니다: {path}:{line_no}")
        tokens.append((indent, line_no, text))

    def parse_block(index: int, indent: int) -> tuple[Any, int]:
        if index >= len(tokens) or tokens[index][0] < indent:
            return {}, index
        is_list = tokens[index][0] == indent and tokens[index][2].startswith("- ")
        if is_list:
            values: list[Any] = []
            while index < len(tokens) and tokens[index][0] == indent:
                _, line_no, text = tokens[index]
                if not text.startswith("- "):
                    raise DomainError(f"list/map 혼합은 허용하지 않습니다: {path}:{line_no}")
                item = text[2:].strip()
                if not item:
                    raise DomainError(f"빈 list 항목은 허용하지 않습니다: {path}:{line_no}")
                values.append(_scalar(item))
                index += 1
                if index < len(tokens) and tokens[index][0] > indent:
                    raise DomainError(f"object list는 현재 Canonical subset에서 허용하지 않습니다: {path}:{tokens[index][1]}")
            return values, index

        result: dict[str, Any] = {}
        while index < len(tokens) and tokens[index][0] == indent:
            _, line_no, text = tokens[index]
            if text.startswith("- ") or ":" not in text:
                raise DomainError(f"key:value 형식이 아닙니다: {path}:{line_no}")
            key, value = text.split(":", 1)
            key = key.strip()
            if not re.fullmatch(r"[A-Za-z][A-Za-z0-9_-]*", key):
                raise DomainError(f"유효하지 않은 key입니다: {path}:{line_no}:{key}")
            if key in result:
                raise DomainError(f"중복 key입니다: {path}:{line_no}:{key}")
            index += 1
            if value.strip():
                result[key] = _scalar(value)
                if index < len(tokens) and tokens[index][0] > indent:
                    raise DomainError(f"scalar 아래 child block은 허용하지 않습니다: {path}:{tokens[index][1]}")
            elif index < len(tokens) and tokens[index][0] > indent:
                child_indent = tokens[index][0]
                if child_indent != indent + 2:
                    raise DomainError(f"indent 계층은 정확히 2칸씩 증가해야 합니다: {path}:{tokens[index][1]}")
                result[key], index = parse_block(index, child_indent)
            else:
                result[key] = {}
        return result, index

    if not tokens:
        return {}
    if tokens[0][0] != 0:
        raise DomainError(f"root indentation은 0이어야 합니다: {path}:{tokens[0][1]}")
    value, index = parse_block(0, 0)
    if index != len(tokens) or not isinstance(value, dict):
        raise DomainError(f"root는 object여야 합니다: {path}")
    return value

def _require_exact_keys(value: dict[str, Any], allowed: set[str], required: set[str], path: str) -> None:
    missing = sorted(required - set(value))
    unknown = sorted(set(value) - allowed)
    if missing: raise DomainError(f"{path} 필수 key 누락: {missing}")
    if unknown: raise DomainError(f"{path} 지원하지 않는 key: {unknown}")


@dataclasses.dataclass(frozen=True)
class DomainDependency:
    name: str
    system_code: str
    operations: tuple[str, ...]

    @property
    def class_name(self) -> str:
        return "".join(part[:1].upper() + part[1:] for part in re.split(r"[-_]", self.name) if part)


@dataclasses.dataclass(frozen=True)
class ExternalClientDefinition:
    name: str
    client_id: str
    capability: str

    @property
    def class_name(self) -> str:
        return "".join(part[:1].upper() + part[1:] for part in re.split(r"[-_]", self.name) if part)


@dataclasses.dataclass(frozen=True)
class DomainOperationContract:
    operation_id: str
    request_type: str
    response_type: str

    @property
    def method_name(self) -> str:
        parts=[x.lower() for x in re.split(r"[^A-Za-z0-9]+", self.operation_id) if x]
        if not parts: return "invoke"
        return parts[0] + "".join(x[:1].upper()+x[1:] for x in parts[1:])


def _resolve_java_type(package_name:str, imports:dict[str,str], token:str) -> str:
    token=token.strip()
    if "." in token: return token
    if token in imports: return imports[token]
    return f"{package_name}.{token}"


def discover_domain_operation_contracts(root: Path, target: "DomainDefinition") -> dict[str,DomainOperationContract]:
    """@CpfOnlineTransaction을 Operation ID authority로 사용하고 typed adapter를 contract binding으로 연결합니다."""
    target_root=root/f"cpf-{target.name}"
    if not target_root.is_dir(): return {}
    annotation_ids:set[str]=set()
    sources=[]
    tx_re=re.compile(r"@CpfOnlineTransaction\s*\((?P<body>.*?)\)",re.S)
    id_re=re.compile(r"\boperationId\s*=\s*\"([^\"]+)\"")
    canonical_java_root=target_root/"online"/"src"/"main"/"java"/target.package_path
    canonical_feature_roots=[canonical_java_root/feature for feature in target.business_features]
    for source in target_root.rglob("*.java"):
        # old <domain>/online/<domain> dual tree가 Delete Manifest 적용 전 남아 있어도
        # dependency discovery는 새 Canonical Business Feature tree만 authority로 사용합니다.
        if canonical_java_root.is_dir() and not any(feature_root in source.parents for feature_root in canonical_feature_roots):
            continue
        try: text=source.read_text(encoding="utf-8-sig",errors="ignore")
        except OSError: continue
        sources.append((source,text))
        for tx in tx_re.finditer(text):
            mid=id_re.search(tx.group("body"))
            if mid: annotation_ids.add(mid.group(1))
    typed:dict[str,DomainOperationContract]={}
    impl_re=re.compile(r"implements\s+CpfDomainOperation\s*<\s*([A-Za-z0-9_.$]+)\s*,\s*([A-Za-z0-9_.$]+)\s*>")
    op_re=re.compile(r"operationId\s*\(\s*\)\s*\{\s*return\s+\"([^\"]+)\"\s*;",re.S)
    package_re=re.compile(r"(?m)^package\s+([A-Za-z0-9_.]+)\s*;")
    import_re=re.compile(r"(?m)^import\s+([A-Za-z0-9_.$]+)\s*;")
    for source,text in sources:
        impl=impl_re.search(text); op=op_re.search(text)
        if not impl or not op: continue
        pkg=package_re.search(text); package_name=pkg.group(1) if pkg else ""
        imports={row.rsplit('.',1)[-1]:row for row in import_re.findall(text)}
        req=_resolve_java_type(package_name,imports,impl.group(1)); resp=_resolve_java_type(package_name,imports,impl.group(2))
        oid=op.group(1)
        if oid in typed: raise DomainError(f"Domain Operation contract duplicate: {target.system_code}/{oid}")
        typed[oid]=DomainOperationContract(oid,req,resp)
    missing=sorted(annotation_ids-set(typed))
    if missing: raise DomainError(f"@CpfOnlineTransaction typed Domain Operation binding 누락: {target.system_code}/{missing}")
    stale=sorted(set(typed)-annotation_ids-{'ping'})
    if stale: raise DomainError(f"Annotation owner가 없는 stale typed Domain Operation: {target.system_code}/{stale}")
    return {oid:contract for oid,contract in typed.items() if oid == 'ping' or oid in annotation_ids}


@dataclasses.dataclass(frozen=True)
class DomainDefinition:
    name: str
    module_name: str
    system_code: str
    package_name: str
    business_features: tuple[str, ...]
    database_role: str
    table_prefix: str
    preset: str
    online: bool
    batch: bool
    persistence: str
    http_client: bool
    resilience: bool
    cache: str
    messaging: str
    object_storage: str
    security_profile: str
    sample_transaction: bool
    generation_mode: str
    local_online_port: int
    domain_dependencies: tuple[DomainDependency, ...]
    external_clients: tuple[ExternalClientDefinition, ...]

    @property
    def class_name(self) -> str:
        return "".join(part[:1].upper() + part[1:] for part in re.split(r"[-_]", self.module_name) if part)

    @property
    def package_path(self) -> Path:
        return Path(*self.package_name.split("."))

    @property
    def online_project(self) -> str: return "online"
    @property
    def shared_domain(self) -> bool:
        # online/batch가 실제 공유 업무모델을 함께 사용할 때만 향후 별도 domain module을 승격한다. 기본 생성은 중복 Surface를 피한다.
        return False
    @property
    def api(self) -> bool:
        # 기존 renderer 내부 의미만 호환하고 외부 Metadata/Physical IA는 online으로 고정한다.
        return self.online
    @property
    def sample_tx_id(self) -> str: return f"{self.system_code}_SAMPLE_TX"

    @property
    def primary_feature(self) -> str:
        """실제 업무 Feature의 첫 항목입니다. Domain 이름을 Feature로 암묵 계산하지 않습니다."""
        return self.business_features[0]

    @property
    def feature_package(self) -> str:
        return f"{self.package_name}.{self.primary_feature}"

    @property
    def feature_path(self) -> Path:
        return self.package_path / self.primary_feature


def _reject_plaintext_secrets(value: Any, path: str = "$") -> None:
    """Generated Domain 입력에 평문 Secret/credential 항목이 들어오면 쓰기 전에 거부한다."""
    sensitive = re.compile(r"(?:password|passwd|secret|token|api[-_]?key|credential)", re.IGNORECASE)
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}"
            if sensitive.search(str(key)):
                raise DomainError(f"평문 Secret/credential 입력 금지: {child_path}; env/secret-manager reference를 사용하세요.")
            _reject_plaintext_secrets(child, child_path)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            _reject_plaintext_secrets(child, f"{path}[{index}]")


def _stable_local_online_port(system_code: str) -> int:
    """명시 Port가 없을 때 systemCode로 재현 가능한 Generated Online local Port를 계산합니다."""
    fingerprint = sum((index + 1) * ord(ch) for index, ch in enumerate(system_code))
    return 18080 + (fingerprint % 900)


def validate_definition(raw: dict[str, Any]) -> DomainDefinition:
    """Canonical cpf-domain.yaml을 검증하고 OS-neutral 내부 모델로 정규화한다.

    정본 구조는 domain/database/features/generation 영역으로 책임을 나눈다.
    Preset은 안전한 기본값을 제공하지만 custom의 명시적 선택을 막지 않는다.
    """
    _reject_plaintext_secrets(raw)
    allowed = {"domain","database","preset","modules","features","businessFeatures","generation","runtime","domainDependencies","externalClients"}
    required = {"domain","database","preset","modules","generation"}
    _require_exact_keys(raw, allowed, required, "$")
    domain = raw["domain"]; modules = raw["modules"]; database = raw["database"]
    features = raw.get("features", {}); business_features_raw = raw.get("businessFeatures", ["sample"]); generation = raw["generation"]; runtime = raw.get("runtime", {})
    domain_dependencies_raw = raw.get("domainDependencies", {}); external_clients_raw = raw.get("externalClients", {})
    if not isinstance(domain, dict): raise DomainError("$.domain은 object여야 합니다.")
    if not isinstance(modules, dict): raise DomainError("$.modules는 object여야 합니다.")
    if not isinstance(database, dict): raise DomainError("$.database는 object여야 합니다.")
    if not isinstance(features, dict): raise DomainError("$.features는 object여야 합니다.")
    if not isinstance(business_features_raw, list) or not business_features_raw: raise DomainError("$.businessFeatures는 1개 이상의 문자열 list여야 합니다.")
    if not isinstance(generation, dict): raise DomainError("$.generation은 object여야 합니다.")
    if not isinstance(runtime, dict): raise DomainError("$.runtime은 object여야 합니다.")
    if not isinstance(domain_dependencies_raw, dict): raise DomainError("$.domainDependencies는 object여야 합니다.")
    if not isinstance(external_clients_raw, dict): raise DomainError("$.externalClients는 object여야 합니다.")
    _require_exact_keys(domain, {"name","systemCode","packageName"}, {"name","systemCode"}, "$.domain")
    _require_exact_keys(modules, {"online","batch"}, {"online"}, "$.modules")
    _require_exact_keys(database, {"role","tablePrefix"}, {"role","tablePrefix"}, "$.database")
    _require_exact_keys(features, {"persistence","httpClient","resilience","cache","messaging","objectStorage","securityProfile"}, set(), "$.features")
    _require_exact_keys(generation, {"sampleTransaction","mode"}, {"sampleTransaction"}, "$.generation")
    _require_exact_keys(runtime, {"localOnlinePort"}, set(), "$.runtime")
    name = str(domain["name"]); module = name; system = str(domain["systemCode"]); package = str(domain.get("packageName") or name); prefix = str(database["tablePrefix"])
    if not re.fullmatch(r"[a-z][a-z0-9-]{1,49}", name): raise DomainError("domain.name 형식이 올바르지 않습니다.")
    if not re.fullmatch(r"[A-Z][A-Z0-9]{2}", system): raise DomainError("domain.systemCode는 정확히 3자리 대문자/숫자여야 합니다.")
    if not re.fullmatch(r"[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)*", package): raise DomainError("domain.packageName 형식이 올바르지 않습니다.")
    business_features: list[str] = []
    for raw_feature in business_features_raw:
        feature = str(raw_feature).strip()
        if not re.fullmatch(r"[a-z][a-z0-9_]{1,49}", feature): raise DomainError(f"businessFeatures 형식 오류: {feature}")
        if feature == name.replace("-", ""): raise DomainError("Business Feature는 Domain 이름과 같은 값을 사용할 수 없습니다. 미지정 scaffold는 sample을 사용하세요.")
        if feature in {"online","batch","base","controller","service","repository","client","dto","model","operation"}: raise DomainError(f"Business Feature 예약어 사용 금지: {feature}")
        if feature in business_features: raise DomainError(f"businessFeatures 중복: {feature}")
        business_features.append(feature)
    if str(generation.get("mode","generated")) != "prebuilt" and (package == "com.cpf" or package.startswith("com.cpf.")): raise DomainError("Generated Customer Domain은 com.cpf.* namespace를 소유할 수 없습니다.")
    if database["role"] != "CUSTOMER_BUSINESS_DB": raise DomainError("database.role은 CUSTOMER_BUSINESS_DB여야 합니다.")
    if not re.fullmatch(r"[A-Z][A-Z0-9_]{1,19}", prefix): raise DomainError("database.tablePrefix는 대문자로 시작하는 2~20자리여야 합니다.")
    preset = str(raw["preset"])
    if preset not in {"minimal","standard-enterprise","full-enterprise","custom"}: raise DomainError(f"지원하지 않는 preset: {preset}")
    if not isinstance(modules["online"], bool): raise DomainError("modules.online은 boolean이어야 합니다.")
    online = modules["online"]
    batch = modules.get("batch", False)
    if not isinstance(batch, bool): raise DomainError("modules.batch는 boolean이어야 합니다.")
    if not online and not batch: raise DomainError("Generated Domain은 online/batch 중 최소 하나의 Runtime을 선택해야 합니다.")

    preset_defaults = {
      "minimal": {"persistence":"none","httpClient":False,"resilience":False,"cache":"none","messaging":"none","objectStorage":"none","securityProfile":"resource-server"},
      "standard-enterprise": {"persistence":"mybatis","httpClient":True,"resilience":True,"cache":"none","messaging":"none","objectStorage":"none","securityProfile":"resource-server"},
      "full-enterprise": {"persistence":"mybatis","httpClient":True,"resilience":True,"cache":"valkey","messaging":"kafka","objectStorage":"s3","securityProfile":"resource-server"},
      "custom": {"persistence":"none","httpClient":False,"resilience":False,"cache":"none","messaging":"none","objectStorage":"none","securityProfile":"resource-server"},
    }[preset]
    f={**preset_defaults, **features}
    persistence=str(f["persistence"]); cache=str(f["cache"]); messaging=str(f["messaging"]); object_storage=str(f["objectStorage"]); security_profile=str(f["securityProfile"])
    if persistence not in {"none","jdbc","mybatis","jpa"}: raise DomainError(f"지원하지 않는 persistence: {persistence}")
    if cache not in {"none","caffeine","redis","valkey"}: raise DomainError(f"지원하지 않는 cache: {cache}")
    if messaging not in {"none","kafka","rabbitmq","jms","ibm-mq"}: raise DomainError(f"지원하지 않는 messaging: {messaging}")
    if object_storage not in {"none","s3"}: raise DomainError(f"지원하지 않는 objectStorage: {object_storage}")
    if security_profile not in {"resource-server","browser-session-valkey","service-identity","oidc"}: raise DomainError(f"지원하지 않는 securityProfile: {security_profile}")
    http_client=f["httpClient"]; resilience=f["resilience"]; sample_tx=generation["sampleTransaction"]; generation_mode=str(generation.get("mode","generated"))
    if not isinstance(http_client,bool) or not isinstance(resilience,bool): raise DomainError("features.httpClient/resilience는 boolean이어야 합니다.")
    if not isinstance(sample_tx,bool): raise DomainError("generation.sampleTransaction은 boolean이어야 합니다.")
    if generation_mode not in {"generated","prebuilt"}: raise DomainError("generation.mode는 generated/prebuilt만 허용합니다.")
    if generation_mode == "prebuilt" and sample_tx:
        raise DomainError("generation.mode=prebuilt에서는 sampleTransaction=false여야 합니다.")
    if generation_mode == "generated" and preset in {"standard-enterprise","full-enterprise"}:
        if persistence != "mybatis" or not http_client or not resilience or not sample_tx:
            raise DomainError(f"{preset}는 mybatis/httpClient/resilience/sampleTransaction=true Golden Path를 유지해야 합니다.")
    if generation_mode == "generated" and preset == "minimal":
        minimal_expected = {
          "persistence":"none","httpClient":False,"resilience":False,"cache":"none",
          "messaging":"none","objectStorage":"none","securityProfile":"resource-server"
        }
        selected = {
          "persistence":persistence,"httpClient":http_client,"resilience":resilience,"cache":cache,
          "messaging":messaging,"objectStorage":object_storage,"securityProfile":security_profile
        }
        if selected != minimal_expected or sample_tx:
            raise DomainError(
                "preset=minimal은 실제 최소 Surface만 허용합니다. "
                f"현재 선택={selected}, sampleTransaction={sample_tx}; "
                "권장 조합=features 생략 또는 none/false + securityProfile=resource-server + generation.sampleTransaction=false; "
                "수정 경로=$.features, $.generation.sampleTransaction"
            )
    if generation_mode == "generated" and sample_tx and not online:
        raise DomainError("generation.sampleTransaction=true는 modules.online=true가 필요합니다.")
    if generation_mode == "generated" and sample_tx and persistence != "mybatis":
        raise DomainError("현재 Canonical Sample Transaction은 중앙 Vendor Mapper Pack을 소비하므로 persistence=mybatis가 필요합니다.")
    if persistence == "none" and (cache not in {"none","caffeine"} or messaging != "none" or object_storage != "none"):
        pass  # DB와 독립적인 Capability는 허용합니다.
    domain_dependencies: list[DomainDependency] = []
    seen_dependency_codes: set[str] = set()
    for dependency_name, dependency_raw in domain_dependencies_raw.items():
        if not re.fullmatch(r"[a-z][a-z0-9-]{1,49}", str(dependency_name)):
            raise DomainError(f"domainDependencies key 형식이 올바르지 않습니다: {dependency_name}")
        if not isinstance(dependency_raw, dict): raise DomainError(f"domainDependencies.{dependency_name}은 object여야 합니다.")
        _require_exact_keys(dependency_raw, {"systemCode","operations"}, {"systemCode","operations"}, f"$.domainDependencies.{dependency_name}")
        dependency_code = str(dependency_raw["systemCode"]).upper()
        if not re.fullmatch(r"[A-Z][A-Z0-9]{2}", dependency_code): raise DomainError(f"domainDependencies.{dependency_name}.systemCode는 3자리 대문자/숫자여야 합니다.")
        if dependency_code == system: raise DomainError("자기 Domain을 domainDependencies에 선언할 수 없습니다.")
        if dependency_code in seen_dependency_codes: raise DomainError(f"domainDependencies systemCode 중복: {dependency_code}")
        operations_raw = dependency_raw.get("operations")
        if not isinstance(operations_raw, list) or not operations_raw:
            raise DomainError(f"domainDependencies.{dependency_name}.operations는 1개 이상의 operationId list여야 합니다.")
        operations: list[str] = []
        for operation in operations_raw:
            operation_id = str(operation).strip()
            if not re.fullmatch(r"[A-Za-z][A-Za-z0-9_.:-]{1,119}", operation_id):
                raise DomainError(f"domainDependencies.{dependency_name}.operations operationId 형식 오류: {operation_id}")
            if operation_id in operations:
                raise DomainError(f"domainDependencies.{dependency_name}.operations 중복: {operation_id}")
            operations.append(operation_id)
        seen_dependency_codes.add(dependency_code)
        domain_dependencies.append(DomainDependency(str(dependency_name), dependency_code, tuple(operations)))

    external_clients: list[ExternalClientDefinition] = []
    seen_external_ids: set[str] = set()
    for client_name, client_raw in external_clients_raw.items():
        if not re.fullmatch(r"[a-z][a-z0-9-]{1,49}", str(client_name)):
            raise DomainError(f"externalClients key 형식이 올바르지 않습니다: {client_name}")
        if not isinstance(client_raw, dict): raise DomainError(f"externalClients.{client_name}은 object여야 합니다.")
        _require_exact_keys(client_raw, {"id","capability"}, {"id","capability"}, f"$.externalClients.{client_name}")
        client_id = str(client_raw["id"]); capability = str(client_raw["capability"])
        if not re.fullmatch(r"[a-z][a-z0-9-]{1,63}", client_id): raise DomainError(f"externalClients.{client_name}.id 형식이 올바르지 않습니다.")
        public_external_capabilities = {"http","fixed-length","messaging","object-storage"}
        if capability not in public_external_capabilities:
            raise DomainError(f"Public Generated Domain에서 지원하지 않는 external client capability: {capability}; public={sorted(public_external_capabilities)}")
        if client_id in seen_external_ids: raise DomainError(f"externalClients id 중복: {client_id}")
        seen_external_ids.add(client_id)
        external_clients.append(ExternalClientDefinition(str(client_name), client_id, capability))

    for client in external_clients:
        if client.capability == "messaging" and messaging == "none":
            raise DomainError(f"externalClients.{client.name}=messaging은 features.messaging provider 선택이 필요합니다.")
        if client.capability == "object-storage" and object_storage == "none":
            raise DomainError(f"externalClients.{client.name}=object-storage는 features.objectStorage=s3가 필요합니다.")

    default_online_port = _stable_local_online_port(system)
    local_online_port = int(runtime.get("localOnlinePort", default_online_port))
    if not 18080 <= local_online_port <= 18999: raise DomainError(f"runtime.localOnlinePort는 18080~18999 범위여야 합니다: {local_online_port}")
    return DomainDefinition(name,module,system,package,tuple(business_features),"CUSTOMER_BUSINESS_DB",prefix,preset,online,batch,persistence,http_client,resilience,cache,messaging,object_storage,security_profile,sample_tx,generation_mode,local_online_port,tuple(domain_dependencies),tuple(external_clients))


def load_domain_gradle_contract(path: Path) -> DomainDefinition:
    """Generated Root의 실제 Build 입력인 gradle.properties에서 Developer Domain 계약을 읽습니다."""
    values: dict[str,str] = {}
    for raw_line in path.read_text(encoding="utf-8-sig").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key,value=line.split("=",1)
        values[key.strip()]=value.strip()
    required = {
        "cpf.domain.contractVersion","cpf.domain.name","cpf.domain.systemCode","cpf.domain.packageName",
        "cpf.domain.tablePrefix","cpf.domain.preset","cpf.domain.online","cpf.domain.batch",
        "cpf.domain.businessFeatures","cpf.domain.persistence","cpf.domain.httpClient","cpf.domain.resilience",
        "cpf.domain.cache","cpf.domain.messaging","cpf.domain.objectStorage","cpf.domain.securityProfile",
        "cpf.domain.sampleTransaction","cpf.domain.generationMode","cpf.domain.localOnlinePort",
        "cpf.domain.dependencies","cpf.domain.externalClients",
    }
    missing=sorted(required-set(values))
    if missing:
        raise DomainError(f"Generated Domain Gradle 계약 누락: {path}:{missing}")
    if values["cpf.domain.contractVersion"] != "1":
        raise DomainError(f"지원하지 않는 Generated Domain Gradle 계약입니다: {path}")
    def boolean(key: str) -> bool:
        value=values[key].lower()
        if value not in {"true","false"}: raise DomainError(f"Generated Domain boolean 계약 오류: {path}:{key}")
        return value == "true"
    dependencies: dict[str,dict[str,Any]] = {}
    for row in filter(None,values["cpf.domain.dependencies"].split(";")):
        parts=row.split(":",2)
        if len(parts)!=3: raise DomainError(f"Generated Domain dependency 계약 오류: {path}:{row}")
        name,system,operations=parts
        dependencies[name]={"systemCode":system,"operations":[x for x in operations.split(",") if x]}
    external_clients: dict[str,dict[str,str]] = {}
    for row in filter(None,values["cpf.domain.externalClients"].split(";")):
        parts=row.split(":",2)
        if len(parts)!=3: raise DomainError(f"Generated External Client 계약 오류: {path}:{row}")
        name,client_id,capability=parts
        external_clients[name]={"id":client_id,"capability":capability}
    raw: dict[str,Any] = {
        "domain":{"name":values["cpf.domain.name"],"systemCode":values["cpf.domain.systemCode"],"packageName":values["cpf.domain.packageName"]},
        "database":{"role":"CUSTOMER_BUSINESS_DB","tablePrefix":values["cpf.domain.tablePrefix"]},
        "preset":values["cpf.domain.preset"],
        "modules":{"online":boolean("cpf.domain.online"),"batch":boolean("cpf.domain.batch")},
        "businessFeatures":[x for x in values["cpf.domain.businessFeatures"].split(",") if x],
        "features":{
            "persistence":values["cpf.domain.persistence"],"httpClient":boolean("cpf.domain.httpClient"),
            "resilience":boolean("cpf.domain.resilience"),"cache":values["cpf.domain.cache"],
            "messaging":values["cpf.domain.messaging"],"objectStorage":values["cpf.domain.objectStorage"],
            "securityProfile":values["cpf.domain.securityProfile"],
        },
        "generation":{"sampleTransaction":boolean("cpf.domain.sampleTransaction"),"mode":values["cpf.domain.generationMode"]},
        "runtime":{"localOnlinePort":int(values["cpf.domain.localOnlinePort"])},
        "domainDependencies":dependencies,
        "externalClients":external_clients,
    }
    return validate_definition(raw)


def load_domain_contract(path: Path) -> DomainDefinition:
    return load_domain_gradle_contract(path) if path.name == "gradle.properties" else validate_definition(load_yaml_subset(path))

def _load_workspace_definitions(root: Path, current: DomainDefinition | None = None) -> dict[str,DomainDefinition]:
    definitions: dict[str,DomainDefinition] = {}
    for definition in _workspace_contract_paths(root):
        try:
            item=load_domain_contract(definition)
        except DomainError as exc:
            raise DomainError(f"기존 Generated Domain 정의가 유효하지 않습니다: {definition}: {exc}") from exc
        if item.name in definitions:
            raise DomainError(f"Generated Domain definition 이름 중복: {item.name}")
        definitions[item.name]=item
    if current is not None:
        definitions[current.name]=current
    return definitions


def validate_domain_dependency_graph(root: Path, d: DomainDefinition) -> None:
    """Declared Business Domain dependency의 target/systemCode/cycle을 생성 전에 검증합니다."""
    definitions=_load_workspace_definitions(root,d)
    for owner in definitions.values():
        for dep in owner.domain_dependencies:
            target=definitions.get(dep.name)
            if target is None:
                raise DomainError(f"{owner.system_code} dependency requires {dep.system_code}/{dep.name}, but target Domain definition is missing")
            if target.system_code != dep.system_code:
                raise DomainError(f"{owner.system_code} dependency target SystemCode mismatch: declared={dep.system_code} actual={target.system_code} domain={dep.name}")
            contracts = discover_domain_operation_contracts(root,target)
            available = set(contracts)
            missing_operations = sorted(set(dep.operations) - available)
            if missing_operations:
                raise DomainError(f"{owner.system_code} dependency target operation 없음/미발견: {dep.system_code}/{dep.name} operations={missing_operations}")
    graph={name:[dep.name for dep in item.domain_dependencies] for name,item in definitions.items()}
    visiting:set[str]=set(); visited:set[str]=set(); path:list[str]=[]
    def visit(node:str)->None:
        if node in visiting:
            start=path.index(node) if node in path else 0
            cycle=path[start:]+[node]
            raise DomainError(f"Business Domain 순환 의존 금지: {' -> '.join(cycle)}")
        if node in visited: return
        visiting.add(node); path.append(node)
        for target in graph.get(node,[]): visit(target)
        path.pop(); visiting.remove(node); visited.add(node)
    for node in sorted(graph): visit(node)


def validate_repository_uniqueness(root: Path, d: DomainDefinition, output: Path) -> None:
    """영구 Project metadata 없이 Framework canonical definitions 사이의 식별자 충돌을 생성 전에 차단한다."""
    if not root.is_dir(): return
    attrs=(("domain.name","name"),("domain.systemCode","system_code"),("domain.packageName(derived)","package_name"),("database.tablePrefix","table_prefix"))
    requested_ports={d.local_online_port} if d.online else set()
    candidates=_workspace_contract_paths(root)
    for definition in candidates:
        try: other=load_domain_contract(definition)
        except DomainError as exc: raise DomainError(f"기존 Generated Domain 정의가 유효하지 않습니다: {definition}: {exc}") from exc
        if other.name==d.name: continue
        other_ports={other.local_online_port} if other.online else set()
        collision=sorted(requested_ports & other_ports)
        if collision: raise DomainError(f"Generated Domain local port 충돌: {collision}; existing={definition}")
        for label,attr in attrs:
            if getattr(other,attr) == getattr(d,attr):
                raise DomainError(f"Generated Domain uniqueness 충돌: {label}={getattr(d,attr)}; existing={definition}")
    validate_domain_dependency_graph(root,d)


def read_stack(root: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    resource_root = _generator_resource_root(root)
    path = root / STACK_REL
    if not path.is_file():
        path = resource_root / STACK_REL
    if not path.is_file():
        return {"javaVersion":"25","springBootVersion":"4.1.0","springDependencyManagementVersion":"1.1.7","cpfVersion":"1.0.0-SNAPSHOT"}
    for raw in path.read_text(encoding="utf-8-sig").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line: continue
        k,v = line.split("=",1); values[k.strip()] = v.strip()
    values.setdefault("cpfVersion", values.get("platformVersion", "1.0.0-SNAPSHOT"))
    return values


def load_catalog(root: Path) -> dict[str, Any]:
    path = _generator_resource_root(root) / CATALOG_REL
    if not path.is_file():
        raise DomainError(f"Starter Catalog가 없습니다: {path}")
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    modules = data.get("modules", [])
    ids = [str(m.get("artifactId")) for m in modules]
    dup = sorted({x for x in ids if ids.count(x) > 1})
    if dup: raise DomainError(f"Starter Catalog artifactId duplicate: {dup}")
    profiles = data.get("profiles")
    providers = data.get("providerCatalog")
    if not isinstance(profiles, dict) or not profiles:
        raise DomainError("Canonical Starter Catalog의 profiles가 비어 있거나 object가 아닙니다.")
    if not isinstance(providers, dict) or not providers:
        raise DomainError("Canonical Starter Catalog의 providerCatalog가 비어 있거나 object가 아닙니다.")
    declared_profiles = set(str(x) for x in data.get("publicProfiles", []))
    if declared_profiles != set(str(x) for x in profiles):
        raise DomainError(f"publicProfiles/profiles 불일치: declared={sorted(declared_profiles)}, definitions={sorted(profiles)}")
    return data


def public_module_map(catalog: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {str(m["artifactId"]): m for m in catalog.get("modules", []) if m.get("visibility") == "public"}


def direct_dependencies(d: DomainDefinition, kind: str, catalog: dict[str, Any]) -> list[str]:
    public = public_module_map(catalog)
    deps = ["cpf-starter"]
    if kind in {"api","online"}: deps.append("cpf-starter-secure-api")
    elif kind == "batch": deps.append("cpf-starter-batch")
    else: raise DomainError(f"지원하지 않는 Generated Domain module: {kind}")
    persistence_map = {"jdbc":"cpf-starter-data-jdbc","mybatis":"cpf-starter-data-mybatis","jpa":"cpf-starter-data-jpa"}
    if d.persistence != "none": deps.append(persistence_map[d.persistence])
    cache_map = {"caffeine":"cpf-starter-cache-caffeine","redis":"cpf-starter-cache-redis","valkey":"cpf-starter-cache-valkey"}
    if d.cache != "none": deps.append(cache_map[d.cache])
    messaging_map = {"kafka":"cpf-starter-messaging-kafka","rabbitmq":"cpf-starter-messaging-rabbitmq","jms":"cpf-starter-messaging-jms","ibm-mq":"cpf-starter-messaging-ibm-mq"}
    if d.messaging != "none": deps.append(messaging_map[d.messaging])
    if d.object_storage == "s3": deps.append("cpf-starter-object-storage-s3")
    if d.security_profile == "browser-session-valkey": deps.append("cpf-starter-session-valkey")
    elif d.security_profile == "oidc": deps.append("cpf-starter-oidc")
    elif d.security_profile == "service-identity":
        # service-identity가 Internal-only이면 Generated Domain이 직접 뚫지 않고 Public Profile Composition에 맡긴다.
        if "cpf-starter-security-service-identity" in public: deps.append("cpf-starter-security-service-identity")
    if d.http_client or d.domain_dependencies or any(client.capability == "http" for client in d.external_clients):
        deps.append("cpf-starter-integration-http")
    if d.resilience:
        deps.append("cpf-starter-integration-resilience")
    if any(client.capability == "fixed-length" for client in d.external_clients): deps.append("cpf-starter-integration-fixed-length")
    for artifact in deps:
        if artifact not in public:
            raise DomainError(f"Generated Domain direct dependency가 Public Starter Catalog에 없습니다: {artifact}")
    return list(dict.fromkeys(deps))


def _gradle_dependency_lines(artifacts: Iterable[str]) -> str:
    return "\n".join(f'    implementation "com.cpf.starter:{a}:${{cpfPlatformVersion}}"' for a in artifacts)


def _developer_selection_summary(d: DomainDefinition, catalog: dict[str, Any]) -> dict[str, Any]:
    """Canonical Public Catalog를 이용해 신규 개발자가 읽을 수 있는 선택 결과를 제공한다."""
    public = public_module_map(catalog)
    artifacts = direct_dependencies(d, "api", catalog)
    selected = []
    for artifact in artifacts:
        module = public[artifact]
        selected.append({
          "artifactId": artifact,
          "displayNameKo": module.get("displayNameKo", artifact),
          "selectionGroup": module.get("selectionGroup", module.get("ownerGroup")),
          "usageHintKo": module.get("usageHintKo"),
          "ownerGroup": module.get("ownerGroup"),
          "role": module.get("role"),
          "configPrefix": module.get("configPrefix"),
          "runtimeRequired": bool(module.get("runtimeRequired", False)),
        })
    return {
      "purpose": {
        "onlineApi": True,
        "database": d.persistence != "none",
        "cache": d.cache != "none",
        "messaging": d.messaging != "none",
        "objectStorage": d.object_storage != "none",
        "externalHttp": d.http_client,
        "resilience": d.resilience,
        "securityProfile": d.security_profile,
        "batch": d.batch,
      },
      "runtime": ["online"] + (["batch"] if d.batch else []),
      "batchCapability": {"selected": d.batch, "selectionOwner": "DOMAIN_DEFINITION", "generatedByDomainGenerator": True},
      "generatedSamples": {"sampleTransaction": d.sample_transaction},
      "publicArtifacts": selected,
      "internalArtifactsDirectlyExposed": [],
    }


def render_root_settings(d: DomainDefinition, dependency_targets: Iterable[DomainDefinition] = ()) -> str:
    includes=(["online"] if d.online else []) + (["batch"] if d.batch else [])
    lines=[
      "// Generated Customer Domain 최소 IA settings입니다.",
      "def cpfDomainName = providers.gradleProperty('cpf.domain.name').get()",
      "rootProject.name = \"cpf-${cpfDomainName}\"",
      "if (rootDir.name != rootProject.name) {",
      "    throw new GradleException(\"Generated Domain Root/name 불일치: root=${rootDir.name} contract=${rootProject.name}\")",
      "}",
      "",
      "def cpfManagedGradleBase = providers.gradleProperty('cpfManagedGradleRoot')",
      "        .orElse(providers.environmentVariable('CPF_MANAGED_GRADLE_ROOT'))",
      "        .orElse(new File(gradle.gradleUserHomeDir, 'cpf/work/gradle').absolutePath).get()",
      "def cpfManagedGradleRoot = new File(cpfManagedGradleBase, rootProject.name)",
      "gradle.startParameter.projectCacheDir = new File(cpfManagedGradleRoot, 'project-cache')",
    ]
    for name in includes: lines.append(f"include '{name}'")
    lines += [
      "",
      "// CPF Product Source와 함께 하는 회귀검증에서만 명시적으로 Composite Build를 연결합니다.",
      "// 고객 Release Build는 이 속성을 지정하지 않고 배포된 CPF Artifact를 소비합니다.",
      "def cpfProductCompositeRoot = providers.gradleProperty('cpfProductCompositeRoot').orNull",
      "def cpfDomainWorkspaceRoot = cpfProductCompositeRoot ? file(cpfProductCompositeRoot).canonicalFile : rootDir.parentFile.canonicalFile",
    ]
    for target in dependency_targets:
        lines += [
          f"def cpfDependencyRoot{target.class_name} = new File(cpfDomainWorkspaceRoot, 'cpf-{target.name}').canonicalFile",
          f"def cpfDependencyContract{target.class_name} = new File(cpfDependencyRoot{target.class_name}, 'gradle.properties')",
          f"if (!cpfDependencyContract{target.class_name}.isFile() || !new File(cpfDependencyRoot{target.class_name}, 'settings.gradle').isFile()) {{",
          f"    throw new GradleException('Declared Domain dependency project is missing: {target.system_code}/{target.name} -> ' + cpfDependencyRoot{target.class_name})",
          "}",
          f"includeBuild(cpfDependencyRoot{target.class_name}) {{",
          f"    name = 'cpf-domain-{target.name}'",
          "    dependencySubstitution {",
          f"        substitute module('{target.package_name}:online') using project(':online')",
        ]
        if target.batch:
            lines.append(f"        substitute module('{target.package_name}:batch') using project(':batch')")
        lines += ["    }", "}"]
    lines += [
      "if (cpfProductCompositeRoot) {",
      "    def productRoot = file(cpfProductCompositeRoot).canonicalFile",
      "    if (!new File(productRoot, 'settings.gradle').isFile()) {",
      "        throw new GradleException(\"CPF Product composite root가 유효하지 않습니다: ${productRoot}\")",
      "    }",
      "    def starterCatalogFile = new File(productRoot, 'cpf-tools/generator/contracts/cpf-starter-catalog.json')",
      "    if (!starterCatalogFile.isFile()) {",
      "        throw new GradleException(\"CPF Public Starter catalog가 없습니다: ${starterCatalogFile}\")",
      "    }",
      "    def starterCatalog = new groovy.json.JsonSlurper().parse(starterCatalogFile)",
      "    if (!(starterCatalog.modules instanceof List)) {",
      "        throw new GradleException(\"CPF Public Starter catalog modules가 유효하지 않습니다: ${starterCatalogFile}\")",
      "    }",
      "    def publicStarterModules = starterCatalog.modules.findAll { row ->",
      "        row instanceof Map && row.visibility?.toString() == 'public'",
      "    }",
      "    if (publicStarterModules.isEmpty()) {",
      "        throw new GradleException(\"CPF Public Starter catalog에 public module이 없습니다: ${starterCatalogFile}\")",
      "    }",
      "    publicStarterModules.each { row ->",
      "        if (!(row.groupId instanceof String) || !(row.artifactId instanceof String) || !(row.projectPath instanceof String)) {",
      "            throw new GradleException(\"CPF Public Starter catalog 좌표가 유효하지 않습니다: ${row}\")",
      "        }",
      "    }",
      "    includeBuild(productRoot) {",
      "        name = 'cpf-product-source'",
      "        dependencySubstitution {",
      "            publicStarterModules.each { starter ->",
      "                substitute module(\"${starter.groupId}:${starter.artifactId}\") using project(starter.projectPath.toString())",
      "            }",
      "        }",
      "    }",
      "}",
    ]
    lines += [
      "",
      "// 고객사 공통 Library는 필요한 Domain만 명시적으로 선택합니다. cpf library sync가 이 파일을 생성합니다.",
      "def cpfCustomerLibrarySettings = new File(settingsDir, 'customer-library-settings.gradle')",
      "if (cpfCustomerLibrarySettings.isFile()) { apply from: cpfCustomerLibrarySettings }",
    ]
    return "\n".join(lines)+"\n"


def render_root_build(d: DomainDefinition, stack: dict[str,str]) -> str:
    java = stack.get("javaVersion","25")
    return rf'''// CPF Generator가 생성한 Domain 공통 Gradle 설정입니다.
import groovy.json.JsonOutput
import java.security.MessageDigest

plugins {{ id 'base' }}

def cpfResolvedPlatformVersion = providers.environmentVariable('CPF_VERSION')
        .orElse(providers.gradleProperty('cpfPlatformVersion'))
        .get()
def cpfProductCompositeRootValue = providers.gradleProperty('cpfProductCompositeRoot').orNull
def cpfPublicRepositoryUrl = providers.environmentVariable('CPF_MAVEN_REPOSITORY_URL')
        .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL'))
        .orElse(providers.gradleProperty('cpfArtifactRepositoryUrl'))
        .orNull
def cpfDomainName = providers.gradleProperty('cpf.domain.name').get()
def cpfDomainSystemCode = providers.gradleProperty('cpf.domain.systemCode').get()
def cpfDomainPackageName = providers.gradleProperty('cpf.domain.packageName').get()
if (cpfDomainName != '{d.name}' || cpfDomainSystemCode != '{d.system_code}' || cpfDomainPackageName != '{d.package_name}') {{
    throw new GradleException('Generated Domain 계약과 생성된 Build가 일치하지 않습니다. cpf domain sync를 실행하세요.')
}}
if (!cpfProductCompositeRootValue && !cpfPublicRepositoryUrl) {{
    throw new GradleException('Standalone Generated Domain은 CPF_MAVEN_REPOSITORY_URL 또는 cpfArtifactRepositoryUrl이 필요합니다. Private Source composite 회귀검증은 -PcpfProductCompositeRoot를 사용하세요.')
}}

allprojects {{
    group = cpfDomainPackageName
    version = '1.0.0-SNAPSHOT'
    ext.set('cpfPlatformVersion', cpfResolvedPlatformVersion)
    repositories {{
        if (cpfPublicRepositoryUrl) {{
            maven {{
                name = 'cpfPublicBinary'
                url = uri(cpfPublicRepositoryUrl)
                content {{ includeGroupByRegex 'com[.]cpf([.].*)?' }}
                def cpfRepoUser = System.getenv('CPF_MAVEN_REPOSITORY_USER') ?: System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfRepoUser) {{
                    credentials {{
                        username = cpfRepoUser
                        password = System.getenv('CPF_MAVEN_REPOSITORY_PASSWORD') ?: System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                    }}
                }}
            }}
        }}
        mavenCentral {{ content {{ excludeGroupByRegex 'com[.]cpf([.].*)?' }} }}
    }}
}}

def cpfTextAttribute = {{ String body, String key ->
    def matcher = body =~ /(?s)(?:^|,)\s*${{java.util.regex.Pattern.quote(key)}}\s*=\s*"((?:\\.|[^"])*)"/
    matcher.find() ? matcher.group(1).replace('\\"','"').replace('\\\\','\\') : null
}}
def cpfFirstString = {{ String body ->
    if (body == null) return ''
    def matcher = body =~ /"((?:\\.|[^"])*)"/
    matcher.find() ? matcher.group(1).replace('\\"','"').replace('\\\\','\\') : ''
}}
def cpfJoinPath = {{ String base, String leaf ->
    def left = (base ?: '').trim()
    def right = (leaf ?: '').trim()
    def joined = "${{left}}/${{right}}".replaceAll('/+', '/')
    if (!joined.startsWith('/')) joined = '/' + joined
    joined.length() > 1 && joined.endsWith('/') ? joined.substring(0, joined.length()-1) : joined
}}
def cpfSha256 = {{ String value ->
    MessageDigest.getInstance('SHA-256').digest(value.getBytes('UTF-8')).encodeHex().toString()
}}

subprojects {{
    plugins.withId('java') {{
        java {{ toolchain {{ languageVersion = JavaLanguageVersion.of({java}) }} }}
        tasks.withType(Test).configureEach {{ useJUnitPlatform() }}

        // Runtime discovery descriptor는 업무 Source가 아니라 Build 산출물로만 생성합니다.
        def generatedDomainDescriptorRoot = layout.buildDirectory.dir('generated/cpf-runtime-descriptor')
        def generatedDomainDescriptor = generatedDomainDescriptorRoot.map {{ it.file('META-INF/cpf/generated-domain.properties') }}
        tasks.register('generateCpfDomainRuntimeDescriptor') {{
            inputs.property('domain', cpfDomainName)
            inputs.property('systemCode', cpfDomainSystemCode)
            inputs.property('kind', project.name)
            inputs.property('scanPackage', cpfDomainPackageName)
            outputs.file(generatedDomainDescriptor)
            doLast {{
                def target = generatedDomainDescriptor.get().asFile
                target.parentFile.mkdirs()
                target.setText("domain=${{cpfDomainName}}\nsystemCode=${{cpfDomainSystemCode}}\nkind=${{project.name}}\nscanPackage=${{cpfDomainPackageName}}\n", 'UTF-8')
            }}
        }}
        sourceSets.main.resources.srcDir(generatedDomainDescriptorRoot)
        tasks.named('processResources').configure {{ dependsOn('generateCpfDomainRuntimeDescriptor') }}

        if (project.name == 'online') {{
            def manifestRoot = layout.buildDirectory.dir('generated/cpf-operation-manifest')
            def manifestFile = manifestRoot.map {{ it.file('META-INF/cpf/business-operation-manifest.json') }}
            tasks.register('generateCpfBusinessOperationManifest') {{
                group = 'CPF 20 | 검증'
                description = '업무 Domain Online Operation을 Build 시점에 전수검사하고 Side Effect 없는 Canonical Manifest를 생성합니다.'
                inputs.files(sourceSets.main.java).withPathSensitivity(PathSensitivity.RELATIVE)
                outputs.file(manifestFile)
                doLast {{
                    def operations = []
                    sourceSets.main.java.files.findAll {{ it.isFile() }}.sort {{ it.path }}.each {{ source ->
                        def text = source.getText('UTF-8')
                        if (!text.contains('@CpfOnlineTransaction')) return
                        def pkgMatcher = text =~ /(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*;/
                        if (!pkgMatcher.find()) throw new GradleException("Business Operation package를 찾을 수 없습니다: ${{source}}")
                        def classMatcher = text =~ /\bclass\s+([A-Za-z_$][A-Za-z0-9_$]*)\b/
                        if (!classMatcher.find()) throw new GradleException("Business Operation class를 찾을 수 없습니다: ${{source}}")
                        def controllerClass = "${{pkgMatcher.group(1)}}.${{classMatcher.group(1)}}"
                        def classPrefix = text.substring(0, classMatcher.start())
                        def classMappings = (classPrefix =~ /(?s)@RequestMapping\s*(?:\((.*?)\))?/) .collect {{ it }}
                        def basePath = classMappings.isEmpty() ? '' : cpfFirstString(classMappings.last()[1] as String)

                        def classBodyStart = text.indexOf('{{', classMatcher.end())
                        if (classBodyStart < 0) throw new GradleException("Business Operation class body를 찾을 수 없습니다: ${{source}}")
                        def previousTransactionEnd = classBodyStart + 1
                        def txMatcher = text =~ /(?s)@CpfOnlineTransaction\s*\((.*?)\)/
                        while (txMatcher.find()) {{
                            def txBody = txMatcher.group(1)
                            def operationId = cpfTextAttribute(txBody, 'operationId')
                            def name = cpfTextAttribute(txBody, 'name')
                            def description = cpfTextAttribute(txBody, 'description')
                            if (!operationId || !name || !description) {{
                                throw new GradleException("@CpfOnlineTransaction operationId/name/description가 필요합니다: ${{source}}")
                            }}
                            // HTTP path templates such as "/{{id}}" contain a literal closing brace.
                            // A raw lastIndexOf('}}') therefore truncates the annotation block and
                            // incorrectly reports that the operation has no Spring mapping.  The
                            // previous CPF transaction is the stable lower boundary: its mapping is
                            // before that annotation, while the current operation annotations are after it.
                            def prefix = text.substring(previousTransactionEnd, txMatcher.start())
                            def openApiMatches = (prefix =~ /(?s)@Operation\s*\((.*?)\)/).collect {{ it }}
                            if (openApiMatches.isEmpty()) throw new GradleException("업무 Online Operation에는 @Operation(operationId=...)이 필요합니다: ${{controllerClass}}#${{operationId}}")
                            def openApiId = cpfTextAttribute(openApiMatches.last()[1] as String, 'operationId')
                            if (operationId != openApiId) throw new GradleException("CPF/OpenAPI operationId mismatch: ${{operationId}} != ${{openApiId}}")
                            def mappingMatches = (prefix =~ /(?s)@(Get|Post|Put|Patch|Delete|Request)Mapping\s*(?:\((.*?)\))?/).collect {{ it }}
                            if (mappingMatches.isEmpty()) throw new GradleException("업무 Online Operation HTTP Mapping이 필요합니다: ${{controllerClass}}#${{operationId}}")
                            def mapping = mappingMatches.last()
                            def mappingType = mapping[1] as String
                            def mappingBody = mapping[2] as String
                            def httpMethod = mappingType == 'Request' ? 'ANY' : mappingType.toUpperCase(Locale.ROOT)
                            def path = cpfJoinPath(basePath, cpfFirstString(mappingBody))
                            def following = text.substring(txMatcher.end(), Math.min(text.length(), txMatcher.end() + 1600))
                                    .replaceFirst(/(?s)^\s*\/\*\*.*?\*\//, '')
                            def methodMatcher = following =~ /(?s)\b(?:public|protected)\s+[A-Za-z0-9_$.<>?,\[\]\s]+\s+([A-Za-z_$][A-Za-z0-9_$]*)\s*\(/
                            if (!methodMatcher.find()) throw new GradleException("업무 Online Operation Handler method를 찾을 수 없습니다: ${{controllerClass}}#${{operationId}}")
                            def handlerMethod = methodMatcher.group(1)
                            def fingerprint = cpfSha256("${{operationId}}|${{name}}|${{description}}|${{httpMethod}}|${{path}}|${{controllerClass}}|${{handlerMethod}}")
                            operations << [operationId:operationId, name:name, description:description,
                                           openApiOperationId:openApiId, httpMethod:httpMethod, apiPath:path,
                                           controllerClass:controllerClass, handlerMethod:handlerMethod,
                                           sourceFingerprint:fingerprint,
                                           sourcePath:project.projectDir.toPath().relativize(source.toPath()).toString().replace('\\','/')]
                            previousTransactionEnd = txMatcher.end()
                        }}
                    }}
                    def duplicateIds = operations.groupBy {{ it.operationId }}.findAll {{ k,v -> v.size() > 1 }}.keySet()
                    if (!duplicateIds.isEmpty()) throw new GradleException("Duplicate business operationId: ${{duplicateIds.sort()}}")
                    operations.sort {{ a,b -> a.operationId <=> b.operationId }}
                    def target = manifestFile.get().asFile
                    target.parentFile.mkdirs()
                    target.setText(JsonOutput.prettyPrint(JsonOutput.toJson([
                            schemaVersion:1,
                            projectPath:project.path,
                            operations:operations
                    ])) + System.lineSeparator(), 'UTF-8')
                }}
            }}
            sourceSets.main.resources.srcDir(manifestRoot)
            tasks.named('processResources') {{ dependsOn tasks.named('generateCpfBusinessOperationManifest') }}
        }}
    }}
}}
'''


def render_vendor_mapper_overlay(d: DomainDefinition) -> str:
    """Gradle contract that materializes only the selected DB mapper in build/.

    Templates stay in the CPF Data-owned artifact.  Changing ``cpfDbVendor`` never
    edits the generated customer Source tree.
    """
    return f'''
def cpfSupportedDbVendors = ['mariadb', 'postgresql', 'oracle'] as Set
def cpfSelectedDbVendor = providers.gradleProperty('cpfDbVendor').orElse(providers.environmentVariable('CPF_DB_VENDOR'))
def cpfVendorResourceRoot = layout.buildDirectory.dir('generated-resources/cpf-vendor')

tasks.register('prepareCpfVendorResources') {{
    inputs.property('cpfDbVendor', providers.provider {{ cpfSelectedDbVendor.orNull ?: 'UNSET' }})
    inputs.files(configurations.runtimeClasspath)
        .withPropertyName('cpfVendorRuntimeClasspath')
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(cpfVendorResourceRoot)
    doLast {{
        def selectedVendor = cpfSelectedDbVendor.orNull
        if (selectedVendor == null || selectedVendor.trim().isEmpty()) {{
            throw new GradleException("DB Vendor가 지정되지 않았습니다. -PcpfDbVendor=<mariadb|postgresql|oracle> 또는 CPF_DB_VENDOR 환경변수를 설정하세요.")
        }}
        def vendor = selectedVendor.toLowerCase(Locale.ROOT)
        if (!cpfSupportedDbVendors.contains(vendor)) {{
            throw new GradleException("Unsupported cpfDbVendor: ${{vendor}}")
        }}
        def relative = "cpf-generated-domain-dialect/${{vendor}}/mybatis/__MAPPER__.xml.template"
        def candidates = []
        configurations.runtimeClasspath.files.each {{ entry ->
            if (entry.isDirectory()) {{
                def candidate = new File(entry, relative)
                if (candidate.isFile()) candidates << candidate
            }} else if (entry.isFile() && entry.name.endsWith('.jar')) {{
                candidates.addAll(zipTree(entry).matching {{ include relative }}.files)
            }}
        }}
        if (candidates.size() != 1) {{
            throw new GradleException("Selected CPF vendor mapper template must resolve exactly once: vendor=${{vendor}} matches=${{candidates.size()}}")
        }}
        def rendered = candidates[0].getText('UTF-8')
        def replacements = [
            '@CPF_MAPPER_NAMESPACE@': '{d.feature_package}.repository.SampleTransactionMapper',
            '@CPF_RESULT_TYPE@': '{d.feature_package}.model.SampleItem',
            '@CPF_IDEMPOTENCY_RESULT_TYPE@': '{d.feature_package}.model.SampleIdempotencyRecord',
            '@CPF_TABLE_PREFIX@': '{d.table_prefix}',
            '@CPF_SCHEMA_NAME@': ''
        ]
        replacements.each {{ token, value -> rendered = rendered.replace(token, value) }}
        def unresolved = (rendered =~ /@CPF_[A-Z_]+@/).findAll()
        if (!unresolved.isEmpty()) {{
            throw new GradleException("Unresolved CPF vendor mapper tokens: ${{unresolved.unique()}}")
        }}
        def target = cpfVendorResourceRoot.get().file('db/mapper/SampleTransactionMapper.xml').asFile
        target.parentFile.mkdirs()
        target.setText(rendered.replace('\\r\\n','\\n'), 'UTF-8')
    }}
}}
sourceSets.main.resources.srcDir(cpfVendorResourceRoot)
tasks.named('processResources') {{ dependsOn tasks.named('prepareCpfVendorResources') }}
'''


def render_domain_build(d: DomainDefinition, deps: list[str]) -> str:
    lines = _gradle_dependency_lines(deps)
    mybatis = "    implementation 'org.mybatis:mybatis'\n" if d.persistence == "mybatis" else ""
    overlay = render_vendor_mapper_overlay(d) if d.sample_transaction and d.persistence == "mybatis" else ""
    return f'''// 실제 Online/Batch가 공유하는 업무 코드가 있을 때만 생성되는 Domain Gradle 설정입니다.
plugins {{ id 'java-library' }}

dependencies {{
    api platform("com.cpf:cpf-platform-bom:${{cpfPlatformVersion}}")
{lines}
    implementation 'org.slf4j:slf4j-api'
{mybatis}    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.assertj:assertj-core'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}}
{overlay}
'''


def render_app_build(d: DomainDefinition, kind: str, deps: list[str], stack: dict[str,str],
                     domain_dependency_groups: Iterable[str] = ()) -> str:
    if kind not in {"online","batch"}: raise DomainError(f"지원하지 않는 Generated Domain module: {kind}")
    boot=stack.get("springBootVersion","4.1.0")
    dm=stack.get("springDependencyManagementVersion","1.1.7")
    project=kind
    lines=_gradle_dependency_lines(deps)
    domain_lines=""
    if kind == "online":
        domain_lines="\n".join(
            f'    implementation "{group}:online:1.0.0-SNAPSHOT"'
            for group in domain_dependency_groups
        )
        if domain_lines: domain_lines += "\n"
    shared=""
    owner_overlay = d.sample_transaction and d.persistence == "mybatis"
    overlay = render_vendor_mapper_overlay(d) if owner_overlay else ""
    jdbc_runtime_prelude = ""
    jdbc_runtime_dependency = ""
    jdbc_runtime_validation = ""
    if d.persistence != "none":
        jdbc_runtime_prelude = '''def cpfRuntimeSupportedDbVendors = ['mariadb', 'postgresql', 'oracle'] as Set
def cpfRuntimeSelectedDbVendor = providers.gradleProperty('cpfDbVendor').orElse(providers.environmentVariable('CPF_DB_VENDOR'))
def cpfJdbcDriverByVendor = [
    mariadb: 'org.mariadb.jdbc:mariadb-java-client',
    postgresql: 'org.postgresql:postgresql',
    oracle: 'com.oracle.database.jdbc:ojdbc11'
]
def cpfRequireRuntimeDbVendor = {
    def selected = cpfRuntimeSelectedDbVendor.orNull
    if (selected == null || selected.trim().isEmpty()) {
        throw new GradleException("DB Vendor가 지정되지 않았습니다. -PcpfDbVendor=<mariadb|postgresql|oracle> 또는 CPF_DB_VENDOR 환경변수를 설정하세요.")
    }
    def vendor = selected.trim().toLowerCase(Locale.ROOT)
    if (!cpfRuntimeSupportedDbVendors.contains(vendor)) {
        throw new GradleException("Unsupported cpfDbVendor: ${vendor}")
    }
    vendor
}
def cpfSelectedJdbcDriver = providers.provider { cpfJdbcDriverByVendor[cpfRequireRuntimeDbVendor()] }

'''
        jdbc_runtime_dependency = "    runtimeOnly cpfSelectedJdbcDriver\n"
        jdbc_runtime_validation = '''tasks.register('validateCpfJdbcDriverSelection') {
    inputs.property('cpfDbVendor', providers.provider { cpfRuntimeSelectedDbVendor.orNull ?: 'UNSET' })
    doLast { cpfSelectedJdbcDriver.get() }
}
tasks.named('processResources') { dependsOn tasks.named('validateCpfJdbcDriverSelection') }

'''
    return f'''// Generated Customer Domain {kind} 실행 Module 설정입니다.
plugins {{
    id 'java'
    id 'org.springframework.boot' version '{boot}'
    id 'io.spring.dependency-management' version '{dm}'
}}

group = providers.gradleProperty('cpf.domain.packageName').get()

{jdbc_runtime_prelude}dependencies {{
{shared}    implementation platform("com.cpf:cpf-platform-bom:${{cpfPlatformVersion}}")
{lines}
{domain_lines}{jdbc_runtime_dependency}    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}}

{jdbc_runtime_validation}// 고객사 공통 Library는 customer-libraries.properties에 명시적으로 선택한 경우에만 연결됩니다.
def cpfCustomerLibraryDependencies = rootProject.file('customer-library-dependencies.gradle')
if (cpfCustomerLibraryDependencies.isFile()) {{ apply from: cpfCustomerLibraryDependencies }}

tasks.named('bootJar') {{ archiveBaseName = 'cpf-{d.name}-{project}' }}
{overlay}
'''


def render_gradle_properties(d: DomainDefinition, stack: dict[str,str]) -> str:
    version = stack.get("cpfVersion") or "1.0.0-SNAPSHOT"
    dependencies = ";".join(f"{x.name}:{x.system_code}:{','.join(x.operations)}" for x in d.domain_dependencies)
    external_clients = ";".join(f"{x.name}:{x.client_id}:{x.capability}" for x in d.external_clients)
    return (
        f"# 개발자가 이해·수정하고 Gradle/Generator/Runtime이 함께 소비하는 Domain 계약입니다. Secret은 저장하지 않습니다.\n"
        "cpf.domain.contractVersion=1\n"
        f"cpf.domain.name={d.name}\n"
        f"cpf.domain.systemCode={d.system_code}\n"
        f"cpf.domain.packageName={d.package_name}\n"
        f"cpf.domain.tablePrefix={d.table_prefix}\n"
        f"cpf.domain.preset={d.preset}\n"
        f"cpf.domain.online={str(d.online).lower()}\n"
        f"cpf.domain.batch={str(d.batch).lower()}\n"
        f"cpf.domain.businessFeatures={','.join(d.business_features)}\n"
        f"cpf.domain.persistence={d.persistence}\n"
        f"cpf.domain.httpClient={str(d.http_client).lower()}\n"
        f"cpf.domain.resilience={str(d.resilience).lower()}\n"
        f"cpf.domain.cache={d.cache}\n"
        f"cpf.domain.messaging={d.messaging}\n"
        f"cpf.domain.objectStorage={d.object_storage}\n"
        f"cpf.domain.securityProfile={d.security_profile}\n"
        f"cpf.domain.sampleTransaction={str(d.sample_transaction).lower()}\n"
        f"cpf.domain.generationMode={d.generation_mode}\n"
        f"cpf.domain.localOnlinePort={d.local_online_port}\n"
        f"cpf.domain.dependencies={dependencies}\n"
        f"cpf.domain.externalClients={external_clients}\n"
        f"cpfPlatformVersion={version}\n"
        f"org.gradle.jvmargs={GRADLE_DAEMON_JVMARGS}\n"
        "org.gradle.workers.max=2\n"
        "org.gradle.parallel=false\n"
    )


def render_domain_base_service(d: DomainDefinition) -> str:
    c = d.class_name
    return f'''package {d.package_name}.common.base;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.api.CpfBaseService;

/**
 * {d.name} 업무 Service가 공통으로 재사용하는 Domain Base입니다.
 * Framework Base 위에서 Domain 식별자, TransactionId, 입력 정규화 정책을 한 곳에 고정합니다.
 */
public abstract class {c}BaseService extends CpfBaseService {{
    protected static final String DOMAIN_NAME = "{d.name}";
    protected static final String SYSTEM_CODE = "{d.system_code}";

    protected final String requireTransactionId() {{
        String transactionId = CpfContexts.requireCurrent().transactionId();
        return requireText(transactionId, "transactionId");
    }}

    protected final long transactionSequence() {{
        return CpfContexts.transactionSequence();
    }}

    protected final String actorId() {{
        String actor = CpfContexts.operatorId();
        return actor == null || actor.isBlank() ? SYSTEM_CODE : actor.trim();
    }}

}}
'''


def render_domain_base_repository(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.common.base;

import com.cpf.data.persistence.api.CpfBaseRepository;

/** {d.name} MyBatis Repository의 공통 namespace/paging 정책을 제공하는 Domain Base입니다. */
public abstract class {c}BaseRepository extends CpfBaseRepository {{
    protected static final String TABLE_PREFIX = "{d.table_prefix}";
    protected final int pageSize(int requested) {{ return boundedSize(requested, 20, 200); }}
    protected final int pageOffset(int page, int size) {{
        requireRule(page >= 0, "page는 0 이상이어야 합니다.");
        return Math.multiplyExact(page, pageSize(size));
    }}
}}
'''


def render_domain_base_controller(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.api.base;

import com.cpf.web.api.CpfBaseController;

/** {d.name} Web API의 Domain 공통 정책과 응답 helper를 제공하는 2단계 Base Controller입니다. */
public abstract class {c}BaseController extends CpfBaseController {{
    protected static final String DOMAIN_NAME = "{d.name}";
    protected static final String SYSTEM_CODE = "{d.system_code}";
    protected final int normalizePageSize(Integer requested) {{
        if (requested == null || requested <= 0) return 20;
        requireRule(requested <= 200, "size는 200 이하여야 합니다.");
        return requested;
    }}
}}
'''


def render_model(d: DomainDefinition) -> str:
    return f'''package {d.package_name}.common.model;

import java.time.Instant;
import com.cpf.core.api.base.CpfResponse;

/** 중앙 Generated Domain Schema와 1:1로 대응하는 Vendor-neutral Sample 모델입니다. */
public final class SampleItem implements CpfResponse {{
    private long sampleItemId;
    private String sampleKey;
    private String itemName;
    private String statusCode;
    private long versionNo;
    private String idempotencyKey;
    private String transactionId;
    private long transactionSequence;
    private Instant transactionAt;
    private String deletedYn;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;

    public SampleItem() {{ }}
    public SampleItem(long sampleItemId, String sampleKey, String itemName, String statusCode,
            long versionNo, String idempotencyKey, String transactionId, long transactionSequence,
            Instant transactionAt, String deletedYn, String createdBy, Instant createdAt,
            String updatedBy, Instant updatedAt) {{
        this.sampleItemId=sampleItemId; this.sampleKey=sampleKey; this.itemName=itemName;
        this.statusCode=statusCode; this.versionNo=versionNo; this.idempotencyKey=idempotencyKey;
        this.transactionId=transactionId; this.transactionSequence=transactionSequence;
        this.transactionAt=transactionAt; this.deletedYn=deletedYn; this.createdBy=createdBy;
        this.createdAt=createdAt; this.updatedBy=updatedBy; this.updatedAt=updatedAt;
    }}
    public long getSampleItemId() {{ return sampleItemId; }}
    public void setSampleItemId(long value) {{ sampleItemId=value; }}
    public String getSampleKey() {{ return sampleKey; }}
    public void setSampleKey(String value) {{ sampleKey=value; }}
    public String getItemName() {{ return itemName; }}
    public void setItemName(String value) {{ itemName=value; }}
    public String getStatusCode() {{ return statusCode; }}
    public void setStatusCode(String value) {{ statusCode=value; }}
    public long getVersionNo() {{ return versionNo; }}
    public void setVersionNo(long value) {{ versionNo=value; }}
    public String getIdempotencyKey() {{ return idempotencyKey; }}
    public void setIdempotencyKey(String value) {{ idempotencyKey=value; }}
    public String getTransactionId() {{ return transactionId; }}
    public void setTransactionId(String value) {{ transactionId=value; }}
    public long getTransactionSequence() {{ return transactionSequence; }}
    public void setTransactionSequence(long value) {{ transactionSequence=value; }}
    public Instant getTransactionAt() {{ return transactionAt; }}
    public void setTransactionAt(Instant value) {{ transactionAt=value; }}
    public String getDeletedYn() {{ return deletedYn; }}
    public void setDeletedYn(String value) {{ deletedYn=value; }}
    public String getCreatedBy() {{ return createdBy; }}
    public void setCreatedBy(String value) {{ createdBy=value; }}
    public Instant getCreatedAt() {{ return createdAt; }}
    public void setCreatedAt(Instant value) {{ createdAt=value; }}
    public String getUpdatedBy() {{ return updatedBy; }}
    public void setUpdatedBy(String value) {{ updatedBy=value; }}
    public Instant getUpdatedAt() {{ return updatedAt; }}
    public void setUpdatedAt(Instant value) {{ updatedAt=value; }}
}}
'''


def render_requests(d: DomainDefinition) -> dict[str,str]:
    pkg=f"{d.package_name}.common.dto"
    return {
      "CreateSampleRequest.java": f'''package {pkg};

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cpf.core.api.base.CpfRequest;

/** Sample Create 입력 계약입니다. */
public record CreateSampleRequest(
        @NotBlank @Size(max=100) String sampleKey,
        @NotBlank @Size(max=200) String itemName,
        @NotBlank @Size(max=180) String idempotencyKey) implements CpfRequest {{ }}
''',
      "UpdateSampleRequest.java": f'''package {pkg};

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cpf.core.api.base.CpfRequest;

/** Optimistic Version을 포함하는 Sample Update 입력 계약입니다. */
public record UpdateSampleRequest(
        @NotBlank @Size(max=200) String itemName,
        @NotBlank @Size(max=30) String statusCode,
        @NotBlank @Size(max=180) String idempotencyKey,
        @Min(0) long expectedVersion) implements CpfRequest {{ }}
''',
      "DeleteSampleRequest.java": f'''package {pkg};

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cpf.core.api.base.CpfRequest;

/** Optimistic Version과 멱등키를 포함하는 논리 삭제 입력 계약입니다. */
public record DeleteSampleRequest(
        @NotBlank @Size(max=180) String idempotencyKey,
        @Min(0) long expectedVersion) implements CpfRequest {{ }}
''',
      "SampleSearchRequest.java": f'''package {pkg};

import com.cpf.core.api.base.CpfRequest;

/** Offset/Page와 Cursor/Slice가 공유하는 Search 입력 계약입니다. */
public record SampleSearchRequest(String keyword, String statusCode, Integer page, Integer size, Long cursor) implements CpfRequest {{
    public int safePage() {{ return page == null || page < 0 ? 0 : page; }}
    public int safeSize() {{ return size == null || size <= 0 ? 20 : Math.min(size, 200); }}
    public long safeCursor() {{ return cursor == null || cursor < 0 ? 0L : cursor; }}
}}
''',
      "SamplePage.java": f'''package {pkg};

import java.util.List;
import {d.package_name}.common.model.SampleItem;
import com.cpf.core.api.base.CpfResponse;

/** Search/Page 표준 응답입니다. */
public record SamplePage(List<SampleItem> items, long total, int page, int size) implements CpfResponse {{ }}
''',
      "SampleSlice.java": f'''package {pkg};

import java.util.List;
import {d.package_name}.common.model.SampleItem;
import com.cpf.core.api.base.CpfResponse;

/** Cursor/Slice 표준 응답입니다. */
public record SampleSlice(List<SampleItem> items, boolean hasNext, Long nextCursor) implements CpfResponse {{ }}
''',
      "SampleIdRequest.java": f'''package {pkg};

import com.cpf.core.api.base.CpfRequest;

/** Path ID를 Typed Domain Call에서도 사용하는 명시적 요청 계약입니다. */
public record SampleIdRequest(long id) implements CpfRequest {{ }}
''',
      "UpdateSampleCommand.java": f'''package {pkg};

import com.cpf.core.api.base.CpfRequest;

/** Update의 Path ID와 Body를 하나의 Typed Domain Call 요청으로 묶습니다. */
public record UpdateSampleCommand(long id, UpdateSampleRequest request) implements CpfRequest {{ }}
''',
      "DeleteSampleCommand.java": f'''package {pkg};

import com.cpf.core.api.base.CpfRequest;

/** Delete의 Path ID와 Body를 하나의 Typed Domain Call 요청으로 묶습니다. */
public record DeleteSampleCommand(long id, DeleteSampleRequest request) implements CpfRequest {{ }}
''',
      "SampleIdempotencyRecord.java": f'''package {pkg};

import java.time.Instant;

/** 같은 key/hash replay와 다른 hash conflict를 판정하는 durable ledger 모델입니다. */
public record SampleIdempotencyRecord(
        String idempotencyKey, String operationCode, String requestHash,
        long sampleItemId, long resultVersion, String deletedYn,
        String transactionId, Instant createdAt) {{ }}
'''
    }


def render_policy(d: DomainDefinition) -> str:
    return f'''package {d.package_name}.common.policy;

import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

/** Sample Transaction의 멱등/버전/입력 규칙을 Runtime에서 재사용하는 순수 정책입니다. */
@Component
public final class SampleTransactionPolicy {{
    public String requireIdempotencyKey(String value) {{
        if (value == null || value.isBlank()) throw new CpfValidationException("idempotencyKey는 필수입니다.");
        String normalized = value.trim();
        if (normalized.length() > 180) throw new CpfValidationException("idempotencyKey는 180자 이하여야 합니다.");
        return normalized;
    }}
    public String requireSampleKey(String value) {{
        if (value == null || value.isBlank()) throw new CpfValidationException("sampleKey는 필수입니다.");
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.length() > 100) throw new CpfValidationException("sampleKey는 100자 이하여야 합니다.");
        return normalized;
    }}
    public String requireItemName(String value) {{
        if (value == null || value.isBlank()) throw new CpfValidationException("itemName은 필수입니다.");
        String normalized = value.trim();
        if (normalized.length() > 200) throw new CpfValidationException("itemName은 200자 이하여야 합니다.");
        return normalized;
    }}
    public String requireStatusCode(String value) {{
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("ACTIVE") && !normalized.equals("INACTIVE"))
            throw new CpfValidationException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        return normalized;
    }}
    public void requireExpectedVersion(long version) {{
        if (version < 0) throw new CpfValidationException("expectedVersion은 0 이상이어야 합니다.");
    }}
}}
'''


def render_mapper(d: DomainDefinition) -> str:
    return f'''package {d.package_name}.common.mapper;

import {d.package_name}.common.model.SampleIdempotencyRecord;
import {d.package_name}.common.model.SampleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 중앙 Vendor Runtime Query Pack이 구현하는 Generated Domain Mapper 계약입니다. */
@Mapper
public interface SampleTransactionMapper {{
    List<SampleItem> search(
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection);
    long count(@Param("keyword") String keyword, @Param("statusCode") String statusCode);
    SampleItem findBySampleKey(@Param("value") String sampleKey);
    SampleItem findById(@Param("value") long sampleItemId);
    SampleIdempotencyRecord findIdempotency(@Param("value") String idempotencyKey);
    SampleItem findForUpdate(@Param("sampleItemId") long sampleItemId);
    List<SampleItem> cursorSlice(
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            @Param("cursor") long cursor,
            @Param("size") int size);
    int insert(SampleItem item);
    int insertIdempotency(SampleIdempotencyRecord record);
    int updateWithVersion(SampleItem item);
    int logicalDeleteWithVersion(SampleItem item);
}}
'''


def render_repository(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.common.repository;

import {d.package_name}.common.base.{c}BaseRepository;
import {d.package_name}.common.mapper.SampleTransactionMapper;
import {d.package_name}.common.model.SampleIdempotencyRecord;
import {d.package_name}.common.model.SampleItem;
import com.cpf.data.persistence.api.CpfRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Sample Transaction의 실제 MyBatis Repository Consumer입니다. */
@CpfRepository
public class SampleTransactionRepository extends {c}BaseRepository {{
    private static final Set<String> SORT_COLUMNS = Set.of(
            "sample_item_id", "sample_key", "item_name", "status_code", "created_at");
    private final SampleTransactionMapper mapper;

    /** Mapper를 주입해 Vendor 중립 Persistence 경로를 구성합니다. */
    public SampleTransactionRepository(SampleTransactionMapper mapper) {{ this.mapper = mapper; }}

    public int insert(SampleItem item) {{ return mapper.insert(item); }}
    public Optional<SampleItem> findById(long id) {{
        requireRule(id > 0, "sampleItemId는 1 이상이어야 합니다.");
        return Optional.ofNullable(mapper.findById(id));
    }}
    public Optional<SampleItem> findBySampleKey(String key) {{
        return Optional.ofNullable(mapper.findBySampleKey(requireText(key,"sampleKey")));
    }}
    public Optional<SampleIdempotencyRecord> findIdempotency(String key) {{
        return Optional.ofNullable(mapper.findIdempotency(requireText(key,"idempotencyKey")));
    }}
    public Optional<SampleItem> findForUpdate(long id) {{
        requireRule(id > 0, "sampleItemId는 1 이상이어야 합니다.");
        return Optional.ofNullable(mapper.findForUpdate(id));
    }}
    public List<SampleItem> search(String keyword, String statusCode, int page, int size, String sortBy, String direction) {{
        int normalizedSize = pageSize(size);
        String column = SORT_COLUMNS.contains(sortBy) ? sortBy : "sample_item_id";
        String order = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        return mapper.search(normalizeKeyword(keyword), normalizeStatus(statusCode),
                pageOffset(page, normalizedSize), normalizedSize, column, order);
    }}
    public long count(String keyword, String statusCode) {{
        return mapper.count(normalizeKeyword(keyword), normalizeStatus(statusCode));
    }}
    public List<SampleItem> cursorSlice(String keyword, String statusCode, long cursor, int size) {{
        requireRule(cursor >= 0, "cursor는 0 이상이어야 합니다.");
        return mapper.cursorSlice(normalizeKeyword(keyword), normalizeStatus(statusCode), cursor,
                boundedSize(size, 21, 201));
    }}
    public int insertIdempotency(SampleIdempotencyRecord record) {{ return mapper.insertIdempotency(record); }}
    public int updateWithVersion(SampleItem item) {{ return mapper.updateWithVersion(item); }}
    public int logicalDeleteWithVersion(SampleItem item) {{ return mapper.logicalDeleteWithVersion(item); }}
    private static String normalizeKeyword(String value) {{
        if (value == null || value.isBlank()) return null;
        String normalized=value.trim();
        if (normalized.length() > 200) throw new IllegalArgumentException("keyword는 200자 이하여야 합니다.");
        return normalized;
    }}
    private static String normalizeStatus(String value) {{
        if (value == null || value.isBlank()) return null;
        String normalized=value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("ACTIVE") && !normalized.equals("INACTIVE"))
            throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        return normalized;
    }}
}}
'''


def render_audit(d: DomainDefinition) -> str:
    return f'''package {d.package_name}.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 민감정보 원문을 남기지 않고 TransactionId 중심의 구조화 업무 Audit event를 기록합니다. */
@Component
public class DomainAuditLogger {{
    private static final Logger log = LoggerFactory.getLogger(DomainAuditLogger.class);
    public void success(String action, String transactionId, String entityId) {{
        log.info("cpfAudit domain={d.name} system={d.system_code} action={{}} transactionId={{}} entityId={{}} result=SUCCESS", action, transactionId, entityId);
    }}
    public void replay(String transactionId, String entityId) {{
        log.info("cpfAudit domain={d.name} system={d.system_code} action=IDEMPOTENT_REPLAY transactionId={{}} entityId={{}} result=SUCCESS", transactionId, entityId);
    }}
}}
'''


def render_service(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.api.service;

import {d.package_name}.common.audit.DomainAuditLogger;
import {d.package_name}.common.base.{c}BaseService;
import {d.package_name}.common.repository.SampleTransactionRepository;
import {d.package_name}.common.model.*;
import {d.package_name}.common.dto.*;
import {d.package_name}.common.policy.SampleTransactionPolicy;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;

/** HTTP -> Service -> DAO/Mapper -> CUSTOMER_BUSINESS_DB 실제 Sample Transaction입니다. */
@CpfService
public class SampleTransactionService extends {c}BaseService {{
    private final SampleTransactionRepository repository;
    private final SampleTransactionPolicy policy;
    private final DomainAuditLogger audit;
    private final Clock clock;
    public SampleTransactionService(SampleTransactionRepository repository, SampleTransactionPolicy policy, DomainAuditLogger audit,
            @Qualifier("cpfStarterClock") Clock clock) {{
        this.repository=repository; this.policy=policy; this.audit=audit; this.clock=clock;
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager")
    public SampleItem create(CreateSampleRequest request) {{
        String tx=requireTransactionId(); String actor=actorId();
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String sampleKey=policy.requireSampleKey(request.sampleKey());
        String itemName=policy.requireItemName(request.itemName());
        String hash=requestHash("CREATE",sampleKey,itemName);
        SampleItem replay=replay(idem,"CREATE",hash,tx);
        if (replay != null) return replay;
        if (repository.findBySampleKey(sampleKey).isPresent())
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE, "sampleKey가 이미 존재합니다.");
        Instant now=clock.instant();
        SampleItem item=new SampleItem(0L,sampleKey,itemName,"ACTIVE",0L,idem,tx,
                transactionSequence(),now,"N",actor,now,actor,now);
        try {{
            if (repository.insert(item) != 1)
                throw new CpfBusinessException(CpfErrorCode.DATABASE_ERROR, "Sample insert 결과가 1건이 아닙니다.");
        }} catch (DuplicateKeyException duplicate) {{
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE,"sampleKey 또는 idempotencyKey가 이미 존재합니다.");
        }}
        recordIdempotency(idem,"CREATE",hash,item,tx,now);
        audit.success("CREATE",tx,Long.toString(item.getSampleItemId()));
        return item;
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager", readOnly=true)
    public SampleItem detail(long id) {{
        return repository.findById(id).orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND, "Sample을 찾을 수 없습니다."));
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager", readOnly=true)
    public SamplePage search(SampleSearchRequest request) {{
        int page=request.safePage(), size=request.safeSize();
        return new SamplePage(repository.search(request.keyword(),request.statusCode(),page,size,
                "sample_item_id","ASC"),repository.count(request.keyword(),request.statusCode()),page,size);
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager", readOnly=true)
    public SampleSlice slice(SampleSearchRequest request) {{
        int size=request.safeSize();
        List<SampleItem> rows=repository.cursorSlice(request.keyword(),request.statusCode(),request.safeCursor(),size+1);
        boolean hasNext=rows.size()>size;
        List<SampleItem> items=hasNext ? List.copyOf(rows.subList(0,size)) : List.copyOf(rows);
        Long nextCursor=hasNext && !items.isEmpty() ? items.get(items.size()-1).getSampleItemId() : null;
        return new SampleSlice(items,hasNext,nextCursor);
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager")
    public SampleItem update(long id, UpdateSampleRequest request) {{
        String tx=requireTransactionId(); String actor=actorId();
        policy.requireExpectedVersion(request.expectedVersion());
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String itemName=policy.requireItemName(request.itemName());
        String status=policy.requireStatusCode(request.statusCode());
        String hash=requestHash("UPDATE",Long.toString(id),itemName,status,Long.toString(request.expectedVersion()));
        SampleItem replay=replay(idem,"UPDATE",hash,tx);
        if (replay != null) return replay;
        SampleItem current=repository.findForUpdate(id)
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND,"Sample을 찾을 수 없습니다."));
        if (current.getVersionNo()!=request.expectedVersion())
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"expectedVersion이 현재 Version과 다릅니다.");
        Instant now=clock.instant();
        SampleItem command=new SampleItem(id,current.getSampleKey(),itemName,status,current.getVersionNo(),idem,tx,
                transactionSequence(),now,"N",current.getCreatedBy(),current.getCreatedAt(),actor,now);
        if (repository.updateWithVersion(command) != 1)
            throw new CpfBusinessException(CpfErrorCode.CONFLICT, "Version 충돌 또는 대상 부재로 Update하지 못했습니다.");
        SampleItem updated=detail(id); recordIdempotency(idem,"UPDATE",hash,updated,tx,now);
        audit.success("UPDATE",tx,Long.toString(id)); return updated;
    }}

    @CpfTransactional(transactionManager="cpfDomainTransactionManager")
    public SampleItem delete(long id, DeleteSampleRequest request) {{
        String tx=requireTransactionId(); String actor=actorId();
        policy.requireExpectedVersion(request.expectedVersion());
        String idem=policy.requireIdempotencyKey(request.idempotencyKey());
        String hash=requestHash("DELETE",Long.toString(id),Long.toString(request.expectedVersion()));
        SampleItem replay=replay(idem,"DELETE",hash,tx);
        if (replay != null) return replay;
        SampleItem current=repository.findForUpdate(id)
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.NOT_FOUND,"Sample을 찾을 수 없습니다."));
        if (current.getVersionNo()!=request.expectedVersion())
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"expectedVersion이 현재 Version과 다릅니다.");
        Instant now=clock.instant();
        SampleItem command=new SampleItem(id,current.getSampleKey(),current.getItemName(),current.getStatusCode(),
                current.getVersionNo(),idem,tx,transactionSequence(),now,"Y",current.getCreatedBy(),
                current.getCreatedAt(),actor,now);
        if (repository.logicalDeleteWithVersion(command)!=1)
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Version 충돌 또는 대상 부재로 Delete하지 못했습니다.");
        SampleItem deleted=detail(id); recordIdempotency(idem,"DELETE",hash,deleted,tx,now);
        audit.success("DELETE",tx,Long.toString(id)); return deleted;
    }}

    /** Failure-injection Test가 실제 Transaction rollback을 증명할 수 있는 명시적 Probe입니다. */
    @CpfTransactional(transactionManager="cpfDomainTransactionManager")
    public void rollbackProbe(CreateSampleRequest request) {{
        create(request);
        throw new CpfBusinessException(CpfErrorCode.BUSINESS_RULE_VIOLATION,
                "의도된 Sample Transaction rollback probe입니다.");
    }}

    private SampleItem replay(String key, String operation, String requestHash, String transactionId) {{
        var existing=repository.findIdempotency(key);
        if (existing.isEmpty()) return null;
        SampleIdempotencyRecord record=existing.get();
        if (!operation.equals(record.operationCode()) || !requestHash.equals(record.requestHash()))
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"같은 idempotencyKey가 다른 요청에 사용되었습니다.");
        SampleItem item=repository.findById(record.sampleItemId())
                .orElseThrow(() -> new CpfBusinessException(CpfErrorCode.CONFLICT,"멱등 결과 Entity가 없습니다."));
        audit.replay(transactionId,Long.toString(item.getSampleItemId()));
        return item;
    }}

    private void recordIdempotency(String key, String operation, String requestHash,
            SampleItem item, String transactionId, Instant now) {{
        SampleIdempotencyRecord record=new SampleIdempotencyRecord(key,operation,requestHash,
                item.getSampleItemId(),item.getVersionNo(),item.getDeletedYn(),transactionId,now);
        try {{
            if (repository.insertIdempotency(record)!=1)
                throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Idempotency ledger 기록에 실패했습니다.");
        }} catch (DuplicateKeyException duplicate) {{
            throw new CpfBusinessException(CpfErrorCode.CONFLICT,"Idempotency ledger가 동시 요청에 의해 먼저 기록되었습니다.");
        }}
    }}

    private static String requestHash(String... values) {{
        try {{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            for (String value: values) {{
                byte[] bytes=(value==null ? "" : value).getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte)':'); digest.update(bytes); digest.update((byte)'|');
            }}
            return HexFormat.of().formatHex(digest.digest());
        }} catch (NoSuchAlgorithmException impossible) {{
            throw new IllegalStateException("SHA-256 provider가 없습니다.", impossible);
        }}
    }}
}}
'''


def render_controller(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.api.controller;

import {d.package_name}.api.base.{c}BaseController;
import {d.package_name}.api.service.SampleTransactionService;
import {d.package_name}.common.model.SampleItem;
import {d.package_name}.common.dto.*;
import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** CRUD/Search(Page·Slice·Cursor)를 제공하는 실제 Generated Business Controller입니다. */
@CpfController
@RequestMapping("/api/v1/{d.name}/samples")
public class SampleTransactionController extends {c}BaseController {{
    private final SampleTransactionService service;
    public SampleTransactionController(SampleTransactionService service) {{ this.service=service; }}

    @PostMapping
    @Operation(operationId="{d.sample_tx_id}_CREATE", summary="{d.name} sample 생성")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_CREATE", name="{d.name} sample 생성", description="{d.name} Sample을 생성한다.")
    public ResponseEntity<SampleItem> create(@Valid @RequestBody CreateSampleRequest request) {{
        SampleItem item=service.create(request); return created(URI.create("/api/v1/{d.name}/samples/"+item.getSampleItemId()),item);
    }}
    @GetMapping("/{{id}}")
    @Operation(operationId="{d.sample_tx_id}_DETAIL", summary="{d.name} sample 상세")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_DETAIL", name="{d.name} sample 상세", description="{d.name} Sample 상세를 조회한다.")
    public ResponseEntity<SampleItem> detail(@PathVariable long id) {{ return ok(service.detail(id)); }}
    @GetMapping
    @Operation(operationId="{d.sample_tx_id}_SEARCH", summary="{d.name} sample 검색")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_SEARCH", name="{d.name} sample 검색", description="{d.name} Sample을 조건·Paging으로 조회한다.")
    public ResponseEntity<SamplePage> search(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Integer page,
        @RequestParam(defaultValue="20") Integer size) {{
        return ok(service.search(new SampleSearchRequest(keyword,statusCode,page,normalizePageSize(size),null)));
    }}
    @GetMapping("/slice")
    @Operation(operationId="{d.sample_tx_id}_SLICE", summary="{d.name} sample cursor slice")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_SLICE", name="{d.name} sample cursor slice", description="{d.name} Sample을 Cursor Slice로 조회한다.")
    public ResponseEntity<SampleSlice> slice(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Long cursor,
        @RequestParam(defaultValue="20") Integer size) {{
        return ok(service.slice(new SampleSearchRequest(keyword,statusCode,null,normalizePageSize(size),cursor)));
    }}
    @PutMapping("/{{id}}")
    @Operation(operationId="{d.sample_tx_id}_UPDATE", summary="{d.name} sample 수정")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_UPDATE", name="{d.name} sample 수정", description="{d.name} Sample을 낙관적 Version으로 수정한다.")
    public ResponseEntity<SampleItem> update(@PathVariable long id, @Valid @RequestBody UpdateSampleRequest request) {{ return ok(service.update(id,request)); }}
    @DeleteMapping("/{{id}}")
    @Operation(operationId="{d.sample_tx_id}_DELETE", summary="{d.name} sample 논리 삭제")
    @CpfOnlineTransaction(operationId="{d.sample_tx_id}_DELETE", name="{d.name} sample 논리 삭제", description="{d.name} Sample을 논리 삭제한다.")
    public ResponseEntity<SampleItem> delete(@PathVariable long id, @Valid @RequestBody DeleteSampleRequest request) {{
        return ok(service.delete(id,request));
    }}
}}
'''


def render_api_application(d: DomainDefinition) -> str:
    c=d.class_name
    mapper_import="import org.mybatis.spring.annotation.MapperScan;\n" if d.persistence == "mybatis" else ""
    mapper_annotation=(f'@MapperScan(basePackages="{d.feature_package}.repository", '
                       'sqlSessionFactoryRef="cpfDomainSqlSessionFactory")\n') if d.persistence == "mybatis" else ""
    return f'''package {d.package_name};

{mapper_import}import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** {d.name} Generated API 실행 진입점입니다. */
@SpringBootApplication(scanBasePackages="{d.package_name}")
{mapper_annotation}public class {c}ApiApplication {{
    public static void main(String[] args) {{ SpringApplication.run({c}ApiApplication.class,args); }}
}}
'''





def render_policy_test(d: DomainDefinition) -> str:
    return f'''package {d.package_name}.common.policy;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated Domain 정책의 멱등성 키와 버전 경계 규칙을 검증합니다. */
class SampleTransactionPolicyTest {{
    private final SampleTransactionPolicy policy=new SampleTransactionPolicy();
    @Test void normalizesIdempotencyKey() {{ assertThat(policy.requireIdempotencyKey("  K-1 ")).isEqualTo("K-1"); }}
    @Test void rejectsBlankIdempotencyKey() {{ assertThatThrownBy(() -> policy.requireIdempotencyKey(" ")).isInstanceOf(CpfValidationException.class); }}
    @Test void rejectsNegativeVersion() {{ assertThatThrownBy(() -> policy.requireExpectedVersion(-1)).isInstanceOf(CpfValidationException.class); }}
}}
'''



def render_api_contract_test(d: DomainDefinition) -> str:
    c=d.class_name
    return f'''package {d.package_name}.api.controller;

import {d.package_name}.api.base.{c}BaseController;
import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated API가 CPF 3단 Base와 업무 Operation Identity 계약을 유지하는지 검증합니다. */
class SampleTransactionControllerContractTest {{
    @Test void keepsThreeLayerControllerAndCpfAnnotation() {{
        assertThat({c}BaseController.class.getSuperclass().getSimpleName()).isEqualTo("CpfBaseController");
        assertThat(SampleTransactionController.class.getSuperclass()).isEqualTo({c}BaseController.class);
        assertThat(SampleTransactionController.class.getAnnotation(CpfController.class)).isNotNull();
    }}

    @Test void keepsCpfAndOpenApiOperationIdentityAligned() {{
        Set<String> operationIds=new HashSet<>();
        for (Method method : SampleTransactionController.class.getDeclaredMethods()) {{
            CpfOnlineTransaction cpf=method.getAnnotation(CpfOnlineTransaction.class);
            if (cpf == null) continue;
            Operation openApi=method.getAnnotation(Operation.class);
            assertThat(openApi).as(method.getName()+" OpenAPI annotation").isNotNull();
            assertThat(cpf.operationId()).isEqualTo(openApi.operationId());
            assertThat(cpf.name()).isNotBlank();
            assertThat(cpf.description()).isNotBlank();
            assertThat(operationIds.add(cpf.operationId())).as("unique operationId "+cpf.operationId()).isTrue();
        }}
        assertThat(operationIds).containsExactlyInAnyOrder(
            "{d.sample_tx_id}_CREATE", "{d.sample_tx_id}_DETAIL", "{d.sample_tx_id}_SEARCH",
            "{d.sample_tx_id}_SLICE", "{d.sample_tx_id}_UPDATE", "{d.sample_tx_id}_DELETE");
    }}
}}
'''


def render_domain_ping_operation(d: DomainDefinition, runtime: str) -> str:
    package = f"{d.package_name}.{runtime}.domaincall"
    c = d.class_name
    return f'''package {package};

import com.cpf.core.api.domain.CpfDomainPingRequest;
import com.cpf.core.api.domain.CpfDomainPingResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Local/Remote 동일 Domain Call 경로를 실제 Runtime에서 검증하는 Generated managed operation입니다. */
@Component
public class {c}DomainPingOperation implements CpfDomainOperation<CpfDomainPingRequest, CpfDomainPingResponse> {{
    private final Clock clock;
    public {c}DomainPingOperation(@Qualifier("cpfStarterClock") Clock clock) {{ this.clock = clock; }}
    @Override public String systemCode() {{ return "{d.system_code}"; }}
    @Override public String operationId() {{ return "ping"; }}
    @Override public Class<CpfDomainPingRequest> requestType() {{ return CpfDomainPingRequest.class; }}
    @Override public Class<CpfDomainPingResponse> responseType() {{ return CpfDomainPingResponse.class; }}
    @Override public CpfResult<CpfDomainPingResponse> invoke(CpfDomainPingRequest request) {{
        return CpfResult.success(new CpfDomainPingResponse("{d.system_code}", request.requestId(), clock.instant()));
    }}
}}
'''


def render_domain_dependency_client(d: DomainDefinition, runtime: str, dependency: DomainDependency,
                                    contracts: dict[str,DomainOperationContract]) -> str:
    package = f"{d.package_name}.{runtime}.domaincall"
    c = dependency.class_name
    imports={contracts[op].request_type for op in dependency.operations}|{contracts[op].response_type for op in dependency.operations}
    methods=[]
    for op in dependency.operations:
        contract=contracts[op]; req=contract.request_type.rsplit('.',1)[-1]; resp=contract.response_type.rsplit('.',1)[-1]
        methods.append(f"    CpfResult<{resp}> {contract.method_name}({req} request);")
    return f'''package {package};

import com.cpf.core.api.result.CpfResult;
{chr(10).join('import '+x+';' for x in sorted(imports))}

/** {dependency.system_code} 논리 Domain의 선택 Operation을 배포 위치와 무관하게 호출하는 Generated Typed Client입니다. */
public interface {c}DomainClient {{
{chr(10).join(methods)}
}}
'''


def render_domain_dependency_adapter(d: DomainDefinition, runtime: str, dependency: DomainDependency,
                                     contracts: dict[str,DomainOperationContract]) -> str:
    package = f"{d.package_name}.{runtime}.domaincall"
    c = dependency.class_name
    imports={contracts[op].request_type for op in dependency.operations}|{contracts[op].response_type for op in dependency.operations}
    methods=[]
    for op in dependency.operations:
        contract=contracts[op]; req=contract.request_type.rsplit('.',1)[-1]; resp=contract.response_type.rsplit('.',1)[-1]
        methods.append(f'''    @Override public CpfResult<{resp}> {contract.method_name}({req} request) {{
        return router.invoke("{dependency.system_code}", "{op}", request, {resp}.class);
    }}''')
    return f'''package {package};

import com.cpf.core.api.result.CpfResult;
{chr(10).join('import '+x+';' for x in sorted(imports))}
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import org.springframework.stereotype.Component;

/** {dependency.system_code} Client를 CPF Domain Binding Resolver의 LOCAL/REMOTE 동일 경로에 연결합니다. */
@Component
public final class Default{c}DomainClient implements {c}DomainClient {{
    private final CpfDomainClientRouter router;
    public Default{c}DomainClient(CpfDomainClientRouter router) {{ this.router = router; }}
{chr(10).join(methods)}
}}
'''


def render_domain_dependency_consumer(d: DomainDefinition, runtime: str,
                                      contracts_by_dependency: dict[str,dict[str,DomainOperationContract]]) -> str:
    package = f"{d.package_name}.{runtime}.domaincall"
    fields=[]; params=[]; assigns=[]; methods=[]; imports=set()
    for dep in d.domain_dependencies:
        c=dep.class_name; var=dep.name.replace('-','_') + "DomainClient"; contracts=contracts_by_dependency[dep.name]
        fields.append(f"    private final {c}DomainClient {var};")
        params.append(f"{c}DomainClient {var}")
        assigns.append(f"        this.{var} = {var};")
        for op in dep.operations:
            contract=contracts[op]; req=contract.request_type.rsplit('.',1)[-1]; resp=contract.response_type.rsplit('.',1)[-1]
            imports.update((contract.request_type,contract.response_type))
            if contract.request_type=='com.cpf.core.api.domain.CpfDomainPingRequest':
                body=f"new {req}(requestId)"; arg='String requestId'
            else:
                body='request'; arg=f'{req} request'
            methods.append(f'''    /** {dep.system_code}/{op} Domain Operation을 실제 Consumer 경로에서 호출합니다. */
    public CpfResult<{resp}> {contract.method_name}{c}({arg}) {{
        return {var}.{contract.method_name}({body});
    }}''')
    return f'''package {package};

import com.cpf.core.api.result.CpfResult;
{chr(10).join('import '+x+';' for x in sorted(imports))}
import com.cpf.foundation.annotation.CpfService;

/** Generated Domain dependency Client를 실제 업무 Bean 주입 경로에서 소비하는 Sample Service입니다. */
@CpfService
public class DomainDependencySampleService {{
{chr(10).join(fields)}
    public DomainDependencySampleService({', '.join(params)}) {{
{chr(10).join(assigns)}
    }}
{chr(10).join(methods)}
}}
'''



def render_sample_business_domain_operations(d: DomainDefinition) -> dict[str,str]:
    """@CpfOnlineTransaction 6개 Sample 업무 Operation을 Same-JVM/Remote 공통 typed contract로 노출합니다."""
    pkg=f"{d.feature_package}.operation"
    service=f"{d.feature_package}.service.SampleTransactionService"
    dto=f"{d.feature_package}.dto"
    model=f"{d.feature_package}.model.SampleItem"
    specs=[
      ("Create","CREATE",f"{dto}.CreateSampleRequest",model,"service.create(request)"),
      ("Detail","DETAIL",f"{dto}.SampleIdRequest",model,"service.detail(request.id())"),
      ("Search","SEARCH",f"{dto}.SampleSearchRequest",f"{dto}.SamplePage","service.search(request)"),
      ("Slice","SLICE",f"{dto}.SampleSearchRequest",f"{dto}.SampleSlice","service.slice(request)"),
      ("Update","UPDATE",f"{dto}.UpdateSampleCommand",model,"service.update(request.id(), request.request())"),
      ("Delete","DELETE",f"{dto}.DeleteSampleCommand",model,"service.delete(request.id(), request.request())"),
    ]
    out={}
    for suffix,op,req_fq,resp_fq,invoke in specs:
        req=req_fq.rsplit('.',1)[-1]; resp=resp_fq.rsplit('.',1)[-1]
        cls=f"Sample{suffix}DomainOperation"
        content=(
            f"package {pkg};\n\n"
            f"import com.cpf.core.api.result.CpfResult;\n"
            f"import com.cpf.integration.api.domaincall.CpfDomainOperation;\n"
            f"import {service};\nimport {req_fq};\nimport {resp_fq};\n"
            f"import org.springframework.stereotype.Component;\n\n"
            f"/** @CpfOnlineTransaction {d.sample_tx_id}_{op}의 typed Same-JVM/Remote 공통 adapter입니다. */\n"
            # Spring AOP observes this canonical operation boundary. CGLIB is the repository-wide
            # proxy strategy, so generated operation adapters must remain proxyable.
            f"@Component\npublic class {cls} implements CpfDomainOperation<{req}, {resp}> {{\n"
            f"    private final SampleTransactionService service;\n"
            f"    public {cls}(SampleTransactionService service) {{ this.service=service; }}\n"
            f"    @Override public String systemCode() {{ return \"{d.system_code}\"; }}\n"
            f"    @Override public String operationId() {{ return \"{d.sample_tx_id}_{op}\"; }}\n"
            f"    @Override public Class<{req}> requestType() {{ return {req}.class; }}\n"
            f"    @Override public Class<{resp}> responseType() {{ return {resp}.class; }}\n"
            f"    @Override public CpfResult<{resp}> invoke({req} request) {{ return CpfResult.success({invoke}); }}\n"
            f"}}\n"
        )
        out[f"online/src/main/java/{d.feature_path.as_posix()}/operation/{cls}.java"]=content
    return out

def render_domain_binding_profile(d: DomainDefinition, profile: str) -> str:
    if not d.domain_dependencies: return ""
    lines=["cpf:", "  integration:", "    domain-call:", "      bindings:"]
    for dep in d.domain_dependencies:
        code=dep.system_code
        if profile in {"local","test"}:
            lines += [f"        {code}:", "          # 동일 JVM operation이 있으면 LOCAL, 없으면 Registry 기반 REMOTE를 선택합니다.", "          mode: AUTO", f"          service-id: {code}"]
        else:
            lines += [f"        {code}:", "          # 공유/운영 환경은 topology와 serviceId를 Deployment Binding으로 명시합니다.", f"          mode: ${{CPF_DOMAIN_{code}_MODE}}", f"          service-id: ${{CPF_DOMAIN_{code}_SERVICE_ID}}"]
    return "\n".join(lines)+"\n"


def render_application_yml(d: DomainDefinition, kind: str) -> str:
    """Generated runtime 공통설정. batch는 web server를 띄우지 않는 기본 Golden Path입니다."""
    prefix=d.system_code
    datasource = ""
    domain_persistence = ""
    mybatis = ""
    if d.persistence != "none":
        datasource = ("  datasource:\n"
                      f"    # 운영 Secret/Endpoint는 환경 Binding으로 주입하며 Sample credential fallback을 두지 않습니다.\n"
                      f"    url: ${{{prefix}_DATASOURCE_URL}}\n"
                      f"    username: ${{{prefix}_DATASOURCE_USERNAME}}\n"
                      f"    password: ${{{prefix}_DATASOURCE_PASSWORD}}\n"
                      f"    driver-class-name: ${{{prefix}_DATASOURCE_DRIVER}}\n")
        domain_persistence = ("  domain:\n"
                              "    persistence:\n"
                              "      enabled: true\n"
                              "      required: true\n"
                              f"      provider: {d.persistence}\n"
                              "      data-source-prefix: spring.datasource\n")
        if d.persistence == "mybatis":
            mybatis = "mybatis:\n  # Oracle/PostgreSQL/MariaDB 공통 Mapper만 로드하며 Vendor별 Business SQL 복제를 금지합니다.\n  mapper-locations: classpath*:db/mapper/*.xml\n"
    web_mode = "  main:\n    web-application-type: none\n" if kind == "batch" else ""
    return ("spring:\n"
            "  application:\n"
            f"    name: {d.name}-{kind}\n"
            + web_mode
            + datasource +
            "cpf:\n"
            "  generated-domain:\n"
            f"    name: {d.name}\n"
            f"    # 거래 식별용 System Code입니다. DB 물리 Prefix는 Generator/Canonical DB 계약으로 고정됩니다.\n"
            f"    system-code: ${{CPF_SYSTEM_CODE:{d.system_code}}}\n"
            "    database-role: CUSTOMER_BUSINESS_DB\n"
            f"    table-prefix: {d.table_prefix}\n"
            + domain_persistence +
            "  operation-policy:\n"
            "    seed:\n"
            "      # 최초 Operation 등록 때만 적용되며, 이후 ADM Policy를 restart/redeploy가 덮지 않습니다.\n"
            "      allowed-callers: ${CPF_OPERATION_POLICY_SEED_ALLOWED_CALLERS:}\n"
            "      source: YML\n"
            "      revision: ${CPF_OPERATION_POLICY_SEED_REVISION:GENERATED-1}\n"
            "  logging:\n"
            "    # 일반 Runtime 로그는 Application/Instance별 경로로 분리하며 Transaction Evidence와 혼용하지 않습니다.\n"
            "    enabled: true\n"
            "    root: ${CPF_LOG_ROOT:logs}\n"
            "    instance-id: ${CPF_RUNTIME_INSTANCE_ID:${HOSTNAME:local}}\n"
            "    maintenance-interval: ${CPF_LOG_MAINTENANCE_INTERVAL:1h}\n"
            "    files:\n"
            "      runtime:\n"
            "        # 전체 Runtime 흐름을 기록하는 기본 파일입니다. Console 출력도 함께 유지됩니다.\n"
            "        enabled: true\n"
            "        file-name: runtime.log\n"
            "        rolling: DAILY\n"
            "        compress-after-days: 5\n"
            "        delete-after-days: 365\n"
            "      error:\n"
            "        # ERROR 이상을 별도 파일에도 기록하여 운영 장애 탐색 경로를 명확히 합니다.\n"
            "        enabled: true\n"
            "        file-name: error.log\n"
            "        level: ERROR\n"
            "        rolling: DAILY\n"
            "        compress-after-days: 5\n"
            "        delete-after-days: 365\n"
            + mybatis)

def render_profile_yml(d: DomainDefinition, kind: str, profile: str) -> str:
    """local/test는 안전한 기본을 제공하고 dev/stg/prod는 누락 Binding을 fail-fast하게 유지합니다."""
    prefix=d.system_code
    levels={"local":"TRACE","dev":"DEBUG","test":"DEBUG","stg":"INFO","prod":"INFO"}
    if profile not in levels:
        raise DomainError(f"지원하지 않는 profile: {profile}")
    logging_profile=(
        "# 환경별 기본 레벨은 개발 추적성과 운영 안정성의 Canonical 기준이며 CPF_LOG_LEVEL로 명시 조정할 수 있습니다.\n"
        "logging:\n"
        "  level:\n"
        f"    root: ${{CPF_LOG_LEVEL:{levels[profile]}}}\n"
    )
    if kind == "batch":
        note = "# Batch Generated Domain은 non-web Runtime이며 Domain Call binding은 Online과 같은 Canonical profile 계약을 사용합니다.\n"
        return note + logging_profile + render_domain_binding_profile(d, profile)
    if kind not in {"api","online"}: raise DomainError(f"지원하지 않는 Generated Domain profile module: {kind}")
    env_key=f"{prefix}_ONLINE_PORT"
    local_port=d.local_online_port
    if profile == "local":
        # local-integrated에서는 모든 Generated Domain이 같은 HTTP Port를 사용합니다.
        # local-distributed를 선택했을 때만 Domain별 stable port가 fallback으로 사용됩니다.
        port_expr=f"${{CPF_LOCAL_SHARED_PORT:${{{env_key}:{local_port}}}}}"
        note="# Local 통합모드는 CPF_LOCAL_SHARED_PORT 한 개를 사용하고, 분산 검증에서만 Domain별 stable Port를 사용합니다.\n"
    elif profile == "test":
        port_expr="0"
        note="# 자동화 Test는 ephemeral port를 사용합니다. 실제 외부기관 연결을 구성하지 않습니다.\n"
    elif profile in {"dev","stg","prod"}:
        port_expr=f"${{{env_key}}}"
        note="# 공유/운영 환경은 명시적 Deployment Binding이 없으면 시작에 실패하도록 기본값을 두지 않습니다.\n"
    return note + logging_profile + "server:\n" + f"  port: {port_expr}\n" + render_domain_binding_profile(d, profile)

def _render_db_template(root: Path, d: DomainDefinition, vendor: str, relative_path: str) -> str:
    """Render the DB Tool-owned canonical vendor template for one generated domain.

    The Generator must not own a second DDL model.  Logical tables and columns come
    from ``cpf-tools/db/canonical/generated-domain-schema.json`` and are projected
    into the five DB resources by the central DB renderer.
    """
    if vendor not in SUPPORTED_VENDORS:
        raise DomainError(f"지원하지 않는 Generated Domain DB Vendor입니다: {vendor}")
    template = _generator_resource_root(root) / "cpf-tools" / "db" / "generated" / "domain-template" / vendor / relative_path
    if not template.is_file():
        raise DomainError(f"Generated Domain canonical DB template이 없습니다: {template}")
    rendered = template.read_text(encoding="utf-8-sig")
    replacements = {
        "@CPF_VENDOR@": vendor,
        "@CPF_DOMAIN@": d.name,
        "@CPF_SYSTEM_CODE@": d.system_code,
        "@CPF_DISPLAY_NAME@": d.class_name,
        "@CPF_MODULE_NAME@": d.module_name,
        "@CPF_PACKAGE_NAME@": d.package_name,
        "@CPF_TABLE_PREFIX@": d.table_prefix,
        "@CPF_MAPPER_NAMESPACE@": f"{d.feature_package}.repository.SampleTransactionMapper",
        "@CPF_MAPPER_NAME@": "SampleTransactionMapper",
        "@CPF_RESULT_TYPE@": f"{d.feature_package}.model.SampleItem",
        "@CPF_IDEMPOTENCY_RESULT_TYPE@": f"{d.feature_package}.model.SampleIdempotencyRecord",
    }
    for token, value in replacements.items():
        rendered = rendered.replace(token, value)
    unresolved = sorted(set(re.findall(r"@CPF_[A-Z_]+@", rendered)))
    if unresolved:
        raise DomainError(f"Generated Domain DB template token 미해결: {template}: {unresolved}")
    return rendered.replace("\r\n", "\n").rstrip() + "\n"


def _ddl(root: Path, d: DomainDefinition, vendor: str) -> str:
    return _render_db_template(root, d, vendor, "install/10_empty_install.sql.template")


def _migration(root: Path, d: DomainDefinition, vendor: str) -> str:
    return _render_db_template(root, d, vendor, "migration/V1____DOMAIN___domain.sql.template")


def _seed(root: Path, d: DomainDefinition, vendor: str) -> str:
    return _render_db_template(root, d, vendor, "seed/20_product_seed.sql.template")


def _rollback(root: Path, d: DomainDefinition, vendor: str) -> str:
    return _render_db_template(root, d, vendor, "rollback/R1__remove___DOMAIN___domain.sql.template")


def _verify_sql(root: Path, d: DomainDefinition, vendor: str) -> str:
    return _render_db_template(root, d, vendor, "verify/90_verify.sql.template")


def render_readme(d: DomainDefinition, deps: dict[str,list[str]]) -> str:
    return f'''# {d.name} Generated Customer Domain

이 Root는 CPF Product Module이 아니라 Developer-Facing `gradle.properties` 계약에서 동일 Domain-neutral Generator로 생성된 Customer Business Domain입니다.

- systemCode: `{d.system_code}`
- packageName: `{d.package_name}`
- databaseRole: `CUSTOMER_BUSINESS_DB`
- tablePrefix: `{d.table_prefix}`
- preset: `{d.preset}`
- sampleTransaction: `{d.sample_tx_id}`
- domainDependencies: `{', '.join(x.system_code + ':' + '|'.join(x.operations) for x in d.domain_dependencies) or 'none'}`
- externalClients: `{', '.join(x.client_id for x in d.external_clients) or 'none'}`
- API direct CPF dependencies: `{', '.join(deps.get('api',[]))}`
- Batch Module: `modules.batch=true`일 때만 `batch/`를 생성하고 Public `cpf-starter-batch`를 소비

Generated Source를 수동 복제하여 다른 Domain을 만들지 않습니다. 새 Domain은 `cpf domain create/setup`으로 생성합니다.
'''


def _feature_java(content: str, d: DomainDefinition, role: str | None = None) -> str:
    """Legacy renderer의 의미를 재사용하면서 Feature-First package로 투영합니다.

    Domain → Business Feature → Technical Role 순서를 강제합니다. Domain-wide Base와
    Application entry만 Feature 밖에 두고, 실제 업무 Source는 모두 primary feature 아래에 둡니다.
    """
    feature=f"{d.package_name}.{d.primary_feature}"
    replacements = {
        f"{d.package_name}.common.base": f"{d.package_name}.base",
        f"{d.package_name}.api.base": f"{d.package_name}.base",
        f"{d.package_name}.common.model": f"{feature}.model",
        f"{d.package_name}.common.dto": f"{feature}.dto",
        f"{d.package_name}.common.policy": f"{feature}.service",
        f"{d.package_name}.common.mapper": f"{feature}.repository",
        f"{d.package_name}.common.repository": f"{feature}.repository",
        f"{d.package_name}.common.audit": f"{feature}.service",
        f"{d.package_name}.api.service": f"{feature}.service",
        f"{d.package_name}.api.controller": f"{feature}.controller",
    }
    for source,target in replacements.items(): content=content.replace(source,target)
    if role is None:
        content=content.replace(f"{d.package_name}.api",d.package_name)
    elif role in {"client","operation"}:
        content=content.replace(f"{d.package_name}.online.domaincall",f"{feature}.{role}")
    content=content.replace(f"{d.class_name}ApiApplication",f"{d.class_name}OnlineApplication")
    return content


def _transient_root(root: Path, d: DomainDefinition) -> Path:
    return managed_generator_root(root)/"verification"/f"cpf-{d.name}"


def _external_client_properties(d: DomainDefinition, client: ExternalClientDefinition) -> str:
    c=client.class_name
    prefix=f"cpf.domain.{d.system_code.lower()}.external.{client.client_id}"
    return f'''package {d.feature_package}.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** {client.name} 외부연계의 환경 Binding입니다. Secret 원문은 이 객체에 저장하지 않습니다. */
@Component
@ConfigurationProperties(prefix="{prefix}")
public class {c}ExternalClientProperties {{
    private String endpoint="";
    private String destination="";
    private String bucket="";
    private String layoutId="";
    private String layoutVersion="";
    public String getEndpoint(){{return endpoint;}} public void setEndpoint(String v){{endpoint=safe(v);}}
    public String getDestination(){{return destination;}} public void setDestination(String v){{destination=safe(v);}}
    public String getBucket(){{return bucket;}} public void setBucket(String v){{bucket=safe(v);}}
    public String getLayoutId(){{return layoutId;}} public void setLayoutId(String v){{layoutId=safe(v);}}
    public String getLayoutVersion(){{return layoutVersion;}} public void setLayoutVersion(String v){{layoutVersion=safe(v);}}
    private static String safe(String v){{return v==null?"":v.trim();}}
}}
'''


def _external_client_exception(d: DomainDefinition, client: ExternalClientDefinition) -> str:
    c=client.class_name
    return f'''package {d.feature_package}.client;

/** Provider 예외를 업무 Domain에 직접 노출하지 않는 {client.name} 외부연계 오류 경계입니다. */
public final class {c}ExternalClientException extends RuntimeException {{
    public {c}ExternalClientException(String message, Throwable cause) {{ super(message, cause); }}
}}
'''


def _external_client_contract(d: DomainDefinition, client: ExternalClientDefinition) -> str:
    c=client.class_name
    if client.capability=='http':
        return f'''package {d.feature_package}.client;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;

/** {client.name} HTTP 외부계약의 generated typed Client입니다. */
public interface {c}ExternalClient {{ {c}Response execute({c}Request request); }}
record {c}Request(String payload) implements CpfRequest {{ }}
record {c}Response(String payload) implements CpfResponse {{ }}
'''
    if client.capability=='messaging':
        return f'''package {d.feature_package}.client;

import com.cpf.messaging.api.CpfBrokerPublishResult;

/** {client.name} Messaging 외부계약의 generated typed Client입니다. */
public interface {c}ExternalClient {{ CpfBrokerPublishResult publish(String messageId, byte[] payload); }}
'''
    if client.capability=='object-storage':
        return f'''package {d.feature_package}.client;

import com.cpf.file.objectstorage.api.CpfObjectStorageMetadata;
import java.util.Optional;

/** {client.name} Object Storage 외부계약의 generated typed Client입니다. */
public interface {c}ExternalClient {{ Optional<CpfObjectStorageMetadata> head(String tenantId, String objectKey); }}
'''
    return f'''package {d.feature_package}.client;

import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
import java.util.Map;

/** {client.name} 고정길이 외부계약의 generated typed Client입니다. */
public interface {c}ExternalClient {{ CpfFixedLengthParseResult exchange(Map<String,?> fields); }}
'''


def _external_client_adapter(d: DomainDefinition, client: ExternalClientDefinition) -> str:
    c=client.class_name; cid=client.client_id
    action=(f"EXTERNAL_{cid}_{client.capability}").upper().replace('-','_')
    annotations=f'''@CpfClient(system="{cid}", operation="{client.capability}", sideEffecting=true, contextRequired=true)\n@CpfLogging(operation="external.{cid}.{client.capability}")\n@CpfAudit(action="{action}")'''
    imports='''import com.cpf.foundation.annotation.CpfLogging;\nimport com.cpf.integration.api.annotation.CpfClient;\nimport com.cpf.integration.api.annotation.CpfRetry;\nimport com.cpf.integration.api.annotation.CpfTimeout;\nimport com.cpf.platform.operations.api.annotation.CpfAudit;\nimport org.springframework.stereotype.Component;'''
    resilience=f'''    @CpfRetry(name="external-{cid}", maxAttempts=1, delayMillis=0, reconcileUnknownOutcome=false)\n    @CpfTimeout(name="external-{cid}")'''
    if client.capability=='http':
        return f'''package {d.feature_package}.client;

{imports}
import com.cpf.integration.api.http.CpfServiceClient;

/** CPF Context/Timeout/Logging/Audit를 적용하는 {client.name} HTTP generated adapter입니다. */
@Component
{annotations}
public final class Default{c}ExternalClient implements {c}ExternalClient {{
    private final CpfServiceClient<{c}Request,{c}Response> transport;
    public Default{c}ExternalClient(CpfServiceClient<{c}Request,{c}Response> transport) {{ this.transport=transport; }}
{resilience}
    @Override public {c}Response execute({c}Request request) {{
        try {{ return transport.execute(request); }} catch(RuntimeException ex) {{ throw new {c}ExternalClientException("{cid} HTTP call failed",ex); }}
    }}
}}
'''
    if client.capability=='messaging':
        return f'''package {d.feature_package}.client;

{imports}
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.api.CpfMessagingTemplate;
import java.util.Map;

/** CPF Context/Timeout/Logging/Audit를 적용하는 {client.name} Messaging generated adapter입니다. */
@Component
{annotations}
public final class Default{c}ExternalClient implements {c}ExternalClient {{
    private final CpfMessagingTemplate messaging; private final {c}ExternalClientProperties properties;
    public Default{c}ExternalClient(CpfMessagingTemplate messaging,{c}ExternalClientProperties properties){{this.messaging=messaging;this.properties=properties;}}
{resilience}
    @Override public CpfBrokerPublishResult publish(String messageId,byte[] payload){{
        if(properties.getDestination().isBlank()) throw new IllegalStateException("{cid} destination binding required");
        try {{ return messaging.send(new CpfBrokerPublishRequest(messageId,properties.getDestination(),messageId,payload,"application/octet-stream","{d.system_code}","{cid}",messageId,Map.of(),Map.of())); }}
        catch(RuntimeException ex) {{ throw new {c}ExternalClientException("{cid} messaging call failed",ex); }}
    }}
}}
'''
    if client.capability=='object-storage':
        return f'''package {d.feature_package}.client;

{imports}
import com.cpf.file.objectstorage.api.CpfObjectStorageMetadata;
import com.cpf.file.objectstorage.api.CpfObjectStorageOperations;
import java.util.Optional;

/** CPF Context/Timeout/Logging/Audit를 적용하는 {client.name} Object Storage generated adapter입니다. */
@Component
{annotations}
public final class Default{c}ExternalClient implements {c}ExternalClient {{
    private final CpfObjectStorageOperations storage; private final {c}ExternalClientProperties properties;
    public Default{c}ExternalClient(CpfObjectStorageOperations storage,{c}ExternalClientProperties properties){{this.storage=storage;this.properties=properties;}}
{resilience}
    @Override public Optional<CpfObjectStorageMetadata> head(String tenantId,String objectKey){{
        if(properties.getBucket().isBlank()) throw new IllegalStateException("{cid} bucket binding required");
        try {{ return storage.head(tenantId,properties.getBucket(),objectKey); }} catch(RuntimeException ex) {{ throw new {c}ExternalClientException("{cid} object-storage call failed",ex); }}
    }}
}}
'''
    return f'''package {d.feature_package}.client;

{imports}
import com.cpf.integration.fixedlength.api.CpfFixedLengthOperations;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
import java.util.Map;

/** 실제 Transport는 기관별 Provider가 구현하고 CPF fixed-length codec 계약은 generated adapter가 소비합니다. */
interface {c}FixedLengthTransport {{ String exchange(String requestMessage); }}

/** CPF Context/Timeout/Logging/Audit와 fixed-length codec을 적용하는 {client.name} generated adapter입니다. */
@Component
{annotations}
public final class Default{c}ExternalClient implements {c}ExternalClient {{
    private final CpfFixedLengthOperations codec; private final {c}FixedLengthTransport transport; private final {c}ExternalClientProperties properties;
    public Default{c}ExternalClient(CpfFixedLengthOperations codec,{c}FixedLengthTransport transport,{c}ExternalClientProperties properties){{this.codec=codec;this.transport=transport;this.properties=properties;}}
{resilience}
    @Override public CpfFixedLengthParseResult exchange(Map<String,?> fields){{
        if(properties.getLayoutId().isBlank()||properties.getLayoutVersion().isBlank()) throw new IllegalStateException("{cid} fixed-length layout binding required");
        try {{ String request=codec.write(fields,properties.getLayoutId(),properties.getLayoutVersion()).message(); String response=transport.exchange(request); return codec.parse(response,properties.getLayoutId(),properties.getLayoutVersion()); }}
        catch(RuntimeException ex) {{ throw new {c}ExternalClientException("{cid} fixed-length call failed",ex); }}
    }}
}}
'''


def _external_client_contract_test(d: DomainDefinition, client: ExternalClientDefinition) -> str:
    c=client.class_name
    method='execute' if client.capability=='http' else ('publish' if client.capability=='messaging' else ('head' if client.capability=='object-storage' else 'exchange'))
    return f'''package {d.feature_package}.client;

import com.cpf.foundation.annotation.CpfLogging;
import com.cpf.integration.api.annotation.CpfClient;
import com.cpf.integration.api.annotation.CpfRetry;
import com.cpf.integration.api.annotation.CpfTimeout;
import com.cpf.platform.operations.api.annotation.CpfAudit;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Generated external client가 CPF의 필수 운영 경계를 실제 소비하는지 검증합니다. */
class {c}ExternalClientContractTest {{
    @Test void exposesCpfOperationalContracts() throws Exception {{
        Class<?> type=Default{c}ExternalClient.class;
        assertThat(type.getAnnotation(CpfClient.class)).isNotNull();
        assertThat(type.getAnnotation(CpfLogging.class)).isNotNull();
        assertThat(type.getAnnotation(CpfAudit.class)).isNotNull();
        var method=java.util.Arrays.stream(type.getDeclaredMethods()).filter(m->m.getName().equals("{method}")).findFirst().orElseThrow();
        assertThat(method.getAnnotation(CpfRetry.class)).isNotNull();
        assertThat(method.getAnnotation(CpfTimeout.class)).isNotNull();
    }}
}}
'''


def render_external_client_files(d: DomainDefinition, client: ExternalClientDefinition) -> dict[str,str]:
    base=f"online/src/main/java/{d.feature_path.as_posix()}/client"
    test=f"online/src/test/java/{d.feature_path.as_posix()}/client"
    c=client.class_name
    return {
        f"{base}/{c}ExternalClient.java": _external_client_contract(d,client),
        f"{base}/{c}ExternalClientProperties.java": _external_client_properties(d,client),
        f"{base}/{c}ExternalClientException.java": _external_client_exception(d,client),
        f"{base}/Default{c}ExternalClient.java": _external_client_adapter(d,client),
        f"{test}/{c}ExternalClientContractTest.java": _external_client_contract_test(d,client),
    }


def render_files(root: Path, d: DomainDefinition, catalog: dict[str,Any]) -> tuple[dict[str,str],dict[str,list[str]]]:
    """개발자가 실제 수정/사용할 Feature-First 최소 Source Surface만 생성한다."""
    stack=read_stack(root)
    online_deps=direct_dependencies(d,"api",catalog)
    batch_deps=direct_dependencies(d,"batch",catalog) if d.batch else []
    workspace_defs=_load_workspace_definitions(root,d)
    dependency_targets=[]
    for dependency in d.domain_dependencies:
        target=workspace_defs.get(dependency.name)
        if target is None: raise DomainError(f"Domain dependency target missing during render: {dependency.name}")
        dependency_targets.append(target)
    deps={"online":online_deps, **({"batch":batch_deps} if d.batch else {})}
    p=d.package_path.as_posix()
    feature=d.primary_feature
    fp=f"{p}/{feature}"
    files: dict[str,str]={
      "settings.gradle":render_root_settings(d,dependency_targets),
      "build.gradle":render_root_build(d,stack),
      "gradle.properties":render_gradle_properties(d,stack),
    }
    if d.online:
        files["online/build.gradle"]=render_app_build(
            d,"online",online_deps,stack,(target.package_name for target in dependency_targets))
        files[f"online/src/main/java/{p}/base/{d.class_name}BaseController.java"]=_feature_java(render_domain_base_controller(d),d)
        files[f"online/src/main/java/{p}/base/{d.class_name}BaseService.java"]=_feature_java(render_domain_base_service(d),d)
        if d.persistence != "none":
            files[f"online/src/main/java/{p}/base/{d.class_name}BaseRepository.java"]=_feature_java(render_domain_base_repository(d),d)
        files[f"online/src/main/java/{p}/{d.class_name}OnlineApplication.java"]=_feature_java(render_api_application(d),d)
        files["online/src/main/resources/application.yml"]=render_application_yml(d,"api").replace(f"{d.name}-api",f"{d.name}-online")
        for profile in ("local","test","dev","stg","prod"):
            files[f"online/src/main/resources/application-{profile}.yml"]=render_profile_yml(d,"api",profile)

        # Runtime/Domain invocation 역시 해당 업무 Feature Owner 아래 둔다.
        files[f"online/src/main/java/{fp}/operation/{d.class_name}DomainPingOperation.java"]=_feature_java(render_domain_ping_operation(d,"online"),d,"operation")
        # 첫 Feature 외에 명시된 Business Feature도 독립 package boundary를 실제 Source로 materialize합니다.
        for extra_feature in d.business_features[1:]:
            extra_path=f"{p}/{extra_feature}"
            extra_class="".join(part[:1].upper()+part[1:] for part in re.split(r"[-_]", extra_feature) if part)
            files[f"online/src/main/java/{extra_path}/operation/{extra_class}FeatureScaffold.java"]=(
                f"package {d.package_name}.{extra_feature}.operation;\n\n"
                f"/** {extra_feature} 업무 Feature의 Generator-owned 확장 시작점입니다. 실제 업무 코드는 이 Feature 아래 역할별로 추가합니다. */\n"
                f"public interface {extra_class}FeatureScaffold {{ }}\n"
            )
        dependency_contracts={}
        for dependency in d.domain_dependencies:
            target=workspace_defs.get(dependency.name)
            if target is None: raise DomainError(f"Domain dependency target missing during render: {dependency.name}")
            contracts=discover_domain_operation_contracts(root,target)
            dependency_contracts[dependency.name]=contracts
            files[f"online/src/main/java/{fp}/client/{dependency.class_name}DomainClient.java"]=_feature_java(render_domain_dependency_client(d,"online",dependency,contracts),d,"client")
            files[f"online/src/main/java/{fp}/client/Default{dependency.class_name}DomainClient.java"]=_feature_java(render_domain_dependency_adapter(d,"online",dependency,contracts),d,"client")
        if d.domain_dependencies:
            consumer=_feature_java(render_domain_dependency_consumer(d,"online",dependency_contracts),d,"client").replace(
                f"package {d.feature_package}.client;",f"package {d.feature_package}.service;"
            )
            client_imports="\n".join(f"import {d.feature_package}.client.{dep.class_name}DomainClient;" for dep in d.domain_dependencies)
            consumer=consumer.replace("import com.cpf.core.api.result.CpfResult;", "import com.cpf.core.api.result.CpfResult;\n"+client_imports)
            files[f"online/src/main/java/{fp}/service/DomainDependencySampleService.java"]=consumer
        for external_client in d.external_clients:
            files.update(render_external_client_files(d, external_client))

        if d.sample_transaction:
            files[f"online/src/main/java/{fp}/model/SampleItem.java"]=_feature_java(render_model(d),d)
            # Durable idempotency record is a business support model, not an HTTP DTO.
            requests=render_requests(d)
            idem=requests.pop("SampleIdempotencyRecord.java")
            files[f"online/src/main/java/{fp}/model/SampleIdempotencyRecord.java"]=_feature_java(idem,d).replace(f"package {d.feature_package}.dto;",f"package {d.feature_package}.model;")
            for fn,content in requests.items():
                files[f"online/src/main/java/{fp}/dto/{fn}"]=_feature_java(content,d)
            files[f"online/src/main/java/{fp}/service/SampleTransactionPolicy.java"]=_feature_java(render_policy(d),d)
            files[f"online/src/main/java/{fp}/repository/SampleTransactionMapper.java"]=_feature_java(render_mapper(d),d)
            files[f"online/src/main/java/{fp}/repository/SampleTransactionRepository.java"]=_feature_java(render_repository(d),d)
            files[f"online/src/main/java/{fp}/service/DomainAuditLogger.java"]=_feature_java(render_audit(d),d)
            files[f"online/src/main/java/{fp}/service/SampleTransactionService.java"]=_feature_java(render_service(d),d)
            files[f"online/src/main/java/{fp}/controller/SampleTransactionController.java"]=_feature_java(render_controller(d),d)
            files[f"online/src/test/java/{fp}/service/SampleTransactionPolicyTest.java"]=_feature_java(render_policy_test(d),d)
            files[f"online/src/test/java/{fp}/controller/SampleTransactionControllerContractTest.java"]=_feature_java(render_api_contract_test(d),d)
            files.update(render_sample_business_domain_operations(d))

    # DB3는 Generated Domain Source Tree가 아니라 Canonical DB Renderer/Installer가 소유합니다.
    # Domain별 Vendor/Host/Schema는 local/environment DB profile에 바인딩하고 Source에는 vendor 폴더를 생성하지 않습니다.

    if d.batch:
        files["batch/build.gradle"]=render_app_build(d,"batch",batch_deps,stack)
        files["batch/src/main/resources/application.yml"]=render_application_yml(d,"batch")
        for profile in ("local","test","dev","stg","prod"):
            files[f"batch/src/main/resources/application-{profile}.yml"]=render_profile_yml(d,"batch",profile)
        app_pkg=d.package_name
        app_path=d.package_path.as_posix()
        files[f"batch/src/main/java/{app_path}/{d.class_name}BatchApplication.java"]=(
            f"package {app_pkg};\n\nimport org.springframework.boot.SpringApplication;\nimport org.springframework.boot.autoconfigure.SpringBootApplication;\n\n/** {d.name} Generated Domain의 선택형 Batch Runtime 진입점입니다. */\n@SpringBootApplication(scanBasePackages=\"{app_pkg}\")\npublic class {d.class_name}BatchApplication {{ public static void main(String[] args) {{ SpringApplication.run({d.class_name}BatchApplication.class,args); }} }}\n")
        files[f"batch/src/main/java/{app_path}/{feature}/job/SampleBatchJob.java"]=(
            f"package {app_pkg}.{feature}.job;\n\nimport com.cpf.batch.api.annotation.CpfBatchJob;\nimport com.cpf.batch.api.annotation.CpfBatchStep;\n\n/** Generator가 생성하는 Feature-First Batch Golden Path. 실제 업무 Job은 같은 Feature Owner 아래 역할별로 확장합니다. */\n@CpfBatchJob(value=\"{d.system_code}_SAMPLE_BATCH\", restartable=true)\npublic class SampleBatchJob {{ @CpfBatchStep(value=\"sampleStep\",order=1,idempotent=true) public void execute() {{ }} }}\n")

    for rel in list(files):
        if rel.endswith((".java",".kt")):
            files[rel]=ensure_java_korean_contract_comments(files[rel])
    return files,deps

def normalized_bytes(content: str) -> bytes:
    return content.replace("\r\n","\n").encode("utf-8")


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.replace("\r\n","\n"), encoding="utf-8", newline="\n")


def definition_hash(path: Path) -> str: return sha256_file(path)


def generator_hash() -> str: return sha256_file(Path(__file__))


def domain_contract_hash(d: DomainDefinition) -> str:
    """Path나 임시 입력 형식과 무관한 Developer Domain 의미 계약 hash입니다."""
    return sha256_bytes(json.dumps(dataclasses.asdict(d),ensure_ascii=False,sort_keys=True,separators=(",",":" )).encode("utf-8"))


def _expected_files(root: Path, d: DomainDefinition) -> tuple[dict[str,str],dict[str,list[str]]]:
    """고객 Project에 영구 metadata 없이도 동일 입력에서 동일 파일 집합을 계산한다."""
    return render_files(root,d,load_catalog(root))


def _expected_hashes(root: Path, d: DomainDefinition) -> dict[str,str]:
    files,_=_expected_files(root,d)
    return {rel:sha256_bytes(normalized_bytes(content)) for rel,content in files.items()}


def _write_transient_state(root: Path, definition_path: Path, d: DomainDefinition, result: dict[str,Any], expected: dict[str,str]) -> None:
    """Generator 실행상태는 고객 Source Tree가 아니라 transient build에만 기록한다."""
    out=_transient_root(root,d)
    out.mkdir(parents=True,exist_ok=True)
    payload={
      "stateVersion":"2.0","generatorVersion":GENERATOR_VERSION,"domain":d.name,
      "contractSha256":domain_contract_hash(d),
      "generatorSha256":generator_hash(),"expectedFiles":[{"path":k,"sha256":v} for k,v in sorted(expected.items())],
      "result":result,
    }
    write_text(out/'generation-state.json',json.dumps(payload,ensure_ascii=False,indent=2)+'\n')


def _write_transient_verification(root: Path, d: DomainDefinition, result: dict[str,Any]) -> None:
    """검증 산출물만 새로 쓰고 lifecycle ownership state는 보존한다.

    고객 Project에는 metadata를 저장하지 않는다. 다만 build/ 아래 generation-state는
    upgrade/remove/restore가 사용자 수정 파일을 덮어쓰지 않기 위한 transient 안전장치다.
    """
    out=_transient_root(root,d)
    out.mkdir(parents=True,exist_ok=True)
    db3=out/'db3'
    if db3.exists(): shutil.rmtree(db3)
    db3.mkdir(parents=True,exist_ok=True)
    for vendor in SUPPORTED_VENDORS:
        v=db3/vendor; v.mkdir(parents=True,exist_ok=True)
        write_text(v/'10_empty_install.sql',_ddl(root,d,vendor))
        write_text(v/'20_product_seed.sql',_seed(root,d,vendor))
        write_text(v/f"V1__{d.name}_domain.sql",_migration(root,d,vendor))
        write_text(v/f"R1__remove_{d.name}_domain.sql",_rollback(root,d,vendor))
        write_text(v/'90_verify.sql',_verify_sql(root,d,vendor))
    write_text(out/'verify-result.json',json.dumps(result,ensure_ascii=False,indent=2)+'\n')


def verify_generated(root: Path, definition_path: Path, output: Path, d: DomainDefinition,
                     *, persist_evidence: bool = True) -> dict[str,Any]:
    """Generated Customer Domain의 최소 IA와 실제 Runtime Consumer를 metadata 없이 검증한다."""
    load_catalog(root)
    if not definition_path.is_file(): raise DomainError(f"Generator 입력 계약이 없습니다: {definition_path}")
    if output.name != f"cpf-{d.name}": raise DomainError(f"Generated Root naming 위반: {output.name}")
    forbidden_metadata=[name for name in ('cpf-domain.yaml','cpf-generator.lock.json') if (output/name).exists()]
    if forbidden_metadata: raise DomainError(f'Generated Root의 Generator 내부 metadata 금지: {forbidden_metadata}')
    contract_path=output/'gradle.properties'
    if not contract_path.is_file(): raise DomainError('Generated Root Developer 계약 gradle.properties 누락')
    canonical=load_domain_gradle_contract(contract_path)
    if canonical != d: raise DomainError('Generated Root gradle.properties Developer 계약과 실행 입력 불일치')
    forbidden_roots=['.cpf','README.md','verification','canonical','vendors','db',d.name+'-api',d.name+'-common',d.name+'-online',d.name+'-batch']
    bad=[x for x in forbidden_roots if (output/x).exists()]
    if bad: raise DomainError(f"Generated Customer Domain 최소 IA 위반: {bad}")
    allowed_dirs=({'online'} if d.online else set()) | ({'batch'} if d.batch else set())
    actual_dirs={p.name for p in output.iterdir() if p.is_dir() and p.name not in {'build','.gradle'} and any(x.is_file() for x in p.rglob('*'))}
    extra=sorted(actual_dirs-allowed_dirs)
    if extra: raise DomainError(f"선택하지 않은/비표준 Generated Directory 발견: {extra}")
    for name in sorted(allowed_dirs):
        cap=output/name
        if not cap.is_dir() or not any(x.is_file() for x in cap.rglob('*')): raise DomainError(f"빈 capability directory 금지: {name}")
    for forbidden in ('domain','jobpack'):
        if (output/forbidden).is_dir() and any(x.is_file() for x in (output/forbidden).rglob('*')): raise DomainError(f'Generated Domain에 금지된 module 발견: {forbidden}')
    for required in ('settings.gradle','build.gradle','gradle.properties'):
        if not (output/required).is_file(): raise DomainError(f"Generated Root Gradle 파일 누락: {required}")
    gradle_properties=(output/'gradle.properties').read_text(encoding='utf-8-sig')
    for token in (f'org.gradle.jvmargs={GRADLE_DAEMON_JVMARGS}', 'org.gradle.workers.max=2', 'org.gradle.parallel=false'):
        if token not in gradle_properties:
            raise DomainError(f'Generated Root Gradle resource ceiling 누락: gradle.properties:{token}')
    text='\n'.join(p.read_text(encoding='utf-8-sig',errors='ignore') for p in output.rglob('*') if p.is_file() and not any(part in {'build','.gradle'} for part in p.relative_to(output).parts) and p.stat().st_size<2_000_000)
    if d.database_role!='CUSTOMER_BUSINESS_DB' or 'CUSTOMER_BUSINESS_DB' not in text: raise DomainError('CUSTOMER_BUSINESS_DB 계약 누락')
    if d.sample_transaction:
        for token in (d.sample_tx_id,):
            if token not in text: raise DomainError(f"Generated 핵심 token 누락: {token}")
    for build in output.rglob('build.gradle'):
        bt=build.read_text(encoding='utf-8-sig')
        # Generated Customer Domain은 Canonical Catalog에서 public으로 선언된 Starter만 직접 참조할 수 있습니다.
        public_artifacts=set(public_module_map(load_catalog(root)).keys())
        for match in re.findall(r"com\.cpf\.starter:([a-z0-9-]+)", bt):
            if match not in public_artifacts:
                raise DomainError(f"Internal/non-public direct dependency 발견: {build}:{match}")
    java='\n'.join(p.read_text(encoding='utf-8-sig',errors='ignore') for p in output.rglob('*.java'))
    required=[]
    if d.online: required += ['extends CpfBaseController']
    if d.online and d.sample_transaction: required += ['@CpfController','@CpfOnlineTransaction','@Operation']
    for token in required:
        if token not in java: raise DomainError(f"Generated Runtime Consumer 누락: {token}")
    if d.online:
        java_root=output/'online'/'src/main/java'/d.package_path
        feature_root=java_root/d.primary_feature
        if not feature_root.is_dir(): raise DomainError(f'Feature-First 업무 Feature package 누락: {feature_root}')
        forbidden_layer_first=[java_root/name for name in ('controller','service','repository','client','dto','model','mapper','domaincall')]
        stale=[str(path.relative_to(output)) for path in forbidden_layer_first if path.exists()]
        if stale: raise DomainError(f'Feature보다 상위인 Layer-First package 금지: {stale}')
    if d.sample_transaction:
        policy=output/'online'/'src/main/java'/d.feature_path/'service/SampleTransactionPolicy.java'
        if not policy.is_file() or '@Component' not in policy.read_text(encoding='utf-8-sig'): raise DomainError('SampleTransactionPolicy wiring 누락')
        local_mappers=list(output.rglob('src/main/resources/db/mapper/SampleTransactionMapper.xml'))
        if local_mappers: raise DomainError(f'Generated Source Tree의 Vendor Mapper 적재 금지: {local_mappers}')
        mapper_owner_build=output/'online'/'build.gradle'
        mapper_build_text=mapper_owner_build.read_text(encoding='utf-8-sig')
        for token in ('prepareCpfVendorResources','cpfDbVendor','cpf-generated-domain-dialect/${vendor}',
                      'generated-resources/cpf-vendor'):
            if token not in mapper_build_text:
                raise DomainError(f'선택 Vendor Mapper build overlay 계약 누락: {mapper_owner_build}:{token}')
        for vendor in SUPPORTED_VENDORS:
            install=_ddl(root,d,vendor); seed=_seed(root,d,vendor); migration=_migration(root,d,vendor)
            rollback=_rollback(root,d,vendor); verify=_verify_sql(root,d,vendor)
            if not all(value.strip() for value in (install,seed,migration,rollback,verify)):
                raise DomainError(f"DB3 Renderer resource set 누락: {vendor}")
            rendered='\n'.join((install,seed,migration,rollback,verify))
            for table in (f"{d.table_prefix}_sample_item",f"{d.table_prefix}_sample_item_idem"):
                if table not in rendered: raise DomainError(f"DB3 canonical table 누락: {vendor}:{table}")
    if java and not re.search(r'[가-힣]',java): raise DomainError('Generated Source 한글 주석 누락')
    if d.domain_dependencies:
        target_defs={x.system_code:x for x in _load_workspace_definitions(root,d).values()}
        online_build_text=(output/'online'/'build.gradle').read_text(encoding='utf-8-sig')
        for dependency in d.domain_dependencies:
            target=target_defs.get(dependency.system_code)
            if target is None:
                raise DomainError(f"Generated Domain dependency target definition 누락: {dependency.system_code}")
            coordinate=f'implementation "{target.package_name}:online:1.0.0-SNAPSHOT"'
            if coordinate not in online_build_text:
                raise DomainError(f"Generated Domain target artifact dependency 누락: {dependency.system_code}:{coordinate}")
            contracts=discover_domain_operation_contracts(root,target)
            for token in (f"interface {dependency.class_name}DomainClient", f"class Default{dependency.class_name}DomainClient"):
                if token not in java:
                    raise DomainError(f"Generated Typed Domain Client/Consumer 누락: {token}")
            for operation_id in dependency.operations:
                contract=contracts.get(operation_id)
                if contract is None:
                    raise DomainError(f"Generated Typed Domain Operation contract 누락: {dependency.system_code}/{operation_id}")
                method=contract.method_name
                for token in (method, f'router.invoke("{dependency.system_code}", "{operation_id}"'):
                    if token not in java:
                        raise DomainError(f"Generated Typed Domain Client/Consumer 누락: {dependency.system_code}/{operation_id}:{token}")
                if operation_id != 'ping' and f'router.invoke("{dependency.system_code}", "ping"' in java:
                    raise DomainError(f"Generated Domain Operation ping fallback 금지: {dependency.system_code}/{operation_id}")
    for client in d.external_clients:
        for token in (f"interface {client.class_name}ExternalClient", f"class Default{client.class_name}ExternalClient", "@CpfClient", "@CpfRetry", "@CpfTimeout", "@CpfLogging", "@CpfAudit"):
            if token not in java: raise DomainError(f"Generated External Client runtime contract 누락: {client.name}:{token}")
        props=output/'online'/'src/main/java'/d.feature_path/'client'/f"{client.class_name}ExternalClientProperties.java"
        if not props.is_file() or '@ConfigurationProperties' not in props.read_text(encoding='utf-8-sig'):
            raise DomainError(f"Generated External Client config binding 누락: {client.name}")
        test=output/'online'/'src/test/java'/d.feature_path/'client'/f"{client.class_name}ExternalClientContractTest.java"
        if not test.is_file(): raise DomainError(f"Generated External Client test skeleton 누락: {client.name}")
        for profile in ("local","test","dev","stg","prod"):
            for runtime in (["online"] + (["batch"] if d.batch else [])):
                profile_text=(output/runtime/'src/main/resources'/f'application-{profile}.yml').read_text(encoding='utf-8-sig')
                for dependency in d.domain_dependencies:
                    if dependency.system_code not in profile_text or 'domain-call:' not in profile_text:
                        raise DomainError(f"Generated Domain Binding profile 누락: {runtime}/{profile}/{dependency.system_code}")
    result={'domain':d.name,'status':'PASS','physicalRoot':f'cpf-{d.name}','ia':{'online':True,'batch':d.batch,'batchCapabilitySelection':'DEVELOPER_GRADLE_CONTRACT'},'developerContract':'gradle.properties','generatorMetadata':'ABSENT','publicBoundary':'PASS','db3Renderer':'EXTERNAL_CANONICAL_RENDERER' if d.persistence != 'none' else 'NOT_APPLICABLE','runtimeConsumers':'BUILD_GENERATED_DESCRIPTOR','generatedFiles':sum(1 for p in output.rglob('*') if p.is_file() and not any(part in {'build','.gradle'} for part in p.relative_to(output).parts))}
    if persist_evidence:
        _write_transient_verification(root,d,result)
    return result


def _materialize(root: Path, definition_path: Path, output: Path, d: DomainDefinition,
                 *, persist_state: bool = True) -> dict[str,Any]:
    files,deps=_expected_files(root,d)
    output.mkdir(parents=True,exist_ok=True)
    for rel,content in files.items(): write_text(output/rel,content)
    result=verify_generated(root,definition_path,output,d,persist_evidence=persist_state)
    if persist_state:
        expected_hashes=_expected_hashes(root,d)
        _write_transient_state(root,definition_path,d,result,expected_hashes)
    return result


def _read_ownership_state(root: Path, d: DomainDefinition) -> dict[str,Any]:
    """Source-controlled lock 없이 외부 managed Evidence의 transient ownership만 읽습니다."""
    return _read_transient_state(root,d)


def _read_transient_state(root: Path, d: DomainDefinition) -> dict[str,Any]:
    """Generated Project 밖 build/에 남긴 마지막 ownership state를 fail-closed로 읽는다."""
    path=_transient_root(root,d)/'generation-state.json'
    if not path.is_file():
        raise DomainError(f"안전한 lifecycle 수행에 필요한 transient generation-state가 없습니다: {path}")
    data=json.loads(path.read_text(encoding='utf-8-sig'))
    if data.get('stateVersion') not in {'1.0','2.0'} or data.get('domain')!=d.name:
        raise DomainError(f"지원하지 않는 generation-state입니다: {path}")
    rows=data.get('expectedFiles')
    if not isinstance(rows,list) or any(not isinstance(x,dict) or not x.get('path') or not x.get('sha256') for x in rows):
        raise DomainError(f"generation-state expectedFiles가 손상되었습니다: {path}")
    return data


def _has_materialized_generated_content(output: Path) -> bool:
    """Developer-Facing 파일이 하나라도 있으면 기존 Generated Root로 취급합니다."""
    if not output.is_dir():
        return False
    return any(output.iterdir())


def preflight(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    """쓰기 전에 입력/Starter 조합/식별자/Target collision을 검증한다."""
    d=load_domain_contract(definition_path)
    schema=_generator_resource_root(root)/SCHEMA_REL
    if not schema.is_file(): raise DomainError(f"Canonical input schema가 없습니다: {schema}")
    json.loads(schema.read_text(encoding='utf-8-sig'))
    validate_repository_uniqueness(root,d,output)
    catalog=load_catalog(root)
    if d.online: direct_dependencies(d,'api',catalog)
    if output.name!=f"cpf-{d.name}": raise DomainError(f"Generated Root는 cpf-<domain>이어야 합니다: {output}")
    resolved_root=root.resolve(); resolved_output=output.resolve()
    if resolved_output==resolved_root or resolved_root not in resolved_output.parents:
        raise DomainError(f"Generated Root는 repository root 내부여야 합니다: {output}")
    target_state='ABSENT'
    if output.exists() and _has_materialized_generated_content(output):
        dr=diff(root,definition_path,output)
        if not dr['clean']:
            raise DomainError(f"Generated target path collision 또는 변경이 감지되었습니다: {output}; changed={dr['changed']}; staleGenerated={dr.get('staleGeneratedFiles', [])}; userFiles={dr['extraUserFiles']}")
        target_state='EXISTING_GENERATED'
    return {
      'status':'PREFLIGHT_PASS','domain':d.name,'target':str(output),'targetState':target_state,
      'definitionSha256':definition_hash(definition_path),'schema':'cpf-domain.schema.json',
      'developerContract':'gradle.properties','generatorMetadata':'ABSENT','publicBoundary':'PASS',
      'selectionSummary':_developer_selection_summary(d,catalog)
    }


def dry_run(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    d=load_domain_contract(definition_path)
    validate_repository_uniqueness(root,d,output)
    with tempfile.TemporaryDirectory(prefix=f"cpf-{d.name}-dry-run-") as td:
        stage=Path(td)/f"cpf-{d.name}"
        verify=_materialize(root,definition_path,stage,d,persist_state=False)
        files=sorted(p.relative_to(stage).as_posix() for p in stage.rglob('*') if p.is_file())
        catalog=load_catalog(root)
        return {'domain':d.name,'status':'DRY_RUN_PASS','target':str(output),'plannedFiles':files,
                'selectionSummary':_developer_selection_summary(d,catalog),'verify':verify}


def _legacy_generated_stale_candidate(d: DomainDefinition, rel: str, path: Path) -> bool:
    """Recover ownership only for an exact, reproducible legacy Generator artifact.

    Older Generator versions could overwrite the workspace lock after a Definition
    removed ``domainDependencies``.  That made the orphaned generated consumer look
    like a user file.  We must not classify files by filename alone: reconstruct the
    historical generated body from dependency identity encoded in the generated
    source and require an exact content match.  Any user edit therefore remains
    ``extraUserFiles`` and is never auto-owned/deleted.
    """
    expected_rel=(
        f"online/src/main/java/{d.package_path.as_posix()}/online/"
        f"{d.name.replace('-','')}/service/DomainDependencySampleService.java"
    )
    if rel != expected_rel or d.domain_dependencies or not path.is_file():
        return False
    try:
        actual=path.read_text(encoding='utf-8-sig')
    except OSError:
        return False
    if "Generated Domain dependency Client를 실제 업무 Bean 주입 경로에서 소비하는 Sample Service입니다." not in actual:
        return False
    field_rows=re.findall(
        r"private\s+final\s+(\w+)DomainClient\s+([a-zA-Z][A-Za-z0-9_]*)DomainClient\s*;",
        actual,
    )
    method_rows=re.findall(
        r"/\*\*\s*([A-Z][A-Z0-9]{2})\s+Domain의 Local/Remote 동일 호출을 실제 Consumer로 검증합니다\.\s*\*/\s*"
        r"public\s+CpfResult<CpfDomainPingResponse>\s+probe(\w+)\(",
        actual,
        re.S,
    )
    if not field_rows or len(field_rows) != len(method_rows):
        return False
    codes_by_class={class_name: system_code for system_code,class_name in method_rows}
    dependencies=[]
    for class_name,var_prefix in field_rows:
        system_code=codes_by_class.get(class_name)
        if not system_code:
            return False
        dependency_name=var_prefix.replace('_','-')
        dependencies.append(DomainDependency(dependency_name,system_code,("ping",)))
    legacy=dataclasses.replace(d,domain_dependencies=tuple(dependencies))
    rendered=_feature_java(render_domain_dependency_consumer(legacy,"online"),legacy,"client").replace(
        f"package {legacy.feature_package}.client;",f"package {legacy.feature_package}.service;"
    )
    return actual == rendered


def verify_prebuilt_domain(root: Path, definition_path: Path, output: Path, d: DomainDefinition) -> dict[str,Any]:
    if d.generation_mode != "prebuilt": raise DomainError("prebuilt verifier misuse")
    required=[output/'build.gradle', output/'settings.gradle', output/'gradle.properties']
    if d.online: required.append(output/'online'/'build.gradle')
    missing=[str(p.relative_to(root)) for p in required if not p.is_file()]
    java_count=sum(1 for p in (output/'online'/'src'/'main'/'java').rglob('*.java')) if d.online and (output/'online'/'src'/'main'/'java').exists() else 0
    if missing or (d.online and java_count == 0):
        raise DomainError(f"Prebuilt Domain physical contract 불완전: missing={missing}, javaCount={java_count}")
    contract=load_domain_gradle_contract(output/'gradle.properties')
    if contract != d: raise DomainError(f"Prebuilt Domain Developer 계약 불일치: {output/'gradle.properties'}")
    legacy=[name for name in ('cpf-domain.yaml','cpf-generator.lock.json') if (output/name).exists()]
    if legacy:
        return {'domain':d.name,'status':'VERIFICATION_PENDING_DELETE','output':str(output),'javaCount':java_count,
                'developerContract':str(output/'gradle.properties'),'deleteCandidates':legacy,
                'deletePrecondition':'USER_APPROVED_DELETE_MANIFEST','mutated':False}
    return {'domain':d.name,'status':'PREBUILT_VERIFIED','output':str(output),'javaCount':java_count,'developerContract':str(output/'gradle.properties')}

def diff(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    d=load_domain_contract(definition_path)
    if d.generation_mode == 'prebuilt': return verify_prebuilt_domain(root,definition_path,output,d)
    validate_repository_uniqueness(root,d,output)
    expected=_expected_hashes(root,d); missing=[]; changed=[]
    for rel,digest in sorted(expected.items()):
        p=output/rel
        if not p.is_file(): missing.append(rel)
        elif sha256_file(p)!=digest: changed.append(rel)
    expected_set=set(expected)
    previous_expected: set[str] = set()
    try:
        state = _read_ownership_state(root, d)
        previous_expected = {str(row['path']) for row in state.get('expectedFiles', [])}
    except DomainError:
        previous_expected = set()
    stale_generated=[]; extra_user=[]
    legacy_metadata=[]
    if output.exists():
        for p in output.rglob('*'):
            if not p.is_file(): continue
            rel=p.relative_to(output).as_posix()
            if rel in {'cpf-domain.yaml','cpf-generator.lock.json'}:
                legacy_metadata.append(rel)
                continue
            if rel in expected_set or rel.startswith('build/') or '/build/' in rel or rel.startswith('.gradle/'):
                continue
            if rel in previous_expected or _legacy_generated_stale_candidate(d, rel, p):
                stale_generated.append(rel)
            else:
                extra_user.append(rel)
    clean=not missing and not changed and not stale_generated
    return {'domain':d.name,'missing':missing,'changed':changed,'staleGeneratedFiles':sorted(stale_generated),
            'extraUserFiles':sorted(extra_user),'legacyMetadataFiles':sorted(legacy_metadata),'clean':clean,'metadataRequired':False}


def generate(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    d=load_domain_contract(definition_path)
    if d.generation_mode == 'prebuilt': return verify_prebuilt_domain(root,definition_path,output,d)
    materialized=_has_materialized_generated_content(output)
    if not materialized:
        preflight(root,definition_path,output)
    else:
        validate_repository_uniqueness(root,d,output)
    if output.name!=f"cpf-{d.name}": raise DomainError(f"Generated Root는 cpf-<domain>이어야 합니다: {output}")
    if materialized:
        dr=diff(root,definition_path,output)
        if dr['clean']:
            vr=verify_generated(root,definition_path,output,d)
            expected_hashes=_expected_hashes(root,d)
            _write_transient_state(root,definition_path,d,vr,expected_hashes)
            return {'domain':d.name,'status':'IDEMPOTENT','output':str(output),'diff':dr,'verify':vr}
        raise DomainError(f"기존 Generated Root의 Seed Source가 달라 자동 덮어쓰기를 금지합니다. diff를 확인하세요: {output}")
    result=_materialize(root,definition_path,output,d)
    return {'domain':d.name,'status':'GENERATED','output':str(output),'verify':result}


def regenerate(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    """영구 ownership 없이 fail-closed로 재생성한다.

    현재 Seed Source가 기대 Template과 다르면 사용자 수정/Template drift를 구분할 수 없으므로
    덮어쓰지 않는다. 동일 파일은 유지하고 누락 파일만 복구한다.
    """
    d=load_domain_contract(definition_path)
    validate_repository_uniqueness(root,d,output)
    if not output.is_dir(): raise DomainError(f"regenerate 대상이 없습니다: {output}")
    expected_files,_=_expected_files(root,d); expected_hashes={rel:sha256_bytes(normalized_bytes(c)) for rel,c in expected_files.items()}
    changed=[]
    for rel,digest in sorted(expected_hashes.items()):
        p=output/rel
        if p.is_file() and sha256_file(p)!=digest: changed.append(rel)
    if changed: raise DomainError(f"사용자 변경 또는 Template drift가 감지되어 무상태 regenerate가 덮어쓰지 않습니다: {changed}")
    restored=[]
    for rel,content in expected_files.items():
        p=output/rel
        if not p.exists(): write_text(p,content); restored.append(rel)
    vr=verify_generated(root,definition_path,output,d)
    _write_transient_state(root,definition_path,d,vr,expected_hashes)
    return {'domain':d.name,'status':'REGENERATED','output':str(output),'restored':sorted(restored),'verify':vr}


def upgrade(root: Path, definition_path: Path, output: Path, *, apply_delete: bool = False) -> dict[str,Any]:
    """Transient ownership state를 기준으로 Generated 파일만 안전하게 새 Template으로 승격한다.

    사용자 수정 파일 또는 state 밖 파일은 절대 덮어쓰거나 삭제하지 않는다. 기존 Generated 파일이
    마지막 state hash와 다르면 전체 upgrade를 적용하기 전에 실패시켜 부분 업그레이드를 방지한다.
    """
    d=load_domain_contract(definition_path)
    validate_repository_uniqueness(root,d,output)
    if not output.is_dir(): raise DomainError(f"upgrade 대상이 없습니다: {output}")
    state=_read_ownership_state(root,d)
    previous={str(x['path']):str(x['sha256']) for x in state['expectedFiles']}
    current_files,_=_expected_files(root,d)
    current_hashes={rel:sha256_bytes(normalized_bytes(content)) for rel,content in current_files.items()}
    user_modified=[]
    for rel,digest in sorted(previous.items()):
        # gradle.properties는 Generator state가 아니라 개발자가 의도적으로 관리하는 입력 계약입니다.
        # 유효성/identity/risky-change는 새 DomainDefinition으로 검증하고, Seed Source drift와 혼동하지 않습니다.
        if rel == 'gradle.properties':
            continue
        p=output/rel
        if p.is_file() and sha256_file(p)!=digest: user_modified.append(rel)
    if user_modified:
        raise DomainError(f"사용자 수정 Generated 파일이 있어 upgrade 중단: {user_modified}")
    added=sorted(set(current_hashes)-set(previous))
    removed=sorted(set(previous)-set(current_hashes))
    changed=sorted(rel for rel in set(previous)&set(current_hashes) if previous[rel]!=current_hashes[rel])
    # 삭제는 Generator가 직접 수행하지 않습니다. 사용자 승인 Delete Manifest에서만 실행합니다.
    legacy_metadata=[name for name in ('cpf-domain.yaml','cpf-generator.lock.json') if (output/name).is_file()]
    delete_candidates=sorted([rel for rel in removed if (output/rel).is_file()] + legacy_metadata)
    if delete_candidates and not apply_delete:
        return {'domain':d.name,'status':'VERIFICATION_PENDING_DELETE','output':str(output),
                'fromGeneratorVersion':state.get('generatorVersion'),'toGeneratorVersion':GENERATOR_VERSION,
                'added':added,'changed':changed,'deleteCandidates':delete_candidates,
                'deletePrecondition':'EXPLICIT_APPROVAL_REQUIRED','mutated':False}
    for rel in delete_candidates:
        target=output/rel
        if target.is_file(): target.unlink()
    for rel in sorted(set(added)|set(changed)):
        write_text(output/rel,current_files[rel])
    for rel,content in current_files.items():
        p=output/rel
        if not p.exists(): write_text(p,content)
    for p in sorted((x for x in output.rglob('*') if x.is_dir()),key=lambda x:len(x.parts),reverse=True):
        try: p.rmdir()
        except OSError: pass
    vr=verify_generated(root,definition_path,output,d)
    _write_transient_state(root,definition_path,d,vr,current_hashes)
    return {'domain':d.name,'status':'UPGRADED','output':str(output),'fromGeneratorVersion':state.get('generatorVersion'),
            'toGeneratorVersion':GENERATOR_VERSION,'added':added,'changed':changed,'removed':[],'verify':vr}


def restore(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    """remove 후 동일 Generation state의 Source를 정확히 복원한다.

    Generator/입력이 바뀐 상태에서 restore를 가장한 신규 생성을 하지 않는다. 이 경우 upgrade 또는
    명시적인 fresh generation을 선택하게 하여 복원과 승격 의미를 분리한다.
    """
    d=load_domain_contract(definition_path)
    state=_read_ownership_state(root,d)
    if output.exists():
        remaining=[p.name for p in output.iterdir()]
        if remaining: raise DomainError(f"restore target에 파일이 남아 있습니다: {output}:{sorted(remaining)}")
    expected=_expected_hashes(root,d)
    previous={str(x['path']):str(x['sha256']) for x in state['expectedFiles']}
    if previous!=expected:
        raise DomainError('현재 Generator Template이 remove 시점과 달라 restore할 수 없습니다. 명시적 fresh generate/upgrade 절차를 사용하세요.')
    state_contract=state.get('contractSha256')
    if state_contract is not None and state_contract!=domain_contract_hash(d):
        raise DomainError('현재 Developer Domain 계약이 remove 시점과 달라 restore할 수 없습니다.')
    result=_materialize(root,definition_path,output,d)
    return {'domain':d.name,'status':'RESTORED','output':str(output),'verify':result}


def remove_plan(root: Path, definition_path: Path, output: Path) -> dict[str,Any]:
    d=load_domain_contract(definition_path)
    try:
        state=_read_transient_state(root,d)
        expected={str(x['path']):str(x['sha256']) for x in state['expectedFiles']}
        state_source='TRANSIENT_GENERATION_STATE'
    except DomainError:
        expected=_expected_hashes(root,d)
        state_source='CURRENT_TEMPLATE_FALLBACK'
    rows=[]
    for rel,digest in sorted(expected.items()):
        p=output/rel; state='MISSING' if not p.exists() else ('UNCHANGED' if p.is_file() and sha256_file(p)==digest else 'USER_MODIFIED')
        rows.append({'path':rel,'sha256':digest,'state':state})
    return {'root':str(output),'fileCount':len(rows),'files':rows,'safeToRemove':all(x['state'] in {'UNCHANGED','MISSING'} for x in rows),'metadataRequired':False,'ownershipSource':state_source}


def remove_owned(root: Path, definition_path: Path, output: Path, apply: bool=False,
                 purge_definition: bool=False, approved_disposable_lifecycle: bool=False) -> dict[str,Any]:
    """Generated Domain 삭제 후보를 계산하고 승인된 disposable lifecycle에서만 적용합니다."""
    root=root.resolve(); output=output.resolve(); definition_path=definition_path.resolve()
    if output==root or root not in output.parents: raise DomainError(f"Repository Root 밖/자체 remove 금지: {output}")
    if not re.fullmatch(r"cpf-[a-z][a-z0-9-]{1,49}",output.name): raise DomainError(f"Generated Root naming 위반: {output.name}")
    plan=remove_plan(root,definition_path,output)
    if not plan['safeToRemove']:
        changed=[x['path'] for x in plan['files'] if x['state']=='USER_MODIFIED']; raise DomainError(f"사용자 변경 파일이 있어 remove 중단: {changed}")
    candidates=[x['path'] for x in plan['files'] if x['state']=='UNCHANGED']
    result={**plan,'status':'PLANNED_DELETE_MANIFEST','applied':False,'deleteCandidates':candidates,
            'deletePrecondition':'USER_APPROVED_DELETE_MANIFEST','purgeDefinitionRequested':bool(purge_definition)}
    if apply:
        domain=output.name.removeprefix('cpf-')
        lifecycle=root/'cpf-docs/work/evidence/generated/domain-generator'/f'lifecycle-{domain}'
        expected_output=(lifecycle/f'cpf-{domain}').resolve()
        expected_definition=(lifecycle/'definition/cpf-domain.yaml').resolve()
        if (not approved_disposable_lifecycle or output != expected_output
                or definition_path != expected_definition or purge_definition):
            raise DomainError('Generated Domain 실제 삭제는 사용자 승인 Delete Manifest 실행기로만 수행할 수 있습니다. 일반 --apply 직접 삭제는 금지됩니다.')
        candidate_set=set(candidates)
        disposable_build_roots=(Path('.gradle'),Path('build'),Path('online/build'),Path('batch/build'))
        unknown=[]
        if output.exists():
            for existing in (p for p in output.rglob('*') if p.is_file()):
                rel=existing.relative_to(output)
                if rel.as_posix() in candidate_set: continue
                if any(rel==build_root or build_root in rel.parents for build_root in disposable_build_roots): continue
                unknown.append(rel.as_posix())
        if unknown:
            raise DomainError(f'승인된 disposable lifecycle에도 사용자/미소유 파일이 있어 remove 중단: {sorted(unknown)}')
        removed=[]
        for rel in candidates:
            candidate=(output/rel).resolve()
            if output not in candidate.parents:
                raise DomainError(f'Delete Manifest가 Generated Root 밖을 가리킵니다: {candidate}')
            if candidate.is_file():
                candidate.unlink(); removed.append(rel)
        discarded_build_artifacts=[]
        for build_root in disposable_build_roots:
            artifact_root=output/build_root
            if artifact_root.exists():
                shutil.rmtree(artifact_root); discarded_build_artifacts.append(build_root.as_posix())
        if output.exists():
            for directory in sorted((p for p in output.rglob('*') if p.is_dir()),key=lambda p:len(p.parts),reverse=True):
                try: directory.rmdir()
                except OSError: pass
            try: output.rmdir()
            except OSError: pass
        remaining=[rel for rel in candidates if (output/rel).exists()]
        if remaining:
            raise DomainError(f'Delete Manifest 적용 후 Generated Source가 남았습니다: {remaining}')
        result.update({'status':'REMOVED','applied':True,'removed':removed,
                       'discardedBuildArtifacts':discarded_build_artifacts,
                       'deletePrecondition':'APPROVED_DISPOSABLE_LIFECYCLE'})
    return result


def verify_genericity(generator_root: Path) -> dict[str,Any]:
    """실제 생성 구현/Template에 특정 회귀 Domain 이름이 고정되지 않았는지 검증한다.

    definitions, verification fixture, capability contract는 특정 Domain/capability 이름을
    의도적으로 기술할 수 있으므로 Genericity 대상이 아니다.
    """
    candidates=[]
    engine=generator_root/"engine"
    templates=generator_root/"templates"
    for base in (engine,templates):
        if base.exists():
            candidates.extend(p for p in base.rglob("*") if p.is_file() and p.suffix.lower() in {".py",".ps1",".sh",".bat",".cmd",".json"})
    # 자기검증 함수의 금지어 정의 자체는 검사 대상에서 제외하고 실제 생성 본문만 확인한다.
    # 회귀용 공식 Domain의 고유 식별자가 Engine/Template에 하드코딩되는지만 검사한다.
    # member/external 같은 일반 업무 개념명은 domainDependencies/externalClients 계약 자체에 필요하므로 금지하지 않는다.
    forbidden_patterns=[r"com\.customer\.",r"\bMBR_[A-Z0-9_]",r"\bEXS_[A-Z0-9_]"]
    hits=[]
    for source in sorted(set(candidates)):
        text=source.read_text(encoding="utf-8-sig",errors="ignore")
        if source.name=="cpf_domain_generator.py":
            text=text.split("def verify_genericity",1)[0]
        for pattern in forbidden_patterns:
            if re.search(pattern,text,re.IGNORECASE if pattern.startswith("com\\.customer") else 0):
                hits.append({"file":str(source),"pattern":pattern})
    return {"status":"PASS" if not hits else "FAIL","filesScanned":len(set(candidates)),"hits":hits}
