#!/usr/bin/env bash
set -euo pipefail
[[ "${1:-}" == "--apply-approved-manifest" ]] || { echo "DELETE NOT APPROVED: pass --apply-approved-manifest" >&2; exit 2; }
root="$(git rev-parse --show-toplevel)"; cd "$root"; h="cpf-docs/governance/development-harness"
python3 "$h/validators/run_all_gates.py"
python3 "$h/validators/validate_migration_semantic_closure.py"
python3 - "$root" "$h/DELETE_MANIFEST.csv" <<'PY'
import csv,hashlib,json,sys
from pathlib import Path
root=Path(sys.argv[1]).resolve(); mf=Path(sys.argv[2]); h=root/'cpf-docs/governance/development-harness'
auth=json.loads((h/'contracts/current-authority-registry.json').read_text(encoding='utf-8'))
forbidden=set(auth['authoritative'])|{
    'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
    'cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md',
}
protected=(
    'cpf-docs/deliverables/', 'cpf-docs/guides/', 'cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/', 'cpf-docs/governance/documentation-harness/',
)
deleted=missing=selected=0; parents=set()
with mf.open(encoding='utf-8-sig',newline='') as f:
    for r in csv.DictReader(f):
        if not (r['approved']=='true' and r['user_approved']=='true' and r['delete_eligible']=='true' and r['semantic_status']=='PASS' and r['precondition']=='HARNESS_AUTHORITY_AND_MIGRATION_SEMANTIC_GATE_PASS'):
            continue
        selected+=1; rel=r['path'].replace('\\','/').lstrip('/')
        if not rel or '..' in rel.split('/') or rel in forbidden: raise SystemExit('UNSAFE DELETE: '+rel)
        if rel.startswith(protected): raise SystemExit('PROTECTED PATH DELETE FORBIDDEN: '+rel)
        p=(root/rel).resolve()
        if not p.exists(): missing+=1; continue
        if not p.is_file(): raise SystemExit('DIRECTORY DELETE REJECTED: '+rel)
        rep=root/r['replacement_path']
        if not rep.is_file(): raise SystemExit('REPLACEMENT MISSING: '+r['replacement_path'])
        actual=hashlib.sha256(p.read_bytes()).hexdigest()
        if actual.lower()!=r['expected_sha256'].lower(): raise SystemExit(f"DELETE SHA256 DRIFT: {rel} expected={r['expected_sha256']} actual={actual}")
        parents.add(p.parent); p.unlink(); deleted+=1
empty=0
for d in sorted(parents,key=lambda p:len(p.parts),reverse=True):
    while d!=root and d.exists() and not any(d.iterdir()):
        rel=d.relative_to(root).as_posix()+'/'
        if rel.startswith(protected): break
        d.rmdir(); empty+=1; d=d.parent
print(f'CPF_DEV_HARNESS_LEGACY_DELETE=PASS SELECTED={selected} DELETED={deleted} ALREADY_MISSING={missing} EMPTY_DIRS_DELETED={empty}')
PY
python3 "$h/validators/run_all_gates.py"
git status --short
