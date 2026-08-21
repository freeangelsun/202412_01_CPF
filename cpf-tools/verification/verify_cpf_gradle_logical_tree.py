#!/usr/bin/env python3
"""Compatibility entrypoint. The hyphenated verifier is the single canonical implementation."""
from __future__ import annotations
import runpy,sys
from pathlib import Path
canonical=Path(__file__).with_name('verify-cpf-gradle-logical-tree.py')
if not canonical.is_file():
    print('CPF_GRADLE_LOGICAL_TREE=FAIL canonical verifier missing',file=sys.stderr); raise SystemExit(2)
runpy.run_path(str(canonical),run_name='__main__')
