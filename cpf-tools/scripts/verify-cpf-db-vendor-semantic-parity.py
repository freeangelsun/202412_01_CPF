#!/usr/bin/env python3
"""Compatibility entrypoint. This gate is static token parity, not runtime semantic proof."""
import subprocess,sys
from pathlib import Path
script=Path(__file__).with_name('verify-cpf-db-vendor-static-token-parity.py')
print('[CPF][DB][NOTICE] semantic-parity was renamed to static-token-parity; runtime proof requires invoke-cpf-qa34-db-runtime-matrix.ps1.',file=sys.stderr)
raise SystemExit(subprocess.call([sys.executable,str(script),*sys.argv[1:]]))
