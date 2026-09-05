"""발행된 Runtime 이 단독으로 기동 가능한지를 정본 계약으로 고정한다.

증상 근거: Fresh Open Git Consumer 가 공개 launcher 로 ADM 을 기동하자 두 번 연속으로 죽었다.

1. `logging.config: classpath:log/cpf-logback-spring.xml` 이 가리키는 자원이 저장소에 존재한
   적이 없었다. Logging 초기화 단계에서 FileNotFoundException 으로 즉시 종료했다.
2. `AdmJdbcConfig` 는 CPF_PLATFORM_DB role 로만 DataSource 를 얻는데 ADM 자신의 설정에는 그
   role 선언이 없었다. `CPF DataSource is required for role: CPF_PLATFORM_DB` 로 죽었다.

원인: 두 결함 모두 One-WAS 통합 Runtime 이 가려 왔다. 통합 Runtime 은 자기 설정과 batch
runtime-support 의 선언을 쓰므로 ADM 단독 기동 경로가 한 번도 성립하지 않았고, 아무도
그 사실을 몰랐다.

되돌리면 재발할 증상: 공개 Consumer 가 발행된 Runtime 을 java -jar 로 띄우지 못한다. 통합
Runtime 으로만 검증하면 이 결함은 다시 보이지 않는다.
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

REPO_ROOT = Path(os.environ.get("CPF_RUNTIME_STARTABILITY_ROOT") or Path(__file__).resolve().parents[3])

RUNTIME_CATALOG = REPO_ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"
DATABASE_ROLE_ENUM = REPO_ROOT / "cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfDatabaseRole.java"

# 빌드 산출물과 IDE 출력은 Source 가 아니다.
IGNORED_DIRECTORIES = {"build", "bin", "out", "node_modules", ".gradle", "cpf-release"}

CLASSPATH_CONFIG = re.compile(r"^\s*config:\s*classpath:(?P<resource>\S+)\s*$")
ROLE_REFERENCE = re.compile(r"CpfDatabaseRole\.(?P<role>[A-Z][A-Z0-9_]*)")
CONFIG_IMPORT = re.compile(r"classpath:(?P<name>application[-A-Za-z0-9_.$}{:]*\.yml)")
ENUM_CONSTANT = re.compile(r"^\s{4}(?P<name>[A-Z][A-Z0-9_]*)\s*,?\s*$")
TABLE_LOOKUP = ".getTables("
# Driver 가 알려 주는 저장 규칙을 쓰거나, 대소문자 변형을 모두 시도해야 한다.
IDENTIFIER_CASE_GUARDS = ("storesLowerCaseIdentifiers", "storesUpperCaseIdentifiers")
IDENTIFIER_CASE_PROBES = ("toUpperCase", "toLowerCase")
# 생성 키는 컬럼 이름을 명시해야 세 Vendor 에서 같은 의미를 갖는다.
GENERATED_KEY_FLAG = "RETURN_GENERATED_KEYS"


def is_source(path: Path) -> bool:
    return not any(part in IGNORED_DIRECTORIES for part in path.relative_to(REPO_ROOT).parts)


def resource_roots() -> list[Path]:
    return [p for p in REPO_ROOT.glob("*/**/src/main/resources") if p.is_dir() and is_source(p)]


def application_yml_files() -> list[Path]:
    files: list[Path] = []
    for root in resource_roots():
        files.extend(sorted(root.glob("application*.yml")))
    return files


def database_roles() -> set[str]:
    """role 이름의 정본은 enum 이다. 검증기가 목록을 따로 들지 않는다."""
    text = DATABASE_ROLE_ENUM.read_text(encoding="utf-8")
    body = text.split("{", 1)[1]
    roles = set()
    for line in body.splitlines():
        match = ENUM_CONSTANT.match(line.rstrip(";"))
        if match:
            roles.add(match.group("name"))
    return roles


def role_property_key(role: str) -> str:
    """CpfJdbcRoleDataSourceAutoConfiguration 의 property 이름 규칙과 같은 변환이다."""
    return role.lower().replace("_", "-")


def runtime_owner_modules() -> list[Path]:
    """catalog 가 선언한 Runtime 중 저장소에 Source 를 가진 owner 모듈."""
    catalog = json.loads(RUNTIME_CATALOG.read_text(encoding="utf-8"))
    modules: list[Path] = []
    for entry in catalog.get("runtimes", []):
        owner = REPO_ROOT / str(entry.get("owner", ""))
        if owner.is_dir() and (owner / "src/main/java").is_dir():
            modules.append(owner)
    return modules


def reachable_yaml(module: Path) -> list[Path]:
    """모듈 자신의 설정과 spring.config.import 로 끌어오는 설정 파일."""
    own = sorted((module / "src/main/resources").glob("application*.yml")) if (module / "src/main/resources").is_dir() else []
    wanted: set[str] = set()
    for path in own:
        for match in CONFIG_IMPORT.finditer(path.read_text(encoding="utf-8")):
            name = match.group("name")
            # ${spring.profiles.active} 를 끼운 이름은 정적으로 확정할 수 없으므로 건너뛴다.
            if "$" in name:
                continue
            wanted.add(name)
    imported = [p for root in resource_roots() for p in root.glob("application*.yml") if p.name in wanted]
    return own + imported


class RuntimeStandaloneStartabilityContract(unittest.TestCase):

    def test_logging_config_resources_exist(self) -> None:
        """logging.config 가 가리키는 classpath 자원은 실제로 존재해야 한다."""
        roots = resource_roots()
        self.assertTrue(roots, "resources 루트를 하나도 찾지 못했다")
        available = {
            path.relative_to(root).as_posix()
            for root in roots
            for path in root.rglob("*")
            if path.is_file()
        }
        checked = 0
        missing: list[str] = []
        for yml in application_yml_files():
            for line in yml.read_text(encoding="utf-8").splitlines():
                match = CLASSPATH_CONFIG.match(line)
                if not match:
                    continue
                resource = match.group("resource").lstrip("/")
                if "$" in resource:
                    continue
                checked += 1
                if resource not in available:
                    missing.append(f"{yml.relative_to(REPO_ROOT).as_posix()} -> {resource}")
        self.assertTrue(checked, "classpath 로 지정된 logging.config 선언을 하나도 찾지 못했다")
        self.assertEqual([], missing, f"존재하지 않는 logging classpath 자원: {missing}")

    def test_runtime_declares_every_database_role_it_requires(self) -> None:
        """Runtime 이 요구하는 DB role 은 그 Runtime 의 설정만으로 해석되어야 한다."""
        roles = database_roles()
        self.assertTrue(roles, "CpfDatabaseRole enum 을 읽지 못했다")
        modules = runtime_owner_modules()
        self.assertTrue(modules, "Source 를 가진 Runtime owner 모듈을 찾지 못했다")

        checked = 0
        undeclared: list[str] = []
        for module in modules:
            required = set()
            for java in (module / "src/main/java").rglob("*.java"):
                for match in ROLE_REFERENCE.finditer(java.read_text(encoding="utf-8")):
                    if match.group("role") in roles:
                        required.add(match.group("role"))
            if not required:
                continue
            declared = "\n".join(path.read_text(encoding="utf-8") for path in reachable_yaml(module))
            for role in sorted(required):
                checked += 1
                # 해석 경로는 두 가지다. role-datasources 로 직접 선언하거나, Generated Domain 이
                # cpf.generated-domain.database-role 로 자기 role 을 선언하면 표준 Domain DataSource 가
                # 그 role 로 해석된다(CpfJdbcDataSourceRegistry).
                declares_role_datasource = role_property_key(role) + ":" in declared
                declares_generated_domain_role = ("database-role: " + role) in declared
                if not declares_role_datasource and not declares_generated_domain_role:
                    undeclared.append(f"{module.relative_to(REPO_ROOT).as_posix()} -> {role}")
        self.assertTrue(checked, "DB role 을 요구하는 Runtime 을 찾지 못했다")
        self.assertEqual([], undeclared, f"Runtime 설정에 선언되지 않은 DB role: {undeclared}")


    def test_table_existence_checks_do_not_assume_identifier_case(self) -> None:
        """JDBC metadata 의 table pattern 은 대소문자를 그대로 비교한다.

        PostgreSQL 은 따옴표 없는 식별자를 소문자로 접어 저장하므로, 대문자 이름을 고정으로 넘기면
        테이블이 있어도 못 찾는다. 실제로 ADM 단독 기동이 "Missing CPF broker table" 로 죽었다.
        """
        roots = [REPO_ROOT / "cpf-starters", REPO_ROOT / "cpf-admin/src/main",
                 REPO_ROOT / "cpf-gateway/src/main", REPO_ROOT / "cpf-batch"]
        checked = 0
        unguarded: list[str] = []
        for root in roots:
            if not root.is_dir():
                continue
            for java in sorted(root.rglob("*.java")):
                if not is_source(java) or "/test/" in java.as_posix():
                    continue
                text = java.read_text(encoding="utf-8")
                if TABLE_LOOKUP not in text:
                    continue
                checked += 1
                declares_rule = any(guard in text for guard in IDENTIFIER_CASE_GUARDS)
                probes_variants = all(probe in text for probe in IDENTIFIER_CASE_PROBES)
                if not declares_rule and not probes_variants:
                    unguarded.append(java.relative_to(REPO_ROOT).as_posix())
        self.assertTrue(checked, "JDBC table 존재 검사를 하나도 찾지 못했다")
        self.assertEqual([], unguarded, f"식별자 대소문자 규칙을 고려하지 않는 table 존재 검사: {unguarded}")


    def test_generated_keys_name_their_key_columns(self) -> None:
        """생성 키는 컬럼을 명시해야 한다.

        PostgreSQL Driver 는 컬럼을 지정하지 않으면 삽입한 행 전체를 생성 키로 돌려주고
        KeyHolder.getKey() 가 "contains multiple keys" 로 실패한다. Oracle 은 ROWID 를 돌려준다.
        실제로 ADM 최초 운영자 생성이 이 이유로 기동 직후 죽었다.
        """
        roots = [REPO_ROOT / "cpf-starters", REPO_ROOT / "cpf-admin/src/main",
                 REPO_ROOT / "cpf-gateway/src/main", REPO_ROOT / "cpf-batch",
                 REPO_ROOT / "cpf-backoffice/online/src/main"]
        offenders: list[str] = []
        scanned = 0
        for root in roots:
            if not root.is_dir():
                continue
            for java in sorted(root.rglob("*.java")):
                if not is_source(java) or "/test/" in java.as_posix():
                    continue
                scanned += 1
                if GENERATED_KEY_FLAG in java.read_text(encoding="utf-8"):
                    offenders.append(java.relative_to(REPO_ROOT).as_posix())
        self.assertTrue(scanned, "Runtime Source 를 하나도 읽지 못했다")
        self.assertEqual([], offenders, f"생성 키 컬럼을 명시하지 않은 INSERT: {offenders}")


if __name__ == "__main__":
    unittest.main(verbosity=2)
