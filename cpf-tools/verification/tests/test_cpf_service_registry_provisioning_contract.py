"""Generated Domain 의 Service Registry provisioning 계약.

Runtime Control Agent 는 자기 service_id 가 중앙 Registry(OPS_SERVICE)에 등록되어 있어야 기동한다.
이 fail-closed 계약은 유지하되, 사용자가 만든 Generated Domain 의 service 를 **누가** 등록하는지가
정해져 있어야 한다. 정해져 있지 않으면 새로 만든 Domain 은 영원히 기동할 수 없다.

- 등록의 실행 주체: `cpf bootstrap` 의 Platform DB provisioning lifecycle
- 등록 규칙의 정본: cpf-tools/db/canonical/service-registry-provisioning.json
- SQL 정본: vendor pack (3 vendor)
- Runtime 자가 등록: 금지
- ADM 수동 등록 / domain-new 즉시 등록: Golden Path 아님

정본 Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §40
"""

from __future__ import annotations

import json
import os
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(os.environ.get("CPF_SERVICE_REGISTRY_ROOT") or Path(__file__).resolve().parents[3])

CONTRACT = REPO_ROOT / "cpf-tools/db/canonical/service-registry-provisioning.json"
BOOTSTRAP = REPO_ROOT / "cpf-tools/runtime/bootstrap/CpfBootstrap.java"
SURFACE_POLICY = REPO_ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"
PLATFORM_SCHEMA = REPO_ROOT / "cpf-tools/db/canonical/platform-schema.json"
HARNESS = REPO_ROOT / "cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md"
WORK_ITEM_REGISTRY = REPO_ROOT / "cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def contract() -> dict:
    if not CONTRACT.is_file():
        raise AssertionError("Service Registry provisioning 계약 정본이 없다")
    return json.loads(read(CONTRACT))


def bootstrap_code() -> str:
    """주석을 뺀 실행 Source. 근거를 적은 주석은 계약 위반이 아니다."""
    return "\n".join(line for line in read(BOOTSTRAP).splitlines()
                     if not line.strip().startswith(("*", "//", "/*")))


class ProvisioningAuthority(unittest.TestCase):
    """등록 규칙의 정본이 machine-readable 로 존재한다."""

    def test_contract_declares_executor_and_owner(self) -> None:
        model = contract()
        self.assertIn("bootstrap", model["executor"].lower(),
                      "등록의 실행 주체가 bootstrap 이 아니다")
        self.assertIn("contract", model["owner"].lower(),
                      "등록 규칙의 Owner 가 계약이 아니다")

    def test_target_table_and_key_match_the_platform_schema(self) -> None:
        model = contract()
        schema = json.loads(read(PLATFORM_SCHEMA))
        table = next((t for t in schema["tables"]
                      if str(t.get("name", "")).upper() == model["table"]), None)
        self.assertIsNotNone(table, f"정본 schema 에 없는 table: {model['table']}")
        columns = {str(c["name"]) for c in table["columns"]}
        self.assertIn(model["keyColumn"], columns, "key column 이 schema 에 없다")
        missing = sorted(set(model["valueSources"]) - columns)
        self.assertEqual([], missing, f"schema 에 없는 column 을 채운다: {missing}")

    def test_every_not_null_column_without_default_is_provided(self) -> None:
        """값을 주지 않으면 INSERT 가 실패한다. 계약이 그것을 미리 잡아야 한다."""
        model = contract()
        schema = json.loads(read(PLATFORM_SCHEMA))
        table = next(t for t in schema["tables"]
                     if str(t.get("name", "")).upper() == model["table"])
        required = {str(c["name"]) for c in table["columns"]
                    if c.get("nullable") is False and c.get("default") in (None, "")}
        missing = sorted(required - set(model["valueSources"]))
        self.assertEqual([], missing, f"필수 column 에 값을 주지 않는다: {missing}")

    def test_values_come_from_canonical_sources_without_inference(self) -> None:
        model = contract()
        allowed = set(model["transformPolicy"]["allowed"])
        forbidden = set(model["transformPolicy"]["forbidden"])
        for name in ("TRUNCATE", "INFER", "FALLBACK"):
            self.assertIn(name, forbidden, f"금지 변환 선언이 빠졌다: {name}")
        offenders: list[str] = []
        for column, source in model["valueSources"].items():
            origin = source["from"]
            if origin not in {"domainContract", "contractConstant"}:
                offenders.append(f"{column}:{origin}")
                continue
            if origin == "domainContract":
                if not str(source.get("key", "")).startswith("cpf.domain."):
                    offenders.append(f"{column}: canonical Domain 계약 key 가 아니다")
                if source.get("transform") not in allowed:
                    offenders.append(f"{column}: 허용되지 않은 변환 {source.get('transform')}")
        self.assertEqual([], offenders, f"값의 출처가 정본이 아니다: {offenders}")

    def test_identity_and_ownership_come_from_the_domain_system_code(self) -> None:
        model = contract()
        for column in ("service_id", "owner_module_code"):
            source = model["valueSources"][column]
            self.assertEqual("domainContract", source["from"],
                             f"{column} 이 Domain 계약에서 오지 않는다")
            self.assertEqual("cpf.domain.systemCode", source["key"],
                             f"{column} 이 canonical SystemCode 가 아니다")

    def test_target_set_is_discovered_not_enumerated(self) -> None:
        model = contract()
        self.assertEqual("everyDiscoveredGeneratedDomain", model["appliesTo"]["selector"],
                         "대상 Domain 을 열거한다")
        text = json.dumps(model, ensure_ascii=False)
        for name in domain_names():
            self.assertNotIn(f'"{name}"', text,
                             f"계약이 특정 Domain 이름을 담고 있다: {name}")


def domain_names() -> list[str]:
    names = []
    for properties in REPO_ROOT.glob("cpf-*/gradle.properties"):
        text = properties.read_text(encoding="utf-8")
        if "cpf.domain.contractVersion=1" not in text.replace(" ", ""):
            continue
        for key in ("cpf.domain.name", "cpf.domain.systemCode"):
            match = re.search(rf"^{re.escape(key)}\s*=\s*(\S+)", text, re.M)
            if match:
                names.append(match.group(1))
    return names


class ReconcileRules(unittest.TestCase):
    """validate → reconcile → fail-closed."""

    def test_reconcile_order_is_declared(self) -> None:
        self.assertEqual(["validate", "reconcile", "failClosed"],
                         contract()["reconcile"]["order"],
                         "reconcile 순서가 정본과 다르다")

    def test_absent_registers_and_identical_is_idempotent(self) -> None:
        reconcile = contract()["reconcile"]
        self.assertEqual("REGISTER", reconcile["absent"])
        self.assertEqual("IDEMPOTENT_PASS", reconcile["identicalContract"])
        self.assertTrue(reconcile["idempotentOnRerun"],
                        "bootstrap 재실행이 안전하다고 선언하지 않았다")

    def test_conflict_fails_closed_and_never_overwrites(self) -> None:
        reconcile = contract()["reconcile"]
        self.assertEqual("FAIL_CLOSED", reconcile["conflict"])
        self.assertTrue(reconcile["neverOverwriteExistingRow"],
                        "기존 Registry row 를 덮어쓸 수 있는 계약이다")
        for column in ("owner_module_code", "service_type"):
            self.assertIn(column, reconcile["conflictColumns"],
                          f"충돌 판정 대상이 아니다: {column}")

    def test_operator_edited_display_values_are_not_overwritten(self) -> None:
        reconcile = contract()["reconcile"]
        for column in ("service_name", "description"):
            self.assertIn(column, reconcile["operatorEditableColumns"],
                          f"운영자 편집 대상 선언이 빠졌다: {column}")

    def test_disabled_row_is_not_silently_re_enabled(self) -> None:
        self.assertEqual("FAIL_CLOSED", contract()["reconcile"]["disabledRow"],
                         "운영자가 내려둔 service 를 provisioning 이 조용히 다시 켠다")

    def test_runtime_self_registration_is_forbidden(self) -> None:
        self.assertEqual("FORBIDDEN", contract()["reconcile"]["runtimeSelfRegistration"],
                         "Runtime 자가 등록을 허용한다")

    def test_contract_is_profile_invariant(self) -> None:
        model = contract()
        self.assertTrue(model["profileInvariant"],
                        "profile 별로 다른 lifecycle 을 쓰는 계약이다")


class SqlAuthority(unittest.TestCase):
    """SQL 정본은 vendor pack 하나다."""

    def test_every_official_vendor_ships_both_statements(self) -> None:
        model = contract()
        missing: list[str] = []
        for vendor in model["officialDbVendors"]:
            for statement in model["statements"].values():
                if not statement.endswith(".sql"):
                    continue
                path = REPO_ROOT / model["statements"]["canonicalRoot"].replace("{vendor}", vendor) / statement
                if not path.is_file():
                    missing.append(path.as_posix())
        self.assertEqual([], missing, f"vendor pack 에 없는 Service Registry SQL: {missing}")

    def test_sql_uses_named_parameters_only(self) -> None:
        model = contract()
        offenders: list[str] = []
        for vendor in model["officialDbVendors"]:
            for statement in ("select", "insert"):
                path = (REPO_ROOT / model["statements"]["canonicalRoot"].replace("{vendor}", vendor)
                        / model["statements"][statement])
                body = "\n".join(line for line in read(path).splitlines()
                                 if not line.strip().startswith("--"))
                if ":serviceId" not in body:
                    offenders.append(f"{vendor}:{statement}: 이름 붙은 parameter 가 없다")
                if re.search(r"'\s*\|\|\s*'", body) and statement == "insert":
                    offenders.append(f"{vendor}:{statement}: 값을 문자열로 조합한다")
        self.assertEqual([], offenders, f"Service Registry SQL 계약 위반: {offenders}")

    def test_bootstrap_never_carries_the_sql_itself(self) -> None:
        code = bootstrap_code()
        self.assertNotIn("INSERT INTO OPS_SERVICE", code,
                         "Bootstrap 이 Service Registry SQL 을 코드에 복제한다")
        self.assertIn("serviceRegistrySql(", code,
                      "Bootstrap 이 vendor pack 의 SQL 정본을 읽지 않는다")


class BootstrapExecution(unittest.TestCase):
    """bootstrap 이 계약대로 실행한다."""

    def test_reconcile_runs_inside_the_database_lifecycle(self) -> None:
        code = bootstrap_code()
        self.assertIn("reconcileServiceRegistry();", code,
                      "DB Lifecycle 이 Service Registry 를 맞추지 않는다")
        lifecycle = code.split("persistentDomains=", 1)
        self.assertEqual(2, len(lifecycle), "DB Lifecycle 종료 지점을 찾지 못했다")
        self.assertIn("reconcileServiceRegistry();", lifecycle[1][:400],
                      "Service Registry 정합이 Platform DB 적용 뒤에 오지 않는다")

    def test_bootstrap_never_hardcodes_a_domain_name(self) -> None:
        code = bootstrap_code()
        block = code.split("private void reconcileServiceRegistry()", 1)
        self.assertEqual(2, len(block), "reconcile 진입점을 찾지 못했다")
        body = block[1].split("\n    }", 1)[0]
        offenders = [name for name in domain_names() if f'"{name}"' in body]
        self.assertEqual([], offenders, f"reconcile 이 Domain 이름을 하드코딩한다: {offenders}")

    def test_bootstrap_fails_closed_on_conflict_and_disabled_rows(self) -> None:
        body = bootstrap_code().split("private void reconcileServiceRegistry()", 1)[1].split("\n    }", 1)[0]
        self.assertIn("ownership conflict", body, "소유 충돌을 fail-closed 로 처리하지 않는다")
        self.assertIn("disabled by an operator", body,
                      "운영자가 내려둔 상태를 fail-closed 로 처리하지 않는다")
        self.assertNotIn("UPDATE OPS_SERVICE", body, "기존 행을 덮어쓴다")

    def test_provisioning_contract_reaches_the_public_workspace(self) -> None:
        policy = json.loads(read(SURFACE_POLICY))
        rules = policy.get("sourceRules", []) + policy.get("templateRules", [])
        targets = {str(rule.get("target", "")) for rule in rules}
        self.assertIn("config/service-registry-provisioning.json", targets,
                      "provisioning 계약이 공개 배포본에 투영되지 않는다")
        vendor_projected = any(str(rule.get("target", "")).startswith("deploy/local/db/vendor/")
                               for rule in rules)
        self.assertTrue(vendor_projected, "vendor SQL pack 이 공개 배포본에 투영되지 않는다")


class EndpointProvisioning(unittest.TestCase):
    """Runtime 은 service 와 endpoint 가 모두 등록되어 있어야 기동한다."""

    def endpoint(self) -> dict:
        model = contract().get("endpointTable")
        if not model:
            raise AssertionError("endpoint provisioning 계약이 없다")
        return model

    def test_endpoint_table_and_key_match_the_platform_schema(self) -> None:
        model = self.endpoint()
        schema = json.loads(read(PLATFORM_SCHEMA))
        table = next((t for t in schema["tables"]
                      if str(t.get("name", "")).upper() == model["table"]), None)
        self.assertIsNotNone(table, f"정본 schema 에 없는 table: {model['table']}")
        columns = {str(c["name"]) for c in table["columns"]}
        required = {str(c["name"]) for c in table["columns"]
                    if c.get("nullable") is False and c.get("default") in (None, "")}
        missing = sorted(required - set(model["valueSources"]))
        self.assertEqual([], missing, f"endpoint 필수 column 에 값을 주지 않는다: {missing}")
        unknown = sorted(set(model["valueSources"]) - columns)
        self.assertEqual([], unknown, f"schema 에 없는 endpoint column: {unknown}")

    def test_endpoint_code_comes_from_the_runtime_control_authority(self) -> None:
        """endpoint code 규칙을 provisioning 이 새로 만들지 않는다."""
        model = self.endpoint()
        authority = model["codeAuthority"]
        self.assertIn("runtime-control", authority["owner"],
                      "endpoint code 의 정본이 Runtime Control 이 아니다")
        self.assertEqual("cpf.runtime.control.agent.endpoint-code", authority["property"])
        self.assertEqual(authority["defaultPattern"],
                         model["valueSources"]["endpoint_code"]["pattern"],
                         "provisioning 이 Runtime Control 과 다른 endpoint code 를 쓴다")

    def test_endpoint_code_pattern_matches_the_runtime_default(self) -> None:
        """Runtime 이 실제로 쓰는 기본값과 계약이 일치해야 한다."""
        source = REPO_ROOT / ("cpf-starters/platform-operations/runtime-control/src/main/java/"
                              "com/cpf/platform/operations/runtimecontrol/"
                              "CpfRuntimeControlAgentAutoConfiguration.java")
        if not source.is_file():
            self.skipTest("Runtime Control Source 가 이 배포 범위에 없다")
        pattern = self.endpoint()["codeAuthority"]["defaultPattern"]
        suffix = pattern.replace("{service_id}", "")
        self.assertIn(f'+ "{suffix}"', read(source),
                      f"Runtime 기본 endpoint code 와 계약이 다르다: {pattern}")

    def test_endpoint_reconcile_never_overwrites_and_fails_closed(self) -> None:
        reconcile = self.endpoint()["reconcile"]
        self.assertEqual("REGISTER", reconcile["absent"])
        self.assertEqual("IDEMPOTENT_PASS", reconcile["present"])
        self.assertTrue(reconcile["neverOverwriteExistingRow"],
                        "기존 endpoint 행을 덮어쓸 수 있는 계약이다")
        self.assertEqual("FAIL_CLOSED", reconcile["scopeConflict"],
                         "같은 endpoint_code 가 다른 service 에 있어도 넘어간다")
        self.assertEqual("FAIL_CLOSED", reconcile["disabledRow"],
                         "운영자가 내려둔 endpoint 를 조용히 다시 켠다")

    def test_endpoint_sql_ships_for_every_vendor(self) -> None:
        model = contract()
        endpoint = self.endpoint()
        missing: list[str] = []
        for vendor in model["officialDbVendors"]:
            for statement in endpoint["statements"].values():
                path = (REPO_ROOT / model["statements"]["canonicalRoot"].replace("{vendor}", vendor)
                        / statement)
                if not path.is_file():
                    missing.append(path.as_posix())
        self.assertEqual([], missing, f"vendor pack 에 없는 endpoint SQL: {missing}")

    def test_bootstrap_reconciles_endpoints_after_services(self) -> None:
        code = bootstrap_code()
        self.assertIn("reconcileServiceEndpoints(", code,
                      "endpoint 를 맞추지 않는다")
        body = code.split("private void reconcileServiceRegistry()", 1)[1].split("\n    }", 1)[0]
        self.assertIn("reconcileServiceEndpoints(", body,
                      "endpoint 정합이 service 정합과 같은 lifecycle 에 있지 않다")
        endpoint_body = code.split("private int reconcileServiceEndpoints(", 1)[1].split("\n    }", 1)[0]
        self.assertIn("scope conflict", endpoint_body, "endpoint 소유 충돌을 fail-closed 로 처리하지 않는다")
        self.assertIn("disabled by an operator", endpoint_body,
                      "운영자가 내려둔 endpoint 를 fail-closed 로 처리하지 않는다")
        self.assertNotIn("UPDATE OPS_SERVICE_ENDPOINT", endpoint_body, "기존 endpoint 행을 덮어쓴다")


class HarnessAndRegistryRelation(unittest.TestCase):
    def test_harness_declares_the_provisioning_rule(self) -> None:
        text = read(HARNESS)
        self.assertIn("Service Registry", text,
                      "Current Harness 에 Service Registry provisioning 계약이 없다")
        self.assertIn("자가 등록", text, "Runtime 자가 등록 금지 규칙이 Harness 에 없다")

    def test_registry_links_the_rule_to_this_validator(self) -> None:
        self.assertTrue(WORK_ITEM_REGISTRY.is_file(), "Work Item Registry 가 없다")
        self.assertIn("test_cpf_service_registry_provisioning_contract", read(WORK_ITEM_REGISTRY),
                      "Registry 가 이 계약 Validator 를 참조하지 않는다")


if __name__ == "__main__":
    unittest.main(verbosity=2)
