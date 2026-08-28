from __future__ import annotations
import csv, subprocess, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
TOOL=ROOT/'cpf-tools/runtime/cli/tools/build-cpf-tool-entrypoint-inventory.py'
CAT=ROOT/'cpf-tools/runtime/cli/contracts/cpf-tool-entrypoint-inventory.csv'

def test_entrypoint_inventory_exact_and_no_unclosed_migration_class():
    cp=subprocess.run([sys.executable,str(TOOL),'--root',str(ROOT),'--check'],cwd=ROOT,text=True,encoding='utf-8',capture_output=True)
    assert cp.returncode==0,cp.stdout+cp.stderr
    with CAT.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    assert len(rows)>=900
    assert not [r for r in rows if r['classification'] in {'MIGRATE_TO_CLI','DUPLICATE','DEAD'}]
    assert len({r['entry_id'] for r in rows})==len(rows)

def test_public_cli_aliases_are_consumers_not_engines():
    with CAT.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    public=[r for r in rows if r['public_surface']=='YES']
    assert public
    assert all(r['classification'] in {'THIN_WRAPPER','CLI_CONSUMER'} for r in public)
    assert not [r for r in public if r['classification'] in {'CANONICAL_ENGINE','INTERNAL_ENGINE'}]
