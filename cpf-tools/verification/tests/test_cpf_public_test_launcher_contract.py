"""공개 Source Development Surface 의 Test 실행 가능성을 정본 계약으로 고정한다.

증상 근거: Fresh Open Git Consumer 가 공개 Golden Path 의 `gradlew cpfVerify` 를 실행하면
`:cpf-education:test` 가 "Could not start Gradle Test Executor 1: Failed to load JUnit Platform"
으로 죽었다. Test 가 실패한 것이 아니라 Test Executor 자체가 기동하지 못한 것이다.

원인: Gradle 9 / JUnit Platform 6 는 Test Executor 에 junit-platform-launcher 를 자동 주입하지
않는다. 생성 Domain(online/batch)은 Generator 가 launcher 를 명시 선언했지만, 손으로 작성한
EDU 표본과 Backoffice Web 은 선언이 없었다. Backoffice Web 은 spring-boot-starter-test 의
전이 의존으로 우연히 살아 있었고, spring-boot-starter-webmvc-test 를 쓰는 EDU 만 죽었다.
즉 "전이로 들어오면 통과, 아니면 실패" 라는 우연에 공개 Golden Path 가 걸려 있었다.

되돌리면 재발할 증상: 공개 사용자가 Fresh Clone 후 첫 `cpfVerify` 에서 자기 코드와 무관한
Test Executor 기동 실패를 만난다. 전이 의존 하나만 바뀌어도 조용히 재발하므로 명시 선언을
계약으로 고정한다.
"""

from __future__ import annotations

import json
import os
import sys
import unittest
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

# Negative Mutation 은 실제 정본을 건드리지 않고 격리 복사본에 대해 돌린다.
REPO_ROOT = Path(os.environ.get("CPF_PUBLIC_SURFACE_ROOT") or Path(__file__).resolve().parents[3])

PRODUCT_SURFACE_POLICY = REPO_ROOT / "cpf-tools/governance/cpf-product-surface-policy.json"
OPEN_GIT_SURFACE_POLICY = REPO_ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"
GENERATOR_ENGINE = REPO_ROOT / "cpf-tools/generator/engine/cpf_domain_generator.py"

PUBLIC_SOURCE_SURFACE = "PUBLIC_SOURCE_DEVELOPMENT"
LAUNCHER_COORDINATE = "org.junit.platform:junit-platform-launcher"
# 빌드 산출물과 의존 캐시는 Source 가 아니다.
IGNORED_DIRECTORIES = {"build", "node_modules", ".gradle"}


def load(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def declares_launcher(text: str) -> bool:
    """주석이 아닌 실제 test runtime 선언만 인정한다."""
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("//") or line.startswith("*"):
            continue
        if "testRuntimeOnly" in line and LAUNCHER_COORDINATE in line:
            return True
    return False


def public_source_prefixes() -> list[str]:
    owners = load(PRODUCT_SURFACE_POLICY)["moduleOwners"]
    return [
        str(entry["prefix"]).rstrip("/")
        for entry in owners
        if entry.get("publicDistributionSurface") == PUBLIC_SOURCE_SURFACE
    ]


def is_source_path(relative: Path) -> bool:
    return not any(part in IGNORED_DIRECTORIES for part in relative.parts)


def has_java_tests(project: Path) -> bool:
    test_root = project / "src/test/java"
    if not test_root.is_dir():
        return False
    return any(test_root.rglob("*.java"))


def template_overrides() -> dict[str, str]:
    """공개 배포본에서 dev build 파일을 대체하는 template 을 target 기준으로 돌려준다."""
    rules = load(OPEN_GIT_SURFACE_POLICY).get("templateRules", [])
    return {
        str(rule["target"]): str(rule["source"])
        for rule in rules
        if str(rule.get("target", "")).endswith("build.gradle")
    }


class PublicTestLauncherContract(unittest.TestCase):

    def setUp(self) -> None:
        self.prefixes = public_source_prefixes()
        # 정본에서 Public Source Development Surface 가 사라지면 이 검증은 공허해진다.
        self.assertTrue(self.prefixes, "Public Source Development Surface 선언이 정본에 없다")

    def public_source_build_files(self) -> list[Path]:
        found: list[Path] = []
        for prefix in self.prefixes:
            base = REPO_ROOT / prefix
            if not base.is_dir():
                continue
            for build_file in sorted(base.rglob("build.gradle")):
                if is_source_path(build_file.relative_to(REPO_ROOT)):
                    found.append(build_file)
        return found

    def test_public_source_projects_with_tests_declare_launcher(self) -> None:
        build_files = self.public_source_build_files()
        self.assertTrue(build_files, "Public Source Development Surface 에 build.gradle 이 하나도 없다")

        checked = 0
        missing: list[str] = []
        for build_file in build_files:
            if not has_java_tests(build_file.parent):
                continue
            checked += 1
            if not declares_launcher(build_file.read_text(encoding="utf-8")):
                missing.append(build_file.relative_to(REPO_ROOT).as_posix())

        # Test Source 를 가진 공개 Project 가 하나도 없다면 계약이 공허하게 통과한다.
        self.assertTrue(checked, "Test Source 를 가진 공개 Project 를 찾지 못했다")
        self.assertEqual([], missing, f"junit-platform-launcher 미선언 공개 Project: {missing}")

    def test_public_projection_templates_declare_launcher(self) -> None:
        """공개 배포본에 실제로 실리는 파일은 template 이다. dev 파일만 고치면 배포본은 그대로 죽는다."""
        overrides = template_overrides()
        checked = 0
        missing: list[str] = []
        for target, source in sorted(overrides.items()):
            project_relative = Path(target).parent
            if not any(
                project_relative == Path(prefix) or Path(prefix) in project_relative.parents
                for prefix in self.prefixes
            ):
                continue
            if not has_java_tests(REPO_ROOT / project_relative):
                continue
            template = REPO_ROOT / source
            self.assertTrue(template.is_file(), f"template 이 없다: {source}")
            checked += 1
            if not declares_launcher(template.read_text(encoding="utf-8")):
                missing.append(source)
        self.assertTrue(checked, "공개 Source Surface 를 대체하는 build template 을 찾지 못했다")
        self.assertEqual([], missing, f"junit-platform-launcher 미선언 공개 template: {missing}")

    def test_generator_emits_launcher_for_new_domains(self) -> None:
        """앞으로 생성될 Domain 도 자동으로 계약을 만족해야 한다. 목록 하드코딩 대신 Generator 를 본다."""
        self.assertTrue(GENERATOR_ENGINE.is_file(), "Domain Generator 엔진 정본이 없다")
        engine = GENERATOR_ENGINE.read_text(encoding="utf-8")
        self.assertIn(
            LAUNCHER_COORDINATE,
            engine,
            "Generator 가 신규 Domain build 에 junit-platform-launcher 를 선언하지 않는다",
        )


if __name__ == "__main__":
    unittest.main(verbosity=2)
