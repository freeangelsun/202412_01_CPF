#!/usr/bin/env python3
"""Run CPF Python tests with the Testing-owned canonical pytest configuration."""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path
from typing import Sequence


REPO_ROOT = Path(__file__).resolve().parents[3]
CONFIG_PATH = Path("cpf-tools/testing/config/pytest.ini")


def build_command(arguments: Sequence[str], repo_root: Path = REPO_ROOT) -> list[str]:
    config = (repo_root / CONFIG_PATH).resolve()
    if not config.is_file():
        raise FileNotFoundError(f"CPF pytest configuration is missing: {config}")
    requested = list(arguments) or ["cpf-tools"]
    return [sys.executable, "-m", "pytest", "-c", str(config), *requested]


def run_pytest(arguments: Sequence[str], repo_root: Path = REPO_ROOT) -> int:
    env = os.environ.copy()
    env["PYTHONDONTWRITEBYTECODE"] = "1"
    completed = subprocess.run(build_command(arguments, repo_root), cwd=repo_root, env=env)
    return completed.returncode


def main(arguments: Sequence[str] | None = None) -> int:
    return run_pytest(sys.argv[1:] if arguments is None else arguments)


if __name__ == "__main__":
    raise SystemExit(main())
