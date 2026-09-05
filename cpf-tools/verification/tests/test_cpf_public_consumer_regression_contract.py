"""Fresh Consumer Runtime Gate 에서 나온 개별 Finding 의 재발을 각각 막는다.

상위 Root Cause 는 하나다. "One-WAS 통합 Runtime 으로만 검증했고 발행된 실행물 단독 기동 경로를
검증하지 않았다." 그러나 직접 결함은 서로 다르므로 Finding 단위로 각각 고정한다.
원장: cpf-docs/governance/development-harness/current/CONSUMER_RUNTIME_FINDING_LEDGER.csv

각 test 는 그 Finding 의 수정이 정본에 남아 있는지만 본다. Runtime 증거는 Consumer E2E 가 갖는다.
"""

from __future__ import annotations

import csv
import io
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

REPO_ROOT = Path(os.environ.get("CPF_CONSUMER_REGRESSION_ROOT") or Path(__file__).resolve().parents[3])

FINDING_LEDGER = REPO_ROOT / "cpf-docs/governance/development-harness/current/CONSUMER_RUNTIME_FINDING_LEDGER.csv"
OPEN_GIT_SURFACE_POLICY = REPO_ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"
CLI_SOURCE = REPO_ROOT / "cpf-tools/runtime/cli/java/CpfCli.java"
BOOTSTRAP_SOURCE = REPO_ROOT / "cpf-tools/runtime/bootstrap/CpfBootstrap.java"
SECURITY_COMMON_AUTOCONFIG = REPO_ROOT / "cpf-starters/security/src/main/java/com/cpf/security/common/CpfSecurityCommonAutoConfiguration.java"
SECURITY_AUTOCONFIG_IMPORTS = REPO_ROOT / "cpf-starters/security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
JDBC_AUTOCONFIG_IMPORTS = REPO_ROOT / "cpf-starters/data/persistence/jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
ADM_PROFILE = REPO_ROOT / "cpf-admin/src/main/resources/application-adm.yml"
ADM_LOCAL_PROFILE = REPO_ROOT / "cpf-admin/src/main/resources/application-adm-local.yml"
ADM_BOOTSTRAP_INITIALIZER = REPO_ROOT / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java"
BATCH_OWNER_ADAPTER = REPO_ROOT / "cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java"
MBW_CONFIG = REPO_ROOT / "cpf-backoffice/online/src/main/resources/application.yml"
MBW_SECURITY_CONFIG = REPO_ROOT / "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/config/BackofficeSecurityConfiguration.java"
MBW_BOOTSTRAP_SERVICE = REPO_ROOT / "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java"
MBW_BOOTSTRAP_RUNNER = REPO_ROOT / "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapRunner.java"
CHANNEL_FRONT_JSON_CONFIG = REPO_ROOT / "cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/web/BackofficeWebJsonConfiguration.java"
PLATFORM_SCHEMA_JSON = REPO_ROOT / "cpf-tools/db/canonical/platform-schema.json"
SEED_MODEL_JSON = REPO_ROOT / "cpf-tools/db/canonical/seed-model.json"
GENERATED_CURRENT = REPO_ROOT / "cpf-tools/db/generated/current"

OFFICIAL_VENDORS = ("mariadb", "oracle", "postgresql")


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def ledger_rows() -> list[dict[str, str]]:
    with io.open(FINDING_LEDGER, encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def executable_lines(text: str) -> list[str]:
    """주석은 계약 위반이 아니다. 근거 기록을 남길 수 있어야 한다."""
    lines = []
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("#") or line.startswith("//") or line.startswith("*") or line.startswith("@rem"):
            continue
        lines.append(line)
    return lines


class ConsumerRuntimeFindingLedger(unittest.TestCase):
    """원장 자체가 Finding 단위를 유지하는지 본다."""

    def test_every_finding_keeps_its_own_row(self) -> None:
        rows = ledger_rows()
        self.assertTrue(rows, "Finding 원장이 비어 있다")
        ids = [row["finding_id"] for row in rows]
        self.assertEqual(len(ids), len(set(ids)), "Finding id 가 중복됐다")
        required = ("symptom", "direct_root_cause", "upper_root_cause", "fixed_source",
                    "affected_consumer", "targeted_test", "negative_mutation",
                    "runtime_evidence", "prevention_rule")
        empty: list[str] = []
        for row in rows:
            for field in required:
                if not str(row.get(field, "")).strip():
                    empty.append(f"{row['finding_id']}.{field}")
        self.assertEqual([], empty, f"Finding 원장에 빈 필수 항목: {empty}")

    def test_direct_root_causes_are_not_collapsed(self) -> None:
        """상위 Root Cause 를 공유해도 직접 원인은 서로 달라야 한다."""
        rows = ledger_rows()
        direct = [row["direct_root_cause"].strip() for row in rows]
        duplicated = {value for value in direct if direct.count(value) > 1}
        # 같은 직접 원인을 가진 Finding 이 있다면 그것은 하나의 Finding 이어야 한다.
        self.assertEqual(set(), duplicated, f"직접 Root Cause 가 중복된 Finding: {sorted(duplicated)}")


class PublicRuntimeLifecycleContract(unittest.TestCase):
    """CRF-11 / CRF-17 / CRF-18 / CRF-19 / CRF-36 / CRF-42"""

    def test_public_runtime_lifecycle_is_self_contained(self) -> None:
        cli = read(CLI_SOURCE)
        self.assertIn('return requireJava25Then(() -> runClass(root, "CpfBootstrap", listing));', cli,
                      "targets가 canonical Java Lifecycle engine으로 가지 않는다")
        self.assertIn('return requireJava25Then(() -> runClass(root, "CpfBootstrap", forwarded));', cli,
                      "Lifecycle selector가 canonical Java engine으로 가지 않는다")
        self.assertNotIn('internalRuntime(root, action, forwarded)', cli,
                         "INTERNAL profile이 구형 Python engine으로 갈라져 Group 계약을 잃는다")
        bootstrap = read(BOOTSTRAP_SOURCE)
        for action in ("start", "stop", "status", "health", "log", "restart"):
            self.assertIn(f'case "{action}"', bootstrap,
                          f"공개 Runtime Lifecycle 에 {action} 동작이 없다")

    def test_runtime_environment_carries_db_vendor(self) -> None:
        """CRF-17."""
        self.assertIn('baseEnv.put("CPF_DB_VENDOR"', read(BOOTSTRAP_SOURCE),
                      "Runtime 환경에 DB vendor 를 전달하지 않는다")

    def test_runtime_environment_carries_vendor_pack_root(self) -> None:
        """CRF-18."""
        self.assertIn('baseEnv.put("CPF_DB_RESOURCE_ROOT"', read(BOOTSTRAP_SOURCE),
                      "Runtime 환경에 Vendor SQL Pack 경로를 전달하지 않는다")

    def test_bootstrap_prepares_local_only_runtime_secrets(self) -> None:
        bootstrap = read(BOOTSTRAP_SOURCE)
        self.assertIn("ensureLocalSecret(", bootstrap,
                      "local 전용 Runtime Secret 준비 경로가 없다")
        self.assertIn("CPF_ADM_APPROVAL_PROOF_KEY_BASE64", bootstrap,
                      "ADM 이 기동에 요구하는 local Secret 이 준비되지 않는다")

    def test_launcher_assigns_per_target_instance_id(self) -> None:
        bootstrap = read(BOOTSTRAP_SOURCE)
        self.assertIn('"CPF_RUNTIME_INSTANCE_ID"', bootstrap,
                      "한 Host 다중 Runtime 을 위한 instanceId 부여가 없다")
        self.assertIn("runtimeHostName() + \"-\" + target.name()", bootstrap,
                      "instanceId 가 Target 별로 유일하지 않다")

    def test_source_runtime_runs_its_built_artifact(self) -> None:
        """CRF-42. cpf runtime start 가 실행물을 만들어 그 실행물을 직접 띄운다."""
        bootstrap = read(BOOTSTRAP_SOURCE)
        self.assertIn("buildSourceRuntimeJar(", bootstrap,
                      "source Runtime 을 실행물로 만들어 띄우지 않는다")
        command = bootstrap.split("private List<String> runtimeCommand(", 1)
        self.assertEqual(2, len(command), "runtimeCommand 진입점을 찾지 못했다")
        self.assertIn('javaExecutable(), "-jar"', command[1][:600],
                      "runtimeCommand 가 실행물을 직접 띄우지 않는다")

    def test_bootstrap_domain_runtime_runs_its_built_artifact(self) -> None:
        """CRF-45. cpf bootstrap --run 도 같은 모델이어야 상태 파일의 pid 가 곧 Runtime 이다."""
        bootstrap = read(BOOTSTRAP_SOURCE)
        self.assertIn("buildDomainRuntimeJar(", bootstrap,
                      "bootstrap 이 Generated Domain Runtime 을 실행물로 만들어 띄우지 않는다")
        body = bootstrap.split("private void startRuntimes()", 1)
        self.assertEqual(2, len(body), "startRuntimes 진입점을 찾지 못했다")
        self.assertIn('javaExecutable(), "-jar"', body[1][:1600],
                      "bootstrap 이 Gradle wrapper 를 Runtime pid 로 기록한다")

    def test_no_runtime_is_started_through_gradle_bootrun(self) -> None:
        """어느 경로로도 wrapper pid 를 Runtime pid 로 기록하지 않는다."""
        self.assertNotIn("bootRun", "\n".join(executable_lines(read(BOOTSTRAP_SOURCE))),
                         "source Runtime 이 여전히 bootRun 으로 기동된다")


class PublicRuntimeAssetContract(unittest.TestCase):
    """CRF-18 / CRF-38"""

    def _template_targets(self) -> list[str]:
        policy = json.loads(read(OPEN_GIT_SURFACE_POLICY))
        rules = policy.get("templateRules", []) + policy.get("sourceRules", [])
        return [str(rule.get("target", "")) for rule in rules]

    def test_vendor_sql_pack_is_public_asset(self) -> None:
        targets = self._template_targets()
        for vendor in OFFICIAL_VENDORS:
            expected = f"deploy/local/db/vendor/{vendor}"
            self.assertIn(expected, targets,
                          f"{vendor} Vendor SQL Pack 이 공개 배포본에 투영되지 않는다")

    def test_platform_and_prebuilt_domain_ddl_are_public_assets(self) -> None:
        targets = self._template_targets()
        for vendor in OFFICIAL_VENDORS:
            platform = [t for t in targets if t.startswith(f"deploy/local/db/platform/{vendor}/")]
            domain = [t for t in targets if t.startswith(f"deploy/local/db/domain/backoffice/{vendor}/")]
            self.assertTrue(platform, f"{vendor} Platform DDL 이 공개 자산이 아니다")
            self.assertTrue(domain, f"{vendor} Prebuilt Domain DDL 이 공개 자산이 아니다")
        bootstrap = read(BOOTSTRAP_SOURCE)
        self.assertIn("PLATFORM_ASSET_ROOT", bootstrap, "Platform DDL 적용 경로가 없다")
        self.assertIn("DOMAIN_ASSET_ROOT", bootstrap, "Prebuilt Domain DDL 적용 경로가 없다")


class StarterAutoConfigurationContract(unittest.TestCase):
    """CRF-14 / CRF-32 / CRF-20"""

    def test_security_common_autoconfiguration_is_registered(self) -> None:
        registered = read(SECURITY_AUTOCONFIG_IMPORTS)
        self.assertIn("com.cpf.security.common.CpfSecurityCommonAutoConfiguration", registered,
                      "CMN 보안 Service 가 AutoConfiguration 으로 공급되지 않는다")

    def test_security_common_provides_crypto_service(self) -> None:
        """CRF-14. ADM 단독 기동이 CmnCryptoService bean 없음으로 실패했다."""
        self.assertIn("CmnCryptoService cmnCryptoService(", read(SECURITY_COMMON_AUTOCONFIG),
                      "CmnCryptoService 를 공급하지 않는다")

    def test_security_common_provides_jwt_service(self) -> None:
        """CRF-32. MBW 단독 기동이 CmnJwtService bean 없음으로 실패했다."""
        self.assertIn("CmnJwtService cmnJwtService(", read(SECURITY_COMMON_AUTOCONFIG),
                      "CmnJwtService 를 공급하지 않는다")

    def test_platform_role_primary_is_provided_by_starter(self) -> None:
        registered = read(JDBC_AUTOCONFIG_IMPORTS)
        self.assertIn("com.cpf.data.persistence.jdbc.CpfPlatformRoleDataSourcePrimaryAutoConfiguration", registered,
                      "Platform role 기본 선택이 Starter AutoConfiguration 으로 제공되지 않는다")


class ControlPlaneRuntimeContract(unittest.TestCase):
    """CRF-22 / CRF-25 / CRF-27 / CRF-28 / CRF-21 / CRF-24"""

    def test_platform_runtime_declares_service_identity(self) -> None:
        profile = read(ADM_PROFILE)
        self.assertIn("service-id:", profile,
                      "SystemCode 가 없는 Platform Runtime 이 Service Identity 를 명시하지 않는다")

    def test_control_plane_surface_is_exempt_from_business_headers(self) -> None:
        """CRF-25. 관리 API 가 업무 System6 Header 를 요구해 400 으로 거절됐다."""
        self.assertIn("management-root-paths:", read(ADM_PROFILE),
                      "Control Plane Surface 가 업무 Header 강제 대상에서 제외되지 않는다")

    def test_control_plane_spa_root_is_listed(self) -> None:
        """CRF-27. SPA 진입 경로가 목록에 없으면 화면 자체를 열 수 없다."""
        self.assertIn("- /adm", read(ADM_PROFILE),
                      "ADM Surface 가 업무 Header 강제 제외 목록에 없다")

    def test_control_plane_runtime_declares_platform_db_role(self) -> None:
        """CRF-13."""
        self.assertIn("cpf-platform-db:", read(ADM_PROFILE),
                      "Control Plane Runtime 이 자기 설정으로 CPF_PLATFORM_DB role 을 해석하지 못한다")

    def test_local_profile_allows_loopback_cookie(self) -> None:
        local = read(ADM_LOCAL_PROFILE)
        self.assertIn("CPF_ADM_SESSION_COOKIE_SECURE:false", local,
                      "local 평문 HTTP 경로에서 세션/CSRF 쿠키가 성립하지 않는다")

    def test_injection_constructor_is_explicit(self) -> None:
        for path in (ADM_BOOTSTRAP_INITIALIZER, BATCH_OWNER_ADAPTER):
            text = read(path)
            self.assertIn("Autowired", text,
                          f"다중 생성자 Bean 이 주입 생성자를 명시하지 않는다: {path.name}")

    @staticmethod
    def _assert_opens_managed_context(case: unittest.TestCase, path) -> None:
        text = read(path)
        case.assertIn("CpfContexts.bind(", text,
                      f"ApplicationRunner 가 관리 실행 Context 를 열지 않는다: {path.name}")
        case.assertIn("newRoot(", text,
                      f"관리 실행 Root Context 생성이 없다: {path.name}")

    def test_control_plane_bootstrap_runner_opens_managed_context(self) -> None:
        """CRF-24."""
        self._assert_opens_managed_context(self, ADM_BOOTSTRAP_INITIALIZER)

    def test_business_domain_bootstrap_runner_opens_managed_context(self) -> None:
        """CRF-43."""
        self._assert_opens_managed_context(self, MBW_BOOTSTRAP_RUNNER)


class BusinessDomainRuntimeContract(unittest.TestCase):
    """CRF-31 / CRF-33 / CRF-34 / CRF-35 / CRF-37 / CRF-40 / CRF-44"""

    def test_business_domain_declares_platform_db_role(self) -> None:
        """CRF-30. 업무 Domain Runtime 도 code/message 정본 때문에 CPF_PLATFORM_DB 를 요구한다."""
        self.assertIn("cpf-platform-db:", read(MBW_CONFIG),
                      "업무 Domain Runtime 이 자기 설정으로 CPF_PLATFORM_DB role 을 해석하지 못한다")

    def test_business_domain_declares_domain_persistence(self) -> None:
        """CRF-31."""
        config = read(MBW_CONFIG)
        self.assertIn("generated-domain:", config, "Business Domain 선언이 없다")
        self.assertIn("persistence:", config,
                      "Business Domain 계약을 선언하고 domain persistence 를 선언하지 않았다")
        self.assertIn("data-source-prefix:", config,
                      "Domain DataSource 를 만들 prefix 선언이 없다")

    def test_mybatis_provider_requires_mapper_resources(self) -> None:
        """provider 는 실제 구현과 일치해야 한다. mapper 가 없으면 mybatis 를 선언하지 않는다."""
        config = read(MBW_CONFIG)
        match = re.search(r"^\s*provider:\s*(\w+)", config, re.M)
        self.assertIsNotNone(match, "domain persistence provider 선언이 없다")
        provider = match.group(1)
        mapper_root = REPO_ROOT / "cpf-backoffice/online/src/main/resources"
        mappers = [p for p in mapper_root.rglob("*.xml") if "mapper" in p.as_posix().lower()] if mapper_root.is_dir() else []
        if provider == "mybatis":
            self.assertTrue(mappers, "mybatis provider 를 선언했는데 mapper 자원이 없다")
        else:
            self.assertEqual("jdbc", provider, f"지원하지 않는 provider: {provider}")

    def test_transactional_beans_are_proxyable(self) -> None:
        text = read(MBW_BOOTSTRAP_SERVICE)
        self.assertNotIn("public final class BackofficeInitialOperatorBootstrapService", text,
                         "프록시 대상 Bean 이 final class 다")

    def test_cpf_service_extends_domain_base_class(self) -> None:
        text = read(MBW_BOOTSTRAP_SERVICE)
        self.assertIn("@CpfService", text, "@CpfService 선언이 사라졌다")
        self.assertIn("extends", text.split("class BackofficeInitialOperatorBootstrapService", 1)[1][:200],
                      "@CpfService 가 Domain Base Class 를 상속하지 않는다")

    @staticmethod
    def _check_expression(constraint_name: str) -> str:
        """Runtime Role 정본은 Platform Registry 의 check 제약이다.

        BAT_* 의 runtime_role 은 Batch Platform 전용 열거이며 cpf.runtime.role 과 다른 namespace 다.
        이름으로 지목하지 않으면 다른 namespace 의 제약을 정본으로 착각한다.
        """
        model = json.loads(read(PLATFORM_SCHEMA_JSON))
        for table in model.get("tables", []):
            for check in (table.get("checkConstraints") or table.get("checks") or []):
                if check.get("name") == constraint_name:
                    return str(check.get("expression", ""))
        raise AssertionError(f"정본 check 제약을 찾지 못했다: {constraint_name}")

    def test_runtime_role_uses_canonical_values(self) -> None:
        expression = self._check_expression("ck_ops_runtime_instance_role")
        match = re.search(r"runtime_role IN \(([^)]*)\)", expression)
        self.assertIsNotNone(match, "Runtime Role 허용값 목록을 찾지 못했다")
        allowed = {value.strip().strip("'") for value in match.group(1).split(",")}
        config = read(MBW_CONFIG)
        role = re.search(r"^\s*runtime:\s*\n(?:\s*#.*\n)*\s*role:\s*(\S+)", config, re.M)
        self.assertIsNotNone(role, "Runtime Role 선언을 찾지 못했다")
        self.assertIn(role.group(1), allowed,
                      f"Runtime Role 이 정본 제약 밖의 값이다: {role.group(1)} not in {sorted(allowed)}")

    def test_batch_runtime_role_stays_a_separate_namespace(self) -> None:
        """SystemCode/Service Identity 처럼 Runtime Role 도 namespace 가 섞이면 안 된다."""
        platform = self._check_expression("ck_ops_runtime_instance_role")
        batch = self._check_expression("ck_bat_runtime_instance_role")
        self.assertIn("'APPLICATION'", platform,
                      "Platform Runtime Registry 가 업무 Domain Runtime 의 role 을 받지 못한다")
        self.assertNotIn("'APPLICATION'", batch,
                         "Batch Platform 전용 role 열거가 Platform Runtime role 과 합쳐졌다")

    def test_channel_front_provides_jackson2_object_mapper(self) -> None:
        text = read(CHANNEL_FRONT_JSON_CONFIG)
        self.assertIn("com.fasterxml.jackson.databind.ObjectMapper", text,
                      "Channel Front 가 코드에서 쓰는 Jackson 2 Mapper 를 공급하지 않는다")

    def test_runtime_declares_its_own_security_chain(self) -> None:
        text = read(MBW_SECURITY_CONFIG)
        self.assertIn("SecurityFilterChain", text,
                      "Runtime 이 자기 인가 경계를 선언하지 않는다")


class InitialOperatorBootstrapContract(unittest.TestCase):
    """CRF-41"""

    def test_initial_operator_predicate_requires_usable_credential(self) -> None:
        for vendor in OFFICIAL_VENDORS:
            path = REPO_ROOT / f"cpf-tools/db/vendor/{vendor}/runtime/backoffice/repository/auth-bootstrap-operator-count.sql"
            self.assertTrue(path.is_file(), f"{vendor} 최초 설치 판정 SQL 이 없다")
            sql = read(path)
            self.assertIn("password_hash IS NOT NULL", sql,
                          f"{vendor} 설치 완료 판정이 인증 불가능한 행까지 센다")

    def test_initial_operator_is_usable_immediately(self) -> None:
        for vendor in OFFICIAL_VENDORS:
            path = REPO_ROOT / f"cpf-tools/db/vendor/{vendor}/runtime/backoffice/repository/auth-bootstrap-operator.sql"
            self.assertTrue(path.is_file(), f"{vendor} 최초 운영자 생성 SQL 이 없다")
            sql = read(path)
            self.assertIn("0, 'N', :passwordExpireAt", sql,
                          f"{vendor} 최초 운영자가 강제 비밀번호 변경 상태로 생성된다")


class CanonicalDatabaseContract(unittest.TestCase):
    """CRF-39"""

    @staticmethod
    def _declared_keys(schema_text: str) -> dict[str, list[tuple[str, ...]]]:
        keys: dict[str, list[tuple[str, ...]]] = {}
        table = None
        for line in schema_text.splitlines():
            created = re.match(r"CREATE TABLE (\w+)", line)
            if created:
                table = created.group(1).upper()
                keys.setdefault(table, [])
                continue
            if table:
                for pattern in (r"CONSTRAINT \w+ PRIMARY KEY \(([^)]*)\)", r"CONSTRAINT \w+ UNIQUE \(([^)]*)\)"):
                    for columns in re.findall(pattern, line, re.I):
                        keys[table].append(tuple(sorted(c.strip().lower() for c in columns.split(","))))
                if line.strip().startswith(");"):
                    table = None
        for match in re.finditer(r"CREATE UNIQUE INDEX \w+ ON (\w+)\s*\(([^)]*)\)", schema_text, re.I):
            keys.setdefault(match.group(1).upper(), []).append(
                tuple(sorted(c.strip().lower() for c in match.group(2).split(","))))
        return keys

    def test_canonical_seed_matches_schema_contract(self) -> None:
        """seed 의 ON CONFLICT 대상은 schema 가 선언한 유일키여야 한다."""
        checked = 0
        mismatched: list[str] = []
        for schema_name, seed_name in (("cpf-platform-schema.sql", "cpf-platform-seed.sql"),
                                       ("backoffice-schema.sql", "backoffice-seed.sql")):
            schema_path = GENERATED_CURRENT / "postgresql" / schema_name
            seed_path = GENERATED_CURRENT / "postgresql" / seed_name
            if not schema_path.is_file() or not seed_path.is_file():
                continue
            keys = self._declared_keys(read(schema_path))
            for chunk in read(seed_path).split("INSERT INTO ")[1:]:
                table = re.match(r"(\w+)", chunk).group(1).upper()
                body = chunk.split(";", 1)[0]
                conflict = re.search(r"ON CONFLICT \(([^)]*)\)", body)
                if not conflict:
                    continue
                checked += 1
                columns = tuple(sorted(c.strip().lower() for c in conflict.group(1).split(",")))
                if columns not in keys.get(table, []):
                    mismatched.append(f"{seed_name}:{table}{columns}")
        self.assertTrue(checked, "ON CONFLICT 를 쓰는 seed 문장을 하나도 찾지 못했다")
        self.assertEqual([], mismatched, f"schema 가 선언하지 않은 유일키로 seed 가 충돌 처리한다: {mismatched}")

    def test_seed_values_satisfy_check_constraints(self) -> None:
        """seed 가 넣는 값이 schema check 제약 목록 안에 있어야 한다."""
        schema_path = GENERATED_CURRENT / "postgresql" / "backoffice-schema.sql"
        seed_path = GENERATED_CURRENT / "postgresql" / "backoffice-seed.sql"
        if not schema_path.is_file() or not seed_path.is_file():
            self.skipTest("backoffice 정본 SQL 이 없다")
        match = re.search(r"employment_status IN \(([^)]*)\)", read(schema_path))
        self.assertIsNotNone(match, "employment_status 제약을 찾지 못했다")
        allowed = {value.strip().strip("'") for value in match.group(1).split(",")}
        seed = read(seed_path)
        used = set()
        for chunk in seed.split("INSERT INTO ")[1:]:
            if not chunk.upper().startswith("MBW_EMPLOYEE "):
                continue
            for value in re.findall(r"'([A-Z_]+)'", chunk.split(";", 1)[0]):
                if value in {"SYSTEM", "Y", "N"}:
                    continue
                used.add(value)
        offending = {value for value in used if value.endswith("ACTIVE") and value not in allowed}
        self.assertEqual(set(), offending,
                         f"seed 가 employment_status 제약 밖의 값을 쓴다: {sorted(offending)}")


if __name__ == "__main__":
    unittest.main(verbosity=2)
