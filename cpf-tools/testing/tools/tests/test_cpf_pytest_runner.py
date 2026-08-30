from __future__ import annotations

import importlib.util
import os
import sys
from pathlib import Path
from unittest.mock import patch


ROOT = Path(__file__).resolve().parents[4]
RUNNER = ROOT / "cpf-tools/testing/tools/run-cpf-pytest.py"
SPEC = importlib.util.spec_from_file_location("cpf_pytest_runner", RUNNER)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(MODULE)


def test_default_command_uses_testing_owned_config_and_tree():
    command = MODULE.build_command([], ROOT)
    assert command == [
        sys.executable,
        "-B",
        "-m",
        "pytest",
        "-c",
        str((ROOT / "cpf-tools/testing/config/pytest.ini").resolve()),
        "cpf-tools",
    ]


def test_runner_forwards_arguments_environment_cwd_and_exit_code():
    original_bytecode_setting = os.environ.get("PYTHONDONTWRITEBYTECODE")
    with patch.object(MODULE.subprocess, "run") as run:
        run.return_value.returncode = 7
        assert MODULE.run_pytest(["--collect-only", "cpf-tools", "-q"], ROOT) == 7

    command = run.call_args.args[0]
    options = run.call_args.kwargs
    assert command[-3:] == ["--collect-only", "cpf-tools", "-q"]
    assert options["cwd"] == ROOT
    assert options["env"]["PYTHONDONTWRITEBYTECODE"] == "1"
    assert os.environ.get("PYTHONDONTWRITEBYTECODE") == original_bytecode_setting
