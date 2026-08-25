from pathlib import Path
import json,hashlib,sys,subprocess
root=Path(__file__).resolve().parents[3]
ev=root/'cpf-docs/deliverables/documentation'
errors=[]
def fail(x): errors.append(x)
# harness validators
for script in ['validate_harness.py','validate_readme.py']:
 p=root/'cpf-docs/governance/documentation-harness/validators'/script
 r=subprocess.run([sys.executable,str(p)],cwd=root,text=True,capture_output=True)
 print(r.stdout,end='')
 if r.returncode!=0: fail(f'{script} exit={r.returncode}: {r.stderr.strip()}')
scope=json.loads((root/'cpf-docs/governance/documentation-harness/scope.json').read_text(encoding='utf-8'))
expected=[]
for a in scope['officialArtifacts']:
 expected.append(a['path'])
 if a.get('pdf'): expected.append(a['pdf'])
for rel in expected:
 if not (root/rel).is_file(): fail('missing official artifact: '+rel)
# no stale documentation visual trees
for rel in ['cpf-docs/assets/architecture','cpf-docs/assets/brand','cpf-docs/assets/readme','cpf-docs/assets/manual','cpf-docs/assets/manuals','cpf-docs/assets/guides']:
 p=root/rel
 if p.exists() and any(x.is_file() for x in p.rglob('*')): fail('stale asset tree not empty: '+rel)
# expected fresh visuals
for name in ['cpf-architecture-map.svg','cpf-batch-control.svg','cpf-canonical-lifecycle.svg','cpf-capability-landscape.svg','cpf-ownership-boundary.svg','cpf-readme-hero.svg','cpf-recovery-state.svg','cpf-topology-parity.svg']:
 if not (root/'cpf-docs/assets/product-docs'/name).is_file(): fail('missing fresh visual: '+name)
# superseded standards must remain absent
for raw in (root/'cpf-docs/governance/documentation-harness/DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
 rel=raw.strip()
 if rel and not rel.startswith('#') and (root/rel).exists(): fail('superseded path exists: '+rel)
# checksum validation
sumfile=ev/'SHA256SUMS.txt'
for raw in sumfile.read_text(encoding='utf-8').splitlines():
 if not raw.strip() or raw.startswith('#'): continue
 expected_hash, rel=raw.split('  ',1)
 p=root/rel
 if not p.is_file(): fail('checksum target missing: '+rel); continue
 h=hashlib.sha256(p.read_bytes()).hexdigest()
 if h!=expected_hash: fail('checksum mismatch: '+rel)
if errors:
 print('DOCUMENTATION_DELIVERY=FAIL')
 for e in errors: print('ERROR:',e)
 sys.exit(1)
print('DOCUMENTATION_DELIVERY=PASS')
print(f'OFFICIAL_ARTIFACT_FILES={len(expected)}')
print('FRESH_VISUALS=8')
