"""System Identity / Channel Identity 계약(Harness 30장)을 정적으로 강제한다.

사용자 확정 Architecture(2026-09-04):

- SystemCode 는 Business Domain / Business Runtime / Reference Runtime 만 가진다.
- ADM(Platform Control Plane), Gateway, Channel Front, Framework, Batch Control Plane 은
  SystemCode 를 가지지 않는다. `LOCAL` / `GWY` / `ADM` / `CPF` / `DEFAULT` / `UNKNOWN` 같은
  가상 SystemCode 로 누락된 Identity 를 보정하지 않는다.
- 1-WAS 는 System 이 아니라 Same-JVM topology 이므로 자체 SystemCode 를 만들지 않는다.
- 계약 경계는 Operation -> Owner Module -> architectureRole 로만 판정한다. 정본 위치는
  `cpf-tools/governance/cpf-product-surface-policy.json` 이다.
- OPS_SYSTEM_REGISTRY 는 실제 System Identity 만 담는다(CPF/CMN/ADM 제외, CEC 포함).

되돌리면 재발할 증상: 1-WAS 에서 ADM/MBW/GWY 거래가 하나의 가상 System 으로 기록되어 Domain
Identity 가 사라지고, 검증기가 `X-System-Code=LOCAL` 같은 하드코딩으로 False Green 을 만든다.
"""

from __future__ import annotations

import io
import json
import re
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(__file__).resolve().parents[3]
POLICY = REPO_ROOT / "cpf-tools/governance/cpf-product-surface-policy.json"

# Role 의 SystemCode 보유 여부와 "System Identity 가 아닌 코드" 목록은 정본 카탈로그가 소유한다.
# 검증기가 같은 값을 다시 적으면 정본이 둘이 되고, 카탈로그를 바꿔도 검증기가 옛 값을 지킨다.
def _roles_with_system_code() -> set[str]:
    return {name for name, spec in _policy().get("architectureRoles", {}).items()
            if bool(spec.get("hasSystemCode"))}


def _roles_without_system_code() -> set[str]:
    return {name for name, spec in _policy().get("architectureRoles", {}).items()
            if not bool(spec.get("hasSystemCode"))}


def _non_system_identity_codes() -> set[str]:
    return {str(code).upper() for code in _policy().get("nonSystemIdentityCodes", {}).get("codes", [])}

SYSTEM_CODE_LINE = re.compile(r"^\s*system-code\s*:\s*(?P<value>\S.*?)\s*$", re.MULTILINE)
ROLE_LINE = re.compile(r"^\s*architecture-role\s*:\s*(?P<value>[A-Z_]+)\s*$", re.MULTILINE)
ENV_DEFAULT = re.compile(r"\$\{CPF_SYSTEM_CODE(?::(?P<default>[^}]*))?\}")

# 1-WAS 는 System 이 아니다(Harness 30.5).
SAME_JVM_TOPOLOGY_ROOTS = ("cpf-tools/runtime/cpf-local-runtime/",)


def _policy() -> dict:
    return json.loads(io.open(POLICY, encoding="utf-8").read())


def _owner_role(relative: str, owners: list[dict]) -> str | None:
    """가장 긴 prefix 가 우선한다(Harness 30.17)."""
    best: tuple[int, str] | None = None
    for entry in owners:
        prefix = str(entry.get("prefix", ""))
        if prefix and relative.startswith(prefix):
            role = str(entry.get("architectureRole", ""))
            if best is None or len(prefix) > best[0]:
                best = (len(prefix), role)
    return best[1] if best else None


def _declared_system_codes() -> list[tuple[str, str]]:
    """`system-code:` 를 선언한 모든 설정 파일과 그 값."""
    found: list[tuple[str, str]] = []
    for path in REPO_ROOT.rglob("application*.y*ml"):
        relative = path.relative_to(REPO_ROOT).as_posix()
        if "/build/" in f"/{relative}" or "/bin/" in f"/{relative}":
            continue
        text = io.open(path, encoding="utf-8").read()
        for match in SYSTEM_CODE_LINE.finditer(text):
            found.append((relative, match.group("value").strip()))
    return found


def _runtime_configs() -> list[tuple[str, str | None, str | None]]:
    """Runtime application.yml 의 (경로, 선언 role, 선언 systemCode)."""
    found: list[tuple[str, str | None, str | None]] = []
    for path in REPO_ROOT.rglob("application.yml"):
        relative = path.relative_to(REPO_ROOT).as_posix()
        if "/build/" in f"/{relative}" or "/bin/" in f"/{relative}":
            continue
        text = io.open(path, encoding="utf-8").read()
        role = ROLE_LINE.search(text)
        code = SYSTEM_CODE_LINE.search(text)
        if role is None and code is None:
            continue
        found.append((relative,
                      role.group("value") if role else None,
                      code.group("value") if code else None))
    return found


# Generated Business Domain 은 Generator/설치 lifecycle 이 Registry 를 채운다(Harness 30.18).
# 정적 seed 가 소유하는 것은 Product Runtime 의 canonical System Identity 뿐이다.
GENERATED_DOMAIN_ROOTS = ("cpf-member/", "cpf-external/")


def _declared_system_codes_by_runtimes(include_generated: bool = False) -> set[str]:
    """SystemCode 를 가지는 Role 의 Runtime 이 실제로 선언한 canonical System Identity 집합."""
    with_code = _roles_with_system_code()
    codes: set[str] = set()
    for relative, role, code in _runtime_configs():
        if role not in with_code or not code or "${" in code:
            continue
        generated = any(relative.startswith(root) for root in GENERATED_DOMAIN_ROOTS)
        if generated and not include_generated:
            continue
        codes.add(code.strip().upper())
    return codes


class SystemIdentityContractTest(unittest.TestCase):
    def test_every_runtime_declares_its_architecture_role(self) -> None:
        """systemCode 유무를 판정하려면 Role 선언이 있어야 한다. 없으면 fail-closed(Harness 30.16)."""
        missing = [rel for rel, role, code in _runtime_configs() if role is None]
        self.assertEqual(
            [], sorted(missing),
            f"Runtime 은 architecture-role 을 선언해야 한다(정본: cpf-product-surface-policy.json): {sorted(missing)}")

    def test_system_code_presence_matches_the_declared_role(self) -> None:
        """Role 이 SystemCode 를 가지면 필수, 가지지 않으면 금지다(Harness 30.20 / 사용자 확정 7)."""
        violations: list[str] = []
        for rel, role, code in _runtime_configs():
            if role in _roles_with_system_code() and not code:
                violations.append(f"{rel}: role={role} 은 canonical systemCode 가 필수인데 없음")
            if role in _roles_without_system_code() and code:
                violations.append(f"{rel}: role={role} 은 systemCode 를 가질 수 없는데 선언됨 -> {code}")
        self.assertEqual(
            [], sorted(violations),
            f"Role 과 systemCode 보유가 어긋났습니다: {sorted(violations)}")

    def test_declared_roles_exist_in_the_canonical_catalog(self) -> None:
        known = set(_policy().get("architectureRoles", {}))
        unknown = sorted({role for _, role, _ in _runtime_configs() if role and role not in known})
        self.assertEqual([], unknown, f"정본 카탈로그에 없는 architectureRole: {unknown}")

    def test_every_module_owner_declares_a_known_architecture_role(self) -> None:
        policy = _policy()
        known = set(policy.get("architectureRoles", {}))
        self.assertTrue(known, "architectureRoles 정의가 정본에 없습니다.")
        missing = [
            entry.get("prefix")
            for entry in policy.get("moduleOwners", [])
            if str(entry.get("architectureRole", "")) not in known
        ]
        self.assertEqual([], missing, f"architectureRole 이 없거나 알 수 없는 module: {missing}")

    def test_platform_components_are_classified_without_system_code(self) -> None:
        policy = _policy()
        owners = {str(e.get("prefix")): str(e.get("architectureRole")) for e in policy.get("moduleOwners", [])}
        self.assertEqual("PLATFORM_CONTROL_PLANE", owners.get("cpf-admin/"))
        self.assertEqual("GATEWAY", owners.get("cpf-gateway/"))
        self.assertEqual("CHANNEL_FRONT", owners.get("cpf-backoffice-web/"))
        self.assertEqual("REFERENCE_RUNTIME", owners.get("cpf-education/"))
        self.assertEqual("TOPOLOGY", owners.get("cpf-tools/runtime/cpf-local-runtime/"))
        # Batch 는 실행 Runtime 과 Control Plane 을 섞지 않는다. BAT/CEC 는 Role 이 아니라 System Identity 다.
        self.assertEqual("BATCH_RUNTIME", owners.get("cpf-batch/"))
        self.assertEqual("BATCH_CONTROL_PLANE", owners.get("cpf-batch/control-plane/"))

    def test_longest_prefix_wins_for_nested_module_owners(self) -> None:
        """중첩 prefix 는 반드시 가장 긴 것이 이겨야 한다(Harness 30.16)."""
        owners = _policy().get("moduleOwners", [])
        self.assertEqual(
            "BATCH_CONTROL_PLANE",
            _owner_role("cpf-batch/control-plane/src/main/resources/application.yml", owners),
        )
        self.assertEqual(
            "BATCH_RUNTIME",
            _owner_role("cpf-batch/worker/src/main/resources/application.yml", owners),
        )
        self.assertEqual(
            "TOPOLOGY",
            _owner_role("cpf-tools/runtime/cpf-local-runtime/src/main/resources/application.yml", owners),
        )
        self.assertEqual(
            "FRAMEWORK_INTERNAL",
            _owner_role("cpf-tools/db/canonical/seed-model.json", owners),
        )

    def test_system_code_is_a_canonical_literal_not_a_runtime_choice(self) -> None:
        """SystemCode 는 정본 고정값이다. 환경변수 default 도, Runtime 선택도 허용하지 않는다."""
        violations: list[str] = []
        for relative, value in _declared_system_codes():
            if ENV_DEFAULT.search(value) or "${" in value:
                violations.append(f"{relative}: {value}")
        self.assertEqual(
            [],
            sorted(violations),
            "SystemCode 는 canonical 고정값이어야 한다(Harness 30.20). "
            "${CPF_SYSTEM_CODE:XXX} 도 ${CPF_SYSTEM_CODE} 도 금지: "
            f"{sorted(violations)}",
        )

    def test_runtime_rejects_an_external_system_code_that_differs_from_canonical_source(self) -> None:
        """외부 SystemCode 는 정본을 바꾸지 못하고 같을 때만 허용한다(Harness 30.20)."""
        source = REPO_ROOT / (
            "cpf-starters/base/runtime/src/main/java/com/cpf/foundation/runtime/CpfRuntimeSystemCode.java"
        )
        text = io.open(source, encoding="utf-8").read()
        self.assertIn("canonicalValue(environment", text,
                      "Runtime 은 Environment 최상위 값만 SystemCode 정본으로 쓰면 안 됩니다.")
        self.assertIn('"systemEnvironment"', text,
                      "OS 환경변수는 canonical SystemCode source 가 아니라 override 로 분류해야 합니다.")
        self.assertIn("override does not match the canonical identity", text,
                      "SystemCode override 불일치를 fail-closed 해야 합니다.")
        self.assertNotIn('"cpf.framework.module-id"', text,
                         "Module ID 를 SystemCode fallback/source 로 재도입하면 안 됩니다.")

    def test_transaction_issuer_is_not_forced_to_equal_original_business_system(self) -> None:
        """issuer는 최초 trusted Channel, Original System은 업무 lineage다(Harness 30.7)."""
        inbound = REPO_ROOT / (
            "cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java"
        )
        gate = REPO_ROOT / "cpf-tools/verification/tools/verify-cpf-transaction-id-standard.py"
        inbound_text = io.open(inbound, encoding="utf-8").read()
        gate_text = io.open(gate, encoding="utf-8").read()
        self.assertNotIn("ORIGINAL_SYSTEM_CODE_MISMATCH", inbound_text,
                         "issuer와 X-Original-System-Code를 universal equality로 강제하면 안 됩니다.")
        self.assertNotIn("issuerCode(tx)", inbound_text,
                         "Inbound adapter가 TransactionId issuer를 Original System 검증에 재사용하면 안 됩니다.")
        self.assertIn("issuer and original-system namespace separation", gate_text,
                      "TransactionId standard gate가 issuer/System namespace 분리를 검증해야 합니다.")

    def test_adm_runtime_smokes_use_channel_not_business_system_identity(self) -> None:
        """ADM은 Control Plane이므로 1-WAS verifier가 MBW System6을 재사용하면 안 된다."""
        policy_smoke = REPO_ROOT / "cpf-tools/runtime/tools/smoke-log-policy-runtime.ps1"
        correlation_smoke = REPO_ROOT / "cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1"
        work_unit = REPO_ROOT / "cpf-tools/verification/tools/run-cpf-runtime-work-unit.ps1"
        full_runner = REPO_ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"
        policy_text = io.open(policy_smoke, encoding="utf-8-sig").read()
        correlation_text = io.open(correlation_smoke, encoding="utf-8-sig").read()
        work_unit_text = io.open(work_unit, encoding="utf-8-sig").read()
        full_runner_text = io.open(full_runner, encoding="utf-8-sig").read()
        adm_headers = correlation_text.split("function New-CpfAdmHeaders", 1)[1].split(
            "$script:admWebSession", 1)[0]

        self.assertIn("[string] $ChannelCode = \"ADM\"", policy_text)
        self.assertIn("X-Original-Channel", policy_text)
        self.assertNotIn('"X-System-Code" = $ChannelCode', policy_text)
        self.assertIn('function Get-AdmCsrfCookie', policy_text)
        self.assertIn('"{0}/adm/" -f $AdmBaseUrl.TrimEnd', policy_text)
        self.assertIn("Select-Object -Last 1", policy_text)
        self.assertIn("function Get-SecretFingerprint", policy_text)
        self.assertIn("csrfFingerprint(initial=", policy_text)
        self.assertIn("[string] $AdmChannelCode = 'ADM'", correlation_text)
        self.assertIn("Get-CpfIssuerCode $AdmChannelCode", adm_headers)
        self.assertIn("function Get-CpfAdmCsrfCookie", correlation_text)
        self.assertIn("Select-Object -Last 1", correlation_text)
        for system_header in ("X-Original-System-Code", "X-System-Code", "X-Caller-System-Code", "X-Target-System-Code"):
            self.assertNotIn(system_header, adm_headers,
                             f"ADM Control Plane header must not carry business System6: {system_header}")
        self.assertIn("-ChannelCode', $admOperationalChannelCode", work_unit_text)
        self.assertIn("-AdmChannelCode', $admOperationalChannelCode", work_unit_text)
        self.assertIn("-ChannelCode',$admOperationalChannelCode", full_runner_text)
        self.assertIn("-AdmChannelCode',$admOperationalChannelCode", full_runner_text)
        for text in (work_unit_text, full_runner_text):
            self.assertIn("CPF_ADM_SESSION_COOKIE_SECURE", text)
            self.assertIn("CPF_ADM_ALLOWED_ORIGINS", text)

    def test_components_without_system_identity_do_not_declare_system_code(self) -> None:
        owners = _policy().get("moduleOwners", [])
        violations: list[str] = []
        for relative, value in _declared_system_codes():
            if any(relative.startswith(root) for root in SAME_JVM_TOPOLOGY_ROOTS):
                violations.append(f"{relative}: 1-WAS 는 System 이 아니다 -> {value}")
                continue
            role = _owner_role(relative, owners)
            if role in _roles_without_system_code():
                violations.append(f"{relative}: role={role} 은 SystemCode 를 가지지 않는다 -> {value}")
        self.assertEqual(
            [],
            sorted(violations),
            "SystemCode 를 가지지 않는 Component 가 system-code 를 선언했습니다: " f"{sorted(violations)}",
        )

    def test_no_virtual_system_code_value_is_used(self) -> None:
        violations: list[str] = []
        for relative, value in _declared_system_codes():
            candidates = {value.strip().strip("'\"").upper()}
            match = ENV_DEFAULT.search(value)
            if match and match.group("default"):
                candidates.add(match.group("default").strip().upper())
            for candidate in candidates:
                if candidate in _non_system_identity_codes():
                    violations.append(f"{relative}: {value}")
        self.assertEqual(
            [],
            sorted(set(violations)),
            "가상 SystemCode 값을 사용했습니다(LOCAL/GWY/ADM/CPF/CMN/DEFAULT/UNKNOWN): "
            f"{sorted(set(violations))}",
        )

    def test_system_registry_seed_holds_only_real_system_identities(self) -> None:
        seeds = sorted(REPO_ROOT.glob("cpf-tools/db/vendor/*/source/50_framework_seed_data.sql"))
        self.assertTrue(seeds, "framework seed 를 찾지 못했습니다.")
        violations: list[str] = []
        for seed in seeds:
            text = io.open(seed, encoding="utf-8").read()
            if "OPS_SYSTEM_REGISTRY" not in text:
                continue
            # Vendor Pack 은 벤더마다 문장 형태가 다르다.
            #   MariaDB/PostgreSQL : INSERT ... VALUES ('CPF', ...)
            #   Oracle             : MERGE INTO ... USING (SELECT 'CPF' AS system_code ...)
            # 한 가지 형태만 파싱하면 다른 벤더를 "전부 누락" 으로 오판한다.
            present: set[str] = set()
            for statement in text.split(";"):
                if "OPS_SYSTEM_REGISTRY" not in statement:
                    continue
                present.update(re.findall(r"'([A-Z0-9_]{2,10})'\s+AS\s+system_code", statement))
                values = statement.find("VALUES")
                if values >= 0:
                    present.update(re.findall(r"\(\s*'([A-Z0-9_]{2,10})'\s*,", statement[values:]))
            relative = seed.relative_to(REPO_ROOT).as_posix()
            # 금지 목록은 정본 카탈로그가, 필수 목록은 "실제로 SystemCode 를 선언한 Runtime" 이 정한다.
            # 검증기가 코드 목록을 따로 적으면 Runtime 이 늘어날 때마다 정본이 어긋난다.
            for forbidden in sorted(_non_system_identity_codes() & present):
                violations.append(f"{relative}: System Identity 가 아닌 코드가 등록됨 -> {forbidden}")
            for required in sorted(_declared_system_codes_by_runtimes()):
                if required not in present:
                    violations.append(f"{relative}: Product Runtime 이 선언한 canonical System Identity 누락 -> {required}")
        self.assertEqual(
            [],
            sorted(violations),
            f"OPS_SYSTEM_REGISTRY 구성이 정본(Harness 30.19)과 다릅니다: {sorted(violations)}",
        )


    def test_generated_domain_system_codes_are_wired_into_the_install_lifecycle(self) -> None:
        """Generated Domain 의 SystemCode 는 정적 seed 가 아니라 설치 lifecycle 이 등록한다.

        Registry 에 `MBR`/`EXS` 같은 코드를 하드코딩하지 않고, 정본 Inventory 를 읽어 등록해야
        신규 Generated Domain 도 자동으로 연결된다(Harness 30.18 / 30.24).
        """
        installer = REPO_ROOT / "cpf-tools/db/tools/initialize-cpf-database.ps1"
        text = io.open(installer, encoding="utf-8").read()
        self.assertIn("OPS_SYSTEM_REGISTRY", text,
                      "platform 설치가 Generated Domain 의 System Registry 를 등록해야 한다.")
        self.assertIn("Get-CpfGeneratedDomainInventory", text,
                      "Registry 등록은 정본 Inventory 에서 SystemCode 를 읽어야 한다.")
        generated = _declared_system_codes_by_runtimes(include_generated=True) -             _declared_system_codes_by_runtimes()
        for code in sorted(generated):
            self.assertNotIn(f"'{code}'", text,
                             f"Generated Domain SystemCode 를 설치기에 하드코딩하면 안 된다: {code}")


if __name__ == "__main__":
    unittest.main()
