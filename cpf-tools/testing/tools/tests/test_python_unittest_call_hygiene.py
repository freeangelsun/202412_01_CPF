from __future__ import annotations

import ast
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]


def test_unittest_assertions_do_not_receive_text_encoding_keywords():
    """Regression gate for the 2026-08-26 locale-fix overreach defect.

    ``encoding=`` belongs on file/subprocess text I/O.  Passing it to unittest
    assertions produces a runtime TypeError and can hide the negative-path test
    that was supposed to validate the real gate.
    """
    violations: list[str] = []
    for path in ROOT.rglob("*.py"):
        rel = path.relative_to(ROOT).as_posix()
        if any(part in {"build", "__pycache__", ".gradle"} for part in path.parts):
            continue
        try:
            tree = ast.parse(path.read_text(encoding="utf-8-sig"), filename=rel)
        except (OSError, UnicodeError, SyntaxError):
            continue
        for node in ast.walk(tree):
            if not isinstance(node, ast.Call):
                continue
            fn = node.func
            name = fn.attr if isinstance(fn, ast.Attribute) else (fn.id if isinstance(fn, ast.Name) else "")
            if name.startswith("assert") and any(keyword.arg == "encoding" for keyword in node.keywords):
                violations.append(f"{rel}:{node.lineno}:{name}")
    assert violations == [], "invalid unittest assertion encoding keyword(s):\n" + "\n".join(violations)
