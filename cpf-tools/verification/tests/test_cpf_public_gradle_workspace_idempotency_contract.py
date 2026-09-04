"""공개 Workspace 의 Gradle 명령이 반복 실행에도 같은 결과를 내도록 고정한다.

증상 근거: 공개 배포본에서 `cpf bootstrap`(내부적으로 `gradlew cpfVerify`)을 연속으로 실행하면
두 번째가 반드시 실패했다. 실패 모습은 `:cpf-education:compileJava UP-TO-DATE` 인데
`build/classes/java/main` 이 사라진 상태여서 `package ... does not exist` 컴파일 오류였고,
회차마다 희생되는 Included Build 가 바뀌었다.

원인: 공개 launcher 가 `--project-cache-dir` 로 Composite 전체에 하나의 Gradle project cache 를
강제했다. Included Build 들이 실행 이력과 stale-output registry 를 공유하게 되고, Gradle 의
OutputsCleaner 가 다른 Build 의 `build/classes` 를 등록되지 않은 산출물로 보고 지웠다.
`--debug` 로그의 "Deleting stale output file" 이 직접 증거다.

되돌리면 재발할 증상: 공개 사용자가 같은 명령을 두 번 실행하는 것만으로 자기 코드와 무관한
컴파일 오류를 만난다. 한 번만 실행해 보는 검증으로는 절대 보이지 않는다.
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

REPO_ROOT = Path(os.environ.get("CPF_PUBLIC_WORKSPACE_ROOT") or Path(__file__).resolve().parents[3])

OPEN_GIT_SURFACE_POLICY = REPO_ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"

# Composite 전체에 하나의 project cache 를 강제하는 형태.
SHARED_PROJECT_CACHE_OPTION = "--project-cache-dir"
# 설정에서 같은 강제를 다시 거는 형태.
SETTINGS_PROJECT_CACHE_ASSIGNMENT = "gradle.startParameter.projectCacheDir"

# 공개 배포본이 사용자에게 제공하는 Gradle 진입점.
LAUNCHER_TARGETS = {"gradlew", "gradlew.bat"}
ROOT_SETTINGS_TARGET = "settings.gradle"


def template_rules() -> list[dict]:
    policy = json.loads(OPEN_GIT_SURFACE_POLICY.read_text(encoding="utf-8"))
    return policy.get("templateRules", [])


def executable_lines(text: str) -> list[str]:
    """주석은 계약 위반이 아니다. 원인 기록을 남길 수 있어야 한다."""
    lines = []
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("#") or line.startswith("@rem") or line.startswith("rem ") or line.startswith("//"):
            continue
        lines.append(line)
    return lines


class PublicGradleWorkspaceIdempotencyContract(unittest.TestCase):

    def setUp(self) -> None:
        self.rules = template_rules()
        self.assertTrue(self.rules, "공개 배포본 template 규칙을 읽지 못했다")

    def test_public_launcher_does_not_force_one_project_cache(self) -> None:
        checked = 0
        offenders: list[str] = []
        for rule in self.rules:
            if str(rule.get("target", "")) not in LAUNCHER_TARGETS:
                continue
            source = REPO_ROOT / str(rule["source"])
            self.assertTrue(source.is_file(), f"공개 launcher template 이 없다: {rule['source']}")
            checked += 1
            text = source.read_bytes().decode("utf-8", errors="surrogateescape")
            if any(SHARED_PROJECT_CACHE_OPTION in line for line in executable_lines(text)):
                offenders.append(str(rule["source"]))
        self.assertEqual(len(LAUNCHER_TARGETS), checked,
                         "공개 launcher template 을 모두 찾지 못했다")
        self.assertEqual([], offenders,
                         f"공개 launcher 가 Composite 전체에 project cache 를 강제한다: {offenders}")

    def test_public_root_settings_does_not_override_project_cache(self) -> None:
        checked = 0
        offenders: list[str] = []
        for rule in self.rules:
            if str(rule.get("target", "")) != ROOT_SETTINGS_TARGET:
                continue
            source = REPO_ROOT / str(rule["source"])
            self.assertTrue(source.is_file(), f"공개 settings template 이 없다: {rule['source']}")
            checked += 1
            text = source.read_text(encoding="utf-8")
            if any(SETTINGS_PROJECT_CACHE_ASSIGNMENT in line for line in executable_lines(text)):
                offenders.append(str(rule["source"]))
        self.assertTrue(checked, "공개 root settings template 을 찾지 못했다")
        self.assertEqual([], offenders,
                         f"공개 root settings 가 project cache 를 다시 덮어쓴다: {offenders}")


if __name__ == "__main__":
    unittest.main(verbosity=2)
