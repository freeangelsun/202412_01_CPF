#!/usr/bin/env python3
from pathlib import Path
import csv,json,hashlib,sys
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
errs=[]
def rows(p):
 with p.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
mm=rows(H/'CANONICAL_MIGRATION_MAP.csv'); sl=rows(H/'CANONICAL_MIGRATION_SEMANTIC_LEDGER.csv'); dm=rows(H/'DELETE_MANIFEST.csv')
sm={r['old_path']:r for r in sl}; dd={r['path']:r for r in dm}; mp={r['old_path']:r for r in mm}
if len(mp)!=len(mm): errs.append('MIGRATION_DUPLICATE_OLD_PATH')
if set(sm)!=set(mp): errs.append(f'SEMANTIC_LEDGER_SET_DRIFT missing={len(set(mp)-set(sm))} extra={len(set(sm)-set(mp))}')
expected_delete={old for old,r in mp.items() if r.get('delete_eligible')=='true'}
if set(dd)!=expected_delete: errs.append(f'DELETE_MANIFEST_SET_DRIFT missing={len(expected_delete-set(dd))} extra={len(set(dd)-expected_delete)}')
auth=json.loads((H/'contracts/current-authority-registry.json').read_text(encoding='utf-8'))
allowed_harness_delete=set(auth.get('deprecatedCurrentFilesToDelete',[]))
protected_prefixes=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/','cpf-docs/governance/documentation-harness/')
for old,r in mp.items():
 cur=r['new_path']; seen={old}
 while cur in mp:
  if cur in seen:
   errs.append('MIGRATION_TRANSITIVE_CYCLE '+old); break
  seen.add(cur); cur=mp[cur]['new_path']
 np=ROOT/cur
 if cur and not np.is_file(): errs.append('MIGRATION_REPLACEMENT_MISSING '+old+' -> '+cur)
 protected=old.startswith(protected_prefixes)
 if r.get('semantic_status')!='PASS': errs.append('MIGRATION_SEMANTIC_NOT_CLOSED '+old)
 s=sm.get(old,{})
 if s.get('coverage_status')!='PASS': errs.append('SEMANTIC_LEDGER_NOT_CLOSED '+old)
 d=dd.get(old,{})
 if protected:
  if r.get('delete_eligible')!='false' or r.get('delete_after_gate')!='false': errs.append('PROTECTED_MIGRATION_DELETE_ELIGIBLE '+old)
  if s.get('delete_eligible')!='false' or s.get('semantic_basis')!='PROTECTED_PATH_RETAINED_AFTER_SEMANTIC_MIGRATION': errs.append('PROTECTED_SEMANTIC_LEDGER_BAD '+old)
  if old in dd: errs.append('PROTECTED_PATH_PRESENT_IN_DELETE_MANIFEST '+old)
 else:
  if d.get('semantic_status')!='PASS': errs.append('DELETE_SEMANTIC_STATUS_BAD '+old)
  if r.get('delete_eligible')!='true': errs.append('MIGRATION_DELETE_NOT_ELIGIBLE '+old)
  if s.get('delete_eligible')!='true': errs.append('SEMANTIC_LEDGER_DELETE_NOT_ELIGIBLE '+old)
  if d.get('delete_eligible')!='true': errs.append('DELETE_NOT_SEMANTICALLY_ELIGIBLE '+old)
  if d.get('approved')!='true' or d.get('user_approved')!='true': errs.append('DELETE_NOT_USER_APPROVED '+old)
  if d.get('precondition')!='HARNESS_AUTHORITY_AND_MIGRATION_SEMANTIC_GATE_PASS': errs.append('DELETE_BAD_PRECONDITION '+old)
 op=ROOT/old
 if op.is_file() and not protected:
  actual=sha(op)
  if d.get('expected_sha256') not in {actual,'ALREADY_MISSING'}: errs.append('DELETE_EXPECTED_SHA_DRIFT '+old)
  if r.get('observed_current_sha256') not in {actual,'ALREADY_MISSING'}: errs.append('MIGRATION_OBSERVED_SHA_DRIFT '+old)
 elif op.is_file() and protected:
  # Protected retained paths are live product/documentation surfaces. Their current bytes may evolve
  # after semantic migration; provenance preserves the historical SHA but deletion gates must not
  # pin or reject the live protected file.
  pass
 else:
  # Already absent is an acceptable cleaned state; expected_sha256 remains the guarded hash if the legacy file reappears before user deletion.
  pass
 if old.startswith('cpf-docs/governance/development-harness/') and old not in allowed_harness_delete:
  errs.append('DELETE_CURRENT_HARNESS_AUTHORITY_FORBIDDEN '+old)
# Strong top canonical semantic anchor.
anchor=json.loads((H/'contracts/product-contract-integrity.json').read_text(encoding='utf-8'))
legacy='cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'
if legacy not in mp: errs.append('LEGACY_PRODUCT_CANONICAL_NOT_MAPPED')
else:
 op=ROOT/legacy
 if op.is_file() and sha(op)!=anchor['legacyCanonicalSha256']: errs.append('LEGACY_PRODUCT_CANONICAL_SHA_DRIFT')
 if sm.get(legacy,{}).get('semantic_basis')!='PRODUCT_CONTRACT_SEMANTIC_ANCHOR': errs.append('LEGACY_PRODUCT_SEMANTIC_ANCHOR_MISSING')
if errs:
 for e in errs[:300]:print('FAIL',e)
 print('MIGRATION_SEMANTIC_CLOSURE=FAIL ERRORS='+str(len(errs)));raise SystemExit(1)
eligible=len(dm)
retained=sum(1 for r in mm if r.get('delete_eligible')!='true')
print(f'MIGRATION_SEMANTIC_CLOSURE=PASS ROWS={len(mm)} DELETE_ELIGIBLE={eligible} PROTECTED_RETAIN={retained}')
