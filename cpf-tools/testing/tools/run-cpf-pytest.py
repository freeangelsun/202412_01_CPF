#!/usr/bin/env python3
"""Run CPF Python tests with the Testing-owned canonical pytest configuration."""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path
from typing import Sequence

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (sys.stdout, sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding="utf-8")
    except (AttributeError, ValueError):
        pass


REPO_ROOT = Path(__file__).resolve().parents[3]
CONFIG_PATH = Path("cpf-tools/testing/config/pytest.ini")


def build_command(arguments: Sequence[str], repo_root: Path = REPO_ROOT) -> list[str]:
    config = (repo_root / CONFIG_PATH).resolve()
    if not config.is_file():
        raise FileNotFoundError(f"CPF pytest configuration is missing: {config}")
    requested = list(arguments) or ["cpf-tools"]
    # Pass -B at interpreter startup as the primary fail-closed bytecode guard. The environment
    # setting below is retained so Python subprocesses spawned by tests inherit the same policy.
    return [sys.executable, "-B", "-m", "pytest", "-c", str(config), *requested]


def run_pytest(arguments: Sequence[str], repo_root: Path = REPO_ROOT) -> int:
    env = os.environ.copy()
    env["PYTHONDONTWRITEBYTECODE"] = "1"
    # CPF 표준 인코딩은 UTF-8 이다. Windows 콘솔 기본 코드페이지(cp949)를 그대로 물려주면
    # pytest 의 한글 단정 메시지가 깨져 실패 원인을 읽을 수 없다. 자식 프로세스에 고정한다.
    env["PYTHONUTF8"] = "1"
    env["PYTHONIOENCODING"] = "utf-8"
    completed = subprocess.run(build_command(arguments, repo_root), cwd=repo_root, env=env)
    return completed.returncode


def main(arguments: Sequence[str] | None = None) -> int:
    return run_pytest(sys.argv[1:] if arguments is None else arguments)


if __name__ == "__main__":
    raise SystemExit(main())
