#!/usr/bin/env bash
set -euo pipefail
ZIP="${1:?usage: apply-current-harness.sh <final-zip> [expected-sha256]}"; EXPECTED="${2:-}"; ROOT="$(git rev-parse --show-toplevel)"; ACTUAL="$(sha256sum "$ZIP"|awk '{print toupper($1)}')"; VERIFIED="INTERNAL_ONLY"
if [[ -n "$EXPECTED" ]]; then EXPECTED="$(printf '%s' "$EXPECTED"|tr '[:lower:]' '[:upper:]')"; [[ "$EXPECTED" == "$ACTUAL" ]] || { echo "ZIP SHA256 MISMATCH expected=$EXPECTED actual=$ACTUAL" >&2; exit 3; }; VERIFIED="EXPECTED_SHA256"; elif [[ -f "$ZIP.sha256.txt" ]]; then EXPECTED="$(awk '{print toupper($1);exit}' "$ZIP.sha256.txt")"; [[ "$EXPECTED" == "$ACTUAL" ]] || exit 3; VERIFIED="SIDECAR"; fi
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
python3 - "$ZIP" "$TMP" <<'PY'
import sys,zipfile,unicodedata,hashlib
from pathlib import Path
z=Path(sys.argv[1]); out=Path(sys.argv[2]).resolve(); seen=set()
with zipfile.ZipFile(z) as f:
 for i in f.infolist():
  if i.is_dir(): continue
  n=unicodedata.normalize('NFC',i.filename.replace('\\','/'))
  if n in seen: raise SystemExit('duplicate NFC zip path: '+n)
  seen.add(n); p=(out/n).resolve()
  if out not in p.parents: raise SystemExit('unsafe zip path: '+n)
 f.extractall(out)
sums=out/'cpf-docs/deliverables/development-harness/SHA256SUMS.txt'
if not sums.is_file(): raise SystemExit('HARNESS SHA256SUMS MISSING')
for line in sums.read_text(encoding='utf-8').splitlines():
 if not line or line.startswith('#'): continue
 h,rel=line.split(None,1); p=out/rel
 if not p.is_file(): raise SystemExit('PACKAGE FILE MISSING: '+rel)
 if hashlib.sha256(p.read_bytes()).hexdigest().lower()!=h.lower(): raise SystemExit('PACKAGE FILE SHA256 MISMATCH: '+rel)
PY
python3 - "$ROOT" "$TMP" <<'PY'
import sys,shutil
from pathlib import Path
root=Path(sys.argv[1]).resolve(); tmp=Path(sys.argv[2]).resolve()
for p in tmp.rglob('*'):
 if p.is_file():
  d=root/p.relative_to(tmp); d.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(p,d)
PY
cd "$ROOT"; python3 ./cpf-docs/governance/development-harness/validators/run_all_gates.py; echo "CPF_DEVELOPMENT_HARNESS_APPLY=PASS ZIP_SHA256=$ACTUAL ZIP_VERIFY=$VERIFIED DELETE_EXECUTED=0"; git status --short
