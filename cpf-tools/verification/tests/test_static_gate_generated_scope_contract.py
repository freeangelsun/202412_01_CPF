"""제품 Source 정적 Gate의 스캔 범위 계약.

정본 Source Identity(`cpf-tools/verification/tools/cpf-source-state.py`)는 `cpf-release`를
`GENERATED_PARTS`로, `logs/`를 `GENERATED_ROOT_PREFIXES`로 두어 제품 Source에서 제외한다.
정적 Gate가 이 생성/일시 산출물까지 제품 Source로 스캔하면, 릴리즈를 한 번 생성했는지 또는
Runtime을 한 번 실행했는지에 따라 **같은 Source가 PASS/FAIL로 갈리는 비결정 Gate**가 된다.

실제로 다음이 그 상태였고 Full Runtime을 통과 불가능하게 만들었다.
- query-db3: `cpf-release/open-git/deploy/local/compose.yaml`의 MariaDB 내부 경로
  `/var/lib/mysql`을 UNSUPPORTED_VENDOR로 오탐(정본 템플릿은 이미 allowlist에 있었다).
- no-partial: Open Git이 의도적으로 projection한 `cpf-education` 사본을 DUPLICATE_FQCN으로 오탐.
- root IA: `cpf-release`/`logs`를 unexpected root entry로 판정.
- clean-source: Open Git 산출물의 `.git` 내부 빈 디렉터리를 garbage로 판정.

또한 `build/`는 Gradle 출력이지 source가 아니다. source-empty project의 canonical compile
output(`build/classes/java/main`)은 IDE classpath 계약이 요구하는 정본 출력 위치이므로,
clean-source가 여기까지 검사하면 Gradle을 한 번 실행하는 것만으로 항상 실패한다.
단 `cpf-tools/build`는 Gradle plugin/BOM 제품 Source라 검사 대상으로 남는다.
"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SOURCE_STATE = ROOT / "cpf-tools/verification/tools/cpf-source-state.py"
QUERY_DB3 = ROOT / "cpf-tools/verification/nxt3/verify_nxt3_query_db3.py"
NO_PARTIAL = ROOT / "cpf-tools/verification/verify_no_partial_implementation.py"
ROOT_IA = ROOT / "cpf-tools/verification/nxt3/verify_root_generated_domain_prefix.py"
CLEAN_SOURCE = ROOT / "cpf-tools/verification/tools/verify-cpf-clean-source-tree.py"
GENERATED_JAVAC = ROOT / "cpf-tools/verification/nxt3/verify_generated_javac.py"
STACK = ROOT / "gradle/cpf-stack.properties"


def _text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def test_canonical_source_identity_still_treats_release_and_logs_as_generated():
    """Gate 쪽 제외의 근거가 되는 정본 분류가 사라지면 이 계약 자체가 무효가 된다."""
    text = _text(SOURCE_STATE)
    assert "GENERATED_PARTS" in text
    assert '"cpf-release"' in text
    assert '"logs/"' in text


def test_product_source_gates_exclude_generated_release_artifacts():
    assert '"cpf-release/"' in _text(QUERY_DB3)
    assert "'cpf-release/'" in _text(NO_PARTIAL)
    assert "'cpf-release'" in _text(ROOT_IA)
    assert "'cpf-release'" in _text(CLEAN_SOURCE)


def test_root_ia_gate_allows_generated_and_ephemeral_root_entries():
    text = _text(ROOT_IA)
    # 허용 집합에 실제로 반영되어야 하며, 상수만 두고 쓰지 않으면 다시 실패한다.
    assert "EPHEMERAL_DIRS" in text
    assert "|EPHEMERAL_DIRS" in text or "| EPHEMERAL_DIRS" in text
    for entry in ("'cpf-release'", "'logs'"):
        assert entry in text


def test_clean_source_gate_does_not_scan_gradle_output_but_keeps_tools_build():
    text = _text(CLEAN_SOURCE)
    # cpf-tools/build/gradle-plugin/build/** 처럼 build 가 중첩되므로 첫 세그먼트만 보면 안 된다.
    assert "for i,part in enumerate(rel.parts)" in text
    assert "('cpf-tools','build')" in text
    assert "GENERATED_EVIDENCE_PREFIX" in text


def test_gates_decode_subprocess_output_as_utf8():
    """encoding 미지정이면 Windows 기본(cp949)으로 디코드하다 한글 진단에서 stdout 이 None 이 되고,
    json.loads(None) 같은 무관한 TypeError 로 Gate 가 실패한다."""
    presets = ROOT / "cpf-tools/verification/nxt3/verify_generator_presets.py"
    for path in (presets, GENERATED_JAVAC):
        text = _text(path)
        assert "capture_output=True" in text
        assert "encoding='utf-8'" in text, path.relative_to(ROOT).as_posix()


def test_generated_javac_reads_java_release_from_canonical_stack():
    """release 를 Gate 에 박아두면 Stack Java 를 올릴 때 preview/release 조합이 깨진다."""
    text = _text(GENERATED_JAVAC)
    assert "canonical_java_release" in text
    assert "gradle/cpf-stack.properties" in text
    assert "'--release',str(java_release)" in text
    assert "'--release','21'" not in text
    declared = [
        line.split("=", 1)[1].strip()
        for line in _text(STACK).splitlines()
        if line.strip().startswith("javaVersion") and "=" in line
    ]
    assert declared, "gradle/cpf-stack.properties 에 javaVersion 정본이 없다"
    assert declared[0].isdigit()


def test_product_gates_do_not_let_child_python_write_bytecode():
    """`python -B` 는 자식 프로세스에 전파되지 않는다.

    Gate 가 CPF Python 도구를 subprocess 로 실행하면서 -B 를 넘기지 않으면 자식이
    `__pycache__` 를 남기고, Harness Gate 의 REPOSITORY_PYTHON_CACHE 가 fail-closed 로
    잡는다. 실제로 verify_generator_presets 가 cpf.py 를 그렇게 실행해 재발했다.
    """
    targets = [
        "cpf-tools/verification/nxt3/verify_generator_presets.py",
        "cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py",
        "cpf-tools/db/verify_canonical_vendor_render.py",
        "cpf-tools/generator/verification/verify-cpf-generator-java-template-compile.py",
        "cpf-tools/release/open-git/cpf_open_git.py",
        "cpf-tools/release/public/publish-cpf-public-repository.py",
    ]
    offenders = []
    for rel in targets:
        for line in _text(ROOT / rel).splitlines():
            stripped = line.strip()
            # 인터프리터로 CPF Python script 를 실행하는 argv 목록만 대상이다.
            interpreter = "sys.executable," in stripped or "[python," in stripped
            if not interpreter or "cpf-tools/" not in stripped:
                continue
            if "-m" in stripped or "'-B'" in stripped or '"-B"' in stripped:
                continue
            offenders.append(f"{rel}: {stripped[:120]}")
    assert not offenders, "자식 Python 에 -B 미전달: " + "; ".join(offenders)
