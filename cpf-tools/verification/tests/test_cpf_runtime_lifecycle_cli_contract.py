"""CPF Public Runtime 의 전체 / 논리 Group / 개별 Target Lifecycle CLI 계약.

사용자는 내부 Module, Gradle Project, Private Source, OS별 wrapper 구조를 몰라도 canonical `cpf`
명령으로 start/stop/restart/status/health/log 를 수행할 수 있어야 한다. Group 과 Generated Domain
Target 은 canonical machine-readable authority 에서 파생하며 대상 목록을 CLI Source 에 복제하지 않는다.

정본 Rule: cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md §38
"""

from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

REPO_ROOT = Path(os.environ.get("CPF_RUNTIME_CLI_ROOT") or Path(__file__).resolve().parents[3])

CATALOG = REPO_ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json"
CLI_SOURCE = REPO_ROOT / "cpf-tools/runtime/cli/java/CpfCli.java"
TARGETS_SOURCE = REPO_ROOT / "cpf-tools/runtime/cli/java/CpfRuntimeTargets.java"
BOOTSTRAP_SOURCE = REPO_ROOT / "cpf-tools/runtime/bootstrap/CpfBootstrap.java"
README = REPO_ROOT / "cpf-tools/release/open-git/templates/README.md"
LAUNCHER_DIR = REPO_ROOT / "cpf-tools/release/open-git/templates/bin"
HARNESS = REPO_ROOT / "cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md"
WORK_ITEM_REGISTRY = REPO_ROOT / "cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv"

LIFECYCLE_VERBS = ("start", "stop", "restart", "status", "health", "log")
REQUIRED_GROUPS = ("all", "platform", "domains", "batch", "backoffice-stack")
CLI_SOURCES = ("cpf-tools/runtime/cli/java/CpfCli.java",
               "cpf-tools/runtime/cli/java/CpfRuntimeTargets.java",
               "cpf-tools/runtime/bootstrap/CpfBootstrap.java")


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def catalog() -> dict:
    return json.loads(read(CATALOG))


def static_targets() -> list[dict]:
    return catalog()["runtimes"]


def group_rows() -> list[dict]:
    return catalog()["runtimeGroups"]["groups"]


class RuntimeGroupAuthority(unittest.TestCase):
    """Group 은 machine-readable authority 한 곳에서만 정의된다."""

    def test_canonical_groups_exist(self) -> None:
        declared = {row["group"] for row in group_rows()}
        missing = [name for name in REQUIRED_GROUPS if name not in declared]
        self.assertEqual([], missing, f"canonical Runtime Group 이 없다: {missing}")

    def test_all_group_selects_every_target(self) -> None:
        rows = {row["group"]: row for row in group_rows()}
        self.assertIn("all", rows, "전체 제어 Group 이 없다")
        self.assertEqual("every-resolved-target", rows["all"]["selector"],
                         "all Group 이 전체 Target 을 선택하지 않는다")

    def test_group_names_never_collide_with_target_names(self) -> None:
        """같은 문자열이 Group 과 Runtime 두 의미를 동시에 가지면 안 된다."""
        groups = {row["group"] for row in group_rows()}
        targets = {row["target"] for row in static_targets()}
        collision = sorted(groups & targets)
        self.assertEqual([], collision, f"Group 이름과 Runtime target 이름이 겹친다: {collision}")

    def test_no_duplicate_group_or_target(self) -> None:
        names = [row["group"] for row in group_rows()]
        self.assertEqual(len(names), len(set(names)), "Group 이 중복 선언됐다")
        targets = [row["target"] for row in static_targets()]
        self.assertEqual(len(targets), len(set(targets)), "Runtime target 이 중복 선언됐다")

    def test_group_membership_is_derived_from_metadata(self) -> None:
        """Group 정의는 이름 배열이 아니라 metadata selector 여야 한다."""
        keys = set(catalog()["runtimeGroups"]["membershipKeys"])
        for row in group_rows():
            selector = row["selector"]
            if selector == "every-resolved-target":
                continue
            self.assertIn(selector, keys,
                          f"Group {row['group']} 이 metadata selector 가 아닌 방식으로 정의됐다")
            self.assertTrue(str(row.get("value", "")).strip(),
                            f"Group {row['group']} 에 selector 값이 없다")
            self.assertNotIn("members", row,
                             f"Group {row['group']} 이 대상 이름 목록을 직접 들고 있다")


class RuntimeTargetMetadata(unittest.TestCase):
    """모든 Runtime 이 Group 파생에 필요한 metadata 를 갖는다."""

    def test_every_runtime_declares_group_metadata(self) -> None:
        missing: list[str] = []
        for row in static_targets():
            for key in ("architectureRole", "runtimeGroups", "dependsOn", "publicLifecycle", "buildSurface"):
                if key not in row:
                    missing.append(f"{row['target']}.{key}")
        self.assertEqual([], missing, f"Group 파생 metadata 가 없는 Runtime: {missing}")

    def test_declared_group_membership_resolves_to_a_real_group(self) -> None:
        """Runtime 이 선언한 Group tag 가 실재해야 한다. tag 오타는 조용한 누락이 된다."""
        declared = set()
        for row in group_rows():
            if row["selector"] == "runtimeGroups":
                declared.add(row["value"])
        dangling = []
        for row in static_targets():
            for tag in row["runtimeGroups"]:
                if tag not in declared:
                    dangling.append(f"{row['target']}:{tag}")
        dyn = catalog()["dynamicRuntimes"]["runtimeGroupsByModule"]
        for module, tags in dyn.items():
            for tag in tags:
                if tag not in declared:
                    dangling.append(f"dynamic:{module}:{tag}")
        self.assertEqual([], dangling, f"존재하지 않는 Group 을 선언한다: {dangling}")

    def test_every_group_selects_at_least_one_runtime(self) -> None:
        """Runtime 이 하나도 없는 Group 은 사용자에게 거짓 선택지를 준다."""
        empty = []
        for row in group_rows():
            if row["selector"] == "every-resolved-target":
                continue
            hit = False
            for target in static_targets():
                if row["selector"] == "runtimeGroups" and row["value"] in target["runtimeGroups"]:
                    hit = True
                if row["selector"] == "architectureRole" and row["value"] == target["architectureRole"]:
                    hit = True
            if row["selector"] == "runtimeGroups":
                for tags in catalog()["dynamicRuntimes"]["runtimeGroupsByModule"].values():
                    if row["value"] in tags:
                        hit = True
            if not hit:
                empty.append(row["group"])
        self.assertEqual([], empty, f"대상이 없는 Group: {empty}")

    def test_dependencies_resolve_and_have_no_cycle(self) -> None:
        rows = {row["target"]: row["dependsOn"] for row in static_targets()}
        unknown = [f"{name}->{dep}" for name, deps in rows.items() for dep in deps if dep not in rows]
        self.assertEqual([], unknown, f"존재하지 않는 Runtime 에 의존한다: {unknown}")
        pending = dict(rows)
        placed: set[str] = set()
        while pending:
            ready = [name for name, deps in pending.items() if all(d in placed for d in deps)]
            self.assertTrue(ready, f"Runtime dependency cycle: {sorted(pending)}")
            for name in ready:
                placed.add(name)
                pending.pop(name)

    def test_channel_front_starts_after_its_business_runtime(self) -> None:
        rows = {row["target"]: row for row in static_targets()}
        fronts = [row for row in rows.values() if row["architectureRole"] == "CHANNEL_FRONT_RUNTIME"]
        self.assertTrue(fronts, "Channel Front Runtime 이 하나도 없다")
        for front in fronts:
            self.assertTrue(front["dependsOn"],
                            f"{front['target']} 이 업무 Runtime 기동 순서를 선언하지 않는다")

    def test_lifecycle_capability_contract_covers_every_runtime(self) -> None:
        capabilities = catalog()["lifecycleCapabilities"]
        missing = [row["target"] for row in static_targets() if row["capability"] not in capabilities]
        self.assertEqual([], missing, f"Lifecycle Capability 계약이 없는 Runtime: {missing}")
        for name, contract in capabilities.items():
            for verb in LIFECYCLE_VERBS:
                if verb == "health":
                    self.assertIn("health", contract, f"{name} 에 health 계약이 없다")
                else:
                    self.assertIn(verb, contract, f"{name} 에 {verb} 지원 여부 선언이 없다")


class GeneratedDomainDiscovery(unittest.TestCase):
    """Generated Domain 은 이름을 어디에도 복제하지 않고 규칙으로 찾는다."""

    def test_domain_target_name_is_derived_from_the_domain(self) -> None:
        dyn = catalog()["dynamicRuntimes"]
        self.assertIn("{domainName}", dyn["targetPattern"],
                      "Generated Domain Target 이름이 Domain 이름에서 파생되지 않는다")
        self.assertTrue(str(dyn.get("primaryModule", "")).strip(),
                        "Domain 을 대표하는 module 선언이 없다")
        self.assertEqual("{domainName}", dyn["targetPattern"],
                         "사용자가 module 이름까지 알아야 Domain 을 띄울 수 있다")

    def test_domain_module_groups_are_declared(self) -> None:
        dyn = catalog()["dynamicRuntimes"]
        self.assertIn("runtimeGroupsByModule", dyn,
                      "Domain module 별 Group 소속 선언이 없다")
        primary = dyn["primaryModule"]
        self.assertIn("domains", dyn["runtimeGroupsByModule"].get(primary, []),
                      "Domain 대표 Runtime 이 domains Group 에 들어가지 않는다")

    def test_cli_source_never_branches_on_a_target_or_group_name(self) -> None:
        """`if target == "member"` 류의 분기가 있으면 새 Domain 마다 CLI 를 고쳐야 한다."""
        names = ({row["group"] for row in group_rows()}
                 | {row["target"] for row in static_targets()}
                 | set(domain_names()))
        offenders: list[str] = []
        for relative in CLI_SOURCES:
            code = selector_surface(REPO_ROOT / relative)
            for name in sorted(names):
                for pattern in (f'.equals("{name}")', f'case "{name}"',
                                f'equalsIgnoreCase("{name}")', f'== "{name}"'):
                    if pattern in code:
                        offenders.append(f"{relative}:{pattern}")
        self.assertEqual([], offenders,
                         f"CLI Source 가 Target/Group 이름으로 분기한다: {offenders}")

    def test_cli_source_never_carries_a_target_name_list(self) -> None:
        """대상 목록을 CLI 가 복제하면 canonical authority 가 둘이 된다."""
        names = ({row["group"] for row in group_rows()}
                 | {row["target"] for row in static_targets()}
                 | set(domain_names()))
        offenders: list[str] = []
        for relative in CLI_SOURCES:
            code = selector_surface(REPO_ROOT / relative)
            for literal in re.findall(r"(?:List|Set)\.of\(([^;]{0,400}?)\)", code, re.S):
                hits = [name for name in names if f'"{name}"' in literal]
                if len(hits) >= 2:
                    offenders.append(f"{relative}:{sorted(hits)}")
        self.assertEqual([], offenders,
                         f"CLI Source 가 Target/Group 목록을 들고 있다: {offenders}")

    def test_domain_module_capabilities_come_from_the_catalog(self) -> None:
        text = read(TARGETS_SOURCE)
        self.assertIn("capabilityByModule", text,
                      "Domain module 목록을 catalog 에서 읽지 않는다")


def source_code(path: Path) -> str:
    """주석을 뺀 실행 Source. 근거를 적은 주석은 계약 위반이 아니다."""
    return "\n".join(line for line in read(path).splitlines()
                     if not line.strip().startswith(("*", "//", "/*")))


def selector_surface(path: Path) -> str:
    """공개 Runtime selector 를 다루는 구간만 본다.

    CpfCli 에는 내부 namespace(`cpf verify all` 등) 도 있고 거기서 쓰는 `all` 은 Runtime Group 이
    아니다. 그 구간까지 같은 규칙으로 보면 서로 다른 의미를 하나로 취급하게 된다.
    """
    code = source_code(path)
    if path.name != "CpfCli.java":
        return code
    start = code.find("private static int runtimeLifecycle(")
    end = code.find("private static int help()")
    if start < 0 or end < 0 or end <= start:
        raise AssertionError("공개 Runtime selector 구간을 찾지 못했다: " + path.name)
    return code[start:end]


def domain_names() -> list[str]:
    """Workspace 에 실재하는 Generated Domain 이름. 목록을 test 가 들고 있지 않는다."""
    names = []
    for properties in REPO_ROOT.glob("cpf-*/gradle.properties"):
        text = properties.read_text(encoding="utf-8")
        if "cpf.domain.contractVersion=1" not in text.replace(" ", ""):
            continue
        match = re.search(r"^cpf\.domain\.name\s*=\s*(\S+)", text, re.M)
        if match:
            names.append(match.group(1))
    return names


class LifecycleCliSurface(unittest.TestCase):
    """사용자 Golden Path 는 `cpf` 동사 하나다."""

    def test_every_lifecycle_verb_is_a_public_command(self) -> None:
        text = read(CLI_SOURCE)
        block = text.split("Set<String> PUBLIC = Set.of(", 1)
        self.assertEqual(2, len(block), "공개 명령 집합을 찾지 못했다")
        public = block[1].split(");", 1)[0]
        missing = [verb for verb in LIFECYCLE_VERBS if f'"{verb}"' not in public]
        self.assertEqual([], missing, f"공개 명령이 아닌 Lifecycle 동사: {missing}")
        self.assertIn('"targets"', public, "실행 가능한 대상을 보여주는 명령이 없다")

    def test_selector_is_accepted_as_a_positional_argument(self) -> None:
        text = read(CLI_SOURCE)
        self.assertIn("target == null && !token.startsWith(\"-\")", text,
                      "cpf start <target> 형태의 위치 인자를 받지 않는다")

    def test_unknown_target_is_answered_with_choices(self) -> None:
        text = read(BOOTSTRAP_SOURCE)
        self.assertIn("UNKNOWN TARGET: ", text, "잘못된 Target 안내가 없다")
        self.assertIn("Available groups:", text, "선택 가능한 Group 을 안내하지 않는다")
        self.assertIn("Available runtimes:", text, "선택 가능한 Runtime 을 안내하지 않는다")

    def test_group_result_is_aggregated_without_hiding_failure(self) -> None:
        text = read(BOOTSTRAP_SOURCE)
        self.assertIn("OVERALL", text, "Group 결과 집계가 없다")
        self.assertIn('"UNSUPPORTED"', text,
                      "의미 없는 Capability 를 UNSUPPORTED 로 알리지 않는다")
        self.assertIn("if (outcome.equals(\"FAIL\")) failed = true;", text,
                      "일부 실패가 전체 결과에 반영되지 않는다")

    def test_stop_reverses_the_start_order(self) -> None:
        text = read(BOOTSTRAP_SOURCE)
        self.assertIn('if (action.equals("stop")) java.util.Collections.reverse(ordered);', text,
                      "정지가 기동 순서의 역순이 아니다")

    def test_status_does_not_report_healthy_from_pid_alone(self) -> None:
        text = read(BOOTSTRAP_SOURCE)
        body = text.split("private int runtimeGroupStatus(", 1)
        self.assertEqual(2, len(body), "Group 상태 집계 진입점을 찾지 못했다")
        block = body[1].split("\n    }", 1)[0]
        self.assertIn("probeHealth(", block, "pid 존재만으로 상태를 판정한다")
        self.assertIn("DEGRADED", block, "부분 장애 상태를 표현하지 않는다")

    def test_dependency_cycle_fails_closed(self) -> None:
        self.assertIn("runtime dependency cycle", read(TARGETS_SOURCE),
                      "dependency cycle 을 fail-closed 로 처리하지 않는다")


class LauncherParity(unittest.TestCase):
    """Windows 와 Linux 가 같은 명령, 같은 의미를 쓴다."""

    @staticmethod
    def _delegated_verb(text: str) -> str:
        """wrapper 가 canonical CLI 에 넘기는 동사. sh 와 ps1 의 호출 형태가 다르므로 둘 다 본다."""
        for pattern in (r"\$ROOT/cpf\"\s+([a-z][a-z-]*)", r"'cpf\.ps1'\)\s+([a-z][a-z-]*)"):
            match = re.search(pattern, text)
            if match:
                return match.group(1)
        return ""

    def test_every_wrapper_pair_delegates_to_the_same_verb(self) -> None:
        mismatched: list[str] = []
        checked = 0
        for shell in sorted(LAUNCHER_DIR.glob("cpf-*.sh")):
            powershell = shell.with_suffix(".ps1")
            self.assertTrue(powershell.is_file(), f"짝이 없는 wrapper: {shell.name}")
            checked += 1
            left = self._delegated_verb(read(shell))
            right = self._delegated_verb(read(powershell))
            if left != right or not left:
                mismatched.append(f"{shell.stem}: sh={left} ps1={right}")
        self.assertTrue(checked, "OS wrapper 를 하나도 찾지 못했다")
        self.assertEqual([], mismatched, f"Windows/Linux wrapper 의미가 다르다: {mismatched}")

    def test_wrappers_never_reimplement_command_parsing(self) -> None:
        """자체 명령 해석을 가진 wrapper 는 canonical CLI 와 반드시 어긋난다."""
        offenders = []
        for script in sorted(LAUNCHER_DIR.glob("cpf*.sh")):
            text = read(script)
            if "case " in text and "esac" in text:
                offenders.append(script.name)
        self.assertEqual([], offenders,
                         f"wrapper 가 자체 명령 목록을 갖고 있다: {offenders}")

    def test_wrapper_verbs_are_real_cli_commands(self) -> None:
        public = read(CLI_SOURCE).split("Set<String> PUBLIC = Set.of(", 1)[1].split(");", 1)[0]
        unknown = []
        for script in sorted(LAUNCHER_DIR.glob("cpf-*.sh")):
            verb = self._delegated_verb(read(script))
            if verb and f'"{verb}"' not in public:
                unknown.append(f"{script.name}->{verb}")
        self.assertEqual([], unknown, f"CLI 가 모르는 명령으로 위임하는 wrapper: {unknown}")


class DocumentationParity(unittest.TestCase):
    """README 에 적힌 명령이 실제 CLI 에서 동작한다."""

    @staticmethod
    def _readme_commands() -> list[tuple[str, str]]:
        """복사해 실행하는 명령만 본다. 산문 속 설명 문장은 실행 대상이 아니다."""
        text = read(README)
        found = []
        for block in re.findall(r"```[a-z]*\n(.*?)```", text, re.S):
            for match in re.finditer(r"cpf(?:\.cmd)?\s+([a-z][a-z-]*)\s*([a-z][a-z0-9-]*)?", block):
                found.append((match.group(1), match.group(2) or ""))
        return found

    def test_readme_uses_only_real_commands(self) -> None:
        public = read(CLI_SOURCE).split("Set<String> PUBLIC = Set.of(", 1)[1].split(");", 1)[0]
        unknown = sorted({verb for verb, _ in self._readme_commands() if f'"{verb}"' not in public})
        self.assertEqual([], unknown, f"README 가 CLI 에 없는 명령을 안내한다: {unknown}")

    def test_readme_uses_only_real_groups(self) -> None:
        groups = {row["group"] for row in group_rows()}
        targets = {row["target"] for row in static_targets()}
        unknown = []
        for verb, selector in self._readme_commands():
            if verb not in LIFECYCLE_VERBS or not selector:
                continue
            if selector in groups or selector in targets:
                continue
            if selector.startswith("<") or "domain" in selector:
                continue
            unknown.append(f"{verb} {selector}")
        self.assertEqual([], unknown, f"README 가 존재하지 않는 대상을 안내한다: {unknown}")

    def test_readme_leads_with_the_canonical_entrypoint(self) -> None:
        text = read(README)
        section = text.split("## Runtime 실행", 1)
        self.assertEqual(2, len(section), "README 에 Runtime 실행 안내가 없다")
        body = section[1].split("##", 1)[0]
        self.assertIn("cpf targets", body, "무엇을 실행할 수 있는지 안내하지 않는다")
        for group in ("all", "platform", "domains", "batch", "backoffice-stack"):
            self.assertIn(group, body, f"README 가 {group} Group 을 안내하지 않는다")

    def test_cli_help_shows_the_three_control_levels(self) -> None:
        text = read(CLI_SOURCE)
        block = text.split("private static int help()", 1)
        self.assertEqual(2, len(block), "help 진입점을 찾지 못했다")
        body = block[1].split("\n    }", 1)[0]
        self.assertIn("cpf targets", body, "help 가 실행 가능한 대상 확인 방법을 알려주지 않는다")
        for verb in LIFECYCLE_VERBS:
            self.assertIn(verb, body, f"help 에 Lifecycle 동사가 없다: {verb}")
        self.assertIn("CpfRuntimeTargets.groups(", body,
                      "help 가 Group 예제를 catalog 에서 만들지 않는다")

    def test_lifecycle_help_lists_selectable_names_from_the_catalog(self) -> None:
        body = read(CLI_SOURCE).split("private static int lifecycleHelp(", 1)[1].split("\n    }", 1)[0]
        self.assertIn("CpfRuntimeTargets.groups(", body, "동사별 help 가 Group 을 파생하지 않는다")
        self.assertIn("CpfRuntimeTargets.resolveAll(", body, "동사별 help 가 Runtime 을 파생하지 않는다")


PROBE_SOURCE = """
import java.nio.file.*;
import java.util.*;

public class CpfTargetProbe {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        StringBuilder out = new StringBuilder();
        for (CpfRuntimeTargets.Target target : CpfRuntimeTargets.resolveAll(root)) {
            out.append("TARGET ").append(target.name()).append(System.lineSeparator());
        }
        for (int index = 1; index < args.length; index++) {
            Optional<List<CpfRuntimeTargets.Target>> selected = CpfRuntimeTargets.select(root, args[index]);
            out.append("SELECT ").append(args[index]).append(" ");
            if (selected.isEmpty()) {
                out.append("UNKNOWN");
            } else {
                out.append(selected.get().stream().map(CpfRuntimeTargets.Target::name)
                        .reduce((a, b) -> a + "," + b).orElse(""));
            }
            out.append(System.lineSeparator());
        }
        System.out.print(out);
    }
}
"""


class DynamicDiscoveryRuntime(unittest.TestCase):
    """CLI Source 를 고치지 않고 신규 Domain/Runtime 이 대상에 들어오는지 실제로 돌려서 본다.

    정적 규칙 확인만으로는 "규칙은 있는데 동작은 안 되는" 상태를 잡지 못한다.
    """

    classes: Path
    workspace: Path

    @classmethod
    def setUpClass(cls) -> None:
        cls._temp = tempfile.mkdtemp(prefix="cpf-runtime-cli-probe-")
        base = Path(cls._temp)
        cls.classes = base / "classes"
        cls.classes.mkdir()
        probe = base / "CpfTargetProbe.java"
        probe.write_text(PROBE_SOURCE, encoding="utf-8")
        javac = shutil.which("javac")
        if not javac:
            raise AssertionError("javac 를 찾지 못했다. Runtime 계약은 실제 실행으로만 확인한다")
        compiled = subprocess.run([javac, "--release", "25", "-d", str(cls.classes),
                                   str(TARGETS_SOURCE), str(probe)],
                                  capture_output=True, text=True)
        if compiled.returncode != 0:
            raise AssertionError("Target resolver 컴파일 실패: " + compiled.stderr)

        # 정본 catalog 를 그대로 쓰고, 신규 Runtime 만 추가한 Workspace 를 만든다.
        cls.workspace = base / "workspace"
        (cls.workspace / "config").mkdir(parents=True)
        (cls.workspace / "settings.gradle").write_text("", encoding="utf-8")
        model = catalog()
        template = dict(model["runtimes"][0])
        template.update({"target": "batch-probe", "owner": "cpf-batch/probe", "capability": "worker",
                         "port": 0, "portEnv": "", "provision": "source",
                         "description": "probe batch runtime", "artifactId": "",
                         "healthPath": "", "architectureRole": "BATCH_RUNTIME",
                         "runtimeGroups": [], "dependsOn": [], "publicLifecycle": True,
                         "buildSurface": "private-binary"})
        model["runtimes"].append(template)
        (cls.workspace / "config/cpf-runtime-target-catalog.json").write_text(
            json.dumps(model, ensure_ascii=False, indent=2), encoding="utf-8")
        (cls.workspace / "cpf-batch/probe").mkdir(parents=True)
        # source provision Runtime 은 owner 디렉터리 존재로 해석된다. 정본 catalog 의 source Target 을
        # 그대로 재현해야 기동 순서 계약까지 확인할 수 있다.
        for entry in model["runtimes"]:
            if entry["provision"] == "source":
                (cls.workspace / entry["owner"]).mkdir(parents=True, exist_ok=True)

        # 신규 Generated Domain. 이름은 어떤 Source 에도 등록하지 않는다.
        domain = cls.workspace / "cpf-zzprobe"
        (domain / "online").mkdir(parents=True)
        (domain / "batch").mkdir(parents=True)
        (domain / "gradle.properties").write_text(
            "cpf.domain.contractVersion=1\ncpf.domain.name=zzprobe\ncpf.domain.systemCode=ZZP\n",
            encoding="utf-8")
        (domain / "settings.gradle").write_text("include 'online'\ninclude 'batch'\n", encoding="utf-8")

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree(cls._temp, ignore_errors=True)

    def _probe(self, *selectors: str) -> dict[str, str]:
        java = shutil.which("java")
        self.assertIsNotNone(java, "java 를 찾지 못했다")
        result = subprocess.run([java, "-Dfile.encoding=UTF-8", "-cp", str(self.classes),
                                 "CpfTargetProbe", str(self.workspace), *selectors],
                                capture_output=True, text=True, encoding="utf-8")
        self.assertEqual(0, result.returncode, f"probe 실패: {result.stderr}")
        parsed: dict[str, str] = {"__targets__": ""}
        targets = []
        for line in result.stdout.splitlines():
            if line.startswith("TARGET "):
                targets.append(line[len("TARGET "):].strip())
            elif line.startswith("SELECT "):
                _, name, value = line.split(" ", 2)
                parsed[name] = value.strip()
        parsed["__targets__"] = ",".join(targets)
        return parsed

    def test_new_generated_domain_appears_without_touching_any_source(self) -> None:
        parsed = self._probe("domains", "all")
        self.assertIn("zzprobe", parsed["__targets__"].split(","),
                      "신규 Generated Domain 이 Target 목록에 없다")
        self.assertIn("zzprobe", parsed["domains"].split(","),
                      "신규 Generated Domain 이 domains Group 에 없다")
        self.assertIn("zzprobe", parsed["all"].split(","),
                      "신규 Generated Domain 이 전체 대상에 없다")

    def test_new_generated_domain_is_individually_selectable(self) -> None:
        parsed = self._probe("zzprobe")
        self.assertEqual("zzprobe", parsed["zzprobe"],
                         "신규 Generated Domain 을 개별 Target 으로 고를 수 없다")

    def test_new_batch_runtime_joins_the_batch_group(self) -> None:
        parsed = self._probe("batch", "all")
        self.assertIn("batch-probe", parsed["batch"].split(","),
                      "신규 Batch Runtime 이 batch Group 에 자동 포함되지 않는다")
        self.assertIn("batch-probe", parsed["all"].split(","),
                      "신규 Batch Runtime 이 전체 대상에 없다")

    def test_all_covers_every_resolved_target(self) -> None:
        parsed = self._probe("all")
        resolved = set(parsed["__targets__"].split(","))
        selected = set(parsed["all"].split(","))
        self.assertEqual(resolved, selected,
                         f"all 이 일부 Runtime 을 빠뜨린다: {sorted(resolved - selected)}")

    def test_unknown_selector_is_reported_as_unknown(self) -> None:
        parsed = self._probe("no-such-thing")
        self.assertEqual("UNKNOWN", parsed["no-such-thing"],
                         "존재하지 않는 대상을 조용히 받아들인다")

    def test_channel_front_is_ordered_after_its_business_runtime(self) -> None:
        parsed = self._probe("backoffice-stack")
        order = parsed["backoffice-stack"].split(",")
        self.assertIn("backoffice", order, "backoffice-stack 에 업무 Runtime 이 없다")
        self.assertIn("backoffice-web", order, "backoffice-stack 에 Channel Front 가 없다")
        self.assertLess(order.index("backoffice"), order.index("backoffice-web"),
                        "Channel Front 가 업무 Runtime 보다 먼저 기동된다")


class InternalLifecycleCliRuntime(unittest.TestCase):
    """Development Master도 Public과 같은 Java Lifecycle engine을 실제로 실행한다."""

    def test_targets_and_group_status_do_not_fall_back_to_legacy_python_engine(self) -> None:
        javac = shutil.which("javac")
        java = shutil.which("java")
        jar = shutil.which("jar")
        self.assertTrue(javac and java and jar, "Java 25 toolchain이 필요하다")
        with tempfile.TemporaryDirectory(prefix="cpf-runtime-lifecycle-cli-") as raw:
            temp = Path(raw)
            workspace = temp / "workspace"
            (workspace / "cpf-tools/runtime").mkdir(parents=True)
            (workspace / "settings.gradle").write_text("", encoding="utf-8")
            shutil.copy2(CATALOG, workspace / "cpf-tools/runtime/cpf-runtime-target-catalog.json")
            classes = temp / "classes"
            classes.mkdir()
            sources = (CLI_SOURCE, BOOTSTRAP_SOURCE, TARGETS_SOURCE,
                       REPO_ROOT / "cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java")
            compiled = subprocess.run(
                [javac, "--release", "25", "-encoding", "UTF-8", "-Xlint:all", "-Werror",
                 "-d", str(classes), *(str(source) for source in sources)],
                capture_output=True, text=True, encoding="utf-8")
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            artifact = temp / "cpf-cli.jar"
            packed = subprocess.run(
                [jar, "--create", "--file", str(artifact), "--main-class", "CpfCli", "-C", str(classes), "."],
                capture_output=True, text=True, encoding="utf-8")
            self.assertEqual(0, packed.returncode, packed.stderr)
            environment = dict(os.environ, JAVA_TOOL_OPTIONS="")
            java_utf8 = [java, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8",
                         "-Dstderr.encoding=UTF-8", "-jar", str(artifact)]
            targets = subprocess.run([*java_utf8, "targets"], cwd=workspace,
                                     capture_output=True, text=True, encoding="utf-8", env=environment)
            self.assertEqual(0, targets.returncode, targets.stdout + targets.stderr)
            self.assertIn("GROUPS", targets.stdout)
            self.assertIn("platform", targets.stdout)
            self.assertNotIn("cpf_local_runtime.py", targets.stdout + targets.stderr)
            status = subprocess.run([*java_utf8, "status", "platform"], cwd=workspace,
                                    capture_output=True, text=True, encoding="utf-8", env=environment)
            self.assertEqual(0, status.returncode, status.stdout + status.stderr)
            self.assertIn("OVERALL", status.stdout)


class PublicBuildSurfaceBoundary(unittest.TestCase):
    """Binary-only Runtime 은 실행만 공개하고 Source Build/Test 는 공개하지 않는다."""

    def test_public_build_all_never_reaches_a_binary_only_runtime(self) -> None:
        template = read(REPO_ROOT / "cpf-tools/release/open-git/templates/build.gradle")
        binary_owners = [row["owner"] for row in static_targets()
                         if row["buildSurface"] == "private-binary"]
        self.assertTrue(binary_owners, "Binary-only Runtime 을 하나도 찾지 못했다")
        leaked = [owner for owner in binary_owners if owner in template]
        self.assertEqual([], leaked,
                         f"공개 Build graph 가 Binary-only Runtime 을 끌어온다: {leaked}")

    def test_binary_only_runtime_still_has_a_public_lifecycle(self) -> None:
        """실행은 공개다. Build 를 막았다고 운영까지 막으면 안 된다."""
        blocked = [row["target"] for row in static_targets()
                   if row["buildSurface"] == "private-binary" and not row["publicLifecycle"]]
        self.assertEqual([], blocked,
                         f"Binary-only Runtime 인데 Lifecycle 조차 공개되지 않는다: {blocked}")


class HarnessAndRegistryRelation(unittest.TestCase):
    """Steering 은 Source 뿐 아니라 정본 Rule 과 Registry 까지 닫혀야 한다."""

    def test_harness_declares_the_lifecycle_cli_rule(self) -> None:
        text = read(HARNESS)
        self.assertIn("Runtime Lifecycle CLI", text,
                      "Current Harness 에 Runtime Lifecycle CLI 계약이 없다")
        self.assertIn("canonical machine-readable authority", text,
                      "Group 파생 정본 규칙이 Harness 에 없다")

    def test_registry_links_the_rule_to_this_validator(self) -> None:
        self.assertTrue(WORK_ITEM_REGISTRY.is_file(), "Work Item Registry 가 없다")
        text = read(WORK_ITEM_REGISTRY)
        self.assertIn("test_cpf_runtime_lifecycle_cli_contract", text,
                      "Registry 가 이 계약 Validator 를 참조하지 않는다")


if __name__ == "__main__":
    unittest.main(verbosity=2)
