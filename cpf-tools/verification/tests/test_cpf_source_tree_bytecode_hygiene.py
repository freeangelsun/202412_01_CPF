"""Source Tree 안의 모듈을 import 하는 도구가 .pyc 를 남기지 않음을 보장한다.

Python 은 import 한 모듈 옆에 __pycache__/*.pyc 를 만든다. 그 경로가 Source Tree 안이면
clean-source 게이트가 garbage 로 판정하므로, 도구를 한 번 실행할 때마다 검증이 깨진다.
실제로 cpf_nxt3_korean_comment_gate / render_generated_domain_template /
sync-canonical-seed-bundles 가 각각 clean-source 를 실패시켰다.

호출자가 -B 를 붙였는지에 의존하면 같은 결함이 계속 재발한다. Source Tree 경로를 sys.path 에
넣거나 형제 모듈을 import 하는 도구는 스스로 sys.dont_write_bytecode 를 설정해야 한다.
"""
from __future__ import annotations

import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
TOOL_ROOT = ROOT / "cpf-tools"
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated", "__pycache__"}

# Source Tree 를 import 경로로 만드는 신호.
SYS_PATH_INSERT = re.compile(r"sys\.path\.(?:insert|append)\s*\(")
# 표준/서드파티가 아닌 형제 모듈 import 신호(패키지 경로가 없는 단일 이름).
BARE_IMPORT = re.compile(r"^\s*import\s+([a-z_][a-z0-9_]*)\s+as\s+\w+\s*$", re.MULTILINE)
GUARD = re.compile(r"sys\.dont_write_bytecode\s*=\s*True")

# 표준 라이브러리/서드파티는 Source Tree 에 .pyc 를 만들지 않는다.
STANDARD = {
    "os", "sys", "re", "json", "csv", "io", "time", "math", "shutil", "hashlib", "base64",
    "argparse", "subprocess", "sqlite3", "textwrap", "unittest", "pytest", "typing",
    "pathlib", "datetime", "collections", "itertools", "functools", "logging", "zipfile",
    "tempfile", "traceback", "uuid", "random", "string", "socket", "struct", "platform",
    "importlib", "inspect", "glob", "copy", "difflib", "statistics", "warnings", "yaml",
}


@lru_cache(maxsize=1)
def _tools() -> list[Path]:
    files: list[Path] = []
    for path in TOOL_ROOT.rglob("*.py"):
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        files.append(path)
    return files


def _violations() -> list[str]:
    found: list[str] = []
    for path in _tools():
        text = path.read_text(encoding="utf-8", errors="replace")
        if GUARD.search(text):
            continue
        siblings = [name for name in BARE_IMPORT.findall(text) if name not in STANDARD]
        if not siblings:
            continue
        if not SYS_PATH_INSERT.search(text):
            # sys.path 조작 없이 형제 이름을 import 하면 같은 디렉터리에서 실행된 경우이며,
            # 그 역시 Source Tree 에 .pyc 를 남긴다.
            pass
        found.append(f"{path.relative_to(ROOT).as_posix()} imports {sorted(set(siblings))}")
    return sorted(found)


def test_source_tree_importers_disable_bytecode_writes() -> None:
    violations = _violations()
    assert violations == [], (
        "Source Tree 모듈을 import 하는 도구는 sys.dont_write_bytecode = True 를 설정해야 한다"
        f" (.pyc 가 clean-source 게이트를 깨뜨린다): {violations}"
    )


def test_scan_actually_covers_tools() -> None:
    # 대상이 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_tools()) > 200, f"cpf-tools python files not scanned: {len(_tools())}"
