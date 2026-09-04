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


if __name__ == "__main__":
    unittest.main(verbosity=2)
