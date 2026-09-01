"""VS Code(JDT) Problems 0 을 Source 기준으로 검증한다.

실측 결함: `web-api` 처럼 source 가 없는 dependency-assembly project 는 bytecode 를 만들지
않는데, Gradle compile-avoidance 는 java-library consumer 에게 jar 대신 compile output
디렉터리를 넘긴다. JDT 는 그 class folder 가 실재해야 하므로, 폴더가 없으면

    Project 'browser-bff' is missing required library: .../web-api/build/classes/java/main
    The project cannot be built until build path errors are resolved

가 Problems 에 뜬다. 사용자 UI 를 열지 않고도 이 조건을 Source/산출물로 검증한다.
"""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTIONS = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
SKIP_PARTS = {"build", "bin", ".git", "node_modules", "cpf-release", "__pycache__"}


def test_source_empty_projects_get_canonical_compile_output():
    """source-empty project 의 compile output 을 materialize 하는 계약이 살아 있어야 한다."""
    text = CONVENTIONS.read_text(encoding="utf-8")
    assert "sourceSets.main.java.files.empty" in text, (
        "source-empty project 의 canonical compile output materialization 이 사라졌다")
    assert "destinationDirectory" in text
    assert "CPF source-empty canonical compile output could not be created" in text


def test_ide_classpath_recovery_task_exists():
    """clean 이후 JDT classpath 를 실제 compile 로 복구하는 canonical task 가 있어야 한다."""
    text = CONVENTIONS.read_text(encoding="utf-8")
    assert "tasks.register('cpfPrepareIdeClasspath')" in text
    assert "CPF_IDE_CLASSPATH_READY" in text


def test_every_java_project_declares_a_compile_output_location():
    """Java plugin 을 쓰는 project 는 JDT 가 요구하는 output location 을 가져야 한다."""
    offenders = []
    for build_file in sorted(ROOT.rglob("build.gradle")):
        relative = build_file.relative_to(ROOT)
        if SKIP_PARTS.intersection(relative.parts):
            continue
        text = build_file.read_text(encoding="utf-8", errors="replace")
        if not re.search(r"id\s+'java(-library)?'", text):
            continue
        # 산출 위치를 임의로 바꾸면 Buildship/JDT 가 기대하는 canonical 경로와 어긋난다.
        if "destinationDirectory" in text and "classes/java/main" not in text:
            offenders.append(relative.as_posix())
    assert not offenders, f"non-canonical java output location: {offenders}"
